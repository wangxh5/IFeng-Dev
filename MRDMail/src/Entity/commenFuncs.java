package Entity;

/**
 * <PRE>
 * 作用 : 
 *   工具类；
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	  日志中日期的格式不统一，存在多种格式，可能会有无法解析的格式，如19801201、201211、2012111等。
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2012-12-26        lidm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import org.apache.commons.logging.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@SuppressWarnings("ALL")
public class commenFuncs {
	// 乱码转换符合集合
	private static String[][] FilterChars = { { "<", "&lt;" }, { ">", "&gt;" },
			{ " ", "&nbsp;?" }, { "\"", "&quot;" }, { "&", "&amp;" },
			{ "/", "&#47;" }, { "\\", "&#92;" }, { "'", "&#39;" },
			{ " ", "&aacute;" }, { "·", "&middot;" }, { "“", "&ldquo;" },
			{ "”", "&rdquo;" }, { "·", "&#8226;" } };

	private static String strNumRegex1 = "^[^\\d]*?(\\d{1,3})[^\\d]*?$";
	private static Pattern patternNum1 = Pattern.compile(strNumRegex1,
			Pattern.DOTALL);

	private static MessageDigest md = null;

	// ****************************
	// Get minimum of three values
	// ****************************

	private static int min(int a, int b, int c) {
		int mi;

		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}

	// 过滤html中无效字符
	public static String formatString(String str) {
		// str = str.replaceAll("\\s{2,100}", " ");
		for (int i = 0; i < FilterChars.length; i++) {
			str = str.replaceAll(FilterChars[i][1], FilterChars[i][0]);
		}
		String regex1 = "&#\\d*?;";// &# => ?
		str = str.replaceAll(regex1, " ");
		// 去除tab space等
		str = str.replaceAll("[\r\n\t]", "");

		// //转换\r\n(CRLF)
		// str = str.replaceAll("\n"," ");
		return str;

	}

	
	/**
	 * 按照一定要求切分字符串
	 * @param string
	 * @return
	 */
	public static List<String> getList(String string){
		if(string == null){
			return null;
		}
		String [] ts = string.split(", |/|，");		
		List<String> tslist = new ArrayList<String>();

		for(String str : ts){
			str = str.replaceAll("》|《", "");
			str = str.trim();
			
			tslist.add(str);
		}
		
		return tslist;
	}
	
	/*
	 * 以probs中概率，rd种子进行采样；
	 */
	public static int Sampling(ArrayList<Double> probs, Random rd) {
		double rdv = rd.nextDouble();
		Double sum = 0.0;
		for (int i = 0; i < probs.size(); ++i) {
			sum += probs.get(i);
			if (sum > rdv)
				return i;
		}
		return 0;
	}

	/*
	 * Compute Levenshtein distance
	*/
	public static int LD(String s, String t) {
		int d[][]; // 矩阵
		int n = s.length();
		int m = t.length();
		int i; // 遍历s的
		int j; // 遍历t的
		char ch1; // s的
		char ch2; // t的
		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}
		for (i = 1; i <= n; i++) { // 遍历s
			ch1 = s.charAt(i - 1);
			// 去匹配t
			for (j = 1; j <= m; j++) {
				ch2 = t.charAt(j - 1);
				if (ch1 == ch2) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]
						+ temp);
			}
		}
		return d[n][m];

	}

	// *****************************
	// Compute Levenshtein distance for strings
	// *****************************

	public static int LD(String[] s, String[] t) {
		int d[][]; // 矩阵
		int n = s.length;
		int m = t.length;
		int i; // 遍历s的
		int j; // 遍历t的
		String s1; // s的
		String s2; // t的
		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}
		for (i = 1; i <= n; i++) { // 遍历s
			s1 = s[i - 1];
			// 去匹配t
			for (j = 1; j <= m; j++) {
				s2 = t[j - 1];
				if (s1.equals(s2) == true) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]
						+ temp);
			}
		}
		return d[n][m];

	}

	// 去除开头和结尾的空格、换行等
	public static String rmBE(String strTxt) {
		int i, j;
		for (i = 0; i < strTxt.length(); i++) {
			char tmp = strTxt.charAt(i);
			if (!(tmp == ' ' || tmp == '\r' || tmp == '\n' || tmp == '\t'))
				break;
		}
		for (j = strTxt.length() - 1; j >= i; j--) {
			char tmp = strTxt.charAt(j);
			if (!(tmp == ' ' || tmp == '\r' || tmp == '\n' || tmp == '\t'))
				break;
		}
		return new String(strTxt.substring(i, j + 1));
	}

	/*
	 * 通过字符串是否是一个形态正常的词 整理出所有单词的有限种形态，进行过滤
	 */
	public static int isNormalWord(String strWord) {
		// TODO Auto-generated method stub
		if (strWord.matches("^.*?[的地了中是]$"))
			return -1;
		if (strWord.matches("^[的地了中是].*?$"))
			return -1;
		if (strWord.matches("^[^\\p{P}]*$"))
			return 1;
		if (strWord.matches("^[^\\p{P}]{1,10}[-.·][^\\p{P}]{1,10}$"))
			return 2;
		if (strWord.matches("^[\"“][^\\p{P}]{1,10}[\"”][^\\p{P}]{1,10}$"))
			return 3;
		if (strWord
				.matches("^[\"“][^\\p{P}]{1,10}[-.·][^\\p{P}]{1,10}[\"”][^\\p{P}]{1,10}$"))
			return 4;
		if (strWord.matches("^《.{1,10}》$"))
			return 5;
		return -1;
	}

	// 判断两个字符串的相似度;求交集运算，比编辑距离计算量小
	public static float simRate(String str1, String str2) {
		// TODO Auto-generated method stub
		if (str1 == null || str2 == null)
			return -1;

		int len1 = str1.length();
		int len2 = str2.length();
		HashSet<String> hm_tmp = new HashSet<String>();
		int combineNum = 0;
		for (char s : str1.toCharArray()) {
			hm_tmp.add(s+"");
		}
		for (char s : str2.toCharArray()) {
			if (hm_tmp.contains(s+""))
				combineNum++;
		}

		int maxLen = (len1 >= len2) ? len1 : len2;
		if (maxLen == 0)
			return 1;
		return (combineNum / (float) maxLen);
	}

	// 判断两个字符串数组的相似度;求交集运算，比编辑距离计算量小
	public static float simRate(String[] str1, String[] str2) {
		// TODO Auto-generated method stub
		if (str1 == null || str2 == null)
			return -1;

		int len1 = str1.length;
		int len2 = str2.length;
		HashSet<String> hm_tmp = new HashSet<String>();
		int combineNum = 0;
		for(String s:str1){
			hm_tmp.add(s);
		}
		for(String s:str2){
			if(hm_tmp.contains(s))
				combineNum++;
		}
		
		int maxLen = (len1 >= len2) ? len1 : len2;
		if (maxLen == 0)
			return 1;
		return (combineNum / (float) maxLen);
	}

//	// 判断两个字符串的差异度
//	public static float diffRate(String str1, String str2) {
//		// TODO Auto-generated method stub
//		if (str1 == null || str2 == null)
//			return -1;
//		int len1 = str1.length();
//		int len2 = str2.length();
//		int diff = LD(str1, str2);
//		int maxLen = (len1 >= len2) ? len1 : len2;
//		if (maxLen == 0)
//			return 0;
//		return (diff / (float) maxLen);
//	}
//
//	
//	// 判断两个字符串数组的差异度
//	public static float diffRate(String[] s1, String[] s2) {
//		// TODO Auto-generated method stub
//		if (s1 == null || s2 == null)
//			return -1;
//		int len1 = s1.length;
//		int len2 = s2.length;
//		int diff = LD(s1, s2);
//		int maxLen = (len1 >= len2) ? len1 : len2;
//		if (maxLen == 0)
//			return 0;
//		return (diff / (float) maxLen);
//	}
	
	// 有个问题：(people、?.people，两个站，同一个网名，fuck)
	public static String getSiteName(String strUrl) {
		// TODO Auto-generated method stub
		if (strUrl == null)
			return null;
		int i1 = strUrl.indexOf(".com");
		int i2 = strUrl.indexOf(".cn");
		int i3 = strUrl.indexOf(".net");
		int e = 0;
		if (i1 > 0)
			e = i1;
		else if (i2 > 0)
			e = i2;
		else if (i3 > 0)
			e = i3;
		if (e == 0)
			return "null";
		int b = e - 1;
		while (b >= 0) {
			if (strUrl.charAt(b) == '.')
				break;
			b--;
		}
		if (b <= 0)
			return "null";
		return new String(strUrl.substring(b + 1, e));
	}

	// find char num in a string
	public static int findCharNum(String str, char s) {
		char[] cs = str.toCharArray();
		int num = 0;
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] == s)
				num++;
		}
		return num;
	}

	/* 获取url绝对路径 */
	@SuppressWarnings("finally")
	public static String getAbsoluteURL(String baseURI, String relativePath) {
		if (baseURI == null || relativePath == null)
			return null;
		// 书签处理
		if (relativePath.equals("#"))
			return baseURI;
		baseURI = baseURI.trim();
		relativePath = relativePath.trim();
		String abURL = null;
		try {
			URI base = new URI(baseURI);// 基本网页URI
			// char encoding
			relativePath = relativePath.replaceAll("\\s", "%20");
			relativePath = relativePath.replaceAll("\\\\", "/");
			URI abs = base.resolve(relativePath);// 解析于上述网页的相对URL，得到绝对URI
			URL absURL = abs.toURL();// 转成URL
			// System.out.println(absURL);
			abURL = absURL.toString();
		} catch (MalformedURLException e) {
			// e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return abURL;
		}
	}

	// 指定字符格式
	public static int writeResult(String dirName, String strFileName,
			String strInput, String strEnConding, boolean isAppending,
			Log logger) {
		try {
			if (dirName == null)
				return -1;
			if (!dirName.isEmpty()) {
				File dirFile = new File(dirName);
				if (!(dirFile.exists()) && !(dirFile.isDirectory())) {
					boolean creadok = dirFile.mkdirs();
					if (creadok) {

					} else {
						if (logger != null)
							logger.error("create dir failed：" + dirName);
						// System.out.println(" err:创建文件夹失败！ ");
						return -1;
					}
				}
			}
			File f = new File(dirName + strFileName);
			if (!f.exists()) {
				if (f.createNewFile()) {
					// writeResult("log.txt",strFileName +":文件创建成功！");
				} else {
					if (logger != null)
						logger.error("create file failed：" + strFileName);
					return -1;
				}
			}
			// }else{
			// logger.warn("file already exsits："+strFileName);
			// }
			if (strEnConding == null)
				strEnConding = "utf-8";
			OutputStreamWriter ow = new OutputStreamWriter(
					new FileOutputStream(f, isAppending), strEnConding);
			ow.write(strInput);
			// System.out.println(strInput);
			// System.out.print("\n=============="+strEnConding+"===============\n");
			// System.out.println(new
			// String(strInput.getBytes("GB2312"),"utf-8"));
			ow.close();
			ow = null;
			f = null;

		} catch (Exception e) {
			if (logger != null) {
				logger.error("write error:" + e.getStackTrace().toString());
				// //logger.info(e.getMessage());
				// e.printStackTrace();
			}
		}
		return 1;
	}

	// / <summary>
	// / 把一个字符串中的 低序位 ASCII 字符 替换成 &#x 字符
	// / 转换 ASCII 0 - 8 -> &#x0 - &#x8
	// / 转换 ASCII 11 - 12 -> &#xB - &#xC
	// / 转换 ASCII 14 - 31 -> &#xE - &#x1F
	// / </summary>
	// / <param name="tmp"></param>
	// / <returns></returns>
	public static String ReplaceLowOrderASCIICharacters(String strIn) {
		StringBuffer sbRes = new StringBuffer();
		char[] ccs = strIn.toCharArray();
		for (int i = 0; i < ccs.length; i++) {
			int ss = (int) ccs[i];
			if (((ss >= 0) && (ss <= 8)) || ((ss >= 11) && (ss <= 12))
					|| ((ss >= 14) && (ss <= 32)))
				sbRes.append(String.format("&#x%d;", ss));
			else
				sbRes.append(ccs[i]);
		}
		return sbRes.toString();
	}

	public static String changeStrEnCoding(String strIn, String strEnCodingIn,
			String strEnCodingOut) {
		// 转码
		if (strEnCodingIn == null)
			strEnCodingIn = "utf-8";
		String strTmp = strIn;
		if (!strEnCodingIn.toLowerCase().equals(strEnCodingOut)) {
			try {
				strTmp = new String(strTmp.getBytes(strEnCodingIn),
						strEnCodingOut);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return strTmp;
	}

	public static int computeWordsLen(String strTextOrign) {
		// TODO Auto-generated method stub
		if (strTextOrign == null || strTextOrign.trim().isEmpty())
			return 0;
		int wordsLen = 0;
		char[] cs = strTextOrign.trim().toCharArray();
		char c = 128;
		for (int i = 0; i < cs.length; i++) {
			c = cs[i];
			if (c == ' ') {
				if (i > 0 && cs[i - 1] <= 127)
					wordsLen++;
			}
			while (c == ' ' && i < cs.length) {
				i++;
				c = cs[i];
			}
			// 亚洲字符
			// System.out.print(c-0);
			if (c > 127) {
				wordsLen++;
				if (i > 0 && cs[i - 1] <= 127 && cs[i - 1] != ' ')
					wordsLen++;
			}
		}
		if (c <= 127)
			wordsLen++;
		return wordsLen;
	}

	public static String getMD5Code(String Input) {
		StringBuffer makeMD5 = new StringBuffer();
		try {
			if (md == null)
				md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(Input.getBytes());
			byte bytes[] = md.digest();
			for (int i = 0; i < bytes.length; i++) {
				String s = Integer.toHexString(bytes[i] & 0xff);
				if (s.length() == 1)
					makeMD5.append(0);
				makeMD5.append(s);
				s = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return makeMD5.toString();
	}
	
//	public static String convertToHexString(String str) throws IOException {
//		return (new sun.misc.BASE64Encoder()).encode(str.getBytes("gb2312"));
//	}

	// 半角转全角
	public static String ToSBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	// ungzip
	public static byte[] unGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/**
	 * ip地址转成整数.
	 * 
	 * @param ip
	 * @return
	 */
	public static long ip2dec(String ip) {
		String[] ips = ip.split("[.]");
		long num = 16777216L * Long.parseLong(ips[0]) + 65536L
				* Long.parseLong(ips[1]) + 256 * Long.parseLong(ips[2])
				+ Long.parseLong(ips[3]);
		return num;
	}

	/**
	 * 整数转成ip地址.
	 * 
	 * @param ipLong
	 * @return
	 */
	public static String dec2ip(long ipLong) {
		long mask[] = { 0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000 };
		long num = 0;
		StringBuffer ipInfo = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			num = (ipLong & mask[i]) >> (i * 8);
			if (i > 0)
				ipInfo.insert(0, ".");
			ipInfo.insert(0, Long.toString(num, 10));
		}
		return ipInfo.toString();
	}

	/**
	 * Compress string
	 * 
	 * @param str
	 * @return
	 */
	public static String CompressStr(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}

		// no effect when the lenth < 72
		if (str.length() <= 72) {
			return str;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String value = null;
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes());
			gzip.close();

			value = out.toString("ISO-8859-1");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Uncompress string
	 * 
	 * @param str
	 * @return
	 */
	public static String UnCompressStr(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}

		// not compressed when the length <= 72
		if (str.length() <= 72) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPInputStream gunzip = null;
		String value = null;
		byte[] buffer = new byte[256];
		int n;
		try {
			in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
			gunzip = new GZIPInputStream(in);
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			value = out.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	/**
	 * Get path of log file,eg /data/stat/2012-12-12/2358.sta
	 * 
	 * @param dirpath
	 * @param i
	 * @return
	 */
	public static String getLogFilePath(String dirpath, int i,int unit) {
		int currentTime = (i * unit / 60) * 100 + ((i * unit) % 60);
		StringBuffer logfilenamebf = new StringBuffer();
		if (currentTime < 10)
			logfilenamebf.append(dirpath).append("000").append(currentTime)
					.append(".sta");
		else if (currentTime >= 10 && currentTime < 100)
			logfilenamebf.append(dirpath).append("00").append(currentTime)
					.append(".sta");
		else if (currentTime >= 100 && currentTime < 1000)
			logfilenamebf.append(dirpath).append("0").append(currentTime)
					.append(".sta");
		else
			logfilenamebf.append(dirpath).append(currentTime).append(".sta");

		return logfilenamebf.toString();
	}

	/**
	 * Get the next date according to specifiedDay
	 * 
	 * @param specifiedDay
	 * @return
	 */
	public static String getSpecifiedDayAfter(String specifiedDay) {
		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + 1);

		String dayAfter = new SimpleDateFormat("yyyy-MM-dd")
				.format(c.getTime());
		return dayAfter;
	}
	
	
	/*
	 * 根据Unicode编码完美的判断中文汉字和符号 
	 */
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}
	
	/*
	 * 判断中文汉字和符号
	 */
	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		if (sPath == null || sPath.isEmpty())
			return false;
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists()){
			return true;
		} 
		if(!dirFile.isDirectory()) {
			return false;
		}
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				if (!files[i].delete())
					return false;
			}// 删除子目录
			else {
				if (!deleteDirectory(files[i].getAbsolutePath()))
					return false;
			}
		}
		
		//删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}
	
	 /** 
     * 获取字符串的长度，中文字符和中文标点占一个字符,英文数字占半个字符 
     * 
     * @param value  指定的字符串           
     * @return 字符串的长度 
     */  

    public static float stringLength(String value) {  
    	if (value == null) 
    		return 0;
    	float valueLength = 0;  
        String chinese = "[\u4e00-\u9fa5]";  
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1  
        for (int i = 0; i < value.length(); i++) {  
            // 获取一个字符  
            String temp = value.substring(i, i + 1);  
            // 判断是否为中文字符  
            if (temp.matches(chinese)) {  
                // 中文字符长度为1  
                valueLength += 1;  
            } else if (temp.matches("[“”《》，。、！：；【】（）￥？]")) {
            	// 中文标点长度为1  
            	valueLength += 1; 
			} else {  
                // 其他字符长度为0.5  
                valueLength += 0.5;  
            }  
        }  
        //进位取整  
        return valueLength;
    }  
	
    
    /** 
     * 根据前端展示的字符串长度限制截取字符串，中文字符和中文标点占一个字符,英文数字占半个字符 
     * 
     * @param title  给定的字符串标题           
     * @param limit  限制长度     
     * @return 不大于限制长度的字符串
     */  
    public static String subString(String title, float limit) {
    	if(title == null || limit < 0)
    		return null;
    	float valueLength = 0;  
        String chinese = "[\u4e00-\u9fa5]";  
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1  
        for (int i = 0; i < title.length(); i++) {  
            // 获取一个字符  
            String temp = title.substring(i, i + 1);  
            // 判断是否为中文字符  
            if (temp.matches(chinese)) {  
                // 中文字符长度为1  
                valueLength += 1;  
            } else if (temp.matches("[“”《》，。、！：；【】（）￥？]")) {
            	// 中文标点长度为1  
            	valueLength += 1; 
			} else {  
                // 其他字符长度为0.5  
                valueLength += 0.5;  
            }  
            
            //若字符串大于限制长度，则截取返回
            if (valueLength >= limit) {
				return new String(title.substring(0, i));
			}
        }  
      //若字符串小于限制长度，则返回原字符串
    	return title;
    }
    
    /**
     * 将一个整数，转成小于8个字节的可视字符串；
	 * 将64位的long值，转为10进制形式，再逐次截取2位构成一个范围是[0,100)的整数，找到ascii码对应的单个字符，最后拼装所有ascii字符构成一个小于8个字节长的字符串；
	 * 利用ascii码表进行可视转换，注意规则如下：
	 * 1）0-9的ascii值，直接用十进制0-9表达字符；
	 * 2）10-99的ascii值，统一加22，也即用[32,121]对应的ascii字符表达；
	 * 3）ascii值如果落在[48,52]，将值转到[122,126]，避免和1）冲突；
	 * 4)ascii值如果落在[53,57],为避免和1）冲突，直接用十进制字符表达,也即字符是两位："53","54",...,"57"
	 * @param num
	 * @return 返回转换后的byte数组
	 */
	public static String longToString(long num) {
		if(num >=0L && num <=9L)
			return String.valueOf(num);
		StringBuffer sbRes = new StringBuffer();
		if(num <= -1L)
			sbRes.append("-");
		num = Math.abs(num);
		long inter = 0;
		while(num>0L){
			inter = num%100L;
			num = num/100L;
			if(inter >=0&&inter<=9){
				sbRes.append(inter);
				continue;
			}
			inter += 22L;
			if(inter>=48L && inter<=52L){
				inter += 74L;
				sbRes.append((char) inter);
			}else if(inter>=53L && inter<=57L){
				sbRes.append(inter);
			}else
				sbRes.append((char) inter);
		}
		
		return sbRes.toString();
	}

	/**
	 * 将8字节的byte数组转成一个long值
	 * 
	 * @param byteArray
	 * @return 转换后的long型数值
	 */
	public static long byteArrayToLong(byte[] byteArray) {
		byte[] a = new byte[8];
		int i = a.length - 1, j = byteArray.length - 1;
		for (; i >= 0; i--, j--) {// 从b的尾部(即int值的低位)开始copy数据
			if (j >= 0)
				a[i] = byteArray[j];
			else
				a[i] = 0;// 如果b.length不足4,则将高位补0
		}
		// 注意此处和byte数组转换成int的区别在于，下面的转换中要将先将数组中的元素转换成long型再做移位操作，
		// 若直接做位移操作将得不到正确结果，因为Java默认操作数字时，若不加声明会将数字作为int型来对待，此处必须注意。
		long v0 = (long) (a[0] & 0xff) << 56;// &0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
		long v1 = (long) (a[1] & 0xff) << 48;
		long v2 = (long) (a[2] & 0xff) << 40;
		long v3 = (long) (a[3] & 0xff) << 32;
		long v4 = (long) (a[4] & 0xff) << 24;
		long v5 = (long) (a[5] & 0xff) << 16;
		long v6 = (long) (a[6] & 0xff) << 8;
		long v7 = (long) (a[7] & 0xff);
		return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7;
	}

	
	/**
	 *	sigmoid smoothing for location probability.
	 * @param sigmoid value
	 * @return
	 */
	public static double sigmoid(double x){		
		return  1/(1+Math.pow(2.7182818284590455,-x));
	}
	
	
	/**
	 * 转义函数.
	 * 
	 * @param string
	 *            s
	 * @return
	 */
	public static String escapeQueryChars(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);

			if ((c == '\\') || (c == '+') || (c == '-') || (c == '!')
					|| (c == '(') || (c == ')') || (c == ':') || (c == '^')
					|| (c == '[') || (c == ']') || (c == '"') || (c == '{')
					|| (c == '}') || (c == '~') || (c == '*') || (c == '?')
					|| (c == '|') || (c == '&') || (c == ';') || (c == '/')
					|| (Character.isWhitespace(c))) {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//字符的几种类型
	enum CharType {
		DELIMITER, // 非字母截止字符，例如，．）（　等等　（ 包含U0000-U0080）
		NUM, // 2字节数字１２３４
		LETTER, // gb2312中的，例如:ＡＢＣ，2字节字符同时包含 1字节能表示的 basic latin and latin-1
				// OTHER,// 其他字符
		CHINESE, // 中文字
		OTHER;
	}

	// 特殊词加权,如果是字母算0.5，汉字算1，数字算0
	public static int cmpWordLen(String word) {
		// TODO Auto-generated method stub
		if (word == null || word.isEmpty())
			return 0;
		char[] chars = word.toCharArray();
		float len = 0;
		for (char c : chars) {
			if (checkType(c) == CharType.NUM)
				;
			else if (checkType(c) == CharType.CHINESE)
				len = len + 1.0f;
			else
				len = len + 0.5f;
		}
		return (int) len;
	}

	/**
	 * 判断输入char类型变量的字符类型
	 * 
	 * @param c
	 *            char类型变量
	 * @return CharType 字符类型
	 */
	public static CharType checkType(char c) {
		CharType ct = null;

		// 中文，编码区间0x4e00-0x9fbb
		if ((c >= 0x4e00) && (c <= 0x9fbb)) {
			ct = CharType.CHINESE;

		}
		// Halfwidth and Fullwidth Forms， 编码区间0xff00-0xffef
		else if ((c >= 0xff00) && (c <= 0xffef)) {
			// 2字节英文字
			if (((c >= 0xff21) && (c <= 0xff3a))
					|| ((c >= 0xff41) && (c <= 0xff5a))) {
				ct = CharType.LETTER;
			} else if ((c >= 0xff10) && (c <= 0xff19)) {
				ct = CharType.NUM;
			} // 2字节数字
			else
				ct = CharType.DELIMITER; // 其他字符，可以认为是标点符号
		}

		// basic latin，编码区间 0000-007f
		else if ((c >= 0x0021) && (c <= 0x007e)) { // 1字节数字
			if ((c >= 0x0030) && (c <= 0x0039)) {
				ct = CharType.NUM;
			} // 1字节字符
			else if (((c >= 0x0041) && (c <= 0x005a))
					|| ((c >= 0x0061) && (c <= 0x007a))) {
				ct = CharType.LETTER;
			}
			// 其他字符，可以认为是标点符号
			else
				ct = CharType.DELIMITER;
		}
		// latin-1，编码区间0080-00ff
		else if ((c >= 0x00a1) && (c <= 0x00ff)) {
			if ((c >= 0x00c0) && (c <= 0x00ff)) {
				ct = CharType.LETTER;
			} else
				ct = CharType.DELIMITER;
		} else
			ct = CharType.OTHER;

		return ct;
	}
	
	/**
	 * @Title: longstrToDate
	 * @Description: 将长字符串格式"yyyy-MM-dd HH:mm:ss"转为时间格式
	 * @author liu_yi
	 * @param strDate
	 * @return
	 * @throws
	 */
	public static Date longstrToDate(String strDate,String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);

		return strtodate;
	}
	
	/**
	 * @Title: longstrToDate
	 * @Description: 将长字符串格式"yyyy-MM-dd HH:mm:ss"转为时间格式
	 * @author liu_yi
	 * @param strDate
	 * @return
	 * @throws
	 */
	public static Date longstrToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);

		return strtodate;
	}

	/**
	 * 将时间格式转为长字符串格式"yyyy-MM-dd HH:mm:ss"
	 * 
	 * @author liu_yi
	 * @param date
	 * @return String格式时间
	 */
	public static String date2Longstr(Date date,String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

		String dateString = formatter.format(date);
		return dateString;
	}
	
	/**
	 * 将时间格式转为长字符串格式"yyyy-MM-dd HH:mm:ss"
	 * 
	 * @author liu_yi
	 * @param date
	 * @return String格式时间
	 */
	public static String date2Longstr(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		
		String dateString = formatter.format(date);
		return dateString;
	}
	
	
	/**
	 * date字符串类型转为long
	 * @author wuyg1
	 * @param datestr
	 * @return  如果datastr为null,则返回-1；
	 */
	public static long datestr2Long(String datestr,String format){
		
		if(null == datestr || "".equals(datestr)){
             return -1;			
		}
		
		if(datestr.contains("#TIME#")){
			datestr = datestr.replace("#TIME#", "");
		}
		
		SimpleDateFormat sdf=new SimpleDateFormat(format);
		long timeStart = 0;
		try {
			timeStart = sdf.parse(datestr).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeStart;
	}
	
	
	/**
	 * date字符串类型转为long
	 * @author wuyg1
	 * @param datestr
	 * @return  如果datastr为null,则返回-1；
	 */
	public static long datestr2Long(String datestr){
		
		if(null == datestr || "".equals(datestr)){
             return -1;			
		}
		
		if(datestr.contains("#TIME#")){
			datestr = datestr.replace("#TIME#", "");
		}
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long timeStart = 0;
		try {
			timeStart = sdf.parse(datestr).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeStart;
	}
	
	/**
	 * 正则匹配String，返回匹配值
	 */
	public static List<String> match(String rex,String txt){
		List<String> result=new ArrayList<String>();
		Pattern pattern = Pattern.compile(rex,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		Matcher matcher = pattern.matcher(txt);
		while(matcher.find()) {
			String res=matcher.group(0);
			result.add(res);
		}
		if(result.size()==0){
			result.add(null);
		}
		return result;
	}
	
	/**
	 * 判断是否一个值是否在一个数组中，如包含，返回true
	 * 
	 * @author liu_yi
	 * @param array
	 * @param v
	 * @return boolean
	 */
	public static <T> boolean valueInArrayJudge(final T[] array, final T v) {
		for (final T e : array)
			if (e == v || v != null && v.equals(e))
				return true;

		return false;
	}
	
	/**
	 * @Title: sortMapByValue
	 * @Description:  Map按value进行排序，不指定参数时，默认升序排
	 * @author liu_yi
	 * @param map
	 * @return
	 * @throws
	 */
	@SuppressWarnings("rawtypes")
	public static <K extends Comparable,V extends Comparable> Map<K,V> sortMapByValues(Map<K,V> map, final String sortType){
        List<Entry<K,V>> entries = new LinkedList<Entry<K,V>>(map.entrySet());
      
        Collections.sort(entries, new Comparator<Entry<K,V>>() {
            @SuppressWarnings("unchecked")
			@Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
            	if (sortType.equals("ascend")) {
            		return o1.getValue().compareTo(o2.getValue());
            	} else if (sortType.equals("descend")){
            		return -o1.getValue().compareTo(o2.getValue());
            	} else {
            		return o1.getValue().compareTo(o2.getValue());
            	} 
            }
        });
      
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
      
        for(Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
    }
	
	/**
	 * @Title: isXMLCharacter
	 * @Description:  isXMLCharacter
	 * @author lixiao
	 * @param
	 * @return
	 * @throws
	 */
	public static boolean isXMLCharacter(int c) {
		if (c <= 0xD7FF) {
			if (c >= 0x20)
				return true;
			else
				return c == '\n' || c == '\r' || c == '\t';
		}
		return (c >= 0xE000 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0x10FFFF);
	}
}
