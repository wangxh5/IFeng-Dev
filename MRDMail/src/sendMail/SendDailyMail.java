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

public class SendDailyMail {
	private static Logger logger = Logger.getLogger(SendDailyMail.class);
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");

	String backColor = "D3FFFA";
	String borderColor = "3AB2A6";
	String titleColor = "FFAC86";

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

	public String convertSourceRateLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			result = result + "<td height=\"20\" width=\"90\">" + keys[i] + "</td>";
		}
		result = result + "</tr>";
		return result;

	}

	// 文章用户访问量
	public String convertuvHtml(String filename) throws IOException {
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

	// 文章类型
	public String convertDocTypeHtml(String filename) throws IOException {
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

	// 标题党
	public String convertTitlePartyHtml(String filename) throws IOException {
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

	// 原创
	public String convertisCreationHtml(String filename) throws IOException {
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

	// 低俗文章
	public String convertPornHtml(String filename) throws IOException {
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

	// 推荐类型
	public String convertRecommendCateHtml(String filename) throws IOException {
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

	// 时效性
	public String convertTimeSensitiveHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "<p>";
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

	// 质量评级得分
	public String convertqualityEvalLevelHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "<p>";
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

	public void process(String endDate) throws IOException {
		String sentContent = "";
		sentContent = sentContent
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><p><span style=\"line-height:normal;\"><strong>注意:由于cmpp系统限制,统计时间为前日23点起的24小时内数据.</strong></span></p>";
		sentContent = sentContent
				+ "<p><span style=\"line-height:normal;\"><strong>标红内容为KPI相关数据,目前包括投放量和分类不足100条的数据.</strong></span></p>";

		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>文章类型统计表</strong></span></p>";
		sentContent = sentContent + convertDocTypeHtml(defaultPath + endDate + "_docType.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>标题党统计表</strong></span></p>";
		sentContent = sentContent + convertTitlePartyHtml(defaultPath + endDate + "_titleParty.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>低俗文章统计表</strong></span></p>";
		sentContent = sentContent + convertPornHtml(defaultPath + endDate + "_porn.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>原创统计表</strong></span></p>";
		sentContent = sentContent + convertisCreationHtml(defaultPath + endDate + "_isCreation.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>推荐类型统计表</strong></span></p>";
		sentContent = sentContent + convertRecommendCateHtml(defaultPath + endDate + "_recommendCate.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>时效性统计表</strong></span></p>";
		sentContent = sentContent + convertTimeSensitiveHtml(defaultPath + endDate + "_timeSensitive.txt");
		sentContent = sentContent + "<p><span style=\"line-height:normal;\"><strong>质量评级得分统计表</strong></span></p>";
		sentContent = sentContent + convertqualityEvalLevelHtml(defaultPath + endDate + "_qualityEvalLevel.txt");

		// sentContent = sentContent + "<p><span
		// style=\"line-height:normal;\"><strong>文章访问量统计表</strong></span></p>";
		// sentContent = sentContent + convertuvHtml(defaultPath + endDate +
		// "_uv.txt");
		// sentContent = sentContent + "<p><span
		// style=\"line-height:normal;\"><strong>文章收藏量统计表</strong></span></p>";
		// sentContent = sentContent + convertuvHtml(defaultPath + endDate +
		// "_storeNum.txt");
		// sentContent = sentContent + "<p><span
		// style=\"line-height:normal;\"><strong>文章分享量统计表</strong></span></p>";
		// sentContent = sentContent + convertuvHtml(defaultPath + endDate +
		// "_shareNum.txt");

		String mail_subject = "MRD移动研发中心|内容入库报表|其他信息统计|" + endDate;
		String mail_content = sentContent;
		String mail_receiver_config_name = "DataStatisticsReceivers2";
		final String mail_receiver_list = LoadConfig.lookUpValueByKey(mail_receiver_config_name);

		logger.info("start");
		FileWriter fw = new FileWriter(defaultPath + endDate + "_sentMailContent.txt");
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
		logger.info("end");

	}

	public static void main(String[] args) throws IOException, ParseException {
		// E:/Test/
		// 生成相应的统计文件保存在本地，例如 20170720_location.txt
		// CalVarDailyData cal = new CalVarDailyData();
		// String startDate = CalTools.getDate(System.currentTimeMillis() - 4 *
		// 24 * 60 * 60 * 1000);
		// String endDate = CalTools.getDate(System.currentTimeMillis() - 24 *
		// 60 * 60 * 1000);
		// cal.calRecommendNum(endDate);
		// cal.process(startDate, endDate);
		// cal.statisticsLocation();
		// cal.output(endDate);

		// 邮件发送 稿源统计表
		// SendSourceMail ssm = new SendSourceMail();
		// ssm.process(endDate);

		// 邮件发送 其他信息统计表
		// String startDate=args[0];
		// String endDate=args[0];
		SendDailyMail sdm = new SendDailyMail();
		sdm.process(args[0]);

		// 邮件发送 地域统计表
		// SendLocationMail sendLocationMail = new SendLocationMail();
		// sendLocationMail.process(endDate);
		System.exit(0);

	}

}
