package cc.mashroom.squirrel.client;

import  java.util.Collection;

public  class  LifecycleEventDispatcher
{
	public  static  void  onDisconnected(     Collection<LifecycleListener>  listeners,boolean  proactively )
	{
		for( LifecycleListener  listener : listeners )  listener.onDisconnected(    proactively );
	}
	
	public  static  void  onAuthenticateComplete( Collection<LifecycleListener>  listeners,int  authenticateResponseCode )
	{
		for( LifecycleListener  listener : listeners )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
	
	public  static  void  onReceiveOfflineData( Collection<LifecycleListener>  listeners,boolean   finished )
	{
		for( LifecycleListener  listener : listeners )  listener.onReceiveOfflineData( finished );
	}
}
