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

import  java.sql.Timestamp;

import  com.fasterxml.jackson.annotation.JsonProperty;

import  cc.mashroom.db.annotation.Column;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain = true )
@NoArgsConstructor
@AllArgsConstructor
public  class    ChatGroup
{
	@JsonProperty( value="ID"  )
	@Column( name="ID"   )
	private  Long  id;
	@JsonProperty( value="IS_DELETED" )
	@Column( name="IS_DELETED" )
	private  Boolean  isDeleted;
	@JsonProperty( value="CREATE_TIME")
	@Column( name="CREATE_TIME")
	private  Timestamp  createTime;
	@JsonProperty( value="CREATE_BY"  )
	@Column( name="CREATE_BY"  )
	private  String    createBy;
	@JsonProperty( value="LAST_MODIFY_TIME" )
	@Column( name="LAST_MODIFY_TIME"  )
	private  Timestamp  lastModifyTime;
	@JsonProperty( value="LAST_MODIFY_BY"   )
	@Column( name="LAST_MODIFY_BY")
	private  String   lastModifyBy;
	@JsonProperty( value="NAME")
	@Column( name="NAME" )
	private  String  name;
}