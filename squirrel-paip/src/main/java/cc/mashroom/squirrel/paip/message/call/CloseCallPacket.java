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
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString
public  class  CloseCallPacket  extends  Packet  <CloseCallPacket>  //  implements  Receiptable
{
	public  CloseCallPacket( long  contactId,long  callId )
	{
		super();
		
		this.setQos(1,contactId).setCallId( callId );
	}
	
	public  CloseCallPacket( ByteBuf buf )
	{
		super( buf,0x00 );
		
		this.setCallId( buf.readLongLE() ).setContactId( buf.readLongLE() );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  callId;
	
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,Unpooled.buffer(16).writeLongLE(callId).writeLongLE(contactId),PAIPPacketType.CLOSE_CALL );
	}


}