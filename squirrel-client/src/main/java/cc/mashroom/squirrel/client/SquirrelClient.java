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
package cc.mashroom.squirrel.client;

import  java.io.File;
import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  io.netty.channel.ChannelHandler.Sharable;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.NonNull;
import  cc.mashroom.router.ServiceListRequestEventListener;
import  cc.mashroom.router.ServiceListRequestStrategy;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.util.FileUtils;

@Sharable

public  class  SquirrelClient  extends  HttpOpsHandlerAdapter
{
	@Override
	public  void  route( @NonNull  final  ServiceListRequestStrategy  strategy , @NonNull  final  ServiceListRequestEventListener  listener )
	{
		this.lifecycleThreadPool.execute( new  Runnable(){public  void  run(){ SquirrelClient.super.route(strategy,listener); }} );
	}
	
	public  SquirrelClient( Object  context, File  cacheDir )
	{
		super();
		
		this.setContext(context).setCacheDir( FileUtils.createDirectoryIfAbsent(cacheDir) );
	}
	private  ThreadPoolExecutor  lifecycleThreadPool = new  ThreadPoolExecutor( 1,1,2,TimeUnit.MINUTES,new  LinkedBlockingQueue<Runnable>(),new  DefaultThreadFactory("LIFECYCLE-TREAD-POOL") );
	@Override
	public  void  newCall( final  long  contactId, final  CallContentType  callContentType )
	{
		this.lifecycleThreadPool.execute( new  Runnable(){public  void  run(){ SquirrelClient.super.newCall(contactId,callContentType); }} );
	}
	@Override
	public  SquirrelClient  connect( final  String  username,final  boolean  isPasswordEncrypted,final  String  password,final  Double  longitude,final  Double  latitude,  final  String  mac )
	{
		this.lifecycleThreadPool.execute( new  Runnable(){public  void  run(){ SquirrelClient.super.connect(username,isPasswordEncrypted,password,longitude,latitude,mac); }} );   return  this;
	}
	@Override
	public  void  disconnect()
	{
		this.lifecycleThreadPool.execute( new  Runnable(){public  void  run(){ SquirrelClient.super.disconnect(); }} );
	}
	@Override
	public  void     release()
	{
		super.release();
		
		this.lifecycleThreadPool.shutdown();
	}
}