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
public  class  User
{
	@JsonProperty( value="ID"   )
	@Column( name="ID"   )
	private  Long  id;
	@JsonProperty( value="LAST_ACCESS_TIME" )
	@Column( name="LAST_ACCESS_TIME")
	private  Timestamp  lastAccessTime;
	@JsonProperty( value="USERNAME" )
	@Column( name="USERNAME" )
	private  String  username;
	@JsonProperty( value="PASSWORD" )
	@Column( name="PASSWORD" )
	private  String  password;
	@JsonProperty( value="NAME" )
	@Column( name="NAME" )
	private  String  name;
	@JsonProperty( value="NICKNAME" )
	@Column( name="NICKNAME" )
	private  String  nickname;
}