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
package cc.mashroom.squirrel.client.storage.model.chat.group;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.AbstractModel;
import  cc.mashroom.util.Reference;

@DataSourceBind(  name="*",table="chat_group_user",primaryKeys="ID" )

public  class  ChatGroupUser  extends  AbstractModel< ChatGroupUser >
{
	public  final  static  ChatGroupUser  dao = new  ChatGroupUser();
	
	public  int  upsert( ChatGroupUser  chatGroupUser )
	{
		return  ChatGroupUser.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatGroupUser.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,LAST_MODIFY_TIME,CHAT_GROUP_ID,CONTACT_ID,VCARD)  VALUES  (?,?,?,?)",new  Object[]{chatGroupUser.getLong("ID"),chatGroupUser.get("CREATE_TIME"),chatGroupUser.get("LAST_MODIFY_TIME"),chatGroupUser.getLong("CHAT_GROUP_ID"),chatGroupUser.getLong("CONTACT_ID"),chatGroupUser.getString("VCARD")} );
	}
}