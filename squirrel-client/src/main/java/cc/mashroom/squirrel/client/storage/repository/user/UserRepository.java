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
package cc.mashroom.squirrel.client.storage.repository.user;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind( name="*",table="user",primaryKeys="ID" )
@NoArgsConstructor(  access = AccessLevel.PRIVATE )
public  class  UserRepository  extends  RepositorySupport
{
	public  final  static  UserRepository  DAO = new  UserRepository();
	
	public  int  upsert( User  user )
	{
		return  super.insert( new  Reference<Object>(),"MERGE  INTO  "+super.getDataSourceBind().table()+"  (ID,LAST_ACCESS_TIME,USERNAME,PASSWORD,NAME,NICKNAME)  VALUES  (?,?,?,?,?,?)",new  Object[]{user.getId(),user.getLastAccessTime(),user.getUsername(),user.getPassword(),user.getName(),user.getNickname()} );
	}
}