package thriftTest;

import java.util.ArrayList;
import java.util.List;

public class ClassifyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String id="";
		String title = "";
		List<String> cates=new ArrayList<String>();
//		cates.add("影视");
		List<String> categoryList = new ArrayList<String>();
		categoryList = ClassifyThriftInterface.getCategoryList(id,title,cates);
		System.out.println(categoryList.toString());
	}
}
