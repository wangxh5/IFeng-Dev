package TestDemo;

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

import com.tinkerpop.blueprints.Vertex;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import knowledgeGraph.KnowledgeGraph;
import utils.LoadConfig;

class CalOdnum implements Runnable {
	String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
	IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
	static HashMap<String, Integer> cateMap = new HashMap<String, Integer>();
	private int n = 35327450;

	FileWriter fw = null;
	KnowledgeGraph kgraph = new KnowledgeGraph();
	ArrayList<Vertex> vertexList = new ArrayList<Vertex>();

	@SuppressWarnings("unused")
	private static List<Map.Entry<Double, String>> mapSort(HashMap<Double, String> map) {
		List<Map.Entry<Double, String>> mappingList = null;
		mappingList = new ArrayList<Map.Entry<Double, String>>(map.entrySet());

		Collections.sort(mappingList, new Comparator<Map.Entry<Double, String>>() {
			public int compare(Map.Entry<Double, String> mapping1, Map.Entry<Double, String> mapping2) {
				return mapping2.getKey().compareTo(mapping1.getKey());

			}
		});
		return mappingList;
	}

	List<String> cateList = new ArrayList<String>();

	@Override
	public void run() {
		try {
			fw = new FileWriter(defaultPath + "WXHTrain1.txt", false);
			FileReader fr1 = new FileReader(defaultPath + "topc.txt");
			BufferedReader br1 = new BufferedReader(fr1);
			String line = "";
			while ((line = br1.readLine()) != null) {
				cateList.add(line);
			}
			br1.close();
			fr1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String id;
		while (n >= 31000000) {
			synchronized (this) {
				n--;
				if (n % 1000000 == 0)
					System.out.println(Thread.currentThread().getName() + "------" + n);
				id = String.valueOf(n);
			}
			itemf item = ikvop.queryItemF("cmpp_" + id);
			String docType = null;
			String title = null;
			List<String> list = new ArrayList<String>();

			if (item == null) {
				continue;
			} else {
				list = item.getFeatures();
				title = item.getTitle();
				docType = item.getDocType();

			}
			synchronized (this) {
//				HashMap<Double, String> cateMap = new HashMap<Double, String>();
				if (docType.equals("video") || docType.equals("docpic")) {
					System.out.println(title);
					// System.out.println(list);
					for (int i = 0; i < list.size(); i += 3) {
						if (cateList.contains(list.get(i)) && list.get(i + 1).equals("c")
								&& Math.abs(Double.valueOf(list.get(i + 2))) >= 0.8) {
							try {
								fw.write(list.get(i) + "\t" + title + "\n");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					continue;
				}

				// List<Map.Entry<Double, String>> list2 = mapSort1(cateMap);
				// int i = 0;
				// for (Entry<Double, String> entry : list2) {
				// if (entry.getKey() == 1.0) {
				// try {
				// fw.write(entry.getValue() + "\t" + title + "\n");
				// break;
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// } else {
				// i++;
				// try {
				// fw.write(entry.getValue() + "\t" + title + "\n");
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// if (i == 2) {
				// break;
				// }
				// }
				// }

			}
		}
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// public void output() throws IOException {
	// FileWriter fw = new FileWriter("E:/Doc/category.txt", false);
	// fw.write("category\t原始抓取数量\n");
	// for (Map.Entry<String, Integer> entry : cateMap.entrySet()) {
	// fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
	// }
	// fw.flush();
	// fw.close();
	// }

	// public HashMap<String, Integer> getCateMap() {
	// return cateMap;
	// }

}

public class CalCateOdnumThread2 {

	public static void main(String[] args) throws IOException, InterruptedException {
		CalOdnum cal = new CalOdnum();
		// long startTime = System.currentTimeMillis();
		Thread t[] = new Thread[10];
		for (int i = 0; i < 10; i++) {
			t[i] = new Thread(cal, "线程" + String.valueOf(i));
			t[i].start();
		}
		for (int i = 0; i < 10; i++) {
			t[i].join();
		}
		System.exit(0);
		/*
		 * try { Thread.sleep(100); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */
		// FileWriter fw = new FileWriter("E:/Doc/category.txt", false);
		// fw.write("category\t原始抓取数量\n");
		//
		// for (Map.Entry<String, Integer> entry : CalOdnum.cateMap.entrySet())
		// {
		// fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
		// }
		// fw.flush();
		// fw.close();
		// long endTime = System.currentTimeMillis();
		// System.out.println("时间：" + (endTime - startTime));

	}

}
