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

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import cc.mashroom.squirrel.client.SquirrelClient;
import cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import cc.mashroom.squirrel.paip.message.PAIPPacketType;
import cc.mashroom.util.JsonUtils;
import cc.mashroom.util.NoopHostnameVerifier;
import cc.mashroom.util.NoopX509TrustManager;
import cc.mashroom.util.ObjectUtils;
import cc.mashroom.util.Reference;
import cc.mashroom.util.collection.map.HashMap;
import cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@DataSourceBind(name="*",table="chat_group",primaryKeys="ID" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  ChatGroupRepository  extends  GenericRepository
{
	public  final  static  ChatGroupRepository  DAO = new  ChatGroupRepository();
	
	public  boolean  attach( SquirrelClient  context )  throws  Exception
	{
		Map<String,Object>  chatGroupLatestModifyTime = ChatGroupRepository.DAO.lookupOne( Map.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+  ChatGroupRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		Map<String,Object>  chatGroupUserLatestModifyTime = ChatGroupUserRepository.DAO.lookupOne( Map.class,"SELECT  MAX(LAST_MODIFY_TIME)  AS  LAST_MODIFY_TIME  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table(),new  Object[]{} );
		
		try( Response  response = new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).build().newCall(new  Request.Builder().addHeader("SECRET_KEY",context.getUserMetadata().getString("SECRET_KEY")).url(new  HttpUrl.Builder().scheme("https").host(context.getHost()).port(context.getHttpPort()).addPathSegments("chat/group/search").addQueryParameter("action",String.valueOf(0)).addQueryParameter("keyword",String.valueOf(context.getUserMetadata().getLong("ID"))).addQueryParameter("extras",JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_GROUPS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",chatGroupLatestModifyTime.get("LAST_MODIFY_TIME") == null ? "2000-01-01T00:00:00.000Z" : chatGroupLatestModifyTime.get("LAST_MODIFY_TIME"))).addEntry("CHAT_GROUP_USERS",new  HashMap<String,Object>().addEntry("LAST_MODIFY_TIME",chatGroupLatestModifyTime.get("LAST_MODIFY_TIME") == null ? "2000-01-01T00:00:00.000Z" : chatGroupUserLatestModifyTime.get("LAST_MODIFY_TIME"))))).build()).build()).execute() )
		{
			if( response.code() == 200 )
			{
				return  attach( context,ObjectUtils.cast(JsonUtils.mapper.readValue(response.body().string(),new  TypeReference<Map<String,List<Map<String,Object>>>>(){}),new  TypeReference<Map<String,List<Map<String,Object>>>>(){}) );
			}
		}
		
		return  false;
	}
	
	public  boolean  attach( SquirrelClient  context,Map<String,List<Map<String,Object>>>  attachedChatGroups )
	{
		for( Map<String,Object>  chatGroup : attachedChatGroups.get("CHAT_GROUPS") )
		{
			chatGroup.addEntry("ID",new  Long(chatGroup.get("ID").toString())).valuesToTimestamp( "CREATE_TIME","LAST_MODIFY_TIME" );
		}
		
		long  nowMillis = DateTime.now(DateTimeZone.UTC).getMillis() - 1;
		
		super.upsert( attachedChatGroups.get("CHAT_GROUPS") );
		
		for(  Map<String,Object>  chatGroupUser  : attachedChatGroups.get( "CHAT_GROUP_USERS" ) )
		{
			chatGroupUser.addEntry("ID",new  Long(chatGroupUser.get("ID").toString())).addEntry("CONTACT_ID",new  Long(chatGroupUser.get("CONTACT_ID").toString())).addEntry("CHAT_GROUP_ID",new  Long(chatGroupUser.get("CHAT_GROUP_ID").toString())).valuesToTimestamp( "CREATE_TIME","LAST_MODIFY_TIME" );
			
			if( chatGroupUser.getLong("CONTACT_ID")    == context.getUserMetadata().getLong("ID").longValue() )
			{
				if( ! chatGroupUser.getBoolean("IS_DELETED") )
				{
					NewsProfile.dao.insert( new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{new  Long(chatGroupUser.get("CHAT_GROUP_ID").toString()),new  Timestamp(nowMillis = nowMillis+1),PAIPPacketType.GROUP_CHAT.getValue(),null,null,0} );
				}
				else
				{
					NewsProfile.dao.update( "DELETE  FROM  "+NewsProfile.dao.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{chatGroupUser.getLong("CHAT_GROUP_ID"),PAIPPacketType.GROUP_CHAT.getValue()} );
				}
			}
		}
		
		ChatGroupUser.dao.upsert(      attachedChatGroups.get("CHAT_GROUP_USERS") );return  true;
	}
}