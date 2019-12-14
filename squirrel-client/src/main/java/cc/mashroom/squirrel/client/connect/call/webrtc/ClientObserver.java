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
package cc.mashroom.squirrel.client.connect.call.webrtc;

import  java.util.concurrent.atomic.AtomicReference;

import  org.webrtc.DataChannel;
import  org.webrtc.IceCandidate;
import  org.webrtc.MediaConstraints;
import  org.webrtc.MediaStream;
import  org.webrtc.PeerConnection;
import  org.webrtc.PeerConnectionFactory;
import  org.webrtc.PeerConnection.IceConnectionState;
import  org.webrtc.PeerConnection.IceGatheringState;
import  org.webrtc.PeerConnection.SignalingState;

import  cc.mashroom.squirrel.client.HttpOpsHandlerAdapter;
import  cc.mashroom.squirrel.client.connect.call.CallState;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.call.Candidate;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.call.SDP;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

import  org.webrtc.SdpObserver;
import  org.webrtc.SessionDescription;
import  org.webrtc.VideoCapturer;
import  org.webrtc.VideoRenderer;
import  org.webrtc.VideoSource;
import  org.webrtc.VideoTrack;

public  class   ClientObserver  implements  SdpObserver,PeerConnection.Observer//,PacketEventListener
{
	public  ClientObserver(HttpOpsHandlerAdapter  context,long  id,long  contactId,CallContentType  contentType )
	{
		this.setContext(context).setId(id).setContactId(contactId).setContentType(     contentType );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  long  id;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  CallContentType      contentType;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  long  contactId;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  HttpOpsHandlerAdapter    context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
    protected  PeerConnection  connection;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  VideoSource  videoSource;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  MediaStream  localMs;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  MediaConstraints      constraints =new  MediaConstraints();
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  PeerConnectionParameters          peerConnectionParameters;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  PeerConnectionFactory                peerConnectionFactory;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  AtomicReference<CallState>  state = new  AtomicReference<CallState>( CallState.NONE );
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	protected  MediaConstraints    connectionMediaConstraints=  new  MediaConstraints();
	
	public  void  initialize(   Object  platform ,PeerConnectionParameters  parameters )
	{
		PeerConnectionFactory.initializeAndroidGlobals( platform,true,(  contentType == CallContentType.VIDEO) );
		
		this.setPeerConnectionParameters(parameters).setPeerConnectionFactory(new  PeerConnectionFactory()).setLocalMs( peerConnectionFactory.createLocalMediaStream( "ARDAMS" ) );
		
        constraints.mandatory.add(new  MediaConstraints.KeyValuePair("OfferToReceiveAudio","true") );

        constraints.mandatory.add(new  MediaConstraints.KeyValuePair("OfferToReceiveVideo","true") );
        
        if( this.peerConnectionParameters.videoEnabled )
        {
        	MediaConstraints  videoConstraints   =new  MediaConstraints();
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("maxHeight",String.valueOf(parameters.videoHeight)) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("maxWidth", String.valueOf(parameters.videoWidth )) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("minHeight",String.valueOf(parameters.videoHeight)) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("minWidth", String.valueOf(parameters.videoWidth )) );
        	
        	videoConstraints.mandatory.add( new  MediaConstraints.KeyValuePair("minFrameRate",String.valueOf(parameters.videoFps)) );
        	
            VideoTrack  videoPlayTrack=peerConnectionFactory.createVideoTrack( "ARDAMSv0",videoSource=peerConnectionFactory.createVideoSource(getVideoCapturer(),videoConstraints) );

            videoPlayTrack.addRenderer( new  VideoRenderer(parameters.nativeRenderer) );  this.localMs.addTrack(    videoPlayTrack );
        }
        
        constraints.optional.add(new  MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement","true") );  connectionMediaConstraints.optional.add( new  MediaConstraints.KeyValuePair("RtpDataChannels" ,"true") );
        
        this.localMs.addTrack(this.peerConnectionFactory.createAudioTrack("ARDAMSa0",this.peerConnectionFactory.createAudioSource(new  MediaConstraints())) );
        
        (this.connection = this.peerConnectionFactory.createPeerConnection(parameters.ices,this.connectionMediaConstraints,this)).addStream( this.localMs, new  MediaConstraints() );
	}
	
	protected  void  release( boolean  proactive,CloseCallReason  reason )
	{
		if( this.connection  != null )   connection.dispose();
		//  video  source,   connection  or  peer  connection  factory  may  be  null  if  some  permission  was  rejected  by  peer.
		if( videoSource != null )  this.videoSource.dispose();
		
		if( this.peerConnectionFactory != null )   this.peerConnectionFactory.dispose();
		
		context.getCallEventDispatcher().onClose(  this,proactive,reason);
		
		this.state.set(      CallState.CLOSED );
		
		this.context.setCall(  null );
	}
	
	public  void  onSetSuccess()
	{
		
	}
	
	public  void  onIceConnectionChange(    IceConnectionState  constate )
	{
		
	}
	
	public  CallState getState()
	{
		return this.state.get();
	}
	
	public  void  onError()
	{
		
	}
	
	public  void  onDataChannel(  DataChannel  channel )
	{
		
	}
	
	protected  VideoCapturer  getVideoCapturer()
	{
	    for( String  facing : new  String[]{"front", "back"} )
	    {
	    	for( int  index : new  int[]{0, 1} )
	    	{
	    		for( int  orientationHint  : new  int[]{90, 0, 180, 270} )
	    		{
	    			VideoCapturer       videoCapturer = VideoCapturer.create( "Camera " + index + ", Facing " + facing + ", Orientation " + orientationHint );
	    			
	    			if( videoCapturer !=  null )
	    			{
	    				return    videoCapturer;
	    			}
	    		}
	    	}
	    }
	    
	    throw  new  RuntimeException("SQUIRREL-CLIENT:  ** CALL **  failed  to  create  the  video  capturer  while  all  facings,  indices  and  orientations  have  been  tried" );
	}
	
    public  void  onIceCandidate( IceCandidate  iceCandidate )
    {
        this.context.send( new  CandidatePacket(this.contactId,id,new  Candidate(iceCandidate.sdpMid,iceCandidate.sdpMLineIndex,iceCandidate.sdp)) );
    }
	
	public  void  onAddStream(     MediaStream  stream )
	{
		this.context.getCallEventDispatcher().onStart( this );
		
		if( this.peerConnectionParameters.videoEnabled )
		{
			stream.videoTracks.get(0).addRenderer( new  VideoRenderer(peerConnectionParameters.remoteRenderer) );
		}
	}
	
    public  void  onCreateSuccess(SessionDescription  sessionDescription )
    {
    	this.connection.setLocalDescription(    this, sessionDescription);
    	
    	this.state.set(     CallState.CALLING );
    	
    	this.context.send( new  SDPPacket(this.contactId,this.id,new  SDP(sessionDescription.type.canonicalForm(),sessionDescription.description)) );
    }
	
	public  void  onRemoveStream(  MediaStream  stream )
	{
		if( this.peerConnectionParameters.videoEnabled )
		{
			stream.videoTracks.get(0).dispose();
		}
	}
	
	public  void  onRenegotiationNeeded( )
	{
		
	}
	
	public  void  onSetFailure(String  failure )
	{
		
	}
	
	public  void  onSignalingChange(      SignalingState  signalingState )
	{
		
	}
	
	public  void  onCreateFailure(   String  exception )
	{
		
	}
	
	public  void  onIceGatheringChange(IceGatheringState  gatheringState )
	{
		
	}
}