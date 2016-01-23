package org.apache.solr.tests.upgrade.workflow;

import java.io.IOException;

import org.apache.solr.tests.upgrade.executor.ExecutorImpl;
import org.apache.solr.tests.upgrade.generic.UtilImpl;
import org.apache.solr.tests.upgrade.interfaces.Directory;
import org.apache.solr.tests.upgrade.interfaces.Messages;
import org.apache.solr.tests.upgrade.manager.FileManagerImpl;

public class TestWorkFlowImpl extends UtilImpl implements Directory {

	UtilImpl util;
	FileManagerImpl fmi;
	ExecutorImpl ei;

	public TestWorkFlowImpl(String version1, String version2) throws IOException, InterruptedException {

		this.postMessage(Messages.HELLO);
		this.postMessage("");

		if (fmi == null) {
			fmi = new FileManagerImpl();
		}

		if (ei == null) {
			ei = new ExecutorImpl();
		}

		//fmi.downloadRelease("3.4.6", TEMP_DIR, ReleaseNames.ZOOKEEPER);
		
		//fmi.unZipDownloadedRelease(TEMP_DIR + "solr-5.4.0.zip", NODE_ONE_DIR);
		//fmi.unZipDownloadedRelease(TEMP_DIR + "solr-5.4.0.zip", NODE_TWO_DIR);
		//fmi.unZipDownloadedRelease(TEMP_DIR + "solr-5.4.0.zip", NODE_THREE_DIR);
		//fmi.unGZipDownloadedRelease(TEMP_DIR + "zookeeper-3.4.6.tar.gz", TEMP_DIR);
		
		
		
		try {
			ei.startZooKeeper("3.4.6");
			///ei.startSOLRNode("N1", "5.4.0", "1235");
			///ei.startSOLRNode("N2", "5.4.0", "1236");
			///ei.startSOLRNode("N3", "5.4.0", "1237");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
			//ei.stopSOLRNode("N1", "5.4.0", "1235");
			//ei.stopSOLRNode("N2", "5.4.0", "1236");
			//ei.stopSOLRNode("N3", "5.4.0", "1237");
		
	}
	
}