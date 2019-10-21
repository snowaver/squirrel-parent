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
package cc.mashroom.squirrel.client.storage;

import  java.lang.reflect.Field;
import  java.util.ArrayList;
import  java.util.Collection;
import  java.util.LinkedList;
import  java.util.List;

import  org.apache.commons.lang3.StringUtils;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.db.util.RecordUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.collection.map.Map;

public  class  RepositorySupport  extends  GenericRepository
{
	public  int  upsert( List<?>  beans )//throws  IllegalArgumentException,IllegalAccessException
	{
		if( beans.isEmpty() )
		{
			return  0;
		}
		
		Object  firstBean= beans.get(0 );
		
		if( firstBean.getClass().getPackage().getName().startsWith("java.") &&      !(firstBean instanceof Map) )
		{
			throw  new  IllegalArgumentException( String.format("SQUIRREL-CLIENT:  ** H2  UTILS **  class  ( %s )  can  not  be  resolved.",beans.get(0).getClass().getName()) );
		}
		
		if( firstBean instanceof    Map )
		{
			ArrayList<String>  fields = new  ArrayList<String>( ObjectUtils.cast(firstBean,Map.class).keySet() );
			
			return  ConnectionUtils.batchUpdatedCount( super.insert(new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+super.getDataSourceBind().table()+"  ("+StringUtils.join(fields,",")+")  VALUES  ("+StringUtils.rightPad("?",2*(fields.size()-1)+1,",?")+")",ConnectionUtils.prepare((Collection<? extends Map>)beans,fields)) );
		}
		else
		{
			Map<String,Field>  columnBeanFieldMapper = RecordUtils.createColumnBeanFieldMapper(  firstBean.getClass()  );
			
			if( columnBeanFieldMapper.isEmpty() )
			{
				throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** REPOSITORY  SUPPORT **  can  not  find  any  column  annotated  fields  in  class  ( %s ).",beans.get(0).getClass().getName()) );
			}
			
			ArrayList<String>  fields = new  ArrayList<String>(  columnBeanFieldMapper.keySet() );
			
			try
			{
				return  ConnectionUtils.batchUpdatedCount( super.insert(new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+getDataSourceBind().table()+"  ("+StringUtils.join(fields,",")+")  VALUES  ("+StringUtils.rightPad("?",2*(fields.size()-1)+1,",?")+")",ConnectionUtils.prepare(beans,fields,columnBeanFieldMapper)) );
			}
			catch( IllegalArgumentException | IllegalAccessException  err )
			{
				throw  new  IllegalStateException( "SQUIRREL-CLIENT:  ** REPOSITORY  SUPPORT **  error  in  merging  beans  into  database.",err );
			}
		}
	}
}