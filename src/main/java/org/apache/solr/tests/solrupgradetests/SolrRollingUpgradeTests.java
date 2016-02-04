package org.apache.solr.tests.solrupgradetests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.tests.solrupgradetests.Util.MessageType;

public class SolrRollingUpgradeTests {

	private static String WORK_DIRECTORY = System.getProperty("user.dir");
	private static String DNAME = "SOLRUpdateTests";
	public static String BASE_DIR = WORK_DIRECTORY + File.separator + DNAME + File.separator;
	public static String TEMP_DIR = BASE_DIR + "temp" + File.separator;

	public String ARG_VERSION_ONE = "-v1";
	public String ARG_VERSION_TWO = "-v2";
	public String ARG_NUM_SHARDS = "-Shards";
	public String ARG_NUM_REPLICAS = "-Replicas";
	public String ARG_NUM_NODES = "-Nodes";

	public static void main(String args[]) throws IOException, InterruptedException, SolrServerException {

		SolrRollingUpgradeTests s = new SolrRollingUpgradeTests();
		s.init();
		s.test(args);

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

	public void test(String args[]) throws IOException, InterruptedException, SolrServerException {

		Map<String, String> argM = new HashMap<String, String>();

		for (int i = 0; i < args.length; i += 2) {
			argM.put(args[i], args[i + 1]);
		}
		
		String versionOne = argM.get(ARG_VERSION_ONE);
		String versionTwo = argM.get(ARG_VERSION_TWO);
		String numNodes = argM.get(ARG_NUM_NODES);
		String numShards = argM.get(ARG_NUM_SHARDS);
		String numReplicas = argM.get(ARG_NUM_REPLICAS);
		
		List<SolrNode> nodes = new ArrayList<SolrNode>();
		boolean collectionCreated = false;
		int nodesCount = Integer.parseInt(numNodes);
		String collectionName = UUID.randomUUID().toString();

		ZookeeperNode z = new ZookeeperNode();
		z.start();
		
		for(int i = 1; i < nodesCount ; i++) {

			SolrNode node = new SolrNode(versionOne, z.getZookeeperIp(), z.getZookeeperPort());
			nodes.add(node);
			node.start();
			
			if (!collectionCreated) {
				node.createCollection(collectionName, numShards, numReplicas);
				collectionCreated = true;
			}
			
		}
		
		z.postData(collectionName);
		
		for (SolrNode node : nodes) {
			
			node.stop();
			node.upgrade(versionTwo);
			node.start();			
			if (!z.verifyData(collectionName)) {
				Util.postMessage("Inconsistencies found in data !", MessageType.RESULT_ERRROR, true);
			}
		}
		
		if (z.getLiveNodes() != nodesCount) {
			Util.postMessage("All the nodes didnt come up !", MessageType.RESULT_ERRROR, true);
		}
		
		for (SolrNode node : nodes) {

			node.stop();
			Util.deleteDirectory(node.getNodeDirectory());
			
		}

		z.stop();
		z.clean();
	}
}