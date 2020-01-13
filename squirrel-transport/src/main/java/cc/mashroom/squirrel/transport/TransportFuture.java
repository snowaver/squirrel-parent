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

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  io.netty.channel.Channel;
import  lombok.AccessLevel;
import  lombok.NonNull;
import  lombok.RequiredArgsConstructor;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors( chain=true )
public  class  TransportFuture<T extends PendingAckPacket<?>>  implements  Runnable,Future<T>
{
	@SneakyThrows
	@Override
	public  void   run()
	{
		if( this.packet.getAckLevel() == 1 && !this.channel.writeAndFlush(this.packet).sync().isSuccess() )
		{
			done(false);
		}
		else
		if( this.packet.getAckLevel() == 0 )
		{
			done( this.channel.writeAndFlush( this.packet ).sync().isSuccess());
		}
	}
	public   T  get()  throws  InterruptedException,ExecutionException
	{
	latch.await();  return  ObjectUtils.cast( this.pendingAckPacket );
	}
	private  List<TransportFutureListener<T>> transportFutureListeners =new  CopyOnWriteArrayList<TransportFutureListener<T>>();
	@Setter( value =   AccessLevel.PACKAGE )
	private  PendingAckPacket  <?>    pendingAckPacket;
	private  CountDownLatch  latch = new  CountDownLatch( 1 );
	private  boolean  isSuccess  ;
	private  boolean  isCancelled;
	private  boolean  isDone;
	@NonNull
	private  Channel      channel;
	@NonNull
	private  Packet  <?>   packet;
	public  boolean  isDone()
	{
		return   isDone;
	}
	public  boolean  isCancelled()
	{
		return   isCancelled;
	}
	protected  TransportFuture<T>  done(  boolean  isSuccess )
	{
		if( !this.isCancelled && (this.isDone = true) )
		{
			this.isSuccess = isSuccess;this.latch.countDown();  for( TransportFutureListener<T>  listener :     this.transportFutureListeners )  listener.onComplete( this );
		}
		return  this;
	}
	public  boolean  cancel( boolean   mayInterruptIfRunning )
	{
		if( !this.isDone && (this.isCancelled = true) )  this.latch.countDown();
		
		return  true;
	}
	public  T  get(  long  timeout,TimeUnit  timeoutTimeUnit )  throws  InterruptedException,ExecutionException,TimeoutException
	{
		this.latch.await(  timeout,  timeoutTimeUnit );  return  ObjectUtils.cast( this.pendingAckPacket );
	}
	public  TransportFuture<T>  addTransportFutureListener(@NonNull  TransportFutureListener<T>  listener )
	{
		this.transportFutureListeners.add(  listener );  if( this.isCancelled() || this.isDone() )  listener.onComplete( this );  return  this;
	}
}