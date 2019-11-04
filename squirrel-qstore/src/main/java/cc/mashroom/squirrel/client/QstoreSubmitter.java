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
import java.util.concurrent.atomic.AtomicLong;

import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.squirrel.paip.message.extensions.ByteArrayPacket;
import  cc.mashroom.squirrel.transport.TransportAndConnectivityGuarantorHandlerAdapter;
import  cc.mashroom.squirrel.transport.TransportFuture;
import  cc.mashroom.squirrel.transport.TransportFutureListener;
import lombok.Getter;

public  class  QstoreSubmitter  extends  TransportAndConnectivityGuarantorHandlerAdapter  implements  TransportFutureListener<PendingAckPacket<?>>
{
	@Getter
	private  AtomicLong  counter = new  AtomicLong(  0 );
	
	public  void  submit( ByteArrayPacket  packet )
	{
		write(packet).addTransportFutureListener( this );
	}
	@Override
	public  void  onComplete(   TransportFuture<PendingAckPacket<?>>   transportFuture )
	{
		counter.incrementAndGet();
	}
}
