package cc.mashroom.squirrel.client.connect;

import  com.fasterxml.jackson.annotation.JsonProperty;

import  cc.mashroom.db.annotation.Column;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain =true )
@NoArgsConstructor
@AllArgsConstructor
public  class  UserMetadata  implements  Cloneable
{
	@JsonProperty( value="ID")
	@Column( name="ID"  )
	private  Long  id;
	@Column( name="USERNAME" )
	private  String  username;
	@JsonProperty( value="NAME" )
	@Column( name="NAME")
	private  String name;
	@JsonProperty( value="NICKNAME"   )
	@Column( name="NICKNAME" )
	private  String  nickname;
	@JsonProperty( value="ROLETYPE"   )
	@Column( name="ROLETYPE" )
	private  Integer roleType;
	@JsonProperty( value="SECRET_KEY" )
	@Column( name="SECRET_KEY"  )
	private  String secretKey;
	
	@SneakyThrows
	public  UserMetadata  clone()
	{
		return  ObjectUtils.cast( super.clone() );
	}
}
