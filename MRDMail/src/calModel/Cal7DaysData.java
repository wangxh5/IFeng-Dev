package calModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import utils.LoadConfig;

public class Cal7DaysData {
	static Logger LOG = Logger.getLogger(Cal7DaysData.class);
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
	ArrayList<String> dateList = new ArrayList<String>();
	HashMap<String, HashMap<String, String>> sMap = new HashMap<String, HashMap<String, String>>();
	ArrayList<String> allList = new ArrayList<String>();
	ArrayList<String> recommendList = new ArrayList<String>();
	String offLineNum = null;
	String editNum = null;

	private void readRecommendFile(String date) throws IOException {
		LOG.info("[INFO] Read file date " + date);
		FileReader fr = null;
		fr = new FileReader(defaultPath + date + "_recommend.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		recommendList.add(line);
		br.close();
		fr.close();
		LOG.info("[readRecommendFile] " + date + "\t" + line);
	}

	private void readOffLineFile(String date) throws IOException {
		FileReader fr = null;
		fr = new FileReader(defaultPath + date + "_offLineNum.txt");
		BufferedReader br = new BufferedReader(fr);
		offLineNum = br.readLine();
		br.close();
		fr.close();
		LOG.info("[offLineListFile] " + date + "\t" + offLineNum);

		FileReader fr1 = new FileReader(defaultPath + date + "_editNum.txt");
		BufferedReader br1 = new BufferedReader(fr1);
		editNum = br1.readLine();
		br1.close();
		fr1.close();
		LOG.info("[editNumFile] " + date + "\t" + editNum);
	}

	private void readFile(String date) throws IOException {
		LOG.info("[INFO] Read file date " + date);
		dateList.add(date);
		FileReader fr = null;
		fr = new FileReader(defaultPath + date + "_source.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("source"))
				continue;
			if (line.startsWith("合计")) {
				String s = line.replaceAll("合计\t", "");
				// allList.add(s);
				allList.add(s.split("\t")[0] + "\t" + s.split("\t")[1] + "\t" + s.split("\t")[2] + "\t" + editNum + "\t"
						+ offLineNum + "\t" + s.split("\t")[3] + "\t" + s.split("\t")[4] + "\t" + s.split("\t")[5]);
				// LOG.info("[readFile] " + date + "\t" +
				// line.replaceAll("合计\t", ""));
				continue;
			}
			if (line.startsWith("垃圾数据")) {
				continue;
			}
			String type = line.split("\t")[0];
			// System.out.println(type);
			if (sMap.containsKey(type)) {
				HashMap<String, String> innerMap = sMap.get(type);
				// System.out.println(line);
				innerMap.put(date, line);
				sMap.put(type, innerMap);
			} else {
				HashMap<String, String> innerMap = new HashMap<String, String>();
				// System.out.println(line);
				innerMap.put(date, line);
				sMap.put(type, innerMap);
			}
		}
		br.close();
		fr.close();
	}

	// public void outputAllData(String date) throws IOException{
	// FileWriter fw = null;
	// fw = new FileWriter(defaultPath+date+"_all7days.txt",false);
	// fw.write("时间\t原始抓取数量\t质量评级过滤后数量\t排重（进入泛编）数量\t泛编出口数量\t推荐数据更新量\t优质数据量\n");
	// for(int i = allList.size() - 1; i >= 0; i--)
	// {
	// String[] linekeys = allList.get(i).split("\t");
	//// int lostNum = Report2Throw.getAllThrowNum();
	// fw.write(dateList.get(i)+"\t"+allList.get(i)+"\n");//"\t"+Integer.valueOf(linekeys[0])+
	// }
	// fw.flush();
	// fw.close();
	// }
	public void outputAllData(String date) throws IOException {
		FileWriter fw = null;
		fw = new FileWriter(defaultPath + date + "_all7days.txt", false);

		fw.write("时间\t原始抓取数量\t质量评级过滤后数量\t排重后数量\t进入泛编数量\t泛编下线数量\t泛编出口数量\t推荐数据更新量\t优质数据量\t算法推荐量\n");
		for (int i = dateList.size() - 1; i >= 0; i--) {
			// String[] linekeys = allList.get(i).split("\t");
			// int lostNum = Report2Throw.getAllThrowNum();
			fw.write(dateList.get(i) + "\t" + allList.get(i) + "\t" + recommendList.get(i) + "\n");// "\t"+Integer.valueOf(linekeys[0])+
		}
		fw.flush();
		fw.close();
	}

	public void output7days(String date) throws IOException {
		FileWriter fw = null;
		fw = new FileWriter(defaultPath + date + "_7days.txt", false);

		for (Entry<String, HashMap<String, String>> entry : sMap.entrySet()) {
			HashMap<String, String> innerMap = entry.getValue();
			fw.write(" \t" + entry.getKey() + "\t \t \t \t \t \n");
			fw.write("时间\t原始抓取数量\t质量评级过滤后数量\t排重后数量\t泛编出口数量\t推荐数据更新量\t优质数据量\n");
			for (int j = dateList.size() - 1; j >= 0; j--) {
				// System.out.println(dateList.get(j));
				String innerLine = "0\t0\t0\t0";
				if (innerMap.get(dateList.get(j)) == null) {
				} else
					innerLine = innerMap.get(dateList.get(j)).replace(entry.getKey() + "\t", "");
				fw.write(dateList.get(j) + "\t" + innerLine + "\n");
			}
		}
		fw.flush();
		fw.close();
	}

	public void process(String endDate) throws ParseException, IOException {
		long terminalTime = CalTools.getTimeStamp(endDate + " 23:00:00") * 1000;
		for (int i = 0; i < 7; i++) {
			String date = CalTools.getDate(terminalTime - i * 24 * 60 * 60 * 1000);
			readOffLineFile(date);
			readRecommendFile(date);
			readFile(date);
		}

		output7days(endDate);
		outputAllData(endDate);
	}

	public static void main(String[] args) throws ParseException, IOException {
		Cal7DaysData ob = new Cal7DaysData();
		String endDate = "20170914";
		ob.process(endDate);
		System.out.println("END");
		System.exit(0);
	}
}
