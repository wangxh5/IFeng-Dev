package Entity;


import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * <PRE>
 * 作用 : 
 *     用于建树结构  
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
 *          1.0          2015年5月20日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class WordStructure {
   
	private EntityInfo wordInfo;    //每个术语实体的基本信息；
    
	private WordStructure parentNode = null;  //存放父节点
	
	private HashMap<String,CopyOnWriteArrayList<WordStructure>> childNodes = new HashMap<String,CopyOnWriteArrayList<WordStructure>>();  //存放子节点
	
	private boolean leaf = false;
	
	private boolean complete = false;

	public EntityInfo getWordInfo() {
		return wordInfo;
	}

	public void setWordInfo(EntityInfo wordInfo) {
		this.wordInfo = wordInfo;
	}

	public WordStructure getParentNode() {
		return parentNode;
	}

	public void setParentNode(WordStructure parentNode) {
		this.parentNode = parentNode;
	}

	public HashMap<String, CopyOnWriteArrayList<WordStructure>> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(HashMap<String, CopyOnWriteArrayList<WordStructure>> childNodes) {
		this.childNodes = childNodes;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	
	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[wordInfo:"+wordInfo.toString()+"]";
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		WordStructure object = null;
		if(obj instanceof WordStructure){
			object = (WordStructure)obj;
		}else{
			return false;
		}
		if(this.wordInfo.equals(object.wordInfo)){
			return true;
		}
		return false;
	}
	

    

}
