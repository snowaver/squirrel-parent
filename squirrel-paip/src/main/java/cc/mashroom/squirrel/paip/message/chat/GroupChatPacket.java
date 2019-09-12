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
import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  GroupChatPacket  extends  Packet  <GroupChatPacket>
{	
	public  GroupChatPacket(ByteBuf  buf )
	{
		super( buf,0x00 );
		
		super.setAckLevel(1,buf.readLongLE()).setGroupId(buf.readLongLE()).setContentType(ChatContentType.valueOf(buf.readByte())).setMd5(PAIPCodecUtils.decode(buf)).setContent( PAIPCodecUtils.decodeBytes(buf) );
	}
	
	public  GroupChatPacket( long  contactId,long  groupId,String  md5,ChatContentType  contentType,byte[]  content )
	{
		super( new  Header(PAIPPacketType.GROUP_CHAT));
		
		setContactId(contactId).setGroupId(groupId).setMd5(md5 ).setContentType( contentType ).setContent( content );
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
	
	public  int      getInitialVariableByteBufferSize()
	{
		return  17+super.getInitialVariableByteBufferSize();
	}
	
	public  ByteBuf  writeToVariableByteBuf(ByteBuf  variableBuf )
	{
		ByteBuf  contentByteBuf= PAIPCodecUtils.encodeBytes( content );  ByteBuf  md5ByteBuf = PAIPCodecUtils.encode( md5 );  variableBuf.writeLongLE(contactId).writeLongLE(groupId).writeByte(contentType.getValue()).writeBytes(md5ByteBuf).writeBytes(contentByteBuf);  md5ByteBuf.release();  contentByteBuf.release();  return  variableBuf;
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.GROUP_CHAT );
	}
	*/
}