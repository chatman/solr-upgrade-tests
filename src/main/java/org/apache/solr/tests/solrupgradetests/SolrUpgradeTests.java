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
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrUpgradeTests {

	final static Logger logger = Logger.getLogger(SolrUpgradeTests.class);

	public String URL_BASE = "http://archive.apache.org/dist/lucene/solr/";
	public String ZOO_URL_BASE = "http://www.us.apache.org/dist/zookeeper/";

	public String WORK_DIRECTORY = System.getProperty("user.dir");
	public String DNAME = "SOLRUpdateTests";
	public String BASE_DIR = WORK_DIRECTORY + File.separator + DNAME + File.separator;
	public String TEMP_DIR = BASE_DIR + "temp" + File.separator;
	public String ZOOKEEPER_DIR = BASE_DIR + "ZOOKEEPER" + File.separator;
	public String NUM_SHARDS = "2";
	public String NUM_REPLICAS = "3";
	public String COLLECTION_NAME = "TestCollection";

	public String ARG_VERSION_ONE = "-v1";
	public String ARG_VERSION_TWO = "-v2";
	public String ARG_SKIPCLEAN = "-SkipClean";
	public String ARG_SKIPUNZIP = "-SkipUnzip";
	public String ARG_TESTTYPE = "-TestType";
	public String ARG_WORK_DIR = "-WorkDirectory";
	public String ARG_VERBOSE = "-Verbose";
	public String ARG_ZK_PORT = "-ZkP";
	public String ARG_HELP = "-Help";
	public String ARG_NUM_SHARDS = "-NShards";
	public String ARG_NUM_REPLICAS = "-NReplicas";
	public String ARG_NUM_NODES = "-NNodes";

	public String zkPort = "2181";
	public String zkIP = "127.0.0.1";
	public String ZOOKEEPER_RELEASE = "3.4.6";

	public static String solrCommand;
	public static String zooCommand;

	public boolean isVerbose = false;

	public int numNodes = 3;
	public int TEST_DOCUMENTS_COUNT = 1000;

	public Map<Integer, String> nodeDirectoryMapping;
	public Map<Integer, String> nodePortMapping;

	public boolean isVerbose() {
		return isVerbose;
	}

	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;
	}

	public enum ReleaseType {
		SOLR, ZOOKEEPER
	};

	public enum MessageType {
		PROCESS, ACTION, RESULT_SUCCESS, RESULT_ERRROR, GENERAL
	};

	public enum Action {
		START, STOP, ADD, UPDATE, DELETE, VERIFY, CREATE
	};

	public enum Location {
		TEMP, ZOOKEEPER_DIR, NODES
	};

	public enum Type {
		COMPRESSED, EXTRACTED
	};

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
					if (isVerbose) {
						SolrUpgradeTests.this.postMessage("  SubProcess: " + type + " >> " + line, MessageType.PROCESS,
								true);
					}

			} catch (IOException ioe) {
				SolrUpgradeTests.this.postMessage(ioe.getMessage(), MessageType.RESULT_ERRROR, true);
			}
		}

	}

	static {

		solrCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "solr.cmd" : "bin" + File.separator + "solr";

		zooCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "zkServer.cmd " : "bin" + File.separator + "zkServer.sh ";

	}

	public int doActionOnSolrNode(int node, String version, String port, Action action, String zkPort,
			Map<Integer, String> nodes) throws IOException, InterruptedException {

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		String act = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			if (action.equals(Action.START)) {
				act = "start";
				this.postMessage("** Attempting to start solr node ..." + " : " + node, MessageType.RESULT_ERRROR,
						true);
			} else if (action.equals(Action.STOP)) {
				act = "stop";
				this.postMessage("** Attempting to stop solr node ..." + " : " + node, MessageType.RESULT_ERRROR, true);
			}

			new File(nodes.get(node) + "solr-" + version + File.separator + solrCommand).setExecutable(true);
			proc = rt.exec(nodes.get(node) + "solr-" + version + File.separator + solrCommand + " " + act + " -p "
					+ port + " -z " + zkIP + ":" + zkPort);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

	public int deleteNodeDirectory(int node, Map<Integer, String> nodes) throws IOException, InterruptedException {

		this.postMessage("Deleting directory for Node : " + node, MessageType.ACTION, true);
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec("rm -r -f " + nodes.get(node));

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

	public int createSOLRCollection(int node, String version, String collectionName, String shards,
			String replicationFactor, Map<Integer, String> nodes) throws IOException, InterruptedException {

		this.postMessage("** Creating collection, configuring shards and replication factor ... " + " : " + node,
				MessageType.ACTION, true);
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec(nodes.get(node) + "solr-" + version + File.separator + solrCommand + " create_collection -c "
					+ collectionName + " -shards " + shards + " -replicationFactor " + replicationFactor);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

	public void upgradeSolr(String versionOne, String versionTwo, int node, Map<Integer, String> nodes)
			throws IOException {

		this.postMessage("** Attempting upgrade on the node by replacing lib folder ..." + "From: " + versionOne
				+ " To: " + versionTwo, MessageType.ACTION, true);
		try {
			String localPath = File.separator + "server" + File.separator + "solr-webapp" + File.separator + "webapp"
					+ File.separator + "WEB-INF" + File.separator + "lib";

			File src = new File(TEMP_DIR + "solr-" + versionTwo + localPath);
			File dest = new File(nodes.get(node) + "solr-" + versionOne + localPath);

			FileUtils.cleanDirectory(dest);
			FileUtils.copyDirectory(src, dest);
			this.postMessage("Upgrade process complete ... ", MessageType.RESULT_SUCCESS, true);
		} catch (Exception e) {
			this.postMessage("Upgrade failed due to some reason ...", MessageType.RESULT_ERRROR, true);
			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public int getFreePort() {

		int port = ThreadLocalRandom.current().nextInt(10000, 60000);
		this.postMessage("Looking for a free port ... Checking availability of port number: " + port,
				MessageType.ACTION, true);
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			this.postMessage("Port " + port + " is free to use. Using this port !!", MessageType.RESULT_SUCCESS, true);
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

		this.postMessage("Port " + port + " looks occupied trying another port number ... ", MessageType.RESULT_ERRROR,
				true);
		return getFreePort();
	}

	public void postMessage(String message, MessageType type, boolean printInLog) {

		String ANSI_RESET = "\u001B[0m";
		String ANSI_RED = "\u001B[31m";
		String ANSI_GREEN = "\u001B[32m";
		String ANSI_YELLOW = "\u001B[33m";
		String ANSI_BLUE = "\u001B[34m";
		String ANSI_WHITE = "\u001B[37m";

		if (isVerbose) {

			if (type.equals(MessageType.ACTION)) {
				System.out.println(ANSI_WHITE + message + ANSI_RESET);
			} else if (type.equals(MessageType.GENERAL)) {
				System.out.println(ANSI_BLUE + message + ANSI_RESET);
			} else if (type.equals(MessageType.PROCESS)) {
				System.out.println(ANSI_YELLOW + message + ANSI_RESET);
			} else if (type.equals(MessageType.RESULT_ERRROR)) {
				System.out.println(ANSI_RED + message + ANSI_RESET);
			} else if (type.equals(MessageType.RESULT_SUCCESS)) {
				System.out.println(ANSI_GREEN + message + ANSI_RESET);
			}

		}

		if (printInLog) {
			logger.info(message);
		}

	}

	public void postMessageOnLine(String message) {

		if (isVerbose) {
			System.out.print(message);
		}

	}

	public boolean createBaseDir() {

		try {
			File baseDir = new File(BASE_DIR);
			this.postMessage("** Checking if base directory exists ...", MessageType.ACTION, true);
			if (!baseDir.exists()) {
				this.postMessage("Base directory does not exist, creating one ...", MessageType.ACTION, true);
				return baseDir.mkdir();
			}
			return false;

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

		}

	}

	public boolean doActionOnNodesDir(Map<Integer, String> nodes, Action action) {

		try {

			this.postMessage("** Checking if SOLR node directory exists ...", MessageType.ACTION, true);

			boolean attempt = true;
			for (Map.Entry<Integer, String> entry : nodes.entrySet()) {

				File node = new File(entry.getValue());
				if (!node.exists()) {
					this.postMessage("Node directory does not exist, creating it ...", MessageType.ACTION, true);
					if (action.equals(Action.CREATE)) {
						attempt = node.mkdir();
						this.postMessage("Directory Created: " + entry.getValue(), MessageType.RESULT_SUCCESS, true);
					} else if (action.equals(Action.DELETE)) {
						attempt = node.delete();
						this.postMessage("Directory Deleted: " + entry.getValue(), MessageType.RESULT_SUCCESS, true);
					}
					if (!attempt) {
						return false;
					}
				}

			}

			return attempt;

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

		}
	}

	public boolean createTempDir() {

		try {

			this.postMessage("** Checking if temp directory exists ...", MessageType.ACTION, true);
			File tempDir = new File(TEMP_DIR);
			if (!tempDir.exists()) {
				this.postMessage("Temp directory does not exist Creating Temp directory ...", MessageType.ACTION, true);
				return tempDir.mkdir();
			}

			return false;

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

		}

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
				this.postMessage("** Attempting to download release ..." + " " + version + " from " + url,
						MessageType.ACTION, true);
				link = new URL(url);
			} else if (what.equals(ReleaseType.ZOOKEEPER)) {
				this.postMessage("** Attempting to download zookeeper release ..." + " : " + version,
						MessageType.ACTION, true);
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

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}
	}

	public void unzipDownloadedRelease(String dir, String destinationDir) throws IOException {

		try {

			this.postMessage("** Attempting to unzip the downloaded release ...", MessageType.ACTION, true);
			File destDir = new File(destinationDir);
			if (!destDir.exists()) {
				destDir.mkdir();
			}

			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(dir));
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				String filePath = destinationDir + File.separator + entry.getName();
				if (!entry.isDirectory()) {
					this.postMessage("Unzipping to : " + destinationDir + " : " + entry.getName(), MessageType.ACTION,
							true);
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

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

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

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public boolean checkForRelease(String version, ReleaseType name, Location location, Type type,
			Map<Integer, String> nodes, int node) {

		this.postMessage("** Checking if release has been downloaded ..." + " >> " + TEMP_DIR + "solr-" + version
				+ ".zip" + " Type: " + type + " Location:" + location, MessageType.ACTION, true);
		File release = null;
		if (name.equals(ReleaseType.SOLR)) {

			if (location.equals(Location.NODES)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(nodes.get(node) + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(nodes.get(node) + "solr-" + version);
				}
			} else if (location.equals(Location.TEMP)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(TEMP_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(TEMP_DIR + "solr-" + version);
				}
			}

			if (release.exists()) {
				this.postMessage("Release is present ...", MessageType.RESULT_SUCCESS, true);
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
				this.postMessage("Release is present ...", MessageType.RESULT_SUCCESS, true);
				return true;
			}

		}

		this.postMessage("Release not present ! Release has to be downloaded / or copied into the node folder ... ",
				MessageType.RESULT_ERRROR, true);
		return false;

	}

	public void cleanNodeDirs(Map<Integer, String> nodes) throws IOException {

		try {

			for (Map.Entry<Integer, String> entry : nodes.entrySet()) {

				File node = new File(entry.getValue());
				if (node.exists()) {
					FileUtils.cleanDirectory(node);
					this.postMessage("Directory Cleaned: " + entry.getValue(), MessageType.RESULT_SUCCESS, true);
				}

			}

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}
	}

	public boolean createZookeeperDir() {

		try {
			File zooDir = new File(ZOOKEEPER_DIR);
			this.postMessage("** Checking if zookeeper directory exists ...", MessageType.ACTION, true);
			if (!zooDir.exists()) {
				this.postMessage("Creating zookeeper directory ...", MessageType.ACTION, true);
				return zooDir.mkdir();
			}
			return false;

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

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

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
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
					+ File.separator + "zoo_sample.cfg " + ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE
					+ File.separator + "conf" + File.separator + "zoo.cfg");

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

	public int deleteZookeeperData() throws IOException, InterruptedException {

		this.postMessage("Deleting directory for zookeeper data ", MessageType.ACTION, true);
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec("rm -r -f /tmp/zookeeper/");

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
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
				this.postMessage("** Attempting to start zookeeper ...", MessageType.ACTION, true);
			} else if (action.equals(Action.STOP)) {
				act = "stop";
				this.postMessage("** Attempting to stop zookeeper ...", MessageType.ACTION, true);
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

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

	public void postData(String collectionName, String zkPort)
			throws IOException, InterruptedException, SolrServerException {

		this.postMessage("** Posting data to the node ... ", MessageType.ACTION, true);
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
				if (i % 10 == 0) {
					this.postMessageOnLine("|");
				}
			}
			this.postMessage("", MessageType.GENERAL, false);
			this.postMessage("Added data into the cluster ...", MessageType.RESULT_SUCCESS, true);
			solr.commit();
			solr.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public boolean verifyData(String collectionName, String zkPort)
			throws IOException, InterruptedException, SolrServerException {

		this.postMessage("** Getting the data from nodes ... ", MessageType.RESULT_ERRROR, true);
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
					this.postMessage("%%%% DATA CORRUPTED, returning false  %%%%", MessageType.RESULT_ERRROR, true);
					return false;
				}
				count++;
				if (count % 10 == 0) {
					this.postMessageOnLine("|");
				}
			}
			this.postMessage("", MessageType.GENERAL, false);

			if (count != TEST_DOCUMENTS_COUNT) {
				this.postMessage("%%%% DATA COUNT MISMATCH, returning false  %%%%", MessageType.RESULT_ERRROR, true);
				solr.close();
				return false;
			}

			solr.close();
			return true;

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

		}

	}

	public void deleteData(String collectionName, String zkPort)
			throws IOException, InterruptedException, SolrServerException {

		this.postMessage("** Deleting data from the nodes ... ", MessageType.ACTION, true);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zkIP + ":" + zkPort);
			solr.connect();
			solr.setDefaultCollection(collectionName);
			solr.deleteByQuery("*:*");
			solr.close();

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}
	}

	public int getLiveNodes() throws IOException {

		this.postMessage("** Attempting to get live nodes on the cluster ... ", MessageType.ACTION, true);
		CloudSolrClient solr = null;
		try {
			solr = new CloudSolrClient(zkIP + ":" + zkPort);
			solr.connect();
			int liveNodes = solr.getZkStateReader().getClusterState().getLiveNodes().size();
			solr.close();
			return liveNodes;

		} catch (Exception e) {

			this.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

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
			this.isVerbose = false;

		} else if (verbose != null && verbose.equalsIgnoreCase("TRUE")) {
			this.isVerbose = true;
		}

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

		this.createZookeeperDir();

		if (!this.checkForRelease(ZOOKEEPER_RELEASE, ReleaseType.ZOOKEEPER, Location.TEMP, Type.COMPRESSED,
				nodeDirectoryMapping, -1)) {
			try {
				this.downloadRelease(ZOOKEEPER_RELEASE, TEMP_DIR, ReleaseType.ZOOKEEPER);
				this.extractZookeeperRelease();
				this.renameZookeeperConfFile();
			} catch (IOException e) {
				this.postMessage("Internet Connection Failure OR Release not present OR BAD Release name ... [EXITING]",
						MessageType.RESULT_ERRROR, true);
				return;
			}
		}

		if (this.doActionOnZookeeper(Action.START) != 0) {
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

			int srv = this.doActionOnSolrNode(entry.getKey(), versionOne, nodePortMapping.get(entry.getKey()),
					Action.START, this.zkPort, nodeDirectoryMapping);
			if (srv != 0) {
				this.postMessage("Node startup failed for node ... : " + entry.getKey(), MessageType.RESULT_ERRROR,
						true);
			}
			Thread.sleep(15000);
		}

		this.createSOLRCollection(1, versionOne, COLLECTION_NAME, NUM_SHARDS, NUM_REPLICAS, nodeDirectoryMapping);
		this.postData(COLLECTION_NAME, this.zkPort);

		boolean isDataIntact = true;
		for (Map.Entry<Integer, String> entry : nodeDirectoryMapping.entrySet()) {

			int hasNodeStopped = this.doActionOnSolrNode(entry.getKey(), versionOne,
					nodePortMapping.get(entry.getKey()), Action.STOP, this.zkPort, nodeDirectoryMapping);
			if (hasNodeStopped != 0) {
				this.postMessage("Node :" + entry.getKey() + "Node shutdown failed for some reason ... ",
						MessageType.RESULT_ERRROR, true);
			}

			Thread.sleep(15000);

			this.upgradeSolr(versionOne, versionTwo, entry.getKey(), nodeDirectoryMapping);

			int hasNodeStarted = this.doActionOnSolrNode(entry.getKey(), versionOne,
					nodePortMapping.get(entry.getKey()), Action.START, this.zkPort, nodeDirectoryMapping);
			if (hasNodeStarted != 0) {
				this.postMessage("Node :" + entry.getKey() + "Node startup failed for some reason ... ",
						MessageType.RESULT_ERRROR, true);
			}

			if (isDataIntact) {
				isDataIntact = this.verifyData(COLLECTION_NAME, this.zkPort);
				if (!isDataIntact) {
					this.postMessage("Data Integrity failed on node : " + entry.getKey(), MessageType.RESULT_ERRROR,
							true);
				}
			}
			Thread.sleep(15000);

		}

		if (this.getLiveNodes() == numNodes) {
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

			int isNodeDownProperly = this.doActionOnSolrNode(entry.getKey(), versionOne,
					nodePortMapping.get(entry.getKey()), Action.STOP, this.zkPort, nodeDirectoryMapping);
			if (isNodeDownProperly != 0) {
				this.postMessage("Node: " + entry.getKey() + "Node shutdown failed for some reason ... ",
						MessageType.RESULT_ERRROR, true);
			}

			Thread.sleep(5000);

			this.deleteNodeDirectory(entry.getKey(), nodeDirectoryMapping);

		}

		this.doActionOnZookeeper(Action.STOP);
		this.deleteZookeeperData();

	}

	public static void main(String[] args) throws Exception {

		new SolrUpgradeTests().run(args);

	}

}