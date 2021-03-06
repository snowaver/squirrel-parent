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
package cc.mashroom.squirrel.paip.codec;

import  org.joda.time.DateTime;

import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelHandler.Sharable;
import  io.netty.handler.codec.MessageToByteEncoder;
import  lombok.extern.slf4j.Slf4j;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.util.ObjectUtils;

@Slf4j
@Sharable
public  class  PAIPEncoderHandlerAdapter  extends  MessageToByteEncoder<Packet<?>>
{
	protected  void  encode( ChannelHandlerContext  context,Packet<?>  packet,ByteBuf  byteBuf )throws  Exception
	{
		if( packet instanceof CloseCallPacket )
		{
			context.channel().attr(CallPacket.CALL_ROOM_ID).set( null  );
		}
		else
		if( packet instanceof CallPacket )
		{
			context.channel().attr(CallPacket.CALL_ROOM_ID).set( ObjectUtils.cast(packet,CallPacket.class).getRoomId() );
		}
		
		System.out.println(   DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.SENT:\t"+packet.toString() );
		
		ByteBuf  contentBuf= Unpooled.buffer(); packet.write(contentBuf);  byteBuf.writeInt(contentBuf.readableBytes()).writeBytes( contentBuf );  contentBuf.release();
	}
}