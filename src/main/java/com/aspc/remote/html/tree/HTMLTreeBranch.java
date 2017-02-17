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
import com.aspc.remote.html.HTMLComponent;
import com.aspc.remote.html.HTMLTable;
import com.aspc.remote.html.HTMLText;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 *  HTMLTreeBranch
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       28 September 1998
 */
public class HTMLTreeBranch extends HTMLTreeLeaf
{

    /**
     *
     * @param id
     * @param name
     */
    public HTMLTreeBranch( String id, String name)
    {
        this(
            id,
            new HTMLText( name)
        );
    }

    /**
     *
     * @param id
     * @param component
     */
    public HTMLTreeBranch( String id, HTMLComponent component)
    {
        super( id, component);
        list = new Vector();//NOPMD
        openedImage = "/images/twisto.gif";
        closedImage = "/images/twistc.gif";
    }

    /**
     *
     * @param id
     * @param text
     * @return the value
     */
    public HTMLTreeBranch addBranch( String id, String text)
    {
        HTMLTreeBranch branch;

        branch = new HTMLTreeBranch( id, text);

        addLeaf( branch);

        return branch;
    }

    /**
     *
     * @param id
     * @param text
     * @return the value
     */
    public HTMLTreeLeaf addLeaf( String id, String text)
    {
        HTMLTreeLeaf leaf;

        leaf = new HTMLTreeLeaf( id, text);

        addLeaf( leaf);

        return leaf;
    }

    /**
     *
     * @param leaf
     * @return the value
     */
    public HTMLTreeLeaf addLeaf( HTMLTreeLeaf leaf)
    {
        list.add(leaf);

        leaf.setTree( getTree());
        leaf.setParent( this);
        loadFlag = true;

        return leaf;
    }

    /**
     *
     * @param leaf
     * @return the value
     */
    public HTMLTreeLeaf insertLeaf( HTMLTreeLeaf leaf)
    {
        list.insertElementAt(leaf, 0);

        leaf.setTree( getTree());
        leaf.setParent( this);
        loadFlag = true;

        return leaf;
    }

    /**
     *
     * @param flag
     */
    public void setLoaded(boolean flag)
    {
        loadFlag = flag;
    }

    /**
     *
     * @return the value
     */
    public boolean isLoaded()
    {
        return loadFlag;
    }

    /**
     *
     * @param src
     */
    public void setClosedImage( String src)
    {
        closedImage = src;
    }

    /**
     *
     * @param src
     */
    public void setOpenImage( String src)
    {
        openedImage = src;
    }

    /**
     *
     * @return the value
     */
    public String getClosedImage()
    {
        return closedImage;
    }

    /**
     *
     * @return the value
     */
    public String getOpenImage()
    {
        return openedImage;
    }

    /**
     *
     * @param flag
     */
    public void setOpen( boolean flag)
    {
        openFlag = flag;

        // Find all branches and close them
        if( flag == false)
        {
            for( int i = 0; i < list.size(); i++)
            {
                HTMLTreeLeaf leaf;

                leaf = (HTMLTreeLeaf)list.elementAt(i);

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
        else
        {
            if( parent instanceof HTMLTreeBranch)
            {
                HTMLTreeBranch branch = (HTMLTreeBranch)parent;

                branch.setOpen(true);
            }
        }

        if( getTree() != null)
        {
            // IF we are opening a branch you should be able to see the first couple of items
            getTree().setScrollTo( this);
        }
    }

    /**
     *
     * @return the value
     */
    public boolean isOpen()
    {
        return openFlag;
    }

    /**
     *
     * @return the value
     */
    @Override
    protected String getHrefOther()
    {
        return "&OPEN=" + !isOpen();
    }

    /**
     *
     * @param i
     * @return the value
     */
    public HTMLTreeLeaf getLeaf( int i)
    {
        return (HTMLTreeLeaf)list.elementAt(i);
    }

    public String encodeRecordState()
    {
        StringBuilder sb=new StringBuilder();
        Hashtable t = recordState();
        
        for( Object key:t.keySet())
        {
            sb.append(key);
            sb.append("\t");
            Object value = t.get( key);
            sb.append( StringUtilities.encode(value.toString()));
            sb.append( "\n");
        }
        return sb.toString();
    }
    
    
    /**
     * Records the state of the tree so that we
     * can restore the state to a new tree
     * @return the value
     */
    public Hashtable recordState()
    {
        Hashtable table = new Hashtable();

        iRecordState( table);

        return table;
    }

    /**
     *
     * @param table
     */
    protected void iRecordState( Hashtable table)
    {
        int count = getLeafCount();

        for( int i = 0; i < count; i++)
        {
            HTMLTreeLeaf leaf = getLeaf(i);

            if( leaf instanceof HTMLTreeBranch)
            {
                HTMLTreeBranch branch;

                branch = (HTMLTreeBranch)leaf;

                if( branch.isOpen())
                {
                    table.put( branch.getID(), "OPEN");
                    branch.iRecordState(table);
                }
            }
        }
    }

    /**
     *
     * @return the value
     */
    @Override
    public String debugList()
    {
        StringBuilder buffer = new StringBuilder();

        for( int i = 0; i <= getDepth(); i++)
        {
            buffer.append( " ");
        }
        buffer.append("*-").append( getID());

        buffer.append( "\n");

        int count = getLeafCount();

        for( int i = 0; i < count; i++)
        {
            HTMLTreeLeaf leaf = getLeaf(i);

            buffer.append( leaf.debugList());
        }

        return buffer.toString();
    }

    /**
     *
     * @return the value
     */
    public int getLeafCount()
    {
        return list.size();
    }

    /**
     *
     * @param id
     * @return the value
     */
    public boolean hasLeaf( String id)
    {
        try
        {
            findLeaf( id);
            return true;
        }
        catch( NotFoundException nf)
        {
            return false;
        }
    }

    /**
     *
     * @param id
     * @throws com.aspc.remote.database.NotFoundException
     * @return the value
     */
    public HTMLTreeLeaf findLeaf( String id) throws NotFoundException
    {
        if( getID().equals( id))
        {
            return this;
        }
        for( int i = 0; i < list.size(); i++)
        {
            HTMLTreeLeaf leaf;

            leaf = (HTMLTreeLeaf)list.elementAt(i);

            if( leaf.getID().equals( id))
            {
                return leaf;
            }
            else if( leaf instanceof HTMLTreeBranch)
            {
                HTMLTreeBranch branch = (HTMLTreeBranch)leaf;

                leaf = branch.findLeaf(id);

                if( leaf != null)
                {
                    return leaf;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param id
     * @throws Exception a serious problem
     */
    public void toggleBranch( String id) throws Exception
    {
        HTMLTreeLeaf leaf;

        leaf = findLeaf( id);

        if( leaf instanceof HTMLTreeBranch)
        {
            HTMLTreeBranch branch;
            branch = (HTMLTreeBranch)leaf;

            branch.setOpen( !branch.isOpen());
        }
    }

    /**
     *
     * @param events
     */
    @Override
    protected void makeListOfEvents( List events)
    {
        super.makeListOfEvents( events);

        for( int i = 0; i < list.size(); i++)
        {
            HTMLTreeLeaf leaf;

            leaf = (HTMLTreeLeaf)list.elementAt(i);
            leaf.makeListOfEvents( events);
        }
    }

    /**
     *
     * @return the value
     */
    protected int getMaxDepth()
    {
        int max = getDepth();

        if( isOpen())
        {
            for( int i = 0; i < list.size(); i++)
            {
                HTMLTreeLeaf leaf;
                int aDepth;

                leaf = (HTMLTreeLeaf)list.elementAt(i);

                if( leaf instanceof HTMLTreeBranch)
                {
                    aDepth= ((HTMLTreeBranch)leaf).getMaxDepth();
                }
                else
                {
                    aDepth = leaf.getDepth();
                }
                if( aDepth > max) max = aDepth;
            }
        }

        return max;
    }

    /**
     *
     * @param table
     * @param maxDepth
     */
    @Override
    protected void putIntoTable( HTMLTable table, int maxDepth)
    {
        super.putIntoTable( table, maxDepth);

        if( isOpen())
        {
            for( int i = 0; i < list.size(); i++)
            {
                HTMLTreeLeaf leaf;

                leaf = (HTMLTreeLeaf)list.elementAt(i);
                leaf.setRowHeight( getRowHeight());
                leaf.putIntoTable( table,maxDepth);
            }
        }
    }

    /**
     *
     * @return the value
     */
    @Override
    protected String getImageSrc()
    {
        if( openFlag)
        {
            return openedImage;
        }
        else
        {
            return closedImage;
        }
    }

    /**
     *
     */
    @Override
    protected void resetParent()
    {
        super.resetParent();

        for( int i = 0; i < list.size(); i++)
        {
            HTMLTreeLeaf leaf;

            leaf = (HTMLTreeLeaf)list.elementAt(i);

            leaf.setParent( this);
        }
    }

    /**
     *
     * @param tree
     */
    @Override
    protected void setTree( HTMLTree tree)
    {
        super.setTree( tree);

        for( int i = 0; i < list.size(); i++)
        {
            HTMLTreeLeaf leaf;

            leaf = (HTMLTreeLeaf)list.elementAt(i);

            leaf.setTree( tree);
        }
    }

    /**
     *
     * @return the value
     */
    public Vector getList()
    {
        return list;
    }

    private Vector  list;

    private String  openedImage,
            closedImage;

    private boolean openFlag,
            loadFlag;
}
