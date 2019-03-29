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
package cc.mashroom.squirrel.paip.message.connect;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  io.netty.buffer.ByteBuf;
import  lombok.ToString;

@ToString(    callSuper = true )
public  class  PingAckPacket  extends  Packet  <PingAckPacket>
{
	public  PingAckPacket()
	{
		super( new  Header(PAIPPacketType.PING_ACK) );
	}
	
	public  PingAckPacket( ByteBuf  byteBuf )
	{
		super( byteBuf , 0x00 );
	}
	
	public  ByteBuf  writeToVariableByteBuf( ByteBuf  variableByteBuf )
	{
		return  variableByteBuf;
	}
	/*
	public  void  writeTo( ByteBuf  byteBuf )
	{
		write( byteBuf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.PING_ACK );
	}
	*/
}