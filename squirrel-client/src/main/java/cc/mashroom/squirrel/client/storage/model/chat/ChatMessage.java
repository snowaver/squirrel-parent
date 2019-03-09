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
import  java.util.LinkedList;
import  java.util.List;
import java.util.concurrent.TimeUnit;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.db.util.BatchPrediction;
import  cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.AbstractModel;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRetractPacket;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.Map;
import  okhttp3.HttpUrl;
import  okhttp3.OkHttpClient;
import  okhttp3.Request;

@DataSourceBind( name="*",table="chat_message",primaryKeys="ID" )

public  class  ChatMessage  extends  AbstractModel< ChatMessage >
{
	public  final  static  ChatMessage  dao = new  ChatMessage();
	
	public  int  remove(    ChatRetractPacket  packet )
	{
		return  ChatMessage.dao.update( "DELETE  FROM  "+ChatMessage.dao.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CONTACT_ID = ?",new  Object[]{packet.getChatPacketId(),packet.getContactId()} );
	}
	
	public  boolean  attach( SquirrelClient  context,File  cacheDir,List<Map<String,Object>>  messages )  throws  IOException
	{
		if( !messages.isEmpty() )
		{
			for( Map<String,Object>  offlineMessage  : messages )
			{
				offlineMessage.addEntry("ID",new  Long(offlineMessage.get("ID").toString())).addEntry("CREATE_TIME",new  Timestamp(DateTime.parse(offlineMessage.get("CREATE_TIME").toString()).withZone(DateTimeZone.UTC).getMillis())).addEntry( "CONTACT_ID",new  Long(offlineMessage.get("CONTACT_ID").toString()) );
				
				if( ChatContentType.valueOf(offlineMessage.getInteger("CONTENT_TYPE"))   == ChatContentType.AUDIO )
				{
					FileUtils.createFileIfAbsent( new  File(cacheDir,"file/"+offlineMessage.getString("MD5")),new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).build().newCall(new  Request.Builder().addHeader("SECRET_KEY",context.getUserMetadata().getString("SECRET_KEY")).get().url(new  HttpUrl.Builder().scheme("https").host(context.getHost()).port(context.getHttpPort()).addPathSegments("file/"+offlineMessage.getString("MD5")).build()).build()).execute().body().bytes() );
				}
			}
			
			ChatMessage.dao.insert( new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",ConnectionUtils.prepare(messages,new  BatchPrediction(){ public  Object[]  predicate(Map<String,Object>  offlineMessage){ return  new  Object[]{offlineMessage.getLong("ID"),offlineMessage.get("CREATE_TIME"),offlineMessage.getLong("CONTACT_ID"),offlineMessage.getString("MD5"),offlineMessage.getInteger("CONTENT_TYPE"),offlineMessage.getString("CONTENT"),TransportState.RECEIVED.getValue()}; } }).toArray(new  Object[messages.size()][]) );
			
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{messages.get(messages.size()-1).getLong("CONTACT_ID"),messages.get(messages.size()-1).get("CREATE_TIME"),PAIPPacketType.CHAT.getValue(),messages.get(messages.size()-1).getLong("CONTACT_ID"),ChatContentType.valueOf(messages.get(messages.size()-1).getInteger("CONTENT_TYPE")) == ChatContentType.WORDS ? new  String(messages.get(messages.size()-1).getString("CONTENT")) : "&"+StringUtils.leftPad(Integer.toHexString(PAIPPacketType.CHAT.getValue()),2,"0")+StringUtils.leftPad(Integer.toHexString(messages.get(messages.size()-1).getInteger("CONTENT_TYPE")),2,"0")+";",messages.get(messages.size()-1).getLong("CONTACT_ID"),PAIPPacketType.CHAT.getValue()} );
		}
		
		return  true;
	}
		
	public  int  upsert( SquirrelClient  context,File  cacheDir,ChatPacket  packet,TransportState  transportState )  throws  IOException
	{
		if( transportState == TransportState.RECEIVED )
		{
			if( packet.getContentType()== ChatContentType.AUDIO )
			{
				FileUtils.createFileIfAbsent( new  File(cacheDir,"file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()),new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).build().newCall(new  Request.Builder().addHeader("SECRET_KEY",context.getUserMetadata().getString("SECRET_KEY")).get().url(new  HttpUrl.Builder().scheme("https").host(context.getHost()).port(context.getHttpPort()).addPathSegments("file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()).build()).build()).execute().body().bytes() );
			}

			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,IFNULL((SELECT  BADGE_COUNT  FROM  news_profile  WHERE  ID = ?  AND  PACKET_TYPE = ?),0)+1)",new  Object[]{packet.getContactId(),new  Timestamp(packet.getId()),PAIPPacketType.CHAT.getValue(),packet.getContactId(),packet.getContentType() == ChatContentType.WORDS ? new  String(packet.getContent()) : "&"+StringUtils.leftPad(Integer.toHexString(PAIPPacketType.CHAT.getValue()),2,"0")+StringUtils.leftPad(Integer.toHexString(packet.getContentType().getValue()),2,"0")+";",packet.getContactId(),PAIPPacketType.CHAT.getValue()} );
		}
		else
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{packet.getContactId(),new  Timestamp(packet.getId()),PAIPPacketType.CHAT.getValue(),packet.getContactId(),packet.getContentType() == ChatContentType.WORDS ? new  String(packet.getContent()) : "&"+StringUtils.leftPad(Integer.toHexString(PAIPPacketType.CHAT.getValue()),2,"0")+StringUtils.leftPad(Integer.toHexString(packet.getContentType().getValue()),2,"0")+";",0} );
		}
		
		return  ChatMessage.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{packet.getId(),new  Timestamp(packet.getId()),packet.getContactId(),packet.getMd5(),packet.getContentType().getValue(),new  String(packet.getContent()),transportState.getValue()} );
	}
}