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
import  java.util.concurrent.atomic.AtomicReference;

import  org.webrtc.IceCandidate;
import  org.webrtc.MediaConstraints;
import  org.webrtc.MediaStream;
import  org.webrtc.PeerConnection;
import  org.webrtc.PeerConnectionFactory;
import  org.webrtc.SessionDescription;
import  org.webrtc.VideoCapturer;
import  org.webrtc.VideoRenderer;
import  org.webrtc.VideoSource;
import  org.webrtc.VideoTrack;

import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.connect.PacketListener;
import  cc.mashroom.squirrel.client.connect.call.webrtc.ClientObserver;
import  cc.mashroom.squirrel.client.connect.call.webrtc.PeerConnectionParameters;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import cc.mashroom.squirrel.paip.message.call.Candidate;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import cc.mashroom.squirrel.paip.message.call.SDP;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  Call   extends  ClientObserver  implements  PacketListener
{
	public  Call( SquirrelClient  context, long  id,long  contactId,CallContentType  callContentType )
	{
		PacketEventDispatcher.addListener(1,this );
		
		this.setContext(context).setId(id).setContactId(contactId).setContentType(  callContentType );
	}
	
	public  Call  initialize(    Object  platform,PeerConnectionParameters  parameters )
	{
		PeerConnectionFactory.initializeAndroidGlobals( platform,true,( contentType == CallContentType.VIDEO) );
		
		this.setPeerConnectionParameters(parameters).setPeerConnectionFactory( new  PeerConnectionFactory() ).setLocalMs( peerConnectionFactory.createLocalMediaStream( "ARDAMS" ) );
		
        constraints.mandatory.add( new  MediaConstraints.KeyValuePair("OfferToReceiveAudio","true") );

        connectionMediaConstraints.optional.add( new  MediaConstraints.KeyValuePair("RtpDataChannels","true") );
        
        constraints.mandatory.add( new  MediaConstraints.KeyValuePair("OfferToReceiveVideo","true") );
        
        constraints.optional.add( new  MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement","true") );
        
        if( peerConnectionParameters.videoEnabled )
        {
        	MediaConstraints  videoConstraints = new  MediaConstraints();
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("maxHeight",String.valueOf(parameters.videoHeight)) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("maxWidth", String.valueOf(parameters.videoWidth )) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("minHeight",String.valueOf(parameters.videoHeight)) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("minWidth", String.valueOf(parameters.videoWidth )) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("minFrameRate",String.valueOf(parameters.videoFps)) );
        	
            VideoTrack  videoPlayTrack=peerConnectionFactory.createVideoTrack( "ARDAMSv0",videoSource=peerConnectionFactory.createVideoSource(getVideoCapturer(),videoConstraints) );

            videoPlayTrack.addRenderer( new  VideoRenderer(parameters.nativeRenderer) );

            this.localMs.addTrack(videoPlayTrack );
        }
        
        this.localMs.addTrack(peerConnectionFactory.createAudioTrack("ARDAMSa0",peerConnectionFactory.createAudioSource(new  MediaConstraints())) );
        
        this.setConnection(peerConnectionFactory.createPeerConnection(parameters.ices,connectionMediaConstraints,this)).getConnection().addStream( localMs,new  MediaConstraints() );
	
        return  this;
	}
		
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  SquirrelClient   context;
	@Setter( value=AccessLevel.PUBLIC    )
	@Getter
	@Accessors(chain=true)
	private  long    callPacketId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  id;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  CallContentType  contentType;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  long  contactId;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  AtomicReference<CallState>  state = new  AtomicReference<CallState>(    CallState.NONE );
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    private  VideoSource  videoSource;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    private  MediaStream  localMs;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    private  MediaConstraints  constraints     = new  MediaConstraints();
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    private  PeerConnectionFactory     peerConnectionFactory;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    private  PeerConnectionParameters  peerConnectionParameters;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
    private  PeerConnection    connection;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    private  MediaConstraints  connectionMediaConstraints     = new  MediaConstraints();

	public  CallState   getState()
	{
		return   state.get();
	}

	public  void  accept()
	{
		if( state.compareAndSet(CallState.REQUESTED , CallState.ACCEPT) )
		{
			context.send( new  CallAckPacket(contactId, id, CallAckPacket.ACK_ACCEPT) );
		}
		else
		{
			throw  new  IllegalStateException( String.format("SIMP:  ** CALL **  can  not  accept  the  call  when  the  state  is  %s",state.get().name()) );
		}
	}
	
	public  void  reject()
	{
		if( state.compareAndSet(CallState.REQUESTED , CallState.REJECT) )
		{
			context.send( new  CallAckPacket(contactId, id, CallAckPacket.ACK_REJECT) );
			
			this.release( true    , CloseCallReason.REJECT );
		}
		else
		{
			throw  new  IllegalStateException( String.format("SIMP:  ** CALL **  can  not  reject  the  call  when  the  state  is  %s",state.get().name()) );
		}
	}
	
	public  void  sent(   Packet  packet,TransportState  transportState )
	{
		if( packet instanceof CloseCallPacket && transportState == TransportState.SENT )
		{
			release( true, ObjectUtils.cast(packet,CloseCallPacket.class).getReason() );
		}
	}

	public  void  demand()
	{
		if( state.compareAndSet(CallState.NONE,   CallState.REQUESTING) )
		{
			context.send( new  CallPacket(this.contactId, this.id, contentType),20,TimeUnit.SECONDS );
		}
		else
		{
			throw  new  IllegalStateException( String.format("SIMP:  ** CALL **  can  not  create  the  call  when  the  state  is  %s",state.get().name()) );
		}
	}
	/**
	 *  close  the  call.  send  close  call  packet  with  reason  CloseCallReason.CANCEL  if  the  call  is  on  state  CallState.REQUESTING,  or  with  reason  CloseCallReason.BY_USER  if  the  call  is  on  the  other  states.  release  the  call  after  the  close  call  packet  sent.
	 */
	public  void   close()
	{
		if( state.compareAndSet(CallState.REQUESTING,   CallState.NONE) )
		{
			context.send( new  CloseCallPacket(contactId,id , CloseCallReason.CANCEL) );
		}
		else
		{
			this.context.send(   new  CloseCallPacket( this.contactId , this.id , CloseCallReason.BY_USER ) , 5 , TimeUnit.SECONDS );
		}
	}
	
	public  boolean  beforeSend(Packet packet )
	{
		return  true;
	}
	
	private  void  release(boolean  proactively,CloseCallReason  reason )
	{
		PacketEventDispatcher.removeListener( this );
		
		context.setCall(   null );
		
		if( this.connection  != null )  connection.dispose();
		//  video  source,   connection  or  peer  connection  factory  may  be  null  if  some  permission  was  rejected  by  user.
		if( videoSource  != null )this.videoSource.dispose();
		
		if( this.peerConnectionFactory!= null )    this.peerConnectionFactory.dispose();
		
		CallEventDispatcher.onClose(this,proactively,reason);
	}
	
	public  void  received(    Packet  packet )
	{
		if( packet instanceof CallPacket )
		{
			state.compareAndSet(  CallState.NONE , CallState.REQUESTED );
		}
		else
		if( packet instanceof CallAckPacket   )
		{
			if( ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode() == CallAckPacket.ACK_ACCEPT && this.state.compareAndSet(CallState.REQUESTING, CallState.ACCEPTED) )
			{
				connection.createOffer( this , constraints );
			}
		}
		else
		if( packet instanceof CloseCallPacket )
		{
			release( false,ObjectUtils.cast(packet,CloseCallPacket.class).getReason() );
		}
		else
		if( packet instanceof SDPPacket  )
		{
			connection.setRemoteDescription( this,new  SessionDescription( SessionDescription.Type.fromCanonicalForm(ObjectUtils.cast(packet,SDPPacket.class).getSdp().getType()),ObjectUtils.cast(packet,SDPPacket.class).getSdp().getDescription() ) );
			
			if( this.connection.getLocalDescription()==null )
			{
				connection.createAnswer(  this,constraints );
			}
		}
		else
		if( packet instanceof CandidatePacket )
		{
			connection.addIceCandidate( new  IceCandidate(ObjectUtils.cast(packet,CandidatePacket.class).getCandidate().getId(),ObjectUtils.cast(packet,CandidatePacket.class).getCandidate().getLineIndex(),ObjectUtils.cast(packet,CandidatePacket.class).getCandidate().getCandidate()) );
		}
	}
	
	public  void  onAddStream(MediaStream  addedMediaStream )
	{
		super.onAddStream(  addedMediaStream );
		
		CallEventDispatcher.onStart(    this );
		
		if( peerConnectionParameters.videoEnabled   )
		{
			addedMediaStream.videoTracks.get(0).addRenderer(    new  VideoRenderer( this.peerConnectionParameters.remoteRenderer ) );
		}
	}
	
    public  void  onCreateSuccess(SessionDescription sessionDescription )
    {
    	super.onCreateSuccess(  sessionDescription );
    	
    	this.connection.setLocalDescription( this , sessionDescription );
    	
    	this.state.set(    CallState.CALLING );
    	
    	this.context.send(new  SDPPacket(this.contactId,this.id,new  SDP(sessionDescription.type.canonicalForm(),sessionDescription.description)) );
    }

    public  void  onIceCandidate(IceCandidate  iceCandidate )
    {
    	super.onIceCandidate(   iceCandidate );
    	
        this.context.send(new  CandidatePacket(this.contactId,id,new  Candidate(iceCandidate.sdpMid,iceCandidate.sdpMLineIndex,iceCandidate.sdp)) );
    }

	public  void  onRemoveStream(   MediaStream   removingMediaStream   )
	{
		super.onRemoveStream(  removingMediaStream );
		
		if( peerConnectionParameters.videoEnabled   )
		{
			removingMediaStream.videoTracks.get(0).dispose();
		}
	}
	
	protected  VideoCapturer getVideoCapturer()
	{
	    for( String  facing : new  String[]{"front","back"} )
	    {
	    	for( int  index : new  int[]{0,1} )
	    	{
	    		for( int  orientationHint : new  int[]{90, 0, 180, 270} )
	    		{
	    			VideoCapturer  videoCapturer = VideoCapturer.create( "Camera " +index +", Facing " +facing +", Orientation " +orientationHint );
	    			
	    			if( videoCapturer != null )
	    			{
	    				return   videoCapturer;
	    			}
	    		}
	    	}
	    }
	    
	    throw  new  RuntimeException( "SIMP:  ** CALL **  failed  to  create  video  capturer  since  all  facings,  indices  and  orientations  have  been  tried" );
	}
}