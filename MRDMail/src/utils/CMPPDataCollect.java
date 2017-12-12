package utils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * <PRE>
 * 作用 : 从页面下载数据并转换成item。解析来自cmpp的数据，输出itemlist
 *   
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
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class CMPPDataCollect {
	static Logger LOG = Logger.getLogger(CMPPDataCollect.class);
	public static final int INTERVAL = 30;
	public static final int INTERVAL2 = 120;
	public static final int DELAYTIME = 120;
//	private static int serverNum = Integer.valueOf(LoadConfig.lookUpValueByKey("serverNum"));; //分布式处理时,全部服务器的数目
//	private static int serverVol = Integer.valueOf(LoadConfig.lookUpValueByKey("serverVol"));; //分布式处理时,该服务器的分配号
	public static List<JsonFromCMPP> readJsonList(String Urlpath) {
		String str = downloadPage(Urlpath);
		if (str == null) {
			return null;
		} else {
			Gson gson = new Gson();
			ArrayList<JsonFromCMPP> jsonList = gson.fromJson(str, new TypeToken<List<JsonFromCMPP>>() {
			}.getType());
			return jsonList;
		}
	}
	public static JsonFromCMPP readJson(String Urlpath) {
		String str = downloadPage(Urlpath);
		if (str == null) {
			return null;
		} else {
			JsonFromCMPP json = JsonUtils.fromJson(str, JsonFromCMPP.class);
			return json;
		}
	}
	public static List<JSONObject> readJsonID(String Urlpath) {
		String str = downloadPage(Urlpath);
		if (str == null) {
			return null;
		} else 
		{
			JSONObject jo = JSONObject.fromObject(str);
			
			if(!jo.getBoolean("success"))
			{
				LOG.error("Get id from interface failed!");
				return null;
			}
			List<JSONObject> joList = (List<JSONObject>) jo.get("data");
			return joList;
		} 
	}
	
	public static JSONObject readJsonByUrl(String Urlpath) {
		String str = downloadPage(Urlpath);
		if (str == null) {
			return null;
		} else 
		{
			JSONObject jo = JSONObject.fromObject(str);
			
			if(!jo.getBoolean("success"))
			{
				LOG.error("Get id from interface failed!");
				return null;
			}
			JSONObject data =  (JSONObject) jo.get("data");
			return data;
		} 
	}
	public static String downloadPage(String url) {
		String content = null;
		int i = 0;
		while (i < 10) {
			try {
				content = getHtml(url);
				if (content != null) {
					return content;
				} else {
					i++;
					LOG.info(url + " 超时次数 : " + i+" wait 15 seconds.");
					Thread.sleep(15 * 1000);
				}
			} catch (Exception e) {
				i++;
				continue;
			}
		}
		return content;
	}

	public static String getHtml(String urlstr) {
		if (urlstr == null)
			return null;
		URL url;
		HttpURLConnection conn = null;
		InputStream in = null;
		BufferedReader br = null;
		String content = null;

		try {
			url = new URL(urlstr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3 * 2000);
			conn.setReadTimeout(3 * 2000);
			conn.addRequestProperty("User-Agent", "	Mozilla/5.0 (Windows NT 5.1; rv:7.0.1) Gecko/20100101 Firefox/7.0.1");
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Cache-Control", "max-age=0");// Cache-Control:

			in = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\r\n");
			}
			content = sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

	static public String dateReverse(long timestamp) {
		String time = new java.text.SimpleDateFormat("yyyy-MM-dd#HH:mm:ss").format(new java.util.Date(timestamp * 1000));
		time = time.replace("#", "T");
		time = time + "Z";
		return time;
	}
	static public String dateReversev2(long timestamp) {
		String time = new java.text.SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss").format(new java.util.Date(timestamp * 1000));
		return time;
	}
	static public void writerFile(String content, String path) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(path, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fw.write(content + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 从cmpp获取数据，转换成jsonItem，存入blockingQueue
	 * 
	 * @param fileQueue
	 */
	public static void getDataFromCMPPReverse(long eTime, BlockingQueue<JsonFromCMPP> fileQueue) {
		long sTime = eTime - INTERVAL;
		// int jsonCount = 0;
		while (true) {
			long cTime = 1427644800;
			String startDate = dateReverse(sTime);
			String endDate = dateReverse(sTime + INTERVAL);
			if (eTime > cTime) {
				String url1 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				String url2 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_346.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				List<JsonFromCMPP> jList = new ArrayList<JsonFromCMPP>();
				List<JsonFromCMPP> jList1 = null;
				List<JsonFromCMPP> jList2 = null;
				try {
					jList1 = CMPPDataCollect.readJsonList(url1);
					if (jList1 == null) {
						LOG.info("Can't get page data. Wating 5 sec for try again.");
						Thread.sleep(5 * 1000);
						continue;
					}
					else
						jList.addAll(jList1);

				} catch (Exception e) {
					LOG.error("GetJsonList error.", e);
				}
				try {
					jList2 = CMPPDataCollect.readJsonList(url2);
					if (jList2 == null) {
						LOG.info("Can't get page data. Wating 5 sec for try again.");
					}else
						jList.addAll(jList2);
				} catch (Exception e) {
					LOG.error("GetJsonList error.", e);
				}
				for (JsonFromCMPP j : jList) {
					try {
						fileQueue.put(j);
						LOG.info("Get id " + j.getId() + " ");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						LOG.error("In put cmpp item into queue.", e);
						// e.printStackTrace();
					}
				}
				eTime = sTime;
				sTime = eTime - INTERVAL;
				LOG.info("sTime is " + sTime + ",\t" + eTime);
			} else {
				LOG.info("Finish read old data, set fin.");
				break;
			}
		}
	}

	/**
	 * 从cmpp获取数据，转换成jsonItem，存入blockingQueue
	 * 
	 * @param fileQueue
	 */
	public static void getDataFromCMPP187x346(long sTime, BlockingQueue<JsonFromCMPP> fileQueue) {
//		LOG.info("serverNum is "+serverNum+" serverVol is "+serverVol);
		long eTime = sTime + INTERVAL2;
		// int jsonCount = 0;
		while (true) {
			long cTime = System.currentTimeMillis() / 1000;
			String startDate = dateReverse(sTime);
			String endDate = dateReverse(sTime + INTERVAL2);
			if (cTime > (eTime + INTERVAL2)) {
				String url1 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				String url2 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_346.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				List<JsonFromCMPP> jList = new ArrayList<JsonFromCMPP>();
				List<JsonFromCMPP> jList1 = null;
				List<JsonFromCMPP> jList2 = null;
				try {
					jList1 = CMPPDataCollect.readJsonList(url1);
					if (jList1 == null) {
						LOG.info("Can't get page data. Wating 5 sec for try again.");
						Thread.sleep(5 * 1000);
						continue;
					}

				} catch (Exception e) {
					LOG.error("GetJsonList error.", e);
				}
				try {
					jList2 = CMPPDataCollect.readJsonList(url2);
					if (jList2 == null) {
						LOG.info("Can't get page data. Wating 5 sec for try again.");
						Thread.sleep(5 * 1000);
						continue;
					}

				} catch (Exception e) {
					LOG.error("GetJsonList error.", e);
				}
				jList.addAll(jList1);
				jList.addAll(jList2);
				for (JsonFromCMPP j : jList) {
					try {
//						if(Integer.valueOf(j.getId()) % serverNum == serverVol)
						{
							if(!j.getOther().contains("source=todaytoutiao"))
								fileQueue.put(j);
							LOG.info("Get id " + j.getId() + " ");
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						LOG.error("In put cmpp item into queue.", e);
						// e.printStackTrace();
					}

				}
				sTime = eTime;
				eTime = sTime + INTERVAL2;
				LOG.info("sTime is " + sTime + ",\t" + eTime);
			} else {
				try {
					LOG.info("[getDataFromCMPP] Get data thread Sleep 15 seconds.");
					Thread.sleep(15 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					LOG.error("Some error occurred when thread sleep.", e);
					// e.printStackTrace();
				}
			}

		}
	}
	/**
	 * 从cmpp获取数据，转换成jsonItem，存入blockingQueue
	 * 用于分布式 数据倒序写入（数据重做）
	 * @param fileQueue
	 */
	public static void getDataForDistribute(BlockingQueue<JsonFromCMPP> fileQueue) {
		long stopTime = System.currentTimeMillis() / 1000;
		long sTime = stopTime - 60 * 60 * 24 * 30;
		long eTime = sTime + INTERVAL;
		// int jsonCount = 0;
		while (sTime <= stopTime) {
			long cTime = System.currentTimeMillis() / 1000;
			String startDate = dateReverse(sTime);
			String endDate = dateReverse(sTime + INTERVAL);
			if (cTime > (eTime + INTERVAL)) {
				String url1 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				String url2 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_346.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				List<JsonFromCMPP> jList = new ArrayList<JsonFromCMPP>();
				List<JsonFromCMPP> jList1 = null;
				List<JsonFromCMPP> jList2 = null;
				try {
					jList1 = CMPPDataCollect.readJsonList(url1);
					if (jList1 == null) {
						LOG.info("Can't get page data. Wating 5 sec for try again.");
						Thread.sleep(5 * 1000);
						continue;
					}
				} catch (Exception e) {
					LOG.error("GetJsonList error.", e);
				}
				try {
					jList2 = CMPPDataCollect.readJsonList(url2);
					if (jList2 == null) {
						LOG.info("Can't get page data. Wating 5 sec for try again.");
						Thread.sleep(5 * 1000);
						continue;
					}

				} catch (Exception e) {
					LOG.error("GetJsonList error.", e);
				}
				jList.addAll(jList1);
				jList.addAll(jList2);
				for (JsonFromCMPP j : jList) {
					try {
	//					if(Integer.valueOf(j.getId()) % serverNum == serverVol)
						{
							fileQueue.put(j);
							LOG.info("Get id " + j.getId() + " ");
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						LOG.error("In put cmpp item into queue.", e);
						// e.printStackTrace();
					}
				}
				sTime = eTime;
				eTime = sTime + INTERVAL;
				LOG.info("sTime is " + sTime + ",\t" + eTime);
			} else {
				try {
					LOG.info("[getDataFromCMPP] Get data thread Sleep 15 seconds.");
					Thread.sleep(15 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					LOG.error("Some error occurred when thread sleep.", e);
					// e.printStackTrace();
				}
			}

		}
	}
	/**
	 * 从cmpp获取数据，转换成jsonItem，存入blockingQueue
	 * 为新的流程准备的取数据方案。（用于内容模型等算法）
	 * 从泛编系统获取文章id，从cmpp接口根据id查询获得json
	 * 对下线内容进行控制，不再进行解析
	 * 泛编状态位：
	 * @param fileQueue
	 */
//	public static void getDataForRedis(long sTime, BlockingQueue<JsonFromCMPP> fileQueue) {
//
//		long eTime = sTime + INTERVAL;
//		long getJsonTime = System.currentTimeMillis();
//		int dayJsonCount = 0;
//		long jsonCountTime = System.currentTimeMillis();
////		boolean ifGetJson = true;
//		long changeTime = System.currentTimeMillis();
//		while (true) {
//			long cTime = System.currentTimeMillis() / 1000;
//			String startDate = dateReversev2(sTime);
//			String endDate = dateReversev2(sTime + INTERVAL);
//			if((System.currentTimeMillis() - jsonCountTime) >= 24 * 60 * 60 * 1000)
//			{
//				jsonCountTime = System.currentTimeMillis();
//				String res = SendMessage.send("17865153777", "[195:getDataForRedis]We have received "+dayJsonCount+"jsons for last day.", null, null, null);
//				dayJsonCount = 0;
//				LOG.info(res);
//			}
//			if (cTime > (eTime + INTERVAL + DELAYTIME)) {
//				String url1 = "http://nyx.staff.ifeng.com/project/api/recommendMgr/getOperationStatus?startDate=" + startDate + "&endDate=" + endDate;
//				String url2 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/dataProvider!getDataById.jhtml?nodeId=16001&formId=600&dataId=";
//				List<JSONObject> joList = null;
//				try{
//					joList = readJsonID(url1);
//					if(joList == null || joList.size() == 0)
//					{
//						if((System.currentTimeMillis() - getJsonTime) > 15 * 60 * 1000)
//						{
//							String res = SendMessage.send("17865153777", "[195:getDataForRedis]We have not received json for 15 minutes.", null, null, null);
//							LOG.info(res);
//							getJsonTime = System.currentTimeMillis();
//						}
//						if((System.currentTimeMillis() - changeTime) > 120 * 60 * 1000)
//						{
//							String res = SendMessage.send("17865153777", "[195:getDataForRedis]We have not received json for 120 minutes.", null, null, null);
//							LOG.info(res);
////							getDataFromCMPP187(sTime,fileQueue);
//						}
//					}
//					else
//					{
////						ifGetJson = true;
//						getJsonTime = System.currentTimeMillis();
//						changeTime = System.currentTimeMillis();
//						dayJsonCount+=joList.size();
//					}
//					}catch(Exception e)
//				{
//					LOG.error("Read json id failed.",e);
//					e.printStackTrace();
//				}
////				String res = SendMessage.send("17865153777", "Test of send message.", null, null, null);
//				if(joList != null)
//				for(JSONObject jo : joList)
//				{
//					String id = jo.getString("id");
//					String jsonurl = "";
//					String editorOther = "state="+jo.getString("state")+"|!|sameId="+jo.getString("sameId")+"|!|channels="+jo.getString("channels");
//					if(id != null && !id.isEmpty())
//						jsonurl = url2 + id;
//					JsonFromCMPP json = new JsonFromCMPP();
//					try {
//						json = CMPPDataCollect.readJson(jsonurl);
//						if (json == null) {
//							LOG.info("Get error page data. Json is null. Get the next one.");
//							continue;
//						}
//						json.setEditorOther(editorOther);
////						String other = json.getOther();
////						if(other == null || other.isEmpty())
////							other = jother;
////						else
////							other = other + "|!|" + jother;
////						json.setOther(other);
//						if(json.getEditorOther().contains("clusterId"))
//							fileQueue.put(json);
//						LOG.info("Get id " + json.getId() + " ");
////						FileWriter fw = null;
////						fw =  new FileWriter("D:\\data\\getidtest",true);
////						fw.write(startDate+" "+endDate+" Get id " + json.getId() + "\n");
////						fw.flush();
////						fw.close();
//					} catch (Exception e) {
//						LOG.error("In put cmpp item into queue.", e);
//						e.printStackTrace();
//					}
//				}
//				sTime = eTime;
//				eTime = sTime + INTERVAL;
//				LOG.info("sTime is " + sTime + ",\t" + eTime);
//			} else {
//				try {
//					LOG.info("[getDataFromCMPP] Get data thread Sleep 15 seconds.");
//					Thread.sleep(15 * 1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					LOG.error("Some error occurred when thread sleep.", e);
//					// e.printStackTrace();
//				}
//			}
//
//		}
//	}
	public static void main(String[] args) {
		long sTime = 1464710400;
		long eTime = sTime + 10 * 60;
		int jsonCount = 0;
		while (true) {

			long cTime = System.currentTimeMillis() / 1000;
			// System.out.println("cTime is "+cTime);
			// System.out.println("cTime is "+eTime);
			String startDate = dateReverse(sTime);
			// System.out.println(startDate);
			String endDate = dateReverse(sTime + 10 * 60);
			// System.out.println(endDate);
			if (cTime > (eTime + 10 * 60)) {
				String url = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				String content = CMPPDataCollect.downloadPage(url);
				List<JsonFromCMPP> jList = CMPPDataCollect.readJsonList(url);
				for (JsonFromCMPP j : jList) {
					jsonCount++;
					System.out.println("Json Count is " + jsonCount);
					// System.out.println(j.getTitle());
					try {
						// String channel =
						// j.getOther().split("\\|!\\|")[1].split("=")[1];
						// System.out.println(j);
						String tags = null;
						String[] tempSplit = j.getOther().split("\\|!\\|");

						for (int i = 0; i < tempSplit.length; i++) {
							// System.out.println("tempSplit is "+tempSplit[i]);
							if (tempSplit[i].contains("tags=")) {
								tags = tempSplit[i].replace("tags=", "").trim();
								break;
							}
						}

						writerFile(j.getOther() + "\t" + j.getSourceLink() + "\t" + j.getTitle() + "\t" + j.getType() + "\t" + j.getId(), "D:\\data\\newchanneltags.txt");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// writerFile(content,"D:\\data\\channeltags.txt");
				sTime = eTime;
				eTime = sTime + 10 * 60;
				// writerFile(url,"D:\\data\\getfromCMPP.txt");
				// writerFile(content,"D:\\data\\getfromCMPP.txt");
			} else {
				try {
					System.out.println("Sleep");
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
