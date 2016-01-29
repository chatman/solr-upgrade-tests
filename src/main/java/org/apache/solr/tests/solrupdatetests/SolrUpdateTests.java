package org.apache.solr.tests.solrupdatetests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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


public class SolrUpdateTests extends MessageConstants {

	public static String solrCommand;

	static {
			solrCommand = System.getProperty("os.name")!=null && System.getProperty("os.name").startsWith("Windows")? "bin/solr.cmd": "bin/solr";
	}

	public void postMessage(String message) {
		
							System.out.println(message);
							
	}	
	
	public void downloadRelease(String version, String dir, ReleaseType what) throws IOException {

		 String fileName = null;
		 URL link = null;
		 InputStream in = null;
		 FileOutputStream fos = null;
		 
		try {

							if (what.equals(ReleaseType.SOLR)) {
								 fileName = "solr-" + version + ".zip"; 
								 String url = URL_BASE + "/" + version + "/" + fileName;
								 this.postMessage(DOWNLOADING_RELEASE + " " + version + " from "+url);
								 link = new URL(url); 
							 } else if (what.equals(ReleaseType.ZOOKEEPER)) {
								 this.postMessage(DOWNLOADING_ZOO_RELEASE + " : " + version);
								 fileName = "zookeeper-" + version + ".tar.gz"; 
								 link = new URL(ZOO_URL_BASE + "zookeeper-" + version + "/" + fileName);  
							 }
							
							 in = new BufferedInputStream(link.openStream());
							 fos = new FileOutputStream(TEMP_DIR + fileName);
							 byte[] buf = new byte[1024*1024]; // 1mb blocks
							 int n = 0;
							 long size = 0;
							 while (-1!=(n=in.read(buf)))
							 {
							   size+=n;
							   this.postMessage("" + size); 
							   fos.write(buf, 0, n);
							 }
							 fos.close();
							 in.close();
							 fos.close();
				 
		} catch (Exception e) {
			
							 fos.close();
							 in.close();
							 fos.close();
							 e.printStackTrace();
			
		}
	}

	public void unZipDownloadedRelease(String dir, String destinationDir) throws IOException {
		
		try {
			
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
		
		} catch (Exception e) {
			
							e.printStackTrace();
		
		}
	} 
	
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		
			BufferedOutputStream bos = null;
			try {	
							
							bos = new BufferedOutputStream(new FileOutputStream(filePath));
							byte[] bytesIn = new byte[4096];
							int read = 0;
							        while ((read = zipIn.read(bytesIn)) != -1) {
							        							bos.write(bytesIn, 0, read);
							        }
							bos.close();
							
			} catch (Exception e) {
				
							bos.close();
							e.printStackTrace();
			
			}
		
    }

	public boolean createBaseDir() {

			try {
							File baseDir = new File(BASE_DIR);
									this.postMessage(CHECKING_BDIR);
									if (!baseDir.exists()) {
										this.postMessage(CREATING_BDIR);
										return baseDir.mkdir();
									}
							return false;
			
			} catch (Exception e) {
				
							e.printStackTrace();
							return false;
			
			}
		
	}

	public boolean createNDir() {
		
			try {
				
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
							
			} catch (Exception e) {
				
							e.printStackTrace();
							return false;
							
			}
	}

	public boolean createTempDir() {
					
			try {
				
							this.postMessage(CHECKING_TDIR);
							File tempDir = new File(TEMP_DIR);
							if (!tempDir.exists()) {
									this.postMessage(CREATING_TDIR);
									return tempDir.mkdir();
							}
							
							return false;
			
			} catch (Exception e) {
				
							e.printStackTrace();
							return false;
			
			}
							
	}
	
	public int doActionOnSolrNode(String node, String version, String port, Action action, String zkPort) throws IOException, InterruptedException {
		
	        Runtime rt = Runtime.getRuntime();
	        Process proc = null;
	        String act = null;
	        StreamGobbler errorGobbler = null;            
	        StreamGobbler outputGobbler = null;

	        try {
	        
					        if (action.equals(Action.START)) {
					        	act = "start";
					    		this.postMessage(START_PROC + " : " + node);
					        } else if (action.equals(Action.STOP)) {
					        	act = "stop";
					    		this.postMessage(STOP_PROC + " : " + node);
					        }        
					        
					        if ("N1".equals(node)) {
					          new File(NODE_ONE_DIR + "solr-"+ version + "/"+solrCommand).setExecutable(true);
					        	proc = rt.exec(NODE_ONE_DIR + "solr-"+ version + "/"+solrCommand+" " + act + " -p " + port + " -z "+ zkIP +":" + zkPort);
					        } else if ("N2".equals(node)) {
					          new File(NODE_TWO_DIR + "solr-"+ version + "/"+solrCommand).setExecutable(true);
					        	proc = rt.exec(NODE_TWO_DIR + "solr-"+ version + "/"+solrCommand+" " + act + " -p " + port + " -z "+ zkIP +":" + zkPort);
					
					        } else if ("N3".equals(node)) {
					          new File(NODE_THREE_DIR + "solr-"+ version + "/"+solrCommand).setExecutable(true);
					        	proc = rt.exec(NODE_THREE_DIR + "solr-"+ version + "/"+solrCommand+" " + act + " -p " + port + " -z "+ zkIP +":" + zkPort);
					        }
					        
					        errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
					        outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
					            
					        errorGobbler.start();
					        outputGobbler.start();
					        proc.waitFor();
					        return proc.exitValue();
					        
	        } catch (Exception e) {

	        				e.printStackTrace();
		        			return -1;
		        			
	        }

		}

		public int createSOLRCollection(String node, String version, String collectionName, String shards, String replicationFactor) throws IOException, InterruptedException {
		
			this.postMessage(CREATING_COLLECTION + " : " + node);
			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			StreamGobbler errorGobbler = null;            
	        StreamGobbler outputGobbler = null;
	        
        	try {
        
					        if ("N1".equals(node)) {
					          proc = rt.exec(NODE_ONE_DIR + "solr-"+ version + "/"+solrCommand+" create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
					        } else if ("N2".equals(node)) {
					          proc = rt.exec(NODE_TWO_DIR + "solr-"+ version + "/"+solrCommand+" create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
					        } else if ("N3".equals(node)) {
					          proc = rt.exec(NODE_THREE_DIR + "solr-"+ version + "/"+solrCommand+" create_collection -c " +collectionName+ " -shards " +shards+ " -replicationFactor " +replicationFactor);
					        }
					
					    	errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
					        outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
					            
					        errorGobbler.start();
					        outputGobbler.start();
					        proc.waitFor();
					        return proc.exitValue();

        	} catch (Exception e) {
        		
        					e.printStackTrace();
        					return -1;
        					
        	}

		}
		
		
		public void postData(String collectionName, String zkPort) throws IOException, InterruptedException, SolrServerException {
			
			this.postMessage(POSTING_DATA);
	        CloudSolrClient solr = null;
	        try {

					        solr = new CloudSolrClient(zkIP + ":" + zkPort);
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
	        
	        } catch (Exception e) {
	        	
				        	solr.close();
				        	e.printStackTrace();
	        
	        }
	        
	        
		}
		
		
		public boolean verifyData(String collectionName, String zkPort) throws IOException, InterruptedException, SolrServerException {
			
			this.postMessage(GETTING_DATA);			
	        CloudSolrClient solr = null;
	        try {	        	
	        
					        solr = new CloudSolrClient(zkIP + ":" + zkPort);
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

	        } catch (Exception e) {
	        	
			        		solr.close();
			        		e.printStackTrace();
			        		return false;
	        		
	        }
	        
		}
		
		public void deleteData(String collectionName, String zkPort) throws IOException, InterruptedException, SolrServerException {
			
			this.postMessage(GETTING_DATA);
			CloudSolrClient solr = null;
			try {
				
					        solr = new CloudSolrClient(zkIP + ":" + zkPort);
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

			} catch (Exception e) {

							solr.close();
			        		e.printStackTrace();
			        		
			}
		}
		
		
		
		public int getLiveNodes() throws IOException {
			
			this.postMessage(GETTING_LIVE_NODES);
	        CloudSolrClient solr = null;
	        try {
			        		solr = new CloudSolrClient(zkIP + ":" + zkPort);
			        		solr.connect();
			        		int liveNodes = solr.getZkStateReader().getClusterState().getLiveNodes().size();	
					        solr.close();			        
					        return liveNodes;

	        } catch (Exception e) {
	        	
			        		solr.close();
			        		e.printStackTrace();
			        		return -1;
	        		
	        }
			
		}
		
		

		
		public void upgradeSolr(String versionOne, String versionTwo, String node) throws IOException {

			try {

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
							FileUtils.copyDirectory(src, dest);

			} catch (Exception e) {
					
							e.printStackTrace();
				
			}
			
		}

		public boolean checkForRelease(String version, ReleaseType name, Location location, Type type) {
		
			this.postMessage(CHECK_RELEASE_DOWNLOADED + " >> " + TEMP_DIR + "solr-" + version + ".zip" + " Type: " + type + " Location:" + location);
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
		
		public void cleanNodeDirs() throws IOException {

			try {
			
							File n1 = new File(NODE_ONE_DIR);
							FileUtils.cleanDirectory(n1);
							File n2 = new File(NODE_TWO_DIR);
							FileUtils.cleanDirectory(n2);
							File n3 = new File(NODE_THREE_DIR);
							FileUtils.cleanDirectory(n3);
							
			} catch (Exception e) {
				
							e.printStackTrace();
			
			}
		}		  
		
		public void run(String[] args) throws Exception {
			
			if(args.length == 0) {
				this.postMessage(HELP_L1);
				this.postMessage(HELP_L2);
				this.postMessage(HELP_L3);
				this.postMessage(HELP_L4);
				this.postMessage(HELP_L5);
				this.postMessage(HELP_L6);
				this.postMessage(HELP_L7);
				
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
			String zkPort = argM.get(ARG_ZK_PORT);
			String help = argM.get(ARG_HELP);			
			String prtOne = argM.get(ARG_PORT_ONE);
			String prtTwo = argM.get(ARG_PORT_TWO);
			String prtThree = argM.get(ARG_PORT_THREE);
			
			if (prtOne != null) {
				this.portOne = prtOne;
			} 
			
			if (prtTwo != null) {
				this.portTwo = prtTwo;
			}
			
			if (prtThree != null) {
				this.portThree = prtThree;
			}
			
			if (help != null) {
				this.postMessage(HELP_L1);
				this.postMessage(HELP_L2);
				this.postMessage(HELP_L3);
				this.postMessage(HELP_L4);
				this.postMessage(HELP_L5);
				this.postMessage(HELP_L6);
				this.postMessage(HELP_L7);
				
				return;
			}
			
			if (zkPort != null)  {
				this.zkPort = zkPort;
			}				
			
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
				FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
			}
			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_TWO, Type.EXTRACTED)) {
				File node = new File(NODE_TWO_DIR + "solr-" + versionOne);
				node.mkdir();
				FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
			}
			if (!this.checkForRelease(versionOne, ReleaseType.SOLR, Location.NODE_THREE, Type.EXTRACTED)) {
				File node = new File(NODE_THREE_DIR + "solr-" + versionOne);
				node.mkdir();
				FileUtils.copyDirectory(new File(TEMP_DIR + "solr-" + versionOne), node);
			}
			
			
			
			// MAIN TEST SEQUENCE HERE //
			
			int evp1 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.START, this.zkPort);
			int evp2 = this.doActionOnSolrNode("N2", versionOne, portTwo, Action.START, this.zkPort);
			int evp3 = this.doActionOnSolrNode("N3", versionOne, portThree, Action.START, this.zkPort);
			
			if (evp1 != 0 || evp2 != 0 || evp3 != 0) {
				this.postMessage(NODES_LAUNCH_FAILURE);
				return;
			}
			
			this.createSOLRCollection("N1", versionOne, COLLECTION_NAME, NUM_SHARDS, NUM_REPLICAS);
			this.postData(COLLECTION_NAME, this.zkPort);
			
			int evp4 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.STOP, this.zkPort);
			if (evp4 != 0) {
				this.postMessage(NODES_SHUTDOWN_FAILURE);
				return; 
			}
			this.upgradeSolr(versionOne, versionTwo, "N1");
			int evp5 = this.doActionOnSolrNode("N1", versionOne, portOne, Action.START, this.zkPort);
			if (evp5 != 0) {
				this.postMessage(NODES_LAUNCH_FAILURE);
				return;
			}

			boolean test1 = this.verifyData(COLLECTION_NAME, this.zkPort);
			
			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.STOP, this.zkPort);
			Thread.sleep(10000);
			this.upgradeSolr(versionOne, versionTwo, "N2");
			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.START, this.zkPort);
			
			Thread.sleep(10000);

			boolean test2 = this.verifyData(COLLECTION_NAME, this.zkPort);

			this.doActionOnSolrNode("N3", versionOne, portThree, Action.STOP, this.zkPort);
			Thread.sleep(10000);
			this.upgradeSolr(versionOne, versionTwo, "N3");
			this.doActionOnSolrNode("N3", versionOne, portThree, Action.START, this.zkPort);
			
			Thread.sleep(10000);

			boolean test3 = this.verifyData(COLLECTION_NAME, this.zkPort);
					
				
			if(test1 && test2 && test3 && this.getLiveNodes() == 3) {
				this.postMessage(DATA_OK);
			} else  {
				this.postMessage(DATA_NOT_OK);
			}
			
			Thread.sleep(10000);

			this.doActionOnSolrNode("N1", versionOne, portOne, Action.STOP, this.zkPort);

			Thread.sleep(10000);

			this.doActionOnSolrNode("N2", versionOne, portTwo, Action.STOP, this.zkPort);

			Thread.sleep(10000);

			this.doActionOnSolrNode("N3", versionOne, portThree, Action.STOP, this.zkPort);
			
			
		}
	

	public static void main(String[] args) throws Exception {
		
		new SolrUpdateTests().run(args);
		
	}
}