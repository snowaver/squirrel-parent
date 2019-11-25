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

import  org.joda.time.DateTime;

import  cc.mashroom.squirrel.client.connect.ConnectState;
import cc.mashroom.squirrel.client.event.LifecycleEventDispatcher;
import cc.mashroom.squirrel.client.event.PacketEventDispatcher;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.util.concurrent.DefaultThreadFactory;
/*
@NoArgsConstructor(  access =  AccessLevel.PRIVATE )
*/
public  class  InboundHandler
{
	private  ScheduledThreadPoolExecutor pendingScheduledChecker = new  ScheduledThreadPoolExecutor(    1, new  DefaultThreadFactory("ACK-SCHEDULED-CHECKER",false,1) );
	
	private  Map<Long,Packet>  pendings =new  ConcurrentHashMap<Long,Packet>();
	
	public  void  channelRead( ChannelHandlerContext  context,Object  object )    throws  Exception
	{
		Packet  packet = ObjectUtils.cast( object );
		
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.READ:\t"+ packet.toString() );
		
		SquirrelClient  adapter  = ObjectUtils.cast( context.pipeline().get( "squirrel.client" ) );
		
		if( packet instanceof DisconnectAckPacket  )
		{
			if( ObjectUtils.cast(packet,DisconnectAckPacket.class).getReason()==DisconnectAckPacket.REASON_CLIENT_LOGOUT || ObjectUtils.cast(packet,DisconnectAckPacket.class).getReason() == DisconnectAckPacket.REASON_REMOTE_SIGNIN )
			{
				if( ObjectUtils.cast(packet, DisconnectAckPacket.class).getReason()    == DisconnectAckPacket.REASON_REMOTE_SIGNIN )
				{
				LifecycleEventDispatcher.onLogoutComplete( adapter.getLifecycleListeners(),200,2 );
				}
				
				adapter.reset();
				
				adapter.close();
			}
		}
		else
		if( packet instanceof     ConnectAckPacket )
		{
			if( ObjectUtils.cast(packet, ConnectAckPacket.class).getResponse() == ConnectAckPacket.CONNECTION_ACCEPTED )
			{
				adapter.onConnectStateChanged(    adapter.setConnectState( ConnectState.CONNECTED ).getConnectState() );
			}
			else
			{
				adapter.close();
				
				if( adapter.isAuthenticated() )
				{
					System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.CONN:\tstill  authenticated,  disconnection  may  orignate  in  authentication  error  (secret  key  unavailable  now),  so  retrive  a  new  secret  key.");
					
					adapter.setConnectivityError(2);  //  access  key  expired,  so  switch  connectivity  error  and  call  the  check  method  to  reconnect  by  a  new  access  key.
				}
			}
		}
		else
		if( packet instanceof GroupChatPacket )
		{
			
		}
		else
		if( packet instanceof ChatPacket )
		{
			adapter.send( new  PendingAckPacket(packet.getContactId(),packet.getId()).setAttatchments(String.format("{\"SYNC_ID\":%d}",ObjectUtils.cast(packet,ChatPacket.class).getContactSyncId())) );
		}
		else
		if( packet instanceof PendingAckPacket)
		{
			unpend( adapter,ObjectUtils.cast(packet,PendingAckPacket.class), TransportState.SENT );
		}
		else
		if( packet instanceof CallPacket )
		{
			adapter.addCall(ObjectUtils.cast(packet,CallPacket.class).getRoomId(),ObjectUtils.cast(packet,CallPacket.class).getContactId(),ObjectUtils.cast(packet,CallPacket.class).getContentType() );
		}
		else
		if( packet.getHeader().getAckLevel() ==  1 )
		{
			adapter.send(          new  PendingAckPacket( packet.getContactId(),packet.getId() ) );
		}
		else
		if( packet instanceof CloseCallPacket )
		{
			if( adapter.getCall() == null || adapter.getCall().getId() != ObjectUtils.cast(packet,CloseCallPacket.class).getRoomId() || adapter.getCall().getContactId() != ObjectUtils.cast(packet,CloseCallPacket.class).getContactId() )
			{
				return;
			}
		}
		
		PacketEventDispatcher.onReceived( adapter.getPacketListeners(),packet );
	}
	
	public  Packet  unpend( TransportLifecycleHandlerAdapter<?>  context,PendingAckPacket<?>  pendingAckPacket,TransportState  transportState )
	{
		Packet  packet = this.pendings.remove( pendingAckPacket.getPacketId() );
		
		if( packet == null )
		{
			return   packet;
		}
		
		if( packet instanceof ChatPacket )
		{
			ObjectUtils.cast(packet,ChatPacket.class).setSyncId(      Long.parseLong(JsonUtils.fromJson(pendingAckPacket.getAttatchments(),Map.class).get("SYNC_ID").toString()) );
		}
		else
		if( packet instanceof GroupChatPacket )
		{
			ObjectUtils.cast(packet,GroupChatPacket.class).setSyncId( Long.parseLong(JsonUtils.fromJson(pendingAckPacket.getAttatchments(),Map.class).get("SYNC_ID").toString()) );
		}
		
		PacketEventDispatcher.onSent(    context.getPacketListeners() , packet ,  transportState );  return  packet;
	}
	
	public  void  pend(final  TransportLifecycleHandlerAdapter<?> context,final  Packet  pendingPacket,final  long  writeTimeout,final  TimeUnit  timeunit )
	{
		pendings.addEntry( pendingPacket.getId(), ObjectUtils.cast( pendingPacket,Packet.class ) );
		
		pendingScheduledChecker.schedule( new  Runnable(){public  void  run() { unpend(context,new  PendingAckPacket(pendingPacket.getContactId(),pendingPacket.getId()) , TransportState.SEND_FAILED ); } },   writeTimeout,timeunit );
	}
}