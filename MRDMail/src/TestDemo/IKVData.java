package TestDemo;

import java.util.ArrayList;
import java.util.List;

import dataBase.IKVOperationv3;
import dataBase.itemf;
import net.sf.json.JSONObject;

public class IKVData {
	public static void main(String[] args) {
		IKVOperationv3 ikvop = new IKVOperationv3("appitemdb");
		itemf item = ikvop.queryItemF("北汽男排打服广东双外援");
		List<String> locList = new ArrayList<String>();
		String timeSensitive = null;
		String docType = null;
		String source = null;// 稿源
		String otherStr = null;
		String latentTopic = null;
		String qualityEvalLevel = null;
		String category = null;// 分类
		String location = null;
		String id = null;
		List<String> cateList = new ArrayList<String>();
		String specialParam = null;
		if (item == null) {
			System.out.println("item is null");

		} else {
			id = item.getID();
			timeSensitive = item.getTimeSensitive();
			locList = item.getLocList();
			docType = item.getDocType();
			source = item.getSource();// 稿源
			qualityEvalLevel = item.getQualityEvalLevel();
			latentTopic = item.getLatentTopic();
			otherStr = item.getOther();
			specialParam = item.getSpecialParam();
			cateList = item.getFeatures();
			System.out.println(cateList.toString());
			System.out.println(id);
			
			// String articleSource =
			// CheckArticleFrom.checkFromByOther(otherStr);
			// System.out.println(articleSource);
			// System.out.println(timeSensitive);
			// System.out.println(source);
			// System.out.println(latentTopic);
			// System.out.println(qualityEvalLevel);
			// System.out.println(category);
			// System.out.println(docType);
			// System.out.println(specialParam);
			// System.out.println(locList.toString());
			// for (int i = 0; i < locList.size(); i++) {
			// String str = locList.get(i);
			// JSONObject jo = JSONObject.fromObject(str);
			// if (Double.valueOf(jo.getString("weight")) >= 0.5 &&
			// jo.getString("loc") != null
			// && !(jo.getString("loc").isEmpty())) {
			// location = jo.getString("loc");
			// System.out.println(location);
			// } else {
			// continue;
			// }
			//
			// }
		}
		System.exit(0);
	}
}
