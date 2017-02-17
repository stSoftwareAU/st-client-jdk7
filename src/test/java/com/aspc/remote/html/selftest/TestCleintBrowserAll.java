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
package com.aspc.remote.html.selftest;

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.timer.StopWatch;
import java.io.FileNotFoundException;
import java.util.HashSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;
import org.json.JSONObject;

/**
 *  Check all known browsers.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 */
public class TestCleintBrowserAll extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.selftest.TestCleintBrowserAll");//#LOGGER-NOPMD
    /**
     * Creates new VirtualDBTestUnit
     * @param name the test name
     */
    public TestCleintBrowserAll(String name)
    {
        super( name);
    }

    /**
     * the main line
     * @param args the args
     */
    public static void main(String[] args)
    {
        Test test=suite();
//        test =TestSuite.createTest(TestClientBrowser.class, "testIsWebCrawler");
        TestRunner.run(test);
    }

    /**
     * the test suite
     * @return the suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestCleintBrowserAll.class);
        return suite;
    }

    private boolean shouldProcess(final String agent, final HashSet<String> uniqueAgents)
    {
        if(
            agent.trim().matches("") ||
            agent.matches("-") ||
            agent.matches("[0-9]+") ||
            agent.startsWith("mpatible") ||
            agent.startsWith("MSIE") ||
            agent.startsWith(";") ||
            agent.startsWith("(") ||
            agent.startsWith("T00WT00W3") ||
            agent.startsWith("User-Agent") ||
            agent.startsWith("Safari") ||
            agent.startsWith("Test") ||
            agent.startsWith("PHP") ||
            agent.toLowerCase().startsWith("python") ||
            agent.startsWith("@") ||
            agent.startsWith("HTTP") ||
            agent.startsWith("http") ||
            agent.startsWith("}") ||
            agent.startsWith("[") ||
            agent.contains("HRCrawler") ||
            agent.contains("MSIE or Firefox") ||
            agent.contains("Win128") ||

            (agent.contains(" MSIE") && agent.contains("Chrome/")) ||
            (agent.contains("iPad") && agent.contains("iPhone"))
        )
        {
            return false;
        }

        String simpliedAgent = agent.trim().toUpperCase();
        while( simpliedAgent.contains("  "))
        {
            simpliedAgent=simpliedAgent.replace("  ", " ");
        }
        int pos=simpliedAgent.indexOf(") APPLEWEBKIT/");
        if(pos!=-1)
        {
            simpliedAgent=simpliedAgent.substring(0,pos);
        }
        simpliedAgent=simpliedAgent.replaceAll("\\)", ";");
        simpliedAgent=simpliedAgent.replaceAll(";", "; ");
        if( simpliedAgent.trim().endsWith(";")==false) simpliedAgent+=";";
        simpliedAgent=simpliedAgent.replaceAll(".NET CLR [0-9\\.]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(".NET[0-9\\.A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll("MICROSOFT OUTLOOK [0-9\\.]+;", "");
        simpliedAgent=simpliedAgent.replaceAll("MSOFFICE [0-9\\.]+[;\\)]", "");
        simpliedAgent=simpliedAgent.replaceAll("MEDIA CENTER PC [0-9\\.]+;", "");
        simpliedAgent=simpliedAgent.replaceAll("MS-OFFICE;", "");
        simpliedAgent=simpliedAgent.replaceAll(" GWX:[A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" CMDTDFJS;", "");
        simpliedAgent=simpliedAgent.replaceAll(" CMDTDF;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MDDS;", "");
        simpliedAgent=simpliedAgent.replaceAll(" FDM;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MAAU;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MATP;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MATP;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MASM;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MDDC;", "");
        simpliedAgent=simpliedAgent.replaceAll(" F[0-9]+J;", "");
        simpliedAgent=simpliedAgent.replaceAll(" BRI/2;", "");
        simpliedAgent=simpliedAgent.replaceAll(" CHROMEFRAME/[0-9\\.]*;", "CHROMEFRAME/x");
        simpliedAgent=simpliedAgent.replaceAll(" MCAFEE;", "");
        simpliedAgent=simpliedAgent.replaceAll(" HPNTDF;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MDD[0-9A-Z]*;", "");
//        simpliedAgent=simpliedAgent.replaceAll(" MALCJS;", "");
        simpliedAgent=simpliedAgent.replaceAll(" MA[MSL][0-9A-Z]*;", "");
        simpliedAgent=simpliedAgent.replaceAll(" TAJ[0-9A-Z]*;", "");
        simpliedAgent=simpliedAgent.replaceAll(" ASK[\\./0-9A-Z]*;", "");
        simpliedAgent=simpliedAgent.replaceAll(" (125|360|AD_|AGD|JACK|RDS|SRC|CRIOS|MOBILE|ASJ|A&R|BTR|CNS|DTS|NP0|GIL|GSA|SVD|CPN|DEAL|MAN|MAA|FBS|HPD|FUN|YIE|SPA|SYN|CIB|ZAN|H9P|MAA|MSN|MS-|NSA|ORI|POW|LUMIA|TNJ|MRA|SBJ|ENU|ASU|BOI|YTB|OFFICE|NOK|CMN|HPN|ZUN|CREAT|MAT|LCJ)[=_ \\./0-9A-Z\\(]*;", "");
//        simpliedAgent=simpliedAgent.replaceAll(" MAM[0-9A-Z]*;", "");
        simpliedAgent=simpliedAgent.replaceAll(" LEN[0-9]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" LEN[0-9]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" EI[0-9A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" EN-[A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" ZH-[A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" ENA[0-9A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" RV:[0-9\\.]+;", "");

        simpliedAgent=simpliedAgent.replaceAll("SLCC[0-9]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" INFOPATH[0-9\\. A-Z]+;", "");
        simpliedAgent=simpliedAgent.replaceAll("TABLET PC [0-9\\.]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" GTB[ 0-9\\.]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" ESOBISUBSCRIBER[ 0-9\\.]+;", "");
        simpliedAgent=simpliedAgent.replaceAll(" SEARCHTOOLBAR[ 0-9\\.]+;", "");

        while( simpliedAgent.contains("  "))
        {
            simpliedAgent=simpliedAgent.replace("  ", " ");
        }
        while( simpliedAgent.contains("; ;"))
        {
            simpliedAgent=simpliedAgent.replace("; ;", "; ");
        }
        simpliedAgent=simpliedAgent.trim();

        if( uniqueAgents.add(simpliedAgent))
        {
            return true;
        }
        return false;

    }

    /**
     * test in not mobile agent
     * @throws Exception a serious problem.
     */
    public void testScanAgents() throws Exception
    {
        String data = FileUtil.readFile(System.getProperty("SRC_DIR") + "/com/aspc/remote/html/selftest/unique_agents.txt");
        String agents[]=data.split("\n");
        HashSet<String> uniqueAgents=new HashSet();
        StopWatch sw=new StopWatch();
        for (String agent : agents)
        {
            if( sw.durationMS() > 5 * 60 * 1000) break;

            ClientBrowser browser=new ClientBrowser( agent);
            if( shouldProcess(agent, uniqueAgents)==false) continue;
            LOGGER.info( "Browser: " + browser);
            sw.start();
            Response res=ReST.builder("http://www.useragentstring.com/")
                    .setMinCachePeriod("3 months")
                    .setErrorCachePeriod("1 months")
                    .addParameter("uas", agent)
                    .addParameter("getJSON", "all")
                    .getResponse();
            sw.stop();
            try
            {
                res.checkStatus();
            }
            catch( FileNotFoundException | ReSTException e)
            {
                LOGGER.warn( agent, e);
                continue;
            }
            JSONObject json = res.getContentAsJSON();
            String info=agent + "->" + json.toString(2);
            String agentName=json.getString("agent_name");
            String osName=json.getString("os_name");
            String osType=json.getString("os_type");
            String agentType=json.getString("agent_type");
            if( agent.contains("AdsBot-Google"))
            {
                agentType="Crawler";
            }

            switch( agentType)
            {
                case "Crawler":
                    assertTrue( info,browser.isWebCrawler());
                    break;
                case "Browser":
                    if( browser.isWebCrawler())
                    {
                        if( agent.matches(".*(Googlebot|bingbot|Baiduspider|WebCrawler).*") == false)
                        {
                            ClientBrowser tmpBrowser=new ClientBrowser( agent);
                            fail( info + " agentType=" + tmpBrowser.getBrowserName());
                        }
                    }
                    break;
                case "Offline Browser":
                case "LinkChecker":
                case "Validator":
                case "unknown":
                    break;
                default:
                    LOGGER.info( "agentType: " + agentType +" <- " + agent);
            }

            switch( agentName)
            {
                case "Safari":
                    if(browser.isBrowserSafari() ==false)
                    {
                        ClientBrowser tmpBrowser=new ClientBrowser( agent);
                        fail( info + " os=" + tmpBrowser.getBrowserName());
                    }
                    break;
                case "Firefox":
                    assertTrue( info,browser.isBrowserFirefox());
                    break;
                case "MSIE":
                case "Internet Explorer":
                    if( browser.isBrowserIE()==false)
                    {
                        ClientBrowser tmpBrowser=new ClientBrowser( agent);
                        fail( info + " browser=" + tmpBrowser.getBrowserName());
                    }
                    break;
                case "unknown":
                case "W3C_Validator":
                case "Java":
                    break;
                default:
                    LOGGER.info( "agentName: " + agentName +" <- " + agent);
            }

            switch( osName)
            {
                case "iPhone OS":
                    if( agent.contains("iPad"))
                    {
                        assertTrue( info,browser.isBrowserIPad());
                    }
                    else
                    {
                        assertTrue( info,browser.isBrowserIPhone());
                    }
                    break;
                case "Android":
                case "android":
                    if( browser.osAndroid()==false)
                    {
                        ClientBrowser tmpBrowser=new ClientBrowser( agent);
                        fail( info + " browser=" + tmpBrowser.getBrowserName());
                    }
                    break;
                case "Windows Server 2003":
                case "Windows 8":
                case "Windows 7":
                case "Windows NT":
                case "Windows XP":
                case "Windows 98":
                case "Windows 95":
                    assertTrue( info,browser.osWindows());
                    break;
                case "Linux":
                    assertTrue( info,browser.osLinux());
                    break;
                case "Darwin":
                case "unknown":
                    break;
                default:
                    LOGGER.info( "osName: " + osName +" <- " + agent);

            }
            switch( osType)
            {
                case "Macintosh":
                    if(browser.osMAC() ==false)
                    {
                        ClientBrowser tmpBrowser=new ClientBrowser( agent);
                        fail( info + " os=" + tmpBrowser.getOSName());
                    }
                    break;
                case "Android":
                case "android":
                    assertTrue( info,browser.osAndroid());
                    break;
                case "Windows":
                    if(browser.osWindows() ==false)
                    {
                        ClientBrowser tmpBrowser=new ClientBrowser( agent);
                        fail( info + " os=" + tmpBrowser.getOSName());
                    }
                    break;
                case "Linux":
                    if(browser.osLinux() ==false)
                    {
                        ClientBrowser tmpBrowser=new ClientBrowser( agent);
                        fail( info + " os=" + tmpBrowser.getOSName());
                    }
                    break;
                case "unknown":
                    break;
                default:
                    LOGGER.info( "osType: " + osType +" <- " + agent);

            }
        }
    }

}
