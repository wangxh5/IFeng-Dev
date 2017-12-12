package getExcellentNum;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 
 *   Jedis操作封装
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
 *          1.0          2015-12-30        jiangmm         create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class RedisUtil {
	private static final Logger log = Logger.getLogger(RedisUtil.class);

	/**
	 * @Title: getJedisClient @Description: 获得jedis客户端 @return @throws
	 */
	private static Jedis getJedisClient(String host, int port) {
		Jedis jedis = null;
		try {
			jedis = new Jedis(host, port, 3000);
		} catch (Exception ex) {
			log.error("Get jedis instance failed:" + ex.getMessage());
		}
		return jedis;
	}

	/**
	 * 获取一个选定db的jedis对象
	 * 
	 * @param host
	 * @param port
	 * @param dbNum
	 * @return
	 */
	public static Jedis getJedisDbClient(String host, int port, int dbNum) {
		Jedis jedis = null;
		try {
			jedis = new Jedis(host, port, 3000);
			jedis.select(dbNum);
		} catch (Exception ex) {
			log.error("Get jedis instance failed:" + ex.getMessage() + " redis: " + host + ", dbNum: " + dbNum);
		}
		return jedis;
	}

	/**
	 * @Title: returnResource @Description: 释放jedis连接资源 @param jedis @throws
	 */
	public static void returnResource(Jedis jedis) {
		if (jedis != null) {
			jedis.disconnect();
			jedis = null;
		}
	}

	/**
	 * @Title: get
	 * @Description: 返回 key所关联的字符串值 ,如果 key不存在那么返回 null
	 * @param userId
	 * @param dbNum
	 * @param key
	 * @return
	 */
	public static String get(String userId, String host, int port, int dbNum, String key) {
		Jedis jedis = getJedisClient(host, port);

		try {
			jedis.select(dbNum);
			String value = jedis.get(key);

			if (value == null || value.isEmpty()) {
				log.info("value of " + key + " in redis is null or empty," + userId);
			} else {
				log.info("Get value of " + key + " success," + userId);
			}
			return value;
		} catch (Exception e) {
			log.error(userId + " get-string-failed:" + key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * @Title: get
	 * @Description: 返回 key所关联的字符串值 ,如果 key不存在那么返回 null
	 * @param userId
	 * @param dbNum
	 * @param key
	 * @return
	 */
	public static String get(String host, int port, int dbNum, String key) {
		Jedis jedis = getJedisClient(host, port);

		try {
			jedis.select(dbNum);
			String value = jedis.get(key);

			if (value == null || value.isEmpty()) {
				log.info("value of " + key + " in redis is null or empty");
			} else {
				log.info("Get value of " + key + " success" + " : " + host + "," + port + "," + dbNum);
			}
			return value;
		} catch (Exception e) {
			log.error(" get-string-failed:" + key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static Map<String, String> hgetAll(String host, int port, int dbNum, String key) {
		Jedis jedis = getJedisClient(host, port);

		try {
			jedis.select(dbNum);

			Map<String, String> map = jedis.hgetAll(key);

			if (map == null || map.isEmpty()) {
				log.info("value of " + key + " in redis is null or empty");
			} else {
				log.info("Get value of " + key + " success,size=" + map.size());
			}
			return map;
		} catch (Exception e) {
			log.error("hgetAll failed:" + key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static String hget(String host, int port, int dbNum, String key, String field) {
		Jedis jedis = getJedisClient(host, port);

		try {
			jedis.select(dbNum);

			String value = jedis.hget(key, field);

			return value;
		} catch (Exception e) {
			log.error("hget failed:" + key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * @Title: setex @Description: 将值 value 关联到 key ，并将 key 的生存时间设为 seconds
	 * (以秒为单位) 如果 key 已经存在， SETEX 命令将覆写旧值 @param dbNum @param key @param
	 * value @param seconds @throws
	 */
	public static void setex(String host, int port, int dbNum, String key, String value, int seconds) {
		Jedis jedis = getJedisClient(host, port);
		try {
			jedis.select(dbNum);
			jedis.setex(key, seconds, value);
		} catch (Exception e) {
			log.error("setex-string-failed:" + key + "," + value + "," + seconds, e);
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * @Title: setex @Description: 将值 value 关联到 key ，并将 key 的生存时间设为 seconds
	 * (以秒为单位) 如果 key 已经存在， SETEX 命令将覆写旧值 @param dbNum @param key @param
	 * value @param seconds @throws
	 */
	public static void hset(String host, int port, int dbNum, String key, String field, String value) {
		Jedis jedis = getJedisClient(host, port);
		try {
			jedis.select(dbNum);
			jedis.hset(key, field, value);
		} catch (Exception e) {
			log.error("hset error:" + key + "," + field, e);
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 保存一条内容(Set)
	 */
	public static void setSetValue(String host, int port, int dbNum, String key, Set<String> value) {
		Jedis jedis = getJedisClient(host, port);
		try {
			jedis.select(dbNum);
			for (String str : value) {
				jedis.sadd(key, str);
			}
		} catch (Exception e) {
			log.error("sadd error : ", e);
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 保存一条内容(String)
	 */
	public static void setStringValue(String host, int port, int dbNum, String key, String value) {
		Jedis jedis = getJedisClient(host, port);
		try {
			jedis.select(dbNum);
			jedis.set(key, value);
		} catch (Exception e) {
			log.error("set error : ", e);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 保存一条map内容
	 * 
	 * @param host
	 * @param port
	 * @param dbNum
	 * @param key
	 * @param value
	 */
	public static void setMapValue(String host, int port, int dbNum, String key, Map<String, String> value) {
		Jedis jedis = getJedisClient(host, port);
		try {
			jedis.select(dbNum);
			if (jedis.exists(key)) {
				jedis.del(key);
			}
			jedis.hmset(key, value);
		} catch (Exception e) {
			log.error("sadd error : ", e);
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 删除一条记录
	 * 
	 * @param args
	 */
	public static void delKey(String host, int port, int dbNum, String key) {
		Jedis jedis = getJedisClient(host, port);
		try {
			jedis.select(dbNum);
			jedis.del(key);
		} catch (Exception e) {
			log.error(e);
		} finally {
			returnResource(jedis);
		}
	}

	public static void main(String[] args) {
		// Jedis jedis = getJedisClient("10.32.21.62",6379);
		//
		// try {
		//
		// for(int i=0;i<16;i++){
		// jedis.select(i);
		//
		// Set<String> value=jedis.keys("*");
		// System.out.println(i+":"+value.size());
		// }
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// return ;
		// } finally {
		// returnResource(jedis);
		// }

		// RedisUtil.setMapValue("10.50.6.126", 6379, 5, "articleInfoMap_2",
		// saveMap);
		// hset("10.50.6.126", 6379, 5, "articleInfoMap_2", "test", "test");
		// String re = hget("10.50.6.126", 6379, 5, "articleInfoMap_2", "test");
		// System.out.println(re);
		// Map s = hgetAll("10.50.6.126", 6379, 5, "articleInfoMap_2");
		// System.out.println(s);
		// System.out.println(s.size());
		// delKey("10.50.6.126", 6379, 5, "articleInfoMap_2");

		// ------------

		// Jedis redisClient = getJedisDbClient("10.32.21.62", 6379, 11);
		Jedis redisClient = getJedisDbClient("10.90.14.17", 6379, 11);
		// Jedis redisClient = getJedisDbClient("10.32.21.62", 6379, 6);
		// Jedis redisClient = getJedisDbClient("10.90.3.52", 6379, 5);
		// Jedis redisClient = getJedisDbClient("10.90.7.52", 6379, 2);
		// Jedis redisClient = getJedisDbClient("10.90.16.23", 6379, 14);
		// Jedis redisClient = getJedisDbClient("10.90.33.72", 6379, 1);
		// Jedis redisClient = getJedisDbClient("10.90.1.56", 6379, 14);
		// Jedis redisClient = getJedisClient("10.90.9.33", 6380);
		// Set<String> re = redisClient.keys("imcp_*");
		// Map<String,String> re = redisClient.hgetAll("6462711");
		// String re = redisClient.get("imcp_114076136");
		// System.out.println(re);
		// System.out.println(re.size());
		String rk = redisClient.get("HotPreload");
		// String re = redisClient.get("ColdData");
		// String re = redisClient.get("deviateUserPCT");
		// Set<String> reSet = redisClient.keys("*");
		// String re = redisClient.randomKey();
		// System.out.println(reSet.size());
		// String rk = redisClient.randomKey();
		System.out.println(rk);
		// System.out.println(redisClient.get(rk));
		// System.out.println(redisClient.get("usp_act_time:865877020204280"));
		// System.out.println(redisClient.hgetAll("video:2:f8b244b7-9353-4bf9-8b2c-65d8101c58cf"));
		// System.out.println(redisClient.exists(rk));

		// Map<String,String> aviPool = redisClient.hgetAll("AviTrueSolrCatch");
		//
		// if(aviPool.containsKey("12537976")){
		// System.out.println("yes");
		// }

		// System.out.println(reSet.size());
		// System.out.println(re);
		// System.out.println(redisClient.get(re));
		// Map<String,String> reMap = redisClient.hgetAll("201701041446");
		// System.out.println(reMap.get("clusterId_4200629"));
		// System.out.println(reMap);
		// System.out.println(reMap.size());

	}
}
