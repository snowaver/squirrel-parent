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
package cc.mashroom.squirrel.paip.message;

import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  io.netty.handler.codec.CorruptedFrameException;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Getter;
import  lombok.NoArgsConstructor;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public  class  Header
{
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors(chain=true)
	private  PAIPPacketType  packetType;
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors(chain=true)
	private  long  id;
	//  0,  no  receipt  packet,  1,  with  receipt  packet  (may  be  lost  on  the  network,  but  cc.mashroom.squirrel.client.connect.PacketListener.sent(packet,TransportState.FAILED)  triggered  after  connection  error  or  timeout  with  no  receipt  packet,  you  can  resend  the  packet  again).
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors(chain=true)
	private  int  qos;
	@Setter( value= AccessLevel.PUBLIC )
	@Getter
	@Accessors(chain=true)
	private  int  remainingLength;
	
	public  byte  toByte()
	{
		return  Unpooled.buffer(1).writeByte(qos).getByte( 0 );
	}
	
	public  Header( ByteBuf  byteBuf , Integer  expectedFlags )
	{
        if( byteBuf.readableBytes()<=1 )
        {
            throw  new  CorruptedFrameException( "SQUIRREL-PAIP:  ** HEADER **  readable  bytes  MUST  be  greater  than  one." );
        }
        
        byte  headerByte = byteBuf.readByte();
        
        this.setQos((headerByte & 0x03)).setPacketType(PAIPPacketType.valueOf(byteBuf.skipBytes(1).readShortLE())).setId(byteBuf.readLongLE()).setRemainingLength( PAIPUtils.decodeRemainingLength(byteBuf) );
	}
}