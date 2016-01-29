package org.apache.solr.tests.solrupdatetests;

public class MessageConstants {

	public final String URL_BASE = "http://archive.apache.org/dist/lucene/solr/";
	
	public final String ZOO_URL_BASE = "http://www.us.apache.org/dist/zookeeper/";
	
	public String WORK_DIRECTORY = System.getProperty("user.dir");
	
	public final String DNAME = "SOLRUpdateTests";
	
	public final String BASE_DIR = WORK_DIRECTORY + "/" + DNAME + "/";
	
	public final String TEMP_DIR = BASE_DIR + "temp/";
	
	public final String NODE_ONE_DIR = BASE_DIR + "N1/";
	
	public final String NODE_TWO_DIR = BASE_DIR + "N2/";
	
	public final String NODE_THREE_DIR = BASE_DIR + "N3/";

	public final String HELLO = "[SOLR UPGRADE TESTS] HOLA !!! use -Help parameter to get more details on parameters";
	
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
	
	public final String ARG_ZK_PORT = "-ZkP";
	
	public final String ARG_HELP = "-Help";
	
	public final String HELP_L1 = "This testing program requires following parameters to run ... ";
	
	public final String HELP_L2 = "-v1 {From Version: ex '5.4.0'}; -v2 {To Version: ex '5.4.1'} ";

	public final String HELP_L3 = "-SkipClean: TRUE/FALSE {To clean the node directories or not}";

	public final String HELP_L4 = "-SkipUnzip: TRUE/FALSE {To unzip the releases or not} ";
	
	public final String HELP_L5 = "-N1Port/-N2Port/-N3Port: {Port number for three nodes, must be different. ex '1234'}";
	
	public final String HELP_L6 = "-WorkDirectory: {Define a working directory on your system ex '/home'}";
	
	public final String PORT_MISSING = "Port for each node is missing please define them through {-N1Port, -N2Port & -N3Port}";
	
	public final String HELP_L7 = "-ZkP {zookeeper port number}";
	
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
	
	public String zkPort = "2181";
	
	public String zkIP = "127.0.0.1";
	
	public static String solrCommand;	
	
	public String GETTING_LIVE_NODES = "Attempting to get live nodes on the cluster ... ";
	
	public String NODES_LAUNCH_FAILURE = "Node startup failed for some reason ... ";
	
	public String NODES_SHUTDOWN_FAILURE = "Node shutdown failed for some reason ... ";
	
}