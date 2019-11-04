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
package cc.mashroom.squirrel.server.handler;

import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;

import  org.joda.time.DateTime;

import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.squirrel.paip.message.extensions.ByteArrayPacket;
import  cc.mashroom.util.ObjectUtils;

public  class    PAIPPacketHandler  extends  ChannelInboundHandlerAdapter
{
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  err )  throws  Exception
	{
		err.printStackTrace();
	}
	
	public  void  channelRead(     ChannelHandlerContext  context,Object  packet )  throws  Exception
	{
//		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.READ:\t"+packet.toString() );
		
		if( packet instanceof ByteArrayPacket && ObjectUtils.cast(packet,ByteArrayPacket.class).getHeader().getAckLevel()  == 1 )
		{
			context.channel().writeAndFlush( new  PendingAckPacket<>(0,ObjectUtils.cast(packet,ByteArrayPacket.class).getId()) );
		}
	}
}