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

import io.netty.buffer.ByteBuf;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;

import java.util.stream.Collectors;

import  org.joda.time.DateTime;

public  class    PAIPPacketHandler       extends  ChannelInboundHandlerAdapter
{
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}
	
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  ukerror )  throws  Exception
	{
		ukerror.printStackTrace();
	}
	
	static byte[] bytesOf(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return data;
    }

    static String stringOf(ByteBuf buf) {
        return new String(bytesOf(buf));
    }
	
	public  void  channelRead( ChannelHandlerContext  context,Object  message )  throws Exception
	{
		if(message instanceof ArrayRedisMessage) {
			ArrayRedisMessage arm = (ArrayRedisMessage) message;
			;
			System.err.println(arm.children().stream().map((a) -> stringOf(((FullBulkStringRedisMessage) a).content())).collect(Collectors.toList()));
		
			context.channel().writeAndFlush(new SimpleStringRedisMessage("OK"));
		}
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.READ:\t"+message.toString() );
		
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}
}