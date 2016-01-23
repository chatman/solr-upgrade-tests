package org.apache.solr.tests.upgrade.interfaces;

public interface Util {

	public enum ReleaseNames { SOLR,ZOOKEEPER };
	
	public void postMessage(String message);
	
}
