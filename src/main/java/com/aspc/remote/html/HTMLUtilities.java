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

import com.aspc.remote.memory.CacheTable;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CUtilities;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.net.NetUrl;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLException;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 *  HTMLUtilities is a
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       April 19, 2001, 12:06 PM
 *
 */
public final class HTMLUtilities
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLUtilities");//#LOGGER-NOPMD
    private static final int MAX_TEXT_LENGTH = 500;
    private static final int MIN_TEXT_LENGTH = 100;
    private static final int SECONDARY_HTTPS_PORT = 8443;
    private static final int SECONDARY_HTTP_PORT = 8080;
    
    private static final CacheTable CHECKED_HOSTS=new CacheTable( "checked hosts");

    // Constructors
    private HTMLUtilities()
    {
    }
    
    public static URL bestURL(final String checkURL, final boolean tryUpgradeToSSL) throws MalformedURLException, URISyntaxException, IOException
    {
        URL currentURL = new URL(checkURL);
        URL testURL=currentURL;
        
        boolean currentlySSL=currentURL.getProtocol().equalsIgnoreCase("https");
        String file = currentURL.getFile();

        int port = currentURL.getPort();
        
        if( port == -1)
        {
            if( currentlySSL)
            {
                port = 443;
            }
            else
            {
                port = 80;
            }
        }
        
        String tmpHTTPS = CUtilities.getHTTPSPort();
        if( port == 80 || port == 8080 || port == 443 || StringUtilities.isBlank(tmpHTTPS)== false)
        {
            int sPort=443;// The default port for the protocol. http=80, https=443

            if (StringUtilities.isBlank(tmpHTTPS)== false)
            {
                sPort = Integer.parseInt( tmpHTTPS);
            }
//
//            // for developers on Unix put a quick hack
//            if( sPort == 443 && port == 8080)
//            {
//                sPort=SECONDARY_HTTPS_PORT;
//            }

            String tempkey = currentURL.getHost() + ":";
            if(tryUpgradeToSSL || currentlySSL)
            {
                tempkey += sPort;
            }
            else
            {
                tempkey += port;
            }
            Boolean checked =(Boolean)CHECKED_HOSTS.get(tempkey);
            if(checked != null && checked == false)
            {
                tempkey = currentURL.getHost() + ":";
                if(tryUpgradeToSSL||currentlySSL)
                {
                    tempkey += SECONDARY_HTTPS_PORT;
                }
                else
                {
                    tempkey += SECONDARY_HTTP_PORT;
                }
                checked =(Boolean)CHECKED_HOSTS.get(tempkey);
                if(checked != null && checked)
                {
                    if(tryUpgradeToSSL||currentlySSL)
                    {
                        sPort = SECONDARY_HTTPS_PORT;
                        testURL = new URL( "https", currentURL.getHost(), sPort, "/");
                    }
                    else
                    {
                        port = SECONDARY_HTTP_PORT;
                        testURL = new URL("http", currentURL.getHost(), port, "/");
                    }
                }
            }
            if( checked == null)
            {
                String host = currentURL.getHost();
                if( host.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+") == false)
                {                    
                    /**
                     * Host names are cached once looked up. It's only the first search that is troublesome. 
                     */
                    for( int loop=0; loop<3;loop++){
                        try{
                            InetAddress.getAllByName(host);
                            break;
                        }
                        catch( UnknownHostException uhe)
                        {
                            LOGGER.warn( "could not resolve: " + host, uhe);
                            sleep((int)(200 * Math.random()));
                        }
                    }                    
                    
                    if(tryUpgradeToSSL||currentlySSL)
                    {
                        testURL = new URL( "https", host, sPort, "/");
                    }
                    else
                    {
                        testURL = new URL("http", host, port, "/");
                    }
                    URLConnection c1=null;
                    try
                    {
                        c1 = testURL.openConnection();
                        NetUrl.relaxSSLConnection(c1);
                        c1.setConnectTimeout(60000);
                        c1.setReadTimeout(1200000);
                        c1.connect();
                        
                        checked=Boolean.TRUE;
                        String key = currentURL.getHost() + ":" + currentURL.getPort();
                        CHECKED_HOSTS.put( key, checked);
                    }
                    catch( UnknownHostException uhe)
                    {
                        String key = currentURL.getHost() + ":" + currentURL.getPort();
                        
                        CHECKED_HOSTS.put( key, Boolean.FALSE);
                        LOGGER.warn("getURL: unknown host " + testURL + " " + uhe.getMessage());
                    }
                    catch( SSLException sslException)
                    {
                        String key = currentURL.getHost() + ":" + currentURL.getPort();
                        CHECKED_HOSTS.put( key, Boolean.FALSE);
                        LOGGER.warn("getURL: invalid certificate on " + testURL + " " + sslException.getMessage());
                    }
                    catch( ConnectException ce)
                    {
                        String key = currentURL.getHost() + ":" + currentURL.getPort();
                        CHECKED_HOSTS.put( key, Boolean.FALSE);
                        
                        tempkey = currentURL.getHost() + ":";
                        if(tryUpgradeToSSL||currentlySSL)
                        {
                            sPort = SECONDARY_HTTPS_PORT;
                            tempkey += sPort;
                            testURL = new URL( "https", currentURL.getHost(), sPort, "/");
                        }
                        else
                        {
                            port = SECONDARY_HTTP_PORT;
                            tempkey += port;
                            testURL = new URL( "http", currentURL.getHost(), port, "/");
                        }
                        checked = (Boolean)CHECKED_HOSTS.get(tempkey);
                        if(checked == null)
                        {
                            // Re-try with the retrected port.
                            if(tryUpgradeToSSL||currentlySSL)
                            {
                                testURL = new URL( "https", currentURL.getHost(), SECONDARY_HTTPS_PORT, "/");
                                //httpget = new HttpGet( url.toURI());
                                try
                                {
                                    URLConnection c = testURL.openConnection();
                                    NetUrl.relaxSSLConnection(c);
                                    c.setConnectTimeout(60000);

                                    c.connect();
                                    checked=Boolean.TRUE;
                                    sPort = SECONDARY_HTTPS_PORT;
                                    CHECKED_HOSTS.put( currentURL.getHost() + ":" + sPort, checked);
                                }
                                catch( SSLException sslException)
                                {
                                    CHECKED_HOSTS.put( currentURL.getHost() + ":" + sPort, Boolean.FALSE);
                                    LOGGER.warn("getURL: invalid certificate on " + testURL + " " + sslException.getMessage());
                                }
                                catch( ConnectException ce2)
                                {
                                    CHECKED_HOSTS.put( currentURL.getHost() + ":" + sPort, Boolean.FALSE);
                                    LOGGER.warn("No HTTPS listener " + testURL + " " + ce.getMessage());
                                }
                            }
                            else
                            {
                                testURL = new URL( "http", currentURL.getHost(), SECONDARY_HTTP_PORT, "/");
                                URLConnection c2=null;
                                try
                                {
                                    c2 = testURL.openConnection();
                                    NetUrl.relaxSSLConnection(c2);
                                    c2.setConnectTimeout(60000);

                                    c2.connect();
                                    checked=Boolean.TRUE;
                                    port = SECONDARY_HTTP_PORT;
                                    CHECKED_HOSTS.put( currentURL.getHost() + ":" + port, checked);
                                }
                                catch( ConnectException ce2)
                                {
                                    CHECKED_HOSTS.put( currentURL.getHost() + ":" + port, Boolean.FALSE);
                                    LOGGER.warn("No HTTP listener " + testURL + " " + ce.getMessage());
                                }
                                finally
                                {
                                    if( c2 instanceof HttpURLConnection)
                                    {
                                        ((HttpURLConnection)c2).disconnect();
                                    }
                                }
                            }
                        }
                    }
                    catch( IOException e)
                    {
                        LOGGER.warn("Could not connect to " + testURL, e);                    
                    }
                    finally
                    {
                        if( c1 instanceof HttpURLConnection)
                        {
                            ((HttpURLConnection)c1).disconnect();
                        }
                    }
                }
                // IP address only
                else if( currentlySSL == false)
                {
                    // Lets see if we need to jump up from 80 -> 8080.
                    if( port == 80)
                    {
                        testURL = new URL("http", currentURL.getHost(), port, "/");
                        String key = currentURL.getHost() + ":" + currentURL.getPort();
                        HttpURLConnection c1=null;
                        try
                        {
                            c1 = (HttpURLConnection)testURL.openConnection();
                            c1.setConnectTimeout(60000);

                            checked=Boolean.TRUE;
                            CHECKED_HOSTS.put( key, checked);
                        }
                        catch( UnknownHostException sslException)
                        {
                            CHECKED_HOSTS.put( key, Boolean.FALSE);
                            LOGGER.warn("getURL: unknown host " + testURL + " " + sslException.getMessage());
                        }
                        catch( IOException e)
                        {                        
                            CHECKED_HOSTS.put( key, Boolean.FALSE);
                            LOGGER.warn("Could not connect to " + testURL, e);      
                            testURL = new URL("http", currentURL.getHost(), SECONDARY_HTTP_PORT, "/");
                            key = testURL.getHost() + ":" + testURL.getPort();

                            HttpURLConnection c2=null;
                            try
                            {
                                c2 = (HttpURLConnection)testURL.openConnection();
                                c2.setConnectTimeout(60000);
                                c2.connect();

                                checked=Boolean.TRUE;
                                CHECKED_HOSTS.put( key, checked);
                            }
                            catch( UnknownHostException sslException)
                            {
                                CHECKED_HOSTS.put( key, Boolean.FALSE);
                                LOGGER.warn("getURL: unknown host " + testURL + " " + sslException.getMessage());
                            }
                            catch( IOException e2)
                            {
                                CHECKED_HOSTS.put( key, Boolean.FALSE);
                                LOGGER.warn("Could not connect to " + testURL, e2);                    
                            } 
                            finally
                            {
                                if( c2 != null) c2.disconnect();
                            }
                        }
                        finally
                        {
                            if( c1 != null) c1.disconnect();
                        }
                    } 
                }
            }

            if( Boolean.TRUE.equals(checked))
            {
                URL url;

                String protocol=testURL.getProtocol();
                int tPort=testURL.getPort();
                
                if(protocol.equalsIgnoreCase("https"))
                {
                    if( tPort==443) tPort=-1;
                }
                else
                {
                    if( tPort==80) tPort=-1;
                }
                
                url = new URL( protocol, testURL.getHost(), tPort, file);

                return url;
            }
        }

        return currentURL;
    }
    
    private static void sleep( final int ms)
    {        
        try{
            Thread.sleep(ms>0?ms:1);
        }
        catch( InterruptedException ie)
        {
            throw CLogger.rethrowRuntimeExcepton(ie);
        }
    }
    
    /**
     * 
     * @param html 
     * @return the value
     */
    public static String stripTags( String html)
    {
        String temp = html;

        while( true)
        {
            int pos = temp.indexOf( "<");
            if( pos == -1) break;

            int pos2 = temp.indexOf( ">", pos);

            if( pos2 == -1) break;

            String front = temp.substring( 0, pos);

            String end = temp.substring( pos2 + 1);

            temp = front + end;
        }

        return temp;
    }

    // Public Methods
    /**
     * 
     * @param orgName 
     * @return the value
     */
    public static String makeValidName( String orgName)
    {
        String name = orgName;

        name = StringUtilities.replace(name, ":", "_");
        name = StringUtilities.replace(name, " ", "_");
        name = StringUtilities.replace(name, "=", "_");
        name = StringUtilities.replace(name, ".", "_");

        name = StringUtilities.replace(name, "_AT_", "_A_T_");
        name = StringUtilities.replace(name, "_DASH_", "_D_A_S_H_");
        name = StringUtilities.replace(name, "_AND_", "_A_N_D_");
        name = StringUtilities.replace(name, "_ROW_", "_R_O_W_");
        name = StringUtilities.replace(name, "_PERCENT_", "_P_E_R_C_E_N_T_");
        name = StringUtilities.replace(name, "_FETCH_", "_F_E_T_C_H_");
        name = StringUtilities.replace(name, "_END_", "_E_N_D_");

        name = StringUtilities.replace(name, "_BCURLY_", "_B_C_U_R_L_Y_");
        name = StringUtilities.replace(name, "_ECURLY_", "_E_C_U_R_L_Y_");

        name = StringUtilities.replace(name, "[", "_FETCH_");
        name = StringUtilities.replace(name, "@", "_AT_");
        name = StringUtilities.replace(name, "%", "_PERCENT_");
        name = StringUtilities.replace(name, "-", "_DASH_");
        name = StringUtilities.replace(name, "&", "_AND_");
        name = StringUtilities.replace(name, "|", "_ROW_");
        name = StringUtilities.replace(name, "~", "_SEP_");
        name = StringUtilities.replace(name, "]", "_END_");

        name = StringUtilities.replace(name, "{", "_BCURLY_"); //curly brackets
        name = StringUtilities.replace(name, "}", "_ECURLY_");

        return name;
    }

    /**
     * make a valid html id
     * @param id id
     * @return new id
     */
    public static String makeValidHTMLId(final String id)
    {
        String temp = id.toUpperCase().trim();

        temp = StringUtilities.replace(temp, " ", "_");
        temp = StringUtilities.replace(temp, "=", "_EQ_");
        temp = StringUtilities.replace(temp, ":", "_COL_");
        temp = StringUtilities.replace(temp, "%", "_PERCENT_");
        temp = StringUtilities.replace(temp, ".", "_PERIOD_");
        temp = StringUtilities.replace(temp, "[", "_FETCH_");
        temp = StringUtilities.replace(temp, "@", "_AT_");
        temp = StringUtilities.replace(temp, "%", "_PERCENT_");
        temp = StringUtilities.replace(temp, "-", "_DASH_");
        temp = StringUtilities.replace(temp, "&", "_AND_");
        temp = StringUtilities.replace(temp, "^", "_HAT_");
        temp = StringUtilities.replace(temp, "|", "_ROW_");
        temp = StringUtilities.replace(temp, "~", "_SEP_");
        temp = StringUtilities.replace(temp, "]", "_END_");
        temp = StringUtilities.replace(temp, "{", "_BCURLY_");
        temp = StringUtilities.replace(temp, "}", "_ECURLY_");
        temp = temp.replaceAll( "[^_A-Z0-9]", "_");
        if( temp.length() > 0 && temp.matches("[A-Z]+.*")== false)
        {
            temp="ID"+temp;
        }
        while( temp.contains("__"))
        {
            temp=temp.replace("__", "_");
        }
        return temp;
    }

    /**
     * 
     * @param cont
     */
    public static void addNoPopupMsg( HTMLContainer cont)
    {
        addNoPopupMsg( cont, null);
    }
    
    /**
     * 
     * @param cont
     * @param script
     */
    public static void addNoPopupMsg( HTMLContainer cont, String script)
    {
        String ascript = script;
        if( StringUtilities.isBlank( ascript))
        {
            ascript = "javascript:doLoad()";
        }
        
        HTMLAnchor a = new HTMLAnchor( ascript);
        a.addText( "click here");

        /*HTMLAnchor info = new HTMLAnchor( "/docs/help/FAQ/stweb_faqs.htm#popup");
        info.setTarget("Help");
        info.addText("click here");
*/
        String redirect = "Page should redirect. If not ";
        String redirect2 = "Note: This may be caused by Popups being disabled. ";
        String subRedirect2 = "For instructions on changing these settings ";
       
        if(cont instanceof HTMLTable)
        {
            HTMLTable table = (HTMLTable)cont;

            HTMLTable rTable = new HTMLTable();
            HTMLTable pTable1 = new HTMLTable();
            HTMLTable pTable2 = new HTMLTable();
           
            table.setCell(rTable,1,1);
            table.setCell(pTable1,3,1);
            table.setCell(pTable2,4,1);

            rTable.setCell( redirect, 0, 0);                 
            rTable.setCell(a, 0,2);
            
            pTable1.setCell( redirect2,0,0);           
          
            pTable2.setCell( subRedirect2,0,0);
       //     pTable2.setCell(info, 0,2);
           
        }
        else
        {
            cont.addText( "\n\n"+ redirect);
            cont.addComponent( a);

            cont.addText( "\n\n"+ redirect2 + subRedirect2);
        //    cont.addComponent( info);
        }
    }
    
    /**
     * if max is h3, replace all h1 to h3, h2 to h4, h3 to h5 and h4, h5, h6 to h6 in the html
     * if h2 is not found, h1 -> h3, h3 -> h4, h4 -> h5, h5 -> h6
     * if h3 is not found, h1 -> h3, h2 -> h4, h4 -> h5, h5 -> h6
     * @param html html to be processed
     * @param max max header tag, eg: h3
     * @return the value
     */
    public static String downGradeHeader(final String html, final String max)
    {
        if(StringUtilities.isBlank(html))
        {
            return html;
        }
        String temp = html;
        int dest = Integer.parseInt(max.substring(1));
        ArrayList<Integer> skipList = new ArrayList<>();
        for(int i = 1;i <= 5;i++)
        {
            String t = temp.replaceAll("\r+\n+|\n+\r+|\n+|\r+", " ");
            if(t.matches(".*<[hH]{1}" + i + "\\s.*>") == false && t.matches(".*<[hH]{1}" + i + ">.*") == false)
            {
                skipList.add(i);
            }
        }
        for(int i = 5; i >= 1;i--)
        {
            int skip = 0;
            for(int s : skipList)
            {
                if(s < i)
                {
                    skip++;
                }
            }
            int out = i + dest - 1 - skip;
            if(out > 6)
            {
                out = 6;
            }
            if(out <= i)
            {
                continue;
            }
            temp = temp.replaceAll("<[hH]{1}" + i + "\\s", "<h" + out + " ");
            temp = temp.replaceAll("<[hH]{1}" + i + ">", "<h" + out + ">");
            temp = temp.replaceAll("</\\s*[hH]{1}" + i + ">", "</h" + out + ">");
        }
        return temp;
    }
    
    /**
     * Gets the summary of an article
     * 
     * @param fullhtml HTML code
     * @param limit limit, if a number is given, gen return only this number of pure text, if limit is null, return html code
     * @return the value
     */
    public static String trimHTML(final String fullhtml, final String limit)
    {
        String html = fullhtml;
        org.jsoup.nodes.Document doc;
        try
        {
            doc = Jsoup.parse(html);
        }
        catch( Exception e )
        {
            LOGGER.warn( fullhtml, e);
            throw CLogger.rethrowRuntimeExcepton(e);  
        }
        
        if(StringUtilities.notBlank(limit))
        {
            String limitStr = limit;
            try
            {
                int intLimit = Integer.parseInt(limitStr);
                if(intLimit >= 0)
                {
                    html = doc.text();
                    //don't cut a word into halves, and append "..." only when where are more contents
                    html = html.replaceAll("(?<=.{" + intLimit + "})\\b.{4,}", "...");
                }
            }
            catch(NumberFormatException e)
            {
                html = doc.text();
                //TODO handle some advanced limit such as "first 2 paragraph", "last 2 section" etc
            }
        }
        else
        {
            int count = getHeaderNumber(doc);
            if(count > 1)
            {
                //get the first heading
                int number = 1;
                keepSections(doc, number);
                String text = doc.text();
                while(number <= count && text.length() < MIN_TEXT_LENGTH)
                {
                    //if the text is too short, get one more section
                    doc = Jsoup.parse(html);//always use original document
                    number++;
                    keepSections(doc, number);
                    text = doc.text();
                }
                if(text.length() > MAX_TEXT_LENGTH)
                {
                    //if the last section is too long, cut it into small section
                    keepText(doc, MAX_TEXT_LENGTH);
                }
            }
            else
            {
                //get every thing within the MAX_TEXT_LENGTH
                String text = doc.text();
                if(text.length() > MAX_TEXT_LENGTH)
                {
                    keepText(doc, MAX_TEXT_LENGTH);
                }
            }
            if(doc.body() == null)
            {
                html = "";
            }
            else
            {
                html = doc.body().html();
            }
        }
        return html;
    }
    
    private static List<Node> getAllNode(final Node node)
    {
        List<Node> list = node.childNodes();
        List<Node> result = new ArrayList<>();
        for(Node n : list)
        {
            result.add(n);
            List<Node> children = n.childNodes();
            if(children.isEmpty() == false)
            {
                result.addAll(getAllNode(n));
            }
        }
        return result;
    }
    
    /**
     * keep at least number of chars of text
     * @param doc the document
     * @param number 
     */
    private static void keepText(final org.jsoup.nodes.Document doc, int number)
    {
        int diff = doc.text().length() - number;
        List<Node> list = getAllNode(doc);
        for(int i = list.size() - 1;i >= 0;i--)
        {
            Node n = list.get(i);
            if(n instanceof TextNode)
            {
                String text = ((TextNode)n).text();
                int count = text.length();
                if(count <= diff)
                {
                    diff -= count;
                    n.remove();
                    if(count == 0)
                    {
                        break;
                    }
                }
                else
                {
                    text = text.replaceAll("(?<=.{" + (count - diff + 1) + "})\\b.*", "");
                    ((TextNode)n).text(text);
                    break;
                }
            }
            else
            {
                n.remove();
            }
        }
    }
    
    /**
     * keep number of sections and remove the rest elements
     * @param doc document to be processed
     * @param number number of sections to be kept
     */
    private static void keepSections(final org.jsoup.nodes.Document doc, int number)
    {
        boolean foundLastHeader = false;
        int c = 1;
        Elements all = doc.getAllElements();
        Iterator<Element> it = all.iterator();
        while(it.hasNext())
        {
            Element ele = it.next();
            if(foundLastHeader == false && ele.tagName().toLowerCase().matches("h[1-6]{1}"))
            {
                c++;
                if(c > number)
                {
                    foundLastHeader = true;
                }
            }
            if(foundLastHeader)
            {
                ele.remove();
            }
        }
    }
    
    private static int getHeaderNumber(final org.jsoup.nodes.Document doc)
    {
        int count = 0;
        Elements all = doc.getAllElements();
        Iterator<Element> it = all.iterator();
        while(it.hasNext())
        {
            Element ele = it.next();
            if(ele.tagName().toLowerCase().matches("h[1-6]{1}"))
            {
                count++;
            }
        }
        return count;
    }
}
