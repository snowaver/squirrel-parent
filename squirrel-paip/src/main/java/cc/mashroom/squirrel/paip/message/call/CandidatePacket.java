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
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.Header;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;

@ToString(callSuper=true )
public  class  CandidatePacket      extends  AbstractCallPacket<CandidatePacket>
{	
	public  CandidatePacket( long  contactId,long  roomId,Candidate  candidate )
	{
		super( new  Header(PAIPPacketType.CALL_CANDIDATE) , roomId );
		
		super.setContactId(contactId).setCandidate( candidate );
	}
	
	public  CandidatePacket( ByteBuf  buf )
	{
		super(  buf, 0x00 );
		
		this.setContactId(buf.readLongLE()).setCandidate( new  Candidate(PAIPCodecUtils.decode(buf),buf.readIntLE(),PAIPCodecUtils.decode(buf)) );
	}
	
	@Setter(  value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain=true )
	private  Candidate  candidate;
	
	public  int  getInitialVariableByteBufferSize()
	{
		return   12  + super.getInitialVariableByteBufferSize();
	}
	
	public  ByteBuf  writeToVariableByteBuf(  ByteBuf  byteBuf )
	{
		ByteBuf  candidateByteBuf = PAIPCodecUtils.encode(candidate.getCandidate() );  ByteBuf  idBuf= PAIPCodecUtils.encode( candidate.getId() );  super.writeToVariableByteBuf(byteBuf).writeLongLE(contactId).writeBytes(idBuf).writeIntLE(candidate.getLineIndex()).writeBytes(candidateByteBuf);  idBuf.release();  candidateByteBuf.release();  return  byteBuf;
	}
	/*
	public  void  writeTo(      ByteBuf  buf )
	{
		write(buf,this.writeToVariableByteBuf(Unpooled.buffer(this.getInitialVariableByteBufferSize())),PAIPPacketType.CALL_CANDIDATE );
	}
	*/
}