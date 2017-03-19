Running the project
====================

mvn clean compile assembly:single


In-place updates benchmark
--------------------------
java -cp target/org.apache.solr.tests.upgradetests-0.0.1-SNAPSHOT-jar-with-dependencies.jar:. org.apache.solr.tests.upgradetests.SimpleBenchmarks -v fcf71e34f20ea74f99933b80d5bd43cd487751f1 -Nodes 1 -Shards 1 -Replicas 1 -numDocs 20000 -iterations 10 -updates 5000 -queueSize 5000 -threads 8 -benchmarkType inplace

General indexing benchmark
--------------------------
java -cp target/org.apache.solr.tests.upgradetests-0.0.1-SNAPSHOT-jar-with-dependencies.jar:. org.apache.solr.tests.upgradetests.SimpleBenchmarks -v fcf71e34f20ea74f99933b80d5bd43cd487751f1 -Nodes 1 -Shards 1 -Replicas 1 -numDocs 20000 -threads 8 -benchmarkType generalIndexing

General indexing benchmark with patch:
--------------------------
java -cp target/org.apache.solr.tests.upgradetests-0.0.1-SNAPSHOT-jar-with-dependencies.jar:. org.apache.solr.tests.upgradetests.SimpleBenchmarks -v 72f75b2503fa0aa4f0aff76d439874feb923bb0e -patchUrl https://issues.apache.org/jira/secure/attachment/12852444/SOLR-10130.patch -Nodes 1 -Shards 1 -Replicas 1 -numDocs 50000 -threads 4 -benchmarkType generalIndexing

General querying benchmark (prefix queries):
--------------------------
java -cp target/org.apache.solr.tests.upgradetests-0.0.1-SNAPSHOT-jar-with-dependencies.jar:. org.apache.solr.tests.upgradetests.SimpleBenchmarks -v 72f75b2503fa0aa4f0aff76d439874feb923bb0e -Nodes 1 -Shards 1 -Replicas 1 -numDocs 10000 -threads 4 -benchmarkType generalQuerying

Note:
Project originally forked from https://github.com/viveknarang/solr-upgrade-tests
