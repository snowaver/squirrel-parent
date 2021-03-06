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

import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;
import  io.netty.handler.codec.CorruptedFrameException;
import  io.netty.util.AttributeKey;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(   callSuper = true )
public  class  ConnectPacket  extends  Packet  <ConnectPacket>
{
	public  final  static  AttributeKey<Integer>  PROTOCOL_VERSION  = AttributeKey.newInstance( "PROTOCOL_VERSION" );
	
	public  final  static  int  CURRENT_PROTOCOL_VERSION  = 1;
	
	public  final  static  AttributeKey<Boolean>  CONNECT_STATUS = AttributeKey.newInstance( "CONNECT_STATUS" );
	
	public  final  static  AttributeKey<Long>  USER_ID = AttributeKey.newInstance( "USER_ID" );
	
	public  final  static  AttributeKey<Integer>  KEEPALIVE = AttributeKey.newInstance( "KEEPALIVE" );
	
	public  ConnectPacket( Channel  channel,ByteBuf  byteBuf )
	{
		super( byteBuf, 0x00 );
		
		this.protocolName = PAIPCodecUtils.decode(   byteBuf);
		
		if( !"PAIP".equals( protocolName ) )
		{
			throw  new  CorruptedFrameException( String.format("SQUIRREL-PAIP:  ** CONNECT  PACKET **  invalid  protocol:  %s",protocolName) );
		}
        
		channel.attr(PROTOCOL_VERSION).set( this.protocolVersion =byteBuf.readByte() );
		
        this.keepalive   = byteBuf.readUnsignedShort();

        this.setAccessKey( PAIPCodecUtils.decode(    byteBuf)).setSecretKey( PAIPCodecUtils.decodeBytes( byteBuf ) );
	}
	
	public  ConnectPacket( String  accessKey,byte[]  secretKey,int  keepalive )
	{
		super( PAIPPacketType.CONNECT,1,0 );
		
		this.setAccessKey(accessKey).setSecretKey(secretKey).setKeepalive( keepalive );
	}
	
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  int  protocolVersion;
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  String  accessKey;
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  int  keepalive;
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  byte[]  secretKey;
	@Setter( value = AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  String  protocolName;

	public  ByteBuf  writeToVariableByteBuf(ByteBuf  byteBuf )
	{
		ByteBuf  protocolByteBuf = PAIPCodecUtils.encode( "PAIP" );  ByteBuf  accessKeyByteBuf = PAIPCodecUtils.encode( accessKey );  ByteBuf  secretKeyByteBuf = PAIPCodecUtils.encodeBytes( secretKey );  byteBuf.writeBytes(protocolByteBuf).writeByte(CURRENT_PROTOCOL_VERSION).writeShortLE(keepalive).writeBytes(accessKeyByteBuf).writeBytes(secretKeyByteBuf);  protocolByteBuf.release();  accessKeyByteBuf.release();  secretKeyByteBuf.release();  return  byteBuf;
	}
	
	public  int      getInitialVariableByteBufferSize()
	{
		return  3  + super.getInitialVariableByteBufferSize();
	}
	/*
	public  void  writeTo(ByteBuf  byteBuf )
	{
		super.write( byteBuf , this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())), PAIPPacketType.CONNECT );
	}
	*/
}