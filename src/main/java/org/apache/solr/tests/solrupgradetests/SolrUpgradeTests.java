package org.apache.solr.tests.solrupgradetests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrUpgradeTests {

	public String URL_BASE = "http://archive.apache.org/dist/lucene/solr/";

	public String ZOO_URL_BASE = "http://www.us.apache.org/dist/zookeeper/";

	public String WORK_DIRECTORY = System.getProperty("user.dir");

	public String DNAME = "SOLRUpdateTests";

	public String BASE_DIR = WORK_DIRECTORY + File.separator + DNAME + File.separator;

	public String TEMP_DIR = BASE_DIR + "temp" + File.separator;

	public String NODE_ONE_DIR = BASE_DIR + "N1" + File.separator;

	public String NODE_TWO_DIR = BASE_DIR + "N2" + File.separator;

	public String NODE_THREE_DIR = BASE_DIR + "N3" + File.separator;

	public String ZOOKEEPER_DIR = BASE_DIR + "ZOOKEEPER" + File.separator;

	public String HELLO = "[SOLR UPGRADE TESTS] HOLA !!! use -Help parameter to get more details on parameters";

	public String CHECKING_BDIR = "Checking if base directory exists ...";

	public String CHECKING_ZOOKEEPER = "Checking if zookeeper directory exists ...";

	public String CREATING_ZOOKEEPER_DIR = "Creating zookeeper directory ...";

	public String CHECKING_NDIR = "Checking if SOLR node directory exists ...";

	public String CHECKING_TDIR = "Checking if temp directory exists ...";

	public String CREATING_BDIR = "Base directory does not exist, creating one ...";

	public String CREATING_NDIR = "Node directory does not exist, creating it ...";

	public String CREATING_TDIR = "Temp directory does not exist Creating Temp directory ...";

	public String OK_MSG_GENERAL = "[OK] ...";

	public String DIR_CREATED = "Directory Successfully Created !";

	public String DOWNLOADING_RELEASE = "Attempting to download release ...";

	public String DOWNLOADING_ZOO_RELEASE = "Attempting to download zookeeper release ...";

	public String UNZIP_RELEASE = "Attempting to unzip the downloaded release ...";

	public String UNZIPPING_TO = "Unzipping to : ";

	public String START_PROC = "Attempting to start solr node ...";

	public String START_ZOO = "Attempting to start zookeeper ...";

	public String STOP_ZOO = "Attempting to stop zookeeper ...";

	public String STARTED_ZOO = "Zookeeper Started ...";

	public String STOPPED_ZOO = "Zookeeper Started ...";

	public String STOP_PROC = "Attempting to stop solr node ...";

	public String NODE_STARTED = "Node Started ... ";

	public String NODE_STOPPED = "Node Stopped ... ";

	public String CHECK_RELEASE_DOWNLOADED = "Checking if release has been downloaded ...";

	public String RELEASE_PRESENT = "Release is present ...";

	public String RELEASE_DOWNLOAD = "Release not present ! Release has to be downloaded / or copied into the node folder ... ";

	public String BAD_RELEASE_NAME = "Internet Connection Failure OR Release not present OR BAD Release name ... [EXITING]";

	public String ARG_VERSION_ONE = "-v1";

	public String ARG_VERSION_TWO = "-v2";

	public String ARG_SKIPCLEAN = "-SkipClean";

	public String ARG_SKIPUNZIP = "-SkipUnzip";

	public String ARG_TESTTYPE = "-TestType";

	public String ARG_WORK_DIR = "-WorkDirectory";

	public String CREATING_COLLECTION = "Creating collection, configuring shards and replication factor ... ";

	public String POSTING_DATA = "Posting data to the node ... ";

	public String GETTING_DATA = "Getting the data from nodes ... ";

	public String DELETING_DATA = "Deleting data from the nodes ... ";

	public String VERIFYING_DATA = "Verifying data from nodes ... ";

	public String DATA_OK = "Data is verified and seems okay ...";

	public String DATA_NOT_OK = "Data has been corrupted by this migration ... ";

	public String ARG_ERROR_VERSION_NULL = "SOLRUpdateTests Says: Need two SOLR versions to conduct this test ...";

	public String ARG_ERROR_VERSION_SAME = "SOLRUpdateTests Says: Comparing same versions is not useful, please provide two versions ...";

	public String ARG_PORT_ONE = "-N1Port";

	public String ARG_PORT_TWO = "-N2Port";

	public String ARG_PORT_THREE = "-N3Port";

	public String ARG_COLLECTION_NAME = "-CollectionName";

	public String ARG_ZK_PORT = "-ZkP";

	public String ARG_HELP = "-Help";

	public String HELP_L1 = "This testing program requires following parameters to run ... ";

	public String HELP_L2 = "-v1 {From Version: ex '5.4.0'}; -v2 {To Version: ex '5.4.1'} ";

	public String HELP_L3 = "-SkipClean: TRUE/FALSE {To clean the node directories or not}";

	public String HELP_L4 = "-SkipUnzip: TRUE/FALSE {To unzip the releases or not} ";

	public String HELP_L5 = "-N1Port/-N2Port/-N3Port: {Port number for three nodes, must be different. ex '1234'}";

	public String HELP_L6 = "-WorkDirectory: {Define a working directory on your system ex '/home'}";

	public String PORT_MISSING = "Port for each node is missing please define them through {-N1Port, -N2Port & -N3Port}";

	public String HELP_L7 = "-ZkP {zookeeper port number}";

	public int TEST_DOCUMENTS_COUNT = 1000;

	public String NUM_SHARDS = "2";

	public String NUM_REPLICAS = "3";

	public String COLLECTION_NAME = "TestCollection";

	public String portOne = "1234";

	public String portTwo = "1235";

	public String portThree = "1236";

	public String zkPort = "2181";

	public String zkIP = "127.0.0.1";

	public String GETTING_LIVE_NODES = "Attempting to get live nodes on the cluster ... ";

	public String NODES_LAUNCH_FAILURE = "Node startup failed for some reason ... ";

	public String NODES_SHUTDOWN_FAILURE = "Node shutdown failed for some reason ... ";

	public String ALL_NODES_UP = "All Nodes are up ... ";

	public String ALL_NODES_NOT_UP = "All of the Nodes are not up ... Test seems failed ... ";

	public String ADDED_DATA = "Added data into the cluster ...";

	public String ZOOKEEPER_RELEASE = "3.4.6";

	public String ATTEMPTING_UPGRADE = "Attempting upgrade on the node by replacing lib folder ...";

	public String UPGRADE_FAILED = "Upgrade failed due to some reason ...";

	public String UPGRADE_COMPELETE = " Upgrade process complete ... ";

	public static String solrCommand;

	public static String zooCommand;

	public enum ReleaseType {
		SOLR, ZOOKEEPER
	};

	public enum Action {
		START, STOP, ADD, UPDATE, DELETE, VERIFY
	};

	public enum Location {
		TEMP, NODE_ONE, NODE_TWO, NODE_THREE, ZOOKEEPER_DIR
	}

	public enum Type {
		COMPRESSED, EXTRACTED
	}

	class StreamGobbler extends Thread {

		InputStream is;
		String type;

		StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					System.out.println("SubProcess Output >> " + type + " >> " + line);
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}

	}

	public SolrUpgradeTests() {

		portOne = "" + this.getFreePort();
		portTwo = "" + this.getFreePort();
		portThree = "" + this.getFreePort();

	}

	public int getFreePort() {

		int port = ThreadLocalRandom.current().nextInt(10000, 60000);
		this.postMessage("Looking for a free port ... Checking availability of port number: " + port);
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			this.postMessage("Port " + port + " is free to use. Using this port !!");
			return port;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}

		this.postMessage("Port " + port + " looks occupied trying another port number ... ");
		return getFreePort();
	}

	static {

		solrCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "solr.cmd" : "bin" + File.separator + "solr";

		zooCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "zkServer.cmd " : "bin" + File.separator + "zkServer.sh ";

	}

	public void postMessage(String message) {

		System.out.println(message);

	}

	public void postMessageOnLine(String message) {
		System.out.print(message);
	}

	public void downloadRelease(String version, String dir, ReleaseType what) throws IOException {

		String fileName = null;
		URL link = null;
		InputStream in = null;
		FileOutputStream fos = null;

		try {

			if (what.equals(ReleaseType.SOLR)) {
				fileName = "solr-" + version + ".zip";
				String url = URL_BASE + File.separator + version + File.separator + fileName;
				this.postMessage(DOWNLOADING_RELEASE + " " + version + " from " + url);
				link = new URL(url);
			} else if (what.equals(ReleaseType.ZOOKEEPER)) {
				this.postMessage(DOWNLOADING_ZOO_RELEASE + " : " + version);
				fileName = "zookeeper-" + version + ".tar.gz";
				link = new URL(ZOO_URL_BASE + "zookeeper-" + version + File.separator + fileName);
			}

			in = new BufferedInputStream(link.openStream());
			fos = new FileOutputStream(TEMP_DIR + fileName);
			byte[] buf = new byte[1024 * 1024]; // 1mb blocks
			int n = 0;
			long size = 0;
			while (-1 != (n = in.read(buf))) {
				size += n;
				this.postMessageOnLine(size + " ");
				fos.write(buf, 0, n);
			}
			fos.close();
			in.close();
			fos.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage());

		}
	}

	public void unZipDownloadedRelease(String dir, String destinationDir) throws IOException {

		try {

			this.postMessage(UNZIP_RELEASE);
			File destDir = new File(destinationDir);
			if (!destDir.exists()) {
				destDir.mkdir();
			}

			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(dir));
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				String filePath = destinationDir + File.separator + entry.getName();
				if (!entry.isDirectory()) {
					this.postMessage(UNZIPPING_TO + destinationDir + " : " + entry.getName());
					extractFile(zipIn, filePath);
				} else {
					File dirx = new File(filePath);
					dirx.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage());

		}
	}

	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {

		BufferedOutputStream bos = null;
		try {

			bos = new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[4096];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
			bos.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage());

		}

	}

	public boolean createBaseDir() {

		try {
			File baseDir = new File(BASE_DIR);
			this.postMessage(CHECKING_BDIR);
			if (!baseDir.exists()) {
				this.postMessage(CREATING_BDIR);
				return baseDir.mkdir();
			}
			return false;

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return false;

		}

	}

	public boolean createZookeeperDir() {

		try {
			File zooDir = new File(ZOOKEEPER_DIR);
			this.postMessage(CHECKING_ZOOKEEPER);
			if (!zooDir.exists()) {
				this.postMessage(CREATING_ZOOKEEPER_DIR);
				return zooDir.mkdir();
			}
			return false;

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return false;

		}

	}

	public boolean createNodesDir() {

		try {

			this.postMessage(CHECKING_NDIR);
			File n1 = new File(NODE_ONE_DIR);
			File n2 = new File(NODE_TWO_DIR);
			File n3 = new File(NODE_THREE_DIR);
			boolean mn1 = false, mn2 = false, mn3 = false;

			if (!n1.exists()) {
				this.postMessage(CREATING_NDIR);
				mn1 = n1.mkdir();
			}

			if (!n2.exists()) {
				this.postMessage(CREATING_NDIR);
				mn2 = n2.mkdir();
			}

			if (!n3.exists()) {
				this.postMessage(CREATING_NDIR);
				mn3 = n3.mkdir();
			}

			return (mn1 && mn2 && mn3);

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return false;

		}
	}

	public boolean createTempDir() {

		try {

			this.postMessage(CHECKING_TDIR);
			File tempDir = new File(TEMP_DIR);
			if (!tempDir.exists()) {
				this.postMessage(CREATING_TDIR);
				return tempDir.mkdir();
			}

			return false;

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return false;

		}

	}

	public int doActionOnSolrNode(String node, String version, String port, Action action, String zkPort)
			throws IOException, InterruptedException {

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		String act = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			if (action.equals(Action.START)) {
				act = "start";
				this.postMessage(START_PROC + " : " + node);
			} else if (action.equals(Action.STOP)) {
				act = "stop";
				this.postMessage(STOP_PROC + " : " + node);
			}

			if ("N1".equals(node)) {
				new File(NODE_ONE_DIR + "solr-" + version + File.separator + solrCommand).setExecutable(true);
				proc = rt.exec(NODE_ONE_DIR + "solr-" + version + File.separator + solrCommand + " " + act + " -p "
						+ port + " -z " + zkIP + ":" + zkPort);
			} else if ("N2".equals(node)) {
				new File(NODE_TWO_DIR + "solr-" + version + File.separator + solrCommand).setExecutable(true);
				proc = rt.exec(NODE_TWO_DIR + "solr-" + version + File.separator + solrCommand + " " + act + " -p "
						+ port + " -z " + zkIP + ":" + zkPort);

			} else if ("N3".equals(node)) {
				new File(NODE_THREE_DIR + "solr-" + version + File.separator + solrCommand).setExecutable(true);
				proc = rt.exec(NODE_THREE_DIR + "solr-" + version + File.separator + solrCommand + " " + act + " -p "
						+ port + " -z " + zkIP + ":" + zkPort);
			}

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return -1;

		}

	}

	public int extractZookeeperRelease() throws IOException, InterruptedException {

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec(
					"tar -xf " + TEMP_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + ".tar.gz" + " -C " + ZOOKEEPER_DIR);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return -1;

		}

	}

	public int renameZookeeperConfFile() throws IOException, InterruptedException {

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec("mv " + ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + "conf"
					+ File.separator + "zoo_sample.cfg zoo.cfg");

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return -1;

		}

	}

	public int doActionOnZookeeper(Action action) throws IOException, InterruptedException {

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		String act = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			if (action.equals(Action.START)) {
				act = "start";
				this.postMessage(START_ZOO);
			} else if (action.equals(Action.STOP)) {
				act = "stop";
				this.postMessage(STOP_ZOO);
			}

			new File(ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + zooCommand)
					.setExecutable(true);
			proc = rt.exec(ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + zooCommand + " " + act);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return -1;

		}

	}

	public int createSOLRCollection(String node, String version, String collectionName, String shards,
			String replicationFactor) throws IOException, InterruptedException {

		this.postMessage(CREATING_COLLECTION + " : " + node);
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			if ("N1".equals(node)) {
				proc = rt
						.exec(NODE_ONE_DIR + "solr-" + version + File.separator + solrCommand + " create_collection -c "
								+ collectionName + " -shards " + shards + " -replicationFactor " + replicationFactor);
			} else if ("N2".equals(node)) {
				proc = rt
						.exec(NODE_TWO_DIR + "solr-" + version + File.separator + solrCommand + " create_collection -c "
								+ collectionName + " -shards " + shards + " -replicationFactor " + replicationFactor);
			} else if ("N3".equals(node)) {
				proc = rt.exec(
						NODE_THREE_DIR + "solr-" + version + File.separator + solrCommand + " create_collection -c "
								+ collectionName + " -shards " + shards + " -replicationFactor " + replicationFactor);
			}

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return -1;

		}

	}

	public void postData(String collectionName, String zkPort)
			throws IOException, InterruptedException, SolrServerException {

		this.postMessage(POSTING_DATA);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zkIP + ":" + zkPort);
			solr.connect();
			solr.setDefaultCollection(collectionName);
			SolrInputDocument document;

			for (int i = 1; i <= TEST_DOCUMENTS_COUNT; i++) {

				document = new SolrInputDocument();
				document.setField("EMP_ID", "EMP_ID@" + i);
				document.setField("TITLE", "TITLE@" + i);
				solr.add(collectionName, document);
				this.postMessageOnLine(". ");
			}
			this.postMessage(ADDED_DATA);
			solr.commit();
			solr.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage());

		}

	}

	public boolean verifyData(String collectionName, String zkPort)
			throws IOException, InterruptedException, SolrServerException {

		this.postMessage(GETTING_DATA);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zkIP + ":" + zkPort);
			solr.connect();
			solr.setDefaultCollection(collectionName);
			SolrQuery query = new SolrQuery("*:*");
			query.setRows(10000);
			SolrDocumentList docList = solr.query(query).getResults();

			int count = 0;
			for (SolrDocument document : docList) {
				if (!(document.getFieldValue("TITLE").toString().split("@", 2)[1]
						.equals(document.getFieldValue("EMP_ID").toString().split("@", 2)[1]))) {
					solr.close();
					return false;
				}
				count++;
				this.postMessageOnLine(". ");
			}

			if (count != TEST_DOCUMENTS_COUNT) {
				solr.close();
				return false;
			}

			solr.close();
			return true;

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return false;

		}

	}

	public void deleteData(String collectionName, String zkPort)
			throws IOException, InterruptedException, SolrServerException {

		this.postMessage(DELETING_DATA);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zkIP + ":" + zkPort);
			solr.connect();
			solr.setDefaultCollection(collectionName);
			solr.deleteByQuery("*:*");
			solr.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage());

		}
	}

	public int getLiveNodes() throws IOException {

		this.postMessage(GETTING_LIVE_NODES);
		CloudSolrClient solr = null;
		try {
			solr = new CloudSolrClient(zkIP + ":" + zkPort);
			solr.connect();
			int liveNodes = solr.getZkStateReader().getClusterState().getLiveNodes().size();
			solr.close();
			return liveNodes;

		} catch (Exception e) {

			this.postMessage(e.getMessage());
			return -1;

		}

	}

	public void upgradeSolr(String versionOne, String versionTwo, String node) throws IOException {

		this.postMessage(ATTEMPTING_UPGRADE + "From: " + versionOne + " To: " + versionTwo);
		try {
			String localPath = File.separator + "server" + File.separator + "solr-webapp" + File.separator + "webapp"
					+ File.separator + "WEB-INF" + File.separator + "lib";
			File src = new File(TEMP_DIR + "solr-" + versionTwo + localPath);
			File dest = null;
			if ("N1".equals(node)) {
				dest = new File(NODE_ONE_DIR + "solr-" + versionOne + localPath);
			} else if ("N2".equals(node)) {
				dest = new File(NODE_TWO_DIR + "solr-" + versionOne + localPath);
			} else if ("N3".equals(node)) {
				dest = new File(NODE_THREE_DIR + "solr-" + versionOne + localPath);
			}

			FileUtils.cleanDirectory(dest);
			FileUtils.copyDirectory(src, dest);
			this.postMessage(UPGRADE_COMPELETE);
		} catch (Exception e) {
			this.postMessage(UPGRADE_FAILED);
			this.postMessage(e.getMessage());

		}

	}

	public boolean checkForRelease(String version, ReleaseType name, Location location, Type type) {

		this.postMessage(CHECK_RELEASE_DOWNLOADED + " >> " + TEMP_DIR + "solr-" + version + ".zip" + " Type: " + type
				+ " Location:" + location);
		File release = null;
		if (name.equals(ReleaseType.SOLR)) {
			if (location.equals(Location.NODE_ONE)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(NODE_ONE_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(NODE_ONE_DIR + "solr-" + version);
				}
			} else if (location.equals(Location.NODE_TWO)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(NODE_TWO_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(NODE_TWO_DIR + "solr-" + version);
				}
			} else if (location.equals(Location.NODE_THREE)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(NODE_THREE_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(NODE_THREE_DIR + "solr-" + version);
				}
			} else if (location.equals(Location.TEMP)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(TEMP_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(TEMP_DIR + "solr-" + version);
				}
			}

			if (release.exists()) {
				this.postMessage(RELEASE_PRESENT);
				return true;
			}
		} else if (name.equals(ReleaseType.ZOOKEEPER)) {

			if (location.equals(Location.TEMP)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(TEMP_DIR + "zookeeper-" + version + ".tar.gz");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(TEMP_DIR + "zookeeper-" + version);
				}
			} else if (location.equals(Location.ZOOKEEPER_DIR)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(ZOOKEEPER_DIR + "zookeeper-" + version + ".tar.gz");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(ZOOKEEPER_DIR + "zookeeper-" + version);
				}
			}

			if (release.exists()) {
				this.postMessage(RELEASE_PRESENT);
				return true;
			}

		}

		this.postMessage(RELEASE_DOWNLOAD);
		return false;

	}

	public void cleanNodeDirs() throws IOException {

		try {

			File n1 = new File(NODE_ONE_DIR);
			FileUtils.cleanDirectory(n1);
			File n2 = new File(NODE_TWO_DIR);
			FileUtils.cleanDirectory(n2);
			File n3 = new File(NODE_THREE_DIR);
			FileUtils.cleanDirectory(n3);

		} catch (Exception e) {

			this.postMessage(e.getMessage());

		}
	}

	public void run(String[] args) throws Exception {

		if (args.length == 0) {
			this.postMessage(HELP_L1);
			this.postMessage(HELP_L2);
			this.postMessage(HELP_L3);
			this.postMessage(HELP_L4);
			this.postMessage(HELP_L5);
			this.postMessage(HELP_L6);
			this.postMessage(HELP_L7);

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
		String prtOne = argM.get(ARG_PORT_ONE);
		String prtTwo = argM.get(ARG_PORT_TWO);
		String prtThree = argM.get(ARG_PORT_THREE);

		if (prtOne != null) {
			this.portOne = prtOne;
		}

		if (prtTwo != null) {
			this.portTwo = prtTwo;
		}

		if (prtThree != null) {
			this.portThree = prtThree;
		}

		if (help != null) {
			this.postMessage(HELP_L1);
			this.postMessage(HELP_L2);
			this.postMessage(HELP_L3);
			this.postMessage(HELP_L4);
			this.postMessage(HELP_L5);
			this.postMessage(HELP_L6);
			this.postMessage(HELP_L7);

			return;
		}

		if (zkPort != null) {
			this.zkPort = zkPort;
		}

		COLLECTION_NAME += "" + UUID.randomUUID().toString();

		if (collectionName != null) {
			COLLECTION_NAME = collectionName;
		}

		if (versionOne == null || versionTwo == null) {
			throw new Exception(ARG_ERROR_VERSION_NULL);
		}

		if (versionOne.equals(versionTwo)) {
			throw new Exception(ARG_ERROR_VERSION_SAME);
		}

		if (rootDir != null) {
			this.WORK_DIRECTORY = rootDir;
		}

		this.postMessage(HELLO);
		this.postMessage("Testing upgrade from " + versionOne + " To " + versionTwo);

		if (this.createBaseDir()) {
			this.postMessage(DIR_CREATED);
		}

		if (this.createNodesDir()) {
			this.postMessage(DIR_CREATED);
		}

		this.cleanNodeDirs();

		if (this.createTempDir()) {
			this.postMessage(DIR_CREATED);
		}

		this.createZookeeperDir();

		if (!this.checkForRelease(ZOOKEEPER_RELEASE, ReleaseType.ZOOKEEPER, Location.TEMP, Type.COMPRESSED)) {
			try {
				this.downloadRelease(ZOOKEEPER_RELEASE, TEMP_DIR, ReleaseType.ZOOKEEPER);
				this.extractZookeeperRelease();
				this.renameZookeeperConfFile();
			} catch (IOException e) {
				this.postMessage(BAD_RELEASE_NAME);
				return;
			}
		}
		
		this.doActionOnZookeeper(Action.START);
		this.doActionOnZookeeper(Action.STOP);
		System.exit(0);
		

		if (!this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.EXTRACTED)) {
			if (this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
				this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionTwo + ".zip", TEMP_DIR);
			}
		}

		if (!this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
			try {
				this.downloadRelease(versionTwo, TEMP_DIR, ReleaseType.SOLR);
				this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionTwo + ".zip", TEMP_DIR);
			} catch (IOException e) {
				this.postMessage(BAD_RELEASE_NAME);
				return;
			}
		}

		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.EXTRACTED)) {
			if (this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
				this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionOne + ".zip", TEMP_DIR);
			}
		}

		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
			try {
				this.downloadRelease(versionOne, TEMP_DIR, ReleaseType.SOLR);
				this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionOne + ".zip", TEMP_DIR);
			} catch (IOException e) {
				this.postMessage(BAD_RELEASE_NAME);
				return;
			}
		}

		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_ONE, Type.EXTRACTED)) {
			File node = new File(NODE_ONE_DIR + "solr-" + versionOne);
			node.mkdir();
			FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
		}
		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_TWO, Type.EXTRACTED)) {
			File node = new File(NODE_TWO_DIR + "solr-" + versionOne);
			node.mkdir();
			FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
		}
		if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_THREE, Type.EXTRACTED)) {
			File node = new File(NODE_THREE_DIR + "solr-" + versionOne);
			node.mkdir();
			FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
		}

		// MAIN TEST SEQUENCE HERE //

		int evp1 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.START, this.zkPort);
		int evp2 = this.doActionOnSolrNode("N2", versionOne, portTwo, Action.START, this.zkPort);
		int evp3 = this.doActionOnSolrNode("N3", versionOne, portThree, Action.START, this.zkPort);

		if (evp1 != 0 || evp2 != 0 || evp3 != 0) {
			this.postMessage(NODES_LAUNCH_FAILURE);
		}

		this.createSOLRCollection("N1", versionOne, COLLECTION_NAME, NUM_SHARDS, NUM_REPLICAS);
		this.postData(COLLECTION_NAME, this.zkPort);

		int evp4 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.STOP, this.zkPort);
		if (evp4 != 0) {
			this.postMessage("N1 " + NODES_SHUTDOWN_FAILURE);
		}
		this.upgradeSolr(versionOne, versionTwo, "N1");
		int evp5 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.START, this.zkPort);
		if (evp5 != 0) {
			this.postMessage("N1 " + NODES_LAUNCH_FAILURE);
		}

		boolean test1 = this.verifyData(COLLECTION_NAME, this.zkPort);

		int evp6 = this.doActionOnSolrNode("N2", versionOne, portTwo, Action.STOP, this.zkPort);
		if (evp6 != 0) {
			this.postMessage("N2 " + NODES_SHUTDOWN_FAILURE);
		}
		this.upgradeSolr(versionOne, versionTwo, "N2");
		int evp7 = this.doActionOnSolrNode("N2", versionOne, portTwo, Action.START, this.zkPort);
		if (evp7 != 0) {
			this.postMessage("N2 " + NODES_LAUNCH_FAILURE);
		}

		boolean test2 = this.verifyData(COLLECTION_NAME, this.zkPort);

		int evp8 = this.doActionOnSolrNode("N3", versionOne, portThree, Action.STOP, this.zkPort);
		if (evp8 != 0) {
			this.postMessage("N3" + NODES_SHUTDOWN_FAILURE);
		}
		this.upgradeSolr(versionOne, versionTwo, "N3");
		int evp9 = this.doActionOnSolrNode("N3", versionOne, portThree, Action.START, this.zkPort);
		if (evp9 != 0) {
			this.postMessage("N3" + NODES_LAUNCH_FAILURE);
		}

		boolean test3 = this.verifyData(COLLECTION_NAME, this.zkPort);

		if (this.getLiveNodes() == 3) {
			this.postMessage(ALL_NODES_UP);
		} else {
			this.postMessage(ALL_NODES_NOT_UP);
		}

		if (test1 && test2 && test3) {
			this.postMessage("#### FINAL RESULT #### " + DATA_OK + " ####");
		} else {
			this.postMessage("#### FINAL RESULT #### " + DATA_NOT_OK + " ####");
		}

		Thread.sleep(10000);

		int evp10 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.STOP, this.zkPort);

		if (evp10 != 0) {
			this.postMessage("N1 " + NODES_SHUTDOWN_FAILURE);
		}

		int evp11 = this.doActionOnSolrNode("N2", versionOne, portTwo, Action.STOP, this.zkPort);
		if (evp11 != 0) {
			this.postMessage("N2 " + NODES_SHUTDOWN_FAILURE);
		}

		int evp12 = this.doActionOnSolrNode("N3", versionOne, portThree, Action.STOP, this.zkPort);
		if (evp12 != 0) {
			this.postMessage("N3 " + NODES_SHUTDOWN_FAILURE);
		}

	}

	public static void main(String[] args) throws Exception {

		new SolrUpgradeTests().run(args);

	}
}