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
package cc.mashroom.squirrel.client.storage.model.chat;

import  java.sql.Timestamp;

import  com.fasterxml.jackson.annotation.JsonProperty;

import  cc.mashroom.db.annotation.Column;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=true )
@NoArgsConstructor
@AllArgsConstructor
public  class     NewsProfile
{
	@JsonProperty( value="ID"   )
	@Column( name="ID" )
	private  Long  id;
	@JsonProperty( value="CREATE_TIME" )
	@Column( name="CREATE_TIME" )
	private  Timestamp   createTime;
	@JsonProperty( value="PACKET_TYPE" )
	@Column( name="PACKET_TYPE" )
	private  Integer  packetType;
	@JsonProperty( value="CONTACT_ID"  )
	@Column( name="CONTACT_ID"  )
	private  Long  contactId;
	@JsonProperty( value="CONTENT" )
	@Column( name="CONTENT" )
	private  String  content;
	@JsonProperty( value="BADGE_COUNT" )
	@Column( name="BADGE_COUNT" )
	private  Integer  badgeCount;
	
	public   PAIPPacketType     getPacketType()
	{
		return  PAIPPacketType.valueOf( this.packetType );
	}
}