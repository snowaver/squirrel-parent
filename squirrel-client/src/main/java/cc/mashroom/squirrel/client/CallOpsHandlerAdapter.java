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
import  java.util.List;
import  java.util.concurrent.CopyOnWriteArrayList;
import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ScheduledFuture;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  org.apache.commons.codec.binary.Hex;

import  cc.mashroom.db.common.Db;
import  cc.mashroom.db.common.Db.Callback;
import  cc.mashroom.db.connection.Connection;
import  cc.mashroom.router.ServiceListRequestStrategy;
import  cc.mashroom.router.ServiceRouteManager;
import  cc.mashroom.squirrel.client.connect.UserMetadata;
import  cc.mashroom.squirrel.client.connect.call.Call;
import  cc.mashroom.squirrel.client.connect.call.CallError;
import  cc.mashroom.squirrel.client.connect.call.CallEventDispatcher;
import  cc.mashroom.squirrel.client.connect.call.CallState;
import  cc.mashroom.squirrel.client.connect.util.HttpUtils;
import  cc.mashroom.squirrel.client.event.LifecycleEventDispatcher;
import  cc.mashroom.squirrel.client.event.LifecycleEventListener;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.repository.OfflineRepository;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.transport.ConnectState;
import  cc.mashroom.squirrel.transport.TransportAndConnectivityGuarantorHandlerAdapter;
import  cc.mashroom.util.DigestUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.NonNull;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.FormBody;
import  okhttp3.HttpUrl;
import okhttp3.Interceptor;
import  okhttp3.OkHttpClient;
import  okhttp3.Request;
import  okhttp3.Response;
import  okhttp3.Interceptor.Chain;

public  class  CallOpsHandlerAdapter   extends  TransportLifecycleHandlerAdapter      <CallOpsHandlerAdapter>
{
	@Getter
	@Accessors(chain=true)
	private  Call    call;
	protected  void  delCall()
	{
		if( call.getState() ==    CallState.CLOSED )
		{
			this.call  = null;
		}
	}

	protected  void  newCall(long  contactId,@NonNull  CallContentType  callContentType)
	{
		try( Response  response = okhttpClient(5,5,10).newCall(new  Request.Builder().url(new  HttpUrl.Builder().scheme(System.getProperty("squirrel.dt.server.schema","https")).host(super.getServiceRouteManager().service().getHost()).port(Integer.parseInt(System.getProperty("squirrel.dt.server.port","8012"))).addPathSegments("call/room/status").build()).post(new  FormBody.Builder().add("calleeId",String.valueOf(contactId)).add("contentType",String.valueOf(contentType.getValue())).build()).build()).execute() )
		{
			if(response.code()  != 200  )
			{
			CallEventDispatcher.onError(null,CallError.CREATE_ROOM,null );
			}
			else
			{
				CallEventDispatcher.onRoomCreated( call = new  Call(this,Long.parseLong(response.body().string()),contactId,contentType) );
			}
		}
		catch( Throwable  ex )
		{
			{
				CallEventDispatcher.onError(null/* CALL  ERROR */,CallError.CREATE_ROOM,   e );
			}
		}
	}
	
	synchronized  void  addCall(   long  roomId,long  contactId,@NonNull  CallContentType  callContentType)
	{
		this.call = new  Call( this,roomId,contactId ,callContentType );
	}
}