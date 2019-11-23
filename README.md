# gephi-neo4j-url-importer-plugin
[![Build Status](https://travis-ci.org/olir/gephi-neo4j-url-importer-plugin.png)](https://travis-ci.org/olir/gephi-neo4j-url-importer-plugin/builds)

## About
A [gephi](http://www.gephi.org) plugin to import from a [neo4j](http://neo4j.com) database via bolt protocol.

## Installation
Build nbm file from source or download from releases. In gephi install nbm over Tools->Plugins->Downloaded->Add Plugins...

## Usage
Choose File->Import Database->Neo4j..., then update hostname, port, username and password.

Node labels and releationship type will be mapped to properties-

Cypher queries can be adjusted for filtering, but keep the node and relationship variables (n,r,m) or the query will fail.

![Importer Dialog](https://raw.githubusercontent.com/olir/gephi-neo4j-url-importer-plugin/master/src/site/gephi-neo4j-import.JPG)
