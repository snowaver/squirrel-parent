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
package cc.mashroom.squirrel.client.storage.model.chat.group;

import  java.sql.Timestamp;
import  java.util.List;
import java.util.concurrent.TimeUnit;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.AbstractModel;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  okhttp3.HttpUrl;
import  okhttp3.OkHttpClient;
import  okhttp3.Request;
import  okhttp3.Response;

@DataSourceBind(name="*",table="chat_group",primaryKeys="ID")

public  class  ChatGroup  extends  AbstractModel< ChatGroup >
{
	public  final  static  ChatGroup  dao = new  ChatGroup();
	
	public  boolean  attach( SquirrelClient  context )  throws  Exception
	{
		ChatGroup  chatGroupLatestModifyTime = ChatGroup.dao.getOne( "SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+ChatGroup.dao.getDataSourceBind().table(),new  Object[]{} );
		
		ChatGroupUser  chatGroupUserLatestModifyTime = ChatGroupUser.dao.getOne( "SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+ChatGroupUser.dao.getDataSourceBind().table(),new  Object[]{} );
		
		try( Response  response = new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).build().newCall(new  Request.Builder().addHeader("SECRET_KEY",context.getUserMetadata().getString("SECRET_KEY")).url(new  HttpUrl.Builder().scheme("https").host(context.getHost()).port(context.getHttpPort()).addPathSegments("chat/group/search").addQueryParameter("action",String.valueOf(0)).addQueryParameter("keyword",String.valueOf(context.getUserMetadata().getLong("ID"))).addQueryParameter("extras",JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_GROUPS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",chatGroupLatestModifyTime.get("LAST_MODIFY_TIME") == null ? "2000-01-01T00:00:00.000Z" : chatGroupLatestModifyTime.get("LAST_MODIFY_TIME"))).addEntry("CHAT_GROUP_USERS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",chatGroupLatestModifyTime.get("LAST_MODIFY_TIME") == null ? "2000-01-01T00:00:00.000Z" : chatGroupUserLatestModifyTime.get("LAST_MODIFY_TIME"))))).build()).build()).execute() )
		{
			if( response.code() == 200 )
			{
				attach( ObjectUtils.cast(JsonUtils.mapper.readValue(response.body().string(),new  TypeReference<Map<String,List<Map<String,Object>>>>(){}),new  TypeReference<Map<String,List<Map<String,Object>>>>(){}) );
			
				return  true;
			}
		}
		
		return  false;
	}
	
	public  boolean  attach( Map<String,List<Map<String,Object>>>  addedChatGroups )
	{
		for( Map<String,Object>  chatGroup : addedChatGroups.get("CHAT_GROUPS") )
		{
			chatGroup.addEntry("ID",new  Long(chatGroup.get("ID").toString())).addEntry("CREATE_TIME",new  Timestamp(DateTime.parse(chatGroup.get("CREATE_TIME").toString()).withZone(DateTimeZone.UTC).getMillis())).addEntry( "LAST_MODIFY_TIME",new  Timestamp(DateTime.parse(chatGroup.get("LAST_MODIFY_TIME").toString()).withZone(DateTimeZone.UTC).getMillis()) );
		}
		
		super.upsert( addedChatGroups.get( "CHAT_GROUPS" ) );
		
		for( Map<String,Object>  chatGroupUser : addedChatGroups.get("CHAT_GROUP_USERS" ) )
		{
			chatGroupUser.addEntry("ID",new  Long(chatGroupUser.get("ID").toString())).addEntry("CREATE_TIME",new  Timestamp(DateTime.parse(chatGroupUser.get("CREATE_TIME").toString()).withZone(DateTimeZone.UTC).getMillis())).addEntry("LAST_MODIFY_TIME",new  Timestamp(DateTime.parse(chatGroupUser.get("LAST_MODIFY_TIME").toString()).withZone(DateTimeZone.UTC).getMillis())).addEntry("CHAT_GROUP_ID",new  Long(chatGroupUser.get("CHAT_GROUP_ID").toString())).addEntry( "CONTACT_ID",new  Long(chatGroupUser.get("CONTACT_ID").toString()) );
		}
		
		ChatGroupUser.dao.upsert( addedChatGroups.get("CHAT_GROUP_USERS") );  return  true; 
	}
}