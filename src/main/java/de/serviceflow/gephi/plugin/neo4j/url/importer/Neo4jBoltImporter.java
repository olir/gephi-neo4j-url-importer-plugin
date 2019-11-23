
package de.serviceflow.gephi.plugin.neo4j.url.importer;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.Issue;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.Map;
import java.util.List;
import java.util.Collection;

/**
 * Importer for Neo4j graph via Bolt protocol.
 
 * @author Oliver Rode
 */
public class Neo4jBoltImporter implements DatabaseImporter {

    //Architecture
    private ContainerLoader container;
    private Report report;
    private Neo4jBoltDatabaseImpl database;
    
    private Driver driver;

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        try {
            //Set container as undirected
            container.setEdgeDefault(EdgeDirectionDefault.DIRECTED);

            //Import
            importData();
        } catch (Exception e) {
            close();
            throw new RuntimeException(e);
        }
        close();
        return true;
    }

    private void close() {
        //Close connection
        if (driver != null) {
            try {
                driver.close();
                report.log("Database connection terminated");
            } catch (Exception e) {
              // ignore close errors
            }
        }
    }
    
    private void importData() throws Exception {
        String url = database.getProtocol()+"://"+database.getHost()+":"+database.getPort();
        report.log("Try to connect at " + url);

        driver = GraphDatabase.driver( url, AuthTokens.basic( database.getUsername(), database.getPasswd() ) );
        Session session = null;
        
        try
        {
            session = driver.session();
            report.log("Database connection established");
            
            getNodes(session);
            getEdges(session);
        }
        finally {
           if (session!=null)
              session.close();
        }
        
    }

    private void getNodes(Session session) {
        //Factory
        ElementDraft.Factory factory = container.factory();

        try {
          StatementResult result = session.run(database.getNodeQuery()+" RETURN n,ID(n),LABELS(n);");
          while (result.hasNext()) {
              Record record = result.next();
              int id = record.get( 1 ).asInt();
              List labelList = record.get( 2 ).asList();
              StringBuffer labels = new StringBuffer();
              for (Object lobj : labelList) {
                if (labels.length()>0)
                  labels.append(',');
                labels.append(lobj);
              }
              
              final NodeDraft node = factory.newNodeDraft(String.valueOf(id));

              node.setValue("_labels", labels.toString());
              
              Value nv = record.get(0);
              Node n = nv.asNode();
              Map<String, Object> properties = n.asMap(Values.ofObject());
              for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Collection)
                  node.setValue(entry.getKey(), value.toString());
                else
                  node.setValue(entry.getKey(), value);
              }
              
              container.addNode(node);
          }
        }
        catch (Exception e) {
            report.logIssue(new Issue("Cypher query failed.", Issue.Level.SEVERE, e));
        }
    }

    private void getEdges(Session session)  {
        //Factory
        ElementDraft.Factory factory = container.factory();

        try {
          StatementResult result = session.run(database.getEdgeQuery()+" RETURN r,ID(r),ID(n),ID(m),TYPE(r);");
          while (result.hasNext()) {
              Record record = result.next();
              int id = record.get( 1 ).asInt();
              int id1 = record.get( 2 ).asInt();
              int id2 = record.get( 3 ).asInt();
              String type = record.get( 4 ).asString();
              EdgeDraft edge = factory.newEdgeDraft(String.valueOf(id));
              edge.setSource(container.getNode(String.valueOf(id1)));
              edge.setTarget(container.getNode(String.valueOf(id2)));

              edge.setValue("_type", type);

              Value rv = record.get(0);
              Relationship r = rv.asRelationship();
              Map<String, Object> properties = r.asMap(Values.ofObject());
              for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Collection)
                  edge.setValue(entry.getKey(), value.toString());
                else
                  edge.setValue(entry.getKey(), value);
              }
              container.addEdge(edge);
          }
        }
        catch (Exception e) {
          report.logIssue(new Issue("Cypher query failed.", Issue.Level.SEVERE, e));
        }
    }
    
    @Override
    public void setDatabase(Database database) {
        this.database = (Neo4jBoltDatabaseImpl)database;
    }

    @Override
    public Database getDatabase() {
        return database;
    }
    
    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }
}