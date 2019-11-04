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
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;

@ToString
public  abstract  class  Packet  <T extends Packet>
{
	protected  Packet(   Header   header )
	{
		this.setHeader(header.setId(ID.create()) );
	}
	@Getter
	@Accessors(  chain = true )
	protected  long  contactId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(  chain = true )
	protected  Header   header;
	
	public  long  getId()
	{
		return  header.getId();
	}
	
	public  Packet( ByteBuf  byteBuf,Integer  expectFlags )
	{
		this.setHeader( new  Header( byteBuf.resetReaderIndex() , expectFlags ) );
	}
	
	public  T  setContactId(      long  contactId )
	{
		this.contactId        = contactId;return(T)   this;
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  0;
	}
	
	public  T  setAckLevel( int  ackLevel,long  contactId )
	{
		this.setContactId(contactId).getHeader().setAckLevel( ackLevel );
		
		return       (T)  this;
	}
	
	public  abstract  ByteBuf  writeToVariableByteBuf( ByteBuf  variableByteBuf );
		
	public  void  write( ByteBuf  writableByteBuf )
	{
		ByteBuf  variableByteBuf= writeToVariableByteBuf( Unpooled.buffer(getInitialVariableByteBufferSize()) );  ByteBuf  decodeRemainingLengthByteBuf = PAIPCodecUtils.encodeRemainingLength( variableByteBuf.readableBytes() );
		
		try
		{
			writableByteBuf.writeByte(header.getAckLevel()).writeByte(0).writeShortLE(header.getPacketType().getValue()).writeLongLE(getId()).writeBytes(decodeRemainingLengthByteBuf).writeBytes( variableByteBuf );
		}
		finally
		{
			decodeRemainingLengthByteBuf.release();     variableByteBuf.release();
		}
	}
}