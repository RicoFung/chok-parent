package chok.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FtpUtil
{
	/**
	 * Description: 向FTP服务器上传文件
	 * 
	 * @Version 1.0
	 * @param url
	 *            FTP服务器hostname
	 * @param port
	 *            FTP服务器端口
	 * @param username
	 *            FTP登录账号
	 * @param password
	 *            FTP登录密码
	 * @param path
	 *            FTP服务器保存目录
	 * @param filename
	 *            上传到FTP服务器上的文件名
	 * @param input
	 *            输入流
	 * @return 成功返回true，否则返回false *
	 */
	public static boolean uploadFile(
			String url, // FTP服务器hostname
			int port, // FTP服务器端口
			String username, // FTP登录账号
			String password, // FTP登录密码
			String path, // FTP服务器保存目录
			String filename, // 上传到FTP服务器上的文件名
			InputStream input // 输入流
	)
	{
		boolean success = false;
		FTPClient ftp = new FTPClient();
		ftp.setControlEncoding("UTF-8");
		ftp.enterLocalPassiveMode();
		try
		{
			int reply;
			ftp.connect(url, port);// 连接FTP服务器
			// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
			ftp.login(username, password);// 登录
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				ftp.disconnect();
				return success;
			}
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

			String pathname = new String(path.getBytes("UTF-8"), "UTF-8");
			ftp.makeDirectory(pathname);

			ftp.changeWorkingDirectory(path);

			String remote = new String(filename.getBytes("UTF-8"), "UTF-8");
			ftp.storeFile(remote, input);

			input.close();
			ftp.logout();
			success = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (ftp.isConnected())
			{
				try
				{
					ftp.disconnect();
				}
				catch (IOException ioe)
				{
				}
			}
		}
		return success;
	}

	/**
	 * 将本地文件上传到FTP服务器上 *
	 */
	public static void upLoad(
			String url, // FTP服务器hostname
			int port, // FTP服务器端口
			String username, // FTP登录账号
			String password, // FTP登录密码
			String path, // FTP服务器保存目录
			String filename, // 上传到FTP服务器上的文件名
			String orginfilename // 输入流文件名
	)
	{
		try
		{
			FileInputStream in = new FileInputStream(new File(orginfilename));
			boolean flag = uploadFile(url, port, username, password, path, filename, in);
			System.out.println("ftp flag --> " + flag);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// 测试
	// public static void main(String[] args) throws UnsupportedEncodingException {
	// System.out.println(new String("中文ABC".getBytes("iso-8859-1"),"iso-8859-1"));
	// System.out.println(new String("中文ABC".getBytes("GBK"),"UTF-8"));
	// System.out.println(new String("中文ABC".getBytes("GBK"),"iso-8859-1"));
	// System.out.println(new String("中文ABC".getBytes("GBK"),"GBK"));
	// System.out.println(new String("中文ABC".getBytes("UTF-8"),"UTF-8"));
	// //upLoad("192.168.9.82", 21, "ipad", "8042501", "test", "207_42839.jpg",
	// "/Users/mac373/dev/apache-tomcat-7.0.65/webapps/epoErp/upload/digitalSign/202_41325.jpg");
	// }
}
