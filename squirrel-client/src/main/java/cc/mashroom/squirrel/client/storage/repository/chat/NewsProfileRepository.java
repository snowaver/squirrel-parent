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

import  java.sql.Timestamp;
import  java.util.Collection;
import  java.util.LinkedList;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.HttpOpsHandlerAdapter;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(name="*",table="news_profile",primaryKeys="ID" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  NewsProfileRepository  extends  RepositorySupport
{
	public  int  clearBadgeCount(     long  id,int  packetType )
	{
		return  super.update( "UPDATE  "+super.getDataSourceBind().table()+"  SET  BADGE_COUNT = 0  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{id,packetType} );
	}
	
	public  void   upsert( HttpOpsHandlerAdapter  context,Collection  <ChatGroupUser>  chatGroupUsers )
	{
		for(    ChatGroupUser  chatGroupUser :  chatGroupUsers )
		{
			if( context.userMetadata().getId() == chatGroupUser.getContactId() && chatGroupUser.getIsDeleted() )
			{
				NewsProfileRepository.DAO.update( "DELETE  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{chatGroupUser.getChatGroupId(),PAIPPacketType.GROUP_CHAT.getValue()} );
			}
		}
	}
	
	public  final  static  NewsProfileRepository  DAO = new  NewsProfileRepository();
	
	public  void   upsert( Collection<ChatGroup>    chatGroups )
	{
		long  nowMillis    =DateTime.now(DateTimeZone.UTC).getMillis() -1;
		
		for( ChatGroup  chatGroup : chatGroups )
		{
			NewsProfile newsProfile = NewsProfileRepository.DAO.lookupOne( NewsProfile.class,"SELECT  *  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{chatGroup.getId(),PAIPPacketType.GROUP_CHAT.getValue()} );
			
			if( chatGroup.getIsDeleted() )
			{
				NewsProfileRepository.DAO.update( "DELETE  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{chatGroup.getId(),PAIPPacketType.GROUP_CHAT.getValue()} );
			}
			else
			if( !       chatGroup.getIsDeleted() &&  newsProfile == null )
			{
				NewsProfileRepository.DAO.insert( new  LinkedList<Reference<Object>>(),"INSERT  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{chatGroup.getId(),new  Timestamp(nowMillis = nowMillis+1),PAIPPacketType.GROUP_CHAT.getValue(),null,null,0} );
			}
		}
	}
}