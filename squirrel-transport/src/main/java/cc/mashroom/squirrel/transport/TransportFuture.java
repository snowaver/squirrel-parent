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

import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;
import  java.util.concurrent.CountDownLatch;
import  java.util.concurrent.ExecutionException;
import  java.util.concurrent.Future;
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.TimeoutException;
import  java.util.concurrent.atomic.AtomicBoolean;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AccessLevel;
import  lombok.NonNull;
import  lombok.RequiredArgsConstructor;
import  lombok.Setter;
import  lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors( chain=true )
public  class  TransportFuture<T extends PendingAckPacket<?>>      implements  Future<T>
{
	private  AtomicBoolean  isSuccess   = new  AtomicBoolean(false );
	
	private  List<TransportFutureListener<T>>  transportFutureListeners = new  CopyOnWriteArrayList<TransportFutureListener<T>>();
	
	private  AtomicBoolean  isCancelled = new  AtomicBoolean(false );

	private  AtomicBoolean  isDone  = new  AtomicBoolean( false );
	
	private  CountDownLatch  latch  = new  CountDownLatch(1);
	@NonNull
	private  Packet<?>pacekt;
	@Setter( value =  AccessLevel.PACKAGE  )
	private  PendingAckPacket  <?>  pendingAckPacket;
	
	public  boolean  isCancelled()
	{
		return  isCancelled.get();
	}
	
	public  boolean  isDone()
	{
		return  this.isDone.get();
	}
	
	protected  TransportFuture<T>  done( boolean  isSuccess )
	{
		if( updateState( false ) )
		{
			this.isSuccess.set( isSuccess );  this.latch.countDown();  for(    TransportFutureListener<T>  transportFutureListener : transportFutureListeners )  transportFutureListener.onComplete( this );
		}
		
		return  this ;
	}
	
	public  boolean  cancel( boolean  mayInterruptIfRunning )
	{
		if( updateState( true  ) )    this.latch.countDown();
		
		return  true ;
	}
	
	public  T  get()   throws InterruptedException,ExecutionException
	{
		latch.await(); return  ObjectUtils.cast(   pendingAckPacket);
	}
	
	public  T  get( long  timeout,TimeUnit  timeoutTimeUnit )  throws  InterruptedException  ,ExecutionException ,TimeoutException
	{
		this.latch.await( timeout,timeoutTimeUnit  );
		
		return  ObjectUtils.cast( pendingAckPacket );
	}
	
	public  TransportFuture<T>  addTransportFutureListener(  @NonNull  TransportFutureListener<T>  transportFutureListener )
	{
		this.transportFutureListeners.add( transportFutureListener );    if( this.isCancelled() || this.isDone() )  transportFutureListener.onComplete( this );
		
		return  this ;
	}
	
	protected  synchronized  boolean  updateState(  boolean  cancel )
	{
		return  cancel ? !this.isDone.get() && this.isCancelled.compareAndSet(false,true) : !this.isCancelled.get() && this.isDone.compareAndSet( false,true );
	}
}