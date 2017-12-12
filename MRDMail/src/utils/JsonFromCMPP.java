package utils;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * 
 * <PRE>
 * 作用 : 
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
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class JsonFromCMPP implements Serializable
{
	private static final long serialVersionUID = 1050128890144400614L;  
	@Expose
	@SerializedName("id")
	protected String id;			//唯一标识
	@Expose
	@SerializedName("title")
	protected String title;			//标题
	@Expose
	@SerializedName("subTitle")
	protected String subTitle;		//副标题
	@Expose
	@SerializedName("sourceAlias")
	protected String sourceAlias;		//稿源
	@Expose
	@SerializedName("sourceLink")
	protected String sourceLink;		//url
	@Expose
	@SerializedName("keywords")
	private String keywords;		//关键词
	@Expose
	@SerializedName("author")
	private String author;			//作者
	@Expose
	@SerializedName("other")
	protected String other;			//其他
	@Expose
	@SerializedName("editorOther")
	protected String editorOther;			//泛编系统其他
	@Expose
	@SerializedName("description")
	protected String description;		//描述
	@Expose
	@SerializedName("summary")
	protected String summary;			//摘要
	@Expose
	@SerializedName("publishedTime")
	protected String publishedTime;	//发布时间
	@Expose
	@SerializedName("content")
	protected String content;			//内容
	@Expose
	@SerializedName("type")
	protected String type;			//文章类型 slide
	@Expose
	@SerializedName("showStyle")
	protected String showStyle;			//文章显示类型
	
	@Expose
	@SerializedName("rank")
	protected String rank;			//文章等级
	
	@Expose
	@SerializedName("life")
	protected float life;			//文章生存周期 float 类型
	@Expose
	@SerializedName("startTime")
	protected String startTime;			//查询起始时间
	@Expose
	@SerializedName("endTime")
	protected String endTime;			//查询终止时间
	public String getId()
	{
		return this.id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getTitle()
	{
		return this.title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getSubTitle()
	{
		return this.subTitle;
	}
	public void setSubTitle(String subTitle)
	{
		this.subTitle = subTitle;
	}
	
	public String getSourceAlias()
	{
		return this.sourceAlias;
	}
	public void setSourceAlias(String sourceAlias)
	{
		this.sourceAlias = sourceAlias;
	}
	
	public String getSourceLink()
	{
		return this.sourceLink;
	}
	public void setSourceLink(String sourceLink)
	{
		this.sourceLink = sourceLink;
	}
	
	public String getKeywords()
	{
		return this.keywords;
	}
	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
	}
	
	public String getAuthor()
	{
		return this.author;
	}
	public void setAuthor(String author)
	{
		this.author = author;
	}
	
	public String getOther()
	{
		return this.other;
	}
	public void setOther(String other)
	{
		this.other = other;
	}
	
	public String getEditorOther()
	{
		return this.editorOther;
	}
	public void setEditorOther(String editorOther)
	{
		this.editorOther = editorOther;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getSummary()
	{
		return this.summary;
	}
	public void setSummary(String summary)
	{
		this.summary = summary;
	}

	public String getPublishedTime()
	{
		return this.publishedTime;
	}
	public void setPublishedTime(String publishedTime)
	{
		this.publishedTime = publishedTime;
	}

	public String getContent()
	{
		return this.content;
	}
	public void setContent(String content)
	{
		this.content = content;
	}
	
	public String getType()
	{
		return this.type;
	}
	public void setType(String type)
	{
		this.type = type;
	}

	public String getRank()
	{
		return this.rank;
	}
	public void setRank(String rank)
	{
		this.rank = rank;
	}
	
	public float getLife()
	{
		return this.life;
	}
	public void setLife(float life)
	{
		this.life = life;
	}
	
	public String getShowStyle()
	{
		return this.showStyle;
	}
	public void setShowStyle(String showStyle)
	{
		this.showStyle = showStyle;
	}
	
	public String getStartTime()
	{
		return this.startTime;
	}
	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}
	
	public String getEndTime()
	{
		return this.endTime;
	}
	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}
}
