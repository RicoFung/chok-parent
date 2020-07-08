package chok.devwork.springboot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;

import chok.common.RestResult;
import chok.util.POIUtil;
import chok.util.TimeUtil;

public class BaseRestController<T>
{
	protected RestResult restResult;

	protected ObjectMapper restMapper;
	
	protected HttpServletResponse response;

	@ModelAttribute
	public void BaseInitialization(HttpServletRequest request, HttpServletResponse response)
	{
		this.restResult = new RestResult();
		this.restMapper = new ObjectMapper();
		this.response = response;
	}

	protected List<String> getValidMsgList(BindingResult validResult)
	{
		List<String> validMsgList = new ArrayList<String>();
		for (ObjectError oError : validResult.getAllErrors())
		{
			validMsgList.add(oError.getDefaultMessage());
		}
		return validMsgList;
	}

	protected String getValidMsgs(BindingResult validResult)
	{
		StringBuilder validMsgsBuilder = new StringBuilder();
		List<String> validMsgList = getValidMsgList(validResult);
		for (String validMsg : validMsgList)
		{
			validMsgsBuilder.append(validMsg + ";");
		}
		return StringUtils.removeEnd(validMsgsBuilder.toString(), ";");
	}
	
	public void export(List<?> list, String fileName, String title, String headerNames, String dataColumns, String exportType)
	{
		ByteArrayOutputStream ba = null;
		ServletOutputStream out = null;
		try
		{
			try 
			{
				ba = new ByteArrayOutputStream();
				ba = (ByteArrayOutputStream) POIUtil.writeExcel(ba, 
						fileName, 
						title, 
						headerNames, 
						dataColumns, 
						list);
				
				response.reset();// 清空输出流
				response.setHeader("Content-disposition", "attachment; filename="
						+ fileName
						+ "_"
						+ TimeUtil.formatDate(new Date(), "yyyyMMdd_HHmmss") + "." +"xlsx");
				if(exportType.equals("xlsx"))
					response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");// 定义输出类型:xlsx
				else
					response.setContentType("application/msexcel;charset=UTF-8");// 定义输出类型:xls
				response.setContentLength(ba.size());
				out = response.getOutputStream();
				ba.writeTo(out);
				out.flush();
			}
			finally 
			{
				if (out != null) out.close();
				if (ba != null) ba.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
