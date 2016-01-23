package org.apache.solr.tests.upgrade.interfaces;

import java.io.IOException;

public interface Executor extends Directory, Messages {

	public void startSOLRNode(String node, String version, String port) throws IOException, InterruptedException;
	
	public void stopSOLRNode(String node, String version, String port) throws IOException, InterruptedException;	
	
	public void startZooKeeper(String version) throws IOException, InterruptedException;
	
	public void stopZooKeeper(String version) throws IOException, InterruptedException;
	
	public void loadData();

}