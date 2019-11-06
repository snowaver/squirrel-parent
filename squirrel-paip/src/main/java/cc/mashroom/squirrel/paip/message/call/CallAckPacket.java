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
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  CallAckPacket    extends  RoomPacket      <CallAckPacket>
{
	public  final  static  int  ACK_AGREE   = 0x00;
	
	public  final  static  int  ACK_DECLINE = 0x01;
	
	public  CallAckPacket( long  contactId,long  roomId, int  response )
	{
		super( PAIPPacketType.CALL_ACK,0,contactId,  roomId );
		
		this.setResponseCode(  response );
	}
	@Override
	public  int  getInitialVariableByteBufferSize()
	{
		return   9 + super.getInitialVariableByteBufferSize();
	}
	
	public  CallAckPacket(  ByteBuf  buf )
	{
		super( buf,0x00 );
		
		setContactId(buf.readLongLE()).setResponseCode(buf.readByte() );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private int  responseCode;
	@Override
	public  ByteBuf  writeToVariableByteBuf(  ByteBuf  variableByteBuf )
	{
		return   super.writeToVariableByteBuf(variableByteBuf).writeLongLE(contactId).writeByte( responseCode );
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.CALL_ACK );
	}
	*/
}