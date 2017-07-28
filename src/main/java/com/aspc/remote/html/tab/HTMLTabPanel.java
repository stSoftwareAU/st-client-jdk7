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
package com.aspc.remote.html.tab;

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.html.HTMLAnchor;
import com.aspc.remote.html.HTMLComponent;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *  HTMLTabPanel
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       June 25, 2000, 6:25 PM
 */
public class HTMLTabPanel extends HTMLComponent
{
    /**
     * Creates new MenuBar
     * @param baseUrl
     */
    public HTMLTabPanel(String baseUrl)
    {
        this( baseUrl, "MENU");
    }

    /**
     * Creates new MenuBar
     * @param baseUrl
     * @param identifier
     */
    public HTMLTabPanel(final String baseUrl, final String identifier)//,int screenVersion)
    {
        this.baseUrl = baseUrl;
       //  this.target = target;
        this.identifier = identifier;
//        this.screenVersion = screenVersion;
        
        list = new ArrayList<>();
    }

    /**
     *
     * @param flag
     */
    public void setHideTarget( boolean flag)
    {
        hideTarget = flag;
    }

    /**
     *
     * @param code
     * @param newName
     */
    public void modifyTabName(String code,String newName)
    {
        TabItem tab=findTab(code);
        if (tab!=null)
        {
            tab.setName(newName);
        }
    }

    /**
     *
     * @param code
     * @param name
     * @param selected
     * @param description
     * @param optionalURL
     * @return the value
     */
    public TabItem addTab(
        String code,
        String name,
        boolean selected,
        String description,
        String optionalURL
    )
    {
        return addTab(code,name,selected,description,optionalURL,-1,null);
    }

    /**
     *
     * @param code
     * @param name
     * @param selected
     * @param description
     * @param optionalURL
     * @param insertAt
     * @param tabBase
     * @return the value
     */
    public TabItem addTab(
        String code,
        String name,
        boolean selected,
        String description,
        String optionalURL,
        int insertAt,
        String tabBase
    )
    {
        TabItem ti = new TabItem( code, name, optionalURL, description);
        if (tabBase!=null)
        {
            ti.setTabBase(tabBase);
        }

        if (insertAt<0)
        {
            list.add(ti);
        }
        else
        {
            list.add(insertAt, ti);
        }

        if( selected == true || selectedItem == null)
        {
            selectedItem = ti;
        }

        return ti;
    }

    /**
     *
     * @param code
     * @param call
     * @param script
     */
    public void addOnLoadEvent( String code, String call, String script)
    {
        if( script != null && StringUtilities.notBlank(script))
        {
           TabItem ti = findTab( code);
           if( ti != null)
           {
               ti.addOnLoadEvent( call, script.trim());
           }
        }
    }

    /**
     *
     * @return the value
     */
    public String getSelectedTarget()
    {
        if( selectedItem != null)
        {
            return selectedItem.getOptionalURL();
        }

        return "";
    }


    /**
     *
     * @return the value
     */
    public HashMap<String, String> getSelectedLoadScripts()
    {
        if( selectedItem != null)
        {
            return selectedItem.getOnLoadScripts();
        }

        return null;
    }

    /**
     *
     * @param code
     */
    public void setSelected( String code)
    {
        TabItem ti = findTab( code);
        if( ti != null)
        {
            selectedItem = ti;
        }
    }

    /**
     *
     * @param code
     * @return the value
     */
    protected TabItem findTab( String code)
    {
        for( TabItem ti: list)
        {
            if( ti.getCode().equals( code))
            {
                return ti;
            }
        }

        return null;
    }

    /**
     * 
     *
     *  <ul class="tabs">
     *     <li><a href="#" class="defaulttab" rel="tabs1">Quick Start</a></li>
     *     <li><a href="#" rel="tabs2">Misc</a></li>
     *     <li><a href="#" rel="tabs3">Info</a></li>
     *     <li><a href="#" rel="tabs4">Log</a></li>
     *  </ul>
     * @param browser
     * @param buffer
     */
    @Override
    protected void iGenerate(final ClientBrowser browser, final StringBuilder buffer)
    {
        /**
         * @TODO This should NOT have been using the text directly. You will NOT be handling special characters. 
         * 
         * @TODO the DIV should include the whole tab. Run in IE with the debugger you'll be getting lots of errors.
         */
        buffer.append("<div id='slider'>\n");
        buffer.append("<ul class=\"tabs\">\n");

        for( TabItem ti: list)
        {
            String href;

            if (ti.getTabBase()!=null)
            {
                href=ti.getTabBase();
            }
            else
            {
                href=baseUrl;
            }

            href = href.replace( "%MENU%", ti.getCode());

            if( !href.contains("javascript:"))
            {

                if( href.contains("?"))
                {
                    href += "&";
                }
                else
                {
                    href += "?";
                }
                String targetUrl= "",
                    url;

                url = ti.getOptionalURL();

                if( url != null && StringUtilities.notBlank(url) && hideTarget == false)
                {
                    targetUrl = "&ETARGET=" + StringUtilities.encodeHex(url);
                }
                href= href + identifier + "=" + ti.getCode() + targetUrl;
            }

            buffer.append("<li>" );

            boolean isOldIE = false;
            if(browser.canHandleCSS3() == false)
            {
                isOldIE = true;
            }

            if(isOldIE)
            {
                buffer.append("<div class='tab-button-left");
                if(ti == selectedItem)
                {
                    buffer.append( "-selected");
                }
                buffer.append("'></div>");
                buffer.append("<div class='tab-button-center");
                if(ti == selectedItem)
                {
                    buffer.append( "-selected");
                }
                buffer.append("'>");
            }

            buffer.append( "<a");

            buffer.append(" onClick=\"");
            if( browser.isBrowserIE())
            {
                buffer.append("javascript:allowUnload=true;");
            }
            String call = href;

            if( href.startsWith("/"))
            {
                call = "window.location= '" + HTMLAnchor.htmlEncodeHREF(href)+ "';";
            }
            buffer.append(call).append(";\"");
            buffer.append(" href=\"#").append(ti.getCode()).append( "\" ");
            
            if(isOldIE == false && ti == selectedItem)
            {
                buffer.append( " class=\"selected\"");
            }
            buffer.append( ">");
            String tmpName = ti.getName();
            String eName;
            eName = StringUtilities.encodeHTML(tmpName);

            buffer.append(eName);

            buffer.append("</a>");

            if(isOldIE)
            {
                buffer.append("</div>");
                buffer.append("<div class='tab-button-right");
                if(ti == selectedItem)
                {
                    buffer.append( "-selected");
                }
                buffer.append("'></div>");
            }

            buffer.append("</li>\n");
        }

        buffer.append("</ul>\n");

        super.iGenerate(browser, buffer);

        buffer.append("</div>\n");
    }

    @Override
    protected void setParent(final HTMLComponent parent) {
        super.setParent(parent); 
        
        HTMLPage parentPage = getParentPage();
        if( parentPage != null)
        {
            parentPage.addModule("jquery");
            parentPage.addModule("jquery-ui-tabs-paging");

            parentPage.addJavaScript(
                "$(function() {\n" +
                "var s=$('#slider');\n"+
                "if(s && s.tabs){\n  s.tabs();\n" +
                "  s.tabs('paging');\n}\n" +
                "});"
            );
        }
    }

    private String  identifier,
                    baseUrl;

    private ArrayList<TabItem> list;
    private TabItem selectedItem;
    private boolean hideTarget;
}
