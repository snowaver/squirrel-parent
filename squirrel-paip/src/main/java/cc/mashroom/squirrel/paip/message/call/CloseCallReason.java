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
public  enum  CloseCallReason
{
	UNKNOWN(0x00),ROOM_NOT_FOUND(0x01),STATE_ERROR(0x02),CANCEL(0x03),TIMEOUT(0x04),REJECT(0x05),CANCELED(0x06), CLOSE_ACTIVELY(0x07),NETWORK_ERROR( 0x08 );
	
	@Getter
	private  int  value;
	
	public  static  CloseCallReason  valueOf( int  value )
	{
		for( CloseCallReason  closeCallReason : CloseCallReason.values() )
		{
			if( value == closeCallReason.getValue() )
			{
				return  closeCallReason;
			}
		}
		
		throw  new  IllegalArgumentException( String.format("SQUIRREL-PAIP:  ** CLOSE  CALL  REASON **  no  close  call  reason  defined  for  %d",value) );
	}
}