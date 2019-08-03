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
package cc.mashroom.squirrel.paip.message.chat;

import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  GroupChatEventPacket  extends  Packet<GroupChatEventPacket>
{
	public  final  static  int  EVENT_GROUP_ADDED     = 0x00;
	
	public  final  static  int  EVENT_GROUP_UPDATED   = 0x01;
	
	public  final  static  int  EVENT_GROUP_REMOVED   = 0x02;
	
	public  final  static  int  EVENT_MEMBER_ADDED    = 0x03;
	
	public  final  static  int  EVENT_MEMBER_UPDATED  = 0x04;
	
	public  final  static  int  EVENT_MEMBER_REMOVED  = 0x05;
	
	public  GroupChatEventPacket( ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		this.setGroupId(byteBuf.readLongLE()).setEvent(byteBuf.readByte()).setAttatchmentsOriginal(PAIPUtils.decode(byteBuf)).setAttatchments( new  HashMap<String,Object>().addEntries(JsonUtils.fromJson(this.attatchmentsOriginal,new  TypeReference<Map<String,Object>>(){})) );
	}
	
	public  GroupChatEventPacket( long  groupId,int  event,Map<String,?>  attatchments )
	{
		super( new  Header(PAIPPacketType.GROUP_CHAT_EVENT));
		
		setEvent(event).setGroupId(groupId).setAttatchments(attatchments);
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  long  groupId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  int  event;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  Map<String,?>  attatchments =  new  HashMap<>();
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  String  attatchmentsOriginal;

	public   int  getInitialVariableByteBufferSize()
	{
		return  9 + super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.GROUP_CHAT_INVITED );
	}
	*/
	public  ByteBuf  writeToVariableByteBuf(    ByteBuf  variableByteBuf )
	{
		return  variableByteBuf.writeLongLE(groupId).writeByte(event).writeBytes( PAIPUtils.encode(JsonUtils.toJson(attatchments)) );
	}
}