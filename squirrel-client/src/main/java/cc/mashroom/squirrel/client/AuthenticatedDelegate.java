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
package cc.mashroom.squirrel.client;

import  java.io.IOException;

import lombok.Setter;
import lombok.experimental.Accessors;
import  okhttp3.Call;
import  okhttp3.Callback;
import  okhttp3.Response;

public  class  AuthenticatedDelegate   implements  Callback
{
	public  AuthenticatedDelegate( SquirrelClient  context,LifecycleListener  lifecycleListener )
	{
		setContext( context ).setLifecycleListener( lifecycleListener );
	}
	
	@Accessors( chain=true )
	@Setter
	private  SquirrelClient  context;
	@Accessors( chain=true )
	@Setter
	private  LifecycleListener  lifecycleListener;
	
	public  void  onFailure(  Call  call,IOException  ioe )
	{
		lifecycleListener.onAuthenticateComplete( 500 );
	}
	
	public  void  onResponse( Call  call,Response  response )  throws  IOException
	{
		try
		{
//			context.setAuthenticated(  response );
			
			lifecycleListener.onAuthenticateComplete( response.code() );
		}
		catch( Exception  e )
		{
			e.printStackTrace();
			
			lifecycleListener.onAuthenticateComplete(500 );
			
			throw  new  IOException( "SQUIRREL-CLIENT:  ** AUTHENTICATED  DELEGATE **  authenticate  internal  error",e );
		}
	}
}