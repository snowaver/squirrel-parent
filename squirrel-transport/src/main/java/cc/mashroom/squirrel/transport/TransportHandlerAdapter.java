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

import  org.joda.time.DateTime;

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
public  class  TransportHandlerAdapter   extends    ChannelInboundHandlerAdapter
{
	@Setter( value= AccessLevel.PRIVATE )
	protected  TransportConfig  transportConfig;
	@Setter( value= AccessLevel.PRIVATE )
	protected  Bootstrap  bootstrap;
	@Setter( value= AccessLevel.PRIVATE )
	protected  Channel    channel;
	@Setter( value= AccessLevel.PRIVATE )
	protected  long  checkIntervalSeconds  = 10;
	
	protected  EventLoopGroup  workerEventLoopGroup =  new  NioEventLoopGroup();
	
	protected  Map<Long,TransportFuture<PendingAckPacket<?>>>  transportFutures=  new  ConcurrentHashMap<Long,TransportFuture<PendingAckPacket<?>>>();
	@Getter
	protected  ConnectState  connectState  =    ConnectState.NONE;
	
	public  TransportFuture<PendingAckPacket<?>>  write(  @NonNull     final  Packet  <?>  packet )
	{
		final  TransportFuture<PendingAckPacket<?>>  transportFuture = new  TransportFuture<PendingAckPacket<?>>(packet );
		
		if( this.channel!=null &&  this.channel.isOpen()  && this.channel.isActive() && (packet.getAckLevel() == 0 || packet.getAckLevel() == 1 && this.transportFutures.put(packet.getId(),transportFuture) == null) )
		{
			workerEventLoopGroup.execute(new  Runnable()  {  public  void  run()  {  write(packet,transportFuture); } } );
			
			return       transportFuture;
		}
		throw  new  IllegalStateException( String.format("SQUIRREL-TRANSPORT:  ** TRANSPORT  HANDLER  ADAPTER **  can  not  write  the  pacekt  ( %s )  while  the  channel  is  not  avaialble .",packet.toString()));
	}
	@SneakyThrows(value = {InterruptedException.class} )
	private  void  write(Packet<?>packet,TransportFuture  <PendingAckPacket <?>>  transportFuture )
	{
		if( 0  == packet.getAckLevel()  )
		{
			transportFuture.done( channel.writeAndFlush(packet).syncUninterruptibly().isSuccess());
		}
		else
		if( 1  == packet.getAckLevel() && ! this.channel.writeAndFlush(packet).sync().isSuccess() )
		{
			transportFuture.done( false);
		}
	}
	@Override
	public  void  channelRead( ChannelHandlerContext  context  ,Object  packet )  throws  Exception
	{
		if( packet instanceof      PendingAckPacket<?> )
		{
			TransportFuture<PendingAckPacket<?>>  transportFuture = this.transportFutures.remove( ObjectUtils.cast(packet,PendingAckPacket.class).getPendingPacketId() );
			
			if( transportFuture != null )  transportFuture.setPendingAckPacket(ObjectUtils.cast(packet,PendingAckPacket.class)).done( /*DONE*/ true );
		}
	super.channelRead( context ,packet );
	}
	protected  TransportHandlerAdapter  setConnectState(  ConnectState   state )
	{
		onConnectStateChanged(this.connectState =state);
		
		return  this;
	}
	@Override
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  t )  throws  Exception
	{
		t.printStackTrace(  );
		
		super.exceptionCaught(context,t);
		
		context.close().syncUninterruptibly();  this.tryconnect();
	}
	protected  void  onConnectStateChanged( ConnectState  connectState )
	{
		
	}
	@Override
	public  void  channelInactive( ChannelHandlerContext  context)                throws  Exception
	{
		setConnectState(    ConnectState.DISCONNECTED );
		
		super.channelInactive( context );
		
		context.close().syncUninterruptibly();  this.tryconnect();
	}
	protected  boolean  authenticate( Object...objects )
	{
		return  true;
	}
	public  void      checkConnectivity()
	{
		System.out.println( String.format(DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.STAT:\tTRANSPORT  CONFIG  (%s),  CHANNEL  (%s)",this.transportConfig == null ? null : this.transportConfig.toString(),this.channel == null ? null : "OPEN:  "+this.channel.isOpen()+",  ACTIVE:  "+this.channel.isActive()+",  WRITABLE:  "+this.channel.isWritable()) );
		
		if( this.transportConfig!= null&& ( this.channel  == null || !this.channel.isOpen() || !this.channel.isActive()) )
		{
		this.connect( transportConfig  );
		}
	}
	@SneakyThrows(value = {InterruptedException.class} )
	public  void  tryconnect()
	{
		for(;; Thread.sleep(checkIntervalSeconds*1000) )
		{
			try
			{
				this.checkConnectivity();         break;
			}
		catch(  Throwable  e )
			{
			setConnectState(ConnectState.DISCONNECTED );
			}
		}
	}
	@Synchronized
	public  void     release()
	{
		workerEventLoopGroup.shutdownGracefully().syncUninterruptibly();  
	}
	/*
	@SneakyThrows(value = {InterruptedException.class} )
	*/
	@Synchronized
	public  void  disconnect()
	{
		this.setTransportConfig(  null );
		
		if( !   channel.disconnect().syncUninterruptibly().isSuccess() )
		{
			setConnectState(ConnectState.DISCONNECTED );
		}
		else
		{
			throw  new  IllegalStateException( "SQUIRREL-TRANSPORT:  ** TRANSPORT  HANDLER  ADAPTER **  can  not  disconnect  from  the  server."   );
		}
	}
	@Synchronized
	public  void  connect( @NonNull   TransportConfig  transportConfig )
	{
		if( this.connectState != ConnectState.NONE &&  connectState !=  ConnectState.DISCONNECTED )
		{
			return;
		}
		this.setTransportConfig(transportConfig).setBootstrap( this.bootstrap != null ? this.bootstrap : new  Bootstrap().group(this.workerEventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,transportConfig.getConnectTimeoutMillis()).option(ChannelOption.SO_KEEPALIVE,true).option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT).option(ChannelOption.TCP_NODELAY,true).handler(new  ChannelInitailizer(this,transportConfig.getSslContext(),transportConfig.getKeepaliveSeconds()))).setConnectState( ConnectState.CONNECTING );
		try
		{
		this.setChannel(this.bootstrap.connect(transportConfig.getHost(),transportConfig.getPort()).sync().channel()).setConnectState( this.connectState = ConnectState.CONNECTED );
		}
		catch(  Throwable  e )
		{
		throw  new  IllegalStateException( "SQUIRREL-TRANSPORT:  ** TRANSPORT  HANDLER  ADAPTER **  can  not  connect  to  the  server."        , e );
		}
		
		if( setConnectState(ConnectState.AUTHENTICATING).setConnectState(authenticate(transportConfig.getAuthenticateObjects()) ? ConnectState.AUTHENTICATED : ConnectState.DISCONNECTED).getConnectState() == ConnectState.DISCONNECTED )  disconnect();
	}
}
