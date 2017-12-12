package knowledgeGraph;

import java.util.ArrayList;

import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;

public class KnowledgeGraphTest {

	@Test
	public void test() {
		KnowledgeGraph kgraph = new KnowledgeGraph();
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		vertexList = kgraph.queryChild("时政");
//		vertexList = kgraph.queryType("c1");
//		for (Vertex vertex : vertexList) {
//			System.out.println(vertex.getProperty("word"));
//		}
		for (Vertex v : vertexList) {
			if (v.getProperty("typelabel").equals("c1")) {
				System.out.println(v.getProperty("word"));
			}
		}
		kgraph.shutdown();// 操作完成记得关闭
	}
}
// KnowledgeGraph kgraph=new KnowledgeGraph();
// ArrayList<Vertex> vertexList= new ArrayList<Vertex>();
//// vertexList=kgraph.queryType("cn");
//// int count=0;
//// for(Vertex v:vertexList){
//// if (!kgraph.queryParent(v.getProperty("word").toString()).isEmpty()) {
//// count++;
//// }
//// }
//// System.out.println(count);
//// vertexList=kgraph.queryType("sc");
//// System.out.println("sc" + vertexList.size());
//// vertexList=kgraph.queryType("c0");
//// System.out.println(vertexList.size());
// vertexList = kgraph.queryType("c0");
// for(Vertex v:vertexList){
// System.out.println(v.getProperty("word").toString());
// }
// vertexList = kgraph.queryType("c1");
// for(Vertex v:vertexList){
// System.out.println(v.getProperty("word").toString());
// }
// try {
// BufferedWriter bw=new BufferedWriter(new
// FileWriter("E:/FeatureProject/Allcn.txt"));
// for(Vertex v:vertexList){
// bw.write(v.getProperty("word").toString());
// bw.newLine();
// }
// bw.close();
// } catch (Exception e) {
// // TODO: handle exception
// }
// for(Vertex v:vertexList){
// if(v.getProperty("typelabel").equals("c0")){
// continue;
// }
// String word=v.getProperty("word");
// ArrayList<Vertex> vList=new ArrayList<Vertex>();
// vList=kgraph.queryChild(word);
// boolean flag=false;
// for(Vertex vc:vList){
// if (vc.getProperty("type").equals("sc")) {
// flag=true;
// break;
// }
// }
// if(!flag){
// System.out.println(v.getProperty("typelabel")+"="+v.getProperty("word"));
// }
// }
// 查询当前词
// kgraph.addVertex("test", "e");
// vertexList=kgraph.queryWord("test_test_hexl_1");
// for(Vertex v:vertexList){
// System.out.println(v.getProperty("word")
// +v.getProperty("typelabel").toString());
// }
// ArrayList<String> resultList=kgraph.queryUpTraverseWeight("瑜伽");
// System.out.println(resultList);
// vertexList=kgraph.queryWord("黄金(T+D)","cn");
// vertexList=kgraph.queryType("e");
// for(Vertex v:vertexList){
// String word=v.getProperty("word");
// int length = computeWordsLen(word);
//
// String textSplited = splitWord(word);
//
// String [] ws = textSplited.split(" ");
//
// if(((ws.length >=3 || length >3) && length<5) ||
// (ws.length < 3 && length <=3))
// {
// System.out.println(word);
// }
//
// }
// }
// vertexList=kgraph.queryType("cn");
// vertexList=kgraph.queryType("c");
// vertexList=kgraph.queryDownTraverse("体育");
// vertexList=kgraph.getAllVetex();
// int count=0;

// }
// for(Vertex v:vertexList){
// if (kgraph.queryParent(v.getProperty("word").toString())==null ||
// kgraph.queryParent(v.getProperty("word").toString()).isEmpty()) {
// System.out.println(v.getProperty("word").toString());
// }
// count++;
// if (kgraph.queryParent(v.getProperty("word").toString())==null ||
// kgraph.queryParent(v.getProperty("word").toString()).isEmpty()) {
// ArrayList<Vertex> vchild=new ArrayList<Vertex>();
// vchild=kgraph.queryChild(v.getProperty("word").toString());
// if (vchild!=null && !vchild.isEmpty()) {
// boolean flag=false;
// for (Vertex vc:vchild) {
// if(vc.getProperty("type").equals("c")){
// System.out.println(v.getProperty("typelabel").toString() + "=" +
// v.getProperty("word").toString()+","+vc.getProperty("typelabel").toString() +
// "=" + vc.getProperty("word").toString());
// flag=true;
// }
// }
// if(!flag){
// System.out.println(v.getProperty("typelabel").toString() + "=" +
// v.getProperty("word").toString());
//
// }
// }
// else {
// System.out.println(v.getProperty("typelabel").toString() + "=" +
// v.getProperty("word").toString());
// }

// }
// System.out.println(count);
// if (kgraph.queryParent(v.getProperty("word").toString())==null ||
// kgraph.queryParent(v.getProperty("word").toString()).isEmpty()) {
// System.out.println(v.getProperty("word"));
// }
// }
// vertexList=kgraph.queryTopNode("风俗");
// for (Vertex v:vertexList) {
// System.out.println(v.getProperty("typelabel").toString() + "=" +
// v.getProperty("word").toString());
// }
// vertexList=kgraph.queryWord("音乐","c1");
// //vertexList=kgraph.queryWord("音乐","c1","c");
//
// //查询父节点
// vertexList=kgraph.queryParent(null);
// vertexList=kgraph.queryParent("音乐","c1");
// //vertexList=kgraph.queryParent("音乐","c1","c");
//
// //查询子节点
// vertexList=kgraph.queryChild("音乐");
// vertexList=kgraph.queryChild("音乐","c1");
// //vertexList=kgraph.queryChild("音乐","c1","c");
//
// //上溯查询
// vertexList=kgraph.queryUpTraverse("段子");
// //vertexList=kgraph.queryUpTraverse("音乐","c1");
// //vertexList=kgraph.queryUpTraverse("音乐","c1","c");
// //vertexList=kgraph.queryType("c");
// for (Vertex v:vertexList) {
// System.out.println(v.getProperty("word").toString()+"
// "+v.getProperty("typelabel").toString());
// }
//
// //下溯查询
// vertexList=kgraph.queryDownTraverse("音乐()");
// System.out.println("test"+vertexList);
// vertexList=kgraph.queryDownTraverse("音乐","c1");
// //vertexList=kgraph.queryDownTraverse("音乐","c1","c");
//
// //上溯传递边权重，返回特征三元组
// ArrayList<String> featureList=new ArrayList<String>();
// //featureList=kgraph.queryUpTraverseWeight("互联网金融");
// //featureList=kgraph.queryUpTraverseWeight("搞笑视频");
// //featureList=kgraph.queryUpTraverseWeight("互联网金融", "sc","sc");
// //System.out.println("test"+featureList);

// 获取两个顶点之间边的权重
// ArrayList<Vertex> v1List = kgraph.queryWord("足球","c1");//当前节点
// ArrayList<Vertex> v2List = kgraph.queryWord("体育","c0");//父亲节点
// String weight=kgraph.getEdgeWeight(v1List.get(0),v2List.get(0));//两个节点之间
// System.out.println(weight);
// vertexList = kgraph.queryType("cn");
// for (Vertex v:vertexList) {
// kgraph.queryUpTraverse(v.getProperty("word").toString());
// System.out.println(v.getProperty("word"));
// }
//
// try {
// BufferedWriter bw=new BufferedWriter(new
// FileWriter("E:/FeatureProject/Alle.txt"));
// ArrayList<Vertex> verList=kgraph.queryType("e");
// for(Vertex v:verList){
// String word=v.getProperty("word").toString();
//// if (word.endsWith(("天气") )|| word.endsWith("房产") || word.endsWith("旅游")
//// || word.endsWith("楼市")) {
//// continue;
//// }
//// if (!kgraph.queryParent(word).isEmpty() && kgraph.queryParent(word)!=null)
// {
//// continue;
//// }
// int length = computeWordsLen(word);
//
// String textSplited = splitWord(word);
//
// String [] ws = textSplited.split(" ");
//
// if(((ws.length >=3 || length >3) && length<5) ||
// (ws.length < 3 && length <=3))
// {
// bw.write(v.getProperty("word").toString());
// bw.newLine();
// }
//
//
// }
// bw.close();
// BufferedReader br=new BufferedReader(new
// FileReader("E:/FeatureProject/filtercn.txt"));
// String str=null;
// while((str=br.readLine())!=null){
// System.out.println(str);
// //cnset.add(str);
//
// //kgraph.deleteSubVertex(word, typelabel);
// if (kgraph.queryParent(str)==null || kgraph.queryParent(str).isEmpty()) {
// //kgraph.deleteVertex(str, "cn");
// if (str.contains("(")) {
// //int index=str.indexOf("(");
// //String substr=str.substring(0, index-1);
// //kgraph.deleteSubVertex(substr,"cn");
// }
// else {
// kgraph.deleteVertex(str,"cn");
// }
// }
//// continue;
//
// }
// br.close();
// } catch (Exception e) {
// // TODO: handle exception
// }

// c0&c1&sc的关系输出
// try {//
// BufferedWriter bw=new BufferedWriter(new
// FileWriter("E:/FeatureProject/csc.txt"));
//// vertexList=kgraph.queryType("cn");
//// for(Vertex v:vertexList){
//// String word=v.getProperty("displayword").toString();
//// if(word.contains("房产") || word.contains("天气") || word.contains("旅游")
// ||word.contains("视频")){
//// continue;
//// }
//// if(kgraph.queryParent(word)!=null && !kgraph.queryParent(word).isEmpty()){
//// continue;
//// }
//// bw.write("cn="+word);
//// bw.newLine();
//// bw.flush();
//// }
// vertexList=kgraph.queryType("c0");
// for(Vertex v:vertexList){
// ArrayList<Vertex> child1=new ArrayList<Vertex>();
// child1=kgraph.queryChild(v.getProperty("word").toString(),"c0");
// for(Vertex vc1:child1){
// ArrayList<Vertex> child2=new ArrayList<Vertex>();
// child2=kgraph.queryChild(vc1.getProperty("word").toString(),"c1");
// if (child2==null || child2.isEmpty()) {
// if (vc1.getProperty("typelabel").equals("c1")) {
// bw.write(v.getProperty("typelabel")+"="+v.getProperty("displayword").toString()+","+vc1.getProperty("typelabel")+"="+vc1.getProperty("displayword").toString());
// bw.newLine();
// bw.flush();
// }
// continue;
// }
// boolean flag=false;
// for (Vertex vc2:child2) {
// if (vc2.getProperty("type").equals("sc")) {
// bw.write(v.getProperty("typelabel")+"="+v.getProperty("displayword").toString()+","+vc1.getProperty("typelabel")+"="+vc1.getProperty("displayword").toString()+","+vc2.getProperty("typelabel")+"="+vc2.getProperty("displayword").toString());
// bw.newLine();
// bw.flush();
// flag=true;
// }
// }
// if (!flag) {
// if (vc1.getProperty("typelabel").equals("c1")) {
// bw.write(v.getProperty("typelabel")+"="+v.getProperty("displayword").toString()+","+vc1.getProperty("typelabel")+"="+vc1.getProperty("displayword").toString());
// bw.newLine();
// bw.flush();
// }
//
// }
// }
// }
// vertexList=kgraph.queryType("c1");
// for(Vertex v:vertexList){
// if(kgraph.queryParent(v.getProperty("word").toString())!=null &&
// !kgraph.queryParent(v.getProperty("word").toString()).isEmpty() ){
// continue;
// }
// ArrayList<Vertex> childList=new ArrayList<Vertex>();
// childList=kgraph.queryChild(v.getProperty("word").toString(), "c1");
// System.out.println(v.getProperty("word").toString());
// if (childList==null ||childList.isEmpty()) {
// bw.write(v.getProperty("typelabel")+"="+v.getProperty("word").toString());
// bw.newLine();
// continue;
// }
// boolean flag=false;
// for(Vertex cv:childList){
// if (cv.getProperty("type").equals("sc")) {
// bw.write(v.getProperty("typelabel")+"="+v.getProperty("displayword").toString()+","+cv.getProperty("typelabel")+"="+cv.getProperty("displayword"));
// bw.newLine();
// flag=true;
// }
// }
// if (!flag) {
// bw.write(v.getProperty("typelabel")+"="+v.getProperty("displayword").toString());
// bw.newLine();
// }
// }
// bw.close();
// } catch (Exception e) {
// // TODO: handle exception
// }

// 输出c-sc-cn
// try {
// BufferedWriter bw=new BufferedWriter(new
// FileWriter("E:/FeatureProject/c_sc_cn.txt"));
// ArrayList<Vertex> v0List=kgraph.queryType("c0");
// for (int i = 0; i < v0List.size(); i++) {
// ArrayList<Vertex> v1List =
// kgraph.queryChild(v0List.get(i).getProperty("word").toString());
// for (int j = 0; j < v1List.size(); j++) {
// bw.write(v0List.get(i).getProperty("typelabel") + "=" +
// v0List.get(i).getProperty("word")+","+v1List.get(j).getProperty("typelabel")
// + "=" + v1List.get(j).getProperty("word"));
// bw.newLine();
// bw.flush();
// }
//
// }
//
// ArrayList<Vertex> v1List=kgraph.queryType("c1");
// for (int i = 0; i < v1List.size(); i++) {
// //if (kgraph.queryParent(v1List.get(i).getProperty("word").toString()) ==null
// ||
// kgraph.queryParent(v1List.get(i).getProperty("word").toString()).isEmpty()) {
// ArrayList<Vertex> v2List =
// kgraph.queryChild(v1List.get(i).getProperty("word").toString());
// for (int j = 0; j < v2List.size(); j++) {
// bw.write(v1List.get(i).getProperty("typelabel") + "=" +
// v1List.get(i).getProperty("word")+","+v2List.get(j).getProperty("typelabel")
// + "=" + v2List.get(j).getProperty("word"));
// bw.newLine();
// bw.flush();
// }
//
//
// //}
// }
//
// ArrayList<Vertex> v2List=kgraph.queryType("sc");
// for (int i = 0; i < v2List.size(); i++) {
// //if (kgraph.queryParent(v1List.get(i).getProperty("word").toString()) ==null
// ||
// kgraph.queryParent(v1List.get(i).getProperty("word").toString()).isEmpty()) {
// ArrayList<Vertex> v3List =
// kgraph.queryChild(v2List.get(i).getProperty("word").toString());
// for (int j = 0; j < v3List.size(); j++) {
// bw.write(v2List.get(i).getProperty("typelabel") + "=" +
// v2List.get(i).getProperty("word")+","+v3List.get(j).getProperty("typelabel")
// + "=" + v3List.get(j).getProperty("word"));
// bw.newLine();
// bw.flush();
// }
//
//
// //}
// }
//
//
// bw.close();
// } catch (Exception e) {
// // TODO: handle exception
// }

//
//

//
//
//
// }
