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

import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  io.netty.buffer.ByteBuf;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@ToString(callSuper=true )
public  class  ConnectAckPacket    extends  PendingAckPacket<ConnectAckPacket>
{
    public  final  static  int  CONNECTION_ACCEPTED  = 0x00;
    
    public  final  static  int  UNNACEPTABLE_PROTOCOL_VERSION = 0x01;
    
    public  final  static  int  IDENTIFIER_REJECTED  = 0x02;
    
    public  final  static  int  BAD_USERNAME_OR_PASSWORD = 0x03;
    
    public  final  static  int  NOT_AUTHORIZED = 0x04;
    
    public  ConnectAckPacket(ByteBuf  buf)
    {
    	super(buf);
    	
    	this.setSessionPresent(buf.readByte() == 0x01).setResponseCode( buf.readByte() );
    }
    @Override
    public  ByteBuf  writeToVariableByteBuf(   ByteBuf  variableBuf )
	{
		return  super.writeToVariableByteBuf(variableBuf).writeByte(this.isSessionPresent? 0x01 : 0x00).writeByte( this.responseCode );
	}
    
    public  ConnectAckPacket( long  connectPacketId, int  responseCode,boolean  isSessionPresent )
    {
    	super(PAIPPacketType.CONNECT_ACK,0,connectPacketId);
    	
    	setResponseCode(responseCode).setSessionPresent(   isSessionPresent );
    }
    @Override
	public  int     getInitialVariableByteBufferSize()
	{
		return  2 +super.getInitialVariableByteBufferSize();
	}
    	
    @Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  int  responseCode;
    @Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  boolean     isSessionPresent;
    /*
	public  void  writeTo(  ByteBuf  buf )
	{
		write( buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.CONNECT_ACK );
	}
	*/
}