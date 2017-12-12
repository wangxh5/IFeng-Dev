package Entity;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import utils.LoadConfig;

import java.io.IOException;
import java.util.*;

/**
 * 
 * <PRE>
 * 作用 : 
 *    将术语从redis库中读出，此类的目的就是便于在对术语读取的时候，不与发布代码进行耦合
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年1月21日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class EntityFromRedis{

	protected static final Logger logger = LoggerFactory
			.getLogger(EntityFromRedis.class);

	/**
	 * @Fields channel : 发布消息的频道name
	 */
	protected String channelName;

	// 发布消息时使用的redis host&port
	protected static String commonDataRedisHost;
	protected static int commonDataRedisPort;

	// 全局使用的Jedis实例
	protected static Jedis publisherJedis;
	
	protected String commonDataDbNum;
	
	/**
	 * @Title: init
	 * @Description: 部分类常量初始化
	 * @author liu_yi
	 * @throws
	 */
	public void init() {
		try {
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			publisherJedis = new Jedis(commonDataRedisHost, commonDataRedisPort);
		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			publisherJedis = null;
		}
	}

/**
 * 
* @Title: EntityFromRedis
* @Description:
* @param channel
* @author:wuyg1
* @date:2016年1月6日
 */
	public EntityFromRedis(String channel,String commonDataDbNum) {
		this.init();
		this.channelName = channel;
		this.commonDataDbNum = commonDataDbNum;
	}
	
	/**
	 * 从redis中获取所有术语
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String, List<String>> readAllFromRedis(
			String keyspattern) throws IOException, ClassNotFoundException {

		Set<String> set = new HashSet<String>();

		int dbname = Integer.valueOf(commonDataDbNum);

		publisherJedis.auth(LoadConfig.lookUpValueByKey("password"));
		
		publisherJedis.select(dbname);

		set.addAll(publisherJedis.keys(keyspattern + "*"));

		HashMap<String, List<String>> dataMap = new HashMap<String, List<String>>();
		logger.info("entityCount:" + set.size());
		logger.info("entities:" + set);
		Iterator<String> iter = set.iterator();
		int size = 0;
		while (iter.hasNext()) {
			String key = iter.next();
			logger.info("filenamekey:" + key);
			size++;
			List<String> dataList = new ArrayList<String>();
			dataList = publisherJedis.lrange(key, 0, -1);
			dataMap.put(key, dataList);
		}
		logger.info("Load entityCount:" + size);
		return dataMap;
	}
	/**
	 * 从redis中获取指定文件的术语
	 * 
	 * @param keys
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String, List<String>> readSomeFileFromRedis(
			ArrayList<String> keys) throws IOException, ClassNotFoundException {
		HashMap<String, List<String>> dataMap = new HashMap<String, List<String>>();
		int dbname = Integer.valueOf(commonDataDbNum);

		publisherJedis.select(dbname);
		for (String key : keys) {
			List<String> dataList = publisherJedis.lrange(key, 0, -1);
			dataMap.put(key, dataList);
		}
		return dataMap;
	}

}
