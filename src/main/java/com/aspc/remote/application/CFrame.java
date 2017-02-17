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
package com.aspc.remote.application;

import org.apache.commons.logging.Log;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.aspc.remote.util.misc.*;

/**
 *  CFrame general Error utilities
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *
 *  @author      Nigel Leck
 *  @since       1 September 1996
 */
public class CFrame extends JFrame implements   ActionListener,
                                                ComponentListener,
                                                MenuListener,
                                                WindowListener
{
    private static final long serialVersionUID = 42L;

    /**
     *
     */
    public static final String MEMU_FILE="File";
    
    /**
     *
     */
    public JMenuBar     theBar;

    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    public JMenu        fileMenu,
                        
                        editMenu;

    /**
     *
     */
    public JMenuItem    quitMItem,
                        printMItem,
                        prefMItem,
                        cutMItem,
                        copyMItem,
                        pasteMItem,
                        sAllMItem;

    /**
     * main panel
     */
    protected           CPanel        mainPanel;

    /**
     * scroll panel
     */
    protected           JScrollPane   scrollPane;

    /**
     * the frame
     */
    public CFrame thisFrame;

    /**
     *
     */
    protected JComponent lastComponentWithFocus;

    /**
     *
     */
    protected static final Hashtable wList = new Hashtable();

    /**
     *
     */
    public CFrame()
    {
        this("Untitled");
    }

    /**
     * 
     * @param title 
     */
    public CFrame( String title)
    {
        super(title);
        thisFrame = this;
        init();//NOPMD
    }

    /**
     * 
     * @param flag 
     */
    public void setBusy( boolean flag)
    {

        //quitMItem,
        printMItem.setEnabled( flag == false);
        prefMItem.setEnabled( flag == false);
        /*
        cutMItem,
        copyMItem,
        pasteMItem,
        sAllMItem,
        */

       

    }

    /**
     *
     */
    public void init()
    {
        setUpMenu();
        wList.put( this, this);

        CApp.checkSwingValid( this);
        if( getTitle().equals("") == true)
        {
            setTitle("Untitled");
        }

        setResizable(true);

        //status = new StatusBar( this);
        //getContentPane().add("South", status);

        mainPanel = new CPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add("Center", mainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE );
        addWindowListener( this);

        HelpUtil.register(this);
    }

    /**
     * 
     * @return the value
     */
    public CPanel getMainPanel()
    {
        return mainPanel;
    }

    /**
    * Scan the list of components for the one with the focus.
    */
    private Component findFocus( Component[] list)
    {
        Component gotIt = null;

        for( int i = 0; i < list.length && gotIt == null; i++)
        {
            Component c;
            c = list[i];

            if( c instanceof JComponent)
            {
                JComponent j;
                j = (JComponent)c;

                if( j.hasFocus())
                {
                    gotIt = j;
                    continue;
                }
            }

            if( c instanceof Container)
            {
                gotIt = findFocus( ((Container)c).getComponents());
            }
        }

        return gotIt;
    }


    /**
     * Finds the component in this frame that has the focus.
     * @return the value
     */
    public synchronized JComponent getComponentWithFocus()
    {
        Component c;

        c = findFocus( getComponents());

        if( c instanceof JMenu  || c instanceof JRootPane  ||c  == null)
        {
            c = lastComponentWithFocus;
        }

        lastComponentWithFocus = (JComponent)c;

        return lastComponentWithFocus;
    }

    /**
     * 
     * @param aClass 
     * @return the value
     */
    public synchronized JComponent getComponentWithFocus( Class aClass)
    {
        JComponent c;

        c = getComponentWithFocus();

        while( c != null)
        {
            if( aClass.isInstance( c))
            {
                return c;
            }

            Component t;
            t = c.getParent();
            c = (JComponent)t;
        }

        return null;
    }

    /**
     * 
     * @param menu 
     */
    protected void addMenuItems( JMenu menu)
    {
        menu.addMenuListener( this);

        addMenuSection( menu, "1", true);
        addMenuSection( menu, "2", true);
        addMenuSection( menu, "L", true);
    }

    /**
     * 
     * @param menu 
     * @param section 
     * @param allowSeparator 
     */
    public void addMenuSection(
        JMenu menu,
        String section,
        boolean allowSeparator
    )
    {
        if( section.equals( "1"))
        {
            if( menu.getText().equals( "File"))
            {
                //Print
                //printSetupMItem = new JMenuItem( "Print Setup...");
                //menu.add( printSetupMItem);

                printMItem = new JMenuItem( "Print");

                printMItem.setAccelerator( KeyStroke.getKeyStroke( 'P', Event.CTRL_MASK));
                printMItem.addActionListener(this);
                printMItem.setMnemonic( 'P');
                menu.add( printMItem);

                if( allowSeparator == true)
                {
                    menu.add( new JSeparator());
                }

                //Quit
                quitMItem = new JMenuItem( "Quit");
                quitMItem.setAccelerator( KeyStroke.getKeyStroke( 'Q', Event.CTRL_MASK));
                quitMItem.setMnemonic( 'Q');

                quitMItem.addActionListener(this);

                menu.add( quitMItem);
            }
            else if( menu.getText().equals( "Edit"))
            {
                cutMItem = new JMenuItem("Cut");
                cutMItem.setAccelerator( KeyStroke.getKeyStroke( 'X', Event.CTRL_MASK));
                menu.add( cutMItem);
                cutMItem.addActionListener(this);

                copyMItem = new JMenuItem("Copy");
                copyMItem.setAccelerator( KeyStroke.getKeyStroke( 'C', Event.CTRL_MASK));
                menu.add( copyMItem);
                copyMItem.addActionListener(this);

                pasteMItem = new JMenuItem("Paste");
                pasteMItem.setAccelerator( KeyStroke.getKeyStroke( 'V', Event.CTRL_MASK));
                menu.add( pasteMItem);
                pasteMItem.addActionListener(this);
            }
        }
        else if( section.equals( "2"))
        {
            if( menu.getText().equals( "Edit"))
            {
                if( allowSeparator == true)
                {
                    menu.add( new JSeparator());
                }

                sAllMItem = new JMenuItem("Select All");
                sAllMItem.setAccelerator( KeyStroke.getKeyStroke( 'A', Event.CTRL_MASK));
                menu.add( sAllMItem);
                sAllMItem.addActionListener(this);
            }
        }

        if( section.equals("L"))
        {
            if( menu.getText().equals( "Edit"))
            {
                if( allowSeparator == true)
                {
                    menu.add( new JSeparator());
                }
                prefMItem = new JMenuItem("Preferences...");
                menu.add( prefMItem);
                prefMItem.addActionListener(this);
            }
        }
    }

    /**
     *
     */
    public void setUpMenu()
    {
        try
        {
            theBar = new JMenuBar();

            // Setup FILE menu
            fileMenu = new JMenu("File");
            fileMenu.setMnemonic('F');
            theBar.add(fileMenu);

            addMenuItems( fileMenu);

            // Setup EDIT menu
            editMenu = new JMenu("Edit");
            editMenu.setMnemonic('E');
            theBar.add(editMenu);
            addMenuItems( editMenu);

            //Gives me a chance to turn edit items on and off.
            editMenu.addMenuListener( this);

            // Setup screen
            setJMenuBar(theBar);

            addComponentListener( this);

            validate();
        }
        catch(Exception e)
        {
            LOGGER.error( "Set up of memu", e);
        }
    }

    /**
     * 
     * @param flag 
     */
    @Override
    public void setVisible(boolean flag)
    {
        if( flag == true)
        {
            String temp;
            int x = 0,
                y = 0,
                h = 0,
                w = 0,
                i,
                j;

            validate();

            temp = CProperties.getProperty(
                getFrameKey(),
                "50,50,640,480"
            );
            StringTokenizer st;

            try
            {
                st = new StringTokenizer(temp,",");
                for(i = 0; st.hasMoreTokens(); i++)
                {
                     temp = st.nextToken();
                     j = Integer.parseInt( temp);
                     switch( i)
                     {
                        case 0:
                            x = j;
                            break;
                        case 1:
                            y = j;
                            break;
                        case 2:
                            w = j;
                            break;
                        case 3:
                            h = j;
                            break;
                        default:
                    }
                }
                setBounds( x, y, w, h);
            }
            catch( Exception e)
            {
                LOGGER.info( "Error - restoring screen bounds", e);
            }
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension fd;
            fd = getSize();

            setLocation(
                screenSize.width/2 - fd.width/2,
                screenSize.height/2 - fd.height/2
            );

            //frameList.put( this, this);
        }
//        else
//        {
            //frameList.remove( this);
//        }

        super.setVisible( flag);
    }

    /**
     * 
     * @param event the event
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if( event.getSource() == printMItem)
        {
            doPrint();
        }
        else if( event.getSource() == prefMItem)
        {
            doPrefs();
        }
        else if( event.getSource() == quitMItem)
        {
            doQuit();
        }
        else if( event.getSource() == copyMItem)
        {
            doCopy();
        }
        else if( event.getSource() == cutMItem)
        {
            doCut();
        }
        else if( event.getSource() == pasteMItem)
        {
            doPaste();
        }
        else if( event.getSource() == sAllMItem)
        {
            doSelectAll();
        }
    }

    /**
     *
     */
    public static void makeSensible()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Enumeration e;

                e = wList.elements();

                while( e.hasMoreElements())
                {
                    Object o;

                    o = e.nextElement();

                    if( o instanceof CFrame)
                    {
                        CFrame aFrame;

                        aFrame = (CFrame)o;

                        aFrame.doSensible();
                    }
                }
            }
        };

        SwingUtilities.invokeLater(r);
    }

    /**
     *
     */
    protected void doSensible()
    {
    }

    /**
     *
     */
    public void doPrefs()
    {
       // new CDBaseAppOptions(this).setVisible( true);
    }

    /**
     *
     */
    public void doQuit()
    {
       Runnable r = new Runnable()
       {
            @Override
            public void run()
            {
                if( isAllowedToClose() == true)
                {
                    close();
                    if( CApp.gApp != null)
                    {
                        CApp.gApp.doQuit();
                    }
                    else
                    {
                        System.exit(0);
                    }
                }
            }
        };

        SwingUtilities.invokeLater(r);
    }

    /**
     * 
     * @return the value
     */
    public Component getComponentToPrint()
    {
        return this;
    }

    /**
     *
     */
    protected void doPrint()
    {
        Printer printer = new Printer( this);
        Component c;

        c = getComponentToPrint();

        if( c == null) return;

        printer.print( c);

    }

    /**
     * 
     * @return the value
     */
    protected boolean isAllowedToClose()
    {
        return true;
    }

    /**
     * 
     * @return the value
     */
    public String getFrameKey()
    {
        String key;

        key = getTitle().toUpperCase();
        StringTokenizer st = new StringTokenizer( key, "-");
        key = st.nextToken().trim();
//        StringUtilities su = new StringUtilities();

        key = StringUtilities.replace( key, " ", "_");
        return "SCREEN_RECTANGLE." + key;
    }

    /**
     *
     */
    public void close()
    {
        Properties p;

        String        value;

        Rectangle r;

        p = System.getProperties();

        setVisible(false);         // hide the Frame

        r = getBounds();
        value = r.x + "," + r.y + "," + r.width + "," + r.height;

        p.put( getFrameKey(), value);

        System.setProperties(p);

        wList.remove( this);
    }

    /**
     * 
     * @param e 
     */
    @Override
    public void componentHidden(ComponentEvent e)
    {
    }

    /**
     * 
     * @param e 
     */
    @Override
    public void componentMoved(ComponentEvent e)
    {
    }

    /**
     * 
     * @param e 
     */
    @Override
    public void componentResized(ComponentEvent e)
    {
        Dimension cd, md;
        Insets insets;

        insets = getInsets();

        cd = getSize();
        md = getMinimumSize();
        boolean changed = false;

        if( cd.width + insets.left + insets.right < md.width)
        {
            changed = true;
            cd.width = md.width;
        }

        if( cd.height + insets.top + insets.bottom  < md.height)
        {
            changed = true;
            cd.height = md.height;
        }

        if( changed == true)
        {
            Toolkit.getDefaultToolkit().beep();
            setSize( cd);
        }
    }

    /**
     * 
     * @param e 
     */
    @Override
    public void componentShown(ComponentEvent e)
    {
    }

    // Menu Listener
    /**
     * 
     * @param me 
     */
    @Override
    public void menuCanceled(MenuEvent me)
    {
        if( me.getSource() == editMenu)
        {
            turnEdit(true);
        }
    }

    /**
     * 
     * @param me 
     */
    @Override
    public void menuDeselected(MenuEvent me)
    {
        if( me.getSource() == editMenu)
        {
            turnEdit(true);
        }
    }

    /**
     * 
     * @param me 
     */
    @Override
    public void menuSelected(MenuEvent me)
    {
        if( me.getSource() == editMenu)
        {
            Component f;

            f = getComponentWithFocus();

            if(
                f == null ||
                f instanceof JTextComponent == false
            )
            {
                turnEdit(false);
            }
        }
    }

    private void turnEdit( boolean flag)
    {
        cutMItem.setEnabled( flag);
        copyMItem.setEnabled( flag);
        pasteMItem.setEnabled( flag);
        sAllMItem.setEnabled( flag);
    }

    /**
     * Invoked when a window has been opened.
     * @param e 
     */
    @Override
    public void windowOpened(WindowEvent e)
    {
    }

    /**
     * Invoked when a window is in the process of being closed.
     * The close operation can be overridden at this point.
     * @param e 
     */
    @Override
    public void windowClosing(WindowEvent e)
    {
        doQuit();
    }

    /**
     * Invoked when a window has been closed.
     * @param e 
     */
    @Override
    public void windowClosed(WindowEvent e)
    {
    }

    /**
     * Invoked when a window is iconified.
     * @param e 
     */
    @Override
    public void windowIconified(WindowEvent e)
    {
    }

    /**
     * Invoked when a window is de-iconified.
     * @param e 
     */
    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }

    /**
     * Invoked when a window is activated.
     * @param e 
     */
    @Override
    public void windowActivated(WindowEvent e)
    {
    }

    /**
     * Invoked when a window is de-activated.
     * @param e 
     */
    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }

    private JTextComponent getTextComponentWithFocus()
    {
        Object o;

        o = getComponentWithFocus();

        if( o instanceof JTextComponent)
        {
            return (JTextComponent)o;
        }

        return null;
    }

    /**
     *
     */
    public void doCopy()
    {
        JTextComponent t;

        t = getTextComponentWithFocus();

        if( t == null) return;

        t.copy();
    }

    /**
     *
     */
    public void doCut()
    {
        JTextComponent t;

        t = getTextComponentWithFocus();

        if( t == null) return;

        t.cut();
    }

    /**
     *
     */
    public void doPaste()
    {
        JTextComponent t;

        t = getTextComponentWithFocus();

        if( t == null) return;

        t.paste();
    }

    /**
     *
     */
    public void doSelectAll()
    {
        JTextComponent t;

        t = getTextComponentWithFocus();

        if( t == null) return;

        t.selectAll();
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CFrame");//#LOGGER-NOPMD

    static
    {
        String systemLaF = UIManager.getSystemLookAndFeelClassName();
        if (
            systemLaF.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel") ||
            systemLaF.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
        )
        {
            try
            {
                UIManager.setLookAndFeel(systemLaF);
            }
            catch (Exception e)
            {
                LOGGER.warn( "couldn't set L&F", e);
            }
        }
        else
        {
            LOGGER.info( "using default L&F not native " + systemLaF);
        }
    }
}
