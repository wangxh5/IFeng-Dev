package utils;
/**
 * <PRE>
 * 作用 : 
 *   http request的java工具类；
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2012-12-26        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRequest {
    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        BufferedReader in = null;
        StringBuffer sbRes = new StringBuffer();
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive"); 
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
//            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            
            if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				throw new Exception("get failed ErrorCode="+conn.getResponseCode());
			} 		
            
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
            		conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
            	sbRes.append(line).append("\r\n");
            }
        }catch (MalformedURLException e) {
			System.out.println("Get failed.The specified URL " + param
					+ " is not a valid URL. Please check");
			e.printStackTrace();
			System.out.println(url+param);
			sbRes.append("get failed,param:"+param);
		} catch (IOException e) {
			System.out.println("Get failed.An error occured. Please check that Solr is running.");
			e.printStackTrace();
			System.out.println(url+param);
			sbRes.append("get failed,param:"+param);
		}catch (Exception e) {
            e.printStackTrace();
            System.out.println(url+param);
            sbRes.append("get failed,param:"+param);
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return sbRes.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer sbRes = new StringBuffer();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
//    		conn.setDoInput(true);
//    		conn.setUseCaches(false);
//    		conn.setAllowUserInteraction(false);
    		conn.setRequestProperty("Content-type", "text/json"); 
//    		conn.setChunkedStreamingMode(1024*50);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            
            if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					throw new Exception("post failed ErrorCode="+conn.getResponseCode());
				}
				System.out.println("ErrorCode="+conn.getResponseCode());
			} 		
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
            	sbRes.append(line).append("\r\n");
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
            sbRes.append("post failed");
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        
        return sbRes.toString();
    }    
}