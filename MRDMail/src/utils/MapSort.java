package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSort {
	
	public static List<Map.Entry<String, Long>> Sort(HashMap<String, Long> map) {
		List<Map.Entry<String, Long>> mappingList = null;
		mappingList = new ArrayList<Map.Entry<String, Long>>(map.entrySet());

		Collections.sort(mappingList, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Map.Entry<String, Long> mapping1, Map.Entry<String, Long> mapping2) {
				return mapping1.getKey().compareTo(mapping2.getKey());
			}
		});
		return mappingList;
	}

	/**
	 * 按关键字升序排列
	 * 
	 * @param map
	 * @return
	 */
	public static List<Map.Entry<String, Integer>> mapSort3(HashMap<String, Integer> map) {
		List<Map.Entry<String, Integer>> mappingList = null;
		mappingList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

		Collections.sort(mappingList, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> mapping1, Map.Entry<String, Integer> mapping2) {
				return Integer.valueOf(mapping1.getKey().split("_")[1])
						.compareTo(Integer.valueOf(mapping2.getKey().split("_")[1]));
			}
		});
		return mappingList;
	}

	/**
	 * 按key升序排列
	 * 
	 * @param map
	 * @return
	 */
	public static List<Map.Entry<String, HashMap<String, Integer>>> mapSort(
			HashMap<String, HashMap<String, Integer>> map) {
		List<Map.Entry<String, HashMap<String, Integer>>> mappingList = null;
		mappingList = new ArrayList<Map.Entry<String, HashMap<String, Integer>>>(map.entrySet());

		Collections.sort(mappingList, new Comparator<Map.Entry<String, HashMap<String, Integer>>>() {
			public int compare(Map.Entry<String, HashMap<String, Integer>> mapping1,
					Map.Entry<String, HashMap<String, Integer>> mapping2) {
				return mapping1.getKey().compareTo(mapping2.getKey());

			}
		});
		return mappingList;
	}

	/**
	 * 利用比较器给map按照value值降序排序
	 * 
	 * @param map
	 * @return
	 */
	public static List<Map.Entry<String, HashMap<String, Integer>>> mapSort2(
			HashMap<String, HashMap<String, Integer>> map) {
		List<Map.Entry<String, HashMap<String, Integer>>> mappingList = null;
		// 通过ArrayList构造函数把map.entrySet()转换成list
		mappingList = new ArrayList<Map.Entry<String, HashMap<String, Integer>>>(map.entrySet());
		// 通过比较器实现比较排序
		Collections.sort(mappingList, new Comparator<Map.Entry<String, HashMap<String, Integer>>>() {
			public int compare(Map.Entry<String, HashMap<String, Integer>> mapping1,
					Map.Entry<String, HashMap<String, Integer>> mapping2) {
				return mapping2.getValue().get("lanum").compareTo(mapping1.getValue().get("lanum"));
			}
		});
		return mappingList;
	}

	/**
	 * 按照value值降序排序
	 * 
	 * @param map
	 * @return
	 */
	public static List<Map.Entry<String, HashMap<String, Integer>>> mapSortList(
			HashMap<String, HashMap<String, Integer>> map) {
		List<Map.Entry<String, HashMap<String, Integer>>> list = null;
		list = new ArrayList<Map.Entry<String, HashMap<String, Integer>>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, HashMap<String, Integer>>>() {
			public int compare(Map.Entry<String, HashMap<String, Integer>> mapping1,
					Map.Entry<String, HashMap<String, Integer>> mapping2) {
				return mapping2.getValue().get("odnum").compareTo(mapping1.getValue().get("odnum"));
			}
		});

		return list;
	}

	/**
	 * 按照value值降序排序
	 * 
	 * @param map
	 * @return
	 */
	public static List<Map.Entry<Boolean, HashMap<String, Integer>>> sortList(
			HashMap<Boolean, HashMap<String, Integer>> map) {
		List<Map.Entry<Boolean, HashMap<String, Integer>>> list = null;
		list = new ArrayList<Map.Entry<Boolean, HashMap<String, Integer>>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Boolean, HashMap<String, Integer>>>() {
			public int compare(Map.Entry<Boolean, HashMap<String, Integer>> mapping1,
					Map.Entry<Boolean, HashMap<String, Integer>> mapping2) {
				return mapping2.getValue().get("odnum").compareTo(mapping1.getValue().get("odnum"));
			}
		});

		return list;
	}

}
