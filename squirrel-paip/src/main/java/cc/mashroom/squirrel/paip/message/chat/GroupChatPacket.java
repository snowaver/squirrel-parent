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
import  lombok.AllArgsConstructor;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@AllArgsConstructor
@ToString
public  class  GroupChatPacket  extends  Packet  <GroupChatPacket>
{
	public  GroupChatPacket( long  contactId,long  groupId,String  md5,ChatContentType  contentType,byte[]  content )
	{
		super();
		
		super.setContactId(contactId).setGroupId(groupId).setMd5(md5).setContentType(contentType).setContent( content );
	}
	
	public  GroupChatPacket(ByteBuf  buf )
	{
		super( buf,0x00 );
		
		super.setContactId(buf.readLongLE()).setGroupId(buf.readLongLE()).setContentType(ChatContentType.valueOf(buf.readByte())).setMd5(PAIPUtils.decode(buf)).setContent( PAIPUtils.decodeBytes(buf) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long groupId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  String  md5 = "";
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  ChatContentType  contentType;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  byte[]   content;
	
	public  void  writeTo(  ByteBuf  buf )
	{
		ByteBuf  contentByteBuf = PAIPUtils.encodeBytes( content );  ByteBuf  md5ByteBuf = PAIPUtils.encode( md5 );  write( buf,Unpooled.buffer(17).writeLongLE(contactId).writeLongLE(groupId).writeByte(contentType.getValue()).writeBytes(md5ByteBuf).writeBytes(contentByteBuf),PAIPPacketType.GROUP_CHAT );  md5ByteBuf.release();  contentByteBuf.release();
	}
}