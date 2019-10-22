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

import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatGroupMessage;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatMessage;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.util.FileUtils;
import  okhttp3.HttpUrl;
import  okhttp3.Request;

public  class  MessageRepository  extends  RepositorySupport
{
	protected  void  cacheAudioFiles(SquirrelClient  context ,ChatMessage     ...  messages )
	{
		for( ChatMessage       message : messages )
		{
			if( ChatContentType.valueOf(message.getContentType())  == ChatContentType.AUDIO )
			{
				cacheAudioFiles( context,message.getMd5() );
			}
		}
	}
	
	protected  void  cacheAudioFiles(SquirrelClient  context ,ChatGroupMessage...  messages )
	{
		for( ChatGroupMessage  message : messages )
		{
			if( ChatContentType.valueOf(message.getContentType())  == ChatContentType.AUDIO )
			{
				cacheAudioFiles( context,message.getMd5() );
			}
		}
	}
	
	protected  void  cacheAudioFiles(SquirrelClient  context ,String...  md5s )
	{
		Service  service = context.getServiceRouteManager().current( Schema.HTTPS );
		
		for( String  md5 : md5s )
		{
			try
			{
				FileUtils.createFileIfAbsent( new  File(context.getCacheDir(),"file/"+md5),context.okhttpClient(5,5,1200).newCall(new  Request.Builder().get().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("file/"+md5).build()).build()).execute().body().bytes() );
			}
			catch( Throwable  e )
			{
				System.err.println( String.format("SQUIRREL-CLIENT:  ** MESSAGE  REPOSITORY **  error  while  pulling  the  audio  file  named  %s.",md5) );
			}
		}
	}
}