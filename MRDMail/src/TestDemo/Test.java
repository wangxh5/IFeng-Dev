package TestDemo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import utils.LoadConfig;

public class Test {

	public static void main(String[] args) throws IOException {
		String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
		FileReader fr = new FileReader(defaultPath + "train_data.txt");
		BufferedReader br = new BufferedReader(fr);
		String title = null;
		String line = null;
		FileWriter fw = new FileWriter(defaultPath + "科技.txt", false);
		// FileWriter fw1 = new FileWriter(defaultPath + "时政样本.txt", false);
		IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
		while ((line = br.readLine()) != null) {
			if (line.split("\t")[0].equals("足球")) {
//				System.out.println(line);
				List<String> cateList = new ArrayList<String>();
				title = line.split("\t")[1];
				itemf item = ikvop.queryItemF(title);
				if (item == null) {
					// fw1.write(line + "\n");
					System.out.println("item is null");
					continue;
				}
				cateList = item.getFeatures();
				if (cateList == null || cateList.isEmpty()) {
					// fw1.write(line + "\n");
					continue;
				} else {
					// if (!cateList.contains("足球") && !cateList.contains("篮球")
					// && !cateList.contains("健身")) {
					// fw1.write(line + "\n");
					// continue;
					// }

					// if (!cateList.contains("军事")) {
					// fw1.write(line + "\n");
					// continue;
					// }

					for (int i = 0; i < cateList.size(); i += 3) {
						if (Double.valueOf(cateList.get(i + 2)) >= 0.8 && cateList.get(i + 1).equals("c")
								&&cateList.get(i).equals("足球")&& !cateList.contains("篮球")) {
							fw.write(cateList.get(i) + "\t" + title + "\n");
							break;
						}
					}
				}
			}
		}
		fw.flush();
		fw.close();
		// fw1.flush();
		// fw1.close();
		br.close();
		fr.close();
		System.out.println("end");
		System.exit(0);
	}
}
