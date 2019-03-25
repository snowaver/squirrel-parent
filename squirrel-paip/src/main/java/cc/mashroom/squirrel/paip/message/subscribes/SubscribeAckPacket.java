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

import  cc.mashroom.squirrel.paip.message.Packet;
import cc.mashroom.util.JsonUtils;
import cc.mashroom.util.collection.map.HashMap;
import cc.mashroom.util.collection.map.Map;
import cc.mashroom.squirrel.paip.codec.PAIPUtils;
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
@ToString
public  class  SubscribeAckPacket  extends  Packet<SubscribeAckPacket>
{
	public  final  static  int  ACK_IGNORE  = 0x03;
	
	public  final  static  int  ACK_DENY    = 0x05;
	
	public  final  static  int  ACK_ACCEPT  = 0x07;
	
	public  SubscribeAckPacket( long  contactId , int  responseCode , Map<String, Object>  subscribeeProfile )
	{
		super();
		
		super.setContactId(contactId).setResponseCode(responseCode).setSubscribeeProfile( subscribeeProfile );
	}
	
	public  SubscribeAckPacket( ByteBuf  byteBuf  )
	{
		super( byteBuf,0x00 );
		
		super.setContactId(byteBuf.readLongLE()).setResponseCode(byteBuf.readByte()).setSubscribeeProfile( new  HashMap<String,Object>().addEntries((java.util.Map<String,Object>)  JsonUtils.fromJson(PAIPUtils.decode(byteBuf))) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  int  responseCode;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  Map<String,Object>  subscribeeProfile = new  HashMap<String,Object>();
	
	public  ByteBuf  writeToVariableByteBuf(ByteBuf  variableByteBuf )
	{
		ByteBuf  subscribeeProfileByteBuf = PAIPUtils.encode( JsonUtils.toJson(subscribeeProfile == null ? new  HashMap<String,Object>() : subscribeeProfile) );  variableByteBuf.writeLongLE(contactId).writeByte(responseCode).writeBytes(subscribeeProfileByteBuf);  subscribeeProfileByteBuf.release();  return  variableByteBuf;
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  9+super.getInitialVariableByteBufferSize();
	}
	
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.SUBSCRIBE_ACK );
	}
}