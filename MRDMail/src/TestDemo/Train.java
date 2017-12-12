package TestDemo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tinkerpop.blueprints.Vertex;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import knowledgeGraph.KnowledgeGraph;
import utils.LoadConfig;

public class Train {
	public static void main(String[] args) throws IOException {

		String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");

		KnowledgeGraph kgraph = new KnowledgeGraph();
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		// vertexList = kgraph.queryType("c0");
		// List<String> c0List = new ArrayList<String>();
		// for (Vertex vertex : vertexList) {
		// c0List.add((String) vertex.getProperty("word"));
		// }
		// System.out.println(c0List);
		// if (c0List.contains("亲子")) {
		// System.out.println("yes");
		// }else{
		// System.out.println("no");
		// }

		FileWriter fw = new FileWriter(defaultPath + "result.txt", false);
		FileWriter fw1 = new FileWriter(defaultPath + "resultDetail.txt", false);
		IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
		FileReader fr = new FileReader(defaultPath + "WXHTrain.txt");
		BufferedReader br = new BufferedReader(fr);
		String title = null;
		String cate = null;
		String line1;
		while ((line1 = br.readLine()) != null) {
			cate = line1.split("\t")[0];
			title = line1.split("\t")[1];
			if (cate.equals("娱乐") || cate.equals("体育") || cate.equals("时政") || cate.equals("教育") || cate.equals("科技")
					|| cate.equals("社会") || cate.equals("财经1")) {
				vertexList = kgraph.queryChild(cate);
				List<String> c1List = new ArrayList<String>();
				for (Vertex vertex : vertexList) {
					if (vertex.getProperty("typelabel").equals("c1"))
						c1List.add((String) vertex.getProperty("word"));
				}
				System.out.println(c1List);
				itemf item = ikvop.queryItemF(title);
				if (item == null) {
					continue;
				}
				List<String> list = new ArrayList<String>();
				list = item.getFeatures();
				for (int i = 0; i < list.size(); i += 3) {
					if (c1List.contains(list.get(i)) && Math.abs(Double.valueOf(list.get(i + 2))) >= 0.5) {
						fw1.write(line1 + "\t" + list.get(i) + "\t" + list.get(i + 2) + "\n");
						fw.write(line1 + "\n");
						break;
					}
				}
			} else {
				fw.write(line1 + "\n");
			}
		}
		kgraph.shutdown();// 操作完成记得关闭

		fw.flush();
		fw.close();
		fr.close();
		br.close();
		fw1.flush();
		fw1.close();
		System.out.println("end");

	}
}
