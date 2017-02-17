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
import static com.aspc.remote.html.HTMLComponent.VALID_NAME_REGEX;
import com.aspc.remote.html.scripts.HTMLStateEvent;
import com.aspc.remote.html.scripts.JavaScript;
import com.aspc.remote.html.scripts.ScriptLink;
import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.html.theme.HTMLMutableTheme;
import com.aspc.remote.html.theme.HTMLTheme;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.rest.Status;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.*;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *  HTMLPage
 *
 *  <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       June 12, 1999, 3:54 PM
 */
public class HTMLPage extends HTMLPanel
{
    /**
     * Content is clipped and content outside of the element's box is not visible. The size of the clipping region is defined by the 'clip' property.
     */
    public static final String SCROLLBAR_HIDDEN="hidden";

    /**
     * Content is not clipped and may be rendered outside of the element's box.
     */
    public static final String SCROLLBAR_VISIBLE="visible";

    /**
     * Content is clipped as necessary, but a horizontal scrollbar is made available where necessary to view the additional, non-visible content.
     * If the Visual media in use is static (such as Print) the content should be treated as if the value was 'visible'.
     */
    public static final String SCROLLBAR_SCROLL="scroll";

    /**
     *      This value is browser and media dependent, but should allow for a horizontal scrollbar if possible in case of overflow.
     */
    public static final String SCROLLBAR_AUTO="auto";

    /**
     * The page version
     */
  //  public static final Version VERSION = new Version("PAGE_VERSION", 2, 1, 2);
  
    private final int version;
    private String      title;
    private String      keywords;
    private String      description;
    private String pageClass;
    
    /** Open graph protocol **/
    private String ogURL;
    private String ogImage;
    private String ogTitle;
    private String ogType;
    private String ogDescription;
    private String ogSiteName;
    
    private String XUACompatible;
    
    private String googleplusid;
    
    private static final String SUPER_DEV_MODULE;
    private static final String SUPER_DEV_SERVER;
    private static final String ENV_SUPER_DEV_MODULE="SUPER_DEV_MODULE";
    private static final String ENV_SUPER_DEV_SERVER="SUPER_DEV_SERVER";
    private Status status = Status.C200_SUCCESS_OK;
    
    private ArrayList<String>heads;
    private LinkedHashMap<String, String>metaList;
    
    /**
     * Constructor
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public HTMLPage()
    {
        this( 1);
    }
    
    /**
     * 
     * @param version 1 for old page mode
     */
    public HTMLPage(final int version)
    {
        this.version=version;
        refreshTime = -1;
        pleaseWaitHook = false;
        if( version <2)
        {
            bgColor = Color.white;
            gMutableTheme = new HTMLMutableTheme();
        }
    }
    
    public void setPageClass(final String pageClass)
    {
        this.pageClass=pageClass;
    }
     
    public String getPageClass()
    {
        return pageClass;
    }
    
    public HTMLPage setStatus(final @Nonnull Status status)
    {
        if(status == null)
        {
            throw new IllegalArgumentException("status is mandatory");
        }
        this.status = status;
        return this;
    }
    
    public @CheckReturnValue @Nonnull Status getStatus()
    {
        return status;
    }
    
    @Override
    protected HTMLPage monitorPage()
    {
        return this;
    }
    
    /**
     * 
     * @return is t
     */
    public int getVersion()
    {
        return version;
    }
    
    /**
    * add a module.
     * @param module the module to add. 
     * @return This page. 
    */
    public HTMLPage addModule( final String module)
    {
        throw new IllegalArgumentException("unkown module " + module);
    }
    
    @Override
    public void addComponent( final HTMLComponent component,
                              final int index)
                              throws ArrayIndexOutOfBoundsException
    {
        assert component instanceof HTMLPage == false: "can not add page to another page";
        
        super.addComponent( component, index);
    }
  
    /**
     *
     * @param url the script URL
     * @param loadType the type of loading.
     */
    public void addJavaScriptLink( final @Nullable String url, final @Nonnull ScriptLink.LoadType loadType)
    {
        addJavaScriptLink(url, loadType, null);
    }   
    
    /**
     *
     * @param url the script URL
     * @param loadType the type of loading.
     * @param cdnFallBackScript CDN fall back script. 
     */
    public void addJavaScriptLink( 
        final @Nullable String url, 
        final @Nonnull ScriptLink.LoadType loadType,
        final @Nullable String cdnFallBackScript
    )
    {
        if( StringUtilities.isBlank(url)) return;
        assert url!=null;
        assert StringUtilities.URI_PATTERN.matcher(url).matches(): "Invalid URL " + url;
        
        boolean found = false;

        if( scripts != null)
        {
            for( JavaScript js : scripts)
            {
                if( js instanceof ScriptLink)
                {
                    ScriptLink sl = (ScriptLink)js;

                    if( sl.url.equals( url))
                    {
                        if( sl.cdnFallBackScript !=null && cdnFallBackScript!=null )
                        {
                            if( sl.cdnFallBackScript.equals(cdnFallBackScript))
                            {
                                found = true;
                                break;
                            }
                        }
                        else if( sl.cdnFallBackScript ==null && cdnFallBackScript ==null )
                        {
                            found=true;
                            
                            break;
                        }
                    }
                }
            }
            
            if( url.startsWith("/ds/"))
            {
                int modulePos=url.indexOf('/', 5);
                if( modulePos != -1)
                {
                    int versionPos=url.indexOf('/', modulePos + 1);
                    
                    if( versionPos != -1)
                    {
                        String tmpVersion=url.substring(modulePos + 1, versionPos);

                        double versionValue=makeVersionValue( url );
                                                
                        String match=url.substring(0, modulePos + 1);
                        JavaScript removeJS=null;
                        
                        int end = url.indexOf('?');
                        if( end == -1) end=url.length();
                        String path = url.substring(versionPos + 1, end);
                        
                        for( JavaScript js : scripts)
                        {
                            if( js instanceof ScriptLink)
                            {
                                ScriptLink sl = (ScriptLink)js;

                                if( sl.url.startsWith(match))
                                {
                                    int matchVersionPos=sl.url.indexOf('/', modulePos + 1);

                                    if( matchVersionPos != -1)
                                    {
                                         
                                        int matchVersionEnd = sl.url.indexOf('?');
                                        if( matchVersionEnd == -1) matchVersionEnd=sl.url.length();
                                        String matchVersionPath = sl.url.substring(matchVersionPos + 1, matchVersionEnd);
                                        if( path.equals(matchVersionPath)==false)
                                        {
                                            continue;
                                        }
                                        
                                        String matchVersion=sl.url.substring(modulePos + 1, matchVersionPos);

                                        if( tmpVersion.equalsIgnoreCase("default"))
                                        {
                                            found=true;
                                        }
                                        else
                                        {
                                            if( matchVersion.equalsIgnoreCase("default"))
                                            {
                                                removeJS=js;
                                            }
                                            else
                                            {
                                                double matchVersionValue=makeVersionValue(sl.url);

                                                if( versionValue<matchVersionValue)
                                                {
                                                    removeJS=js;
                                                }
                                                else
                                                {
                                                    found=true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if( removeJS != null)
                        {
                            scripts.remove(removeJS);
                        }
                    }
                }
            }
        }
        
        if( found == false)
        {
            addJavaScript(new ScriptLink(url, loadType, cdnFallBackScript));
        }
    }
        
    private double makeVersionValue( final String URL)
    {
        String tmpVersion=null;
        int modulePos=URL.indexOf('/', 5);
        if( modulePos != -1)
        {
            int versionPos=URL.indexOf('/', modulePos + 1);

            if( versionPos != -1)
            {
                tmpVersion=URL.substring(modulePos + 1, versionPos);
            }
        }

        if( tmpVersion!=null && tmpVersion.equalsIgnoreCase("default")==false)
        {
            int dotPos=tmpVersion.indexOf('.');
            if( dotPos != -1)
            {
                String working=tmpVersion.substring(0, dotPos);
                working +=".";
                
                String tmp=tmpVersion.substring(dotPos+1);//.replace(".", "");
                int pos=tmp.indexOf('%');
                if( pos != -1)
                {
                    tmp=tmp.substring(0, pos);
                }
                pos=tmp.indexOf('+');
                if( pos != -1)
                {
                    tmp=tmp.substring(0, pos);
                }
                pos=tmp.indexOf('-');
                if( pos != -1)
                {
                    tmp=tmp.substring(0, pos);
                }
                pos=tmp.indexOf('.');
                if( pos != -1)
                {
                    if( pos < 2)
                    {
                        working+="0";
                    }
                    working+=tmp.substring(0,pos);
                    tmp=tmp.substring(pos+1);

//                    pos=tmp.indexOf(".");
//                    if( pos != -1)
//                    {
                        if( tmp.length() < 2)
                        {
                            working+="0";
                        }
                        working+=tmp;//.substring(0,pos);
//                    }
                }
                else
                {
                    working+=tmp;
                }
                
                tmpVersion=working;
            }
            try
            {
                return Double.parseDouble(tmpVersion);
            }
            catch( NumberFormatException nfe)
            {
                LOGGER.warn( "could not parse '" + URL + "'", nfe);
            }
        }
        
        return -1;
    }
    /**
     *
     * @param flag
     */
    public void setLinkExternalScripts( final boolean flag)
    {
        if( flag)
        {
            linkExternalScripts = "YES";
        }
        else
        {
            linkExternalScripts = "NO";
        }
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
     */
    public void setId( final String id)
    {
        if(version > 1)
        {
            if( StringUtilities.isBlank( id) == false)
            {
                assert id.matches(VALID_NAME_REGEX): "invalid ID " + id;
                this.id = id;
            }
            else
            {
                this.id = null;
            }
        }
        else
        {
            iSetId(id);
        }
    }

    /**
     *
     * @param flag
     */
    public void setShowNavigateAwayWarning( final boolean flag)
    {
        showNavigateAwayWarning = flag;
    }

    /**
     *
     * @return the value
     */
    public boolean showNavigateAwayWarning()
    {
        return showNavigateAwayWarning;
    }

    /**
     *
     * @param id
     */
    public void setTopMargin( final String id)
    {
        topmargin = id;
    }

    /**
     *
     * @param margin
     */
    public void setBottomMargin( final String margin)
    {
        bottomMargin = margin;
    }

    /**
     *
     * @param id
     */
    public void setLeftMargin( final String id)
    {
        leftmargin = id;
    }


    /**
     *
     * @param id
     */
    public void setRightMargin( final String id)
    {
        rightmargin = id;
    }

    /**
     *
     * @param id
     */
    public void setMarginHeight( final String id)
    {
        marginheight = id;
    }

    /**
     *
     * @param id
     */
    public void setMarginWidth( final String id)
    {
        marginwidth = id;
    }

    /**
     *
     * @param index
     * @return the value
     */
    public HTMLStyleSheet getStyle(final int index)
    {
        if(styles.size() > index)
        {
            return (HTMLStyleSheet)styles.get(index);
        }
        else
        {
            return null;
        }
    }

    /**
     *
     * @param s the style sheet
     */
    public void registerStyleSheet( final HTMLStyleSheet s)
    {
        if( styles == null)
        {
            styles = new ArrayList();
        }

        s.lock();

        int pos = styles.indexOf(s);
        if( pos == -1)
        {
            styles.add(s);
            s.setPageUniqueCount( styles.size());
        }
        else
        {
            HTMLStyleSheet copy = (HTMLStyleSheet)styles.get(pos);
            if( copy != s)
            {
                s.setPageUniqueCount(copy.getPageUniqueCount());
            }
        }
    }

    /**
     *
     *
     * @param comp
     */
    public void addHeadComponent( final HTMLComponent comp)
    {
        if( headComps == null)
        {
            headComps = new ArrayList();
        }

        headComps.add( comp);
    }

    /**
     * append a style sheet
     * @param url append a style sheet
     */
    public void addStyleSheet( final String url)
    {
        addStyleSheet(url, null, -1);
    }

    /**
     * append a style sheet
     * @param url append a style sheet
     * @param media the media 
     */
    public void addStyleSheet( final String url, final String media)
    {
        addStyleSheet(url, media, -1);
    }
    
    public void addLink( final @Nonnull String rel, final @Nonnull String href )
    {
        String tag="<link rel=\"" + rel + "\" href=\"" + href + "\">";
        
        addLink(tag, -1);
    }
    /**
     * 
     * @param tag the attributes in this tag must be html encoded, such as &lt;script src=&quot;/abc.js?a=b<b>&amp;amp;</b>c=d&quot;/&gt;
     * @param index 
     */
    public void addLink(final String tag, final int index)
    {
        if( tag==null) throw new IllegalArgumentException( "link tag must not be null");
        
        if(links == null)
        {
            links = new ArrayList<>();
        }
        int tmpIndex=index;
        if( tmpIndex < 0 || tmpIndex>links.size())
        {
            tmpIndex=links.size();
        }

        links.add(tmpIndex, tag);
    }
    
    /**
     * prepend a style sheet
     * @param url append a style
     * @param media the media for this style sheet
     * @param index the index
     */
    public void addStyleSheet( final @Nonnull String url, final @Nullable String media, final int index)
    {
        if( url==null) throw new IllegalArgumentException( "URL must not be null");
        if( StringUtilities.URI_PATTERN.matcher(url).find()==false) 
        {
            throw new IllegalArgumentException( url + " is invalid");
        }
        
        if( styleSheets == null)
        {
            styleSheets = new ArrayList<>();
        }
        
        String urlAndMedia;
        if( url.contains("&"))
        {
            if( url.toLowerCase().contains("&amp;"))
            {
                urlAndMedia=url;
            }
            else
            {
                urlAndMedia=url.replace("&", "&amp;");
            }
        }
        else
        {
            urlAndMedia=url;
        }
        if( StringUtilities.notBlank(media))
        {
            urlAndMedia+="\t" + media;
        }
        styleSheets.remove( urlAndMedia);
        int tmpIndex=index;
        if( tmpIndex < 0 || tmpIndex>styleSheets.size())
        {
            tmpIndex=styleSheets.size();
        }

        styleSheets.add(tmpIndex, urlAndMedia);
    }

    /**
     *
     * @param script
     */
    public void addJavaScript( final @Nonnull JavaScript script)
    {
        assert script!=null;
        if( scripts == null)
        {
            scripts = new ArrayList<>();
        }

        scripts.add(script);
    }

    /**
     *
     * @return the value
     */
    public String[] listScripts()
    {
        if( scripts == null) return new String[0];
        int size;
        size = scripts.size();

        String list[] = new String[size];
        for( int i=0;i < size; i++)
        {
            list[i] = scripts.get( i).toString();
        }

        return list;
    }

    /**
     * the CSS links
     * @return the list of CSS links
     */
    public String[] listCSS()
    {
        if( styleSheets == null) return new String[0];
        int size;
        size = styleSheets.size();

        int i=0;
        String list[] = new String[size];
        for( String urlAndMedia: styleSheets)
        {
            String[] split = urlAndMedia.split("\t");
            list[i] = split[0];
            i++;
        }

        return list;
    }

    /**
     * HTMLArea needs a variable to be set up before loading the java scripts
     * @param scriptVariable
     */
    public void addJavaScriptVariable( final String scriptVariable)
    {
        if( scriptVariables == null)
        {
            scriptVariables = new ArrayList();
        }

        scriptVariables.add(scriptVariable);
    }

    /**
     *
     * @param toolTip
     */
    public void setToolTip( final String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param sEvent
     */
    public void addStateEvent( final HTMLStateEvent sEvent)
    {
        super.iAddEvent(sEvent, "");
    }

    /**
     *
     * @param scriptListing
     */
    public void addJavaScript( final @Nonnull String scriptListing)
    {
        addJavaScript( new JavaScript( scriptListing));
    }

    /**
     * add a GWT module. If any modules added then include a call for /scripts/gwt.js into the page.
     * @param module the module to add.
     */
    public void addGWT( final String module)
    {
        if( gwtModules == null)
        {
            gwtModules = new ConcurrentHashMap();
            HTMLStateEvent se = new HTMLStateEvent(HTMLStateEvent.onUnloadEvent, "if( window.gwtUnload) window.gwtUnload()");
            addStateEvent(se);
        }

        String tempModule=module;
        if( module.startsWith( "/") == false)
        {
            tempModule = "/" + module + "/" + module;
        }

        gwtModules.put(tempModule, "");
    }

    public void addMeta( final @Nonnull String name,final @Nonnull String content )
    {
        if( metaList ==null)
        {
            metaList=new LinkedHashMap<>();
        }
        
        metaList.put(name, content);
    }
    /**
     *
     * @param headerTag
     */
    public void addHeadTag( final @Nonnull String headerTag)
    {
        if( headerTag==null) throw new IllegalArgumentException("header tag is mandatory");
        String tmpTag=headerTag.trim();
        
        if( tmpTag.isEmpty()) return;
        
        if( tmpTag.toLowerCase().startsWith("<link") && tmpTag.endsWith(">"))
        {
            tmpTag=tmpTag.replace("\"", "'");
            if( tmpTag.toLowerCase().contains("rel='shortcut icon'"))
            {
                int pos=headerTag.indexOf("href=");

                if( pos!=-1)
                {
                    String tmpHREF=tmpTag.substring(pos);
                    pos=tmpHREF.indexOf("'");
                    if( pos!=-1)
                    {
                        tmpHREF = tmpHREF.substring(pos + 1);
                        pos=tmpHREF.indexOf("'");
                        if( pos!=-1)
                        {
                            tmpHREF = tmpHREF.substring(0,pos);
                            setFavIconPath(tmpHREF);
                            return;
                        }
                    }
                }
            }
        }
        else if( tmpTag.toLowerCase().startsWith("<meta") && tmpTag.endsWith(">"))
        {
            int namePos=headerTag.indexOf(" name=");
            if( namePos!=-1)
            {
                char quote='"';
                int quotePos = headerTag.indexOf(quote, namePos);
                int singleQuotePos = headerTag.indexOf('\'', namePos);
                if( singleQuotePos!=-1 && singleQuotePos<quotePos || quotePos==-1)
                {
                    quote='\'';
                    quotePos=singleQuotePos;
                }

                if( quotePos!=-1)
                {
                    int endPos = headerTag.indexOf(quote, quotePos + 1);
                    if( endPos != -1)
                    {
                        String nameValue=headerTag.substring(quotePos + 1,endPos);

                        int contentPos=headerTag.indexOf(" content=");

                        if( contentPos!=-1)
                        {
                            quote='"';
                            quotePos = headerTag.indexOf(quote, contentPos);
                            singleQuotePos = headerTag.indexOf('\'', contentPos);
                            if( singleQuotePos!=-1 && singleQuotePos<quotePos|| quotePos==-1)
                            {
                                quote='\'';
                                quotePos=singleQuotePos;
                            }

                            if( quotePos!=-1)
                            {
                                endPos = headerTag.indexOf(quote, quotePos + 1);
                                if( endPos != -1)
                                {
                                    String contentValue=headerTag.substring(quotePos + 1,endPos);
                                    addMeta(nameValue, contentValue);
                                    return;
                                }
                                else
                                {
                                    assert false: "malformed meta tag( missing end " + quote + "): " + headerTag;
                                }
                            }
                            else
                            {
                                assert false: "malformed meta tag( missing end " + quote + "): " + headerTag;
                            }
                        }
                        else
                        {
                            assert false: "malformed meta tag( missing 'content='): " + headerTag;
                        }
                    }
                    else
                    {
                        assert false: "malformed meta tag( missing start " + quote + "): " + headerTag;
                    }
                }
                else
                {
                    assert false: "malformed meta tag( missing end " + quote + "): " + headerTag;
                }
            }
//            else
//            {
//                assert false: "malformed meta tag( missing 'name='): " + headerTag;
//            }
        }
        if( heads == null)
        {
            heads = new ArrayList();
        }

        heads.add(headerTag);
    }

    /**
     *
     * @param heads
     */
    public void addPreRefreshCall( final String heads)
    {
        if( preRefreshCalls == null)
        {
            preRefreshCalls = new ArrayList();
        }

        preRefreshCalls.add(heads);
    }

    /**
     *
     * @param browser
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        isCompiled = true;
        if( showNavigateAwayWarning &&
            browser != null &&
            browser.isBrowserHTTPUnit() == false)
        {
            addJavaScript( "window.onbeforeunload = unloadMessage;\n");
        }

        if( version < 2)
        {
            HTMLTheme theme = getTheme();
            HTMLStyleSheet ss;

            ss = new HTMLStyleSheet("A:active");

            ss.setColour(
                "color",
                theme.getDefaultColor(HTMLTheme.DKEY_LINK_ACTIVE_COLOR)
            );

            registerStyleSheet(ss);

            ss = new HTMLStyleSheet("A:link");

            ss.setColour(
                "color",
                theme.getDefaultColor(HTMLTheme.DKEY_LINK_COLOR)
            );

            if( theme.getDefaultBoolean(HTMLTheme.DKEY_LINK_BOLD))
            {
                ss.addElement(
                    "font-weight",
                    "bold"
                );
            }

            registerStyleSheet(ss);

            ss = new HTMLStyleSheet("A:visited");

            ss.setColour(
                "color",
                theme.getDefaultColor(HTMLTheme.DKEY_LINK_VISITED_COLOR)
            );

            registerStyleSheet(ss);

            ss = new HTMLStyleSheet("A:hover");

            ss.setColour(
                "color",
                theme.getDefaultColor(HTMLTheme.DKEY_LINK_ACTIVE_COLOR)
            );

            registerStyleSheet(ss);
        }

        super.compile(browser);
    }

    public String getXUACompatible()
    {
        return XUACompatible;
    }

    public void setXUACompatible(final String XUACompatible)
    {
        this.XUACompatible = XUACompatible;
    }

    /**
     *
     * @return the value
     */
    public String getTitle()
    {
        return title == null ? "" : title;
    }

    /**
     * set the title
     * @param title the title
     */
    public void setTitle( final String title)
    {
        if( "Untitled".equalsIgnoreCase(title))
        {
            LOGGER.info(
                "*** Warning: Netscape doesn't show Page title's of '" + title + "'"
            );
        }
        this.title =  title;
    }

    /**
     * set the description
     * @param description the description
     */
    public void setDescription( final String description)
    {
        this.description =  description;
    }

    /**
     * set the keywords
     * @param keywords the keywords
     */
    public void setKeywords( final String keywords)
    {
        this.keywords =  keywords;
    }

    /**
     *
     * @param color
     */
    public void setBackGroundColor( final Color color)
    {
        bgColor = color;
    }

    /**
     *
     * @param secs
     */
    public void setRefreshTime( final int secs)
    {
        refreshTime = secs;
    }
    
    public int getRefreshTime()
    {
        return refreshTime;
    }

    /**
     *
     * @param imageSrc
     */
    public void setBackgroundImage( final String imageSrc)
    {
        setBackgroundImage( imageSrc, false);
    }
    /**
     *
     * @param imageSrc
     */
    public void setBackgroundImageFullPage( final String imageSrc)
    {
        setBackgroundImage( imageSrc, false);
        bgImageFullPage=true;
    }

    /**
     *
     * @param imageSrc
     * @param fixedPosition
     */
    public void setBackgroundImage(
        final String imageSrc,
        final boolean fixedPosition
    )
    {
        bgImage = imageSrc;

        bgImageFixedPosition = fixedPosition;
    }

    @CheckReturnValue @Nullable
    public String getHost( )
    {
        return host;
    }

    /**
     * add this host to the css links if it's not blank
     * this host must include protocol, or starts with '//'
     * 
     * @param host 
     */
    public void setHost(final @Nonnull String host)
    {
        assert StringUtilities.notBlank(host) && (host.startsWith("//") || host.contains("://")) : "please add protocol to the host: " + host;
        this.host = host;
    }

    /**
     * generate the raw WML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate WML
     */
    public void iGenerateSuper(final ClientBrowser browser, final StringBuilder buffer) // this method will work for WML code
    {
        super.iGenerate(browser,buffer);
    }

    /**
     *
     * @param buffer
     */
    private void pleaseWaitHook(final StringBuilder buffer)
    {
        // Note: Tried setting to 110% as IE is leaving edge on right hand side of screen but made iframe reports wider then they needed to be
        buffer.append("<div id=\"CURTAIN\" onkeydown = \"cancelEvent();\" class=\"aspc-GlassPanel\" style='top: 0px;width: 100%;height: 100%;left: 0px;position: fixed;z-index: 100;");

        buffer.append("'>\n");

        buffer.append("<table style=\"border:0; background:#fafafa;width:100%;height:100%\">");
        buffer.append("<tr><td >");
        buffer.append("<div id=\"PLEASEWAIT\" style='text-align:center; position: absolute;top:50%;left:50%;font-size: 14px;font-family: Arial;font-weight:bold;\'>Please&nbsp;wait&nbsp;loading...</div>");
        buffer.append("</td></tr></table></div>\n");

        buffer.append(
            "<script type=\"text/javascript\">\n" +
            "try{\n" +
            "if( showLayerCount !== undefined && showLayerCount !== null)\n" +
            "{\n" +
            "  showLayerCount = showLayerCount + 1;\n" +
            "}\ncentrePleaseWait();}catch( e) {;}\n"+
            "</script>\n"
        );
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        if( hasFrameSet == true)
        {
            buffer.append(
                "<!doctype html>\n"
            );
        }
        else
        {
            buffer.append(
                "<!DOCTYPE html>\n"
            );
        }

        buffer.append(
            "<html lang='en'"
        );
        String tmpClass=getPageClass();
        
        if( StringUtilities.notBlank(tmpClass))
        {
            buffer.append(" class=\"").append(tmpClass).append("\"");
        }

        // Header section
        buffer.append(
             ">\n<head>\n"
        );

        if(StringUtilities.notBlank(XUACompatible))
        {
            buffer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"").append(XUACompatible).append("\" />\n");
        }
        else
        {
            if( version < 2)
            {
                if( browser.isBrowserIE())
                {
                    /* work around fieldset issue in IE8, it seems to work well in IE7 */
                    if( browser.getBrowserVersion() >= 9)
                    {
                        /*
                        * http://msdn.microsoft.com/en-us/library/cc288325(v=vs.85).aspx
                        */
                        buffer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=100\" />\n");
                    }
                    else if(browser.getBrowserVersion() < 9 && browser.getBrowserVersion() >= 7)
                    {
                        buffer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=7\" />\n");
                    }
                }
            }
        }
        
        buffer.append(
            "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n"
        );

        if( refreshTime != -1)
        {
            buffer.append("<meta http-equiv=\"Refresh\" content='").append(refreshTime).append("'/>\n");
        }
        
        /** Setting meta for the open graph object **/
        if (StringUtilities.notBlank(ogTitle))
        {
            makeMeta( buffer, "property", "og:title", ogTitle);

        //    buffer.append("<meta property=\"og:title\" content=\"").append(StringUtilities.encodeHTML(ogTitle)).append("\"/>\n");
        }
        if (StringUtilities.notBlank(ogDescription))
        {
            makeMeta( buffer, "property", "og:description", ogDescription);
           // buffer.append("<meta property=\"og:description\" content=\"").append(StringUtilities.encodeHTML(ogDescription)).append("\"/>\n");
        }
        if (StringUtilities.notBlank(ogImage))
        {
            makeMeta( buffer, "property", "og:image", ogImage);
            //buffer.append("<meta property=\"og:image\" content=\"").append(StringUtilities.encodeHTML(ogImage)).append("\"/>\n");
        }
        if (StringUtilities.notBlank(ogURL))
        {
            makeMeta( buffer, "property", "og:url", ogURL);

            //buffer.append("<meta property=\"og:url\" content=\"").append(StringUtilities.encodeHTML(ogURL)).append("\"/>\n");
        }
        if (StringUtilities.notBlank(ogType))
        {
            makeMeta( buffer, "property", "og:type", ogType);
            //buffer.append("<meta property=\"og:type\" content=\"").append(StringUtilities.encodeHTML(ogType)).append("\"/>\n");
        }
        
        if (StringUtilities.notBlank(ogSiteName))
        {
            makeMeta( buffer, "property", "og:site_name", ogSiteName);
            //buffer.append("<meta property=\"og:site_name\" content=\"").append(StringUtilities.encodeHTML(ogSiteName)).append("\"/>\n");
        }
        
        if(StringUtilities.notBlank(googleplusid))
        {
            buffer.append("<link rel=\"author\" href=\"https://plus.google.com/").append(googleplusid).append("/posts\"/>\n");
        }
        
        if( title != null)
        {
            if (browser.isBrowserMOBILE())
            {
                //buffer.append("<meta name=\"page_title_for_mobile\" content=\"").append(title).append("\">\n");
                //makeMeta( buffer, "property", "og:site_name", ogSiteName);
                if (mobileProperties != null)
                {
                    String props[] = new String[ mobileProperties.size()];
                    mobileProperties.keySet().toArray(props);
                    for (String propKey : props)
                    {
                        String value = (String)mobileProperties.get(propKey);

                        buffer.append("<meta property_key=\"");
                        buffer.append(propKey);
                        buffer.append("\"");
                        buffer.append(" property_value=\"");
                        buffer.append(StringUtilities.encodeHTML(value));
                        buffer.append("\">\n");
                    }
                }
            }

            // encodeHTML prevents JavaScript Injection in the title
            buffer.append("<title>").append(StringUtilities.encodeHTML(title)).append(
                "</title>\n");
        }
        else
        {
            // it's needed to pass the w3c validator
            buffer.append( "<title></title>\n");
        }

        if( StringUtilities.notBlank( keywords))
        {
            makeMeta( buffer,"name","keywords", keywords);
            
           // buffer.append("<meta name=\"keywords\" content=\"").append(StringUtilities.encodeHTML(keywords)).append("\">\n");
        }

        if( StringUtilities.notBlank( description))
        {
            makeMeta(buffer,  "name", "description", description);
            //buffer.append("<meta name=\"description\" content=\"").append(StringUtilities.encodeHTML(description)).append("\">\n");
        }

        boolean viewportFound=false;
        if( metaList!=null)
        {
            for( String metaName: metaList.keySet())
            {
                if( metaName.equalsIgnoreCase("viewport"))
                {
                    viewportFound=true;
                }
                
                String metaContent=metaList.get(metaName);
                buffer.append("<meta name=\"").append(metaName).append("\" content=\"").append(metaContent).append("\"/>\n");
            }
        }
        if( heads != null)
        {
            for( String head: heads)
            {
                if( head.contains("viewport"))
                {
                    viewportFound=true;
                }
                buffer.append( head);
                buffer.append( "\n");
            }
        }

        if( viewportFound==false)
        {
            if( browser.isBrowserIPhone())
            {
                buffer.append("<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0\"/>\n");
            }
        }
        
        if( styleSheets != null)
        {
            boolean hasHost = StringUtilities.notBlank(host);
            String tempHost = host;
            if(hasHost)
            {
                while(tempHost.endsWith("/"))
                {
                    tempHost = tempHost.substring(0, tempHost.length() - 1);
                }
            }
            for( String urlAndMedia: styleSheets)
            {
                String split[]=urlAndMedia.split("\t");
                String url=split[0];
                String cleanURL=url.replace("\\", "\\\\");
                cleanURL=cleanURL.replace("\"", "\\\"");
                if(hasHost && cleanURL.startsWith("//") == false && cleanURL.contains("://") == false)
                {
                    if(cleanURL.startsWith("/") == false)
                    {
                        cleanURL = "/" + cleanURL;
                    }
                    cleanURL = tempHost + cleanURL;
                }
                String cleanMedia=null;
                if( split.length>1)
                {
                    String media=split[1];
                    cleanMedia=media.replace("\\", "\\\\");
                    cleanMedia=cleanMedia.replace("\"", "\\\"");
                }
                buffer.append("<link rel=\"stylesheet\" href=\"").append(cleanURL).append( "\"");
                if( cleanMedia != null)
                {
                    buffer.append(" media=\"").append(cleanMedia).append( "\"");
                    
                }
                buffer.append( ">\n");
            }
        }
        
        if(links != null)
        {
            for(String link : links)
            {
                buffer.append(link).append("\n");
            }
        }

        if (StringUtilities.notBlank(faviconPath))
        {
            buffer.append("<link rel=\"shortcut icon\" href=\"");
            buffer.append(faviconPath);
            buffer.append("\" />");
        }

        StringBuilder styleBuffer=new StringBuilder();

        if( hasFrameSet == true)
        {
            buffer.append(
                 "</head>\n"
            );
        }
        else
        {
            if( bgImage != null)
            {
                styleBuffer.append( "html {background: url(")
                      .append( bgImage)
                      .append( ")");

                if( bgImageFullPage)
                {
                    styleBuffer.append( " no-repeat center center fixed; -webkit-background-size: cover; -moz-background-size: cover;")
                          .append( " -o-background-size: cover; background-size: cover");
                }
                else if( bgImageFixedPosition == true)
                {
                    styleBuffer.append( " fixed");
                }

                styleBuffer.append( "; }\n");
            }
            else if( bgColor != null)
            {
                styleBuffer.append( "body {background-color:" ).append( makeColorID( bgColor)).append( ";}\n");
            }

            iGenerateJSLibrary(browser, buffer);

            if( headComps != null)
            {
                for (Object headComp : headComps)
                {
                    HTMLComponent comp;
                    comp = (HTMLComponent) headComp;
                    comp.iGenerate(browser, buffer);
                    buffer.append( "\n");
                }
            }

            buffer.append( "</head>");

            // BODY
            buffer.append(
                "\n<body"
            );
            
            if( topmargin != null)
            {
                buffer.append(" TOPMARGIN=\"").append(topmargin).append( "\"");
            }
            if( bottomMargin != null)
            {
                buffer.append(" BOTTOMMARGIN=\"").append(bottomMargin).append( "\"");
            }
            if( leftmargin != null)
            {
                buffer.append(" LEFTMARGIN=\"").append(leftmargin).append( "\"");
            }
            if( rightmargin != null)
            {
                buffer.append(" RIGHTMARGIN=\"").append(rightmargin).append( "\"");
            }
            if( marginheight != null)
            {
                buffer.append(" MARGINHEIGHT=\"").append(marginheight).append( "\"");
            }
            if( marginwidth != null)
            {
                buffer.append(" MARGINWIDTH=\"").append(marginwidth).append( "\"");
            }

            Color bgColorHold=bgColor;

            try
            {
                bgColor=null;
                super.iGenerateAttributes(browser, buffer);
            }
            finally
            {
                bgColor=bgColorHold;
            }
            buffer.append( ">\n");

            if(getPleaseWaitHook() &&
               browser.canHandleDHTML()==true &&
               browser.isBrowserHTTPUnit() == false) //isBrowserMOBILE() == false)
            {
                pleaseWaitHook(buffer);
            }
        }

        super.iGenerate( browser, buffer );

        if( hasFrameSet == false)
        {
            //insert the internal style sheet
            if( styles != null || styleBuffer.length() > 0)
            {
                StringBuilder fullText = new StringBuilder();
                fullText.append(
                    "<style type=\"text/css\">\n"
                );
                fullText.append(styleBuffer);

                for (Object style : styles)
                {
                    HTMLStyleSheet styleSheet;
                    styleSheet = (HTMLStyleSheet) style;
                    styleSheet.iGenerate(browser, fullText);
                    fullText.append( "\n");
                }
                fullText.append(
                    "</style>\n"
                );


                //insert the css right after <HEAD> tag
                int styleIndex = buffer.indexOf("</head>");
                if(styleIndex > 6)
                {
                    buffer.insert(styleIndex, fullText.toString());
                }
                else
                {
                    LOGGER.error("could not insert internal style sheet!\n" + buffer.toString());
                }
            }

            // IE needs this to be last otherwise we get an error on slow page loads.
            if( browser.canHandleGWT())
            {
                createGWT( buffer);
            }
            createNonBlockingScripts( browser, buffer);
            buffer.append(
                "</body>\n"
            );
        }

        buffer.append(
            "</html>\n"
        );
    }

//    private static final String AND="&amp;";
//    private static final String SLASH="&#92;";
//    private static final String QUOTE="&quot;";
    private void makeMeta( final StringBuilder sb, final String type, final String name, final String value )
    {
        if( StringUtilities.notBlank(value))
        {
            String cleanValue=value.replace("\n", " ").replace("\r", " ").trim();
            while( cleanValue.contains("  ")){
                cleanValue=cleanValue.replace("  ", " ");
            }
//            String encodedValue = value.replace( "&", AND).replace("\\", SLASH).replace("\"", QUOTE);
            String temp="<meta " + type + "=\"" + StringUtilities.encodeHTML(name) + "\" content=\"" + StringUtilities.encodeHTML(cleanValue) + "\">\n";
            
            sb.append(temp);
        }        
    }
    
    private void createNonBlockingScripts( final ClientBrowser browser, final StringBuilder sb)
    {
        if( scripts == null) return;
        
        StringBuilder tmp=null;
        for(JavaScript script:scripts)
        {
           // JavaScript script = scripts.get(pos-1);
            if( script instanceof ScriptLink )
            {
                ScriptLink sl=(ScriptLink)script;
                if( sl.loadType != ScriptLink.LoadType.BLOCKING)
                {
                    if( tmp==null)
                    {
                        tmp=new StringBuilder();
                    }
                    
                    sl.iGenerate(browser, tmp);
                }
            }
        }
        
        if( tmp == null) return;
        
        int pos = sb.indexOf("<script");
        if( pos != -1)
        {            
            if( pos > 0)
            {
                if( sb.charAt(pos -1) == '\n')
                {
                    pos=pos -1;
                }
                else
                {
                    tmp.append("\n");
                }
            }
            int start=-1;
            while( true)
            {
                int commentStart=sb.indexOf("<!--", start);
                
                if( commentStart == -1 || commentStart > pos) break;
                
                int endComment=sb.indexOf("-->", commentStart);
                
                if( endComment==-1 || endComment > pos)
                {
                    pos =endComment;
                    break;
                }
                start=endComment;
            }
            if( pos>=0){
                sb.insert(pos, tmp);
            }
        }
        else
        {
            sb.append(tmp);
            sb.append( "\n");
        }
    }
    
    /**
     * generate the GWT module. If there multiple modules then look for a module
     * that contains all of these modules.
     *
     * @param buffer the html buffer
     */
    private void createGWT( final StringBuilder buffer)
    {
        if( gwtModules == null)
        {
            return;
        }

        int size = gwtModules.size();

        if( size == 0)
        {
            return;
        }

        /**
         * Add the GWT modules.
         */
        String modules[] = new String[size];

        gwtModules.keySet().toArray(modules);

        String bestModule;

        if( modules.length == 1)
        {
            bestModule = modules[0];
        }
        else
        {
            bestModule = findBestGWT( modules);
        }

        buffer.append(
            "<script src=\""
        );

        if( StringUtilities.notBlank(SUPER_DEV_MODULE) && bestModule.contains(SUPER_DEV_MODULE))
        {
            buffer.append(SUPER_DEV_SERVER);
        }
        buffer.append( bestModule);
        buffer.append( ".nocache.js\" type=\"text/javascript\"></script>\n");
    }

    /**
     * Find the best module that contains all of these modules.
     *
     * @param modules The list of required modules.
     * @return the best module
     */
    private String findBestGWT( final String modules[])
    {
        HashMap tmpModules;

        try
        {
            tmpModules= loadModules();
        }
        catch( Exception e)
        {
            LOGGER.warn( "could not load GWT modules", e);
            return modules[0];
        }

        int size = tmpModules.size();

        String names[] = new String[size];

        tmpModules.keySet().toArray( names);

        String bestModule = null;
        int bestSize = Integer.MAX_VALUE;
        for (String tmpSM : names)
        {
            String searchModule=tmpSM;
            int pos = searchModule.lastIndexOf( '/');
            searchModule = searchModule.substring( pos + 1);

            HashMap includedMap;
            includedMap = (HashMap)tmpModules.get( searchModule);

            String includedModule[] = new String[ includedMap.size()];
            includedMap.keySet().toArray( includedModule);

            boolean foundAll = true;

            for( int j = 0; foundAll && j < modules.length; j++)
            {
                String currentModule = modules[j];
                pos = currentModule.lastIndexOf( '/');
                currentModule = currentModule.substring( pos + 1);

                boolean found = false;

                for( int k = 0; found == false && k < includedModule.length; k++)
                {
                    if( currentModule.equals( includedModule[k]))
                    {
                        found = true;
                    }
                }

                if( found == false)
                {
                    foundAll = false;
                }
            }

            if( foundAll)
            {
                if( bestModule == null)
                {
                    bestModule = searchModule;
                    bestSize = includedModule.length;
                }
                else if( bestSize > includedModule.length)
                {
                    bestModule = searchModule;
                    bestSize = includedModule.length;
                }
            }
        }

        if( bestModule != null)
        {
            return "/" + bestModule + "/" + bestModule;
        }

        StringBuilder buffer = new StringBuilder();
        for( int i = 0; i < modules.length; i++)
        {
            if( i != 0)
            {
                buffer.append(";");
            }
            String temp = modules[i];
            int pos = temp.lastIndexOf( '/');
            temp = temp.substring( pos + 1);
            buffer.append( temp);
        }

        throw new RuntimeException( "couldn't find a single module for " + buffer);
    }

    /**
     * Load the modules from the definition files in WEB-INF/gwt/com/aspc/gwt...
     *
     * @throws Exception a serious problem
     * @return a hash map of all the modules.
     */
    private HashMap loadModules() throws Exception
    {
        HashMap map = gwtModulesCache;

        if( map != null)
        {
            return map;
        }

        map = HashMapFactory.create();

        String root = CProperties.getDocRoot();
        File dir = new File( root + "/WEB-INF/gwt/");

        if( dir.isDirectory())
        {
            loadModules( map, dir, dir);
        }

        assert ThreadCop.monitor(map, ThreadCop.MODE.READONLY);
        gwtModulesCache = map;

        return map;
    }

    /**
     * Load the modules from the definition files in WEB-INF/gwt/com/aspc/gwt...
     *
     * @param map load into this hash map
     * @param rootDir the base directory
     * @param currentDir the current directory
     * @throws Exception a serious problem
     */
    private void loadModules( final HashMap map, final File rootDir, final File currentDir) throws Exception
    {
        File list[] = currentDir.listFiles();
        for (File file : list)
        {
            if( file.isDirectory())
            {
                loadModules( map, rootDir, file);
            }
            else
            {
                String tmpName = file.getAbsolutePath();

                if( tmpName.endsWith( ".gwt.xml"))
                {
                    HashMap moduleMap = HashMapFactory.create( ThreadCop.MODE.EXTERNAL_SYNCHRONIZED);
                    String temp = tmpName.substring( rootDir.getAbsolutePath().length());
                    temp = temp.substring(0, temp.length() - ".gwt.xml".length());
                    temp = StringUtilities.replace( temp, "/", ".");
                    temp = StringUtilities.replace( temp, "\\", ".");
                    while( temp.contains(".."))
                    {
                        temp = StringUtilities.replace( temp, "..", ".");
                    }

                    if( temp.startsWith( "."))
                    {
                        temp = temp.substring(1);
                    }

                    map.put( temp, moduleMap);
                    moduleMap.put( temp, "");

                    Document doc = DocumentUtil.loadDocument( file);

                    NodeList nl = doc.getElementsByTagName( "inherits");

                    int len = nl.getLength();

                    for( int j = 0; j < len; j++)
                    {
                        Element inherits = (Element)nl.item( j);

                        temp = inherits.getAttribute( "name");

                        moduleMap.put( temp, "");
                    }
                }
            }
        }
    }
    
    /**
     * puts General purpose flags for your own component's use
     * @param name
     * @param value the value
     */
    public void putFlag( final String name, final String value)
    {
        if( flags == null)
        {
            flags = HashMapFactory.create();
        }

        flags.put( name, value);
    }

    /**
     *
     * @param theme
     */
    public void setTheme( final HTMLMutableTheme theme)
    {
        gMutableTheme = theme;
    }

    /**
     * gets General purpose flags for your own component's use
     * @param name
     * @return the value
     */
    @CheckReturnValue @Nonnull 
    public String getFlag( final @Nonnull String name)
    {
        if( flags == null)
        {
            return "";
        }

        Object value = flags.get( name);

        if( value == null)
        {
            return "";
        }

        return (String)value;
    }

    /**
     *  Generates a new unique identifier for new components that do not have
     *  any other way of generating one for themselves.
     * @return the value
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public String doGenerateId()
    {
        String tmpID;
        int inc = 0;
        tmpID = getFlag( "GeneratedId");
        if( tmpID.length() != 0 )
        {
            inc = Integer.parseInt(tmpID);
        }

        tmpID = "ID_GEN" + (++inc);
        putFlag( "GeneratedId", Integer.toString( inc));

        return tmpID;
    }

    /**
     *
     * @return the value
     */
    public static long getStartTime()
    {
        return START_TIME;
    }

//    /**
//     * register native combos are being used.
//     */
//    public void registerNativeCombosUsed()
//    {
//        if( "Y".equals(getFlag("NATIVE_COMBOS")) == false)
//        {
//            putFlag("NATIVE_COMBOS", "Y");
//            addJavaScriptVariable("var nativeCombos='Y';");
//        }
//    }

    /**
     *
     * @param browser
     * @param buffer
     */
    protected void iGenerateJSLibrary(final ClientBrowser browser, final StringBuilder buffer)
    {
        if( scriptVariables != null || lists != null || dictionaries != null)
        {
            buffer.append( "<script type=\"text/javascript\"><!--\n");

            for(
                int i = 0;
                scriptVariables != null &&i < scriptVariables.size();
                i++
            )
            {
                String variable;

                variable = (String)scriptVariables.get(i);

                buffer.append( variable);
                buffer.append( "\n");
            }
            /*
             * Add all the lists as required
             */
            if( lists != null)
            {
                Object keys[] = lists.keySet().toArray();
                for (Object key1 : keys)
                {
                    String key = (String) key1;
                    ArrayList values = lists.get(key);
                    buffer.append("var ");
                    buffer.append(key);
                    buffer.append("='");
                    for( int v= 0; v < values.size(); v++)
                    {
                        if( v != 0)
                        {
                            buffer.append(",");
                        }
                        String value = (String)values.get(v);
                        value = StringUtilities.replace(value, "\\", "\\\\");
                        value = StringUtilities.replace(value, "'", "\\'");
                        value = StringUtilities.replace(value, ",", "\\,");
                        buffer.append(value);
                    }
                    buffer.append( "';");
                    buffer.append( "\n");
                }
            }

            /*
             * Add all the dictionaries as required
             *
             *  var CurrentTheme = {
             *      highlightColor: "#FFFFFF",
             *      shadowColor: "#808080",
             *      errorColor: "#FF0000",
             *      errorIconSrc: "stopsign.gif"
             *  };
             */
            if( dictionaries != null)
            {
                for( String dictionaryCode: dictionaries.keySet())
                {
                    HashMap<String, String> dictionary = dictionaries.get(dictionaryCode);

                    buffer.append("var ");
                    buffer.append(dictionaryCode);
                    buffer.append("={");
                    boolean found=false;
                    for( String code: dictionary.keySet())
                    {
                        if( found)
                        {
                            buffer.append(",");
                        }
                        found=true;
                        buffer.append("\n  ");
                        buffer.append( code);
                        buffer.append( ": \"");

                        String value = dictionary.get(code);
                        value = StringUtilities.replace(value, "\\", "\\\\");
                        value = StringUtilities.replace(value, "\"", "\\\"");

                        buffer.append(value);
                        buffer.append("\"");
                    }
                    buffer.append( "\n};\n");
                }
            }

            buffer.append( "\n--></script>\n");
        }

        boolean needed = false;

        if( scripts != null)
        {
            needed = true;
        }

        if( preRefreshCalls != null)
        {
            needed = true;
        }

        ArrayList list = new ArrayList();//NOPMD

        makeListOfEvents( list);

        if( list.size() > 0)
        {
            needed = true;
        }

        boolean hasHost = StringUtilities.notBlank(host);
        String tempHost = host;
        if(hasHost)
        {
            while(tempHost.endsWith("/"))
            {
                tempHost = tempHost.substring(0, tempHost.length() - 1);
            }
        }
        if( scripts != null || needed == true)
        {
            if( version < 2)
            {
                /*
                 * Add a unique Id to the end of the URL so that
                 * the proxies don't keep a old copy.
                 */
                if( linkExternalScripts == null || linkExternalScripts.equals( "YES"))
                {
                    buffer.append("\n<script src=\"");
                    if(hasHost)
                    {
                        buffer.append(tempHost);
                    }
                    buffer.append("/ds/st_utilities/default/st_utilities.js").append("?ts=").append(getStartTime()).append("\"></script>");
                }
            }
            
            boolean found=false;
            for(
                int i = 0;
                scripts != null &&i < scripts.size();
                i++
            )
            {
                JavaScript script;

                script = scripts.get(i);

                if( script instanceof ScriptLink )
                {
                    ScriptLink sl=(ScriptLink)script;
                    if( sl.loadType == ScriptLink.LoadType.BLOCKING)
                    {
                        script.iGenerate(browser, buffer);
                    }
                }
                else
                {
                    found=true;
                }
            }

            if( found)
            {
                buffer.append( "\n<script type=\"text/javascript\">\n");

                for(
                    int i = 0;
                    scripts != null &&i < scripts.size();
                    i++
                )
                {
                    JavaScript script;

                    script = scripts.get(i);

                    if( (script instanceof ScriptLink) == false )
                    {
                        script.iGenerate(browser, buffer);

                        int pos = buffer.length() -1;
                        if( pos != -1)
                        {
                            char c=buffer.charAt(pos);
                            if( c != '\n')
                            {
                                buffer.append("\n");
                            }
                        }
                    }
                }

                if( preRefreshCalls != null)
                {
                    buffer.append(" function preRefresh( )\n");
                    buffer.append( "{\n");
                    for (Object preRefreshCall : preRefreshCalls)
                    {
                        buffer.append( "  ");
                        buffer.append(preRefreshCall);
                        buffer.append( ";\n");
                    }
                    buffer.append( "}");
               }

                int pos = buffer.length() -1;
                if( pos != -1)
                {
                    char c=buffer.charAt(pos);
                    if( c != '\n')
                    {
                        buffer.append("\n");
                    }
                }
                buffer.append( "\n</script>\n");
            }
        }
    }

    /**
     *
     * @param flag
     */
    protected void setHasFrameSet( final boolean flag)
    {
        hasFrameSet = flag;
    }

    /**
     * theme for page
     */
    protected final void setTheme()
    {
    }

    /**
     *
     * @param addPleaseWait
     */
    public void setPleaseWaitHook(final boolean addPleaseWait)
    {
        pleaseWaitHook = true;
    }

    /**
     *
     * @return the value
     */
    public boolean getPleaseWaitHook()
    {
        return pleaseWaitHook;
    }

    /**
     *
     * @param scroll
     */
    public void setHorizontalScrollBar(final String scroll)
    {
        setStyleProperty( "overflow-x", scroll);
    }

    /**
     * set the temporary working storage variable
     * @param key the key value
     * @param value the value
     */
    public void setWorkingStorage( final String key, final Object value)
    {
        if( workingStorage == null)
        {
            workingStorage = HashMapFactory.create();
        }

        workingStorage.put( key, value);
    }

    /**
     * get the temporary working storage value
     * @param key the key
     * @return the value
     */
    public Object getWorkingStorage( final String key)
    {
        if( workingStorage == null)
        {
            return null;
        }

        return workingStorage.get( key);
    }

    /**
     *
     * @return the value
     */
    public boolean isPageCompiled()
    {
        return this.isCompiled;
    }

    /**
     *
     * @param scroll
     */
    public void setVerticalScrollBar(final String scroll)
    {
        setStyleProperty( "overflow-y", scroll);
    }

    /**
     *
     * @param code
     * @param value the value
     * @return the value
     */
    public boolean listContains( final String code, final String value)
    {
        if( lists == null) return false;

        ArrayList list = lists.get( code);

        if( list != null)
        {
            return list.contains(value);
        }

        return false;
    }

    /**
     *
     * @param code
     * @param value the value
     */
    public void addToList( final String code, final String value)
    {
        if( lists == null)
        {
            lists = new ConcurrentHashMap();
        }

        ArrayList list = lists.get( code);

        if( list == null)
        {
            list = new ArrayList();
            list.add(value);
            lists.put(code, list);
        }
        else
        {
            list.add(value);
        }
    }

    /**
     *
     * @param dictionaryCode
     * @param code
     * @param value the value
     */
    public void addToDictionary( final String dictionaryCode, final String code, final String value)
    {
        if( dictionaries == null)
        {
            dictionaries = HashMapFactory.create();
        }

        HashMap map = dictionaries.get( dictionaryCode);

        if( map == null)
        {
            map = HashMapFactory.create();
            dictionaries.put(dictionaryCode, map);
        }

        map.put(code, value);
    }
    
    public int getDictionarySize( final String dictionaryCode)
    {
        if( dictionaries == null)
        {
            return 0;
        }
        HashMap map = dictionaries.get( dictionaryCode);
        if(map == null)
        {
            return 0;
        }
        return map.size();
    }


     /**
     * puts mobile property
     * @param name
     * @param value the value
     */
    public void putMobileProperty( final String name, final String value)
    {
        if( mobileProperties == null)
        {
            mobileProperties = HashMapFactory.create();
        }

        mobileProperties.put( name, value);
    }

    public void setOgURL(final String ogURL) 
    {
        this.ogURL = ogURL;
    }
    
    public void setOgSiteName( final String ogSiteName) 
    {
        this.ogSiteName = ogSiteName;
    }
    public void setOgImage(String ogImage)
    {
        this.ogImage = ogImage;
    }

    public void setOgTitle(String ogTitle) 
    {
        this.ogTitle = ogTitle;
    }

    public void setOgType(final String ogType) 
    {
        this.ogType = ogType;
    }

    public void setOgDescription(final String ogDescription) 
    {
        this.ogDescription = ogDescription;
    }

    public void setGooglePlusID(final String googleplusid)
    {
        assert googleplusid == null || googleplusid.matches("[\\+0-9a-zA-Z/]+"): "invald goodle plus id " + googleplusid;
        this.googleplusid = googleplusid;
    }        
    
    @Nullable @CheckReturnValue
    public String getFavIconPath()
    {
        return faviconPath;
    }
    
    public void setFavIconPath(final @Nullable String path)
    {
        faviconPath = path;
    }

    private HashMap<String, HashMap<String, String>>       dictionaries;
    private ConcurrentHashMap<String, ArrayList>     lists;
    private ArrayList<String>   styleSheets, links;
    private HashMap     workingStorage;

    private boolean     showNavigateAwayWarning;//NOPMD

    private String      bgImage;

    private int         refreshTime;

    private boolean     hasFrameSet,
                        bgImageFixedPosition,
                        bgImageFullPage,
                        isCompiled,
                        pleaseWaitHook;    

    private ArrayList<JavaScript> scripts;

    private ArrayList   scriptVariables,
                        styles,
                        headComps,

                        preRefreshCalls;

    private HashMap     flags;

    private ConcurrentHashMap     gwtModules;
    
    private String faviconPath;

    /**
     * top margin for html page
     */
    protected String    topmargin;

    /**
     * bottom margin for the html page
     */
    protected String bottomMargin;
    /**
     * left margin for html page
     */
    protected String leftmargin;
    /**
     * Link External Script
     */
    protected String linkExternalScripts;
    /**
     * Right margin for html page
     */
    protected String rightmargin;
    /**
     * margin height for html page
     */
    protected String marginheight;

    /**
     * margin width
     */
    protected String                         marginwidth;

    private static final long START_TIME = System.currentTimeMillis();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLPage");//#LOGGER-NOPMD


    /**
     * a cache of the GWT modules
     */
    private static HashMap gwtModulesCache;//MT CHECKED

    private HashMap mobileProperties;
    
    /**
     * add this host to the css links if it's not blank
     * this host must include protocol, or starts with '//'
     */
    private String host = null;
     
    static 
    {
        SUPER_DEV_MODULE=System.getProperty(ENV_SUPER_DEV_MODULE, "");
        SUPER_DEV_SERVER=System.getProperty(ENV_SUPER_DEV_SERVER, "http://localhost:9876");
    }
}
