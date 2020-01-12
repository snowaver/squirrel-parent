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

import  java.io.IOException;
import  java.util.Arrays;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.util.event.EventDispather;

public  class  PacketEventDispatcher  extends  EventDispather  <PacketEventListener>
{
	public  PacketEventDispatcher( PacketEventListener    ...  listeners )
	{
		super.listeners.addAll( Arrays.asList( listeners ) );
	}
	
	public  void  onReceived(   Packet  packet )
	{
		for( PacketEventListener  listener : this.listeners )  listener.onReceived(   packet );
	}
	
	public  void  onSent(  Packet  packet,TransportState  transportState )
	{
		for( PacketEventListener  listener : this.listeners )  listener.onSent(  packet,transportState );
	}
	
	public  void  onBeforeSend( Packet  packet )       throws  IOException
	{
		for( PacketEventListener  listener : this.listeners )  listener.onBeforeSend( packet );
	}
}