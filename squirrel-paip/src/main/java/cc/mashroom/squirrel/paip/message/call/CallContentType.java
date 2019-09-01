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
package cc.mashroom.squirrel.paip.message.call;

import  lombok.AllArgsConstructor;
import  lombok.Getter;

@AllArgsConstructor
public  enum  CallContentType
{
	AUDIO   (0x01,"${0D01}"),VIDEO( 0x02,"${0D02}" );
	
	@Getter
	private  int  value;
	@Getter
	private  String  placeholder;
	
	public  static  CallContentType  valueOf( int  value )
	{
		for( CallContentType  callContentType : CallContentType.values() )
		{
			if( value == callContentType.getValue() )
			{
				return  callContentType;
			}
		}
		
		throw  new  IllegalArgumentException( String.format("SQUIRREL-PAIP:  ** CALL  TYPES **  no  call  type  defined  for  %d",value) );
	}
}