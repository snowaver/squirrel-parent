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

import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;

public  class  CallEventDispatcher
{
	private  static  List<CallListener>  LISTENERS= new  CopyOnWriteArrayList<CallListener>();
	
	public  static  void  addListener(    CallListener  listener )
	{
		if( listener != null )
		{
			LISTENERS.add(      listener );
		}
	}
	
	public  static  void  onRoomCreated(   Call  call )
	{
		for( CallListener  listener : LISTENERS )
		{
			listener.onRoomCreated( call );
		}
	}

	public  static  void  removeListener( CallListener  listener )
	{
		if( listener != null )
		{
			LISTENERS.remove(   listener );
		}
	}
	
	public  static  void  onStart(   Call  call )
	{
		for( CallListener  listener : LISTENERS )
		{
			listener.onStart( call );
		}
	}
	
	public  static  void  onClose(   Call  call,boolean  proactively,CloseCallReason  reason )
	{
		for( CallListener  listener : LISTENERS )
		{
			listener.onClose( call,proactively,reason );
		}
	}
	
	public  static  void  onError( Call  call , CallError  error )
	{
		for( CallListener  listener : LISTENERS )
		{
			listener.onError( call,error );
		}
	}
}