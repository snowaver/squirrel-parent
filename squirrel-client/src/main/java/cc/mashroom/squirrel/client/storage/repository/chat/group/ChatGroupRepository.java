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
package cc.mashroom.squirrel.client.storage.repository.chat.group;

import  java.sql.Timestamp;
import  java.util.LinkedList;
import java.util.List;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupCheckpoint;
import  cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(name="*",table="chat_group",primaryKeys="ID" )
@NoArgsConstructor(  access = AccessLevel.PRIVATE )
public  class  ChatGroupRepository  extends  RepositorySupport
{
	public  final  static  ChatGroupRepository  DAO =  new  ChatGroupRepository();
	
	public  synchronized boolean  attach( SquirrelClient  context,OoIData  ooiData, boolean  validateCheckpoints )  throws  IllegalArgumentException,IllegalAccessException
	{
		long  nowMillis  =  DateTime.now(DateTimeZone.UTC).getMillis()-1;
		
		List<ChatGroupCheckpoint>    checkpoints  = new  LinkedList<ChatGroupCheckpoint>();
		
		super.upsert(    ooiData.getChatGroups() );
		
		for( ChatGroup   chatGroup : ooiData.getChatGroups() )
		{
			if( validateCheckpoints )
			{
				Timestamp  latestModifyTime = ChatGroupRepository.DAO.lookupOne( Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LATEST_MODIFY_TIME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroup.getId()} );
				
				if( chatGroup.getCheckPointTime().getTime() != latestModifyTime.getTime() )
				{
					checkpoints.add( new  ChatGroupCheckpoint( chatGroup.getId(), latestModifyTime ) );  continue;
				}
			}
			
			if( !        chatGroup.getIsDeleted() )
			{
				NewsProfileRepository.DAO.insert( new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{chatGroup.getId(),new  Timestamp(nowMillis = nowMillis+1),PAIPPacketType.GROUP_CHAT.getValue(),null,null,0} );
			}
			else
			{
				NewsProfileRepository.DAO.update( "DELETE  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{chatGroup.getId(),PAIPPacketType.GROUP_CHAT.getValue()} );
			}
		}
		
		ChatGroupUserRepository.DAO.upsert(ooiData.getChatGroupUsers() );     return  true;
	}
}