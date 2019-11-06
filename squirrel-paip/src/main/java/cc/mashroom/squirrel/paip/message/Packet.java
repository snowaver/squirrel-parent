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

import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.NoArgsConstructor;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.util.ObjectUtils;

@ToString
@NoArgsConstructor
public  abstract  class  Packet  <T extends Packet>
{
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors( chain=true )
	private  PAIPPacketType  packetType;
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors( chain=true )
	private  long  id    = ID.create();
	//  0,  no  pending  ack  packet,  1,  require  pending  ack  packet,  which  may  be  lost  on  the  network,  but  assume  transport  failure  if  connection  error  or  timeout  without  pending  ack  packet,  you  can  resend  the  packet  again.
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors( chain=true )
	private  int   ackLevel;
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors( chain=true )
	private  int   remainingLength;
	@Getter
	@Accessors( chain=true )
	protected  long      contactId;
	
	protected  Packet( PAIPPacketType  packetType ,int  ackLevel,long  contactId )
	{
		setAckLevel(ackLevel).setPacketType(packetType).setContactId(  contactId);
	}
	
	protected  Packet( ByteBuf  byteBuf,int  expectedFlags )
	{
		byte  headerByte   =    byteBuf.readByte();
		
        this.setAckLevel((headerByte & 0x03)).setPacketType(PAIPPacketType.valueOf(byteBuf.skipBytes(1).readShortLE())).setId(byteBuf.readLongLE()).setRemainingLength( PAIPCodecUtils.decodeRemainingLength(byteBuf) );
	}
	
	public  T  setContactId(      long  contactId )
	{
		this.contactId     = contactId;return  ObjectUtils.cast( this );
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  0;
	}
	
	public  abstract  ByteBuf  writeToVariableByteBuf( ByteBuf  variableByteBuf );
	
	public  T  setAckLevel( int  ackLevel, long  contactId )
	{
		return  ObjectUtils.cast( setContactId(contactId).setAckLevel(ackLevel) );
	}
			
	public  void  write( ByteBuf  writableByteBuf )
	{
		ByteBuf  variableByteBuf= writeToVariableByteBuf( Unpooled.buffer(getInitialVariableByteBufferSize()) );  ByteBuf  decodeRemainingLengthByteBuf = PAIPCodecUtils.encodeRemainingLength( variableByteBuf.readableBytes() );
		
		writableByteBuf.writeByte(ackLevel).writeByte(0).writeShortLE(packetType.getValue()).writeLongLE(getId()).writeBytes(decodeRemainingLengthByteBuf).writeBytes( variableByteBuf );  decodeRemainingLengthByteBuf.release();  variableByteBuf.release();
	}
}