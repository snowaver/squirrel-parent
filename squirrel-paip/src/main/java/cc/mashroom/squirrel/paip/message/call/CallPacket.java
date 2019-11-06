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
import  io.netty.util.AttributeKey;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  CallPacket  extends   RoomPacket<CallPacket>
{
	public  final  static  AttributeKey<Long>  CALL_ROOM_ID  = AttributeKey.newInstance( "CALL_ROOM_ID" );
	
	public  CallPacket( long  contactId,long  roomId,CallContentType  contentType )
	{
		super( PAIPPacketType.CALL, 0, contactId, roomId );
		
		this.setContentType( contentType);
	}
	@Override
	public  int  getInitialVariableByteBufferSize()
	{
		return  9+super.getInitialVariableByteBufferSize();
	}
	
	public  CallPacket( ByteBuf  byteBuf )
	{
		super( byteBuf , 0x00 );
		
		setContactId(byteBuf.readLongLE()).setContentType( CallContentType.valueOf( byteBuf.readByte()) );
	}

	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  CallContentType  contentType;
	@Override
	public  ByteBuf  writeToVariableByteBuf(ByteBuf  variableByteBuf )
	{
		return  super.writeToVariableByteBuf(variableByteBuf).writeLongLE(this.contactId).writeByte( this.contentType.getValue() );
	}
}