package knowledgeGraph;

import java.util.ArrayList;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.parser.Node;
import com.orientechnologies.orient.core.sql.parser.OTraverseStatement.Strategy;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;

/**
 * 
 * <PRE>
 * 作用 : 
 *    提供知识图谱的各种查询操作
 * 使用 : 
 *    通过word,typelabel,type来进行当前节点的查询、父子节点查询，上下溯查询等
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年4月21日        houyx         create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class KnowledgeGraph {

	private OrientGraph graph = GraphFactory.getInstance().getFactory().getTx();

	public void testConfig() {
		OGlobalConfiguration.dumpConfiguration(System.out);
	}

	/**
	 * 对知识图谱的各种操作
	 *
	 * @param word
	 *            当前词
	 * @param type
	 *            类型，c、sc、cn、e等
	 * @param typelabel
	 *            用于区分c0和c1，其他和type一致
	 * 
	 */
	// 查询当前的节点
	public ArrayList<Vertex> queryWord(String word) {
		// Iterable<Vertex> VertexI= graph.getVertices("word",word);
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> VertexI = graph.command(new OCommandSQL("SELECT FROM node WHERE word ='" + word + "'"))
				.execute();
		// Iterable<Vertex> VertexI=graph.getVertices("node.word",word);

		return toArray(VertexI);

	}

	public ArrayList<Vertex> queryWord(String word, String typelabel) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			Iterable<Vertex> VertexI = graph
					.command(new OCommandSQL(
							"SELECT FROM node WHERE word ='" + word + "' and typelabel ='" + typelabel + "'"))
					.execute();
			return toArray(VertexI);
		}
		Iterable<Vertex> VertexI = graph
				.command(new OCommandSQL("SELECT FROM node WHERE word ='" + word + "' and type ='" + typelabel + "'"))
				.execute();
		return toArray(VertexI);
	}

	public ArrayList<Vertex> queryWord(String word, String typelabel, String type) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> VertexI = graph.command(new OCommandSQL("SELECT FROM node WHERE word ='" + word
				+ "' and type ='" + type + "' and typelabel = '" + typelabel + "'")).execute();
		return toArray(VertexI);
	}

	// 查询父节点
	public ArrayList<Vertex> queryParent(String word) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> parentVertex = graph
				.command(new OCommandSQL("SELECT expand(in('contain')) FROM node WHERE word ='" + word + "'"))
				.execute();
		return toArray(parentVertex);

	}

	public ArrayList<Vertex> queryParent(String word, String typelabel) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			Iterable<Vertex> parentVertex = graph
					.command(new OCommandSQL("SELECT expand(in('contain')) FROM node WHERE word ='" + word
							+ "' and typelabel ='" + typelabel + "'"))
					.execute();
			return toArray(parentVertex);
		}
		Iterable<Vertex> parentVertex = graph.command(new OCommandSQL(
				"SELECT expand(in('contain')) FROM node WHERE word ='" + word + "' and type ='" + typelabel + "'"))
				.execute();

		return toArray(parentVertex);
	}

	public ArrayList<Vertex> queryParent(String word, String typelabel, String type) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> parentVertex = graph
				.command(new OCommandSQL("SELECT expand(in('contain')) FROM node WHERE word ='" + word + "' and type ='"
						+ type + "' and typelabel = '" + typelabel + "'"))
				.execute();
		return toArray(parentVertex);
	}

	// 查询子节点
	public ArrayList<Vertex> queryChild(String word) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> parentVertex = graph
				.command(new OCommandSQL("SELECT expand(out('contain')) FROM node WHERE word ='" + word + "'"))
				.execute();
		return toArray(parentVertex);
	}

	public ArrayList<Vertex> queryChild(String word, String typelabel) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			Iterable<Vertex> parentVertex = graph
					.command(new OCommandSQL("SELECT expand(out('contain')) FROM node WHERE word ='" + word
							+ "' and typelabel ='" + typelabel + "'"))
					.execute();
			return toArray(parentVertex);
		}
		Iterable<Vertex> parentVertex = graph.command(new OCommandSQL(
				"SELECT expand(out('contain')) FROM node WHERE word ='" + word + "' and type ='" + typelabel + "'"))
				.execute();

		return toArray(parentVertex);
	}

	public ArrayList<Vertex> queryChild(String word, String typelabel, String type) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> parentVertex = graph
				.command(new OCommandSQL("SELECT expand(out('contain')) FROM node WHERE word ='" + word
						+ "' and type ='" + type + "' and typelabel = '" + typelabel + "'"))
				.execute();
		return toArray(parentVertex);
	}

	// 查询上溯节点
	public ArrayList<Vertex> queryUpTraverse(String word) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		// ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		// Iterable<Vertex> VertexI = graph.getVertices("node.word", word);
		// for (Vertex v : VertexI) {
		// Iterable<Vertex> traVertex = graph.command(
		// new OSQLSynchQuery<Vertex>("traverse in('contain') from "
		// + v.getId() + " Strategy DEPTH_FIRST")).execute();
		// vertexList.addAll(toArray(traVertex));
		// }
		// return vertexList;
		Iterable<Vertex> traVertex = graph.command(new OSQLSynchQuery<Vertex>(
				"traverse in('contain') from (select from node where word= '" + word + "') strategy DEPTH_FIRST"))
				.execute();
		// return traVertex;
		return toArray(traVertex);
	}

	public ArrayList<Vertex> queryUpTraverse(String word, String typelabel) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			Iterable<Vertex> traVertex = graph
					.command(new OSQLSynchQuery<Vertex>("traverse in('contain') from (select from node where word= '"
							+ word + "' and typelabel ='" + typelabel + "') strategy DEPTH_FIRST"))
					.execute();
			return toArray(traVertex);
		}
		Iterable<Vertex> traVertex = graph
				.command(new OSQLSynchQuery<Vertex>("traverse in('contain') from (select from node where word= '" + word
						+ "' and type ='" + typelabel + "') strategy DEPTH_FIRST"))
				.execute();
		return toArray(traVertex);
	}

	public ArrayList<Vertex> queryUpTraverse(String word, String typelabel, String type) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> traVertex = graph
				.command(new OSQLSynchQuery<Vertex>("traverse in('contain') from (select from node where word= '" + word
						+ "' and type ='" + type + "' and typelabel = '" + typelabel + "') strategy DEPTH_FIRST"))
				.execute();
		return toArray(traVertex);
	}

	// 查询下溯节点
	public ArrayList<Vertex> queryDownTraverse(String word) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> traVertex = graph.command(new OSQLSynchQuery<Vertex>(
				"traverse out('contain') from (select from node where word= '" + word + "') strategy DEPTH_FIRST"))
				.execute();
		// return traVertex;
		return toArray(traVertex);
	}

	public ArrayList<Vertex> queryDownTraverse(String word, String typelabel) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			Iterable<Vertex> traVertex = graph
					.command(new OSQLSynchQuery<Vertex>("traverse out('contain') from (select from node where word= '"
							+ word + "' and typelabel ='" + typelabel + "') strategy DEPTH_FIRST"))
					.execute();
			return toArray(traVertex);
		}
		Iterable<Vertex> traVertex = graph
				.command(new OSQLSynchQuery<Vertex>("traverse out('contain') from (select from node where word= '"
						+ word + "' and type ='" + typelabel + "') strategy DEPTH_FIRST"))
				.execute();
		return toArray(traVertex);
	}

	public ArrayList<Vertex> queryDownTraverse(String word, String typelabel, String type) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		Iterable<Vertex> traVertex = graph.command(
				new OSQLSynchQuery<Vertex>("traverse out('contain') from (select from node where word= '" + word
						+ "' and type ='" + type + "' and typelabel = '" + typelabel + "') strategy DEPTH_FIRST"))
				.execute();
		return toArray(traVertex);
	}

	private ArrayList<Vertex> toArray(Iterable<Vertex> vertexI) {
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		for (Vertex v : vertexI) {
			vertexList.add(v);
		}
		return vertexList;

	}

	private ArrayList<Edge> toArrayEdge(Iterable<Edge> EdgeI) {
		ArrayList<Edge> edgeList = new ArrayList<Edge>();
		for (Edge e : EdgeI) {
			edgeList.add(e);
		}
		return edgeList;
	}

	private ArrayList<Edge> getEdges(Vertex v1, Vertex v2) {
		Iterable<Edge> edgeI = graph
				.command(new OCommandSQL("SELECT FROM contain WHERE (out in(select from node where word= '"
						+ v1.getProperty("word").toString() + "' and type='" + v1.getProperty("type").toString()
						+ "')  AND in in (select from node where word= '" + v2.getProperty("word").toString()
						+ "' and type='" + v2.getProperty("type").toString() + "'))"))
				.execute();
		if (!edgeI.iterator().hasNext()) {
			edgeI = graph.command(new OCommandSQL("SELECT FROM contain WHERE (out in(select from node where word= '"
					+ v2.getProperty("word").toString() + "' and type='" + v2.getProperty("type").toString()
					+ "')  AND in in (select from node where word= '" + v1.getProperty("word").toString()
					+ "' and type='" + v1.getProperty("type").toString() + "'))")).execute();

		}
		return toArrayEdge(edgeI);
	}

	public String getEdgeWeight(Vertex parentV, Vertex childV) {
		ArrayList<Edge> edgeList = new ArrayList<Edge>();
		edgeList = getEdges(parentV, childV);
		if (edgeList.isEmpty()) {
			return null;
		}
		return edgeList.get(0).getProperty("weight");
	}

	// 上溯传递权重
	public ArrayList<String> queryUpTraverseWeight(String word) {
		ArrayList<Vertex> vertexs = queryWord(word);
		ArrayList<String> featureList = new ArrayList<String>();
		for (Vertex v : vertexs) {
			featureList.addAll(queryUpTraverseWeight(word, v.getProperty("typelabel").toString()));
		}
		return featureList;
	}

	// 上溯传递权重
	public ArrayList<String> queryUpTraverseWeight(String word, String typelabel) {
		ArrayList<String> featureList = new ArrayList<String>();
		if (word.isEmpty() || word == null) {
			return featureList;
		}
		ArrayList<Vertex> allList = new ArrayList<Vertex>();
		allList = queryUpTraverse(word, typelabel);
		if (allList.isEmpty() || allList == null) {
			return featureList;
		}
		featureList.add(allList.get(0).getProperty("word").toString());
		featureList.add(allList.get(0).getProperty("type").toString());
		if ("c0".equals(allList.get(0).getProperty("typelabel"))) {
			featureList.add("-1");
		} else {
			featureList.add("1");
		}
		double weight = 1.0;
		for (int i = 0; i < allList.size() - 1; i++) {
			ArrayList<Edge> edgeList = getEdges(allList.get(i), allList.get(i + 1));
			if (!edgeList.isEmpty() && edgeList != null) {
				weight = Double.valueOf(edgeList.get(0).getProperty("weight").toString()) * weight;
			}
			featureList.add(allList.get(i + 1).getProperty("word").toString());
			featureList.add(allList.get(i + 1).getProperty("type").toString());
			if ("c0".equals(allList.get(i + 1).getProperty("typelabel"))) {
				featureList.add("-" + String.valueOf(weight));
			} else {
				featureList.add(String.valueOf(weight));
			}
		}
		return featureList;
	}

	public ArrayList<String> queryUpTraverseWeight(String word, String typelabel, String type) {
		return queryUpTraverseWeight(word, typelabel);
	}

	public ArrayList<Vertex> queryTopNode(String word) {
		ArrayList<Vertex> vList = new ArrayList<Vertex>();
		for (Vertex v : queryUpTraverse(word)) {
			ArrayList<Vertex> parentList = queryParent(v.getProperty("word").toString());
			if (parentList == null || parentList.isEmpty()) {
				vList.add(v);
			}
		}
		return vList;
	}

	public void addVertex(String displayword, String typelabel) {
		String word = displayword.toLowerCase();
		if (!queryWord(word, typelabel).isEmpty()) {
			return;
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			graph.addVertex("class:node", "word", word, "type", "c", "typelabel", typelabel, "displayword",
					displayword);
		} else {
			graph.addVertex("class:node", "word", word, "type", typelabel, "typelabel", typelabel, "displayword",
					displayword);
		}
		graph.commit();
	}

	public void addEdge(Vertex v1, Vertex v2, String edgetype, String weight) {
		if (!IsExistEdge(graph, v1, v2)) {
			Edge e = graph.addEdge("class:edge", v1, v2, edgetype);
			// Edge e=v1.addEdge("REL:contains", v2);
			e.setProperty("weight", weight);
		}
		graph.commit();
	}

	// 判断图graph 两个顶点v1和v2之间是否已经有此边
	private static boolean IsExistEdge(OrientGraph graph, Vertex v1, Vertex v2) {
		Iterable<Edge> edges = v1.getEdges(Direction.OUT);
		for (Edge e : edges) {
			if (e.getVertex(Direction.IN).equals(v2)) {
				return true;
			}
		}
		return false;
	}

	public void deleteVertex(String word) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		graph.command(new OCommandSQL("delete VERTEX from node where word='" + word + "'")).execute();
		graph.commit();
	}

	public void deleteVertex(String word, String typelabel) {
		if (word != null && !word.isEmpty()) {
			word = word.toLowerCase().replace("'", "\\'");
		}
		if ("c0".equals(typelabel) || "c1".equals(typelabel)) {
			graph.command(new OCommandSQL(
					"delete VERTEX from node where word='" + word + "' and typelabel='" + typelabel + "'")).execute();
		} else {
			graph.command(
					new OCommandSQL("delete VERTEX from node where word='" + word + "' and type='" + typelabel + "'"))
					.execute();
		}
		graph.commit();
	}

	public ArrayList<Vertex> queryType(String type) {
		Iterable<Vertex> vertexList = graph
				.command(new OSQLSynchQuery<Vertex>("SELECT from node where type= '" + type + "'")).execute();
		ArrayList<Vertex> resultList = new ArrayList<Vertex>();
		resultList = toArray(vertexList);
		if (resultList.isEmpty()) {
			vertexList = graph.command(new OSQLSynchQuery<Vertex>("SELECT from node where typelabel= '" + type + "'"))
					.execute();
			resultList = toArray(vertexList);
		}
		return resultList;
	}

	public ArrayList<Vertex> getAllVetex() {
		ArrayList<Vertex> VList = new ArrayList<Vertex>();
		for (Vertex v : graph.getVertices()) {
			VList.add(v);
		}
		return VList;
	}

	// 断开数据库连接
	public void shutdown() {
		graph.shutdown();
	}

	public void commit() {
		graph.commit();

	}
}
