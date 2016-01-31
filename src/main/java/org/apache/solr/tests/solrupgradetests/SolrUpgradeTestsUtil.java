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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class SolrUpgradeTestsUtil {
	
	final static Logger logger = Logger.getLogger(SolrUpgradeTestsUtil.class);
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
	public String ARG_VERSION_ONE = "-v1";
	public String ARG_VERSION_TWO = "-v2";
	public String ARG_SKIPCLEAN = "-SkipClean";
	public String ARG_SKIPUNZIP = "-SkipUnzip";
	public String ARG_TESTTYPE = "-TestType";
	public String ARG_WORK_DIR = "-WorkDirectory";
	public String ARG_PORT_ONE = "-N1Port";
	public String ARG_PORT_TWO = "-N2Port";
	public String ARG_PORT_THREE = "-N3Port";
	public String ARG_VERBOSE = "-Verbose";
	public String ARG_COLLECTION_NAME = "-CollectionName";
	public String ARG_ZK_PORT = "-ZkP";
	public String ARG_HELP = "-Help";
	public String ARG_NUM_SHARDS = "-NShards";
	public String ARG_NUM_REPLICAS = "-NReplicas";
	public String ARG_NUM_NODES = "-NNodes";
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
	public String ZOOKEEPER_RELEASE = "3.4.6";
	public static String solrCommand;
	public static String zooCommand;
	public boolean isVerbose = false;
	public int numNodes = 3;
	public Map<Integer, String> nodeDirectoryMapping;
	public Map<Integer, String> nodePortMapping;
	
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
						SolrUpgradeTestsUtil.this.postMessage("  SubProcess: " + type + " >> " + line,
								MessageType.PROCESS, true);
					}

			} catch (IOException ioe) {
				SolrUpgradeTestsUtil.this.postMessage(ioe.getMessage(), MessageType.RESULT_ERRROR, true);
			}
		}

	}
	
	static {

		solrCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "solr.cmd" : "bin" + File.separator + "solr";

		zooCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "zkServer.cmd " : "bin" + File.separator + "zkServer.sh ";

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

}
