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
package cc.mashroom.squirrel.client.connect;

import  java.util.HashSet;
import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;

import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.client.storage.Storage;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;

public  class  PacketEventDispatcher
{
	private  final  static  List<PacketListener>  listeners = new  CopyOnWriteArrayList<PacketListener>( Lists.newArrayList( Storage.INSTANCE) );
	
	public  static  void  sent( Packet  packet,TransportState  transportState )  throws  Exception
	{
		for( PacketListener  listener : listeners )
		{
			listener.sent( packet,transportState );
		}
	}
	
	public  static  List<PacketListener>  getAllListeners()
	{
		return  Lists.newArrayList( new  HashSet<PacketListener>(listeners) );
	}
	
	public  static  void  addListener(      PacketListener  listener )
	{
		if( listener != null )
		{
			listeners.add(    listener );
		}
	}
	
	public  static  void  removeListener(   PacketListener  listener )
	{
		if( listener != null )
		{
			listeners.remove( listener );
		}
	}
		
	public  static  void  received( Packet packet )  throws  Exception
	{
		for( PacketListener  listener : listeners )
		{
			listener.received(  packet );
		}
	}
	
	public  static  boolean  beforeSend(   Packet  packet )  throws  Exception
	{
		for( PacketListener  listener : listeners )
		{
			if( ! listener.beforeSend(   packet ) )
			{
				return  false;
			}
		}
		
		return  true;
	}
	
	public  static  void  addListener( int  index , PacketListener  listener )
	{
		if( listener != null )
		{
			if( index   >= 1 )
			{
				listeners.add(  index , listener );
			}
			else
			{
				throw  new  IllegalStateException( "SQUIRREL-CLIENT:  ** PACKET  EVENT  DISPATCHER **  listener  index  0  is  not  permitted" );
			}
		}
	}
}