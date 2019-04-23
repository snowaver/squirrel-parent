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
package cc.mashroom.squirrel.client.storage.model.user;

import  java.sql.Timestamp;
import  java.util.List;

import  org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.AbstractModel;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;

@DataSourceBind( name= "*", table="contact",primaryKeys= "ID" )

public  class  Contact  extends  AbstractModel< Contact >
{
	public  final  static  Contact  dao = new  Contact();
	
	private  LinkedMap<Long,Contact>  contactDirect = new  LinkedMap<Long,Contact>();
	
	private  ArrayListValuedHashMap<String,Contact>  contactGroups = new  ArrayListValuedHashMap<String,Contact>();
	
	protected  int  upsert( Contact  contact, boolean  isUpdateNewsProfile )
	{
		Contact  older = contactDirect.put( contact.getLong("ID"),contact );

		if( contact.getInteger("SUBSCRIBE_STATUS") == 6 || contact.getInteger("SUBSCRIBE_STATUS") == 7 )
		{
			if( older!= null )
			{
				contactGroups.removeMapping(  older.getString("GROUP_NAME"), older );
			}

			contactGroups.put(    contact.getString("GROUP_NAME"),contact );
			//  considering  performance,  the  news  profile  should  not  be  updated  while  the  news  profile  of  the  latest  chat  message  will  override  it.
			if( isUpdateNewsProfile )
			{
				NewsProfile.dao.update( "DELETE  FROM  "+NewsProfile.dao.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{contact.getLong("ID"),PAIPPacketType.SUBSCRIBE.getValue()} );
				
				NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contact.getLong("ID"),contact.get("CREATE_TIME"),PAIPPacketType.CHAT.getValue(),contact.getLong("ID"),"&"+StringUtils.leftPad(Integer.toHexString(PAIPPacketType.SUBSCRIBE_ACK.getValue()),2,"0")+StringUtils.leftPad(Integer.toHexString(SubscribeAckPacket.ACK_ACCEPT),2,"0")+";",1} );
			}
			
			return  Contact.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+Contact.dao.getDataSourceBind().table()+"  (ID,USERNAME,CREATE_TIME,LAST_MODIFY_TIME,SUBSCRIBE_STATUS,REMARK,GROUP_NAME,IS_DELETED)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[]{contact.getLong("ID"),contact.getString("USERNAME"),contact.get("CREATE_TIME"),contact.get("LAST_MODIFY_TIME"),contact.getInteger("SUBSCRIBE_STATUS"),contact.getString("REMARK"),contact.getString("GROUP_NAME"),contact.getBoolean("IS_DELETED")} );
		}
		else
		if( contact.getInteger("SUBSCRIBE_STATUS") == 0 || contact.getInteger("SUBSCRIBE_STATUS") == 1 )
		{
//			if( isUpdateNewsProfile )
			{
				NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contact.getLong("ID"),contact.get("CREATE_TIME"),PAIPPacketType.SUBSCRIBE.getValue(),contact.getLong("ID"),  contact.getInteger("SUBSCRIBE_STATUS"),1} );
			}
			
			return  Contact.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+Contact.dao.getDataSourceBind().table()+"  (ID,USERNAME,CREATE_TIME,LAST_MODIFY_TIME,SUBSCRIBE_STATUS,REMARK,GROUP_NAME,IS_DELETED)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[]{contact.getLong("ID"),contact.getString("USERNAME"),contact.get("CREATE_TIME"),contact.get("LAST_MODIFY_TIME"),contact.getInteger("SUBSCRIBE_STATUS"),contact.getString("REMARK"),contact.getString("GROUP_NAME"),contact.getBoolean("IS_DELETED")} );
		}

		throw  new  IllegalArgumentException( String.format("SQUIRREL-CLIENT:  ** CONTACT **  subscribe  status  ( %d )  is  not  supported" , contact.getInteger( "SUBSCRIBE_STATUS" )) );
	}
	
	private  int  upsert( SubscribeAckPacket  packet,TransportState  transportState )
	{
		Contact  contact = contactDirect.get( packet.getContactId() );
		
		return  upsert( ObjectUtils.cast(new  Contact().addEntry("ID",packet.getContactId()).addEntry("USERNAME",packet.getSubscribeeProfile().getString("USERNAME")).addEntry("CREATE_TIME",null).addEntry("LAST_MODIFY_TIME",null).addEntry("IS_DELETED",false).addEntry("SUBSCRIBE_STATUS",transportState == TransportState.RECEIVED ? 7 : 6).addEntry("REMARK",contact.get("REMARK")).addEntry("GROUP_NAME",transportState == TransportState.RECEIVED ? contact.get("GROUP_NAME") : packet.getSubscribeeProfile().getString("GROUP")),Contact.class),true );
	}
		
	private  int  upsert( SubscribePacket  packet,   TransportState  transportState )
	{
		return  upsert( ObjectUtils.cast(new  Contact().addEntry("ID",packet.getContactId()).addEntry("USERNAME",packet.getSubscriberProfile().getString("USERNAME")).addEntry("CREATE_TIME",null).addEntry("LAST_MODIFY_TIME",null).addEntry("IS_DELETED",false).addEntry("SUBSCRIBE_STATUS",transportState == TransportState.RECEIVED ? 1 : 0).addEntry("REMARK",packet.getSubscriberProfile().getString("NICKNAME")).addEntry("GROUP_NAME",transportState == TransportState.RECEIVED ? "" : packet.getSubscriberProfile().getString("GROUP")),Contact.class),true );
	}
	
	public  ArrayListValuedHashMap<String,Contact>  getContactGroups()
	{
		return  new  ArrayListValuedHashMap<String,Contact>(contactGroups );
	}
	
	public  LinkedMap<Long,Contact>    getContactDirect()
	{
		return  new  LinkedMap().addEntries(   contactDirect );
	}
	
	public  Contact  recache()
	{
		clearCache();
		
		for( Contact  contact : Contact.dao.search("SELECT  ID,USERNAME,CREATE_TIME,LAST_MODIFY_TIME,SUBSCRIBE_STATUS,REMARK,GROUP_NAME,IS_DELETED  FROM  "+Contact.dao.getDataSourceBind().table()) )
		{
			if( (contact.getInteger("SUBSCRIBE_STATUS") == 6 || contact.getInteger("SUBSCRIBE_STATUS") == 7) && !contact.getBoolean("IS_DELETED") )  this.contactGroups.put( contact.getString("GROUP_NAME"), contact );
		
			contactDirect.put( contact.getLong("ID"),contact );
		}
		
		return  this;
	}
	
	public  boolean  remove( long  contactId )
	{
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  IS_DELETED = 1  WHERE  ID = ?" , new  Object[]{contactId} );
		
		return  this.contactGroups.removeMapping( contactId ,  this.contactDirect.remove( contactId ) );
	}
	
	public  void  attach( List<Map<String, Object>>  contacts )
	{
		for(    Map<String, Object>  contact : contacts )
		{
			upsert( (Contact)  new  Contact().addEntries(contact.addEntry("ID",new  Long(contact.get("ID").toString())).addEntry("CREATE_TIME",new  Timestamp(DateTime.parse(contact.get("CREATE_TIME").toString()).withZone(DateTimeZone.UTC).getMillis())).addEntry("LAST_MODIFY_TIME",new  Timestamp(DateTime.parse(contact.get("LAST_MODIFY_TIME").toString()).withZone(DateTimeZone.UTC).getMillis()))),true );
		}
		/*
		Stream.forEach( contacts,(contact) -> upsert( (Contact)  new  Contact().addEntries(contact) ) );
		*/
	}
	
	public  void  clearCache()
	{
		contactDirect.clear();
		
		contactGroups.clear();
	}
	
	public   int  upsert( Packet  packet,   TransportState  transportState )
	{
		if( packet instanceof SubscribePacket||packet instanceof SubscribeAckPacket )
		{
			return  packet instanceof SubscribeAckPacket ? upsert( ObjectUtils.cast(packet,SubscribeAckPacket.class),transportState ) : upsert( ObjectUtils.cast(packet,SubscribePacket.class),transportState );
		}
		
		throw  new  IllegalArgumentException( String.format("SQUIRREL-CLIENT:  ** CONTACT **  packet  should  be  instance  of  cc.mashroom.squirrel.simp.message.subscribes.SubscribePacket  or  cc.mashroom.squirrel.simp.message.subscribes.SubscribeAckPacket,  but  found  %s",(packet == null ? null : packet.getClass().getName())) );
	}
}