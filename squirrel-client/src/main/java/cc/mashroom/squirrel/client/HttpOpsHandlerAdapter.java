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
import  java.util.concurrent.TimeUnit;

import  org.apache.commons.codec.binary.Hex;

import  cc.mashroom.db.common.Db;
import  cc.mashroom.db.common.Db.Callback;
import  cc.mashroom.db.connection.Connection;
import  cc.mashroom.squirrel.client.connect.UserMetadata;
import  cc.mashroom.squirrel.client.connect.call.Call;
import  cc.mashroom.squirrel.client.connect.call.CallError;
import  cc.mashroom.squirrel.client.connect.call.CallEventDispatcher;
import  cc.mashroom.squirrel.client.connect.util.HttpUtils;
import  cc.mashroom.squirrel.client.storage.Storage;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.repository.OfflineRepository;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.transport.ConnectState;
import  cc.mashroom.util.DigestUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
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
import  okhttp3.Interceptor.Chain;

public  class  HttpOpsHandlerAdapter       extends  TransportLifecycleHandlerAdapter<HttpOpsHandlerAdapter>
{
	@Getter
	private  CallEventDispatcher   callEventDispatcher       = new  CallEventDispatcher();
	
	private  Storage  storage    = new  Storage();
	@Getter
	@Setter(value=AccessLevel.PUBLIC   )
	@Accessors(  chain=true )
	protected  Call     call;
	@Setter(value=AccessLevel.PROTECTED)
	@Getter
	@Accessors( chain= true )
	private  File   cacheDir;
	@Setter(value=AccessLevel.PROTECTED)
	@Accessors( chain= true )
	private  UserMetadata  userMetadata;
	@Setter(value=AccessLevel.PROTECTED)
	@Accessors( chain= true )
	private  Map<String, Object>connectParameters;
	
	protected  void  newCall( long  contactId,@NonNull  CallContentType  callContentType )
	{
		try(Response  clresponse = okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(System.getProperty("squirrel.dt.server.schema","https")).host(super.getServiceRouteManager().service().getHost()).port(Integer.parseInt(System.getProperty("squirrel.dt.server.port","8011"))).addPathSegments("call/room/status").build()).post(new  FormBody.Builder().add("calleeId",String.valueOf(contactId)).add("contentType",String.valueOf(callContentType.getValue())).build()).build()).execute() )
		{
		if (clresponse.code()  == 200  )
			{
				this.callEventDispatcher.onRoomCreated( this.call = new  Call(this,Long.parseLong(clresponse.body().string()),contactId,callContentType) );
			}
			else
			{
				throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** CALL  OPS  HANDLER  ADAPTER **  can  not  create  a  new  call  for  response  code  ( %d ).",clresponse.code()) );
			}
		}
		catch( Throwable  e )
		{
			callEventDispatcher.onError( null/* SHOULD  NULL */,CallError.CREATE_ROOM,e );
		}
	}
	/**
	 *  clear  all  states,  include  user  metadata,  connect  parameters,  call,  lifecycle  listener  and  super  class  states  ( id,  authenticate  state  and  connect  state ).
	 */
	@Override
	protected  void   reset()
	{
		userMetadata =  null;
		
		this.setConnectParameters(null);
	}
	/**
	 *  connect  the  server.  throws  illegal  state  exception  if  not  routed. use  latest  connect  parameters  if  the  username  is  not  blank.  http  request  ( include  username,  password  encrypted,  longitude,  latitude  and  mac.  connect  and  read  timeout  are  5  seconds )  will  be  used  to  retrieve  the  secret  key.  initialize  user  metadata  and  offline  datas,  connect  to  paip  protocol  server  after  successful  authentication.  authenticate  complete  method  on  lifecycle  listener  will  be  called  no  matter  authentication  error  or  not.  connectivity  guarantor  will  be  scheduled  at  fixed  rate  (5  seconds)  after  connecting  by  id  or  authenticated  successfully.
	 */
	protected  HttpOpsHandlerAdapter  connect( String  username,boolean  isPasswordEncrypted,String  password,Double  longitude,Double  latitude,String  mac )
	{
		try(Response  clresponse = okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(System.getProperty("squirrel.dt.server.schema","https")).host(super.getServiceRouteManager().service().getHost()).port(Integer.parseInt(System.getProperty("squirrel.dt.server.port","8011"))).addPathSegments("user/signin").build()).post(HttpUtils.form(this.connectParameters = new  HashMap<String,Object>().addEntry("username",username).addEntry("password",isPasswordEncrypted ? password : new  String(Hex.encodeHex(DigestUtils.md5(password))).toUpperCase()).addEntry("protocolVersion",ConnectPacket.CURRENT_PROTOCOL_VERSION).addEntry("longitude",longitude).addEntry("latitude",latitude).addEntry("mac",mac))).build()).execute() )
		{
		if( clresponse.code()  == 200  )
			{
				setUserMetadata(JsonUtils.mapper.readValue(clresponse.body().string(),UserMetadata.class)).checkConnectivity( 10 );
				//  connecting  to  the  user's  database  and  merge  offline  datas  from  remote  server  to  native  storage.
				this.storage.initialize( this, false,lifecycleEventDispatcher,this.cacheDir,this.userMetadata,this.connectParameters.getString("password") );
			
				this.lifecycleEventDispatcher.onAuthenticateComplete( clresponse.code() );
				
				this.connect( String.valueOf(this.userMetadata.getId()),this.userMetadata.getSecretKey() );
			}
			else
			{
				this.lifecycleEventDispatcher.onAuthenticateComplete( clresponse.code() );
			}
		}
		catch( Throwable  e )
		{
		lifecycleEventDispatcher.onAuthenticateComplete( e instanceof SocketTimeoutException ? 501 : 500 );
		}
		
		return   this;
	}
	@SneakyThrows
	public  UserMetadata  userMetadata()
	{
		return  this.userMetadata== null  ? null : userMetadata.clone();
	}
	@Override
	protected  void  onConnectStateChanged( ConnectState  connectState )
	{
		if( connectState==ConnectState.CONNECTED )  super.lifecycleEventDispatcher.onReceivedOfflineData( (OoIData)  Db.tx(String.valueOf(this.userMetadata.getId()),java.sql.Connection.TRANSACTION_REPEATABLE_READ,new  Callback(){public  Object  execute(Connection  connection)  throws  Throwable{ return  OfflineRepository.DAO.attach(HttpOpsHandlerAdapter.this,true,true,true,true); }}) );
		
		lifecycleEventDispatcher.onConnectStateChanged(   connectState);
	}
	
	public  Response  intercept( Chain  reqchain )  throws   IOException
	{
		return  reqchain.proceed( reqchain.request().newBuilder().addHeader("SECRET_KEY",this.userMetadata == null || this.userMetadata.getSecretKey() == null ? "" : this.userMetadata.getSecretKey()).build() );
	}
	
	@Override
	public  void disconnect()
	{
		try(Response  clresponse = okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(System.getProperty("squirrel.dt.server.schema","https")).host(super.getServiceRouteManager().service().getHost()).port(Integer.parseInt(System.getProperty("squirrel.dt.server.port","8011"))).addPathSegments("user/logout").build()).post(new  FormBody.Builder().build()).build()).execute() )
		{
		if (clresponse.code()  == 200  )
			{
			this.lifecycleEventDispatcher.onLogoutComplete( 200,DisconnectAckPacket.REASON_CLIENT_LOGOUT );
			
				this.reset();  super.disconnect();
			}
			else
			{
				throw  new  IllegalStateException( String.format("SQUIRREL-CLIENT:  ** HTTP  OPS  HANDLER  ADAPTER **  can  not  disconnect  for  response  code  ( %d ).",clresponse.code()) );
			}
		}
		catch( Throwable  e )
		{
			this.lifecycleEventDispatcher.onLogoutComplete( 500,DisconnectAckPacket.REASON_CLIENT_LOGOUT );
		}
	}

	public  OkHttpClient  okhttpClient(          long  connectTimeoutSeconds,long  writeTimeoutSeconds,long    readTimeoutSeconds )
	{
		return  new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).addInterceptor((Interceptor) this).connectTimeout(connectTimeoutSeconds,TimeUnit.SECONDS).writeTimeout(writeTimeoutSeconds,TimeUnit.SECONDS).readTimeout(readTimeoutSeconds,TimeUnit.SECONDS).build();
	}
}