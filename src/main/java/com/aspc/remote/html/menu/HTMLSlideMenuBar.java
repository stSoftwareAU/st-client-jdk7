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

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.html.HTMLDiv;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLTable; 
import com.aspc.remote.html.HTMLComponent;
import com.aspc.remote.html.HTMLTags;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.html.style.HTMLStyleSheet;

import com.aspc.remote.html.style.InternalStyleSheet;
import java.util.ArrayList;
import java.awt.Color;

/**
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 * @author michael
 * @since 29 September 2006
 */
public class HTMLSlideMenuBar extends HTMLMenuBar
{
    private String title;
    
    /** Creates a new instance of HTMLSlideMenuBar
     * @param code
     */
    public HTMLSlideMenuBar( final String code)
    {
        super( code);
    }
   
    /**
     *
     * @param title
     */
    public void setTitle( final String title)
    {
        this.title = title;
    }
    
    /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser 
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        super.compile( browser);
        
        if( browser.isMDI() == false)
        {
            HTMLPage page = getParentPage();
            
            page.setTitle( title);
        }
    }
        
    /** 
     * Adds the menus to the menu bar table
     *
     * @param  page parent page
     * @param  table the table which menus are added in
     * @param  menuList the menu list
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
                table.setCell( menuDiv, rowNum++, colNum);
                if( menu instanceof HTMLMenuBar)
                {
                    HTMLMenuBar subMenu = (HTMLMenuBar)menu;
                    if( subMenu.getMenuList() != null &&
                        subMenu.getMenuList().size() > 0)
                    {
                        HTMLDiv subMenuDiv = buildSubMenu(table.getId(), menuDiv, subMenu, seq);
                        table.setCell( subMenuDiv, rowNum++, colNum);
                    }
                }  
                else if( menu instanceof HTMLMenuAnchor)
                {
                    addLink( menuDiv, (HTMLMenuAnchor)menu);
                    
                    HTMLMenuAnchor menuAnchor = (HTMLMenuAnchor)menu;
                    
                    if( menuAnchor.getHighlightSelection() == true )
                    {                        
//                        String strBgColor = makeColorID(getBgColor());
//                        Color selectionColor = getSelectionColor();
//                        String strSelColor;
//                        if (selectionColor == null)
//                        {
//                           strSelColor = strBgColor;   
//                        }
//                        else
//                        {
//                            strSelColor = makeColorID(selectionColor);  
//                        } 
                        HTMLMouseEvent mouseClick4Id = new  HTMLMouseEvent(
                            HTMLMouseEvent.onClickEvent,
                            "setSelectedMenu( '" + table.getId()  + "', '" + seq  + "')"
                        );     
                        menuDiv.addMouseEvent(mouseClick4Id);
                    }                   
                }
            }
            
            // Add script to maintain up/down state
            StringBuilder arrayScript = new StringBuilder();           
            arrayScript.append( "var " + table.getId() + "_menu_array = new Array(");
           
            for (int seq = 0; seq < menuList.size(); seq++)
            {
                Object objMenu = menuList.get(seq);
                if( seq > 0)
                {
                    arrayScript.append(",");
                }
                if (objMenu instanceof HTMLMenuBar  && ((HTMLMenuBar)objMenu).isOpen() == true  )
                {
                    arrayScript.append( "'down'");
                }
                else
                {
                    arrayScript.append( "'up'");
                }
            }

            arrayScript.append( ");");
            page.addJavaScript( arrayScript.toString());
            addShowMenuScript();
            addSelectedMenuScript();
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
        menuDiv.setWidth( "100%");
        menuDiv.setPosition( "relative");
        menuDiv.setCursor("hand");
        menuDiv.addComponent( getMenuContents( menuDiv, menu));
        
        int height = getMenuItemHeight();
        if( height == 0)
        {
           height = 20;
        }
        
        menuDiv.setHeight( "" + height);
        if (getMenuItemWidth() > 0)
        {
            menuDiv.setWidth("" + getMenuItemWidth());
        }
        setDivBorder(menuDiv);        
       
        Color selectedColor =  getSelectionColor();   
        
        String strBgColor = null;
        Color tmpBgColor = getBgColor();
        if (tmpBgColor != null)
        {
            if (menu.isSelected() == false)
            {
               strBgColor =  makeColorID(tmpBgColor);
               menuDiv.setBackgroundColor(strBgColor);
            }
            else 
            {
               if ( menu instanceof HTMLMenuAnchor)
               {
                    if (selectedColor== null)
                    {
                       strBgColor = makeColorID(tmpBgColor);
                    }
                    else
                    {
                        strBgColor = makeColorID(selectedColor);  
                    }                    
                    menuDiv.setBackgroundColor(strBgColor);                    
               }          
            }           
        }        
        
        setUpRollover(menuDiv, tableId);                
  
        return menuDiv;        
    }
    
   
    /**
     * Adds a sub menu to a menu
     * @param  tableId   -   Id of the table which menus are added in
     * @param  menuDiv  -   div that contains menu
     * @param  subMenu  -   the sub menus that will be created
     * @param  seq         -  menu's sequence number that is used to create menu menuBarId
     * @return  HTMLDiv -  the div contains sub Menus
     */
    
    private HTMLDiv buildSubMenu(  String tableId,
            HTMLDiv menuDiv,
            HTMLMenuBar subMenu,
            int seq)
    {
        boolean isOpen = subMenu.isOpen();
        String subMenuId = menuDiv.getId() + "_submenu";
        HTMLDiv  div = new HTMLDiv(subMenuId);
        div.setVisible(true);
        div.setWidth( "100%");
        div.addComponent(subMenu);
        div.setOverflow( "hidden");        
        div.setPosition( "relative");
        if ( isOpen == false)
        {
            div.setHeight( "0");
            div.setDisplayMode("none");
        }
        else
        {
            div.setHeight("" + subMenu.getFullHeight());
            div.setDisplayMode("block");
        }
        
        StringBuilder showMenuCmd = new StringBuilder(50);
        
        showMenuCmd.append( "javascript:slideMenu(");
        showMenuCmd.append( "'");
        showMenuCmd.append( tableId);
        showMenuCmd.append( "',");
        showMenuCmd.append( seq);
        showMenuCmd.append( ",");
        showMenuCmd.append( subMenu.getFullHeight());
        showMenuCmd.append( ");");
        HTMLMouseEvent mouseClickEvent = new  HTMLMouseEvent(
            HTMLMouseEvent.onClickEvent,
            showMenuCmd.toString()
        );
        menuDiv.addMouseEvent( mouseClickEvent);
        
        
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
    
    private HTMLComponent getMenuContents(  HTMLDiv menuDiv,
                                            HTMLMenuItem menu)
    {
        HTMLTable table = new HTMLTable();
        table.setWidth( "100%");
            
        int depth = getDepth();
 
        HTMLStyleSheet ss = getStyleSheet();
        ss.addElement( InternalStyleSheet.STYLE_FONT_FAMILY, "courier" );
        ss.addElement( InternalStyleSheet.STYLE_FONT_SIZE, "8px" );
        //intStyleSheet.setColour( "color", fc);
        
        StringBuilder buffer = new StringBuilder();
        String menuId = menuDiv.getId();

        HTMLPage page = getParentPage();
        if(page != null)
        {
            page.registerStyleSheet(ss);
        }
        int col = 0;
        if( depth >= 0)
        {
            buffer.append("<SPAN id=\"" + menuId + "_INDENT\" ");
            buffer.append("class=\"" + ss.getTarget() + "\"");
            buffer.append( ">");
            for( int i = 0; i <= depth; i++)
            {
                buffer.append( "&nbsp;");
            }
            buffer.append( "</SPAN>");
            table.setCell( new HTMLTags( buffer.toString()), 0, col);
            table.setCellWidth( "1", 0, col);
            col++;
        }
        
        buffer = new StringBuilder();
        buffer.append("<SPAN id=\"" + menuId + "_EXPANDER\" ");
        if(page != null && page.isPageCompiled())
        {
            buffer.append("class=\"" + ss.getTarget() + "\"");
        }
        else
        {
            buffer.append(ss.toInlineStyleSheet());
        }
        buffer.append( ">");        
        
        if( menu instanceof HTMLMenuBar)
        {
            buffer.append( "+");
        }
        else
        {
            buffer.append( "&nbsp;");
        }
        buffer.append( "</SPAN>");
        
        table.setCell( new HTMLTags( buffer.toString()), 0, col);
        table.setCellWidth( "1", 0, col);
        col++;
        table.setCell( menu.getLabel(),  0, col);      
        
        return table;
    }      
    
    
     /**
     * This method is to add script for showing sub menu
     */
    
    private void addShowMenuScript()
    {
        String script = "";
        script = "function slideMenu( menuBarId, seq, totalh, currh)\n" +
                "{\n" +            
                "var steps = 4; \n" +
                "var menuh =  parseInt( totalh/steps);  \n" +
                
                "var menuName = menuBarId + '_MENU_' + seq;\n" +
                "var subMenuName = menuName + '_SUBMENU';\n" +
                "var stateArr = eval(menuBarId + '_menu_array'); \n" +
                "var state =  stateArr[seq];\n" +
                "if( typeof(currh) == 'undefined' ) \n " + "" +
                "{ \n" +
                "  currh = ( state=='up'?0:totalh); \n" +              
                "} \n" +
                "else \n" +
                "{ \n" +
                "currh = ( state=='up'?currh+menuh:currh-menuh);\n" +      
                "if ( currh < 0) currh = 0;\n" +
                "if ( currh > totalh) currh = totalh;\n" +
                "} \n" +
                "var  subMenuDiv = findElement(subMenuName); \n" +
                "if ( currh == 0) \n" +
                "{ \n" +
                " subMenuDiv.style.display = (state == 'up'?'block':'none');  \n" +
                "} \n" +
                " else \n" +
                " { \n" +
                " subMenuDiv.style.height = currh; \n" +
                " } \n" +
                "if((state=='up' && currh < totalh) || (state=='down' && currh>0))\n" +
                "{\n" +
                "   timerID = setTimeout(\"slideMenu('\"+menuBarId+\"',\"+seq+\",\"+totalh+\",\"+currh+\")\",0);\n" +
                "}\n" +
                "else\n" +
                "{\n" +
                " timerID = ';'\n" +
                "  var  expander = findElement(menuName + '_EXPANDER'); \n" +
                "  if( expander != null){\n" +
                "     expander.innerHTML= (state=='up'?'-':'+')};\n" +                 
                "  eval(menuBarId + '_menu_array')[seq] = (state=='up'?'down':'up');\n" +
                "}\n" +
                "}";
            addJavaScript("ADDSHOWMENUSCRIPT" ,  script);
    }
    
    
      /**
     * This method is to get clicked div's menuBarId
     */
    
    private void addSelectedMenuScript()
    {
        String script = "";
        script ="var clickedTableId; \n"  + 
                "function setSelectedMenu( menuBarId, seq)\n" +
                "{\n" +
                  " if( clickedDivId != null)\n" +
                  " {\n" +
                  "   var  lastClickedDiv = findElement(clickedDivId); \n" +
                  "   var  lastClickedDivColorArr = eval(clickedTableId + '_ARRAY') \n" +
                  "   if (lastClickedDiv  != null && lastClickedDivColorArr != null && lastClickedDivColorArr[0] != null) { \n" +
                  "        lastClickedDiv.style.backgroundColor=lastClickedDivColorArr[0]; \n" +
                 "    }\n" +
                 " }\n" +
                 " clickedTableId = menuBarId; \n" +                    
                 " clickedDivId = menuBarId + '_MENU_' + seq ; \n" +    
                 " var  clickedDiv = findElement(clickedDivId );\n" +
                 " var  clickedDivSelectionColor = eval(menuBarId + '_ARRAY')[2] \n" +
                 " if (clickedDivSelectionColor != null) \n" +
                 "  { \n" +
                 "      clickedDiv.style.backgroundColor=clickedDivSelectionColor; \n" +
                 "  } \n" +
                "} \n";
                 
        addJavaScript("SETSELECTEDMENU",  script);
    }
    
   
}

