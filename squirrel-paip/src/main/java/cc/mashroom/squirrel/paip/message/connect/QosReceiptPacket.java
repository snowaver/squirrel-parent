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
import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@AllArgsConstructor
@ToString(callSuper=true )
public  class  QosReceiptPacket<T extends QosReceiptPacket<?>>  extends  Packet<T>
{
	public  QosReceiptPacket(ByteBuf  buf )
	{
		super( buf,0x00 );
		
		this.setContactId(buf.readLongLE()).setPacketId(buf.readLongLE()).setAttatchments( new  HashMap<String,Object>().addEntries((java.util.Map<String,Object>)  JsonUtils.fromJson(PAIPUtils.decode(buf))) );
	}
	
	public  QosReceiptPacket( long  contactId,long  packetId )
	{
		super.setContactId(contactId).setPacketId( packetId );
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
		return  byteBuf.writeLongLE(contactId).writeLongLE(packetId).writeBytes( PAIPUtils.encode( JsonUtils.toJson(attatchments) ) );
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  16 + super.getInitialVariableByteBufferSize();
	}
	
	public  void  writeTo(   ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.QOS_RECEIPT );
	}
}