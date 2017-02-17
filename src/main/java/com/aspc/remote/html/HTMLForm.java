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

import com.aspc.remote.html.input.HTMLInput;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  HTML FORM element
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       29 February 1998
 */
public class HTMLForm extends HTMLContainer
{
    /**
     * Create a new HTML FORM
     * 
     * @param uri 
     */
    public HTMLForm(final String uri)
    {
        if(StringUtilities.notBlank(uri))
        {
            assert uri.startsWith("javascript:") || StringUtilities.URI_PATTERN.matcher(uri).find(): uri;
        }
        this.uri = uri;
        this.method = "POST";
    }
    
    /**
     * Create a new HTML FORM
     * 
     * @param uri 
     * @param windowMode 
     */
    public HTMLForm(final String uri, final String windowMode)
    {
        this( uri);
        this.windowMode = windowMode;
    }    
    
    /**
     * get the ID of this component
     *
     * @return the ID
     */
    @Override
    public String getId()
    {
        return id;
    }
    
    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     * @return This current form
     */
    public HTMLForm setId( final String id)
    {
        iSetId(id);
        
        return this;
    }
    
    /**
     * Set the name of this form
     *
     * @param name The form name
     * @return This current form
     */
    public HTMLForm setName( final String name)
    {
        iSetName( name);
        
        return this;
    }
    
    /**
     * Turn on auto complete
     *
     * @param on turn ON or OFF
     * @return This current form
     */
    public HTMLForm setAutoComplete( final boolean on)
    {
        autoComplete=on;
        return this;
    }
    
    /**
     * Set the target window
     *
     * @param target The target window.
     * @return This current form
     */
    public HTMLForm setTarget( final String target)
    {
        this.target = target;
        return this;
    }
    
    /**
     * The form's method
     *
     * @param method POST or GET
     * @return This current form
     */
    public HTMLForm setMethod( final String method)
    {
        this.method = method;
        return this;
    }
    
    /**
     * Does this form handle files ?
     *
     * @return This current form
     * @param flag true if handles files
     */
    public HTMLForm setHandlesFiles( final boolean flag)
    {
        handlesFiles = flag;
        return this;
    }
    
    /**
     * The ON SUBMIT action
     *
     * @param submit The java script
     * @return This current form
     */
    public HTMLForm setOnSubmit( final String submit)
    {
        onSubmit = submit;
        return this;
    }
    
    /**
     * Set the on submit action
     *
     * @param browser The browser.
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        if( isCompiled() == true)
        {
            return;
        }
        HTMLPage parentPage = getParentPage();
        if( parentPage == null || parentPage.getFlag("messageID").equals(""))
        {
            HTMLInput messageID = new HTMLInput(
                "messageID",
                Long.toHexString( FORM_COUNT.incrementAndGet())
            );

            messageID.setInvisible(true);

            addComponent(messageID);
            if( parentPage != null) 
            {
                parentPage.putFlag("messageID", "DONE");
            }
        }
        
        super.compile( browser);
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
        String seperator = "?";
        if( uri.contains("?")) seperator="&amp;";
        buffer.append("<form action=\"").append(HTMLAnchor.htmlEncodeHREF(uri)).append( seperator).append( FORM_CHECK).append("=Y\" method=\"").append(method).append(
            "\" accept-charset=\"UTF-8\"");
        
        if( autoComplete == false)
        {
            buffer.append( " autocomplete=\"off\"");
        }
        
        if( handlesFiles)
        {
            buffer.append(
                " enctype=\"multipart/form-data\""
            );
        }
        
        if( target != null)
        {
            buffer.append(" target=\"").append(target).append( "\"");
        }
        
        if (browser.isBrowserIE())
        {
            buffer.append(" style=\"margin-bottom:0\"");
        
        }
        
        iGenerateAttributes(browser, buffer);
        
        if ( StringUtilities.isBlank(onSubmit) == false)
        {
            buffer.append(onSubmit);
        }
        else
        {

            String onSubmitStr;
            if (this.windowMode.equalsIgnoreCase("SDI"))
            {
                onSubmitStr = " onSubmit='window.onbeforeunload=null;parent.maskDialog(); return true'>\n";
            } else
            {
                onSubmitStr = " onSubmit='window.onbeforeunload=null;showCurtain(true); return true'>\n";
            }
           
            if( browser.isBrowserHTTPUnit())
            {
                buffer.append( " onSubmit='window.onbeforeunload=null; return true'>\n");
            }
            else
            {
                buffer.append(onSubmitStr);
            }
        }
        
        //buffer.append("<INPUT TYPE=hidden NAME='" + FORM_CHECK + "' VALUE='" + FORM_CHECK + "'>");
        super.iGenerate(browser, buffer);
        buffer.append("<input type=hidden name='" + FORM_END + "' value='" + FORM_END + "'>");
        
        buffer.append(
            "</form>\n"
        );
    }
    
    private static final AtomicLong FORM_COUNT=new AtomicLong();
    
    private boolean handlesFiles,
                    autoComplete;
    
    private String onSubmit = null;
    
    private String windowMode = "";
    
    private String  target,
                    method;
    private final String  uri;
    
    /**
     * String constant to check FORM start field
     */
    public static final String FORM_CHECK = "FC";

    /**
     * String constant to check FORM end field
     */    
    public static final String FORM_END = "FE";    
    
    /**
     * The mobile iframe name
     */
    public static final String MOBILE_IFRAME = "mobileiframe";
    
}
