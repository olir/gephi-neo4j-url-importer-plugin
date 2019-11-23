
package de.serviceflow.gephi.plugin.neo4j.url.importer;

import org.openide.util.lookup.ServiceProvider;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.gephi.io.importer.spi.DatabaseImporter;

/**
 * File Importer builder implementation for Neo4j URL.
 * 
 * @author Oliver Rode
 */
@ServiceProvider(service = DatabaseImporterBuilder.class)
public class Neo4jBoltImporterBuilder implements DatabaseImporterBuilder {

    public static final String IDENTIFER = "neo4j";

    @Override
    public DatabaseImporter buildImporter() {
        return new Neo4jBoltImporter();
    }

    @Override
    public String getName() {
        return IDENTIFER;
    }

}