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
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.CONNECTION.CHANGE:\t" + connectionState.name() );
	}
	
	public  void  onAddStream(    MediaStream  stream )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ADD.STREAM:\t"+   stream );
	}

	public  void  onDataChannel( DataChannel  channel )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  DATA.CHANNEL:\t"+channel );
	}

	public  void  onError()
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ERROR" );
	}

	public  void  onIceCandidate( IceCandidate  iceCandidate )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.CANDIDATE:\t"+iceCandidate );
	}

	public  void  onSetSuccess()
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  SET.SUCCESS" );
	}
	
	public  void  onRemoveStream( MediaStream  stream )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  REMOVE.STREAM:\t"+stream );
	}
	
	public  void  onRenegotiationNeeded()
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  RENEGOTIATION.NEEDED"    );
	}

	public  void  onSignalingChange( SignalingState  signalingState )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  SIGNALING.CHANGE:\t"+signalingState.name() );
	}

	public  void  onCreateFailure(  String  exception )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CREATE.FAILURE:\t"+  exception );
	}
	
	public  void  onCreateSuccess(    SessionDescription  sessionDescription )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CREATE.SUCCESS:\t"+     sessionDescription );
	}

	public  void  onSetFailure(     String  exception )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  SET.FAILURE:\t"+exception);
	}
	
	public  void  onIceGatheringChange( IceGatheringState  iceGatheringState )
	{
		System.err.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  ICE.GATHERING.CHANGE:\t"+ iceGatheringState.name() );
	}
}