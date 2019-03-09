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
package cc.mashroom.squirrel.client.connect.util;

import  java.util.Map.Entry;

import  cc.mashroom.util.collection.map.Map;
import  okhttp3.FormBody;

public  class  HttpUtils
{
	public  static  FormBody  form( Map<String,Object>  parameters )
	{
		FormBody.Builder  builder = new  FormBody.Builder();
		
		for(   Entry<String,Object>  entry : parameters.entrySet() )
		{
			if( entry.getValue() != null )
			{
				builder.add( entry.getKey(),entry.getValue().toString() );
			}
		}
		
		return  builder.build();
	}
}