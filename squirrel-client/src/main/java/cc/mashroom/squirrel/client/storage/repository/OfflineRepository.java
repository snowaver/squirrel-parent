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
package cc.mashroom.squirrel.client.storage.repository;

import  java.io.IOException;
import  java.sql.Timestamp;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.repository.chat.ChatMessageRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupSyncRepository;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  okhttp3.HttpUrl;
import  okhttp3.Request;
import  okhttp3.Response;

@DataSourceBind( name="*"  , table="*" )
@NoArgsConstructor(access=AccessLevel.PRIVATE )
public  class  OfflineRepository  extends  GenericRepository
{
	public  final  static  OfflineRepository  DAO    = new  OfflineRepository();
	
	public  OoIData  attach( SquirrelClient  context )  throws  IOException,NumberFormatException,IllegalArgumentException,IllegalAccessException
	{
		Timestamp  contactLatestModifyTime = ContactRepository.DAO.lookupOne( Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LATEST_MODIFY_TIME  FROM  "+ContactRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		Long  latestChatGroupSyncId = ChatGroupSyncRepository.DAO.lookupOne( Long.class,"SELECT  MAX(SYNC_ID)  FROM  "+ChatGroupSyncRepository.DAO.getDataSourceBind().table() );
		
		Long  latestChatMessageSyncId = ChatMessageRepository.DAO.lookupOne( Long.class,"SELECT  MAX(SYNC_ID)  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		Long  latestChatGroupMessageSyncId = ChatGroupMessageRepository.DAO.lookupOne( Long.class,"SELECT  MAX(SYNC_ID)  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		Service  service=context.getServiceRouteManager().current(Schema.HTTPS);
		
		try(Response  response = context.okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("offline/lookup").addQueryParameter("checkpoints",JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_MESSAGE_CHECK_POINT",latestChatMessageSyncId == null ? 0 : latestChatMessageSyncId).addEntry("GROUP_CHAT_MESSAGE_CHECK_POINT",latestChatGroupMessageSyncId == null ? 0 : latestChatGroupMessageSyncId).addEntry("CONTACT_CHECK_POINT",contactLatestModifyTime == null ? 0 : contactLatestModifyTime.getTime()).addEntry("CHAT_GROUP_CHECK_POINT",latestChatGroupSyncId == null ? 0 : latestChatGroupSyncId))).build()).build()).execute() )
		{
			if( response.code() == 200 )
			{
				OoIData  ooiData = JsonUtils.mapper.readValue( response.body().string(),OoIData.class );
				
				ContactRepository.DAO.recache().attach( ooiData.getContacts() );  ChatGroupRepository.DAO.attach( context,ooiData,false );
				
				ChatMessageRepository.DAO.attach( context,context.getCacheDir(),ooiData.getOfflineChatMessages() );
				
				ChatGroupMessageRepository.DAO.attach( context,context.getCacheDir(),ooiData.getOfflineGroupChatMessages() );    return  ooiData;
			}
			else
			{
				throw  new  IllegalStateException("SQUIRREL-CLIENT:  ** OFFLINE  REPOSITORY **  error  in  retrieving  the  offline  messages" );
			}
		}
	}
}