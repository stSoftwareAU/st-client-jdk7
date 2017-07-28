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

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.util.*;
import java.io.*;

/**
 *  DocFrame
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 * @since 29 September 2006
 *  @since
 */
public class DocFrame extends CFrame
{
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    protected JMenuItem     openMItem,
                            newMItem,
                            closeMItem,
                            saveMItem,
                            
                            saveAsMItem;

    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    protected JMenu         windowMenu,
                            windowListMenu,
                            recentMenu;

    /**
     *
     */
    protected JDesktopPane desktopPane;
    /**
     *
     */
    //protected PleaseWait pleaseWait;

    /**
     *
     */
    public DocFrame()
    {
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(235, 245, 255));

        getContentPane().add("Center", desktopPane);
       // pleaseWait = new PleaseWait(this, "Processing...", false);
    }

    /**
     * 
     * @return the value
     */
    @Override
    protected boolean isAllowedToClose()
    {
        JInternalFrame list[];

        list = desktopPane.getAllFramesInLayer(
                JDesktopPane.MODAL_LAYER);

        if( list.length > 0) return false;

        list = desktopPane.getAllFramesInLayer(
                JDesktopPane.DEFAULT_LAYER);

        for( int i = 0; i < list.length; i++)
        {
            JInternalFrame jf;
            int p = list.length - i -1;

            jf = list[p];

            if( jf instanceof CInternalFrame)
            {
                CInternalFrame iFrame;

                iFrame = (CInternalFrame)jf;

                iFrame.toFront();

                if( iFrame.isAllowedToClose() == false)
                {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     *
     */
    @Override
    protected void doSensible()
    {
        super.doSensible();

        JInternalFrame list[];

        list = desktopPane.getAllFramesInLayer(
                JDesktopPane.MODAL_LAYER);

        if( list.length > 0) return;

        list = desktopPane.getAllFramesInLayer(
                JDesktopPane.DEFAULT_LAYER);

        CInternalFrame iFrame = null;

        for( int i = 0; i < list.length; i++)
        {
            JInternalFrame jf;
            int p = list.length - i -1;

            jf = list[p];

            if( jf.isIcon() == false && jf instanceof CInternalFrame)
            {
                iFrame = (CInternalFrame)jf;
            }
            else
            {
                iFrame = null;
            }
        }

        if( iFrame != null)
        {
            try
            {
                //iFrame.grabFocus();
                //desktopPane.moveToFront(iFrame);
                iFrame.setSelected(true);

            }
            catch( Exception e)
            {
                LOGGER.error( "Error", e);
            }

        }
    }

    /**
     * 
     * @param flag 
     */
    @Override
    public void setBusy( boolean flag)
    {
        super.setBusy( flag);
        openMItem.setEnabled( flag == false);
        newMItem.setEnabled( flag == false);
        closeMItem.setEnabled( flag == false);
        saveMItem.setEnabled( flag == false);

        saveAsMItem.setEnabled( flag == false);

        windowMenu.setEnabled( flag == false);
        windowListMenu.setEnabled( flag == false);
        if( recentMenu != null) recentMenu.setEnabled( flag == false);
    }

    /**
     * 
     * @return the value
     */
    public JDesktopPane getDesktop()
    {
        return desktopPane;
    }

    /**
     * 
     * @return the value
     */
    protected DocInternalFrame getDFrameWithFocus()
    {
        CInternalFrame iFrame;

        iFrame = getCurrentFrame();

        if( iFrame instanceof DocInternalFrame)
        {
            return (DocInternalFrame)iFrame;
        }

        return null;
    }

    /**
     * 
     * @return the value
     */
    protected CInternalFrame getCurrentFrame()
    {
        JInternalFrame list[];

        list = desktopPane.getAllFrames();

        for (JInternalFrame list1 : list) {
            if (list1.isSelected() && list1.isClosed() == false && list1.isVisible() == true && list1 instanceof CInternalFrame) {
                return (CInternalFrame) list1;
            }
        }

        return null;
    }

    @SuppressWarnings("empty-statement")
    public void tileWindows()
    {
        int w = -1;
        int h = -1;
        int n = 0;
        JInternalFrame list[];

        list = desktopPane.getAllFrames();

        for (JInternalFrame vmIF : list)
        {
            if (!vmIF.isIcon())
            {
                n++;
                if (w == -1)
                {
                    try
                    {
                        vmIF.setMaximum(true);
                        w = vmIF.getWidth();
                        h = vmIF.getHeight();
                    } 
                    catch (PropertyVetoException ex)
                    {
                        ;// Ignore
                    }
                }
            }
        }
        if (n > 0 && w > 0 && h > 0)
        {
            int rows = (int)Math.ceil(Math.sqrt(n));
            int cols = n / rows;
            if (rows * cols < n) cols++;
            int x = 0;
            int y = 0;
            w /= cols;
            h /= rows;
            int col = 0;
            for (JInternalFrame vmIF : list)
            {
                if (!vmIF.isIcon())
                {
                    try
                    {
                        vmIF.setMaximum(n==1);
                    } 
                    catch (PropertyVetoException ex)
                    {
                        ;// Ignore
                    }

                    if (n > 1)
                    {
                        vmIF.setBounds(x, y, w, h);
                    }
                    if (col < cols-1)
                    {
                        col++;
                        x += w;
                    } 
                    else
                    {
                        col = 0;
                        x = 0;
                        y += h;
                    }
                }
            }
        }
    }

    protected void restoreAllWindows()
    {
        JInternalFrame list[];

        list = desktopPane.getAllFrames();

        for (JInternalFrame iFrame : list)
        {
            try
            {
                iFrame.setIcon(false);
            }
            catch (PropertyVetoException ex)
            {
                LOGGER.warn( "could not restore", ex);
            }
        }
    }

    protected void minimizeAllWindows()
    {
        JInternalFrame list[];

        list = desktopPane.getAllFrames();

        for (JInternalFrame iFrame : list)
        {
            try
            {
                iFrame.setIcon(true);
            }
            catch (PropertyVetoException ex)
            {
                LOGGER.warn( "could not minimize", ex);
            }
        }
    }

    @SuppressWarnings("empty-statement")
    protected void cascadeWindows()
    {
        int n = 0;
        int w = -1;
        int h = -1;
        JInternalFrame list[];

        list = desktopPane.getAllFrames();

        for (JInternalFrame vmIF : list)
        {
            if (!vmIF.isIcon())
            {
                try
                {
                    vmIF.setMaximum(false);
                }
                catch (PropertyVetoException ex)
                {
                    ;// Ignore
                }
                n++;
                vmIF.pack();
                if (w == -1)
                {
                    try
                    {
                        w = vmIF.getWidth();
                        h = vmIF.getHeight();
                        vmIF.setMaximum(true);
                        w = vmIF.getWidth() - w;
                        h = vmIF.getHeight() - h;
                        vmIF.pack();
                    }
                    catch (PropertyVetoException ex)
                    {
                        ;// Ignore
                    }
                }
            }
        }
        int x = 0;
        int y = 0;
        int dX = (n > 1) ? (w / (n - 1)) : 0;
        int dY = (n > 1) ? (h / (n - 1)) : 0;
        for (JInternalFrame vmIF : list)
        {
            if (!vmIF.isIcon())
            {
                vmIF.setLocation(x, y);
                vmIF.moveToFront();
                x += dX;
                y += dY;
            }
        }
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Component getComponentToPrint()
    {
        return getCurrentFrame();
    }

    /**
     * 
     * @param me 
     */
    @Override
    public void menuSelected(MenuEvent me)
    {
        super.menuSelected( me);

        Object o;

        o = me.getSource();

        if( (o instanceof JMenu) == false) return;

        JMenu menu;

        menu = (JMenu)o;

        if( menu == windowMenu)
        {
            if( windowListMenu.getItemCount() != 0)
            {
                windowListMenu.removeAll();
            }

            JInternalFrame list[];

            list = desktopPane.getAllFrames();

            for (JInternalFrame list1 : list) {
                windowListMenu.add(new WMenuItem(list1));
            }

            if( desktopPane.getComponentCountInLayer( JDesktopPane.MODAL_LAYER) > 0)
            {
                windowListMenu.setEnabled( false);
            }
            else
            {
                windowListMenu.setEnabled( true);
            }
        }
        else if( menu.getText().equals( "File"))
        {
            CInternalFrame iFrame;

            iFrame = getCurrentFrame();
            DocInternalFrame dFrame = null;
            if( iFrame instanceof DocInternalFrame)
            {
                dFrame = (DocInternalFrame)iFrame;
            }

            closeMItem.setEnabled( true);
            openMItem.setEnabled( true);
            newMItem.setEnabled( true);
            saveMItem.setEnabled( true);
            saveAsMItem.setEnabled( true);
            printMItem.setEnabled( true);

            if( iFrame == null)
            {
                saveMItem.setEnabled( false);
                saveAsMItem.setEnabled( false);
                closeMItem.setEnabled( false);
                printMItem.setEnabled( false);
            }
            else
            {
                if( dFrame == null)
                {
                    saveMItem.setEnabled( false);
                    saveAsMItem.setEnabled( false);
                }
                else
                {
                    if( dFrame.getFile() == null)
                    {
                        saveAsMItem.setEnabled( false);
                    }
                }

                if( iFrame.isClosable() == false)
                {
                    closeMItem.setEnabled( false);
                }
            }
        }
    }

    /**
     * 
     * @return the value
     */
    protected boolean handlesDocs()
    {
        return true;
    }

    /**
     * 
     * @param menu 
     */
    @Override
    protected void addMenuItems( JMenu menu)
    {
        if( menu.getText().equals( "File"))
        {
            // New
            newMItem = new JMenuItem( "New");
            menu.add( newMItem);
            newMItem.setAccelerator( KeyStroke.getKeyStroke( 'N', Event.CTRL_MASK));
            newMItem.setMnemonic('N');
            newMItem.addActionListener(this);

            // Open
            openMItem = new JMenuItem( "Open...");
            openMItem.setAccelerator( KeyStroke.getKeyStroke( 'O', Event.CTRL_MASK));
            openMItem.setMnemonic('O');
            openMItem.addActionListener(this);
            if( handlesDocs()) menu.add( openMItem);

            // Close
            closeMItem = new JMenuItem( "Close");
            closeMItem.setAccelerator( KeyStroke.getKeyStroke( 'W', Event.CTRL_MASK));
            closeMItem.addActionListener(this);
            closeMItem.setMnemonic('C');
            menu.add( closeMItem);

            menu.addSeparator();
            // Save
            saveMItem = new JMenuItem( "Save");
            saveMItem.setMnemonic('S');
            saveMItem.setAccelerator( KeyStroke.getKeyStroke( 'S', Event.CTRL_MASK));
            saveMItem.addActionListener(this);
            if( handlesDocs()) menu.add( saveMItem);

            // Save As
            saveAsMItem = new JMenuItem( "Save As...");
            saveAsMItem.addActionListener(this);
            saveAsMItem.setMnemonic('A');
            if( handlesDocs()) menu.add( saveAsMItem);

            if( handlesDocs())
            {
                // Recent
                recentMenu = new JMenu( "Recent");
                recentMenu.setMnemonic('R');
                doRecentMenu(null);
                menu.add( recentMenu);

                menu.addSeparator();
            }
        }

        if( menu.getText().equals( "Window"))
        {
            //Window List
            windowListMenu = new JMenu( "Window List");
            windowListMenu.setMnemonic('L');

            menu.add( windowListMenu);

            JMenuItem cascadeMI = new JMenuItem("Cascade");
            cascadeMI.setMnemonic('C');
            cascadeMI.addActionListener(
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        cascadeWindows();
                    }
                }
            );
            menu.add(cascadeMI);

            JMenuItem tileMI = new JMenuItem("Tile");
            tileMI.setMnemonic('T');
            tileMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                                         InputEvent.CTRL_MASK));
            tileMI.addActionListener(
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        tileWindows();
                    }
                }
            );
            menu.add(tileMI);

            JMenuItem minimizeAllMI = new JMenuItem("Minimize All");
            minimizeAllMI.setMnemonic('M');
            minimizeAllMI.addActionListener(
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        minimizeAllWindows();
                    }
                }
            );

            menu.add(minimizeAllMI);

            JMenuItem restoreAllMI = new JMenuItem("Restore All");
            restoreAllMI.setMnemonic('R');
            restoreAllMI.addActionListener(
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        restoreAllWindows();
                    }
                }
            );
            menu.add(restoreAllMI);

        }

        if( menu.getText().equals( "Edit"))
        {
            super.addMenuItems( menu);

            // Setup Window menu
            windowMenu = new JMenu("Window");
            windowMenu.setMnemonic('W');
            theBar.add(windowMenu);

            addMenuItems( windowMenu);

        }
        else
        {
            super.addMenuItems( menu);
        }
    }

    /**
     * 
     * @param file 
     */
    protected void doRecentMenu( String file)
    {
        if( recentMenu == null) return;

        if( recentMenu.getMenuComponentCount() != 0)
        {
            recentMenu.removeAll();
        }

        String t;

        t = CProperties.getProperty( "RECENT.FILES", "");

//        StringUtilities su = new StringUtilities();

        t = StringUtilities.decode( t);

        Vector recentList = new Vector();//NOPMD

        StringTokenizer st = new StringTokenizer( t, "\n");

        while( st.hasMoreTokens())
        {
            StringTokenizer lt = new StringTokenizer(  st.nextToken(), "\t");

            String f;
            f = lt.nextToken();
            long time;

            time = Long.parseLong( lt.nextToken());

            recentList.addElement( new RecentMItem( f, time ));
        }

        if( file != null)
        {
            recentList.addElement(
                new RecentMItem(
                    file,
                    System.currentTimeMillis()
                )
            );
        }

        Collections.sort( recentList);
        Collections.reverse(recentList);

        Hashtable uf = new Hashtable();

        for( int i = 0; i < recentList.size(); i++)
        {
            RecentMItem rm;

            rm = (RecentMItem)recentList.elementAt( i);

            if( uf.get( rm.getFile()) != null)
            {
                recentList.removeElementAt(i);
                i--;
            }

            uf.put( rm.getFile(),"");
        }

        int h;

        h = Integer.parseInt(
            CProperties.getProperty( "RECENT.FILES.MAX", "10")
        );

        t = "";

        for( int i = 0; i < h && i < recentList.size(); i++)
        {
            RecentMItem rm;

            rm = (RecentMItem)recentList.elementAt( i);

            rm.addActionListener(this);
                //fm.setMnemonic('A');
            recentMenu.add( rm);

            t += rm.getFile() + "\t" + rm.getTime() + "\n";
        }

        t = t.trim();

        recentMenu.setVisible( true);

        if( t.equals( ""))
        {
            recentMenu.setVisible( false);
        }

        System.getProperties().put( "RECENT.FILES", StringUtilities.encode( t));
    }

    /**
     * 
     * @param event the event
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if( event.getSource() == closeMItem)
        {
            doClose();
        }
        else if( event.getSource() == openMItem && handlesDocs())
        {
            doOpen();
        }
        else if( event.getSource() instanceof RecentMItem && handlesDocs())
        {
            doOpenRecent((RecentMItem)event.getSource());
        }
        else if( event.getSource() == newMItem)
        {
            setBusy( true);
            //pleaseWait.setVisible( true);
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        doNew( null);
                    }
                    finally
                    {
                        setBusy( false);
                       // pleaseWait.setVisible( false);
                    }
                }
            };

            SwingUtilities.invokeLater( r);
        }
        else if( event.getSource() == saveMItem && handlesDocs())
        {
            doSave();
        }
        else if( event.getSource() == saveAsMItem && handlesDocs())
        {
            doSaveAs();
        }
        else
        {
            super.actionPerformed( event);
        }
    }

    /**
     *
     */
    @Override
    public void close()
    {
        JInternalFrame iFrame;
        try
        {
            Component list[];

            list = desktopPane.getComponentsInLayer(
                    JLayeredPane.DEFAULT_LAYER);

            for (Component list1 : list) {
                if (list1 instanceof JInternalFrame) {
                    iFrame = (JInternalFrame) list1;
                    iFrame.setSelected( true);
                    iFrame.moveToFront();
                    if( iFrame instanceof CInternalFrame)
                    {
                        ((CInternalFrame)iFrame).doClose();
                    }
                    else
                    {
                        iFrame.setClosed(true);
                    }
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.error( "Opps", e);
            CMessage message = new CMessage( e.toString());
            message.setVisible( true);
        }
    }

    /**
     * 
     * @param item 
     */
    public void doOpenRecent( RecentMItem item)
    {
        DocInternalFrame iFrame = null;
        try
        {
            File file;

            file =         new File( item.getFile());

            if( file.exists() == false)
            {
                throw new Exception( file.toString() + " Does not exists");
            }

            iFrame = doNew( getMINEType( file));

            iFrame.openFile( file);

            doRecentMenu(file.toString());
        }
        catch( Exception e)
        {
            LOGGER.error( "Opps", e);
            CMessage message = new CMessage( e.toString());
            if( iFrame != null) iFrame.setVisible( false);
            message.setVisible( true);
        }
    }

    /**
     *
     */
    public void doOpen()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                FileDialog theFileDialog = new FileDialog(thisFrame, "Open File...");

                theFileDialog.show();

                String selectedFile = theFileDialog.getFile();

                if(selectedFile != null)
                {
                    DocInternalFrame iFrame = null;
                    try
                    {
                        File file;

                        file =         new File(
                            theFileDialog.getDirectory(),
                            selectedFile
                        );

                        iFrame = doNew( getMINEType( file));

                        iFrame.openFile( file);

                        doRecentMenu(file.toString());
                    }
                    catch( Exception e)
                    {
                        LOGGER.error( "Opps", e);
                        CMessage message = new CMessage( e.toString());
                        if( iFrame != null) iFrame.setVisible( false);
                        message.setVisible( true);
                    }
                }
            }
        };

        SwingUtilities.invokeLater( r);
    }

    /**
     * 
     * @param file 
     * @return the value
     */
    protected String getMINEType( File file)
    {
        String t;

        t = file.toString().toLowerCase();

        if( t.endsWith( "txt"))
        {
            return "text";
        }

        return null;
    }

    /**
     * 
     * @param type the type
     * @return the value
     */
    protected DocInternalFrame createInternalFrame( String type)
    {
        return new TextEditor( this);
    }

    /**
     * 
     * @param MINEType 
     * @return the value
     */
    public DocInternalFrame doNew( String MINEType)
    {
        final Object data[] = new Object[2];

        data[0] = MINEType;

        CApp.swingInvokeAndWait(
            new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        DocInternalFrame docInternalFrame;
                        docInternalFrame = createInternalFrame( (String)data[0]);

                        addFrame( docInternalFrame);
                        docInternalFrame.setVisible( true);
                        data[1] = docInternalFrame;
                    }
                    catch( Exception e)
                    {
                        LOGGER.error( "Error", e);
                    }
                }
            }
        );

        return (DocInternalFrame)data[1];
    }

    /**
     * 
     * @param aFrame 
     */
    public void addFrame( CInternalFrame aFrame)
    {
        desktopPane.add(
            aFrame,
            JLayeredPane.DEFAULT_LAYER
        );

        aFrame.moveToFront();
        aFrame.grabFocus();
        try
        {
            aFrame.setSelected( true);
        }
        catch( Exception e)
        {
            LOGGER.info("select", e);
        }
    }

    /**
     *
     */
    public void doSave()
    {
        DocInternalFrame iFrame;
        try
        {
            iFrame = getDFrameWithFocus();

            if( iFrame != null)
            {
                if( iFrame.getFile() == null)
                {
                    iFrame.doSaveAs();
                }
                else
                {
                    iFrame.doSave();
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.error( "Opps", e);
            CMessage message = new CMessage( e.toString());
            message.setVisible( true);
        }
    }

    /**
     *
     */
    public void doClose()
    {
        DocInternalFrame iFrame;
        try
        {
            iFrame = getDFrameWithFocus();

            if( iFrame != null)
            {
                if( iFrame.isAllowedToClose())
                {
                    iFrame.doClose();
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.error( "Opps", e);
            CMessage message = new CMessage( e.toString());
            message.setVisible( true);
        }
    }

    /**
     *
     */
    public void doSaveAs()
    {
        DocInternalFrame iFrame;
        try
        {
            iFrame = getDFrameWithFocus();

            if( iFrame != null)
            {
                iFrame.doSaveAs();

                doRecentMenu(iFrame.getFile().toString());
            }
        }
        catch( Exception e)
        {
            LOGGER.error( "Opps", e);
            CMessage message = new CMessage( e.toString());
            message.setVisible( true);
        }
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.DocFrame");//#LOGGER-NOPMD
}
