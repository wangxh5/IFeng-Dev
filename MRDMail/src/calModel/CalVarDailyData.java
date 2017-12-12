package calModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import Hbase.HbaseOperation;
import dataBase.itemf;
import getExcellentNum.RedisUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import sendMail.SendDailyMail;
import sendMail.SendLocationMail;
import sendMail.SendMail;
import sendMail.SendSourceMail;
import sendMail.SendVideoMail;
import utils.CMPPDataCollect;
import utils.FeatureExTools;
import utils.JsonFromCMPP;
import utils.LoadConfig;
import utils.MapSort;
import utils.Report2Throw;

public class CalVarDailyData {
	private static final Logger LOG = Logger.getLogger(CalVarDailyData.class);
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath"); // 读取路径，配置文件conf/sys.properties
	private String sourcePath = LoadConfig.lookUpValueByKey("SourceMapPath"); // SourceMapPath=E:/Test/source.txt
	private String topLocationPath = LoadConfig.lookUpValueByKey("topLocationPath"); // 一级地域
	HbaseOperation hbaseOperation = new HbaseOperation();
	HashSet<String> originalId = new HashSet<String>();// 推荐表id
	HashSet<String> algorithmId = new HashSet<String>();// 算法表id
	HashSet<String> editId = new HashSet<String>();// 泛编出口id
	HashMap<String, String> source1Map = new HashMap<String, String>();// 来源映射
	List<String> offLineId = new ArrayList<String>();// 下线id

	HashMap<String, HashMap<String, Integer>> topLocationMap = new HashMap<String, HashMap<String, Integer>>();// 一级地域
	HashMap<String, HashMap<String, Integer>> allLocationMap = new HashMap<String, HashMap<String, Integer>>();// 所有地域
	HashMap<String, Location> locationMap = new HashMap<String, Location>();// 省市对应

	HashMap<String, HashMap<String, Integer>> docTypeMap = new HashMap<String, HashMap<String, Integer>>(); // 文章类型
	HashMap<String, HashMap<String, Integer>> sourceMap = new HashMap<String, HashMap<String, Integer>>();// 稿源
	HashMap<String, HashMap<String, Integer>> titlePartyMap = new HashMap<String, HashMap<String, Integer>>();// 标题党
	HashMap<String, HashMap<String, Integer>> pornMap = new HashMap<String, HashMap<String, Integer>>();// 低俗
	HashMap<String, HashMap<String, Integer>> isCreationMap = new HashMap<String, HashMap<String, Integer>>();// 原创
	HashMap<String, HashMap<String, Integer>> timeSensitiveMap = new HashMap<String, HashMap<String, Integer>>();// 时效性
	HashMap<String, HashMap<String, Integer>> qualityEvalLevelMap = new HashMap<String, HashMap<String, Integer>>();// 质量评级得分
	HashMap<String, Data> dataMap = new HashMap<String, Data>();// 来源-category对应
	HashMap<String, Data> mappingDataMap = new HashMap<String, Data>();// 来源-category对应
	HashMap<String, HashMap<String, Integer>> sourceMap2 = new HashMap<String, HashMap<String, Integer>>();// 来源
	HashMap<String, HashMap<String, Integer>> categoryMap = new HashMap<String, HashMap<String, Integer>>();// 分类
	HashMap<String, Data> videoSourceMap = new HashMap<String, Data>();// 视频稿源
	HashMap<String, HashMap<String, Integer>> videoTopSourceMap = new HashMap<String, HashMap<String, Integer>>();// 视频来源
	HashMap<String, Integer> latentTopicMap = new HashMap<String, Integer>();// 专题
	HashMap<String, Long> sourceRateMap = new HashMap<String, Long>();// 文章生产量的稿源评级
	HashMap<String, Long> qEvalLevelMap = new HashMap<String, Long>();// 文章生产量的质量评级过滤
	HashMap<String, Long> tSensitiveMap = new HashMap<String, Long>();// 文章生产量的时效性
	// 地域统计：质量评级过滤id总数，排重，泛编出口，推荐更新id总数
	int odsum = 0;
	int resum = 0;
	int edsum = 0;
	int lasum = 0;
	List<String> topLocationList = new ArrayList<String>();

	public CalVarDailyData() throws IOException {
		loadSourceMap(sourcePath);
		loadTocLocation(topLocationPath);
	}

	/**
	 * 来源映射 yidianzixun=机器抓取
	 * 
	 * @throws IOException
	 */
	public void loadSourceMap(String path) throws IOException {
		FileReader fr = null;
		fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String source = "";
		while ((source = br.readLine()) != null) {
			if (source.contains("=")) {
				source1Map.put(source.split("=")[0], source.split("=")[1]);
			}
		}
		br.close();
		fr.close();
	}

	public void loadTocLocation(String path) throws IOException {
		FileReader fr = null;
		fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		topLocationList = Arrays.asList(line.split(", "));
		br.close();
		fr.close();
	}

	class Data {
		String dataSource;
		// category 原始抓取数量odnum 排重之后的数量renum 泛编通过数量ednum 推荐内容体系数量lanum
		private HashMap<String, HashMap<String, Integer>> catesMap;

		public Data(String dataSource) {
			this.dataSource = dataSource;
			catesMap = new HashMap<String, HashMap<String, Integer>>();
		}

		public HashMap<String, HashMap<String, Integer>> getCatesMap() {
			return this.catesMap;
		}

		public void setCatesMap(HashMap<String, HashMap<String, Integer>> catesMap) {
			this.catesMap = catesMap;
		}
	}

	class Location {
		String province;
		private HashMap<String, HashMap<String, Integer>> locMap;

		public Location(String province) {
			this.province = province;
			locMap = new HashMap<String, HashMap<String, Integer>>();
		}

		public HashMap<String, HashMap<String, Integer>> getLocMap() {
			return locMap;
		}

		public void setLocMap(HashMap<String, HashMap<String, Integer>> locMap) {
			this.locMap = locMap;
		}
	}

	/**
	 * 读取泛编接口
	 * 
	 * @param sTime
	 * @param terminalTime
	 */
	public void readEditData(long sTime, long terminalTime) {
		long eTime = sTime + CMPPDataCollect.INTERVAL2;
		LOG.info("[INFO]Begin to read edit data.");
		while (sTime != terminalTime) {
			long cTime = System.currentTimeMillis() / 1000;
			String startDate = CMPPDataCollect.dateReversev2(sTime);
			String endDate = CMPPDataCollect.dateReversev2(sTime + CMPPDataCollect.INTERVAL2);
			if (cTime > (eTime + CMPPDataCollect.INTERVAL2)) {
				String url1 = "http://nyx.staff.ifeng.com/project/api/recommendMgr/getOperationStatus?startDate="
						+ startDate + "&endDate=" + endDate; // 返回json串
				List<JSONObject> joList = null;

				try {
					joList = CMPPDataCollect.readJsonID(url1);
				} catch (Exception e) {
					LOG.error("[ERROR] Read json id failed.", e);
					e.printStackTrace();
				}
				if (joList != null) {
					for (JSONObject jo : joList) {
						String id = jo.getString("id");
						if (jo.getString("state").equals("2")) {
							editId.add(id);
						}
						if (jo.getString("state").equals("0")) {
							editId.remove(id);
						}
					}
				}
				sTime = eTime;
				eTime = sTime + CMPPDataCollect.INTERVAL2;
			} else {
				try {
					Thread.sleep(15 * 1000);
				} catch (InterruptedException e) {
					LOG.error("[ERROR] Some error occurred when thread sleep.", e);
					e.printStackTrace();
				}
			}

		}
		LOG.info("[INFO] editId is " + editId.size());
	}

	/**
	 * 读取推荐表和算法表
	 * 
	 * @param sTime
	 * @param terminalTime
	 */
	public void readOriginalData(long sTime, long terminalTime) {
		long eTime = sTime + CMPPDataCollect.INTERVAL2;
		LOG.info("[INFO]Begin to read original data.");
		while (sTime != terminalTime) {
			long cTime = System.currentTimeMillis() / 1000;
			String startDate = CMPPDataCollect.dateReverse(sTime);
			String endDate = CMPPDataCollect.dateReverse(sTime + CMPPDataCollect.INTERVAL2);
			if (cTime > (eTime + CMPPDataCollect.INTERVAL2)) {
				String url1 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=" + startDate
						+ "&endTime=" + endDate;
				String url2 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_346.jhtml?startTime=" + startDate
						+ "&endTime=" + endDate;
				List<JsonFromCMPP> jList1 = null;
				List<JsonFromCMPP> jList2 = null;
				try {
					jList1 = CMPPDataCollect.readJsonList(url1);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("[ERROR]GetJsonList error.", e);
				}
				try {
					jList2 = CMPPDataCollect.readJsonList(url2);
				} catch (Exception e) {
					LOG.error("[ERROR]GetJsonList error.", e);
				}
				if (jList1 != null) {
					for (JsonFromCMPP j : jList1) {
						originalId.add(j.getId());
					}
				} else
					LOG.info("[INFO] jList1 is null!");
				if (jList2 != null) {
					for (JsonFromCMPP j : jList2) {
						algorithmId.add(j.getId());
					}
				} else
					LOG.info("[INFO] jList2 is null!");
				sTime = eTime;
				eTime = sTime + CMPPDataCollect.INTERVAL2;
			} else {
				break;
			}
		}
		LOG.info("[INFO] originalId size is " + originalId.size());
		LOG.info("[INFO] algorithmId size is " + algorithmId.size());
	}

	// public List<String> checkLocationRedis(String location) {
	// Jedis jedis = new Jedis("10.90.16.22", 6379, 5000);
	// List<String> loclist = new ArrayList<String>();
	// try {
	// jedis.auth("Q19YT2vxc206zKB");
	// jedis.select(2);
	// loclist = jedis.lrange(location, 0, -1);
	// return loclist;
	// } catch (Exception e) {
	// LOG.error("[ERROR] In check location Redis.", e);
	// return null;
	// } finally {
	// jedis.close();
	// }
	//
	// }

	/**
	 * 获取稿源的评级
	 * 
	 * @param source
	 * @return
	 */
	public String getSourceRate(String source) {
		String rate = null;
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.80.9.143", 6379, 6000);
			jedis.select(8);
			rate = jedis.get(source);
			if (rate == null) {
				rate = "other";
			}
			return rate;

		} catch (Exception e) {
			LOG.error("[ERROR] In check sourceRate Redis", e);
			return "null";
		} finally {
			jedis.close();
		}

	}

	/**
	 * 判断是否为排重后的文章
	 * 
	 * @param id
	 * @return
	 */
	// public boolean checkRepeateRedis(String id) {

	/**
	 * 判断是否为泛编出口文章
	 * 
	 * @param id
	 * @return
	 */
	public boolean checkEditSet(String id) {
		return (editId.contains(id));
	}

	/**
	 * 判断文章是否为推荐数据更新的文章
	 * 
	 * @param id
	 * @return
	 */
	public boolean checkSolr(String id) {
		String xmlStr = "";
		String url = "http://10.90.14.19:8082/solr46/item/select?q=itemid:" + id;// 全量数据
		String url1 = "http://10.90.14.19:8081/solr46/item/select?q=itemid:" + id;// 长效数据
		xmlStr = CMPPDataCollect.downloadPage(url);
		if (xmlStr.contains("numFound=\"1\"")) {
			return true;
		}
		xmlStr = CMPPDataCollect.downloadPage(url1);
		if (xmlStr.contains("numFound=\"1\"")) {
			return true;
		}
		return false;
	}

	/**
	 * 获取进入泛编的文章数量
	 * 
	 * @param endDate
	 * @throws IOException
	 * @throws ParseException
	 */
	public void getEditNum(String endDate) throws IOException, ParseException {
		String startDate1 = CalTools.getRecommendDate(CalTools.getTimeStamp2(endDate) * 1000);
		String endDate1 = CalTools.getRecommendDate((CalTools.getTimeStamp2(endDate) + 1 * 24 * 60 * 60) * 1000);
		String url = "http://nyx.staff.ifeng.com/project/api/recommendMgr/userAuditStatistic?startDate=" + startDate1
				+ "&endDate=" + endDate1 + "&system=smart"; // 进入泛编接口
		JSONObject data = null;
		try {
			data = CMPPDataCollect.readJsonByUrl(url);
		} catch (Exception e) {
			LOG.error("[ERROR] Read json by url failed.", e);
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<Integer> list = (List<Integer>) data.get("article");
		int editNum = 0;
		for (int i = 0; i < list.size(); i++) {
			editNum += list.get(i);
		}
		FileWriter fw = new FileWriter(defaultPath + endDate + "_editNum.txt", false);// 进入泛编数量
		fw.write(String.valueOf(editNum));
		fw.flush();
		fw.close();

	}

	/**
	 * 下线id
	 * 
	 * @param endDate
	 * @return
	 */
	public List<String> getOffLineNum(String endDate) {
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.90.7.52", 6379, 5000);
			jedis.select(6);
			offLineId = jedis.lrange(endDate, 0, -1);
			return offLineId;
		} catch (Exception e) {
			LOG.error("[error] get offLineId redis.", e);
			return null;
		} finally {
			jedis.close();
		}

	}

	/**
	 * 算法推荐量
	 * 
	 * @param endDate
	 */
	public void getRecommendNum(String endDate) {
		int num = 0;
		String date = null;
		try {
			date = CalTools.getRecommendDate(CalTools.getTimeStamp(endDate + " 11:11:11") * 1000);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String solr8081 = "http://10.90.14.19:8081/solr46/item/select?q=available:*";
		String solr8082 = "http://10.90.14.19:8082/solr46/item/select?q=date:";
		String xmlStr = "";
		xmlStr = CMPPDataCollect.downloadPage(solr8081);
		num = num + Integer.valueOf(xmlStr.split("numFound=\"")[1].split("\" start=")[0]);

		for (int i = 0; i < 2; i++) {
			xmlStr = CMPPDataCollect.downloadPage(solr8082 + date + "*");
			LOG.info(solr8082 + date + "*");
			num = num + Integer.valueOf(xmlStr.split("numFound=\"")[1].split("\" start=")[0]);
			try {
				date = CalTools.getRecommendDate(
						CalTools.getRecommendTimeStamp(date + " 11:11:11") * 1000 - 24 * 60 * 60 * 1000);
				LOG.info("Get recommend num Date is " + date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		LOG.info("Solr recommend num is " + num);
		FileWriter fw = null;
		try {
			fw = new FileWriter(defaultPath + endDate + "_recommend.txt", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fw.write(String.valueOf(num));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 推荐数按无图和视频统计
	 * 
	 * @param endDate
	 * @throws ParseException
	 * @throws IOException
	 */
	public void calRecommendNum(String endDate) throws ParseException, IOException {
		HashMap<String, String> recommendMap = new HashMap<String, String>();
		String date = null;
		int num1 = 0;
		int num2 = 0;

		date = CalTools.getRecommendDate(CalTools.getTimeStamp(endDate + " 11:11:11") * 1000);
		String firstsolr8081 = "http://10.90.14.19:8081/solr46/item/select?q=available:*%20AND%20doctype:doc";
		String secondsolr8081 = "http://10.90.14.19:8081/solr46/item/select?q=available:*%20AND%20doctype:video";
		String solr8082 = "http://10.90.14.19:8082/solr46/item/select?q=date:";
		String xmlStr = "";
		String xmlStr1 = "";
		xmlStr = CMPPDataCollect.downloadPage(firstsolr8081);
		num1 = num1 + Integer.valueOf(xmlStr.split("numFound=\"")[1].split("\" start=")[0]);
		xmlStr = CMPPDataCollect.downloadPage(secondsolr8081);
		num2 = num2 + Integer.valueOf(xmlStr.split("numFound=\"")[1].split("\" start=")[0]);

		for (int i = 0; i < 2; i++) {

			xmlStr = CMPPDataCollect.downloadPage(solr8082 + date + "*%20AND%20doctype:doc");
			xmlStr1 = CMPPDataCollect.downloadPage(solr8082 + date + "*%20AND%20doctype:video");

			num1 = num1 + Integer.valueOf(xmlStr.split("numFound=\"")[1].split("\" start=")[0]);
			num2 = num2 + Integer.valueOf(xmlStr1.split("numFound=\"")[1].split("\" start=")[0]);
			try {
				date = CalTools.getRecommendDate(
						CalTools.getRecommendTimeStamp(date + " 11:11:11") * 1000 - 24 * 60 * 60 * 1000);

			} catch (ParseException e) {
				e.printStackTrace();
			}
			;

		}

		recommendMap.put("无图", String.valueOf(num1));
		recommendMap.put("视频", String.valueOf(num2));
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.90.7.52", 6379);
			jedis.select(5);

			jedis.hmset(endDate + "-推荐类型统计数据", recommendMap);

		} catch (Exception ex) {
			LOG.error("[ERROR] In write item to Redis.", ex);
		} finally {
			jedis.close();
		}

		FileWriter fw7 = new FileWriter(defaultPath + endDate + "_recommendCate.txt", false);// 推荐类型
		fw7.write("推荐类型\t推荐数量 \n");
		int sum = 0;
		for (Map.Entry<String, String> entry : recommendMap.entrySet()) {
			sum = sum + Integer.parseInt(entry.getValue());
			fw7.write(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		fw7.write("合计" + "\t" + sum);
		fw7.flush();
		fw7.close();
	}

	/**
	 * 地域省市对应
	 */
	public void statisticsLocation() {

		for (Map.Entry<String, HashMap<String, Integer>> entry : allLocationMap.entrySet()) {

			if (entry.getKey().split("->").length == 1) {
				continue;
			}
			String location = entry.getKey().split("->")[0];
			String location1 = entry.getKey().split("->")[1];
			if (location1.contains("北京") || location1.contains("上海") || location1.contains("重庆")
					|| location1.contains("天津")) {
				location1 = entry.getKey().split("->")[2];
			}
			Location loc;
			HashMap<String, HashMap<String, Integer>> locMap = new HashMap<String, HashMap<String, Integer>>();
			if (locationMap.containsKey(location)) {
				loc = locationMap.get(location);
				locMap = loc.getLocMap();
				locMap.put(location1, entry.getValue());
			} else {
				loc = new Location(location);
				locMap = loc.getLocMap();
				locMap.put(location1, entry.getValue());

			}
			loc.setLocMap(locMap);
			locationMap.put(location, loc);
		}
	}

	/**
	 * 来源映射关系
	 */
	public void mappingData() {
		for (Entry<String, Data> entry : dataMap.entrySet()) {
			String mappingSource = null;
			if ("null".equals(entry.getKey())) {
				mappingSource = "null";
			} else if ("item#null".equals(entry.getKey())) {
				mappingSource = "item#null";
			} else {
				mappingSource = source1Map.get(entry.getKey());
			}
			if (mappingSource == null) {
				continue;
			}
			if (mappingDataMap.containsKey(mappingSource)) {
				Data data = entry.getValue();
				Data mappingdata = mappingDataMap.get(mappingSource);
				HashMap<String, HashMap<String, Integer>> cateMap = data.getCatesMap();
				HashMap<String, HashMap<String, Integer>> mappingCateMap = mappingdata.getCatesMap();
				for (Entry<String, HashMap<String, Integer>> catentry : cateMap.entrySet()) {
					String cate = catentry.getKey();
					if (mappingCateMap.containsKey(cate)) {
						HashMap<String, Integer> numMap = mappingCateMap.get(cate);
						numMap.put("ornum", numMap.get("ornum") + catentry.getValue().get("ornum"));
						numMap.put("odnum", numMap.get("odnum") + catentry.getValue().get("odnum"));
						numMap.put("renum", numMap.get("renum") + catentry.getValue().get("renum"));
						numMap.put("ednum", numMap.get("ednum") + catentry.getValue().get("ednum"));
						numMap.put("lanum", numMap.get("lanum") + catentry.getValue().get("lanum"));
						mappingCateMap.put(cate, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						numMap.put("ornum", catentry.getValue().get("ornum"));
						numMap.put("odnum", catentry.getValue().get("odnum"));
						numMap.put("renum", catentry.getValue().get("renum"));
						numMap.put("ednum", catentry.getValue().get("ednum"));
						numMap.put("lanum", catentry.getValue().get("lanum"));
						mappingCateMap.put(cate, numMap);
					}
				}
				mappingdata.setCatesMap(mappingCateMap);
				mappingDataMap.put(mappingSource, mappingdata);
			} else {
				Data data = entry.getValue();
				Data mappingdata = new Data(mappingSource);
				HashMap<String, HashMap<String, Integer>> cateMap = data.getCatesMap();
				HashMap<String, HashMap<String, Integer>> mappingCateMap = mappingdata.getCatesMap();
				for (Entry<String, HashMap<String, Integer>> catentry : cateMap.entrySet()) {
					HashMap<String, Integer> numMap = new HashMap<String, Integer>();
					numMap.put("ornum", catentry.getValue().get("ornum"));
					numMap.put("odnum", catentry.getValue().get("odnum"));
					numMap.put("renum", catentry.getValue().get("renum"));
					numMap.put("ednum", catentry.getValue().get("ednum"));
					numMap.put("lanum", catentry.getValue().get("lanum"));
					mappingCateMap.put(catentry.getKey(), numMap);
				}
				mappingdata.setCatesMap(mappingCateMap);
				mappingDataMap.put(mappingSource, mappingdata);
			}

		}
	}

	/**
	 * 数据统计
	 * 
	 * @param startDate
	 * @param endDate
	 * @throws ParseException
	 * @throws IOException
	 */
	public void process(String startDate, String endDate) throws ParseException, IOException {
		long sTime = CalTools.getTimeStamp(startDate + " 23:00:00");
		long terminalTime = CalTools.getTimeStamp(endDate + " 23:00:00");
		LOG.info("[INFO] start date is " + startDate + " ,end date is " + endDate);

		readEditData(sTime, terminalTime);
		readOriginalData(sTime, terminalTime);
		getOffLineNum(endDate);
		FileWriter fw = new FileWriter(defaultPath + endDate + "_timeSensitive#null.txt", false);
		FileWriter fw1 = new FileWriter(defaultPath + endDate + "_articleSource#null.txt", false);// 稿源
		FileWriter fw2 = new FileWriter(defaultPath + endDate + "_offLineNum.txt", false);
		FileWriter fw3 = new FileWriter(defaultPath + endDate + "_category#null.txt", false);
		FileWriter fw4 = new FileWriter(defaultPath + endDate + "_latentTopic#null.txt", false);
		FileWriter fw5 = new FileWriter(defaultPath + endDate + "_recommendId.txt", false);
		Jedis repeatejedis = RedisUtil.getJedisDbClient("10.90.14.13", 6379, 9);

		Jedis jedis = null;
		try {
			jedis = new Jedis("10.80.8.143", 6379, 5000);
			jedis.select(2);
		} catch (Exception ex) {
			LOG.error("[error] get 'item is null' redis", ex);
		}
		Jedis jedis2 = null;
		try {
			jedis2 = new Jedis("10.90.16.22", 6379, 5000);
			jedis2.auth("Q19YT2vxc206zKB");
			jedis2.select(5);
		} catch (Exception ex) {
			LOG.error("[ERROR]", ex);
		}
		// IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
		int offLineNum = 0;
		int num = 0;
		List<String> keys = new ArrayList<String>();
		for (String id : originalId) {
			num++;
			keys.add("cmpp_" + id);
			if (num % 2000 == 0 || num == originalId.size()) {
				LOG.info("进行到 ：" + num);
				Map<String, itemf> ItemMap = hbaseOperation.gets(keys);
				keys = new ArrayList<String>();
				for (Map.Entry<String, itemf> entry : ItemMap.entrySet()) {
					itemf item = entry.getValue();
					String articleId = entry.getKey().split("_")[1];

					boolean odnum = true;
					boolean renum = repeatejedis.exists(articleId);
					boolean ednum = checkEditSet(articleId);
					boolean lanum = checkSolr(articleId);
					if (lanum) {
						fw5.write(articleId + "\n");
					}
					List<String> locList = new ArrayList<String>();
					String timeSensitive = null;
					String location = null;

					String docType = null;
					String source = null;// 稿源
					// String otherStr = null;
					String source1 = null; // 来源
					String videoSource = null;// 视频来源
					String latentTopic = null;
					String porn = null;
					String titleParty = null;
					String isCreation = null;
					String qualityEvalLevel = null;
					String category = null;// 分类
					String specialParam = null;// 视频来源
					if (offLineId.contains(articleId)) {
						offLineNum++;
					}
					if (item == null) {
						LOG.info("[originalId] item is null :" + articleId);
						try {
							jedis.set("cmpp_" + articleId, "null");
						} catch (Exception e) {
							LOG.info("[ERROR] write 'item#null' redis error");
						}
						timeSensitive = "item#null";
						docType = "item#null";
						source = "item#null";
						porn = "item#null";
						titleParty = "item#null";
						isCreation = "item#null";
						qualityEvalLevel = "item#null";
						videoSource = "item#null";
						latentTopic = "item#null";
						category = "item#null";
						source1 = "item#null";
						location = "item#null";
					} else {
						timeSensitive = item.getTimeSensitive();
						locList = item.getLocList();
						if (locList == null || locList.isEmpty()) {
							location = "null";
						}
						docType = item.getDocType();

						if (item.getPorn()) {
							porn = "true";
						} else {
							porn = "false";
						}
						if (item.getTitleParty()) {
							titleParty = "true";
						} else {
							titleParty = "false";
						}
						if (item.getisCreation()) {
							isCreation = "true";
						} else {
							isCreation = "false";
						}

						specialParam = item.getSpecialParam();
						if (specialParam == null || specialParam.isEmpty()) {
							videoSource = "null";
						} else {
							JSONObject jsonObject = JSONObject.fromObject(specialParam);
							if (jsonObject.toString().contains("cpName") && jsonObject.getString("cpName") != null
									&& !jsonObject.getString("cpName").isEmpty()) {
								videoSource = jsonObject.getString("cpName");
							} else {
								videoSource = "null";
							}
						}
						source = item.getSource();// 稿源
						if (source == null || source.isEmpty()) {
							source = "null";
							fw1.write(id + "\n");
						}
						if (docType == null || docType.isEmpty()) {
							docType = "null";
						}
						latentTopic = item.getLatentTopic();
						if (latentTopic == null || latentTopic.isEmpty()) {
							latentTopic = "null";
							fw4.write(id + "\n");
						}
						// 来源
						// source=phvideo|!|channel=video|!|hotlevel=D|!|tags=综艺-真人秀|!|recommendLevel=2|!|aspect=16:9|!|qualitylevel=A|!|reviewStatus=auto
						if (item.getOther() == null || item.getOther().isEmpty()) {
							LOG.info("item.getOther() is null or empty,set source is null,id is:" + id);
							source1 = "null";
						} else {
							if (item.getOther().contains("source=")) {
								source1 = item.getOther().split("source=")[1].split("\\|!\\|")[0];
								if (source1.contains("wemedia")) {
									if (item.getOther().contains("from=yidian"))
										source1 = "yidianwemedia";
									else
										source1 = "otherwemedia";
								}
							} else {
								LOG.info("[INFO]Item other is " + item.getOther());
								LOG.info("[INFO]Source is empty, set source = spider");
								source1 = "spider";
							}
						}
						// otherStr = item.getOther();
						// source1 =
						// CheckArticleFrom.checkFromByOther(otherStr);
						if (!source1Map.containsKey(source1)) {
							LOG.info("[INFO] 未知的source：" + source1);
						}
						ArrayList<String> categoryList = FeatureExTools.whatCategory(item.getFeatures());
						if (categoryList == null || categoryList.isEmpty()) {
							category = "null";
							fw3.write(id + "\n");
						} else
							category = categoryList.get(0);

						// 质量评级得分
						qualityEvalLevel = item.getQualityEvalLevel();
						if (qualityEvalLevel == null || qualityEvalLevel.isEmpty()) {
							qualityEvalLevel = "null";
						} else if (Double.valueOf(qualityEvalLevel) >= 0 && Double.valueOf(qualityEvalLevel) < 3) {
							qualityEvalLevel = "E等";
						} else if (Double.valueOf(qualityEvalLevel) >= 3 && Double.valueOf(qualityEvalLevel) < 5.5) {
							qualityEvalLevel = "D等";
						} else if (Double.valueOf(qualityEvalLevel) >= 5.5 && Double.valueOf(qualityEvalLevel) < 7) {
							qualityEvalLevel = "C等";
						} else if (Double.valueOf(qualityEvalLevel) >= 7 && Double.valueOf(qualityEvalLevel) < 8) {
							qualityEvalLevel = "B等";
						} else if (Double.valueOf(qualityEvalLevel) >= 8 && Double.valueOf(qualityEvalLevel) <= 10) {
							qualityEvalLevel = "A等";
						} else if (Double.valueOf(qualityEvalLevel) == -1) {
							qualityEvalLevel = "视频";
						}
						// 时效性
						if (timeSensitive == null || timeSensitive.isEmpty()) {
							timeSensitive = "null";
							fw.write(id + "\n");
						} else if (!("nt".equals(timeSensitive))) {
							long time = CalTools.getRecommendTimeStamp(timeSensitive);
							if (time <= (sTime + 7 * 60 * 60)) {
								timeSensitive = "06点";
							} else if (time <= (sTime + 13 * 60 * 60)) {
								timeSensitive = "12点";
							} else if (time <= (sTime + 19 * 60 * 60)) {
								timeSensitive = "18点";
							} else if (time <= (sTime + 25 * 60 * 60)) {
								timeSensitive = "24点";
							} else if (time <= (sTime + 25 * 60 * 60 + 2 * 24 * 60 * 60)) {
								timeSensitive = "2天";
							} else if (time <= (sTime + 25 * 60 * 60 + 3 * 24 * 60 * 60)) {
								timeSensitive = "3天";
							} else if (time <= (sTime + 25 * 60 * 60 + 5 * 24 * 60 * 60)) {
								timeSensitive = "5天";
							} else if (time <= (sTime + 25 * 60 * 60 + 7 * 24 * 60 * 60)) {
								timeSensitive = "7天";
							} else {
								timeSensitive = "other";
							}
						}
					}

					Data data;
					HashMap<String, HashMap<String, Integer>> catesmap = new HashMap<String, HashMap<String, Integer>>();
					if (dataMap.containsKey(source1)) {
						data = dataMap.get(source1);
						catesmap = data.getCatesMap();
						if (catesmap.containsKey(category)) {
							HashMap<String, Integer> numMap = catesmap.get(category);
							numMap.put("ornum", numMap.get("ornum") + 1);// ornum原始抓取数量
							if (odnum)
								numMap.put("odnum", numMap.get("odnum") + 1);
							if (renum)
								numMap.put("renum", numMap.get("renum") + 1);
							if (ednum)
								numMap.put("ednum", numMap.get("ednum") + 1);
							if (lanum)
								numMap.put("lanum", numMap.get("lanum") + 1);
							catesmap.put(category, numMap);
						} else {
							HashMap<String, Integer> numMap = new HashMap<String, Integer>();
							numMap.put("ornum", 1);
							if (odnum)
								numMap.put("odnum", 1);
							else
								numMap.put("odnum", 0);
							if (renum)
								numMap.put("renum", 1);
							else
								numMap.put("renum", 0);
							if (ednum)
								numMap.put("ednum", 1);
							else
								numMap.put("ednum", 0);
							if (lanum)
								numMap.put("lanum", 1);
							else
								numMap.put("lanum", 0);
							catesmap.put(category, numMap);
						}
					} else {
						data = new Data(source1);
						catesmap = data.getCatesMap();
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						numMap.put("ornum", 1);
						if (odnum)
							numMap.put("odnum", 1);
						else
							numMap.put("odnum", 0);
						if (renum)
							numMap.put("renum", 1);
						else
							numMap.put("renum", 0);
						if (ednum)
							numMap.put("ednum", 1);
						else
							numMap.put("ednum", 0);
						if (lanum)
							numMap.put("lanum", 1);
						else
							numMap.put("lanum", 0);
						catesmap.put(category, numMap);
					}
					data.setCatesMap(catesmap);
					dataMap.put(source1, data);
					// 视频稿源
					if ("video".equals(docType)) {
						// 一级视频稿源
						if (videoTopSourceMap.containsKey(videoSource)) {
							HashMap<String, Integer> numMap = videoTopSourceMap.get(videoSource);
							if (odnum) {
								numMap.put("odnum", numMap.get("odnum") + 1);
							}
							if (renum) {
								numMap.put("renum", numMap.get("renum") + 1);
							}
							if (ednum) {
								numMap.put("ednum", numMap.get("ednum") + 1);
							}
							if (lanum) {
								numMap.put("lanum", numMap.get("lanum") + 1);
							}
							videoTopSourceMap.put(videoSource, numMap);
						} else {
							HashMap<String, Integer> numMap = new HashMap<String, Integer>();
							if (odnum) {
								numMap.put("odnum", 1);
							} else {
								numMap.put("odnum", 0);
							}
							if (renum) {
								numMap.put("renum", 1);
							} else {
								numMap.put("renum", 0);
							}
							if (ednum) {
								numMap.put("ednum", 1);
							} else {
								numMap.put("ednum", 0);
							}
							if (lanum) {
								numMap.put("lanum", 1);
							} else {
								numMap.put("lanum", 0);
							}
							videoTopSourceMap.put(videoSource, numMap);
						}

						Data data1;
						HashMap<String, HashMap<String, Integer>> vSourcemap = new HashMap<String, HashMap<String, Integer>>();
						if (videoSourceMap.containsKey(videoSource)) {
							data1 = videoSourceMap.get(videoSource);
							vSourcemap = data1.getCatesMap();
							if (vSourcemap.containsKey(source)) {
								HashMap<String, Integer> numMap = vSourcemap.get(source);
								if (odnum) {
									numMap.put("odnum", numMap.get("odnum") + 1);
								}
								if (renum) {
									numMap.put("renum", numMap.get("renum") + 1);
								}
								if (ednum) {
									numMap.put("ednum", numMap.get("ednum") + 1);
								}
								if (lanum) {
									numMap.put("lanum", numMap.get("lanum") + 1);
								}
								vSourcemap.put(source, numMap);
							} else {
								HashMap<String, Integer> numMap = new HashMap<String, Integer>();
								if (odnum) {
									numMap.put("odnum", 1);
								} else {
									numMap.put("odnum", 0);
								}
								if (renum) {
									numMap.put("renum", 1);
								} else {
									numMap.put("renum", 0);
								}
								if (ednum) {
									numMap.put("ednum", 1);
								} else {
									numMap.put("ednum", 0);
								}
								if (lanum) {
									numMap.put("lanum", 1);
								} else {
									numMap.put("lanum", 0);
								}
								vSourcemap.put(source, numMap);
							}
						} else {
							data1 = new Data(videoSource);
							vSourcemap = data1.getCatesMap();
							HashMap<String, Integer> numMap = new HashMap<String, Integer>();
							if (odnum)
								numMap.put("odnum", 1);
							else
								numMap.put("odnum", 0);
							if (renum)
								numMap.put("renum", 1);
							else
								numMap.put("renum", 0);
							if (ednum)
								numMap.put("ednum", 1);
							else
								numMap.put("ednum", 0);
							if (lanum)
								numMap.put("lanum", 1);
							else
								numMap.put("lanum", 0);
							vSourcemap.put(videoSource, numMap);
						}
						data1.setCatesMap(vSourcemap);
						videoSourceMap.put(videoSource, data1);
					}

					// 质量评级得分
					if (qualityEvalLevelMap.containsKey(qualityEvalLevel)) {
						HashMap<String, Integer> numMap = qualityEvalLevelMap.get(qualityEvalLevel);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						qualityEvalLevelMap.put(qualityEvalLevel, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}

						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						qualityEvalLevelMap.put(qualityEvalLevel, numMap);
					}

					// 时效性
					if (timeSensitiveMap.containsKey(timeSensitive)) {
						HashMap<String, Integer> numMap = timeSensitiveMap.get(timeSensitive);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						timeSensitiveMap.put(timeSensitive, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}
						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						timeSensitiveMap.put(timeSensitive, numMap);
					}
					// 稿源
					if (sourceMap.containsKey(source)) {
						HashMap<String, Integer> numMap = sourceMap.get(source);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						sourceMap.put(source, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}

						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						sourceMap.put(source, numMap);
					}

					if (!("null".equals(latentTopic)) && !("item#null".equals(latentTopic))) {
						JSONObject jObject = JSONObject.fromObject(latentTopic);
						JSONArray jsonArray = (JSONArray) jObject.get("topics");
						if (jsonArray.size() == 0) {
							fw4.write(id + "\n");
						} else {
							String topic = null;
							for (int i = 0; i < jsonArray.size(); i++) {
								topic = jsonArray.getJSONObject(i).getString("id");
								if (latentTopicMap.containsKey(topic)) {
									latentTopicMap.put(topic, latentTopicMap.get(topic) + 1);
								} else {
									latentTopicMap.put(topic, 1);
								}
								if (i == 1)
									break;
							}
						}
					}
					// 地域
					if (("item#null").equals(location) || "null".equals(location)) {
						if (topLocationMap.containsKey(location)) {
							HashMap<String, Integer> numMap = topLocationMap.get(location);
							if (odnum) {
								numMap.put("odnum", numMap.get("odnum") + 1);
							}
							if (renum) {
								numMap.put("renum", numMap.get("renum") + 1);
							}
							if (ednum) {
								numMap.put("ednum", numMap.get("ednum") + 1);
							}
							if (lanum) {
								numMap.put("lanum", numMap.get("lanum") + 1);
							}
							topLocationMap.put(location, numMap);
						} else {
							HashMap<String, Integer> numMap = new HashMap<String, Integer>();
							if (odnum) {
								numMap.put("odnum", 1);
							} else {
								numMap.put("odnum", 0);
							}
							if (renum) {
								numMap.put("renum", 1);
							} else {
								numMap.put("renum", 0);
							}
							if (ednum) {
								numMap.put("ednum", 1);
							} else {
								numMap.put("ednum", 0);
							}
							if (lanum) {
								numMap.put("lanum", 1);
							} else {
								numMap.put("lanum", 0);
							}
							topLocationMap.put(location, numMap);
						}
					} else {
						HashSet<String> set = new HashSet<String>();
						for (int i = 0; i < locList.size(); i++) {
							String str = locList.get(i);
							JSONObject jo = JSONObject.fromObject(str);
							if (Double.valueOf(jo.getString("weight")) >= 0.5 && jo.getString("loc") != null
									&& !(jo.getString("loc").isEmpty())) {
								location = jo.getString("loc");
								// if (location.contains("北京市")) {
								// System.out.println(location);
								// }
							} else {
								continue;
							}
							if (topLocationList.contains(location.split("->")[0])) {
								set.add(location.split("->")[0]);
							}
							if (allLocationMap.containsKey(location)) {
								HashMap<String, Integer> numMap = allLocationMap.get(location);
								if (odnum) {
									numMap.put("odnum", numMap.get("odnum") + 1);
								}
								if (renum) {
									numMap.put("renum", numMap.get("renum") + 1);
								}
								if (ednum) {
									numMap.put("ednum", numMap.get("ednum") + 1);
								}
								if (lanum) {
									numMap.put("lanum", numMap.get("lanum") + 1);
								}
								allLocationMap.put(location, numMap);
							} else {
								HashMap<String, Integer> numMap = new HashMap<String, Integer>();
								if (odnum) {
									numMap.put("odnum", 1);
								} else {
									numMap.put("odnum", 0);
								}
								if (renum) {
									numMap.put("renum", 1);
								} else {
									numMap.put("renum", 0);
								}
								if (ednum) {
									numMap.put("ednum", 1);
								} else {
									numMap.put("ednum", 0);
								}

								if (lanum) {
									numMap.put("lanum", 1);
								} else {
									numMap.put("lanum", 0);
								}
								allLocationMap.put(location, numMap);
							}

						}
						for (String topLocation : set) {
							if (topLocationMap.containsKey(topLocation)) {
								HashMap<String, Integer> numMap = topLocationMap.get(topLocation);
								if (odnum) {
									numMap.put("odnum", numMap.get("odnum") + 1);
								}
								if (renum) {
									numMap.put("renum", numMap.get("renum") + 1);
								}
								if (ednum) {
									numMap.put("ednum", numMap.get("ednum") + 1);
								}
								if (lanum) {
									numMap.put("lanum", numMap.get("lanum") + 1);
								}
								topLocationMap.put(topLocation, numMap);
							} else {
								HashMap<String, Integer> numMap = new HashMap<String, Integer>();
								if (odnum) {
									numMap.put("odnum", 1);
								} else {
									numMap.put("odnum", 0);
								}
								if (renum) {
									numMap.put("renum", 1);
								} else {
									numMap.put("renum", 0);
								}
								if (ednum) {
									numMap.put("ednum", 1);
								} else {
									numMap.put("ednum", 0);
								}

								if (lanum) {
									numMap.put("lanum", 1);
								} else {
									numMap.put("lanum", 0);
								}
								topLocationMap.put(topLocation, numMap);
							}
						}
					}

					// 文章类型
					if (docTypeMap.containsKey(docType)) {
						HashMap<String, Integer> numMap = docTypeMap.get(docType);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						docTypeMap.put(docType, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}
						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						docTypeMap.put(docType, numMap);
					}
					// 色情
					if (pornMap.containsKey(porn)) {
						HashMap<String, Integer> numMap = pornMap.get(porn);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						pornMap.put(porn, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}
						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						pornMap.put(porn, numMap);
					}
					// 标题党
					if (titlePartyMap.containsKey(titleParty)) {
						HashMap<String, Integer> numMap = titlePartyMap.get(titleParty);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						titlePartyMap.put(titleParty, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}
						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						titlePartyMap.put(titleParty, numMap);
					}

					// 是否为原创
					if (isCreationMap.containsKey(isCreation)) {
						HashMap<String, Integer> numMap = isCreationMap.get(isCreation);
						if (odnum) {
							numMap.put("odnum", numMap.get("odnum") + 1);
						}
						if (renum) {
							numMap.put("renum", numMap.get("renum") + 1);
						}
						if (ednum) {
							numMap.put("ednum", numMap.get("ednum") + 1);
						}
						if (lanum) {
							numMap.put("lanum", numMap.get("lanum") + 1);
						}
						isCreationMap.put(isCreation, numMap);
					} else {
						HashMap<String, Integer> numMap = new HashMap<String, Integer>();
						if (odnum) {
							numMap.put("odnum", 1);
						} else {
							numMap.put("odnum", 0);
						}
						if (renum) {
							numMap.put("renum", 1);
						} else {
							numMap.put("renum", 0);
						}
						if (ednum) {
							numMap.put("ednum", 1);
						} else {
							numMap.put("ednum", 0);
						}
						if (lanum) {
							numMap.put("lanum", 1);
						} else {
							numMap.put("lanum", 0);
						}
						isCreationMap.put(isCreation, numMap);
					}
				}

			}
			// itemf item = ikvop.queryItemF("cmpp_" + id);

		}
		jedis.close();
		jedis2.close();
		fw2.write(String.valueOf(offLineNum));

		// 遍历算法表id
		// for (String id : algorithmId) {
		// itemf item = ikvop.queryItemF("cmpp_" + id);
		// String source1 = null;
		//
		// if (item == null) {
		// continue;
		// }
		//
		// // 来源
		// //
		// source=phvideo|!|channel=video|!|hotlevel=D|!|tags=综艺-真人秀|!|recommendLevel=2|!|aspect=16:9|!|qualitylevel=A|!|reviewStatus=auto
		// if (item.getOther() == null || item.getOther().isEmpty()) {
		// LOG.info("item.getOther() is null or empty,set source is null,id is:"
		// + id);
		// source1 = "null";
		// } else {
		// if (item.getOther().contains("source=")) {
		// source1 = item.getOther().split("source=")[1].split("\\|!\\|")[0];
		// if (source1.contains("wemedia")) {
		// if (item.getOther().contains("from=yidian"))
		// source1 = "yidianwemedia";
		// else
		// source1 = "otherwemedia";
		// }
		// } else {
		// LOG.info("[INFO]Item other is " + item.getOther());
		// LOG.info("[INFO]Source is empty, set source = spider");
		// source1 = "spider";
		// }
		// }
		// if (!source1Map.containsKey(source1)) {
		// LOG.info("[INFO] 未知的source：" + source1);
		// }
		// String category = null;
		// ArrayList<String> categoryList =
		// FeatureExTools.whatCategory(item.getFeatures());
		// if (categoryList == null || categoryList.isEmpty()) {
		// category = "null";
		// } else
		// category = categoryList.get(0);
		//
		// Data data;
		// HashMap<String, HashMap<String, Integer>> catesmap;
		// if (dataMap.containsKey(source1)) {
		// data = dataMap.get(source1);
		// catesmap = data.getCatesMap();
		// if (catesmap.containsKey(category)) {
		// HashMap<String, Integer> numMap = catesmap.get(category);
		// numMap.put("ornum", numMap.get("ornum") + 1);
		// catesmap.put(category, numMap);
		// } else {
		// HashMap<String, Integer> numMap = new HashMap<String, Integer>();
		// numMap.put("ornum", 1);
		// numMap.put("odnum", 0);
		// numMap.put("renum", 0);
		// numMap.put("ednum", 0);
		// numMap.put("lanum", 0);
		// catesmap.put(category, numMap);
		// }
		// } else {
		// data = new Data(source1);
		// catesmap = data.getCatesMap();
		// HashMap<String, Integer> numMap = new HashMap<String, Integer>();
		// numMap.put("ornum", 1);
		// numMap.put("odnum", 0);
		// numMap.put("renum", 0);
		// numMap.put("ednum", 0);
		// numMap.put("lanum", 0);
		// }
		// data.setCatesMap(catesmap);
		// dataMap.put(source1, data);
		//
		// }
		fw.flush();
		fw.close();
		fw1.flush();
		fw1.close();
		fw2.flush();
		fw2.close();
		fw3.flush();
		fw3.close();
		fw4.flush();
		fw4.close();
		fw5.flush();
		fw5.close();
		hbaseOperation.close();
		repeatejedis.close();
	}

	public void output(String endDate) throws IOException {
		LOG.info("[INFO] start to write.");
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);

		FileWriter fw = new FileWriter(defaultPath + endDate + "_articleSource.txt", false);// 稿源
		FileWriter fw2 = new FileWriter(defaultPath + endDate + "_porn.txt", false);// 色情
		FileWriter fw3 = new FileWriter(defaultPath + endDate + "_titleParty.txt", false);// 标题党
		FileWriter fw4 = new FileWriter(defaultPath + endDate + "_docType.txt", false);// 文章类型
		FileWriter fw5 = new FileWriter(defaultPath + endDate + "_location.txt", false);// 地域类型
		FileWriter fw6 = new FileWriter(defaultPath + endDate + "_timeSensitive.txt", false);// 时效性
		FileWriter fw7 = new FileWriter(defaultPath + endDate + "_topLocation.txt", false);// 地域类型
		FileWriter fw8 = new FileWriter(defaultPath + endDate + "_isCreation.txt", false);// 是否为原创
		FileWriter fw9 = new FileWriter(defaultPath + endDate + "_qualityEvalLevel.txt", false);// 质量评级得分
		FileWriter fw10 = new FileWriter(defaultPath + endDate + "_videoSource.txt", false);// 视频稿源
		FileWriter fw11 = new FileWriter(defaultPath + endDate + "_latentTopic.txt", false);// 主题
		FileWriter fw12 = new FileWriter(defaultPath + endDate + "_videoTopSource.txt", false);// 一级视频稿源
		fw.write("稿源\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw2.write("低俗\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw3.write("标题党\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw4.write("文章类型\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw5.write("地域\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw6.write("时效性\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw7.write("一级地域\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw8.write("原创\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw9.write("质量评级得分\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw10.write("稿源\t视频稿源\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw12.write("稿源\t质量评级过滤后数量 \t排重后数量\t泛编出口数量\t推荐数据更新量\n");

		int addod = 0;
		int addre = 0;
		int added = 0;
		int addla = 0;
		// 稿源
		HashMap<String, String> map1 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap = MapSort.mapSortList(sourceMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");
			map1.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum") + "\t"
					+ entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("稿源合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);

		// 色情
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		HashMap<String, String> map2 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap1 = MapSort.mapSortList(pornMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap1) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");

			map2.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw2.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw2.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("色情合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);
		// 标题党
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		Map<String, String> map3 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap2 = MapSort.mapSortList(titlePartyMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap2) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");

			map3.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw3.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw3.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("标题党合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);

		// 文章类型
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		HashMap<String, String> map4 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap3 = MapSort.mapSortList(docTypeMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap3) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");
			map4.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw4.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw4.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("文章类型合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);
		// 地域
		HashMap<String, String> map5 = new HashMap<String, String>();
		for (Map.Entry<String, Location> entry : locationMap.entrySet()) {
			if (topLocationMap.containsKey(entry.getKey())) {
				map5.put(entry.getKey(),
						topLocationMap.get(entry.getKey()).get("odnum") + "#"
								+ topLocationMap.get(entry.getKey()).get("renum") + "#"
								+ topLocationMap.get(entry.getKey()).get("ednum") + "#"
								+ topLocationMap.get(entry.getKey()).get("lanum"));
				fw5.write(entry.getKey() + "\t" + topLocationMap.get(entry.getKey()).get("odnum") + "\t"
						+ topLocationMap.get(entry.getKey()).get("renum") + "\t"
						+ topLocationMap.get(entry.getKey()).get("ednum") + "\t"
						+ topLocationMap.get(entry.getKey()).get("lanum") + "\n");
			}
			Location location = locationMap.get(entry.getKey());
			HashMap<String, HashMap<String, Integer>> locMap = location.getLocMap();
			List<Map.Entry<String, HashMap<String, Integer>>> sortedLocationList = MapSort.mapSortList(locMap);

			for (Map.Entry<String, HashMap<String, Integer>> entry1 : sortedLocationList) {
				map5.put(entry1.getKey(), entry1.getValue().get("odnum") + "#" + entry1.getValue().get("renum") + "#"
						+ entry1.getValue().get("ednum") + "#" + entry1.getValue().get("lanum"));
				fw5.write(
						entry1.getKey() + "\t" + entry1.getValue().get("odnum") + "\t" + entry1.getValue().get("renum")
								+ "\t" + entry1.getValue().get("ednum") + "\t" + entry1.getValue().get("lanum") + "\n");
			}
			fw5.write("0000\n");
		}
		fw5.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("地域合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);
		// 一级地域
		List<Map.Entry<String, HashMap<String, Integer>>> sortedLocationList1 = MapSort.mapSortList(topLocationMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedLocationList1) {
			fw7.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}

		// 时效性
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		HashMap<String, String> map6 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap5 = MapSort.mapSort(timeSensitiveMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap5) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");
			map6.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw6.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw6.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("时效性合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);
		// 是否为原创
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		HashMap<String, String> map8 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap7 = MapSort.mapSortList(isCreationMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap7) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");

			map8.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw8.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw8.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("原创合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);

		// 质量评级得分
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		HashMap<String, String> map9 = new HashMap<String, String>();
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap9 = MapSort.mapSort(qualityEvalLevelMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap9) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");
			map9.put(entry.getKey(), entry.getValue().get("odnum") + "#" + entry.getValue().get("renum") + "#"
					+ entry.getValue().get("ednum") + "#" + entry.getValue().get("lanum"));
			fw9.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw9.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		LOG.info("质量评级得分合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla);

		// 视频稿源
		for (Entry<String, Data> entry : videoSourceMap.entrySet()) {
			Data data = entry.getValue();
			HashMap<String, HashMap<String, Integer>> vSourceMap = data.getCatesMap();
			List<Map.Entry<String, HashMap<String, Integer>>> sortedMap10 = MapSort.mapSortList(vSourceMap);
			for (Entry<String, HashMap<String, Integer>> source : sortedMap10) {
				fw10.write(entry.getKey() + "\t" + source.getKey() + "\t" + source.getValue().get("odnum") + "\t"
						+ source.getValue().get("renum") + "\t" + source.getValue().get("ednum") + "\t"
						+ source.getValue().get("lanum") + "\n");
			}
		}

		// 视频稿源
		addod = 0;
		addre = 0;
		added = 0;
		addla = 0;
		List<Map.Entry<String, HashMap<String, Integer>>> sortedLocationList12 = MapSort.mapSortList(videoTopSourceMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedLocationList12) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");
			fw12.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + entry.getValue().get("ednum") + "\t" + entry.getValue().get("lanum") + "\n");
		}
		fw12.write("合计\t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");

		List<Map.Entry<String, Integer>> sortedMap11 = MapSort.mapSort3(latentTopicMap);
		for (Map.Entry<String, Integer> entry : sortedMap11) {
			fw11.write(entry.getKey() + "\t" + entry.getValue() + "\n");
		}

		fw.flush();
		fw.close();
		fw2.flush();
		fw2.close();
		fw3.flush();
		fw3.close();
		fw4.flush();
		fw4.close();
		fw5.flush();
		fw5.close();
		fw6.flush();
		fw6.close();
		fw7.flush();
		fw7.close();
		fw8.flush();
		fw8.close();
		fw9.flush();
		fw9.close();
		fw10.flush();
		fw10.close();
		fw11.flush();
		fw11.close();
		fw12.flush();
		fw12.close();

		Jedis jedis = null;
		try {
			jedis = new Jedis("10.90.7.52", 6379);
			jedis.select(5);
			jedis.hmset(endDate + "-稿源统计数据", map1);
			jedis.hmset(endDate + "-低俗统计数据", map2);
			jedis.hmset(endDate + "-标题党统计数据", map3);
			jedis.hmset(endDate + "-文章类型统计数据", map4);
			jedis.hmset(endDate + "-地域统计数据", map5);
			jedis.hmset(endDate + "-时效性统计数据", map6);
			jedis.hmset(endDate + "-原创统计数据", map8);
			jedis.hmset(endDate + "-质量评级得分统计数据", map9);

		} catch (Exception ex) {
			LOG.error("[ERROR] In write item to Redis.", ex);
		} finally {
			jedis.close();
		}
	}

	public void output1(String endDate) throws IOException {
		HashMap<String, HashMap<String, Integer>> calAllMap = new HashMap<String, HashMap<String, Integer>>();// 分类统计数据
		FileWriter fw1 = null;
		fw1 = new FileWriter(defaultPath + endDate + "_sandc.txt", false);
		FileWriter fw2 = null;
		fw2 = new FileWriter(defaultPath + endDate + "_source.txt", false);
		fw1.write("source\tcategory\t质量评级过滤后数量\t排重后数量\t泛编出口数量\t推荐数据更新量\n");
		fw2.write("source\t原始抓取数量\t质量评级过滤后数量\t排重后数量\t泛编出口数量\t推荐数据更新量\t优质数据量\n");
		int addor = 0;
		int addod = 0;
		int addre = 0;
		int added = 0;
		int addla = 0;
		HashMap<String, String> map = new HashMap<String, String>();
		for (Entry<String, Data> entry : mappingDataMap.entrySet()) {
			int ornum = 0;
			int odnum = 0;
			int renum = 0;
			int ednum = 0;
			int lanum = 0;
			Data data = entry.getValue();
			HashMap<String, HashMap<String, Integer>> categoryMap = data.getCatesMap();
			List<Map.Entry<String, HashMap<String, Integer>>> sortedMap = MapSort.mapSort2(categoryMap);
			for (Entry<String, HashMap<String, Integer>> catentry : sortedMap) {
				addor = addor + catentry.getValue().get("ornum");
				addod = addod + catentry.getValue().get("odnum");
				addre = addre + catentry.getValue().get("renum");
				added = added + catentry.getValue().get("ednum");
				addla = addla + catentry.getValue().get("lanum");

				ornum = ornum + catentry.getValue().get("ornum");
				odnum = odnum + catentry.getValue().get("odnum");
				renum = renum + catentry.getValue().get("renum");
				ednum = ednum + catentry.getValue().get("ednum");
				lanum = lanum + catentry.getValue().get("lanum");
			}
			HashMap<String, Integer> calAllNumMap = new HashMap<String, Integer>();
			calAllNumMap.put("ornum", ornum);
			calAllNumMap.put("odnum", odnum);
			calAllNumMap.put("renum", renum);
			calAllNumMap.put("ednum", ednum);
			calAllNumMap.put("lanum", lanum);
			calAllMap.put(entry.getKey(), calAllNumMap);
		}
		int excellentSum = 0;
		HashMap<String, Integer> excellentNumMap = new HashMap<String, Integer>();
		excellentNumMap = Report2Throw.getExcellentNum(endDate);
		List<Map.Entry<String, HashMap<String, Integer>>> sortedCalAllMap = MapSort.mapSort2(calAllMap);
		for (Entry<String, HashMap<String, Integer>> entry : sortedCalAllMap) {

			String source = entry.getKey();
			int excellentNum = 0;
			if (excellentNumMap.containsKey(source)) {
				excellentNum = excellentNumMap.get(source);
			} else {
				excellentNum = 0;
			}
			excellentSum = excellentSum + excellentNum;
			map.put(source,
					entry.getValue().get("ornum") + "#" + entry.getValue().get("odnum") + "#"
							+ entry.getValue().get("renum") + "#" + entry.getValue().get("ednum") + "#"
							+ entry.getValue().get("lanum") + "#" + excellentNum);
			fw2.write(source + "\t" + entry.getValue().get("ornum") + "\t" + entry.getValue().get("odnum") + "\t"
					+ entry.getValue().get("renum") + "\t" + entry.getValue().get("ednum") + "\t"
					+ entry.getValue().get("lanum") + "\t" + excellentNum + "\n");

			Data data = mappingDataMap.get(source);
			HashMap<String, HashMap<String, Integer>> categoryMap = data.getCatesMap();
			List<Map.Entry<String, HashMap<String, Integer>>> sortedMap = MapSort.mapSort2(categoryMap);
			for (Entry<String, HashMap<String, Integer>> catentry : sortedMap) {
				fw1.write(entry.getKey() + "\t" + catentry.getKey() + "\t" + catentry.getValue().get("odnum") + "\t"
						+ catentry.getValue().get("renum") + "\t" + catentry.getValue().get("ednum") + "\t"
						+ catentry.getValue().get("lanum") + "\n");
			}
		}
		fw1.write("合计\t \t" + addod + "\t" + addre + "\t" + added + "\t" + addla + "\n");
		// int lostNum = Report2Throw.getAllThrowNum(endDate);
		// System.out.println(lostNum);
		// fw2.write("垃圾数据\t" + (originalId.size() + algorithmId.size() - addor)
		// + "\t0\t0\t0\t0\t0\n");
		fw2.write("合计\t" + (originalId.size() + algorithmId.size()) + "\t" + addod + "\t" + addre + "\t" + added + "\t"
				+ addla + "\t" + excellentSum + "\n");
		fw1.flush();
		fw1.close();
		fw2.flush();
		fw2.close();

		FileReader fr = null;
		fr = new FileReader(defaultPath + endDate + "_recommend.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		br.close();
		fr.close();
		FileReader fr1 = null;
		fr1 = new FileReader(defaultPath + endDate + "_offLineNum.txt");
		BufferedReader br1 = new BufferedReader(fr1);
		String line1 = br1.readLine();
		br1.close();
		fr1.close();
		FileReader fr2 = new FileReader(defaultPath + endDate + "_editNum.txt");
		BufferedReader br2 = new BufferedReader(fr2);
		String line2 = br2.readLine();
		br2.close();
		fr2.close();
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.90.7.52", 6379);
			jedis.select(5);
			jedis.hmset(endDate + "-来源统计数据", map);
			jedis.set(endDate + "-每日数据总量", (originalId.size() + algorithmId.size()) + "#" + addod + "#" + addre + "#"
					+ line2 + "#" + line1 + "#" + added + "#" + addla + "#" + excellentSum + "#" + line);
		} catch (Exception ex) {
			LOG.error("[ERROR] In write item to Redis.", ex);
		} finally {
			jedis.close();
		}
	}

	public static void main(String[] args) throws ParseException, IOException {

		// E:/Test/
		// 生成相应的统计文件保存在本地，例如 20170720_location.txt

		CalVarDailyData cal = new CalVarDailyData();
		String startDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 2 * 24 * 60 * 60) * 1000);
		String endDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 1 * 24 * 60 * 60) * 1000);
		// String startDate = args[0];
		// String endDate = args[1];

		cal.process(startDate, endDate);
		cal.calRecommendNum(endDate);
		cal.statisticsLocation();
		cal.getEditNum(endDate);
		cal.mappingData();
		cal.getRecommendNum(endDate);
		cal.output(endDate);
		cal.output1(endDate);

		// 统计分类的数据
		CalCategoryNum op = new CalCategoryNum();
		op.readFile(endDate);
		op.output(endDate);
		// 统计七天的数据
		Cal7DaysData ob = new Cal7DaysData();
		ob.process(endDate);

		SendDailyMail sdm = new SendDailyMail();
		sdm.process(endDate);

		SendLocationMail sendLocationMail = new SendLocationMail();
		sendLocationMail.process(endDate);

		SendMail send = new SendMail();
		send.process(endDate);

		SendVideoMail sendVideoMail = new SendVideoMail();
		sendVideoMail.process(endDate);

		SendSourceMail sendSourceMail = new SendSourceMail();
		sendSourceMail.process(endDate);

		LOG.info("[INFO] End");
		System.exit(0);
	}
}
