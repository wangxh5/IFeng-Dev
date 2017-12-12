package calModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import utils.LoadConfig;

public class CalCategoryNum {
	static Logger LOG = Logger.getLogger(CalCategoryNum.class);
	HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();// 统计分类category
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
	String total = null;

	public void readFile(String endDate) throws IOException {
		FileReader fr = null;
		fr = new FileReader(defaultPath + endDate + "_sandc.txt");
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		while ((s = br.readLine()) != null) {
			if (s.startsWith("source"))
				continue;
			if (s.startsWith("合计")) {
				total = s.split("\t")[2];
				continue;
			}
			// System.out.println(s);
			String[] lines = s.split("\t");
			if (map.containsKey(lines[1])) {
				HashMap<String, Integer> numMap = map.get(lines[1]);
				numMap.put("odnum", numMap.get("odnum") + Integer.valueOf(lines[2]));
				numMap.put("renum", numMap.get("renum") + Integer.valueOf(lines[3]));
				numMap.put("ednum", numMap.get("ednum") + Integer.valueOf(lines[4]));
				numMap.put("lanum", numMap.get("lanum") + Integer.valueOf(lines[5]));
				map.put(lines[1], numMap);
			} else {
				HashMap<String, Integer> numMap = new HashMap<String, Integer>();
				numMap.put("odnum", Integer.valueOf(lines[2]));
				numMap.put("renum", Integer.valueOf(lines[3]));
				numMap.put("ednum", Integer.valueOf(lines[4]));
				numMap.put("lanum", Integer.valueOf(lines[5]));
				map.put(lines[1], numMap);
			}
		}
		LOG.info("[INFO] " + map.size());
		br.close();
		fr.close();
	}

	private static List<Map.Entry<String, HashMap<String, Integer>>> mapSort(
			HashMap<String, HashMap<String, Integer>> map) {
		List<Map.Entry<String, HashMap<String, Integer>>> mappingList = null;
		// 通过ArrayList构造函数把map.entrySet()转换成list
		mappingList = new ArrayList<Map.Entry<String, HashMap<String, Integer>>>(map.entrySet());
		// 通过比较器实现比较排序
		Collections.sort(mappingList, new Comparator<Map.Entry<String, HashMap<String, Integer>>>() {
			public int compare(Map.Entry<String, HashMap<String, Integer>> mapping1,
					Map.Entry<String, HashMap<String, Integer>> mapping2) {
				return mapping2.getValue().get("lanum").compareTo(mapping1.getValue().get("lanum"));
			}
		});
		return mappingList;
	}

	public void output(String endDate) throws IOException {
		List<Map.Entry<String, HashMap<String, Integer>>> maplist = mapSort(map);
		FileWriter fw = null;
		fw = new FileWriter(defaultPath + endDate + "_category.txt", false);
		fw.write("category\t质量评级过滤后数量\t排重（进入泛编）数量\t泛编出口数量\t推荐数据更新量\n");
		int odnum = 0;
		int renum = 0;
		int ednum = 0;
		int lanum = 0;
		// int redisDbNum = 10;
		// Jedis jedis = new Jedis("10.32.24.194", 6379);
		// Gson gson = new Gson();
		Map<String, String> cateMap = new HashMap<String, String>();
		for (Entry<String, HashMap<String, Integer>> entry : maplist) {
			HashMap<String, Integer> numMap = entry.getValue();
			odnum = odnum + numMap.get("odnum");
			renum = renum + numMap.get("renum");
			ednum = ednum + numMap.get("ednum");
			lanum = lanum + numMap.get("lanum");

			cateMap.put(entry.getKey(), numMap.get("odnum") + "#" + numMap.get("renum") + "#" + numMap.get("ednum")
					+ "#" + numMap.get("lanum"));
			fw.write(entry.getKey() + "\t" + numMap.get("odnum") + "\t" + numMap.get("renum") + "\t"
					+ numMap.get("ednum") + "\t" + numMap.get("lanum") + "\n");
		}
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.90.7.52", 6379);
			jedis.select(5);
			jedis.hmset(endDate + "-分类统计数据", cateMap);
			jedis.hmset("分类统计数据", cateMap);
		} catch (Exception ex) {
			LOG.error("[ERROR] In write item to Redis.", ex);
		} finally {
			jedis.close();
		}

		// String gsonStr = gson.toJson(cateMap);
		// try {
		// jedis.select(redisDbNum);
		// jedis.set(endDate + "_category", gsonStr);
		// jedis.close();
		// LOG.info("set-string-succeed:" + endDate + "," + gsonStr);
		// } catch (Exception e) {
		// LOG.error("[ERROR] In write item to Redis.", e);
		// jedis.close();
		// }
		fw.write("合计" + "\t" + total + "\t" + renum + "\t" + ednum + "\t" + lanum);
		fw.flush();
		fw.close();
	}

	public static void main(String[] args) throws IOException {
		CalCategoryNum op = new CalCategoryNum();
		// op.readFile("20161108");
		// op.output("20161108");
		String endDate = CalTools.getDate(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		op.readFile(endDate);
		op.output(endDate);
		System.out.println("end");
		System.exit(0);

	}
}
