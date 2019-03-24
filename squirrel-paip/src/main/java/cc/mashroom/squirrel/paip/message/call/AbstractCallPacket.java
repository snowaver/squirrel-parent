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
import  cc.mashroom.squirrel.paip.message.Packet;

@ToString
public  abstract  class  AbstractCallPacket<T extends AbstractCallPacket<?>>  extends  Packet<T>
{	
	public  AbstractCallPacket( long roomId )
	{
		super();
		
		setRoomId(roomId);
	}
	
	public  ByteBuf  writeToVariableByteBuf( ByteBuf  byteBuf )
	{
		return  byteBuf.writeLongLE( this.roomId );
	}
	
	public  AbstractCallPacket( ByteBuf   byteBuf,int  expectedFlags )
	{
		super( byteBuf , 0x00 );
		
		this.setRoomId(byteBuf.readLongLE());
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  8 +   super.getInitialVariableByteBufferSize();
	}
	
	@Setter( value  = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  roomId;
	/*
	public  void  writeTo( ByteBuf  byteBuf )
	{
		
	}
	*/
}