package Entity;


import java.util.ArrayList;


/**
 * 
 * <PRE>
 * 作用 : 
 *   返回查找属于实体的数据结构
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
 *          1.0          2015年5月21日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class EntityQueue {
	private WordStructure wordStructure; // 当前节点

	private ArrayList<WordStructure> parentNodes = new ArrayList<WordStructure>(); // 当前节点的父节点

	private ArrayList<WordStructure> childNodes = new ArrayList<WordStructure>(); // 当前节点的孩子节点

	public WordStructure getWordStructure() {
		return wordStructure;
	}

	public void setWordStructure(WordStructure wordStructure) {
		this.wordStructure = wordStructure;
	}

	public ArrayList<WordStructure> getParentNodes() {
		return parentNodes;
	}

	public void setParentNodes(ArrayList<WordStructure> parentNodes) {
		this.parentNodes = parentNodes;
	}

	public ArrayList<WordStructure> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(ArrayList<WordStructure> childNodes) {
		this.childNodes = childNodes;
	}

	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return wordStructure.toString();
	}
	
	/**
	 * {
    "wordInfo": [
        {
            "word": "施魏因斯泰格",
            "count": "735",
			"category":"足球",
			"levels":["足球", "德甲", "拜仁"],
			"nicks":["托比亚斯·施魏因斯泰格", "巴斯蒂安·施魏因施泰格", "Bastian Schweinsteiger","小猪"]
        },
        {
            "word": "施魏因斯泰格",
            "count": "735",
			"category":"足球",
			"levels":["足球", "德甲", "拜仁"],
			"nicks":["托比亚斯·施魏因斯泰格", "巴斯蒂安·施魏因施泰格", "Bastian Schweinsteiger","小猪"]
        }
    ]
}
	 */
}
