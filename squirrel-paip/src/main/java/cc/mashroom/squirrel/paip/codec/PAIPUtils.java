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

import java.io.UnsupportedEncodingException;

import  cc.mashroom.squirrel.paip.message.Packet;

public  class  PAIPUtils
{
    public  static  ByteBuf  encodeRemainingLength(  int  length )
    {
        ByteBuf  byteBuf = Unpooled.buffer( 4 );
        
        for( Byte  remainingLengthByte = null;remainingLengthByte == null || length >= 1; )
        {
        	remainingLengthByte=(byte)  (length%128);
        	
        	length = length/ 128;
        	
            if( length >= 1 )
            {
                remainingLengthByte = (byte)  (remainingLengthByte | 0x80);
            }
            
            byteBuf.writeByte( remainingLengthByte );
        }
        
        return  byteBuf;
    }
	
    public  static  int  decodeRemainingLength( ByteBuf  byteBuf )
    {
        int  remainingLength = 0;
        
        for( Byte  remainingLengthByte = null;remainingLengthByte == null || (remainingLengthByte & 0x80) != 0; )
        {
        	if( byteBuf.readableBytes() <= 0 )
        	{
        		return  -1;
        	}
        	
        	remainingLength = (remainingLength << 8)+((remainingLengthByte = byteBuf.readByte()) & 0x7F);
        }
        
        return   remainingLength;
    }
    
    public  static  ByteBuf  encodeBytes( byte[]    bytes )
    {
        return  Unpooled.buffer(2).writeShortLE(bytes.length).writeBytes( bytes );
    }
    
    public  static  byte[]  decodeBytes( ByteBuf  byteBuf )
    {
        if( byteBuf.readableBytes() <= 1 )
        {
            return  null;
        }
        
        byte[]  bytes = new  byte[ byteBuf.readShortLE() ];
        
        byteBuf.readBytes(bytes);

        return  bytes;
    }
    
    public  static  ByteBuf  encode(  String  string )
    {
    	try
    	{
			return  encodeBytes(string.getBytes("UTF-8") );
		}
    	catch( UnsupportedEncodingException  e )
    	{
    		return  null;
		}
    }
    
	public  static  String  decode( ByteBuf  byteBuf )
	{
		try
		{
			return  new  String( decodeBytes( byteBuf ),"UTF-8" );
		}
		catch( UnsupportedEncodingException  e )
		{
			return  null;
		}
	}
	
    public  static  ByteBuf  encodePacket(   Packet  <?>  packet )
    {
    	ByteBuf  byteBuf = Unpooled.buffer( 2 );
    	
        packet.writeTo(byteBuf );
        
        return   byteBuf;
    }
}