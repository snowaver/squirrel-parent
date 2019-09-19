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
import  cc.mashroom.squirrel.client.handler.AbstractChannelInboundHandlerAdapter;
import  cc.mashroom.squirrel.client.storage.repository.ServiceRepository;
import  lombok.Getter;
import  lombok.experimental.Accessors;

import  java.sql.Connection;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.common.Db;
import  cc.mashroom.db.common.Db.Callback;
import  cc.mashroom.router.Schema;

public  class  RoutableChannelInboundHandlerAdapter  extends  AbstractChannelInboundHandlerAdapter
{
	@Accessors( chain= true )
	@Getter
	protected  ServiceRouteManager  serviceRouteManager= new  ServiceRouteManager( null );
	/**
	 *  use  previous  cached  services  by  default.  merge  all  requested  services  to  cached  services  if  request  successfully,  then  cache  all  merged  services  and  point  to  a  HTTPS  and  a  TCP  service.
	 */
	protected  RoutableChannelInboundHandlerAdapter  route()
	{
		this.serviceRouteManager.request();
		
		if( !  serviceRouteManager.getServices().isEmpty() )
		{
			Db.tx( "config",Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(cc.mashroom.db.connection.Connection  connection)  throws  Throwable { ServiceRepository.DAO.insert(Lists.newArrayList(serviceRouteManager.getServices()));  return  true; }} );
		}
		
		if( serviceRouteManager.tryNext(Schema.TCP)  != null && serviceRouteManager.tryNext(Schema.HTTPS) != null )
		{
//			System.err.println( "SQUIRREL-CLIENT:  ** ROUTABLE  CHANNEL  INBOUND  HANDLER  ADAPTER **  service  route  successfully." );
		}
		
		return  this;
	}
}