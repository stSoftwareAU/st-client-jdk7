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
package com.aspc.remote.html.tree;

import com.aspc.remote.html.*;
import com.aspc.remote.util.misc.*;
import java.util.Enumeration;
import java.util.List;

/**
 *  HTMLTreeLeaf
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       28 September 1998
 */
public class HTMLTreeLeaf extends HTMLContainer
{
    /**
     * Create new Leaf
     *
     * @param id The ID
     * @param name The name
     */
    public HTMLTreeLeaf( String id, String name)
    {
        this(
            id,
            new HTMLText( name)
        );
    }

    /**
     * @param id
     * @param component
     */
    public HTMLTreeLeaf(
        String id,
        HTMLComponent component
    )
    {
        this.id = id;

        addComponent( component);
        setImage( "/icons/document.gif");

        // This is a short hand to make the document image point to the same thing as
        // the passed anchor
        if( (this instanceof HTMLTreeBranch ) == false && component instanceof HTMLAnchor)
        {
            HTMLAnchor a = (HTMLAnchor)component;

            setCallBack( a.getHREF());
            setCallBackTarget( a.getTarget());
        }
    }

    /**
     * @param name
     */
    public void setName( String name)
    {
        iClear();
        addComponent( new HTMLText( name));
    }

    /**
     * @param component
     */
    @Override
    public final void addComponent( HTMLComponent component)
    {
        if( component instanceof HTMLText)
        {
            HTMLText t;

            t = (HTMLText)component;

            t.setNoWrap(true);
        }

        super.addComponent(component);
    }


    /**
     * @param parent
     */
    @Override
    public void setParent( HTMLComponent parent)
    {
        if( parent instanceof HTMLTreeBranch)
        {
            depth = ((HTMLTreeBranch)parent).getDepth() + 1;
        }

        super.setParent( parent);
    }

    /**
     * @param flag
     */
    public void setVisible( boolean flag)
    {
        if( parent instanceof HTMLTreeBranch)
        {
            ((HTMLTreeBranch)parent).setOpen( flag);
        }

        if( tree != null)
        {
            tree.setScrollTo( this);
        }
    }

    /**
     * @param tip
     */
    public void setToolTip( String tip)
    {
        this.tip = tip;
    }

    /**
     * @return the value
     */
    public String getID()
    {
        return id;
    }

    /**
     * @return the value
     */
    public String debugList()
    {
        String str = "";

        for( int i = 0; i <= depth; i++)
        {
            str += " ";
        }
        str += "|\n";

        for( int i = 0; i <= depth; i++)
        {
            str += " ";
        }
        str += "+-" + getID() + "\n";

        return str;
    }

    // Protected
    /**
     * @param tree
     */
    protected void setTree( HTMLTree tree)
    {
        this.tree = tree;
    }


    /**
     * @param table
     * @param maxDepth
     */
    protected void putIntoTable( HTMLTable table, int maxDepth)
    {
        int row = table.getComponentCount();
        int col = depth -1;

        if( getTree().isRootHidden())
        {
            col -= 1;
        }

        String tmpImage;
        tmpImage = getImageSrc();

        if( tmpImage != null)
        {
            HTMLAnchor a;

            a = getAnchor( true);

            super.addComponent(a);

            HTMLImage aImage = new HTMLImage( tmpImage);
            aImage.setBorder(0);
            aImage.setAlignment("RIGHT");
            a.addComponent(aImage);
            if( StringUtilities.isBlank( rowHeight) == false)
            {
                table.setCellHeight( rowHeight, row, col);
            }
            table.setCell(a, row, col);
        }

        HTMLComponent c;

        c = iGetComponent(0);

        if( c instanceof HTMLText)
        {
            if( notAllowedOpen == false)
            {
                HTMLAnchor a;

                a = getAnchor( false);

                a.addComponent(c);

                super.addComponent(a);
                c = a;
            }
        }

        table.setCell(c, row, col + 1);
        table.setCellID( "CELL_" + row +"_" + col + getID(), row, col + 1);

        if (StringUtilities.isBlank(rowHeight) == false)
        {
            table.setCellHeight(rowHeight, row, col+1);
        }

        if( tip != null)
        {
            table.setCellToolTip(tip, row, col);
            table.setCellToolTip(tip, row, col + 1);
        }

        table.setCellColSpan(maxDepth - col, row, col + 1);

        HTMLRow aRow = (HTMLRow)table.getComponent(row);


        if( hasTheme() )
        {
            Enumeration keys = getMutableTheme().getChangedFields();

            while( keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();

                aRow.getMutableTheme().setDefault(key, getMutableTheme().getDefault(key));
            }
        }
    }

    /**
     * @return the value
     */
    protected String getHrefOther()
    {
        return "";
    }

    /**
     * @param list
     */
    @Override
    protected void makeListOfEvents( List list)
    {
        super.makeListOfEvents( list);

        // Add this to the list of components checked for javascripts
        if( callBackAnchor != null)
        {
            list.add(callBackAnchor);
        }
    }

    private HTMLAnchor getAnchor( boolean isControl)
    {
        if( callBackAnchor != null)
        {
            HTMLAnchor a = new HTMLAnchor( callBackAnchor.getHREF());

            a.setTarget( callBackAnchor.getTarget());
            a.setTargetWidth( callBackAnchor.getTargetWidth());
            a.setTargetHeight( callBackAnchor.getTargetHeight());
            a.setTargetWindowPlain( callBackAnchor.getTargetWindowPlain());
            a.setId((isControl ? "c" :"") +id);

            return a;
        }

        String href;

        if( callBack == null)
        {
            href = tree.getCallBack();

            if( href.contains("?"))
            {
                href += "&";
            }
            else
            {
                href += "?";
            }

            if( this instanceof HTMLTreeBranch)
            {
                href += "BRANCH=" + StringUtilities.encode(id);
            }
            else
            {
                href += "LEAF=" + StringUtilities.encode(id);
            }

            href += getHrefOther();
        }
        else
        {
            href = callBack;
        }

        HTMLAnchor a;

        a= new HTMLAnchor(href);
        a.setId((isControl ? "c" :"") + id);

        String aTarget = null;

        if( isControl)
        {
            aTarget = controlTarget;
        }

        if( aTarget == null) aTarget = target;

        if( aTarget == null)
        {

            aTarget = tree.getCallBackTarget();
        }

        if( aTarget != null)
        {
            a.setTarget(aTarget);
        }

        return a;
    }

    /**
     * @param href
     */
    public final void setCallBack( String href)
    {
        callBack = href;
    }

    /**
     * @param a
     */
    public final void setCallBack( HTMLAnchor a)
    {
        callBackAnchor = a;
    }

    /**
     * @return the value
     */
    public String getCallBackTarget()
    {
        return target;
    }

    /**
     * @return the value
     */
    public String getCallBack()
    {
        return callBack;
    }

    /**
     * @param target
     */
    public final void setCallBackTarget( String target)
    {
        this.target = target;
    }

    /**
     * @param target
     */
    public final void setControlCallBackTarget( String target)
    {
        this.controlTarget = target;
    }

    /**
     * @param image
     */
    public final void setImage( String image)
    {
        this.image = image;
    }

    /**
     * @return the value
     */
    protected String getImageSrc()
    {
        return image;
    }

    /**
     * @return the value
     */
    public HTMLTree getTree( )
    {
        return tree;
    }

    /**
     * @return the value
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     *
     * @param allow
     */
    public void setNotAllowedOpen( boolean allow)
    {
        notAllowedOpen = allow;
    }


    /**
     *
     * @param height
     */
    public void setRowHeight( String height)
    {
        rowHeight = height;
    }

    /**
     *
     * @return the value
     */
    public String getRowHeight()
    {
        return rowHeight;
    }



    private boolean     notAllowedOpen = false;
    
    private String     // id,
                        callBack,
                        controlTarget,
                        target,
                        rowHeight;

    private HTMLAnchor  callBackAnchor;

    private int         depth;
    private String      image,
                        tip;



    private HTMLTree  tree;
}
