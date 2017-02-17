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
package com.aspc.remote.html.menu;

import com.aspc.remote.util.misc.StringUtilities; 
import com.aspc.remote.html.*;

/**
 * @since 6 January 2006
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 * @author michael
 */
public abstract class HTMLMenuItem extends HTMLContainer
{    
    /**
     * Get object's Id
     * @return String - object's Id
     */
    @Override
    public String getId()
    {
        String strId = super.getId();
        if( StringUtilities.isBlank(strId))
        {
            strId = getParentPage().doGenerateId();
        }
        
        return strId;
    
    }
    
    /**
     * This method is to set menu bar
     * @param menuBar - menu bar that this menu item is on
     */
    public void setMenuBar( HTMLMenuBar menuBar)
    {
        this.menuBar = menuBar;
    }
    
     /**
     * This method is to set if the menu item is selected
     * @param isSelected - if the  menu item is selected
     */
    public void  setSelected(boolean isSelected)
    {
        this.isSelected =  isSelected;
    }
    
    
    /**
     * This method is to return menu bar
     * @return HTMLMenuBar - menu bar that this menu item is on
     */
    public HTMLMenuBar getMenuBar()
    {
        return menuBar;
    }
    
    /**
     * This method is to return if the menu item is selected
     * @return boolean - if  the menu item is selected
     */
    public boolean isSelected()
    {
        return isSelected;
    }
    
    /**
     * This method is to set menu item's label
     * @param label - Menu item's label
     */
    public void setLabel( final HTMLComponent label)
    {
        this.label = label;
    }
    
    /**
     * This method is to return menu item's label
     * @return HTMLText - label
     */
    public HTMLComponent getLabel()
    {
        return label;
    }
    
    /**
     * This method is to get menu item 's level   
     * @return int - menu item's level 
     */
    public int getDepth()
    {
        int depth = -1;
        
        HTMLMenuBar bar;
        
        bar = getMenuBar();
        
        if( bar != null)
        {
            depth = bar.getDepth();
            depth++;
        }
        
        return depth;
    }
        
    private  HTMLMenuBar   menuBar;
    private  HTMLComponent label;   
    private  boolean       isSelected;//NOPMD
}
