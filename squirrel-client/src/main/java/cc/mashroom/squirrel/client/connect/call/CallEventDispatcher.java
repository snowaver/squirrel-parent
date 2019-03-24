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
package cc.mashroom.squirrel.client.connect.call;

import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;

import  cc.mashroom.squirrel.paip.message.call.Candidate;
import  cc.mashroom.squirrel.paip.message.call.SDP;

public  class  CallEventDispatcher
{
	protected  static  List<CallListener>  listeners     = new  CopyOnWriteArrayList<CallListener>();
	
	public  static  void  addListener(    CallListener  listener )
	{
		if( listener != null )
		{
			listeners.add( listener );
		}
	}

	public  static  void  removeListener( CallListener  listener )
	{
		if( listener != null )
		{
			listeners.remove( listener );
		}
	}
	
	public  static  void  onRoomCreated(     long  roomId )
	{
		for( CallListener  listener : listeners )
		{
			listener.onRoomCreated( roomId );
		}
	}
	
	public  static  void  onResponded( long  roomId, long  contactId, int  responseCode )
	{
		for( CallListener  listener : listeners )
		{
			listener.onResponded( roomId,contactId,responseCode );
		}
	}
	
	public  static  void  onReceivedSdp(   long  roomId,long  contactId,SDP  sdp )
	{
		for( CallListener  listener : listeners )
		{
			listener.onReceivedSdp( roomId,contactId,sdp );
		}
	}
	
	public  static  void  onClose( long  roomId,long  contactId,boolean  proactive,CallState  state )
	{
		for( CallListener  listener : listeners )
		{
			listener.onClose( roomId, contactId,proactive,state );
		}
	}
		
	public  static  void  onWaitingResponse( long  roomId,long  contactId )
	{
		for( CallListener  listener : listeners )
		{
			listener.onWaitingResponse( roomId,contactId );
		}
	}
	
	public  static  void  onError( long  roomId,long  contactId,CallError  error )
	{
		for( CallListener  listener : listeners )
		{
			listener.onError(   roomId, contactId, error );
		}
	}
	
	public  static  void  onStart( long  roomId, long  contactId )
	{
		for( CallListener  listener : listeners )
		{
			listener.onStart( roomId,contactId );
		}
	}
	
	public  static  void  onReceivedCandidate(    long  roomId,long  contactId,Candidate  candidate )
	{
		for( CallListener  listener : listeners )
		{
			listener.onReceivedCandidate( roomId , contactId , candidate );
		}
	}
}