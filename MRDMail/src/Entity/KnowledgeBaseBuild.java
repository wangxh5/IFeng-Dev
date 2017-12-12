package Entity;


import com.google.gson.Gson;

import utils.LoadConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 
 * <PRE>
 * 作用 : 
 *      构建垂直领域的术语实体库，比如足球领域，包括联赛名称、球队名称、球员信息（别名）
 * 使用 : 
 *      包括上下级关联查询，比如查找的是某只球队的话，可以反馈出该球队所属的联赛及该俱乐部的球员信息集合等，目前上级查询到树根，而下级查询则只有下一级一层信息。
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
public class KnowledgeBaseBuild {

	static Log LOG = LogFactory.getLog(KnowledgeBaseBuild.class);

	public static ArrayList<EntityInfo> wordInfos = new ArrayList<EntityInfo>(); // 存放术语实体集合

	public static WordStructure rootNode = null; // 垂直领域的根节点

	public static ReadWriteLock rwl = new ReentrantReadWriteLock();

	public static HashMap<String, ArrayList<ArrayList<String>>> indexMap = new HashMap<String, ArrayList<ArrayList<String>>>();// 对所有的术语词建立索引

	public static final int FATHER = 1; // 得到query实体的父节点

	public static final int SENIORITY = 2; // 得到query实体的所有长辈节点

	public static final int CHILDREN = 3; // 得到query实体的所有孩子节点

	public static String entLibKeyPattern = LoadConfig
			.lookUpValueByKey("entLibKeyPattern");

	/**
	 * 初始化根节点，所有领域的根节点word=-1;
	 */
	private static void initRootNode() {

		if (null == rootNode) {
			EntityInfo rootInfo = new EntityInfo(); // 生成所有类别的总根节点

			rootNode = new WordStructure();

			rootInfo.setWord("-1");
			rootNode.setWordInfo(rootInfo);
			rootNode.setParentNode(null);
		}
	}

	/**
	 * 从redis中读出术语，同时，构建术语库的树形结构,对术语库的树结构进行重构的过程中相当于写操作，应该是写写互斥的
	 * 
	 *
	 */
	public static void initEntityTree() {
		rwl.writeLock().lock();

		try {
			LOG.info("init entity tree");
			KnowledgeBaseBuild.initRootNode();// 初始化总根节点，只需要初始化一次，作为所有领域类别的根

			EntityFromRedis entityFromRedis = new EntityFromRedis(
					LoadConfig.lookUpValueByKey("entLibMessageChannel"),
					LoadConfig.lookUpValueByKey("commonDataDbNum"));

			HashMap<String, List<String>> dataMap = entityFromRedis
					.readAllFromRedis(entLibKeyPattern);

			Iterator<String> iterator = dataMap.keySet().iterator();
			wordInfos.clear();
			while (iterator.hasNext()) {
				String filename = (String) iterator.next();

				List<String> dataList = dataMap.get(filename);

				for (String record : dataList) {

					wordInfos.add(EntityInfo.redis2Object(record, filename));
				}
			}
			buildTree(wordInfos); // 构建树结构

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			rwl.writeLock().unlock();
		}

	}


	/**
	 * 将一条术语记录转为EntityInfo对象，为了便于生成术语库的结构树
	 * 
	 * @param record
	 * @return
	 */
	private static EntityInfo string2EntityInfo(String record) {

		EntityInfo entityInfo = new EntityInfo();
		String word = record.substring(
				record.indexOf("word:") + "word:".length(),
				record.indexOf("  count:"));
		entityInfo.setWord(word);

		String count = record.substring(
				record.indexOf("count:") + "count:".length(),
				record.indexOf("  category:"));
		int icount = Integer.valueOf(count);

		entityInfo.setCount(icount);

		String category = record.substring(record.indexOf("category:")
				+ "category:".length(), record.indexOf(" filename:"));

		entityInfo.setCategory(category);

		String filenames = record.substring(record.indexOf("filename:")
				+ "filename:".length(), record.indexOf("  levels:"));

		entityInfo.setFilename(filenames);

		String levels = record.substring(record.indexOf("levels:[")
				+ "levels:[".length(), record.indexOf("]  nicks:"));

		List<String> levelList = commenFuncs.getList(levels);

		if (!levelList.get(0).equalsIgnoreCase(category)) {
			levelList.add(0, category);
		}

		if (!levelList.get(levelList.size() - 1).contains("术语")) {
			levelList.add("术语");
		}

		String nicks = record.substring(record.indexOf("nicks:[")
				+ "nicks:[".length());

		nicks = nicks.substring(0, nicks.indexOf("]"));

		List<String> nickList = commenFuncs.getList(nicks);

		nickList.remove(word);

		entityInfo.getNicknameList().addAll(nickList);

		entityInfo.getLevels().addAll(levelList);

		return entityInfo;
	}

	/**
	 * 在总根节点下面，初始化领域节点
	 * 
	 * @param domainName
	 */
	@SuppressWarnings("unused")
	private static void initDomainRootNode(String domainName) {

		EntityInfo wordInfo = new EntityInfo();
		wordInfo.setWord(domainName);

		WordStructure wordStructure = new WordStructure();
		wordStructure.setWordInfo(wordInfo);
		wordStructure.setParentNode(rootNode);
		CopyOnWriteArrayList<WordStructure> wordStructures = new CopyOnWriteArrayList<WordStructure>();
		wordStructures.add(wordStructure);
		rootNode.getChildNodes().put(domainName, wordStructures);
	}

	/**
	 * 创建中间节点
	 * 
	 *
	 * @return
	 */
	private static WordStructure createNode(EntityInfo entityInfo, int index) {

		WordStructure wordStructure = new WordStructure();

		EntityInfo wordInfo = new EntityInfo();

		wordInfo.setWord(entityInfo.getLevels().get(index));

		wordInfo.setCategory(entityInfo.getCategory());

		wordStructure.setWordInfo(wordInfo);

		wordStructure.setComplete(false);

		return wordStructure;
	}

	/**
	 * 构建树过程
	 * 
	 * @param wordInfos
	 */
	private static <T> void buildTree(ArrayList<T> wordInfos) {

		for (T record : wordInfos) {
			EntityInfo entityInfo = (EntityInfo) record;

			addNode2Tree(record);
		}

	}

	/**
	 * 添加单个节点到树结构
	 * 
	 * @param record
	 */
	private static <T> void addNode2Tree(T record) {
		if (null == record) {
			return;
		}

		WordStructure curStructure = new WordStructure();
		curStructure = rootNode;

		boolean isLeaf = true; // 判断该节点是否为中间节点

		Set<String> nameSet = new HashSet<String>();

		EntityInfo wordInfo = null;

		if (record instanceof String) {
			wordInfo = string2EntityInfo((String) record);
		} else if (record instanceof EntityInfo) {
			wordInfo = (EntityInfo) record;
		}

		if (null == wordInfo) {
			return;
		}

		ArrayList<EntityInfo> entityInfos = KnowledgeBaseBuild
				.getObjectList(wordInfo.getWord());

		if (!(null == entityInfos || entityInfos.isEmpty())) {
			boolean isExist = false;
			for (EntityInfo entityInfo : entityInfos) {
				if (entityInfo.equals(wordInfo)) {
					isExist = true;
					break;
				}
			}
			if (isExist) {
				return;
			}
		}

		nameSet.add(wordInfo.getWord());

		if (wordInfo.getNicknameList().size() > 1
				|| (wordInfo.getNicknameList().size() == 1 && wordInfo
						.getNicknameList().get(0).trim().length() > 0)) {
			nameSet.addAll(wordInfo.getNicknameList());
		}

		Iterator<String> iterator = nameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();

			name = name.toLowerCase();

			if (indexMap.containsKey(name)) {
				if (!indexMap.get(name).contains(wordInfo.getLevels())) {
					indexMap.get(name).add(wordInfo.getLevels());
				}
			} else {
				ArrayList<ArrayList<String>> levelsList = new ArrayList<ArrayList<String>>();
				levelsList.add(wordInfo.getLevels());
				indexMap.put(name, levelsList);
			}

		}

		ArrayList<String> levels_category = new ArrayList<String>();

		if (!wordInfo.getLevels().get(0)
				.equalsIgnoreCase(wordInfo.getCategory())) {
			levels_category.add(wordInfo.getCategory());
		}
		levels_category.addAll(wordInfo.getLevels());

		// 遍历中间节点，遇到没有存在的节点，即创建节点，同时添加入树结构中
		for (int index = 0; index < levels_category.size(); index++) {

			if (null == levels_category.get(index)) {
				continue;
			}

			String entity = levels_category.get(index);

			boolean isCurLevel = false; // 如果wordInfo为中间节点，且
										// wordInfo.getWord().equals(entity)的话，则令isCurLevel=true;
										// 那么在加入节点的时候，需要将wordInfo全部载入WordStructure

			if (entity == null) {
				continue;
			}

			if (visitTree(curStructure, entity)) {// 如果存在于树结构中，则继续遍历

				if (!isCurLevel) {
					curStructure = curStructure.getChildNodes().get(entity)
							.get(0);// 由于存在于当前层，因此往下一级遍历
				}

			} else {
				if (!isCurLevel) {
					insertNode(curStructure, wordInfo, index);

					curStructure = curStructure.getChildNodes().get(entity)
							.get(0); // 在visitTree的过程中，发现entity不存在于树结构中，因此直接创建，故而，可以遍历下一级
				}

			}

			if (isCurLevel) {

				if (curStructure.getChildNodes()
						.containsKey(wordInfo.getWord())) {
					WordStructure leaf = new WordStructure();
					leaf.setWordInfo(wordInfo);
					leaf.setComplete(true);
					leaf.setParentNode(curStructure);
					curStructure.getChildNodes().get(wordInfo.getWord()).get(0)
							.setWordInfo(wordInfo);
					curStructure.getChildNodes().get(wordInfo.getWord()).get(0)
							.setComplete(true);

					curStructure = curStructure.getChildNodes().get(entity)
							.get(0);
				} else {
					WordStructure leaf = new WordStructure();
					leaf.setWordInfo(wordInfo);
					leaf.setComplete(true);
					leaf.setParentNode(curStructure);
					CopyOnWriteArrayList<WordStructure> wordStructures = new CopyOnWriteArrayList<WordStructure>();
					wordStructures.add(leaf);
					curStructure.getChildNodes().put(wordInfo.getWord(),
							wordStructures);
					curStructure = curStructure.getChildNodes().get(entity)
							.get(0);
				}

			}

		}

		// 如果该节点为叶子节点，则构建叶子节点
		if (isLeaf) {
			WordStructure leaf = new WordStructure();
			leaf.setWordInfo(wordInfo);
			leaf.setParentNode(curStructure);
			leaf.setComplete(true);
			leaf.setLeaf(true);
			if (curStructure.getChildNodes().containsKey(wordInfo.getWord())) {
				curStructure.getChildNodes().get(wordInfo.getWord()).add(leaf);
			} else {
				CopyOnWriteArrayList<WordStructure> wordStructures = new CopyOnWriteArrayList<WordStructure>();
				wordStructures.add(leaf);
				curStructure.getChildNodes().put(wordInfo.getWord(),
						wordStructures);
			}

		}
	}

	/**
	 * 找到query术语实体对应的所有上级节点，和下一级的孩子节点
	 * 
	 * @param wordStructures
	 *            query所指的所有节点
	 * @return
	 */
	private static ArrayList<EntityQueue> getNodes(
			ArrayList<WordStructure> wordStructures) {
		ArrayList<EntityQueue> entityQueues = new ArrayList<EntityQueue>();

		for (WordStructure wordStructure : wordStructures) {

			EntityQueue entityQueue = new EntityQueue();
			entityQueue.setWordStructure(wordStructure);
			entityQueue.setParentNodes(getSingleParentNodes(wordStructure));// 得到父节点序列
			entityQueue.setChildNodes(getSingleChildNodes(wordStructure));// 得到孩子节点

			entityQueues.add(entityQueue);
		}

		return entityQueues;
	}

	private static ArrayList<EntityInfo> getWordInfos() {
		return wordInfos;
	}

	/**
	 * 
	 * @Title:getAllEntLib
	 * @Description:获取到EntLib的全量
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月16日
	 */
	public static ArrayList<EntityInfo> getAllEntLib() {
		return getWordInfos();
	}

	/**
	 * 
	 *
	 *            query在知识结构中对应的所有节点
	 * @param flag
	 *            1:得到query的父节点 2:得到query的所有长辈节点 3：得到query的所有孩子节点
	 * @return
	 */
	@SuppressWarnings("unused")
	private static ArrayList<EntityQueue> getNodes(
			ArrayList<EntityQueue> enArrayList, int flag) {
		ArrayList<EntityQueue> entityQueues = new ArrayList<EntityQueue>();

		for (EntityQueue en : enArrayList) {

			EntityQueue entityQueue = new EntityQueue();

			entityQueue.setWordStructure(en.getWordStructure());

			if (flag == FATHER) { // 得到wordStructure的父节点
				entityQueue.getParentNodes().add(en.getParentNodes().get(0));// 得到父节点序列
			}

			if (flag == SENIORITY) { // 得到wordStructure的所有长辈节点
				entityQueue.getParentNodes().addAll(en.getParentNodes());
			}

			if (flag == CHILDREN) { // 得到wordStructure的孩子节点
				entityQueue.getChildNodes().addAll(en.getChildNodes());
			}
			entityQueues.add(entityQueue);
		}
		return entityQueues;
	}

	/**
	 * 得到某一个术语实体的父节点和孩子节点信息
	 * 
	 * @param wordStructure
	 *            当前节点
	 * @return
	 */
	@SuppressWarnings("unused")
	private static EntityQueue getNodes(WordStructure wordStructure) {

		EntityQueue entityQueue = new EntityQueue();
		entityQueue.setWordStructure(wordStructure);
		entityQueue.setParentNodes(getSingleParentNodes(wordStructure));// 得到父节点序列
		entityQueue.setChildNodes(getSingleChildNodes(wordStructure));// 得到孩子节点

		return entityQueue;

	}

	/**
	 * 得到单一节点wordStructure指代的父节点
	 * 
	 * @param wordStructure
	 * @return
	 */
	private static ArrayList<WordStructure> getSingleParentNodes(
			WordStructure wordStructure) {
		ArrayList<WordStructure> arrayList = new ArrayList<WordStructure>();
		WordStructure curStructure = wordStructure;
		while (curStructure.getParentNode() != null) {
			if (!curStructure.getParentNode().getWordInfo().getWord()
					.equals("-1")) {
				arrayList.add(curStructure.getParentNode());
				curStructure = curStructure.getParentNode();
			} else {
				break;
			}
		}
		return arrayList;

	}

	/**
	 * 得到单一节点wordStructure指代的所有孩子节点
	 * 
	 * @param wordStructure
	 * @return
	 */
	private static ArrayList<WordStructure> getSingleChildNodes(
			WordStructure wordStructure) {
		ArrayList<WordStructure> arrayList = new ArrayList<WordStructure>();

		if (!wordStructure.isLeaf()) {// 如果当前节点不是叶子节点，则获取其对应的所有孩子节点
			HashMap<String, CopyOnWriteArrayList<WordStructure>> hashMap = wordStructure
					.getChildNodes();
			Iterator<String> iterator = hashMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				arrayList.addAll(hashMap.get(key));
			}
		}
		return arrayList;
	}

	/**
	 * 返回查询词对应的树结构索引路径，如果返回null，表示树结构中没有该节点存在
	 * 
	 * @param key
	 * @return
	 */
	private static ArrayList<ArrayList<String>> getIndexRoute(String key) {
		return indexMap.get(key);
	}

	/**
	 * 根据indexRouteList提供的实体路径索引查找query实体，如果query对应知识结构中的多个实体，则返回所有的节点。
	 * 
	 * @param query
	 *            待查询实体
	 */
	private static ArrayList<WordStructure> searchNode(String query,
			ArrayList<ArrayList<String>> indexRouteList) {

		ArrayList<WordStructure> wordStructures = new ArrayList<WordStructure>(); // 用于返回查找的节点集合

		for (ArrayList<String> indexRoute : indexRouteList) {
			WordStructure cur_word = new WordStructure();
			cur_word = rootNode;
			for (String index : indexRoute) {
				cur_word = cur_word.getChildNodes().get(index).get(0);
			}
			Iterator<String> iterator = cur_word.getChildNodes().keySet()
					.iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				// WordStructure wordStructure =
				// cur_word.getChildNodes().get(key);
				// if (key.equalsIgnoreCase(query)
				// || wordStructure.getWordInfo().contains(
				// wordStructure.getWordInfo().getNicknameList(),
				// query)) {
				// wordStructures.add(wordStructure);
				// }

				CopyOnWriteArrayList<WordStructure> wordStructure = cur_word
						.getChildNodes().get(key);

				for (WordStructure ws : wordStructure) {
					if (key.equalsIgnoreCase(query)
							|| ws.getWordInfo().contains(
									ws.getWordInfo().getNicknameList(), query)) {
						wordStructures.add(ws);
					}
				}
			}

		}

		return wordStructures;
	}

	/**
	 * 在树结构中查找query实体，如果query对应知识结构中的多个实体，则返回所有的节点。
	 * 
	 * @param query
	 *            待查询实体
	 */
	private static ArrayList<WordStructure> searchNode(String query) {

		ArrayList<WordStructure> wordStructures = new ArrayList<WordStructure>(); // 用于返回查找的节点集合

		Queue<WordStructure> queue = new LinkedList<WordStructure>();
		queue.add(rootNode);

		while (!queue.isEmpty()) {
			WordStructure curStructure = queue.poll();

			if (curStructure.getWordInfo().getWord() != null
					&& (curStructure.getWordInfo().getWord().trim()
							.equalsIgnoreCase(query) // &&
														// curStructure.getChildNodes().size()
														// == 0 //
														// 如果当前节点是query要查询的节点，即Word.equals(query)
					|| curStructure.getWordInfo()
							.contains(
									curStructure.getWordInfo()
											.getNicknameList(), query) // 或者nicknameList.contains(query)
					) && curStructure.getWordInfo().getLevels().size() > 0) {
				wordStructures.add(curStructure);
			}

			HashMap<String, CopyOnWriteArrayList<WordStructure>> childHashMap = curStructure
					.getChildNodes(); // 继续遍历curStructure的孩子节点，目的是为了找到同名的其他节点
			Iterator<String> iterator = childHashMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				queue.addAll(childHashMap.get(key));
			}
		}

		return wordStructures;
	}

	/**
	 * 该实体是否在树中存在
	 * 
	 * @param entity
	 * @return
	 */
	private static boolean visitTree(WordStructure parentStructure,
			String entity) {

		if (parentStructure.getChildNodes().containsKey(entity)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 该实体是否在树中存在，如果不存在加入该节点
	 * 
	 *
	 * @return
	 */
	private static void insertNode(WordStructure parentStructure,
			EntityInfo entityInfo, int index) {

		WordStructure wordStructure = null;

		wordStructure = createNode(entityInfo, index);

		// if(parentStructure.getChildNodes().get(entityInfo.getLevels().get(index)).equals(wordStructure)){
		// return;
		// }
		if (parentStructure.getChildNodes().containsKey(
				entityInfo.getLevels().get(index))) {
			parentStructure.getChildNodes()
					.get(entityInfo.getLevels().get(index)).add(wordStructure);
		} else {
			CopyOnWriteArrayList<WordStructure> ws = new CopyOnWriteArrayList<WordStructure>();
			ws.add(wordStructure);
			parentStructure.getChildNodes().put(
					entityInfo.getLevels().get(index), ws);
		}

		wordStructure.setParentNode(parentStructure);

	}


	/**
     * 获取到query对应的术语实体序列，以json格式返回
     *
     * @param query
     * @return
     */
    @SuppressWarnings("finally")
	public static ArrayList<EntityInfo> getObjectList(String query) {

		rwl.readLock().lock();

		ArrayList<EntityInfo> list = new ArrayList<EntityInfo>();
		try {
			query = query.toLowerCase();

			ArrayList<ArrayList<String>> indexRouteList = getIndexRoute(query);
			if (null == indexRouteList) {
				return null;
			}

			ArrayList<WordStructure> wordStructures = searchNode(query,
					indexRouteList); // 得到符合query要求的所有节点

			ArrayList<EntityQueue> entityQueues = getNodes(wordStructures); // 查询query对应的所有

			for (EntityQueue entityQueue : entityQueues) {

				list.add(entityQueue.getWordStructure().getWordInfo());
			}

		} catch (Exception e) {

		} finally {
			rwl.readLock().unlock();
			return list;
		}

	}

	/**
	 * 获取到query对应的术语实体序列，以json格式返回
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String getEntityList(String query) {
		rwl.readLock().lock();
		String jsonString = null;
		try {
			query = query.toLowerCase();

			ArrayList<ArrayList<String>> indexRouteList = getIndexRoute(query);
			if (null == indexRouteList) {
				return null;
			}

			ArrayList<WordStructure> wordStructures = searchNode(query,
					indexRouteList); // 得到符合query要求的所有节点

			ArrayList<EntityQueue> entityQueues = getNodes(wordStructures); // 查询query对应的所有

			ArrayList<EntityInfo> list = new ArrayList<EntityInfo>();

			for (EntityQueue entityQueue : entityQueues) {

				list.add(entityQueue.getWordStructure().getWordInfo());
			}

			if (entityQueues.size() > 0) {
				Gson gson = new Gson();

				jsonString = gson.toJson(list);
			}
		} catch (Exception e) {

		} finally {
			rwl.readLock().unlock();
			return jsonString;
		}

	}

}
