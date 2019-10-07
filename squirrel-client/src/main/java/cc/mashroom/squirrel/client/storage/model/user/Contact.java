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

import  java.io.Serializable;
import  java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import  com.fasterxml.jackson.annotation.JsonProperty;

import  cc.mashroom.db.annotation.Column;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=true )
@NoArgsConstructor
@AllArgsConstructor
public  class  Contact  implements  Cloneable     ,Serializable
{
	@JsonProperty(value="ID" )
	@Column( name="ID" )
	private  Long  id;
	@JsonProperty(value="USERNAME" )
	@Column( name="USERNAME" )
	private  String  username;
	@JsonFormat( pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" )
	@JsonProperty(value="CREATE_TIME" )
	@Column( name="CREATE_TIME" )
	private  Timestamp   createTime;
	@JsonFormat( pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" )
	@JsonProperty(value="LAST_MODIFY_TIME"  )
	@Column( name="LAST_MODIFY_TIME"  )
	private  Timestamp  lastModifyTime;
	@JsonProperty(value="SUBSCRIBE_STATUS"  )
	@Column( name="SUBSCRIBE_STATUS"  )
	private  Integer   subscribeStatus;
	@JsonProperty(value="REMARK")
	@Column( name="REMARK"   )
	private  String  remark;
	@JsonProperty(value="GROUP_NAME"  )
	@Column( name="GROUP_NAME"  )
	private  String    groupName;
	@JsonProperty(value="IS_DELETED"  )
	@Column( name="IS_DELETED"  )
	private  Boolean   isDeleted;
	@SneakyThrows
	@Override
	public  Contact  clone()  throws  CloneNotSupportedException
	{
		return  (Contact)super.clone();
	}
}