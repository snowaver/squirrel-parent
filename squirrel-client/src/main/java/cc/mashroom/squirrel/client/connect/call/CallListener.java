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

import  cc.mashroom.squirrel.paip.message.call.Candidate;
import  cc.mashroom.squirrel.paip.message.call.SDP;

public  interface  CallListener
{
	/**
	 *  this  method  will  be  triggered  when  a  new  room  is  created.
	 */
	public  void  onRoomCreated( long  roomId );
	
	public  void  onWaitingResponse( long  roomId,long  contactId );
	
	public  void  onError( long  roomId,long  contactId,CallError  error );
	
	public  void  onResponded( long  roomId,long  contactId,int  responseCode );
	
	public  void  onClose( long  roomId,long  contactId,boolean  proactive,CallState  callState );
	
	public  void  onReceivedSdp(   long  roomId,long  contactId,SDP  sdp );
	/**
	 *  call  is  connected,  udp/tcp  multimedia  packet  is  transfering  now.
	 */
	public  void  onStart( long  roomId,long  contactId );
	
	public  void  onReceivedCandidate( long  roomId,long  contactId,Candidate  candidate );
}