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
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  CloseCallPacket  extends  AbstractCallPacket<CloseCallPacket>
{
	public  CloseCallPacket( long  contactId,long  roomId,CloseCallReason  reason )
	{
		super( new  Header(PAIPPacketType.CLOSE_CALL),roomId );
		
		setContactId(contactId).setReason(reason );
	}
	
	public  ByteBuf  writeToVariableByteBuf( ByteBuf  byteBuf )
	{
		return  super.writeToVariableByteBuf(byteBuf).writeLongLE(contactId).writeShortLE( reason.getValue() );
	}
	
	public  CloseCallPacket( ByteBuf buf )
	{
		super( buf,0x00 );
		
		this.setContactId(buf.readLongLE()).setReason( CloseCallReason.valueOf(buf.readShortLE()) );
	}
	
	@Setter( value=   AccessLevel.PUBLIC )
	@Getter
	@Accessors(chain=true)
	private  CloseCallReason  reason;
		
	public  int  getInitialVariableByteBufferSize()
	{
		return  10 +  super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.CLOSE_CALL );
	}
	*/
}