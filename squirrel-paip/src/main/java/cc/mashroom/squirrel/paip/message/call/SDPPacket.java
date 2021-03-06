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
package cc.mashroom.squirrel.paip.message.call;

import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(  callSuper = true )
public  class  SDPPacket  extends  RoomPacket<SDPPacket>
{
	public  SDPPacket( long  contactId,long roomId,SDP  sdp )
	{
		super( PAIPPacketType.CALL_SDP,0,contactId,roomId  );
		
		setSdp( sdp );
	}
	
	public  SDPPacket(  ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		setContactId(byteBuf.readLongLE()).setSdp( new  SDP(PAIPCodecUtils.decode(byteBuf),PAIPCodecUtils.decode(byteBuf)) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	private  SDP  sdp;

	public  ByteBuf  writeToVariableByteBuf(ByteBuf  variableByteBuf )
	{
		ByteBuf  typeBuf = PAIPCodecUtils.encode(this.sdp.getType() );  ByteBuf  descriptionBuf = PAIPCodecUtils.encode( this.sdp.getDescription() );  super.writeToVariableByteBuf(variableByteBuf).writeLongLE(contactId).writeBytes(typeBuf).writeBytes(descriptionBuf);  typeBuf.release();  descriptionBuf.release();  return  variableByteBuf;
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return   8+ super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())), PAIPPacketType.CALL_SDP );  
	}
	*/
}