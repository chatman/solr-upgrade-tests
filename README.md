# solr-upgrade-tests
Running
-------

First, run a Zookeeper instance at 2181 port. Then run the following:

    mvn clean compile assembly:single

    java -cp target/solr-upgrade-tests-1.0-SNAPSHOT-jar-with-dependencies.jar org.apache.solr.tests.solrupdatetests.SolrUpdateTests -v1 5.2.0 -v2 5.3.1


