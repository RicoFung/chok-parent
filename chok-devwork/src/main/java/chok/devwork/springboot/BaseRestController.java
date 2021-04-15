package chok.devwork.springboot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import chok.common.RestConstants;
import chok.common.RestResult;
import chok.util.POIUtil;
import chok.util.TimeUtil;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

public class BaseRestController<T>
{
    @Autowired
    private Validator validator;
    
	protected RestResult restResult;

	protected ObjectMapper restMapper;
	
	protected HttpServletRequest request;
	protected HttpServletResponse response;

	@ModelAttribute
	public void BaseInitialization(HttpServletRequest request, HttpServletResponse response)
	{
		this.restResult = new RestResult();
		this.restMapper = new ObjectMapper();
		this.request = request;
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
	
	protected List<String> getValidMsgListBySet(Set<ConstraintViolation<?>> validSet)
	{
		List<String> validMsgList = new ArrayList<String>();
		for (ConstraintViolation<?> constraintViolation : (Set<ConstraintViolation<?>>)validSet) 
		{
			validMsgList.add(constraintViolation.getMessage());
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
	
	@SuppressWarnings("unchecked")
	protected String getValidMsgsBySet(Object validSet)
	{
		StringBuilder validMsgsBuilder = new StringBuilder();
		List<String> validMsgList = getValidMsgListBySet((Set<ConstraintViolation<?>>) validSet);
		for (String validMsg : validMsgList)
		{
			validMsgsBuilder.append(validMsg + ";");
		}
		return StringUtils.removeEnd(validMsgsBuilder.toString(), ";");
	}
	
	protected RestResult validImportBefore(MultipartFile file)
	{
		restResult = new RestResult();
		// 校验file
		if (file == null)
		{
			restResult.setSuccess(false);
			restResult.setCode(RestConstants.ERROR_CODE1);
			restResult.setMsg("file不能为空！");
		}
		return restResult;
	}
	
	protected RestResult validImportBefore(MultipartFile file, String json, Class<?> clazz) throws JsonParseException, JsonMappingException, IOException
	{
		restResult = new RestResult();
		// 校验file
		if (file == null)
		{
			restResult.setSuccess(false);
			restResult.setCode(RestConstants.ERROR_CODE1);
			restResult.setMsg("file不能为空！");
		}
		// 校验json
		if (json != null)
		{
			Object jsonDTO = restMapper.readValue(json, clazz);
			Set<ConstraintViolation<Object>> validSet = validator.validate(jsonDTO);
			if (validSet.size() > 0) 
			{
				restResult.setSuccess(false);
				restResult.setCode(RestConstants.ERROR_CODE1);
				restResult.setMsg(getValidMsgsBySet(validSet));
			}
		}
		return restResult;
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
				response.setHeader("Content-Disposition", "attachment; filename="
						+ java.net.URLEncoder.encode(fileName, "UTF-8")
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
	
	/**
	 * 导出JRMapList
	 * @param bizDatasetKey
	 * @param bizDatasetValue
	 * @param jasperFileName
	 * @param rptFileName
	 * @param rptFileFormat ("pdf"/"xlsx"/"html")
	 * @throws Exception
	 */
	public void expJRMapList(String bizDatasetKey, List<Map<String, ?>> bizDatasetValue, String jasperFileName, String rptFileName, String rptFileFormat) throws Exception 
	{
		// 0.定义pdf参数集
		Map<String, Object> rptParams = new HashMap<String, Object>();
		// 1.转换Dataset数据集参数
		// 业务数据转pdf数据
		List<Map<String, ?>> rptData = new ArrayList<Map<String, ?>>();
		// 1）往下标0写入空行数据（由于jasper dataset从索引1开始遍历数据，所以先插入一条空行）
		rptData.add(0, null);
		// 2）从下标1开始写入数据
		rptData.addAll(bizDatasetValue);
		// 转换数据源
		JRDataSource rptDataSource = new JRMapCollectionDataSource(rptData);
		rptParams.put(bizDatasetKey, rptDataSource);
		
		// 2.编译报表模板
		String jrxmlPath = ResourceUtils.getFile("classpath:templates/rpt/" + jasperFileName + ".jrxml").getAbsolutePath();
		String jasperPath = ResourceUtils.getFile("classpath:templates/rpt/" + jasperFileName + ".jasper").getAbsolutePath();
		JasperCompileManager.compileReportToFile(jrxmlPath);
		
		// 3.输出文件
		// 按格式生成
		File rptFile = new File(jasperPath);
		switch (rptFileFormat)
		{
			case "pdf":
				pdf(rptFileName, rptFile.getPath(), rptParams, rptDataSource);
				break;
			case "xlsx":
				xlsx(rptFileName, rptFile.getPath(), rptParams, rptDataSource);
				break;
			case "html":
				html(rptFile.getPath(), rptParams, rptDataSource);
				break;
			default:
				pdf(rptFileName, rptFile.getPath(), rptParams, rptDataSource);
				break;
		}
	}
	
	/**
	 * 导出JRBeanList
	 * @param bizDatasetKey
	 * @param bizDatasetValue
	 * @param jasperFileName
	 * @param rptFileName
	 * @param rptFileFormat ("pdf"/"xlsx"/"html")
	 * @throws Exception
	 */
	public void expJRBeanList(String bizDatasetKey, List<?> bizDatasetValue, String jasperFileName, String rptFileName, String rptFileFormat) throws Exception 
	{
		// 0.定义pdf参数集
		Map<String, Object> rptParams = new HashMap<String, Object>();
		// 1.转换Dataset数据集参数
		// 业务数据转pdf数据
		List<Object> rptData = new ArrayList<Object>();
		// 1）往下标0写入空行数据（由于jasper dataset从索引1开始遍历数据，所以先插入一条空行）
		rptData.add(0, null);
		// 2）从下标1开始写入数据
		rptData.addAll(bizDatasetValue);
		// 转换数据源
		JRDataSource rptDataSource = new JRBeanCollectionDataSource(rptData);
		rptParams.put(bizDatasetKey, rptDataSource);
		
		// 2.编译报表模板
		String jrxmlPath = ResourceUtils.getFile("classpath:templates/rpt/" + jasperFileName + ".jrxml").getAbsolutePath();
		String jasperPath = ResourceUtils.getFile("classpath:templates/rpt/" + jasperFileName + ".jasper").getAbsolutePath();
		JasperCompileManager.compileReportToFile(jrxmlPath);
		
		// 3.输出文件
		// 按格式生成
		File rptFile = new File(jasperPath);
		switch (rptFileFormat)
		{
			case "pdf":
				pdf(rptFileName, rptFile.getPath(), rptParams, rptDataSource);
				break;
			case "xlsx":
				xlsx(rptFileName, rptFile.getPath(), rptParams, rptDataSource);
				break;
			case "html":
				html(rptFile.getPath(), rptParams, rptDataSource);
				break;
			default:
				pdf(rptFileName, rptFile.getPath(), rptParams, rptDataSource);
				break;
		}
	}
	
//	public void exportPdf(Map<String, List<Map<String, ?>>> bizDatasetParams, Map<String, String> bizDataParams, String jasperFileName, String reportFileName) throws JRException, IOException 
//	{
//		// 定义pdf参数集
//		Map<String, Object> pdfParams = new HashMap<String, Object>();
//		// 1.转换Dataset数据集参数
//		if (bizDatasetParams != null)
//		{
//			bizDatasetParams.forEach((k, v) -> {
//				// 业务数据转pdf数据
//				List<Map<String, ?>> pdfData = new ArrayList<Map<String, ?>>();
//				// 1）往下标0写入空行数据（由于jasper dataset从索引1开始遍历数据，所以先插入一条空行）
//				pdfData.add(0, null);
//				// 2）从下标1开始写入数据
//				pdfData.addAll(v);
//				// 转换数据源
//				JRDataSource pdfDataSource = new JRMapCollectionDataSource(pdfData);
//				pdfParams.put(k, pdfDataSource);
//			});
//		}
//		// 2.转换String参数
//		if (bizDataParams != null) 
//		{
//			bizDataParams.forEach((k, v) -> {
//				pdfParams.put(k, v);
//			});
//		}
//		// 3.编译报表模板
//		String jrxmlPath = ResourceUtils.getFile("classpath:templates/pdf/" + jasperFileName + ".jrxml").getAbsolutePath();
//		String jasperPath = ResourceUtils.getFile("classpath:templates/pdf/" + jasperFileName + ".jasper").getAbsolutePath();
//		JasperCompileManager.compileReportToFile(jrxmlPath);
//		// 4.输出文件
//		File pdfFile = new File(jasperPath);
//		byte[] bytes = JasperRunManager.runReportToPdf(pdfFile.getPath(), pdfParams, new JRMapCollectionDataSource());
////		byte[] bytes = JasperRunManager.runReportToPdf(pdfFile.getPath(), pdfParams, pdfDataSource);
//		response.reset();// 清空输出流
//		String fileName = reportFileName + "_" + TimeUtil.formatDate(new Date(), "yyyyMMdd_HHmmss")+".pdf";
//		fileName = new String(fileName.getBytes("utf-8"), "ISO_8859_1");
//		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
//		response.setContentType("application/pdf;charset=UTF-8");
//		ServletOutputStream ouputStream = response.getOutputStream();
//		ouputStream.write(bytes, 0, bytes.length);
//		ouputStream.flush();
//		ouputStream.close();
//	}
	

	/**
	 * 生成PDF
	 * @param response
	 * @param reportFilePath
	 * @param param
	 * @param mainDs
	 * @throws Exception
	 */
	private void pdf(String rptFileName, String rptFilePath, Map<String, Object> param, JRDataSource mainDs) throws Exception
	{
		byte[] bytes = JasperRunManager.runReportToPdf(rptFilePath, param, mainDs);
		String fileName = rptFileName + "_" + TimeUtil.formatDate(new Date(), "yyyyMMdd_HHmmss")+".pdf";
	    fileName = new String(fileName.getBytes("utf-8"), "ISO_8859_1");
	    response.reset();// 清空输出流
	    response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		response.setContentType("application/pdf;charset=UTF-8");
		ServletOutputStream ouputStream = response.getOutputStream();
		ouputStream.write(bytes, 0, bytes.length);
		ouputStream.flush();
		ouputStream.close();
	}
	
	/**
	 * 生成HTML
	 * @param response
	 * @param reportFilePath
	 * @param param
	 * @param mainDs
	 * @throws Exception
	 */
	private void html(String rptFilePath, Map<String, Object> param, JRDataSource mainDs) throws Exception
	{
		response.reset();// 清空输出流
		response.setContentType("text/html;charset=UTF-8");
		JasperPrint jasperPrint = JasperFillManager.fillReport(rptFilePath, param, mainDs);
		HtmlExporter exporter = new HtmlExporter(DefaultJasperReportsContext.getInstance());
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(response.getWriter()));
		exporter.exportReport();
	}
	
	/**
	 * 生成XLSX
	 * @param response
	 * @param name
	 * @param reportFilePath
	 * @param param
	 * @param mainDs
	 * @throws Exception
	 */
	private void xlsx(String rptFileName, String rptFilePath, Map<String, Object> param, JRDataSource mainDs) throws Exception
	{
		String fileName = rptFileName + "_" + TimeUtil.formatDate(new Date(), "yyyyMMdd_HHmmss")+".xlsx";
		response.reset();// 清空输出流
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
		JasperPrint jasperPrint = JasperFillManager.fillReport(rptFilePath, param, mainDs);
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
		SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
		configuration.setOnePagePerSheet(false);
		exporter.setConfiguration(configuration);
		exporter.exportReport();
	}
}
