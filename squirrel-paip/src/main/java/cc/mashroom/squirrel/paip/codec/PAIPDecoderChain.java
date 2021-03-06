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

import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;

import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.paip.message.Packet;
import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;

public  class   PAIPDecoderChain
{
	private List<PAIPDecoder>  decoders = new  CopyOnWriteArrayList<PAIPDecoder>( Lists.newArrayList(new  PAIPCoresDecoder()) );
	
	public  Packet<?>  decode( Channel  channel,ByteBuf  byteBuf )
	{
		for( PAIPDecoder  decoder : this.decoders )
		{
			Packet<?>  packet  =decoder.decode( channel,byteBuf );  if( packet != null )  return   packet;
		}
		
		throw  new  IllegalArgumentException( "SQUIRREL-PAIP:  ** PAIP  DECODER  CHAIN **  no  decoder  can  not  decode  the  packet." );
	}
}
