package cc.mashroom.squirrel.paip.codec;

import  java.lang.reflect.Constructor;

import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.Packet;
import cc.mashroom.squirrel.paip.message.extensions.ByteArrayPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.collection.map.Map.Computer;
import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;

public  class  PAIPCoresDecoder  implements  PAIPDecoder,Computer<PAIPPacketType,Constructor<Packet<?>>>
{
	protected  Map<PAIPPacketType,Constructor<Packet<?>>>  constructors = new  ConcurrentHashMap<PAIPPacketType,Constructor<Packet<?>>>();
	
	@Override
	public  Constructor<Packet<?>>  compute( PAIPPacketType  packetType ) throws  Exception
	{
		return  ObjectUtils.cast( packetType.getPacketClass().getConstructor(packetType == PAIPPacketType.CONNECT ? new  Class[]{Channel.class,ByteBuf.class} : new  Class[]{ByteBuf.class}) );
	}
	
	public  Packet<?>  decode( Channel  channel, ByteBuf  byteBuf )
	{
		byteBuf.skipBytes( 2 );
		
		try
		{
			switch( PAIPPacketType.valueOf(byteBuf.readShortLE()) )
			{
				case  PAIPPacketType.CONNECT:
				{
					
				}
			}
			if( packetType == PAIPPacketType.BYTE_ARRAY )
			{
				return  new  ByteArrayPacket(  byteBuf );
			}
			
			return  this.constructors.computeIfLackof(packetType,this).newInstance( packetType == PAIPPacketType.CONNECT ? new  Object[]{channel,byteBuf} : new  Object[]{byteBuf} );
		}
		catch( Throwable  cfe )
		{
			
		}
		
		return  null;
	}
}
