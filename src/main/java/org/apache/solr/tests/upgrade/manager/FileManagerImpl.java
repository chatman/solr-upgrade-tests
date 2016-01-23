package org.apache.solr.tests.upgrade.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.solr.tests.upgrade.generic.UtilImpl;
import org.apache.solr.tests.upgrade.interfaces.FileManager;

public class FileManagerImpl extends UtilImpl implements FileManager {
	
	public FileManagerImpl() {
		
		if (createBaseDir()) {
			this.postMessage(DIR_CREATED);
		}
		
		if (createNDir()) {
			this.postMessage(DIR_CREATED);
		}
		
		if (createTempDir()) {
			this.postMessage(DIR_CREATED);
		}
		
	}

	@Override
	public boolean downloadRelease(String version, String dir, ReleaseNames what) throws IOException {

		 String fileName = null;
		 URL link = null;
		
		 if (what.equals(ReleaseNames.SOLR)) {
			 this.postMessage(DOWNLOADING_RELEASE + " : " + version);
			 fileName = "solr-" + version + ".zip"; //The file that will be saved on your computer
			 link = new URL(URL_BASE + "/" + version + "/" + fileName); //The file that you want to download
		 } else if (what.equals(ReleaseNames.ZOOKEEPER)) {
			 this.postMessage(DOWNLOADING_ZOO_RELEASE + " : " + version);
			 fileName = "zookeeper-" + version + ".tar.gz"; //The file that will be saved on your computer
			 link = new URL(ZOO_URL_BASE + "zookeeper-" + version + "/" + fileName); //The file that you want to download 
		 }
		
		 //Code to download
		 InputStream in = new BufferedInputStream(link.openStream());
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 byte[] buf = new byte[1024];
		 int n = 0;
		 while (-1!=(n=in.read(buf)))
		 {
			this.postMessage("" + out.size()); 
		    out.write(buf, 0, n);
		 }
		 out.close();
		 in.close();
		 byte[] response = out.toByteArray();
 
		 FileOutputStream fos = new FileOutputStream(TEMP_DIR + fileName);
		 fos.write(response);
		 fos.close();
		 //End download code
		 
		 this.postMessage(OK_MSG_GENERAL);
		return false;
	}

	@Override
	public boolean unZipDownloadedRelease(String dir, String destinationDir) throws IOException {
		
		this.postMessage(UNZIP_RELEASE);
	
		
		File destDir = new File(destinationDir);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(dir));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destinationDir + File.separator + entry.getName();
            if (!entry.isDirectory()) {
            	this.postMessage(UNZIPPING_TO + destinationDir + " : " +  entry.getName());
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dirx = new File(filePath);
                dirx.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        
        this.postMessage(OK_MSG_GENERAL);
        
		return false;
	} 
	
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

	@Override
	public boolean verifyDownload(String version, String dir) {
		
		return false;
	}

	@Override
	public boolean createBaseDir() {
		File baseDir = new File(BASE_DIR);
		this.postMessage(CHECKING_BDIR);
		if (!baseDir.exists()) {
			this.postMessage(CREATING_BDIR);
			return baseDir.mkdir();
		}
		return false;
	}

	@Override
	public boolean createNDir() {
		this.postMessage(CHECKING_NDIR);
		File n1 = new File(NODE_ONE_DIR);
		File n2 = new File(NODE_TWO_DIR);
		File n3 = new File(NODE_THREE_DIR);
		boolean mn1 = false, mn2 = false , mn3 = false;
		
		if (!n1.exists()) {
			this.postMessage(CREATING_NDIR);
			mn1 = n1.mkdir();
		}
		
		if (!n2.exists()) {
			this.postMessage(CREATING_NDIR);
			mn2 = n2.mkdir();
		}
		
		if (!n3.exists()) {
			this.postMessage(CREATING_NDIR);
			mn3 = n3.mkdir();
		}
		
		return (mn1 && mn2 && mn3);
	}

	@Override
	public boolean createTempDir() {
		this.postMessage(CHECKING_TDIR);
		File tempDir = new File(TEMP_DIR);
		if (!tempDir.exists()) {
			this.postMessage(CREATING_TDIR);
			return tempDir.mkdir();
		}
		return false;
	}

	@Override
	public boolean unGZipDownloadedRelease(String dir, String destinationDir) throws IOException {	
		
		//TO-DO       
        return false;
	}
	
}