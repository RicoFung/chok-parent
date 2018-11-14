package chok.security.dto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author rico
 * @version 1.0
 * @since 1.0
 */
public class TbAuthorityDto implements Serializable
{
	private static final long serialVersionUID = 1L;

	// id db_column: id
	private java.lang.Long id;
	// pid db_column: pid
	private java.lang.Long pid;
	// tcAppId db_column: tc_app_id
	private java.lang.Long tcAppId;
	// tcCode db_column: tc_code
	private java.lang.String tcCode;
	// tcName db_column: tc_name
	private java.lang.String tcName;
	// 1:菜单权限 2:按钮权限 db_column: tc_type
	private java.lang.Integer tcType;
	// tcUrl db_column: tc_url
	private java.lang.String tcUrl;
	// tcOrder db_column: tc_order
	private java.lang.String tcOrder;

	private List<TbRoleDto> tcRoles;

	public TbAuthorityDto()
	{
	}

	public TbAuthorityDto(java.lang.Long id, java.lang.Long pid, java.lang.Long tcAppId, java.lang.String tcCode,
			java.lang.String tcName, java.lang.Integer tcType, java.lang.String tcUrl, java.lang.String tcOrder)
	{
		this.id = id;
		this.pid = pid;
		this.tcAppId = tcAppId;
		this.tcCode = tcCode;
		this.tcName = tcName;
		this.tcType = tcType;
		this.tcUrl = tcUrl;
		this.tcOrder = tcOrder;
	}

	public void setId(java.lang.Long value)
	{
		this.id = value;
	}

	public java.lang.Long getId()
	{
		return this.id;
	}

	public void setPid(java.lang.Long value)
	{
		this.pid = value;
	}

	public java.lang.Long getPid()
	{
		return this.pid;
	}

	public void setTcAppId(java.lang.Long value)
	{
		this.tcAppId = value;
	}

	public java.lang.Long getTcAppId()
	{
		return this.tcAppId;
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

	public void setTcType(java.lang.Integer value)
	{
		this.tcType = value;
	}

	public java.lang.Integer getTcType()
	{
		return this.tcType;
	}

	public void setTcUrl(java.lang.String value)
	{
		this.tcUrl = value;
	}

	public java.lang.String getTcUrl()
	{
		return this.tcUrl;
	}

	public void setTcOrder(java.lang.String value)
	{
		this.tcOrder = value;
	}

	public java.lang.String getTcOrder()
	{
		return this.tcOrder;
	}

	public List<TbRoleDto> getTcRoles()
	{
		return tcRoles;
	}

	public void setTcRoles(List<TbRoleDto> tcRoles)
	{
		this.tcRoles = tcRoles;
	}

}
