package cc.mashroom.squirrel.client;

import  java.util.Collection;
import  java.util.List;

import  cc.mashroom.util.collection.map.Map;

public  class  LifecycleEventDispatcher
{	
	public  static  void  onDisconnected(  Collection<LifecycleListener>  listeners, int  reason )
	{
		for( LifecycleListener  listener : listeners )  listener.onDisconnected( reason );
	}
	
	public  static  void  onReceiveOfflineData( Collection<LifecycleListener>  listeners,Map<String,List<Map<String,Object>>>  offline )
	{
		for( LifecycleListener  listener : listeners )  listener.onReceivedOfflineData( offline );
	}
	
	public  static  void  onAuthenticateComplete( Collection<LifecycleListener>  listeners, int  authenticateResponseCode )
	{
		for( LifecycleListener  listener : listeners )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
}
