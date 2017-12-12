package knowledgeGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.internal.bind.TypeAdapter.Factory;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

import utils.LoadConfig;

/**
 * 
 * <PRE>
 * 作用 : 
 *    为OrientGraph提供一个连接池：没有必要每次操作都连接一次图数据库，只需从factory中取出一个实例，
 *    操作结束，释放该实例到factory中
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
 *          1.0          2016年4月28日        houyx         create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class GraphFactory {
	private static String GraphDBurl = LoadConfig.lookUpValueByKey("GraphDBUrl");
	// private OrientGraph graph=new OrientGraph(GraphDBurl,"admin","admin");
	// private static classifyGraph instance = null;
	private static OrientGraphFactory factory = new OrientGraphFactory(GraphDBurl, "root", "root").setupPool(50, 3000);
	private static GraphFactory instance = new GraphFactory();

	public static GraphFactory getInstance() {
		// TODO Auto-generated method stub
		return instance;
	}

	public OrientGraphFactory getFactory() {
		Map<String, Object> configMap = new HashMap<String, Object>();

		configMap.put("client.channel.minPool", 50);
		configMap.put("client.channel.maxPool", 1000);
		configMap.put("profiler.enabled", false);
		configMap.put("cache.local.enabled", true);

		OGlobalConfiguration.setConfiguration(configMap);
		// OGlobalConfiguration.WAL_LOCATION.setValue(false);
		OGlobalConfiguration.USE_WAL.setValue(false);

		return factory;
	}

	public void close() {
		factory.close();
	}
}
