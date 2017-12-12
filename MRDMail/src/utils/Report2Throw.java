package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import getExcellentNum.CheckArticleFrom;
import getExcellentNum.ReportThrowUtil;
import redis.clients.jedis.Jedis;

/**
 * 统计抓站丢弃以及文章质量丢弃数
 * 
 * @author zhangyang6
 *
 */
public class Report2Throw {
	static Logger LOG = Logger.getLogger(Report2Throw.class);

	// 来源数组
	// private static String[] sourceArry =
	// {"机器抓取","自媒体数据","编辑数据","本地数据","视频数据","凤凰自媒体数据","一点自媒体数据"};

	/**
	 * 获取抓站丢弃和质量评级 type: 抓站丢弃 or 质量评级
	 */
	public static Map<String, Integer> getThrowNum(String type, String dateTime) {
		Map<String, Integer> reMap = new HashMap<String, Integer>();
		SimpleDateFormat fmtDay = null;
		Calendar cal = null;
		try {
			// fmtDay = new SimpleDateFormat("yyyyMMdd");
			// cal = Calendar.getInstance();
			// cal.add(Calendar.DATE, -1); //取前一天
			// String dateTime = fmtDay.format(cal.getTime());
			reMap = getInfosFromRedis(dateTime, type);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fmtDay = null;
			cal = null;
		}
		return reMap;
	}

	/**
	 * 获取所有统计信息 return map<String,Integer>
	 */
	private static Map<String, Integer> getInfosFromRedis(String dayTime, String type) {
		Jedis reportClient = null;
		Map<String, Integer> reMap = new HashMap<String, Integer>();
		try {
			reportClient = getJedisDbClient("10.32.24.86", 6379, 2);
			Set<String> throwKeys = reportClient.keys(dayTime + "_" + type + "*");
			for (String key : throwKeys) {
				try {
					String source = key.substring(key.lastIndexOf("_") + 1).trim();
					int valueNums = reportClient.smembers(key).size();
					if (reMap.containsKey(source)) {
						reMap.put(source, reMap.get(source) + valueNums);
					} else {
						reMap.put(source, valueNums);
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reportClient != null) {
				reportClient.disconnect();
				reportClient = null;
			}
		}
		return reMap;
	}

	/**
	 * 获取一个选定db的jedis对象
	 * 
	 * @param host
	 * @param port
	 * @param dbNum
	 * @return
	 */
	private static Jedis getJedisDbClient(String host, int port, int dbNum) {
		Jedis jedis = null;
		try {
			jedis = new Jedis(host, port, 3000);
			jedis.select(dbNum);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return jedis;
	}

	public static int getAllThrowNum(String dateTime) {
		Map<String, Integer> reMap1 = getThrowNum("质量评级", dateTime);
		if (reMap1 == null)
			LOG.info("[INFO] 质量评级 reMap1 size is null.");
		else
			LOG.info("[INFO] 质量评级 reMap1 size is " + reMap1.size());
		Map<String, Integer> reMap2 = getThrowNum("抓站丢弃", dateTime);
		if (reMap2 == null)
			LOG.info("[INFO] 抓站丢弃 reMap2 size is null.");
		else
			LOG.info("[INFO] 抓站丢弃 reMap2 size is " + reMap2.size());
		int result = 0;
		for (Entry<String, Integer> entry : reMap1.entrySet()) {
			if (!entry.getKey().equals("一点自媒体"))
				result = result + entry.getValue();
		}
		for (Entry<String, Integer> entry : reMap2.entrySet()) {
			result = result + entry.getValue();
		}
		return result;
	}

	public static int getAddBothNum(String key, String dateTime) {
		Map<String, Integer> reMap1 = getThrowNum("质量评级", dateTime);
		if (reMap1 == null)
			LOG.info("[INFO] 质量评级 reMap1 size is null.");
		else
			LOG.info("[INFO] 质量评级 reMap1 size is " + reMap1.size());
		Map<String, Integer> reMap2 = getThrowNum("抓站丢弃", dateTime);
		if (reMap2 == null)
			LOG.info("[INFO] 抓站丢弃 reMap2 size is null.");
		else
			LOG.info("[INFO] 抓站丢弃 reMap2 size is " + reMap2.size());
		int result = 0;
		int result1 = 0;
		int result2 = 0;
		try {
			if (!key.equals("一点自媒体"))
				result1 = reMap1.get(key);
		} catch (Exception e) {
			LOG.error("[ERROR] redis map get error.");
			result1 = 0;
		}
		try {
			result2 = reMap2.get(key);
		} catch (Exception e) {
			LOG.error("[ERROR] redis map get error.");
			result2 = 0;
		}
		result = result1 + result2;
		return result;
	}

	public static int getAllExcellentNum(String dateTime) {
		Map<String, Integer> reMap = getThrowNum("优质数据", dateTime);
		if (reMap == null)
			LOG.info("[INFO] 优质数据 reMap size is null.");
		else
			LOG.info("[INFO] 优质数据 reMap size is " + reMap.size());
		int result = 0;
		for (Entry<String, Integer> entry : reMap.entrySet()) {
			result = result + entry.getValue();
		}
		return result;
	}

	public static HashMap<String, Integer> getExcellentNum(String dateTime) {
		HashMap<String, Integer> excellentDataBySource = new HashMap<String, Integer>();
		Jedis jedis = null;
		jedis = ReportThrowUtil.getReportJedisClient();
		Set<String> docId = jedis.smembers(dateTime + "-优质数据");
		IKVOperationv3 ikv = new IKVOperationv3("appitemdb");
		for (String id : docId) {
			itemf item = ikv.queryItemF("cmpp_" + id);
			String otherStr = item.getOther();
			String source = CheckArticleFrom.checkFromByOther(otherStr);
			if (excellentDataBySource.containsKey(source)) {
				excellentDataBySource.put(source, excellentDataBySource.get(source) + 1);
			} else {
				excellentDataBySource.put(source, 1);
			}
		}
		jedis.close();
		ikv.close();
		System.out.println("ikv is closed");
		return excellentDataBySource;

	}

	public static void main(String[] args) {
		// Map<String,Integer> reMap = getThrowNum("质量评级","20161130");
		// Map<String,Integer> reMap = getThrowNum("抓站丢弃","20161130");
		// int reMap = getAddBothNum("一点自媒体", "20170406");
		// System.out.println(reMap);
		// System.out.println(reMap.size());
		Report2Throw.getExcellentNum("20170824");
		System.out.println(Report2Throw.getExcellentNum("20170824").toString());
	}
}
