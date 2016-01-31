package org.apache.solr.tests.solrupgradetests;

import java.io.File;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class ZookeeperNode extends SolrUpgradeTestsUtil {

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

		this.postMessage("** Getting the data from nodes ... ", MessageType.ACTION, true);
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

}
