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

import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.handler.codec.ByteToMessageDecoder;
import  io.netty.handler.codec.CorruptedFrameException;
import  lombok.extern.slf4j.Slf4j;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRetractPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatInvitedPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;

@Slf4j

public  class  PAIPDecoder  extends  ByteToMessageDecoder
{
	/*
	private  final  org.slf4j.Logger  logger= LoggerFactory.getLogger( ByteToMessageDecoder.class );
	*/
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
		
		int  packetType    = byteBuf.readShortLE();
		
		switch(    PAIPPacketType.valueOf( packetType ) )
		{
			case  CONNECT:
			{
				return  new  ConnectPacket( channel, byteBuf );
			}
			case  CONNECT_ACK:
			{
				return  new  ConnectAckPacket( byteBuf );
			}
			case  PING:
			{
				return  new  PingPacket( byteBuf );
			}
			case  PING_ACK:
			{
				return  new  PingAckPacket( byteBuf );
			}
			case  CHAT:
			{
				return  new  ChatPacket( byteBuf );
			}
			case  QOS_RECEIPT:
			{
				return  new  QosReceiptPacket<>(     byteBuf );
			}
			case  SUBSCRIBE:
			{
				return  new  SubscribePacket(  byteBuf );
			}
			case  SUBSCRIBE_ACK:
			{
				return  new  SubscribeAckPacket(     byteBuf );
			}
			case  DISCONNECT:
			{
				return  new  DisconnectPacket( byteBuf );
			}
			case  DISCONNECT_ACK:
			{
				return  new  DisconnectAckPacket(    byteBuf );
			}
			case  CALL:
			{
				return  new  CallPacket( byteBuf );
			}
			case  CALL_ACK:
			{
				return  new  CallAckPacket( byteBuf );
			}
			case  CALL_SDP:
			{
				return  new  SDPPacket(  byteBuf );
			}
			case  CALL_CANDIDATE:
			{
				return  new  CandidatePacket(  byteBuf );
			}
			case  CLOSE_CALL:
			{
				return  new  CloseCallPacket(  byteBuf );
			}
			case  GROUP_CHAT:
			{
				return  new  GroupChatPacket(  byteBuf );
			}
			case  GROUP_CHAT_INVITED:
			{
				return  new  GroupChatInvitedPacket( byteBuf );
			}
			case  CHAT_WITHDRAW:
			{
				return  new  ChatRetractPacket(byteBuf );
			}
			default:
			{
				throw  new  CorruptedFrameException( "SQUIRREL-PAIP:  ** PAIP  DECODER **  can  not  recognise  the  packet  for  unknown  type:  "+packetType+",  length:  "+byteBuf.resetReaderIndex().readableBytes() );
			}
		}
	}
}