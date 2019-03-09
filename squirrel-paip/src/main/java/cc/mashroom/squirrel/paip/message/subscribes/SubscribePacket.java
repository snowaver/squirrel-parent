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
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString
public  class  SubscribePacket  extends  Packet<SubscribePacket>
{
	public  SubscribePacket( long  subscriberId,HashMap<String,Object>  subscriberProfile )
	{
		super();
		
		super.setContactId(subscriberId).setSubscriberProfile( subscriberProfile );
	}
	
	public  SubscribePacket(ByteBuf  buf )
	{
		super( buf,0x00 );
		
		super.setContactId(buf.readLongLE()).setSubscriberProfile( new  HashMap<String,Object>().addEntries((java.util.Map<String,Object>)  JsonUtils.fromJson(PAIPUtils.decode(buf))) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  Map<String,Object>  subscriberProfile = new  HashMap<String,Object>();

	public  void  writeTo(  ByteBuf  buf )
	{
		ByteBuf  subscriberProfileByteBuf = PAIPUtils.encode( JsonUtils.toJson(subscriberProfile == null ? new  HashMap<String,Object>() : subscriberProfile) );  write( buf,Unpooled.buffer(8).writeLongLE(contactId).writeBytes(subscriberProfileByteBuf),PAIPPacketType.SUBSCRIBE );  subscriberProfileByteBuf.release();
	}
}