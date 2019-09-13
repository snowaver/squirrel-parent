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
import cc.mashroom.router.Schema;
import cc.mashroom.router.Service;
import cc.mashroom.router.ServiceRouteManager;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.chat.GroupChatMessage;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  okhttp3.HttpUrl;
import  okhttp3.Request;

@DataSourceBind(name="*",table="group_chat_message",primaryKeys="ID")
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  GroupChatMessageRepository  extends  RepositorySupport
{
	public  final  static  GroupChatMessageRepository  DAO = new  GroupChatMessageRepository();
	
	public  boolean  attach( SquirrelClient  context,File  cacheDir,List<GroupChatMessage>  messages )  throws  IOException,IllegalArgumentException,IllegalAccessException
	{
		if( !messages.isEmpty() )
		{
			Service  service = ServiceRouteManager.INSTANCE.current( Schema.HTTPS );
			
			for( GroupChatMessage  message : messages )
			{
				if( ChatContentType.valueOf(message.getContentType()) ==ChatContentType.AUDIO )
				{
					FileUtils.createFileIfAbsent( new  File(cacheDir,"file/"+message.getMd5()),context.okhttpClient(5,5,1200).newCall(new  Request.Builder().get().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("file/"+message.getMd5()).build()).build()).execute().body().bytes() );
				}
				
				message.setTransportState( TransportState.RECEIVED.getValue()   ).setIsLocal( false );
			}
			
			upsert(   messages );
			
			GroupChatMessage  gm = messages.get( messages.size()-1 );
			
			NewsProfileRepository.DAO.insert(new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{gm.getGroupId(),new  Timestamp(gm.getId()),PAIPPacketType.GROUP_CHAT.getValue(),gm.getContactId(),ChatContentType.valueOf(gm.getContentType()).getPlaceholder() == null ? new  String(gm.getContent()) : ChatContentType.valueOf(gm.getContentType()).getPlaceholder(),gm.getGroupId(),PAIPPacketType.GROUP_CHAT.getValue()} );
		}
		
		return  true;
	}
	
	public  int  upsert( SquirrelClient  context,File  cacheDir,GroupChatPacket  packet,TransportState  transportState )  throws  IOException
	{
		if( transportState == TransportState.RECEIVED )
		{
			Service  service = ServiceRouteManager.INSTANCE.current( Schema.HTTPS );
			
			if( packet.getContentType()    == ChatContentType.AUDIO )
			{
				FileUtils.createFileIfAbsent(new  File(cacheDir,"file/"+packet.getMd5()),context.okhttpClient(5,5,1200).newCall(new  Request.Builder().get().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("file/"+packet.getMd5()).build()).build()).execute().body().bytes() );
			}

			NewsProfileRepository.DAO.insert(new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{packet.getGroupId(),new  Timestamp(packet.getId()),PAIPPacketType.GROUP_CHAT.getValue(),packet.getContactId(),packet.getContentType().getPlaceholder() == null ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),packet.getGroupId(),PAIPPacketType.GROUP_CHAT.getValue()} );
		}
		else
		{
			NewsProfileRepository.DAO.insert(new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{packet.getGroupId(),new  Timestamp(packet.getId()),PAIPPacketType.GROUP_CHAT.getValue(),packet.getContactId(),packet.getContentType().getPlaceholder() == null ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),0} );
		}
		//  transport  state  should  be  TransportState.SENT  by  default  while  no  qos  adapting  to  the  group  chat  message.
		return  super.insert(new  Reference<Object>(),"MERGE  INTO  "+super.getDataSourceBind().table()+"  (ID,CREATE_TIME,GROUP_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[]{packet.getId(),new  Timestamp(packet.getId()),packet.getGroupId(),packet.getContactId(),packet.getMd5(),packet.getContentType().getValue(),new  String(packet.getContent()),transportState.getValue()} );
	}
}