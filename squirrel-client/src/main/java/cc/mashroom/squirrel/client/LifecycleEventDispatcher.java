package cc.mashroom.squirrel.client;

import  java.util.Collection;

import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.storage.model.OoIData;

public  class  LifecycleEventDispatcher
{
	public  static  void  onConnectStateChanged(  Collection<LifecycleListener>  listeners,   ConnectState  connectState )
	{
		for( LifecycleListener  listener : listeners )  listener.onConnectStateChanged( connectState );
	}
	
	public  static  void  onReceivedOfflineData( Collection<LifecycleListener>  listeners,OoIData  ooiData )
	{
		for( LifecycleListener  listener : listeners )  listener.onReceivedOfflineData( ooiData );
	}
	
	public  static  void  onLogout( Collection<LifecycleListener>  listeners,int  logoutResponseCode  ,int  reason )
	{
		for( LifecycleListener  listener : listeners )  listener.onLogoutComplete(     logoutResponseCode,  reason);
	}
	
	public  static  void  onError( Collection<LifecycleListener>  listeners,Throwable  throwable )
	{
		for( LifecycleListener  listener : listeners )  listener.onError( throwable );
	}
	
	public  static  void  onAuthenticateComplete( Collection<LifecycleListener>  listeners,int  authenticateResponseCode )
	{
		for( LifecycleListener  listener : listeners )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
}
