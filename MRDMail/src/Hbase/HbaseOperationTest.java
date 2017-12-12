package Hbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import utils.JsonUtils;

public class HbaseOperationTest {
	private static HbaseOperation hbOp = null;
	IKVOperationv3 ikvOp = null;

	@Before
	public void setUp() throws Exception {
		hbOp = new HbaseOperation();
		// ikvOp = new IKVOperationv3("appitemdb");
	}

	@After
	public void tearDown() throws Exception {
		hbOp.close();
		ikvOp.close();
	}

	@Ignore
	public void testPut() {
		itemf item = new itemf();
		item.setID("test1");
		item.setFeatures(new ArrayList<String>(Arrays.asList("时政", "c", "-1.0")));
		// 为表添加数据
		hbOp.put(HbaseOperation.CONTENT_TableName, "test1", JsonUtils.toJson(item));
		//
		String key = "imcp_117399231";
		itemf item1 = ikvOp.queryItemF(key);
		hbOp.put(HbaseOperation.CONTENT_TableName, item1.getID(), JsonUtils.toJson(item1));
	}

	@Test
	public void testGet() {
		// itemf resultItem = hbOp.get("test1");
		// assertNotNull(resultItem);
		List<String> keys = new ArrayList<String>();
		keys.add("cmpp_33370777");
		keys.add("cmpp_33370771");
		keys.add("cmpp_33370772");
		keys.add("cmpp_33370773");
		keys.add("cmpp_33370774");
		Map<String, itemf> map = hbOp.gets(keys);
		// assertNotNull(resultItem);
		for(Entry<String, itemf> entry : map.entrySet()){
			itemf resultItem = entry.getValue();
		System.out.println("start");
		System.out.println("id is " + resultItem.getID());
		System.out.println("title is " + resultItem.getTitle());
		System.out.println("split title is " + resultItem.getSplitTitle());
		// System.out.println("split content is "+resultItem.getSplitContent());
		System.out.println("doc type is " + resultItem.getDocType());
		// System.out.println("other is "+resultItem.getOther());
		System.out.println("publish time is " + resultItem.getPublishedTime());
		System.out.println("source is " + resultItem.getSource());
		System.out.println("url is " + resultItem.getUrl());
		System.out.println("tags is " + resultItem.getTags());
		System.out.println("hot event is " + resultItem.getHotEvent());
		System.out.println("loc list is " + resultItem.getLocList());
		System.out.println("feature is " + resultItem.getFeatures());
		System.out.println("modify time is " + resultItem.getModifyTime());
		System.out.println("sub id is " + resultItem.getSubid());
		System.out.println("new cmpp id is " + resultItem.getNewcmppid());
		System.out.println("zmt id is " + resultItem.getZmtid());
		System.out.println("app id is " + resultItem.getAppId());
		System.out.println("show style is " + resultItem.getShowStyle());
		System.out.println("beauty is " + resultItem.getBeauty());
		System.out.println("category is " + resultItem.getCategory());
		System.out.println("simid is " + resultItem.getSimId());
		System.out.println("AuthLabel is " + resultItem.getAuthLabel());
		System.out.println("ReadableFeatures is " + resultItem.getReadableFeatures());}
	}
	//
	// @Ignore
	// public void testGenDataAuthLabel(){
	// itemf resultItem = hbOp.get("imcp_126118068");
	// System.out.println("AuthLabel is "+resultItem.getAuthLabel());
	// System.out.println("ReadableFeatures is
	// "+resultItem.getReadableFeatures());
	// resultItem.setAuthLabel(HBaseFilling.genDataAuthLabel(resultItem));
	// resultItem = HBaseFilling.getReadableFeature(resultItem);
	// System.out.println("AuthLabel is "+resultItem.getAuthLabel());
	// System.out.println("ReadableFeatures is
	// "+resultItem.getReadableFeatures());
	// }

}
