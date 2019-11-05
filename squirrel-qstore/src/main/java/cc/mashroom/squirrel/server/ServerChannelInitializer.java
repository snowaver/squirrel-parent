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

import  io.netty.channel.socket.SocketChannel;
import  io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import  lombok.AllArgsConstructor;

import  cc.mashroom.squirrel.paip.codec.PAIPDecoderHandlerAdapter;
import  cc.mashroom.squirrel.paip.codec.PAIPEncoderHandlerAdapter;
import  cc.mashroom.squirrel.server.handler.PAIPPacketHandler;
import  cc.mashroom.squirrel.transport.ChannelDuplexIdleTimeoutHandler;

@AllArgsConstructor

public  class  ServerChannelInitializer  extends  io.netty.channel.ChannelInitializer<SocketChannel>
{
	protected  void  initChannel( SocketChannel  channel )  throws  Exception
	{
		channel.pipeline().addLast("handler.idle.timeout",new  ChannelDuplexIdleTimeoutHandler()).addLast("length.based.decoder",new  LengthFieldBasedFrameDecoder(2*1024*1024,0,4,0,4)).addLast("decoder",new  PAIPDecoderHandlerAdapter()).addLast("encoder",new  PAIPEncoderHandlerAdapter()).addLast( "handler",new  PAIPPacketHandler() );		
	}
}