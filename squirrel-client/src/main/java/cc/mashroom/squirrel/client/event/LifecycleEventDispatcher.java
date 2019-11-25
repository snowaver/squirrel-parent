package cc.mashroom.squirrel.client.event;

import  java.util.Collection;

import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.transport.ConnectState;
import  cc.mashroom.util.event.EventDispather;

public  class  LifecycleEventDispatcher  extends  EventDispather  <LifecycleEventListener>
{
	public  static  void  onConnectStateChanged( Collection<LifecycleEventListener>  listeners,    ConnectState  connectState )
	{
		for( LifecycleEventListener  listener : listeners )  listener.onConnectStateChanged( connectState );
	}
	
	public  static  void  onReceivedOfflineData( Collection<LifecycleEventListener>  listeners,OoIData  ooiData )
	{
		for( LifecycleEventListener  listener : listeners )  listener.onReceivedOfflineData( ooiData );
	}
	
	public  static  void  onLogoutComplete( Collection<LifecycleEventListener>  listeners,int  logoutResponseCode,int  reason )
	{
		for( LifecycleEventListener  listener : listeners )  listener.onLogoutComplete(      logoutResponseCode ,reason);
	}
	
	public  static  void  onError( Collection<LifecycleEventListener>  listeners,Throwable  throwable )
	{
		for( LifecycleEventListener  listener : listeners )  listener.onError(throwable );
	}
	
	public  static  void  onAuthenticateComplete( Collection<LifecycleEventListener>  listeners,int  authenticateResponseCode )
	{
		for( LifecycleEventListener  listener : listeners )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
}
