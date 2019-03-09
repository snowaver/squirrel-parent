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
package cc.mashroom.squirrel.client.handler;

import  cc.mashroom.router.BalancerStateListener;
import  cc.mashroom.router.BalancingProxy;
import  cc.mashroom.router.BalancingProxyFactory;
import  cc.mashroom.util.StringUtils;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  RoutableChannelInboundHandlerAdapter  extends  AbstractChannelInboundHandlerAdapter
{
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  String   host;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	protected  int   port;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	@Getter
	protected  int  httpPort;
	
	public  boolean  isRouted()
	{
		return  StringUtils.isNotBlank(host) && port >= 1 && httpPort >= 1;
	}
	
	public  RoutableChannelInboundHandlerAdapter  route(    String  host,int  port,int  httpPort )
	{
		return  setHttpPort(httpPort).setHost(host).setPort( port );
	}
	
	public  RoutableChannelInboundHandlerAdapter  route( BalancingProxyFactory  balancingProxyFactory,BalancerStateListener  balancerListener )
	{
		for( BalancingProxy  balancingProxy : balancingProxyFactory.get() )
		{
			if( balancingProxy.getProtocol() == 0 )
			{
				setHost(balancingProxy.getHost()).setPort( balancingProxy.getPort() );
			}
			else
			if( balancingProxy.getProtocol() == 2 )
			{
				httpPort= balancingProxy.getPort();
			}
		}
		
		balancerListener.onBalanceComplete( isRouted() ? 200 : 404,isRouted() ? null : new  IllegalStateException("SQUIRREL-CLIENT:  ** ROUTABLE  CHANNEL  INBOUND  HANDLER  ADAPTER **  no  route  available") );
		
		return  this;
	}
}