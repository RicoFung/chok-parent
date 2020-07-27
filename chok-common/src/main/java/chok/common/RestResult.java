package chok.common;

import java.util.HashMap;
import java.util.Map;

public class RestResult
{
	private boolean				success		= true;
	private String				code		= RestConstants.SUCCESS_CODE;
	private String				msg			= "Success!";
	private String				path		= "";
	private String				timestamp	= "";
	private Map<Object, Object>	data		= new HashMap<Object, Object>();

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(String timestamp)
	{
		this.timestamp = timestamp;
	}

	public Map<Object, Object> getData()
	{
		return data;
	}

	public void setData(Map<Object, Object> data)
	{
		this.data = data;
	}

	public void put(Object k, Object v)
	{
		this.data.put(k, v);
	}
}
