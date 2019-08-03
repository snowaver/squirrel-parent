package cc.mashroom.squirrel.client;

import  java.util.Collection;

import  cc.mashroom.squirrel.client.storage.model.Offline;

public  class  LifecycleEventDispatcher
{	
	public  static  void  onDisconnected(  Collection<LifecycleListener>  listeners, int  reason )
	{
		for( LifecycleListener  listener : listeners )  listener.onDisconnected( reason );
	}
	
	public  static  void  onReceiveOfflineData( Collection<LifecycleListener>  listeners,Offline  offline )
	{
		for( LifecycleListener  listener : listeners )  listener.onReceivedOfflineData( offline );
	}
	
	public  static  void  onAuthenticateComplete( Collection<LifecycleListener>  listeners,int  authenticateResponseCode )
	{
		for( LifecycleListener  listener : listeners )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
}
