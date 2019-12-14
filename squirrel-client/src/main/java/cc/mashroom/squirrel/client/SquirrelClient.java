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

import  io.netty.channel.ChannelHandler.Sharable;
import  lombok.AccessLevel;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  cc.mashroom.util.FileUtils;

@Sharable

public  class  SquirrelClient  extends  HttpOpsHandlerAdapter
{
	public  SquirrelClient( Object  context, File  cacheDir )
	{
		this.setContext(context).setCacheDir( FileUtils.createDirectoryIfAbsent(cacheDir) );
	}
	
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors(chain=true)
	private  Object  context;
	
}