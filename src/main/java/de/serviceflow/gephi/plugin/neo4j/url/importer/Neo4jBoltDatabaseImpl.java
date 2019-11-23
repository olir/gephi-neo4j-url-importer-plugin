/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
Portions Copyrighted 2019 Oliver Rode.
*/
package de.serviceflow.gephi.plugin.neo4j.url.importer;

import org.gephi.io.importer.api.AbstractDatabase;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;

/**
 * Neo4j Importer. This class is based on gephi ImportPlugin.
 *
 * @author Oliver Rode
 */
public class Neo4jBoltDatabaseImpl extends AbstractDatabase {

    private String protocol;
    private String nodeLabelsMapping;
    private String edgeTypeMapping;
    private String nodeQuery;
    private String edgeQuery;

    public Neo4jBoltDatabaseImpl() {

        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.ID, "id");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");
        properties.addNodePropertyAssociation(NodeProperties.COLOR, "color");
        properties.addNodePropertyAssociation(NodeProperties.START, "start");
        properties.addNodePropertyAssociation(NodeProperties.END, "end");
        properties.addNodePropertyAssociation(NodeProperties.START, "start_open");
        properties.addNodePropertyAssociation(NodeProperties.END_OPEN, "end_open");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.SOURCE, "source");
        properties.addEdgePropertyAssociation(EdgeProperties.TARGET, "target");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
        properties.addNodePropertyAssociation(NodeProperties.COLOR, "color");
        properties.addEdgePropertyAssociation(EdgeProperties.START, "start");
        properties.addEdgePropertyAssociation(EdgeProperties.END, "end");
        properties.addEdgePropertyAssociation(EdgeProperties.START, "start_open");
        properties.addEdgePropertyAssociation(EdgeProperties.END_OPEN, "end_open");
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getEdgeTypeMapping() {
        return edgeTypeMapping;
    }

    public void setEdgeTypeMapping(String edgeTypeMapping) {
        this.edgeTypeMapping = edgeTypeMapping;
    }

    public String getNodeLabelsMapping() {
        return nodeLabelsMapping;
    }

    public void setNodeLabelsMapping(String nodeLabelsMapping) {
        this.nodeLabelsMapping = nodeLabelsMapping;
    }
    
    public String getEdgeQuery() {
        return edgeQuery;
    }

    public void setEdgeQuery(String edgeQuery) {
        this.edgeQuery = edgeQuery;
    }

    public String getNodeQuery() {
        return nodeQuery;
    }

    public void setNodeQuery(String nodeQuery) {
        this.nodeQuery = nodeQuery;
    }
    
}
