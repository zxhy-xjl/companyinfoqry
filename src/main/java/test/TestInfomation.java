package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.util.List;
import util.MyX509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.sf.json.JSONObject;
import sun.net.www.protocol.http.HttpURLConnection;

public class TestInfomation {
	private static Logger log = LoggerFactory.getLogger(TestInfomation.class);
	static String testurl = "http://www.jsgsj.gov.cn:58888/province/infoQueryServlet.json";
	static String infourl = "http://www.jsgsj.gov.cn:58888/province/infoQueryServlet.json";
	//static String requestMethod = "GET";
	static String outputStr = null;
	static String jsessionID = "";
	static int flag =0;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONObject jsonObject =httpRequest(testurl, "POST", "checkCode=false&name=%E4%B8%AD%E5%85%B4%E8%BD%AF%E5%88%9B");
		String name = jsonObject.getJSONObject("bean").get("name").toString();	
		System.out.println(jsonObject.getJSONObject("bean").get("name"));
		String tmpinfourl = infourl.replace("NAME", name);
		System.out.println(tmpinfourl);
		JSONObject jsonObject2 =httpRequest(tmpinfourl, "POST", "queryCinfo=true&name="+name+"&searchType=qyxx&pageNo=1&pageSize=10");
		System.out.println(jsonObject2);	
	}
	
	
	/**
	 * @author linhd
	 * @method getSessionID
	 * @description 执行从cookie获取会话sessionID的方法，用于保持与服务器的会话
	 * @param actionURL 远程服务器的URL
	 * */
public static String getSessionID(String actionURL){
	String sessionID;
	String sessionIDtmp;
	try {
		URL url = new URL(actionURL);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		String cookieValue = connection.getHeaderField("set-cookie");
		String cookieValue1 = connection.getHeaderFields().toString();
		System.out.println(cookieValue1);
		if(cookieValue != null){
			sessionIDtmp = cookieValue1.substring( cookieValue1.indexOf("J"), cookieValue1.lastIndexOf(";"));
			sessionID = sessionIDtmp.substring(sessionIDtmp.indexOf("J"),sessionIDtmp.lastIndexOf(";") );
		}else{
			sessionID = "";
		}
	} catch (IOException e) {
		e.printStackTrace();
		sessionID = "";
	}
	return sessionID;
}
	
	@SuppressWarnings("null")
	public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		flag++;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			CookieManager manager = new CookieManager();
			CookieHandler.setDefault(manager);
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			//SSLSocketFactory ssf = sslContext.getSocketFactory();
			if(jsessionID.equals(""))
			{jsessionID = getSessionID("http://www.jsgsj.gov.cn:58888/province");	
			System.out.println(jsessionID);
			}		
			URL url = new URL(requestUrl);		
			HttpURLConnection	httpUrlConn = (HttpURLConnection) url.openConnection();
		  /*  CookieStore cookieJar = manager.getCookieStore();
		    List<HttpCookie> cookies = cookieJar.getCookies();*/
		  /*  for (HttpCookie cookie : cookies) {
		      System.out.println(cookie);
		    }*/
			httpUrlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpUrlConn.setRequestProperty("Connection", "Keep-Alive");// 
			if(flag==1){
				httpUrlConn.addRequestProperty("Cookie", jsessionID);
				}else
				{
					httpUrlConn.addRequestProperty("Cookie", jsessionID);
				}
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
//			httpUrlConn.disconnect();
			jsonObject = JSONObject.fromObject(buffer.toString());
		} catch (ConnectException ce) {
			log.error("Weixin server connection timed out.");
		} catch (Exception e) {
			log.error("https request error:{}", e);
		}
		return jsonObject;
	}
	
	
}
