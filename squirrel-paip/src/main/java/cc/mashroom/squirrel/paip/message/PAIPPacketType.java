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
package cc.mashroom.squirrel.paip.message;

import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRetractPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.squirrel.paip.message.extensions.ByteArrayPacket;
import  cc.mashroom.squirrel.paip.message.extensions.StringPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;

import  lombok.AllArgsConstructor;
import  lombok.Getter;

@AllArgsConstructor
public  enum  PAIPPacketType
{
	RESERVED(0x00,null),CONNECT(0x01,ConnectPacket.class),CONNECT_ACK(0x02,ConnectAckPacket.class),SUBSCRIBE(0x03,SubscribePacket.class),SUBSCRIBE_ACK(0x04,SubscribeAckPacket.class),UNSUBSCRIBE(0x05,null),UNSUBSCRIBE_ACK(0x06,null),PING(0x07,PingPacket.class),PING_ACK(0x08,PingAckPacket.class),DISCONNECT(0x09,DisconnectPacket.class),DISCONNECT_ACK(0x0A,DisconnectAckPacket.class),CHAT(0x0B,ChatPacket.class),QOS_RECEIPT(0x0C,QosReceiptPacket.class),CALL(0x0D,CallPacket.class),CALL_ACK(0x0E,CallAckPacket.class),CALL_SDP(0x0F,SDPPacket.class),CALL_CANDIDATE(0x10,CandidatePacket.class),CLOSE_CALL(0x11,CloseCallPacket.class),GROUP_CHAT(0x12,GroupChatPacket.class),GROUP_CHAT_EVENT(0x13,GroupChatEventPacket.class),CHAT_RETRACT(0x14,ChatRetractPacket.class),STRING(0x15,StringPacket.class),BYTE_ARRAY( 0x16,ByteArrayPacket.class );
	
	@Getter
	private  int  value;
	@Getter
	private  Class  <? extends Packet>  packetClass;

	public  static  PAIPPacketType  valueOf( int  value )
	{
		for( PAIPPacketType  paipPacketType : PAIPPacketType.values() )
		{
			if( value == paipPacketType.getValue() )
			{
				return  paipPacketType;
			}
		}
		
		return RESERVED;
	}
}