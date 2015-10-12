/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://www.popelganda.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.relaunch64.popelganda;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.Tools;
import de.relaunch64.popelganda.assemblers.Assembler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author Daniel Lüdecke
 */
public class InsertByteFromFileDlg extends javax.swing.JDialog {

    private File fileToOpen = null;
    private String bytetable = null;
    private final Assembler assembler;
    private final Settings settings;

    /**
     * Creates new form InsertByteFromFileDlg
     *
     * @param parent the parent frame of this dialog window
     * @param s a reference to the Settings class
     * @param assembler a reference to the Assembler-class, representing the
     * assembler of the currently activated editor pane where the bytes should
     * be inserted.
     */
    public InsertByteFromFileDlg(java.awt.Frame parent, Settings s, Assembler assembler) {
        super(parent);
        this.settings = s;
        this.assembler = assembler;
        bytetable = null;
        fileToOpen = null;
        initComponents();
        // set application icon
        setIconImage(ConstantsR64.r64icon.getImage());
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    @Action
    public void browseFile() {
        fileToOpen = FileTools.chooseFile(null, JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, settings.getLastUsedPath().getAbsolutePath(), "", "Open binary File", null, "All files");
        jTextFieldFile.setText((fileToOpen != null && fileToOpen.exists()) ? fileToOpen.toString() : "");
    }

    @Action
    public void apply() {
        try {
            // retrieve offsets
            int start = Integer.parseInt(jTextFieldStartOffset.getText());
            int end = Integer.parseInt(jTextFieldEndOffset.getText());
            int bpl = Integer.parseInt(jTextFieldBytesPerLine.getText());
            // check for valid bounds 
            if (start >= 0 && (start < end || -1 == end) && bpl > 0) {
                // check for valid file
                if (fileToOpen != null && fileToOpen.exists()) {
                    // retrieve byte table from file
                    bytetable = Tools.getByteTableFromFile(fileToOpen, start, end, assembler, bpl);
                    // update last used directory
                    settings.setLastUsedPath(fileToOpen);
                    setVisible(false);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "File not found or no valid file name specified!");
                    jTextFieldFile.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid values for offsets or bytes per line!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid values for offsets or bytes per line!");
        }
    }

    @Action
    public void cancel() {
        bytetable = null;
        setVisible(false);
        dispose();
    }

    public String getByteTable() {
        return bytetable;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextFieldFile = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldStartOffset = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldEndOffset = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldBytesPerLine = new javax.swing.JTextField();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(InsertByteFromFileDlg.class);
        setTitle(resourceMap.getString("InsertBytesFromFileDlg.title")); // NOI18N
        setModal(true);
        setName("InsertBytesFromFileDlg"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldFile.setName("jTextFieldFile"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(InsertByteFromFileDlg.class, this);
        jButtonBrowse.setAction(actionMap.get("browseFile")); // NOI18N
        jButtonBrowse.setName("jButtonBrowse"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldStartOffset.setColumns(8);
        jTextFieldStartOffset.setText(resourceMap.getString("jTextFieldStartOffset.text")); // NOI18N
        jTextFieldStartOffset.setToolTipText(resourceMap.getString("jTextFieldStartOffset.toolTipText")); // NOI18N
        jTextFieldStartOffset.setName("jTextFieldStartOffset"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextFieldEndOffset.setColumns(8);
        jTextFieldEndOffset.setText(resourceMap.getString("jTextFieldEndOffset.text")); // NOI18N
        jTextFieldEndOffset.setToolTipText(resourceMap.getString("jTextFieldEndOffset.toolTipText")); // NOI18N
        jTextFieldEndOffset.setName("jTextFieldEndOffset"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextFieldBytesPerLine.setColumns(8);
        jTextFieldBytesPerLine.setText(resourceMap.getString("jTextFieldBytesPerLine.text")); // NOI18N
        jTextFieldBytesPerLine.setName("jTextFieldBytesPerLine"); // NOI18N

        jButtonApply.setAction(actionMap.get("apply")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldBytesPerLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldEndOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldStartOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldFile, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowse))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldStartOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldEndOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldBytesPerLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextFieldBytesPerLine;
    private javax.swing.JTextField jTextFieldEndOffset;
    private javax.swing.JTextField jTextFieldFile;
    private javax.swing.JTextField jTextFieldStartOffset;
    // End of variables declaration//GEN-END:variables
}
