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
import  java.util.List;
import  java.util.concurrent.TimeUnit;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatMessage;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupUserRepository;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.util.DateUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  okhttp3.HttpUrl;
import  okhttp3.OkHttpClient;
import  okhttp3.Request;
import  okhttp3.Response;

@DataSourceBind(     name="squirrel",table="*" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  DefaultRepository  extends  GenericRepository
{
	public  final  static  GenericRepository  DAO = new  GenericRepository();
	
	public  Map<String,List<Map<String,Object>>>  attach( SquirrelClient  context )  throws  IOException,NumberFormatException,IllegalArgumentException,IllegalAccessException
	{
		Timestamp  chatGroupLatestModifyTime = ChatGroupRepository.DAO.lookupOne( Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		Timestamp  chatGroupUserLatestModifyTime = ChatGroupUserRepository.DAO.lookupOne( Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		Timestamp  contactLatestModifyTime = ContactRepository.DAO.lookupOne( Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+ContactRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		try( Response  response = new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).build().newCall(new  Request.Builder().addHeader("SECRET_KEY",context.getUserMetadata().getString("SECRET_KEY")).url(new  HttpUrl.Builder().scheme("https").host(context.getHost()).port(context.getHttpPort()).addPathSegments("offline/search").addQueryParameter("action",String.valueOf(0)).addQueryParameter("keyword",String.valueOf(context.getUserMetadata().getLong("ID"))).addQueryParameter("extras",JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CONTACTS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",DateUtils.toString(contactLatestModifyTime,"yyyy-MM-dd'T'HH:mm:ss'Z'","2000-01-01T00:00:00.000Z")).addEntry("CHAT_GROUPS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",DateUtils.toString(chatGroupLatestModifyTime,"yyyy-MM-dd'T'HH:mm:ss'Z'","2000-01-01T00:00:00.000Z")).addEntry("CHAT_GROUP_USERS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",DateUtils.toString(chatGroupUserLatestModifyTime,"yyyy-MM-dd'T'HH:mm:ss'Z'","2000-01-01T00:00:00.000Z"))))))).build()).build()).execute() )
		{
			if( response.code() == 200 )
			{
				Map<String,List<Map<String,Object>>>  offline    = JsonUtils.mapper.readValue( response.body().string(),new  TypeReference<HashMap<String,List<HashMap<String,Object>>>>(){} );
				
				ContactRepository.DAO.recache().attach(  offline.get("CONTACTS") );  ChatGroupRepository.DAO.attach( context,offline );
				
				ChatMessageRepository.DAO.attach( context,context.getCacheDir(),offline.get("OFFLINE_MESSAGES") );  return  offline;
			}
		}
		
		return  null;
	}
}