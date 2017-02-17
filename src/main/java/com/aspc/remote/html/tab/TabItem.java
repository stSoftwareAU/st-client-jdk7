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
package com.aspc.remote.html.tab;

import com.aspc.remote.memory.HashMapFactory;
import java.util.HashMap;

/**
 *  TabItem.java
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       June 26, 2000, 9:52 PM
 */
public class TabItem
{
    /** Creates new TabItem */
    public TabItem(
       String code,
       String name,
       String optionalURL,
       String description)
    {
        this.code = code;
        this.name = name;
        this.optionalURL = optionalURL;
        //this.onLoadScript = onLoadScript;
        this.description = description;
        this.tabBase = null;
    }

    public String getName()
    {
        return name;
    }

    public void addOnLoadEvent( String call, String script)
    {
        if( scripts == null)
        {
            scripts = HashMapFactory.create();
        }

        if( script != null && script.equals( "") == false)
        {
            scripts.put( call, script.trim());
        }
    }


    public String getDescription()
    {
        return description;
    }

    public String getCode()
    {
        return code;
    }

    public String getOptionalURL()
    {
        return optionalURL;
    }

    public HashMap<String, String> getOnLoadScripts()
    {
        return scripts;
    }

    private String  name,//NOPMD
                    optionalURL,//NOPMD
                    code,//NOPMD
                    description,//NOPMD
                    tabBase;


    private HashMap<String, String> scripts;

    public String getTabBase()
    {
        return tabBase;
    }

    public void setTabBase(String tabBase)
    {
        this.tabBase = tabBase;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
