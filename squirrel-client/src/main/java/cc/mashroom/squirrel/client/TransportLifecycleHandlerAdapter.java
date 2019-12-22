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

import  java.util.concurrent.ExecutionException;
import  java.util.concurrent.ExecutorService;
import  java.util.concurrent.Executors;
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.TimeoutException;

import  javax.net.ssl.SSLContext;

import  org.joda.time.DateTime;

import cc.mashroom.router.Service;
import  cc.mashroom.squirrel.client.event.LifecycleEventDispatcher;
import  cc.mashroom.squirrel.client.event.PacketEventDispatcher;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.transport.ConnectState;
import cc.mashroom.squirrel.transport.TransportAndConnectivityGuarantorHandlerAdapter;
import  cc.mashroom.squirrel.transport.TransportConfig;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.SecureUtils;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

public  class  TransportLifecycleHandlerAdapter<T extends TransportLifecycleHandlerAdapter<?>>  extends           TransportAndConnectivityGuarantorHandlerAdapter
{
	public  final  static  SSLContext      SSL_CONTEXT  = SecureUtils.getSSLContext( "/squirrel.cer" );
	
	@Getter( value = AccessLevel.PUBLIC    )
	private  int  keepalive = 600;
	@Setter( value = AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  String  accessKey;
	private  ExecutorService  packetWritingPool = Executors.newFixedThreadPool( 2,new  DefaultThreadFactory("MULTIPART-SENDER",false,1) );
	@Getter
	private  PacketEventDispatcher  packetEventDispatcher  = new  PacketEventDispatcher();
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  String  id;
	@Getter
	protected  LifecycleEventDispatcher  lifecycleEventDispatcher  =  new   LifecycleEventDispatcher();
	@Override
	public  void  channelRead( ChannelHandlerContext  context,Object  packet )
	{
		super.channelRead( context,packet );
		
		if( packet instanceof DisconnectAckPacket || ObjectUtils.cast(packet,DisconnectAckPacket.class).getReason() == DisconnectAckPacket.REASON_REMOTE_SIGNIN )
		{
			this.lifecycleEventDispatcher.onLogoutComplete(  200  , 2 );
			
			this.reset();super.disconnect();
		}
		
		this.handler.channelRead(  context,packet );
	}
	
	protected  synchronized  void  connect(String id,String  secretKey )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.CONN:\tCONNECT.STATE = "+ connectState.name() );
		
		super.connect( new  TransportConfig(SSL_CONTEXT,service().getHost(),Integer.parseInt(System.getProperty("squirrel.im.server.port","8012")),5* 1000,this.keepalive,new  Object[]{id,secretKey}) );
			
		super.checkConnectivity(  10 );
	}
	@Setter( value = AccessLevel.PROTECTED )
	private  InboundHandler  handler = new  InboundHandler();
	@Override
	protected  void  onConnectStateChanged( ConnectState  connectState )
	{
		super.onConnectStateChanged( connectState );
	}
	
	protected  void   reset()
	{
		this.setId(null).setAccessKey(null);
	}
	@Override
	public  void    release()
	{
		super.release();
		
		this.packetWritingPool.shutdown( /*STDN*/ );
	}
	
	public  void  send(     Packet  packet )
	{
		this.send( packet , 10 , TimeUnit.SECONDS );
	}
	@SneakyThrows( value={InterruptedException.class,ExecutionException.class,TimeoutException.class} )
	@Override
	protected  boolean  authenticate( Object  ...  objects  )
	{
		return  ObjectUtils.cast(super.write(new  ConnectPacket(objects[0].toString(),this.accessKey.getBytes(),this.keepalive)).get(5,TimeUnit.SECONDS),ConnectAckPacket.class).getResponseCode() == ConnectAckPacket.CONNECTION_ACCEPTED;
	}
	/**
	 *  write  the  packet  to  the  non-null,  opened  and  active  channel  and  await  until  timeout.  may  failed  if  exception  in  {@link  PacketEventDispatcher#onBeforeSend(Packet)},  {@link  PacketEventDispatcher#onSent(Packet,TransportState)}  or  timeout  in  writing.
	 */
	public  void  send(Packet  packet,long  writeTimeout, TimeUnit  writeTimeoutTimeUnit )
	{
		try
		{
			this.packetEventDispatcher.onBeforeSend(packet );
			
			this.packetEventDispatcher.onSent(packet,TransportState.SENDING );
			
			super.write(packet).get(writeTimeout,writeTimeoutTimeUnit );  this.packetEventDispatcher.onSent( packet,TransportState.SENT );
		}
		catch( Throwable  e )
		{
			packetEventDispatcher.onSent( packet,TransportState.SEND_FAILED );
		}
	}
	
	public  Service service()
	{
		return  null;
	}
	
	public  void  send( ExecutorService  executor,final  Packet  packet,final  long  writeTimeout, final  TimeUnit  writeTimeoutTimeUnit )
	{
		(executor != null ? executor : this.packetWritingPool).execute( new  Runnable(){public  void  run(){send(packet,writeTimeout,writeTimeoutTimeUnit);} } );
	}
}