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

package de.serviceflow.gephi.plugin.neo4j.url.importui;

import javax.swing.JPanel;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.importer.api.Database;
import de.serviceflow.gephi.plugin.neo4j.url.importer.Neo4jBoltImporterBuilder;
import de.serviceflow.gephi.plugin.neo4j.url.importer.Neo4jBoltImporter;

import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Neo4j Importer. This class is based on gephi ImportPluginUI.
 *
 * @author Oliver Rode
 */
@ServiceProvider(service = ImporterUI.class)
public class Neo4jBoltDatabaseImporterUI implements ImporterUI {

    private Neo4jBoltPanel panel;
    private DatabaseImporter[] importers;

    @Override
    public void setup(Importer[] importers) {
        this.importers = (DatabaseImporter[]) importers;
        if (panel == null) {
            panel = new Neo4jBoltPanel();
        }

        panel.setup();
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {
            panel = new Neo4jBoltPanel();
        }
        return Neo4jBoltPanel.createValidationPanel(panel);
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            Database database = panel.getSelectedDatabase();
            for (DatabaseImporter importer : importers) {
                importer.setDatabase(database);
            }
        }
        panel = null;
        importers = null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "Neo4jBoltBuilder.displayName");
    }

    public String getIdentifier() {
        return Neo4jBoltImporterBuilder.IDENTIFER;
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof Neo4jBoltImporter;
    }
}
