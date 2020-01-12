package cc.mashroom.squirrel.paip.codec;

import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.Packet;
import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;

public  class  PAIPCoresDecoder  implements  PAIPDecoder
{
	public  Packet<?>  decode( Channel  channel,ByteBuf  byteBuf )
	{
		int  packetTypeValue = byteBuf.skipBytes(2).readShortLE();
		
		try
		{
			if( packetTypeValue >= 1 && packetTypeValue  <= 1024 )
			{
				PAIPPacketType  packetType = PAIPPacketType.valueOf( packetTypeValue );
				
				return  packetType.getPacketClass().getConstructor(packetType == PAIPPacketType.CONNECT ? new  Class[]{Channel.class,ByteBuf.class} : new  Class[]{ByteBuf.class}).newInstance( packetType == PAIPPacketType.CONNECT ? new  Object[]{channel,byteBuf.resetReaderIndex()} : new  Object[]{byteBuf.resetReaderIndex()} );
			}
		}
		catch( Throwable  ife )
		{
			ife.printStackTrace();
		}
		
		return  null;
	}
}
