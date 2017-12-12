package calModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import Hbase.HbaseOperation;

import java.util.Set;

import dataBase.itemf;
import redis.clients.jedis.Jedis;
import utils.CMPPDataCollect;
import utils.JsonFromCMPP;
import utils.LoadConfig;

public class availableDataAnalyse {
	public static String redisip = "10.80.8.143";
	public static int redisport = 6379;
	public static int redisdb = 0;
	public static String idsKey = "availableDocIds";
	// public IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
	HbaseOperation hbaseOperation = new HbaseOperation();
	public HashMap<String, videoData> videoDataMap;
	HashSet<String> originalId = new HashSet<String>();// 推荐表id
	static Logger LOG = Logger.getLogger(availableDataAnalyse.class);
	String borderColor = "FFFFFF";
	String titleColor = "d1d9fa";
	String color = "f7f6f6";
	int[] sum = new int[34];
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath1");
	private static final String videoSourcePath = LoadConfig.lookUpValueByKey("videoSourcePath");
	private static final String videoSourcedis = LoadConfig.lookUpValueByKey("videoSourcedis");

	class videoData {
		public HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		public int recomNum;
		public int appendNum;

		videoData() {
			initialCategoryMap();
		}

		public int getRecomNum() {
			return this.recomNum;
		}

		public void setRecomNum(int recomnum) {
			this.recomNum = recomnum;
		}

		public int getAppendNum() {
			return this.appendNum;
		}

		public void setAppendNum(int appendnum) {
			this.appendNum = appendnum;
		}

		public void initialCategoryMap() {
			for (String category : categoryArr) {
				categoryMap.put(category, 0);
			}
		}
	}

	private String[] categoryArr = { "社会", "娱乐", "美食", "科技", "时尚", "体育", "时政", "财经", "汽车", "文化", "健康", "搞笑", "萌宠", "旅游",
			"教育", "情感", "游戏", "家居", "科学探索", "收藏", "动漫", "星座", "历史", "职场", "美女", "公益", "摄影", "佛教", "风水", "移民", "考古",
			"天气" };

	public void getVideoDataMap() {
		videoDataMap = new HashMap<String, videoData>();
		FileReader fr = null;
		try {
			fr = new FileReader(videoSourcePath);
			// fr = new FileReader("D:\\videosource.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String source = null;
		try {
			while ((source = br.readLine()) != null) {
				videoDataMap.put(source, new videoData());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getVideoDataCount() {
		if (videoDataMap == null || videoDataMap.size() <= 1)
			getVideoDataMap();
		System.out.println("videoDataMap size is : " + videoDataMap.size());
		Jedis jedis = new Jedis(redisip, redisport);
		jedis.select(redisdb);
		Set<String> ids = jedis.smembers(idsKey);
		LOG.info("availableDocIds is：" + ids.size());
		int idcount = 0;
		List<String> keys = new ArrayList<String>();
		for (String id : ids) {
			idcount++;
			keys.add("cmpp_" + id);
			if (idcount % 2000 == 0) {
				LOG.info("porcess ids : " + idcount);
				Map<String, itemf> ItemMap = hbaseOperation.gets(keys);
				keys = new ArrayList<String>();
				for (Map.Entry<String, itemf> entry : ItemMap.entrySet()) {
					itemf item = entry.getValue();
					if (item == null)
						continue;

					if (item.getDocType().equals("video") && videoDataMap.containsKey(item.getSource())) {
						videoData videodata = videoDataMap.get(item.getSource());
						videodata.recomNum++;
						if (originalId.contains(id)) {
							videodata.appendNum++;
						}
						if (item.getCategory() != null && item.getCategory().size() >= 1)
							videodata.categoryMap.put(item.getCategory().get(0),
									videodata.categoryMap.get(item.getCategory().get(0)) + 1);
						videoDataMap.put(item.getSource(), videodata);
					}
				}
			}
		}
		hbaseOperation.close();
		jedis.close();
	}

	public void output(String endDate) {
		FileWriter fw = null;
		try {
			LOG.info("start to write");
			fw = new FileWriter(defaultPath + endDate + "_videoDataCount");
			// fw = new FileWriter("D:\\data\\videoDataCount");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Entry<String, videoData> entry : videoDataMap.entrySet()) {
			videoData videodata = entry.getValue();
			try {
				fw.write(entry.getKey() + "\t" + videodata.getAppendNum() + "\t" + videodata.getRecomNum() + "\t");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Entry<String, Integer> videoEntry : videodata.categoryMap.entrySet()) {
				try {
					fw.write(videoEntry.getKey() + "\t" + videoEntry.getValue() + "\t");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				fw.write("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fw.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("end to write");
	}

	// 标题

	public String convertTitleLineHtml(String[] keys) {
		String result = "<tr>";
		result = result + "<td style=\"background-color:#" + titleColor
				+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + "视频稿源" + "</td>";
		result = result + "<td style=\"background-color:#" + titleColor
				+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + "每日新增入库量" + "</td>";
		result = result + "<td style=\"background-color:#" + titleColor
				+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + "当前可推荐量" + "</td>";
		for (int i = 3; i < keys.length; i += 2) {
			result = result + "<td style=\"background-color:#" + titleColor
					+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + keys[i] + "</td>";
		}
		result = result + "</tr>";
		return result;

	}

	public String convertLineHtml(String[] keys) {

		String result = "<tr>";
		result = result + "<td style=\"background-color:#" + titleColor
				+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + keys[0] + "</td>";
		result = result + "<td style=\"background-color:#" + color + ";\"   height=\"20\" width=\"90\">" + keys[1]
				+ "</td>";
		sum[0] += Integer.valueOf(keys[1]);
		result = result + "<td style=\"background-color:#" + color + ";\"   height=\"20\" width=\"90\">" + keys[2]
				+ "</td>";
		sum[1] += Integer.valueOf(keys[2]);
		for (int i = 4; i < keys.length; i += 2) {
			result = result + "<td style=\"background-color:#" + color + ";\"   height=\"20\" width=\"90\">" + keys[i]
					+ "</td>";
			sum[i / 2] += Integer.valueOf(keys[i]);
		}
		result = result + "</tr>";
		return result;

	}

	public String convertSumHtml(int[] keys) {
		String result = "<tr>";
		result = result + "<td style=\"background-color:#" + titleColor
				+ ";font-weight: bold;text-align: center;\"  height=\"40\" width=\"120\">" + "合计" + "</td>";
		for (int i = 0; i < keys.length; i++) {
			result = result + "<td style=\"background-color:#" + color + ";\"   height=\"20\" width=\"90\">"
					+ String.valueOf(keys[i]) + "</td>";
		}
		result = result + "</tr>";
		return result;
	}

	// 文章类型
	public String convertHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "";
		String[] keys = line.split("\t");
		result = result + "<table  style=\"font-size:14px;text-align: center;\" border=\"1\" bordercolor=\"#"
				+ borderColor + "\" class=\"table\"><tbody>";
		result = result + convertTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while (line != null) {
			lineList.add(line);
			line = br.readLine();
		}
		for (int i = 0; i < lineList.size(); i++) {
			result = result + convertLineHtml(lineList.get(i).split("\t"));
		}
		br.close();
		fr.close();
		result = result + convertSumHtml(sum);
		result = result + "</tbody></table>";
		return result;

	}

	public void output2(String endDate) throws IOException {
		String sentContent = "";
		sentContent = sentContent + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";

		sentContent = sentContent + convertHtml(defaultPath + endDate + "_videoDataCount");
		FileWriter fw = new FileWriter(videoSourcedis + endDate + "_videoSourceContent.html");
		fw.write(sentContent);
		fw.flush();
		fw.close();
	}

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

	public static void main(String[] args) throws ParseException, IOException {
		availableDataAnalyse op = new availableDataAnalyse();
		// for (int i = 1; i <= 3; i++) {
		String startDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 2 * 24 * 60 * 60) * 1000);
		String endDate = CalTools.getDate((System.currentTimeMillis() / 1000 - 1 * 24 * 60 * 60) * 1000);
		// String startDate = args[0];
		// String endDate = args[1];
		LOG.info(startDate + "  " + endDate);
		long sTime = CalTools.getTimeStamp(startDate + " 23:00:00");
		long terminalTime = CalTools.getTimeStamp(endDate + " 23:00:00");
		op.readOriginalData(sTime, terminalTime);
		op.getVideoDataMap();
		op.getVideoDataCount();
		op.output(endDate);
		op.output2(endDate);
		LOG.info("end");
		// }
		System.exit(0);
	}
}
