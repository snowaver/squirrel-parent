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
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.message.Packet;

import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString
public  class  GroupChatInvitedPacket    extends  Packet  < GroupChatInvitedPacket >
{
	public  GroupChatInvitedPacket( long  contactId,long  groupId )
	{
		super();
		
		super.setQos(0,contactId).setGroupId( groupId );
	}
	
	public  GroupChatInvitedPacket(   ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		super.setContactId(byteBuf.readLongLE()).setGroupId( byteBuf.readLongLE() );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  long  groupId;
	
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,Unpooled.buffer(16).writeLongLE(contactId).writeLongLE(groupId),PAIPPacketType.GROUP_CHAT_INVITED );
	}
}