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

import  java.io.UnsupportedEncodingException;

import  cc.mashroom.util.StringUtils;
import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.SneakyThrows;

public  class  PAIPCodecUtils
{
    public  static  ByteBuf  encodeBytes(     byte[]  bytes )
    {
    	return  Unpooled.buffer().writeBytes(encodeRemainingLength(bytes.length)).writeBytes(     bytes );
    }
	/**
	 *  write  length  to  a  new  byte  buffer  from  length  lowest  7  bits  to  highest  7  bits  in  turn  by  the  format  (remaining  flag  (1  bit,  1/0  indicates  yes/no)  +  length  content  (7  bits)).
	 */
    public  static  ByteBuf  encodeRemainingLength(int  len )
    {
        ByteBuf  byteBuf=Unpooled.buffer( 1 );
        
        for( Byte  remainingLengthByte = null;remainingLengthByte == null || len > 0; )
        {
        	remainingLengthByte = (byte)  (len % 128);
        	
        	len=len/128;
        	
            if( len> 0 )  remainingLengthByte=  (byte)  ( remainingLengthByte | 0x80 );
            
            byteBuf.writeByte(  remainingLengthByte );
        }
        
        return  byteBuf;
    }
    /**
     *  read  remaining  length  from  the  byte  buffer   by  the  format  (remaining  flag  (1  bit,  1/0  indicates  yes/no)  +  length  content  (7  bits)).
     */
    public  static  int  decodeRemainingLength(ByteBuf  buf )
    {
        int  remainingLength= 0;
        
        int  counter= 0;
        
        if(  buf.readableBytes()       <=  0 )     return  0;
        
        for( Byte  remainingLengthByte = null;;counter = counter+1 )
        {
        	if(((remainingLength = remainingLength+(((remainingLengthByte = buf.readByte()) & 0x7F) << (7*counter))) & 0x80) == 0 )
        	{
        		break  ;
        	}
        }
        
        return  remainingLength;
    }
    public  static  ByteBuf  encode( String  string,String  encode )  throws  UnsupportedEncodingException
    {
    	return  encodeBytes( StringUtils.isBlank(encode) ? string.getBytes() : string.getBytes(encode ) );
    }
    @SneakyThrows(value={UnsupportedEncodingException.class})
    public  static  ByteBuf  encode( String  string  )
    {
    	return  encode(      string,"UTF-8" );
    }
    @SneakyThrows(value={UnsupportedEncodingException.class})
    public  static  String  decode(ByteBuf  byteBuf  )
    {
    	return  decode(     byteBuf,"UTF-8" );
    }
    
    public  static  byte[]  decodeBytes(   ByteBuf  byteBuf )
    {
        if(     byteBuf.readableBytes() <= 1 )  return  null;
        
        byte[]  bytes = new  byte[ decodeRemainingLength(byteBuf) ];
        
        byteBuf.readBytes(bytes);return bytes;
    }
        
	public  static  String  decode(ByteBuf  byteBuf,String  encode )  throws  UnsupportedEncodingException
	{
		return  StringUtils.isBlank(encode) ? new  String(decodeBytes(byteBuf)) :       new  String( decodeBytes(byteBuf),encode );
	}
}