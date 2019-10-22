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
package cc.mashroom.squirrel.client.storage.repository.chat;

import  java.io.File;
import  java.io.IOException;
import  java.sql.Timestamp;
import  java.util.List;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatMessage;
import  cc.mashroom.squirrel.client.storage.repository.OfflineRepository;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRecallPacket;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  okhttp3.HttpUrl;
import  okhttp3.Request;

@DataSourceBind( name="*",table="chat_message",primaryKeys="ID" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  MessageRepository   extends  RepositorySupport
{
	public  void  cache()
	{
		
	}
	
	public  int  remove(     ChatRecallPacket  packet )
	{
		return  super.update( "DELETE  FROM  "+MessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CONTACT_ID = ?",new  Object[]{packet.getChatPacketId(),packet.getContactId()} );
	}
	
	public  boolean  attach( SquirrelClient  context,File  cacheDir,List<ChatMessage>  messages )  throws  IOException,IllegalArgumentException,IllegalAccessException
	{
		if( !messages.isEmpty() )
		{
			Service   service=context.getServiceRouteManager().current(Schema.HTTPS);
			
			for(      ChatMessage  message : messages )
			{
				if( ChatContentType.valueOf(message.setIsLocal( false ).getContentType())== ChatContentType.AUDIO )
				{
					FileUtils.createFileIfAbsent( new  File(cacheDir,"file/"+message.getMd5()),context.okhttpClient(5,5,1200).newCall(new  Request.Builder().get().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("file/"+message.getMd5()).build()).build()).execute().body().bytes() );
				}
			}
			
			upsert(   messages );
			
			NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{messages.get(messages.size()-1).getContactId(),messages.get(messages.size()-1).getCreateTime(),PAIPPacketType.CHAT.getValue(),messages.get(messages.size()-1).getContactId(),ChatContentType.valueOf(messages.get(messages.size()-1).getContentType()) == ChatContentType.WORDS ? new  String(messages.get(messages.size()-1).getContent()) : ChatContentType.valueOf(messages.get(messages.size()-1).getContentType()).getPlaceholder(),messages.get(messages.size()-1).getContactId(),PAIPPacketType.CHAT.getValue()} );
		}
		
		return  true;
	}
		
	public  int  upsert( SquirrelClient  context,File  cacheDir,ChatPacket  packet,TransportState  transportState )  throws  IOException
	{
		if( transportState == TransportState.RECEIVED )
		{
			Service   service=context.getServiceRouteManager().current(Schema.HTTPS);
			
			Long  latestChatMessageSyncId = ObjectUtils.getOrDefaultIfNull( MessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  FROM  "+MessageRepository.DAO.getDataSourceBind().table(),new  Object[]{}),0L );
			
			if( packet.getSyncId() != latestChatMessageSyncId+1 )
			{
				OfflineRepository.DAO.attach( context , false, false, true , false );  return  1;
			}
			
			if( packet.getContentType()== ChatContentType.AUDIO )
			{
				FileUtils.createFileIfAbsent( new  File(cacheDir,"file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()),context.okhttpClient(5,5,1200).newCall(new  Request.Builder().get().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()).build()).build()).execute().body().bytes() );
			}

			NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{packet.getContactId(),new  Timestamp(packet.getId()),PAIPPacketType.CHAT.getValue(),packet.getContactId(),packet.getContentType() == ChatContentType.WORDS ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),packet.getContactId(),PAIPPacketType.CHAT.getValue()} );
		}
		else
		{
			NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{packet.getContactId(),new  Timestamp(packet.getId()),PAIPPacketType.CHAT.getValue(),packet.getContactId(),packet.getContentType() == ChatContentType.WORDS ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),0} );
		}
		
		return  super.insert( new  Reference<Object>(),"MERGE  INTO  "+super.getDataSourceBind().table()+"  (ID,CREATE_TIME,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[]{packet.getId(),new  Timestamp(packet.getId()),packet.getSyncId(),packet.getContactId(),packet.getMd5(),packet.getContentType().getValue(),new  String(packet.getContent()),transportState.getValue()} );
	}
}