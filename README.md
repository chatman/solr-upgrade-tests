# solr-upgrade-tests
Running
-------
Try the following to run the project:

    mvn clean compile assembly:single

    java -cp target/solr-upgrade-tests-1.0-SNAPSHOT-jar-with-dependencies.jar SOLRUpgradeTests -v1 5.3.0 -v2 5.4.1


