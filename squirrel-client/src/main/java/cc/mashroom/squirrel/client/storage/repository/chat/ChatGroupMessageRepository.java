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
import  cc.mashroom.squirrel.client.HttpOpsHandlerAdapter;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatGroupMessage;
import  cc.mashroom.squirrel.client.storage.repository.OfflineRepository;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(name="*",table="chat_group_message",primaryKeys="ID")
@NoArgsConstructor( access =    AccessLevel.PRIVATE )
public  class  ChatGroupMessageRepository  extends  MessageRepository
{
	public  boolean  attach( HttpOpsHandlerAdapter  context,File  cacheDir,List<ChatGroupMessage>  messages )  throws  IOException,IllegalArgumentException,IllegalAccessException
	{
		if( !messages.isEmpty() )
		{
			super.cacheAudioFiles( context,messages.toArray(new  ChatGroupMessage[messages.size()]) );
			
			upsert(   messages );
			
			ChatGroupMessage  gm = messages.get( messages.size()-1 );
			
			NewsProfileRepository.DAO.insert(new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{gm.getGroupId(),new  Timestamp(gm.getId()),PAIPPacketType.GROUP_CHAT.getValue(),gm.getContactId(),ChatContentType.valueOf(gm.getContentType()) == ChatContentType.WORDS ? new  String(gm.getContent()) : ChatContentType.valueOf(gm.getContentType()).getPlaceholder(),gm.getGroupId(),PAIPPacketType.GROUP_CHAT.getValue()} );
		}
		
		return  true;
	}
	
	public  final  static  ChatGroupMessageRepository  DAO      = new    ChatGroupMessageRepository();
	
	public  int  upsert( HttpOpsHandlerAdapter  context,File  cacheDir,GroupChatPacket  packet,TransportState  transportState )  throws  IOException
	{
		if( transportState==TransportState.RECEIVED )
		{
			if( packet.getSyncId() > ObjectUtils.getOrDefaultIfNull(ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table(),new  Object[]{}),0L)+1 )
			{
				OfflineRepository.DAO.attach(context, false,false   ,false,true );return  1;
			}
			
			if( packet.getContentType()    == ChatContentType.AUDIO )
			{
				super.cacheAudioFiles(     context,packet.getMd5() );
			}
			
			NewsProfileRepository.DAO.insert(new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{packet.getGroupId(),new  Timestamp(packet.getId()),PAIPPacketType.GROUP_CHAT.getValue(),packet.getContactId(),packet.getContentType() == ChatContentType.WORDS ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),packet.getGroupId(),PAIPPacketType.GROUP_CHAT.getValue()} );
		}
		else
		{
			NewsProfileRepository.DAO.insert(new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{packet.getGroupId(),new  Timestamp(packet.getId()),PAIPPacketType.GROUP_CHAT.getValue(),packet.getContactId(),packet.getContentType() == ChatContentType.WORDS ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),0} );
		}
		
		return  super.insert(new  Reference<Object>(),"MERGE  INTO  "+super.getDataSourceBind().table()+"  (ID,CREATE_TIME,SYNC_ID,GROUP_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?,?,?)",new  Object[]{packet.getId(),new  Timestamp(packet.getId()),packet.getSyncId(),packet.getGroupId(),packet.getContactId(),packet.getMd5(),packet.getContentType().getValue(),new  String(packet.getContent()),transportState.getValue()} );
	}
}