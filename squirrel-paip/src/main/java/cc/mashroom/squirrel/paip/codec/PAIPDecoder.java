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

import  java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.handler.codec.ByteToMessageDecoder;
import  io.netty.handler.codec.CorruptedFrameException;
import  lombok.extern.slf4j.Slf4j;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.Packet;
import cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;

@Slf4j

public  class  PAIPDecoder  extends  ByteToMessageDecoder
{
	protected  void  decode( ChannelHandlerContext  context,ByteBuf  byteBuf,List<Object>  objectList )  throws  Exception
	{
		try
		{
			objectList.add(decode(context.channel(),byteBuf.markReaderIndex().resetReaderIndex()) );
		}
		catch( Exception  e )
		{
			e.printStackTrace();
			
			log.error( e.getMessage(),e );
		}
	}
	
	public  static  Packet<?>  decode( Channel  channel,ByteBuf  byteBuf )
	{
		byteBuf.skipBytes(  2 );
		
		try
		{
			int  packetTypeValue = byteBuf.readShortLE();
			
			PAIPPacketType  packetType = PAIPPacketType.valueOf( packetTypeValue );
			
			if( packetType   != PAIPPacketType.RESERVED )
			{
				return  packetType.getPacketClass().getConstructor(packetType == PAIPPacketType.CONNECT ? new  Class[]{Channel.class,ByteBuf.class} : new  Class[]{ByteBuf.class}).newInstance( packetType == PAIPPacketType.CONNECT ? new  Object[]{channel,byteBuf} : new  Object[]{byteBuf} );
			}
			else
			{
				String  externalDecoderClassName = System.getProperty( "squirrel.paip.packet.externalDecoderClass" , "" );
				
				if( StringUtils.isNotBlank(   externalDecoderClassName ) )
				{
					ObjectUtils.cast(Class.forName(externalDecoderClassName),new  TypeReference<Class<? extends PAIPExternalDecoder>>(){}).newInstance().decode( byteBuf );
				}
			}
			
			return  null;
		}
		catch( Exception  e )
		{
			e.printStackTrace();
			
			throw  new  CorruptedFrameException(  "SQUIRREL-PAIP:  ** PAIP  DECODER **  can  not  decode  the  packet." );
		}
	}
}