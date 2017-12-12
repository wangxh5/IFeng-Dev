package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * <PRE>
 * 作用 : 为特征工程中的处理提供可用的工具类
 *   
 *   
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
 *          1.0          2015-9-23         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class FeatureExTools
{
	static Logger LOG = Logger.getLogger(FeatureExTools.class);
	// 特征的类型
	public static final String[] featureType = { "c", "sc", "e", "cn", "t", "s", "s1", "loc","kb", "et","kq", "ks","swm","x", "nr","nt","nz","n", "nx", "ns","ne","k"};
	public static final String splitTag = "|*$*|";
	public static final String splitedTag = "\\|_w \\*_w \\$_w \\*_w \\|_w";
	public static final String picTag = "#_w p_nx #_w";
	
	public static ArrayList<String> getFeatureTypeList(){
		ArrayList<String> featureTypeList = new ArrayList<String>();
		for(int i = 0; i < featureType.length; i++){
			featureTypeList.add(featureType[i]);
		}
		return featureTypeList;
	}
	/**
	 * 处理featureList中重复的词
	 * 忽略大小写
	 * 权重合并取大值
	 * 按顺序输出
	 * @param featureList
	 */
	public static ArrayList<String> delSpareFea(ArrayList<String> featureList)
	{
		if (featureList == null || featureList.size() <= 2)
			return featureList;
		ArrayList<String> resultList = new ArrayList<String>();
		for (int k = 0; k < featureType.length; k++)
		{
			for (int i = 0; i < featureList.size() - 2; i += 3)
			{
				if(featureList.get(i).length() <= 1)
					continue;
				if(!featureList.get(i+1).trim().equals(featureType[k]))
					continue;
				boolean addFlag = true; //添加标志位
				String rlw = null;//结果feature
				String flw = null;//原始feature
				for(int j = 0; j < resultList.size() - 2; j += 3)
				{
					 rlw = resultList.get(j).toLowerCase().trim();
					if(rlw.length()>=3 && (rlw.endsWith("省")||rlw.endsWith("市")||rlw.endsWith("县")))
					{
						rlw = rlw.substring(0, rlw.length() - 1);
					}
						
					 flw = featureList.get(i).toLowerCase().trim();
					if(flw.length()>=3 && (rlw.endsWith("省")||flw.endsWith("市")||flw.endsWith("县")))
					{
						flw = flw.substring(0, flw.length() - 1);
					}
					if(rlw.equals(flw))
					{
						double pnflagr = 1.0;
						if(Double.valueOf(resultList.get(j+2).trim())>=0)
							pnflagr = 1;
						else
							pnflagr = -1;
						
						double pnflagf = 1.0;
						if(Double.valueOf(featureList.get(i+2).trim())>=0)
							pnflagf = 1;
						else
							pnflagf = -1;
						addFlag = false;//当结果list中已经有相同的词，匹配标志位设为false
//						if(resultList.get(j+1).trim().equals(featureList.get(i+1).trim()))
//						{//类型相同，保留较大的词权重
						if(resultList.get(j+1).equals("c") && resultList.get(j+1).equals(featureList.get(i+1))&& Math.abs(Double.valueOf(resultList.get(j+2).trim())) <= Math.abs(Double.valueOf(featureList.get(i+2).trim())))
						{
							if(pnflagr < 0 && pnflagf < 0)
								resultList.set(j+2, String.valueOf(-1*Math.abs(Double.valueOf(featureList.get(i+2).trim()))));
							else
								resultList.set(j+2, String.valueOf(Math.abs(Double.valueOf(featureList.get(i+2).trim()))));
							
						}
						else if(!resultList.get(j+1).equals("c") && Math.abs(Double.valueOf(resultList.get(j+2).trim())) <= Math.abs(Double.valueOf(featureList.get(i+2).trim())))
							{
								resultList.set(j+2, String.valueOf(Math.abs(Double.valueOf(featureList.get(i+2).trim()))*pnflagr));
							}
//						}
					}
				}
				if(addFlag)//根据添加标志位判断是否需要放入结果list
				{
					resultList.add(featureList.get(i));
					resultList.add(featureList.get(i + 1).trim());
					resultList.add(featureList.get(i + 2).trim());
				}
			}
		}
		return resultList;
	}
	
	/**
	 * 编辑距离计算
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
//	public static double similarity(String[] str1, String[] str2)
//	{
//		if (str1 == null || str2 == null)
//		{
//			return 0.0;
//		}
//		int min = commenFuncs.LD(str1, str2);
//		double similarity = 1 - (double) min / ((str1.length + str2.length) / 2);
//		return similarity;
//	}
	
	
	/**
	 * 合并两个featureList //重载 增加了文章类型
	 * 
	 * @param feature1
	 * @param feature2
	 *            
	 * @return 合并后的feature list
	 */
	/**
	 * 
	 * @param feature1
	 * @param type1     说明feature是slide还是doc
	 * @param feature2
	 * @param type2
	 * 注意：以feature1为主，应该是itemNow的featureList，假如存在问题直接返回feature1不发生改变
	 *            feature2的t，s，k*不再给feature1
	 * @param mode   用以区分不同的合并模式  模式1表示 c sc cn e t谨慎合并，模式2表示无差别全部合并
	 * @return
	 */
	public static ArrayList<String> mergeFeature(ArrayList<String> feature1, String type1, ArrayList<String> feature2, String type2,int mode)
	{
		if (feature2 == null)
			return feature1;
		if (feature2.size() % 3 != 0)
			return feature1;
		if((type1.equals("slide") || type2.equals("slide")) && !type1.equals(type2))
			return feature1;
		HashMap<String,Double> featureMap = new HashMap<String,Double>();
		String word = null;
		String type = null;
		Double value = null;
		String feature = null;
		if (feature1 != null && feature1.size() % 3 == 0)
		{
			for(int i=0; i < feature1.size() - 2; i = i +3)
			{
				word = feature1.get(i);
				type = feature1.get(i + 1);
				value = Double.valueOf(feature1.get(i + 2));
				feature = word+"||"+type;
				featureMap.put(feature, value);
			}
		}
		for (int j = 0; j < feature2.size() - 2; j += 3)
		{
			word = feature2.get(j);
			type = feature2.get(j + 1);
			value = Double.valueOf(feature2.get(j + 2));
			boolean negaFlag = false;
			if(value < 0)
				negaFlag = true;
			value = Math.abs(value) - 0.3;
			if(value < 0.1)
				value = 0.1;
			if(type2!=null && type2.equals("slide"))
			{
				if(type.equals("c") || type.equals("sc"))
				{
					feature = word + "||" + type;
					if(featureMap.containsKey(feature)&&Math.abs(featureMap.get(feature)) < value)
					{
						if(negaFlag)
							featureMap.put(feature, value * (-1));
						else
							featureMap.put(feature, value);
					}
					else if(!featureMap.containsKey(feature))
					{
						if(negaFlag)
							featureMap.put(feature, value * (-1));
						else
							featureMap.put(feature, value);
					}
				}
			}
			else
			{
				if((mode == 1 && (type.equals("c") || type.equals("sc") || type.equals("cn") || type.equals("t"))) || mode == 2)
				{
					feature = word + "||" + type;
					if(featureMap.containsKey(feature)&&Math.abs(featureMap.get(feature))<value)
					{
						if(negaFlag)
							featureMap.put(feature, value * (-1));
						else
							featureMap.put(feature, value);
					}
					else if(!featureMap.containsKey(feature))
					{
						if(negaFlag)
							featureMap.put(feature, value * (-1));
						else
							featureMap.put(feature, value);
					}
				}
			}
		}
		ArrayList<String> resultList = new ArrayList<String>();
		for(Entry<String,Double> entry : featureMap.entrySet())
		{
			String[] temp = entry.getKey().split("\\|\\|");
			if(temp.length == 2)
			{
				resultList.add(temp[0]);
				resultList.add(temp[1]);
				resultList.add(String.valueOf(entry.getValue()));
			}
		}
		if (resultList.size() < 1)
		{
			return feature1;
		}
		return resultList;
	}
	/**
	 * 给百度热词赋值，权重为1，type为e
	 * @param title
	 * @return
	 */
//	public static ArrayList<String> getBaiduKey(String title, String s_title,String s_content, ConcurrentHashMap<String, HotWordInfo> concurrentHashMap, boolean ifEco)
//	{
//		if(s_title == null && s_content == null)
//			return null;
//		if(ifEco)
//			return null;
//		HashSet<String> wordSet = new HashSet<String>();
//		String[] Array = (s_title+" "+ s_content).split(" ");
//		for(int i = 0; i < Array.length - 1; i++)
//		{
//			String s = Array[i];
//			if(s.contains("_"))
//			{
//				wordSet.add(s);
//			}
//			else
//			{
//				Array[i+1] = s+" "+Array[i+1];
//			}
//		}
//		ArrayList<String> baiduHotKey = new ArrayList<String>();
//		for(Entry<String,HotWordInfo> eKey : concurrentHashMap.entrySet())
//		{
//			String splitWord = eKey.getValue().getSplitContent();
//			if(splitWord == null)
//				continue;
//			if(commenFuncs.stringLength(splitWord) < 5 && splitWord.trim().length() > 1 && title.contains(splitWord))
//			{
//				baiduHotKey.add(splitWord);
//				baiduHotKey.add("et");
//				baiduHotKey.add("0.5");
//				continue;
//			}
//			String[] keys = splitWord.split(" ");
//			double probability = 0.0;
//			for(String s : keys)
//			{
//				if(wordSet.contains(s))
//					probability += 1;
//			}
//			probability = probability / keys.length;
//			if(probability > 0.9)
//			{
//				String word = splitWord.replaceAll(" *_[a-z]+ *", "");
//				if(commenFuncs.stringLength(word)>=5)
//				{
//					baiduHotKey.add(word);
//					baiduHotKey.add("e");
//					baiduHotKey.add("1");
//				}
//				else if(word.trim().length() >= 2)
//				{
//					baiduHotKey.add(word);
//					baiduHotKey.add("et");
//					baiduHotKey.add("0.5");
//				}
//			}
//		}
//		if(baiduHotKey != null && !baiduHotKey.isEmpty())
//			return baiduHotKey;
//		else return null;
//	}
//	
//	public static ArrayList<String> getBaiduKey(String title, ConcurrentHashMap<String, HotWordInfo> concurrentHashMap, boolean ifEco)
//	{
//		if(title == null)
//			return null;
//		if(ifEco)
//			return null;
//		ArrayList<String> baiduHotKey = new ArrayList<String>();
//		for(Entry<String,HotWordInfo> eKey : concurrentHashMap.entrySet())
//		{
//			String hotword = eKey.getKey();
//			if(hotword == null)
//				continue;
//			if(commenFuncs.stringLength(hotword) < 5 && hotword.trim().length() > 1 && title.contains(hotword))
//			{
//				baiduHotKey.add(hotword);
//				baiduHotKey.add("et");
//				baiduHotKey.add("1");
//				continue;
//			}
//			else if(commenFuncs.stringLength(hotword) > 5 && title.contains(hotword))
//			{
//				baiduHotKey.add(hotword);
//				baiduHotKey.add("e");
//				baiduHotKey.add("1");
//				continue;
//			}
//		}
//		if(baiduHotKey != null && !baiduHotKey.isEmpty())
//			return baiduHotKey;
//		else return null;
//	}
	/**
	 * 判断在solr建立 等场景下选取细粒度（k系列，n系列，et 粒度）feature
	 * @param label
	 * @return
	 * @author lixiao
	 */
	public static boolean isUsableLabel(String label){
		if(label == null)
			return false;
		if ("et".equals(label) || label.startsWith("k") || label.startsWith("n")
				|| label.equals("x"))
			return true;
		return false;
	}
	/**
	 * 用于判断xml中的自媒体文章
	 * @param source
	 * @param other
	 * @return
	 */
//	public static boolean isWeMediaForXML(String source, String other)
//	{
//		if(other==null||other.isEmpty())
//			return false;
//		if(GetUsefulKeyFromRedis.wemediaCheck(source))
//			return true;
//		if(other.contains("wemedialevel"))
//			return true;
//		return false;
//	}
	/**
	 * 用于判断cmpp中的自媒体文章
	 * @param source
	 * @param other
	 * @return
	 */
//	public static boolean isWeMediaForCMPP(String source, String other)
//	{
//		if(other==null||other.isEmpty())
//			return false;
//		if(GetUsefulKeyFromRedis.wemediaCheck(source))
//			return true;
//		if(other.contains("source=ignore")||other.contains("source=wemedia"))
//			return true;
//		return false;
//	}

	
	/**
	 * 根据传入的features 提取出c1
	 * @param features
	 * @return
	 * @author lixiao
	 */
	
	public static Set<String> getC1FromList(List<String> features){
		if (features == null || features.isEmpty()
				|| features.size() < 3) {
			return null;
		}
		Set<String> c1Set = new HashSet<String>(5);
		for (int k = 0; k < features.size(); k += 3) {
			String label = features.get(k);
			String labelType = features.get(k + 1);
			String weight = features.get(k + 2);

			double w = Double.valueOf(weight);
			if ("c".equals(labelType) && w >= 0) {
				c1Set.add(label);
			}
		}
		return c1Set;
	}
	
	
	public static boolean ifContainC1(ArrayList<String> featureList)
	{
		if(featureList == null || featureList.size() < 3)
			return false;
		boolean result = false;
		for (int i = 0; i < featureList.size() - 2; i += 3) {
			if (featureList.get(i + 1).equals("c") && Double.valueOf(featureList.get(i + 2)) > 0) {
				result = true;
				break;
			}
		}
		return result;
	}
//	public static String whatCategory(ArrayList<String> featureList)
//	{
//		if (featureList == null || featureList.isEmpty())
//		{
//			LOG.info("1.featureList is null or empty. Category is null.");
//			return null;
//		}
//		String category = null;
//		double weight = 0.0;
//		for (int i = 0; i < featureList.size() - 2; i += 3)
//		{
//			if(featureList.get(i + 1).equals("c") && Math.abs(Double.valueOf(featureList.get(i + 2))) >= 0.5)
//			{}
//				else
//					continue;
//			if (featureList.get(i).equals("体育") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "体育";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("娱乐") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "娱乐";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("美食") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "美食";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("财经1") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "财经";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("科技") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "科技";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("汽车") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "汽车";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("游戏") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "游戏";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("旅游") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "旅游";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("时政") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "时政";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("历史") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "历史";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("考古") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "考古";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("家居") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "家居";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("健康") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "健康";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("教育") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "教育";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("社会") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "社会";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("佛教") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "佛教";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("星座") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "星座";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("时尚") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "时尚";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("文化") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "文化";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("萌宠") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "萌宠";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("科学探索") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "科学探索";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("收藏") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "收藏";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("情感") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "情感";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("风水") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "风水";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("动漫") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "动漫";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("摄影") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "摄影";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("职场") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "职场";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("公益") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "公益";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("美女") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "美女";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
//			else if (featureList.get(i).equals("移民") && weight < Math.abs(Double.valueOf(featureList.get(i + 2))))
//			{category = "移民";
//			weight = Math.abs(Double.valueOf(featureList.get(i + 2)));}
// 		}
//		if(category != null && !category.equals(""))
//			return category;
//		else
//			return "其他";
//	}
	/**
	 * 从feature中选出top2分类信息
	 * @param featureList
	 * @return
	 */
	public static ArrayList<String> whatCategory(ArrayList<String> featureList)
	{
		if (featureList == null || featureList.isEmpty())
		{
			LOG.info("1.featureList is null or empty. Category is null.");
			return null;
		}
		ArrayList<String> cateList = new ArrayList<String>();
		String category1 = null;
		double weight1 = 0.0;
		String category2 = null;
		double weight2 = 0.0;
		for (int i = 0; i < featureList.size() - 2; i += 3)
		{
			if(featureList.get(i + 1).equals("c") && Math.abs(Double.valueOf(featureList.get(i + 2))) >= 0.5)
			{}
				else
					continue;
			if (featureList.get(i).equals("体育") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "体育";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "体育";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("娱乐") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "娱乐";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "娱乐";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("美食") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "美食";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "美食";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("财经1") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "财经";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "财经";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("科技") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "科技";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "科技";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("汽车") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "汽车";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "汽车";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("游戏") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "游戏";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "游戏";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("旅游") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "旅游";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "旅游";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("时政") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "时政";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "时政";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("历史") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "历史";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "历史";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("考古") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "考古";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "考古";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("家居") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "家居";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "家居";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("健康") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "健康";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "健康";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("教育") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "教育";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "教育";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("社会") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "社会";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "社会";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("佛教") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "佛教";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "佛教";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("星座") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "星座";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "星座";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("时尚") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "时尚";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "时尚";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("文化") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "文化";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "文化";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("萌宠") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "萌宠";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "萌宠";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("科学探索") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "科学探索";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "科学探索";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("收藏") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "收藏";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "收藏";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("情感") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "情感";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "情感";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("风水") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "风水";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "风水";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("动漫") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "动漫";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "动漫";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("摄影") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "摄影";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "摄影";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("职场") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "职场";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "职场";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("公益") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "公益";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "公益";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("美女") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "美女";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "美女";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("移民") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "移民";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "移民";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("天气") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "天气";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "天气";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
			else if (featureList.get(i).equals("搞笑") && weight2 < Math.abs(Double.valueOf(featureList.get(i + 2))))
			{
				if(weight1 <= Math.abs(Double.valueOf(featureList.get(i + 2))))
				{
					category2 = category1;
					weight2 = weight1;
					category1 = "搞笑";
					weight1 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
				else
				{
					category2 = "搞笑";
					weight2 = Math.abs(Double.valueOf(featureList.get(i + 2)));
				}
			}
 		}
		if(category1 != null && !category1.equals(""))
			cateList.add(category1);
		if(category2 != null && !category2.equals(""))
			cateList.add(category2);
		if(cateList.size() >= 1)	
			return cateList;
		else
			return null;
	}
}
