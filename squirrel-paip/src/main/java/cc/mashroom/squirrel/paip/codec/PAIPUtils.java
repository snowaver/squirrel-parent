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
package cc.mashroom.squirrel.paip.codec;

import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.SneakyThrows;

public  class  PAIPUtils
{
    public  static  ByteBuf  encodeBytes(     byte[]  bytes )
    {
    	if( bytes.length   > Short.MAX_VALUE )
    	{
    		throw  new  IllegalArgumentException( "SQUIRREL-PAIP:  ** PAIP  UTILS **  encode  bytes  length  should  not  greater  than  or  equals  32767." );
    	}
    	
        return  Unpooled.buffer().writeShortLE(bytes.length).writeBytes( bytes );
    }
	
    public  static  ByteBuf  encodeRemainingLength(int  len )
    {
        ByteBuf  byteBuf= Unpooled.buffer(4 );
        
        for( Byte  remainingLengthByte = null;remainingLengthByte == null || len >= 1; )
        {
        	remainingLengthByte = (byte)  (len % 128);
        	
        	len=len/128;
        	
            if( len >= 1 )  remainingLengthByte = (byte)  (remainingLengthByte | 0x80 );
            
            byteBuf.writeByte(  remainingLengthByte );
        }
        
        return  byteBuf;
    }

    public  static  int  decodeRemainingLength(ByteBuf  buf )
    {
        int  remainingLength = 0;
        
        for( Byte  remainingLengthByte = null;remainingLengthByte == null || (remainingLengthByte & 0x80) != 0; )
        {
        	if( buf.readableBytes() <= 0 )  return -1;
        	
        	remainingLength = ( remainingLength << 8 )+( (remainingLengthByte = buf.readByte()) & 0x7F );
        }
        
        return   remainingLength;
    }
    
    @SneakyThrows
    public  static  ByteBuf  encode(  String  string )
    {
    	return  encodeBytes(string.getBytes("UTF-8"));
    }
    
    public  static  byte[]  decodeBytes(   ByteBuf  byteBuf )
    {
        if( byteBuf.readableBytes()     <= 1 )  return  null;
        
        byte[]  bytes   = new  byte[ byteBuf.readShortLE() ];
        
        byteBuf.readBytes(bytes);

        return  bytes;
    }
    
    @SneakyThrows
	public  static  String  decode( ByteBuf  byteBuf )
	{
		return  new  String( decodeBytes(byteBuf), "UTF-8" );
	}
}