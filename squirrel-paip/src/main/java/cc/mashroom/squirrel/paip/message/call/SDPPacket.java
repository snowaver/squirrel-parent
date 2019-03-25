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
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString
public  class  SDPPacket       extends  AbstractCallPacket  <SDPPacket>
{
	public  SDPPacket( long  contactId,long  roomId,SDP  sdp )
	{
		super( roomId );
		
		this.setContactId(contactId).setSdp( sdp );
	}
	
	public  SDPPacket(  ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		setContactId(byteBuf.readLongLE()).setSdp( new  SDP(PAIPUtils.decode(byteBuf),PAIPUtils.decode(byteBuf)) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain = true )
	
	private  SDP    sdp;

	public  ByteBuf  writeToVariableByteBuf( ByteBuf  variableByteBuf )
	{
		ByteBuf  sdpBuf = sdp.toByteBuf();  super.writeToVariableByteBuf(variableByteBuf).writeLongLE(contactId).writeBytes( sdpBuf );  sdpBuf.release();  return  variableByteBuf;
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  8 +  super.getInitialVariableByteBufferSize();
	}
	
	public  void  writeTo(  ByteBuf  buf )
	{
		  write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())), PAIPPacketType.CALL_SDP );  
	}
}