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
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.squirrel.paip.message.extensions.ByteArrayPacket;
import  cc.mashroom.squirrel.paip.message.extensions.StringPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.UnsubscribePacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;

import  lombok.AllArgsConstructor;
import  lombok.Getter;

@AllArgsConstructor
public  enum  PAIPPacketType
{
	RESERVED(0x00,null),CONNECT(0x01,ConnectPacket.class),CONNECT_ACK(0x02,ConnectAckPacket.class),PING(0x03,PingPacket.class),PING_ACK(0x04,PingAckPacket.class),DISCONNECT_ACK(0x05,DisconnectAckPacket.class),SUBSCRIBE(0x06,SubscribePacket.class),SUBSCRIBE_ACK(0x07,SubscribeAckPacket.class),UNSUBSCRIBE(0x08,UnsubscribePacket.class),QOS_RECEIPT(0x09,QosReceiptPacket.class),CHAT(0x0A,ChatPacket.class),CHAT_RETRACT(0x0B,ChatRetractPacket.class),GROUP_CHAT(0x0C,GroupChatPacket.class),GROUP_CHAT_EVENT(0x0D,GroupChatEventPacket.class),CALL(0x0E,CallPacket.class),CALL_ACK(0x0F,CallAckPacket.class),CALL_SDP(0x10,SDPPacket.class),CALL_CANDIDATE(0x11,CandidatePacket.class),CLOSE_CALL(0x12,CloseCallPacket.class),STRING(0x13,StringPacket.class),BYTE_ARRAY( 0x14,ByteArrayPacket.class );
	
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