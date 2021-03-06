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
package cc.mashroom.squirrel.client.event;

import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.transport.ConnectState;

public  interface  LifecycleEventListener
{
	public  void  onError( Throwable  error );
	
	public  void  onLogoutComplete( int  logoutResponseCode,int  reason );
	
	public  void  onReceivedOfflineData( OoIData  ooiData );
	
	public  void  onConnectStateChanged( ConnectState  connectState );
	
	public  void  onAuthenticateComplete( int  authenticateResponseCode );
	
}