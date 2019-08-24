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

import  java.util.ArrayList;
import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;

import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.client.storage.Storage;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;

public  class  PacketEventDispatcher
{
	private  final  static  List<PacketListener>  LISTENERS = new  CopyOnWriteArrayList<PacketListener>( Lists.newArrayList( Storage.INSTANCE) );
	
	public  static  void  onSent(Packet packet,TransportState transportState )
	{
		for(   PacketListener  listener : LISTENERS )
		{
			listener.onSent( packet,transportState );
		}
	}
	
	public  static  List<PacketListener>  getAllListeners()
	{
		return  new  ArrayList<PacketListener>(LISTENERS );
	}
	
	public  static  void  addListener(    PacketListener  listener )
	{
		if( listener != null )
		{
			LISTENERS.add(    listener );
		}
	}
	
	public  static  void  removeListener( PacketListener  listener )
	{
		if( listener != null )
		{
			LISTENERS.remove( listener );
		}
	}
		
	public  static  void  onReceived(      Packet  packet )
	{
		for( PacketListener  listener : LISTENERS )
		{
			listener.onReceived(packet );
		}
	}
	
	public  static  boolean  onBeforeSend( Packet  packet )  throws  Throwable
	{
		for( PacketListener  listener : LISTENERS )
		{
			if( ! listener.onBeforeSend( packet ) )
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
				LISTENERS.add(  index , listener );
			}
			else
			{
				throw  new  IllegalStateException( "SQUIRREL-CLIENT:  ** PACKET  EVENT  DISPATCHER **  listener  index  0  is  not  permitted" );
			}
		}
	}
}