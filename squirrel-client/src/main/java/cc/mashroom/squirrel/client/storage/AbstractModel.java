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

import  java.util.LinkedList;
import  java.util.List;

import  org.apache.commons.lang3.StringUtils;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.XModel;
import  cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.collection.map.Map;

public  class  AbstractModel<M extends XModel>  extends  XModel<M>
{
	public  void  upsert( List<? extends Map>  records )
	{
		if( records.isEmpty() )
		{
			return;
		}
		
		List<String>  fields = new  LinkedList<String>( records.get(0).keySet() );
		
		ConnectionUtils.batchUpdatedCount( insert(new  LinkedList<Reference<Object>>(),"MERGE  INTO  "+getDataSourceBind().table()+"  ("+StringUtils.join(fields,",")+")  VALUES  ("+StringUtils.rightPad("?",2*(fields.size()-1)+1,",?")+")",ConnectionUtils.prepare(Lists.newArrayList(records),fields).toArray(new  Object[records.size()][])) );
	}
}