# Solr Cloud Rolling Upgrade Tests
Introduction
------------

The purpose of this program is to test rolling upgrades for Solr Cloud. This is a standalone system where the program takes care of zookeeper and solr releases and manages the process end-to-end.


To Run
------
    
Use the following command to run this program on server

                 java -cp target/solr-upgrade-tests-1.0-SNAPSHOT-jar-with-dependencies.jar org.apache.solr.tests.solrupdatetests.SolrUpdateTests -v1 5.2.0 -v2 5.3.1 -NNodes 3

Program parameters
------------------

                -v1         {Version One}                   Example 5.4.0
                -v2         {Version Two}                   Example 5.4.1
                -NNodes     {Number of nodes}               Example 3
                -NShards    {Number of shards}              Example 2
                -NReplicas  {Number of Replicas}            Example 3
                -Help       {To get help about more options}    -
    
Steps
-----

Following is the summary of the steps that the program follows to test the rolling upgrade
    
                Initial steps include, creating local directories, locating and registering ports to use.
                Looking locally for zookeeper, if not present dowload, install and start zookeeper on port 2181
                Looking locally for Solr releases, if not present download it and copy the release on each node folder.
                Start Solr nodes 
                Create a Test collection
                Insert a set of 1000 documents
                Stop each node one by one, upgrade each node by replacing lib folder on server and then start each node
                upon start of the node check if all the documents are present and that the documents are intact. 
                When all the documents are present and the nodes are normal the program identifies as the test successful
                Upon failure of any node or documents count or state abnormal the program declares as the test as failed.
                Final steps include shutting down the nodes and cleaning zookeeper data.


Contributing to this project
----------------------------

Please checkout the code from the repository

                https://github.com/chatman/solr-upgrade-tests
    
Import the project on eclise and do the following 

                mvn eclipse:eclipse
                mvn clean compile assembly:single


Contributions
-------------

                Ishan Chattopadhyaya
                Vivek Narang
    

  
