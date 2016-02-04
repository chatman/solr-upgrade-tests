package org.apache.solr.tests.solrupgradetests;

import java.io.File;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.tests.solrupgradetests.Util.MessageType;

public class SolrRollingUpgradeTests {

	private static String WORK_DIRECTORY = System.getProperty("user.dir");
	private static String DNAME = "SOLRUpdateTests";
	public static String BASE_DIR = WORK_DIRECTORY + File.separator + DNAME + File.separator;
	public static String TEMP_DIR = BASE_DIR + "temp" + File.separator;

	public static void main(String args[]) throws IOException, InterruptedException, SolrServerException {

		SolrRollingUpgradeTests s = new SolrRollingUpgradeTests();
		s.init();
		s.test();

	}

	public void init() {

		try {

			File baseDir = new File(BASE_DIR);
			Util.postMessage("** Checking if base directory exists ...", MessageType.ACTION, true);
			if (!baseDir.exists()) {
				Util.postMessage("Base directory does not exist, creating one ...", MessageType.ACTION, true);
				baseDir.mkdir();
			}

			Util.postMessage("** Checking if temp directory exists ...", MessageType.ACTION, true);
			File tempDir = new File(TEMP_DIR);
			if (!tempDir.exists()) {
				Util.postMessage("Temp directory does not exist Creating Temp directory ...", MessageType.ACTION, true);
			}

		} catch (Exception e) {
			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
		}

	}

	public void test() throws IOException, InterruptedException, SolrServerException {

		ZookeeperNode z = new ZookeeperNode();
		z.start();
		SolrNode s1 = new SolrNode("5.4.0", z.getZookeeperIp(), z.getZookeeperPort());
		SolrNode s2 = new SolrNode("5.4.0", z.getZookeeperIp(), z.getZookeeperPort());
		SolrNode s3 = new SolrNode("5.4.0", z.getZookeeperIp(), z.getZookeeperPort());

		s1.start();
		s2.start();
		s3.start();
		s1.createCollection("TestCollection","2","3");

		z.postData("TestCollection");
		z.verifyData("TestCollection");
		z.deleteData("TestCollection");

		s1.stop();
		s2.stop();
		s3.stop();

		z.stop();
		z.clean();

		Util.deleteDirectory(s1.getNodeDirectory());
		Util.deleteDirectory(s1.getNodeDirectory());
		Util.deleteDirectory(s3.getNodeDirectory());
	}

}