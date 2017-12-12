package getExcellentNum;

/**
 * 根据文章的other字段对文章来源做归类
 * 
 * @author zhangyang6
 *
 */
public class CheckArticleFrom {

	/**
	 * 根据other字段检查其来源
	 * 
	 * @param otherStr
	 * @returnhuoqu
	 */
	public static String checkFromByOther(String otherStr) {
		String re = null;
		if (otherStr == null || otherStr.isEmpty()) {
			return "null";
		}
		try {
			String[] others = otherStr.split("\\|!\\|");
			for (String s : others) {
				if (s.contains("source=")) {
					String source = s.substring(s.indexOf("source=") + 7);
					// ----对source=wemedia做特殊判断
					if (source != null && source.equals("wemedia")) {
						if (otherStr.contains("from=")) {
							if (otherStr.contains("from=ifeng")) {
								re = "凤凰自媒体";
								break;
							} else if (otherStr.contains("from=yidian")) {
								re = "一点自媒体";
								break;
							}
						}
					}
					// ----
					re = s2sMapping(source);
					break;
				}
			}
		} catch (Exception e) {
			re = null;
		}
		return re;
	}

	/**
	 * 获取所有文章来源映射关系 从文章other字段到找到分群 视频数据 机器抓取 编辑数据 自媒体数据 本地数据
	 * 
	 */
	private static String s2sMapping(String otherSource) {
		if (otherSource == null || otherSource.isEmpty()) {
			return null;
		}
		String re = null;
		switch (otherSource.trim()) {
		case "chaoslocal":
			re = "本地数据";
			break;
		case "phvideo":
			re = "视频数据";
			break;
		case "phvideo_error":
			re = "视频数据";
			break;
		case "jxeditor":
			re = "编辑数据";
			break;
		case "bjcmsHouse":
			re = "编辑数据";
			break;
		case "original":
			re = "编辑数据";
			break;
		case "ifengtoutiao":
			re = "编辑数据";
			break;
		case "appeditor":
			re = "编辑数据";
			break;
		case "spiderblog":
			re = "编辑数据";
			break;
		case "ifengpush":
			re = "编辑数据";
			break;
		case "ifengpc":
			re = "编辑数据";
			break;
		case "appeditor_error":
			re = "编辑数据";
			break;
		case "ifengpc_error":
			re = "编辑数据";
			break;
		case "cxBigPicPool":
			re = "大图长效池";
			break;
		case "jpBigPicPool":
			re = "大图精品池";
			break;
		case "channeljingpinchi":
			re = "频道精品池";
			break;
		case "yidianjxeditor":
			re = "一点精选";
			break;
		case "jpPool":
			re = "精品池数据";
			break;
		case "focusNews":
			re = "要闻数据";
			break;
		case "liveDataPool":
			re = "直播数据";
			break;
		case "cxjxPool":
			re = "长效精选";
			break;
		default:
			re = "机器抓取";
			break;
		}
		return re;
	}

	public static void main(String[] args) {
		// test
		String other = "source=jpPool|!|channel=news|!|tags=旅游|!|qualitylevel=B|!|reviewStatus=auto|!|from=yidian|!|checkPolitics=true";
		String re = checkFromByOther(other);
		System.out.println(re);
	}
}
