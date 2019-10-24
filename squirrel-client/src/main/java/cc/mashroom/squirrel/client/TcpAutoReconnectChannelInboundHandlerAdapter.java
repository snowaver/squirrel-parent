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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import  java.util.ArrayList;
import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;
import  java.util.concurrent.ExecutorService;
import  java.util.concurrent.Executors;
import  java.util.concurrent.TimeUnit;

import  javax.net.ssl.SSLContext;

import  org.joda.time.DateTime;

import  com.google.common.collect.Lists;

import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.storage.Storage;
import  cc.mashroom.squirrel.common.Tracer;
import  cc.mashroom.squirrel.paip.codec.PAIPDecoder;
import  cc.mashroom.squirrel.paip.codec.PAIPExternalDecoder;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.util.CollectionUtils;
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
import  lombok.NonNull;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

public  class  TcpAutoReconnectChannelInboundHandlerAdapter<T extends TcpAutoReconnectChannelInboundHandlerAdapter<?>>  extends  RoutableChannelInboundHandlerAdapter
{
	public  final  static  SSLContext  SSL_CONTEXT= SecureUtils.getSSLContext( "/squirrel.cer" );
	
	private  ExecutorService  multipartsSendPool  = Executors.newFixedThreadPool( 2,new  DefaultThreadFactory("MULTIPART-SENDER",false,1) );
	
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
	private  EventLoopGroup  eventLooperGroup     = new  NioEventLoopGroup( Integer.parseInt(System.getProperty("eventlopper.size","2") ) );
	private  InboundHandler  qosHandler  =new     InboundHandler();
	private  LinkedMap<String  , PAIPExternalDecoder>     externalDecoders = new  LinkedMap<String,PAIPExternalDecoder>();
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  ConnectState  connectState  =ConnectState.NONE;
	
	protected  Storage     storage =   new Storage();
	
	private  List<PacketListener>  packetListeners  = new  CopyOnWriteArrayList<PacketListener>(       Lists.newArrayList( this.storage ) );
		
	public  T  removePacketListener(     @NonNull   PacketListener  listener )
	{
		CollectionUtils.remove( this.packetListeners,   listener );
		
		return  (T)  this;
	}
	
	public  List<PacketListener> getPacketListeners()
	{
		return  new  ArrayList<PacketListener>(  packetListeners );
	}
	
	public  T  addPacketListener(        @NonNull   PacketListener  listener )
	{
		CollectionUtils.addIfAbsent( packetListeners,   listener );
		
		return  (T)  this;
	}
	
	public  void  channelInactive(  ChannelHandlerContext  context)  throws  Exception
	{
		super.channelInactive(  context );
		
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS") +"  CHANNEL.LEFT:\tNONE" );
		
		context.close(  );
		
		this.onConnectStateChanged( this.connectState   =  ConnectState.DISCONNECTED);
		
		this.eventLooperGroup.execute(new  Runnable()   {  public  void  run()  {connect(); }} );
	}
	
	protected  void  onConnectStateChanged(     ConnectState   connectState  )
	{
		
	}
	
	protected  void  connect( String  id,String  accessKey )
	{
		setId(id).setAccessKey( accessKey).setAuthenticated( true ).connect();
	}
	
	public  void  channelRead(ChannelHandlerContext  context,Object   object )  throws  Exception
	{
		this.qosHandler.channelRead(context,object );
	}
	
	private  synchronized  void  connect()
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.CONN:\tCONNECT.STATE="+connectState.name()+",AUTHENTICATED="+authenticated+",BOOTSTRAP="+bootstrap+",CHANNEL="+channel );
		
        try
        {
        	if( connectState != ConnectState.CONNECTING && connectState != ConnectState.CONNECTED && authenticated && (this.channel == null || !channel.isActive()) )
        	{
        		this.onConnectStateChanged( connectState =  ConnectState.CONNECTING );
        		{
        			bootstrap = bootstrap != null ?   bootstrap : new  Bootstrap().group(eventLooperGroup).channel( NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5*1000).handler( new  ClientChannelInitailizer(this) );
        			
        			Service  service = this.serviceRouteManager.current( Schema.TCP );
        			
        			setChannel(bootstrap.connect(service.getHost(),service.getPort()).sync().channel()).send( new  ConnectPacket(id,accessKey.getBytes(),keepalive),10,TimeUnit.SECONDS );
        			
        			for( PAIPExternalDecoder  externalDecoder : this.externalDecoders.values()  )
        			{
        				ObjectUtils.cast(   this.channel.pipeline().get("decoder"),PAIPDecoder.class).addExternalDecoder( externalDecoder );
        			}
        		}
        	}
        }
        catch(  Throwable  e )
        {
        	if( e instanceof SocketTimeoutException || e instanceof ConnectException )
			{
				serviceRouteManager.tryNext(   Schema.TCP );
			}
        	
        	Tracer.trace( e );
        	
        	this.onConnectStateChanged( this.connectState=ConnectState.DISCONNECTED );
		}
	}
	@SneakyThrows
	public  void     release()
	{
		eventLooperGroup.shutdownGracefully().sync();
		
		multipartsSendPool.shutdown();
	}
	/**
	 *  close  the  channel  if  open.
	 */
	protected  synchronized  void  close()
	{
		if( channel.isOpen() )  this.channel.close();
	}
	
	public  void  send(Packet  packet)
	{
		this.send(  packet , 10 , TimeUnit.SECONDS );
	}
	
	protected  void    reset()
	{
		setId(null).setAuthenticated(false).setAuthenticated(false).setConnectState(  ConnectState.NONE );
	}
	/**
	 *  write  the  packet  to  the  active  and  writable  channel  if  prepared  (false  if  return  false  or  exception  when  files  uploading  in  onbeforesend  method)  before  send.  waiting  for  pending  ack  packet  about  the  packet  sent  after  writen  if  ack  level  is  one.
	 */
	private    void  syncsend( Packet  packet,long  writeTimeout, TimeUnit  timeunit )
	{
		boolean   isPreparedBeforeSend       = false;
		
		try
		{
			isPreparedBeforeSend = PacketEventDispatcher.onBeforeSend( packetListeners ,packet );
		}
		catch(  Throwable  e )
		{
			Tracer.trace(  e);
		}
		finally
		{
			if( isPreparedBeforeSend && channel != null  && channel.isActive()   && channel.isWritable() )
			{
				PacketEventDispatcher.onSent(packetListeners,packet,    TransportState.SENDING );
				
				channel.writeAndFlush(packet).addListener(new  DefaultGenericFutureListener(this,qosHandler,packet,writeTimeout,timeunit) );
			}
			else
			{
				PacketEventDispatcher.onSent(packetListeners,packet,TransportState.SEND_FAILED );
			}
		}
	}
	/**
	 *  an  extension  for  decoding  customized  packet  (packet  type  value  MUST  be  greater  than  1024  and  less  than  65536).  the  external  decoder  finally  is  added  to  paip  decoder.
	 */
	public  void  addExternalDecorder(  PAIPExternalDecoder  externalDecoder )
	{
		this.externalDecoders.put(      externalDecoder.getClass().getName() , externalDecoder );
		
		if(  channel != null )  ObjectUtils.cast(channel.pipeline().get("decoder"),PAIPDecoder.class).addExternalDecoder( externalDecoder );
	}
	/**
	 *  multipart  uploading  is  a  heavily  time-consuming  io  operation,  so  seperate  it  from  other  data  packet  by  a  new  pool  named  MULTIPART-PACKET-SEND-THREAD  to  avoid  blocking  data  interaction  by  multipart  uploading  operations.
	 */
	public  void  send(  final  Packet  packet,final  long  timeout,     final  TimeUnit timeoutTimeUnit )
	{
		((packet instanceof ChatPacket && ObjectUtils.cast(packet,ChatPacket.class).getContentType() != ChatContentType.WORDS) || (packet instanceof GroupChatPacket && ObjectUtils.cast(packet,GroupChatPacket.class).getContentType() != ChatContentType.WORDS) ? multipartsSendPool : eventLooperGroup).execute( new  Runnable() { public  void  run(){syncsend(packet,timeout,timeoutTimeUnit);} } );
	}
}