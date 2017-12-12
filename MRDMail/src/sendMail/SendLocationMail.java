package sendMail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;
import utils.LoadConfig;

public class SendLocationMail {
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");

	private static Logger logger = Logger.getLogger(SendLocationMail.class);
	String backColor = "D3FFFA";
	String borderColor = "3AB2A6";
	String titleColor = "FFAC86";

	public String convertLocationLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0) {
				result = result + "<td style=\"background-color:#" + titleColor + ";\"  height=\"20\" width=\"90\">"
						+ keys[i] + "</td>";
			} else {
				result = result + "<td height=\"20\" width=\"90\">" + keys[i] + "</td>";
			}
		}
		result = result + "</tr>";
		return result;

	}

	public String convertLocationTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {

			result = result + "<td style=\"background-color:#" + titleColor + ";\"  height=\"20\" width=\"90\">"
					+ keys[i] + "</td>";
		}
		result = result + "</tr>";
		return result;

	}

	// 标题
	public String convertTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			result = result + "<td style=\"background-color:#" + titleColor + ";\"  height=\"20\" width=\"90\">"
					+ keys[i] + "</td>";
		}

		result = result + "</tr>";
		return result;

	}

	public String convertLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0 || i == 5) {
				result = result + "<td style=\"background-color:#" + titleColor + ";\"  height=\"20\" width=\"90\">"
						+ keys[i] + "</td>";
			} else {
				result = result + "<td height=\"20\" width=\"90\">" + keys[i] + "</td>";
			}
		}
		result = result + "</tr>";
		return result;

	}

	// 一级地域

	public String convertTopLocationHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "";
		String[] keys = line.split("\t");
		result = result + "<table style=\"background-color:#" + backColor + ";\"  border=\"1\" bordercolor=\"#"
				+ borderColor + "\" class=\"table\"><tbody>";
		result = result + convertTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			result = result + convertLineHtml(lineList.get(i).split("\t"));
		}
		br.close();
		fr.close();
		result = result + "</tbody></table>";
		return result;
	}

	// 地域
	public String convertLocationHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "";
		String[] keys = line.split("\t");
		result = result + "<table style=\"background-color:#" + backColor + ";\"  border=\"1\" bordercolor=\"#"
				+ borderColor + "\" class=\"table\"><tbody>";
		result = result + convertLocationTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		line = br.readLine();
		keys = line.split("\t");
		result = result + convertLocationTitleLineHtml(keys);
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			if (lineList.get(i).equals("0000")) {
				i++;
				result = result + convertLocationTitleLineHtml(lineList.get(i).split("\t"));
			} else {
				result = result + convertLocationLineHtml(lineList.get(i).split("\t"));
			}
		}

		br.close();
		fr.close();
		result = result + "</tbody></table>";
		return result;

	}

	public void process(String endDate) throws IOException {
		String sentContent = "";
		sentContent = sentContent
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><p><span style=\"line-height:normal;\"><strong>注意:由于cmpp系统限制,统计时间为前日23点起的24小时内数据.</strong></span></p>";
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>一级地域统计表</strong></span></p>";
		sentContent = sentContent + convertTopLocationHtml(defaultPath + endDate + "_topLocation.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>地域统计表</strong></span></p>";
		sentContent = sentContent + convertLocationHtml(defaultPath + endDate + "_location.txt");

		String mail_subject = "MRD移动研发中心|内容入库报表|地域统计表|" + endDate;
		String mail_content = sentContent;

		String mail_receiver_config_name = "DataStatisticsReceivers2";
		final String mail_receiver_list = LoadConfig.lookUpValueByKey(mail_receiver_config_name);

		logger.info("[INFO]start");
		FileWriter fw = new FileWriter(defaultPath + endDate + "_sentLocationContent.txt");
		fw.write(mail_content);
		fw.flush();
		fw.close();
		// MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject,
		// mail_content, mail_receiver_config_name);
		// boolean mailSendState = mswr.sendEmailWithRotam();
		// if (mailSendState) {
		// System.out.println("Mail Send State: Success");
		// } else {
		// System.out.println("Mail Send State: Failed");
		//
		// }

		String url = "http://rtd.ifeng.com/rotdam/mail/v0.0.1/send";
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost method = new HttpPost(url);
		// 接收参数json列表
		JSONObject jsonParam = new JSONObject();
		jsonParam.put("ars", mail_receiver_list);
		jsonParam.put("txt", mail_content);
		jsonParam.put("sub", mail_subject);
		StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");// 解决中文乱码问题
		entity.setContentEncoding("UTF-8");
		entity.setContentType("application/json");
		method.setEntity(entity);
		HttpResponse res = null;
		try {
			res = httpclient.execute(method);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("res:" + res);
		if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			System.out.println("success!");
		}
		logger.info("[INFO] End");
	}

	public static void main(String[] args) throws IOException, ParseException {
		SendLocationMail sendLocationMail = new SendLocationMail();
		sendLocationMail.process("20171025");
		System.exit(0);

	}

}
