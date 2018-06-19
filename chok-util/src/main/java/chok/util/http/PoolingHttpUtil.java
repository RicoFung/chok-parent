package chok.util.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolingHttpUtil
{
	static Logger log = LoggerFactory.getLogger(PoolingHttpUtil.class);

	private static CloseableHttpClient httpClient = null;

	private final static Object syncLock = new Object();

	// 最大连接数
	private static int maxTotal = 30;
	// 每个路由基础的连接
	private static int maxPerRoute = 40;
	// 目标主机的最大连接数
	private static int maxRoute =100;
	// 超时
	private static int timeOut = 60 * 1000;

	private static void config(HttpRequestBase httpRequestBase)
	{
		// 设置Header等
		// httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
		// httpRequestBase
		// .setHeader("Accept",
		// "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		// httpRequestBase.setHeader("Accept-Language",
		// "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
		// httpRequestBase.setHeader("Accept-Charset",
		// "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");

		// 配置请求的超时设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeOut).setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
		httpRequestBase.setConfig(requestConfig);
	}

	/**
	 * 自定义配置
	 * @param maxTotal 最大连接数，默认30
	 * @param maxPerRoute 每个路由基础的连接，默认40
	 * @param maxRoute 目标主机的最大连接数，默认100
	 * @param timeOut 超时，默认60*1000，包括ConnectionRequestTimeout，ConnectTimeout，SocketTimeout
	 * @return
	 */
	public static void customConfig(int maxTotal, int maxPerRoute, int maxRoute, int timeOut)
	{
		PoolingHttpUtil.maxTotal = maxTotal;
		PoolingHttpUtil.maxPerRoute = maxPerRoute;
		PoolingHttpUtil.maxRoute = maxRoute;
		PoolingHttpUtil.timeOut = timeOut;
	}

	/**
	 * 获取HttpClient对象
	 * @param url
	 * @param cookieStore
	 * @return
	 */
	public static CloseableHttpClient getHttpClient(String url, BasicCookieStore cookieStore)
	{
		String hostname = url.split("/")[2];
		int port = 80;
		if (hostname.contains(":"))
		{
			String[] arr = hostname.split(":");
			hostname = arr[0];
			port = Integer.parseInt(arr[1]);
		}
		if (httpClient == null)
		{
			synchronized (syncLock)
			{
				if (httpClient == null)
				{
					httpClient = createHttpClient(maxTotal, maxPerRoute, maxRoute, hostname, port, cookieStore);
				}
			}
		}
		if(log.isInfoEnabled()) 
		{
			log.info("[maxTotal]: " + PoolingHttpUtil.maxTotal);
			log.info("[maxPerRoute]: " + PoolingHttpUtil.maxPerRoute);
			log.info("[maxRoute]: " + PoolingHttpUtil.maxRoute);
			log.info("[timeOut]: " + PoolingHttpUtil.timeOut);
		}
		return httpClient;
	}

	/**
	 * 创建HttpClient对象
	 * @param maxTotal 最大连接数
	 * @param maxPerRoute 每个路由基础的连接
	 * @param maxRoute 目标主机的最大连接数
	 * @param hostname
	 * @param port
	 * @return
	 */
	public static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute, int maxRoute, String hostname, int port, BasicCookieStore cookieStore)
	{
		
		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainsf).register("https", sslsf).build();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		// 将最大连接数增加
		cm.setMaxTotal(maxTotal);
		// 将每个路由基础的连接增加
		cm.setDefaultMaxPerRoute(maxPerRoute);
		HttpHost httpHost = new HttpHost(hostname, port);
		// 将目标主机的最大连接数增加
		cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

		// 请求重试处理
		HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler()
		{
			@Override
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
			{
				if (executionCount >= 5)
				{// 如果已经重试了5次，就放弃
					return false;
				}
				if (exception instanceof NoHttpResponseException)
				{// 如果服务器丢掉了连接，那么就重试
					return true;
				}
				if (exception instanceof SSLHandshakeException)
				{// 不要重试SSL握手异常
					return false;
				}
				if (exception instanceof InterruptedIOException)
				{// 超时
					return false;
				}
				if (exception instanceof UnknownHostException)
				{// 目标服务器不可达
					return false;
				}
				if (exception instanceof ConnectTimeoutException)
				{// 连接被拒绝
					return false;
				}
				if (exception instanceof SSLException)
				{// SSL握手异常
					return false;
				}

				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpRequest request = clientContext.getRequest();
				// 如果请求是幂等的，就再次尝试
				if (!(request instanceof HttpEntityEnclosingRequest))
				{
					return true;
				}
				return false;
			}
		};
		// 是否设置cookie
		if (cookieStore != null) 
		{
			return HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).setDefaultCookieStore(cookieStore).build();
		}
		else
		{
			return HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).build();
		}
	}

	private static void setPostParams(HttpPost httpost, Map<String, Object> params)
	{
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		Set<String> keySet = params.keySet();
		for (String key : keySet)
		{
			nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
		}
		try
		{
			httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void setPostJsonParams(HttpPost httpost, String jsonParams)
	{
	    httpost.setHeader("Accept", "application/json");
	    httpost.setHeader("Content-type", "application/json");
		try
		{
			StringEntity entity = new StringEntity(jsonParams);
			httpost.setEntity(entity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * POST请求URL获取内容
	 * @param url
	 * @param jsonParams
	 * @return
	 * @throws IOException
	 */
	public static String post(String url, String jsonParams) throws IOException
	{
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		setPostJsonParams(httppost, jsonParams);
		CloseableHttpResponse response = null;
		try
		{
			response = getHttpClient(url, null).execute(httppost, HttpClientContext.create());
			HttpEntity entity = (HttpEntity) response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (response != null)
					response.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static CloseableHttpResponse post(String url, String jsonParams, BasicCookieStore cookieStore) throws IOException
	{
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		setPostJsonParams(httppost, jsonParams);
		CloseableHttpResponse response = null;
		try
		{
			response = getHttpClient(url, cookieStore).execute(httppost, HttpClientContext.create());
			return response;
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
//			由调用方关闭response
//			try
//			{
//				if (response != null)
//					response.close();
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
		}
	}
	
	public static String post(String url, Map<String, Object> params) throws IOException
	{
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		setPostParams(httppost, params);
		CloseableHttpResponse response = null;
		try
		{
			response = getHttpClient(url, null).execute(httppost, HttpClientContext.create());
			HttpEntity entity = (HttpEntity) response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw e;
		}
		finally
		{
			try
			{
				if (response != null)
					response.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * GET请求URL获取内容
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url)
	{
		HttpGet httpget = new HttpGet(url);
		config(httpget);
		CloseableHttpResponse response = null;
		try
		{
			response = getHttpClient(url, null).execute(httpget, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (response != null)
					response.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
}
