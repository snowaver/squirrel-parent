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
package cc.mashroom.squirrel.client.event;

import  java.util.Collection;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;

public  class  PacketEventDispatcher
{
	public  static  void  onReceived( Collection<PacketEventListener>  listeners,Packet  packet )
	{
		for( PacketEventListener  listener : listeners )
		{
			listener.onReceived( packet );
		}
	}
	
	public  static  void  onSent(Collection<PacketEventListener> listeners,Packet  packet,TransportState  transportState  )
	{
		for( PacketEventListener  listener : listeners )
		{
			listener.onSent(packet,transportState);
		}
	}
	
	public  static  boolean  onBeforeSend(   Collection<PacketEventListener>  listeners,Packet  packet )  throws  Throwable
	{
		for( PacketEventListener  listener : listeners )
		{
			if( ! listener.onBeforeSend( packet ) )
			{
				return  false;
			}
		}
		
		return  true;
	}
}