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

import java.lang.reflect.Field;
import  java.util.LinkedList;
import  java.util.List;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.util.ConnectionUtils;
import cc.mashroom.db.util.RecordUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.Map;

public  class  H2Utils
{
	public  static  int  upsert( GenericRepository  repository,String  table,List<Map<String,Object>>  rcs )
	{
		if( rcs.isEmpty() )
		{
			return  0;
		}
		
		List<String> fields = new  LinkedList<String>( rcs.get(0).keySet() );
		
		return  ConnectionUtils.batchUpdatedCount( repository.insert(new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+table+"  ("+StringUtils.join(fields,",")+")  VALUES  ("+StringUtils.rightPad("?",2*(fields.size()-1)+1,",?")+")",ConnectionUtils.prepare(Lists.newArrayList(rcs),fields).toArray(new  Object[rcs.size()][])) );
	}
	
	public  static  int  upsert( GenericRepository  repository,String  table,List<Object>  rcs )
	{
		if( rcs.isEmpty() )
		{
			return  0;
		}
		
		Map<String,Field>  columnBeanFieldMapper = RecordUtils.createColumnBeanFieldMapper( rcs.get(0).getClass() );
		
		
	}
}