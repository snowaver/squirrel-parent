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

import  lombok.AllArgsConstructor;
import  lombok.Getter;

@AllArgsConstructor
public  enum  PAIPPacketType
{
	RESERVED(0x00),CONNECT(0x01),CONNECT_ACK(0x02),SUBSCRIBE(0x03),SUBSCRIBE_ACK(0x04),UNSUBSCRIBE(0x05),UNSUBSCRIBE_ACK(0x06),PING(0x07),PING_ACK(0x08),DISCONNECT(0x09),DISCONNECT_ACK(0x0A),CHAT(0x0B),QOS_RECEIPT(0x0C),CALL(0x0D),CALL_ACK(0x0E),CALL_SDP(0x0F),CALL_CANDIDATE(0x10),CLOSE_CALL(0x11),GROUP_CHAT(0x12),GROUP_CHAT_INVITED(0x13),CHAT_WITHDRAW(0x14);
	
	@Getter
	private  int  value;

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