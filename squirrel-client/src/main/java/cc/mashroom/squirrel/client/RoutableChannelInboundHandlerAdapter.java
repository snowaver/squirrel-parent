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

import  cc.mashroom.router.ServiceRouteManager;
import cc.mashroom.squirrel.client.handler.AbstractChannelInboundHandlerAdapter;
import  cc.mashroom.router.Schema;
import  cc.mashroom.router.ServiceListRequestStrategy;

public  class  RoutableChannelInboundHandlerAdapter  extends  AbstractChannelInboundHandlerAdapter
{	
	public  RoutableChannelInboundHandlerAdapter  route(    ServiceListRequestStrategy  strategy )
	{
		ServiceRouteManager.INSTANCE.setStrategy(strategy).request();
		
		if(     ServiceRouteManager.INSTANCE.tryNext(Schema.TCP) != null && ServiceRouteManager.INSTANCE.tryNext(Schema.HTTPS) != null )
		{
			System.err.println( "SQUIRREL-CLIENT:  ** ROUTABLE  CHANNEL  INBOUND  HANDLER  ADAPTER **  route  successfully." );
		}
		
		return  this;
	}
	
	public  boolean  isRouted()
	{
		return  ServiceRouteManager.INSTANCE.current(Schema.TCP) != null && ServiceRouteManager.INSTANCE.current(Schema.HTTPS) != null ;
	}
}