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
package cc.mashroom.squirrel.client.storage.model.chat;

import  java.io.File;
import  java.io.IOException;
import  java.sql.Timestamp;
import  java.util.concurrent.TimeUnit;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.AbstractModel;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.Reference;
import  okhttp3.HttpUrl;
import  okhttp3.OkHttpClient;
import  okhttp3.Request;

@DataSourceBind( name="*" , table="group_chat_message" , primaryKeys="ID" )

public  class  GroupChatMessage  extends  AbstractModel< GroupChatMessage >
{
	public  final  static  GroupChatMessage  dao = new  GroupChatMessage();
	
	public  int  upsert( SquirrelClient  context,File  cacheDir,GroupChatPacket  packet,TransportState  transportState )  throws  IOException
	{
		if( transportState  == TransportState.RECEIVED )
		{
			if( packet.getContentType() == ChatContentType.AUDIO )
			{
				FileUtils.createFileIfAbsent( new  File(cacheDir,"file/"+packet.getMd5()),new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).build().newCall(new  Request.Builder().addHeader("SECRET_KEY",context.getUserMetadata().getString("SECRET_KEY")).get().url(new  HttpUrl.Builder().scheme("https").host(context.getHost()).port(context.getHttpPort()).addPathSegments("file/"+packet.getMd5()).build()).build()).execute().body().bytes() );
			}

			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{packet.getGroupId(),new  Timestamp(packet.getId()),PAIPPacketType.GROUP_CHAT.getValue(),packet.getContactId(),packet.getContentType().getPlaceholder() == null ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),packet.getGroupId(),PAIPPacketType.GROUP_CHAT.getValue()} );
		}
		else
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{packet.getGroupId(),new  Timestamp(packet.getId()),PAIPPacketType.GROUP_CHAT.getValue(),packet.getContactId(),packet.getContentType().getPlaceholder() == null ? new  String(packet.getContent()) : packet.getContentType().getPlaceholder(),0} );
		}
		//  transport  state  should  be  TransportState.SENT  by  default  while  no  qos  adapting  to  the  group  chat  message.
		return  GroupChatMessage.dao.insert(new  Reference<Object>(),"MERGE  INTO  "+GroupChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,GROUP_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[]{packet.getId(),new  Timestamp(packet.getId()),packet.getGroupId(),packet.getContactId(),packet.getMd5(),packet.getContentType().getValue(),new  String(packet.getContent()),transportState.getValue()} );
	}
}