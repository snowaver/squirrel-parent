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
package cc.mashroom.squirrel.paip.message.extensions;

import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.Packet;
import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  ByteArrayPacket  extends  Packet       <ByteArrayPacket>
{
	public  ByteArrayPacket( ByteBuf  byteBuf )
	{
		super(byteBuf,0x00 );
	}
	
	public  ByteArrayPacket(    byte[]  bytes )
	{
		super( new  Header(PAIPPacketType.BYTE_ARRAY ) );
		
		setBytes(    bytes );
	}
	
	@Setter( value    = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  byte[]  bytes;
	
	public  ByteBuf  writeToVariableByteBuf( ByteBuf  variableByteBuf )
	{
		return  variableByteBuf.writeBytes( this.bytes );
	}
}












