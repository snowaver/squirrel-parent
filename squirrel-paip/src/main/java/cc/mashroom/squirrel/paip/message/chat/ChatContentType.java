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
package cc.mashroom.squirrel.paip.message.chat;

import  lombok.AllArgsConstructor;
import  lombok.Getter;

@AllArgsConstructor
public  enum  ChatContentType
{
	WORDS(0x00,null),IMAGE(0x01,"$(0a01)"),AUDIO(0x02,"$(0a02)"),VIDEO(0x03,"$(0a03)");
	
	@Getter
	private  int  value;
	@Getter
	private  String  placeholder;
	
	public  static  ChatContentType  valueOf( int  value )
	{
		for( ChatContentType  callType : ChatContentType.values() )
		{
			if( value == callType.getValue() )
			{
				return  callType;
			}
		}
		
		throw  new  IllegalArgumentException( String.format("SQUIRREL-PAIP:  ** CHAT  CONTENT  TYPE **  no  call  type  defined  for  %d",value) );
	}
}