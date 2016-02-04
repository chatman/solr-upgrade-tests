package org.apache.solr.tests.solrupgradetests;

import java.io.File;

import org.apache.solr.tests.solrupgradetests.Util.MessageType;

public class SolrRollingUpgradeTests {

	private static String WORK_DIRECTORY = System.getProperty("user.dir");

	private static String DNAME = "SOLRUpdateTests";

	public static String BASE_DIR = WORK_DIRECTORY + File.separator + DNAME + File.separator;

	public static String TEMP_DIR = BASE_DIR + "temp" + File.separator;

	public void init() {

		try {
			File baseDir = new File(BASE_DIR);
			Util.postMessage("** Checking if base directory exists ...", MessageType.ACTION, true);
			if (!baseDir.exists()) {
				Util.postMessage("Base directory does not exist, creating one ...", MessageType.ACTION, true);
				baseDir.mkdir();
			}
		} catch (Exception e) {
			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
		}

		try {

			Util.postMessage("** Checking if temp directory exists ...", MessageType.ACTION, true);
			File tempDir = new File(TEMP_DIR);
			if (!tempDir.exists()) {
				Util.postMessage("Temp directory does not exist Creating Temp directory ...", MessageType.ACTION, true);
			}

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public void test() {
		
		ZookeeperNode z = new ZookeeperNode();
		z.start();
		z.stop();
		
	}
	
	public static void main(String args[]) {

		SolrRollingUpgradeTests s = new SolrRollingUpgradeTests();
		s.init();
		s.test();
		
	}

}