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
import  cc.mashroom.squirrel.paip.message.SystemPacket;

import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  ChatGroupEventPacket     extends  SystemPacket<ChatGroupEventPacket>
{
	public  final  static  int  EVENT_GROUP_ADDED     = 0x00;
	
	public  final  static  int  EVENT_GROUP_UPDATED   = 0x01;
	
	public  final  static  int  EVENT_GROUP_REMOVED   = 0x02;
	
	public  final  static  int  EVENT_MEMBER_ADDED    = 0x03;
	
	public  final  static  int  EVENT_MEMBER_UPDATED  = 0x04;
	
	public  final  static  int  EVENT_MEMBER_REMOVED  = 0x05;
	
	public  ChatGroupEventPacket( ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		this.setGroupId(byteBuf.readLongLE()).setEvent(byteBuf.readByte()).setAttatchments( PAIPCodecUtils.decode(byteBuf) );
	}
	
	public  ChatGroupEventPacket( long  groupId,int  event,String  clusterNodeId,String  attatchments )
	{
		super( PAIPPacketType.GROUP_CHAT_EVENT,0, 0,      clusterNodeId );
		
		this.setEvent(event).setGroupId(groupId).setAttatchments(   attatchments );
	}
	
	public   int  getInitialVariableByteBufferSize()
	{
		return  9 + super.getInitialVariableByteBufferSize();
	}
	
	public  ByteBuf    writeToVariableByteBuf(   ByteBuf  variableByteBuf)
	{
		ByteBuf  attatchmentsBuf = PAIPCodecUtils.encode(      this.attatchments );  super.writeToVariableByteBuf(variableByteBuf).writeLongLE(groupId).writeByte(event).writeBytes(attatchmentsBuf);  attatchmentsBuf.release();  return  variableByteBuf;
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
	private  String  attatchments;
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.GROUP_CHAT_INVITED );
	}
	*/
}