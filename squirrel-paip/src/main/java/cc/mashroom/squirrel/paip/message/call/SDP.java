package cc.mashroom.squirrel.paip.message.call;

import  cc.mashroom.squirrel.paip.codec.PAIPUtils;
import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.ToString;
import  lombok.experimental.Accessors;

@AllArgsConstructor
@ToString
public   class  SDP
{
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  String  type;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  String  description;

	public   ByteBuf  toByteBuf()
	{
		return  Unpooled.buffer().writeBytes(PAIPUtils.encode(type)).writeBytes( PAIPUtils.encode(description) );
	}
}
