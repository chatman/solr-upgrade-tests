package org.apache.solr.tests.solrupgradetests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.tests.solrupgradetests.Util.MessageType;

public class ZookeeperNode {

	public static String zooCommand;

	static {

		zooCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "zkServer.cmd " : "bin" + File.separator + "zkServer.sh ";

	}

	public String ZOO_URL_BASE = "http://www.us.apache.org/dist/zookeeper/";
	public String ZOOKEEPER_RELEASE = "3.4.6";
	public String ZOOKEEPER_DIR = SolrRollingUpgradeTests.BASE_DIR + "ZOOKEEPER" + File.separator;
	public int TEST_DOCUMENTS_COUNT = 1000;

	private String zookeeperIp = "127.0.0.1";
	private String zookeeperPort = "2181";
	private boolean started = false;
	private boolean stopped = false;

	public void install() throws IOException {
		
		Util.postMessage("** Installing Zookeeper Node ...", MessageType.ACTION, true);

		File base = new File(ZOOKEEPER_DIR);
		if (!base.exists()) {
			base.mkdir();
			base.setExecutable(true);
		}

		File release = new File(SolrRollingUpgradeTests.TEMP_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + ".tar.gz");
		if (!release.exists()) {

			String fileName = null;
			URL link = null;
			InputStream in = null;
			FileOutputStream fos = null;

			try {
				Util.postMessage("** Attempting to download zookeeper release ..." + " : " + ZOOKEEPER_RELEASE,
						MessageType.ACTION, true);
				fileName = "zookeeper-" + ZOOKEEPER_RELEASE + ".tar.gz";
				link = new URL(ZOO_URL_BASE + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + fileName);

				in = new BufferedInputStream(link.openStream());
				fos = new FileOutputStream(SolrRollingUpgradeTests.TEMP_DIR + fileName);
				byte[] buf = new byte[1024 * 1024]; // 1mb blocks
				int n = 0;
				long size = 0;
				while (-1 != (n = in.read(buf))) {
					size += n;
					Util.postMessageOnLine(size + " ");
					fos.write(buf, 0, n);
				}
				fos.close();
				in.close();
				fos.close();

			} catch (Exception e) {

				Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

			}

		}

		File urelease = new File(SolrRollingUpgradeTests.TEMP_DIR + "zookeeper-" + ZOOKEEPER_RELEASE);
		if (!urelease.exists()) {

			{
				Runtime rt = Runtime.getRuntime();
				Process proc = null;
				StreamGobbler errorGobbler = null;
				StreamGobbler outputGobbler = null;

				try {

					proc = rt.exec("tar -xf " + SolrRollingUpgradeTests.TEMP_DIR + "zookeeper-" + ZOOKEEPER_RELEASE
							+ ".tar.gz" + " -C " + ZOOKEEPER_DIR);

					errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
					outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

					errorGobbler.start();
					outputGobbler.start();
					proc.waitFor();

				} catch (Exception e) {

					Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

				}
			}

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

			} catch (Exception e) {

				Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

			}

		} else {

			File node = new File(SolrRollingUpgradeTests.TEMP_DIR + "zookeeper-" + ZOOKEEPER_RELEASE);
			node.mkdir();
			FileUtils.copyDirectory(new File(SolrRollingUpgradeTests.TEMP_DIR + "zookeeper-" + ZOOKEEPER_RELEASE),
					node);

		}

	}

	public void start() {
		
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			Util.postMessage("** Attempting to start zookeeper ...", MessageType.ACTION, true);

			new File(ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + zooCommand)
					.setExecutable(true);
			proc = rt.exec(ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + zooCommand + " start");

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();

			if (proc.exitValue() == 0) {
				this.started = true;
			} else {
				this.started = false;
			}

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public void stop() {

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			Util.postMessage("** Attempting to stop zookeeper ...", MessageType.ACTION, true);

			new File(ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + zooCommand)
					.setExecutable(true);
			proc = rt.exec(ZOOKEEPER_DIR + "zookeeper-" + ZOOKEEPER_RELEASE + File.separator + zooCommand + " stop");

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();

			if (proc.exitValue() == 0) {
				this.stopped = true;
			} else {
				this.stopped = false;
			}

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public int clean() throws IOException, InterruptedException {

		Util.postMessage("Deleting directory for zookeeper data ", MessageType.ACTION, true);
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

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}

	public void postData(String collectionName)
			throws IOException, InterruptedException, SolrServerException {

		Util.postMessage("** Posting data to the node ... ", MessageType.ACTION, true);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
			solr.connect();
			solr.setDefaultCollection(collectionName);
			SolrInputDocument document;

			for (int i = 1; i <= TEST_DOCUMENTS_COUNT; i++) {

				document = new SolrInputDocument();
				document.setField("EMP_ID", "EMP_ID@" + i);
				document.setField("TITLE", "TITLE@" + i);
				solr.add(collectionName, document);
				if (i % 10 == 0) {
					Util.postMessageOnLine("|");
				}
			}
			Util.postMessage("", MessageType.GENERAL, false);
			Util.postMessage("Added data into the cluster ...", MessageType.RESULT_SUCCESS, true);
			solr.commit();
			solr.close();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

	}

	public boolean verifyData(String collectionName)
			throws IOException, InterruptedException, SolrServerException {

		Util.postMessage("** Getting the data from nodes ... ", MessageType.RESULT_ERRROR, true);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
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
					Util.postMessage("%%%% DATA CORRUPTED, returning false  %%%%", MessageType.RESULT_ERRROR, true);
					return false;
				}
				count++;
				if (count % 10 == 0) {
					Util.postMessageOnLine("|");
				}
			}
			Util.postMessage("", MessageType.GENERAL, false);

			if (count != TEST_DOCUMENTS_COUNT) {
				Util.postMessage("%%%% DATA COUNT MISMATCH, returning false  %%%%", MessageType.RESULT_ERRROR, true);
				solr.close();
				return false;
			}

			solr.close();
			return true;

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

		}

	}

	public void deleteData(String collectionName)
			throws IOException, InterruptedException, SolrServerException {

		Util.postMessage("** Deleting data from the nodes ... ", MessageType.ACTION, true);
		CloudSolrClient solr = null;
		try {

			solr = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
			solr.connect();
			solr.setDefaultCollection(collectionName);
			solr.deleteByQuery("*:*");
			solr.close();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}
	}

	public String getZookeeperIp() {
		return zookeeperIp;
	}

	public void setZookeeperIp(String zookeeperIp) {
		this.zookeeperIp = zookeeperIp;
	}

	public String getZookeeperPort() {
		return zookeeperPort;
	}

	public void setZookeeperPort(String zookeeperPort) {
		this.zookeeperPort = zookeeperPort;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}	 

}
