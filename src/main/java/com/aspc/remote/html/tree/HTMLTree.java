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
package com.aspc.remote.html.tree;

import com.aspc.remote.database.NotFoundException;
import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLPanel;
import com.aspc.remote.html.HTMLTable;
import com.aspc.remote.html.scripts.HTMLStateEvent;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  HTMLTree
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       17 January 1998
 */
public class HTMLTree extends HTMLTreeBranch
{
    /**
     *
     * @param callBack
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public HTMLTree( String callBack)
    {
        super( "ROOT", "");

        setCallBack(callBack);
        setTree( this);
    }

    /**
     *
     * @param id
     * @throws com.aspc.remote.database.NotFoundException
     * @return the value
     */
    @Override
    public HTMLTreeLeaf findLeaf( String id) throws NotFoundException
    {
        HTMLTreeLeaf leaf;

        leaf = super.findLeaf( id);

        if( leaf == null)
        {
            throw new NotFoundException( "No leaf " + id);
        }

        return leaf;
    }

    /**
     *
     * @param flag
     */
    public void setRootHidden( boolean flag)
    {
        rootHidden = flag;
    }

    /**
     *
     * @return the value
     */
    public boolean isRootHidden()
    {
        return rootHidden;
    }

    /**
     *
     */
    public void closeAll()
    {
        for( int i = 0; i < getList().size(); i++)
        {
            HTMLTreeLeaf leaf;

            leaf = (HTMLTreeLeaf)getList().elementAt(i);

            if( leaf instanceof HTMLTreeBranch)
            {
                HTMLTreeBranch branch = (HTMLTreeBranch)leaf;

                if( branch.isOpen())
                {
                    branch.setOpen(false);
                }
            }
        }
    }

    /**
     *
     * @return the value
     */
    @Override
    public boolean isOpen()
    {
        return true;
    }

    /**
     *
     * @param id
     */
    public void setScrollTo( HTMLTreeLeaf id)
    {
        scrollTo = id;
    }

    /**
     * Restore the state of a tree to what was recorded before.
     * If a null pointer is pass then no previous state was recorded
     * and I'll just return.
     * @param table
     * @throws Exception a serious problem
     */
    public void restoreState( Map table) throws Exception
    {
        restoreState( table, null);
    }

    public void restoreState( final String encodedState) throws Exception
    {
        restoreState(encodedState, null);
    }
    
    public void restoreState( final String encodedState, IHTMLBranchLoader loader) throws Exception
    {
        Map<String, String> t=new HashMap();
        
        for( String line: encodedState.split("\n"))
        {
            String[] values = line.split("\t");
            if( values.length != 2) continue;
            String key=values[0];
            String value=StringUtilities.decode(values[1]);

            t.put(key, value);
        }
        
        restoreState( t, loader);        
    }
    /**
     * Restore the state of a tree to what was recorded before.
     * If a null pointer is pass then no previous state was recorded
     * and I'll just return.
     * @param table
     * @param loader
     * @throws Exception a serious problem
     */
    @SuppressWarnings("empty-statement")
    public void restoreState( Map table, IHTMLBranchLoader loader) throws Exception
    {
        if( table == null) return;

        Iterator e;

        e = table.keySet().iterator();

        while( e.hasNext())
        {
            String tmpID;

            tmpID = (String)e.next();

            try
            {
                HTMLTreeLeaf leaf = findLeaf(tmpID);

                /**
                 * It is possible to change a leaf to a branch and back again.
                 */
                if( leaf instanceof HTMLTreeBranch)
                {
                    HTMLTreeBranch branch;

                    branch = (HTMLTreeBranch)leaf;

                    if( branch.isLoaded() == false && loader != null)
                    {
                        loader.loadBranch( this, tmpID);
                    }

                    branch.setOpen(true);
                }
            }
            catch( NotFoundException nf)
            {
                // Try to load it then open it.
                if( loader != null)
                {
                    loader.loadBranch( this, tmpID);

                    try
                    {
                        HTMLTreeLeaf leaf = findLeaf(tmpID);

                        if( leaf instanceof HTMLTreeBranch)
                        {
                            HTMLTreeBranch branch;

                            branch = (HTMLTreeBranch)leaf;

                            branch.setOpen(true);
                        }
                    }
                    catch( NotFoundException nf2)
                    {
                        ;// No error we are just restoring state.
                    }
                }
            }
        }
    }


    /**
     *
     * @param browser
     */
    @Override
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    protected void compile( final ClientBrowser browser)
    {
        HTMLPanel panel = new HTMLPanel();

        iAddComponent( panel);


        HTMLTable table = new HTMLTable();
        panel.addComponent(table);

        int maxCol;

        maxCol = getMaxDepth();

        int subRow = 0;
        for( int i = 0; i < getList().size(); i++)
        {
            HTMLTreeLeaf leaf;

            leaf = (HTMLTreeLeaf)getList().elementAt(i);

            if( isRootHidden())
            {
                if( leaf instanceof HTMLTreeBranch)
                {

                    HTMLTable subTable = new HTMLTable();
                    if( StringUtilities.isBlank( getRowHeight()) == false)
                    {
                        table.setCellHeight( getRowHeight(), subRow, 0);
                    }
                    table.setCell( subTable, subRow++, 0);
                    HTMLTreeBranch root = (HTMLTreeBranch)leaf;

                    for( int j = 0; j < root.getList().size(); j++)
                    {
                        HTMLTreeLeaf subleaf;
                        subleaf = (HTMLTreeLeaf)root.getList().elementAt(j);

                        subleaf.setRowHeight( getRowHeight());
                        subleaf.putIntoTable( subTable, maxCol);
                    }
                }
            }
            else
            {
                 leaf.setRowHeight( getRowHeight());
                leaf.putIntoTable( table, maxCol);
            }
        }

        for( int i = 0; i < maxCol; i++)
        {
            table.setColumnWidth(i,"20");

        }

        if( scrollTo != null)
        {
            HTMLPage page = getParentPage();

            if( page != null)
            {
                HTMLStateEvent sEvent;

                sEvent = new HTMLStateEvent(
                    HTMLStateEvent.onLoadEvent,
                    "doScroll()"
                );

                page.addStateEvent( sEvent);

                HTMLTreeBranch branch = null;
                if( scrollTo instanceof HTMLTreeBranch)
                {
                    branch = (HTMLTreeBranch)scrollTo;
                }

                StringBuilder buffer;

                if(
                    branch != null &&
                    branch.isOpen() &&
                    branch.getLeafCount() > 0
                )
                {
                    HTMLTreeLeaf leaf = branch.getLeaf(branch.getLeafCount() - 1);

                    buffer = new StringBuilder(
                        "function doScroll()\n" +
                        "{\n" +
                        " var theLeaf;\n" +
                        " theLeaf = findElement( 'CELL_" + leaf.getID() + "');\n" +
                        " var theBranch;\n" +
                        " theBranch = findElement( 'CELL_" + scrollTo.getID() + "');\n" +
                        " if( theLeaf !== null && theBranch !== null) \n" +
                        " {\n" +
                        "    var branchY, \n" +
                        "        leafY;\n" +
                        "    branchY = getY( theBranch);\n" +
                        "    leafY = getY( theLeaf)+theLeaf.offsetHeight;\n" +
                        "    if( leafY-branchY > getWindowHeight())\n" +
                        "    {\n" +
                        "       scrollTo( 0, branchY);\n" +
                        "    }\n" +
                        "    else\n" +
                        "    {\n" +
                        "       if( leafY > getWindowHeight())\n" +
                        "       {\n" +
                        "          scrollTo( 0, leafY - getWindowHeight());\n" +
                        "       }\n" +
                        "    }\n" +
                        " }\n" +
                        "}"
                    );
                }
                else
                {
                   buffer = new StringBuilder(
                        "function doScroll()\n" +
                        "{\n" +
                        " var theLeaf;\n" +
                        " theLeaf = findElement( 'CELL_" + scrollTo.getID() + "');\n" +
                        " if( theLeaf != null) \n" +
                        " {\n" +
                        "    var leafY;\n" +
                        "    leafY = getY( theLeaf)+theLeaf.offsetHeight;\n" +
                        "    if( leafY > getWindowHeight())\n" +
                        "    {\n" +
                        "       scrollTo( 0, leafY - getWindowHeight());\n" +
                        "    }\n" +
                        " }\n" +
                        "}"
                    );
                }

                page.addJavaScript(buffer.toString());
            }
        }

        super.compile( browser);
    }



    private boolean rootHidden;

    private HTMLTreeLeaf scrollTo;

}
