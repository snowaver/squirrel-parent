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
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;
import  cc.mashroom.squirrel.paip.codec.PAIPCodecUtils;
import  cc.mashroom.squirrel.paip.message.Packet;

@ToString(callSuper=true )
public  abstract  class  SystemPacket<T extends SystemPacket<?>>  extends  Packet<T>
{	
	public  SystemPacket( PAIPPacketType  packetType,int  ackLevel,long  contactId ,String  clusterNodeId )
	{
		super( packetType,ackLevel,    contactId );
		
		this.setClusterNodeId(    clusterNodeId  );
	}
	
	public  ByteBuf  writeToVariableByteBuf(  ByteBuf  byteBuf )
	{
		ByteBuf  clusterNodeIdBuf = PAIPCodecUtils.encode(     this.clusterNodeId );  byteBuf.writeBytes(clusterNodeIdBuf);  clusterNodeIdBuf.release();  return  byteBuf;
	}
	@Setter
	@Getter
	@Accessors(chain=true)
	private  String  clusterNodeId;
	
	public  SystemPacket( ByteBuf   byteBuf,int  expectedFlags )
	{
		super(    byteBuf , 0x00 );
		
		this.setClusterNodeId( PAIPCodecUtils.decode(byteBuf) );
	}
	
	public  int  getInitialVariableByteBufferSize()
	{
		return  super.getInitialVariableByteBufferSize();
	}
}