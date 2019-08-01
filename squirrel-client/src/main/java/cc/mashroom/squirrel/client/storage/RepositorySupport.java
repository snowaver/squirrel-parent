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
import  java.util.LinkedList;
import  java.util.List;

import  org.apache.commons.lang3.StringUtils;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.db.util.RecordUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.collection.map.Map;

public  class  RepositorySupport  extends  GenericRepository
{
	public  int  upsert( List<?>  rcs )  throws  IllegalArgumentException,IllegalAccessException
	{
		if( rcs.isEmpty() )
		{
			return  0;
		}
		
		if( rcs.get(0).getClass().getPackage().getName().startsWith("java.") && !(rcs.get(0) instanceof Map) )
		{
			throw  new  IllegalArgumentException( String.format("SQUIRREL-CLIENT:  ** H2  UTILS **  class  ( %s )  can  not  be  resolved.",rcs.get(0).getClass().getName()) );
		}
		
		if( rcs.get(0) instanceof Map )
		{
			List<String> fields = new  LinkedList<String>(  ObjectUtils.cast(rcs.get(0),Map.class).keySet() );
			
			return  ConnectionUtils.batchUpdatedCount( super.insert(new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+getDataSourceBind().table()+"  ("+StringUtils.join(fields,",")+")  VALUES  ("+StringUtils.rightPad("?",2*(fields.size()-1)+1,",?")+")",ConnectionUtils.prepare((List<? extends Map>)  Lists.newArrayList(rcs),fields).toArray(new  Object[rcs.size()][])) );
		}
		else
		{
			Map<String,Field>  columnBeanFieldMapper = RecordUtils.createColumnBeanFieldMapper( rcs.get(0).getClass() );
			
			if( columnBeanFieldMapper.isEmpty() )
			{
				throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** H2  UTILS **  can  not  find  any  column  annotated  fields  in  class  ( %s ).",rcs.get(0).getClass().getName()) );
			}
			
			List<String>  fields    = new  LinkedList<String>( columnBeanFieldMapper.keySet() );
			
			return  ConnectionUtils.batchUpdatedCount( super.insert(new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+getDataSourceBind().table()+"  ("+StringUtils.join(fields,",")+")  VALUES  ("+StringUtils.rightPad("?",2*(fields.size()-1)+1,",?")+")",ConnectionUtils.prepare(Lists.newArrayList(rcs),fields,columnBeanFieldMapper ).toArray(new  Object[rcs.size()][])) );
		}
	}
}