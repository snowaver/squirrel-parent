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
package cc.mashroom.squirrel.transport;

import  java.util.concurrent.TimeUnit;

import  javax.net.ssl.SSLContext;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  io.netty.bootstrap.Bootstrap;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelFuture;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import  io.netty.channel.ChannelOption;
import  io.netty.channel.EventLoopGroup;
import  io.netty.channel.nio.NioEventLoopGroup;
import  io.netty.channel.socket.nio.NioSocketChannel;
import  lombok.NonNull;

public  class  TransportHandlerAdapter  extends  ChannelInboundHandlerAdapter
{
	protected  EventLoopGroup  eventLooperGroup = new  NioEventLoopGroup( Integer.parseInt( System.getProperty( "netty.eventloopergroup.thread.count",  "2" ) ) );
	
	protected  Bootstrap  bootstrap;
	
	protected  Channel  channel;
	
	protected  Map<Long,TransportFuture<PendingAckPacket<?>>>  transportFutures = new  ConcurrentHashMap<Long,TransportFuture<PendingAckPacket<?>>>();
	
	@Override
	public  void  channelRead(ChannelHandlerContext  context,Object  packet )  throws  Exception
	{
		super.channelRead( context,packet );
		
		if( packet instanceof      PendingAckPacket<?> )
		{
			TransportFuture<PendingAckPacket<?>>  transportFuture = this.transportFutures.remove( ObjectUtils.cast(packet,PendingAckPacket.class).getPacketId() );
			
			if(    transportFuture != null )
			{
				transportFuture.setPendingAckPacket(ObjectUtils.cast( packet,PendingAckPacket.class)).done( true );
			}
		}
	}
	
	protected  TransportFuture<PendingAckPacket<?>>  send(@NonNull  Packet<?>  pacekt,long  timeout,@NonNull  TimeUnit  timeoutTimeUnit )
	{
		if( channel != null  && channel.isActive() &&  channel.isWritable() )
		{
			ChannelFuture  channelFuture = channel.writeAndFlush(   pacekt );
			
			try
			{
				if( channelFuture.await().isSuccess()  )
				{
					TransportFuture<PendingAckPacket<?>>  transportFuture=new  TransportFuture<PendingAckPacket<?>>( pacekt );
				
					if( pacekt.getHeader().getAckLevel()  == 0 )
					{
						transportFuture.done(    true );
					}
					else
					{
					transportFutures.put( pacekt.getId() ,transportFuture  );
					}
					
					return  transportFuture;
				}
			}
			catch(Throwable  e )
			{
				e.printStackTrace();
			}
		}
		
		return  new TransportFuture<PendingAckPacket<?>>(pacekt).done(false);
	}
	
	protected  void  connect( @NonNull  String  host, int  port,  int  connectTimeoutMillis, int  keepaliveSeconds, @NonNull  SSLContext  sslContext )
	{
		bootstrap = bootstrap != null ? bootstrap : new  Bootstrap().group(eventLooperGroup).channel( NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,connectTimeoutMillis).handler( new  ChannelInitailizer(this,sslContext,keepaliveSeconds) );
	
		try
		{
			this.channel= this.bootstrap.connect(host,port).sync().channel();
		}
		catch(Throwable  itrpe )
		{
			throw  new  RuntimeException( String.format("SQUIRREL-TRANSPORT:  ** TRANSPORT  HANDLER  ADAPTER **  error  while  connecting  to  the  server  ( %s:%d )  with  parameters  ( connect  timeout  millis:  %d  and  keepalive  seconds:  %d ).",host,port,connectTimeoutMillis,keepaliveSeconds),itrpe );
		}
	}
}
