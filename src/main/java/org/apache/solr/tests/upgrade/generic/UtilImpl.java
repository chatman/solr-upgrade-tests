package org.apache.solr.tests.upgrade.generic;

import org.apache.solr.tests.upgrade.interfaces.Util;

public class UtilImpl implements Util{

	@Override
	public void postMessage(String message) {
			System.out.println(message);
	}	
	
}
