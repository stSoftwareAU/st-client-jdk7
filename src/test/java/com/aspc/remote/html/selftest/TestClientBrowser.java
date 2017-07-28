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
package com.aspc.remote.html.selftest;

import com.aspc.remote.html.ClientBrowser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *  check the detection of browsers.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  
 *  @since          October 22, 2004
 */
public class TestClientBrowser extends TestCase
{
    /**
     * Creates new VirtualDBTestUnit
     * @param name the test name
     */
    public TestClientBrowser(String name)
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
        TestSuite suite = new TestSuite(TestClientBrowser.class);
        return suite;
    }


     /**
      * test in not mobile agent
     * @throws Exception a serious problem.
      */
    public void testIsNotMobileAgent() throws Exception
    {
        String agents[]={
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727; OfficeLiveConnector.1.3; OfficeLivePatch.0.0)",
            "Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10.5; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6",
            "Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10.4; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6",
            "Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; en) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10",
            "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.9) Gecko/20100402 Ubuntu/9.10 (karmic) Firefox/3.5.9",
            "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3 (.NET CLR 3.5.30729)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; GTB6.4; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)",
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);
            assertFalse( "should not be a mobile " + agent, browser.formMobile());
        }
    }

    /**
     * test in not mobile agent
     * @throws Exception a serious problem.
     */
    public void testAndroid() throws Exception
    {
        String agents[]={
            "Mozilla/5.0 (Linux; Android 6.0.99; Nexus 6P Build/NPD56N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.90 Mobile Safari/537.36"  
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);
            assertTrue( "should be a mobile " + agent, browser.formMobile());
            assertTrue( "should be a Chrome " + agent, browser.isBrowserChrome());
            assertTrue( "should be a Android " + agent, browser.osAndroid());
        }
    }
    
    /**
     * check for DAV
     * @throws Exception a serious problem
     */
    public void testIsDAV() throws Exception
    {
        String davAgents[] = {
            "Microsoft Data Access Internet Publishing Provider DAV 1.1"
        };
        
        for (String agent : davAgents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserDAV() == false)
            {
                fail( "Should be DAV: " + agent);
            }
        }
    }

    /**
     * check for mobile phones                                                  <BR>
     *
     * http://en.wikipedia.org/wiki/List_of_user_agents_for_mobile_phones
     *
     * @throws Exception a serious problem
     */
    public void testIsMobile() throws Exception
    {
        String mobileAgents[] = {

            /** problem phone */
            "problem phone: MOZILLA/5.0 (LINUX; U; ANDROID 4.3; EN-CA; SGH-I747M BUILD/JSS15J) APPLEWEBKIT/534.30 (KHTML, LIKE GECKO) VERSION/4.0 MOBILE SAFARI/534.30 GSA/3.1.24.941712.ARM",

            /** Nigel's Galaxy S3 phone */
            "Android Opera: Opera/9.80 (Android 4.0.4; Linux; Opera Mobi/ADR-1207201819; U; en) Presto/2.10.254 Version/12.00",
            "Android Chrome: Mozilla/5.0 (Linux; Android 4.0.4; GT-I9300T Build/IMM76D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19",
            "Android Firefox: Mozilla/5.0 (Android; Mobile; rv:15.0) Gecko/15.0 Firefox/15.0",
            "Android Dolphin: Mozilla/5.0 (Linux; U; Android 4.0.4; en-au; GT-I9300T Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
            "Android Internet: Mozilla/5.0 (Linux; U; Android 4.0.4; en-au; GT-I9300T Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",           
            /**** Apple Inc. ****/
            "Apple iPhone: Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/1A542a Safari/419.3",
            "iPod Touch: Mozila/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3",
            "Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5",
            /**** BenQ-Siemens (Openwave) ****/
            //"S68: SIE-S68/36 UP.Browser/7.1.0.e.18 (GUI) MMP/2.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"EF81: SIE-EF81/58 UP.Browser/7.0.0.1.181 (GUI) MMP/2.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",

            /**** BlackBerry ****/
            "BlackBerry 7100i: BlackBerry7100i/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/103",
            "BlackBerry 7130e: BlackBerry7130e/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/104",
            "BlackBerry 7250: BlackBerry7250/4.0.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "BlackBerry 7230: BlackBerry7230/3.7.0",
            "BlackBerry 7520: BlackBerry7520/4.0.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "BlackBerry 7730: BlackBerry7730/3.7.0",
            "BlackBerry 8100: Mozilla/4.0 BlackBerry8100/4.2.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/100",
            "BlackBerry 8130: BlackBerry8130/4.3.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/109",
            "BlackBerry 8310: BlackBerry8310/4.2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/121",
            "BlackBerry 8320: BlackBerry8320/4.3.1 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "BlackBerry 8700: BlackBerry8700/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/100",
            //"BlackBerry 8703: Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11",
            "BlackBerry 8703e: BlackBerry8703e/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/105",
            "BlackBerry 8820: BlackBerry8820/4.2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102",
            "BlackBerry 8830: BlackBerry8830/4.2.2 Profile/MIDP-2.0 Configuration/CLOC-1.1 VendorID/105",
            "BlackBerry 9000: BlackBerry9000/4.6.0.65 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102",

            /**** Google ****/
            //"Android SDK 1.5r3: Mozilla/5.0 (Linux; U; Android 1.5; de-; sdk Build/CUPCAKE) AppleWebkit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1",

            /**** HTC ****/
            "8500:  HTC-8500/1.2 Mozilla/4.0 (compatible; MSIE 5.5; Windows CE; PPC; 240x320)",
            //"8500:  HTC-8500/1.2 Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) UP.Link/6.3.1.17.0",
            "P3650: HTC_P3650 Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6)",
            "P3450: Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12) PPC; 240x320; HTC P3450; OpVer 23.116.1.611",
            //"S710:  Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) SP; 240x320; HTC_S710/1.0 ...",
            "Hero:  Mozilla/5.0 (Linux; U; Android 1.5; en-za; HTC Hero Build/CUPCAKE) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1",

            /**** LG Electronics ****/
/*
            "LG U880:   LG/U880/v1.0",
            "LG B2050:  LG-B2050 MIC/WAP2.0 MIDP-2.0/CLDC-1.0",
            "LG C1100:  LG-C1100 MIC/WAP2.0 MIDP-2.0/CLDC-1.0",
            "LG CU8080: LGE-CU8080/1.0 UP.Browser/4.1.26l",
            "LG G1800:  LG-G1800 MIC/WAP2.0 MIDP-2.0/CLDC-1.0",
            "LG G210:   LG-G210/SW100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G220:   LG-G220/V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G232:   LG-G232/V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G262:   LG-G262/V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G5200:  LG-G5200 AU/4.10",
            "LG G5600:  LG-G5600 MIC/WAP2.0 MIDP-2.0/CLDC-1.0",
            //"LG G610:   LG-G610 V100 AU/4.10 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            //"LG G622:   LG-G622/V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            "LG G650:   LG-G650 V100 AU/4.10 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            //"LG G660:   LG-G660/V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G672:   LG-G672/V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G682:   LG-G682 /V100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G688:   LG-G688 MIC/V100/WAP2.0 MIDP-2.0/CLDC-1.0",
            "LG G7000:  LG-G7000 AU/4.10",
            "LG G7050:  LG-G7050 UP.Browser/6.2.2 (GUI) MMP/1.0 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            "LG G7100:  LG-G7100 AU/4.10 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            "LG G7200:  LG-G7200 UP.Browser/6.2.2 (GUI) MMP/1.0 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            //"LG G822:   LG-G822/SW100/WAP2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0",
            //"LG G850:   LG-G850 V100 UP.Browser/6.2.2 (GUI) MMP/1.0 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            //"LG G920:   LG-G920/V122/WAP2.0 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            //"LG G922:   LG-G922 Obigo/WAP2.0 MIDP-2.0/CLDC-1.1",
            //"LG G932:   LG-G932 UP.Browser/6.2.3(GUI)MMP/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "LG L1100:  LG-L1100 UP.Browser/6.2.2 (GUI) MMP/1.0 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            "LG MX8700: LGE-MX8700/1.0 UP.Browser/6.2.3.2 (GUI) MMP/2.0",
            "LG T5100:  LG-T5100 UP.Browser/6.2.3 (GUI) MMP/1.0 Profile/MIDP-1.0 Configuration/CLDC-1.0",
            "LG U8120:  LG/U8120/v1.0",
            "LG U8130:  LG/U8130/v1.0",
            "LG U8138:  LG/U8138/v2.0",
            "LG U8180:  LG/U8180/v1.0",
            "LG VX9100: LGE-VX9100/1.0 UP.Browser/6.2.3.2 (GUI) MMP/2.0",
*/
            /**** Motorola ****/
      //      "Motorola V3: MOT-V3r/08.BD.43R MIB/2.2.1 Profile/MIDP-2.0 Configuration/CLDC-1.1",
     //       "Motorola K1: MOT-K1/08.03.08R MIB/BER2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 EGE/1.0",
            //"Motorola L6: MOT-L6/0A.52.2BR MIB/2.2.1 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"Motorola V9: MOT-MOTORAZRV9/4 BER2.2 Mozilla/4.0 (compatible; MSIE 6.0; 14003181) Profile/MIDP-2.0 Configuration/CLDC-1.1 Op! era 8.00 [en] UP.Link/6.3.0.0.0",
            //"Motorola V3xx: MOT-RAZRV3xx/96.64.21P BER2.2 Mozilla/4.0 (compatible; MSIE 6.0; 11003002) Profile/MIDP-2.0 Configuration/CLDC-1.1 Opera 8.00 [en] UP.Link/6.3.0.0.0",
            //"Motorola V9x: MOT-MOTORAZRV9x/9E.03.15R BER2.2 Mozilla/4.0 (compatible; MSIE 6.0; 13003337) Profile/MIDP-2.0 Configuration/CLDC-1.1 Opera 8.60 [en] UP.Link/6.3.0.0.0",
            //"Motorola Z9: MOT-MOTOZ9/9E.01.03R BER2.2 Mozilla/4.0 (compatible; MSIE 6.0; 11003002) Profile/MIDP-2.0 Configuration/CLDC-1.1 Opera 8.60 [en] UP.Link/6.3.0.0.0",

            /**** Nokia ****/
            //"Nokia 2610: Nokia2610/2.0 (07.04a) Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.1.20.0",
            "Nokia 5300: Nokia5300/2.0 (05.51) Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"Nokia 6030: Nokia6030/2.0 (y3.44) Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"Nokia 6230i: Nokia6230i/2.0 (03.40) Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "Nokia 6280: Nokia6280/2.0 (03.60) Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"Nokia 6650: Nokia6650d-1bh/ATT.2.15 Mozilla/5.0 (SymbianOS/9.3; U; [en]; Series60/3.2; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413",
            //"Nokia E51-1: Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaE51-1/220.34.37; Profile/MIDP-2.0 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413",
            //"Nokia E71x: NokiaE71x/ATT.03.11.1 Mozilla/5.0 SymbianOS/9.3; U; [en]; Series60/3.2; "+
            //                                    "Profile/MIDP-2.1 Configuration/CLDC-1.1 AppleWebKit/413 KHTML, like Gecko) Safari/413 UP.Link/6.3.0.0.0",
            //"Nokia N70: NokiaN70-1/5.0616.2.0.3 Series60/2.8 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"Nokia N75: NokiaN75-3/3.0 (1.0635.0.0.6); SymbianOS/9.1 Series60/3.0 Profile/MIDP-2.0 Configuration/CLDC-1.1) UP.Link/6.3.0.0",
            //"Nokia N80: NokiaN80-1/3.0(4.0632.0.10) Series60/3.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
           // "Nokia N90: NokiaN90-1/5.0607.7.3 Series60/2.8 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"Nokia N95: Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/11.0.026; Profile MIDP-2.0 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413",

            /**** Samsung ****/

            //"Samsung A737: SAMSUNG-SGH-A737/UCGI3 SHP/VPP/R5 NetFront/3.4 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1 UP.Link/6.3.1.17.0",
            //"Samsung A737: SAMSUNG-SGH-A737/1.0 SHP/VPP/R5 NetFront/3.3 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1 UP.Link/6.3.0.0.0",
            //"Samsung A767: SAMSUNG-SGH-A767/A767UCHG2 SHP/VPP/R5 NetFront/3.4 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1 UP.Link/6.3.0.0.0",
           // "Samsung A867: SAMSUNG-SGH-A867/A867UCHG5 SHP/VPP/R5 NetFront/3.4 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1 UP.Link/6.3.0.0.0",
           // "Samsung A877: SAMSUNG-SGH-A877/A877UCHK1 SHP/VPP/R5 NetFront/3.5 SMM-MMS/1.2.0 profile/MIDP-2.1 configuration/CLDC-1.1 UP.Link/6.3.0.0.0",
            "Samsung D600: SAMSUNG-SGH-D600/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Browser/6.2.3.3.c.1.101 (GUI) MMP/2.0",
            //"Samsung Z720: SAMSUNG-SGH-Z720/1.0 SHP/VPR/R5 NetFront/3.3 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1",
           // "Samsung SGH-E250: SAMSUNG-SGH-E250/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Browser/6.2.3.3.c.1.101 (GUI) MMP/2.0",
            //"Samsung SGH-U600: SEC-SGHU600/1.0 NetFront/3.2 Profile",
            "Samsung SGH-U900: SAMSUNG-SGH-U900-Vodafone/U900BUHD6 SHP/VPP/R5 NetFront/3.4 Qtv5.3 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1",
            //"Samsung SGH-i900 Omnia: SAMSUNG-SGH-i900/1.0 Opera 9.5",
            //"Samsung i617: SAMSUNG-SGH-I617/1.0 Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12) UP.Link/6.3.0.0.0",
            //"Samsung i7500 Galaxy: Mozilla/5.0 (Linux; U; Android 1.5; de-de; Galaxy Build/CUPCAKE) AppleWebkit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1",
            //"Samsung Z720: Opera/9.50 (J2ME/MIDP; Opera Mini/4.1.11355/542; U; en)",
            "Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
            
            /**** SonyEricsson ****/

            //"SonyEricsson K510i: SonyEricssonK510i/R4CJ Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1",
           // "SonyEricsson K550i: SonyEricssonK550i/R8BA Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "SonyEricsson K610i: SonyEricssonK610i/R1CB Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"SonyEricsson K630i: SonyEricssonK630i/R1CA Browser/NetFront/3.4 Profile/MIDP-2.1 Configuration/CLDC-1.1",
            "SonyEricsson K700: SonyEricssonK700/R1A Profile/MIDP-1.0 MIDP-2.0 Configuration/CLDC-1.1",
            "SonyEricsson K750i: SonyEricssonK750i/R1CA Browser/SEMC-Browser/4.2 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "SonyEricsson K800i: SonyEricssonK800i/R8BF Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1",

            "SonyEricsson W800i: SonyEricssonW800i/R1AA Browser/SEMC-Browser/4.2 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            //"SonyEricsson W810i: SonyEricssonW810i/MIDP-2.0 Configuration/CLDC-1.1",
            //"SonyEricsson W900i: SonyEricssonW900i/R5AH Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "SonyEricsson W995i: SonyEricssonW995/R1DB Browser/NetFront/3.4 Profile/MIDP-2.1 Configuration/CLDC-1.1 JavaPlatform/JP-8.4.1",

            //"SonyEricsson Z500a: SonyEricssonZ500a/R1A SEMC-Browser/4.0.1 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.1.20.0",

            /** Pocket PC */
          //  "Mozilla/4.0 (compatible; MSIE 4.01; Windows CE; PPC; 240x320)",

        };
        for (String temp : mobileAgents) 
        {
            int pos = temp.indexOf(':');
            String name = "browser";
            String agent = temp;

            if(pos != -1)
            {
                name = temp.substring(0, pos).trim();
                agent = temp.substring(pos + 1).trim();
            }
            
            ClientBrowser browser=new ClientBrowser( agent);
            if( browser.formMobile() == false)
            {
                fail( name + " should be mobile phone: " + agent);
            }

//            assertFalse(name + " which is a mobile browser should *NOT* be multi-document interface", browser.isMDI());
            assertTrue(name + " should be a mobile", browser.formMobile());
            assertFalse(name + " can not be a mobile and a desktop", browser.formDesktop());
            assertFalse(name + " can not be a mobile and a tablet", browser.formTablet());                 
        }
    }


    public void testCanHandleGWT()
    {
        String mobileAgents[][]= {

            /** Nigel's Galaxy S3 phone */
            {"Android Opera","Opera/9.80 (Android 4.0.4; Linux; Opera Mobi/ADR-1207201819; U; en) Presto/2.10.254 Version/12.00"},
            {"Android Chrome","Mozilla/5.0 (Linux; Android 4.0.4; GT-I9300T Build/IMM76D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"},
            {"Android Firefox","Mozilla/5.0 (Android; Mobile; rv:15.0) Gecko/15.0 Firefox/15.0"},
            {"Android Dolphin","Mozilla/5.0 (Linux; U; Android 4.0.4; en-au; GT-I9300T Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"},
            {"Android Internet","Mozilla/5.0 (Linux; U; Android 4.0.4; en-au; GT-I9300T Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"},
            {"Android Tablet","Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-P7500 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30"},
        };
        
        for (String[] mobileAgent : mobileAgents) 
        {
            String name = mobileAgent[0];
            String agent = mobileAgent[1];
            ClientBrowser browser=new ClientBrowser( agent);
            if( browser.canHandleGWT() == false)
            {
                fail( name + " should handle GWT: " + agent);
            }
            if( browser.canHandleDHTML() == false)
            {
                fail( name + " should handle DHTML: " + agent);
            }
            if( browser.canHandleTables() == false)
            {
                fail( name + " should handle TABLES: " + agent);
            }
        }
    }


    public void testIsAndroid()
    {
        String mobileAgents[][]= {

            /** Nigel's Galaxy S3 phone */
           // {"Android Safari","Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-P7500 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30"},
            {"Android Opera","Opera/9.80 (Android 4.0.4; Linux; Opera Mobi/ADR-1207201819; U; en) Presto/2.10.254 Version/12.00"},
            {"Android Chrome","Mozilla/5.0 (Linux; Android 4.0.4; GT-I9300T Build/IMM76D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"},
            {"Android Firefox","Mozilla/5.0 (Android; Mobile; rv:15.0) Gecko/15.0 Firefox/15.0"},
            {"Android Dolphin","Mozilla/5.0 (Linux; U; Android 4.0.4; en-au; GT-I9300T Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"},
            {"Android Internet","Mozilla/5.0 (Linux; U; Android 4.0.4; en-au; GT-I9300T Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"},
            {"Android Internet","Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"}
        };
        
        for (String[] mobileAgent : mobileAgents) 
        {
            String name = mobileAgent[0];
            String agent = mobileAgent[1];
            ClientBrowser browser=new ClientBrowser( agent);
            if( browser.isBrowserAndroidMobile() == false)
            {
                //          new ClientBrowser( agent);
                fail( name + " is an Android mobile: " + agent);
            }
        }
        
        String tabletAgents[][] = {
            {"Android Chrome", "Mozilla/5.0 (Linux; Android 4.4.2; Android SDK built for x86 Build/KK) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Safari/537.36"}
        };
        
        for (String[] mobileAgent : tabletAgents) 
        {
            String name = mobileAgent[0];
            String agent = mobileAgent[1];
            ClientBrowser browser=new ClientBrowser( agent);
            if( browser.osAndroid() == false)
            {
                fail( name + " is an Android: " + agent);
            }
            if(browser.formTablet() == false)
            {
                fail( name + " is an Tablet: " + agent);
            }
        }
    }


    /**
     * Check we detect that it's a blackberry                                               <BR>
     *
     * http://en.wikipedia.org/wiki/List_of_user_agents_for_mobile_phones
     *
     * @throws Exception a serious problem
     */
    public void testIsBlackBerry() throws Exception
    {
        String mobileAgents[] = {

            /**** BlackBerry ****/
            "BlackBerry 7100i: BlackBerry7100i/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/103",
            "BlackBerry 7130e: BlackBerry7130e/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/104",
            "BlackBerry 7250: BlackBerry7250/4.0.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "BlackBerry 7230: BlackBerry7230/3.7.0",
            "BlackBerry 7520: BlackBerry7520/4.0.0 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "BlackBerry 7730: BlackBerry7730/3.7.0",
            "BlackBerry 8100: Mozilla/4.0 BlackBerry8100/4.2.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/100",
            "BlackBerry 8130: BlackBerry8130/4.3.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/109",
            "BlackBerry 8310: BlackBerry8310/4.2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/121",
            "BlackBerry 8320: BlackBerry8320/4.3.1 Profile/MIDP-2.0 Configuration/CLDC-1.1",
            "BlackBerry 8700: BlackBerry8700/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/100",
            "BlackBerry 8703e: BlackBerry8703e/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/105",
            "BlackBerry 8820: BlackBerry8820/4.2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102",
            "BlackBerry 8830: BlackBerry8830/4.2.2 Profile/MIDP-2.0 Configuration/CLOC-1.1 VendorID/105",
            "BlackBerry 9000: BlackBerry9000/4.6.0.65 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102",

        };
        
        for (String temp : mobileAgents) 
        {
            int pos = temp.indexOf(':');

            String name = temp.substring(0, pos).trim();
            String agent = temp.substring(pos + 1).trim();

            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserBlackBerry() == false)
            {
                fail( name + " should be a blackberry: " + agent);
            }

            assertTrue(name + " which is a mobile browser", browser.formMobile());
            assertTrue(name + " should handle TEL:", browser.canHandleTEL());
            assertFalse("not a pocket PC", browser.isPocketPC());
            assertFalse("not a web crawler", browser.isWebCrawler());
        }
    }
    
    /**
     * Check that it's a web crawler
     *
     * @throws Exception a serious problem
     */
    public void testIsWebCrawler() throws Exception
    {
        String agents[] = {
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
            "Googlebot/2.1 (+http://www.googlebot.com/bot.html)",
            "Googlebot/2.1 (+http://www.google.com/bot.html)",
            "Googlebot-Image/1.0",
            "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
            "Mozilla/5.0 (compatible; bingbot/2.0 +http://www.bing.com/bingbot.htm)",
            "Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)",
            "Mozilla/5.0 (compatible; Yahoo! Slurp China; http://misc.yahoo.com.cn/help.html)",
            "Mozilla/5.0 (compatible; 008/0.83; http://www.80legs.com/webcrawler.html) Gecko/2008032620"
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isWebCrawler() == false)
            {
                fail( "Should be a web crawler: " + agent);
            }
        }
    }

    /**
     * Check we detect that it's a IPhone                                               <BR>
     *
     * http://en.wikipedia.org/wiki/List_of_user_agents_for_mobile_phones
     *
     * @throws Exception a serious problem
     */
    public void testIPhone() throws Exception
    {
        String mobileAgents[] = {

            /**** Apple Inc. ****/
            "Apple iPhone: Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/1A542a Safari/419.3",
            "iPod Touch: Mozila/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3",
            "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_1 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8G4 Safari/6533.18.5",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3"

        };
        
        for (String temp : mobileAgents) 
        {
            int pos = temp.indexOf(':');
            if( pos < 0) pos =0;
            String name = temp.substring(0, pos).trim();
            String agent = temp.substring(pos + 1).trim();

            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserIPhone() == false)
            {
                fail( name + " should be a iPhone: " + agent);
            }

            assertTrue(name + " which is a mobile browser", browser.formMobile());
            assertTrue(name + " which is Safari type", browser.isBrowserSafari());
            assertTrue(name + " should handle TEL:", browser.canHandleTEL());
            assertFalse("not a pocket PC", browser.isPocketPC());
            assertFalse("not a web crawler", browser.isWebCrawler());
            assertTrue( "can handle GWT", browser.canHandleGWT());
        }
    }
    
    public void testRealVersion() throws Exception
    {
        //[browser version], [browser real version], [user agent]
        String agents[][] = {
            {"7", "11", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E;"},
            {"7", "9", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2)"},
            {"8", "8", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; chromeframe/13.0.782.218; chromeframe; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)",}
        };
        
        for (String[] agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent[2]);

            assertEquals( "browser version for '" + agent[2] + "' doesn't match", Double.parseDouble(agent[0]), browser.getBrowserVersion(), 0.1);
            assertEquals( "Real Version for '" + agent[2] + "' doesn't match", Double.parseDouble(agent[1]), browser.getBrowserRealVersion(), 0.1);
        }
    }

    /**
     * Check that we correctly map to IE
     * @throws Exception a serious problem.
     */
    public void testIsIE() throws Exception
    {
        String agents[]={
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; chromeframe/13.0.782.218; chromeframe; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; chromeframe/25.0.1364.97; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.1; MS-RTC LM 8; .NET4.0C; .NET4.0E)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E)",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; Xbox)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; InfoPath.3; Creative AutoUpdate v1.40.02)",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Win64; x64; Trident/6.0)",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; InfoPath.2; .NET4.0C)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; Tablet PC 2.0)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; GTB7.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; OfficeLiveConnector.1.3; OfficeLivePatch.0.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; InfoPath.2)",
            "MOZILLA/5.0 (WINDOWS NT 6.3; WOW64; TRIDENT/7.0; TOUCH; RV:11.0) LIKE GECKO" //IE11
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserIE() == false)
            {
                fail( "Should be IE: " + agent);
            }

            assertFalse( "should not be a mobile", browser.formMobile());
        }
    }

    /**
     * Check that we correctly map to Windows
     * @throws Exception a serious problem.
     */
    public void testWindowsOS() throws Exception
    {
        String agents[]={
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E)\n",
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.osWindows() == false)
            {
                fail( "Should be Windows: " + agent);
            }

            assertFalse( "should NOT be a Linux", browser.osLinux());
            assertFalse( "should NOT be a Mac", browser.osMAC());
        }
    }

    /**
     * Check that we correctly map to Windows
     * @throws Exception a serious problem.
     */
    public void testMacOS() throws Exception
    {
        String agents[]={
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0",
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            assertFalse( "should NOT be a Linux", browser.osLinux());
            assertFalse( "should NOT be windows", browser.osWindows());
            assertTrue( "should be a Mac", browser.osMAC());
        }
    }
    
    /**
     * Check that we correctly map to Windows
     * @throws Exception a serious problem.
     */
    public void testVersionOS() throws Exception
    {
        String agents[][]={
            {"2.33","Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"},
            {"10.68", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.51.22 (KHTML, like Gecko) Version/5.1.1 Safari/534.51.22"},
            {"10.411", "Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; en) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10"},
            {"10.6", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6; en-us) AppleWebKit/531.9 (KHTML, like Gecko) Version/4.0.3 Safari/531.9"},
            {"10.63", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/531.21.11 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10"},
            {"10.64", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8"},
            {"10.71", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_1) AppleWebKit/534.48.3 (KHTML, like Gecko) Version/5.1 Safari/534.48.3"},
            {"10.68", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50"},
            {"10.68", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1"},
            {"10.58", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1"},
            {"10.68", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-au) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1"},
            {"10.72", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/534.51.22 (KHTML, like Gecko) Version/5.1.1 Safari/534.51.22"},
            {"10.67", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27"},
            {"10.67", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1"},
            {"10.58", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-us) AppleWebKit/530.19.2 (KHTML, like Gecko) Version/4.0.2 Safari/530.19"},
            {"10.65", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5"},
            {"6.1", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50"},
            {"-1", "Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5"},
            {"10.7", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7) AppleWebKit/534.48.3 (KHTML, like Gecko) Version/5.1 Safari/534.48.3"},
            {"10.63", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5"},
            {"10.63", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_5_8) AppleWebKit/534.50.2 (KHTML, like Gecko) Version/5.0.6 Safari/533.22.3"},
            {"10.68", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.52.7 (KHTML, like Gecko) Version/5.1.2 Safari/534.52.7"},
            {"10.8", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0"},
            {"6.1", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E)"},
            {"6.1", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"},
            {"6.1", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; Xbox)"},
            {"6.1", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; InfoPath.3; Creative AutoUpdate v1.40.02)"},
            {"6.2", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Win64; x64; Trident/6.0)"},
            {"5.1", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; chromeframe/13.0.782.218; chromeframe; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"},
            {"6.1", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.5; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET CLR 1.1.4322; .NET CLR 3.0.04506; SLCC1; Tablet PC 2.0; .NET4.0C; .NET4.0E)"},
        };
        
        for (String[] agentList : agents) 
        {
            double version = Double.parseDouble(agentList[0]);
            String agent = agentList[1];
            ClientBrowser browser=new ClientBrowser( agent);
            double osVersion=browser.getOSVersion();
            assertEquals(agent, version, osVersion, 0.1);
        }
    }

    /**
     * Check that we correctly map to TABLET
     * @throws Exception a serious problem.
     */
    public void testFormTablet() throws Exception
    {
        String agents[]={

            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; InfoPath.2; .NET4.0C)",
            "Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; Tablet PC 2.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C)",
            "Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-P7500 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30"
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.formTablet() == false)
            {
                fail( "Should be TABLET: " + agent);
            }
        }
    }
    /**
     * Check that we correctly map to IPAD
     * @throws Exception a serious problem.
     */
    public void testIPAD() throws Exception
    {
        String agents[]={
            "Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5",
            "Mozilla/5.0 (iPad; U; CPU OS 4_3_2 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5",
            "Mozilla/5.0 (iPad; CPU OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A405 Safari/7534.48.3",
            "Mozilla/5.0 (iPad; U; CPU OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5"

        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserIPad() == false)
            {
                fail( "Should be IPAD: " + agent);
            }
        }
    }

    /**
     * Check that we correctly map to IPAD
     * @throws Exception a serious problem.
     */
    public void testNotIPAD() throws Exception
    {
        String agents[]={
            "Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-P7500 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
            "Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserIPad())
            {
                fail( "Should NOT be IPAD: " + agent);
            }
        }
    }

    public void testVersion()
    {
        String checks[][]={
            {"Mozilla/5.0 (iPhone; CPU iPhone OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A405 Safari/7534.48.3", "5.1"},
            {"Mozilla/5.0 (iPad; CPU OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A405 Safari/7534.48.3","5.1"},
            {"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.10.229 Version/11.60", "9.8"},
            {"BlackBerry 9000: BlackBerry9000/4.6.0.65 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102","4.6"},
            {"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0", "8.0"},
            {"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322)","6.0"},
            {"MOZILLA/5.0 (X11; LINUX X86_64) APPLEWEBKIT/535.16 (KHTML, LIKE GECKO) CHROME/18.0.1003.1 SAFARI/535.16", "18.0"},
            {"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2","15.0"},
            {"Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5", "5.0"},
            {"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; InfoPath.2; .NET4.0C)","8.0"},
            {"Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1","533.1"},
            {"MOZILLA/5.0 (WINDOWS NT 6.3; WOW64; TRIDENT/7.0; TOUCH; RV:11.0) LIKE GECKO", "11.0"} //IE11
        };
        
        for (String[] check : checks)
        {
            String agent = check[0];
            String vStr = check[1];
            ClientBrowser browser=new ClientBrowser( agent);
            double bVersion = browser.getBrowserVersion();
            assertEquals( agent, Double.parseDouble(vStr), bVersion, 0.001);
        }
    }

    public void testChromeFrame()
    {
        String agents[]={
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; chromeframe/13.0.782.218; chromeframe; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; chromeframe/25.0.1364.97; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.1; MS-RTC LM 8; .NET4.0C; .NET4.0E)",
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserChrome())
            {
                fail( "Should NOT be Chrome: " + agent);
            }

            if( browser.hasPlugin( ClientBrowser.PLUGIN.CHROME_FRAME) == false)
            {
                fail( "must have plugin ChromeFrame");
            }
            //     assertFalse( "should not be a mobile", browser.formMobile());
        }
    }

    /**
     * Check that we correctly map to Chrome
     * @throws Exception a serious problem.
     */
    public void testIsChrome() throws Exception
    {
        String agents[]={
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.106 Safari/535.2",
            "Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.63 Safari/534.3",
            "Mozilla/5.0 (Linux; Android 4.0.4; GT-I9300T Build/IMM76D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"
        };
        
        for (String agent : agents) {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserChrome() == false)
            {
                fail( "Should be Chrome: " + agent);
            }
            
            //     assertFalse( "should not be a mobile", browser.formMobile());
        }
    }


    /**
     * Check that we correctly map to Opera
     * @throws Exception a serious problem.
     */
    public void testIsOpera() throws Exception
    {
        String agents[]={
            "Opera/9.80 (Android 4.0.4; Linux; Opera Mobi/ADR-1207201819; U; en) Presto/2.10.254 Version/12.00",
            "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.10.229 Version/11.60"
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserOpera() == false)
            {
                fail( "Should be Opera: " + agent);
            }
            
            // assertFalse( "should not be a mobile", browser.formMobile());
        }
    }


    /**
     * Check that we correctly map to Opera
     * @throws Exception a serious problem.
     */
    public void testIsFirefox() throws Exception
    {
        String agents[]={
            "MOZILLA/5.0 (X11; U; LINUX X86_64; EN-US; RV:1.9.2.24) GECKO/20111107 UBUNTU/10.04 (LUCID) FIREFOX/3.6.24",
            "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.23) Gecko/20110921 Ubuntu/10.04 (lucid) Firefox/3.6.23",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:7.0.1) Gecko/20100101 Firefox/7.0.1",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:7.0.1) Gecko/20100101 Firefox/7.0.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:5.0.1) Gecko/20100101 Firefox/5.0.1",
            "Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-GB; rv:1.9.2.23) Gecko/20110920 Firefox/3.6.23",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0.1) Gecko/20100101 Firefox/8.0.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:8.0.1) Gecko/20100101 Firefox/8.0.1",
            "Mozilla/5.0 (Android; Mobile; rv:15.0) Gecko/15.0 Firefox/15.0"
        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserFirefox() == false)
            {
                fail( "Should be firefox: " + agent);
            }

//            assertFalse( "should not be a mobile", browser.formMobile());
            assertTrue( "can handle tiny MCE", browser.canHandleTinyMCE());
        }
    }


    /**
     * Check that we correctly map to Opera
     * @throws Exception a serious problem.
     */
    public void testIsFamilySafari() throws Exception
    {
        String agents[]={
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6; en-us) AppleWebKit/531.9 (KHTML, like Gecko) Version/4.0.3 Safari/531.9",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/531.21.11 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.51.22 (KHTML, like Gecko) Version/5.1.1 Safari/534.51.22",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_1) AppleWebKit/534.48.3 (KHTML, like Gecko) Version/5.1 Safari/534.48.3",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-au) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/534.51.22 (KHTML, like Gecko) Version/5.1.1 Safari/534.51.22",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-us) AppleWebKit/530.19.2 (KHTML, like Gecko) Version/4.0.2 Safari/530.19",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5",
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
            "Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7) AppleWebKit/534.48.3 (KHTML, like Gecko) Version/5.1 Safari/534.48.3",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_5_8) AppleWebKit/534.50.2 (KHTML, like Gecko) Version/5.0.6 Safari/533.22.3",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.52.7 (KHTML, like Gecko) Version/5.1.2 Safari/534.52.7"

        };
        
        for (String agent : agents) 
        {
            ClientBrowser browser=new ClientBrowser( agent);

            if( browser.isBrowserSafari() == false)
            {
                fail( "Should be Safari: " + agent);
            }

            assertFalse( "should not be a mobile", browser.formMobile());
        }
    }

}
