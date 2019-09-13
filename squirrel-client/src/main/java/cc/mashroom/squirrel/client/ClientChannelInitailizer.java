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

import  io.netty.channel.ChannelInitializer;
import  io.netty.channel.socket.SocketChannel;
import  io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import  io.netty.handler.ssl.SslHandler;
import  io.netty.handler.timeout.IdleStateHandler;
import  lombok.AllArgsConstructor;

import  javax.net.ssl.SSLEngine;

import  cc.mashroom.squirrel.client.handler.ChannelDuplexIdleTimeoutHandler;
import  cc.mashroom.squirrel.paip.codec.PAIPDecoder;
import  cc.mashroom.squirrel.paip.codec.PAIPEncoder;

@AllArgsConstructor

public  class  ClientChannelInitailizer  extends  ChannelInitializer  <SocketChannel>
{
	private  TcpAutoReconnectChannelInboundHandlerAdapter  adapter;
	
	protected  void  initChannel( SocketChannel  channel )  throws  Exception
	{
		SSLEngine  sslEngine  = SquirrelClient.SSL_CONTEXT.createSSLEngine();
		
		sslEngine.setUseClientMode( true );
		
		channel.pipeline().addLast("handler.ssl",new  SslHandler(sslEngine)).addLast("handler.idle.state",new  IdleStateHandler((int)  (adapter.getKeepalive()*1.5),adapter.getKeepalive(),0)).addLast("handler.idle.timeout",new  ChannelDuplexIdleTimeoutHandler()).addLast("length-based.decoder",new  LengthFieldBasedFrameDecoder(2*1024*1024,0,4,0,4)).addLast("decoder",new  PAIPDecoder()).addLast("encoder",new  PAIPEncoder())/*.addLast("qos.handler",QosHandler.INSTANCE)*/.addLast( "squirrel.client",adapter );
	}
}