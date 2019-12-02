package cc.mashroom.squirrel.client.event;

import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.transport.ConnectState;
import  cc.mashroom.util.event.EventDispather;

public  class  LifecycleEventDispatcher  extends  EventDispather  <LifecycleEventListener>
{
	public  void  onError(Throwable  throwable )
	{
		for( LifecycleEventListener  listener : this.listeners )  listener.onError( throwable );
	}
	
	public  void  onConnectStateChanged( ConnectState  connectState )
	{
		for( LifecycleEventListener  listener : this.listeners )  listener.onConnectStateChanged( connectState  );
	}
	
	public  void  onReceivedOfflineData( OoIData  ooiData )
	{
		for( LifecycleEventListener  listener : this.listeners )  listener.onReceivedOfflineData( ooiData );
	}
	
	public  void  onLogoutComplete( int  responseCode,  int  reason )
	{
		for( LifecycleEventListener  listener : this.listeners )  listener.onLogoutComplete(responseCode,reason );
	}
	
	public  void  onAuthenticateComplete( int  authenticateResponseCode )
	{
		for( LifecycleEventListener  listener : this.listeners )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
}
