package TestDemo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import utils.LoadConfig;

public class WordToLoad {

	private static Jedis jedis;

	public static void main(String[] args) throws IOException {
		String defaultPath = LoadConfig.lookUpValueByKey("defaultPath");
		FileWriter fw = new FileWriter(defaultPath + "文化.txt", false);
		jedis = new Jedis("10.90.16.22", 6379);
		jedis.auth("Q19YT2vxc206zKB");
		jedis.select(1);
		List<String> values = new ArrayList<String>();
		values = jedis.lrange("entLib_文化", 0, -1);
		String name;
		for (String s : values) {
			System.out.println(s);
			name = s.split("w:")[1].split("#")[0];
			fw.write("文化\t" + name + "\n");

		}
		// System.out.println("w:范冰冰#num:".split("w:")[1].split("#")[0]);
		fw.flush();
		fw.close();
		
	}

}
