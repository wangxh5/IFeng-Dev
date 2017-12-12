package TestDemo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import Entity.EntityInfo;
import Entity.EntityLibQuery;
import Entity.KnowledgeBaseBuild;
import utils.HttpRequest;

public class Test3 {

	@Test
	public void test() throws UnsupportedEncodingException {
		EntityLibQuery.init();

		String title = "阿里巴巴携中国扶贫基金会打造精准扶贫新模式";
		String query = "http://local.segment.nlp.ifengidc.com/HanlpSegmentService/segmentService?";
		String resultList = HttpRequest.sendPost(query,
				"text=" + URLEncoder.encode(title, "utf-8") + "&posModel=false");
		System.out.println(resultList);
		String words[] = resultList.split("\\[|\\]|,");
		for (String s : words) {
			if (s.trim().isEmpty()) {
				continue;
			}
			System.out.print(s.trim() + "\t");
		}
		String category = null;
		EntityInfo entityInfo = null;

		for (String w : words) {
			if (w.trim() == null || w.isEmpty())
				continue;
			String str = KnowledgeBaseBuild.getEntityList(w.trim());
			// 区分是不是实体词
			if (str == null) {
				continue;
			}
			System.out.println(w.trim());
			System.out.println(str);
			Gson gson = new Gson();
			JsonParser parser = new JsonParser();
			JsonArray Jarray = parser.parse(str).getAsJsonArray();
			for (JsonElement obj : Jarray) {
				entityInfo = gson.fromJson(obj, EntityInfo.class);
				category = entityInfo.getCategory();
				System.out.println(category);
			}
		}
		// String str1 = KnowledgeBaseBuild.getEntityList("精准扶贫");
		// System.out.println(str1);

	}

}
