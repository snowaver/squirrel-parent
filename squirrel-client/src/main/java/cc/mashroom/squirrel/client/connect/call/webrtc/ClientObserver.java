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

import  org.joda.time.DateTime;
import  org.webrtc.DataChannel;
import  org.webrtc.IceCandidate;
import  org.webrtc.MediaStream;
import  org.webrtc.PeerConnection;
import  org.webrtc.PeerConnection.IceConnectionState;
import  org.webrtc.PeerConnection.IceGatheringState;
import  org.webrtc.PeerConnection.SignalingState;
import  org.webrtc.SdpObserver;
import  org.webrtc.SessionDescription;

public  class  ClientObserver  implements  SdpObserver,PeerConnection.Observer
{
	public  void  onIceConnectionChange( IceConnectionState  connectionState )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.CONNECTION_CHANGE:\t"+ connectionState.name() );
	}
	
	public  void  onDataChannel( DataChannel  channel )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.DATA_CHANNEL:\t"+    channel );
	}
	
	public  void  onAddStream(    MediaStream  stream )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.ADD_STREAM:\t"+stream );
	}

	public  void  onError()
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.ERROR" );
	}
	
	public  void  onSetSuccess()
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.SET_SUCCESS" );
	}
	
	public  void  onSetFailure(     String  exception )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.SET_FAILURE:\t"+   exception );
	}

	public  void  onIceCandidate( IceCandidate  iceCandidate )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.CANDIDATE:\t"+  iceCandidate );
	}
	
	public  void  onRemoveStream( MediaStream  stream )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.REMOVE_STREAM:\t"+    stream );
	}
	
	public  void  onRenegotiationNeeded()
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.RENEGOTIATION_NEEDED" );
	}

	public  void  onSignalingChange( SignalingState  signalingState )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.SIGNALING_CHANGE:\t"+   signalingState.name() );
	}

	public  void  onCreateFailure(  String  exception )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.CREATE_FAILURE:\t"+exception );
	}
	
	public  void  onCreateSuccess(    SessionDescription  sessionDescription )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.CREATE_SUCCESS:\t"+        sessionDescription );
	}
	
	public  void  onIceGatheringChange( IceGatheringState  iceGatheringState )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.GATHERING_CHANGE:\t"+iceGatheringState.name() );
	}
}