/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */

package com.aspc.remote.application;

import java.util.*;

import com.aspc.remote.database.*;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.*;


/**
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 * @author  nigel
 * @since 29 September 2006
 */
public class CLogin extends javax.swing.JDialog
{
    /**
     * Creates new form CLogin
     * @param parent
     * @param modal
     */
    public CLogin(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);

        if( parent instanceof CFrame)
        {
            this.master = (CFrame)parent;
        }
        else
        {
            master=null;
        }

        initComponents();
        init();
    }

    private String getUserId()
    {
        return userIdField.getText();
    }

    //private String getPassword()
    //{
    //    return passwdField.getText();
    //}

    private String getURL()
    {
        return urlField.getText();
    }

    private String getConnectType()
    {
        return (String)typeCombo.getSelectedItem();
    }

    private void init()
    {
        // --- Load types.
        typeCombo.addItem( "Sybase");
        typeCombo.addItem( "MSSQL");
        typeCombo.addItem( "MySQL");
        typeCombo.addItem( "HSQLDB");
        typeCombo.addItem( "SOAP");
        typeCombo.addItem( "ORACLE");
        typeCombo.addItem( "Postgresql");

        typeCombo.setEditable(true);

        String current;
        current = CProperties.getProperty( "CONNECT.TYPE","");

        typeCombo.setSelectedIndex(0);

        for( int i = 0; i < typeCombo.getItemCount(); i++)
        {
            String s;
            s = (String)typeCombo.getItemAt(i);

            if( s.equals( current))
            {
                typeCombo.setSelectedIndex(i);
            }
        }

        // ---- Load previous connections

        String temp;

        temp = CProperties.getProperty( "CONNECT.LIST","");

        String lastKey = CProperties.getProperty("CONNECT.KEY");

        temp = StringUtilities.decode( temp);

        StringTokenizer st= new StringTokenizer( temp, "\n");

        previous = HashMapFactory.create();
        while( st.hasMoreTokens())
        {
            String line = st.nextToken();

            StringTokenizer st2= new StringTokenizer( line, "\t");

            String user = "";

            if( st2.hasMoreTokens()) user = st2.nextToken();
            String url = "";

            if( st2.hasMoreTokens()) url = st2.nextToken();

//            String type = "";

//            if( st2.hasMoreTokens()) type = st2.nextToken();

            String key = user + "@" + url;

            previous.put( key, line);


            named.addItem( key);
        }

        if( lastKey != null && lastKey.equals( "") == false)
        {
            named.setSelectedItem( lastKey);
        }

        // ----- Load last user id
        if( CProperties.getProperty("USER.ID") != null)
        {
            userIdField.setText(CProperties.getProperty("USER.ID"));
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        named = new javax.swing.JComboBox();
        userIdField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        urlField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        passwdField = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Login");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(405, 300));
        jPanel3.setMinimumSize(new java.awt.Dimension(405, 300));
        jPanel3.setMaximumSize(new java.awt.Dimension(405, 300));
        jLabel1.setText("Connection:");
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        jPanel3.add(jLabel1, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel4.setPreferredSize(new java.awt.Dimension(200, 42));
        jPanel4.setMinimumSize(new java.awt.Dimension(200, 42));
        jPanel4.setMaximumSize(new java.awt.Dimension(200, 42));
        jPanel4.setAutoscrolls(true);
        named.setPreferredSize(new java.awt.Dimension(200, 22));
        named.setMinimumSize(new java.awt.Dimension(200, 22));
        named.setMaximumSize(new java.awt.Dimension(200, 22));
        named.setAutoscrolls(true);
        named.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namedActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
        jPanel4.add(named, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.ipadx = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel3.add(jPanel4, gridBagConstraints);

        userIdField.setPreferredSize(new java.awt.Dimension(150, 17));
        userIdField.setMinimumSize(new java.awt.Dimension(150, 17));
        userIdField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userIdFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = -30;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
        jPanel3.add(userIdField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(27, 20, 0, 0);
        jPanel3.add(jPanel2, gridBagConstraints);

        jPanel11.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 286;
        gridBagConstraints.ipady = -12;
        gridBagConstraints.insets = new java.awt.Insets(8, 40, 0, 0);
        jPanel3.add(jPanel11, gridBagConstraints);

        urlField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 216;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
        jPanel3.add(urlField, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(35, 70, 0, 0);
        jPanel3.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(35, 19, 0, 0);
        jPanel3.add(okButton, gridBagConstraints);

        jLabel5.setText("URL:");
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 69;
        gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 0);
        jPanel3.add(jLabel5, gridBagConstraints);

        jLabel4.setText("Type:");
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 62;
        gridBagConstraints.insets = new java.awt.Insets(18, 10, 0, 0);
        jPanel3.add(jLabel4, gridBagConstraints);

        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 0);
        jPanel3.add(typeCombo, gridBagConstraints);

        jLabel2.setText("User Id:");
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 45;
        gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 0);
        jPanel3.add(jLabel2, gridBagConstraints);

        jPanel1.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 286;
        gridBagConstraints.ipady = -12;
        gridBagConstraints.insets = new java.awt.Insets(20, 40, 0, 0);
        jPanel3.add(jPanel1, gridBagConstraints);

        passwdField.setPreferredSize(new java.awt.Dimension(150, 17));
        passwdField.setMinimumSize(new java.awt.Dimension(150, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = -30;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel3.add(passwdField, gridBagConstraints);

        jLabel3.setText("Password:");
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 29;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        jPanel3.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jPanel3, gridBagConstraints);

        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }//GEN-END:initComponents

    private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_typeComboActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void namedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namedActionPerformed
        // Add your handling code here:
        String temp = (String)named.getSelectedItem();

        if( temp != null)
        {
            String line = (String)previous.get( temp);

            StringTokenizer st2= new StringTokenizer( line, "\t");

            String user = "";
            if( st2.hasMoreTokens()) user = st2.nextToken();
            String url = "";
            if( st2.hasMoreTokens()) url =st2.nextToken();
            String type = "";
            if( st2.hasMoreTokens()) type = st2.nextToken();

            userIdField.setText( user);
            urlField.setText( url);
            typeCombo.setSelectedItem( type);

            passwdField.requestFocus();
        }
    }//GEN-LAST:event_namedActionPerformed

    private void urlFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_urlFieldActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Add your handling code here:
        try
        {
            DataBase dataBase;
            dataBase = new DataBase(
                userIdField.getText(),
                new String( passwdField.getPassword()),
                getConnectType(),
                getURL(), DataBase.Protection.NONE
            );
            dataBase.connect( );

            if( dataBase != null && dataBase.isConnected() == true)
            {
                if( master instanceof DataBaseConnection)
                {
                    ((DataBaseConnection)master).dataBaseConnected();
                }

                String key = getUserId() + "@" + getURL();

                String line = getUserId() + "\t" + getURL() + "\t" + getConnectType();

                previous.put( key, line);

                Object list[];

                list = previous.values().toArray();

                StringBuilder buffer = new StringBuilder();

                for (Object list1 : list) {
                    buffer.append(list1);
                    buffer.append( "\n");
                }

                String temp = StringUtilities.encode( buffer.toString());

                System.setProperty("CONNECT.LIST", temp);
                System.setProperty("CONNECT.LAST", key);
            }
            setVisible(false);
            dispose();

        }
        catch( Throwable e)
        {
            CMessage aMess = new CMessage(this, "Sorry - Failed to connect");
            aMess.waitForUser();
        }
       // finally
       // {
       //     if( master != null) master.status.setText( "");
       // }
    }//GEN-LAST:event_okButtonActionPerformed

    private void userIdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userIdFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_userIdFieldActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JComboBox named;
    private javax.swing.JButton okButton;
    private javax.swing.JPasswordField passwdField;
    private javax.swing.JComboBox typeCombo;
    private javax.swing.JTextField urlField;
    private javax.swing.JTextField userIdField;
    // End of variables declaration//GEN-END:variables

    private HashMap previous;
    private final CFrame master;
}
