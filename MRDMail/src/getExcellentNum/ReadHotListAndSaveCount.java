package getExcellentNum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import redis.clients.jedis.Jedis;

/**
 * 记录凤凰热闻榜数据并保存
 * 
 * @author zhangyang6
 *
 */
public class ReadHotListAndSaveCount {

	public static void run() {
		IKVOperationv3 dataob = null;
		Jedis reportJedis = null;
		// int index = 1;
		try {
			SimpleDateFormat fmtDay = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat fmtFilter = new SimpleDateFormat("yyyy-MM-dd");
			String dayTime = fmtDay.format(new Date());
			String dayFilter = fmtFilter.format(new Date()); // 用于检测是否是当日数据
			// 基础数据ikv
			dataob = new IKVOperationv3("appitemdb");
			String[] reArray = readHotList();
			reportJedis = ReportThrowUtil.getReportJedisClient();
			// 组合优质数据查询key
			// String reportKeyHead = dayTime + "_" + "优质数据" + "_" + "优质分级高于C" +
			// "_";
			// 组合当日排重key
			String repeatKey = dayTime + "-" + "优质数据";
			Set<String> repeatSet = reportJedis.smembers(repeatKey);
			// int total = reArray.length;
			for (int i = 0; i < reArray.length; i++) {
				try {
					// System.out.println(index + " of " + total);
					// index++;
					String docId = reArray[i].trim();
					if (repeatSet != null) {
						if (repeatSet.contains(docId)) {
							System.out.println("exists , continue.");
							continue;
						}
					}
					itemf appitem = dataob.queryItemF("cmpp_" + docId);
					if (appitem == null) {
						continue;
					}
					String publishDate = appitem.getPublishedTime();
					if (!publishDate.contains(dayFilter)) {
						System.out.println("old data");
						continue;
					}
					// String other = appitem.getOther();
					// String source = CheckArticleFrom.checkFromByOther(other);
					// 这里借用此结构保存数据而绝非丢弃
					// 反而保存的事优质数据
					// reportJedis.sadd(reportKeyHead + source, docId);
					// 排重保存
					reportJedis.sadd(repeatKey, docId);
					System.out.println(docId);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			dataob.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			System.out.println("run over");
			if (dataob != null) {
				dataob.close();
			}
			if (reportJedis != null) {
				reportJedis.disconnect();
			}
			System.exit(0);
		}

	}

	/**
	 * 读入热闻榜数据
	 */
	public static String[] readHotList() {
		String re = RedisUtil.get("10.90.14.17", 6379, 11, "deviateUserPCT");
		String[] reArray = re.split(",");
		return reArray;
	}

	public static void main(String[] args) {
		run();

		// String key = "repeat_" + 20161219 + "_" + "优质数据";
		// String key1 = 20161217 + "_" + "优质数据" + "_" + "优质分级高于C" + "_";
		// Jedis reportJedis = ReportThrowUtil.getReportJedisClient();
		// Set<String> re = reportJedis.keys(key1 + "*");
		// for(String r:re){
		// reportJedis.del(r);
		// }
		// System.out.println(readHotList().length);
		// System.out.println(re);
		// System.out.println(re.size());
	}
}
