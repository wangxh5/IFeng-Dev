package getExcellentNum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * 上报丢弃原因至汇总redis
 * 
 * <PRE>
 * 作用 : 
 *   
 * 使用 : 丢弃汇报redis：
 * 		10.32.24.86:6379
 * 		db2 ---> 丢弃文章记录,保存set结构: key: yyyyMMdd_mainReason_detailedReason_source value: id or urls Set
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016-11-23        zhangyang6          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class ReportThrowUtil {

	/**
	 * 丢弃信息汇报
	 */
	public static void reportThrowReason(String mainReason, String detailedReason, String otherStr, String throwTing) {
		if (mainReason == null || otherStr == null || mainReason.isEmpty() || otherStr.isEmpty()) {
			return;
		}
		Jedis reportClient = null;
		String saveKey = null;
		try {
			// 时间获取,当天时间
			SimpleDateFormat fmtDay = new SimpleDateFormat("yyyyMMdd");
			String dayTime = fmtDay.format(new Date());
			// 来源获取
			String sourceFrom = CheckArticleFrom.checkFromByOther(otherStr);
			if (sourceFrom == null) {
				sourceFrom = "未分类数据";
			}
			// jedis获取
			reportClient = getReportJedisClient();
			if (detailedReason != null) {
				saveKey = dayTime + "_" + mainReason + "_" + detailedReason + "_" + sourceFrom;
			} else {
				saveKey = dayTime + "_" + mainReason + "_" + detailedReason + "_" + sourceFrom;
			}
			reportClient.sadd(saveKey, throwTing);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			saveKey = null;
			if (reportClient != null) {
				reportClient.disconnect();
				reportClient = null;
			}
		}
	}

	/**
	 * 丢弃信息汇报自带来源
	 */
	public static void reportThrowReasonWithSource(String mainReason, String detailedReason, String source,
			String throwTing) {
		if (mainReason == null || source == null || mainReason.isEmpty() || source.isEmpty()) {
			return;
		}
		Jedis reportClient = null;
		String saveKey = null;
		try {
			// 时间获取,当天时间
			SimpleDateFormat fmtDay = new SimpleDateFormat("yyyyMMdd");
			String dayTime = fmtDay.format(new Date());
			// 来源获取
			String sourceFrom = source;
			if (sourceFrom == null) {
				sourceFrom = "未分类数据";
			}
			// jedis获取
			reportClient = getReportJedisClient();
			if (detailedReason != null) {
				saveKey = dayTime + "_" + mainReason + "_" + detailedReason + "_" + sourceFrom;
			} else {
				saveKey = dayTime + "_" + mainReason + "_" + detailedReason + "_" + sourceFrom;
			}
			reportClient.sadd(saveKey, throwTing);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			saveKey = null;
			if (reportClient != null) {
				reportClient.disconnect();
				reportClient = null;
			}
		}
	}
	


	/**
	 * 获取保存丢弃汇报jedisClient
	 */
	public static Jedis getReportJedisClient() {
		Jedis jedis = null;
		try {
			jedis = new Jedis("10.90.7.52", 6379, 3000);
			jedis.select(4);
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return jedis;
	}

	public static void main(String[] args) {
		Jedis j = getReportJedisClient();
		Set<String> rs = j.keys("20161127*");
		System.out.println(rs);
		System.out.println(rs.size());
		// Set<String> re = j.smembers("20161124_抓站丢弃_黑名单稿源_机器抓取");
		// System.out.println(re);
		j.disconnect();
	}

}
