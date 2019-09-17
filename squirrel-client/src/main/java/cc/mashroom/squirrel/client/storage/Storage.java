/*
 * Copyright 2019 snowaver.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.mashroom.squirrel.client.storage;

import  java.io.File;
import  java.io.InputStream;
import  java.sql.Connection;
import  java.util.Collection;

import  cc.mashroom.db.ConnectionManager;
import  cc.mashroom.db.DataSourceLocator;
import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.db.common.Db.Callback;
import  cc.mashroom.squirrel.client.LifecycleEventDispatcher;
import  cc.mashroom.squirrel.client.LifecycleListener;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.connect.UserMetadata;
import  cc.mashroom.squirrel.client.connect.PacketListener;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.OfflineRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.GroupChatMessageRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.ChatMessageRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.client.storage.repository.user.UserRepository;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRecallPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.IOUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  Storage    implements  PacketListener  //  ,  cc.mashroom.squirrel.client.LifecycleListener
{
	public  void  initialize( final  SquirrelClient  context,boolean  isConnectDataSourceOnly,final  Collection<LifecycleListener>  lifecycleListeners,      File  cacheDir,final  UserMetadata  metadata,final  String  encryptPassword )  throws  Exception
	{
		setContext(context).setCacheDir(cacheDir).setId( metadata.getId() );
		
		ConnectionManager.INSTANCE.setDataSourceLocator( new  DataSourceLocator(){public  String  locate(GenericRepository  repository,DataSourceBind  dataSourceBind){return  "*".equals(dataSourceBind.name()) ? String.valueOf(getId())  : dataSourceBind.name();}} );
		
		ConnectionManager.INSTANCE.addDataSource("org.h2.Driver",String.valueOf(id),"jdbc:h2:"+FileUtils.createFileIfAbsent(new  File(cacheDir,"db/"+StringUtils.leftPad(String.valueOf(id),20,String.valueOf(0))+".db"),null).getPath()+";FILE_LOCK=FS;DB_CLOSE_DELAY=-1;AUTO_RECONNECT=TRUE",null,null,2,4,null,"SELECT  2",true );
		
		//  recache  the  contacts  if  no  connection  or  connecting  by  stored  credential  (only  ID  is  provided  for  username  and  encryped  password).
		if(      isConnectDataSourceOnly )  {        ContactRepository.DAO.recache(); }
		else
		{
			try( InputStream  is =  getClass().getResourceAsStream( "/squirrel.ddl" ) )
			{
				Db.tx( String.valueOf(id),java.sql.Connection.TRANSACTION_SERIALIZABLE,new  Callback(){public  Object  execute( cc.mashroom.db.connection.Connection  connection )  throws  Throwable{ connection.runScripts( IOUtils.toString(is,"UTF-8") );  UserRepository.DAO.upsert( new  User(metadata.getId(),null,metadata.getUsername(),encryptPassword,metadata.getName(),metadata.getNickname()) );  LifecycleEventDispatcher.onReceivedOfflineData(lifecycleListeners,OfflineRepository.DAO.attach(context));  return  true; }});
			}
		}
	}
		
	@Getter( value=AccessLevel.PROTECTED )
	@Setter
	@Accessors( chain=true )
	protected  long  id;
	
	public  void  stop()
	{
		ConnectionManager.INSTANCE.stop();
	}
	
	public  final  static  Storage  INSTANCE = new  Storage();
	
	@Getter( value=AccessLevel.PROTECTED )
	@Setter
	@Accessors( chain=true )
	protected  SquirrelClient     context;
	@Getter( value=AccessLevel.PROTECTED )
	@Setter
	@Accessors( chain=true )
	protected  File      cacheDir;

	public  boolean  onBeforeSend(   final    Packet  packet )        throws  Throwable
	{
		if( packet instanceof ChatPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ChatMessageRepository.DAO.upsert(context,cacheDir,ObjectUtils.cast(packet,ChatPacket.class),   TransportState.SENDING);}} );
		}
		else
		if( packet instanceof GroupChatPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  GroupChatMessageRepository.DAO.upsert(context,cacheDir,ObjectUtils.cast(packet,GroupChatPacket.class),   TransportState.SENDING);}} );
		}
		
		return  true;
	}
	
	public  void  onSent( final  Packet  packet,final  TransportState  transportState )
	{
		if( packet instanceof ChatPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ChatMessageRepository.DAO.upsert( context,cacheDir,ObjectUtils.cast(packet,ChatPacket.class),transportState );}} );
		}
		else
		if( packet instanceof      ChatRecallPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ChatMessageRepository.DAO.remove( ObjectUtils.cast(packet,ChatRecallPacket.class) );}} );
		}
		else
		if( packet instanceof CloseCallPacket )
		{
			
		}
		else
		if( packet instanceof GroupChatPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  GroupChatMessageRepository.DAO.upsert( context,cacheDir,ObjectUtils.cast(packet,GroupChatPacket.class),    TransportState.SENT );}} );
		}
	}
	
	public  void  onReceived(  final  Packet  packet )
	{
		if( packet instanceof GroupChatPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  GroupChatMessageRepository.DAO.upsert( context,cacheDir,ObjectUtils.cast(packet,GroupChatPacket.class),TransportState.RECEIVED );}} );
		}
		else
		if( packet instanceof GroupChatEventPacket   )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ChatGroupRepository.DAO.attach( context,JsonUtils.fromJson(ObjectUtils.cast(packet,GroupChatEventPacket.class).getAttatchmentsOriginal(),OoIData.class) );}} );
		}
		else
		if( packet instanceof ChatPacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ChatMessageRepository.DAO.upsert( context,cacheDir,ObjectUtils.cast(packet,ChatPacket.class),TransportState.RECEIVED );}} );
		}
		else
		if( packet instanceof CloseCallPacket )
		{
			
		}
		else
		if( packet instanceof ChatRecallPacket       )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ChatMessageRepository.DAO.remove( ObjectUtils.cast(packet,ChatRecallPacket.class) );}} );
		}
		else
		if( packet instanceof SubscribePacket )
		{
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ContactRepository.DAO.upsert( JsonUtils.fromJson(ObjectUtils.cast(packet,SubscribePacket.class     ).getSubscriberProfileOriginal(),Contact.class),true );}} );
		}
		else
		if( packet instanceof SubscribeAckPacket     )
		{
			final  Contact  contact = JsonUtils.fromJson( ObjectUtils.cast(packet,SubscribeAckPacket.class).getSubscribeeProfileOriginal(),Contact.class );
			
			Db.tx( String.valueOf(id),Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable{return  ContactRepository.DAO.upsert( ContactRepository.DAO.getContactDirect().get(contact.getId()).clone().setLastModifyTime(contact.getLastModifyTime()).setSubscribeStatus(contact.getSubscribeStatus()),true );}} );
		}
	}
}