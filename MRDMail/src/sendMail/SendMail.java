package sendMail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import net.sf.json.JSONObject;
import utils.LoadConfig;
import utils.MailSenderWithRotam;

public class SendMail {
	private static final String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
	String lineColor = "3AB2A6";
	String titleColor = "FFAC86";
	String lieColor = "FFCEB7";
	String backColor = "D3FFFA";
	private BufferedReader br;

	public String convertLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td  class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertSandCLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td  class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertSandCTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + titleColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td style=\"background-color:#" + titleColor
						+ ";\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convert7DayLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + titleColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td  style=\"background-color:#" + titleColor
						+ ";\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	// public String convert7DayTitleLineHtml(String[] keys){
	// String result = "<tr>";
	// for(int i = 0; i < keys.length; i++)
	// {
	// if(i == 0)
	// result = result + "<td style=\"background-color:#"+titleColor+";\"
	// height=\"20\" class=\"xl65\" width=\"90\"><span>"+keys[i]+"</span>
	// </td>";
	// else
	// result = result + "</td><td style=\"background-color:#"+titleColor+";\"
	// class=\"xl65\" width=\"90\"><span>"+keys[i]+"</span> </td>";
	// }
	// result = result + "</tr>";
	// return result;
	// }
	public String convertSourceLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td  class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertSourceTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + titleColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td style=\"background-color:#" + titleColor
						+ ";\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertall7DayTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + titleColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td style=\"background-color:#" + titleColor
						+ ";\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertCateTitleLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td  style=\"background-color:#" + titleColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else
				result = result + "</td><td style=\"background-color:#" + titleColor
						+ ";\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertCateLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td  style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else if ((i == keys.length - 1) && Integer.valueOf(keys[i]) < 100)
				result = result + "</td><td class=\"xl65\" width=\"90\"><span style=\"color:#E53333\"><strong>"
						+ keys[i] + "</strong></span> </td>";
			else
				result = result + "</td><td class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertSourceLastLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else if ((i == keys.length - 2) && Integer.valueOf(keys[i]) < 20000 && keys[0].equals("合计"))
				result = result + "</td><td class=\"xl65\" width=\"90\"><span style=\"color:#E53333\"><strong>"
						+ keys[i] + "</strong></span> </td>";
			else
				result = result + "</td><td class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertLastLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else if ((i == keys.length - 1) && Integer.valueOf(keys[i]) < 20000 && keys[0].equals("合计"))
				result = result + "</td><td class=\"xl65\" width=\"90\"><span style=\"color:#E53333\"><strong>"
						+ keys[i] + "</strong></span> </td>";
			else
				result = result + "</td><td class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertLastSandCLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
			else if ((i == keys.length - 1) && Integer.valueOf(keys[i]) < 20000 && keys[0].equals("合计"))
				result = result + "</td><td class=\"xl65\" width=\"90\"><span style=\"color:#E53333\"><strong>"
						+ keys[i] + "</strong></span> </td>";
			else
				result = result + "</td><td class=\"xl65\" width=\"90\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertAll7DaysLineHtml(String[] keys) {
		String result = "<tr>";
		for (int i = 0; i < keys.length; i++) {
			if (i == 0)
				result = result + "<td style=\"background-color:#" + lieColor
						+ ";\" height=\"20\" class=\"xl65\" width=\"120\"><span>" + keys[i] + "</span> </td>";
			else if ((i == keys.length - 3) && Integer.valueOf(keys[i]) < 20000)
				result = result + "</td><td class=\"xl65\" width=\"120\"><span style=\"color:#E53333\"><strong>"
						+ keys[i] + "</strong></span> </td>";
			else
				result = result + "</td><td class=\"xl65\" width=\"120\"><span>" + keys[i] + "</span> </td>";
		}
		result = result + "</tr>";
		return result;
	}

	public String convertAll7DayHtml(String filename) throws IOException {
		FileReader fr = null;
		fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "";
		int width = 0;
		String[] keys = line.split("\t");
		width = keys.length * 120;
		result = result + "<table style=\"background-color:#" + backColor + ";\" border=\"1\" bordercolor=\"#"
				+ lineColor + "\" cellpadding=\"0\" cellspacing=\"1\" width=\"" + width
				+ "\" class=\"ke-zeroborder\"><tbody>";
		result = result + convertall7DayTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			result = result + convertAll7DaysLineHtml(lineList.get(i).split("\t"));
		}

		br.close();
		fr.close();
		result = result + "</tbody></table></p>";
		return result;
	}

	public String convertSourceHtml(String filename) throws IOException {
		FileReader fr = null;
		fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "<p>";
		int width = 0;
		String[] keys = line.split("\t");
		width = keys.length * 90;
		result = result + "<table style=\"background-color:#" + backColor + ";\" border=\"1\" bordercolor=\"#"
				+ lineColor + "\" cellpadding=\"0\" cellspacing=\"0\" width=\"" + width
				+ "\" class=\"ke-zeroborder\"><tbody>";
		result = result + convertSourceTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			if (i == lineList.size() - 1)
				result = result + convertSourceLastLineHtml(lineList.get(i).split("\t"));
			else
				result = result + convertSourceLineHtml(lineList.get(i).split("\t"));
		}

		br.close();
		fr.close();
		result = result + "</tbody></table></p>";
		return result;
	}

	public String convertSandCHtml(String filename) throws IOException {
		FileReader fr = null;
		fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "<p>";
		int width = 0;
		String[] keys = line.split("\t");
		width = keys.length * 90;
		result = result + "<table style=\"background-color:#" + backColor + ";\" border=\"1\" bordercolor=\"#"
				+ lineColor + "\" cellpadding=\"0\" cellspacing=\"0\" width=\"" + width
				+ "\" class=\"ke-zeroborder\"><tbody>";
		result = result + convertSandCTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			if (i == lineList.size() - 1)
				result = result + convertLastLineHtml(lineList.get(i).split("\t"));
			else
				result = result + convertSandCLineHtml(lineList.get(i).split("\t"));
		}

		br.close();
		fr.close();
		result = result + "</tbody></table></p>";
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

	public String convertVideoSourceHtml(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "";
		String[] keys = line.split("\t");
		result = result + "<table style=\"background-color:#" + backColor + ";\"  border=\"1\" bordercolor=\"#"
				+ lineColor + "\" class=\"table\"><tbody>";
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

	public String convert7DayHtml(String filename) throws IOException {
		FileReader fr = null;
		fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "<p>";
		int width = 0;
		String[] keys = line.split("\t");
		width = keys.length * 90;
		result = result + "<table style=\"background-color:#" + backColor + ";\" border=\"1\" bordercolor=\"#"
				+ lineColor + "\" cellpadding=\"0\" cellspacing=\"0\" width=\"" + width
				+ "\" class=\"ke-zeroborder\"><tbody>";
		result = result + convert7DayLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			if (i % 9 == 0 || (i + 1) % 9 == 0)
				result = result + convert7DayLineHtml(lineList.get(i).split("\t"));
			// else if(i == lineList.size() - 1)
			// result = result +
			// convertLastLineHtml(lineList.get(i).split("\t"));
			else
				result = result + convertLineHtml(lineList.get(i).split("\t"));
		}

		br.close();
		fr.close();
		result = result + "</tbody></table></p>";
		return result;
	}

	public String convertCateHtml(String filename) throws IOException {
		FileReader fr = null;
		fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String result = "<p>";
		int width = 0;
		String[] keys = line.split("\t");
		width = keys.length * 90;
		result = result + "<table style=\"background-color:#" + backColor + ";\" border=\"1\" bordercolor=\"#"
				+ lineColor + "\" cellpadding=\"0\" cellspacing=\"0\" width=\"" + width
				+ "\" class=\"ke-zeroborder\"><tbody>";
		result = result + convertCateTitleLineHtml(keys);
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			if (i == lineList.size() - 1)
				result = result + convertLastLineHtml(lineList.get(i).split("\t"));
			else
				result = result + convertCateLineHtml(lineList.get(i).split("\t"));
		}
		br.close();
		fr.close();
		result = result + "</tbody></table></p>";
		return result;
	}

	// public String convertHtml(String filename) throws IOException{
	// FileReader fr = null;
	// fr = new FileReader(filename);
	// BufferedReader br = new BufferedReader(fr);
	// String line = br.readLine();
	// String result = "<p>";
	// int width = 0;
	// String[] keys = line.split("\t");
	// width = keys.length * 90;
	// result = result + "<table border=\"1\" cellpadding=\"0\"
	// cellspacing=\"0\" width=\""+width+"\" class=\"ke-zeroborder\"><tbody>";
	// result = result + convertLineHtml(keys);
	// while((line = br.readLine())!=null){
	// result = result + convertLineHtml(line.split("\t"));
	// }
	// br.close();
	// fr.close();
	// result = result + "</tbody></table></p>";
	// return result;
	// }
	public void post(String endDate) throws IOException {
		FileReader fr = null;
		fr = new FileReader(defaultPath + endDate + "_postContent.txt");
		br = new BufferedReader(fr);
		String s = null;
		String sentContent = "";
		while ((s = br.readLine()) != null) {
			sentContent = sentContent + s;
		}
		String mail_subject = "MRD移动研发中心|内容入库报表|" + endDate;
		String mail_content = sentContent;
		String mail_receiver_config_name = "DataStatisticsReceivers";
		FileWriter fw = new FileWriter(defaultPath + endDate + "_postContent.txt");

		fw.write(mail_content);
		fw.flush();
		fw.close();
		MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject, mail_content, mail_receiver_config_name);
		boolean mailSendState = mswr.sendEmailWithRotam();
		if (mailSendState) {
			System.out.println("Mail Send State: Success");
		} else {
			System.out.println("Mail Send State: Failed");
		}
	}

	public void process(String endDate) throws IOException {
		String sentContent = "";
		sentContent = sentContent
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><p><span><span style=\"line-height:normal;\"><strong>注意:由于cmpp系统限制,统计时间为前日23点起的24小时内数据.</strong></span></span></p>";
		sentContent = sentContent
				+ "<p><span><span style=\"line-height:normal;\"><strong>标红内容为KPI相关数据,目前包括投放量和分类不足100条的数据.</strong></span></span></p>";
		sentContent = sentContent
				+ "<p><span><span style=\"line-height:normal;\"><strong>总量7天统计表</strong></span></span></p>";
		sentContent = sentContent + convertAll7DayHtml(defaultPath + endDate + "_all7days.txt");
		sentContent = sentContent
				+ "<p><span><span style=\"line-height:normal;\"><strong>来源统计表</strong></span></span></p>";
		sentContent = sentContent + convertSourceHtml(defaultPath + endDate + "_source.txt");
		sentContent = sentContent
				+ "<p><span><span style=\"line-height:normal;\"><strong>来源7天统计表</strong></span></span></p>";
		sentContent = sentContent + convert7DayHtml(defaultPath + endDate + "_7days.txt");
		sentContent = sentContent
				+ "<p><span><span style=\"line-height:normal;\"><strong>分类统计表</strong></span></span></p>";
		sentContent = sentContent + convertCateHtml(defaultPath + endDate + "_category.txt");
		sentContent = sentContent
				+ "<p><span><span style=\"line-height:normal;\"><strong>分类详细统计表</strong></span></span></p>";
		sentContent = sentContent + convertSandCHtml(defaultPath + endDate + "_sandc.txt");
		
		// System.out.println(sentContent);
		String mail_subject = "MRD移动研发中心|内容入库报表|" + endDate;
		String mail_content = sentContent;
		String mail_receiver_config_name = "DataStatisticsReceivers";
		final String mail_receiver_list = LoadConfig.lookUpValueByKey(mail_receiver_config_name);

		FileWriter fw = new FileWriter(defaultPath + endDate + "_postContent.txt");
		fw.write(mail_content);
		fw.flush();
		fw.close();
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
	}

	public static void main(String[] args) throws IOException {
		SendMail send = new SendMail();
		send.process("20171016");
		System.out.println("end");
		System.exit(0);
	}
}
