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

import com.aspc.developer.ThreadCop;
import com.aspc.remote.database.NotFoundException;
import static com.aspc.remote.html.HTMLAnchor.validateHREF;
import com.aspc.remote.html.input.HTMLInput;
import com.aspc.remote.html.internal.HandlesSingleClick;
import com.aspc.remote.html.internal.InternalEvent;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.Version;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.logging.Log;

/**
 *  A HTMLComponent which generates the HTML code for the specified text.
 *  An image file is produced based on the current font and color settings specified in
 *  the current theme.
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author         Nigel Leck
 *  
 *  @since          November 1, 1999, 7:16 PM
 */
public class HTMLButton extends HTMLComponent implements HandlesSingleClick
{
    private static final String SPAWN_WIN_WIDTH = "spawnWinWidth";
    private static final String SPAWN_WIN_HEIGHT = "spawnWinHeight";
    private static final String NOT_REPLACE_WINDOW = "notReplaceWindow";
    private String target;
    public static final Version VERSION=new Version( "HTML_BUTTON", 2, 1, 2);

    private boolean noValidate;
    
    /**
     * A constructor with one string parameter.
     * @param displayName
     */
    public HTMLButton(final String displayName)
    {
        this( displayName, null);

    }

    /**
     * constructor with two parameter
     * @param displayName
     * @param name
     */
    public HTMLButton(
        String displayName,
        final String name
    )
    {
        this.displayName = displayName;
        String tempName = name;
        if( StringUtilities.isBlank(tempName))
        {
            tempName = displayName;
        }

        setName( tempName);//NOPMD
        ext ="";
    }

    public HTMLButton(String name, int fldCount, ArrayList pathList)
    {
        this.displayName = name;
        this.fldCount = fldCount;
        this.pathList = pathList;
    }

    public void setTarget( final String target)
    {
        this.target=target;
    }

    /**
     * no validate
     * @param flag true if not to validate. 
     */
    public void setNoValidate( final boolean flag)
    {
        noValidate=flag;
    }
    
    /**
     * Is GWT button
     * @param flag
     * @param autoSave
     */
    public void setButtGWTFg(final boolean flag, final boolean autoSave)
    {
        buttGWTFg = flag;
        this.autoSave=autoSave;
    }

    /**
     * set is GWT button
     * @return the value
     */
    public boolean getButtGWTFg()
    {
        return buttGWTFg;
    }
    /**
     * return string value.
     * @return the value
     */
    public String getValue()
    {
        return displayName;
    }

    /**
     *
     * @param value string type
     */
    public void setValue( String value)
    {
        this.displayName = value;
    }

    /**
     *
     * @return the value
     */
    public String getName()
    {
        return name;
    }


    /**
     *
     * @param name the name
     */
    public final void setName( final String name)
    {
        this.name=name;
        String tmpName = name.trim();
        tmpName = StringUtilities.replace( tmpName, " ","_");
        while( tmpName.contains("__"))
        {
            tmpName = StringUtilities.replace( tmpName, "__","_");
        }
        tmpName = tmpName.toUpperCase();

        iSetName( tmpName);
    }

    /**
     * Sets button width
     * @param width int type
     */
    public void setButtonWidth(final int width)
    {
        buttonWidth = width;
    }

    /**
     * return button width
     * @return int type
     */
    public int getButtonWidth()
    {
        return buttonWidth;
    }

     /**
     * Sets button height
     * @param height int type
     */
    public void setButtonHeight(final int height)
    {
        buttonHeight =height;
    }

    /**
     * return button Height
     * @return int type
     */
    public int getButtonHeight()
    {
        return buttonHeight;
    }
    /**
     * Sets button background color
     * @param color Color type
     */
    public void setButtonColor(final Color color)
    {
        buttonColor = color;
    }

    /**
     * return button color
     * @return type color
     */
    public Color getButtonColor()
    {
        return buttonColor;
    }

    /**
     * Sets button font name.
     * @param fontName string type
     */
    public void setButtonFontName(final String fontName)
    {
        this.btnFontName=fontName;
    }

    /**
     * return button font name
     * @return type string
     */
    public String getButtonFontName()
    {
        return btnFontName;
    }

    /**
     * Sets button font bold style
     * @param fontBold Boolean type
     */
    public void setButtonFontBold(final Boolean fontBold)
    {
        this.btnFontBold=fontBold;
    }

    /**
     * return button font bold style is true or not
     * @return Boolean type
     */
    public Boolean getButtonFontBold()
    {
        return btnFontBold;
    }

    /**
     * Sets button font size
     * @param fontSize Integer type
     */
    public void setButtonFontSize(final Integer fontSize)
    {
        this.btnFontSize=fontSize;
    }

    /**
     * return button font size
     * @return Integer type
     */
    public Integer getButtonFontSize()
    {
        return btnFontSize;
    }

    /**
     * Sets button font color.
     * @param color
     */
    public void setButtonFontColor(final Color color)
    {
        this.btnFontColor=color;
    }

    /**
     * return button font color
     * @return the value
     */
    public Color getButtonFontColor()
    {
        return btnFontColor;
    }

    /**
     * Prevent the click of the mouse from propagation up the dom and kicking off other things.
     */
    @Override
    public void cancelClickBubble()
    {
        cancelBubble = true;
        touch();
    }

    /**
     *
     * @param call
     * @param script
     */
    public void addOnClickEvent( final String call, final String script)
    {
        iAddEvent( new InternalEvent( "onClick", call), script);
    }

    /**
     * add a mouse event to this component
     * @param me The mouse event
     */
    public void addMouseEvent(HTMLMouseEvent me)
    {
        if( mouseEvents == null)
        {
            mouseEvents = new ArrayList<>();
        }

        mouseEvents.add( me);
    }

    /**
     * Disables this input.
     * @param flag
     */
    public void setDisabled( boolean flag)
    {
        disabledFg = flag;
    }

    /**
     *
     * @param flag
     */
    public void setPlaceHolderOnly( boolean flag)
    {
        placeHolderOnly = flag;
    }

    /**
     *
     * @param href
     */
    public void setImage( final String href)
    {
        assert validateHREF(href): "invalid href: " + href;
        image = href;
    }
    
    public String getImage()
    {
        return image;
    }

    /**
     * if the href starts with javascript:, it will be put in a javascript function, and
     * put in the &lt;script> tag. This means the href should NOT be html escaped. 
     * eg: & should be &, not &amp;. ' should NOT be &#39;
     * you may use javascript escape to escape ' or " etc to \' and \"
     * @param href
     */
    public void setCall( final String href)
    {
        assert validateHREF(href): "invalid href: " + href;
        this.href = href;
    }

    /**
     *
     * @param spawnWinHeight
     */
    public void setSpawnWinHeight(int spawnWinHeight)
    {
        this.spawnWinHeight = spawnWinHeight;
    }

    /**
     *
     * @param spawnWinWidth
     */
    public void setSpawnWinWidth(int spawnWinWidth)
    {
        this.spawnWinWidth = spawnWinWidth;
    }

    /**
     *
     * @return the value
     */
    public int getSpawnWinHeight()
    {
        return spawnWinHeight;
    }

    /**
     *
     * @return the value
     */
    public int getSpawnWinWidth()
    {
        return spawnWinWidth;
    }

    /**
     * @return the value
     */
    public boolean getNotReplasceWindow()
    {
        return notReplaceWindow;
    }

    /**
     * @param notReplaceWindow
     */
    public void setNotReplaceWindow(boolean notReplaceWindow)
    {
        this.notReplaceWindow = notReplaceWindow;
    }

    /**
     *
     * @return the value
     */
    public String getCall()
    {
        return href;
    }

    /**
     *
     * @param sql
     */
    public void setSqlCommand( String sql)
    {
        this.sqlCommand = sql;
    }

    /**
     *
     * @param href
     */
    public void setRedirectCall( final String href)
    {
        assert validateHREF(href): "invalid href: " + href;
        this.redirectHref = href;
    }

    /**
     *
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }
    
    public String getToolTip()
    {
        return toolTip;
    }

    /**
     *
     * @param target
     */
    public void setRedirectTarget( String target)
    {
        this.redirectTarget = target;
    }

    /**
     *
     * @param href
     */
    public void setSpawnCall( final String href)
    {
        //assert href.startsWith("javascript:") == false: "Should not start with javascript " + href;
        assert href.contains("&amp;") == false: "href should NOT be html encoded (contains &amp;) " + href;
        assert validateHREF(href): "invalid href: " + href;
        this.spawnHref = href;
    }

    /**
     *
     * @param flag
     */
    public void setShowNavigateAwayWarning( boolean flag)
    {
        showNavigateAwayWarning = flag;
    }

    /**
     *
     * @param browser
     */
    @Override
    @SuppressWarnings({"AssertWithSideEffects", "ResultOfMethodCallIgnored"})
    protected void compile( final ClientBrowser browser)
    {
        assert ThreadCop.modify(getParentPage());
        String url = generateUrl(browser);
        StringBuilder onClickScript = getOnclickScript(browser);

        if(buttGWTFg == true && browser.canHandleGWT())
        {
            int iCount = 0;
            HTMLPage page = getParentPage();
            page.addGWT("com.aspc.gwt.editablereport.EditableReport");

            String counter = page.getFlag("EditableReport:counter");
            int tmpID = 1;

            if( StringUtilities.isBlank( counter) == false)
            {
                tmpID = Integer.parseInt(counter);
                tmpID++;
            }

            page.putFlag("EditableReport:counter", "" + tmpID);

            suggestId="ER" + tmpID;

            if(pathList.size() > 0)
            {
                Iterator iter = pathList.iterator();
                while (iter.hasNext())
                {
                    iCount += 1;
                    String paths = (String) iter.next();
                    page.addToDictionary("DIC_"+suggestId,"PATH"+iCount, paths);
                }
            }
            page.addToDictionary("DIC_"+suggestId,"GB_KEY", displayName);
            page.addToDictionary("DIC_"+suggestId,"EDIT_FIELDS", String.valueOf(fldCount));

            page.addToList("DIC",suggestId);
            if( autoSave)
            {
                page.addToDictionary("DIC_"+suggestId,"AUTO_SAVE", "true");
            }
            editableRecord =  true;
        }
        else
        {

            HTMLPage page = getParentPage();

            for(int i =0; page != null; i++)
            {
                String key = "ID:" + id + ext;

                if( page.getFlag(key).equals( ""))
                {
                    page.putFlag(key, "DONE");
                    break;
                }

                ext = "_v" + (i +1);
            }

            if( StringUtilities.isBlank( url) == false)
            {
                HTMLAnchor a = new HTMLAnchor( url);
                a.setShowNavigateAwayWarning( showNavigateAwayWarning);
                a.setId( id + ext );

                if( mouseEvents != null)
                {
                    for (HTMLMouseEvent me : mouseEvents)
                    {
                        a.addMouseEvent( me);
                    }
                }

                if( tabIndex != null && tabIndex != 0)
                {
                    a.setTabIndex( tabIndex);
                }

                if( events != null)
                {
                    for (HTMLEvent event: events)
                    {
                        a.iAddEvent(event, null);
                    }
                }

                if( StringUtilities.isBlank( redirectHref) == false)
                {
                    addJavaScript(
                            "HAS_REDIRECT",
                            "function setRedirect( theCall, theTarget)\n" +
                            "{\n"+
                            "  var aElement;\n" +
                            "  aElement = findElement( 'REDIRECT_URL');\n" +
                            "  if( aElement !== null)\n" +
                            "  {\n" +
                            "    aElement.value = decodeHex(theCall);\n" +
                            "  }\n" +
                            "  aElement = findElement( 'REDIRECT_TARGET');\n" +
                            "  if( aElement !== null && theTarget != null && theTarget != '')\n" +
                            "  {\n" +
                            "    aElement.value = decodeHex(theTarget);\n" +
                            "  }\n" +
                            "  var theButton;\n" +
                            "  theButton = findElement( 'UPDATE');\n" +
                            "  allowUnload=true;\n" +
                            "  theButton.form.submit();\n" +
                            "}"
                            );

                    // Add components to the screen to handle redirection
                    // I could have checked the HAS_REDIRECT flag on the page
                    // but I thought that these params could have been created by
                    // things other then the HTMLButton
                    if( page != null)
                    {
                        HTMLInput redirect;
                        try
                        {
                            page.findId( "REDIRECT_URL");
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( "REDIRECT_URL");
                            redirect.setInvisible(true);
                            iAddComponent(redirect);
                        }

                        try
                        {
                            page.findId( "REDIRECT_TARGET");
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( "REDIRECT_TARGET", "");
                            redirect.setInvisible(true);
                            iAddComponent(redirect);
                        }
                    }
                }

                if( StringUtilities.isBlank( spawnHref) == false)
                {
                    if( VERSION.calculateVersion() > 1)
                    {
                        addJavaScript(
                                "HAS_SPAWN",
                            "function setSpawn( theCall, theTarget)\n" +
                                "{\n"+
                                "  var aElement;\n" +
                                "  aElement = findElement( 'SPAWN');\n" +
                                "  if( aElement !== null)\n" +
                                "  {\n" +
                                "    aElement.value = decodeHex(theCall);\n" +
                                "  }\n" +
                                "  aElement = findElement( 'SPAWN_UPDATE');\n" +
                                "  if( aElement !== null)\n" +
                                "  {\n" +
                                "    aElement.value = 'YES';\n" +
                                "  }\n" +
                                "  aElement = findElement( 'SPAWN_TARGET');\n" +
                                "  if( aElement !== null)\n" +
                                "  {\n" +
                                "    aElement.value = decodeHex(theTarget);\n" +
                                "  }\n" +

                        "  aElement = findElement( 'SPAWN_NO_WINDOW');\n" +
                        "  if( aElement !== null)\n" +
                        "  {\n" +
                        "    aElement.value = 'YES';\n" +
                        "  }\n" +

                                "  var theButton;\n" +
                                "  theButton = findElement( 'UPDATE');\n" +
                                "  allowUnload=true;\n" +
                                "  theButton.form.submit();\n" +
                                "}"
                        );
                    }
                    else
                    {
                        addJavaScript(
                            "HAS_SPAWN",
                            "function setSpawn( theCall)\n" +
                            "{\n"+
                            "  var aElement;\n" +
                            "  aElement = findElement( 'SPAWN');\n" +
                            "  if( aElement !== null)\n" +
                            "  {\n" +
                            "    aElement.value = decodeHex(theCall);\n" +
                            "  }\n" +
                            "  aElement = findElement( 'SPAWN_UPDATE');\n" +
                            "  if( aElement !== null)\n" +
                            "  {\n" +
                            "    aElement.value = 'YES';\n" +
                            "  }\n" +
                            "  var theButton;\n" +
                            "  theButton = findElement( 'UPDATE');\n" +
                            "  allowUnload=true;\n" +
                            "  theButton.form.submit();\n" +
                            "}"
                        );
                    }


                    if( page != null)
                    {
                        HTMLInput redirect;
                        try
                        {
                            page.findId( "SPAWN");
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( "SPAWN");
                            redirect.setInvisible(true);
                            iAddComponent(redirect);
                        }

                        try
                        {
                            page.findId( "SPAWN_UPDATE");
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( "SPAWN_UPDATE");
                            redirect.setInvisible(true);
                            iAddComponent(redirect);
                        }

                        try
                        {
                            page.findId( "SPAWN_HEADER");
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( "SPAWN_HEADER");
                            redirect.setInvisible(true);
                            redirect.setValue(this.getName());
                            iAddComponent(redirect);
                        }

                        try
                        {
                            page.findId( SPAWN_WIN_WIDTH);
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( SPAWN_WIN_WIDTH);
                            redirect.setInvisible(true);
                            redirect.setValue(Integer.toString(this.getSpawnWinWidth()));
                            iAddComponent(redirect);
                        }

                        try
                        {
                            page.findId( SPAWN_WIN_HEIGHT);
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( SPAWN_WIN_HEIGHT);
                            redirect.setInvisible(true);
                            redirect.setValue(Integer.toString(this.getSpawnWinHeight()));
                            iAddComponent(redirect);
                        }

                        try
                        {
                            page.findId( NOT_REPLACE_WINDOW);
                        }
                        catch( NotFoundException nf)
                        {
                            redirect = new HTMLInput( NOT_REPLACE_WINDOW);
                            redirect.setInvisible(true);
                            iAddComponent(redirect);
                        }

                        if( VERSION.calculateVersion()> 1)
                        {
                            try
                            {
                                page.findId( "SPAWN_TARGET");
                            }
                            catch( NotFoundException nf)
                            {
                                redirect = new HTMLInput( "SPAWN_TARGET");
                                redirect.setInvisible(true);
                                iAddComponent(redirect);
                            }
                            try
                            {
                                page.findId( "SPAWN_NO_WINDOW");
                            }
                            catch( NotFoundException nf)
                            {
                                redirect = new HTMLInput( "SPAWN_NO_WINDOW");
                                redirect.setInvisible(true);
                                iAddComponent(redirect);
                            }
                        }
                    }
                }

                if( StringUtilities.isBlank( sqlCommand) == false)
                {
                    addJavaScript(
                        "HAS_SQL_COMMAND",
                        "function setSqlCommand( theCall)\n" +
                        "{\n"+
                        "  var aElement;\n" +
                        "  aElement = findElement( 'SQL_COMMAND');\n" +
                        "  if( aElement !== null)\n" +
                        "  {\n" +
                        "    aElement.value = decodeHex(theCall);\n" +
                        "  }\n" +
                        "  var theButton;\n" +
                        "  theButton = findElement( 'UPDATE');\n" +
                        "  allowUnload=true;\n" +
                        "  theButton.click();\n" +
                        "}"
                    );

                    HTMLInput sqlInput;
                    if( page != null)
                    {
                        try
                        {
                            page.findId( "SQL_COMMAND");
                        }
                        catch( NotFoundException nf)
                        {
                            sqlInput = new HTMLInput( "SQL_COMMAND");
                            sqlInput.setInvisible(true);
                            iAddComponent(sqlInput);
                        }
                    }
                }
            }
            else
            {
                if( mouseEvents != null)
                {
                    for (HTMLMouseEvent me : mouseEvents)
                    {
                        iAddEvent( me, "");
                    }
                }
            }

            if( iHasEvent(HTMLMouseEvent.onClickEvent))
            {
                shouldSubmitForm=false;
            }

            // Add an event so that we will not get a 'are you sure you want to close this window msg'
            // This is for IE only
            if( browser.isBrowserIE() && showNavigateAwayWarning == false)
            {
                if( onClickScript == null)
                {

                    HTMLMouseEvent unloadMe = new HTMLMouseEvent(
                    HTMLMouseEvent.onClickEvent,
                        "javascript:allowUnload=true"
                    );

                    iAddEvent(unloadMe, "");
                }
//                else
//                {
//                    onClickScript.insert(0, "allowUnload=true;\n");
//                }
            }
        }

        if( onClickScript != null)
        {
            StringBuilder sb=new StringBuilder();
            
            String functionCall=id + ext + "_onClick";
            functionCall = functionCall.replaceAll("[\\W]", "_");
            sb.append("function ").append(functionCall).append( "()\n{\n");
            String tmp=onClickScript.toString();
            final String javaScriptPlaceholder="javascript:";
            if( tmp.startsWith(javaScriptPlaceholder))
            {
                tmp=tmp.substring(javaScriptPlaceholder.length());
            }
            tmp=tmp.trim();
            while( tmp.endsWith(";"))
            {
                tmp=tmp.substring(0, tmp.length() -1).trim();
            }
            sb.append(tmp);
           // onClickScript.insert(0, "function " + functionCall + "()\n{\n");
            sb.append(";\n}\n");
            addJavaScript(functionCall, sb.toString());
            HTMLMouseEvent onClick = new HTMLMouseEvent(
                HTMLMouseEvent.onClickEvent,
                "javascript:" + functionCall + "()"
            );

            iAddEvent(onClick, "");
            shouldSubmitForm=false;
        }

        if( browser.isBrowserIE() && shouldSubmitForm == false && browser.getBrowserVersion() < 9)
        {
            HTMLMouseEvent onClick = new HTMLMouseEvent(
                HTMLMouseEvent.onClickEvent,
                "return false"
            );

            iAddEvent(onClick, "");
        }

        super.compile(browser);
    }
    
    private String generateUrl(final ClientBrowser browser)
    {
        String url = "";
        if(buttGWTFg == false || browser.canHandleGWT() == false)
        {
            if( StringUtilities.isBlank( redirectHref) == false)
            {
                url = "javascript:setRedirect('" + StringUtilities.encodeHex(redirectHref) + "','" + StringUtilities.encodeHex(redirectTarget) +"')";
            }
            else if( StringUtilities.isBlank( spawnHref) == false)
            {
                if( VERSION.calculateVersion() > 1 && StringUtilities.notBlank(redirectTarget))
                {
                    url = "javascript:setSpawn('" + StringUtilities.encodeHex(spawnHref) + "','" + StringUtilities.encodeHex(redirectTarget) +"')";
                }
                else
                {
                    url = "javascript:setSpawn('" + StringUtilities.encodeHex(spawnHref) + "')";
                }
            }
            else if( StringUtilities.isBlank( sqlCommand) == false)
            {
                url = "javascript:setSqlCommand('" + StringUtilities.encodeHex(sqlCommand) + "')";
            }

            if( StringUtilities.isBlank( href) == false)
            {
                if(StringUtilities.isBlank(url)== false)
                {
                    if(href.startsWith("javascript") )// we can add javascript only if href is a script call.
                    {
                         url = href +";"+url;
                    }
                    else
                    {
                         LOGGER.error( "Can't create button with supplied Script: "+ url, new Exception("A JavaScript call in href"+ " " + href));
                    }
                }
                else
                {
                    url = href;
                }
            }
        }
        return url;
    }
    
    public StringBuilder getOnclickScript(final ClientBrowser browser)
    {
        String url = generateUrl(browser);
        StringBuilder onClickScript = null;
        if( StringUtilities.isBlank( url) == false)
        {
            onClickScript=new StringBuilder( url);

            if( StringUtilities.isBlank(href)==false)
            {
                onClickScript=new StringBuilder();

                if( href.startsWith("javascript:"))
                {
                    String tmpHref=href.substring("javascript:".length()).trim();
                    tmpHref=tmpHref.replace("&amp;", "&");
                    onClickScript.append(tmpHref);
                    if( onClickScript.charAt(onClickScript.length()-1) != ';')
                    {
                        onClickScript.append(";");
                    }
                }
                else
                {
                    onClickScript.append("window.open( \"");
                    String jsHREF=StringUtilities.decode(href).replace("\"", "&quot;");
                    onClickScript.append(jsHREF);
                    if( StringUtilities.notBlank(target))
                    {
                        onClickScript.append("\",\"");
                        onClickScript.append(target.replace("\"", "&quot;"));
                    }
//                        onClickScript.append( "\");\n");
                    onClickScript.append( "\").focus();\n");
                }
            }
            // Add an event so that we will not get a 'are you sure you want to close this window msg'
            // This is for IE only
            if( browser.isBrowserIE() && showNavigateAwayWarning == false)
            {
                onClickScript.insert(0, "allowUnload=true;\n");
            }
        }
        return onClickScript;
    }

    /**
     * generate the raw HTML for this component.
     *
     * http://allinthehead.com/retro/330/coping-with-internet-explorers-mishandling-of-buttons
     *
     * http://hardlikesoftware.com/weblog/2007/02/27/html-button-is-not-useful/
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        // Place Holders are not to generate any HTML

        if(editableRecord)
        {
            buffer.append("<div id=\"").append(suggestId).append("\"></div>");
            super.iGenerate(browser, buffer);
        }
        else
        {
            if( placeHolderOnly )
            {
                return;
            }

            String  type,
                    other = "";

            type = "submit";
            String className = this.getClassName();
            if( StringUtilities.isBlank(className) == true)
            {
                className = "x-btn-txt";
            }
            else
            {
                className = "x-btn-txt" + " " + className;
            }


            if( browser.isBrowserHTTPUnit())
            {
                buffer.append("<input name='");
                buffer.append(StringUtilities.encodeHTML(name));
                buffer.append("' id='");
                buffer.append( id);
                buffer.append( ext);
                buffer.append("' class=\"").append(className).append("\" type='submit' value='");
                buffer.append(StringUtilities.encodeHTML(displayName));
                buffer.append("'");
                iGenerateEvents( browser, buffer);
                buffer.append(">");
                return;
            }

            boolean imageComponent=false;
            boolean inputComponent=false;

            if( StringUtilities.isBlank(image))
            {
                if( browser.isBrowserIE() && browser.getBrowserVersion() < 9)
                {
                    inputComponent=true;
                    buffer.append("<input value='");
                    buffer.append(StringUtilities.encodeHTML(displayName));
                    buffer.append("'");
                }
                else
                {
                    buffer.append("<button");
                }
                buffer.append( " class=\"").append(className).append("\"");
            }
            else
            {
                type="image";
                if( StringUtilities.isBlank(href) == false)
                {
                    buffer.append("<img");
                    buffer.append( " class=\"x-btn-image\"");
                    imageComponent=true;
                }
                else
                {
                    if( shouldSubmitForm==false)
                    {
                        imageComponent=true;
                        buffer.append( "<img class=\"x-btn-image\"");
                    }
                    else
                    {
                        buffer.append( "<input class=\"x-btn-image\"");
                    }
                }
            }

            buffer.append( " name=\"");
            buffer.append( StringUtilities.encodeHTML( name));
            buffer.append( "\"");

            boolean tmpNoValidate=noValidate;
            if( imageComponent == false)
            {
                if( shouldSubmitForm ==false && inputComponent == false)
                {
                    /**
                     * http://www.w3schools.com/tags/att_button_type.asp
                     *
                     * <button type="button|submit|reset">
                     * 
                     * type = button means no submit. 
                     */
                    buffer.append(" type=\"button\"");

                    tmpNoValidate=false;
                }
                else
                {
                    buffer.append(" type=\"").append(type).append( "\"");
                }
            }
            if( tmpNoValidate)
            {
                /**
                 * can't be a button and no validate ( as it doesn't send anyway)
                 */
                buffer.append( " formnovalidate");
            }

            boolean readonly = false;
            if( disabledFg )
            {
                readonly = true;
                setTabIndex( -1);
            }

            if( tabIndex != null && tabIndex != 0)
            {
                buffer.append( " TABINDEX=");
                buffer.append( tabIndex.toString());
            }

            /**
             * readonly & disabled seem only to work with IE4 & above
             */
            if( readonly == true)
            {
                buffer.append( " READONLY");
                buffer.append( " DISABLED");
            }


            buffer.append( " id=\"");
            buffer.append( id);
            buffer.append( ext);
            buffer.append( "\"");
            buffer.append( other);
            iGenerateEvents( browser, buffer);

            if( StringUtilities.isBlank(image) == false)
            {
                // We can't have double slashes in URL's as netscape gets confused.
                if( image.startsWith("/") && image.contains("//"))
                {
                    image = StringUtilities.replace( image, "//", "/");
                }
                buffer.append(" src=\"").append(image).append( "\"");
                buffer.append(" alt=\"");
                if( toolTip != null) buffer.append( StringUtilities.encodeHTML(toolTip));
                buffer.append( "\"");
                displayName = "";
            }
            else
            {
                if( StringUtilities.isBlank(toolTip) == false)
                {
                    buffer.append( " title=\"");
                    buffer.append( StringUtilities.encodeHTML(toolTip));
                    buffer.append( "\"");
                }
            }

            if( inputComponent == false && StringUtilities.isBlank(image))
            {
                buffer.append( ">");
                if( StringUtilities.isBlank(displayName) == false )
                {
                    buffer.append( StringUtilities.encodeHTML(displayName));
                }

                buffer.append( "</button>\n");
            }
            else
            {
                buffer.append( "/>\n");
            }
            super.iGenerate(browser, buffer);
        }
    }

    private String      image,
            ext,
            href,
            displayName,
            redirectHref,
            redirectTarget,
            spawnHref,
            sqlCommand;
    private int         buttonWidth, buttonHeight;

    private int         spawnWinWidth, spawnWinHeight;

    private boolean notReplaceWindow = false;

    /**
     * This property contain button background color associate with font-formating
     */
    private Color       buttonColor;

    /**
     * This property contain button font name associate with font-formating
     */
    private String btnFontName;

    /**
     * This property contain button font style bold associate with font-formating
     */
    private Boolean btnFontBold;

    /**
     * This property contain button font size associate with font-formating
     */
    private Integer btnFontSize;

    /**
     * This property contain button font foreground color associate with font-formating
     */
    private Color btnFontColor;
    /**
     *
     */
    protected boolean disabledFg;

    /**
     * check weather it is showNavigateAwayWarning on or not
     */
    protected boolean               showNavigateAwayWarning;

    private boolean shouldSubmitForm=true;
    private boolean     placeHolderOnly;
    private ArrayList<HTMLMouseEvent> mouseEvents;
    private int fldCount;
    private ArrayList<String> pathList;
    private boolean buttGWTFg = false;
    private boolean autoSave = false;
    private String suggestId;
    private boolean editableRecord = false;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLButton");//#LOGGER-NOPMD

}
