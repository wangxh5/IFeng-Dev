package Entity;


import java.util.ArrayList;
import java.util.Arrays;

public class EntityInfo {

	private String word = null; // 术语

	private int count = 0; // 热度

	private ArrayList<String> nicknameList = new ArrayList<String>(); // 别名

	private ArrayList<String> levels = new ArrayList<String>(); // 实体的级别信息

	private String category = null; // 所属类别

	private String filename = null; // 该实体所属的文件名

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<String> getNicknameList() {
		return nicknameList;
	}

	public void setNicknameList(ArrayList<String> nicknameList) {
		this.nicknameList = nicknameList;
	}

	public ArrayList<String> getLevels() {
		return levels;
	}

	public void setLevels(ArrayList<String> levels) {
		this.levels = levels;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean contains(ArrayList<String> list, String key) {
		for (String str : list) {
			if (str.equalsIgnoreCase(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object object) {
		// TODO Auto-generated method stub
		boolean isEquals = false;
		EntityInfo obj = null;
		if (object instanceof EntityInfo) {
			obj = (EntityInfo) object;
		} else {
			return isEquals;
		}
		if (this.getWord().equals(obj.getWord())
				&& this.getCategory().equals(obj.getCategory())
				&& this.getCount() == obj.getCount()
				&& this.getFilename().equals(obj.getFilename())
				&& this.getLevels().equals(obj.getLevels())
				&& this.getNicknameList().equals(obj.getNicknameList())) {
			isEquals = true;
		}
		return isEquals;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "word:" + word + "  count:" + count + "  category:" + category
				+ "  filename:" + filename.substring(filename.indexOf("entLib_")+"entLib_".length()) + "  levels:" + levels + "  nicks:"
				+ nicknameList;
	}

	public static EntityInfo redis2Object(String record, String filename) {
		String[] words = record.split("#");
		EntityInfo entityInfo = new EntityInfo();
		// System.err.println(record);
		for (String word : words) {
			if (word.startsWith("w:")) {
				String value = word.substring(word.indexOf("w:")
						+ "w:".length());
				entityInfo.setWord(value);
				continue;
			}
			if (word.startsWith("num")) {
				String value = word.substring(word.indexOf("num:")
						+ "num:".length());
				entityInfo.setCount(Integer.valueOf(value));
				continue;
			}
			if (word.startsWith("c")) {
				String value = word.substring(word.indexOf("c:")
						+ "c:".length());
				entityInfo.setCategory(value);
				continue;
			}
			if (word.startsWith("ls")) {
				String value = word.substring(word.indexOf("ls:")
						+ "ls:".length());
				value = value.substring(value.indexOf("[") + 1,
						value.indexOf("]"));
				ArrayList<String> levelList = new ArrayList<String>(
						Arrays.asList(value.split(", ")));
				entityInfo.setLevels(levelList);
				continue;
			}
			if (word.startsWith("ns")) {
				String value = word.substring(word.indexOf("ns:")
						+ "ns:".length());
				value = value.substring(value.indexOf("[") + 1,
						value.indexOf("]"));
				ArrayList<String> nickList = new ArrayList<String>(
						Arrays.asList(value.split(", ")));
				entityInfo.setNicknameList(nickList);
				continue;
			}
		}
		entityInfo.setFilename(filename);
		return entityInfo;
	}
}
