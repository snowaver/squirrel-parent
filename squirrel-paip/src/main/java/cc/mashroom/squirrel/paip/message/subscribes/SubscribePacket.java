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
package cc.mashroom.squirrel.paip.message.subscribes;

import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString( callSuper = true )
public  class  SubscribePacket  extends  Packet<SubscribePacket>
{
	public  SubscribePacket( long  subscriberId,Map<String,?>  subscriberProfile )
	{
		super( new  Header(PAIPPacketType.SUBSCRIBE) );
		
		super.setContactId(subscriberId).setSubscriberProfile( subscriberProfile);
	}
	
	public  SubscribePacket(ByteBuf  buf )
	{
		super( buf ,  0x00 );
		
		super.setContactId(buf.readLongLE()).setSubscriberProfile( new  HashMap<String,Object>().addEntries(JsonUtils.fromJson(PAIPUtils.decode(buf),new  TypeReference<Map<String,Object>>(){})) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain= true )
	private  Map<String,?>  subscriberProfile= new  HashMap<>();

	public  ByteBuf  writeToVariableByteBuf(  ByteBuf  byteBuf )
	{
		ByteBuf  subscriberProfileByteBuf = PAIPUtils.encode( JsonUtils.toJson(subscriberProfile == null ? new  HashMap<String,Object>() : subscriberProfile) );  byteBuf.writeLongLE(contactId).writeBytes(subscriberProfileByteBuf);  subscriberProfileByteBuf.release();  return  byteBuf;
	}
	
	public  int      getInitialVariableByteBufferSize()
	{
		return  8 +    super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.SUBSCRIBE );
	}
	*/
}