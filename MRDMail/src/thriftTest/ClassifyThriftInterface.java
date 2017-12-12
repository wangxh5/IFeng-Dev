package thriftTest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ClassifyThriftInterface {
	private static final Log LOG = LogFactory.getLog("ClassifyThriftInterface");
	// class boostJson {
	// boostJson() {
	// features = new ArrayList<String>();
	// }
	//
	// ArrayList<String> features;
	//
	// public ArrayList<String> GetFeatures() {
	// return this.features;
	// }
	//
	// public void SetFeatures(ArrayList<String> features) {
	// this.features = features;
	// }
	// }

	public static List<String> getCategoryList(String id, String title, List<String> cates) {
		TTransport transport1 = new TSocket("10.90.7.47", 8382, 10000);// 线上服务端口;
		TFramedTransport transport2 = new TFramedTransport(transport1);
		TProtocol protocol = new TCompactProtocol(transport2);
		classifyModel.Client client = new classifyModel.Client(protocol);
		String strLog = "id_" + id + "#title_" + title + "#cates_" + cates.toString();
		try {
			transport1.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 建立连接
		long time = System.currentTimeMillis();
		List<String> list = new ArrayList<String>();
		try {
			// String result = client.classify_default(id, "hexl", title,
			// splitTitle, splitContent, source, featureList);
			// Gson gson = new Gson();
			// boostJson json = gson.fromJson(result, boostJson.class);
			// list.addAll(json.GetFeatures());
			list = client.getCategory(id, title, cates);
		} catch (TTransportException e) {
			e.printStackTrace();
			LOG.error("In get c list result. Connect reset.");
			LOG.error("logstashFlag#boosting#ERROR#" + strLog, e);
			// SendMessage.send("18201202621", "[警告] ConnectException in thrift
			// server [get boosting list]", null, null, null);//柳泽明
			// SendMessage.send("15910270996", "[警告] ConnectException in thrift
			// server [get boosting list]", null, null, null);
			// try {
			// sendMail.sendEmailByHttp("lixin5@ifeng",
			// "[警告]分类算法异常","ConnectException in thrift server" );
			// } catch (UnsupportedEncodingException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("logstashFlag#boosting#ERROR#" + strLog, e);
			// SendMessage.send("18201202621", "[警告] ConnectException in thrift
			// server [get boosting list]", null, null, null);//柳泽明
			// SendMessage.send("15910270996", "[警告] ConnectException in thrift
			// server [get boosting list]", null, null, null);
			// try {
			// sendMail.sendEmailByHttp("lixin5@ifeng",
			// "[警告]分类算法异常","ConnectException in thrift server" );
			// } catch (UnsupportedEncodingException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
		}
		transport1.close();
		long caltime = System.currentTimeMillis() - time;
		LOG.info("logstashFlag#classify#RESULT#" + list + "#" + strLog);
		LOG.info("logstashFlag#classify#TIME#" + caltime + "#" + strLog);
		LOG.info("[TIME] short text classify used time " + caltime);
		return list;
	}

}
