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

import  cc.mashroom.db.annotation.Column;
import  lombok.Data;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=true )
public  class  Contact
{
	@Column( name="ID" )
	private  Long  id;
	@Column( name="USERNAME" )
	private  String  username;
	@Column( name="CREATE_TIME" )
	private  Timestamp  createTime;
	@Column( name="LAST_MODIFY_TIME"  )
	private  Timestamp  lastModifyTime;
	@Column( name="SUBSCRIBE_STATUS"  )
	private  Integer   subscribeStatus;
	@Column( name="REMARK"   )
	private  String  remark;
	@Column( name="GROUP_NAME"  )
	private  String    groupName;
	@Column( name="IS_DELETED"  )
	private  Boolean   isDeleted;
}