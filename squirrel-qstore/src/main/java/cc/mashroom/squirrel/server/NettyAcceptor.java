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
package cc.mashroom.squirrel.server;

import  io.netty.bootstrap.ServerBootstrap;
import  io.netty.channel.ChannelOption;
import  io.netty.channel.EventLoopGroup;
import  io.netty.channel.epoll.EpollEventLoopGroup;
import  io.netty.channel.epoll.EpollServerSocketChannel;
import  io.netty.channel.nio.NioEventLoopGroup;
import  io.netty.channel.socket.nio.NioServerSocketChannel;

public  class  NettyAcceptor
{
	public  void  stop()
	{
		acceptEventGroup.shutdownGracefully();
		
		handleEventGroup.shutdownGracefully();
	}
	
	private  EventLoopGroup  acceptEventGroup;
	
	private  EventLoopGroup  handleEventGroup;
	
	public  NettyAcceptor  initialize( String  host,int  port )
	{
		boolean  isLinuxSystem = System.getProperty("os.name" ).toLowerCase().contains( "linux" );
		
		try
		{
			ServerBootstrap  bootstrap = new  ServerBootstrap().group(acceptEventGroup = isLinuxSystem ? new  EpollEventLoopGroup() : new  NioEventLoopGroup(),handleEventGroup = isLinuxSystem ? new  EpollEventLoopGroup() : new  NioEventLoopGroup()).channel(isLinuxSystem ? EpollServerSocketChannel.class : NioServerSocketChannel.class)/*.option(ChannelOption.SO_TIMEOUT,120)*/.option(ChannelOption.SO_BACKLOG,1024*1024).option(ChannelOption.SO_REUSEADDR,true).childHandler( new  ServerChannelInitializer() );
			
			bootstrap.bind(host, port).sync();
			
			System.err.println( "Memcached server started port: "+port );
		}
		catch(Throwable  e )
		{
			throw  new  IllegalStateException( e );
		}
		
		return  this;
	}
}