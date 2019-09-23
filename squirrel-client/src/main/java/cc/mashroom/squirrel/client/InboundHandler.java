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
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
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
					LifecycleEventDispatcher.onLogout( adapter.getLifecycleListeners() ,  200, 1 );
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
					
					adapter.setConnectivityError(2);  //  access  key  expired,  so  switch  connectivity  error  and  call  the  check  method  to  reconnect  for  a  new  access  key.
				}
			}
		}
		else
		if( packet instanceof GroupChatPacket )
		{
			//  do  nothing  while  the  pending  ack  packet  is  delivered  by  the  server  side
		}
		else
		if( packet instanceof PendingAckPacket)
		{
			this.unpend( adapter, ObjectUtils.cast(packet, PendingAckPacket.class).getPacketId()  ,TransportState.SENT);
			
			if( !(packet instanceof CallAckPacket) )
			{
				return;
			}
		}
		else
		if( packet instanceof      CallPacket )
		{
			adapter.addCall( ObjectUtils.cast(packet , CallPacket.class).getRoomId(),ObjectUtils.cast(packet , CallPacket.class).getContactId(),ObjectUtils.cast( packet,CallPacket.class ).getContentType() );
		}
		else
		if( packet.getHeader().getAckLevel() ==  1 )
		{
			adapter.send(          new  PendingAckPacket( packet.getContactId(),packet.getId() ) );
		}
		else
		if( packet instanceof      CloseCallPacket )
		{
			if( adapter.getCall() == null || adapter.getCall().getId() != ObjectUtils.cast(packet,CloseCallPacket.class).getRoomId() ||adapter.getCall().getContactId() != ObjectUtils.cast(packet,CloseCallPacket.class).getContactId() )
			{
				return;
			}
		}
		
		PacketEventDispatcher.onReceived( adapter.getPacketListeners(),packet );
	}
	
	public  Packet  unpend( TcpAutoReconnectChannelInboundHandlerAdapter<?>  context,long  pendKey, TransportState  transportState )
	{
		Packet  packet = pendings.remove( pendKey );
		
		if( packet != null )
		{
			PacketEventDispatcher.onSent( context.getPacketListeners(),  packet,  transportState );
		}
		
		return  packet;
	}
	
	public  void  pend(final  TcpAutoReconnectChannelInboundHandlerAdapter<?> context,final  Packet  pendingPacket,final  long  writeTimeout,final  TimeUnit  timeunit )
	{
		pendings.addEntry( pendingPacket.getId(), ObjectUtils.cast( pendingPacket,Packet.class ) );
		
		pendingScheduledChecker.schedule( new  Runnable(){public  void  run() { try{ unpend(context,pendingPacket.getId(),TransportState.SEND_FAILED); } catch( Throwable  t ) { t.printStackTrace(); } } },      writeTimeout,timeunit );
	}
}