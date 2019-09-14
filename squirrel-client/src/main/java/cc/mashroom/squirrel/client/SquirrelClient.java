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
package cc.mashroom.squirrel.client;

import  java.io.File;
import  java.io.IOException;
import  java.net.SocketTimeoutException;
import  java.util.Collection;
import  java.util.LinkedHashSet;
import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  org.apache.commons.codec.binary.Hex;

import  io.netty.channel.ChannelHandler.Sharable;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.NonNull;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.FormBody;
import  okhttp3.HttpUrl;
import  okhttp3.Interceptor;
import  okhttp3.OkHttpClient;
import  okhttp3.Request;
import  okhttp3.Response;
import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceListRequestStrategy;
import  cc.mashroom.router.ServiceRouteManager;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.connect.UserMetadata;
import  cc.mashroom.squirrel.client.connect.call.Call;
import  cc.mashroom.squirrel.client.connect.call.CallError;
import  cc.mashroom.squirrel.client.connect.call.CallEventDispatcher;
import  cc.mashroom.squirrel.client.connect.call.CallState;
import  cc.mashroom.squirrel.client.connect.util.HttpUtils;
import  cc.mashroom.squirrel.client.storage.Storage;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.user.UserRepository;
import  cc.mashroom.squirrel.common.Tracer;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.DigestUtils;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;

@Sharable

public  class  SquirrelClient  extends  TcpAutoReconnectChannelInboundHandlerAdapter    implements  Interceptor
{
	public  SquirrelClient( Object  context,File  cacheDir )
	{
		setContext(context).setCacheDir(  FileUtils.createDirectoryIfAbsent(cacheDir) );
	}
	
	private  ScheduledThreadPoolExecutor  connectivityGuarantorThreadPool = new  ScheduledThreadPoolExecutor( 1,new  DefaultThreadFactory("CONNECT-THREAD",false,1) );
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  Object  context;
	private  UserMetadata    userMetadata;
	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors(chain=true)
	private  File   cacheDir;
	/*
	private  List<BalancingProxy>  balancingProxies= new  LinkedList<BalancingProxy>();
	*/
	private  ThreadPoolExecutor  synchronousRunner = new  ThreadPoolExecutor( 1,1,2,TimeUnit.MINUTES,new  LinkedBlockingQueue<Runnable>(),new  DefaultThreadFactory("SYNCHRONOUS-HANDLER-THREAD",false,1) );
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  Map<String,Object>  connectParameters;
	@Getter
	@Accessors(chain=true)
	private  Call    call;
	@Getter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  LinkedHashSet<LifecycleListener>  lifecycleListeners = new  LinkedHashSet<LifecycleListener>();
	@Setter( value=AccessLevel.PROTECTED )
	//  0. normal,  do  nothing.  1. connecting  by  id,  but  network  error. 2. (deprecated:  deliver  to  qos  handler,  it  makes  connecting  immediately )  secret  key  expired,  a  new  secret  key  should  be  requested.
	private  int  connectivityError= 0x00;
	
	private  Runnable        connectivityGuarantor = new  Runnable()   { public  void run()  { check(); } };

	private  synchronized   void   check()
	{
		if( this.connectivityError == 0x01 || this.connectivityError == 0x02 )
		{
			this.connect( null,null,null,null,null, this.lifecycleListeners );
		}
		else
		if( getConnectState() == ConnectState.DISCONNECTED )
		{
			super.connect( String.valueOf(this.userMetadata.getId())    ,this.userMetadata.getSecretKey() );
		}
	}
	
	public  SquirrelClient  addLifecycleListener(LifecycleListener  listener )
	{
		lifecycleListeners.add(listener );   return    this;
	}
	
	public  SquirrelClient  removeLifecycleListener(      LifecycleListener  listener )
	{
		this.lifecycleListeners.remove( listener );
		
		return   this;
	}
	
	protected  SquirrelClient  setConnectState(   ConnectState  connectState )
	{
		return  ObjectUtils.cast(     super.setConnectState( connectState ) );
	}
	
	@SneakyThrows
	public  UserMetadata getUserMetadata()
	{
		return  this.userMetadata  == null ? null : this.userMetadata.clone();
	}
	
	public  void removeCall()
	{
		if( call.getState() !=   CallState.CLOSED )
		{
			throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** SQUIRREL  CLIENT **  can't  close  call  in  %s  state", call.getState().name()) );
		}
		this.call     = null;
	}
	
	synchronized  void  addCall(final  long  roomId,final  long  contactId,@NonNull final  CallContentType  contentType )
	{
		this.call     = new  Call(  this , roomId , contactId , contentType );
	}
	/**
	 *  return  null  if  a  call  exists  or  a  new  call.
	 */
	public  synchronized  void  newCall(final  long contactId,@NonNull  final  CallContentType contentType )
	{
//		if( roomId     <= 0 )
		{
			this.synchronousRunner.execute
			(
				new  Runnable(){public  void  run()
				{
					final  Service  currentService=    ServiceRouteManager.INSTANCE.current( Schema.HTTPS );
					
					try(Response  response = okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(currentService.getSchema()).host(currentService.getHost()).port(currentService.getPort()).addPathSegments("call/room/status").build()).post(new  FormBody.Builder().add("calleeId",String.valueOf(contactId)).add("contentType",String.valueOf(contentType.getValue())).build()).build()).execute() )
					{
						if(response.code() != 200 )
						{
							CallEventDispatcher.onError(null/* CALL  ERROR */, CallError.CREATE_ROOM,null );
						}
						else
						{
							CallEventDispatcher.onRoomCreated( call = new  Call(SquirrelClient.this,Long.parseLong(response.body().string()),contactId,contentType) );
						}
					}
					catch(  Exception  e )
					{
						{
							CallEventDispatcher.onError(null/* CALL  ERROR */, CallError.CREATE_ROOM,   e );
						}
					}
				}}
			);
		}
		/*
		return  this.call != null ? null : ( call = new  Call(this,id >= 1 ? id : Packet.forId( DateTime.now(DateTimeZone.UTC).getMillis() ),contactId,contentType) );
		*/
	}
	
	public  OkHttpClient  okhttpClient( long  connecttimeoutSeconds ,long  readtimeoutSeconds,long  writetimeoutSeconds )
	{
		return  new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).addInterceptor(this).connectTimeout(connecttimeoutSeconds,TimeUnit.SECONDS).writeTimeout(writetimeoutSeconds,TimeUnit.SECONDS).readTimeout(readtimeoutSeconds,TimeUnit.SECONDS).build();
	}
	/**
	 *  connect  by  the  user  id.  the  username  and  encrypted  password,  which  are  stored  in  local  h2  database,  can  be  fetched  by  the  unique  user  id.  a  new  connect  parameters  will  be  created  for  the  https  authenticate  request.
	 */
	protected  SquirrelClient  connect( @NonNull  Long  id, Double  longitude,Double  latitude,String  mac )
	{
		try
		{
			Storage.INSTANCE.initialize( this,true,lifecycleListeners,this.cacheDir,new  UserMetadata().setId(id),null );
			
			User  user = UserRepository.DAO.lookupOne( User.class,"SELECT  ID,LAST_ACCESS_TIME,USERNAME,PASSWORD,NAME,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{id} );
			
			this.userMetadata      = new  UserMetadata( id,user.getUsername(),user.getName(),user.getNickname(),0,null );
			
			this.connectParameters = new  HashMap<String,Object>().addEntry("username",user.getUsername()).addEntry("password",user.getPassword()).addEntry("protocolVersion",ConnectPacket.CURRENT_PROTOCOL_VERSION).addEntry("isConnectingById",true).addEntry("longitude",longitude).addEntry("latitude",latitude).addEntry( "mac",mac );
		
			this.connect( null,null,null,null,(String)  null/*,listeners */ );
		}
		catch( Throwable  e )
		{
			Tracer.trace(e );  LifecycleEventDispatcher.onAuthenticateComplete(this.lifecycleListeners,500);
		}
		
		return   this;
	}
	
	public  void  connect( final  @NonNull  Long  id,final  Double  longitude,final  Double  latitude,final  String  mac,@NonNull  Collection<LifecycleListener>  lifecycleListeners )
	{
		this.lifecycleListeners.addAll( lifecycleListeners);
		
		this.synchronousRunner.execute( new  Runnable(){ public  void  run() { try{ connect(id,longitude,latitude,mac); } catch( Throwable  e ) { LifecycleEventDispatcher.onError( SquirrelClient.this.lifecycleListeners,e); } } } );
	}
	
	public  SquirrelClient  route(final ServiceListRequestStrategy  strategy )
	{
		this.synchronousRunner.execute( new  Runnable(){ public  void  run() { route(     strategy ); } } );
	
		return   this;
	}
	/**
	 *  connect  the  server.  throws  illegal  state  exception  if  not  routed.  use  latest  connect  parameters  if  the  username  is  not  blank.  http  request  ( include  username,  password  encrypted,  longitude,  latitude  and  mac.  connect  and  read  timeout  are  5  seconds )  will  be  used  to  retrieve  the  secret  key.  initialize  user  metadata  and  offline  datas,  connect  to  paip  protocol  server  after  successful  authentication.  authenticate  complete  method  on  lifecycle  listener  will  be  called  no  matter  authentication  error  or  not.  connectivity  guarantor  will  be  scheduled  at  fixed  rate  (5  seconds)  after  connecting  by  id  or  authenticated  successfully.
	 */
	protected  SquirrelClient  connect( String  username,String  password,Double  longitude,Double latitude,String  mac )
	{
		if(    ! isRouted() )
		{
			super.route( super.serviceListRequestStrategy,super.serviceRouteListener );
			
//			ServiceRouteManager.INSTANCE.request();
		}
		
		if(    ! isRouted() )
		{
			throw  new  IllegalStateException(   "SQUIRREL-CLIENT:  ** SQUIRREL  CLIENT **  no  route  is  available." );
		}
		
		if( lifecycleListeners.isEmpty() )
		{
			throw  new  IllegalArgumentException("SQUIRREL-CLIENT:  ** SQUIRREL  CLIENT **  lifecycle  listeners  is  empty."   );
		}
		//  clear  the  connect  parameters  if  the  username  isn' t  blank.
		if( StringUtils.isNotBlank(    username ) )
		{
			this.connectParameters = null;
		}
		//  reset  the  connnectivity  error  to  normal  state, which  should  be  changed  by  the  special  situation.
		this.connectivityError     = 0x00;
		
		Service  service=ServiceRouteManager.INSTANCE.current( Schema.HTTPS );
		
		try(Response  response = okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(service.getSchema()).host(service.getHost()).port(service.getPort()).addPathSegments("/user/signin").build()).post(HttpUtils.form(connectParameters = connectParameters != null ? connectParameters.addEntry("isAutoReconnect",true) : new  HashMap<String,Object>().addEntry("username",username).addEntry("password",new  String(Hex.encodeHex(DigestUtils.md5(password))).toUpperCase()).addEntry("protocolVersion",ConnectPacket.CURRENT_PROTOCOL_VERSION).addEntry("longitude",longitude).addEntry("latitude",latitude).addEntry("mac",mac))).build()).execute() )
		{
			if(   response.code() == 200 )
			{
				this.userMetadata =JsonUtils.mapper.readValue(response.body().string(),UserMetadata.class );
				//  connecting  to  the  user' s  database  and  merge  offline  datas  from  remote  server  to  native  storage.
				Storage.INSTANCE.initialize(this,false,lifecycleListeners,this.cacheDir,this.userMetadata  , connectParameters.getString("password") );
			
				this.connect( String.valueOf(this.userMetadata.getId()), this.userMetadata.getSecretKey() );
			}
			
			LifecycleEventDispatcher.onAuthenticateComplete(    this.lifecycleListeners,  response.code() );
		}
		catch( Throwable  e )
		{
			if( connectParameters.containsKey("isConnectingById") && connectParameters.getBoolean( "isConnectingById" ) )
			{
				connectivityError  = 0x01;
			}
			
			Tracer.trace(e );
			
			LifecycleEventDispatcher.onAuthenticateComplete(this.lifecycleListeners,(e instanceof SocketTimeoutException) ? 501  /* TIMEOUT */ : 500 );
		}
		finally
		{
			//  checking  connectivity  is  necessary  when  connecting  by  id  (authenticated  successfully  last  time)  or  authenticated  successfully.  it  should  be  scheduled  only  once.
			
			if( connectivityGuarantorThreadPool.getTaskCount() == 0 && (connectParameters.containsKey("isConnectingById") && this.connectParameters.getBoolean("isConnectingById") || super.isAuthenticated()) )
			{
				this.connectivityGuarantorThreadPool.scheduleAtFixedRate( connectivityGuarantor,10,10,TimeUnit.SECONDS );
			}
		}
		
		return   this;
	}
	@Override
	public  void  onConnectStateChanged(   ConnectState  changedConnectState )
	{
		LifecycleEventDispatcher.onConnectStateChanged(    this.lifecycleListeners  , changedConnectState );
	}
	/**
	 *  clear  all  states,  include  user  metadata,  connect  parameters,  call,  lifecycle  listener  and  super  class  states  ( id,  authenticate  state  and  connect  state ).
	 */
	protected   void  clear()
	{
		super.clear();
		
		userMetadata =  null;
		
		this.setConnectParameters( null );
		call   = null;
		this.lifecycleListeners.clear(  );
	}
	/**
	 *  close  the  client.  send  a  disconnect  packet  to  the  server,  then  close  the  netty  nio  event  loop  group  and  connectiviy  guarantor  thread  pool.  the  client  instance  can  not  be  used  anymore  after  closed.
	 */
	public  void    release()
	{
		super.close();
		
		connectivityGuarantorThreadPool.shutdown();
	}
	/**
	 *  logout  by  http  protocol,  channel  will  be  closed  by  the  server,  so  only  clear  the  client  after  logged  out  by  http  protocol.
	 */
	public  void disconnect()
	{
		clear();
		//  deprecated:  it  is  not  necessary  that  close  the  channel,  while  the  socket  channel  can  be  reused  anyway.  sending  packet  should  be  restricted  by  id,  connect  state  and  authenticate  state.
//		send(   new  DisconnectPacket() );
	}
	
	public  void  connect( final  String  username,final  String  password,final  Double  longitude,final  Double  latitude,final  String  mac,final  @NonNull  Collection<LifecycleListener>  lifecycleListeners )
	{
		this.lifecycleListeners.addAll( lifecycleListeners);
		
		this.synchronousRunner.execute( new  Runnable(){ public  void  run() { try{ connect(username,password,longitude,latitude,mac,lifecycleListeners); }  catch( Throwable  e ) { LifecycleEventDispatcher.onError(lifecycleListeners,e); } } } );
	}

	public  Response  intercept(     Chain  chain )        throws  IOException
	{
		return  chain.proceed( chain.request().newBuilder().addHeader("SECRET_KEY",this.userMetadata == null || this.userMetadata.getSecretKey() == null     ? "" :    this.userMetadata.getSecretKey()).build() );
	}
}