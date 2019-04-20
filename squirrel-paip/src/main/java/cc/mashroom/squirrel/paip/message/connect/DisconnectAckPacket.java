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
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@ToString(callSuper=true )
public  class  DisconnectAckPacket  extends  Packet<DisconnectAckPacket>
{
	public  final  static  int  REASON_PROACTIVELY   = 0x00;
	
	public  final  static  int  REASON_NETWORK_ERROR = 0x01;
	
	public  final  static  int  REASON_REMOTE_LOGIN  = 0x02;
		
	public  DisconnectAckPacket(int reason )
	{
		super( new  Header(PAIPPacketType.DISCONNECT_ACK) );
		
		this.setReason( reason );
	}
	
	public  DisconnectAckPacket(     ByteBuf  buf )
	{
		super( buf,0x00 );
		
		this.setReason( buf.readShortLE() );
	}
	
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  int   reason;

	public  ByteBuf  writeToVariableByteBuf(  ByteBuf  variableByteBuf )
	{
		return  variableByteBuf.writeShortLE( this.reason );
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  2+ super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(ByteBuf  byteBuf )
	{
		write( byteBuf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.DISCONNECT_ACK );
	}
	*/
}