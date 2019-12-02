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

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  io.netty.bootstrap.Bootstrap;
import  io.netty.buffer.PooledByteBufAllocator;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import  io.netty.channel.ChannelOption;
import  io.netty.channel.EventLoopGroup;
import  io.netty.channel.nio.NioEventLoopGroup;
import  io.netty.channel.socket.nio.NioSocketChannel;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.NonNull;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.Synchronized;
import  lombok.experimental.Accessors;

@Accessors( chain=true )
public  class     TransportHandlerAdapter  extends  ChannelInboundHandlerAdapter
{
	@Getter
	protected  ConnectState  connectState  =  ConnectState.NONE;
	@Setter( value= AccessLevel.PRIVATE )
	private  Channel  channel;
	@Setter( value= AccessLevel.PRIVATE )
	private  Bootstrap  bootstrap;
	protected  EventLoopGroup  eventLoopGroup= new  NioEventLoopGroup();
	@Setter( value= AccessLevel.PRIVATE )
	protected  TransportConfig  transportConfig;
	/*
	protected  boolean     authenticated;
	*/
	protected  TransportHandlerAdapter  setConnectState(  ConnectState  cstate )
	{
		onConnectStateChanged( connectState =  cstate );  return   this;
	}
	protected  Map<Long,TransportFuture<PendingAckPacket<?>>>  transportFutures=   new  ConcurrentHashMap<Long,TransportFuture<PendingAckPacket<?>>>();
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
		final  TransportFuture<PendingAckPacket<?>>  transportFuture = new  TransportFuture<PendingAckPacket<?>>(  packet );
		
		if( this.channel != null&& this.channel.isOpen()  && this.channel.isActive() && (packet.getAckLevel()== 0 || packet.getAckLevel() == 1 && this.transportFutures.put(packet.getId(),transportFuture) == null) )
		{
			this.eventLoopGroup.submit(  new  Runnable()  {public  void  run(){ write(packet,transportFuture); }} );  return  transportFuture;
		}
		
		throw  new  IllegalStateException( "SQUIRREL-TRANSPORT:  ** TRANSPORT  HANDLER  ADAPTER **  the  channel  is  not  avaialble." );
	}
	@SneakyThrows( value= {InterruptedException.class} )
	private void  write(Packet <?>packet,TransportFuture  <PendingAckPacket<?>>  transportFuture )
	{
		if( 1  == packet.getAckLevel() && !this.channel.writeAndFlush(packet).sync().isSuccess() )
		{
			transportFuture.done( false);
		}
		else
		if( 0  == packet.getAckLevel()  )
		{
			transportFuture.done( this.channel.writeAndFlush(packet).sync().isSuccess() );
		}
	}
	@Override
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  t )
	{
		t.printStackTrace(  );
	}
	protected  void  onConnectStateChanged( ConnectState  connectState )
	{
		
	}
	@SneakyThrows( value= {InterruptedException.class} )
	@Synchronized
	public  void  disconnect()
	{
		this.channel.disconnect().sync();
		
		setConnectState(    ConnectState.DISCONNECTED );
	}
	@SneakyThrows( value= {InterruptedException.class} )
	@Synchronized
	public  void     release()
	{
		this.eventLoopGroup.shutdownGracefully().sync();
	}
	protected  boolean  authenticate( Object...objects )
	{
		return  true;
	}
	@Override
	public  void  channelInactive( ChannelHandlerContext  context      )
	{
		setConnectState(    ConnectState.DISCONNECTED );context.close();
	}
	@SneakyThrows( value= {InterruptedException.class} )
	@Synchronized
	public  void  connect(  @NonNull  TransportConfig  transportConfig )
	{
		this.setTransportConfig(transportConfig).setBootstrap(this.bootstrap != null ? this.bootstrap : new  Bootstrap().group(this.eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,transportConfig.getConnectTimeoutMillis()).option(ChannelOption.SO_KEEPALIVE,true).option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT).option(ChannelOption.TCP_NODELAY,true).handler(new  ChannelInitailizer(this,transportConfig.getSslContext(),transportConfig.getKeepaliveSeconds()))).setConnectState( ConnectState.CONNECTING );
		
		this.setChannel(this.bootstrap.connect(transportConfig.getHost(),transportConfig.getPort()).sync().channel()).setConnectState( this.connectState = ConnectState.CONNECTED );
		
		if( setConnectState(ConnectState.AUTHENTICATING).setConnectState(authenticate(transportConfig.getAuthenticateObjects()) ? ConnectState.AUTHENTICATED : ConnectState.DISCONNECTED).getConnectState() == ConnectState.DISCONNECTED )  disconnect();
	}
}
