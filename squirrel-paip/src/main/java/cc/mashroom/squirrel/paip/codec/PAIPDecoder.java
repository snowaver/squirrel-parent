package cc.mashroom.squirrel.paip.codec;

import  cc.mashroom.squirrel.paip.message.Packet;
import  io.netty.buffer.ByteBuf;
import  io.netty.channel.Channel;

public  interface  PAIPDecoder
{
	public  Packet<?>  decode( Channel  channel,ByteBuf  byteBuf );
}
