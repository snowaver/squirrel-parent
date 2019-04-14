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
import cc.mashroom.util.JsonUtils;
import cc.mashroom.util.collection.map.HashMap;
import cc.mashroom.util.collection.map.Map;
import cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  GroupChatEventPacket  extends  Packet  <GroupChatEventPacket>
{
	public  final  static  int  EVENT_MEMBER_ADDED     = 0x00;
	
	public  final  static  int  EVENT_MEMBER_UPDATED   = 0x01;
	
	public  final  static  int  EVENT_MEMBER_REMOVED   = 0x02;
	
	public  GroupChatEventPacket( ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		this.setGroupId(byteBuf.readLongLE()).setAttatchments( new  HashMap<String,Object>().addEntries((java.util.Map<String,Object>)  JsonUtils.fromJson(PAIPUtils.decode(byteBuf))) );
	}
	
	public  GroupChatEventPacket( long  groupId,int  event,Map<String,Object>  attatchments )
	{
		super( new  Header(PAIPPacketType.GROUP_CHAT_EVENT) );
		
		this.setEvent(event).setGroupId(  groupId );
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
	private  Map<String,Object>  attatchments=new  HashMap<String,Object>();

	public   int  getInitialVariableByteBufferSize()
	{
		return  16 + super.getInitialVariableByteBufferSize();
	}
	
	public  ByteBuf  writeToVariableByteBuf(  ByteBuf  variableBuf )
	{
		return  variableBuf.writeLongLE(groupId).writeByte(event).writeBytes( PAIPUtils.encode(JsonUtils.toJson(attatchments)) );
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.GROUP_CHAT_INVITED );
	}
	*/
}