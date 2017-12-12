package TestDemo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;

import org.apache.log4j.Logger;

import calModel.CalTools;
import dataBase.IKVOperationv3;
import dataBase.itemf;
import redis.clients.jedis.Jedis;
import utils.LoadConfig;

public class Cal7DaysSourceRate {
	HashMap<String, HashMap<String, Long>> sourceRateMap = new HashMap<String, HashMap<String, Long>>();
	static Logger LOG = Logger.getLogger(Cal7DaysSourceRate.class);
	private String sourceRatePath = LoadConfig.lookUpValueByKey("sourceRatePath");
	HashMap<String, Long> pvMap = new HashMap<String, Long>();
	HashMap<String, Long> expoMap = new HashMap<String, Long>();
	HashMap<String, Long> storeMap = new HashMap<String, Long>();
	HashMap<String, Long> shareMap = new HashMap<String, Long>();
	HashMap<String, Long> commentMap = new HashMap<String, Long>();

	private void readFile(String date) throws IOException {
		LOG.info("[INFO] Read file date " + date);
		BufferedReader br = new BufferedReader(new FileReader(sourceRatePath + date + "_sourceRateData.txt"));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			if (sourceRateMap.containsKey(line.split("\t")[0])) {
				HashMap<String, Long> numMap = sourceRateMap.get(line.split("\t")[0]);
				numMap.put("A级", numMap.get("A级") + Long.valueOf(line.split("\t")[1]));
				numMap.put("B级", numMap.get("B级") + Long.valueOf(line.split("\t")[2]));
				numMap.put("C级", numMap.get("C级") + Long.valueOf(line.split("\t")[3]));
				numMap.put("D级", numMap.get("D级") + Long.valueOf(line.split("\t")[4]));
				numMap.put("E级", numMap.get("E级") + Long.valueOf(line.split("\t")[5]));
				if (line.split("\t").length == 6) {
					numMap.put("other级", numMap.get("other级") + (long) 0);
				} else {
					numMap.put("other级", numMap.get("other级") + Long.valueOf(line.split("\t")[6]));
				}
				sourceRateMap.put(line.split("\t")[0], numMap);
			} else {
				HashMap<String, Long> numMap = new HashMap<String, Long>();
				numMap.put("A级", Long.valueOf(line.split("\t")[1]));
				numMap.put("B级", Long.valueOf(line.split("\t")[2]));
				numMap.put("C级", Long.valueOf(line.split("\t")[3]));
				numMap.put("D级", Long.valueOf(line.split("\t")[4]));
				numMap.put("E级", Long.valueOf(line.split("\t")[5]));
				if (line.split("\t").length == 6) {
					numMap.put("other级", (long) 0);
				} else {
					numMap.put("other级", Long.valueOf(line.split("\t")[6]));
				}
				sourceRateMap.put(line.split("\t")[0], numMap);
			}
		}

		br.close();
	}

	private String pvPath = LoadConfig.lookUpValueByKey("pvPath");
	private BufferedReader br1;

	public void process1(String date) throws IOException {
		br1 = new BufferedReader(new FileReader(pvPath + "pv_expo_" + date));
		IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
		String source = null;
		String rate = null;
		String line = null;
		while ((line = br1.readLine()) != null) {
			itemf item = null;
			if (line.split("\t")[0] == null || line.split("\t")[0].isEmpty()) {
				continue;
			} else if (line.split("\t")[0].startsWith("sub")) {
				System.out.println(line.split("\t")[0]);
				item = ikvop.queryItemF(line.split("\t")[0]);
			} else if (line.split("\t")[0].length() > 8) {
				System.out.println(line.split("\t")[0]);
				item = ikvop.queryItemF(line.split("\t")[0]);
			} else {
				System.out.println(line.split("\t")[0]);
				item = ikvop.queryItemF("cmpp_" + line.split("\t")[0]);
			}
			if (item == null) {
				continue;
			}
			source = item.getSource();
			System.out.println(source);
			if (source == null || source.isEmpty()) {
				continue;
			}
			rate = getSourceRate(source);
			System.out.println(source + "\t" + rate);
			if (pvMap.containsKey(rate)) {
				pvMap.put(rate, pvMap.get(rate) + Long.valueOf(line.split("\t")[1]));
			} else {
				pvMap.put(rate, Long.valueOf(line.split("\t")[1]));
			}

			if (expoMap.containsKey(rate)) {
				expoMap.put(rate, expoMap.get(rate) + Long.valueOf(line.split("\t")[7]));
			} else {
				expoMap.put(rate, Long.valueOf(line.split("\t")[7]));
			}

			if (storeMap.containsKey(rate)) {
				storeMap.put(rate, storeMap.get(rate) + Long.valueOf(line.split("\t")[4]));
			} else {
				storeMap.put(rate, Long.valueOf(line.split("\t")[4]));
			}

			if (shareMap.containsKey(rate)) {
				shareMap.put(rate, shareMap.get(rate) + Long.valueOf(line.split("\t")[5]));
			} else {
				shareMap.put(rate, Long.valueOf(line.split("\t")[5]));
			}

			if (commentMap.containsKey(rate)) {
				commentMap.put(rate, commentMap.get(rate) + Long.valueOf(line.split("\t")[6]));
			} else {
				commentMap.put(rate, Long.valueOf(line.split("\t")[6]));
			}

		}
	}

	public void output() throws IOException {
		for (int i = 4; i <= 43; i++) {
			String date = CalTools.getDate((System.currentTimeMillis() / 1000 - i * 24 * 60 * 60) * 1000);
			readFile(date);
			process1(date);
		}
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);
		System.out.println("start to write");
		FileWriter fw = new FileWriter(sourceRatePath + "allSourceRateData2.txt", false);
		FileWriter fw1 = new FileWriter(sourceRatePath + "allSourceRatePercentData2.txt", false);
		fw.write(" \tA级\tB级\tC级\tD级\tE级\n");
		fw1.write(" \tA级\tB级\tC级\tD级\tE级\n");
		long total = 0;
		long originalTotal = 0;
		long pvTotal = 0;
		long storeTotal = 0;
		long shareTotal = 0;
		long commentTotal = 0;
		long expoTotal = 0;
		long recommendTotal = 0;
		HashMap<String, Long> numMap = new HashMap<String, Long>();
		numMap = sourceRateMap.get("源数据");
		total = numMap.get("A级") + numMap.get("B级") + numMap.get("C级") + numMap.get("D级") + numMap.get("E级")
				+ numMap.get("other级");
		fw.write("源数据\t" + numMap.get("A级") + "\t" + numMap.get("B级") + "\t" + numMap.get("C级") + "\t"
				+ numMap.get("D级") + "\t" + numMap.get("E级") + "\t" + numMap.get("other级") + "\n");
		fw1.write("源数据\t" + nt.format(numMap.get("A级") * 1.0 / total) + "\t" + nt.format(numMap.get("B级") * 1.0 / total)
				+ "\t" + nt.format(numMap.get("C级") * 1.0 / total) + "\t" + nt.format(numMap.get("D级") * 1.0 / total)
				+ "\t" + nt.format(numMap.get("E级") * 1.0 / total) + "\n");

		numMap = sourceRateMap.get("文章生产量");
		originalTotal = numMap.get("A级") + numMap.get("B级") + numMap.get("C级") + numMap.get("D级") + numMap.get("E级")
				+ numMap.get("other级");
		fw.write("文章生产量\t" + numMap.get("A级") + "\t" + numMap.get("B级") + "\t" + numMap.get("C级") + "\t"
				+ numMap.get("D级") + "\t" + numMap.get("E级") + "\t" + numMap.get("other级") + "\n");
		fw1.write("文章生产量\t" + nt.format(numMap.get("A级") * 1.0 / originalTotal) + "\t"
				+ nt.format(numMap.get("B级") * 1.0 / originalTotal) + "\t"
				+ nt.format(numMap.get("C级") * 1.0 / originalTotal) + "\t"
				+ nt.format(numMap.get("D级") * 1.0 / originalTotal) + "\t"
				+ nt.format(numMap.get("E级") * 1.0 / originalTotal) + "\n");

		numMap = sourceRateMap.get("推荐数据分级");
		recommendTotal = numMap.get("A级") + numMap.get("B级") + numMap.get("C级") + numMap.get("D级") + numMap.get("E级")
				+ numMap.get("other级");
		fw.write("推荐数据分级\t" + numMap.get("A级") + "\t" + numMap.get("B级") + "\t" + numMap.get("C级") + "\t"
				+ numMap.get("D级") + "\t" + numMap.get("E级") + "\t" + numMap.get("other级") + "\n");
		fw1.write("推荐数据分级\t" + nt.format(numMap.get("A级") * 1.0 / recommendTotal) + "\t"
				+ nt.format(numMap.get("B级") * 1.0 / recommendTotal) + "\t"
				+ nt.format(numMap.get("C级") * 1.0 / recommendTotal) + "\t"
				+ nt.format(numMap.get("D级") * 1.0 / recommendTotal) + "\t"
				+ nt.format(numMap.get("E级") * 1.0 / recommendTotal) + "\n");

		expoTotal = expoMap.get("A") + expoMap.get("B") + expoMap.get("C") + expoMap.get("D") + expoMap.get("E")
				+ expoMap.get("other");
		fw.write("曝光分布\t" + expoMap.get("A") + "\t" + expoMap.get("B") + "\t" + expoMap.get("C") + "\t"
				+ expoMap.get("D") + "\t" + expoMap.get("E") + "\t" + expoMap.get("other") + "\n");
		fw1.write("曝光分布\t" + nt.format(expoMap.get("A") * 1.0 / expoTotal) + "\t"
				+ nt.format(expoMap.get("B") * 1.0 / expoTotal) + "\t" + nt.format(expoMap.get("C") * 1.0 / expoTotal)
				+ "\t" + nt.format(expoMap.get("D") * 1.0 / expoTotal) + "\t"
				+ nt.format(expoMap.get("E") * 1.0 / expoTotal) + "\n");
		long expoA = expoMap.get("A");
		long expoB = expoMap.get("B");
		long expoC = expoMap.get("C");
		long expoD = expoMap.get("D");
		long expoE = expoMap.get("E");

		pvTotal = pvMap.get("A") + pvMap.get("B") + pvMap.get("C") + pvMap.get("D") + pvMap.get("E")
				+ pvMap.get("other");
		fw.write("点击分布\t" + pvMap.get("A") + "\t" + pvMap.get("B") + "\t" + pvMap.get("C") + "\t" + pvMap.get("D")
				+ "\t" + pvMap.get("E") + "\t" + pvMap.get("other") + "\n");
		fw1.write("点击分布\t" + nt.format(pvMap.get("A") * 1.0 / pvTotal) + "\t"
				+ nt.format(pvMap.get("B") * 1.0 / pvTotal) + "\t" + nt.format(pvMap.get("C") * 1.0 / pvTotal) + "\t"
				+ nt.format(pvMap.get("D") * 1.0 / pvTotal) + "\t" + nt.format(pvMap.get("E") * 1.0 / pvTotal) + "\n");
		long pvA = pvMap.get("A");
		long pvB = pvMap.get("B");
		long pvC = pvMap.get("C");
		long pvD = pvMap.get("D");
		long pvE = pvMap.get("E");

		storeTotal = storeMap.get("A") + storeMap.get("B") + storeMap.get("C") + storeMap.get("D") + storeMap.get("E")
				+ storeMap.get("other");
		fw.write("收藏分布\t" + storeMap.get("A") + "\t" + storeMap.get("B") + "\t" + storeMap.get("C") + "\t"
				+ storeMap.get("D") + "\t" + storeMap.get("E") + "\t" + storeMap.get("other") + "\n");
		fw1.write("收藏分布\t" + nt.format(storeMap.get("A") * 1.0 / storeTotal) + "\t"
				+ nt.format(storeMap.get("B") * 1.0 / storeTotal) + "\t"
				+ nt.format(storeMap.get("C") * 1.0 / storeTotal) + "\t"
				+ nt.format(storeMap.get("D") * 1.0 / storeTotal) + "\t"
				+ nt.format(storeMap.get("E") * 1.0 / storeTotal) + "\n");

		shareTotal = shareMap.get("A") + shareMap.get("B") + shareMap.get("C") + shareMap.get("D") + shareMap.get("E")
				+ shareMap.get("other");
		fw.write("分享分布\t" + shareMap.get("A") + "\t" + shareMap.get("B") + "\t" + shareMap.get("C") + "\t"
				+ shareMap.get("D") + "\t" + shareMap.get("E") + "\t" + shareMap.get("other") + "\n");
		fw1.write("分享分布\t" + nt.format(shareMap.get("A") * 1.0 / shareTotal) + "\t"
				+ nt.format(shareMap.get("B") * 1.0 / shareTotal) + "\t"
				+ nt.format(shareMap.get("C") * 1.0 / shareTotal) + "\t"
				+ nt.format(shareMap.get("D") * 1.0 / shareTotal) + "\t"
				+ nt.format(shareMap.get("E") * 1.0 / shareTotal) + "\n");

		commentTotal = commentMap.get("A") + commentMap.get("B") + commentMap.get("C") + commentMap.get("D")
				+ commentMap.get("E") + commentMap.get("other");
		fw.write("评论分布\t" + commentMap.get("A") + "\t" + commentMap.get("B") + "\t" + commentMap.get("C") + "\t"
				+ commentMap.get("D") + "\t" + commentMap.get("E") + "\t" + commentMap.get("other") + "\n");
		fw1.write("评论分布\t" + nt.format(commentMap.get("A") * 1.0 / commentTotal) + "\t"
				+ nt.format(commentMap.get("B") * 1.0 / commentTotal) + "\t"
				+ nt.format(commentMap.get("C") * 1.0 / commentTotal) + "\t"
				+ nt.format(commentMap.get("D") * 1.0 / commentTotal) + "\t"
				+ nt.format(commentMap.get("E") * 1.0 / commentTotal) + "\n");
		fw1.write("点击率\t" + nt.format(pvA * 1.0 / expoA) + "\t" + nt.format(pvB * 1.0 / expoB) + "\t"
				+ nt.format(pvC * 1.0 / expoC) + "\t" + nt.format(pvD * 1.0 / expoD) + "\t"
				+ nt.format(pvE * 1.0 / expoE) + "\n");

		fw.flush();
		fw.close();
		fw1.flush();
		fw1.close();
		System.out.println("write end");

	}

	public String getSourceRate(String source) {
		String rate = null;
		Jedis jedis = new Jedis("10.90.14.16", 6379, 6000);
		jedis.select(8);
		rate = jedis.get(source);
		if (rate == null) {
			rate = "other";
		}
		jedis.close();
		return rate;
	}

	public static void main(String[] args) throws IOException {
		Cal7DaysSourceRate cal7DaysSourceRate = new Cal7DaysSourceRate();
		cal7DaysSourceRate.output();
		System.out.println("end");
		System.exit(0);
	}

}
