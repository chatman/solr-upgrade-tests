package org.apache.solr.tests.solrupgradetests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class SolrUpgradeTests extends SolrUpgradeTestsUtil {

	final static Logger logger = Logger.getLogger(SolrUpgradeTests.class);
	private SolrNode solrNode = null;
	private ZookeeperNode zookeeperNode = null;

	public SolrUpgradeTests() {
	
		nodeDirectoryMapping = new HashMap<Integer, String>();
		nodePortMapping = new HashMap<Integer, String>();

	}


	public void run(String[] args) throws Exception {

		if (args.length == 0) {
			this.postMessage("This testing program requires following parameters to run ... ", MessageType.GENERAL,
					false);
			this.postMessage("-v1 {From Version: ex '5.4.0'}; -v2 {To Version: ex '5.4.1'} ", MessageType.GENERAL,
					false);
			this.postMessage("-SkipClean: TRUE/FALSE {To clean the node directories or not}", MessageType.GENERAL,
					false);
			this.postMessage("-SkipUnzip: TRUE/FALSE {To unzip the releases or not} ", MessageType.GENERAL, false);
			this.postMessage("-N1Port/-N2Port/-N3Port: {Port number for three nodes, must be different. ex '1234'}",
					MessageType.GENERAL, false);
			this.postMessage("-WorkDirectory: {Define a working directory on your system ex '/home'}",
					MessageType.GENERAL, false);
			this.postMessage("-ZkP {zookeeper port number}", MessageType.GENERAL, false);
			return;
		}

		Map<String, String> argM = new HashMap<String, String>();

		for (int i = 0; i < args.length; i += 2) {
			argM.put(args[i], args[i + 1]);
		}

		String versionOne = argM.get(ARG_VERSION_ONE);
		String versionTwo = argM.get(ARG_VERSION_TWO);
		String rootDir = argM.get(ARG_WORK_DIR);
		String collectionName = argM.get(COLLECTION_NAME);
		String zkPort = argM.get(ARG_ZK_PORT);
		String help = argM.get(ARG_HELP);
		String verbose = argM.get(ARG_VERBOSE);
		String numNodesI = argM.get(ARG_NUM_NODES);
		String numShards = argM.get(ARG_NUM_SHARDS);
		String numReplicas = argM.get(ARG_NUM_REPLICAS);

		if (numShards != null) {
			NUM_SHARDS = numShards;
		}

		if (numReplicas != null) {
			NUM_REPLICAS = numReplicas;
		}

		if (numNodesI != null) {
			numNodes = Integer.parseInt(numNodesI);

			if (numNodes < 3) {
				numNodes = 3;
			}
		}

		for (int i = 1; i <= numNodes; i++) {
			nodeDirectoryMapping.put(i, BASE_DIR + UUID.randomUUID().toString() + File.separator);
		}

		if (verbose != null && verbose.equalsIgnoreCase("FALSE")) {
			
			solrNode = new SolrNode(false);
			zookeeperNode = new ZookeeperNode(false);
			super.isVerbose = false;

		} else if (verbose != null && verbose.equalsIgnoreCase("TRUE")) {
			
			solrNode = new SolrNode(true);
			zookeeperNode = new ZookeeperNode(true);
			super.isVerbose = true;

		}

		solrNode = new SolrNode(this.isVerbose);
		zookeeperNode = new ZookeeperNode(this.isVerbose);

		
		this.postMessage("#########################################################", MessageType.GENERAL, false);
		this.postMessage(
				"##### [SOLR UPGRADE TESTS] HOLA !!! use -Help parameter to get more details on parameters #####",
				MessageType.GENERAL, false);
		this.postMessage("Testing upgrade from " + versionOne + " To " + versionTwo, MessageType.GENERAL, true);
		this.postMessage("#########################################################", MessageType.GENERAL, false);

		if (help != null) {
			this.postMessage("This testing program requires following parameters to run ... ", MessageType.GENERAL,
					false);
			this.postMessage("-v1 {From Version: ex '5.4.0'}; -v2 {To Version: ex '5.4.1'} ", MessageType.GENERAL,
					false);
			this.postMessage("-SkipClean: TRUE/FALSE {To clean the node directories or not}", MessageType.GENERAL,
					false);
			this.postMessage("-SkipUnzip: TRUE/FALSE {To unzip the releases or not} ", MessageType.GENERAL, false);
			this.postMessage("-N1Port/-N2Port/-N3Port: {Port number for three nodes, must be different. ex '1234'}",
					MessageType.GENERAL, false);
			this.postMessage("-WorkDirectory: {Define a working directory on your system ex '/home'}",
					MessageType.GENERAL, false);
			this.postMessage("-ZkP {zookeeper port number}", MessageType.GENERAL, false);
			return;
		}

		if (zkPort != null) {
			this.zkPort = zkPort;
		}

		COLLECTION_NAME += UUID.randomUUID().toString();

		if (collectionName != null) {
			COLLECTION_NAME = collectionName;
		}

		if (versionOne == null || versionTwo == null) {
			throw new Exception("SOLRUpdateTests Says: Need two SOLR versions to conduct this test ...");
		}

		if (versionOne.equals(versionTwo)) {
			throw new Exception(
					"SOLRUpdateTests Says: Comparing same versions is not useful, please provide two versions ...");
		}

		if (rootDir != null) {
			this.WORK_DIRECTORY = rootDir;
		}

		if (this.createBaseDir()) {
			this.postMessage("Directory Successfully Created !", MessageType.RESULT_SUCCESS, true);
		}

		if (this.doActionOnNodesDir(nodeDirectoryMapping, Action.CREATE)) {
			this.postMessage("Directory Successfully Created !", MessageType.RESULT_SUCCESS, true);
		}

		if (this.createTempDir()) {
			this.postMessage("Directory Successfully Created !", MessageType.RESULT_SUCCESS, true);
		}

		zookeeperNode.createZookeeperDir();

		if (!this.checkForRelease(ZOOKEEPER_RELEASE, ReleaseType.ZOOKEEPER, Location.TEMP, Type.COMPRESSED,
				nodeDirectoryMapping, -1)) {
			try {
				this.downloadRelease(ZOOKEEPER_RELEASE, TEMP_DIR, ReleaseType.ZOOKEEPER);
				zookeeperNode.extractZookeeperRelease();
				zookeeperNode.renameZookeeperConfFile();
			} catch (IOException e) {
				this.postMessage("Internet Connection Failure OR Release not present OR BAD Release name ... [EXITING]",
						MessageType.RESULT_ERRROR, true);
				return;
			}
		}

		if (zookeeperNode.doActionOnZookeeper(Action.START) != 0) {
			this.postMessage("Zookeeper startup failed, the test cannot continue ... exiting now !",
					MessageType.RESULT_ERRROR, true);
			return;
		}

		if (!this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.EXTRACTED, nodeDirectoryMapping,
				-1)) {
			if (this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED, nodeDirectoryMapping,
					-1)) {
				this.unzipDownloadedRelease(TEMP_DIR + "solr-" + versionTwo + ".zip", TEMP_DIR);
			}
		}

		if (!this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED, nodeDirectoryMapping,
				-1)) {
			try {
				this.downloadRelease(versionTwo, TEMP_DIR, ReleaseType.SOLR);
				this.unzipDownloadedRelease(TEMP_DIR + "solr-" + versionTwo + ".zip", TEMP_DIR);
			} catch (IOException e) {
				this.postMessage("Internet Connection Failure OR Release not present OR BAD Release name ... [EXITING]",
						MessageType.RESULT_ERRROR, true);
				return;
			}
		}

		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.EXTRACTED, nodeDirectoryMapping,
				-1)) {
			if (this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED, nodeDirectoryMapping,
					-1)) {
				this.unzipDownloadedRelease(TEMP_DIR + "solr-" + versionOne + ".zip", TEMP_DIR);
			}
		}

		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED, nodeDirectoryMapping,
				-1)) {
			try {
				this.downloadRelease(versionOne, TEMP_DIR, ReleaseType.SOLR);
				this.unzipDownloadedRelease(TEMP_DIR + "solr-" + versionOne + ".zip", TEMP_DIR);
			} catch (IOException e) {
				this.postMessage("Internet Connection Failure OR Release not present OR BAD Release name ... [EXITING]",
						MessageType.RESULT_ERRROR, true);
				return;
			}
		}

		for (int i = 1; i <= numNodes; i++) {
			nodePortMapping.put(i, String.valueOf(this.getFreePort()));
		}

		for (Map.Entry<Integer, String> entry : nodeDirectoryMapping.entrySet()) {

			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODES, Type.EXTRACTED,
					nodeDirectoryMapping, entry.getKey())) {
				File node = new File(entry.getValue() + "solr-" + versionOne);
				node.mkdir();
				FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
			}

		}

		for (Map.Entry<Integer, String> entry : nodeDirectoryMapping.entrySet()) {

			int srv = solrNode.doActionOnSolrNode(entry.getKey(), versionOne, nodePortMapping.get(entry.getKey()),
					Action.START, this.zkPort, nodeDirectoryMapping);
			if (srv != 0) {
				this.postMessage("Node startup failed for node ... : " + entry.getKey(), MessageType.RESULT_ERRROR,
						true);
			}
			Thread.sleep(15000);
		}

		solrNode.createSOLRCollection(1, versionOne, COLLECTION_NAME, NUM_SHARDS, NUM_REPLICAS, nodeDirectoryMapping);
		zookeeperNode.postData(COLLECTION_NAME, this.zkPort);

		boolean isDataIntact = true;
		for (Map.Entry<Integer, String> entry : nodeDirectoryMapping.entrySet()) {

			int hasNodeStopped = solrNode.doActionOnSolrNode(entry.getKey(), versionOne,
					nodePortMapping.get(entry.getKey()), Action.STOP, this.zkPort, nodeDirectoryMapping);
			if (hasNodeStopped != 0) {
				this.postMessage("Node :" + entry.getKey() + "Node shutdown failed for some reason ... ",
						MessageType.RESULT_ERRROR, true);
			}

			Thread.sleep(15000);

			solrNode.upgradeSolr(versionOne, versionTwo, entry.getKey(), nodeDirectoryMapping);

			int hasNodeStarted = solrNode.doActionOnSolrNode(entry.getKey(), versionOne,
					nodePortMapping.get(entry.getKey()), Action.START, this.zkPort, nodeDirectoryMapping);
			if (hasNodeStarted != 0) {
				this.postMessage("Node :" + entry.getKey() + "Node startup failed for some reason ... ",
						MessageType.RESULT_ERRROR, true);
			}

			if (isDataIntact) {
				isDataIntact = zookeeperNode.verifyData(COLLECTION_NAME, this.zkPort);
				if (!isDataIntact) {
					this.postMessage("Data Integrity failed on node : " + entry.getKey(), MessageType.RESULT_ERRROR,
							true);
				}
			}
			Thread.sleep(15000);

		}

		if (zookeeperNode.getLiveNodes() == numNodes) {
			this.postMessage("All Nodes are up ... ", MessageType.RESULT_SUCCESS, true);

			if (isDataIntact) {
				this.postMessage("#### FINAL RESULT #### " + "@@ Data is verified and seems okay ... @@" + " ####",
						MessageType.RESULT_SUCCESS, true);
			} else {
				this.postMessage(
						"#### FINAL RESULT #### " + "@@ Data has been corrupted by this migration ... @@" + " ####",
						MessageType.RESULT_ERRROR, true);
			}

		} else {
			this.postMessage("All of the Nodes are not up ... Test seems failed ... ", MessageType.RESULT_ERRROR, true);
		}

		for (Map.Entry<Integer, String> entry : nodeDirectoryMapping.entrySet()) {

			int isNodeDownProperly = solrNode.doActionOnSolrNode(entry.getKey(), versionOne,
					nodePortMapping.get(entry.getKey()), Action.STOP, this.zkPort, nodeDirectoryMapping);
			if (isNodeDownProperly != 0) {
				this.postMessage("Node: " + entry.getKey() + "Node shutdown failed for some reason ... ",
						MessageType.RESULT_ERRROR, true);
			}

			Thread.sleep(5000);

			solrNode.deleteNodeDirectory(entry.getKey(), nodeDirectoryMapping);

		}

		zookeeperNode.doActionOnZookeeper(Action.STOP);
		zookeeperNode.deleteZookeeperData();

	}

	public static void main(String[] args) throws Exception {

		new SolrUpgradeTests().run(args);

	}
}