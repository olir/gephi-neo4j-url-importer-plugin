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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.gephi.io.importer.api.Database;
import de.serviceflow.gephi.plugin.neo4j.url.importer.Neo4jBoltDatabaseImpl;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Neo4j Importer. This class is based on gephi ImportPluginUI.
 *
 * @author Oliver Rode
 */
public class Neo4jBoltDatabaseManager {

    private FileObject databaseConfigurations;
    private List<Database> neo4jBoltDatabases = new ArrayList();

    public Neo4jBoltDatabaseManager() {
        load();
    }

    public List<Database> getNeo4jBoltDatabases() {
        return neo4jBoltDatabases;
    }

    public List getNames() {
        List names = new ArrayList();
        for (Database db : neo4jBoltDatabases) {
            names.add(db.getName());
        }
        return names;
    }

    public void addDatabase(Neo4jBoltDatabaseImpl db) {
        neo4jBoltDatabases.add(db);
    }

    public boolean removeDatabase(Neo4jBoltDatabaseImpl db) {
        return neo4jBoltDatabases.remove(db);
    }

    public void persist() {
        doPersist();
    }

    private void load() {
        if (databaseConfigurations == null) {
            databaseConfigurations
                    = FileUtil.getConfigFile("Neo4jBoltDatabase");
        }

        if (databaseConfigurations != null) {
            InputStream is = null;

            try {
                is = databaseConfigurations.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                List unserialized = (List) ois.readObject();
                if (unserialized != null) {
                    neo4jBoltDatabases = unserialized;
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private void doPersist() {
        FileLock lock = null;
        ObjectOutputStream ois = null;

        try {
            if (databaseConfigurations != null) {
                databaseConfigurations.delete();
            }

            databaseConfigurations = FileUtil.getConfigRoot().createData("Neo4jBoltDatabase");
            lock = databaseConfigurations.lock();

            ois = new ObjectOutputStream(databaseConfigurations.getOutputStream(lock));
            ois.writeObject(neo4jBoltDatabases);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
}
