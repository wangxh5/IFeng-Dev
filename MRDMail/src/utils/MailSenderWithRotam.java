package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <PRE>
 * 作用 : 
 *   rotam服务化平台邮件发送
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   如有格式需求, 邮件内容需要以html格式编写
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年12月4日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class MailSenderWithRotam {
	private static Logger logger = LoggerFactory.getLogger(MailSenderWithRotam.class);

	/**
	 * @Fields ROTAM_MAIL_SERVICE_URL : rotam邮件服务平台url
	 */
	private final static String ROTAM_MAIL_SERVICE_URL = "http://10.32.24.233:5001/rotdam";
	// private final static String ROTAM_MAIL_SERVICE_URL =
	// "http://rtd.ifeng.com/rotdam";

	/**
	 * @Fields mail_subject : 邮件主题
	 */
	private String mail_subject;

	/**
	 * @Fields mail_content : 邮件内容
	 */
	private String mail_content;

	/**
	 * @Fields mail_receiver_list : 收件人列表
	 */
	private String mail_receiver_list;

	/**
	 * @Fields mail_receiver_config_name : 从配置文件中获取收件人列表
	 */
	private String mail_receiver_config_name;

	/**
	 * <p>
	 * Title: 构造方法
	 * </p>
	 * <p>
	 * Description: 类初始化
	 * </p>
	 * 
	 * @author liu_yi
	 * @param mail_subject
	 *            邮件主题
	 * @param mail_content
	 *            邮件内容
	 * @param mail_receiver_config_name
	 *            收件人列表配置文件key
	 */
	public MailSenderWithRotam(String mail_subject, String mail_content, String mail_receiver_config_name) {
		this.setMail_subject(mail_subject);
		this.setMail_content(mail_content);
		this.setMail_receiver_config_name(mail_receiver_config_name);

		// 收件人列表以逗号分隔，需要编码
		this.mail_receiver_list = LoadConfig.lookUpValueByKey(this.mail_receiver_config_name);
		// try {
		// this.mail_receiver_list =
		// URLEncoder.encode(LoadConfig.lookUpValueByKey(this.mail_receiver_config_name),
		// "utf-8");
		// } catch (UnsupportedEncodingException e) {
		// logger.error("URLEncoder Error:", e);
		// this.mail_receiver_list = null;
		// }
	}

	/**
	 * @Title: sendEmailWithRotam @Description: 发送邮件方法 @author liu_yi @return
	 *         邮件发送状态，success返回true, failed返回false @throws
	 */
	public boolean sendEmailWithRotam() {
		boolean mailSendState = true;
		String mailParam = this.getPostParam();
		if (null == mailParam) {
			logger.error("mailParam null");
			mailSendState = false;
			return mailSendState;
		}
		// System.out.println(mailParam);
		if (null != mail_subject && null != mail_content) {
			String postResult = this.sendPost(ROTAM_MAIL_SERVICE_URL, mailParam);
			if (postResult.indexOf("failed") >= 0) {
				mailSendState = false;
			}
		} else {
			logger.error("mail title or content null");
			mailSendState = false;
		}

		return mailSendState;
	}

	/**
	 * @Title: getPostParam @Description: 拼接post方法参数 @author
	 *         liu_yi @return @throws
	 */
	public String getPostParam() {
		if (null == this.mail_receiver_list) {
			return null;
		}

		StringBuffer postParam = new StringBuffer();
		postParam.append("fn=CMSMail&args.ars=");
		postParam.append(this.mail_receiver_list);
		postParam.append("&args.txt=");
		postParam.append(this.mail_content);
		postParam.append("&args.sub=");
		postParam.append(this.mail_subject);
		// postParam.append("&args.user=cmppesm");
		return postParam.toString();
	}

	/**
	 * 执行一个HTTP POST请求，返回请求响应的HTML
	 * 
	 * @param url
	 *            请求的URL地址
	 * @param params
	 *            请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 */
	public String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuffer sbRes = new StringBuffer();
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			// conn.setRequestProperty("Content-type", "text/html");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(3000);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();

			if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					logger.error("post failed ErrorCode=" + conn.getResponseCode());
				}
			}

			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				sbRes.append(line).append("\r\n");
			}
		} catch (Exception e) {
			logger.error("post failed:", e);
			sbRes.append("post failed");
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error("connect closed failed:", e);
				;
			}
		}

		return sbRes.toString();
	}

	public String getMail_subject() {
		return mail_subject;
	}

	public void setMail_subject(String mail_subject) {
		this.mail_subject = mail_subject;
	}

	public String getMail_content() {
		return mail_content;
	}

	public void setMail_content(String mail_content) {
		this.mail_content = mail_content;
	}

	public String getMail_receiver_list() {
		return mail_receiver_list;
	}

	public void setMail_receiver_list(String mail_receiver_list) {
		this.mail_receiver_list = mail_receiver_list;
	}

	public String getMail_receiver_config_name() {
		return mail_receiver_config_name;
	}

	public void setMail_receiver_config_name(String mail_receiver_config_name) {
		this.mail_receiver_config_name = mail_receiver_config_name;
	}

	// test code
	public static void main(String[] args) throws IOException {
		String mail_subject = "subject_test";
		// FileReader fr = new
		// FileReader("E:/Test/20171015_sentLocationContent.txt");
		// BufferedReader br = new BufferedReader(fr);
		// String mail_content = br.readLine();
		String mail_content = "23333";
		String mail_receiver_config_name = "DataStatisticsReceivers";
		MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject, mail_content, mail_receiver_config_name);
		boolean mailSendState = mswr.sendEmailWithRotam();
		if (mailSendState) {
			System.out.println("Mail Send State: Success");
		} else {
			System.out.println("Mail Send State: Failed");
		}
	}

}
