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
package cc.mashroom.squirrel.client.storage.repository.user;

import  java.sql.Timestamp;
import  java.util.List;

import  org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.client.storage.RepositorySupport;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.LinkedMap;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(   name="*",table="contact",primaryKeys="ID" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  ContactRepository    extends  RepositorySupport
{
	public  final  static  ContactRepository  DAO = new  ContactRepository();
	
	private  LinkedMap<Long,Contact>  contactDirect  = new  LinkedMap<Long,Contact>();
	
	private  ArrayListValuedHashMap<String,Contact>  contactGroups = new  ArrayListValuedHashMap<String,Contact>();
	
	public  int  upsert(      Contact  contact,boolean  isUpdateNewsProfile ) throws  IllegalArgumentException,IllegalAccessException
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Contact  older = this.contactDirect.put( contact.getId() , contact );

		if( contact.getSubscribeStatus() == 7 || contact.getSubscribeStatus()   == 8 )
		{
			if( older!= null )
			{
				this.contactGroups.removeMapping(        older.getGroupName(),older );
			}

			this.contactGroups.put( contact.getGroupName(), contact );
			//  considering  the  performance,  the  news  profile  should  not  be  updated  while  the  news  profile  of  the  latest  chat  message  will  override  it,  especially  for  offline  messages.
			if( isUpdateNewsProfile    )
			{
				NewsProfileRepository.DAO.update( "DELETE  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{contact.getId(),PAIPPacketType.SUBSCRIBE.getValue()} );
				
				NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contact.getId(),now,PAIPPacketType.CHAT.getValue(),contact.getId(),"${"+StringUtils.leftPad(Integer.toHexString(PAIPPacketType.SUBSCRIBE_ACK.getValue()),2,"0")+StringUtils.leftPad(Integer.toHexString(SubscribeAckPacket.ACK_ACCEPT),2,"0")+"}",1} );
			}
			
			return  super.upsert(       Lists.newArrayList(contact) );
		}
		else
		if( contact.getSubscribeStatus() == 1 || contact.getSubscribeStatus()   == 2 )
		{
//			if( isUpdateNewsProfile    )
			{
				NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contact.getId(),now,PAIPPacketType.SUBSCRIBE.getValue(),contact.getId(),contact.getSubscribeStatus(),1} );
			}
			
			return  super.upsert(       Lists.newArrayList(contact) );
		}

		throw  new  IllegalArgumentException( String.format("SQUIRREL-CLIENT:  ** CONTACT **  subscribe  status  ( %d )  is  not  supported.",  contact.getSubscribeStatus()) );
	}
	
	public  ArrayListValuedHashMap<String,Contact>  getContactGroups()
	{
		return  new  ArrayListValuedHashMap<String,Contact>(contactGroups );
	}
	
	public  LinkedMap<Long,Contact>  getContactDirect()
	{
		return  new  LinkedMap().addEntries(  contactDirect );
	}
	
	public  ContactRepository  recache()
	{
		clearCache();
		
		for(Contact  contact : ContactRepository.DAO.lookup(Contact.class,"SELECT  ID,USERNAME,CREATE_TIME,LAST_MODIFY_TIME,SUBSCRIBE_STATUS,REMARK,GROUP_NAME,IS_DELETED  FROM  "+ContactRepository.DAO.getDataSourceBind().table()) )
		{
			if( (contact.getSubscribeStatus() == 7 || contact.getSubscribeStatus() == 8) && !contact.getIsDeleted() )  this.contactGroups.put( contact.getGroupName(),contact );
		
			this.contactDirect.put( contact.getId(),contact );
		}
		
		return  this;
	}
	
	public  void  attach(    List<Contact>   contacts ) throws  NumberFormatException,IllegalArgumentException,IllegalAccessException
	{
		for(Contact  contact :contacts )
		{
			this.upsert( contact,true );
		}
	}
	
	public  void  clearCache()
	{
		contactDirect.clear();
		
		contactGroups.clear();
	}
	
	public  boolean  remove(   long  contactId )
	{
		super.update( "UPDATE  "+super.getDataSourceBind().table()+"  SET  IS_DELETED = 1  WHERE  ID = ?",new  Object[]{contactId} );  return  this.contactGroups.removeMapping( contactId,this.contactDirect.remove(contactId) );
	}
}