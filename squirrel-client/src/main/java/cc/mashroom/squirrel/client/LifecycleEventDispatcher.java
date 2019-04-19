package cc.mashroom.squirrel.client;

import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;

class   LifecycleEventDispatcher
{
	protected  final  static  List<LifecycleListener>  LISTENERS   = new  CopyOnWriteArrayList<LifecycleListener>();
	
	public  static  void  removeListener( LifecycleListener  listener )
	{
		if( listener   != null )
		{
			LISTENERS.remove( listener );
		}
	}
	
	public  static  void  addListener(    LifecycleListener  listener )
	{
		if( listener   != null )
		{
			LISTENERS.add(    listener );
		}
	}
	
	public  static  void  onDisconnected( boolean  active )
	{
		for( LifecycleListener  listener : LISTENERS )  listener.onDisconnected( active );
	}
	
	public  static  void  onAuthenticateComplete( int  authenticateResponseCode )
	{
		for( LifecycleListener  listener : LISTENERS )  listener.onAuthenticateComplete( authenticateResponseCode );
	}
	
	public  static  void  onReceiveOfflineData(    boolean   finished )
	{
		for( LifecycleListener  listener : LISTENERS )  listener.onReceiveOfflineData( finished );
	}
}
