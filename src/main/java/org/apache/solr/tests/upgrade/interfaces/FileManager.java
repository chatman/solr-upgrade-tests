package org.apache.solr.tests.upgrade.interfaces;

import java.io.IOException;

import org.apache.solr.tests.upgrade.interfaces.Util.ReleaseNames;

public interface FileManager extends Messages, Directory {

		
	public boolean createBaseDir();
	
	public boolean createTempDir();
	
	public boolean createNDir();

	public boolean downloadRelease(String version, String dir, ReleaseNames what) throws IOException;
	
	public boolean verifyDownload(String version, String dir);
	
	public boolean unZipDownloadedRelease(String dir, String destinationDir) throws IOException;
	
	public boolean unGZipDownloadedRelease(String dir, String destinationDir) throws IOException;
	
}
