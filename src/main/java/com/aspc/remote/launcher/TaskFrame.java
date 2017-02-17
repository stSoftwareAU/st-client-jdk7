/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
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
package com.aspc.remote.launcher;

import com.aspc.remote.tail.TailLine;
import com.aspc.remote.tail.TailQueue;
import com.aspc.remote.tail.TailStream;
import com.aspc.remote.tail.TailTextPane;
import com.aspc.remote.util.misc.ThreadPool;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

/**
 *  <i>THREAD MODE: SINGLETON frame SINGLE-THREADED</i>
 *  @author      Nigel Leck
 *  @since       24 May 2009

 */
public class TaskFrame extends javax.swing.JInternalFrame implements Runnable
{
    private final AtomicBoolean completed=new AtomicBoolean();
    private final TailQueue queue=new TailQueue();
    private final TailTextPane ttPane;
    private final TailStream outTail=new TailStream( queue, null);
    private final TailStream errTail=new TailStream( queue, Color.RED);
    /**
     * Creates new form TaskFrame
     * @param target the target
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public TaskFrame( final JFrame frame, final String target)
    {
        initComponents();
        final InputDialog id=new InputDialog( frame, true);
        final DefaultLogger logger = new DefaultLogger();
        ttPane=new TailTextPane( tailTextPane);
        PrintStream errStream = new PrintStream(errTail);
        PrintStream outStream = new PrintStream(outTail);
        logger.setErrorPrintStream( errStream);
        logger.setOutputPrintStream( outStream);
        logger.setMessageOutputLevel(Project.MSG_INFO);
        setTitle( target);
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Util.runTarget(target,logger, queue, id);
                }
                catch( Throwable t)
                {
                    doError( t);
                }
                finally
                {
                    doComplete();
                }
            }
        };

        ThreadPool.schedule(r, "run: " + target);
        ThreadPool.schedule(this, "monitor: " + target);
    }

    private void doError( Throwable e)
    {
        StringBuilder buffer = new StringBuilder( );
        //buffer.append("\n");
        Throwable tmpE =e;
        while( tmpE != null)
        {
            StringWriter w = new StringWriter( );
            PrintWriter pw = new PrintWriter( w);
            e.printStackTrace(pw);

            buffer.append( w.toString());

            tmpE = tmpE.getCause();
            buffer.append("\n");
        }

        try
        {
            outTail.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
        }
        catch( IOException ioE)
        {

        }
    }

    /** {@inheritDoc } */
    @Override
    public void run()
    {
        while( completed.get() == false)
        {
            try
            {
                checkTail();
            }
            catch( Exception e)
            {
                doError( e);
                break;
            }
        }
        try
        {
            checkTail();
        }
        catch( Exception e)
        {
           doError( e);
        }
        finally
        {
            queue.close();
        }
    }

    private void checkTail() throws Exception
    {
        do
        {
            TailLine line = queue.nextLine();
            ttPane.addLine(line);
        }
        while( queue.isEmpty() == false);
    }

    private void doComplete( )
    {
        completed.set(true);
        SwingUtilities.invokeLater(
            new Runnable()
            {
                @Override
                public void run()
                {
                    setClosable(true);
                }
            }
        );
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
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane tailTextPane;
    // End of variables declaration//GEN-END:variables

}
