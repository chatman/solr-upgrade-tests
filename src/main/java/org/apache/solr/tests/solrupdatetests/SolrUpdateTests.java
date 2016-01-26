package org.apache.solr.tests.solrupdatetests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.ZkStateReader;


class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println("SubProcess Output >> " + type + " >> " + line);    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}


public class SolrUpdateTests {

	public final String URL_BASE = "http://archive.apache.org/dist/lucene/solr/";
	
	public final String ZOO_URL_BASE = "http://www.us.apache.org/dist/zookeeper/";
	
	public String WORK_DIRECTORY = "C:";
	
	public final String DNAME = "SOLRUpdateTests";
	
	public final String BASE_DIR = WORK_DIRECTORY + "/" + DNAME + "/";
	
	public final String TEMP_DIR = BASE_DIR + "temp/";
	
	public final String NODE_ONE_DIR = BASE_DIR + "N1/";
	
	public final String NODE_TWO_DIR = BASE_DIR + "N2/";
	
	public final String NODE_THREE_DIR = BASE_DIR + "N3/";

	public final String HELLO = "[SOLR UPDATE TESTS] HOLA !!!";
	
	public final String CHECKING_BDIR = "Checking if base directory exists ...";
	
	public final String CHECKING_NDIR = "Checking if SOLR node directory exists ...";
	
	public final String CHECKING_TDIR = "Checking if temp directory exists ...";
	
	public final String CREATING_BDIR = "Base directory does not exist, creating one ...";
	
	public final String CREATING_NDIR = "Node directory does not exist, creating it ...";
	
	public final String CREATING_TDIR = "Temp directory does not exist Creating Temp directory ...";
	
	public final String OK_MSG_GENERAL = "[OK] ...";
	
	public final String DIR_CREATED = "Directory Successfully Created !";
	
	public final String DOWNLOADING_RELEASE = "Attempting to download release ...";
	
	public final String DOWNLOADING_ZOO_RELEASE = "Attempting to download zookeeper release ...";
	
	public final String UNZIP_RELEASE = "Attempting to unzip the downloaded release ...";
	
	public final String UNZIPPING_TO = "Unzipping to : ";
	
	public final String START_PROC = "Attempting to start solr node ...";
	
	public final String START_ZOO = "Attempting to start zookeeper ...";
	
	public final String STOP_ZOO = "Attempting to stop zookeeper ...";

	public final String STARTED_ZOO = "Zookeeper Started ...";
	
	public final String STOPPED_ZOO = "Zookeeper Started ...";
	
	public final String STOP_PROC = "Attempting to stop solr node ...";
	
	public final String NODE_STARTED = "Node Started ... ";
	
	public final String NODE_STOPPED = "Node Stopped ... ";
	
	public final String CHECK_RELEASE_DOWNLOADED = "Checking if release has been downloaded ...";
	
	public final String RELEASE_PRESENT = "Release is present ...";
	
	public final String RELEASE_DOWNLOAD = "Release not present ! Release has to be downloaded ... ";
	
	public final String BAD_RELEASE_NAME = "Internet Connection Failure OR Release not present OR BAD Release name ... [EXITING]";
	
	public final String ARG_VERSION_ONE = "-v1";
	
	public final String ARG_VERSION_TWO = "-v2";

	public final String ARG_SKIPCLEAN = "-SkipClean";
	
	public final String ARG_SKIPUNZIP = "-SkipUnzip";
	
	public final String ARG_TESTTYPE = "-TestType";
	
	public final String ARG_WORK_DIR = "-WorkDirectory";
	
	public final String CREATING_COLLECTION = "Creating collection, configuring shards and replication factor ... ";
	
	public final String POSTING_DATA = "Posting data to the node ... ";
	
	public final String GETTING_DATA = "Getting the data from nodes ... ";
	
	public final String VERIFYING_DATA = "Verifying data from nodes ... ";
	
	public final String DATA_OK = "Data is verified and seems okay ...";
	
	public final String DATA_NOT_OK = "Data has been corrupted by this migration ... ";
	
	public final String ARG_ERROR_VERSION_NULL = "SOLRUpdateTests Says: Need two SOLR versions to conduct this test ...";
	
	public final String ARG_ERROR_VERSION_SAME = "SOLRUpdateTests Says: Comparing same versions is not useful, please provide two versions ...";
	
	public final String ARG_PORT_ONE = "-N1Port";
	
	public final String ARG_PORT_TWO = "-N2Port";
	
	public final String ARG_PORT_THREE = "-N3Port";
	
	public final String ARG_COLLECTION_NAME = "-CollectionName";
	
	public final String HELP_L1 = "This testing program requires following parameters to run ... ";
	
	public final String HELP_L2 = "-v1 {From Version: ex '5.4.0'}; -v2 {To Version: ex '5.4.1'} ";

	public final String HELP_L3 = "-SkipClean: TRUE/FALSE {To clean the node directories or not}";

	public final String HELP_L4 = "-SkipUnzip: TRUE/FALSE {To unzip the releases or not} ";
	
	public final String HELP_L5 = "-N1Port/-N2Port/-N3Port: {Port number for three nodes, must be different. ex '1234'}";
	
	public final String HELP_L6 = "-WorkDirectory: {Define a working directory on your system ex '/home'}";
	
	public final String PORT_MISSING = "Port for each node is missing please define them through {-N1Port, -N2Port & -N3Port}";
	
	public enum ReleaseType { SOLR,ZOOKEEPER };
	
	public enum Action {START,STOP,ADD,UPDATE,DELETE,VERIFY};
	
	public enum Location { TEMP,NODE_ONE, NODE_TWO, NODE_THREE }
	
	public enum Type { COMPRESSED, EXTRACTED }
	
	public enum OSType {   Windows, MacOS, Linux, Other };
	
	public final int TEST_DOCUMENTS_COUNT = 1000;
	
	public final String NUM_SHARDS = "2";
	
	public final String NUM_REPLICAS = "3";
	
	public String COLLECTION_NAME = "TestCollection";
	
	public String portOne = "1234";
	
	public String portTwo = "1235";
	
	public String portThree = "1236";

	public void postMessage(String message) {
			System.out.println(message);
	}	
	
	public boolean downloadRelease(String version, String dir, ReleaseType what) throws IOException {

		 String fileName = null;
		 URL link = null;
		
		 if (what.equals(ReleaseType.SOLR)) {
			 this.postMessage(DOWNLOADING_RELEASE + " : " + version);
			 fileName = "solr-" + version + ".zip"; 
			 link = new URL(URL_BASE + "/" + version + "/" + fileName); 
		 } else if (what.equals(ReleaseType.ZOOKEEPER)) {
			 this.postMessage(DOWNLOADING_ZOO_RELEASE + " : " + version);
			 fileName = "zookeeper-" + version + ".tar.gz"; 
			 link = new URL(ZOO_URL_BASE + "zookeeper-" + version + "/" + fileName);  
		 }
		
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
		 
		return false;
	}

	public boolean unZipDownloadedRelease(String dir, String destinationDir) throws IOException {
		
		this.postMessage(UNZIP_RELEASE);
		
		File destDir = new File(destinationDir);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(dir));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = destinationDir + File.separator + entry.getName();
            if (!entry.isDirectory()) {
            	this.postMessage(UNZIPPING_TO + destinationDir + " : " +  entry.getName());
                extractFile(zipIn, filePath);
            } else {
                File dirx = new File(filePath);
                dirx.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        
        
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

	public boolean createBaseDir() {
		
		File baseDir = new File(BASE_DIR);
				this.postMessage(CHECKING_BDIR);
				if (!baseDir.exists()) {
					this.postMessage(CREATING_BDIR);
					return baseDir.mkdir();
				}
		return false;
		
	}

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

	public boolean createTempDir() {
			this.postMessage(CHECKING_TDIR);
				File tempDir = new File(TEMP_DIR);
					if (!tempDir.exists()) {
							this.postMessage(CREATING_TDIR);
							return tempDir.mkdir();
					}
			return false;
	}
	
	public void doActionOnSolrNode(String node, String version, String port, Action action) throws IOException, InterruptedException {
		
		
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        String act = null;
        
        if (action.equals(Action.START)) {
        	act = "start";
    		this.postMessage(START_PROC + " : " + node);
        } else if (action.equals(Action.STOP)) {
        	act = "stop";
    		this.postMessage(STOP_PROC + " : " + node);
        }        
        
        if ("N1".equals(node)) {
        	proc = rt.exec(NODE_ONE_DIR + "solr-"+ version + "/bin/solr.cmd " + act + " -p " + port + " -z localhost:2181");
        } else if ("N2".equals(node)) {
        	proc = rt.exec(NODE_TWO_DIR + "solr-"+ version + "/bin/solr.cmd " + act + " -p " + port + " -z localhost:2181");

        } else if ("N3".equals(node)) {
        	proc = rt.exec(NODE_THREE_DIR + "solr-"+ version + "/bin/solr.cmd " + act + " -p " + port + " -z localhost:2181");
        }
        
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            
        errorGobbler.start();
        outputGobbler.start();
        proc.waitFor();

		}

		public void createSOLRCollection(String node, String version, String collectionName, String shards, String replicationFactor) throws IOException, InterruptedException {
		
		this.postMessage(CREATING_COLLECTION + " : " + node);
		
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
     
        
        if (this.getOperatingSystemType().equals(OSType.Windows)) {
		
        		if ("N1".equals(node)) {
		        	proc = rt.exec(NODE_ONE_DIR + "solr-"+ version + "/bin/solr.cmd create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
		        } else if ("N2".equals(node)) {
		        	proc = rt.exec(NODE_TWO_DIR + "solr-"+ version + "/bin/solr.cmd create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
		        } else if ("N3".equals(node)) {
		        	proc = rt.exec(NODE_THREE_DIR + "solr-"+ version + "/bin/solr.cmd create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
		        }
        
        } else if (this.getOperatingSystemType().equals(OSType.Linux)) {
        	
    		if ("N1".equals(node)) {
	        	proc = rt.exec(NODE_ONE_DIR + "solr-"+ version + "/bin/solr create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
	        } else if ("N2".equals(node)) {
	        	proc = rt.exec(NODE_TWO_DIR + "solr-"+ version + "/bin/solr create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
	        } else if ("N3".equals(node)) {
	        	proc = rt.exec(NODE_THREE_DIR + "solr-"+ version + "/bin/solr create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
	        }
        	
        }
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            
        errorGobbler.start();
        outputGobbler.start();
        proc.waitFor();

		}
		
		
		public void postData(String collectionName) throws IOException, InterruptedException, SolrServerException {
			
			this.postMessage(POSTING_DATA);
			
	        CloudSolrClient solr = new CloudSolrClient("Localhost:2181");
	        solr.connect();
	        
	        solr.setDefaultCollection(collectionName);
	        SolrInputDocument document;
	        
	        for (int i = 1; i <= TEST_DOCUMENTS_COUNT; i++) {
	        	
	        	document = new SolrInputDocument();
	        	document.setField("EMP_ID", "EMP_ID@"+i);
	        	document.setField("TITLE", "TITLE@"+i);

	        	this.postMessage("Adding" + i);
	        	solr.add(collectionName, document);
	        }
	        solr.commit();
	        solr.close();
		}
		
		
		public boolean verifyData(String collectionName) throws IOException, InterruptedException, SolrServerException {
			
			this.postMessage(GETTING_DATA);
			
	        CloudSolrClient solr = new CloudSolrClient("Localhost:2181");
	        solr.connect();	        
	        solr.setDefaultCollection(collectionName);
	        
	        SolrQuery query = new SolrQuery("*:*");
	        query.setRows(10000);
	        SolrDocumentList docList = solr.query(query).getResults();
	        int count = 0;
	        for(SolrDocument document: docList) {
	        	if (!(document.getFieldValue("TITLE").toString().split("@", 2)[1].equals(document.getFieldValue("EMP_ID").toString().split("@", 2)[1]))) {
	        		solr.close();
	        		return false;
	        	}
	        	count++;
	        }
	        
	        if (count != TEST_DOCUMENTS_COUNT) {
	        	solr.close();
	        	return false;
	        }
	        
	        solr.close();
	        return true;
		}
		
		public void deleteData(String collectionName) throws IOException, InterruptedException, SolrServerException {
			
			this.postMessage(GETTING_DATA);
			
	        CloudSolrClient solr = new CloudSolrClient("Localhost:2181");
	        solr.connect();	        
	        solr.setDefaultCollection(collectionName);	
	        
	        SolrQuery query = new SolrQuery("*:*");
	        query.setRows(10000);
	        SolrDocumentList docList = solr.query(query).getResults();
	        
	        for(SolrDocument document: docList) {
	        	this.postMessage(document.toString());
	        	solr.deleteById(document.getFieldValue("id").toString());
	        }
	        
	        this.postMessage("" + docList.size());
	        
	        solr.close();
		}

		
		public void upgradeSolr(String versionOne, String versionTwo, String node) throws IOException {

			File src = new File(TEMP_DIR + "solr-" + versionTwo + "/server/solr-webapp/webapp/WEB-INF/lib");
			File dest = null;
			if ("N1".equals(node)) {
					dest = new File(NODE_ONE_DIR + "solr-" + versionOne + "/server/solr-webapp/webapp/WEB-INF/lib");
			} else if ("N2".equals(node)) {
					dest = new File(NODE_TWO_DIR + "solr-" + versionOne + "/server/solr-webapp/webapp/WEB-INF/lib");
			} else if ("N3".equals(node)) {
					dest = new File(NODE_THREE_DIR + "solr-" + versionOne + "/server/solr-webapp/webapp/WEB-INF/lib");
			}	

			FileUtils.cleanDirectory(dest);
			copyFolder(src, dest);			
			
		}
		
		
		public void copyFolder(File src, File dest)
		    	throws IOException{
			
		    	
		    	if(src.isDirectory()){
		    		
						    		if(!dest.exists()){
						    		   dest.mkdir();
						    		   this.postMessage("Directory copied from " + src + "  to " + dest);
						    		}
		    		
						    		String files[] = src.list();
		    		
						    		for (String file : files) {
						    		   File srcFile = new File(src, file);
						    		   File destFile = new File(dest, file);
						    		   copyFolder(srcFile,destFile);
						    		}
		    	   
							    	}else{
							    			InputStream in = new FileInputStream(src);
							    	        OutputStream out = new FileOutputStream(dest); 
							    	                     
							    	        byte[] buffer = new byte[1024];
							    	        int length;
							    	        while ((length = in.read(buffer)) > 0){
							    	    	   out.write(buffer, 0, length);
							    	        }
							 
							    	        in.close();
							    	        out.close();
							    	        this.postMessage("File copied from " + src + " to " + dest);
							    	}
		    }

		public boolean checkForRelease(String version, ReleaseType name, Location location, Type type) {
		
		this.postMessage(CHECK_RELEASE_DOWNLOADED + " >> " + TEMP_DIR + "solr-" + version + ".zip");
		File release = null;
		if(name.equals(ReleaseType.SOLR)) {
			if (location.equals(Location.NODE_ONE)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(NODE_ONE_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(NODE_ONE_DIR + "solr-" + version);
				}
			} else if (location.equals(Location.NODE_TWO)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(NODE_TWO_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(NODE_TWO_DIR + "solr-" + version);
				}
			} else if (location.equals(Location.NODE_THREE)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(NODE_THREE_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(NODE_THREE_DIR + "solr-" + version);
				}
			} else if (location.equals(Location.TEMP)) {
				if (type.equals(Type.COMPRESSED)) {
					release = new File(TEMP_DIR + "solr-" + version + ".zip");
				} else if (type.equals(Type.EXTRACTED)) {
					release = new File(TEMP_DIR + "solr-" + version);
				}					
			}
			
			if(release.exists()) {
				this.postMessage(RELEASE_PRESENT);
				return true;
			}
		}		
		
		this.postMessage(RELEASE_DOWNLOAD);
		return false;
		
		}
		
		public boolean cleanNodeDirs() throws IOException {
			
			File n1 = new File(NODE_ONE_DIR);
			FileUtils.cleanDirectory(n1);
			File n2 = new File(NODE_TWO_DIR);
			FileUtils.cleanDirectory(n2);
			File n3 = new File(NODE_THREE_DIR);
			FileUtils.cleanDirectory(n3);
		
			return true;
		}
		

		  protected OSType detectedOS;
		 
		  public OSType getOperatingSystemType() {
		    if (detectedOS == null) {
		      String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		      if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
		        detectedOS = OSType.MacOS;
		      } else if (OS.indexOf("win") >= 0) {
		        detectedOS = OSType.Windows;
		      } else if (OS.indexOf("nux") >= 0) {
		        detectedOS = OSType.Linux;
		      } else {
		        detectedOS = OSType.Other;
		      }
		    }
		    return detectedOS;
		  }
		
		public void run(String[] args) throws Exception {
			
			if(args.length == 0) {
				this.postMessage(HELP_L1);
				this.postMessage(HELP_L2);
				this.postMessage(HELP_L3);
				this.postMessage(HELP_L4);
				this.postMessage(HELP_L5);
				this.postMessage(HELP_L6);
				return;
			}			
			
			Map<String, String> argM = new HashMap<String, String>();
			
			for (int i = 0 ; i < args.length ; i+=2) {
				argM.put(args[i], args[i+1]);
			}
			
			String versionOne = argM.get(ARG_VERSION_ONE);
			String versionTwo = argM.get(ARG_VERSION_TWO);
			String rootDir	 = argM.get(ARG_WORK_DIR);
			String collectionName = argM.get(COLLECTION_NAME);
			
			COLLECTION_NAME += "" + UUID.randomUUID().toString();
			
			if (collectionName != null) {
				COLLECTION_NAME = collectionName;
			}
			
			if (versionOne == null || versionTwo == null) {
				throw new Exception(ARG_ERROR_VERSION_NULL);
			}

			if (versionOne.equals(versionTwo)) {
				throw new Exception(ARG_ERROR_VERSION_SAME);
			}
			
			if (rootDir != null) {
				this.WORK_DIRECTORY = rootDir;
			}
					
			this.postMessage(HELLO);
			this.postMessage("Testing upgrade from " + versionOne + " To " + versionTwo);
			
			if (this.createBaseDir()) {
				this.postMessage(DIR_CREATED);
			}
			
			if (this.createNDir()) {
				this.postMessage(DIR_CREATED);
			}
			
			this.cleanNodeDirs();
			
			if (this.createTempDir()) {
				this.postMessage(DIR_CREATED);
			}
			
			if (!this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.EXTRACTED)) {
				if (this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
					this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionTwo +".zip", TEMP_DIR);
				}
			}
			
			if (!this.checkForRelease(versionTwo, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
				try {
					this.downloadRelease(versionTwo, TEMP_DIR, ReleaseType.SOLR);
					this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionTwo +".zip", TEMP_DIR);
				} catch (IOException e) {
					this.postMessage(BAD_RELEASE_NAME);
					return;
				}
			} 
			
			
			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.EXTRACTED)) {
				if (this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
					this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionOne +".zip", TEMP_DIR);
				}
			}

			
			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.TEMP, Type.COMPRESSED)) {
				try {
					this.downloadRelease(versionOne, TEMP_DIR, ReleaseType.SOLR);
					this.unZipDownloadedRelease(TEMP_DIR + "solr-" + versionOne +".zip", TEMP_DIR);
				} catch (IOException e) {
					this.postMessage(BAD_RELEASE_NAME);
					return;
				}
			} 

			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_ONE, Type.EXTRACTED)) {
				File node = new File(NODE_ONE_DIR + "solr-" + versionOne);
				node.mkdir();
				this.copyFolder(new File(TEMP_DIR + "solr-" + versionOne), node);
			}
			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_TWO, Type.EXTRACTED)) {
				File node = new File(NODE_TWO_DIR + "solr-" + versionOne);
				node.mkdir();
				this.copyFolder(new File(TEMP_DIR + "solr-" + versionOne), node);
			}
			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_THREE, Type.EXTRACTED)) {
				File node = new File(NODE_THREE_DIR + "solr-" + versionOne);
				node.mkdir();
				this.copyFolder(new File(TEMP_DIR + "solr-" + versionOne), node);
			}
			
			
			
			// MAIN TEST SEQUENCE HERE //
			
			this.doActionOnSolrNode("N1", versionOne, portOne, Action.START);
			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.START);
			this.doActionOnSolrNode("N3", versionOne, portThree, Action.START);
			
			this.createSOLRCollection("N1", versionOne, COLLECTION_NAME, NUM_SHARDS, NUM_REPLICAS);
			
			this.postData(COLLECTION_NAME);
			
			this.doActionOnSolrNode("N1", versionOne, portOne, Action.STOP);
			Thread.sleep(10000);
			this.upgradeSolr(versionOne, versionTwo, "N1");
			this.doActionOnSolrNode("N1", versionOne, portOne, Action.START);
			
			Thread.sleep(10000);

			boolean test1 = this.verifyData(COLLECTION_NAME);
			
			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.STOP);
			Thread.sleep(10000);
			this.upgradeSolr(versionOne, versionTwo, "N2");
			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.START);
			
			Thread.sleep(10000);

			boolean test2 = this.verifyData(COLLECTION_NAME);

			this.doActionOnSolrNode("N3", versionOne, portThree, Action.STOP);
			Thread.sleep(10000);
			this.upgradeSolr(versionOne, versionTwo, "N3");
			this.doActionOnSolrNode("N3", versionOne, portThree, Action.START);
			
			Thread.sleep(10000);

			boolean test3 = this.verifyData(COLLECTION_NAME);
					
				
			if(test1 && test2 && test3) {
				this.postMessage(DATA_OK);
			} else  {
				this.postMessage(DATA_NOT_OK);
			}
			
			Thread.sleep(10000);

			this.doActionOnSolrNode("N1", versionOne, portOne, Action.STOP);

			Thread.sleep(10000);

			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.STOP);

			Thread.sleep(10000);

			this.doActionOnSolrNode("N3", versionOne, portThree, Action.STOP);
			
			
		}
	

	public static void main(String[] args) throws Exception {
		
		new SolrUpdateTests().run(args);
		
	}
}