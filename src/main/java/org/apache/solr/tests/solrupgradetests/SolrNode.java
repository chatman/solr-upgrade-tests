package org.apache.solr.tests.solrupgradetests;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class SolrNode extends SolrUpgradeTestsUtil {

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

}
