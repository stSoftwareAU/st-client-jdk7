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

import com.aspc.remote.application.DocFrame;
import com.aspc.remote.application.HelpUtil;
import com.aspc.remote.tail.TailFrame;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.HeadlessException;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.Target;

/**
 * A copy of JConsole.java. Supports StServer menu item in the menu bar.
 * THREAD MODE: SINGLE THREAD
 * @author padma
 * @since 27 Jun 2009
 */
@SuppressWarnings("serial")
public class Console extends DocFrame
{
    private static File currentDir;
    private static String baseDir = "";

    /**
     * new console
     */
    public Console()
    {
        super();
    }

    /**
     * create the main window
     * @return the window
     * @throws Exception a serious problem
     */
    public static Console mainWidnow() throws Exception
    {

        final Console holder[]=new Console[1];
        // Always create Swing GUI on the Event Dispatching Thread
        SwingUtilities.invokeAndWait(() -> {
            final Console console= new Console();
            console.setVisible(true);
            JMenuBar menuBar1 = console.getJMenuBar();
            JMenu helpMenu=null;
            JMenu fileMenu1 = null;
            JMenu editMenu1 = null;
            int count = menuBar1.getMenuCount();
            for (int i = 0; i < count; i++) {
                JMenu menu = menuBar1.getMenu(i);
                if (menu.getText().startsWith("Help")) {
                    helpMenu = menu;
                } else if (menu.getText().startsWith("File")) {
                    fileMenu1 = menu;
                } else if (menu.getText().startsWith("Edit")) {
                    editMenu1 = menu;
                }
            }
            if (fileMenu1 != null) {
                menuBar1.remove(fileMenu1);
            }
            if (helpMenu != null) {
                menuBar1.remove(helpMenu);
            }
            boolean remoteMode = false;
            try
            {
                baseDir = Util.getBaseDir();
                remoteMode = Util.isRemoteMode();
            } catch (Exception e)
            {
                LOGGER.error("Error when obtain launcher.xml properties", e);
            }
            if (editMenu1 != null) {
                JMenuItem localPro = new JMenuItem();
                localPro.setText("Remote Properties");
                localPro.addActionListener((ActionEvent e) -> {
                    String fileName = baseDir + "/conf/remote.properties";
                    try
                    {
                        if (baseDir != null && !baseDir.equals(""))
                        {
                            File file = new File(baseDir + "/conf");
                            if (!file.exists())
                            {
                                file.mkdir();
                            }
                            file = new File(fileName);
                            
                            if (!file.exists())
                            {
                                file.createNewFile();
                                InputStream in = Util.class.getResourceAsStream("/com/aspc/Install/Common/support/conf/remote.properties");
                                if (in == null || in.available() <= 0)
                                {
                                    in = Util.class.getResourceAsStream("/com/aspc/remote/remote.properties");
                                }
                                if (in != null && in.available() > 0)
                                {
                                    try (OutputStream out = new FileOutputStream(file)) {
                                        int read;
                                        byte[] bytes = new byte[1024];
                                        
                                        while ((read = in.read(bytes)) != -1)
                                        {
                                            out.write(bytes, 0, read);
                                        }
                                        in.close();
                                        out.flush();
                                    }
                                }
                            }
                            if (java.awt.Desktop.isDesktopSupported())
                            {
                                java.awt.Desktop.getDesktop().edit(file);
                                
                            } else
                            {
                                JOptionPane.showMessageDialog(null, "Can't open remote property file: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else
                        {
                            JOptionPane.showMessageDialog(null, "Can't locate project base dir", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException | HeadlessException ex)
                    {
                        JOptionPane.showMessageDialog(null, "Error when opening remote property file: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                editMenu1.add(localPro);
                int itemCount = editMenu1.getItemCount();
                JMenuItem temp = null;
                for (int i = 0; i < itemCount; i++) {
                    JMenuItem item = editMenu1.getItem(i);
                    if (item != null && item.getText().contains("Preferences"))
                    {
                        if (remoteMode)
                        {
                            temp = item;
                            break;
                        } 
                        else
                        {
                            item.setText("Properties");
                            item.addActionListener((ActionEvent e) -> {
                                String fileName = baseDir + "/conf/common.properties";
                                try
                                {
                                    if (baseDir != null && !baseDir.equals(""))
                                    {
                                        File file = new File(baseDir + "/conf");
                                        if (!file.exists())
                                        {
                                            file.mkdir();
                                        }
                                        file = new File(fileName);
                                        if (!file.exists())
                                        {
                                            file.createNewFile();
                                            
                                            InputStream in = Util.class.getResourceAsStream("/com/aspc/Install/Common/support/conf/common.properties");
                                            
                                            if (in != null && in.available() > 0)
                                            {
                                                try (OutputStream out = new FileOutputStream(file)) {
                                                    int read;
                                                    byte[] bytes = new byte[1024];
                                                    
                                                    while ((read = in.read(bytes)) != -1)
                                                    {
                                                        out.write(bytes, 0, read);
                                                    }
                                                    in.close();
                                                    out.flush();
                                                }

                                            }
                                        }
                                        if (java.awt.Desktop.isDesktopSupported())
                                        {
                                            java.awt.Desktop.getDesktop().edit(file);
                                            
                                        } else
                                        {
                                            JOptionPane.showMessageDialog(null, "Can't open property file: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } else
                                    {
                                        JOptionPane.showMessageDialog(null, "Can't locate project base dir", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (IOException | HeadlessException ex)
                                {
                                    JOptionPane.showMessageDialog(null, "Error when opening property file: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            break;
                        }
                    }
                }
                if (temp != null) {
                    editMenu1.remove(temp);
                }
            }
            HelpUtil.register(console);
            JMenu runMenu = new JMenu( "Run", true);
            runMenu.setMnemonic('R');
            menuBar1.add(runMenu, 0);
            try
            {
                Map targetTable = Util.listTargets();
                int size = targetTable.size();
                String targets[] = new String[size];
                targetTable.keySet().toArray(targets);
                Arrays.sort(targets);
                
                ActionListener al = (ActionEvent e) -> {
                    TaskFrame tf = new TaskFrame( console,  e.getActionCommand());
                    console.desktopPane.add(tf);
                    tf.setVisible(true);
                };
                JMenu targetMenu=runMenu;
                HashMap<String,String> addedMap =HashMapFactory.create();
                int lastSize = 0;
                for( int loop = 0; loop < 3;loop++)
                {
                    if( loop == 1 )
                    {
                        if( runMenu.getItemCount() > 0)
                        {
                            runMenu.addSeparator();
                            lastSize = runMenu.getItemCount();
                        }
                    }
                    else if( loop == 2)
                    {
                        if( runMenu.getItemCount() > lastSize)
                        {
                            runMenu.addSeparator();
                            
                            targetMenu = new JMenu( "Other Targets");
                            runMenu.add(targetMenu);
                        }
                    }

                    for( String target: targets)
                    {
                        if( addedMap.containsKey(target)) continue;
                        
                        if( target.startsWith("-"))
                        {
                            addedMap.put(target, "");
                            continue;
                        }
                        
                        Target antTarget = (Target) targetTable.get(target);
                        String description = antTarget.getDescription();
                        
                        if( loop == 0)
                        {
                            String defaultTarget = antTarget.getProject().getDefaultTarget();
                            
                            if( target.equals(defaultTarget) == false)
                            {
                                continue;
                            }
                        }
                        else if( loop == 1)
                        {
                            if( StringUtilities.isBlank(description)) continue;
                        }
                        
                        JMenuItem targetItem;
                        
                        targetItem = new JMenuItem(target);
                        //aboutMItem.setMnemonic('T');
                        targetItem.setToolTipText(description);
                        targetItem.addActionListener(
                                al
                        );
                        targetMenu.add( targetItem);
                        
                        addedMap.put(target, "");
                    }
                }
            }
            catch( Exception e)
            {
                LOGGER.info( "could not list targets", e);
            }
            JMenuItem exitMI = new JMenuItem("Exit");
            exitMI.setMnemonic('x');
            exitMI.addActionListener((ActionEvent e) -> {
                System.exit(0);
            });
            //if( runMenu.getItemCount() > 0)
            // {
            runMenu.addSeparator();
            // }
            JMenuItem tailMI = new JMenuItem("Tail");
            tailMI.setMnemonic('t');
            tailMI.addActionListener((ActionEvent e) -> {
                JFileChooser chooser = new JFileChooser(currentDir);
                int returnVal = chooser.showOpenDialog(console);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = new File(chooser.getCurrentDirectory(), chooser.getSelectedFile().getName());
                    
                    currentDir=file.getParentFile();
                    TailFrame tf=new TailFrame( file);
                    console.desktopPane.add(tf);
                    tf.setVisible(true);
                }
            });
            runMenu.add(tailMI);
            runMenu.add(exitMI);
            console.setTitle("st Console");
            holder[0]=console;
        });

        return holder[0];
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.launcher.Console");//#LOGGER-NOPMD

}
