package Entity;


import java.util.ArrayList;



/**
 * <PRE>
 * 作用 : 
 *   知识库初始化
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
 *          1.0          2015年10月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class EntityLibQuery {
	// 初始化实体库
	public static void init() {
		KnowledgeBaseBuild.initEntityTree();
	}
	/**
	 * 
	* @Title:getEntityList
	* @Description: 如果需要获取到全量，则输入 * 
	* @param word
	* @return
	* @author:wuyg1
	* @date:2016年8月16日
	 */
	public static ArrayList<EntityInfo> getEntityList(String word) {
		
		ArrayList<EntityInfo> entityList = new ArrayList<EntityInfo>();
		
		if(word.equals("*")){
			entityList = KnowledgeBaseBuild.getAllEntLib();
		}else{
			entityList = KnowledgeBaseBuild.getObjectList(word);
		}
		
		return entityList;
	}
	
	public static void main(String[] agrs) {
		EntityLibQuery.init();

//		System.out.println(KnowledgeBaseBuild.getObjectList("中国外交部"));
		System.out.println(KnowledgeBaseBuild.getEntityList("中考状元"));
//		System.out.println(KnowledgeBaseBuild.getAllEntLib());
	
	}
}
