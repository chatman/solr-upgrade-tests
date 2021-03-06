package org.apache.solr.tests.upgradetests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.lucene.util.TestUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.google.common.collect.ImmutableMap;

public class SolrClient {

	public int testDocumentsCount = 1000;
	private String zookeeperIp;
	private String zookeeperPort;
	private final CloudSolrClient cloudSolrClient;

	public SolrClient(int testDocumentsCount, String zookeeperIp, String zookeeperPort) {
		this(testDocumentsCount, zookeeperIp, zookeeperPort, 
				20000, 10, 5000, 5000, 4, false);
	}
	public SolrClient(int testDocumentsCount, String zookeeperIp, String zookeeperPort, 
			int numDocs, int iterations, int updates, int queueSize, int threads, 
			boolean onlyRegularUpdates) {
		super();
		this.testDocumentsCount = testDocumentsCount;
		this.zookeeperIp = zookeeperIp;
		this.zookeeperPort = zookeeperPort;
		
		this.cloudSolrClient = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
		this.numDocs = numDocs;
		this.iterations = iterations;
		this.updates = updates;
		this.queueSize = queueSize;
		this.threads = threads;
		this.onlyRegularUpdates = onlyRegularUpdates;
	}

	public void postData(String collectionName) throws IOException, InterruptedException, SolrServerException {

		Util.postMessage("** Posting data to the node ... ", MessageType.ACTION, true);
		try {
			
			cloudSolrClient.connect();
			cloudSolrClient.setDefaultCollection(collectionName);
			SolrInputDocument document;

			for (int i = 1; i <= testDocumentsCount; i++) {
				document = new SolrInputDocument();
				document.addField("id", i);
				document.setField("EMP_ID", "EMP_ID@" + i);
				document.setField("TITLE", "TITLE@" + i);
				cloudSolrClient.add(collectionName, document);
				if (i % 10 == 0) {
					Util.postMessageOnLine("|");
				}
			}
			Util.postMessage("", MessageType.GENERAL, false);
			Util.postMessage("Added data into the cluster ...", MessageType.RESULT_SUCCESS, true);
			cloudSolrClient.commit();
			cloudSolrClient.close();
		} finally {
			cloudSolrClient.close();
		}

	}

	private final int numDocs;
	private final int iterations;
	private final int updates;
	private final int threads;
	private final int queueSize;
	private final boolean onlyRegularUpdates;

	public void benchmarkInPlaceUpdates(String collectionName, List<SolrNode> nodes) throws SolrServerException, IOException, InterruptedException {
		Random r = new Random(0); // fixed seed, so that benchmarks are reproducible easily
		org.apache.solr.client.solrj.SolrClient client = cloudSolrClient;
		cloudSolrClient.setDefaultCollection(collectionName);
		
		ConcurrentUpdateSolrClient cusc = new ConcurrentUpdateSolrClient(nodes.get(0).getBaseUrl()+"/" + collectionName, queueSize, threads);
		client = cusc;
		
		List<String> fields;
		
		if (onlyRegularUpdates) {
			fields = Arrays.asList("stored_l");
		} else {
			fields = Arrays.asList("stored_l", "inplace_dvo_l");
		}

		long start = System.nanoTime();
		List<SolrInputDocument> batch = new ArrayList<>();
		// Index numDocs docs
		for (int i=1; i<=numDocs; i++) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", i);
			doc.addField("stored_l", r.nextLong());
			doc.addField("inplace_dvo_l", r.nextLong());
			doc.addField("text", Util.getSentence(r, 1000));
			client.add(doc);
			if (i % 10000 == 0) {
				System.out.println(i + ": "+doc);
			}
		}
		System.out.println("Committing...");
		client.commit();
		System.out.println("Committed...");
		long end = System.nanoTime();
		Util.postMessage("Time for adding "+numDocs+" documents: " + (end-start)/1000000000 + " secs", MessageType.RESULT_SUCCESS, true);
		batch.clear();
		
		Map<String, Long> times = new HashMap<String, Long>();
		times.put("stored_l", 0l);
		times.put("inplace_dvo_l", 0l);
		for (int iter=0; iter<iterations; iter++) {
			for (String field: fields) {
				start = System.nanoTime();
				batch = new ArrayList<>();
				for (int i=1; i<=updates; i++) {
					SolrInputDocument doc = new SolrInputDocument();
					int docid = 1 + r.nextInt(numDocs);
					doc.addField("id", docid);
					doc.addField(field, ImmutableMap.of("set", r.nextInt()));
					client.add(doc);

					if (i % 5000 == 0) {
						System.out.println(iter+" ("+field+"), "+docid + ": "+doc);
					}
				}
				//client.add(batch);
				client.commit();
				end = System.nanoTime();
				
				times.put(field, times.get(field) + (end-start));
				System.out.println("Iteration "+iter+", field "+field+", took "+ (end-start)/1000000000+ "secs");
				//Thread.sleep(2000);
			}
		}
		
		for (String field: fields) {
			Util.postMessage("Time for "+field+": " + (times.get(field))/1000000000 + " secs", MessageType.RESULT_SUCCESS, true);
		}
		
		cusc.close();
	}

	public boolean verifyData(String collectionName) throws IOException, InterruptedException, SolrServerException {

		Util.postMessage("** Getting the data from nodes ... ", MessageType.RESULT_SUCCESS, true);
		CloudSolrClient solr = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
		try {

			solr.connect();
			solr.setDefaultCollection(collectionName);
			SolrQuery query = new SolrQuery("*:*");
			query.setRows(10000);
			SolrDocumentList docList = solr.query(query).getResults();

			int count = 0;
			for (SolrDocument document : docList) {
				if (!(document.getFieldValue("TITLE").toString().split("@", 2)[1]
						.equals(document.getFieldValue("EMP_ID").toString().split("@", 2)[1]))) {
					solr.close();
					Util.postMessage("%%%% DATA CORRUPTED, returning false  %%%%", MessageType.RESULT_ERRROR, true);
					return false;
				}
				count++;
				if (count % 10 == 0) {
					Util.postMessageOnLine("|");
				}
			}
			Util.postMessage("", MessageType.GENERAL, false);

			if (count != testDocumentsCount) {
				Util.postMessage("%%%% DATA COUNT MISMATCH, returning false  %%%%", MessageType.RESULT_ERRROR, true);
				solr.close();
				return false;
			}

			solr.close();
			return true;

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return false;

		} finally {

			solr.close();

		}

	}

	public void benchmarkGeneralIndexing(String collectionName, List<SolrNode> nodes) throws SolrServerException, IOException, InterruptedException {
		Random r = new Random(0); // fixed seed, so that benchmarks are reproducible easily
		org.apache.solr.client.solrj.SolrClient client = cloudSolrClient;
		cloudSolrClient.setDefaultCollection(collectionName);
		
		ConcurrentUpdateSolrClient cusc = new ConcurrentUpdateSolrClient(nodes.get(0).getBaseUrl()+"/" + collectionName, queueSize, threads);
		client = cusc;
		
		long start = System.nanoTime();
		List<SolrInputDocument> batch = new ArrayList<>();
		// Index numDocs docs
		for (int i=1; i<=numDocs; i++) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", i);
			doc.addField("stored_l", r.nextLong());
			String sentence = Util.getSentence(r, 1000);
			doc.addField("text", sentence);
			client.add(doc);
			if (i % 10000 == 0) {
				System.out.println(i + ": "+doc);
			}
		}
		System.out.println("Committing...");
		client.commit();
		System.out.println("Committed...");
		long end = System.nanoTime();
		Util.postMessage("Time for adding "+numDocs+" documents: " + (end-start)/1000000000 + " secs", MessageType.RESULT_SUCCESS, true);
		batch.clear();
		
		cusc.close();
	}
	
	public void benchmarkGeneralQuerying(String collectionName, List<SolrNode> nodes) throws SolrServerException, IOException, InterruptedException {
		benchmarkGeneralIndexing(collectionName, nodes);
		
		final HttpSolrClient hsc = new HttpSolrClient(nodes.get(0).getBaseUrl()+"/" + collectionName);
		
		ConcurrentLinkedDeque<Integer> prefixQueryTimes = new ConcurrentLinkedDeque<>();
		AtomicInteger prefixQueryTime = new AtomicInteger();
		
		Random r = new Random();
		ExecutorService executor = Executors.newFixedThreadPool(threads*4); // 4 times as many query threads as indexing threads
		for (int i=0; i<numDocs; i++) {
            executor.execute(new Runnable() {
				public void run() {
					ModifiableSolrParams params = new ModifiableSolrParams();
					String randomWord = "";
					while (randomWord.length()==0) {
						randomWord = TestUtil.randomSimpleString(r, 3);
					}
					params.add("q", "text:" + randomWord + "*");
					try {
						int qtime = Integer.parseInt(hsc.query(params).getHeader().get("QTime").toString());
						prefixQueryTimes.add(qtime);
						prefixQueryTime.addAndGet(qtime);
					} catch (SolrServerException | IOException e) {	}
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(600, TimeUnit.SECONDS);

		System.out.println("Got results for prefix queries: "+prefixQueryTimes.size());
		System.out.println("Max time (prefix queries): "+Collections.max(prefixQueryTimes));
		System.out.println("Total time (prefix queries): "+prefixQueryTime);
	}

	public void deleteData(String collectionName) throws IOException, InterruptedException, SolrServerException {

		Util.postMessage("** Deleting data from the nodes ... ", MessageType.ACTION, true);
		CloudSolrClient solr = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
		try {
			solr.connect();
			solr.setDefaultCollection(collectionName);
			solr.deleteByQuery("*:*");
			solr.close();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		} finally {

			solr.close();

		}
	}

	public int getLiveNodes() throws IOException {

		Util.postMessage("** Attempting to get live nodes on the cluster ... ", MessageType.ACTION, true);
		CloudSolrClient solr = new CloudSolrClient(zookeeperIp + ":" + zookeeperPort);
		try {
			solr.connect();
			int liveNodes = solr.getZkStateReader().getClusterState().getLiveNodes().size();
			solr.close();
			return liveNodes;

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		} finally {

			solr.close();

		}
	}

	public String getZookeeperIp() {
		return zookeeperIp;
	}

	public void setZookeeperIp(String zookeeperIp) {
		this.zookeeperIp = zookeeperIp;
	}

	public String getZookeeperPort() {
		return zookeeperPort;
	}

	public void setZookeeperPort(String zookeeperPort) {
		this.zookeeperPort = zookeeperPort;
	}

	public CloudSolrClient getCloudSolrClient() {
		return cloudSolrClient;
	}
}
