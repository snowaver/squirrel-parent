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
package cc.mashroom.squirrel.client.storage.model.user;

import  java.sql.Timestamp;

import  org.joda.time.DateTime;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.AbstractModel;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.collection.map.Map;

@DataSourceBind( name="*",table="user",primaryKeys="ID" )

public  class  User  extends  AbstractModel< User >
{
	public  final  static  User  dao = new  User();
	
	public  int  upsert( Map<String,Object>  user )
	{
		return  User.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+User.dao.getDataSourceBind().table()+"  (ID,LAST_ACCESS_TIME,USERNAME,PASSWORD,NAME,NICKNAME)  VALUES  (?,?,?,?,?,?)",new  Object[]{user.getLong("ID"),new  Timestamp(DateTime.now().getMillis()),user.getString("USERNAME"),user.getString("PASSWORD"),user.getString("NAME"),user.getString("NICKNAME")} );
	}
}