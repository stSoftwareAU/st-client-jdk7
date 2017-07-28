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
package com.aspc.remote.tail;

import com.aspc.remote.application.ExceptionDialog;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CUtilities;
import com.aspc.remote.util.misc.ThreadPool;
import java.io.File;
import org.apache.commons.logging.Log;

/**
 *  <i>THREAD MODE: SINGLETON frame SINGLE-THREADED</i>
 *  @author      Nigel Leck
 *  @since       24 May 2009

 */
public class TailFrame extends javax.swing.JInternalFrame implements Runnable
{
    private final TailQueue queue=new TailQueue();
    private final TailTextPane ttPane;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.tail.TailFrame");//#LOGGER-NOPMD
    private final TailFile tailFile;

    /**
     * Creates new form TaskFrame
     * @param file the file to tail
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public TailFrame( final File file)
    {
        initComponents();

        setClosable(true);
        setTitle( "Tail: " + file.toString());
        ttPane=new TailTextPane( tailTextPane);
        ThreadPool.schedule(this, "display: " + file);
        tailFile=new TailFile( file, queue);
        tailFile.schedule();
    }

    private void doError( Throwable e)
    {
        ExceptionDialog ed = new ExceptionDialog( null, true, e);
        ed.setVisible(true);
        CUtilities.center(this, ed);
    }

    /** {@inheritDoc } */
    @Override
    public void run()
    {
        while( isClosed == false)
        {
            try
            {
                checkTail();
            }
            catch( Exception e)
            {
                doError( e);
            }
        }

        queue.close();
    }

    private void checkTail() throws Exception
    {
        TailLine line = queue.nextLine();
        ttPane.addLine(line);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jScrollPane2 = new javax.swing.JScrollPane();
        tailTextPane = new javax.swing.JTextPane();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        tailTextPane.setBackground(java.awt.Color.black);
        tailTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tailTextPane.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        tailTextPane.setForeground(java.awt.Color.green);
        tailTextPane.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane2.setViewportView(tailTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane tailTextPane;
    // End of variables declaration//GEN-END:variables

}
