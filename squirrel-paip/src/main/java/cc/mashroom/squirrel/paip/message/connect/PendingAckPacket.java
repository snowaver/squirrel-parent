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
package cc.mashroom.squirrel.paip.message.connect;

import  cc.mashroom.squirrel.paip.message.Packet;

import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@ToString(callSuper=true )
public  class  PendingAckPacket<T extends PendingAckPacket<?>>  extends  Packet<T>
{	
	protected  PendingAckPacket( PAIPPacketType  packetType,  long  contactId,long  packetId  )
	{
		super( packetType , 0, contactId );
		
		this.setPendingPacketId(packetId );
	}
	
	public  PendingAckPacket(   long  contactId,long  pendingPacketId )
	{
		this( PAIPPacketType.PENDING_ACK  ,contactId,pendingPacketId );
	}
	
	public  PendingAckPacket( ByteBuf  buf)
	{
		super(buf, 0x00 );
		
		this.setContactId(buf.readLongLE()).setPendingPacketId(buf.readLongLE()).setAttatchments( PAIPCodecUtils.decode(buf) );
	}
	
	@Setter( value= AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  long  pendingPacketId;
	@Setter( value= AccessLevel.PUBLIC    )
	@Getter
	@Accessors(chain=true)
	protected  String   attatchments= "{}";
	
	public  int      getInitialVariableByteBufferSize()
	{
		return  16+super.getInitialVariableByteBufferSize();
	}
	
	public  ByteBuf  writeToVariableByteBuf( ByteBuf  variablebyteBuf )
	{
		ByteBuf  attatchmentsBuf = PAIPCodecUtils.encode(     this.attatchments );  variablebyteBuf.writeLongLE(this.contactId).writeLongLE(this.pendingPacketId).writeBytes(attatchmentsBuf);  attatchmentsBuf.release();  return  variablebyteBuf;
	}
}