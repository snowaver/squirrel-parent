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

@ToString(  callSuper = true )
public  class  ChatPacket  extends  Packet<ChatPacket>  //  implements  Receiptable
{	
	public  ChatPacket( ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		super.setContactId(byteBuf.readLongLE()).setSyncId(byteBuf.readLongLE()).setContentType(ChatContentType.valueOf(byteBuf.readByte())).setMd5(PAIPCodecUtils.decode(byteBuf)).setContent( PAIPCodecUtils.decodeBytes(byteBuf) );
	}
	
	public  ChatPacket( long  contactId,String  md5, ChatContentType  contentType , byte[]  content )
	{
		super( new  Header(   PAIPPacketType.CHAT ) );
		
		super.setAckLevel(1,contactId).setMd5(md5).setContentType(contentType).setContent( content );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  syncId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  String   md5= "";
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  ChatContentType  contentType;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  byte[]   content;
	
	public  ByteBuf writeToVariableByteBuf( ByteBuf  variableByteBuf )
	{
		ByteBuf  contentByteBuf = PAIPCodecUtils.encodeBytes( this.content );  ByteBuf  md5ByteBuf = PAIPCodecUtils.encode( md5 == null ? "" : md5 );  variableByteBuf.writeLongLE(contactId).writeLongLE(syncId).writeByte(contentType.getValue()).writeBytes(md5ByteBuf).writeBytes(contentByteBuf);  md5ByteBuf.release();  contentByteBuf.release();  return  variableByteBuf;
	}
	
	public  int     getInitialVariableByteBufferSize()
	{
		return   17+super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		super.getHeader().setQos(  0x01 );
		
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.CHAT );  
	}
	*/
}