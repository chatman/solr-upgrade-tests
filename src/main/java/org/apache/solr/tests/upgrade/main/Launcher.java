package org.apache.solr.tests.upgrade.main;

import java.io.IOException;

import org.apache.solr.tests.upgrade.workflow.TestWorkFlowImpl;

public class Launcher {

	public static void main(String[] args) throws IOException, InterruptedException {

		TestWorkFlowImpl m = new TestWorkFlowImpl("5.4.0","5.4.1");
		
	}

}
