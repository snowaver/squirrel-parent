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
	public  void  onAddStream( MediaStream  stream )
	{
		
	}

	public  void  onDataChannel( DataChannel  channel )
	{
		
	}

	public  void  onError()
	{
		
	}

	public  void  onIceCandidate( IceCandidate  candidate )
	{
		
	}

	public  void  onIceConnectionChange(   IceConnectionState  connectionState )
	{
		
	}

	public  void  onIceGatheringChange( IceGatheringState  arg0 )
	{
		
	}

	public  void  onRemoveStream( MediaStream  stream )
	{
		
	}

	public  void  onRenegotiationNeeded()
	{
		
	}

	public  void  onSignalingChange( SignalingState  signalingState )
	{
		
	}

	public  void  onCreateFailure( String  cause )
	{
		
	}
	
	public  void  onCreateSuccess(      SessionDescription  sessionDescription )
	{
		
	}

	public  void  onSetFailure( String  cause )
	{
		
	}

	public  void  onSetSuccess()
	{
		
	}
}