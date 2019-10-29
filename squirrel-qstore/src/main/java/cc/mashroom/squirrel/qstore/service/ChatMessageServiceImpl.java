package cc.mashroom.squirrel.qstore.service;

public  interface  ChatMessageServiceImpl
{
	public  void  add( Object[]  params );
	
	public  void  lookup( long  userId,long  syncId );
}
