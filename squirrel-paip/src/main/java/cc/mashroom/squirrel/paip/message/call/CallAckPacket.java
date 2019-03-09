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
package cc.mashroom.squirrel.paip.message.call;

import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString
public  class  CallAckPacket  extends  QosReceiptPacket  <CallAckPacket>
{
	public  final  static  int  ACCEPT = 0x00;
	
	public  final  static  int  REJECT = 0x01;
	
	public  final  static  int  CONTACT_OFFLINE    = 0x03;
	
	public  final  static  int  BUSY   = 0x02;
	
	public  CallAckPacket(  ByteBuf  buf )
	{
		super( buf );
		
		setCallId(buf.readLongLE()).setResponseCode(   buf.readByte() );
	}
	
	public  CallAckPacket( long  callPacketId,long  contactId,long  callId,int  responseCode )
	{
		super( contactId , callPacketId );
		
		setCallId(callId).setResponseCode( responseCode );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  callId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	
	private  int  responseCode;
	
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,Unpooled.buffer(25).writeLongLE(contactId).writeLongLE(packetId).writeBytes(PAIPUtils.encode(JsonUtils.toJson(attatchments))).writeLongLE(callId).writeByte(responseCode),PAIPPacketType.CALL_ACK );
	}
}