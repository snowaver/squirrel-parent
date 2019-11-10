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
package cc.mashroom.squirrel.client;

import  java.util.concurrent.TimeUnit;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  io.netty.util.concurrent.Future;
import  io.netty.util.concurrent.GenericFutureListener;
import  lombok.AllArgsConstructor;

@AllArgsConstructor

public class DefaultGenericFutureListener<F extends Future<?>>  implements  GenericFutureListener<F>
{
	private       TransportLifecycleHandlerAdapter  arcontext;
	
	private  InboundHandler  handler;
	
	private  Packet  packet;
	
	private  long  writeTimeout;
	
	private  TimeUnit  timeunit;
	
	public  void  operationComplete(  Future  future )
	{
		if( future.isDone()&& future.isSuccess() )
		{
			if( packet.getHeader().getAckLevel()== 1 )
			{
				handler.pend( arcontext, packet, writeTimeout, timeunit );
			}
			else
			{
				PacketEventDispatcher.onSent(this.arcontext.getPacketListeners(),this.packet, TransportState.SENT );
			}
		}
	}
}