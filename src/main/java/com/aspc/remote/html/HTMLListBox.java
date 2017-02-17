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
package com.aspc.remote.html;

import com.aspc.remote.html.internal.HTMLFormComponent;
import com.aspc.remote.html.internal.HTMLReadOnlyToggle;
import com.aspc.remote.html.internal.InternalEvent;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.*;
import org.apache.commons.logging.Log;

/**
 *  HTMLListBox
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       23 May 1998
 */
public class HTMLListBox extends HTMLFormComponent implements HTMLReadOnlyToggle
{
    /**
     *
     */
    public static final String SEPERATOR_VALUE = "-----";
    private String placeHolder;

    /** Is this disabled */
    protected boolean     disabledFg,
                          readonlyFg,
                          forceEnabled,
                          allowMultiple;

    /** The selected item */
    protected HashMap<String,String> selectedItems = HashMapFactory.create();
    private int       size;
    private boolean   isVisible = true;

    /**
     * the items
     */
    protected ArrayList<String>  listItems;
    /**
     * the codes.
     */
    protected ArrayList<String>  listCodes;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLListBox");//#LOGGER-NOPMD

    /**
     *
     * @param name
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public HTMLListBox(final String name)
    {
        iSetName(name);
        listItems = new ArrayList<>();
        listCodes = new ArrayList<>();
        size = 1;
        appendClassName(STYLE_STS_FIELD);
    }

    /**
     * set the placeholder string
     * @param plSring the place holder string
     */
    public void setPlaceHolder( final String plSring)
    {
        placeHolder=plSring;
    }

    /**
     * get the placeholder string
     * @return the place holder string
     */
    public String getPlaceHolder()
    {
        return placeHolder;
    }

    /**
     *
     * @return the value
     */
    public int getNumberOfItems()
    {
        return listItems.size();
    }

    /**
     *
     * @param call
     */
    @Override
    public void addOnChangeEvent( String call)
    {
        addOnChangeEvent( call, "");
    }

    /**
     *
     * @param call
     * @param script
     */
    @Override
    public void addOnChangeEvent( String call, String script)
    {
        iAddEvent( new InternalEvent( "onChange", call), script);
    }

    /**
     *
     * @param itemName
     */
    public void addItem( String itemName)
    {
        addItem(itemName, false);
    }

    /**
     *
     * @param itemName
     * @param itemCode
     */
    public void addItem( String itemName, String itemCode)
    {
        addItem(itemName, itemCode, false);
    }

    /**
     *
     * @param itemCode
     */
    public void setSelected( String itemCode)
    {
        setSelected( itemCode, true);
    }

    /**
     *
     * @param itemCode
     * @param on
     */
    public void setSelected( String itemCode, boolean on)
    {
        if( on)
        {
            if( allowMultiple == false)
            {
                selectedItems.clear();
            }

            selectedItems.put( itemCode.toUpperCase(), "");
        }
        else
        {
            if( selectedItems.containsKey( itemCode))
            {
                selectedItems.remove( itemCode);
            }
        }

    }

    /**
     *
     * @param on
     */
    public void setAllowMultiple( boolean on)
    {
        allowMultiple = on;
    }

    /**
     *
     * @param visible
     */
    public void setVisible( final boolean visible)
    {
        isVisible = visible;
    }


    /**
     * Does the list contain this code.
     * @param itemCode
     * @return the value
     */
    public boolean hasValue( String itemCode)
    {
        for (String listCode : listCodes) 
        {
            String  c;
            c = listCode;
            if( c.equalsIgnoreCase( itemCode))
            {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param size
     */
    public void setSize( int size)
    {
        this.size = size;
    }

    /**
     *
     * @return the value
     */
    public int getSize( )
    {
        return size;
    }

    /**
     *
     * @param itemName
     * @param selected
     */
    public void addItem( final String itemName, boolean selected)//NOPMD
    {
        String pItemName=itemName;
        if( pItemName == null)
        {
            pItemName = "";
        }

        if( selected)
        {
            setSelected( pItemName);
        }

        listItems.add(pItemName);
        listCodes.add(pItemName);
    }

    /**
     *
     * @param itemName
     * @param itemCode
     * @param selected
     */
    public void addItem( final String itemName, String itemCode, boolean selected)//NOPMD
    {
        String pItemName=itemName;
        String pItemCode=itemCode;

        if( pItemName == null)
        {
            pItemName = "";
        }
        if( pItemCode == null)
        {
            pItemCode = "";
        }

        if( selected)
        {
            setSelected( itemCode);
        }

        listItems.add(pItemName);
        listCodes.add(pItemCode);
    }

    /**
     *
     * @param index
     * @param itemName
     * @param itemCode
     * @param selected
     */
    public void insertItem( int index, String itemName, String itemCode, boolean selected)//NOPMD
    {
        String pItemName=itemName;
        String pItemCode=itemCode;

        if( pItemName == null)
        {
            pItemName = "";
        }

        if( pItemCode == null)
        {
            pItemCode = "";
        }

        if( selected)
        {
            setSelected( itemCode);
        }

        listItems.add(index, pItemName);
        listCodes.add(index, pItemCode);
    }

    /**
     *
     */
    public void forceEnabled()
    {
        disabledFg = false;
        forceEnabled = true;
    }

    /**
     * Disables this list box.
     * @param flag
     */
    @Override
    public void setDisabled( boolean flag)
    {
        if( forceEnabled == false)
        {
            disabledFg = flag;
        }
    }

    /**
     *
     */
    public void addSeperator()
    {
        listItems.add(SEPERATOR_VALUE);
        listCodes.add(SEPERATOR_VALUE);
    }

    /**
     *
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( ClientBrowser browser, StringBuilder buffer)
    {
        iListBoxGenerate( browser, buffer);

        super.iGenerate( browser, buffer);
    }

    @Override
    protected void compile(ClientBrowser browser) {
        super.compile(browser); 
        
        if( StringUtilities.notBlank(placeHolder))
        {
            if( selectedItems.isEmpty())
            {
                setStyleProperty("color", "gray");
                addOnChangeEvent("this.style.color='black'");
            }
        }
    }

    /**
     *
     * @param browser
     * @param buffer
     */
    protected void iListBoxGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        buffer.append("<select size=").append(
            size);

        if( allowMultiple)
        {
            buffer.append( " multiple=\"multiple\"");
        }

        if( disabledFg )
        {
            buffer.append( " disabled");
            setTabIndex( -1);
        }

        if( !isVisible)
        {
            buffer.append( " style=\"visibility:hidden\"");
        }
//        if( StringUtilities.notBlank(placeHolder))
//        {
//            if( selectedItems.isEmpty())
//            {
//                setStyleProperty("color", "gray");
//                addOnChangeEvent("this.style.color='black'");
//            }
//        }

        iGenerateAttributes(browser, buffer);

        buffer.append( ">\n");

        boolean placeholderShown=false;
        if( StringUtilities.notBlank(placeHolder))
        {
            if( selectedItems.isEmpty())
            {
                placeholderShown=true;
                String t=placeHolder.replace("<", "&lt;").replace(">", "&gt;");
                buffer.append("<option value='' disabled selected style='display:none;color:gray;'>").append(t).append("</option>");
            }
        }
        for( int i = 0; i < listItems.size(); i++)
        {
            String  t,
                    c,
                    selected = null;

            t = listItems.get(i);
            c = listCodes.get(i);

            if( selectedItems.containsKey( c.toUpperCase()))
            {
                selected = " selected";
            }

            String value = c;

            if( t.startsWith("-"))
            {

                //value = null; // jm we need the value so that we know that it was selected
                selected = "";
            }

            /*
             * jm - no point putting out value if it is the same as name (i.e. t)
             * since value is assumed to be name if it is not specified
             *
             * nl - Some scripts fail if the value isn't passed for IE5, the value comes back as null see Adv. search screens
             */
            if( t.equals(c))
            {
                if( browser.isBrowserNETSCAPE())
                {
                    value = null;
                }
            }

            buffer.append("<option");

            if( placeholderShown)
            {
                buffer.append( " style=\"color:black\"");
            }

            if( selected != null)
            {
                buffer.append( selected);
            }

            if( value != null)
            {
                buffer.append( " value=\"");
                buffer.append( StringUtilities.encodeHTML(value));
                buffer.append( "\"");
            }

            buffer.append( ">");
            buffer.append( StringUtilities.encodeHTML( t));
            buffer.append( "</option>\n");
        }


        buffer.append( "</select>\n");
    }

    /**
     *
     * @return the value
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     *
     * @return the value
     */
    @Override
    public String getValue()
    {
        StringBuilder sb = new StringBuilder();
        Set keySet = selectedItems.keySet();
        Iterator it = keySet.iterator();
        while( it.hasNext())
        {
            if( sb.length() > 0)
            {

                sb.append( ",");
            }
            sb.append( (String)it.next());

        }
        return sb.toString();
    }

    /**
     *
     * @param index
     */
    public void removeItem( int index)
    {
        listItems.remove( index);
        listCodes.remove( index);
    }

    /**
     * remove all items.
     */
    public void removeAllItems( )
    {
        listItems.clear();
        listCodes.clear();
    }

    /**
     *
     * @param index
     * @return the value
     */
    public String getItemValue( int index)
    {
        String val = null;
        if( index < listItems.size())
        {
            val = listItems.get(index);
        }

        return val;
    }

    /**
     *
     * @param index
     * @return the value
     */
    public String getItemCode( int index)
    {
        String val = null;
        if( index < listCodes.size())
        {
            val = listCodes.get(index);
        }
        return val;
    }

    /**
     *
     * @param value the value
     */
    @Override
    public void setValue(String value)
    {
        selectedItems = HashMapFactory.create();
        String[] tokens = parseValues(value);
        for (String token : tokens) 
        {
            setSelected(token);
        }
    }

    /**
     *
     * @param itemCode
     * @return the value
     */
    public int getIndex(String itemCode)
    {
        return listCodes.indexOf(itemCode);
    }

    /**
     * Sets the field to READONLY.
     * READONLY is a little stronger than disabled as it can on be turned back on in the browser via javascript.
     * @param flag
     */
    @Override
    public void setReadOnly(boolean flag)
    {
        setDisabled( flag);
        readonlyFg=flag;
    }

     /**
     * Is this is Disabled
     * @return TRUE if disabled
     */
    @Override
    public boolean isDisabled()
    {
        return disabledFg;
    }

    /**
     * Is this is ReadOnly
     * @return TRUE if readonly
     */
    @Override
    public boolean isReadOnly()
    {
        return readonlyFg;
    }

    private String[] parseValues( String values )
    {
        String[] valueList = StringUtilities.split( values, ',', '\'');
        return valueList;

    }

}
