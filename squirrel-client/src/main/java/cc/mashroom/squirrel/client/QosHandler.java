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
package cc.mashroom.squirrel.client;

import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  cc.mashroom.squirrel.client.connect.ClientConnectEventDispatcher;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.util.concurrent.DefaultThreadFactory;
/*
@NoArgsConstructor(  access = AccessLevel.PRIVATE )
*/
public  class  QosHandler  //  extends  ChannelInboundHandlerAdapter
{
	private  ScheduledThreadPoolExecutor  pendingScheduledChecker = new  ScheduledThreadPoolExecutor(1,new  DefaultThreadFactory("QOS-HANDLE-THREAD",false,1) );
	
	private  Map<Long,Packet>  pendings= new  ConcurrentHashMap<Long,Packet>();
	
	public  void  channelRead(   ChannelHandlerContext  context,Object  object )  throws  Exception
	{
		Packet  packet= ObjectUtils.cast( object );
		
		System.out.println( "//*RCV:\t\t"+packet );
		
		SquirrelClient  adapter  = ObjectUtils.cast( context.pipeline().get( "squirrel.client" ) );
		
		if( packet instanceof DisconnectAckPacket )
		{
			if( ObjectUtils.cast(packet,DisconnectAckPacket.class).getReason() == DisconnectAckPacket.ACTIVE || ObjectUtils.cast(packet,DisconnectAckPacket.class).getReason() == DisconnectAckPacket.REMOTE_LOGIN_ERROR )
			{
				adapter.getLifecycleListener().onDisconnected( ObjectUtils.cast( packet,DisconnectAckPacket.class ).getReason() == DisconnectAckPacket.ACTIVE );
				
				adapter.clear();
				
				context.close();
			}
		}
		else
		if( packet instanceof ConnectAckPacket    )
		{
			if( ObjectUtils.cast(packet, ConnectAckPacket.class).getResponseCode() == ConnectAckPacket.CONNECTION_ACCEPTED )
			{
				ClientConnectEventDispatcher.connectStateChanged( adapter.setConnectState(ConnectState.CONNECTED).getConnectState() );
			}
			else
			{
				context.close();
				
				if(     adapter.isAuthenticated() )
				{
					System.err.println( "still  authenticated,  disconnection  of  network  may  result  in  an  authentication  error  (secret  key  unavailable  now),  so  retrive  a  new  secret  key." );
					
					adapter.connect( null, null, null, null, null,adapter.getLifecycleListener() );
				}
			}
		}
		else
		if( packet instanceof QosReceiptPacket    )
		{
			unpend( ObjectUtils.cast(packet, QosReceiptPacket.class).getPacketId() , TransportState.SENT );
			
			if( !(packet instanceof CallAckPacket))
			{
				return;
			}
		}
		else
		if( packet instanceof CallPacket    )
		{
			if( adapter.newCall(ObjectUtils.cast(packet , CallPacket.class).getCallId(),ObjectUtils.cast(packet , CallPacket.class).getContactId(),ObjectUtils.cast( packet,CallPacket.class ).getContentType()) == null )
			{
				adapter.send( new  CallAckPacket(ObjectUtils.cast(packet,CallPacket.class).getId(),ObjectUtils.cast(packet,CallPacket.class).getContactId(),ObjectUtils.cast(packet,CallPacket.class).getCallId(),CallAckPacket.BUSY) );
			
				return;
			}
			else
			{
				adapter.getCall().setCallPacketId(     ObjectUtils.cast(packet,CallPacket.class).getId() );
			}
		}
		else
		if( packet.getHeader().getQos()== 1 )
		{
			adapter.asynchronousSend(new  QosReceiptPacket(packet.getContactId(),packet.getId()) );
		}
		else
		if( packet instanceof CloseCallPacket     )
		{
			if( adapter.getCall() == null || adapter.getCall().getId() != ObjectUtils.cast(packet, CloseCallPacket.class).getCallId() || adapter.getCall().getContactId() != ObjectUtils.cast(packet,CloseCallPacket.class).getContactId() )
			{
				return;
			}
		}
		
		PacketEventDispatcher.received(   packet );
	}
	
	public  Packet  unpend(    long  pendKey,TransportState  dispatchingState )   throws  Exception
	{
		Packet  packet= pendings.remove( pendKey );
		
		if( packet != null )
		{
			PacketEventDispatcher.sent(  packet, dispatchingState );
		}
		
		return  packet;
	}
	
	public  void  pend( final Packet  pendingPacket,final long  timeout,final  TimeUnit  timeunit )
	{
		pendings.addEntry( pendingPacket.getId(), ObjectUtils.cast( pendingPacket,Packet.class ) );
		
		pendingScheduledChecker.schedule( new  Runnable(){public  void  run(){ try{ unpend(pendingPacket.getId(),TransportState.SEND_FAILED); }catch(Throwable  t){t.printStackTrace();} }},timeout,timeunit );
	}
}