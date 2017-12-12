package calModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Hbase.HbaseOperation;
import dataBase.itemf;
import getExcellentNum.CheckArticleFrom;
import getExcellentNum.RedisUtil;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import utils.CMPPDataCollect;
import utils.FeatureExTools;
import utils.JsonFromCMPP;
import utils.LoadConfig;
import utils.MapSort;

public class CalDailyData {
	static Logger LOG = Logger.getLogger(CalDailyData.class);
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath1"); // 读取路径，配置文件conf/sys.properties
	private static final String toplocPath = LoadConfig.lookUpValueByKey("toplocPath");
	HashSet<String> originalId = new HashSet<String>();// 推荐表id
	HashSet<String> editId = new HashSet<String>();// 泛编出口id
	List<String> offLineId = new ArrayList<String>();// 下线id
	long recommendSum = 0;// 可推荐量
	long editNum = 0;// 进入泛编数量
	long offLineNum = 0;// 泛编下线数量
	HbaseOperation hbaseOperation = new HbaseOperation();
	HashMap<String, HashMap<String, Integer>> sourceMap = new HashMap<String, HashMap<String, Integer>>();// 来源
	HashMap<String, HashMap<String, Integer>> sourceRateMap = new HashMap<String, HashMap<String, Integer>>();// 稿源评级
	HashMap<String, HashMap<String, Integer>> qualityEvalLevelMap = new HashMap<String, HashMap<String, Integer>>();// 文章质量评级
	HashMap<String, HashMap<String, Integer>> categoryMap = new HashMap<String, HashMap<String, Integer>>();// 分类topc
	HashMap<String, HashMap<String, Integer>> topLocationMap = new HashMap<String, HashMap<String, Integer>>();
	List<String> topcList = new ArrayList<String>();
	List<String> topLocationList = new ArrayList<String>();

	// Jedis sourceRatejedis = new Jedis("10.80.9.143", 6379, 6000);

	/**
	 * 读取推荐表
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
				List<JsonFromCMPP> jList1 = null;
				try {
					jList1 = CMPPDataCollect.readJsonList(url1);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("[ERROR]GetJsonList error.", e);
				}
				if (jList1 != null) {
					for (JsonFromCMPP j : jList1) {
						originalId.add(j.getId());
					}
				} else
					LOG.info("[INFO] jList1 is null!");
				sTime = eTime;
				eTime = sTime + CMPPDataCollect.INTERVAL2;
			} else {
				break;
			}
		}
		LOG.info("[INFO] originalId size is " + originalId.size());
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
	 * topLocation
	 * 
	 * @throws IOException
	 */
	public void loadTocLocation() throws IOException {
		FileReader fr = null;
		fr = new FileReader(toplocPath);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		topLocationList = Arrays.asList(line.split(", "));
		br.close();
		fr.close();
	}

	/**
	 * 获取稿源的评级
	 * 
	 * @param source
	 * @return
	 */
	// public String getSourceRate(String source) {
	// String rate = null;
	// Jedis jedis = null;
	// int i = 0;
	// while (i < 10) {
	// try {
	// jedis = new Jedis("10.80.9.143", 6379, 6000);
	// jedis.select(8);
	// rate = jedis.get(source);
	// if (rate == null) {
	// rate = "other";
	// }
	// return rate;
	// } catch (Exception e) {
	// LOG.error("[ERROR] In check sourceRate Redis.", e);
	//
	// // return "null";
	// } finally {
	// jedis.close();
	// }
	// }
	// return "null";
	// }

	/**
	 * 判断是否为排重后的文章
	 * 
	 * @param id
	 * @return
	 */
	// public boolean checkRepeateRedis(String id) {
	// Jedis jedis = new Jedis("10.90.14.13", 6379);
	// try {
	// jedis.select(9);
	// return (jedis.exists(id));
	// } catch (Exception e) {
	// LOG.error("[ERROR] In check repeat Redis.", e);
	// return false;
	// } finally {
	// jedis.close();
	// }
	// }

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
	 * 判断是否为下线文章
	 * 
	 * @param id
	 * @return
	 */
	public boolean checkOffLine(String id) {
		return (offLineId.contains(id));
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
		for (int i = 0; i < list.size(); i++) {
			editNum += list.get(i);
		}
	}

	/**
	 * 下线id
	 * 
	 * @param endDate
	 * @return
	 */
	public List<String> getOffLineId(String endDate) {
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
	 * 可推荐文章量
	 */
	public void getRecommendNum() {
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.80.9.143", 6379, 6000);
			jedis.select(0);
			recommendSum = jedis.smembers("availableDocIds").size();
		} catch (Exception e) {
			recommendSum = 0;
			LOG.error("[ERROR]get recommendSum error", e);
		} finally {
			jedis.close();
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
		getOffLineId(endDate);
		getEditNum(endDate);
		loadTocLocation();
		getRecommendNum();

		// IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
		Jedis sourceRatejedis = RedisUtil.getJedisDbClient("10.80.9.143", 6379, 8);
		Jedis repeatejedis = RedisUtil.getJedisDbClient("10.90.14.13", 6379, 9);

		int num = 0;
		List<String> keys = new ArrayList<String>();
		// HbaseOperation hbaseOperation = new HbaseOperation();
		for (String id : originalId) {
			num++;
			keys.add("cmpp_" + id);
			if (num % 2000 == 0 || num == originalId.size()) {
				LOG.info("[INFO] " + num);
				Map<String, itemf> ItemMap = hbaseOperation.gets(keys);
				keys = new ArrayList<String>();
				for (Map.Entry<String, itemf> entry : ItemMap.entrySet()) {
					itemf item = entry.getValue();
					String articleId = entry.getKey().split("_")[1];

					String rate = null;
					String source = null;// 稿源
					String source1 = null;// 来源
					String otherStr = null;
					String qualityEvalLevel = null;
					String category = null;
					String location = null;
					List<String> locList = new ArrayList<String>();
					boolean odnum = true;
					boolean renum = repeatejedis.exists(articleId);
					boolean ednum = checkEditSet(articleId);
					boolean lanum = checkSolr(articleId);
					boolean olnum = checkOffLine(articleId);
					if (item == null) {
						rate = "null";
						qualityEvalLevel = "null";
						source1 = "null";
						category = "null";
						location = "item#null";
					} else {
						locList = item.getLocList();
						if (locList == null || locList.isEmpty()) {
							location = "null";
						}
						ArrayList<String> categoryList = FeatureExTools.whatCategory(item.getFeatures());
						if (categoryList == null || categoryList.isEmpty()) {
							category = "null";
						} else
							category = categoryList.get(0);

						otherStr = item.getOther();
						source1 = CheckArticleFrom.checkFromByOther(otherStr);

						// 稿源评级
						source = item.getSource();// 稿源
						if (source == null || source.isEmpty()) {
							source = "null";
							rate = "null";
						} else {
							rate = sourceRatejedis.get(source);
							if (rate == null) {
								rate = "other";
							}
						}
						// 文章质量评级得分
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
					}

					// 地域
					if ("null".equals(location) || "item#null".equals(location)) {
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
							if (olnum) {
								numMap.put("olnum", numMap.get("olnum") + 1);
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
							if (olnum) {
								numMap.put("olnum", 1);
							} else {
								numMap.put("olnum", 0);
							}
							topLocationMap.put(location, numMap);
						}
					} else {
						HashSet<String> locSet = new HashSet<String>();
						for (int i = 0; i < locList.size(); i++) {
							String str = locList.get(i);
							JSONObject jo = JSONObject.fromObject(str);
							if (Double.valueOf(jo.getString("weight")) >= 0.5 && jo.getString("loc") != null
									&& !(jo.getString("loc").isEmpty())) {
								location = jo.getString("loc");
							} else {
								continue;
							}
							if (topLocationList.contains(location.split("->")[0])) {
								locSet.add(location.split("->")[0]);
							}
						}
						for (String topLocation : locSet) {
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
								if (olnum) {
									numMap.put("olnum", numMap.get("olnum") + 1);
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
								if (olnum) {
									numMap.put("olnum", 1);
								} else {
									numMap.put("olnum", 0);
								}
								topLocationMap.put(topLocation, numMap);
							}
						}
					}
					// 分类
					if (categoryMap.containsKey(category)) {
						HashMap<String, Integer> numMap = categoryMap.get(category);
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
						if (olnum) {
							numMap.put("olnum", numMap.get("olnum") + 1);
						}
						categoryMap.put(category, numMap);
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
						if (olnum) {
							numMap.put("olnum", 1);
						} else {
							numMap.put("olnum", 0);
						}
						categoryMap.put(category, numMap);
					}
					// 来源
					if (sourceMap.containsKey(source1)) {
						HashMap<String, Integer> numMap = sourceMap.get(source1);
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
						if (olnum) {
							numMap.put("olnum", numMap.get("olnum") + 1);
						}
						sourceMap.put(source1, numMap);
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
						if (olnum) {
							numMap.put("olnum", 1);
						} else {
							numMap.put("olnum", 0);
						}
						sourceMap.put(source1, numMap);
					}

					if (sourceRateMap.containsKey(rate)) {
						HashMap<String, Integer> numMap = sourceRateMap.get(rate);
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
						if (olnum) {
							numMap.put("olnum", numMap.get("olnum") + 1);
						}
						sourceRateMap.put(rate, numMap);
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
						if (olnum) {
							numMap.put("olnum", 1);
						} else {
							numMap.put("olnum", 0);
						}
						sourceRateMap.put(rate, numMap);
					}

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
						if (olnum) {
							numMap.put("olnum", numMap.get("olnum") + 1);
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
						if (olnum) {
							numMap.put("olnum", 1);
						} else {
							numMap.put("olnum", 0);
						}
						qualityEvalLevelMap.put(qualityEvalLevel, numMap);
					}
				}
			}
		}
		hbaseOperation.close();
		sourceRatejedis.close();
		repeatejedis.close();

	}

	public void output(String endDate) throws IOException {
		LOG.info("[INFO] start to write.");
		FileWriter fw1 = new FileWriter(defaultPath + endDate + "_sum.txt", false);
		FileWriter fw2 = new FileWriter(defaultPath + endDate + "_category.txt", false);
		FileWriter fw3 = new FileWriter(defaultPath + endDate + "_source.txt", false);
		FileWriter fw4 = new FileWriter(defaultPath + endDate + "_qualityEvalLevel.txt", false);
		FileWriter fw5 = new FileWriter(defaultPath + endDate + "_sourceRate.txt", false);
		FileWriter fw6 = new FileWriter(defaultPath + endDate + "_topLocation.txt", false);
		fw1.write("全量\t原始抓取数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t可推荐量\n");
		fw2.write("分类\t原始抓取数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t可推荐量\n");
		fw3.write("来源\t原始抓取数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t可推荐量\n");
		fw4.write("质量评级得分\t原始抓取数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t可推荐量\n");
		fw5.write("稿源评级\t原始抓取数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t可推荐量\n");
		fw6.write("地域\t原始抓取数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t可推荐量\n");

		int addod = 0;
		int addre = 0;
		int added = 0;
		int addla = 0;
		int addol = 0;

		for (Map.Entry<String, HashMap<String, Integer>> entry : sourceMap.entrySet()) {
			addod = addod + entry.getValue().get("odnum");
			addre = addre + entry.getValue().get("renum");
			added = added + entry.getValue().get("ednum");
			addla = addla + entry.getValue().get("lanum");
			addol = addol + entry.getValue().get("olnum");
		}
		fw1.write("全量" + "\t" + addod + "\t" + addre + "\t" + editNum + "\t" + addol + "\t" + added + "\t" + addla
				+ "\t" + recommendSum + "\n");
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap = MapSort.mapSortList(sourceMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap) {

			fw3.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + "0\t" + entry.getValue().get("olnum") + "\t" + entry.getValue().get("ednum") + "\t"
					+ entry.getValue().get("lanum") + "\t0\n");
		}
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap2 = MapSort.mapSortList(categoryMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap2) {

			fw2.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + "0\t" + entry.getValue().get("olnum") + "\t" + entry.getValue().get("ednum") + "\t"
					+ entry.getValue().get("lanum") + "\t0\n");
		}
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap3 = MapSort.mapSort(qualityEvalLevelMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap3) {

			fw4.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + "0\t" + entry.getValue().get("olnum") + "\t" + entry.getValue().get("ednum") + "\t"
					+ entry.getValue().get("lanum") + "\t0\n");
		}
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap4 = MapSort.mapSort(sourceRateMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap4) {

			fw5.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + "0\t" + entry.getValue().get("olnum") + "\t" + entry.getValue().get("ednum") + "\t"
					+ entry.getValue().get("lanum") + "\t0\n");
		}
		int num1 = 0;
		int num2 = 0;
		int num3 = 0;
		int num4 = 0;
		int num5 = 0;
		List<Map.Entry<String, HashMap<String, Integer>>> sortedMap5 = MapSort.mapSortList(topLocationMap);
		for (Map.Entry<String, HashMap<String, Integer>> entry : sortedMap5) {
			if ("null".equals(entry.getKey())) {
				num1 = entry.getValue().get("odnum");
				num2 = entry.getValue().get("renum");
				num3 = entry.getValue().get("olnum");
				num4 = entry.getValue().get("ednum");
				num5 = entry.getValue().get("lanum");
				continue;
			}

			fw6.write(entry.getKey() + "\t" + entry.getValue().get("odnum") + "\t" + entry.getValue().get("renum")
					+ "\t" + "0\t" + entry.getValue().get("olnum") + "\t" + entry.getValue().get("ednum") + "\t"
					+ entry.getValue().get("lanum") + "\t0\n");
		}
		fw6.write("全量" + "\t" + (addod - num1) + "\t" + (addre - num2) + "\t" + "0\t" + (addol - num3) + "\t"
				+ (added - num4) + "\t" + (addla - num5) + "\t0\n");
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
		fw6.flush();
		fw6.close();
		LOG.info("[INFO] end");
	}

	public static void main(String[] args) throws ParseException, IOException {
		CalDailyData calDailyData = new CalDailyData();
		String startDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 2 * 24 * 60 * 60) * 1000);
		String endDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 1 * 24 * 60 * 60) * 1000);
		// String startDate = "20171206";
		// String endDate = "20171207";
		calDailyData.process(startDate, endDate);
		calDailyData.output(endDate);
		System.exit(0);
	}

}
