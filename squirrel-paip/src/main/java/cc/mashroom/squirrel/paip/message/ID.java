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
package cc.mashroom.squirrel.paip.message;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

public  class  ID
{
	private  static  long  PREVIOUS_ID= 0;
	
	public  synchronized  static  long  create()
	{
		long  nowMillis = DateTime.now(DateTimeZone.UTC).getMillis();  return  PREVIOUS_ID = (nowMillis > PREVIOUS_ID ? nowMillis : PREVIOUS_ID+1);
	}
}
