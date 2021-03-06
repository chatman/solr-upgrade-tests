package org.apache.solr.tests.upgradetests;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.lib.Ref;

public class SolrNode {

	final static Logger logger = Logger.getLogger(SolrNode.class);
	public static final String URL_BASE = "http://archive.apache.org/dist/lucene/solr/";

	private String nodeDirectory;
	private String port;
	private String version;
	private String patchUrl;
	private static String solrCommand;
	private String zooKeeperIp;
	private String zooKeeperPort;

	static {

		solrCommand = System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows")
				? "bin" + File.separator + "solr.cmd" : "bin" + File.separator + "solr";

	}

	private String gitDirectoryPath = SolrRollingUpgradeTests.TEMP_DIR + "git-repository";

	public SolrNode(String version, String patchUrl, String zooKeeperIp, String zooKeeperPort) throws IOException, GitAPIException {
		super();
		this.version = version;
		this.zooKeeperIp = zooKeeperIp;
		this.zooKeeperPort = zooKeeperPort;
		this.patchUrl = patchUrl;
		this.install();
	}

	private void install() throws IOException, GitAPIException {

		Util.postMessage("** Installing Solr Node ...", MessageType.ACTION, true);

		this.nodeDirectory = SolrRollingUpgradeTests.BASE_DIR + UUID.randomUUID().toString() + File.separator;
		this.port = String.valueOf(getFreePort());

		try {
			Util.postMessage("** Checking if SOLR node directory exists ...", MessageType.ACTION, true);
			File node = new File(nodeDirectory);

			if (!node.exists()) {
				Util.postMessage("Node directory does not exist, creating it ...", MessageType.ACTION, true);
				node.mkdir();
				Util.postMessage("Directory Created: " + nodeDirectory, MessageType.RESULT_SUCCESS, true);
			}
		} catch (Exception e) {
			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
		}

		File release = new File(SolrRollingUpgradeTests.TEMP_DIR + "solr-" + version + ".zip");
		if (!release.exists()) {
			if (Character.isDigit(version.charAt(0)) && version.length() == 5) { // must be a release version, e.g. "6.4.0"
				download(version);
			} else { // assuming this is a git branch/commit
				checkoutCommitAndBuild(version);
			}
		}

		String baseVersion = getBaseVersion(gitDirectoryPath + "/lucene/version.properties");
		String zipFile = (patchUrl==null) ? SolrRollingUpgradeTests.TEMP_DIR + "solr-" + version + ".zip":
			SolrRollingUpgradeTests.TEMP_DIR + "solr-" + version + "-" + Util.md5(patchUrl) + ".zip";
		String extractedDir = (patchUrl==null) ? SolrRollingUpgradeTests.TEMP_DIR + "solr-" + version:
			SolrRollingUpgradeTests.TEMP_DIR + "solr-" + version + "-" + Util.md5(patchUrl);

		System.out.println("Checking for presence of directory: " + extractedDir);

		File uzrelease = new File(extractedDir);
		if (!uzrelease.exists()) {
			Util.extract(zipFile, extractedDir);
			for (File file: new File(extractedDir + "/solr-"+baseVersion).listFiles()) {
				String src = file.getAbsolutePath();
				String dest = extractedDir;
				System.out.println("Moving "+src+ " to "+dest);
				if (file.isDirectory()) {
					FileUtils.moveDirectoryToDirectory(file, new File(dest), true);
				} else {
					FileUtils.moveFileToDirectory(file, new File(dest), true);
				}
			}
		}

		File node = new File(nodeDirectory + "solr-" + version);
		node.mkdir();
		FileUtils.copyDirectory(new File(extractedDir), node);
	}

	String getBaseVersion(String versionFile) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File(versionFile)));
		return prop.getProperty("version.base") + "-" + prop.getProperty("version.suffix");
	}
	void download(String version) {
		String fileName = null;
		URL link = null;
		InputStream in = null;
		FileOutputStream fos = null;

		try {

			fileName = "solr-" + version + ".zip";
			String url = URL_BASE + version + File.separator + fileName;
			Util.postMessage("** Attempting to download release ..." + " " + version + " from " + url,
					MessageType.ACTION, true);
			link = new URL(url);

			in = new BufferedInputStream(link.openStream());
			fos = new FileOutputStream(SolrRollingUpgradeTests.TEMP_DIR + fileName);
			byte[] buf = new byte[1024 * 1024]; // 1mb blocks
			int n = 0;
			long size = 0;
			while (-1 != (n = in.read(buf))) {
				size += n;
				Util.postMessageOnLine("\r" + size + " ");
				fos.write(buf, 0, n);
			}
			fos.close();
			in.close();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}
	}

	void checkoutCommitAndBuild (String commit) throws IOException, GitAPIException {
		Util.postMessage("** Checking out Solr: "+commit+" ...", MessageType.ACTION, true);

		File gitDirectory = new File(gitDirectoryPath);

		Git repository;

		if (gitDirectory.exists()) {
			repository = Git.open(gitDirectory);
			
			repository.stashCreate().call(); // drop all local changes

			repository.checkout()
			.setName(commit)
			.call();

		} else {
			repository = Git.cloneRepository()
					.setURI("https://github.com/apache/lucene-solr")
					.setDirectory(gitDirectory)
					.call();
			repository.checkout()
			.setName(commit)
			.call();
		}

		if (patchUrl != null) {
			InputStream in = new URL(patchUrl).openStream();
			String patch = IOUtils.toString(in, StandardCharsets.UTF_8);
			
			try {
				ApplyResult apply = repository.apply().setPatch(new ByteArrayInputStream(patch.getBytes(StandardCharsets.UTF_8))).call();
				System.out.println("Patch applied? " + apply);
			} catch (PatchApplyException ex) {
				ex.printStackTrace();
				String patchFile = "/tmp/"+Util.md5(patchUrl)+".patch";
				FileUtils.write(new File(patchFile), patch);
				int status = Util.execute("git apply " + patchFile, gitDirectoryPath);
				System.out.println("Patch applied? Status="+status);
				Status statusResult = repository.status().call();
				System.out.println("Has changes? "+statusResult.hasUncommittedChanges());
				if (statusResult.hasUncommittedChanges()==false) {
					throw new IOException("Patch couldn't be applied. Status now: "+statusResult);
				}
			}
		}

		String baseVersion = getBaseVersion(gitDirectoryPath + "/lucene/version.properties");
		String packageFilename = gitDirectoryPath + "/solr/package/solr-"+baseVersion+".zip";
		String tarballLocation = (patchUrl == null)? SolrRollingUpgradeTests.TEMP_DIR+"solr-"+commit+".zip":
			SolrRollingUpgradeTests.TEMP_DIR + "solr-" + commit + "-" + Util.md5(patchUrl) + ".zip";

		if (new File(tarballLocation).exists() == false) {
			Util.postMessage("** There were new changes, need to rebuild ...", MessageType.ACTION, true);
			Util.execute("ant ivy-bootstrap", gitDirectoryPath);
			Util.execute("ant clean compile", gitDirectoryPath);
			Util.execute("ant package", gitDirectoryPath + File.separator + "solr");

			if (new File(packageFilename).exists()) {
				System.out.println("Trying to copy: "+packageFilename + " to "+tarballLocation);
				Files.copy(Paths.get(packageFilename), Paths.get(tarballLocation));
				System.out.println("File copied!");
			} else {
				throw new IOException("Couldn't build the package"); // nocommit fix, better exception
			}
		}

		Util.postMessage("** Do we have packageFilename? "+(new File(tarballLocation).exists()? "yes": "no")+" ...", MessageType.ACTION, true);
	}

	@SuppressWarnings("finally")
	public int start() {

		Util.postMessage("** Starting Solr Node ...", MessageType.ACTION, true);

		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			new File(nodeDirectory + "solr-" + version + File.separator + solrCommand).setExecutable(true);
			proc = rt.exec(nodeDirectory + "solr-" + version + File.separator + solrCommand + " start "
					+ "-p " + port
					+ " -m 4g"
					+ " -z " + zooKeeperIp + ":" + zooKeeperPort);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		} finally {

			return proc.exitValue();

		}

	}

	@SuppressWarnings("finally")
	public int stop() {

		Util.postMessage("** Stopping Solr Node ...", MessageType.ACTION, true);
		return Util.execute(solrCommand + " stop -p " + port
				+ " -z " + zooKeeperIp + ":" + zooKeeperPort, nodeDirectory + "solr-" + version);
	}

	public int createCollection(String collectionName, String shards, String replicationFactor) throws IOException, InterruptedException {
		return createCollection(collectionName, null, shards, replicationFactor);
	}

	public int createCollection(String collectionName, String configName, String shards, String replicationFactor)
			throws IOException, InterruptedException {

		Util.postMessage("** Creating collection, configuring shards and replication factor ... ", MessageType.ACTION,
				true);
		
		if (configName != null) {
			return Util.execute(solrCommand + " create_collection "
					+ "-c " + collectionName
					+ " -shards " + shards
					+ " -n " + configName 
					+ " -replicationFactor " + replicationFactor, nodeDirectory + "solr-" + version);
		} else {
			return Util.execute(solrCommand + " create_collection -c "
					+ collectionName + " -shards " + shards + " -replicationFactor " + replicationFactor, nodeDirectory + "solr-" + version);
		}

	}

	public void upgrade(String toVersion) throws IOException, InterruptedException {


		File release = new File(SolrRollingUpgradeTests.TEMP_DIR + "solr-" + toVersion + ".zip");
		if (!release.exists()) {

			String fileName = null;
			URL link = null;
			InputStream in = null;
			FileOutputStream fos = null;

			try {

				fileName = "solr-" + toVersion + ".zip";
				String url = URL_BASE + toVersion + File.separator + fileName;
				Util.postMessage("** Attempting to download release ..." + " " + toVersion + " from " + url,
						MessageType.ACTION, true);
				link = new URL(url);

				in = new BufferedInputStream(link.openStream());
				fos = new FileOutputStream(SolrRollingUpgradeTests.TEMP_DIR + fileName);
				byte[] buf = new byte[1024 * 1024]; // 1mb blocks
				int n = 0;
				long size = 0;
				while (-1 != (n = in.read(buf))) {
					size += n;
					Util.postMessageOnLine("\r" + size + " ");
					fos.write(buf, 0, n);
				}
				fos.close();
				in.close();

			} catch (Exception e) {

				Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

			}
		}

		File uzrelease = new File(SolrRollingUpgradeTests.TEMP_DIR + "solr-" + toVersion);
		if (!uzrelease.exists()) {

			ZipInputStream zipIn = null;

			try {

				Util.postMessage("** Attempting to unzip the downloaded release ...", MessageType.ACTION, true);
				zipIn = new ZipInputStream(
						new FileInputStream(SolrRollingUpgradeTests.TEMP_DIR + "solr-" + toVersion + ".zip"));
				ZipEntry entry = zipIn.getNextEntry();
				while (entry != null) {
					String filePath = SolrRollingUpgradeTests.TEMP_DIR + File.separator + entry.getName();
					if (!entry.isDirectory()) {
						Util.postMessage("Unzipping to : " + SolrRollingUpgradeTests.TEMP_DIR + " : " + entry.getName(),
								MessageType.ACTION, true);
						Util.extractFile(zipIn, filePath);
					} else {
						File dirx = new File(filePath);
						dirx.mkdir();
					}
					zipIn.closeEntry();
					entry = zipIn.getNextEntry();
				}
				zipIn.close();

			} catch (Exception e) {

				Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

			}

		} 

		Thread.sleep(1000);

		this.stop();

		Util.postMessage("** Attempting upgrade on the node by replacing lib folder ..." + "From: " + version + " To: "
				+ toVersion, MessageType.ACTION, true);
		try {
			String upgradeLocations[] = {
					File.separator + "server" + File.separator + "lib",
					File.separator + "server" + File.separator + "solr-webapp" + File.separator + "webapp"
							+ File.separator + "WEB-INF" + File.separator + "lib"};
			for (String localPath: upgradeLocations) {
				File src = new File(SolrRollingUpgradeTests.TEMP_DIR + "solr-" + toVersion + localPath);
				File dest = new File(nodeDirectory + "solr-" + version + localPath);

				FileUtils.cleanDirectory(dest);
				FileUtils.copyDirectory(src, dest);
			}
			Util.postMessage("Upgrade process complete ... ", MessageType.RESULT_SUCCESS, true);
		} catch (Exception e) {
			Util.postMessage("Upgrade failed due to some reason ...", MessageType.RESULT_ERRROR, true);
			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}

		this.start();

	}

	private static int getFreePort() {

		int port = ThreadLocalRandom.current().nextInt(10000, 60000);
		Util.postMessage("Looking for a free port ... Checking availability of port number: " + port,
				MessageType.ACTION, true);
		ServerSocket serverSocket = null;
		DatagramSocket datagramSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			datagramSocket = new DatagramSocket(port);
			datagramSocket.setReuseAddress(true);
			Util.postMessage("Port " + port + " is free to use. Using this port !!", MessageType.RESULT_SUCCESS, true);
			return port;
		} catch (IOException e) {
		} finally {
			if (datagramSocket != null) {
				datagramSocket.close();
			}

			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
				}
			}
		}

		Util.postMessage("Port " + port + " looks occupied trying another port number ... ", MessageType.RESULT_ERRROR,
				true);
		return getFreePort();
	}

	@SuppressWarnings("finally")
	public int clean() {

		Util.postMessage("Deleting node ... ", MessageType.ACTION, true);
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec("rm -r -f " + nodeDirectory);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		} finally {

			return proc.exitValue();

		}

	}

	public String getNodeDirectory() {
		return nodeDirectory;
	}

	public String getBaseUrl() {
		return "http://localhost:"+port+"/solr";
	}
}
