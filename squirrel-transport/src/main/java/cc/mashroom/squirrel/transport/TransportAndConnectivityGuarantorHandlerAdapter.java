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
package cc.mashroom.squirrel.transport;

import  java.util.concurrent.Future;
import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  lombok.AccessLevel;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

@Accessors( chain=true )
public  class    TransportAndConnectivityGuarantorHandlerAdapter  extends  TransportHandlerAdapter  implements  Runnable
{
	private  ThreadPoolExecutor  connectivityGuarantorThreadPool = new  ThreadPoolExecutor( 1,1,2,TimeUnit.MINUTES,new  LinkedBlockingQueue<Runnable>() );
	@Setter( value=AccessLevel.PRIVATE )
	protected  Future<?>     connectivityCheckingFuture;
	@Setter( value=AccessLevel.PRIVATE )
	private  long  checkIntervalSeconds;
	@Override
	public  void  release()
	{
		super.release();
		
		this.connectivityGuarantorThreadPool.shutdown();
	}
	
	public  void  checkConnectivity( long  checkIntervalSeconds)
	{
		this.setCheckIntervalSeconds(checkIntervalSeconds).setConnectivityCheckingFuture( this.connectivityGuarantorThreadPool.submit(this) );
	}
	@SneakyThrows( value= {InterruptedException.class} )
	public  void   run()
	{
		for( ;super.connectState == ConnectState.DISCONNECTED;Thread.sleep(checkIntervalSeconds*1000) )super.connect( super.transportConfig );
	}
}
