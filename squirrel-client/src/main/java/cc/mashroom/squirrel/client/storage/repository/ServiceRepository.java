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
package cc.mashroom.squirrel.client.storage.repository;

import  java.util.ArrayList;
import  java.util.List;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.router.Service;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind( name="config"   ,table="service" )
@NoArgsConstructor(    access=AccessLevel.PRIVATE )
public  class  ServiceRepository  extends  GenericRepository
{
	public  final  static  ServiceRepository  DAO = new  ServiceRepository();
	
	public  List<Service>  lookup()
	{
		List<Service>  services = new  ArrayList<Service>();
		
		for( Map<String,Object>  service  : super.lookup(Map.class,   "SELECT  ID,SCHEMA,HOST,PORT  FROM  "+super.getDataSourceBind().table()+"  ORDER  BY  ID  ASC") )
		{
			services.add( new  Service().setId(service.getLong("ID")).setSchema(service.getString("SCHEMA")).setHost(service.getString("HOST")).setPort(service.getInteger("PORT")) );
		}
		
		return  services;
	}
	
	public  void  insert( List<Service>  services )
	{
		List<Object[]>  params = new  ArrayList<Object[]>();
		
		for( Service  service : services )
		{
			if( service.getId() > 0 )  params.add( new  Object[]{service.getId(),service.getSchema(),service.getHost(),service.getPort()} );
		}
		
		super.update( "DELETE  FROM  "+super.getDataSourceBind().table()   );
		
		super.update( "INSERT  INTO  "+super.getDataSourceBind().table()+"  (ID,SCHEMA,HOST,PORT)  VALUES  (?,?,?,?)",params.toArray(new  Object[services.size()][]) );
	}
}