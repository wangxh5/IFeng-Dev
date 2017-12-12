package Entity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * <PRE>
 * 作用 : 提取标题和文章中处于特殊位置的词。并根据特征赋予不同的权重
 *   书名号中的词
 *   引号中的词
 *   方括号中的词
 *   冒号前面的词
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
 *          1.0          2015-11-5         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class SpecialWordExtract
{
	/**
	 * 对词里多余的内容进行过滤
	 * @param s
	 * @return
	 */
	private static String filter(String s)
	{
		String rs = s;
		rs = rs.replaceAll("“", "").replaceAll("”", "").replaceAll("《", "").replaceAll("》", "");
		return rs;
	}
	
	/**
	 * 计算词在内容中出现的次数
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static int strTimes(String s, String content)
	{
		if(s == null || content == null)
			return 0;
		int count = 0;
		int start = 0;
		while(content.indexOf(s,start) >= 0 && start < content.length())
		{
			count++;
			start = content.indexOf(s,start) + s.length();
		}
		return count;
	}
	
	/**
	 * 把分词的内容再合并
	 * @param s_content
	 * @return
	 */
	public static String scnt2cnt(String s_content)
	{
		if(s_content == null || s_content.isEmpty())
			return null;
		String content = s_content.replaceAll(" *_[a-zA-Z]+ *", "");
		return content;
	}
	/**
	 * 提取书名号内部的内容 优先级最高
	 * 长度大于1小于等于12
	 * @param s
	 * @return
	 */
	public static HashMap<String,Double> extractBookmark(String title,String content)
	{
		if(title == null && content == null)
			return null;
		HashMap<String,Double> wordMap = new HashMap<String,Double>();
		Pattern pattern = Pattern.compile("《(.+?)》");//\"(.+)\"|
		Matcher matcher = null;
		if(title != null)
		{
		matcher = pattern.matcher(title);
		while(matcher.find())
		{
			if(matcher.group(1).length() > 1 && commenFuncs.stringLength(matcher.group(1))<=12)
			{
				wordMap.put(matcher.group(1), -999.0);
			}
		}
		}
		if(content != null)
		{
			matcher = pattern.matcher(content);
			while(matcher.find())
			{
				if(matcher.group(1).length() > 1 && commenFuncs.stringLength(matcher.group(1))<=12)
				{
					if(wordMap.containsKey(matcher.group(1)))
					{
						wordMap.put(matcher.group(1), wordMap.get(matcher.group(1))+1);
					}
					else
					{
						wordMap.put(matcher.group(1), 1.0);
					}
				}
			}
		}
			
		return wordMap;
	}
	/**
	 * 提取引号内部的内容 优先级中等
	 * 长度大于1小于等于6
	 * @param s
	 * @return
	 */
	public static HashMap<String,Double> extractQuo(String title)
	{
		HashMap<String,Double> quMap = new HashMap<String,Double>();
		Pattern pattern = Pattern.compile("“(.+?)”|\"(.+?)\"");//\"(.+)\"|
		Matcher matcher = pattern.matcher(title);
		while(matcher.find())
		{
			if(matcher.group(1)!=null&&matcher.group(1).length() > 2&&matcher.group(1).length() <= 12)
			{
				String rs = filter(matcher.group(1));
				quMap.put(rs, 1.0);
			}
			if(matcher.group(2)!=null&&matcher.group(2).length() > 2&&matcher.group(2).length() <= 12)
			{
				String rs = filter(matcher.group(2));
				quMap.put(rs, 1.0);
			}
		}
		return quMap;
	}
	/**
	 * 提取方括号内部的内容 优先级中等
	 * 长度大于1小于等于6
	 * @param s
	 * @return
	 */
	public static HashMap<String,Double> extractSquare(String title)
	{
		HashMap<String,Double> quMap = new HashMap<String,Double>();
		Pattern pattern = Pattern.compile("^【(.+?)】|^\\[(.+?)\\]");//\"(.+)\"|
		Matcher matcher = pattern.matcher(title);
		while(matcher.find())
		{
			if(matcher.group(1)!=null&& matcher.group(1).length() >= 2&&matcher.group(1).length() <= 10&&!matcher.group(1).contains("•"))
			{
				String rs = filter(matcher.group(1));
				quMap.put(rs, 1.0);
			}
			if(matcher.group(2)!=null&& matcher.group(2).length() >= 2&&matcher.group(2).length() <= 10&&!matcher.group(2).contains("•"))
			{
				String rs = filter(matcher.group(2));
				quMap.put(rs, 1.0);
			}
		}
		return quMap;
	}
	
	/**
	 * 提取冒号前面的部分，一般为人名，或者来源
	 * 优先级最低
	 * 长度大于1小于等于5
	 * @param s
	 * @return
	 */
	public static HashMap<String,Double> extractColon(String title, String content)
	{
		HashMap<String,Double> colMap = new HashMap<String,Double>();
		if(title.contains("："))
		{
			String temp1 = title.split("：")[0];
			temp1 = temp1.replaceAll("第.+期|第.+集", "");
			//System.out.println("temp1 "+temp1);
			//当冒号在书名号内部时不作处理
			if(temp1.contains("《")&&!temp1.contains("》"))
			{
				return null;
			}
			//当冒号在引号内部时不作处理
			if(temp1.contains("“")&&!temp1.contains("”"))
			{
				return null;
			}	
			if(temp1.contains(" "))
			{
				Pattern pattern = Pattern.compile("[0-9a-zA-Z]+ [0-9a-zA-Z]+.*");
				Matcher matcher = pattern.matcher(temp1);
				if(matcher.find())
				{
					String temp2 = matcher.group();
					if(temp2.length() > 2 && temp2.length() < 11)
					{
						int count = strTimes(temp2,content);
						colMap.put(temp2, Double.valueOf(count));
					}
				}
				else
				{
					String temp2 = temp1.split(" ")[temp1.split(" ").length-1];
					temp2 =  filter(temp2);
					if(temp2.length()>1 && temp2.length()<=8)
					{
						int count = strTimes(temp2,content);
						colMap.put(temp2, Double.valueOf(count));
					}
				}
								
			}
			else
			{
				temp1 =  filter(temp1);
				if(temp1.length()>=2 && temp1.length()<=8)
				{
					int count = strTimes(temp1,content);
					colMap.put(temp1, Double.valueOf(count));
				}
			}
		}
		else
		{
//			return wordSet;
		}
		return colMap;
	}
	/**
	 * 合并提取的所有特殊词
	 * @param title
	 * @param content
	 * @return
	 */
	public static ArrayList<String> ExtractSpecialWord(String title, String content)
	{
		if(title == null)
			return null;
		ArrayList<String> specialWordList = new ArrayList<String>();
		
		HashMap<String, Double> bookmarkMap = extractBookmark(title,content);
		HashMap<String, Double> quoMap = extractQuo(title);
		HashMap<String, Double> squareMap = extractSquare(title);
		HashMap<String, Double> colonMap = extractColon(title, content);
		if(squareMap != null)
			for(Entry<String, Double> entry: squareMap.entrySet())
			{
				specialWordList.add(entry.getKey());
				specialWordList.add("s1");
				specialWordList.add("1.0");
			}
		if(bookmarkMap != null)
			for(Entry<String, Double> entry: bookmarkMap.entrySet())
			{
				if(entry.getValue() < 0 || entry.getValue() > 5)
				{
					specialWordList.add(entry.getKey());
					specialWordList.add("kb");
					specialWordList.add("1.0");
				}
				else
				{
					specialWordList.add(entry.getKey());
					specialWordList.add("kb");
					specialWordList.add("0.1");
				}
				
			}
		if(quoMap != null)
			for(Entry<String, Double> entry: quoMap.entrySet())
			{
				specialWordList.add(entry.getKey());
				specialWordList.add("kq");
				specialWordList.add("-0.5");
			}
		if(colonMap != null)
			for(Entry<String, Double> entry: colonMap.entrySet())
			{
				
				if(entry.getValue() > 10)
				{
					specialWordList.add(entry.getKey());
					specialWordList.add("ks");
					specialWordList.add("0.7");
				}
					
				else if(entry.getValue() > 1 && entry.getKey().length() >= 3 && entry.getKey().length() <= 7)
				{
					specialWordList.add(entry.getKey());
					specialWordList.add("ks");
					specialWordList.add("0.5");
				}
					
				else if(entry.getKey().length() >= 2)
				{}
			}
		return specialWordList;
	}
}
