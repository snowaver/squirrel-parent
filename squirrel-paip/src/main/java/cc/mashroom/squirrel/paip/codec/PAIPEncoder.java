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
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.handler.codec.CorruptedFrameException;
import  io.netty.handler.codec.MessageToByteEncoder;
import  lombok.extern.slf4j.Slf4j;
import  cc.mashroom.squirrel.paip.message.Packet;

@Slf4j

public  class  PAIPEncoder  extends  MessageToByteEncoder  <Packet<?>>
{
	protected  void  encode( ChannelHandlerContext  channel,Packet<? >  packet,   ByteBuf  byteBuf )  throws  Exception
	{
		ByteBuf  contentByteBuf=channel.alloc().buffer();
		
//		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.SENT:\t"+packet.toString() );
		
		try
		{
			packet.write( contentByteBuf );
			
			if(     contentByteBuf.readableBytes() > Short.MAX_VALUE )
			{
				throw  new  CorruptedFrameException( String.format("SQUIRREL-PAIP:  ** PAIP  ENCODER **  content  length  (%d)  excceeds  %d",contentByteBuf.readableBytes(),Short.MAX_VALUE) );
			}
			
			byteBuf.writeInt(contentByteBuf.readableBytes()).writeBytes( contentByteBuf );
		}
		catch( Throwable  e )
		{
			e.printStackTrace();
			
			log.error(  e.getMessage(),e );
		}
		finally
		{
			contentByteBuf.release();
		}
	}
}