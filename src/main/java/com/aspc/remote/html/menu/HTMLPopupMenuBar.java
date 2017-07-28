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

import com.aspc.remote.html.HTMLDiv;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLTable; 
import com.aspc.remote.html.HTMLComponent; 
import com.aspc.remote.html.scripts.HTMLMouseEvent;
 

import java.util.ArrayList;
import java.awt.Color;

/**
 *  <!--#ASPC_HEAD_START-->
 *
 *  A menu item that provides a anchor.
 *                                                                              <BR>
 *                                                                              <BR>
 *  <a href='doc-files/tree_sdbcomponent.html' >data model</a><BR>
 *  <!--#ASPC_HEAD_END--><BR>
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 * @author Jason McGrath
 * @since 30 January 2006
 */
public class HTMLPopupMenuBar extends HTMLMenuBar
{
    
    /** Creates a new instance of HTMLPopupMenuBar
     * @param code
     */
    public HTMLPopupMenuBar( final String code)
    {
        super( code);
    }
   
    /** Adds the menus to the menu bar table
     * @param  page     -   parent page
     * @param  table    -   the table which menus are added in
     * @param  menuList -   the menu list
     */
    @Override
    protected void addMenus(HTMLPage page, HTMLTable table, ArrayList menuList)
    {
        if (menuList != null && menuList.size() > 0)
        {
            int rowNum = 0;
            int colNum = 0;
            for (int seq = 0; seq < menuList.size(); seq++)
            {
                HTMLMenuItem menu = (HTMLMenuItem)menuList.get(seq);
                HTMLDiv menuDiv = buildMenu( table.getId(), menu, seq);   
                 table.setCell( menuDiv, rowNum, colNum);
                if( menu instanceof HTMLMenuBar)
                {
                    HTMLMenuBar subMenu = (HTMLMenuBar)menu;
                    if( subMenu.getMenuList() != null &&
                        subMenu.getMenuList().size() > 0)
                    {
                        HTMLDiv subMenuDiv = buildSubMenu(menuDiv, subMenu);
                        page.addComponent( subMenuDiv);
                        
                        HTMLMouseEvent mouseOutEvent = new  HTMLMouseEvent(
                                    HTMLMouseEvent.onMouseOutEvent,
                                    "popup.mouseOut()"
                                    ); 
                        subMenuDiv.addMouseEvent(mouseOutEvent);                    
                        table.addCellEvent(mouseOutEvent, rowNum, colNum);  

                        
                        String strOffSetTop ="document.getElementById('" + menuDiv.getId() + "').clientHeight";
                        HTMLMouseEvent mouseOverEvent = new  HTMLMouseEvent(
                                HTMLMouseEvent.onMouseOverEvent,
                                "popup.mouseIn( '" + subMenuDiv.getId() + "', '" + menuDiv.getId() + "'," + strOffSetTop + ",0)"
                                );   
                        subMenuDiv.addMouseEvent(mouseOverEvent);    
                        table.addCellEvent(mouseOverEvent, rowNum, colNum); 
                    }            
                    
                }  
                else if( menu instanceof HTMLMenuAnchor)
                {
                    addLink( menuDiv, (HTMLMenuAnchor)menu);                                  
                }
                colNum++;
                 
            }
                        

            addRolloverScript();
            addMenuBarColorArray(table.getId());
        }        
    }
    
    
    /** 
     * Adds the menus to the menu bar table
     *
     * @param  tableId    -   Id of the table which menus are added in
     * @param  menu -   the menu that will be built
     * @param  seq         -  menu's sequence number that is used to create menu menuBarId
     * @return  HTMLDiv -  the div contains menu
     */    
    private HTMLDiv buildMenu( String tableId, HTMLMenuItem menu, int seq)
    {       
        // add menu to menu bar
        String menuId =  tableId + "_MENU_" + seq;
        HTMLDiv menuDiv =  new HTMLDiv(menuId);
        
        menuDiv.setPosition( "relative");
        menuDiv.setCursor("hand");
        menuDiv.addComponent( getMenuContents(menu));
        
        if (getMenuItemHeight() > 0)
        {
            menuDiv.setHeight("" + getMenuItemHeight());
        }
        
        if (getMenuItemWidth() > 0)
        {
            menuDiv.setWidth("" + getMenuItemWidth());
        }     
        
        String strBgColor = null;
        Color tmpBgColor = getBgColor();
        if (tmpBgColor != null)
        {
            strBgColor =  makeColorID(tmpBgColor);
            menuDiv.setBackgroundColor(strBgColor);           
        }        
        
        setUpRollover(menuDiv, tableId);                
  
        return menuDiv;        
    }
    
   
    /** Adds a sub menu to a menu
     * @param  tableId   -   Id of the table which menus are added in
     * @param  menuDiv  -   div that contains menu
     * @param  subMenu  -   the sub menus that will be created
     * @param  seq         -  menu's sequence number that is used to create menu menuBarId
     * @return  HTMLDiv -  the div contains sub Menus
     */
    
    private HTMLDiv buildSubMenu( HTMLDiv menuDiv,
            HTMLMenuBar subMenu)
    {
        String subMenuId = menuDiv.getId() + "_submenu";
        HTMLDiv  div = new HTMLDiv(subMenuId);
        div.setVisible(false);
        div.setPosition("absolute");
        div.setWidth( "100%");
        div.addComponent(subMenu);                     
        
        return div;
    }
    
    /** Adds a sub menu to a menu
     * @param  menuDiv  -   div that contains menu
     * @param  menuAnchor  -  menu containing anchor
     */
    
    private void addLink(  
        final HTMLDiv menuDiv,
        final HTMLMenuAnchor menuAnchor
    )
    {
        String href = menuAnchor.getHREF();
                
        if( 
            href.startsWith( "/") || 
            href.startsWith( "http://") ||
            href.startsWith( "https://")
        )
        {
            href = "window.open('" + href + "','_self')";
        }
        
        HTMLMouseEvent mouseClickEvent = new  HTMLMouseEvent(
            HTMLMouseEvent.onClickEvent,
            href
        );
        menuDiv.addMouseEvent( mouseClickEvent);

    }
    
    private HTMLComponent getMenuContents(HTMLMenuItem menu)
    {
        return menu.getLabel();
    }        
}
