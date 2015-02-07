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
import de.relaunch64.popelganda.assemblers.Assembler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author Daniel Lüdecke
 */
public class InsertSinusTableDlg extends javax.swing.JDialog {

    private String bytetable = null;
    private final String byteToken;
    /**
     * Computes a table with sinus curve data.
     * 
     * @param parent the parent frame of this dialog window
     * @param assembler a reference to the Assembler-class, representing the assembler of the
     * currently activated editor pane where the bytes should be inserted.
     */
    public InsertSinusTableDlg(java.awt.Frame parent, Assembler assembler) {
        super(parent);
        byteToken = assembler.getByteDirective();
        bytetable = null;
        initComponents();
        // set application icon
        setIconImage(ConstantsR64.r64icon.getImage());
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    @Action
    public void apply() {
        try {
            // retrieve offsets
            int min = Integer.parseInt(jTextFieldMin.getText());
            int max = Integer.parseInt(jTextFieldMax.getText());
            int count = Integer.parseInt(jTextFieldCount.getText());
            int bpl = Integer.parseInt(jTextFieldBytesPerLine.getText());
            int anzahlKurven = Integer.parseInt(jTextFieldCurves.getText());
            int phase = (max+1-min)/2;
            double step = (double) anzahlKurven*2*Math.PI;
            // calculation fix
            step = step / count;
            int dummy = (int)(step*10000)+1;
            step = (double) dummy/10000;
            // get compiler byte-token
            StringBuilder sb = new StringBuilder("");
            // some indicators for new lines and line-length of table
            boolean startNewLine = true;
            int bytesPerLine = 0;
            // check for valid bounds 
            if (min>=0 && min<max && bpl>0 && count>0 && anzahlKurven>0) {
                while (anzahlKurven>0) {
                    for (double i=-Math.PI/2; i<Math.PI/2; i=i+step) {
                        // check if we have a new line
                        if (startNewLine) {
                            sb.append(byteToken).append(" ");
                            startNewLine = false;
                        }
                        int value = (int)((Math.sin(i)*phase)+(phase+min));
                        if (value>max) value=max;
                        if (value<min) value=min;
                        // append byte
                        sb.append("$").append(String.format("%02x", value));
                        // increase counter for bytes per line
                        bytesPerLine++;
                        // check if we reached end of line
                        if (bytesPerLine>=bpl) {
                            sb.append("\n");
                            // start new line
                            startNewLine = true;
                            // reset bytes per line counter
                            bytesPerLine = 0;
                        }
                        else {
                            sb.append(", ");
                        }
                    }
                    for (double i=Math.PI/2; i>-Math.PI/2; i=i-step) {
                        // check if we have a new line
                        if (startNewLine) {
                            sb.append(byteToken).append(" ");
                            startNewLine = false;
                        }
                        int value = (int)((Math.sin(i)*phase)+(phase+min));
                        if (value>max) value=max;
                        if (value<min) value=min;
                        // append byte
                        sb.append("$").append(String.format("%02x", value));
                        // increase counter for bytes per line
                        bytesPerLine++;
                        // check if we reached end of line
                        if (bytesPerLine>=bpl || i<=-Math.PI/2) {
                            sb.append("\n");
                            // start new line
                            startNewLine = true;
                            // reset bytes per line counter
                            bytesPerLine = 0;
                        }
                        else {
                            sb.append(", ");
                        }
                    }
                    anzahlKurven--;
                }
                bytetable = sb.toString();
                setVisible(false);
                dispose();
            }
            else {
                JOptionPane.showMessageDialog(null, "Invalid values or wrong range specified!");
            }
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid values!");
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
        jTextFieldMin = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldMax = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldCount = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldBytesPerLine = new javax.swing.JTextField();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldCurves = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(InsertSinusTableDlg.class);
        setTitle(resourceMap.getString("InsertSinusTableDlg.title")); // NOI18N
        setModal(true);
        setName("InsertSinusTableDlg"); // NOI18N

        jLabel1.setDisplayedMnemonic('i');
        jLabel1.setLabelFor(jTextFieldMin);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldMin.setColumns(8);
        jTextFieldMin.setText(resourceMap.getString("jTextFieldMin.text")); // NOI18N
        jTextFieldMin.setName("jTextFieldMin"); // NOI18N

        jLabel2.setDisplayedMnemonic('x');
        jLabel2.setLabelFor(jTextFieldMax);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldMax.setColumns(8);
        jTextFieldMax.setText(resourceMap.getString("jTextFieldMax.text")); // NOI18N
        jTextFieldMax.setName("jTextFieldMax"); // NOI18N

        jLabel3.setDisplayedMnemonic('v');
        jLabel3.setLabelFor(jTextFieldCount);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextFieldCount.setColumns(8);
        jTextFieldCount.setText(resourceMap.getString("jTextFieldCount.text")); // NOI18N
        jTextFieldCount.setName("jTextFieldCount"); // NOI18N

        jLabel4.setDisplayedMnemonic('l');
        jLabel4.setLabelFor(jTextFieldBytesPerLine);
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextFieldBytesPerLine.setColumns(8);
        jTextFieldBytesPerLine.setText(resourceMap.getString("jTextFieldBytesPerLine.text")); // NOI18N
        jTextFieldBytesPerLine.setName("jTextFieldBytesPerLine"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(InsertSinusTableDlg.class, this);
        jButtonApply.setAction(actionMap.get("apply")); // NOI18N
        jButtonApply.setMnemonic('A');
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jLabel5.setDisplayedMnemonic('c');
        jLabel5.setLabelFor(jTextFieldCurves);
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jTextFieldCurves.setColumns(8);
        jTextFieldCurves.setText(resourceMap.getString("jTextFieldCurves.text")); // NOI18N
        jTextFieldCurves.setName("jTextFieldCurves"); // NOI18N

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
                            .addComponent(jTextFieldCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldCurves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldCurves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldBytesPerLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField jTextFieldBytesPerLine;
    private javax.swing.JTextField jTextFieldCount;
    private javax.swing.JTextField jTextFieldCurves;
    private javax.swing.JTextField jTextFieldMax;
    private javax.swing.JTextField jTextFieldMin;
    // End of variables declaration//GEN-END:variables
}
