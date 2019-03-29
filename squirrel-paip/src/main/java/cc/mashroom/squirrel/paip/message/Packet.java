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

import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

import  java.util.concurrent.atomic.AtomicLong;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.squirrel.paip.codec.PAIPUtils;

@ToString
public  abstract  class  Packet  <T extends Packet>
{
	/*
	public  Packet()
	{
		this.setHeader( new  Header() ).setId(   Packet.forId( DateTime.now( DateTimeZone.UTC ).getMillis() ) );
	}
	*/
	public  Packet(       Header  header )
	{
		this.setHeader( header.setId(Packet.forId(DateTime.now(DateTimeZone.UTC).getMillis()) ) );
	}
	/*
	public  abstract  void  writeTo(ByteBuf  buf );
	*/
	//  the  id  should  be  greater  than  previous  id,  else  a  new  id  ( the  old  id  plus  one )  will  be  generated.
	public  static  long  forId(long  id )
	{
		return  ID_GENERATOR.get() >= id ? ID_GENERATOR.incrementAndGet() : ID_GENERATOR.addAndGet( (id-ID_GENERATOR.get()) );
	}
	@Getter
	@Accessors(  chain = true )
	protected  long  contactId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(  chain = true )
	protected  Header   header;
	
	public  long  getId()
	{
		return  header.getId();
	}
	
	protected  final  static  AtomicLong  ID_GENERATOR  = new  AtomicLong( 0x00 );
	
	public  T  setContactId(      long  contactId )
	{
		this.contactId        = contactId;  return  (T)  this;
	}
	
	public  T  setQos(  int  qos, long  contactId )
	{
		if(    contactId <= 0 )
		{
			throw  new  IllegalArgumentException("SQUIRREL-PAIP:  ** PACKET **  contact  id  is  invalidate." );
		}
		
		this.setContactId(contactId).getHeader().setQos(qos );  return  (T)  this;
	}
		
	public  int  getInitialVariableByteBufferSize()
	{
		return  0;
	}
	
	public  abstract  ByteBuf  writeToVariableByteBuf( ByteBuf  variableByteBuf );
		
	public  void  write( ByteBuf  writableByteBuf )
	{
		ByteBuf  variableByteBuf= writeToVariableByteBuf( Unpooled.buffer(getInitialVariableByteBufferSize()) );  ByteBuf  decodeRemainingLengthByteBuf = PAIPUtils.encodeRemainingLength( variableByteBuf.readableBytes() );
		
		try
		{
			writableByteBuf.writeByte(header.toByte()).writeByte(0).writeShortLE(header.getPacketType().getValue()).writeLongLE(getId()).writeBytes(decodeRemainingLengthByteBuf).writeBytes( variableByteBuf );
		}
		finally
		{
			decodeRemainingLengthByteBuf.release();     variableByteBuf.release();
		}
	}
	
	public  Packet( ByteBuf  byteBuf, Integer  expectedFlags )
	{
		this.setHeader(   new  Header(byteBuf.resetReaderIndex(),expectedFlags) );
	}
}