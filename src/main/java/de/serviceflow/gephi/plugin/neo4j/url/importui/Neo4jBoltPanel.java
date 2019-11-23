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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.Database;
import de.serviceflow.gephi.plugin.neo4j.url.importer.Neo4jBoltDatabaseImpl;
import org.gephi.ui.utils.DialogFileFilter;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

/**
 * Neo4j Importer. This class is based on gephi ImportPluginUI.
 *
 * @author Oliver Rode
 */
public class Neo4jBoltPanel extends javax.swing.JPanel {

    private Neo4jBoltDatabaseManager databaseManager;
    private static String NEW_CONFIGURATION_NAME
            = NbBundle.getMessage(Neo4jBoltPanel.class,
                    "Neo4jBoltPanel.template.name");
    private boolean inited = false;
    private final String [] protocols = { "bolt" };
    private final String [] defaultports = { "7687" };
    
    /**
     * Creates new form Neo4jBoltPanel
     */
    public Neo4jBoltPanel() {
        databaseManager = new Neo4jBoltDatabaseManager();
        initComponents();

        protocolComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    init((String) e.getItem());
                    
                    for (int i=0; i<protocols.length; i++) {
                      if (protocols[i].equals((String) e.getItem()))
                        portTextField.setText(defaultports[i]);
                    }
                }
            }
        });
    }
    
    static ValidationGroup group;

    public static ValidationPanel createValidationPanel(Neo4jBoltPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            throw new NullPointerException();
        }
        validationPanel.setInnerComponent(innerPanel);

        group = validationPanel.getValidationGroup();

        //Validators
        group.add(innerPanel.configNameTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.hostTextField, new HostValidator(innerPanel));
        group.add(innerPanel.portTextField, new PortValidator(innerPanel));
        group.add(innerPanel.userTextField, new NotEmptyValidator(innerPanel));

        return validationPanel;
    }

    private void init(final String protocol) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                hostLabel.setText(NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.hostLabel.text"));
                portTextField.setEnabled(true);
                portLabel.setEnabled(true);
                userLabel.setEnabled(true);
                userTextField.setEnabled(true);
                pwdLabel.setEnabled(true);
                pwdTextField.setEnabled(true);

                if (group!=null)
                  group.validateAll();
            }
        });
    }

    public Database getSelectedDatabase() {
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();

        populateNeo4jBoltDatabase(item.db);

        // add configuration if user changed the template configuration
        if (item.equals(model.templateConfiguration)) {
            databaseManager.addDatabase(item.db);
        }

        databaseManager.persist();

        return item.db;
    }

    public String getSelectedProtocol() {
        return (String)protocolComboBox.getSelectedItem();
    }


    public void setup() {
        configurationCombo.setModel(new Neo4jBoltPanel.ConfigurationComboModel());
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        if (model.getSelectedItem().equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
        inited = true;
        group.validateAll();
    }

    private void populateForm(Neo4jBoltDatabaseImpl db) {
        configNameTextField.setText(db.getName());
        String host = db.getHost();
        if (host.length()==0)
          host = "localhost";
        hostTextField.setText(host);
        portTextField.setText(db.getPort() == 0 ? "7687" : "" + db.getPort());
        String username = db.getUsername();
        if (username == null || username.length()==0)
          username = "neo4j";
        userTextField.setText(username);
        pwdTextField.setText(db.getPasswd());
        String protocol = db.getProtocol();
        if (protocol==null || protocol.length()==0)
          protocol=protocols[0];
        for (int i=0; i<protocols.length; i++) {
          if (protocols[i].equals(protocol))
            protocolComboBox.setSelectedIndex(i);
        }
        
        nodeLabelsMappingTextField.setText(db.getNodeLabelsMapping());
        edgeTypeMappingTextField.setText(db.getEdgeTypeMapping());
        nodeQueryTextField.setText(db.getNodeQuery());
        edgeQueryTextField.setText(db.getEdgeQuery());

        init(db.getProtocol());
    }

    private void populateNeo4jBoltDatabase(Neo4jBoltDatabaseImpl db) {
        db.setName(this.configNameTextField.getText());
        db.setHost(this.hostTextField.getText());
        db.setPasswd(new String(this.pwdTextField.getPassword()));
        db.setPort(!portTextField.getText().isEmpty()
                ? Integer.parseInt(portTextField.getText()) : 0);
        db.setUsername(this.userTextField.getText());
        db.setProtocol(this.getSelectedProtocol());
        db.setNodeLabelsMapping(this.nodeLabelsMappingTextField.getText());
        db.setEdgeTypeMapping(this.edgeTypeMappingTextField.getText());
        db.setNodeQuery(this.nodeQueryTextField.getText());
        db.setEdgeQuery(this.edgeQueryTextField.getText());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configurationCombo = new javax.swing.JComboBox();
        configurationLabel = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        pwdLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        protocolLabel = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox();
        nodeLabelsMappingLabel = new javax.swing.JLabel();
        nodeLabelsMappingTextField = new javax.swing.JTextField();
        edgeTypeMappingLabel = new javax.swing.JLabel();
        edgeTypeMappingTextField = new javax.swing.JTextField();
        nodeQueryLabel = new javax.swing.JLabel();
        nodeQueryTextField = new javax.swing.JTextField();
        edgeQueryLabel = new javax.swing.JLabel();
        edgeQueryTextField = new javax.swing.JTextField();
        testConnection = new javax.swing.JButton();
        pwdTextField = new javax.swing.JPasswordField();
        configNameTextField = new javax.swing.JTextField();
        configNameLabel = new javax.swing.JLabel();
        removeConfigurationButton = new javax.swing.JButton();
        jXHeader1 = new org.jdesktop.swingx.JXHeader();

        DefaultComboBoxModel protocolModel = new DefaultComboBoxModel(protocols);
        protocolComboBox.setModel(protocolModel);
        
        configurationCombo.setModel(new Neo4jBoltPanel.ConfigurationComboModel());
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });

        configurationLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.configurationLabel.text")); // NOI18N

        hostLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.hostLabel.text")); // NOI18N

        portLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.portLabel.text")); // NOI18N

        hostTextField.setName("host"); // NOI18N

        portTextField.setName("port"); // NOI18N

        userLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.userLabel.text")); // NOI18N

        pwdLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.pwdLabel.text")); // NOI18N

        userTextField.setName("user name"); // NOI18N

        protocolLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.protocolLabel.text")); // NOI18N

        nodeLabelsMappingLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.nodeLabelsMappingLabel.text")); // NOI18N

        nodeLabelsMappingTextField.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.nodeLabelsMappingTextField.text")); // NOI18N

        edgeTypeMappingLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.edgeTypeMappingLabel.text")); // NOI18N

        edgeTypeMappingTextField.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.edgeTypeMappingTextField.text")); // NOI18N

        nodeQueryLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.nodeQueryLabel.text")); // NOI18N

        nodeQueryTextField.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.nodeQueryTextField.text")); // NOI18N

        edgeQueryLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.edgeQueryLabel.text")); // NOI18N

        edgeQueryTextField.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.edgeQueryTextField.text")); // NOI18N

        testConnection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/serviceflow/gephi/plugin/neo4j/url/importui/resources/test_connection.png"))); // NOI18N
        testConnection.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.testConnection.text")); // NOI18N
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        pwdTextField.setName("password"); // NOI18N

        configNameTextField.setName("configName"); // NOI18N

        configNameLabel.setText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.configNameLabel.text")); // NOI18N

        removeConfigurationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/serviceflow/gephi/plugin/neo4j/url/importui/resources/remove_config.png"))); // NOI18N
        removeConfigurationButton.setToolTipText(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.removeConfigurationButton.toolTipText")); // NOI18N
        removeConfigurationButton.setMargin(new java.awt.Insets(0, 4, 0, 2));
        removeConfigurationButton.setPreferredSize(new java.awt.Dimension(65, 29));
        removeConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeConfigurationButtonActionPerformed(evt);
            }
        });

        jXHeader1.setDescription(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.header")); // NOI18N
        jXHeader1.setTitle(org.openide.util.NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.jXHeader1.title")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jXHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(testConnection)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userLabel)
                            .addComponent(pwdLabel)
                            .addComponent(protocolLabel)
                            .addComponent(hostLabel)
                            .addComponent(portLabel)
                            .addComponent(nodeLabelsMappingLabel)
                            .addComponent(edgeTypeMappingLabel)
                            .addComponent(nodeQueryLabel)
                            .addComponent(edgeQueryLabel)
                            .addComponent(configNameLabel)
                            .addComponent(configurationLabel))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(configurationCombo, 0, 423, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(configNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(edgeTypeMappingTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(nodeLabelsMappingTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(edgeQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(nodeQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(portTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(userTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pwdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(hostTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                ))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jXHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(removeConfigurationButton, 0, 0, Short.MAX_VALUE)
                    .addComponent(configurationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(configurationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(protocolLabel)
                    .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    )
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pwdLabel)
                    .addComponent(pwdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeLabelsMappingLabel)
                    .addComponent(nodeLabelsMappingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeTypeMappingLabel)
                    .addComponent(edgeTypeMappingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeQueryLabel)
                    .addComponent(nodeQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeQueryLabel)
                    .addComponent(edgeQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testConnection)
                .addContainerGap(53, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        if (!portTextField.getText().isEmpty()) {
            try {
                Integer.parseInt(portTextField.getText());
            } catch (Exception e) {
                return;
            }
        }
        try {
            test();
            String message = NbBundle.getMessage(Neo4jBoltPanel.class, "Neo4jBoltPanel.alert.connection_successful");
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        } catch (Exception ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
    }//GEN-LAST:event_testConnectionActionPerformed

    private void test() throws Exception {
        String url = this.getSelectedProtocol()+"://"+hostTextField.getText()+":"+(portTextField.getText().isEmpty() ? 0 : Integer.parseInt(portTextField.getText()));
        Driver driver = GraphDatabase.driver( url, AuthTokens.basic( userTextField.getText(), new String(pwdTextField.getPassword()) ) );
        Session session = null;
        
        try
        {
          session = driver.session();
        }
        finally {
           if (session!=null) {
              session.close();
           }
        }
        
    }
    
    
    private void removeConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeConfigurationButtonActionPerformed
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();

        if (databaseManager.removeDatabase(item.db)) {

            model.removeElement(item);
            databaseManager.persist();
            String message = NbBundle.getMessage(Neo4jBoltPanel.class,
                    "Neo4jBoltPanel.alert.configuration_removed", item.toString());
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
            model.setSelectedItem(model.getElementAt(0));

        } else {
            String message = NbBundle.getMessage(Neo4jBoltPanel.class,
                    "Neo4jBoltPanel.alert.configuration_unsaved");
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        }

    }//GEN-LAST:event_removeConfigurationButtonActionPerformed

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();
        if (item.equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
    }//GEN-LAST:event_configurationComboActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    //private javax.swing.JButton browseButton;
    private javax.swing.JLabel configNameLabel;
    private javax.swing.JTextField configNameTextField;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JLabel protocolLabel;
    private javax.swing.JLabel hostLabel;
    protected javax.swing.JTextField hostTextField;
    private org.jdesktop.swingx.JXHeader jXHeader1;
    private javax.swing.JLabel nodeLabelsMappingLabel;
    protected javax.swing.JTextField nodeLabelsMappingTextField;
    private javax.swing.JLabel edgeTypeMappingLabel;
    protected javax.swing.JTextField edgeTypeMappingTextField;
    private javax.swing.JLabel portLabel;
    protected javax.swing.JTextField portTextField;
    private javax.swing.JLabel pwdLabel;
    protected javax.swing.JPasswordField pwdTextField;
    private javax.swing.JLabel nodeQueryLabel;
    protected javax.swing.JTextField nodeQueryTextField;
    private javax.swing.JLabel edgeQueryLabel;
    protected javax.swing.JTextField edgeQueryTextField;
    private javax.swing.JButton removeConfigurationButton;
    private javax.swing.JButton testConnection;
    private javax.swing.JLabel userLabel;
    protected javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    public void initEvents() {
    }

    private class ConfigurationComboModel extends DefaultComboBoxModel {

        /**
         * The template configuration (will appear as "New Configuration")
         */
        ConfigurationComboItem templateConfiguration;

        public ConfigurationComboModel() {
            super();
            Collection<Database> configs = databaseManager.getNeo4jBoltDatabases();
            for (Database db : configs) {
                Neo4jBoltDatabaseImpl dbe = (Neo4jBoltDatabaseImpl) db;
                ConfigurationComboItem item = new ConfigurationComboItem(dbe);
                this.insertElementAt(item, this.getSize());
            }

            // add template configuration option at end
            Neo4jBoltDatabaseImpl db = new Neo4jBoltDatabaseImpl();
            populateNeo4jBoltDatabase(db);
            templateConfiguration = new ConfigurationComboItem(db);
            templateConfiguration.setConfigurationName(NEW_CONFIGURATION_NAME);
            this.insertElementAt(templateConfiguration, this.getSize());

            ConfigurationComboItem selected = (ConfigurationComboItem) this.getElementAt(0);
            this.setSelectedItem(selected);

            protocolComboBox.setSelectedItem(selected.db.getProtocol());
        }

        @Override
        public void setSelectedItem(Object anItem) {
            ConfigurationComboItem item = (ConfigurationComboItem) anItem;
            populateForm(item.db);
            super.setSelectedItem(anItem);
        }
    }

    private class ConfigurationComboItem {

        private final Neo4jBoltDatabaseImpl db;
        private String configurationName;

        public ConfigurationComboItem(Neo4jBoltDatabaseImpl db) {
            this.db = db;
            this.configurationName = db.getName();
        }

        public Neo4jBoltDatabaseImpl getDb() {
            return db;
        }

        public void setConfigurationName(String configurationName) {
            this.configurationName = configurationName;
        }

        @Override
        public String toString() {
            String name = configurationName;
            if (name == null || name.isEmpty()) {
                name = db.getProtocol()+":"+db.getHost()+":"+db.getPort();
            }
            return name;
        }
    }

    private static class HostValidator implements Validator<String> {

        private Neo4jBoltPanel panel;

        public HostValidator(Neo4jBoltPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            }
            return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model);
        }
    }


    private static class NotEmptyValidator implements Validator<String> {

        private Neo4jBoltPanel panel;

        public NotEmptyValidator(Neo4jBoltPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            }
            return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model);
        }
    }

    private static class PortValidator implements Validator<String> {

        private Neo4jBoltPanel panel;

        public PortValidator(Neo4jBoltPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            }
            return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model)
                    && Validators.REQUIRE_VALID_INTEGER.validate(problems, compName, model)
                    && Validators.numberRange(1, 65535).validate(problems, compName, model);
        }
    }
}
