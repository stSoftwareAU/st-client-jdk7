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

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *  ClientBrowser
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       15 May 1998
 */
public class ClientBrowser
{

    /**
     * MAC OS
     */
    public static final String OS_MAC="OS_MAC";
    /**
     *
     */
    public static final String OS_UNIX="UNIX";
    public static final String OS_ANDROID = "ANDROID";

    /**
     *
     */
    public static final String OS_WIN="WIN";

    /*
     * Browsers
     */

    /**
     * mobile phone
     */
    public static final String BROWSER_MOBILE="MOBILE";
    /**
     *
     */
    public static final String BROWSER_DAV="DAV";
    /**
     *
     */
    public static final String BROWSER_BLACKBERRY="BLACKBERRY";

    /**
     * the browser is an IPhone
     */
    public static final String BROWSER_IPHONE="IPHONE";

    /**
     * The browser is a IPad
     */
    public static final String BROWSER_IPAD="IPAD";

    /**
     *
     */
    public static final String BROWSER_SAFARI="SAFARI";

    /**
     * Internet Explorer
     */
    public static final String BROWSER_IE="IE";

    /**
     * Fire fox
     */
    public static final String BROWSER_FIREFOX="FIREFOX";

    /**
     * chrome
     */
    public static final String BROWSER_CHROME="CHROME";
    
    private static final boolean DISABLE_CDN;
    private Boolean knownAsWebCrawler;

    private static final Pattern WEB_CRAWLERS[]={
        Pattern.compile(".*AdsBot-.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*Googlebot.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*ia_archiver.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*bingbot/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*Gigabot/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(msnbot|TurnitinBot|TwengaBot|WWW-Mechanize).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".* (Blekkobot|DotBot|Exabot|MojeekBot|NetSeer crawler|SeznamBot|YandexBot|YandexImages|yoozBot|msnbot/|spider/).*", Pattern.CASE_INSENSITIVE),
//        Pattern.compile(".* DotBot.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*amaya/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Apache-Http(Async|)Client/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Java/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("lwp-trivial/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*Baiduspider.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*Yahoo! Slurp.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*webcrawler.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*Mediapartners-Google.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".* Girafabot;.*", Pattern.CASE_INSENSITIVE),
        
    };
    @SuppressWarnings("PublicInnerClass")
    public static enum PLUGIN
    {
        CHROME_FRAME( "chromeframe");

        public final String name;
        private PLUGIN( final String name)
        {
            this.name=name;
        }

        public boolean match( final String agent)
        {
            return agent.contains(name.toUpperCase());
        }
    };

    /**
     * Opera
     */
    public static final String BROWSER_OPERA="OPERA";

    /**
     *
     */
    public static final String BROWSER_NETSCAPE="NETSCAPE";
    /**
     *
     */
    public static final String BROWSER_HTTPUNIT="HTTPUNIT";
    /**
     *
     */
    public static final String BROWSER_ANDROID="ANDROID";

    private String rawAgent;
    private String agent;
    private String  name,
                    os;

    private boolean //webCrawler,
                    IE64Bit = false;

    private double  browserVersion,
                    osVersion;

    private boolean isMobile;
    private boolean isTablet;

    private boolean isSimulateMobileBrowser;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.ClientBrowser");//#LOGGER-NOPMD

    /**
     * The constructor
     */
    public ClientBrowser( )
    {
        name            = "UNKNOWN";
        browserVersion  = 4;
        osVersion       = -1;
        os              = "UNKNOWN";
    }

    /**
     *
     * @param agent
     */
    public ClientBrowser( final String agent)
    {
        this();

        String encodedAgent="";

        if( agent != null)
        {
            encodedAgent = StringUtilities.encode( agent);
        }

        parseAgent( encodedAgent);//NOPMD
    }
    
    /**
     * Platform for Privacy Preferences Project
     * 
     * https://en.wikipedia.org/wiki/P3P#The_future_of_P3P
     * https://support.google.com/accounts/answer/151657?hl=en
     * 
     * https://www.w3.org/P3P/
     * 
     * @return true if supported 
     */
    @CheckReturnValue
    public boolean supportsP3P()
    {
        if( isBrowserIE()) return true;
        
        return false;
    }
    
    /**
     * Is the input required supported.
     * 
     * http://www.the-art-of-web.com/html/html5-form-validation
     * 
     * http://caniuse.com/#feat=form-validation
     * 
     * @return true if supported 
     */
    @CheckReturnValue
    public boolean supportsInputRequired()
    {
        if( isBrowserChrome()) return true;
        if( isBrowserFirefox()) return true;
        if( isBrowserSafari()) return true;

        double version = getBrowserVersion();

        if( isBrowserIE() && version >= 10) return true;

        return isBrowserOpera() && version >= 12.1;
    }
    
    @CheckReturnValue
    public boolean supportsServiceWorker()
    {
        if( isBrowserChrome())
        {
            return true;
        }
        else if( isBrowserFirefox())
        {
            return true;
        }
        else if( isBrowserOpera())
        {
            return true;
        }
        return false;
    }
    
    /**
     * http://caniuse.com/input-placeholder
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean supportsPlaceholder()
    {
        double version = getBrowserVersion();

        if( isBrowserChrome() && version >= 4) return true;
        if( isBrowserFirefox() && version >= 4) return true;

        if( isBrowserIE() && version >= 10) return true;
        if( isBrowserSafari() && version >= 5) return true;

        return isBrowserOpera() && version >= 11.6;
    }

    /**
     * generate the raw HTML for this component.
     *
     * http://www.w3schools.com/html5/html5_form_input_types.asp
     * <pre>
     *     Input type   IE  Firefox Opera   Chrome  Safari
     *     ==========   === ======= =====   ======  ======
     *     email        No  4.0     9.0     10.0    No
     *     url          No  4.0     9.0     10.0    No
     *     number       No  No      9.0     7.0     5.1
     *     range        No  No      9.0     4.0     4.0
     *     Date pickers No  No      9.0     10.0    5.1
     *     search       No  4.0     11.0    10.0    No
     *     color        No  No      11.0    12      No
     * </pre>
     * @return true if the input type DATE
     */
    @CheckReturnValue
    public boolean supportsTypeDate()
    {
        return !isBrowserFirefox() && !isBrowserIE() && (!isBrowserOpera() || getBrowserVersion() >= 9) && (!isBrowserChrome() || getBrowserVersion() >= 10) && (!isBrowserSafari() || getBrowserVersion() >= 5.1);
    }

    /**
     * generate the raw HTML for this component.
     *
     * http://html5test.com/compare/browser/ie10.html
     * <pre>
     *     Input type   IE  Firefox       Opera   Chrome  Safari
     *     ==========   === =======       =====   ======  ======
     *     search       10    No           ??      10.0    5.1
     * </pre>
     *
     * http://css-tricks.com/webkit-html5-search-inputs/
     *
     * @return true if the input type DATE
     */
    @CheckReturnValue
    public boolean supportsTypeSearch()
    {        
        double version=getBrowserVersion();
        return isBrowserIE() && version >= 10 ||
                //(isBrowserFirefox() && getBrowserVersion() >= 4) ||
                (isBrowserChrome() && version >= 10) ||
                // (isBrowserOpera() && getBrowserVersion() >= 11)  ||
                (isBrowserSafari() && version >= 5.1);
    }

    /**
     * generate the raw HTML for this component.
     *
     * http://html5test.com/compare/browser/ie-10.html
     * <pre>
     *     Input type   IE  Firefox Opera   Chrome  Safari
     *     ==========   === ======= =====   ======  ======
     *     number        10      28   9.0      7.0     5.1
     * </pre>
     * @return true if the input type NUMBER
     */
    @CheckReturnValue
    public boolean supportsTypeNumber()
    {
        if(isBrowserFirefox() )
        {
            if( getBrowserVersion() >= 28)
            {
                return true;                
            }
        }
        else
        {
            return  (!isBrowserIE() || getBrowserVersion() >= 10) && (!isBrowserOpera() || getBrowserVersion() >= 9) && (!isBrowserChrome() || getBrowserVersion() >= 7) && (!isBrowserSafari() || getBrowserVersion() >= 5.1);
        }
        
        return false;
    }

    /**
     * generate the raw HTML for this component.
     *
     * http://html5test.com/compare/browser/ie10.html
     * <pre>
     *     Input type   IE  Firefox Opera   Chrome  Safari
     *     ==========   === ======= =====   ======  ======
     *     email         10     4.0   9.0     10.0      No
     * </pre>
     * @return true if the input type NUMBER
     */
    @CheckReturnValue
    public boolean supportsTypeEmail()
    {
        return (!isBrowserFirefox() || getBrowserVersion() >= 4) && (!isBrowserIE() || getBrowserVersion() >= 10) && (!isBrowserOpera() || getBrowserVersion() >= 9) && (!isBrowserChrome() || getBrowserVersion() >= 10);
    }

    /**
     * generate the raw HTML for this component.
     *
     * http://html5test.com/compare/browser/ie10.html
     * <pre>
     *     Input type   IE  Firefox Opera   Chrome  Safari
     *     ==========   === ======= =====   ======  ======
     *     url           10     4.0   9.0     10.0      No
     * </pre>
     * @return true if the input type NUMBER
     */
    @CheckReturnValue
    public boolean supportsTypeURL()
    {
        return (!isBrowserFirefox() || getBrowserVersion() >= 4) && (!isBrowserIE() || getBrowserVersion() >= 10) && (!isBrowserOpera() || getBrowserVersion() >= 9) && (!isBrowserChrome() || getBrowserVersion() >= 10);
    }

    /**
     * Should use native Date picker
     * @return true if we should use the native date picker
     */
    @CheckReturnValue
    public boolean nativeDatePicker()
    {
        return formMobile() || formTablet();
    }

    /**
     * http://msdn2.microsoft.com/en-us/library/ms537503.aspx
     *
     * @param encodedAgent the agent.
     */
    @SuppressWarnings("empty-statement")
    private void parseAgent( final String encodedAgent)
    {
        try
        {
            if( encodedAgent == null)
            {
                return;
            }

            agent = StringUtilities.decode( encodedAgent);
            rawAgent=agent;
            agent = agent.toUpperCase();
            int likePos=agent.indexOf(") LIKE");
            if( likePos != -1)
            {
                agent=agent.substring(0, likePos);
            }
//            if( agent.matches("[\\['].*"))
//            {
//                agent=agent.substring(1);
//            }
            boolean found = false;
            if( agent.contains("TABLET PC"))
            {
                isTablet=true;
            }
            String browsers[][] = {
                {"MSIE",    BROWSER_IE},
//                {"IEMOBILE",    BROWSER_IE},
                {"CONTYPE", BROWSER_IE},
                {"TRIDENT", BROWSER_IE},
                {"SAFARI",  BROWSER_SAFARI},
                {BROWSER_HTTPUNIT,  BROWSER_HTTPUNIT},
            };

            if(agent.contains("ANDROID"))
            {
                os = OS_ANDROID;
                
                String android=" ANDROID ";
                int pos = agent.indexOf(android);
                if( pos != -1)
                {
                    int start = pos + android.length();
                    int end = agent.indexOf(";", start);
                    if( end == -1)
                    {
                        end = agent.indexOf(")", start);
                    }

                    if( end != -1)
                    {
                        String tmpVersion = agent.substring(start, end);
                        pos = tmpVersion.indexOf(".");
                        if( pos != -1)
                        {
                            tmpVersion = tmpVersion.substring(0, pos) + "." + tmpVersion.substring(pos + 1).replace(".", "");
                        }

                        try
                        {
                            osVersion = Double.parseDouble(tmpVersion);
                        }
                        catch( NumberFormatException nfe)
                        {
                            LOGGER.warn( agent,  nfe);
                        }
                    }
                }    
            }
            
            if( agent.startsWith("OPERA"))
            {
                name=BROWSER_OPERA;
                makeVersion( BROWSER_OPERA + "/");
                if( agent.contains("ANDROID"))
                {
                    isMobile=true;
                }

            }
            else if(
                agent.contains(BROWSER_BLACKBERRY)
            )
            {
                name = BROWSER_BLACKBERRY;
                isMobile=true;

                makeVersion( "/");
            }
            else if(
                agent.contains(BROWSER_IPHONE) ||
                agent.contains("IPOD")
            )
            {
                name = BROWSER_IPHONE;
                makeVersion( "VERSION/");
                isMobile=true;
                os=OS_MAC;
            }
            else if(
                agent.contains("IPAD")
            )
            {
                name = BROWSER_IPAD;
                isTablet=true;
                os=OS_MAC;
                makeVersion( "VERSION/");
            }
            else if(
                agent.contains(BROWSER_FIREFOX)
            )
            {
                name = BROWSER_FIREFOX;

                makeVersion( BROWSER_FIREFOX + "/");
                if(agent.contains("ANDROID") && ( agent.contains("MOBILE") || agent.contains("PHONE") ))
                {
                    isMobile=true;
                }
            }
            else if(
                agent.contains(BROWSER_CHROME) &&
                agent.contains("CHROMEFRAME") ==false
            )
            {
                name = BROWSER_CHROME;

                makeVersion( BROWSER_CHROME + "/");
                if(agent.contains("MOBILE SAFARI") )
                {
                    isMobile=true;
                }
                else if(agent.contains("ANDROID"))
                {
                    isTablet = true;
                }

            }
            /*else if(agent.equals("MOZILLA/4.0") || agent.equals("MOZILLA/5.0"))
            {
                name = BROWSER_MOBILE;
                isTablet=true;
            }*/
            else if(agent.contains("ANDROID") )
            {
                name = BROWSER_ANDROID;
                if( agent.contains("MOBILE") || agent.contains("PHONE") )
                {
                    isMobile=true;
                }
                else
                {
                    isTablet=true;
                }
                
                String safariVersion=" SAFARI/";
                int pos = agent.indexOf(safariVersion);
                if( pos != -1)
                {
                    int start = pos + safariVersion.length();
                    int end = agent.indexOf(";", start);
                    if( end==-1)
                    {
                        end = agent.length();
                    }
                    int end2 = agent.indexOf(" ", start);
                    if( end2!=-1)
                    {
                        if( end > end2)
                        {
                            end=end2;
                        }
                    }
                    
                    String tmpVersion = agent.substring(start, end);
                    pos = tmpVersion.indexOf(".");
                    if( pos != -1)
                    {
                        tmpVersion = tmpVersion.substring(0, pos) + "." + tmpVersion.substring(pos + 1).replace(".", "");
                    }
                    
                    try
                    {
                        browserVersion = Double.parseDouble(tmpVersion);
                    }
                    catch( NumberFormatException nfe)
                    {
                        LOGGER.warn( agent,  nfe);
                    }
                }
            }
            else if( agent.startsWith("MOZILLA") ||agent.startsWith("OUTLOOK") || agent.startsWith("MOZILA") || agent.startsWith( BROWSER_HTTPUNIT))
            {
                for (String[] browser : browsers) 
                {
                    int pos;
                    pos = agent.indexOf(browser[0]);
                    if (pos != -1) {
                        found = true;
                        name = browser[1];
                        break;
                    }
                }
                if( found == false)
                {
                    if( agent.contains("WEBKIT"))
                    {
                        name=BROWSER_SAFARI;
                    }
                    else
                    {
                        name = BROWSER_NETSCAPE;
                    }
                }

                int p1, p2 = -1;

                if( name.equals(BROWSER_IE))
                {
                    if( agent.contains("IEMOBILE"))
                    {
                        isMobile=true;
                    }
                    p1 = agent.indexOf("MSIE");
                    if( p1 != -1 )
                    {
                        p1 += 5;

                        p2 = agent.substring( p1).indexOf(';');

                        p2 += p1;
                    }
                    else
                    {
                        //IE 11
                        p1 = agent.indexOf(" RV:");
                        if(p1 != -1)
                        {
                            p1 += 4;
                        }
                        int idx = 0;
                        String temp = agent.substring(p1);
                        for(;idx < temp.length();idx++)
                        {
                            char c = temp.charAt(idx);
                            if((c < 48 || c > 57) && c != '.')
                            {
                                break;
                            }
                        }
                        p2 = p1 + idx;
                    }
                }
                else
                {
                    p1 = agent.indexOf('/');
                    if( p1 != -1)
                    {
                        p1 ++;

                        for(
                            p2 = p1;
                            p2 < agent.length() &&
                            (
                                Character.isDigit(agent.charAt(p2)) ||
                                agent.charAt(p2) == '.'
                            );
                            p2++
                        )
                        {
                            ;
                        }
                    }
                }

                if( p1 != -1 && p2 > p1)
                {
                    try
                    {
                        String temp = agent.substring( p1, p2 );
                        int pos = 0;
                        for(
                            ;
                            pos < temp.length() &&
                            (
                                Character.isDigit(temp.charAt(pos)) ||
                                temp.charAt(pos) == '.'
                            );
                            pos++
                        )
                        {
                            ;
                        }

                        temp = temp.substring(0, pos);

                        if( StringUtilities.isBlank(temp)== false)
                        {
                            browserVersion = (new Double( temp ));
                        }

                        if( name.equals( BROWSER_NETSCAPE) && browserVersion >= 5 && browserVersion <= 6)
                        {
                            browserVersion = 6;
                        }
                    }
                    catch( NumberFormatException e)
                    {
                        LOGGER.info( "Error parsing agent", e);
                        browserVersion = 6;
                    }
                }
                else
                {
                    LOGGER.info("No version in " + agent);
                    browserVersion = 4;
                }
            }
            else if(
                agent.contains(BROWSER_DAV) ||
                agent.contains("MICROSOFT DATA ACCESS INTERNET PUBLISHING")
            )
            {
                name = BROWSER_DAV;
            }
            else if(
                agent.contains("WINDOWS CE") ||
                agent.contains("CONFIGURATION/CLDC")
            )
            {
                name = BROWSER_MOBILE;
                isMobile=true;
            }
//            boolean osFound = true;

            if( osVersion < 0)
            {
                /*
                 * http://www.zytrax.com/tech/web/msie-history.html
                 */
                String osList[][] = {
                    {"WINDOWS NT 6.2",          "WIN_8",        "6.2"},       // Windows 8
                    {"WINDOWS NT 6.1",          "WIN_7",        "6.1"},       // Windows 7
                    {"WINDOWS NT 6.0",          "WIN_VISTA",    "6.0"}, // Windows Vista
                    {"WINDOWS NT 5.2",          "WIN_XP",       "5.2"}, // Windows Server 2003; Windows XP x64 Edition
                    {"WINDOWS NT 5.1",          "WIN_XP",       "5.1"}, // Windows XP
                    {"WINDOWS NT 5.0",          "WIN_NT",       "5.0"}, // Windows 2000
                    {"WINDOWS NT 5.01",         "WIN_NT",       "5.0"}, // Windows 2000, Service Pack 1 (SP1)
                    {"WINNT",                   "WIN_NT",       "4.0"},
                    {"WINDOWS NT 4.0",          "WIN_NT",       "4.0"}, //Microsoft Windows NT 4.0
                    {"WIN16",                   "WINDOWS",      "3.1"},
                    {"WIN32",                   "WINDOWS",      "3.1"},
                    {"WIN95",                   "WINDOWS",      "95"},
                    {"WINDOWS 95",              "WINDOWS",      "95"}, //Windows 95 Windows 95
                    {"WIN98",                   "WINDOWS",      "98"},
                    {"WINDOWS 98; Win 9x 4.90", "WINDOWS",      "98"}, //Windows Millennium Edition (Windows Me)
                    {"WINDOWS 98",              "WINDOWS",      "98"}, //Windows 98 Windows 98
                    {"WINDOWS CE",              "WINDOWS_CE",   "-1"}, //Windows CE Windows CE
                    {"WINDOWS",                 "WINDOWS",      "-1"},
                    {"LINUX",                   OS_UNIX,         "-1"},
                    {"X11",                   OS_UNIX,         "-1"},
                    {"SUNOS",                   OS_UNIX,         "-1"},
                    {"MAC",                     OS_MAC,          "-1"},
                    {"HTTPUNIT/1.5",            "WIN_XP",         "5.1"},
                    {"MICROSOFT DATA ACCESS INTERNET",   "WIN_XP",         "5.1"},
                    {"CONFIGURATION/CLDC-1.0",   "WAP",         "1.0"},
                    {"CONFIGURATION/CLDC-1.1",   "WAP",         "1.1"},
                };
//                osFound = true;
                for (String[] osList1 : osList) 
                {
                    String signature;
                    signature = osList1[0];
                    if (agent.contains(signature)) 
                    {
                        os = osList1[1];
                        osVersion = Double.parseDouble(osList1[2]);
                        if( osVersion <= 0)
                        {
                            String macOS=" MAC OS X ";
                            int pos = agent.indexOf(macOS);
                            if( pos != -1)
                            {
                                int start = pos + macOS.length();
                                int end = agent.indexOf(";", start);
                                if( end == -1)
                                {
                                    end = agent.indexOf(")", start);
                                }

                                if( end != -1)
                                {
                                    String tmpVersion = agent.substring(start, end);
                                    pos = tmpVersion.indexOf("_");
                                    if( pos != -1)
                                    {
                                        tmpVersion = tmpVersion.substring(0, pos) + "." + tmpVersion.substring(pos + 1);
                                    }

                                    tmpVersion = tmpVersion.replace("_", "");
                                    int firstDot=tmpVersion.indexOf(".");
                                    if( firstDot != -1)
                                    {
                                        int secondDot=tmpVersion.indexOf(".", firstDot+ 1);
                                        
                                        if( secondDot != -1)
                                        {
                                            tmpVersion=tmpVersion.substring(0, secondDot);
                                        }
                                    }
                                    if( tmpVersion.matches("[0-9]+(\\.[0-9]+){0,1}"))
                                    {
                                        try
                                        {
                                            osVersion = Double.parseDouble(tmpVersion);
                                        }
                                        catch( NumberFormatException nfe)
                                        {
                                            LOGGER.warn( agent,  nfe);
                                        }
                                    }
                                }
                            }                                            
                        }
//                        osFound = true;
                        break;
                    }
                }
            }
            
            if(BROWSER_IE.equals(name))
            {
                if(agent.contains("WIN64; X64") || agent.contains("WIN64; IA64"))
                {
                    IE64Bit = true;
                }
                if(agent.contains("WOW64"))
                {
                    IE64Bit = false;
                }
            }
            if( BROWSER_ANDROID.equals(name))
            {
                os=OS_ANDROID;
            }
        }
        catch( RuntimeException e)
        {
            LOGGER.info("Couldn't get client info ", e);
        }
    }

    private void makeVersion(final String search )
    {
        String temp=null;
        int start = agent.indexOf( search);
        if( start > -1 )
        {
            start += search.length();
            int end = start;

            while( end < agent.length() && Character.isDigit(agent.charAt(end)))
            {
                end++;
            }

            if( end < agent.length() && agent.charAt(end) == '.')
            {
                end++;
                while( end < agent.length() && Character.isDigit(agent.charAt(end)))
                {
                    end++;
                }

                temp=agent.substring(start, end);
            }
        }
        if( StringUtilities.isBlank(temp)== false)
        {
            browserVersion = (new Double( temp ));
        }
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getInfo()
    {
        String  t;

        t = "BROWSER="+ name +
            ",BROWSER_VERSION=" + browserVersion +
            ",OS=" + os + ",OS_VERSION=" + osVersion;

        return t;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isWebCrawler()
    {
        if( knownAsWebCrawler==null)
        {                              
            boolean tmpFlag=false;
            if( StringUtilities.notBlank(agent))
            {
                for( Pattern p: WEB_CRAWLERS)
                {
                    if( p.matcher(agent).matches())
                    {
                        tmpFlag=true;
                        break;
                    }
                }
            }
            
            knownAsWebCrawler= tmpFlag;
        }
        
        return knownAsWebCrawler;
    }
    
    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserOpera()
    {

        return name.equals(BROWSER_OPERA);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserChrome()
    {

        return name.equals(BROWSER_CHROME);
    }

    @CheckReturnValue
    public boolean hasPlugin( PLUGIN plugin )
    {
        return plugin.match( agent);
    }

    /**
     * is this fire fox ?
     * @return true if firefox
     */
    @CheckReturnValue
    public boolean isBrowserFirefox()
    {

        return name.equals(BROWSER_FIREFOX);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserIE()
    {

        return name.equals("IE");
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserHTTPUnit()
    {

        return name.equalsIgnoreCase( ClientBrowser.BROWSER_HTTPUNIT);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserNETSCAPE()
    {

        return name.equals(BROWSER_NETSCAPE);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserBlackBerry()
    {

        return name.equals(BROWSER_BLACKBERRY);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserIPhone()
    {

        return name.equals(BROWSER_IPHONE);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserIPad()
    {

        return name.equals(BROWSER_IPAD);
    }
    
    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserAndroidMobile()
    {
        return (name.equals(BROWSER_ANDROID)|| agent.contains("ANDROID")) && isMobile;
    }
    
    /**
     * Android tablet or mobile
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserAndroid()
    {
        return name.equals(BROWSER_ANDROID);
    }
    
    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserDAV()
    {

        return name.equals(BROWSER_DAV);
    }

    /**
     * Is this browser a desktop browser ?
     * @return true if this is a desktop browser
     */
    @CheckReturnValue
    public boolean formDesktop()
    {
        return formMobile() == false && formTablet() == false;
    }

    /**
     * Is this browser a mobile phone ?
     * @return true if a mobile phone
     */
    @CheckReturnValue
    public boolean formMobile()
    {
        return isMobile || isSimulateMobileBrowser;
    }

    /**
     * Is this browser a mobile phone ?
     * @return true if a mobile phone
     */
    @CheckReturnValue
    public boolean isBrowserMOBILE()
    {
        return formMobile();
    }

    /**
     * Is this browser a mobile phone ?
     * @return true if a mobile phone
     */
    @CheckReturnValue
    public boolean formTablet()
    {
        return isTablet;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isBrowserSafari()
    {

        return name.equals("SAFARI") || isBrowserIPhone() || isBrowserIPad();
    }

    /**
     * Should we use a Multi-Document interface ?
     * @return false means Single Document Interface ie. no popups.
     */
    @CheckReturnValue
    public boolean isMDI()
    {

         return !isPocketPC();
    }
    
    /**
     * is the browser 64-bit IE
     * @return false for 32-bit IE or other browsers
     */
    @CheckReturnValue
    public boolean is64BitIE()
    {
        return IE64Bit;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean osMAC()
    {
        return os.equals(OS_MAC);
    }
    
    @CheckReturnValue
    public boolean osAndroid()
    {
        return os.equals(OS_ANDROID);
    }

    /**
     * Is this windows
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean osWindows()
    {
        return os.startsWith(OS_WIN);
    }

    /**
     * Is this Linux or Unix
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean osLinux()
    {
        return os.equals(OS_UNIX);
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean isPocketPC()
    {
        return os.equals("WINDOWS_CE");
    }

     /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean canHandleDHTML()
    {
        return isPocketPC() != true && (formMobile() != true || isBrowserIPhone() != false || isBrowserAndroidMobile() != false) && isWebCrawler() != true;
    }

    /**
     * We know that this browser doesn't support a browser and we shouldn't bother sending the applets.
     * @return true if the applets will not run.
     */
    @CheckReturnValue
    public boolean knownNotToSupportJavaApplets()
    {
        return formMobile() || isBrowserIPad();
    }

     /**
     * We know that this browser doesn't support a javascript and we shouldn't bother sending it.
     * @return true if the applets will not run.
     */
    @CheckReturnValue
    public boolean knownNotToSupportJavaScripts()
    {
        if( formMobile())
        {
            return !isBrowserIPhone() && !isBrowserAndroidMobile();
        }
        return false;
    }

    /**
     * can this browser handle TEL: anchors ?
     * @return true if this browser handles TEL: anchors
     */
    @CheckReturnValue
    public boolean canHandleTEL()
    {

        return formMobile() || isBrowserHTTPUnit();
    }

    /**
     * can this browser handle GWT ?
     * @return true if this browser handle GWT
     */
    @CheckReturnValue
    public boolean canHandleGWT()
    {
        return isBrowserHTTPUnit() != true && (formMobile() != true || isBrowserIPhone() != false || isBrowserAndroidMobile() != false) && isBrowserDAV() != true && isWebCrawler() != true;
    }
    
    @CheckReturnValue
    public boolean canHandleJqGrid()
    {
        if(isBrowserHTTPUnit())
        {
            return false;
        }
        if(isBrowserIE() && getBrowserVersion() < 9)
        {
            return false;
        }
        return true;
    }

    /**
     * http://codemirror.net/
     * 
     * The following desktop browsers are able to run CodeMirror:
     *
     *   Firefox 3 or higher
     *   Chrome, any version
     *   Safari 5.2 or higher
     *   Opera 9 or higher (with some key-handling problems on OS X)
     *   Internet Explorer 8 or higher in standards mode
     *   (Not quirks mode. But quasi-standards mode with a transitional doctype is also flaky. <!doctype html> is recommended.)
     *   Internet Explorer 7 (standards mode) is usable, but buggy. It has a z-index bug that prevents CodeMirror from working properly.
     * 
     * @return true if has full support
     */
    @CheckReturnValue    
    public boolean canHandleCodeMirror()
    {
        if( isBrowserMOBILE())
        {
            return false;
        }
        
        if( isBrowserIE())
        {
            return getBrowserVersion()>=9;
        }
        else if( isBrowserOpera())
        {
            return getBrowserVersion()>=9;
        }
            
        return true;
    }
    
    /**
     * https://developer.mozilla.org/en-US/docs/Web/API/event.stopPropagation
     * 
     * Feature          Chrome  Firefox  Internet Explorer  Opera   Safari
     * Basic support    (Yes)   (Yes)        9                  (Yes)   (Yes)
     * 
     * @return true if browser supports the method. 
     */
    @CheckReturnValue
    public boolean hasEventStopPropagation()
    {
        if( isBrowserIE() && getBrowserVersion() <9)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
            
    /**
     * http://stackoverflow.com/questions/550038/is-it-valid-to-replace-http-with-in-a-script-src-http
     * 
     * This is used for CDN
     * @return true if supported.
     */
    @CheckReturnValue
    public boolean canHandleCDN()
    {
        if( DISABLE_CDN) return false;
        
        if( isBrowserHTTPUnit())
        {
            return false;
        }
        return true;
    }
    
    /**
     * Firefox doesn't allow copy of disabled fields. 
     * 
     * https://bugzilla.mozilla.org/show_bug.cgi?id=253870
     * 
     * @return true if the browser correctly handles disabled. 
     */
    @CheckReturnValue
    public boolean canHandleDisabledInput()
    {
        if( isBrowserFirefox())
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * can this browser handler CSS3?
     * @return true if this browser handlers CSS3
     */
    @CheckReturnValue
    public boolean canHandleCSS3()
    {
        return isBrowserIE() != true || getBrowserVersion() > 8.99;
    }

    /**
     * Does this browser support the HTML editor ?
     *
     * @return true if does support.
     */
    @CheckReturnValue
    public boolean canHandleTinyMCE()
    {
        if (isBrowserIE())
        {
            if (getBrowserVersion() < 5.0)
            {
                return false;
            }
            return !osMAC();
        }
        else if( isBrowserFirefox())
        {
            return true;
        }
        else if (isBrowserNETSCAPE())
        {
            return getBrowserVersion() >= 1.3;
        }
        else if( isBrowserIPhone())
        {
            return false;
        }
        else if (isBrowserSafari())
        {
            return true;
        }
        else if( isBrowserChrome())
        {
            return true;
        }
        else return isBrowserOpera();
    }

    /**
     * can this browser handle IFrame ?
     * @return true if can handle.
     */
    @CheckReturnValue
    public boolean canHandleIFrame()
    {
        return formMobile() != true || isBrowserIPhone() != false || isBrowserAndroidMobile() != false;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getBrowserName()
    {
        return name;
    }

    /**
     * get the browser version, eg: if IE11 is running in compatibility view mode of IE7,
     * the browser version should be 7. The getBrowserRealVersion will return 11
     * @return the value
     */
    @CheckReturnValue
    public double getBrowserVersion()
    {
        return browserVersion;
    }
    
    /**
     * get the browser real version
     * @return 
     */
    @CheckReturnValue 
    public double getBrowserRealVersion()
    {
        if(isBrowserIE())
        {
            int idx = agent.indexOf("TRIDENT/");
            if(idx > -1)
            {
                String str = agent.substring(idx + 8, agent.indexOf(';', idx));
                switch(str)
                {
                    case "7.0": return 11;
                    case "6.0": return 10;
                    case "5.0": return 9;
                    case "4.0": return 8;
                    default: LOGGER.warn("unknown Trident token value " + str);
                }
            }
        }
        return getBrowserVersion();
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getOSName()
    {
        return os;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public double getOSVersion()
    {
        return osVersion;
    }
    
    /**
     * is match the block browser pattern
     * @param blockPattern browser pattern to block
     * @return the browser should be blocked if returns true
     */
    @CheckReturnValue
    public boolean isMatchBlockPattern(final @Nonnull String blockPattern) throws PatternSyntaxException
    {
        return Pattern.matches(blockPattern, agent);
    }

    /**
     *
     * @param pBrowserName
     * @return the value
     */
    @CheckReturnValue
    public boolean isMatch( final @Nonnull String pBrowserName)
    {
        String browserName = pBrowserName.toUpperCase().trim();

        if( browserName.equals("*"))
        {
            return true;
        }

        return browserName.equals(name);
    }

    /**
     *
     * @param browserName
     * @param OSname
     * @return the value
     */
    @CheckReturnValue
    public boolean isMatch( final @Nonnull String browserName, final @Nonnull String OSname)
    {
        if( isMatch( browserName) == false)
        {
            return false;
        }

        String osName = OSname.toUpperCase().trim();

        if( osName.equals("*"))
        {
            return true;
        }

        if( osName.length() > 3)
        {
            osName = osName.substring(0,3);
        }

        return os.startsWith(osName);
    }

    /**
     *
     * @param browserName
     * @param osName
     * @param lowVersion
     * @return the value
     */
    @CheckReturnValue
    public boolean isMatch(
        final @Nonnull String browserName,
        final @Nonnull String osName,
        final double lowVersion
    )
    {
        if( isMatch( browserName, osName) == false)
        {
            return false;
        }

        return lowVersion <= browserVersion || browserVersion == -1;
    }

    /**
     * NTLM is a Windows only technology which is supported by the following browsers. 
     * 
     * http://en.wikipedia.org/wiki/Integrated_Windows_Authentication#Supported_web_browsers
     * 
     * @return true if the browser/OS supports NTML
     */
    @CheckReturnValue
    public boolean canHandleNTLM()
    {
        if( isMatch("IE", "*", 6.0))
        {
            return true;
        }
        else if( isBrowserChrome() && osWindows())
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 
     * @return the value
     */
    @CheckReturnValue
    public boolean canHandlePrintBootStrap()
    {
        boolean canHandle = true;
        double version = getBrowserVersion();
        if (isBrowserSafari() && version < 6)
        {
            canHandle = false;
        }
        return canHandle;
    }
    

    /**
     * Does the current browser match ?
     *
     * @param browserName
     * @param osName
     * @param lowVersion
     * @param highVersion
     *
     * @return true if match
     */
    @CheckReturnValue
    public boolean isMatch(
        final @Nonnull String browserName,
        final @Nonnull String osName,
        final double lowVersion,
        final double highVersion
    )
    {
        if( isMatch( browserName, osName, lowVersion) == false)
        {
            return false;
        }

        return highVersion >= browserVersion;
    }

    /**
     * 1) There is a BUG in IE5.5 that strips off the leading 2048 bytes                                                <BR>
     * 2) Problems with IE6.0 and Windows 2000                                                                          <BR>
     * 3) Having lots of problems with this I'll disable for most of the IE's                                           <BR>
     * 4) <a href="http://blogs.msdn.com/ie/archive/2005/10/31/487509.aspx">Looks like it's all now fixed in IE7</a>    <BR>
     * 5) believe that the compression works for IE6.0 service pack 2 which we can detect by
     *    <a href="http://www.codingforums.com/archive/index.php?t-75770.html">looking for SV1</a>
     *
     * @return TRUE if this browser can support compression
     */
    @CheckReturnValue
    public boolean canHandleHttpCompression()
    {
        if( isBrowserIE())
        {
            if( browserVersion > 6.0)
            {
                return true;
            }
            else if( isEqual( browserVersion, 6.0) )
            {
                // Windows Server 2003 is having issues supporting compression.
                if( isEqual( osVersion, 5.2))
                {
                    return false;
                }

                if( agent != null && agent.contains("SV1"))
                {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    @CheckReturnValue
    private boolean isEqual( final double v1, final double v2)
    {

        return v1 > v2 - 0.001 && v1 < v2 + 0.001;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean canHandleTables()
    {
        return true;
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue
    public boolean doesPDFDisplayInFrame()
    {
        return isMatch( "IE", "WIN", 4.0);
    }

    /**
     * simulate mobile browser.
     * @param isSimulate
     */
    public void setSimulateMobileBrowser(boolean isSimulate)
    {
        isSimulateMobileBrowser = isSimulate;
    }

    /**
     * The string 
     * @return the agent.
     */
    @Override @CheckReturnValue @Nonnull
    public String toString() 
    {
        return getAgent(); 
    }
    
    /**
     * The browser agent.
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getAgent()
    {
        if( rawAgent == null) return "";
        
        return rawAgent;
    }
    
    static
    {
        DISABLE_CDN=CProperties.isDisabled("CDN");
    }
}
