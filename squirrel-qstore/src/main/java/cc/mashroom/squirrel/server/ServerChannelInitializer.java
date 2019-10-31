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
package cc.mashroom.squirrel.server;

import  io.netty.channel.socket.SocketChannel;
import  lombok.AllArgsConstructor;
import  cc.mashroom.squirrel.paip.codec.PAIPDecoder;
import  cc.mashroom.squirrel.paip.codec.PAIPEncoder;
import  cc.mashroom.squirrel.server.handler.RedisPacketHandler;

@AllArgsConstructor

public  class  ServerChannelInitializer  extends  io.netty.channel.ChannelInitializer<SocketChannel>
{
	protected  void  initChannel( SocketChannel  channel )  throws  Exception
	{
		channel.pipeline().addLast("encoder",new  PAIPEncoder()).addLast("decoder",new  PAIPDecoder()).addLast( "handler",new  RedisPacketHandler() );
		
//		channel.pipeline().addLast("encoder",new  RedisEncoder()).addLast("decoder",new  RedisDecoder(false))/*.addLast("bulkstring.aggregator",new  RedisBulkStringAggregator())*/.addLast("array.aggregator",new  RedisArrayAggregator()).addLast( "handler",new  RedisPacketHandler() );
	}
}