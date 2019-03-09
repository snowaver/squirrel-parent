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

import  org.slf4j.LoggerFactory;

import  io.netty.buffer.ByteBuf;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.handler.codec.CorruptedFrameException;
import  io.netty.handler.codec.MessageToByteEncoder;
import  cc.mashroom.squirrel.paip.message.Packet;

public  class  PAIPEncoder  extends  MessageToByteEncoder  <Packet<?>>
{
	private  final  static  org.slf4j.Logger  logger = LoggerFactory.getLogger( PAIPEncoder.class );
	
	protected  void  encode( ChannelHandlerContext  channel,Packet<?>  packet,ByteBuf  byteBuf )  throws  Exception
	{
		System.out.println( "//*SED:\t\t"+packet.toString() );
		
		ByteBuf  contentByteBuf    = channel.alloc().buffer();
		
		try
		{
			packet.writeTo( contentByteBuf );
			
			if(     contentByteBuf.readableBytes() > Short.MAX_VALUE )
			{
				throw  new  CorruptedFrameException( String.format("SQUIRREL-PAIP:  ** PAIP  ENCODER **  content  length  (%d)  excceeds  %d",contentByteBuf.readableBytes(),Short.MAX_VALUE) );
			}
			
			byteBuf.writeInt(contentByteBuf.readableBytes()).writeBytes( contentByteBuf );
		}
		catch( Throwable  e )
		{
			e.printStackTrace();
			
			logger.error( e.getMessage(),e );
		}
		finally
		{
			contentByteBuf.release();
		}
	}
}