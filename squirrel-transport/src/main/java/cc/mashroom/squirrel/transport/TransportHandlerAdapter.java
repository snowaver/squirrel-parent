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

import java.util.concurrent.atomic.AtomicLong;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  io.netty.bootstrap.Bootstrap;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import  io.netty.channel.ChannelOption;
import  io.netty.channel.EventLoopGroup;
import  io.netty.channel.nio.NioEventLoopGroup;
import  io.netty.channel.socket.nio.NioSocketChannel;
import  lombok.AccessLevel;
import  lombok.NonNull;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

@Accessors( chain=true )
public  class     TransportHandlerAdapter  extends  ChannelInboundHandlerAdapter
{
	protected  EventLoopGroup  eventLooperGroup = new  NioEventLoopGroup( Integer.parseInt( System.getProperty( "netty.eventloopergroup.thread.count", "2")) );
	
	protected  ConnectState  connectState =ConnectState.NONE;
	@Setter( value= AccessLevel.PRIVATE )
	private  Channel  channel;
	@Setter( value= AccessLevel.PRIVATE )
	private  Bootstrap  bootstrap;
	@Setter( value= AccessLevel.PRIVATE )
	protected  TransportConfig  transportConfig;
	
	protected  Map<Long,TransportFuture<PendingAckPacket<?>>>  transportFutures=   new  ConcurrentHashMap<Long,TransportFuture<PendingAckPacket<?>>>();
	AtomicLong readCount = new AtomicLong();
	@Override
	public  void  channelRead( ChannelHandlerContext  context  ,Object  packet )
	{
		if( packet instanceof      PendingAckPacket<?> )
		{
			TransportFuture<PendingAckPacket<?>>  transportFuture = this.transportFutures.remove( ObjectUtils.cast(packet,PendingAckPacket.class).getPendingPacketId() );
			
			if( transportFuture != null )transportFuture.setPendingAckPacket(ObjectUtils.cast(packet,PendingAckPacket.class)).done(true);
		}
	}
	
	public  TransportFuture<PendingAckPacket<?>>  write( @NonNull  final  Packet  <?>  packet  /*,long  timeout,@NonNull  TimeUnit  timeoutTimeUnit*/ )
	{
		TransportFuture<PendingAckPacket<?>>  transportFuture = new  TransportFuture<PendingAckPacket<?>>( packet );
		
		if( this.channel != null && this.channel.isActive() && this.channel.isWritable() && (packet.getHeader().getAckLevel() == 0 || packet.getHeader().getAckLevel() == 1 && this.transportFutures.put(packet.getId(),transportFuture) == null) )
		{
			this.eventLooperGroup.submit(new  Runnable()  {@SneakyThrows(value={InterruptedException.class})  public  void  run(){ channel.writeAndFlush(packet).sync().isSuccess(); }} );  return  transportFuture;
		}
		
		throw  new  IllegalStateException( "SQUIRREL-TRANSPORT:  ** TRANSPORT  HANDLER  ADAPTER **  the  channel  is  not  avaialble." );
	}
	@SneakyThrows( value= {InterruptedException.class} )
	public  void  disconnect()
	{
		this.channel.disconnect().sync();
		
		this.onConnectStateChanged( connectState  = ConnectState.DISCONNECTED );
	}
	protected  void  onConnectStateChanged( ConnectState  connectState )
	{
		
	}
	@SneakyThrows( value= {InterruptedException.class} )
	public  void     release()
	{
		this.eventLooperGroup.shutdownGracefully().sync().await();
	}
	@SneakyThrows( value= {InterruptedException.class} )
	public  void  connect(          @NonNull  TransportConfig  transportConfig )
	{
		this.setTransportConfig(transportConfig).setBootstrap(this.bootstrap != null ? this.bootstrap : new  Bootstrap().group(this.eventLooperGroup).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,transportConfig.getConnectTimeoutMillis()).handler(new  ChannelInitailizer(this,transportConfig.getSslContext(),transportConfig.getKeepaliveSeconds()))).onConnectStateChanged( this.connectState = ConnectState.CONNECTING );
		
		this.setChannel(this.bootstrap.connect(transportConfig.getHost(),transportConfig.getPort()).sync().channel()).onConnectStateChanged( this.connectState = ConnectState.CONNECTED );
	}
}
