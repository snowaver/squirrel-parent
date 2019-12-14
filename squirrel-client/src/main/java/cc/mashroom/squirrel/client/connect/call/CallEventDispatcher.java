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

import  cc.mashroom.squirrel.client.connect.call.webrtc.ClientObserver;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.util.event.EventDispather;

public  class  CallEventDispatcher  extends  EventDispather<CallListener>
{
	public  void  onRoomCreated( ClientObserver  call )
	{
		for( CallListener  listener : super.listeners )  listener.onRoomCreated( call );
	}
	
	public  void  onStart( ClientObserver  call )
	{
		for( CallListener  listener : super.listeners )  listener.onStart( call );
	}
	
	public  void  onClose( ClientObserver  call,boolean  proactively,CloseCallReason  reason )
	{
		for( CallListener  listener : super.listeners )  listener.onClose( call,proactively,reason );
	}
	
	public  void  onError( ClientObserver  call,CallError  error,Throwable cause )
	{
		for( CallListener  listener : super.listeners )  listener.onError( call,error,cause );
	}
}