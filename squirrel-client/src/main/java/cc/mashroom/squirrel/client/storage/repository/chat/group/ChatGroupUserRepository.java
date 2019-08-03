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
package cc.mashroom.squirrel.client.storage.repository.chat.group;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.util.Reference;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(name="*",table="chat_group_user",primaryKeys="ID")
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  ChatGroupUserRepository  extends  RepositorySupport
{
	public  final  static  ChatGroupUserRepository  DAO = new  ChatGroupUserRepository();
	
	public  int  upsert( ChatGroupUser  chatGroupUser )
	{
		return  super.insert( new  Reference<Object>(),"MERGE  INTO  "+super.getDataSourceBind().table()+"  (ID,CREATE_TIME,LAST_MODIFY_TIME,CHAT_GROUP_ID,CONTACT_ID,VCARD)  VALUES  (?,?,?,?)",new  Object[]{chatGroupUser.getId(),chatGroupUser.getCreateTime(),chatGroupUser.getLastModifyTime(),chatGroupUser.getChatGroupId(),chatGroupUser.getContactId(),chatGroupUser.getVcard()} );
	}
}