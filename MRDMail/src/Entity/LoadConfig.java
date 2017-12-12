package Entity;

/**
*   
*   @Description	load config file and read config     
*   @creator         tangkun
*   @create-time     2011-8-3
*   @revision        $Id
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class LoadConfig {
	
	private static final Log LOG = LogFactory.getLog("file");
	private static Properties property = null;
	private static final LoadConfig config = new LoadConfig();
	//singleton
	private LoadConfig(){}
	
	static String configurePath = "./data/sys.properties";
	// load config 
	static
	{
		property = new Properties();
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(configurePath);
			property.load(fileStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		}
	}
	
	/**
	 * return value by key
	 * @param key
	 * @return value mapping with key
	 */
	public static String lookUpValueByKey(String key)
	{
		if(!property.containsKey(key))
		{
			try {
				throw new NoSuchFieldException(key);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.info("NoSuchFieldException: "+key,e);
			}
		}
		String value = (String) property.get(key);
		return value.trim();
	}
	
	
	/**
	 * 重新指定一个路径，加载配置
	 * @param path
	 */
	public static void reLoadPath(String path){
		property = new Properties();
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(path);
			property.load(fileStream);
		} catch (FileNotFoundException e) {
			LOG.error("error:", e);
		} catch (IOException e) {
			LOG.error("error:", e);
		}
	}
	
	public static LoadConfig getInstance()
	{
		return config;
	}
	
	public static void main(String args[])
	{
		String value = LoadConfig.lookUpValueByKey("abc");
		System.out.println(value);
	}
}
