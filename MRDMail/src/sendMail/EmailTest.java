package sendMail;
import java.io.IOException;


public class EmailTest {
	
    public static void main(String[] args) throws IOException {
    	SendDailyMail sdm = new SendDailyMail();
		sdm.process(args[0]);

		SendLocationMail sendLocationMail = new SendLocationMail();
		sendLocationMail.process(args[0]);

		SendSourceMail sendSourceMail = new SendSourceMail();
		sendSourceMail.process(args[0]);

		SendMail send = new SendMail();
		send.process(args[0]);

		System.out.println("end");
		System.exit(0);
    }
}