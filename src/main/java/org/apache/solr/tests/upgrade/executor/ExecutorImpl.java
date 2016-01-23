package org.apache.solr.tests.upgrade.executor;

import java.io.IOException;

import org.apache.solr.tests.upgrade.generic.UtilImpl;
import org.apache.solr.tests.upgrade.interfaces.Executor;


public class ExecutorImpl extends UtilImpl implements Executor {

	@Override
	public void startSOLRNode(String node, String version, String port) throws IOException, InterruptedException {
		
			this.postMessage(START_PROC + " : " + node);
			
            Runtime rt = Runtime.getRuntime();
            Process proc = null;
            
            if ("N1".equals(node)) {
            	proc = rt.exec(NODE_ONE_DIR + "solr-5.4.0/bin/solr.cmd start -p " + port);
            } else if ("N2".equals(node)) {
            	proc = rt.exec(NODE_TWO_DIR + "solr-5.4.0/bin/solr.cmd start -p " + port);

            } else if ("N3".equals(node)) {
            	proc = rt.exec(NODE_THREE_DIR + "solr-5.4.0/bin/solr.cmd start -p " + port);
            }
            
            // any error message? 
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            
            proc.waitFor();
 
            this.postMessage(NODE_STARTED + OK_MSG_GENERAL);
	}

	@Override
	public void stopSOLRNode(String node, String version, String port) throws IOException, InterruptedException {
		
			this.postMessage(STOP_PROC);
		
			Runtime rt = Runtime.getRuntime();
	        Process proc = null;
	        
	        if ("N1".equals(node)) {
	        	proc = rt.exec(NODE_ONE_DIR + "solr-5.4.0/bin/solr.cmd stop -p " + port);
	        } else if ("N2".equals(node)) {
	        	proc = rt.exec(NODE_TWO_DIR + "solr-5.4.0/bin/solr.cmd stop -p " + port);
	
	        } else if ("N3".equals(node)) {
	        	proc = rt.exec(NODE_THREE_DIR + "solr-5.4.0/bin/solr.cmd stop -p " + port);
	        }
	        
	        // any error message? 
	        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
	        // any output?
	        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
	            
	        // kick them off
	        errorGobbler.start();
	        outputGobbler.start();
	        
	        proc.waitFor();
			
	        this.postMessage(NODE_STOPPED + OK_MSG_GENERAL);
	}

	@Override
	public void loadData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startZooKeeper(String version) throws IOException, InterruptedException {
		
		this.postMessage(START_ZOO);
		
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        
       	proc = rt.exec(BASE_DIR + "zookeeper-"+ version +"/bin/zkServer.cmd");
        
        // any error message? 
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        
        proc.waitFor();

        this.postMessage(STARTED_ZOO + OK_MSG_GENERAL);
	
	}

	@Override
	public void stopZooKeeper(String version) throws IOException, InterruptedException {
		
		this.postMessage(STOP_ZOO);
		
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        
       	proc = rt.exec(BASE_DIR + "zookeeper-"+ version +"/bin/zkServer.cmd");
        
        // any error message? 
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        
        proc.waitFor();

        this.postMessage(STOPPED_ZOO + OK_MSG_GENERAL);		
	}	
	
}
