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
package cc.mashroom.squirrel.client.storage.repository;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind( name="config"   ,table="service" )
@NoArgsConstructor(    access=AccessLevel.PRIVATE )
public  class  ServiceRepository  extends  GenericRepository
{
	public  final  static  ServiceRepository  DAO = new  ServiceRepository();
}