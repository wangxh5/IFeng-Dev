package knowledgeGraph;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import utils.LoadConfig;

/**
 * 
 * <PRE>
 * 作用 : 
 *    将分类体系存储到OrientDB图数据库中
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
 *          1.0          2016年4月6日         houyx         create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class OrientDBGraph {
	public static void createGdb(){
		try {
			String GraphDBurl = LoadConfig.lookUpValueByKey("GraphDBUrl");
			 //OrientGraph graph=new OrientGraph("plocal:E:/orientdb-community-2.2.2/databases/KnowledgeGraph","root","root");
			OrientGraph graph=new OrientGraph(GraphDBurl,"root","root");
			//OrientGraph graph=new OrientGraph("plocal:e:/KnowlegeGraph");
			//OrientGraph graph=new OrientGraph("remote:10.90.1.41/data/hyx/orientdb-community-2.1.13/databases/OrientGraph/");
			//graph.begin();
			//String Filename = LoadConfig.lookUpValueByKey("graphNodeFile");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("E:/graphnode.txt"),"UTF-8"));
			String str;
			
			while((str=reader.readLine())!=null){
				//str=str.toLowerCase();
				String[] feature=str.split(",");
				ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
				for (int i = 0; i < feature.length; i++) {
					String[] group=feature[i].split("=");
					String type=group[0];
					if ("c0".equals(group[0]) || "c1".equals(group[0])) {
						type="c";
					}
					String displayword=group[1];
					group[1]=group[1].toLowerCase();
					Vertex v=ExistVertex(graph, group[1], group[0]);
					if (v!=null) {
						vertexList.add(v);
					}
					else{
						Vertex vertex = graph.addVertex("class:node","word",group[1],"type",type,"typelabel",group[0]);
						vertexList.add(vertex);
						System.out.println(vertex);

					}
				}
					
				ArrayList<Vertex> presentVertexList=new ArrayList<Vertex>();
				ArrayList<Vertex> nextVertexList=new ArrayList<Vertex>();
				
				presentVertexList=getVertexList(vertexList);
				if (vertexList!=null && !vertexList.isEmpty()) {
					nextVertexList=getVertexList(vertexList);
				}
				
				addVertexandEdge(graph,vertexList,presentVertexList,nextVertexList);
				graph.commit();
			}
//			HotWordData hotWordData=HotWordData.getInstance();
//			ConcurrentHashMap<String, HotWordInfo> hotwordMap = new ConcurrentHashMap<String, HotWordInfo>();
//			hotwordMap=hotWordData.getHotwordMap();
//			for (String key:hotwordMap.keySet()) {
//				key=key.toLowerCase();
//				graph.addVertex("class:node","word",key,"type","e","typelabel","e");
//			}
			//graph.createKeyIndex("wordIndex", Vertex.class, new Parameter<K, V>(key, value));
			

			graph.commit();
			graph.shutdown();
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}
		
	}
	
	/**
	 * 向图数据库中增加节点 
	 * 
	 * @param graph
	 * @param presentVertexList 当前顶点列表
	 * @param nextVertexList 当前顶点连接的下一个节点列表
	 */
	private static void addVertexandEdge(OrientGraph graph, ArrayList<Vertex> vertexList,ArrayList<Vertex> presentVertexList, ArrayList<Vertex> nextVertexList){
		if (nextVertexList==null || nextVertexList.isEmpty()) {
//				for (Vertex v:presentVertexList) {
//					graph.addVertex(v);
//				}
			return;
		}
		for (Vertex v1: presentVertexList) {
			for (Vertex v2:nextVertexList) {
				System.out.println(v1.getProperty("word"));
				System.out.println(v2.getProperty("word"));
				if(!IsExistEdge(graph,v1,v2)){
					Edge e=graph.addEdge("class:edge", v1, v2, "contain");
					//Edge e=v1.addEdge("REL:contains", v2);
					e.setProperty("weight", "1");
					
				}
			}
		}
		presentVertexList=nextVertexList;
		nextVertexList=new ArrayList<Vertex>();
		if (vertexList!=null && !vertexList.isEmpty()) {
			nextVertexList=getVertexList(vertexList);
		}
		addVertexandEdge(graph, vertexList, presentVertexList, nextVertexList);
	}
	
	//根据word和typelabel判断图中是否已经有该顶点
	private static Vertex ExistVertex(OrientGraph graph, String word, String typelabel){
		Iterable<Vertex> iv=graph.getVertices("word", word);
		for (Vertex v:iv) {
			System.out.println(v);
			System.out.println(v.getProperty("word").toString() + v.getProperty("typelabel").toString());
			if (v.getProperty("typelabel").equals(typelabel)) {
				return v;
				//break;
			}
		}
		return null;
	}	
	//判断图graph中是否有顶点v
	private static Vertex ExistVertex(OrientGraph graph,Vertex v){
		String word=v.getProperty("word");
		String typelabel=v.getProperty("typelabel");
		return ExistVertex(graph, word, typelabel);
	}
	
	//判断图graph 两个顶点v1和v2之间是否已经有此边 
	private static boolean IsExistEdge(OrientGraph graph,Vertex v1,Vertex v2){
		Iterable<Edge> edges = v1.getEdges(Direction.OUT, "contain");
		for(Edge e : edges){
			if (e.getVertex(Direction.IN).equals(v2)) {
				return true;
			}
		}
		return false;
	}
	
	private static ArrayList<Vertex> getVertexList(ArrayList<Vertex> vertexList){
		ArrayList<Vertex> resultList=new ArrayList<Vertex>();
		resultList.add(vertexList.get(0));
		String typeflag=vertexList.get(0).getProperty("typelabel");
		vertexList.remove(0);
		while(!vertexList.isEmpty() && vertexList.get(0).getProperty("typelabel").equals(typeflag)){
			resultList.add(vertexList.get(0));
			vertexList.remove(0);
		}
		return resultList;
	}
	
	public static void main(String[] args){
		//createGdb();
		String GraphDBurl = LoadConfig.lookUpValueByKey("GraphDBUrl");
		OrientGraph graph=new OrientGraph(GraphDBurl,"root","root");
	//	graph.begin();
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					new FileInputStream("E:\\FeatureProject\\分类体系\\需补充的.txt"),"UTF-8"));
//			String str;
//			while((str=reader.readLine())!=null){
//				if(str.contains("(")){
//					System.out.println(str);
//					continue;
//				}
//				graph.command(
//						new OCommandSQL(
//								"delete VERTEX from node where word='"
//										+ str + "' and type='cn'")).execute();
//				
//			}
//		reader.close();
//		} catch (Exception e) {
//		// TODO: handle exception
//			System.out.print(e);
//		}
//		graph.commit();
		
//		graph.begin();
//		
//		graph.createKeyIndex("word", Vertex.class,new Parameter("class", "node"),new Parameter("type", "NOTUNIQUE"));
//		graph.commit();
//		Index<Vertex> index=graph.getIndex("node.word", Vertex.class);
//		Iterator<Vertex> iterable=index.get("node.word", "娱乐").iterator();
//		Iterable<Vertex> VertexI=graph.getVertices("node.word","娱乐");
//		for(Vertex v:VertexI){
//		System.out.println(v.getProperty("word"));
//		}
		 
		//OrientGraph graph=new OrientGraph("plocal:e:/classifyGraph");
		
//		//graph.command(new OCommandSQL("create PROPERTY node.word STRING")).execute();
//
//	//	graph.command(new OSQLSynchQuery<Vertex>("create index wordIndex on node(word) NOTUNIQUE")).execute();
//		//graph.commit();
//		//graph.begin();
//		//TransactionalGraph graph=new OrientGraph("plocal:e:/OrientGraph");
		//查找某种类型的
//		for(Vertex v:graph.getVertices("typelabel","c0")){
//			System.out.println(v.getProperty("typelabel").toString() + "=" + v.getProperty("word").toString());
//		}
//		graph.shutdown();
//		
//		//查找顶级的
		for(Vertex v:graph.getVertices("typelabel","c0")){
			
			System.out.println("c0="+v.getProperty("word").toString());
		}
//		graph.shutdown();
//		
////			 OrientGraphQuery queryBuilder = (OrientGraphQuery)graph.query();
////			
//		Iterable<Vertex> vs=graph.command(new OSQLSynchQuery<Vertex>("traverse out('contain') from (select from node where word='互联网') strategy DEPTH_FIRST")).execute();
//	    for (Vertex v:vs) {
//	    	System.out.println("下溯遍历 ：" + v.getProperty("word") + v.getProperty("typelabel"));
//		}
//	    
//	    System.out.println("---------------------------------");
//	    Iterable<Vertex> vs1=(Iterable<Vertex>)graph.command(new OCommandSQL("SELECT expand(in('contain')) FROM node WHERE word ='互联网金融'")).execute();
//	    for (Vertex v:vs1) {
//			System.out.println("上层父节点："+v.getProperty("word")+v.getProperty("typelabel"));
//			
//			
//		}
////		    Iterable<Edge> es=(Iterable<Edge>)graph.command(new OCommandSQL("SELECT e.weight FROM node v1,node v2,edge e WHERE v1.word='娱乐' and v2.word='音乐' and")).execute();
////		    for (Edge e:es) {
////				System.out.println("上层父节点："+e.getProperty("weight"));		
////			}
//	  // Iterable<Edge> edges = v.getEdges(Direction.OUT, "contain");
//	    
//	    
////			for(Edge e : graph.getEdges()){
////				if (e.getVertex(Direction.OUT).getProperty("word").equals("娱乐") && e.getVertex(Direction.IN).getProperty("word").equals("音乐")) {
////					System.out.println(e.getProperty("weight"));
////				}
////			}
//		
//		graph.commit();
//		    for (Vertex v:graph.getVertices()) {
//		    	if (v.getPropertyKeys().contains("displayword")) {
//					
//				}
//		    	else {
//		    		System.out.println(v.getProperty("word")+"="+v.getProperty("typelabel"));
//				}
//				
//			}
	}


}
