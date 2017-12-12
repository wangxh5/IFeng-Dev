package Hbase;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import utils.JsonUtils;
import utils.LoadConfig;

public class HbaseOperation {
	static Logger LOG = Logger.getLogger(HbaseOperation.class);

	private Configuration conf = HBaseConfiguration.create();
	private Connection con = null;
	private static String familyName = "info";
	private static String columnName = "jsonItemf";
	private static String importColumn = "importDate";
	private static String modifyColumn = "modifyTime";
	private static final int HASH_CODE = 499;
	public static final String CONTENT_TableName = "news_itemf";
	public static final String INDEX_TableName = "news_itemf_index";

	public HbaseOperation() {
		String HBasePort = LoadConfig.lookUpValueByKey("HBasePort");
		String HBaseIP = LoadConfig.lookUpValueByKey("HBaseIP");
		conf.set("hbase.zookeeper.property.clientPort", HBasePort);
		// conf.set("hbase.zookeeper.quorum",
		// "10.21.6.86,10.21.6.87,10.21.6.88");//测试
		conf.set("hbase.zookeeper.quorum", HBaseIP);
		try {
			con = ConnectionFactory.createConnection(conf);
		} catch (IOException e) {
			LOG.error("[HBase] " + e.getMessage());
			// e.printStackTrace();
		}
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	// 判断表是否存在
	private boolean isExist(String tableName) throws IOException {
		Admin hAdmin = con.getAdmin();
		boolean exists = false;
		exists = hAdmin.tableExists(TableName.valueOf(tableName));
		hAdmin.close();
		return exists;
	}

	/**
	 * 为表添加数据（适合知道有多少列族的固定表）
	 *
	 * @rowKey rowKey 1.自有cmpp数据：cmpp_4773047 2.imcp数据： imcp_112320266
	 *         3.新的cmpp数据：cmpp_033760049820820 4.subid数据： sub_xxxxxxx
	 * @tableName 表名
	 * @param value
	 *            对应的itemfJson字串 或索引指向的ID
	 */
	public void put(String tableName, String rowKey, String value) {
		// 设置rowkey
		if (rowKey == null || rowKey.isEmpty()) {
			LOG.error("[HBase] Adding null/empty key!");
			return;
		}

		String ultimateRowKey = getHashedID(rowKey);
		if (ultimateRowKey == null || ultimateRowKey.isEmpty()) {
			LOG.error("[HBase] Adding null/empty hashed key! Original key is " + rowKey);
			return;
		}

		boolean ifInsertTime = false;
		if (value == null) {
			LOG.error("[HBase] Adding null/empty value! Key is " + rowKey);
			return;
		}
		String[] strArray = value.split("\"modifyTime\":", 2);
		String time = null;
		if (strArray.length == 2) {
			time = strArray[1].substring(0, strArray[1].indexOf(","));
			ifInsertTime = true;
		}

		Put put = new Put(Bytes.toBytes(ultimateRowKey));
		// HTabel负责跟记录相关的操作如增删改查等
		Table table = null;
		try {
			table = con.getTable(TableName.valueOf(tableName));
			put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName), Bytes.toBytes(value));
			if (ifInsertTime) {
				long modifyTime = Long.valueOf(time);
				Date date = new Date(modifyTime);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = sdf.format(date);
				put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(modifyColumn), Bytes.toBytes(formattedDate));
			}
			table.put(put);
		} catch (IOException e) {
			LOG.error("[HBase] Failed to connect to table while adding!  " + e.getMessage());
		} finally {
			try {
				if (table != null)
					table.close();
			} catch (IOException e) {
				LOG.error("[HBase] Error while closing table " + e.getMessage());
			}
		}
	}

	/**
	 * 批量写入内容数据
	 * 
	 * @param <T>
	 * 
	 * @param tableName
	 *            写入的表名
	 * @param items
	 *            要写入的itemf map
	 */
	public <T> void puts(String tableName, Map<String, T> items) {
		if (items == null || items.isEmpty()) {
			LOG.error("[HBase] Adding null/empty item map!");
			return;
		}
		int maxSize = 10000;
		Table table = null;
		try {
			table = con.getTable(TableName.valueOf(tableName));
			int eachSize = Math.min(maxSize, items.size());
			List<Put> puts = new ArrayList<Put>(eachSize);
			int handled = 0;
			for (Entry<String, T> entry : items.entrySet()) {
				String ultimateRowKey = getHashedID(entry.getKey());
				if (ultimateRowKey == null || ultimateRowKey.isEmpty()) {
					LOG.error("[HBase] Adding null/empty hashed key! Original key is " + entry.getKey());
					handled++;
					continue;
				}
				// System.out.println(ultimateRowKey);
				Put put = new Put(Bytes.toBytes(ultimateRowKey));

				String value = null;
				if (entry.getValue().getClass().getName().equals("java.lang.String")) {
					value = (String) entry.getValue();
				} else
					value = JsonUtils.toJson(entry.getValue());
				put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName), Bytes.toBytes(value));
				puts.add(put);
				handled++;
				// 每隔10000,写一次
				if (handled == eachSize) {
					LOG.info("[HBase] Adding " + eachSize + "rows!");
					table.put(puts);
					puts = new ArrayList<Put>(eachSize);
				}
			}
			if (puts.size() > 0)
				table.put(puts);
		} catch (IOException e) {
			LOG.error("[HBase] Error while putting data " + e.getMessage());
		} finally {
			try {
				if (table != null)
					table.close();
			} catch (IOException e) {
				LOG.error("[HBase] Error while closing table " + e.getMessage());
			}
		}
	}

	/**
	 * 根据rowkey查询单条数据
	 *
	 * @rowKey rowKey
	 * @tableName 表名
	 */
	public itemf get(String rowKey) {
		// 先查询索引表
		Table idxTable = null;
		String cntID = null;
		try {
			idxTable = con.getTable(TableName.valueOf(INDEX_TableName));
			String ultimateRowKey = getHashedID(rowKey);
			// System.out.println(ultimateRowKey);
			if (ultimateRowKey == null || ultimateRowKey.isEmpty()) {
				LOG.error("[HBase] Getting hashed null/empty key! Original key is " + rowKey);
				return null;
			}
			Get get = new Get(Bytes.toBytes(ultimateRowKey));
			Result result = idxTable.get(get);

			cntID = JsonUtils.fromJson(
					Bytes.toString(
							result.getValue(Bytes.toBytes(HbaseOperation.familyName), Bytes.toBytes(columnName))),
					String.class);
			// System.out.println(Bytes.toString(result.getValue(
			// Bytes.toBytes(HbaseOperation.familyName),
			// Bytes.toBytes(columnName))));

		} catch (IOException e) {
			LOG.error("[HBase] Failed to connect to " + INDEX_TableName + " while getting!  " + e.getMessage());
			return null;
		}

		Table cntTable = null;
		if (cntID == null) {
			cntID = rowKey;
		}
		try {
			cntTable = con.getTable(TableName.valueOf(CONTENT_TableName));
			String ultimateRowKey = getHashedID(cntID);
			if (ultimateRowKey == null || ultimateRowKey.isEmpty()) {
				LOG.error("[HBase] Getting hashed null/empty key! Original key is " + cntID);
				return null;
			}
			Get get = new Get(Bytes.toBytes(ultimateRowKey));
			Result result = cntTable.get(get);
			itemf item = JsonUtils.fromJson(
					Bytes.toString(
							result.getValue(Bytes.toBytes(HbaseOperation.familyName), Bytes.toBytes(columnName))),
					itemf.class);
			return item;
		} catch (IOException e) {
			LOG.error("[HBase] Failed to connect to " + CONTENT_TableName + " while getting!  " + e.getMessage());
			return null;
		} finally {
			try {
				if (idxTable != null)
					idxTable.close();
			} catch (IOException e) {
				LOG.error("[HBase] Error while closing table " + INDEX_TableName + e.getMessage());
			}

			try {
				if (cntTable != null)
					cntTable.close();
			} catch (IOException e) {
				LOG.error("[HBase] Error while closing table " + CONTENT_TableName + e.getMessage());
			}
		}
	}

	/**
	 * 查询item库，获取item详细信息
	 *
	 * @param keyMap
	 * @param cntTable
	 * @return
	 */
	public Map<String, itemf> gets(List<String> keys) {
		Map<String, itemf> reMap = new HashMap<String, itemf>(keys.size());
		List<Get> listGets = new ArrayList<Get>();
		try {
			Table cntTable = con.getTable(TableName.valueOf(CONTENT_TableName));
			for (int i = 0; i < keys.size(); i++) {
				try {
					String cntID = keys.get(i);
					String rowKey = getHashedID(cntID);
					// System.out.println("rowKey is:" + rowKey);
					if (StringUtils.isBlank(rowKey)) {
						LOG.error("key get rowKey error: {} " + cntID);
						continue;
					}
					Get get = new Get(Bytes.toBytes(rowKey));
					listGets.add(get);
				} catch (Exception e) {
					continue;
				}
			}

			Result[] cntlist = cntTable.get(listGets);
			if (cntlist == null || cntlist.length < 1) {
				LOG.error("batch get cntlist list error.");
			} else {
				for (Result result : cntlist) {
					try {
						itemf item = JsonUtils.fromJson(
								Bytes.toString(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName))),
								itemf.class);
						String key = Bytes.toString(result.getRow());

						if (item != null) {
							if (key != null && key.contains("_")) {
								key = key.substring(key.indexOf("_") + 1);
								reMap.put(key, item);
								keys.remove(key);
							}
						} else {
							LOG.error("Item is Null. ");
						}

					} catch (Exception e) {
						LOG.error("batch getItem error {}", e);
						continue;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("batch getItemBatch list error {}", e);
		}
		for (String id : keys) {
			itemf item = null;
			reMap.put(id, item);
		}
		LOG.info(keys);
		return reMap;
	}

	/**
	 * 遍历查询hbase表
	 *
	 * @tableName 表名
	 * @return key itemfJson 对应的map
	 */
	public List<String> getResultAll(String tableName) {
		Filter filter = new KeyOnlyFilter();
		Scan scan = new Scan();
		scan.setFilter(filter);
		ResultScanner rs = null;
		Table table;
		LOG.info("BEGIN TO READ...");
		try {
			table = con.getTable(TableName.valueOf(tableName));
			rs = table.getScanner(scan);
		} catch (IOException e) {
			LOG.error("[HBase] Failed to connect to table while scanning all result!!  " + e.getMessage());
			return null;
		}
		if (rs != null) {
			List<String> resultList = new ArrayList<String>(30000000);
			int i = 0;
			for (Result result : rs) {
				// List<Cell> cells = result.listCells();
				// cells.
				String r = Bytes.toString(result.getRow());
				if (r.indexOf("cmpp_") < 0 && r.indexOf("imcp_") < 0)
					resultList.add(r);
				i++;
				if (i % 3000 == 0)
					LOG.info("Read 3000 lines!!");
			}
		}
		return null;
	}

	/**
	 * 遍历查询hbase表
	 *
	 * @tableName 表名
	 */
	public ResultScanner getResultScann(String tableName, String start_rowkey, String stop_rowkey) throws IOException {
		Scan scan = new Scan();
		scan.setStartRow(Bytes.toBytes(start_rowkey));
		scan.setStopRow(Bytes.toBytes(stop_rowkey));
		ResultScanner rs = null;
		HTable table = null;
		try {
			table = new HTable(conf, Bytes.toBytes(tableName));
			rs = table.getScanner(scan);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * 查询表中的某一列
	 *
	 * @tableName 表名
	 * @rowKey rowKey
	 */
	public Result getResultByColumn(String tableName, String rowKey, String familyName, String columnName)
			throws IOException {
		HTable table = new HTable(conf, Bytes.toBytes(tableName));
		Get get = new Get(Bytes.toBytes(rowKey));
		get.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName)); // 获取指定列族和列修饰符对应的列
		Result result = table.get(get);
		return result;
	}

	/**
	 * 更新表中的某一列
	 *
	 * @tableName 表名
	 * @rowKey rowKey
	 * @familyName 列族名
	 * @columnName 列名
	 * @value 更新后的值
	 */
	public void updateTable(String tableName, String rowKey, String familyName, String columnName, String value)
			throws IOException {
		HTable table = new HTable(conf, Bytes.toBytes(tableName));
		Put put = new Put(Bytes.toBytes(rowKey));
		put.add(Bytes.toBytes(familyName), Bytes.toBytes(columnName), Bytes.toBytes(value));
		table.put(put);
		System.out.println("update table Success!");
	}

	/**
	 * 查询某列数据的多个版本
	 *
	 * @tableName 表名
	 * @rowKey rowKey
	 * @familyName 列族名
	 * @columnName 列名
	 */
	public Result getResultByVersion(String tableName, String rowKey, String familyName, String columnName)
			throws IOException {
		HTable table = new HTable(conf, Bytes.toBytes(tableName));
		Get get = new Get(Bytes.toBytes(rowKey));
		get.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName));
		get.setMaxVersions(5);
		Result result = table.get(get);
		return result;
	}

	/**
	 * 计数
	 * 
	 * @param tableName
	 * @return
	 * @throws IOException
	 */

	public ResultScanner ScanOnlyRowKey(String tableName) throws IOException {
		Scan scan = new Scan();
		scan.setFilter(new FirstKeyOnlyFilter());
		ResultScanner rs = null;
		Table table = null;
		try {
			table = con.getTable(TableName.valueOf(tableName));
			rs = table.getScanner(scan);
		} catch (IOException e) {
			e.printStackTrace();
		}
		table.close();
		return rs;
	}

	/**
	 * 删除指定的列
	 *
	 * @tableName 表名
	 * @rowKey rowKey
	 * @familyName 列族名
	 * @columnName 列名
	 */
	public void deleteColumn(String tableName, String rowKey, String falilyName, String columnName) throws IOException {
		HTable table = new HTable(conf, Bytes.toBytes(tableName));
		Delete deleteColumn = new Delete(Bytes.toBytes(rowKey));
		deleteColumn.deleteColumns(Bytes.toBytes(falilyName), Bytes.toBytes(columnName));
		table.delete(deleteColumn);
		System.out.println(falilyName + ":" + columnName + "is deleted!");
	}

	/**
	 * 删除指定的列
	 *
	 * @tableName 表名
	 * @rowKey rowKey
	 */
	public void deleteAllColumn(String tableName, String rowKey) throws IOException {
		HTable table = new HTable(conf, Bytes.toBytes(tableName));
		Delete deleteAll = new Delete(Bytes.toBytes(rowKey));
		table.delete(deleteAll);
		System.out.println("all columns are deleted!");
	}

	/**
	 * 删除表
	 *
	 * @tableName 表名
	 */
	public void deleteTable(String tableName) throws IOException {
		HBaseAdmin admin = new HBaseAdmin(conf);
		admin.disableTable(tableName);
		admin.deleteTable(tableName);
		System.out.println(tableName + "is deleted!");
	}

	/**
	 * 打印多个结果集
	 *
	 * @param rs
	 */
	public void printResultScan(ResultScanner rs) {
		try {
			int i = 0;
			for (Result r : rs) {
				i++;
				for (KeyValue kv : r.list()) {
					System.out.println("row:" + Bytes.toString(kv.getRow()));
					System.out.println("family:" + Bytes.toString(kv.getFamily()));
					System.out.println("qualifier:" + Bytes.toString(kv.getQualifier()));
					System.out.println("value:" + Bytes.toString(kv.getValue()));
					System.out.println("timestamp:" + kv.getTimestamp());
					System.out.println("-------------------------------------------");
				}
				if (i > 10) {
					break;
				}
			}
		} finally {
			rs.close();
		}
	}

	/**
	 * 对传入的rowKey hash化避免热点问题
	 * 
	 * @param rowKey
	 * @return
	 */
	private String getHashedID(String rowKey) {
		if (rowKey == null || rowKey.isEmpty()) {
			return null;
		}
		try {
			byte[] btInput = rowKey.getBytes();
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(btInput);
			byte[] resultByteArray = messageDigest.digest();
			int i = 0;
			for (int offset = 0; offset < resultByteArray.length; offset++) {
				i += Math.abs(resultByteArray[offset]);
			}

			int prefix = 1000 + i % HASH_CODE;
			return "" + prefix + "_" + rowKey;
		} catch (NoSuchAlgorithmException e) {
			LOG.error("[HBase] Exception while getting hashed ID!!  " + e.getMessage());
			return null;
			// e.printStackTrace();
		}

	}

	/**
	 * 打印单条结果集
	 *
	 * @param result
	 */
	public void printResult(Result result) {
		for (KeyValue kv : result.list()) {
			System.out.println("family:" + Bytes.toString(kv.getFamily()));
			System.out.println("qualifier:" + Bytes.toString(kv.getQualifier()));
			System.out.println("value:" + Bytes.toString(kv.getValue()));
			System.out.println("Timestamp:" + kv.getTimestamp());
			System.out.println("-------------------------------------------");
		}

	}

	public String getModifyTime(String rowKey) {

		// 先查询索引表
		Table idxTable = null;
		String cntID = null;
		try {
			idxTable = con.getTable(TableName.valueOf(INDEX_TableName));
			String ultimateRowKey = getHashedID(rowKey);
			// System.out.println(ultimateRowKey);
			if (ultimateRowKey == null || ultimateRowKey.isEmpty()) {
				LOG.error("[HBase] Getting hashed null/empty key! Original key is " + rowKey);
				return null;
			}
			Get get = new Get(Bytes.toBytes(ultimateRowKey));
			Result result = idxTable.get(get);

			cntID = JsonUtils.fromJson(
					Bytes.toString(
							result.getValue(Bytes.toBytes(HbaseOperation.familyName), Bytes.toBytes(columnName))),
					String.class);
			// System.out.println(Bytes.toString(result.getValue(
			// Bytes.toBytes(HbaseOperation.familyName),
			// Bytes.toBytes(columnName))));

		} catch (IOException e) {
			LOG.error("[HBase] Failed to connect to " + INDEX_TableName + " while getting!  " + e.getMessage());
			return null;
		}

		Table cntTable = null;
		if (cntID == null) {
			cntID = rowKey;
		}
		try {
			cntTable = con.getTable(TableName.valueOf(CONTENT_TableName));
			String ultimateRowKey = getHashedID(cntID);
			if (ultimateRowKey == null || ultimateRowKey.isEmpty()) {
				LOG.error("[HBase] Getting hashed null/empty key! Original key is " + cntID);
				return null;
			}
			Get get = new Get(Bytes.toBytes(ultimateRowKey));
			Result result = cntTable.get(get);
			String time = Bytes
					.toString(result.getValue(Bytes.toBytes(HbaseOperation.familyName), Bytes.toBytes(modifyColumn)));
			return time;
		} catch (IOException e) {
			LOG.error("[HBase] Failed to connect to " + CONTENT_TableName + " while getting!  " + e.getMessage());
			return null;
		} finally {
			try {
				if (idxTable != null)
					idxTable.close();
			} catch (IOException e) {
				LOG.error("[HBase] Error while closing table " + INDEX_TableName + e.getMessage());
			}

			try {
				if (cntTable != null)
					cntTable.close();
			} catch (IOException e) {
				LOG.error("[HBase] Error while closing table " + CONTENT_TableName + e.getMessage());
			}
		}
	}

	public void close() {
		try {
			con.close();
		} catch (IOException e) {
			LOG.error("[HBase] Exception while closing connection!!  " + e.getMessage());
		}
	}

	public static void main(String[] args) throws IOException {
		HbaseOperation hbOp = new HbaseOperation();

		String tableName = "news_itemf";

		// System.out.println(hbOp.isExist(tableName));

		// itemf item = new itemf();
		// item.setID("test1");
		// item.setFeatures(new
		// ArrayList<String>(Arrays.asList("时政","c","-1.0")));
		// // 为表添加数据
		// hbOp.put(tableName, "test1", JsonUtils.toJson(item));
		// //
		// // // 单条按主键查询
		// itemf resultItem = hbOp.get(tableName, "test1");
		// System.out.println(resultItem);
		IKVOperationv3 op = new IKVOperationv3("appitemdb");

		int start = 18824379;
		int end = 18924379;
		// int end = 18824378;
		String[] ids = new String[end - start];
		for (int i = 0; i < (end - start); i++) {
			ids[i] = "cmpp_" + String.valueOf(start + i);
		}
		Map<String, itemf> map = op.queryItems(ids);
		hbOp.puts(tableName, map);
		hbOp.close();
		op.close();
		/*
		 * // 遍历所有数据 hbOp.getResultAll(tableName);
		 * 
		 * // 根据row key范围遍历查询 // hbOp.printResultScan(getResultScann(tableName,
		 * "1009", "1010")); // hbOp.printResultScan(getResultScann(tableName,
		 * "1", "2"));
		 * 
		 * // 查询某一列的值 hbOp.getResultByColumn("blog2", "rowkey1", "author",
		 * "name");
		 * 
		 * // 更新列 hbOp.updateTable("blog2", "rowkey1", "author", "name", "bin");
		 * 
		 * // 查询某一列的值 hbOp.getResultByColumn("blog2", "rowkey1", "author",
		 * "name");
		 * 
		 * // 查询某列的多版本 hbOp.getResultByVersion("blog2", "rowkey1", "author",
		 * "name");
		 * 
		 * // 删除一列 // hbOp.deleteColumn("blog2", "rowkey1", "author",
		 * "nickname");
		 * 
		 * // 删除所有列 hbOp.deleteAllColumn("blog2", "rowkey1");
		 * 
		 * // 删除表 // hbOp.deleteTable("blog2");
		 * 
		 * ResultScanner rs = ScanOnlyRowKey("userLoadExposure"); try { for
		 * (Result r : rs) { for (KeyValue kv : r.list()) {
		 * System.out.println("row:" + Bytes.toString(kv.getRow()));
		 * System.out.println("family:" + Bytes.toString(kv.getFamily()));
		 * System.out.println("qualifier:" + Bytes.toString(kv.getQualifier()));
		 * System.out .println("value:" + Bytes.toString(kv.getValue()));
		 * System.out.println("timestamp:" + kv.getTimestamp()); System.out
		 * .println("-------------------------------------------"); } } }
		 * finally { rs.close(); }
		 */
	}

}
