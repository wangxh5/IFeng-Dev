package calModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import getExcellentNum.RedisUtil;
import redis.clients.jedis.Jedis;
import utils.CMPPDataCollect;
import utils.JsonFromCMPP;
import utils.LoadConfig;
import utils.MapSort;

public class CalSourceRate {

	String borderColor = "FFFFFF";
	String titleColor = "d1d9fa";
	String color = "f7f6f6";
	static Logger LOG = Logger.getLogger(CalSourceRate.class);
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
	private String sourceRatePath = LoadConfig.lookUpValueByKey("sourceRatePath");
	private String expoPath = LoadConfig.lookUpValueByKey("expoPath");
	private String opercountPath = LoadConfig.lookUpValueByKey("opercountPath");
	private String operdisPath = LoadConfig.lookUpValueByKey("operdisPath");
	IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
	private String pvPath = LoadConfig.lookUpValueByKey("pvPath");
	int expoSum = 0;
	// 稿源分级
	long updateRecommendTotal = 0;
	HashMap<String, Long> sourceRateMap = new HashMap<String, Long>();
	HashMap<String, Long> expoMap = new HashMap<String, Long>();// 曝光
	HashMap<String, Long> pvMap = new HashMap<String, Long>();// 点击
	HashMap<String, Long> shareMap = new HashMap<String, Long>();
	HashMap<String, Long> storeMap = new HashMap<String, Long>();//
	HashMap<String, Long> commentMap = new HashMap<String, Long>();//
	HashMap<String, Long> recommendMap = new HashMap<String, Long>();
	HashMap<String, Long> updateRecommendMap = new HashMap<String, Long>();
	// 统计推荐更新和曝光
	HashSet<String> originalId = new HashSet<String>();
	// List<String> recommendUpdateId = new ArrayList<String>();
	// List<String> recommendExpoId = new ArrayList<String>();

	// 质量评级得分
	HashMap<String, Long> qualityEvalLevelMap = new HashMap<String, Long>();
	HashMap<String, Long> expoMap1 = new HashMap<String, Long>();// 曝光
	HashMap<String, Long> pvMap1 = new HashMap<String, Long>();// 点击
	HashMap<String, Long> shareMap1 = new HashMap<String, Long>();
	HashMap<String, Long> storeMap1 = new HashMap<String, Long>();//
	HashMap<String, Long> commentMap1 = new HashMap<String, Long>();//
	HashMap<String, Long> recommendMap1 = new HashMap<String, Long>();
	HashMap<String, Long> updateRecommendMap1 = new HashMap<String, Long>();

	// 时效性
	HashMap<String, Long> timeSensitiveMap = new HashMap<String, Long>();
	HashMap<String, Long> expoMap2 = new HashMap<String, Long>();// 曝光
	HashMap<String, Long> pvMap2 = new HashMap<String, Long>();// 点击
	HashMap<String, Long> shareMap2 = new HashMap<String, Long>();
	HashMap<String, Long> storeMap2 = new HashMap<String, Long>();//
	HashMap<String, Long> commentMap2 = new HashMap<String, Long>();//
	HashMap<String, Long> recommendMap2 = new HashMap<String, Long>();
	HashMap<String, Long> updateRecommendMap2 = new HashMap<String, Long>();

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
		LOG.info("originalId size is " + originalId.size());
	}

	// public String getSourceRate(String source) {
	// String rate = null;
	// Jedis jedis = new Jedis("10.80.9.143", 6379, 6000);
	// try {
	// jedis.select(8);
	// rate = jedis.get(source);
	// if (rate == null) {
	// rate = "other";
	// }
	// return rate;
	// } catch (Exception e) {
	// LOG.error("[ERROR] In check sourceRate Redis.", e);
	// return "null";
	// } finally {
	// jedis.close();
	// }
	// }

	public void CalData(String endDate, String startDate, String lastOneDate) throws IOException, ParseException {
		long sTime = CalTools.getTimeStamp(startDate + " 23:00:00");
		long terminalTime = CalTools.getTimeStamp(endDate + " 23:00:00");
		LOG.info("[INFO] start date is " + startDate + " ,end date is " + endDate);
		readOriginalData(sTime, terminalTime);
		int recommendSum = 0;
		int recommendTodaySum = 0;
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);
		BufferedReader br = new BufferedReader(new FileReader(pvPath + "pv_expo_" + endDate));
		BufferedReader br1 = new BufferedReader(new FileReader(pvPath + "pv_expo_" + startDate));
		BufferedReader br2 = new BufferedReader(new FileReader(pvPath + "pv_expo_" + lastOneDate));
		FileWriter fileWriter = new FileWriter(sourceRatePath + endDate + "_recommendId#not#today.txt", false);
		String line = null;
		HashSet<String> Id1 = new HashSet<String>();
		HashSet<String> Id2 = new HashSet<String>();
		HashSet<String> Id3 = new HashSet<String>();
		while ((line = br1.readLine()) != null) {
			Id1.add(line.split("\t")[0]);
		}
		while ((line = br2.readLine()) != null) {
			Id2.add(line.split("\t")[0]);
		}

		while ((line = br.readLine()) != null) {
			if (originalId.contains(line.split("\t")[0])) {
				recommendSum++;
				if (Id1.contains(line.split("\t")[0]) || Id2.contains(line.split("\t")[0])) {
					Id3.add(line.split("\t")[0]);
				}
			}
		}
		recommendTodaySum = recommendSum - Id3.size();
		fileWriter.write(Id3.toString() + "\n");
		fileWriter.write("当日数据：" + recommendSum + "\n");
		fileWriter.write("当日新增数据：" + recommendTodaySum + "\n");
		fileWriter.write("当日新增的曝光比：" + nt.format(recommendTodaySum * 1.0 / recommendSum) + "\n");
		LOG.info("[info]当日数据：" + recommendSum);
		LOG.info("[info]当日新增数据：" + recommendTodaySum);
		LOG.info("[info]当日新增的曝光比：" + nt.format(recommendTodaySum * 1.0 / recommendSum));
		br.close();
		br1.close();
		br2.close();
		fileWriter.flush();
		fileWriter.close();
	}

	public void calProcess(String startDate, String endDate) throws ParseException {
		long sTime = CalTools.getTimeStamp(startDate + " 23:00:00");
		long terminalTime = CalTools.getTimeStamp(endDate + " 23:00:00");
		LOG.info("[INFO] start date is " + startDate + " ,end date is " + endDate);
		readOriginalData(sTime, terminalTime);
		Jedis sourceRatejedis = RedisUtil.getJedisDbClient("10.80.9.143", 6379, 8);
		int count = 0;
		for (String id : originalId) {
			count++;
			if (count % 2000 == 0) {
				LOG.info("process ids:" + count);
			}
			itemf item = ikvop.queryItemF("cmpp_" + id);
			String rate = null;
			String timeSensitive = null;
			String source = null;// 稿源
			String qualityEvalLevel = null;
			if (item == null) {
				rate = "null";
				timeSensitive = "null";
				qualityEvalLevel = "null";
			} else {
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
				timeSensitive = item.getTimeSensitive();
				if (timeSensitive == null || timeSensitive.isEmpty()) {
					timeSensitive = "null";
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

			if (sourceRateMap.containsKey(rate)) {
				sourceRateMap.put(rate, sourceRateMap.get(rate) + 1);
			} else {
				sourceRateMap.put(rate, (long) 1);
			}

			if (qualityEvalLevelMap.containsKey(qualityEvalLevel)) {
				qualityEvalLevelMap.put(qualityEvalLevel, qualityEvalLevelMap.get(qualityEvalLevel) + 1);
			} else {
				qualityEvalLevelMap.put(qualityEvalLevel, (long) 1);
			}

			if (timeSensitiveMap.containsKey(timeSensitive)) {
				timeSensitiveMap.put(timeSensitive, timeSensitiveMap.get(timeSensitive) + 1);
			} else {
				timeSensitiveMap.put(timeSensitive, (long) 1);
			}

		}

		sourceRatejedis.close();
	}

	public void process(String startDate, String endDate) throws IOException, ParseException {
		FileWriter fw2 = new FileWriter(sourceRatePath + endDate + "_userLogId#item#null.txt", false);
		FileWriter fw7 = new FileWriter(sourceRatePath + endDate + "_recommendExpoId.txt", false);

		BufferedReader br1 = new BufferedReader(new FileReader(pvPath + "pv_expo_" + endDate));
		long sTime = CalTools.getTimeStamp(startDate + " 23:00:00");
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);
		int total = 27185 + 28954 + 165360 + 2342 + 51412;// 源数据
		String rateA = nt.format(27185.0 / total);
		String rateB = nt.format(28954.0 / total);
		String rateC = nt.format(165360.0 / total);
		String rateD = nt.format(2342.0 / total);
		String rateE = nt.format(51412.0 / total);

		String line = null;

		long pvTotal = 0;
		long storeTotal = 0;
		long shareTotal = 0;
		long commentTotal = 0;
		long expoTotal = 0;
		long recommendTotal = 0;

		long pvTotal1 = 0;
		long storeTotal1 = 0;
		long shareTotal1 = 0;
		long commentTotal1 = 0;
		long expoTotal1 = 0;

		long pvTotal2 = 0;
		long storeTotal2 = 0;
		long shareTotal2 = 0;
		long commentTotal2 = 0;
		long expoTotal2 = 0;
		// Jedis jedis = null;
		// try {
		// jedis = new Jedis("10.80.8.143", 6379, 5000);
		// jedis.select(2);
		// } catch (Exception ex) {
		// LOG.error("[ERROR] get 'item is null' redis", ex);
		// }
		Jedis sourceRatejedis = RedisUtil.getJedisDbClient("10.80.9.143", 6379, 8);
		int lintcount = 0;
		while ((line = br1.readLine()) != null) {
			lintcount++;
			if (lintcount % 2000 == 0) {
				LOG.info("process line :" + lintcount);
			}
			expoSum++;
			String source = null;
			String rate = null;
			itemf item = null;
			String id = null;
			String qualityEvalLevel = null;
			String timeSensitive = null;
			if (line.split("\t")[0] == null || line.split("\t")[0].isEmpty()) {
				continue;
			} else if (line.split("\t")[0].startsWith("sub")) {
				item = ikvop.queryItemF(line.split("\t")[0]);
			} else if (line.split("\t")[0].length() > 8) {
				item = ikvop.queryItemF(line.split("\t")[0]);
			} else {
				item = ikvop.queryItemF("cmpp_" + line.split("\t")[0]);
			}

			if (item == null) {
				LOG.info("[INFO] item is null,id :" + line.split("\t")[0]);
				fw2.write(line.split("\t")[0] + "\n");
				source = "null";
				rate = "null";
				qualityEvalLevel = "null";
				timeSensitive = "null";
			} else {
				source = item.getSource();
				if (source == null || source.isEmpty()) {
					rate = "null";
				} else {
					rate = sourceRatejedis.get(source);
					if (rate == null) {
						rate = "other";
					}
				}
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
				timeSensitive = item.getTimeSensitive();
				if (timeSensitive == null || timeSensitive.isEmpty()) {
					timeSensitive = "null";
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

				if (item.getID().contains("cmpp")) {
					id = item.getID().split("_")[1];
				} else {
					id = item.getID();
				}
			}

			if (originalId.contains(id)) {
				fw7.write(id + "\n");
				updateRecommendTotal++;
				if (updateRecommendMap.containsKey(rate)) {
					updateRecommendMap.put(rate, updateRecommendMap.get(rate) + 1);
				} else {
					updateRecommendMap.put(rate, (long) 1);
				}

				if (updateRecommendMap1.containsKey(qualityEvalLevel)) {
					updateRecommendMap1.put(qualityEvalLevel, updateRecommendMap1.get(qualityEvalLevel) + 1);
				} else {
					updateRecommendMap1.put(qualityEvalLevel, (long) 1);
				}

				if (updateRecommendMap2.containsKey(timeSensitive)) {
					updateRecommendMap2.put(timeSensitive, updateRecommendMap2.get(timeSensitive) + 1);
				} else {
					updateRecommendMap2.put(timeSensitive, (long) 1);
				}
			}

			if (recommendMap.containsKey(rate)) {
				recommendTotal++;
				recommendMap.put(rate, recommendMap.get(rate) + 1);
			} else {
				recommendTotal++;
				recommendMap.put(rate, (long) 1);
			}

			if (pvMap.containsKey(rate)) {
				pvTotal += Long.valueOf(line.split("\t")[1]);
				pvMap.put(rate, pvMap.get(rate) + Long.valueOf(line.split("\t")[1]));
			} else {
				pvTotal += Long.valueOf(line.split("\t")[1]);
				pvMap.put(rate, Long.valueOf(line.split("\t")[1]));
			}

			if (expoMap.containsKey(rate)) {
				expoTotal += Long.valueOf(line.split("\t")[7]);
				expoMap.put(rate, expoMap.get(rate) + Long.valueOf(line.split("\t")[7]));
			} else {
				expoTotal += Long.valueOf(line.split("\t")[7]);
				expoMap.put(rate, Long.valueOf(line.split("\t")[7]));
			}

			if (storeMap.containsKey(rate)) {
				storeTotal += Long.valueOf(line.split("\t")[4]);
				storeMap.put(rate, storeMap.get(rate) + Long.valueOf(line.split("\t")[4]));
			} else {
				storeTotal += Long.valueOf(line.split("\t")[4]);
				storeMap.put(rate, Long.valueOf(line.split("\t")[4]));
			}

			if (shareMap.containsKey(rate)) {
				shareTotal += Long.valueOf(line.split("\t")[5]);
				shareMap.put(rate, shareMap.get(rate) + Long.valueOf(line.split("\t")[5]));
			} else {
				shareTotal += Long.valueOf(line.split("\t")[5]);
				shareMap.put(rate, Long.valueOf(line.split("\t")[5]));
			}

			if (commentMap.containsKey(rate)) {
				commentTotal += Long.valueOf(line.split("\t")[6]);
				commentMap.put(rate, commentMap.get(rate) + Long.valueOf(line.split("\t")[6]));
			} else {
				commentTotal += Long.valueOf(line.split("\t")[6]);
				commentMap.put(rate, Long.valueOf(line.split("\t")[6]));
			}

			if (recommendMap1.containsKey(qualityEvalLevel)) {
				recommendMap1.put(qualityEvalLevel, recommendMap1.get(qualityEvalLevel) + 1);
			} else {
				recommendMap1.put(qualityEvalLevel, (long) 1);
			}

			if (pvMap1.containsKey(qualityEvalLevel)) {
				pvTotal1 += Long.valueOf(line.split("\t")[1]);
				pvMap1.put(qualityEvalLevel, pvMap1.get(qualityEvalLevel) + Long.valueOf(line.split("\t")[1]));
			} else {
				pvTotal1 += Long.valueOf(line.split("\t")[1]);
				pvMap1.put(qualityEvalLevel, Long.valueOf(line.split("\t")[1]));
			}

			if (expoMap1.containsKey(qualityEvalLevel)) {
				expoTotal1 += Long.valueOf(line.split("\t")[7]);
				expoMap1.put(qualityEvalLevel, expoMap1.get(qualityEvalLevel) + Long.valueOf(line.split("\t")[7]));
			} else {
				expoTotal1 += Long.valueOf(line.split("\t")[7]);
				expoMap1.put(qualityEvalLevel, Long.valueOf(line.split("\t")[7]));
			}

			if (storeMap1.containsKey(qualityEvalLevel)) {
				storeTotal1 += Long.valueOf(line.split("\t")[4]);
				storeMap1.put(qualityEvalLevel, storeMap1.get(qualityEvalLevel) + Long.valueOf(line.split("\t")[4]));
			} else {
				storeTotal1 += Long.valueOf(line.split("\t")[4]);
				storeMap1.put(qualityEvalLevel, Long.valueOf(line.split("\t")[4]));
			}

			if (shareMap1.containsKey(qualityEvalLevel)) {
				shareTotal1 += Long.valueOf(line.split("\t")[5]);
				shareMap1.put(qualityEvalLevel, shareMap1.get(qualityEvalLevel) + Long.valueOf(line.split("\t")[5]));
			} else {
				shareTotal1 += Long.valueOf(line.split("\t")[5]);
				shareMap1.put(qualityEvalLevel, Long.valueOf(line.split("\t")[5]));
			}

			if (commentMap1.containsKey(qualityEvalLevel)) {
				commentTotal1 += Long.valueOf(line.split("\t")[6]);
				commentMap1.put(qualityEvalLevel,
						commentMap1.get(qualityEvalLevel) + Long.valueOf(line.split("\t")[6]));
			} else {
				commentTotal1 += Long.valueOf(line.split("\t")[6]);
				commentMap1.put(qualityEvalLevel, Long.valueOf(line.split("\t")[6]));
			}

			if (recommendMap2.containsKey(timeSensitive)) {
				recommendMap2.put(timeSensitive, recommendMap2.get(timeSensitive) + 1);
			} else {
				recommendMap2.put(timeSensitive, (long) 1);
			}

			if (pvMap2.containsKey(timeSensitive)) {
				pvTotal2 += Long.valueOf(line.split("\t")[1]);
				pvMap2.put(timeSensitive, pvMap2.get(timeSensitive) + Long.valueOf(line.split("\t")[1]));
			} else {
				pvTotal2 += Long.valueOf(line.split("\t")[1]);
				pvMap2.put(timeSensitive, Long.valueOf(line.split("\t")[1]));
			}

			if (expoMap2.containsKey(timeSensitive)) {
				expoTotal2 += Long.valueOf(line.split("\t")[7]);
				expoMap2.put(timeSensitive, expoMap2.get(timeSensitive) + Long.valueOf(line.split("\t")[7]));
			} else {
				expoTotal2 += Long.valueOf(line.split("\t")[7]);
				expoMap2.put(timeSensitive, Long.valueOf(line.split("\t")[7]));
			}

			if (storeMap2.containsKey(timeSensitive)) {
				storeTotal2 += Long.valueOf(line.split("\t")[4]);
				storeMap2.put(timeSensitive, storeMap2.get(timeSensitive) + Long.valueOf(line.split("\t")[4]));
			} else {
				storeTotal2 += Long.valueOf(line.split("\t")[4]);
				storeMap2.put(timeSensitive, Long.valueOf(line.split("\t")[4]));
			}

			if (shareMap2.containsKey(timeSensitive)) {
				shareTotal2 += Long.valueOf(line.split("\t")[5]);
				shareMap2.put(timeSensitive, shareMap2.get(timeSensitive) + Long.valueOf(line.split("\t")[5]));
			} else {
				shareTotal2 += Long.valueOf(line.split("\t")[5]);
				shareMap2.put(timeSensitive, Long.valueOf(line.split("\t")[5]));
			}

			if (commentMap2.containsKey(timeSensitive)) {
				commentTotal2 += Long.valueOf(line.split("\t")[6]);
				commentMap2.put(timeSensitive, commentMap2.get(timeSensitive) + Long.valueOf(line.split("\t")[6]));
			} else {
				commentTotal2 += Long.valueOf(line.split("\t")[6]);
				commentMap2.put(timeSensitive, Long.valueOf(line.split("\t")[6]));
			}

		}

		ikvop.close();

		FileWriter fw = new FileWriter(sourceRatePath + endDate + "_sourceRatePercentData.txt", false);// 稿源
		FileWriter fw1 = new FileWriter(sourceRatePath + endDate + "_sourceRateData.txt", false);// 稿源
		FileWriter fw4 = new FileWriter(sourceRatePath + endDate + "_qualityEvalLevelPercentData.txt", false);// 质量评级
		FileWriter fw3 = new FileWriter(sourceRatePath + endDate + "_qualityEvalLevelData.txt", false);// 质量评级

		FileWriter fw6 = new FileWriter(sourceRatePath + endDate + "_timeSensitivePercentData.txt", false);// 时效性
		FileWriter fw5 = new FileWriter(sourceRatePath + endDate + "_timeSensitiveData.txt", false);// 时效性
		LOG.info("start to write");
		fw.write(" \tA级\tB级\tC级\tD级\tE级\tnull\tother级\n");
		fw.write("源数据\t" + rateA + "\t" + rateB + "\t" + rateC + "\t" + rateD + "\t" + rateE + "\n");
		// 27185 + 28954 + 165360 + 2342 + 51412;
		fw1.write(" \tA级\tB级\tC级\tD级\tE级\tnull\tother级\n");
		fw1.write("源数据\t" + 27185 + "\t" + 28954 + "\t" + 165360 + "\t" + 2342 + "\t" + 51412 + "\n");
		fw3.write(" ");
		fw4.write(" ");

		fw5.write(" ");
		fw6.write(" ");
		if (!(qualityEvalLevelMap.containsKey("null"))) {
			qualityEvalLevelMap.put("null", (long) 0);
		}
		LOG.info(qualityEvalLevelMap);
		List<Map.Entry<String, Long>> list1 = MapSort.Sort(qualityEvalLevelMap);
		for (Entry<String, Long> entry : list1) {
			fw3.write("\t" + entry.getKey());
			fw4.write("\t" + entry.getKey());
		}
		if (!(timeSensitiveMap.containsKey("null"))) {
			timeSensitiveMap.put("null", (long) 0);
		}
		if (!(timeSensitiveMap.containsKey("other"))) {
			timeSensitiveMap.put("other", (long) 0);
		}
		LOG.info(timeSensitiveMap);
		List<Map.Entry<String, Long>> list = MapSort.Sort(timeSensitiveMap);
		for (Entry<String, Long> entry : list) {
			fw5.write("\t" + entry.getKey());
			fw6.write("\t" + entry.getKey());
		}

		fw1.write("文章生产量");
		fw.write("文章生产量");
		fw3.write("\n文章生产量");
		fw4.write("\n文章生产量");

		fw5.write("\n文章生产量");
		fw6.write("\n文章生产量");
		if (!(sourceRateMap.containsKey("null"))) {
			sourceRateMap.put("null", (long) 0);
		}
		if (!(sourceRateMap.containsKey("other"))) {
			sourceRateMap.put("other", (long) 0);
		}
		LOG.info(sourceRateMap);
		List<Map.Entry<String, Long>> sourceRateList = MapSort.Sort(sourceRateMap);
		for (Entry<String, Long> entry : sourceRateList) {
			fw1.write("\t" + entry.getValue());
			LOG.info(entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / originalId.size()));
		}

		LOG.info(qualityEvalLevelMap);
		List<Map.Entry<String, Long>> qualityEvalLevelList = MapSort.Sort(qualityEvalLevelMap);
		for (Entry<String, Long> entry : qualityEvalLevelList) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / originalId.size()));
		}

		LOG.info(timeSensitiveMap);
		List<Map.Entry<String, Long>> timeSensitiveList = MapSort.Sort(timeSensitiveMap);
		for (Entry<String, Long> entry : timeSensitiveList) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / originalId.size()));
		}

		fw1.write("\n更新数据推荐");
		fw.write("\n更新数据推荐");

		fw3.write("\n更新数据推荐");
		fw4.write("\n更新数据推荐");

		fw5.write("\n更新数据推荐");
		fw6.write("\n更新数据推荐");
		if (!updateRecommendMap.containsKey("null")) {
			updateRecommendMap.put("null", (long) 0);
		}
		if (!updateRecommendMap.containsKey("other")) {
			updateRecommendMap.put("other", (long) 0);
		}
		LOG.info(updateRecommendMap);
		List<Map.Entry<String, Long>> updateRateList = MapSort.Sort(updateRecommendMap);
		for (Entry<String, Long> entry : updateRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / updateRecommendTotal));
		}
		if (!updateRecommendMap1.containsKey("null")) {
			updateRecommendMap1.put("null", (long) 0);
		}
		LOG.info(updateRecommendMap1);
		List<Map.Entry<String, Long>> updateList1 = MapSort.Sort(updateRecommendMap1);
		for (Entry<String, Long> entry : updateList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / updateRecommendTotal));
		}
		if (!updateRecommendMap2.containsKey("null")) {
			updateRecommendMap2.put("null", (long) 0);
		}
		if (!updateRecommendMap2.containsKey("other")) {
			updateRecommendMap2.put("other", (long) 0);
		}
		LOG.info(updateRecommendMap2);
		List<Map.Entry<String, Long>> updateList2 = MapSort.Sort(updateRecommendMap2);
		for (Entry<String, Long> entry : updateList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / updateRecommendTotal));
		}

		fw.write("\n" + "推荐数据分级");
		fw1.write("\n" + "推荐数据分级");

		fw3.write("\n" + "推荐数据分级");
		fw4.write("\n" + "推荐数据分级");

		fw5.write("\n" + "推荐数据分级");
		fw6.write("\n" + "推荐数据分级");
		if (!recommendMap.containsKey("null")) {
			recommendMap.put("null", (long) 0);
		}
		if (!recommendMap.containsKey("other")) {
			recommendMap.put("other", (long) 0);
		}
		LOG.info(recommendMap);
		List<Map.Entry<String, Long>> recommendRateList = MapSort.Sort(recommendMap);
		for (Entry<String, Long> entry : recommendRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / recommendTotal));
		}
		if (!recommendMap1.containsKey("null")) {
			recommendMap1.put("null", (long) 0);
		}
		LOG.info(recommendMap1);
		List<Map.Entry<String, Long>> recommendList1 = MapSort.Sort(recommendMap1);
		for (Entry<String, Long> entry : recommendList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / recommendTotal));
		}
		if (!recommendMap2.containsKey("null")) {
			recommendMap2.put("null", (long) 0);
		}
		if (!recommendMap2.containsKey("other")) {
			recommendMap2.put("other", (long) 0);
		}
		LOG.info(recommendMap2);
		List<Map.Entry<String, Long>> recommendList2 = MapSort.Sort(recommendMap2);
		for (Entry<String, Long> entry : recommendList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / recommendTotal));
		}

		fw.write("\n" + "曝光分布");
		fw1.write("\n" + "曝光分布");

		fw3.write("\n" + "曝光分布");
		fw4.write("\n" + "曝光分布");

		fw5.write("\n" + "曝光分布");
		fw6.write("\n" + "曝光分布");
		if (!expoMap.containsKey("null")) {
			expoMap.put("null", (long) 0);
		}
		if (!expoMap.containsKey("other")) {
			expoMap.put("other", (long) 0);
		}
		LOG.info(expoMap);
		List<Map.Entry<String, Long>> expoRateList = MapSort.Sort(expoMap);
		for (Entry<String, Long> entry : expoRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / expoTotal));
		}
		if (!expoMap1.containsKey("null")) {
			expoMap1.put("null", (long) 0);
		}
		LOG.info(expoMap1);
		List<Map.Entry<String, Long>> expoList1 = MapSort.Sort(expoMap1);
		for (Entry<String, Long> entry : expoList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / expoTotal1));
		}
		if (!expoMap2.containsKey("null")) {
			expoMap2.put("null", (long) 0);
		}
		if (!expoMap2.containsKey("other")) {
			expoMap2.put("other", (long) 0);
		}
		LOG.info(expoMap2);
		List<Map.Entry<String, Long>> expoList2 = MapSort.Sort(expoMap2);
		for (Entry<String, Long> entry : expoList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / expoTotal2));
		}

		fw.write("\n" + "点击分布");
		fw1.write("\n" + "点击分布");

		fw3.write("\n" + "点击分布");
		fw4.write("\n" + "点击分布");

		fw5.write("\n" + "点击分布");
		fw6.write("\n" + "点击分布");
		if (!pvMap.containsKey("null")) {
			pvMap.put("null", (long) 0);
		}
		if (!pvMap.containsKey("other")) {
			pvMap.put("other", (long) 0);
		}
		LOG.info(pvMap);
		List<Map.Entry<String, Long>> pvRateList = MapSort.Sort(pvMap);
		for (Entry<String, Long> entry : pvRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / pvTotal));
		}
		if (!pvMap1.containsKey("null")) {
			pvMap1.put("null", (long) 0);
		}
		LOG.info(pvMap1);
		List<Map.Entry<String, Long>> pvList1 = MapSort.Sort(pvMap1);
		for (Entry<String, Long> entry : pvList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / pvTotal1));
		}
		if (!pvMap2.containsKey("null")) {
			pvMap.put("null", (long) 0);
		}
		if (!pvMap2.containsKey("other")) {
			pvMap.put("other", (long) 0);
		}
		LOG.info(pvMap2);
		List<Map.Entry<String, Long>> pvList2 = MapSort.Sort(pvMap2);
		for (Entry<String, Long> entry : pvList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / pvTotal2));
		}

		fw.write("\n" + "收藏分布");
		fw1.write("\n" + "收藏分布");

		fw3.write("\n" + "收藏分布");
		fw4.write("\n" + "收藏分布");

		fw5.write("\n" + "收藏分布");
		fw6.write("\n" + "收藏分布");
		if (!storeMap.containsKey("null")) {
			storeMap.put("null", (long) 0);
		}
		if (!storeMap.containsKey("other")) {
			storeMap.put("other", (long) 0);
		}
		LOG.info(storeMap);
		List<Map.Entry<String, Long>> storeRateList = MapSort.Sort(storeMap);
		for (Entry<String, Long> entry : storeRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / storeTotal));
		}
		if (!storeMap1.containsKey("null")) {
			storeMap1.put("null", (long) 0);
		}
		LOG.info(storeMap1);
		List<Map.Entry<String, Long>> storeList1 = MapSort.Sort(storeMap1);
		for (Entry<String, Long> entry : storeList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / storeTotal1));
		}
		if (!storeMap2.containsKey("null")) {
			storeMap2.put("null", (long) 0);
		}
		if (!storeMap2.containsKey("other")) {
			storeMap2.put("other", (long) 0);
		}
		LOG.info(storeMap2);
		List<Map.Entry<String, Long>> storeList2 = MapSort.Sort(storeMap2);
		for (Entry<String, Long> entry : storeList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / storeTotal2));
		}

		fw.write("\n" + "分享分布");
		fw1.write("\n" + "分享分布");

		fw3.write("\n" + "分享分布");
		fw4.write("\n" + "分享分布");

		fw5.write("\n" + "分享分布");
		fw6.write("\n" + "分享分布");
		if (!shareMap.containsKey("null")) {
			shareMap.put("null", (long) 0);
		}
		if (!shareMap.containsKey("other")) {
			shareMap.put("other", (long) 0);
		}
		LOG.info(shareMap);
		List<Map.Entry<String, Long>> shareRateList = MapSort.Sort(shareMap);
		for (Entry<String, Long> entry : shareRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / shareTotal));
		}
		if (!shareMap1.containsKey("null")) {
			shareMap1.put("null", (long) 0);
		}
		LOG.info(shareMap1);
		List<Map.Entry<String, Long>> shareList1 = MapSort.Sort(shareMap1);
		for (Entry<String, Long> entry : shareList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / shareTotal1));
		}
		if (!shareMap2.containsKey("null")) {
			shareMap2.put("null", (long) 0);
		}
		if (!shareMap2.containsKey("other")) {
			shareMap2.put("other", (long) 0);
		}
		LOG.info(shareMap2);
		List<Map.Entry<String, Long>> shareList2 = MapSort.Sort(shareMap2);
		for (Entry<String, Long> entry : shareList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / shareTotal2));
		}

		fw.write("\n" + "评论分布");
		fw1.write("\n" + "评论分布");

		fw3.write("\n" + "评论分布");
		fw4.write("\n" + "评论分布");

		fw5.write("\n" + "评论分布");
		fw6.write("\n" + "评论分布");
		if (!commentMap.containsKey("null")) {
			commentMap.put("null", (long) 0);
		}
		if (!commentMap.containsKey("other")) {
			commentMap.put("other", (long) 0);
		}
		LOG.info(commentMap);
		List<Map.Entry<String, Long>> commentRateList = MapSort.Sort(commentMap);
		for (Entry<String, Long> entry : commentRateList) {
			fw1.write("\t" + entry.getValue());
			fw.write("\t" + nt.format(entry.getValue() * 1.0 / commentTotal));
		}
		if (!commentMap1.containsKey("null")) {
			commentMap1.put("null", (long) 0);
		}
		LOG.info(commentMap1);
		List<Map.Entry<String, Long>> commentList1 = MapSort.Sort(commentMap1);
		for (Entry<String, Long> entry : commentList1) {
			fw3.write("\t" + entry.getValue());
			fw4.write("\t" + nt.format(entry.getValue() * 1.0 / commentTotal1));
		}
		if (!commentMap2.containsKey("null")) {
			commentMap2.put("null", (long) 0);
		}
		if (!commentMap2.containsKey("other")) {
			commentMap2.put("other", (long) 0);
		}
		LOG.info(commentMap2);
		List<Map.Entry<String, Long>> commentList2 = MapSort.Sort(commentMap2);
		for (Entry<String, Long> entry : commentList2) {
			fw5.write("\t" + entry.getValue());
			fw6.write("\t" + nt.format(entry.getValue() * 1.0 / commentTotal2));
		}

		fw.write("\n点击率");
		List<Map.Entry<String, Long>> pvList20 = MapSort.Sort(pvMap);
		for (Entry<String, Long> entry : pvList20) {
			if (entry.getValue() == 0) {
				fw.write("\t" + "0.00%");
			} else {
				fw.write("\t" + nt.format(entry.getValue() * 1.0 / expoMap.get(entry.getKey())));
			}
		}
		fw4.write("\n点击率");
		List<Map.Entry<String, Long>> pvList21 = MapSort.Sort(pvMap1);
		for (Entry<String, Long> entry : pvList21) {
			if (entry.getValue() == 0) {
				fw4.write("\t" + "0.00%");
			} else {
				fw4.write("\t" + nt.format(entry.getValue() * 1.0 / expoMap1.get(entry.getKey())));
			}
		}
		fw6.write("\n点击率");
		List<Map.Entry<String, Long>> pvList22 = MapSort.Sort(pvMap2);
		for (Entry<String, Long> entry : pvList22) {
			if (entry.getValue() == 0) {
				fw6.write("\t" + "0.00%");
			} else {
				fw6.write("\t" + nt.format(entry.getValue() * 1.0 / expoMap2.get(entry.getKey())));
			}
		}

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
		fw6.flush();
		fw6.close();
		fw7.flush();
		fw7.close();
		br1.close();
	}

	public String convertSourcePercentRateHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "";
		String[] keys = line.split("\t");
		result = result + "<table  style=\"font-size:14px;text-align: center;\" border=\"1\" bordercolor=\"#"
				+ borderColor + "\" class=\"table\"><tbody>";
		result = result + convertTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			result = result + convertLineHtml(lineList.get(i).split("\t"));
		}
		br.close();
		fr.close();
		result = result + "</tbody></table>";
		return result;

	}

	public String convertTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			result = result + "<td style=\"background-color:#" + titleColor
					+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + keys[i] + "</td>";
		}

		result = result + "</tr>";
		return result;

	}

	public String convertLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0) {
				result = result + "<td style=\"background-color:#" + titleColor
						+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + keys[i] + "</td>";
			} else {
				result = result + "<td style=\"background-color:#" + color + ";\"   height=\"20\" width=\"90\">"
						+ keys[i] + "</td>";
			}
		}
		result = result + "</tr>";
		return result;

	}

	public void outPutHtml(String endDate) throws IOException {
		String sentContent = "";

		sentContent = sentContent
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><p><span style=\"line-height:normal;\"><strong>稿源分级运营指标情况</strong></span></p>";
		sentContent = sentContent
				+ convertSourcePercentRateHtml(sourceRatePath + endDate + "_sourceRatePercentData.txt");

		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>质量评级得分运营指标情况</strong></span></p>";
		sentContent = sentContent
				+ convertSourcePercentRateHtml(sourceRatePath + endDate + "_qualityEvalLevelPercentData.txt");

		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>时效性运营指标情况</strong></span></p>";
		sentContent = sentContent
				+ convertSourcePercentRateHtml(sourceRatePath + endDate + "_timeSensitivePercentData.txt");
		FileWriter fw = new FileWriter(operdisPath + endDate + "_allPercentDataHtml.html");
		fw.write(sentContent);
		fw.flush();
		fw.close();

	}

	public void outPutHtml2(String endDate) throws IOException {
		String sentContent = "";

		sentContent = sentContent
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><p><span style=\"line-height:normal;\"><strong>稿源分级运营指标情况</strong></span></p>";
		sentContent = sentContent + convertSourcePercentRateHtml(sourceRatePath + endDate + "_sourceRateData.txt");

		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>质量评级得分运营指标情况</strong></span></p>";
		sentContent = sentContent
				+ convertSourcePercentRateHtml(sourceRatePath + endDate + "_qualityEvalLevelData.txt");

		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>时效性运营指标情况</strong></span></p>";
		sentContent = sentContent + convertSourcePercentRateHtml(sourceRatePath + endDate + "_timeSensitiveData.txt");
		FileWriter fw = new FileWriter(opercountPath + endDate + "_allDataHtml.html");
		fw.write(sentContent);
		fw.flush();
		fw.close();

	}

	public void outPutHtml3(String endDate) throws IOException {
		String sentContent = "";

		sentContent = sentContent
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><p><span style=\"line-height:normal;\"><strong>曝光情况</strong></span></p>";

		FileReader fr = new FileReader(sourceRatePath + endDate + "_expoRate.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while ((line = br.readLine()) != null) {
			sentContent = sentContent + "<p>" + line + "</p>";
		}

		FileWriter fw = new FileWriter(expoPath + endDate + "_expoHtml.html");
		fw.write(sentContent);
		fw.flush();
		fw.close();
		br.close();
		fr.close();

	}

	public void Cal(String date) throws IOException {
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);
		Jedis jedis = null;
		long recommendSum = 0;
		try {
			jedis = new Jedis("10.80.9.143", 6379, 6000);
			jedis.select(0);
			recommendSum = jedis.smembers("availableDocIds").size();
			LOG.info("可推荐量:" + recommendSum);
		} catch (Exception e) {
			LOG.error(e);
		}

		FileReader fr1 = new FileReader(defaultPath + date + "_source.txt");
		BufferedReader br1 = new BufferedReader(fr1);
		FileWriter fileWriter = new FileWriter(sourceRatePath + date + "_expoRate.txt", false);
		String line1 = null;
		int sum1 = 0;
		while ((line1 = br1.readLine()) != null) {
			if (line1.contains("合计")) {
				sum1 = Integer.valueOf(line1.split("\t")[5]);
				fileWriter.write("推荐数据更新量为：" + sum1 + "\n");
			}
		}

		fileWriter.write("推荐数据更新曝光量为：" + updateRecommendTotal + "\n");

		fileWriter.write("当日数据曝光比（更新曝光量/当日新增推荐量）：" + nt.format(updateRecommendTotal * 1.0 / sum1) + "\n");

		fileWriter.write("曝光总量：" + expoSum + "\n");
		fileWriter.write("可推荐量：" + recommendSum + "\n");

		fileWriter.write("当日曝光数据比（更新曝光量/曝光总数）：" + nt.format(updateRecommendTotal * 1.0 / expoSum) + "\n");
		fileWriter.write("总体曝光比（曝光总数/可推荐量）：" + nt.format(expoSum * 1.0 / recommendSum) + "\n");
		fileWriter.flush();
		fileWriter.close();
		fr1.close();
		br1.close();

	}

	public static void main(String[] args) throws IOException, ParseException {
		// for (int i = 2; i <= 9; i++) {

		// String lastOneDate = CalTools.getDate((System.currentTimeMillis() /
		// 1000 - (2 + 2) * 24 * 60 * 60) * 1000);
		// String startDate = CalTools.getDate((System.currentTimeMillis() /
		// 1000 - (2 + 1) * 24 * 60 * 60) * 1000);
		// String endDate = CalTools.getDate((System.currentTimeMillis() / 1000
		// - 2 * 24 * 60 * 60) * 1000);

		CalSourceRate calSourceRate = new CalSourceRate();
		String startDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 3 * 24 * 60 * 60) * 1000);
		String endDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 2 * 24 * 60 * 60) * 1000);
		// String startDate = args[0];
		// String endDate = args[1];
		calSourceRate.calProcess(startDate, endDate);
		calSourceRate.process(startDate, endDate);
		calSourceRate.outPutHtml(endDate);
		calSourceRate.outPutHtml2(endDate);
		calSourceRate.Cal(endDate);
		calSourceRate.outPutHtml3(endDate);
		// calSourceRate.CalData(endDate, startDate, lastOneDate);
		// }
		LOG.info("end");
		System.exit(0);

	}

}
