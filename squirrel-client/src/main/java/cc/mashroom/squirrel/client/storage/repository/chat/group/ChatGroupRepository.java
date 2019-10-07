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

import  java.io.IOException;
import  java.util.Collection;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupSync;
import  cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  okhttp3.HttpUrl;
import  okhttp3.Request;
import  okhttp3.Response;

@DataSourceBind(name="*",table="chat_group",primaryKeys="ID" )
@NoArgsConstructor(  access = AccessLevel.PRIVATE )
public  class  ChatGroupRepository  extends  RepositorySupport
{
	public  final  static  ChatGroupRepository  DAO = new  ChatGroupRepository();
	
	public  synchronized boolean  attach( SquirrelClient  context,OoIData  ooiData,boolean  validateChatGroupSyncId )  throws  IllegalArgumentException,IllegalAccessException,IOException
	{
		Long  nativeLatestSyncId = super.lookupOne(Long.class,   "SELECT  MAX(SYNC_ID)  FROM  "+super.getDataSourceBind().table() );
		
		if( validateChatGroupSyncId && ooiData.getChatGroupSyncId() !=nativeLatestSyncId  + 1 )
		{
			Service  service =context.getServiceRouteManager().current(  Schema.HTTPS );
			
			try( Response  response = context.okhttpClient(5,5,10).newCall(new  Request.Builder().url(new   HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("offline/lookup").addQueryParameter("checkpoints",JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_GROUP_SYNC_ID",nativeLatestSyncId))).build()).build()).execute() )
			{
				return   response.code()   == 200 ? this.attach(context,JsonUtils.mapper.readValue(response.body().string(),OoIData.class),false) : false;
			}
		}
		
		NewsProfileRepository.DAO.upsert( ObjectUtils.cast(ooiData.getChatGroups(),new  TypeReference<Collection<ChatGroup>>(){}) );
		
		ChatGroupSyncRepository.DAO.insert( new  ChatGroupSync(ooiData.getChatGroupSyncId()) );
		
		super.upsert(    ooiData.getChatGroups() );  ChatGroupUserRepository.DAO.upsert( ooiData.getChatGroupUsers() );return  true;
	}
}