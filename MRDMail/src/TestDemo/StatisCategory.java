package TestDemo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import utils.LoadConfig;

public class StatisCategory {

	public static void main(String[] args) throws ParseException, IOException {
		final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
		FileReader fr = new FileReader(defaultPath + "test.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		HashMap<String, Integer> cateMap = new HashMap<String, Integer>();
		while ((line = br.readLine()) != null) {
//			System.out.println(line);
//			cateMap.put(line.split("\t")[0],Integer.valueOf(line.split("\t")[3])*1.0/Integer.valueOf(line.split("\t")[1]));
//			if (line.split("\t")[0].equals(line.split("\t")[1])) {
				
				if (cateMap.containsKey(line.split("\t")[0])) {
					cateMap.put(line.split("\t")[0], cateMap.get(line.split("\t")[0]) + 1);
				} else {
					cateMap.put(line.split("\t")[0], 1);
				}
//			}
		}
		FileWriter fw = new FileWriter(defaultPath + "cate10.txt", false);
		// fw.write("分类\t数量\n");
		int sum = 0;
		for (Entry<String, Integer> list : cateMap.entrySet()) {
			fw.write(list.getKey() + "\t" + list.getValue() + "\n");
			sum += list.getValue();

		}
		fw.flush();
		fw.close();
		br.close();
		System.out.println(sum);
		System.out.println("end");
	}

	
}
