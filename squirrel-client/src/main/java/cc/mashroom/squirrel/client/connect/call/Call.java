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

import  java.util.concurrent.TimeUnit;

import  org.webrtc.IceCandidate;
import  org.webrtc.SessionDescription;

import  cc.mashroom.squirrel.client.HttpOpsHandlerAdapter;
import  cc.mashroom.squirrel.client.connect.call.webrtc.ClientObserver;
import  cc.mashroom.squirrel.client.event.PacketEventListener;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.util.ObjectUtils;

public  class  Call             extends  ClientObserver  implements  PacketEventListener
{
	public  Call(  HttpOpsHandlerAdapter  context,  long  id,long  contactId,CallContentType  contentType )
	{
		super( context,id,contactId,contentType );
		
		super.context.getPacketEventDispatcher().addListeners(Call.this);
	}
	
	public  void   agree()
	{
		if( state.compareAndSet(CallState.REQUESTED , CallState.AGREED) )
		{
			context.write( new  CallAckPacket(contactId,id, CallAckPacket.ACK_AGREE),10,TimeUnit.SECONDS );
		}
		else
		{
			throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** CALL **  can  not  accept  the  call  in  %s  state",state.get().name()) );
		}
	}
	
	public  void decline()
	{
		if( state.compareAndSet(CallState.REQUESTED ,CallState.DECLINE) )
		{
			this.context.write( new  CallAckPacket(this.contactId,this.id,CallAckPacket.ACK_DECLINE),10,TimeUnit.SECONDS );
			
			this.release( true    ,CloseCallReason.DECLINE );
		}
		else
		{
			throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** CALL **  can  not  reject  the  call  in  %s  state",state.get().name()) );
		}
	}
	
	public  void  onSent( Packet  packet,TransportState  transportState )
	{
		if( packet instanceof CloseCallPacket && transportState == TransportState.SENT )
		{
			release( true, ObjectUtils.cast(packet,CloseCallPacket.class).getReason() );
		}
	}

	public  void request()
	{
		if( state.compareAndSet(CallState.NONE,   CallState.REQUESTING) )
		{
			this.context.write( new  CallPacket(this.contactId,this.id, contentType),20,TimeUnit.SECONDS );
		}
		else
		{
			throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** CALL **  can  not  create  the  call  in  %s  state",state.get().name()) );
		}
	}
	
	public  void   close()
	{
		if( state.compareAndSet(CallState.REQUESTING,   CallState.NONE) )
		{
			context.write( new  CloseCallPacket(contactId,id,CloseCallReason.CANCEL),10,TimeUnit.SECONDS );
		}
		else
		{
			this.context.write( new  CloseCallPacket(this.contactId,this.id,CloseCallReason.BY_USER ),5,TimeUnit.SECONDS );
		}
	}
	@Override
	protected  void  release( boolean  proactive,CloseCallReason reason )
	{
		this.context.getPacketEventDispatcher().removeListeners( this  );
		
		super.release(proactive,reason );
	}
	
	public  void  onBeforeSend(Packet  packet )
	{
		
	}
	
	public  void  onReceived(  Packet  packet )
	{
		if( packet instanceof CallPacket)
		{
			state.compareAndSet( CallState.NONE  , CallState.REQUESTED );
		}
		else
		if( packet instanceof CallAckPacket   )
		{
			if( ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode() == CallAckPacket.ACK_AGREE && this.state.compareAndSet(CallState.REQUESTING   ,    CallState.AGREED) )
			{
				connection.createOffer(  this ,constraints );
			}
		}
		else
		if( packet instanceof CloseCallPacket )
		{
			release( false,ObjectUtils.cast(packet,CloseCallPacket.class).getReason() );
		}
		else
		if( packet instanceof SDPPacket )
		{
			connection.setRemoteDescription( this,new  SessionDescription( SessionDescription.Type.fromCanonicalForm(ObjectUtils.cast(packet,SDPPacket.class).getSdp().getType()),ObjectUtils.cast(packet,SDPPacket.class).getSdp().getDescription() ) );
			
			if( this.connection.getLocalDescription()==null )
			{
				connection.createAnswer( this ,constraints );
			}
		}
		else
		if( packet instanceof CandidatePacket )
		{
			connection.addIceCandidate( new  IceCandidate(ObjectUtils.cast(packet,CandidatePacket.class).getCandidate().getId(),ObjectUtils.cast(packet,CandidatePacket.class).getCandidate().getLineIndex(),ObjectUtils.cast(packet,CandidatePacket.class).getCandidate().getCandidate()) );
		}
	}
}