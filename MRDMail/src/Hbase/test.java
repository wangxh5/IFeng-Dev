package Hbase;

import dataBase.itemf;

public class test {
	public static void main(String[] args){
		HbaseOperation hbaseOperation=new HbaseOperation();
		itemf item = hbaseOperation.get("cmpp_33370777");
		System.out.println(item.getDocType());
	}
}
