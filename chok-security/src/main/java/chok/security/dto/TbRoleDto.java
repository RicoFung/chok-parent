package chok.security.dto;

import java.io.Serializable;
/**
 *
 * @author rico
 * @version 1.0
 * @since 1.0
 * */
public class TbRoleDto implements Serializable
{
	private static final long serialVersionUID = 1L;
	
    // id       db_column: id 
	private java.lang.Long id;
    // tcCode       db_column: tc_code 
	private java.lang.String tcCode;
    // tcName       db_column: tc_name 
	private java.lang.String tcName;

	public TbRoleDto(){
	}

	public TbRoleDto(
		java.lang.Long id,
		java.lang.String tcCode,
		java.lang.String tcName
	)
	{
		this.id = id;
		this.tcCode = tcCode;
		this.tcName = tcName;
	}

	public void setId(java.lang.Long value) 
	{
		this.id = value;
	}
	
	public java.lang.Long getId() 
	{
		return this.id;
	}
	public void setTcCode(java.lang.String value) 
	{
		this.tcCode = value;
	}
	
	public java.lang.String getTcCode() 
	{
		return this.tcCode;
	}
	public void setTcName(java.lang.String value) 
	{
		this.tcName = value;
	}
	
	public java.lang.String getTcName() 
	{
		return this.tcName;
	}
}

