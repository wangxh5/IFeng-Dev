package dataBase;

/**
 * item with features
 * item的特征表达类；包含item基本信息以及特征表达信息
 */

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * <PRE>
 * 作用 : 
 *   item的表达类；
 *   1）item的基本信息：ID、title 、url、分词后title、分词后content信息
 *   2）item的特征表达信息：一级分类、二级分类、栏目或者专题事件、topic、keywords实体、稿源（足够优质的来源才会做出表达）；
 * 使用 : 
 *   
 * 示例 :
 * 
 * 注意 :
 *   来源：足够优质和垂直的来源才会做出表达，一般情况下表达为空；
 *   特征的表达，需要给出在当前item中的权重[0-1]；（初始可以默认为1）
 *   
 *   feature的类型:"c"一级分类（足球，篮球）， "sc"二级分类（中国足球）， "cn"专题事件， "t"主题词， "s"稿源，  "s1" 少量优质栏目 
 *   			   "et"实体库词， "kb"书名号中的词（游戏名，书名，电影名等）， "ks"冒号前的词（发言人，地区等） "kq"引号中的词（特指，特定词），
 *   			   "kr"分词得到的人名，自定义实体词 "kl"分词得到的地名  
 *    
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年4月8日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class itemf {
	@Expose
	@SerializedName("ID")
	private String id; // ID
	@Expose
	@SerializedName("newcmppid")
	private String newcmppid; // newcmppid
	@Expose
	@SerializedName("subid")
	private String subid; // subid
	@Expose
	@SerializedName("zmtid")
	private String zmtid; // zmtid
	@Expose
	@SerializedName("simId")
	private String simId; //
	@Expose
	@SerializedName("url")
	private String url; // url
	@Expose
	@SerializedName("title")
	private String title; // 标题
	@Expose
	@SerializedName("splitTitle")
	private String splitTitle; // 分词后的标题
	@Expose
	@SerializedName("content")
	private String content; // 内容
	@Expose
	@SerializedName("splitContent")
	private String splitContent; // 分词后的内容
	@Expose
	@SerializedName("publishedTime")
	private String publishedTime; // 发布时间
	@Expose
	@SerializedName("source")
	private String source; // 稿源
	@Expose
	@SerializedName("original") // 数据获取来源
	private String original; //
	@Expose
	@SerializedName("appId")
	private String appId;// 这个item所对应的应用，如newspush、ifengapp等
	@Expose
	@SerializedName("docType")
	private String docType;// item类型，如slide,doc,video
	@Expose
	@SerializedName("beauty")
	private boolean beauty;// 美女分类的结果
	@Expose
	@SerializedName("showStyle")
	private String showStyle;// 前端显示样式，支持从cmpp后端人工控制
	@Expose
	@SerializedName("isCreation")
	private boolean isCreation;// 是否原创
	@Expose
	@SerializedName("modifyTime")
	private long modifyTime;// 标识feature修改的最后时间
	@Expose
	@SerializedName("other")
	private String other; // 其他
	@Expose
	@SerializedName("porn")
	private boolean porn; // 色情
	@Expose
	@SerializedName("titleParty")
	private boolean titleParty;// 标题党
	@Expose
	@SerializedName("qualityEvalLevel") // 质量评分
	private String qualityEvalLevel;
	@Expose
	@SerializedName("dynamic_qualityEvalLevel") // 动态质量评分
	private String dynamic_qualityEvalLevel;
	@Expose
	@SerializedName("timeSensitive") // 时效性
	private String timeSensitive;
	@Expose
	@SerializedName("latentTopic") // 主题
	private String latentTopic;
	@Expose
	@SerializedName("imgNum") // 图片数量
	private int imgNum;
	@Expose
	@SerializedName("CNwordNum")
	private int CNwordNum; // 文章内容汉字数
	@Expose
	@SerializedName("ENwordNum")
	private int ENwordNum; // 文章内容英文单词数
	@Expose
	@SerializedName("biaodianNum")
	private int biaodianNum; // 文章内容标点符号数
	@Expose
	@SerializedName("sentenceNum")
	private int sentenceNum;
	@Expose
	@SerializedName("paragraphNum")
	private int paragraphNum;
	@Expose
	@SerializedName("canbeSlide") //
	private boolean canbeSlide;
	@Expose
	@SerializedName("uv") // 文章点击数
	private int uv; // shareNum,storeNum,joinCommentNum,tj_recNum,tj_clickNum
	@Expose
	@SerializedName("storeNum") //
	private int storeNum; //
	@Expose
	@SerializedName("shareNum")
	private int shareNum; //
	@Expose
	@SerializedName("joinCommentNum")
	private int joinCommentNum; //
	@Expose
	@SerializedName("tj_recNum")
	private int tj_recNum; //
	@Expose
	@SerializedName("tj_clickNum")
	private int tj_clickNum; //
	@Expose
	@SerializedName("specialParam")
	private String specialParam;
	@Expose
	@SerializedName("thumbnailpic")
	protected String thumbnailpic;
	@Expose
	@SerializedName("picFingerprint")
	protected String picFingerprint;
	@Expose
	@SerializedName("summary")
	protected String summary;
	@Expose
	@SerializedName("readableFeatures")
	protected String readableFeatures;
	@Expose
	@SerializedName("authLabel")
	protected String authLabel;
	@Expose
	@SerializedName("multihead")
	protected String multihead;
	@Expose
	@SerializedName("relatedfeatures")
	protected String relatedfeatures;
	@Expose
	@SerializedName("topic1")
	protected String topic1;
	@Expose
	@SerializedName("topic2")
	protected String topic2;
	@Expose
	@SerializedName("topic3")
	protected String topic3;

	public String getLatentTopic() {
		return latentTopic;
	}

	public void setLatentTopic(String latentTopic) {
		this.latentTopic = latentTopic;
	}

	public String getID() {
		return this.id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getNewcmppid() {
		return this.newcmppid;
	}

	public void setNewcmppid(String newcmppid) {
		this.newcmppid = newcmppid;
	}

	public String getSubid() {
		return this.subid;
	}

	public void setSubid(String subid) {
		this.subid = subid;
	}

	public String getZmtid() {
		return this.zmtid;
	}

	public void setZmtid(String zmtid) {
		this.zmtid = zmtid;
	}

	public String getSimId() {
		return this.simId;
	}

	public void setSimId(String simId) {
		this.simId = simId;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSplitTitle() {
		return this.splitTitle;
	}

	public void setSplitTitle(String splitTitle) {
		this.splitTitle = splitTitle;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSplitContent() {
		return this.splitContent;
	}

	public void setSplitContent(String splitContent) {
		this.splitContent = splitContent;
	}

	public String getPublishedTime() {
		return this.publishedTime;
	}

	public void setPublishedTime(String publishedTime) {
		this.publishedTime = publishedTime;
	}

	public String getOriginal() {
		return this.original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAppId() {
		return this.appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDocType() {
		return this.docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public boolean getBeauty() {
		return this.beauty;
	}

	public void setBeauty(boolean beauty) {
		this.beauty = beauty;
	}

	public String getShowStyle() {
		return this.showStyle;
	}

	public void setShowStyle(String showStyle) {
		this.showStyle = showStyle;
	}

	public boolean getisCreation() {
		return this.isCreation;
	}

	public void setIsCreation(boolean isCreation) {
		this.isCreation = isCreation;
	}

	public boolean getPorn() {
		return this.porn;
	}

	public void setPorn(boolean porn) {
		this.porn = porn;
	}

	public int getUV() {
		return this.uv;
	}

	public void setUV(int uv) {
		this.uv = uv;
	}

	public int getStoreNum() {
		return this.storeNum;
	}

	public void setStoreNum(int storeNum) {
		this.storeNum = storeNum;
	}

	public int getShareNum() {
		return this.shareNum;
	}

	public void setShareNum(int shareNum) {
		this.shareNum = shareNum;
	}

	public int getJoinCommentNum() {
		return this.joinCommentNum;
	}

	public void setJoinCommentNum(int joinCommentNum) {
		this.joinCommentNum = joinCommentNum;
	}

	public int getTjRecNum() {
		return this.tj_recNum;
	}

	public void setTjRecNum(int tj_recNum) {
		this.tj_recNum = tj_recNum;
	}

	public int getTjClickNum() {
		return this.tj_clickNum;
	}

	public void setTjClickNum(int tj_clickNum) {
		this.tj_clickNum = tj_clickNum;
	}

	public long getModifyTime() {
		return this.modifyTime;
	}

	public void setModifyTime(int modifyTime) {
		this.modifyTime = modifyTime;
	}

	public void setModifyTimeCurrent() {
		this.modifyTime = System.currentTimeMillis();
	}

	public void setModifyTimeDate(String date) {// 把一定格式的时间字符串转换成long并设置为修改时间
		try {
			this.modifyTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String displayModifyTimeToDate() {// 返回字符串格式化的修改时间
		String ModifyDate = new java.text.SimpleDateFormat("yyyy-MM-dd#HH:mm:ss")
				.format(new java.util.Date(this.modifyTime));
		return ModifyDate;
	}

	public String getOther() {
		return this.other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public boolean getTitleParty() {
		return this.titleParty;
	}

	public void setTitleParty(boolean titleParty) {
		this.titleParty = titleParty;
	}

	public String getQualityEvalLevel() {
		return this.qualityEvalLevel;
	}

	public void setQualityEvalLevel(String qualityEvalLevel) {
		this.qualityEvalLevel = qualityEvalLevel;
	}

	public String getDynamic_qualityEvalLevel() {
		return this.dynamic_qualityEvalLevel;
	}

	public void setDynamic_qualityEvalLevel(String dynamic_qualityEvalLevel) {
		this.dynamic_qualityEvalLevel = dynamic_qualityEvalLevel;
	}

	public String getTimeSensitive() {
		return this.timeSensitive;
	}

	public void setTimeSensitive(String timeSensitive) {
		this.timeSensitive = timeSensitive;
	}

	public int getImgNum() {
		return this.imgNum;
	}

	public void setImgNum(int imgNum) {
		this.imgNum = imgNum;
	}

	public int getCNwordNum() {
		return this.CNwordNum;
	}

	public void setCNwordNum(int CNwordNum) {
		this.CNwordNum = CNwordNum;
	}

	public int getENwordNum() {
		return this.ENwordNum;
	}

	public void setENwordNum(int ENwordNum) {
		this.ENwordNum = ENwordNum;
	}

	public int getBiaodianNum() {
		return this.biaodianNum;
	}

	public void setBiaodianNum(int biaodianNum) {
		this.biaodianNum = biaodianNum;
	}

	public int getSentenceNum() {
		return this.sentenceNum;
	}

	public void setSentenceNum(int sentenceNum) {
		this.sentenceNum = sentenceNum;
	}

	public int getParagraphNum() {
		return this.paragraphNum;
	}

	public void setParagraphNum(int paragraphNum) {
		this.paragraphNum = paragraphNum;
	}

	public boolean getCanbeSlide() {
		return this.canbeSlide;
	}

	public void setCanbeSlide(boolean canbeSlide) {
		this.canbeSlide = canbeSlide;
	}

	// 地域识别的结果
	@Expose
	@SerializedName("loclist")
	private ArrayList<String> loclist;

	public ArrayList<String> getLocList() {
		return this.loclist;
	}

	public void setLocList(ArrayList<String> loclist) {
		this.loclist = loclist;
	}

	// tags解析的结果
	@Expose
	@SerializedName("tags")
	private ArrayList<String> tags;

	public ArrayList<String> getTags() {
		return this.tags;
	}

	public void setTags(ArrayList<String> tagsList) {
		// this.al_features.clear();
		this.tags = tagsList;
	}

	// 从features中提取的分类特征
	@Expose
	@SerializedName("category")
	private ArrayList<String> category;

	public ArrayList<String> getCategory() {
		return this.category;
	}

	public void setCategory(ArrayList<String> categoryList) {
		// this.al_features.clear();
		this.category = categoryList;
	}

	// 隐含主题
	@Expose
	@SerializedName("topic")
	private ArrayList<String> topic;

	public ArrayList<String> getTopic() {
		return this.topic;
	}

	public void setTopic(ArrayList<String> topicList) {
		// this.al_features.clear();
		this.topic = topicList;
	}

	// 热点事件、热点词汇
	@Expose
	@SerializedName("hotEvent")
	private ArrayList<String> hotEvent;

	public ArrayList<String> getHotEvent() {
		return this.hotEvent;
	}

	public void setHotEvent(ArrayList<String> hotEventList) {
		// this.al_features.clear();
		this.hotEvent = hotEventList;
	}

	// 特征表达：三个一组顺序构成数组，也即feature1 type weight feature2 type weight...
	// 按一级分类、二级分类、栏目专题事件、topic顺序排列
	@Expose
	@SerializedName("features")
	private ArrayList<String> al_features;
	@Expose
	@SerializedName("features2")
	private ArrayList<String> al2_features;

	public itemf() {
		al_features = new ArrayList<String>();
		al2_features = new ArrayList<String>();
		tags = new ArrayList<String>();
		category = new ArrayList<String>();
		hotEvent = new ArrayList<String>();
	}

	public ArrayList<String> getFeatures() {
		return this.al_features;
	}

	/**
	 * 加入一个feature表达到itemf；
	 * 
	 * 注意：
	 * 
	 * @param feature
	 * @param type
	 *            feature的类型:"c" "sc" "cn" "t"
	 *            "s",分布对应：一级分类、二级分类、栏目专题事件、topic、稿源source
	 * @param weight
	 *            feature在item中的权重
	 * 
	 */
	public void addFeatures(String feature, String type, String weight) {
		this.al_features.add(feature);
		this.al_features.add(type);
		this.al_features.add(weight);
	}

	public void addFeatures(String s) {
		this.al_features.add(s);
	}

	public void addFeatures(ArrayList<String> tagList) {
		this.al_features.clear();
		this.al_features.addAll(tagList);
	}

	public void setFeatures(ArrayList<String> featureList) {
		this.al_features = featureList;
	}

	public void addFeatures2(String feature, String type, String weight) {
		this.al2_features.add(feature);
		this.al2_features.add(type);
		this.al2_features.add(weight);
	}

	public void addFeatures2(String s) {
		this.al2_features.add(s);
	}

	public void addFeatures2(ArrayList<String> tagList) {
		this.al2_features.clear();
		this.al2_features.addAll(tagList);
	}

	public void setFeatures2(ArrayList<String> featureList) {
		this.al2_features = featureList;
	}

	public ArrayList<String> getFeatures2() {
		return this.al2_features;
	}

	public String getSpecialParam() {
		return this.specialParam;
	}

	public void setSpecialParam(String specialParam) {
		this.specialParam = specialParam;
	}

	public String getThumbnailpic() {
		return this.thumbnailpic;
	}

	public void setThumbnailpic(String thumbnailpic) {
		this.thumbnailpic = thumbnailpic;
	}

	public String getPicFingerprint() {
		return this.picFingerprint;
	}

	public void setPicFingerprint(String picFingerprint) {
		this.picFingerprint = picFingerprint;
	}

	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getReadableFeatures() {
		return this.readableFeatures;
	}

	public void setReadableFeatures(String readableFeatures) {
		this.readableFeatures = readableFeatures;
	}

	public String getAuthLabel() {
		return this.authLabel;
	}

	public void setAuthLabel(String authLabel) {
		this.authLabel = authLabel;
	}

	public String getMultihead() {
		return this.multihead;
	}

	public void setMultihead(String multihead) {
		this.multihead = multihead;
	}

	public String getRelatedfeatures() {
		return this.relatedfeatures;
	}

	public void setRelatedfeatures(String relatedfeatures) {
		this.relatedfeatures = relatedfeatures;
	}

	public String getTopic1() {
		return this.topic1;
	}

	public void setTopic1(String topic1) {
		this.topic1 = topic1;
	}

	public String getTopic2() {
		return this.topic2;
	}

	public void setTopic2(String topic2) {
		this.topic2 = topic2;
	}

	public String getTopic3() {
		return this.topic3;
	}

	public void setTopic3(String topic3) {
		this.topic3 = topic3;
	}

}
