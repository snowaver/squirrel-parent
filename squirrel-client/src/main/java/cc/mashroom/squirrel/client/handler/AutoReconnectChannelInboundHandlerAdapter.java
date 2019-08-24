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
package cc.mashroom.squirrel.client.handler;

import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  javax.net.ssl.SSLContext;

import  org.joda.time.DateTime;

import  cc.mashroom.squirrel.client.ClientChannelInitailizer;
import  cc.mashroom.squirrel.client.QosHandler;
import  cc.mashroom.squirrel.client.connect.ClientConnectEventDispatcher;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.connect.DefaultGenericFutureListener;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.common.Tracer;
import  cc.mashroom.squirrel.paip.codec.PAIPDecoder;
import  cc.mashroom.squirrel.paip.codec.PAIPExternalDecoder;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.SecureUtils;
import  cc.mashroom.util.collection.map.LinkedMap;
import  io.netty.bootstrap.Bootstrap;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelOption;
import  io.netty.channel.EventLoopGroup;
import  io.netty.channel.nio.NioEventLoopGroup;
import  io.netty.channel.socket.nio.NioSocketChannel;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

public  class  AutoReconnectChannelInboundHandlerAdapter     extends  RoutableChannelInboundHandlerAdapter
{
	public  final  static  SSLContext  SSL_CONTEXT= SecureUtils.getSSLContext( "/squirrel.cer" );
	
	private  ThreadPoolExecutor  multipartsSendPool = new  ThreadPoolExecutor( 4,4,2,TimeUnit.MINUTES,new  LinkedBlockingQueue<Runnable>(),new  DefaultThreadFactory("MULTIPART-PACKET-SEND-THREAD",false,1) );
	/*
	public  final  static  SSLSocketFactory  SSL_SOCKET_FACTORY = SecureUtils.getSSLSocketFactory( "/squirrel.cer" );
	*/
	@Getter( value=   AccessLevel.PUBLIC )
	private  int  keepalive = 600;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  String  accessKey;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  String  id;
	private  Bootstrap  bootstrap;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  Channel   channel;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	@Getter
	private  boolean  authenticated=false;
	private  EventLoopGroup  eventLooperGroup    = new  NioEventLoopGroup( Integer.parseInt( System.getProperty("eventlopper.size","2") ) );
	private  QosHandler  qosHandler     = new  QosHandler();
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  ConnectState  connectState = ConnectState.NONE;

	private  LinkedMap<String , PAIPExternalDecoder>  externalDecoders   = new  LinkedMap<String , PAIPExternalDecoder>();
	
	public  void  channelInactive( ChannelHandlerContext  context )  throws  Exception
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS") +"  CHANNEL.LEFT:\tNONE" );
		
		super.channelInactive(  context );
		
		context.close();
		
		eventLooperGroup.execute( new  Runnable(){public  void  run() {connect();}} );
		
		ClientConnectEventDispatcher.connectStateChanged( this.connectState = ConnectState.DISCONNECTED );
	}
		
	public  void  channelRead( ChannelHandlerContext  context,Object  object )  throws  Exception
	{
		this.qosHandler.channelRead(context,object );
	}
	
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  throwable ) throws  Exception
	{
		super.exceptionCaught( context , throwable );
		
		this.connect( );
	}
	
	private  synchronized  void  connect()
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.CONN:\tCONNECT.STATE="+connectState.name()+",AUTHENTICATED="+authenticated+",BOOTSTRAP="+bootstrap+",CHANNEL="+channel );
		
        try
        {
        	if( connectState != ConnectState.CONNECTING && connectState != ConnectState.CONNECTED && authenticated && (channel == null || !channel.isActive()) )
        	{
        		ClientConnectEventDispatcher.connectStateChanged( connectState= ConnectState.CONNECTING );
        		{
        			bootstrap = bootstrap != null ? bootstrap : new  Bootstrap().group(eventLooperGroup).channel(NioSocketChannel.class)/*.option(ChannelOption.SO_KEEPALIVE,true)*/.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5*1000).handler( new  ClientChannelInitailizer(this) );
        			
        			setChannel(bootstrap.connect(host,port).sync().channel()).send( new  ConnectPacket(id,accessKey.getBytes(),keepalive),10,TimeUnit.SECONDS );
        			
        			for(  PAIPExternalDecoder  externalDecoder : this.externalDecoders.values() )
        			{
        				ObjectUtils.cast(   this.channel.pipeline().get("decoder"),PAIPDecoder.class).addExternalDecoder( externalDecoder );
        			}
        		}
        	}
        }
        catch(  Throwable  e )
        {
        	Tracer.trace( e );
        	
			ClientConnectEventDispatcher.connectStateChanged(  connectState = ConnectState.DISCONNECTED );
		}
	}
	
	public  void  addExternalDecorder(  PAIPExternalDecoder  externalDecoder )
	{
		this.externalDecoders.put(      externalDecoder.getClass().getName() , externalDecoder );
		
		if( channel  != null )
		{
			ObjectUtils.cast(channel.pipeline().get("decoder") , PAIPDecoder.class).addExternalDecoder( externalDecoder );
		}
	}
	
	@SneakyThrows
	public  void  close()
	{
		disconnect();
		
		eventLooperGroup.shutdownGracefully().sync();
		
		multipartsSendPool.shutdown();
	}
	
	protected  void  connect( String  id,String  accessKey )
	{
		setId( id).setAccessKey( accessKey).setAuthenticated(true ).connect();
	}
	
	public  void  asynchronousSend(  Packet  packet )
	{
		this.asynchronousSend( packet,10,TimeUnit.SECONDS );
	}
	
	public  void  send(Packet packet )
	{
		this.send(  packet , 10 , TimeUnit.SECONDS );
	}
	
	public  void  disconnect()
	{
		/*
		send( new  DisconnectPacket(),10,TimeUnit.SECONDS );
		*/
	}
	
	public  void  clear()
	{
		setId(null).setAuthenticated(false).setAuthenticated(false).setConnectState(  ConnectState.NONE );
	}

	@SneakyThrows
	public  void  send(  Packet  packet , long  timeout , TimeUnit  timeunit )
	{
		boolean isSendPrepared =false;
		
		try
		{
			isSendPrepared     =PacketEventDispatcher.onBeforeSend( packet  );
		}
		catch(  Throwable  e )
		{
			Tracer.trace( e );
		}
		finally
		{
			boolean  isChannelAvailable =   channel != null && channel.isActive() && channel.isWritable();
			
			PacketEventDispatcher.onSent( packet,isSendPrepared && isChannelAvailable ? TransportState.SENDING:TransportState.SEND_FAILED );
			
			if( isSendPrepared       && isChannelAvailable )
			{
				this.channel.writeAndFlush(packet).addListener( new  DefaultGenericFutureListener( qosHandler, packet, timeout,timeunit ) );
			}
		}
	}
	/**
	 *  multipart  uploading  is  a  heavily  time-consuming  io  operation,  so  seperate  it  from  other  data  packet  by  a  new  pool  named  MULTIPART-PACKET-SEND-THREAD  to  avoid  blocking  data  interaction  by  multipart  uploading  operations.
	 */
	public  void  asynchronousSend( final  Packet  packet,final  long  timeout,final  TimeUnit  timeunit )
	{
		if( (packet instanceof ChatPacket && ObjectUtils.cast(packet,ChatPacket.class).getContentType() != ChatContentType.WORDS) || (packet instanceof GroupChatPacket && ObjectUtils.cast(packet,GroupChatPacket.class).getContentType() != ChatContentType.WORDS) )
		{
			multipartsSendPool.execute( new  Runnable(){ public  void  run(){ send(packet, timeout, timeunit); } } );
		}
		else
		{
			this.eventLooperGroup.execute( new  Runnable(){ public  void  run(){ send( packet, timeout, timeunit ); } } );
		}
	}
}