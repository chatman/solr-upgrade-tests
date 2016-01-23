package org.apache.solr.tests.upgrade.interfaces;

public interface Directory {
	
	public static final String URL_BASE = "http://archive.apache.org/dist/lucene/solr/";
	
	public static final String ZOO_URL_BASE = "http://www.us.apache.org/dist/zookeeper/";
	
	public static final String DRIVE = "C:";
	
	public static final String DNAME = "gooseberry";
	
	public static final String BASE_DIR = DRIVE + "/" + DNAME + "/";
	
	public static final String TEMP_DIR = BASE_DIR + "temp/";
	
	public static final String NODE_ONE_DIR = BASE_DIR + "N1/";
	
	public static final String NODE_TWO_DIR = BASE_DIR + "N2/";
	
	public static final String NODE_THREE_DIR = BASE_DIR + "N3/";
	

}
