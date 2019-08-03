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
package cc.mashroom.squirrel.client.storage.repository.chat;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(name="*",table="news_profile",primaryKeys="ID" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  NewsProfileRepository  extends  RepositorySupport
{
	public  final  static  NewsProfileRepository  DAO = new  NewsProfileRepository();
	
	public  int  clearBadgeCount( long  id,int  packetType )
	{
		return  super.update( "UPDATE  "+super.getDataSourceBind().table()+"  SET  BADGE_COUNT = 0  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{id,packetType} );
	}
}