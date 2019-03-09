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
import  io.netty.util.AttributeKey;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString
public  class  CallPacket  extends  Packet  <CallPacket>  //  implements  Receiptable
{
	public  final  static  AttributeKey<Long>  CALL_CONTACT_ID = AttributeKey.newInstance( "CALL_CONTACT_ID" );
	
	public  final  static  AttributeKey<Long>  CALL_ID = AttributeKey.newInstance( "CALL_ID" );
	
	public  CallPacket( long  contactId, long  callId, CallContentType  contentType )
	{
		super();
		
		super.setQos( 1, contactId ).setCallId(callId).setContentType( contentType );
	}
	
	public  CallPacket( ByteBuf  byteBuf )
	{
		super( byteBuf,0x00 );
		
		this.setCallId(byteBuf.readLongLE()).setContactId(byteBuf.readLongLE()).setContentType( CallContentType.valueOf( byteBuf.readByte()) );
	}

	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  callId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  CallContentType  contentType;

	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,Unpooled.buffer(17).writeLongLE(this.callId).writeLongLE(contactId).writeByte(contentType.getValue()),PAIPPacketType.CALL );
	}
}