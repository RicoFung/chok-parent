package chok.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
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

public class JasperUtil
{
	
	@SuppressWarnings("unchecked")
	/**
	 * 导出
	 * @param response
	 * @param jasperFileName 报表模板文件名
	 * @param rptFileName 报表文件名
	 * @param rptFileFormat 报表格式
	 * @param rptBizDatasetKV 基础控件数据集KV
	 * @param rptBizDatasetTableKV Table控件数据集KV
	 * @param rptBizDatasetTableClazzes Table控件数据集类型数组
	 * @throws Exception
	 */
	public static void export(HttpServletResponse response, String jasperFileName, String rptFileName, String rptFileFormat, Map<String, ?> rptBizDatasetKV, LinkedHashMap<String, List<?>> rptBizDatasetTableKV, Class<?>... rptBizDatasetTableClazzes) throws Exception
	{
		// 校验
		if (rptBizDatasetTableKV.size() != rptBizDatasetTableClazzes.length)
		{
			throw new Exception("参数[rptBizDatasetTableKV]与[rptBizDatasetTableClazzes]长度不一致！");
		}
		
		// 0.基础控件数据采用JRDataSource来传参
		JRDataSource rptBizDatasetDS = new JREmptyDataSource();
		if (rptBizDatasetKV != null)
		{
			List<Map<String, ?>> rptBizDatasetKVs = new ArrayList<Map<String, ?>>();
			rptBizDatasetKVs.add(rptBizDatasetKV);
			rptBizDatasetDS = new JRMapCollectionDataSource(rptBizDatasetKVs);
		}
		
		// 1.Table控件数据采用Map<>来传参
		Map<String, Object> rptBizDatasetTableParams = new HashMap<String, Object>();
		int index = 0;
		for (Entry<String, List<?>> entry : rptBizDatasetTableKV.entrySet()) {
			String bizDatasetTableK = entry.getKey();
			List<?> bizDatasetTableV = entry.getValue();
			JRDataSource subRptDataSource = null;
			String clazzName = rptBizDatasetTableClazzes[index].getName();
			if (clazzName == null) 
			{
				throw new RuntimeException("数据类型不能为空！");
			}
			if (Object.class.getName().equals(clazzName)) 
			{
				List<Object> subRptData = new ArrayList<Object>();
				// 1）往下标0写入空行数据（由于jasper dataset从索引1开始遍历数据，所以先插入一条空行）
//				subRptData.add(0, null);
				// 2）从下标1开始写入数据
				subRptData.addAll(bizDatasetTableV);
				subRptDataSource = new JRBeanCollectionDataSource(subRptData);
			}
			else if (Map.class.getName().equals(clazzName)) 
			{
				List<Map<String, ?>> subRptData = new ArrayList<Map<String, ?>>();
				// 1）往下标0写入空行数据（由于jasper dataset从索引1开始遍历数据，所以先插入一条空行）
//				subRptData.add(0, null);
				// 2）从下标1开始写入数据
				subRptData.addAll((Collection<? extends Map<String, ?>>) bizDatasetTableV);
				subRptDataSource = new JRMapCollectionDataSource(subRptData);
			}
			else if (HashMap.class.getName().equals(clazzName)) 
			{
				List<Map<String, ?>> subRptData = new ArrayList<Map<String, ?>>();
				// 1）往下标0写入空行数据（由于jasper dataset从索引1开始遍历数据，所以先插入一条空行）
//				subRptData.add(0, null);
				// 2）从下标1开始写入数据
				subRptData.addAll((Collection<? extends Map<String, ?>>) bizDatasetTableV);
				subRptDataSource = new JRMapCollectionDataSource(subRptData);
			} 
			else
			{
				throw new RuntimeException("数据类型不匹配！");
			}
			rptBizDatasetTableParams.put(bizDatasetTableK, subRptDataSource);
			index++;
		}
		
		// 3.按文件格式导出
		exportByFormat(response, jasperFileName, rptFileName, rptBizDatasetTableParams, rptBizDatasetDS, rptFileFormat);
	}
	
	/**
	 * 按文件格式导出
	 * @param response
	 * @param jasperFileName 报表模板文件名
	 * @param rptFileName 报表文件名
	 * @param rptBizDatasetTableParams 报表Table数据参数
	 * @param rptBizDatasetDS 报表数据源
	 * @param rptFileFormat 报表格式
	 * @throws Exception
	 */
	public static void exportByFormat(HttpServletResponse response, String jasperFileName, String rptFileName, Map<String, Object> rptBizDatasetTableParams, JRDataSource rptBizDatasetDS, String rptFileFormat) throws Exception
	{
		File rptFile = compileReportToFile(jasperFileName);
		switch (rptFileFormat)
		{
			case "pdf":
				pdf(response, rptFileName, rptFile.getPath(), rptBizDatasetTableParams, rptBizDatasetDS);
				break;
			case "xlsx":
				xlsx(response, rptFileName, rptFile.getPath(), rptBizDatasetTableParams, rptBizDatasetDS);
				break;
			case "html":
				html(response, rptFile.getPath(), rptBizDatasetTableParams, rptBizDatasetDS);
				break;
			default:
				pdf(response, rptFileName, rptFile.getPath(), rptBizDatasetTableParams, rptBizDatasetDS);
				break;
		}
	}
	
	/**
	 * 编译至文件
	 * @param jasperFileName
	 * @return
	 * @throws JRException
	 * @throws IOException 
	 */
	private static File compileReportToFile(String jasperFileName) throws FileNotFoundException, JRException
	{
		String jrxmlPath = ResourceUtils.getFile("classpath:templates/rpt/" + jasperFileName + ".jrxml").getAbsolutePath();
		String jasperPath = ResourceUtils.getFile("classpath:templates/rpt/" + jasperFileName + ".jasper").getAbsolutePath();
		JasperCompileManager.compileReportToFile(jrxmlPath);
		File rptFile = new File(jasperPath);
		return rptFile;
	}
	
	/**
	 * 生成PDF
	 * @param response
	 * @param rptFileName
	 * @param rptFilePath
	 * @param rptBizDatasetTableParams
	 * @param rptBizDatasetDS
	 * @throws Exception
	 */
	private static void pdf(HttpServletResponse response, String rptFileName, String rptFilePath, Map<String, Object> rptBizDatasetTableParams, JRDataSource rptBizDatasetDS) throws Exception
	{
		byte[] bytes = JasperRunManager.runReportToPdf(rptFilePath, rptBizDatasetTableParams, rptBizDatasetDS);
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
	 * @param rptFilePath
	 * @param rptBizDatasetTableParams
	 * @param rptBizDatasetDS
	 * @throws Exception
	 */
	private static void html(HttpServletResponse response, String rptFilePath, Map<String, Object> rptBizDatasetTableParams, JRDataSource rptBizDatasetDS) throws Exception
	{
		response.reset();// 清空输出流
		response.setContentType("text/html;charset=UTF-8");
		JasperPrint jasperPrint = JasperFillManager.fillReport(rptFilePath, rptBizDatasetTableParams, rptBizDatasetDS);
		HtmlExporter exporter = new HtmlExporter(DefaultJasperReportsContext.getInstance());
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(response.getWriter()));
		exporter.exportReport();
	}
	
	/**
	 * 生成XLSX
	 * @param response
	 * @param rptFileName
	 * @param rptFilePath
	 * @param rptBizDatasetTableParams
	 * @param rptBizDatasetDS
	 * @throws Exception
	 */
	private static void xlsx(HttpServletResponse response, String rptFileName, String rptFilePath, Map<String, Object> rptBizDatasetTableParams, JRDataSource rptBizDatasetDS) throws Exception
	{
		String fileName = rptFileName + "_" + TimeUtil.formatDate(new Date(), "yyyyMMdd_HHmmss")+".xlsx";
		response.reset();// 清空输出流
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
		JasperPrint jasperPrint = JasperFillManager.fillReport(rptFilePath, rptBizDatasetTableParams, rptBizDatasetDS);
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
		SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
		configuration.setOnePagePerSheet(false);
		exporter.setConfiguration(configuration);
		exporter.exportReport();
	}
}
