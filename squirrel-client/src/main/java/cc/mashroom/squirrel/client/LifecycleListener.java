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

import  cc.mashroom.squirrel.client.storage.model.OoIData;

public  interface LifecycleListener
{
	public  void  onReceivedOfflineData( OoIData  ooiData );
	
	public  void  onDisconnected();
	/**
	 *  triggered  after  logout  by  specified  reason  (1.active,  2.remote  signin).
	 */
	public  void  onLogout( int  reason );
	
	public  void  onAuthenticateComplete( int  authenticatedResponseCode );
}