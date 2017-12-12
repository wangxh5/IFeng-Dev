package TestDemo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import utils.FeatureExTools;

public class CalCateOdnumThread1 extends Thread {

	static Logger LOG = Logger.getLogger(CalCateOdnumThread1.class);
	int start;
	int end;
	IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
	HashMap<String, Integer> cateMap = new HashMap<String, Integer>();

	public CalCateOdnumThread1(int start, int end) {
		super();
		this.start = start;
		this.end = end;

	}

	public void calOdnum() {

		for (int i = start; i <= end; i++) {
			String id = String.valueOf(i);
			itemf item = ikvop.queryItemF("cmpp_" + id);
			String category = null;
			if (item == null) {
				continue;
			} else {
				ArrayList<String> categoryList = new ArrayList<String>();
				categoryList = FeatureExTools.whatCategory(item.getFeatures());
				if (categoryList == null || categoryList.isEmpty()) {
					category = "null";
				} else {
					category = categoryList.get(0);
				}
			}

			if (cateMap.containsKey(category)) {
				cateMap.put(category, cateMap.get(category) + 1);
			} else {
				cateMap.put(category, 1);
			}
		}
	}

	public void output() throws IOException {
		FileWriter fw = new FileWriter("E:/Doc/_category.txt", false);
		fw.write("category\t原始抓取数量\n");
		for (Map.Entry<String, Integer> entry : cateMap.entrySet()) {
			fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		fw.flush();
		fw.close();

	}

//	public void run() {
//		for (int i = start; i <= end; i++) {
//			if (i % 100 == 0)
//				System.out.println("进行到：" + i);
//			String id = String.valueOf(i);
//			itemf item = ikvop.queryItemF("cmpp_" + id);
//			String category = null;
//			if (item == null) {
//				continue;
//			} else {
//				ArrayList<String> categoryList = new ArrayList<String>();
//				categoryList = FeatureExTools.whatCategory(item.getFeatures());
//				if (categoryList == null || categoryList.isEmpty()) {
//					category = "null";
//				} else {
//					category = categoryList.get(0);
//				}
//			}
//
//			if (cateMap.containsKey(category)) {
//				cateMap.put(category, cateMap.get(category) + 1);
//			} else {
//				cateMap.put(category, 1);
//			}
//
//		}
//		LOG.info("线程结束1");
//	}
//
//	public HashMap<String, Integer> getCateMap() {
//		return cateMap;
//	}

	public static void main(String[] args) throws IOException, InterruptedException {

//		CalOdnumByCategory cal1 = new CalOdnumByCategory(1, 500);
//		CalOdnumByCategory cal2 = new CalOdnumByCategory(501, 1000);
//		HashMap<String, Integer> cateMap1 = new HashMap<String, Integer>();
//		HashMap<String, Integer> cateMap2 = new HashMap<String, Integer>();
//		long startTime = System.currentTimeMillis();
//		cal1.start();
//		cal2.start();
//		cal1.join();
//		cal2.join();
//
//		cateMap1 = cal1.getCateMap();
//		cateMap2 = cal2.getCateMap();
//		// cateMap1.putAll(cateMap2);
//		for (String key : cateMap2.keySet()) {
//			if (cateMap1.containsKey(key)) {
//				cateMap1.put(key, cateMap1.get(key) + cateMap2.get(key));
//			} else {
//				cateMap1.put(key, cateMap2.get(key));
//			}
//		}
//		FileWriter fw = new FileWriter("E:/Doc/_category.txt", false);
//		fw.write("category\t原始抓取数量\n");
//		for (Map.Entry<String, Integer> entry : cateMap1.entrySet()) {
//			fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
//		}
//		fw.flush();
//		fw.close();
//		long endTime = System.currentTimeMillis();
//		System.out.println("时间：" + (endTime - startTime));

		CalCateOdnumThread1 cal = new CalCateOdnumThread1(0, 1000);
		cal.calOdnum();
		cal.output();

	}

}
