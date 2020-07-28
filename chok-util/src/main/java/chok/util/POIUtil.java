package chok.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import chok.common.BaseModel;

public class POIUtil
{
	private final static Logger	log					= LoggerFactory.getLogger(POIUtil.class);
	private final static String	XLS					= "xls";
	private final static String	XLSX				= "xlsx";
	private final static String	MSG_FORMAT_ERR		= "excel格式错误！";
	private final static String	MSG_FILE_NOT_FOUND	= "文件不存在！";
	private static int			titleFontSize		= 14;
	private static int			headerFontSize		= 10;

	/**
	 * 读入EXCEL
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static List<String[]> readExcel(MultipartFile file) throws IOException
	{
		return readExcel(file, null);
	}
	
	/**
	 * 读入EXCEL
	 * @param file
	 * @param sheetSize 读取多少个sheet的数据。为null时，读取所有sheet
	 * @return
	 * @throws IOException
	 */
	public static List<String[]> readExcel(MultipartFile file, Integer sheetSize) throws IOException
	{
		// 检查文件
		checkFile(file);
		// 获得Workbook工作薄对象
		Workbook workbook = getWorkBook(file);
		// 读取到List<String[]>
		return workbookToList(workbook, sheetSize);
	}

	public static List<String[]> readExcel(String filePath) throws Exception
	{
		// 检查文件
		checkFile(filePath);
		// 获得Workbook工作薄对象
		Workbook workbook = getWorkBook(filePath);
		// 读取到List<String[]>
		return workbookToList(workbook);
	}

	/**
	 * 
	 * @param workbook
	 * @return List<String[]>
	 * @throws IOException
	 */
	private static List<String[]> workbookToList(Workbook workbook) throws IOException
	{
		return workbookToList(workbook, null);
	}
	
	/**
	 * 
	 * @param workbook
	 * @param sheetSize
	 * @return
	 * @throws IOException
	 */
	private static List<String[]> workbookToList(Workbook workbook, Integer sheetSize) throws IOException
	{
		// 创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
		List<String[]> list = new ArrayList<String[]>();
		if (workbook != null)
		{
			if (sheetSize == null)
			{
				sheetSize = workbook.getNumberOfSheets();
			}
			for (int sheetNum = 0; sheetNum < sheetSize; sheetNum++)
			{
				// 获得当前sheet工作表
				Sheet sheet = workbook.getSheetAt(sheetNum);
				if (sheet == null)
				{
					continue;
				}
				// 获得当前sheet的开始行
				int firstRowNum = sheet.getFirstRowNum();
				// 获得当前sheet的结束行
				int lastRowNum = sheet.getLastRowNum();
				// 循环除了第一行的所有行
				for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++)
				{
					// 获得当前行
					Row row = sheet.getRow(rowNum);
					if (row == null)
					{
						continue;
					}
					// 获得当前行的开始列
					int firstCellNum = row.getFirstCellNum();
					// 获得当前行的列数
					int lastCellNum = row.getLastCellNum();
					String[] cells = new String[row.getLastCellNum()];
					// int lastCellNum = row.getPhysicalNumberOfCells();
					// String[] cells = new String[row.getPhysicalNumberOfCells()];
					// 循环当前行
					for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++)
					{
						Cell cell = row.getCell(cellNum);
						cells[cellNum] = getCellValue(cell);
					}
					list.add(cells);
				}
			}
			workbook.close();
		}
		return list;
	}

	/**
	 * 写EXCEL
	 * 
	 * @param os
	 * @param sheetName
	 * @param title
	 * @param headerNames
	 * @param dataColumn
	 * @param list
	 * @return
	 */
	public static OutputStream writeExcel(OutputStream os, String sheetName, String title, String headerNames,
			String dataColumn, List<?> list)
	{
		try
		{
			XSSFWorkbook wbook = new XSSFWorkbook();
			XSSFSheet wsheet = wbook.createSheet(sheetName);
			wsheet.setDefaultColumnWidth(15);
			XSSFCellStyle titleCellStyle = getTitleStyle(wbook);
			XSSFCellStyle headerCellStyle = getHeaderStyle(wbook);
			XSSFCellStyle contentCellStyle = getContentStyle(wbook);
			// 待写入的行号
			int writingRow = 0;
			// 列名
			String[] headerNameArray = null;
			// 表列名
			String[] dataColumnArray = null;
			if (headerNames != null && headerNames.length() > 0)
				headerNameArray = headerNames.split(",");
			if (dataColumn != null && dataColumn.length() > 0)
				dataColumnArray = dataColumn.split(",");
			// 写入标题
			if (title != null && !title.equals("") && title.length() > 0)
			{
				if (headerNameArray.length > 1)
				{
					wsheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headerNameArray.length - 1));
				}
				XSSFCell c = wsheet.createRow(writingRow).createCell(0);
				c.setCellStyle(titleCellStyle);
				c.setCellValue(new XSSFRichTextString(title));
				writingRow++;
			}
			// 写入列名
			if (headerNameArray.length > 0)
			{
				XSSFRow r1 = wsheet.createRow(writingRow);
				for (int i = 0; i < headerNameArray.length; i++)
				{
					r1.createCell(i).setCellValue(new XSSFRichTextString(headerNameArray[i]));
					r1.getCell(i).setCellStyle(headerCellStyle);
				}
				writingRow++;
			}
			// 写入每行每列数据
			for (int i = 0; i < list.size(); i++)
			{
				int columnIndex = 0;
				String v = "";
				XSSFRow rContent = wsheet.createRow(i + writingRow);
				// BaseModel 类型
				// if (list.get(i).getClass().getSuperclass().isAssignableFrom(BaseModel.class))
				if (BaseModel.class.getName().equals(list.get(i).getClass().getSuperclass().getName()))
				{
					for (int j = 0; j < dataColumnArray.length; j++)
					{
						v = ((BaseModel) list.get(i)).getString(dataColumnArray[j]);
						if (v != null)
							rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(v));
						else
							rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(""));
						rContent.getCell(j).setCellStyle(contentCellStyle);
						columnIndex++;
					}
				}
				// Map 类型
				// else if (list.get(i).getClass().getSuperclass().isAssignableFrom(Map.class))
				else if (Map.class.getName().equals(list.get(i).getClass().getSuperclass().getName()))
				// if
				// (Map.class.getName().equals(list.get(i).getClass().getSuperclass().getName()))
				{
					for (int j = 0; j < dataColumnArray.length; j++)
					{
						v = (String) ((Map<?, ?>) list.get(i)).get(dataColumnArray[j]);
						if (v != null)
							rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(v));
						else
							rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(""));
						rContent.getCell(j).setCellStyle(contentCellStyle);
						columnIndex++;
					}
				}
				else if (HashMap.class.getName().equals(list.get(i).getClass().getSuperclass().getName()))
				{
					for (int j = 0; j < dataColumnArray.length; j++)
					{
						v = String.valueOf(((HashMap<?, ?>) list.get(i)).get(dataColumnArray[j]));
						if (v != null)
							rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(v));
						else
							rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(""));
						rContent.getCell(j).setCellStyle(contentCellStyle);
						columnIndex++;
					}
				}
				// 其他 类型
				else
				{
					try
					{
						for (int j = 0; j < dataColumnArray.length; j++)
						{
							Method m = list.get(i).getClass()
									.getMethod("get" + dataColumnArray[j].substring(0, 1).toUpperCase()
											+ dataColumnArray[j].substring(1));
							v = String.valueOf(m.invoke(list.get(i)));
							if (v != null)
								rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(v));
							else
								rContent.createCell(columnIndex).setCellValue(new XSSFRichTextString(""));
							rContent.getCell(j).setCellStyle(contentCellStyle);
							columnIndex++;
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

			}
			wbook.write(os); // 写入文件
		}
		catch (IOException e)
		{
			try
			{
				os.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return os;
	}

	public static void checkFile(MultipartFile file) throws IOException
	{
		// 判断文件是否存在
		if (null == file)
		{
			log.error(MSG_FILE_NOT_FOUND);
			throw new FileNotFoundException(MSG_FILE_NOT_FOUND);
		}
		// 获得文件名
		String fileName = file.getOriginalFilename();
		// 判断文件是否是excel文件
		if (!fileName.endsWith(XLS) && !fileName.endsWith(XLSX))
		{
			log.error(MSG_FORMAT_ERR + "(" + fileName + ")");
			throw new IOException(MSG_FORMAT_ERR + "(" + fileName + ")");
		}
	}
	
	public static void checkFile(String filePath) throws IOException 
	{
		// 判断文件是否存在
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(filePath);
		}
		catch (Exception e)
		{
			log.error(MSG_FILE_NOT_FOUND);
			throw new FileNotFoundException(MSG_FILE_NOT_FOUND);
		}
		finally 
		{
			if (null != is)
			{
				is.close();
			}
		}
		// 判断文件是否是excel文件
		if (!filePath.toLowerCase().endsWith(XLS) && !filePath.toLowerCase().endsWith(XLSX))
		{
			log.error(MSG_FORMAT_ERR + "(" + filePath + ")");
			throw new IOException(MSG_FORMAT_ERR + "(" + filePath + ")");
		}
	}

	public static Workbook getWorkBook(MultipartFile file) throws IOException
	{
		// 获得文件名
		String fileName = file.getOriginalFilename();
		// 创建Workbook工作薄对象，表示整个excel
		Workbook workbook = null;
		try
		{
			// 获取excel文件的io流
			InputStream is = file.getInputStream();
			// 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
			if (fileName.endsWith(XLS))
			{
				// 2003
				workbook = new HSSFWorkbook(is);
			}
			else if (fileName.endsWith(XLSX))
			{
				// 2007
				workbook = new XSSFWorkbook(is);
			}
		}
		catch (IOException e)
		{
			log.error(e.getMessage());
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(workbook);
		}
		return workbook;
	}

	public static Workbook getWorkBook(String filePath) throws IOException
	{
		// 获得文件名
		FileInputStream is = null;
		// 创建Workbook工作薄对象，表示整个excel
		Workbook workbook = null;
		try
		{
			// 获取excel文件的io流
			is = new FileInputStream(filePath);
			// 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
			if (filePath.toLowerCase().endsWith(XLS))
			{
				// 2003
				workbook = new HSSFWorkbook(is);
			}
			else if (filePath.toLowerCase().endsWith(XLSX))
			{
				// 2007
				workbook = new XSSFWorkbook(is);
			}
		}
		catch (IOException e)
		{
			log.error(e.getMessage());
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(workbook);
		}
		return workbook;
	}

	public static String getCellValue(Cell cell)
	{
		String cellValue = "";
		if (cell == null)
		{
			return cellValue;
		}
		// 把数字当成String来读，避免出现1读成1.0的情况
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
		{
			// 判断是日期类型
			if (HSSFDateUtil.isCellDateFormatted(cell))
			{
				SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
				Date dt = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());// 获取成DATE类型
				cellValue = dateformat.format(dt);
				return cellValue;
			}
			cell.setCellType(Cell.CELL_TYPE_STRING);
		}
		// 判断数据的类型
		switch (cell.getCellType())
		{
			case Cell.CELL_TYPE_NUMERIC: // 数字
				cellValue = String.valueOf(cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_STRING: // 字符串
				cellValue = String.valueOf(cell.getStringCellValue());
				break;
			case Cell.CELL_TYPE_BOOLEAN: // Boolean
				cellValue = String.valueOf(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA: // 公式
				cellValue = String.valueOf(cell.getCellFormula());
				break;
			case Cell.CELL_TYPE_BLANK: // 空值
				cellValue = "";
				break;
			case Cell.CELL_TYPE_ERROR: // 故障
				cellValue = "非法字符";
				break;
			default:
				cellValue = "未知类型";
				break;
		}
		return cellValue;
	}

	// 设置标题样式
	private static XSSFCellStyle getTitleStyle(XSSFWorkbook wbook)
	{
		XSSFCellStyle titleCellStyle = wbook.createCellStyle();
		// 背景色
		titleCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
		titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// 居中
		titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
		titleCellStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		// 字体
		XSSFFont titleFont = wbook.createFont();
		titleFont.setFontName("仿宋_GB2312");
		titleFont.setFontHeightInPoints((short) titleFontSize);
		titleCellStyle.setFont(titleFont);
		return titleCellStyle;
	}

	// 设置表头样式
	private static XSSFCellStyle getHeaderStyle(XSSFWorkbook wbook)
	{
		XSSFCellStyle headerCellStyle = wbook.createCellStyle();
		// 背景色
		headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// 居中
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		// 字体
		XSSFFont font = wbook.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) headerFontSize);
		headerCellStyle.setFont(font);
		return headerCellStyle;
	}

	// 设置内容样式
	private static XSSFCellStyle getContentStyle(XSSFWorkbook wbook)
	{
		XSSFCellStyle contentCellStyle = wbook.createCellStyle();
		// 居中
		contentCellStyle.setAlignment(HorizontalAlignment.RIGHT);
		contentCellStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		// 字体
		XSSFFont font = wbook.createFont();
		font.setFontName("Arial");
		contentCellStyle.setFont(font);
		return contentCellStyle;
	}
}