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

import com.aspc.remote.html.internal.HTMLAbstractAnchor;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.StringTokenizer;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.RegEx;
import org.apache.commons.logging.Log;

/**
 *  HTMLAnchor
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       August 9, 1999, 4:29 PM
 */
public class HTMLAnchor extends HTMLContainer implements HTMLAbstractAnchor
{
    /**
     *
     */
    public static final String TARGET_BLANK_WINDOW = "_blank";
    /**
     *
     */
    public static final String TARGET_SELF         = "_self";
    /**
     *
     */
    public static final String TARGET_TOP          = "_top";
    /**
     *
     */
    public static final String TARGET_PARENT       = "_parent";

    //** disable dialog box mode
    public static final String DISABLE_DIALOG = "disable";
    
//    public static final Pattern VALID_HREF=Pattern.compile("((http[s]*://([a-z0-9/\\-_\\.]+(:[a-z0-9/\\-_\\.:]+@|))|/([a-z0-9/\\-_\\.~\\\\:]|%([0-9a-f][0-9a-e]|[013-9a-f]f))*((/|\\?|)[\\w\\-:;,\\./%&=~@\\+\\(\\)\\*\\|]*))|(javascript:|#|/|[^h][^t][^t]*[^p]*([^s]|)[^:]*).*)", Pattern.CASE_INSENSITIVE);
    //private static final Pattern VALID_HREF=Pattern.compile("((http(s|)://([a-z0-9/\\-_\\.]+(:[a-z0-9/\\-_\\.:]+@|)|/([a-z0-9/\\-_\\.~\\\\:]|%([0-9a-f][0-9a-e]|[013-9a-f]f)))+(/|)+(\\?[\\w\\-:;,\\./%&=~@\\+\\(\\)\\*\\|]*))|((javascript:|#|/|[^h][^t][^t]*[^p]*([^s]|)[^:]).*))", Pattern.CASE_INSENSITIVE);
    
    /**
     *
     * @param href
     */
    public HTMLAnchor(final String href)
    {
        setURL(href);
        showUnderline = true;
    }

    private static final @RegEx String REGEX_GLOBAL_KEY="([0-9]{9,20}|([0-9a-zA-Z_/:\\+\\(\\)\\-\\.]|%(2[bBfFaA]|3[aA])){1,250}@[0-9]{1,10})~[0-9]{1,10}@[0-9]{1,10}";
    public static boolean validateHREF( final @Nonnull String href)
    {
        if( href.matches(".*%[^0-9a-fA-F](|[^0-9a-fA-F]).*"))
        {
            LOGGER.warn( "Invalid characters in URL: " + href);
            return false;
        }
        int pos = href.indexOf("GLOBAL_KEY=");
        if( pos !=-1)
        {
            int end=href.indexOf("&", pos);
            int end2=href.indexOf("'", pos);
            if( (end2!=-1&&end2<end)||end==-1)
            {
                end=end2;
            }
            
            if( end == -1)
            {
                end=href.length();
            }
            String ekey=href.substring(pos+11, end);
            
            if( StringUtilities.notBlank(ekey) && ekey.matches("@{10,15}")==false)
            {
                String key=StringUtilities.decode(ekey);
                if( key.matches(REGEX_GLOBAL_KEY)==false)
                {
                    LOGGER.warn( "invalid global key: " + key);
                    return false;
                }
            }
        }
        
        if( StringUtilities.URI_PATTERN.matcher(href).matches())
        {
            if( href.matches("http(s|):/+http(s|)://.*"))
            {
                return false;
            }
            else if( href.matches("http(s|):/+javascript:.*"))
            {
                return false;
            }
            return true;
        }
        else if( href.startsWith("javascript:") || href.startsWith("#"))
        {
            return true;
        }
        else if( href.startsWith("/") == false)
        {
            if( href.matches("http(s|):/+javascript:.*"))
            {
                return false;
            }
            else if( href.startsWith("http"))
            {
                return false;
            }
            
            int qPos=href.indexOf("?");
            if( qPos!=-1)
            {
                String query=href.substring(qPos + 1);
                for( String par:query.split("&"))
                {
                    int vPos=par.indexOf("=");
                    
                    if( vPos==-1)
                    {
                        if( StringUtilities.isNotEncoded(par))
                        {
                            LOGGER.warn( "Unencoded name: '" +par +"' in " + href);
                            return false;
                        }
                    }
                    else
                    {
                        String ename=par.substring(0, vPos);
                        
                        if(StringUtilities.isNotEncoded(ename))
                        {
                            LOGGER.warn( "Unencoded name: '" +ename + "' in " + href);
                            return false;
                        }
                        String evalue=par.substring(vPos +1);
                        if( StringUtilities.isNotEncoded(evalue))
                        {
                            LOGGER.warn( "Unencoded value: '" +evalue + "' in " + href);
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        else 
        {
            if( href.matches(".*[ ]+.*"))
            {
                LOGGER.warn( "Invalid characters in URL: " + href);
                return false;
            }
            
            int qPos=href.indexOf("?");
            if( qPos!=-1)
            {
                String query=href.substring(qPos + 1);
                for( String par:query.split("&"))
                {
                    int vPos=par.indexOf("=");
                    
                    if( vPos==-1)
                    {
                        if( StringUtilities.isNotEncoded(par))
                        {
                            LOGGER.warn( "Unencoded name: '" +par +"' in " + href);
                            return false;
                        }
                    }
                    else
                    {
                        String ename=par.substring(0, vPos);
                        
                        if(StringUtilities.isNotEncoded(ename))
                        {
                            LOGGER.warn( "Unencoded name: '" +ename + "' in " + href);
                            return false;
                        }
                        String evalue=par.substring(vPos +1);
                        if( StringUtilities.isNotEncoded(evalue))
                        {
                            LOGGER.warn( "Unencoded value: '" +evalue + "' in " + href);
                            return false;
                        }
                    }
                }
            }
            
            if( StringUtilities.URI_PATTERN.matcher(href.replace("$", "_").replace("*", "_").replace( "+", " ").replace(" ", "%20").replace("{", "_").replace("}", "_").replace("'", "_").replace("[", "_").replace("]", "_")).matches())
            {
                LOGGER.warn( "dodgy URL: " +href);
                return true;
            }
        }
        return false;
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
     * @param cursor
     */
    public void setMouseOverCursor( String cursor)
    {
        rollOverCursor = cursor;
    }

    /**
     *
     * @param href
     */
    public final void setURL( final @Nonnull String href)
    {
        if( validateHREF(href)==false)
        {
            validateHREF(href);
            LOGGER.info( href);
        }
        assert validateHREF(href): "invalid href " + href;
        this.href = href;
    }

    /**
     * add a mouse event to this component
     *
     * @param me The mouse event
     */
    @Override
    public void addMouseEvent(HTMLMouseEvent me)
    {
        iAddEvent(me, "");
    }

    /**
     *
     * @param toolTip
     */
    @Override
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param styleId
     */
    public void setStyle( String styleId)
    {
        iSetStyleId(styleId);
    }

    /**
     *
     * @param flag
     * @return the value
     */
    public HTMLAnchor setMaximise( boolean flag)
    {
        maximiseFg = flag;

        return this;
    }

    /**
     *
     * @param flag
     * @return the value
     */
    public HTMLAnchor setMaximise( Boolean flag)
    {
        maximiseFg = flag;

        return this;
    }

    /**
     *
     * @return the value
     */
    public String getTarget()
    {
        return target;
    }

    /**
     *
     * @param callTarget
     */
    @Override
    public void setTarget(String callTarget)
    {
        if( StringUtilities.isBlank( callTarget))
        {
             target = null;
             return;
        }

        target = callTarget.trim();

        if( target.startsWith("_"))
        {
            String list[] = {
                TARGET_BLANK_WINDOW,
                TARGET_SELF,
                TARGET_TOP,
                TARGET_PARENT
            };

            boolean found = false;

            for (String list1 : list)
            {
                if (target.equalsIgnoreCase(list1))
                {
                    found = true;
                    target = list1;
                    break;
                }
            }

            if( found == false)
            {
                LOGGER.info(
                    "HTML Warning - special target name '" + target + "' not valid"
                );
                target = null;
            }
        }
    }

    /**
     *
     * @param show
     */
    public void showUnderline( boolean show)
    {
        this.showUnderline = show;
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
        iSetId(id);
    }

    /**
     *
     * @param pixels
     */
    @Override
    public void setTargetWidth( int pixels)
    {
        targetWidth = pixels;
    }

    /**
     *
     * @param pixels
     */
    @Override
    public void setTargetHeight( int pixels)
    {
        targetHeight = pixels;
    }

    /**
     *
     * @param on
     */
    @Override
    public void setTargetWindowPlain( boolean on)
    {
        targetWindowPlain = on;
    }

    /**
     *
     * @param on
     */
    @Override
    public void setTargetStatusBar( boolean on)
    {
        targetStatusBar = on ? 1 : -1;
    }

    /**
     *
     * @param mode
     */
    public void setDialogMode( String mode)
    {
        dialogMode = mode;
    }

    /**
     *
     * @param header
     */
    public void setSdiHeader( String header)
    {
        sdiHeader = header;
    }

    /**
     *
     * @return the value
     */
    public int getTargetWidth()
    {
        return targetWidth;
    }

    /**
     *
     * @return the value
     */
    public int getTargetHeight()
    {
        return targetHeight;
    }

    /**
     *
     * @return the value
     */
    public boolean getTargetWindowPlain()
    {
        return targetWindowPlain;
    }

    /**
     *
     * @return the value
     */
    @Override
    public String getURL()
    {
        return href;
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return "HTMLAnchor( '" + getHREF() + "')";
    }

    /**
     *
     * @param url
     * @return the value
     */
    public static String makeBondaryCheckURL( final String url)
    {
        return makeBondaryCheckURL(url, false);
    }

    /**
     *
     * @param url
     * @param isEncoded
     * @return the value
     */
    public static String makeBondaryCheckURL( final String url, boolean isEncoded)
    {
       // assert url.contains("'")==false: "URLs should not contain quote " + url;

        int uriSepLength = 1;
        String uriSeperator = "?";
        boolean knownLength=true;
        if(isEncoded)
        {
            uriSeperator = StringUtilities.encode("?");
  //          pos = url.indexOf(uriSeperator);
            uriSepLength = uriSeperator.length();
            knownLength=false;
        }

        int pos = url.indexOf(uriSeperator);

        if( pos == -1) return url;

        String uri = url.substring(0, pos);

        String params = url.substring( pos + uriSepLength);

        if( params.contains(URL_CHECK) || params.contains(URL_END))
        {
            StringTokenizer st=new StringTokenizer( params, "&");
            StringBuilder buffer = new StringBuilder();

            while( st.hasMoreTokens())
            {
                String temp = st.nextToken();

                if( temp.startsWith( URL_CHECK)) continue;
                if( temp.startsWith( URL_END)) continue;

                if( buffer.length() > 0) buffer.append( "&");
                buffer.append( temp);
            }

            params = buffer.toString();
        }

        int len;

        len = uri.length();            // localhost:9000
        len += URL_CHECK.length() + 3; // ?URL_CHECK=xxx&
        len += params.length();  //HELLO=WORLD
        len += URL_END.length() + 2 + 8;//&UE=0000ffff

        if( len <9 )
        {
            len += 1; // URL_CHECK=0-9
        }
        else if( len < 98)
        {
            len += 2; // URL_CHECK=10-99
        }
        else if( len < 997)
        {
            len += 3; // URL_CHECK=100-999
        }
        else if( len < 9996)
        {
            len += 4; // URL_CHECK=1000-9999
        }
        else
        {
            len += 5; // this will never happen or the browsers will ignore the URL
        }

        StringBuilder buffer = new StringBuilder( len);

        buffer.append( uri);
        buffer.append( uriSeperator );
        buffer.append( URL_CHECK);
        buffer.append( "=");
        if( knownLength)
        {
            buffer.append( len);
        }
        buffer.append( "&");
        buffer.append( params);
        String tmp = StringUtilities.checkSumAdler32(buffer.toString());
        String checkSumAdler32 = "00000000".substring(0, 8-tmp.length()) + tmp;
        
        buffer.append( "&");
        buffer.append( URL_END);
        buffer.append( "=");
        buffer.append( checkSumAdler32);

        String checkedUrl=buffer.toString();

        if( knownLength && checkedUrl.length() != len)
        {
            LOGGER.error( "URL length calculation wrong for '" + checkedUrl + "' " + len + "!=" + checkedUrl.length());
        }

        if( len > URL_MAX_LENGTH)
        {
            String msg="URL exceeds maximum length ( " + len + ") '" + checkedUrl + "'";
            LOGGER.warn( msg);
            assert false: msg;
        }

        return checkedUrl;
    }

    /**
     *
     * @return the href is html escaped, & -> &amp;, ' -> &#39;, " -> &#34;
     */
    @Override
    public String getHREF()
    {

        return getHREF( null);

    }

    public static String htmlEncodeHREF( final String rawHREF)
    {
        StringBuilder sb = new StringBuilder();

        char chars[]=rawHREF.toCharArray();
        for( int i=0; i < chars.length; i++ )
        {
            char c=chars[i];

            if( c == '&')
            {
                boolean found = false;
                for( int j=i; j < i+5 && j<chars.length;j++)
                {
                    char c2 = chars[j];
                    if( c2 == '=')break;

                    if( c2 == ';')
                    {
                        found=true;
                        break;
                    }
                }

                if( found == false)
                {
                    sb.append("&amp;");
                }
                else
                {
                    sb.append( "&");
                }
            }
          //  else if( c == '@')
          //  {
         //       sb.append( "&#");
         //       sb.append( (int) c);
         //       sb.append( ";");
         //   }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 
     * @param browser
     * @return the href is html escaped, & -> &amp;, ' -> &#39;, " -> &#34;
     */
    @Override
    public String getHREF( final ClientBrowser browser)
    {
        String temp;
        temp = href;
        String lowerCaseHref = temp.trim().toLowerCase();
        if( StringUtilities.isBlank(href))
        {
            return "";
        }
        temp=temp.replace("\"", "&#34;");

        if( lowerCaseHref.startsWith( "tel:"))
        {
            String value=temp.substring(4).trim().toUpperCase();
            value = value.replace(' ', '-');
            while( value.contains("--"))
            {
                value = value.replace( "--", "-");
            }

            temp = "tel:" + value;
        }
        else if(
            !lowerCaseHref.contains("javascript:") &&
            !lowerCaseHref.contains("mailto:") &&
            !lowerCaseHref.contains("news:")
        )
        {
            temp = temp.replace(' ', '+');
            temp = temp.replace("'", "&#39;");
            temp = makeBondaryCheckURL( temp, false);
            if(
                targetWidth > 0     ||
                targetWindowPlain == true ||
                targetHeight > 0
            )
            {
                 StringBuilder link = new StringBuilder();


                if(
                    target != null &&
                    target.equals(TARGET_PARENT) == false &&
                    target.equals(TARGET_SELF) == false &&
                    target.equals(TARGET_TOP) == false &&
                    ( browser == null ||
                      browser.isMDI() == true)
                )
                {
                    String function;

                    if( targetWindowPlain == true)
                    {
                        if( targetStatusBar == -1)
                        {
                            // plain with no status bar
                            function = "wosn";
                        }
                        else
                        {
                            // plain with status bar.
                            function = "wosy";
                        }

                    }
                    else
                    {
                       /**
                        * If we use the window opener then we must set on all the features. Why?? I don't know
                        */
                       function = "won";
                    }
                                 
                    String maximiseOption = "";

                    if( maximiseFg != null)
                    {
                        maximiseOption = "," + maximiseFg;
                    }

                    if (!dialogMode.equalsIgnoreCase(DISABLE_DIALOG))
                    {
                        link.append("javascript:openDialog");
                        link.append("('");
                        link.append(temp);
                        link.append("','");
                        link.append(dialogMode);
                        link.append("','");
                        link.append(sdiHeader);
                        link.append("')");
                    } else
                    {
                        assert temp.contains("&amp;")==false: "invalid href " + temp;
//                        LOGGER.info( temp);
                        link.append("javascript:");
                        link.append(function);
                        link.append("('");
                        link.append(temp.replace("&", "&amp;"));
//                                .replace("%7c", "%257c")); // THIS IS WRONG...
                        link.append("','");
                        link.append(target);
                        link.append("',");
                        link.append(targetWidth);
                        link.append(",");
                        link.append(targetHeight);
                        link.append(maximiseOption);
                        link.append(")");
                    }
                    
                    return link.toString();
                }
            }
        }

        return temp;
    }

    /**
     *
     * @param show
     * @return the value
     */
    public HTMLAnchor setShowNavigateAwayWarning( boolean show)
    {
        showNavigateAwayWarning = show;

        return this;
    }

   /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser
     */
    @Override
    protected void compile( ClientBrowser browser)
    {
        HTMLPage page = getParentPage();

        if( page != null)
        {
            if( unloadAdded == false)
            {
                if( showNavigateAwayWarning == false && page.showNavigateAwayWarning())
                {
                    unloadAdded = true;
                    HTMLMouseEvent unloadMe = new HTMLMouseEvent(
                        HTMLMouseEvent.onClickEvent,
                        "allowUnload=true"
                    );

                    addMouseEvent(unloadMe);
                }
            }
        }

        super.compile(browser);
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
        String theHREF;

        theHREF = getHREF(browser);

        buffer.append( "<a");

        if( StringUtilities.isBlank(theHREF) == false)
        {
            assert theHREF.startsWith("javascript:") || theHREF.length() < URL_MAX_LENGTH: "invalid href: " + theHREF;

            buffer.append( " href=\"");
            buffer.append( htmlEncodeHREF(theHREF));
            buffer.append( "\"");
        }

        if(! showUnderline)
        {
            setStyleProperty("text-decoration", "none");
        }

        if( StringUtilities.isBlank( rollOverCursor) == false)
        {
        // Netscape cannot handle hand cursor

            if( rollOverCursor.equalsIgnoreCase( "hand") &&
                browser.isBrowserNETSCAPE())
            {
                setStyleProperty("cursor", "pointer");
            }
            else
            {
                setStyleProperty("cursor", rollOverCursor);
            }

        }

        iGenerateAttributes(browser, buffer);

        /*
         * If the link includes javascript: we can't set the target because
         * Netscape opens the new window then tries to fire off the script
         * and can't find it now ( previous window)
         */
        if( browser.isMDI() && target != null && !theHREF.contains("javascript:"))
        {
            buffer.append(" target=\"").append(target).append( "\"");
        }

        buffer.append( ">");

        super.iGenerate(browser, buffer);

        buffer.append( "</a>");
    }

    /**
     *
     * @param encodedToken The encoded token
     * @param encodedValue the encoded value
     */
    @Override
    public void addCallParameter(String encodedToken, String encodedValue)
    {
        assert StringUtilities.isNotEncoded(encodedToken) == false : "token " + encodedToken + " is not encoded";
        assert StringUtilities.isNotEncoded(encodedValue) == false : encodedValue + " is not encoded";
        if( !href.contains("?"))
        {
            href += "?" + encodedToken + "=" + encodedValue;
        }
        else
        {
            href += "&" + encodedToken + "=" + encodedValue;
        }
    }

    private String      href,
                        target;
    private String      dialogMode = DISABLE_DIALOG;
    private String      sdiHeader = "";

    private int         targetWidth,
                        targetStatusBar,
                        targetHeight;

    private boolean     showUnderline,//NOPMD
                        unloadAdded,
                        showNavigateAwayWarning,
                        targetWindowPlain;

    private Boolean     maximiseFg;

    private String      rollOverCursor;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLAnchor");//#LOGGER-NOPMD
}
