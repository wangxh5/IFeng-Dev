package TestDemo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import Entity.EntityInfo;
import Entity.EntityLibQuery;
import Entity.KnowledgeBaseBuild;
import utils.HttpRequest;
import utils.LoadConfig;

public class EntityCategory {
	private static List<Map.Entry<String, String>> mapSort(HashMap<String, String> map) {
		List<Map.Entry<String, String>> list = null;
		list = new ArrayList<Map.Entry<String, String>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			public int compare(Entry<String, String> map1, Entry<String, String> map2) {
				return map1.getValue().compareTo(map2.getValue());
			}
		});
		return list;
	}

	public static void main(String[] args) throws IOException {
		EntityLibQuery.init();
		final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
		FileReader fr = new FileReader(defaultPath + "娱乐.txt");
		BufferedReader br = new BufferedReader(fr);
		FileWriter fw = new FileWriter(defaultPath + "娱乐测试结果.txt", false);
		String line = null;
		String title = "";
		String resultList = "";
		String category = null;
		EntityInfo entityInfo = null;
		while ((line = br.readLine()) != null) {
			title = line.split("\t")[1];
			System.out.println(title);
			String query = "http://local.segment.nlp.ifengidc.com/HanlpSegmentService/segmentService?";
			resultList = HttpRequest.sendPost(query, "text=" + URLEncoder.encode(title, "utf-8") + "&posModel=false");
			System.out.println(resultList);
			String words[] = resultList.split("\\[|\\]|,");

			HashMap<String, Integer> categoryNum = new HashMap<String, Integer>();
			int total = 0;
			for (String w : words) {
				String str = KnowledgeBaseBuild.getEntityList(w);
				// 区分是不是实体词
				if (str == null) {
					continue;
				}
				List<String> cates = new ArrayList<String>();
				Gson gson = new Gson();
				JsonParser parser = new JsonParser();
				JsonArray Jarray = parser.parse(str).getAsJsonArray();
				for (JsonElement obj : Jarray) {
					entityInfo = gson.fromJson(obj, EntityInfo.class);
					category = entityInfo.getCategory();
					if (category.equals("全部")) {
						continue;
					}
					if (cates == null || cates.isEmpty()) {
						cates.add(category);
						if (categoryNum.containsKey(category)) {
							categoryNum.put(category, categoryNum.get(category) + 1);
							total++;
						} else {
							categoryNum.put(category, 1);
							total++;
						}
					} else if (cates.contains(category)) {
						continue;
					} else {
						if (categoryNum.containsKey(category)) {
							total++;
							categoryNum.put(category, categoryNum.get(category) + 1);
						} else {
							total++;
							categoryNum.put(category, 1);
						}
					}
				}

			}
			// 计算权重
			HashMap<String, String> weightMap = new HashMap<String, String>();
			for (Entry<String, Integer> entry : categoryNum.entrySet()) {
				weightMap.put(entry.getKey(), String.valueOf(entry.getValue() * 1.0 / total));
			}

			List<Map.Entry<String, String>> mappingList = mapSort(weightMap);
			for (Entry<String, String> entry : mappingList) {
				fw.write(entry.getKey() + "\t" + entry.getValue() + "\t" + title + "\n");
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			}
		}
		fw.flush();
		fw.close();

		fr.close();
		br.close();

		System.out.println("end");
		System.exit(0);
	}
}
