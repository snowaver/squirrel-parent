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
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@ToString(callSuper=true )
public  class  ContactAckPacket<T extends ContactAckPacket<?>>  extends  Packet<T>
{
	public  ContactAckPacket( long  contactId,long  packetId )
	{
		super(new  Header(PAIPPacketType.CONNECT_ACK));
		
		super.setContactId(contactId).setPacketId( packetId );
	}
	
	public  ContactAckPacket(ByteBuf  buf )
	{
		super(buf, 0x00 );
		
		this.setContactId(buf.readLongLE()).setPacketId(buf.readLongLE()).setAttatchments( new  HashMap<String,Object>().addEntries(JsonUtils.fromJson(PAIPCodecUtils.decode(buf),new  TypeReference<Map<String,Object>>(){})) );
	}
	
	@Setter( value= AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  long   packetId;
	@Setter( value= AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  Map<String,Object>  attatchments   = new  HashMap<String,Object>();

	public  ByteBuf  writeToVariableByteBuf(ByteBuf  byteBuf )
	{
		return  byteBuf.writeLongLE(this.contactId).writeLongLE(this.packetId).writeBytes( PAIPCodecUtils.encode(JsonUtils.toJson(this.attatchments)) );
	}
	
	public  int      getInitialVariableByteBufferSize()
	{
		return  16 + super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(   ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.QOS_RECEIPT );
	}
	*/
}