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
package com.aspc.remote.html.scripts;
import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.util.misc.StringUtilities;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  ScriptLink.java
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       September 13, 2000, 3:20 PM
 */
public class ScriptLink extends JavaScript
{
    public static enum LoadType{
        BLOCKING, 
        DEFERED,
        ASYNC;
    }
    
    /**
     * The script URL
     */
    public final String url;
    public final LoadType loadType;
    public final String cdnFallBackScript;
    
    private long userTime;
    private static final long START_TIME=System.currentTimeMillis();
      
    /**
     * Creates new ScriptLink
     * @param url raw unescaped url
     * @param loadType the type of loading.
     */
    public ScriptLink(
        final @Nonnull String url, 
        final @Nonnull LoadType loadType
    )
    {
        this( url, loadType, null);
    }
    
    /**
     * Creates new ScriptLink
     * @param url raw unescaped url
     * @param loadType the type of loading.
     * @param cdnFallBackScript fall back script
     */
    public ScriptLink(
        final @Nonnull String url, 
        final @Nonnull LoadType loadType, 
        final @Nullable String cdnFallBackScript
    )
    {
        super();

        assert url.toLowerCase().contains("&amp;") == false : "url should be unescaped: " + url;
        this.url = url;
        this.loadType=loadType;
        this.cdnFallBackScript=cdnFallBackScript;
        
        if( StringUtilities.notBlank(cdnFallBackScript))
        {
            if( url.matches("(https:|http:)*//.+")==false)
            {
                throw new IllegalArgumentException("CDN fall back is only allowed for CDN links");
            }
        }
    }

    /**
     *
     * @param time
     */
    public void setTimeStamp( final long time)
    {
        userTime = time;
    }

    /**
     * Generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    public void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        String tmpURL = url.trim();

        if( tmpURL.matches("(https:|http:)*//.+")==false)
        {
            if( tmpURL.startsWith("/ds/") == false)
            {
                long ts;

                /*
                 * Add a unique Id to the end of the URL so that
                 * the proxies don't keep a old copy.
                 */
                ts = START_TIME;
                if( userTime != 0)
                {
                    ts = userTime;
                }

                if( tmpURL.indexOf('?') == -1)
                {
                    tmpURL += "?ts=" + ts;
                }
                else
                {
                    tmpURL += "&ts=" + ts;
                }
            }
        }
        
        String qualifier="";
        if( loadType==LoadType.ASYNC)
        {
            qualifier="async ";
        }
        
        /**
         * HTML5 doesn't require the type
         * http://www.w3schools.com/html5/att_script_src.asp
         */
        //escape &, <, >, " and '
        tmpURL = tmpURL.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
//        tmpURL = StringUtilities.encodeHTML(tmpURL); //encodeHTML method encode too many characters, it makes url looks hard for human being
        buffer.append("\n<script ").append(qualifier).append( "src=\"").append(tmpURL).append( "\"");
        
        /**
         * add crossorigin="anonymous" property for the script link if the domain support cross domain script request,
         * so that our st-error could track the details of the javascript error instead of just log a useless "Script error." message
         */
        if(knownSupportCORS(tmpURL))
        {
            buffer.append(" crossorigin=\"anonymous\"");
        }
        buffer.append("></script>");
        
        if( StringUtilities.notBlank(cdnFallBackScript))
        {
            buffer.append("\n<script>\n").append(cdnFallBackScript.trim()).append( "\n</script>");            
        }
    }
    
    private boolean knownSupportCORS(final @Nonnull String tmpURL)
    {
        for(String domain : DOMAIN_SUPPORT_CORS)
        {
            if(tmpURL.contains(domain))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * these domain support cross-domain script
     */
    private static final String[] DOMAIN_SUPPORT_CORS = {
        "//cdnjs.cloudflare.com/",
        "//maxcdn.bootstrapcdn.com/",
        "//ajax.googleapis.com/"
    };
}
