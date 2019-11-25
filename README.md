# gephi-neo4j-url-importer-plugin
[![Build Status](https://travis-ci.org/olir/gephi-neo4j-url-importer-plugin.png)](https://travis-ci.org/olir/gephi-neo4j-url-importer-plugin/builds)
[![Downloads](https://img.shields.io/github/downloads/olir/gephi-neo4j-url-importer-plugin/0.2/total.svg)](https://github.com/olir/gephi-neo4j-url-importer-plugin/releases/tag/0.2)

## About
A [gephi](http://www.gephi.org) v0.9+ plugin to import from a [neo4j](http://neo4j.com) database via bolt protocol.

## Installation
Build nbm file from source or download from releases. In gephi install nbm over Tools->Plugins->Downloaded->Add Plugins...

## Usage
Choose File->Import Database->Neo4j..., then update hostname, port, username and password.

Node labels and releationship type will be mapped to properties-

Cypher queries can be adjusted for filtering, but keep the node and relationship variables (n,r,m) or the query will fail.

![Importer Dialog](https://raw.githubusercontent.com/olir/gephi-neo4j-url-importer-plugin/master/src/site/gephi-neo4j-import.JPG)

### Connecting to a remote neo4j container
Since the import operation may take a while, depending on the size of your database, keep in mind that you may need to increase timeouts for  the container. E.g. for a openshift port-forward you can archive this with --request-timeout=240m
