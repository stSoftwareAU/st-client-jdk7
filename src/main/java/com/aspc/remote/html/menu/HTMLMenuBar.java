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
package com.aspc.remote.html.menu;

import com.aspc.remote.html.*;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.util.ArrayList;

/**
 * a menu bar
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 * @author michael
 * @since 6 January 2006
 */
public abstract class HTMLMenuBar extends HTMLMenuItem
{
    /**
     * create a menu bar
     * @param code the code.
     */
    public HTMLMenuBar( final String code)
    {
        menuCode = code;
        bgColor = Color.WHITE;
        highlightColor = Color.WHITE;
    }
    
    /**
     * add HTMLMenuBar to parent HTMLPage
     * @param  browser  -   client Browser
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        /*if( browser.isBrowserMOBILE())
        {
            compileMOBILE();
        }
        else
        {*/
            compileWEB( );
        //}
        
        super.compile(browser);
    }
    
    private void compileWEB()
    {
        HTMLPage page = getParentPage();
        
        // Get unique id for this menu bar so that we can use it to reference
        // objects from scripts
        String tableId = getId();
        if( StringUtilities.isBlank(tableId))
        {
            tableId = getParentPage().doGenerateId();
        }
        
        // Create a table to hold contents of menu bar
        HTMLTable table = new HTMLTable();
        table.setId( tableId);
        table.setWidth("100%");
        iAddComponent( table);
        
        // Add menus to table
        addMenus(page, table, menuList);
        addRolloverScript();
    }
    
    /*private void compileMOBILE()
    {
        // Get unique id for this menu bar so that we can use it to reference
        // objects from scripts
        String tableId = getId();
        if( StringUtilities.isBlank(tableId))
        {
            tableId = getParentPage().doGenerateId();
        }
        
        // Create a table to hold contents of menu bar
        HTMLTable table = new HTMLTable();
        table.setId( tableId);
        table.setWidth("100%");
        iAddComponent( table);
        
        if (menuList != null && menuList.size() > 0)
        {
            for (int seq = 0; seq < menuList.size(); seq++)
            {
                HTMLMenuItem menu = (HTMLMenuItem)menuList.get(seq);
                
                if( menu instanceof HTMLMenuAnchor)
                {
                    HTMLAnchor a = new HTMLAnchor( ((HTMLMenuAnchor)menu).getHREF());
                    a.addComponent( menu.getLabel());
                    table.setCell( a, seq, 0);
                }
                else if( menu instanceof HTMLMenuBar) 
                {
                    HTMLAnchor a = new HTMLAnchor( "/wap/?MENU=" + ((HTMLMenuBar)menu).getMenuCode());
                    a.addComponent( menu.getLabel());
                    
                    table.setCell( a, seq, 0);
                }
            }            
        }        
    }*/
    
    
    
    /** 
     * Adds the menus to the menu bar table
     *
     * @param  page     -   parent page
     * @param  table    -   the table which menus are added in
     * @param  menuList -   the menu list
     */
    protected abstract void addMenus(HTMLPage page, HTMLTable table, ArrayList menuList);
    
    /**
     * set  menu to cell
     *
     * @param  menu  -  one HTMLMenu
     */
    public void addMenu( HTMLMenuItem menu)
    {
        if (menuList == null)
        {
            menuList = new ArrayList();
        }
        menuList.add( menu);
    }
    
    /**
     * This method is to set menu div's border
     *
     * @param div -  the div that contains menu
     */
    protected void setDivBorder(HTMLDiv div)
    {
        String strHTMLColor = null;
        if (borderColor != null)
        {
            strHTMLColor = makeColorID(borderColor);
        }            
     
        div.setBorderBottomWidth(borderWidth);
        if (strHTMLColor != null)
        {
            div.setBorderBottomColor(strHTMLColor);
        }
        if (StringUtilities.isBlank(borderStyle) == false)
        {
            div.setBorderBottomStyle(borderStyle);
        }        
    }
    
    /**
     * This method is to set div's hightlight color
     *
     *  @param menuDiv   -  HTMLDiv   it is object that rollover is added       
     *  @param menubarId  -  Menu bar's Id
     */
    protected void setUpRollover(HTMLDiv menuDiv, String menubarId)    
    {
        String menuId = menuDiv.getId();
             
        String strSelectedMenuId = "clickedDivId";

        HTMLMouseEvent mouseOverEvent4HightLight = new  HTMLMouseEvent(
            HTMLMouseEvent.onMouseOverEvent,
            "doMenuRollover( '" + menubarId + "', '" + menuId + "', true, '" + strSelectedMenuId + "')"
        );

        HTMLMouseEvent mouseOutEvent4HightLight = new  HTMLMouseEvent(
            HTMLMouseEvent.onMouseOutEvent,
            "doMenuRollover( '" + menubarId + "', '" +menuId + "', false, '" + strSelectedMenuId + "')"
        );
        menuDiv.addMouseEvent( mouseOverEvent4HightLight);
        menuDiv.addMouseEvent( mouseOutEvent4HightLight);

    }
    
    /**
     * This method is to add rollover script
     */
    protected void addRolloverScript()
    {
        String script = "var clickedDivId; \n"  + 
                        "function doMenuRollover(menuBarId, menuId, turnOn, selectedMenuId)\n" + 
                        "{\n" +
                        " var elem = findElement( menuId);\n" +
                        " var colorArray = eval(menuBarId + '_ARRAY');\n" +
                        " var bgColor = colorArray[0]; \n" + 
                        " var hightlightColor = colorArray[1]; \n" + 
                        " var selectedColor = colorArray[2]; \n" +                
                        " var actBgColor = (menuId == eval(selectedMenuId)?selectedColor:bgColor);\n" +
                        " elem.style.backgroundColor=( turnOn == true ? hightlightColor : actBgColor)\n" +
                        "}";
        addJavaScript( "DOROLLOVER", script);
    }
        
    
    /**
     * This method is to add rollover script
     * @param strMenuBarId   menu bar's id
     */
    protected void addMenuBarColorArray(String strMenuBarId)
    {  
        String strBackgroundColor = makeColorID(getBgColor());
        String strHighLightColor = makeColorID(getHighlightColor());
        String strSelectionColor = makeColorID(getSelectionColor());
        
        String script = " \n// the array's values are backgroundColor, highlightColor, selectionColor \n" +
                         " var " + strMenuBarId.toUpperCase() + "_ARRAY = new Array('" + 
                                       strBackgroundColor + "','" + strHighLightColor + "','" + strSelectionColor + "'); \n";
                          
        addJavaScript( strMenuBarId.toUpperCase() + "_ARRAY", script);
    }
    
    
    /**
     * set  MennuBar's Timeout
     *
     * @param  timeout  Menu's timeout
     */
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }
    
    /**
     * set  sub menu's width
     *
     * @param  menuItemWidth  sub Menu's width
     */
    public void setMenuItemWidth(int menuItemWidth)
    {
        this.menuItemWidth = menuItemWidth;
    }
    
    /**
     * set  sub menu's height
     *
     * @param  menuItemHeight  sub Menu's height
     */
    public void setMenuItemHeight(int menuItemHeight)
    {
        this.menuItemHeight = menuItemHeight;
    }
            
    /**
     * set  Menu list
     *
     * @param  menuList  menu list
     */
    public void setMenuList(ArrayList menuList)
    {
        this.menuList = menuList;
    }
            
    /**
     * set  background color
     *
     * @param   bgColor -  the background color .
     */
    public void setBgColor(Color bgColor)
    {
        this.bgColor = bgColor;
    }
    
    /**
     * set  highlight color
     *
     * @param   highlightColor -  the highlight color .
     */
    public void setHighlightColor(Color highlightColor)
    {
        this.highlightColor = highlightColor;
    }
    
    /**
     * set  border width
     * @param   borderWidth -  menu's bordre width .
     */
    public void setBorderWidth( int borderWidth)
    {
        this.borderWidth = borderWidth;
    }
    
    /**
     * set  border style
     * @param   borderStyle -  menu's bordre style .
     */
    public void setBorderStyle( String borderStyle)
    {
        this.borderStyle = borderStyle;
    }
    
    /**
     * set  border Color
     * @param   borderColor -  menu's bordre color .
     */
    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
    }
    
   
   /**
    *This method is to set selection Color
    *@param selectionColor - selection Color
    *
    */
    public void setSelectionColor(Color selectionColor)
    {
        this.selectionColor = selectionColor;
    }
    
     /**
     * set  if the menubar is open
     * @param   isOpen -  true: open it; false: close it .
     */
    public void setOpen(boolean isOpen)
    {
        this.isOpen= isOpen;
    }
    
    
 
    /**
     * get menu's timeout
     * @return  int  -    menu's timeout
     */
    public int getTimeout()
    {
        return timeout;
    }    
    
    /**
     * get submenu's width
     * @return  String  -   submenu's width
     */
    public int getMenuItemWidth()
    {        
        return menuItemWidth;     
    }
    
    /**
     * get submenu's height
     * @return  String  -   submenu's height
     */
    public int getMenuItemHeight( )
    {
        return menuItemHeight;
    }
    
    /**
     * get  MenuBar's  Full Height
     * @return  int  - MenuBar's  Full Height
     */
    public int getFullHeight( )
    {
        int itemHeight = getMenuItemHeight() + 1; // add 1 for border bottom width
        
        int size = 0;
        if( menuList != null)
        {
            size = menuList.size();
        }
        
        return size * itemHeight;
    }
    
    /**
     * get  background color
     * @return   bgColor -  the background color .
     */
    public Color getBgColor()
    {
        return bgColor;
    }
    
     /**
     * get  highlight 
     * @return   Color -  the highlight color .
     */
    public Color getHighlightColor()
    {
        return highlightColor;
    }    
    
    /**
     * get  Menu list
     * @return  Arraylist  - menu list
     */
    public ArrayList getMenuList()
    {
        return menuList;
    }    
    
    
    /**
     * get the Menu code
     * @return  the code
     */
    public String getMenuCode()
    {
        return menuCode;
    }    
    
   /**
    *This method is to get selection Color
    *@return Color - selection Color
    */
    public Color getSelectionColor()
    {        
        Color selColor = null;
        
        if (this.selectionColor != null)
        {
           selColor =  this.selectionColor;
        }
        else
        {
            HTMLMenuBar menuBar = getMenuBar();
            if( menuBar != null)
            {
                selColor = menuBar.getSelectionColor();
            }
            else
            {
                if (getBgColor() != null)
                {
                    selColor = getBgColor();
                } 
            }
        }
        return selColor;
    }
    
     
    /**
     * get  if the menubar is open
     * @return boolean -  true: open it; false: close it .
     */
    public boolean isOpen()
    {
        return isOpen;
    }
    
    private  int        timeout,            //hide the sub Menu when mouse is out and time is over timeout.
                        menuItemWidth,
                        borderWidth = 1,
                        menuItemHeight;
    
    private ArrayList   menuList;
    
    private  boolean    isOpen;//NOPMD
        
    private  String     borderStyle = "solid";
    private final String     menuCode;
    
    private Color       bgColor,
                        highlightColor,
                        borderColor = Color.WHITE,
                        selectionColor;
}
