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
package com.aspc.remote.html;

import com.aspc.developer.ThreadCop;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.html.scripts.HTMLStateEvent;
import com.aspc.remote.html.scripts.JavaScript;
import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.html.theme.HTMLMutableTheme;
import com.aspc.remote.html.theme.HTMLTheme;
import com.aspc.remote.html.theme.HTMLThemeOverlay;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.util.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  HTMLComponent
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component </i>
 *
 *  @author      Nigel Leck
 *  @since       June 26, 1999, 2:13 PM
 */
@SuppressWarnings("NestedAssignment")
public abstract class HTMLComponent
{
    /**
     * http://www.w3.org/TR/html4/types.html#type-id
     */
    public static final String VALID_NAME_REGEX="[A-Za-z][A-Za-z0-9\\-_:\\.]*";
    
    /**
     * Valid style
     */
    public static final String VALID_STYLE_REGEX="(((-?[_a-zA-Z]+[_a-zA-Z0-9\\-]* *:)[^;]*;)| )*";
    private String className;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLComponent");//#LOGGER-NOPMD
    /**
     *
     */
    protected String alt;
    private HashMap<String, String>tagAttributes;

    /**
     *
     */
    public void touch()
    {
        compiled = false;
    }

    /**
     * Clears all of the object's style names and sets it to the given style.
     *
     * @param className the class name to set. 
     */
    public final void setClassName(final @Nullable String className)
    {
        this.className=className;
    }

    /**
     * Append a class name to the existing class names ( if name).
     *
     * @param className the class name to append. 
     */
    public final void appendClassName(final @Nullable String className)
    {
        if( StringUtilities.isBlank(className)) return;

        if( StringUtilities.isBlank(this.className))
        {
            this.className=className;
        }
        else
        {
            this.className += " " + className;
        }
    }

//    /**
//     * clear the style class
//     */
//    public final void clearStyleClass()
//    {
//        className=null;
//    }

    /**
     *
     * @return the style class
     */
    @Nonnull @CheckReturnValue
    public final String getClassName()
    {
        if( StringUtilities.isBlank(className)) return "";
        return className;
    }

    /**
     *
     * @param token The token
     * @param scr
     */
    public void addJavaScript( final @Nonnull String token, final @Nullable String scr)
    {
        if( scr != null && StringUtilities.notBlank(scr))
        {
            addJavaScript( token, new JavaScript(scr.trim()));
        }
    }

    /**
     *
     * @param token The token
     * @param script
     */
    public void addJavaScript( final @Nonnull String token, final @Nullable JavaScript script)
    {
        if( tokenScripts == null)
        {
            tokenScripts = HashMapFactory.create();
        }

        if( script != null)
        {
            tokenScripts.put( token, script);
        }
    }

    /**
     *
     * @param token The token
     * @param script
     */
    public void addOnLoadScript( final @Nonnull String token, final @Nullable String script)
    {
        if( onloadScripts == null)
        {
            onloadScripts = HashMapFactory.create();
        }

        if( script != null)
        {
            onloadScripts.put( token, script.trim());
        }
    }

    /**
     *
     * @param searchId
     * @return the value
     */
    protected HTMLComponent iFindId( final String searchId)
    {
        if( id != null && id.equalsIgnoreCase(searchId))
        {
            return this;
        }

        for(
            int i = 0;
            items != null && i < items.size();
            i++
        )
        {
            HTMLComponent c;

            c = items.get(i);
            
            if( c == null ) continue;

            assert c instanceof HTMLPage == false: "can't add a page to itself";

            HTMLComponent found;

            found = c.iFindId( searchId);

            if( found != null) return found;
        }

        return null;
    }

    /**
     * get the ID of this component
     *
     * @return the ID
     */
    @CheckReturnValue
    public String getId()
    {
        return id;
    }

    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     */
    protected void iSetId( final String id)
    {
        String temp = HTMLUtilities.makeValidHTMLId(id);
//if( temp.equalsIgnoreCase("EFIELD_DBREPORT_COL_LAYOUT"))
//{
//    LOGGER.info( "A");
//}
        if( StringUtilities.isBlank( temp) == false)
        {
            assert temp.matches(VALID_NAME_REGEX): "invalid ID " + temp;
            this.id = temp;
        }
        else
        {
            this.id = null;
        }
    }

    /**
     *
     * @param inName
     */
    protected void iSetName( final @Nonnull String inName)
    {
       // assert inName.matches(VALID_NAME_REGEX): "invalid name '" + inName +"'";
        name = inName;
        name = name.replace( "=",StringUtilities.ENCODED_URL_EQUALS);

        // If you have set a name and not the ID set the ID
        // so that it can be found in Netscape
        if( id == null)
        {
            iSetId( name);
        }
    }

    /**
     *
     * @param styleId
     */
    protected void iSetStyleId( String styleId)
    {
        this.styleId = styleId;
    }

    /**
     *
     * @param c
     * @param browser
     * @param count
     * @param buffer
     */
    protected void iGenerateComponent(
        HTMLComponent c,
        ClientBrowser browser,
        int count,
        StringBuilder buffer
    )
    {
        iGenerateComponent( c, browser, buffer);
    }

    /**
     * <B>INTERNAL ONLY</B> Adds the event to the event list.
     *
     * NB: Was protected but JDK1.4 complains.
     * @param event the event
     * @param script
     */
    @SuppressWarnings("AssertWithSideEffects")
    public void iAddEvent( final HTMLEvent event, final String script)
    {
        assert ThreadCop.modify(getParentPage());
        if( event == null) return;

        if( events == null)
        {
            events = new ArrayList<>();
        }
        
        events.add( event);
        
        if( eventScripts == null)
        {
            eventScripts = HashMapFactory.create();
        }

        if( script != null && StringUtilities.notBlank(script))
        {
            eventScripts.put( script.trim(), "");
        }
    }

    /**
     *
     * @param eventName
     * @return the value
     */
    @SuppressWarnings("AssertWithSideEffects")
    protected boolean iHasEvent( final String eventName)
    {
        assert ThreadCop.read(getParentPage());
        
        if( events == null) return false;

        for( HTMLEvent ev:  events)
        {
            if( ev.getName(null).equals( eventName))
            {
                return true;
            } 
        }
        
        return false;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isCompiled()
    {
        return compiled;
    }

    @CheckReturnValue
    protected HTMLPage monitorPage()
    {
        HTMLPage page = getParentPage();
        
        return page;
    }
    
    /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser
     */
    @SuppressWarnings("AssertWithSideEffects")
    protected void compile( final ClientBrowser browser)
    {        
        HTMLPage monitorPage=monitorPage();
        
        try
        {
            assert ThreadCop.enter(monitorPage, ThreadCop.ACCESS.MODIFY);
        
            if( isCompiled() == true)
            {
                return;
            }

            compiled = true;

            if( cancelBubble)
            {
                HTMLMouseEvent me;

                if(browser.hasEventStopPropagation())
                {
                    me = new HTMLMouseEvent( HTMLMouseEvent.onClickEvent, "if(event){event.stopPropagation();}");
                    iAddEvent( me, null);
                    me = new HTMLMouseEvent( HTMLMouseEvent.onMouseDownEvent, "if(event){event.stopPropagation();}");
                    iAddEvent( me, null);
                    me = new HTMLMouseEvent( HTMLMouseEvent.onMouseUpEvent, "if(event){event.stopPropagation();}");
                    iAddEvent( me, null);
                }
                else
                {
                    me = new HTMLMouseEvent( HTMLMouseEvent.onClickEvent, "if(event){event.cancelBubble=true;}");
                    iAddEvent( me, null);
                    me = new HTMLMouseEvent( HTMLMouseEvent.onMouseDownEvent, "if(event){event.cancelBubble=true;}");
                    iAddEvent( me, null);
                    me = new HTMLMouseEvent( HTMLMouseEvent.onMouseUpEvent, "if(event){event.cancelBubble=true;}");
                    iAddEvent( me, null);                
                }
            }

            doBuildToolTip( browser);

            for(
                int i = 0;
                items != null && i < items.size();
                i++
            )
            {
                HTMLComponent c;

                c = items.get(i);

                if( c == null ) continue;

                if( c.isCompiled() == false)
                {
                    c.compile( browser);
                }
            }

            if( eventScripts != null)
            {
                HTMLPage page = getParentPage();

                if( page != null)
                {
                    for( Object key: eventScripts.keySet())
                    {
                        page.addJavaScript(key.toString());
                    }
                }
            }

          // Add scripts to page that have not already been added
            if( tokenScripts != null)
            {
                HTMLPage page = getParentPage();
                if( page == null)
                {
                    if( this instanceof HTMLPage )
                    {
                        page=(HTMLPage)this;
                    }
                    else
                    {
                        LOGGER.warn( "No Page for component :" + this);
                    }
                }
                if( page != null)
                {
                    for( Object key:  tokenScripts.keySet())
                    {
                        JavaScript value;

                        value = (JavaScript)tokenScripts.get( key);
                        if( page.getFlag( (String)key).isEmpty())
                        {
                            page.addJavaScript(value);
                            page.putFlag( (String)key, "DONE");
                        }
                    }
                }
            }

          // Add onload scripts to page that have not already been added
            if( onloadScripts != null)
            {
                HTMLPage page;
                if( this instanceof HTMLPage )
                {
                    page = (HTMLPage)this;
                }
                else
                {
                    page= getParentPage();
                }

                for( Object key: onloadScripts.keySet())
                {
                    String value;

                    value = (String)onloadScripts.get( key);

                    if( page.getFlag( (String)key).isEmpty())
                    {
                        HTMLStateEvent sEvent = new HTMLStateEvent( HTMLStateEvent.onLoadEvent, value);
                        page.addStateEvent( sEvent);
                        page.putFlag( (String)key, "DONE");
                    }
                }
            }

            // If tabindex set to 1 then set focus on this field
            // when screen opens
            boolean setInitFocus = false;
            if( tabIndex != null && tabIndex == 1)
            {
                setInitFocus = true;
            }
            if( hasInitFocus != null)
            {
                setInitFocus = hasInitFocus;
            }

            if( setInitFocus)
            {
                if( browser.isBrowserIPad() == false && browser.isBrowserMOBILE()==false)
                {
                    HTMLPage page = getParentPage();
                    if( page != null)
                    {
                        String pageFocusKey = "SET_INIT_FOCUS";
                        if( page.getFlag( pageFocusKey).isEmpty())
                        {
                            String script  = "function setInitFocus()\n" +
                                             "{\n" +
                                             "  var focusField;\n" +
                                             "  focusField = findElement( '" + id +"');\n" +
                                             "  if( focusField !== null && isStyleVisible(focusField) && focusField.type !== 'hidden' && !focusField.disabled)focusField.focus();\n" +
                                             "}\n";
                            page.addJavaScript(script);

                            HTMLStateEvent sEvent = new HTMLStateEvent( HTMLStateEvent.onLoadEvent, "setInitFocus()");
                            page.addStateEvent( sEvent);
                            page.putFlag( pageFocusKey, "DONE");
                        }
                    }
                    else
                    {
                        LOGGER.warn( "could not set focus as page is null");
                    }
                }
            }
        }
        finally
        {
            assert ThreadCop.leave(monitorPage);
        }
    }

    /**
     * This is the post compile if needed
     *
     * @param browser
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void postCompile( final ClientBrowser browser)
    {

    }

    /**
     *
     */
    protected void resetParent()
    {
        for(
            int i = 0;
            items != null && i < items.size();
            i++
        )
        {
            HTMLComponent c;

            c = items.get(i);

            if( c == null ) continue;

            c.setParent( this);
        }
    }


    /**
     *
     * @param list
     */
    @SuppressWarnings("AssertWithSideEffects")
    protected void makeListOfEvents( final List list)
    {
        assert ThreadCop.read(getParentPage());
        if( events != null)
        {
            list.addAll(events);
        }
        
        for(
            int i = 0;
            items != null && i < items.size();
            i++
        )
        {
            HTMLComponent c;

            c = items.get(i);

            if( c == null ) continue;

            if( c instanceof HTMLAnchor)
            {
                HTMLAnchor a = (HTMLAnchor)c;

                if( a.getHREF( ).contains("javascript:"))
                {
                    list.add(a);
                }
            }

            c.makeListOfEvents( list);
        }
    }

    /**
     *
     * @param alignment
     */
    public void setAlignment( final String alignment)
    {
        String tmpAlignment = alignment;
        if( tmpAlignment != null)
        {
            String vList[] = {
                "TOP",
                "BOTTOM",
                "TEXTTOP",
                "ABSMIDDLE"
            };

            for( String vAlign: vList)
            {
                if( tmpAlignment.equalsIgnoreCase( vAlign))
                {
                    assert false: "Vertical align passed to horizontal align: " + vAlign;
                    return;
                }
            }

            String list[] = {
                "LEFT",
                "RIGHT",
                "CENTER",
                "MIDDLE",
                "JUSTIFY"
            };

            boolean found = false;

            for (String list1 : list)
            {
                if (tmpAlignment.equalsIgnoreCase(list1))
                {
                    found = true;
                    break;
                }
            }

            if( found == false)
            {
                throw new RuntimeException( "Invalid Alignment '" + alignment + "'");
            }
        }

        this.alignment = tmpAlignment;
    }

    /**
     * Sets the index that defines the tab order for the Component    <br>
     *  The tabIndex value determines the tab order as follows:         <br>
     *                                                                  <br>
     *  1. Objects with  a positive tabIndex are selected in            <br>
     *     increasing index order and in source order to resolve        <br>
     *     duplicates.                                                  <br>
     *  2. Objects with a tabIndex of zero are selected in source order.<br>
     *  3. Objects with a negative tabIndex are omitted from the tabbing<br>
     *     order.
     * @param tabIndex
     */
    public void setTabIndex( int tabIndex)
    {
        this.tabIndex = tabIndex;
    }

    /**
     * Sets whether this component receives focus when the page is loaded<br>
     * If value is not set then the component with tab index of one will  <br>
     * receive the focus                                                 <br>
     * @param fg
     */
    public void setHasInitFocus( boolean fg)
    {
        hasInitFocus = fg;
    }

    /**
     * Set the parent of this component internally called by
     * addComponent
     * @param parent
     */
    protected void setParent( HTMLComponent parent)
    {
        this.parent = parent;
        myPage = null;

        HTMLPage page;
        page = getParentPage();

        if( page != null)
        {
            iAddedToPage( page);
        }
        resetParent();
    }

    /**
     *
     * @param page
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void iAddedToPage( HTMLPage page)
    {
    }

    private boolean requestingPP;
    /**
     *
     * @return the value
     */
    @CheckReturnValue
    protected HTMLPage getParentPage()
    {
        if( myPage == null)
        {
            if( parent == null)
            {
                return null;
            }

            if( parent instanceof HTMLPage)
            {
                myPage = (HTMLPage)parent;
            }
            else
            {
                try
                {
                    if( requestingPP==false)
                    {
                        requestingPP=true;
                        myPage = parent.getParentPage();
                    }
                    else
                    {
                        String msg="component extends itself";
                        assert false: msg;
                        LOGGER.warn( msg);
                    }
                }
                finally
                {
                    requestingPP=false;
                }
            }
        }

        return myPage;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public HTMLComponent getParent( )
    {
        return parent;
    }

    /**
     *
     * @param color
     * @return the value
     */
    @CheckReturnValue
    public static String makeColorID( Color color)
    {
        int c;
        c = color.getRGB() & 0xffffff;

        String  t;

        t = "000000" + Integer.toHexString(c);

        t = t.substring(t.length() - 6);

        return "#" + t;

    }

    /**
     * get the style property if set
     * @param type the property
     * @return the value NULL if not set
     */
    @CheckReturnValue
    public String fetchStyleProperty( final String type)
    {
        if( intStyleSheet == null) return null;

        return intStyleSheet.getValue(type);
    }

    /**
     * Set the internal style of this component.<br>
     * if this method is used for a component, this component will be appended a
     * class attribute.<br>
     * If this method is used, iGenerateAttributes() method must be used to generate
     * the CSS.
     * @param type the type
     * @param value the value
     */
    public void setStyleProperty( final String type, final String value)
    {
        HTMLStyleSheet ss = getStyleSheet();

        if( StringUtilities.isBlank( value))
        {
            ss.removeType(type);
        }
        else
        {
            ss.addElement(type, value);
        }
    }


    public void setAttribute( final String name, final String value)
    {
        if( tagAttributes == null)
        {
            tagAttributes=new HashMap();
        }
        tagAttributes.put(name, value);
    }
    
    /**
     *
     * @param browser
     * @param buffer
     */
    protected void iGenerateAttributesID(final ClientBrowser browser, final StringBuilder buffer)
    {
        if( id != null && id.length()>0)
        {
            buffer.append( " id=\"");
            buffer.append( id);
            buffer.append( "\"");
        }

        if( name != null)
        {
            buffer.append( " name=\"");
            buffer.append( name);
            buffer.append( "\"");
        }
    }

    /**
     *
     * @param browser
     * @param buffer
     */
    protected void iGenerateAttributes(final ClientBrowser browser, final StringBuilder buffer)
    {
        if( tagAttributes != null)
        {
            for( String attrName: tagAttributes.keySet())
            {
                String value=tagAttributes.get( attrName);
                value=value.replace("\"", "\\\"");
                buffer.append(" ").append(attrName).append("=\"").append(value).append( "\"");
            }
        }

        if( alignment != null)
        {
            buffer.append( " ALIGN=");
            buffer.append( alignment);
        }

        if( styleId != null)
        {
            buffer.append( " style=\"");
            buffer.append( styleId);
            buffer.append( "\"");
        }

        iGenerateAttributesID( browser, buffer);

        if( tabIndex != null && tabIndex != 0)
        {
            buffer.append( " tabindex=");
            buffer.append( tabIndex.toString());
        }

        if( bgColor != null)
        {
            buffer.append( " BGCOLOR=\"");
            buffer.append( makeColorID( bgColor));
            buffer.append( "\"");
        }

        if( StringUtilities.isBlank(toolTip) == false)
        {
            buffer.append( " title=\"");
            String tmpTitle=toolTip;
            tmpTitle = tmpTitle.replace( "\r\n", "\n");
            tmpTitle = tmpTitle.replace( "\r", "\n");
            buffer.append( StringUtilities.encodeHTML(tmpTitle));
            buffer.append( "\"");
        }

        if( StringUtilities.isBlank(className) == false)
        {
            buffer.append(" class=\"").append(className).append("\"");
        }

        if( StringUtilities.isBlank(alt) == false)
        {
            buffer.append(" alt=\"").append( StringUtilities.encode(alt)).append("\"");
        }

        HTMLStyleSheet ss = getStyleSheet();
        if( ss != null)
        {
            if(ss.getNumElements() > 0)
            {
                HTMLPage page = getParentPage();
                if(page != null)
                {
                    page.registerStyleSheet(ss);
                }
                if(page != null && page.isPageCompiled() && StringUtilities.isBlank(className))
                {
                    buffer.append(" class=\"").append(ss.getTarget()).append("\"");
                }
                else
                {
                    buffer.append(" ").append(ss.toInlineStyleSheet());
                }
            }
        }

        iGenerateEvents( browser, buffer);
    }

    /**
     *
     * @param browser
     * @param buffer
     */
    @SuppressWarnings({"AssertWithSideEffects", "null"})
    protected void iGenerateEvents(final ClientBrowser browser, final StringBuilder buffer)
    {
        assert ThreadCop.read(monitorPage());
        if( events != null)
        {
/*
            ArrayList<HTMLEvent> tmpEvents=(ArrayList<HTMLEvent>) events.clone();
            Collections.sort(tmpEvents, (HTMLEvent e1, HTMLEvent e2) -> {
                assert e1 != null && e2!=null: "containts null events" + tmpEvents.toString();
                
                int p1=0;
                if( e1!=null){
                    p1=e1.getPriority();
                }
                int p2=0;
                if( e2!=null){
                    p2=e2.getPriority();
                }

                return p1 - p2;
            });
            
            HashMap map = HashMapFactory.create();

            for (HTMLEvent event : tmpEvents)
            {
                assert event!=null: "event is null";
                if( event==null)continue;
                String  call,
                        key;
                String en=event.getName(browser);
                assert en!=null: "event name is null";
                String ec=event.getCall();
                assert ec!=null: "event call is null";
                
                key = en.trim().toUpperCase();
                
                call = (String)map.get(key);
                if( call == null)
                {
                    call = " " + en + "=\"" + ec.trim().replace("\"", "\\\"");
                }
                else
                {                
                    if( call.endsWith(";")==false)
                    {
                        call += ";";
                    }
                    call += ec.trim();
                }
                if( call.endsWith(";")==false)
                {
                    call += ";";
                }
                map.put( key, call);
            }

            Object calls[];
            calls = map.values().toArray();
            for (Object call : calls)
            {
                buffer.append(call);
               
                buffer.append( "\"");
            }
*/
        }
    }

    /**
     * Protected methods
     * @param browser
     */
    protected void doBuildToolTip(ClientBrowser browser)
    {
     //   String divId = "";

        if( toolTip == null || StringUtilities.isBlank(toolTip)) return;

        // Only works for ie
        if( browser.isBrowserIE() == false) return;

        HTMLPage page = getParentPage();

        HTMLMouseEvent me;
        me = new HTMLMouseEvent(
            HTMLMouseEvent.onMouseUpEvent,
            "doToolTipMouseIn( '')"
        );
        iAddEvent(me, null);


        me = new HTMLMouseEvent(
            HTMLMouseEvent.onMouseOverEvent,
            "doToolTipMouseIn(  '" + toolTip + "', event)"
        );
        iAddEvent(me, null);

        me = new HTMLMouseEvent(
            HTMLMouseEvent.onMouseOutEvent,
            "doToolTipMouseOut( )");
        iAddEvent(me, null);


        if( page.getFlag( "HTMLToolTip").isEmpty())
        {
            page.addJavaScript(
                "function doToolTipMouseOut( )\n" +
                "{\n" +
                "  window.status = '';\n" +
                "}\n\n" +
                "function doToolTipMouseIn( txt, event )\n" +
                "{\n" +
                "  var pos=0;\n" +
                "  pos = txt.indexOf('\\n');\n" +
                "  if( pos == -1) pos = txt.length;\n" +
                "  window.status = txt.substring(0,pos);\n" +
                "}\n\n"
            );
            page.putFlag( "HTMLToolTip", "DONE");
        }

    }

    /**
     *
     * @param at
     * @return the value
     */
    @CheckReturnValue @Nullable
    protected HTMLComponent iGetComponent( int at)
    {
        if( items == null || at >= items.size())
        {
            return null;
        }
        HTMLComponent component;

        component = items.get(at);

        return component;
    }

    /**
    * Just Create the items vector if needed
    */
    protected void checkIsContainer()
    {
        if( items == null)
        {
            items = new ArrayList();//NOPMD
        }
    }

    /**
     * Adds a component to this container
     * @param component
     */
    protected void iAddComponent( final @Nullable HTMLComponent component)
    {
        checkIsContainer();
        if( component != null)
        {
            component.setParent( this);
            items.add(component);
        }
    }

    /**
     * Adds a component to this container at a particular position
     * @param component
     * @param index
     * @throws java.lang.ArrayIndexOutOfBoundsException
     */
    protected void iAddComponent( HTMLComponent component,
                                  int index)
                                  throws ArrayIndexOutOfBoundsException
    {
        checkIsContainer();
        component.setParent( this);
        items.add(index, component);
    }

    /**
     * Remove component from the container
     * @param at
     */
    protected void iRemoveComponent( int at )
    {
        if( items == null) return;

        if( iGetComponentCount() > at)
        {
            HTMLComponent component = items.get( at );
            component.setParent( null );
            items.remove( at );
        }
    }

    /**
     * The count of components contained
     * @return the value
     */
    @CheckReturnValue
    protected int iGetComponentCount()
    {
        if( items == null) return 0;

        return items.size();
    }

    /**
     *
     */
    protected void iClear()
    {
        if( items != null)
        {
            items.clear();
        }
    }

    /**
     * This is the method that creates the raw HTML.
     * DO NOT CREATE ANY HTMLComponents IN HERE. Do it
     * in the compile method.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    protected void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        if( items == null)
        {
            return;
        }

        for( int i = 0; i < items.size(); i++)
        {
            HTMLComponent c;

            c = items.get(i);

            iGenerateComponent(c, browser, i, buffer);
        }
    }

    /**
     * Test to see if this component has a Theme ( without creating one)
     * @return the value
     */
    @CheckReturnValue
    public boolean hasTheme()
    {
        if( parent == null)
        {
            return true;
        }

        return gMutableTheme != null && gMutableTheme.hasChanges();
    }

    /**
     * Returns a mutable theme
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public HTMLMutableTheme getMutableTheme()
    {
        if( gMutableTheme != null)
        {
            return gMutableTheme;
        }

        gMutableTheme = new HTMLThemeOverlay( this);

        return gMutableTheme;
    }

    /**
     * Returns the current theme or an Overlay theme if one doesn't exists.
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public HTMLTheme getTheme()
    {
        HTMLTheme tmpTheme=gMutableTheme;

        if( tmpTheme==null)
        {
            if( parent == null)
            {
                gMutableTheme = new HTMLThemeOverlay( this);
                return gMutableTheme;
            }
            else
            {
                HTMLComponent tmpParent = parent;

                while( tmpParent != null)
                {
                    if( tmpParent.hasTheme())
                    {
                        tmpTheme= tmpParent.getTheme();
                        break;
                    }

                    tmpParent = tmpParent.getParent();
                }
            }
        }
        
        assert tmpTheme!=null: "should have returned a theme";
        return tmpTheme;
    }

    /**
     *
     * @param c
     * @param browser
     * @param buffer
     */
    protected void iGenerateComponent(
        HTMLComponent c,
        ClientBrowser browser,
        StringBuilder buffer
    )
    {
        c.iGenerate(browser, buffer);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    protected HTMLStyleSheet getStyleSheet()
    {
        if( intStyleSheet == null)
        {
            intStyleSheet = new HTMLStyleSheet( true);
        }
        return intStyleSheet;
    }

    /**
     *
     * @param table
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "AssertWithSideEffects"})
    protected void copyAttributes( final HTMLComponent table)
    {
        assert ThreadCop.read(table.getParentPage());
        assert ThreadCop.modify(getParentPage());
        alignment = table.alignment;
        id = table.id;
        name = table.name;
        styleId = table.styleId;
        toolTip = table.toolTip;
        tabIndex = table.tabIndex;
        gMutableTheme = table.gMutableTheme;
        ArrayList tmpEvents=table.events;
        if( tmpEvents!=null && tmpEvents.isEmpty()==false)
        {
            events=(ArrayList)tmpEvents.clone();
        }
        else
        {
            events = null;
        }
        bgColor = table.bgColor;
        styleProperty = table.styleProperty;
        myPage = table.myPage;
        eventScripts = table.eventScripts;
        tokenScripts = table.tokenScripts;
        onloadScripts = table.onloadScripts;
    }

    /**
     *
     */
    protected   HTMLComponent parent;

    /**
     *
     */
    protected   String  alignment,
                        id,
                        name,
                        styleId,
                        toolTip;

    /**
     *
     */
    protected   Integer tabIndex;
    /**
     *
     */
    protected   Boolean hasInitFocus;

    /**
     *
     */
    protected   HTMLMutableTheme gMutableTheme;

    /**
     *
     */
    protected   ArrayList<HTMLEvent>      events;
    
    /**
     *
     */
    protected   Color       bgColor;

    /**
     *
     */
    protected   ArrayList<HTMLComponent>      items;

    /**
     *
     */
    protected   boolean     cancelBubble;
    /**
     *
     */
    protected   HashMap     styleProperty;

    private     HTMLPage    myPage;

    private     HashMap   eventScripts,
                            tokenScripts,
                            onloadScripts;

    private     boolean     compiled;

    /**
     *
     */
    protected HTMLStyleSheet intStyleSheet;
/*
    static
    {
        boolean flag=false;
        assert flag=true;
        ASSERT=flag;
    }*/
}
