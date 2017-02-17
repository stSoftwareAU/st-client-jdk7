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
package com.aspc.remote.util.misc.selftest;

import com.aspc.remote.database.selftest.DBTestUnit;
import com.aspc.remote.util.crypto.CryptoUtil;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;
import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Document;

/**
 *  test string utilities
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  
 *  @since          October 22, 2004
 */
public class TestStringUtilities extends TestCase
{
    /**
     * Creates new VirtualDBTestUnit
     *
     * @param name the name of the test
     */
    public TestStringUtilities(String name)
    {
        super( name);
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments */
    public static void main(String[] args)
    {
        Test test = suite();
//        test = TestSuite.createTest(TestStringUtilities.class, "testReplaceQuotes");
        TestRunner.run(test);
        QueueLog.flush(60000);
    }
    
    public void testReplaceQuotes()
    {
        StringBuilder sb=new StringBuilder();
        for( int i =0;i < 256;i++)
        {
            sb.append("'");
        }
        
        String tmp=sb.toString();
        
        String tmp2=StringUtilities.replace(tmp, "'", "''");
        
        assertEquals( "should have doubled", 512, tmp2.length());
        
    }
        
    public void testValidHTML()
    {
        String list[]={
            "" + (char)55295,
            "Hello world"
        };
        
        for( String value: list)
        {
            StringUtilities.checkIllegalCharactersHTML(value);            
        }
    }
        
    public void testInvalidHTML()
    {
        String list[]={
            "" + (char)55296,
            "" + (char)55296,
            "" + (char)55300,
            "" + (char)55400,
            "" + (char)55500,
            "" + (char)55600,
            "" + (char)55700,
            "" + (char)55800,
            "" + (char)55900,
            "" + (char)55999,
            "" + (char)56000,
            "" + (char)57000,
            "" + (char)57100,
            "" + (char)57300,
            "" + (char)57320,
            "" + (char)57340,
            "" + (char)57342,
            "" + (char)57343,
            "" + (char)57343,
            StringUtilities.decodeHTML("&#55359;&#57329;&#55423;&#57330;&#55487;&#57331;&#55551;&#57332;&#55615;&#57333;&#55679;&#57334;&#56319;&#57335;"),
            "\u0000",
            "\u0001"
        };
        
        for( String value: list)
        {
            try{
                StringUtilities.checkIllegalCharactersHTML(value);
                fail( "should be illegal: " + value);
            }
            catch( IllegalArgumentException iae)
            {
                
            }
            
        }
    }
    public void testValidDomian()
    {
        String list[]={"www.stsoftware.com.au", "gmail.com"};
        for( String domain:list)
        {
            if( StringUtilities.HOST_PATTERN.matcher(domain).matches()==false)
            {
                fail( "Should be an invalid domain: " + domain);
            }
        }
    }
    
    public void testInvalidDomian()
    {
        String list[]={".stsoftware.com.au", "-gmail.com", "xyz.?abc"};
        for( String domain:list)
        {
            if( StringUtilities.HOST_PATTERN.matcher(domain).matches())
            {
                fail( "should not be a valid domain: " + domain);
            }
        }
    }
    
    public void testNotBlank()
    {
        String list[]={"abc", " xyz"};
        for( String test:list)
        {
            assertTrue(test, StringUtilities.notBlank(test));
            assertTrue(test, StringUtilities.notBlank(new StringBuffer( test)));
            assertTrue(test, StringUtilities.notBlank(new StringBuilder( test) ));
        }
    }
    public void testIsBlank()
    {
        assertTrue("null", StringUtilities.isBlank(null));
        String list[]={"", "    "," \t\n\r"};
        for( String test:list)
        {
            assertTrue(test, StringUtilities.isBlank(test));
            assertTrue(test, StringUtilities.isBlank(new StringBuffer( test)));
            assertTrue(test, StringUtilities.isBlank(new StringBuilder( test) ));
        }
    }

    private final int MAX_IN_MEMORY_COMPRESS=2 * 1024 * 1024;
    public void testCompressToMemory() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();
        StringBuilder sb=new StringBuilder();
        for( int loop=0;loop< 1000;loop++)
        {
            if( sb.length() > MAX_IN_MEMORY_COMPRESS/2) break;
            for( String injectionString: sqlInjectionStrings)
            {
                if( sb.length() > MAX_IN_MEMORY_COMPRESS/2) break;
                sb.append( injectionString).append("\n");
            }
        }
        String text=sb.toString();
        if( text.length() <= MAX_IN_MEMORY_COMPRESS/2)
        {
            fail( "too short " + text.length());
        }
        byte gzData[]=StringUtilities.compressToBytes(text);
        
        String text2=StringUtilities.decompress(gzData);
        
        assertEquals( "decompresed from temp disk", text, text2);
    }
       
    public void testCompressToDisk() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();
        StringBuilder sb=new StringBuilder();
        for( int loop=0;loop< 1000;loop++)
        {
            if( sb.length() > MAX_IN_MEMORY_COMPRESS) break;
            for( String injectionString: sqlInjectionStrings)
            {
                if( sb.length() > MAX_IN_MEMORY_COMPRESS) break;
                sb.append( injectionString).append("\n");
            }
        }
        String text=sb.toString();
        if( text.length() <= MAX_IN_MEMORY_COMPRESS)
        {
            fail( "too short " + text.length());
        }
        byte gzData[]=StringUtilities.compressToBytes(text);
        
        String text2=StringUtilities.decompress(gzData);
        
        assertEquals( "decompresed from temp disk", text, text2);
    }
        
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestStringUtilities.class);
        return suite;
    }    
    
    public void testSafeMessage( )
    {
        String stdPassword="ABC123!lop";
        String scans[]={
            stdPassword
        };
        String checks[]={
            "Network error IOException: Connection refused thePars:jdbc:jtds:sqlserver://austdb1/ld_data;useLOBs=false props:{user=ld_user, password=" + stdPassword +"}",
            "FATAL: database \"aspc_master/aspc_master\" does not exist thePars:jdbc:postgresql://devserver6/aspc_master/aspc_master props:{user=postgres, password=" + stdPassword +"}"
        };
        
        for( String check: checks)
        {
            String safe=StringUtilities.safeMessage( check);
            
            for( String scan: scans)
            {
                if( safe.contains(scan))
                {
                    fail( "found " + scan + " : " + safe);
                }
            }
        }
    }
    /**
     * Check we can encode all the strings. 
     *
     * @throws Exception a test failure
     */
    public void testInjectionXML() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();
        
        for( String injectionString: sqlInjectionStrings)
        {
           if( StringUtilities.checkXML(injectionString) && StringUtilities.validCharactersHTML(injectionString))
           {
               String encoded=StringUtilities.encodeHTML(injectionString);
               
               String xml="<mail><subject>"+encoded +"</subject></mail>";
               LOGGER.info( xml);
               Document doc=DocumentUtil.makeDocument(xml);
               String xmlValue=doc.getElementsByTagName("subject").item(0).getTextContent();
               
               assertEquals( encoded, injectionString, xmlValue);
           }
        }                
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkHTML( final String html, final String expected) throws Exception
    {
       String encoded=StringUtilities.encodeHTML(html);

        if( expected !=null)
        {
            assertEquals(html, expected, encoded);
        }

        String decoded=StringUtilities.decodeHTML(encoded);
        if( html.equals(decoded)==false)
        {
            LOGGER.info( "problem");
            StringUtilities.encodeHTML(html);
            StringUtilities.decodeHTML(encoded);
        }
        assertEquals("decoded", html, decoded);
        
    }

    public void testDecode() 
    {
        String checks[][]={
            {
                "http://access-air.stsoftware.com.au/docs/web/images/icons/Plain_signature%252",
                "http://access-air.stsoftware.com.au/docs/web/images/icons/Plain_signature%2"
            },
            {
                "/docs//Office/Tina's Laptop business files/Business/ST Software/ASPC2002TaxReturnandFiancials/BPayment Confirmation - ATO_files",
                "/docs//Office/Tina's Laptop business files/Business/ST Software/ASPC2002TaxReturnandFiancials/BPayment Confirmation - ATO_files"
            }
        };
        
        for( String check[]:checks)
        {
            String encoded=check[0];
            String expected=check[1];
            
            String decoded=StringUtilities.decode(encoded);
            
            assertEquals( encoded, expected, decoded);
        }
    }
    
    public void testHTML() throws Exception
    {
        //checkHTML( )
        String checks[][]={
            {"Hello & world, needs of many > needs of the few ∴ €s", "Hello &amp; world, needs of many &gt; needs of the few &#8756; &#8364;s"},
            //{"Hello & world, needs of many > needs of the few ∴ €s", "Hello &amp; world, needs of many &gt; needs of the few &there4; &euro;s"},
            //{"%01;", "Hello &amp; world, needs of many &gt; needs of the few &there4; €s"},
        };
        for( String check[]: checks)
        {
            checkHTML( check[0], check[1]);
        }
        
        for( String inject: DBTestUnit.getSQLInjectionStrings())
        {            
            if( StringUtilities.validCharactersHTML(inject))
            {
                checkHTML( inject, null);
            }
        }
    }
    
    public void testGoodUUEncode2()
    {
        String list[]={
            "/docs/Member%20reply/Untitled%20Folder/", 
            "%2fdocs%2fweb+test%2findex.html"
        };
        
        for( String check: list)
        {            
            if( StringUtilities.UUENCODED_PATH_PATTERN.matcher(check).matches()==false)
            {
                fail( check);
            }
        }
    }
    public void testGoodUUEncode()
    {
        for( String check: DBTestUnit.getSQLInjectionStrings())
        {
            byte[] bytes = check.getBytes();
            String encoded=StringUtilities.encode(bytes, 0, bytes.length, true);
            if( StringUtilities.UUENCODED_PATH_PATTERN.matcher(encoded).matches()==false)
            {
                fail( encoded);
            }
        }
    }
        
    public void testBadUUEncode()
    {
        String list[]={
            " ",
            "#~`",
            "%%",
            "%zz",
            "hello%world"
        };
        
        for( String check: list)
        {
            if( StringUtilities.UUENCODED_PATH_PATTERN.matcher(check).matches()==true)
            {
                fail( check);
            }
        }
    }
        
    public void testGoodURI()
    {
        String list[]={
            "/",
            "/ds/code_mirror/4.7%2bzz/lib/codemirror.css",
            "/ds/code_mirror/4.7/lib/codemirror.css?ext=%2bxyz",
            "http://www.aph.gov.au/About_Parliament/Senate/Powers_practice_n_procedures/~/media/AC79BBA0B87A4906A6D71ACCEEF10535.ashx",
            "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js",
            "http://fonts.googleapis.com/css?family=Source+Code+Pro%7cOpen+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800",
            "sftp://docmgr:dhsjah@devserver6/tmp/",            
            "https://www.stsoftware.com.au",
            "/summary/generic?SCREEN_KEY=270650@2~170@1&LAYERID=110&FC=Y",
            "/29/29032/SoW+for_a_Contractor",
            "/(good)/transfer/docs/mail/tenders%40smegateway.com.au/29/29032/SoW+for+a+Contractor+(BOI+ROP+IT).doc",
            "/explorer/(good)/docs/mail/tenders%40smegateway.com.au/29/29032/SoW+for+a+Contractor+(BOI+ROP+IT).doc",
            "/explorer/transfer/docs/mail/tenders%40smegateway.com.au/29/29032/SoW+for+a+Contractor+(BOI+ROP+IT).doc?DOC_KEY=82249@2011~632@1",
            "file:///home/nigel/projects/ST/logs/pages/2014/Aug/20/11/30",
            "ftp://noboby:xxxx@localhost/docs2",
            "file://C:\\Users\\ADMIN~1\\AppData\\Local\\Temp\\2",
            "http://www.youtube.com/v/7zxEw3QanhY",
            "http://localhost:8080/ds/jshint/2.5.6/jshint.js?ts=1417847961433&code=%2fabc%2f"
        };
        
        for( String uri: list)
        {
            if( StringUtilities.URI_PATTERN.matcher(uri).find()==false)
            {
                fail( uri);
            }
        }
    }
    
    public void testBadURI()
    {
        String list[]={
            "http://localhost:8080/ds/jshint%2f2.5.6%2fjshint.js?ts=1417847961433",
            "https://www,stsoftware.com.au",
            "/summary/generic&SCREEN_KEY=270650@2~170@1&LAYERID=110?FC=Y",
            "/summary/generic?SCREEN_KEY=270650@2~170@1&LAYERID=110?FC=Y",
            "ftp://noboby:xxxx@localhost/docs2,ftp://noboby:xxxx@localhost/docs",
            "http:\\/\\/www.youtube.com\\/v\\/7zxEw3QanhY",
            "/report_explorer/transfer?ESEARCHOR1=ESEARCHAND|Task%3asimpleStatus|IS|OPEN%402%7e2791%402&EFIELD_sortDesc=&SCREEN_KEY=231009@2~170@1&EFIELD_sortField=&FORMAT=CSV&EFIELD_name=Job&CLASS_KEY=74@2~1@1&TS=1484111658864&ADD_VALUES=@@@@@@@@@@@@@"
        };
        
        for( String uri: list)
        {
            if( StringUtilities.URI_PATTERN.matcher(uri).find())
            {
                fail( uri);
            }
        }
    }
    
            
    public void testHtmlWordCount()
    {
        String list[][] = {
            {"hello world", "2"},
            {"The China &amp; HK governments hold very divergent positions vs. the activist group Occupy Central (OC). We believe the differing stances and firm attitudes on both sides raise odds of major protests in coming weeks. Two key dates: 1) July 15 when govt releases a constitutional reform consultation report, and 2) an unconfirmed date in August when China's NPC issues report on HK's constitutional review. We think banks will hoard funds in the days to weeks ahead of an August OC event. This, coupled with capital inflows and large business funding needs that we expect to continue, will likely push HKD forwards further to the right. We expect HKMA to be ready to inject additional HKD, similar to multiple injections totaling HK$37.1bn since 1 July. Our base-case scenario is for only a brief disruption of the Central business district, though long-term we think the HK risk premium is rising.", "149"},
            {"", "0"},
            {"aaaa–bb—cccc&ndash;ddddd&mdash;eeeee\u2013fffff\u2014ggggg", "7"},
            {" \n\t\r ", "0"},
            {"a<br />b<br/>c<br>def <strong>ddd</strong>", "5"},
            { "\"&amp;%00<!--\\'';你好","1"},
            {"©¡¢£¤¥¦§¨ª¬®°º»¼½¾¿ ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß àáâãäåæçèéêëìíîï ðñòóôõö÷øùúûüýþÿ", "5"},
            {"a?b", "1"},
            {"a&#09;b", "2"},
            {"a&#142;b", "1"},
            {"a&#142; b", "2"},
        };
        
        for( String[] check : list)
        {
            String text = check[0];
            int expectedCount = Integer.parseInt(check[1]);
            
            int count = StringUtilities.htmlWordCount( text);
            
            assertEquals( text, expectedCount, count);
            
        }
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testSqlValueGood()
    {
        String values[]={
            "'hello' ",
            "\"O'neil\"",
            "'O\\'neil'",
            "';DELETE'",
            "name, dob",
            "",
            "\n",
            " dob{format='dd -mmm;'}, dob{FORMAT='yyy'}",
        };
        
        for( String value: values)
        {
            StringUtilities.sqlValueCheck(value);
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testSqlValueBad()
    {
        String values[]={
            ";DELETE FROM PERSON ",
            "'Hello'\\",
            "'start but no end",
            "\"start but no end",
            "'O\\'neil\\'",
            " dob{format='dd -mmm;'}, dob{FORMAT='yyy'};",
        };
        
        for( String value: values)
        {
            try
            {
                StringUtilities.sqlValueCheck(value);
                fail( value);
            }
            catch( IllegalArgumentException iae)
            {
                
            }
        }
    }
    
    public void testInValidIPAddressTest() 
    {
        String IPs[]={
            "10.10.10",
            "10.10",
            "10",
            "a.a.a.a",
            "10.0.0.a",
            "10.10.10.256",
            "999.10.10.20",
            "2222.22.22.22",
            "22.2222.22.2",
            "22.2222.22.2",
            "22.2222.22.2",            
        };

        for( String ip: IPs)
        {
            assertFalse( ip, StringUtilities.IP_PATTERN.matcher(ip).matches());
        }
    }
    
    /**
     * http://en.wikipedia.org/wiki/Private_network
     */
    public void testLocalIPAddress() 
    {
        String IPs[]={
            "10.1.1.1",
            "10.0.0.0",
            "10.255.255.255",
            "172.16.0.0",
            "172.31.255.255",
            "192.168.0.0",
            "192.168.255.255",
            "127.0.0.1",
            "0:0:0:0:0:0:0:1",            
        };

        for( String ip: IPs)
        {
            assertTrue( ip, StringUtilities.LOCAL_IP_PATTERN.matcher(ip).matches());
        }
    }
    
    public void testValidIPAddressTest() 
    {
        String IPs[]={
            "::1",
            "1.1.1.1",
            "255.255.255.255",
            "192.168.1.1",
            "10.10.1.1",
            "132.254.111.10",
            "26.10.2.10",
            "127.0.0.1",
            "0:0:0:0:0:0:0:1"
        };

        for( String ip: IPs)
        {
            assertTrue( ip, StringUtilities.IP_PATTERN.matcher(ip).matches());
        }
    }
    
    public void testMimeType() throws Exception
    {
        String valid[] = {
"application/vnd.hzn-3d-crossword",
"video/3gpp",
"video/3gpp2",
"application/vnd.mseq",
"application/vnd.3m.post-it-notes",
"application/vnd.3gpp.pic-bw-large",
"application/vnd.3gpp.pic-bw-small",
"application/vnd.3gpp.pic-bw-var",
"application/vnd.3gpp2.tcap",
"application/x-7z-compressed",
"application/x-abiword",
"application/x-ace-compressed",
"application/vnd.americandynamics.acc",
"application/vnd.acucobol",
"application/vnd.acucorp",
"audio/adpcm",
"application/x-authorware-bin",
"application/x-authorware-map",
"application/x-authorware-seg",
"application/vnd.adobe.air-application-installer-package+zip",
"application/x-shockwave-flash",
"application/vnd.adobe.fxp",
"application/pdf",
"application/vnd.cups-ppd",
"application/x-director",
"application/vnd.adobe.xdp+xml",
"application/vnd.adobe.xfdf",
"audio/x-aac",
"application/vnd.ahead.space",
"application/vnd.airzip.filesecure.azf",
"application/vnd.airzip.filesecure.azs",
"application/vnd.amazon.ebook",
"application/vnd.amiga.ami",
"application/andrew-inset",
"application/vnd.android.package-archive",
"application/vnd.anser-web-certificate-issue-initiation",
"application/vnd.anser-web-funds-transfer-initiation",
"application/vnd.antix.game-component",
"application/vnd.apple.installer+xml",
"application/applixware",
"application/vnd.hhe.lesson-player",
"application/vnd.aristanetworks.swi",
"text/x-asm",
"application/atomcat+xml",
"application/atomsvc+xml",
"application/atom+xml",
"application/pkix-attr-cert",
"audio/x-aiff",
"video/x-msvideo",
"application/vnd.audiograph",
"image/vnd.dxf",
"model/vnd.dwf",
"text/plain-bas",
"application/x-bcpio",
"application/octet-stream",
"image/bmp",
"application/x-bittorrent",
"application/vnd.rim.cod",
"application/vnd.blueice.multipass",
"application/vnd.bmi",
"application/x-sh",
"image/prs.btif",
"application/vnd.businessobjects",
"application/x-bzip",
"application/x-bzip2",
"application/x-csh",
"text/x-c",
"application/vnd.chemdraw+xml",
"text/css",
"chemical/x-cdx",
"chemical/x-cml",
"chemical/x-csml",
"application/vnd.contact.cmsg",
"application/vnd.claymore",
"application/vnd.clonk.c4group",
"image/vnd.dvb.subtitle",
"application/cdmi-capability",
"application/cdmi-container",
"application/cdmi-domain",
"application/cdmi-object",
"application/cdmi-queue",
"application/vnd.cluetrust.cartomobile-config",
"application/vnd.cluetrust.cartomobile-config-pkg",
"image/x-cmu-raster",
"model/vnd.collada+xml",
"text/csv",
"application/mac-compactpro",
"application/vnd.wap.wmlc",
"image/cgm",
"x-conference/x-cooltalk",
"image/x-cmx",
"application/vnd.xara",
"application/vnd.cosmocaller",
"application/x-cpio",
"application/vnd.crick.clicker",
"application/vnd.crick.clicker.keyboard",
"application/vnd.crick.clicker.palette",
"application/vnd.crick.clicker.template",
"application/vnd.crick.clicker.wordbank",
"application/vnd.criticaltools.wbs+xml",
"application/vnd.rig.cryptonote",
"chemical/x-cif",
"chemical/x-cmdf",
"application/cu-seeme",
"application/prs.cww",
"text/vnd.curl",
"text/vnd.curl.dcurl",
"text/vnd.curl.mcurl",
"text/vnd.curl.scurl",
"application/vnd.curl.car",
"application/vnd.curl.pcurl",
"application/vnd.yellowriver-custom-menu",
"application/dssc+der",
"application/dssc+xml",
"application/x-debian-package",
"audio/vnd.dece.audio",
"image/vnd.dece.graphic",
"video/vnd.dece.hd",
"video/vnd.dece.mobile",
"video/vnd.uvvu.mp4",
"video/vnd.dece.pd",
"video/vnd.dece.sd",
"video/vnd.dece.video",
"application/x-dvi",
"application/vnd.fdsn.seed",
"application/x-dtbook+xml",
"application/x-dtbresource+xml",
"application/vnd.dvb.ait",
"application/vnd.dvb.service",
"audio/vnd.digital-winds",
"image/vnd.djvu",
"application/xml-dtd",
"application/vnd.dolby.mlp",
"application/x-doom",
"application/vnd.dpgraph",
"audio/vnd.dra",
"application/vnd.dreamfactory",
"audio/vnd.dts",
"audio/vnd.dts.hd",
"image/vnd.dwg",
"application/vnd.dynageo",
"application/ecmascript",
"application/vnd.ecowin.chart",
"image/vnd.fujixerox.edmics-mmr",
"image/vnd.fujixerox.edmics-rlc",
"application/exi",
"application/vnd.proteus.magazine",
"application/epub+zip",
"message/rfc822",
"application/vnd.enliven",
"application/vnd.is-xpr",
"image/vnd.xiff",
"application/vnd.xfdl",
"application/emma+xml",
"application/vnd.ezpix-album",
"application/vnd.ezpix-package",
"image/vnd.fst",
"video/vnd.fvt",
"image/vnd.fastbidsheet",
"application/vnd.denovo.fcselayout-link",
"video/x-f4v",
"video/x-flv",
"image/vnd.fpx",
"image/vnd.net-fpx",
"text/vnd.fmi.flexstor",
"video/x-fli",
"application/vnd.fluxtime.clip",
"application/vnd.fdf",
"text/x-fortran",
"application/vnd.mif",
"application/vnd.framemaker",
"image/x-freehand",
"application/vnd.fsc.weblaunch",
"application/vnd.frogans.fnc",
"application/vnd.frogans.ltf",
"application/vnd.fujixerox.ddd",
"application/vnd.fujixerox.docuworks",
"application/vnd.fujixerox.docuworks.binder",
"application/vnd.fujitsu.oasys",
"application/vnd.fujitsu.oasys2",
"application/vnd.fujitsu.oasys3",
"application/vnd.fujitsu.oasysgp",
"application/vnd.fujitsu.oasysprs",
"application/x-futuresplash",
"application/vnd.fuzzysheet",
"image/g3fax",
"application/vnd.gmx",
"model/vnd.gtw",
"application/vnd.genomatix.tuxedo",
"application/vnd.geogebra.file",
"application/vnd.geogebra.tool",
"model/vnd.gdl",
"application/vnd.geometry-explorer",
"application/vnd.geonext",
"application/vnd.geoplan",
"application/vnd.geospace",
"application/x-font-ghostscript",
"application/x-font-bdf",
"application/x-gtar",
"application/x-texinfo",
"application/x-gnumeric",
"application/vnd.google-earth.kml+xml",
"application/vnd.google-earth.kmz",
"application/vnd.grafeq",
"image/gif",
"text/vnd.graphviz",
"application/vnd.groove-account",
"application/vnd.groove-help",
"application/vnd.groove-identity-message",
"application/vnd.groove-injector",
"application/vnd.groove-tool-message",
"application/vnd.groove-tool-template",
"application/vnd.groove-vcard",
"video/h261",
"video/h263",
"video/h264",
"application/vnd.hp-hpid",
"application/vnd.hp-hps",
"application/x-hdf",
"audio/vnd.rip",
"application/vnd.hbci",
"application/vnd.hp-jlyt",
"application/vnd.hp-pcl",
"application/vnd.hp-hpgl",
"application/vnd.yamaha.hv-script",
"application/vnd.yamaha.hv-dic",
"application/vnd.yamaha.hv-voice",
"application/vnd.hydrostatix.sof-data",
"application/hyperstudio",
"application/vnd.hal+xml",
"text/html",
"application/vnd.ibm.rights-management",
"application/vnd.ibm.secure-container",
"text/calendar",
"application/vnd.iccprofile",
"image/x-icon",
"application/vnd.igloader",
"image/ief",
"application/vnd.immervision-ivp",
"application/vnd.immervision-ivu",
"application/reginfo+xml",
"text/vnd.in3d.3dml",
"text/vnd.in3d.spot",
"model/iges",
"application/vnd.intergeo",
"application/vnd.cinderella",
"application/vnd.intercon.formnet",
"application/vnd.isac.fcs",
"application/ipfix",
"application/pkix-cert",
"application/pkixcmp",
"application/pkix-crl",
"application/pkix-pkipath",
"application/vnd.insors.igm",
"application/vnd.ipunplugged.rcprofile",
"application/vnd.irepository.package+xml",
"text/vnd.sun.j2me.app-descriptor",
"application/java-archive",
"application/java-vm",
"application/x-java-jnlp-file",
"application/java-serialized-object",
"text/x-java-source",
"application/javascript",
"application/json",
"application/vnd.joost.joda-archive",
"video/jpm",
"image/jpeg",
"video/jpeg",
"application/vnd.kahootz",
"application/vnd.chipnuts.karaoke-mmd",
"application/vnd.kde.karbon",
"application/vnd.kde.kchart",
"application/vnd.kde.kformula",
"application/vnd.kde.kivio",
"application/vnd.kde.kontour",
"application/vnd.kde.kpresenter",
"application/vnd.kde.kspread",
"application/vnd.kde.kword",
"application/vnd.kenameaapp",
"application/vnd.kidspiration",
"application/vnd.kinar",
"application/vnd.kodak-descriptor",
"application/vnd.las.las+xml",
"application/x-latex",
"application/vnd.llamagraphics.life-balance.desktop",
"application/vnd.llamagraphics.life-balance.exchange+xml",
"application/vnd.jam",
"application/vnd.lotus-1-2-3",
"application/vnd.lotus-approach",
"application/vnd.lotus-freelance",
"application/vnd.lotus-notes",
"application/vnd.lotus-organizer",
"application/vnd.lotus-screencam",
"application/vnd.lotus-wordpro",
"audio/vnd.lucent.voice",
"audio/x-mpegurl",
"video/x-m4v",
"application/mac-binhex40",
"application/vnd.macports.portpkg",
"application/vnd.osgeo.mapguide.package",
"application/marc",
"application/marcxml+xml",
"application/mxf",
"application/vnd.wolfram.player",
"application/mathematica",
"application/mathml+xml",
"application/mbox",
"application/vnd.medcalcdata",
"application/mediaservercontrol+xml",
"application/vnd.mediastation.cdkey",
"application/vnd.mfer",
"application/vnd.mfmp",
"model/mesh",
"application/mads+xml",
"application/mets+xml",
"application/mods+xml",
"application/metalink4+xml",
"application/vnd.ms-powerpoint.template.macroenabled.12",
"application/vnd.ms-word.document.macroenabled.12",
"application/vnd.ms-word.template.macroenabled.12",
"application/vnd.mcd",
"application/vnd.micrografx.flo",
"application/vnd.micrografx.igx",
"application/vnd.eszigno3+xml",
"application/x-msaccess",
"video/x-ms-asf",
"application/x-msdownload",
"application/vnd.ms-artgalry",
"application/vnd.ms-cab-compressed",
"application/vnd.ms-ims",
"application/x-ms-application",
"application/x-msclip",
"image/vnd.ms-modi",
"application/vnd.ms-fontobject",
"application/vnd.ms-excel",
"application/vnd.ms-excel.addin.macroenabled.12",
"application/vnd.ms-excel.sheet.binary.macroenabled.12",
"application/vnd.ms-excel.template.macroenabled.12",
"application/vnd.ms-excel.sheet.macroenabled.12",
"application/vnd.ms-htmlhelp",
"application/x-mscardfile",
"application/vnd.ms-lrm",
"application/x-msmediaview",
"application/x-msmoney",
"application/vnd.openxmlformats-officedocument.presentationml.presentation",
"application/vnd.openxmlformats-officedocument.presentationml.slide",
"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
"application/vnd.openxmlformats-officedocument.presentationml.template",
"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
"application/vnd.openxmlformats-officedocument.spreadsheetml.template",
"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
"application/vnd.openxmlformats-officedocument.wordprocessingml.template",
"application/x-msbinder",
"application/vnd.ms-officetheme",
"application/onenote",
"audio/vnd.ms-playready.media.pya",
"video/vnd.ms-playready.media.pyv",
"application/vnd.ms-powerpoint",
"application/vnd.ms-powerpoint.addin.macroenabled.12",
"application/vnd.ms-powerpoint.slide.macroenabled.12",
"application/vnd.ms-powerpoint.presentation.macroenabled.12",
"application/vnd.ms-powerpoint.slideshow.macroenabled.12",
"application/vnd.ms-project",
"application/x-mspublisher",
"application/x-msschedule",
"application/x-silverlight-app",
"application/vnd.ms-pki.stl",
"application/vnd.ms-pki.seccat",
"application/vnd.visio",
"video/x-ms-wm",
"audio/x-ms-wma",
"audio/x-ms-wax",
"video/x-ms-wmx",
"application/x-ms-wmd",
"application/vnd.ms-wpl",
"application/x-ms-wmz",
"video/x-ms-wmv",
"video/x-ms-wvx",
"application/x-msmetafile",
"application/x-msterminal",
"application/msword",
"application/x-mswrite",
"application/vnd.ms-works",
"application/x-ms-xbap",
"application/vnd.ms-xpsdocument",
"audio/midi",
"application/vnd.ibm.minipay",
"application/vnd.ibm.modcap",
"application/vnd.jcp.javame.midlet-rms",
"application/vnd.tmobile-livetv",
"application/x-mobipocket-ebook",
"application/vnd.mobius.mbk",
"application/vnd.mobius.dis",
"application/vnd.mobius.plc",
"application/vnd.mobius.mqy",
"application/vnd.mobius.msl",
"application/vnd.mobius.txf",
"application/vnd.mobius.daf",
"text/vnd.fly",
"application/vnd.mophun.certificate",
"application/vnd.mophun.application",
"video/mj2",
"audio/mpeg",
"video/vnd.mpegurl",
"video/mpeg",
"application/mp21",
"audio/mp4",
"video/mp4",
"application/mp4",
"application/vnd.apple.mpegurl",
"application/vnd.musician",
"application/vnd.muvee.style",
"application/xv+xml",
"application/vnd.nokia.n-gage.data",
"application/vnd.nokia.n-gage.symbian.install",
"application/x-dtbncx+xml",
"application/x-netcdf",
"application/vnd.neurolanguage.nlu",
"application/vnd.dna",
"application/vnd.noblenet-directory",
"application/vnd.noblenet-sealer",
"application/vnd.noblenet-web",
"application/vnd.nokia.radio-preset",
"application/vnd.nokia.radio-presets",
"text/n3",
"application/vnd.novadigm.edm",
"application/vnd.novadigm.edx",
"application/vnd.novadigm.ext",
"application/vnd.flographit",
"audio/vnd.nuera.ecelp4800",
"audio/vnd.nuera.ecelp7470",
"audio/vnd.nuera.ecelp9600",
"application/oda",
"application/ogg",
"audio/ogg",
"video/ogg",
"application/vnd.oma.dd2+xml",
"application/vnd.oasis.opendocument.text-web",
"application/oebps-package+xml",
"application/vnd.intu.qbo",
"application/vnd.openofficeorg.extension",
"application/vnd.yamaha.openscoreformat",
"audio/webm",
"video/webm",
"application/vnd.oasis.opendocument.chart",
"application/vnd.oasis.opendocument.chart-template",
"application/vnd.oasis.opendocument.database",
"application/vnd.oasis.opendocument.formula",
"application/vnd.oasis.opendocument.formula-template",
"application/vnd.oasis.opendocument.graphics",
"application/vnd.oasis.opendocument.graphics-template",
"application/vnd.oasis.opendocument.image",
"application/vnd.oasis.opendocument.image-template",
"application/vnd.oasis.opendocument.presentation",
"application/vnd.oasis.opendocument.presentation-template",
"application/vnd.oasis.opendocument.spreadsheet",
"application/vnd.oasis.opendocument.spreadsheet-template",
"application/vnd.oasis.opendocument.text",
"application/vnd.oasis.opendocument.text-master",
"application/vnd.oasis.opendocument.text-template",
"image/ktx",
"application/vnd.sun.xml.calc",
"application/vnd.sun.xml.calc.template",
"application/vnd.sun.xml.draw",
"application/vnd.sun.xml.draw.template",
"application/vnd.sun.xml.impress",
"application/vnd.sun.xml.impress.template",
"application/vnd.sun.xml.math",
"application/vnd.sun.xml.writer",
"application/vnd.sun.xml.writer.global",
"application/vnd.sun.xml.writer.template",
"application/x-font-otf",
"application/vnd.yamaha.openscoreformat.osfpvg+xml",
"application/vnd.osgi.dp",
"application/vnd.palm",
"text/x-pascal",
"application/vnd.pawaafile",
"application/vnd.hp-pclxl",
"application/vnd.picsel",
"image/x-pcx",
"image/vnd.adobe.photoshop",
"application/pics-rules",
"image/x-pict",
"application/x-chat",
"application/pkcs10",
"application/x-pkcs12",
"application/pkcs7-mime",
"application/pkcs7-signature",
"application/x-pkcs7-certreqresp",
"application/x-pkcs7-certificates",
"application/pkcs8",
"application/vnd.pocketlearn",
"image/x-portable-anymap",
"image/x-portable-bitmap",
"application/x-font-pcf",
"application/font-tdpfr",
"application/x-chess-pgn",
"image/x-portable-graymap",
"image/png",
"image/x-portable-pixmap",
"application/pskc+xml",
"application/vnd.ctc-posml",
"application/postscript",
"application/x-font-type1",
"application/vnd.powerbuilder6",
"application/pgp-encrypted",
"application/pgp-signature",
"application/vnd.previewsystems.box",
"application/vnd.pvi.ptid1",
"application/pls+xml",
"application/vnd.pg.format",
"application/vnd.pg.osasli",
"text/prs.lines.tag",
"application/x-font-linux-psf",
"application/vnd.publishare-delta-tree",
"application/vnd.pmi.widget",
"application/vnd.quark.quarkxpress",
"application/vnd.epson.esf",
"application/vnd.epson.msf",
"application/vnd.epson.ssf",
"application/vnd.epson.quickanime",
"application/vnd.intu.qfx",
"video/quicktime",
"application/x-rar-compressed",
"audio/x-pn-realaudio",
"audio/x-pn-realaudio-plugin",
"application/rsd+xml",
"application/vnd.rn-realmedia",
"application/vnd.realvnc.bed",
"application/vnd.recordare.musicxml",
"application/vnd.recordare.musicxml+xml",
"application/relax-ng-compact-syntax",
"application/vnd.data-vision.rdz",
"application/rdf+xml",
"application/vnd.cloanto.rp9",
"application/vnd.jisp",
"application/rtf",
"text/richtext",
"application/vnd.route66.link66+xml",
"application/rss+xml",
"application/shf+xml",
"application/vnd.sailingtracker.track",
"image/svg+xml",
"application/vnd.sus-calendar",
"application/sru+xml",
"application/set-payment-initiation",
"application/set-registration-initiation",
"application/vnd.sema",
"application/vnd.semd",
"application/vnd.semf",
"application/vnd.seemail",
"application/x-font-snf",
"application/scvp-vp-request",
"application/scvp-vp-response",
"application/scvp-cv-request",
"application/scvp-cv-response",
"application/sdp",
"text/x-setext",
"video/x-sgi-movie",
"application/vnd.shana.informed.formdata",
"application/vnd.shana.informed.formtemplate",
"application/vnd.shana.informed.interchange",
"application/vnd.shana.informed.package",
"application/thraud+xml",
"application/x-shar",
"image/x-rgb",
"application/vnd.epson.salt",
"application/vnd.accpac.simply.aso",
"application/vnd.accpac.simply.imp",
"application/vnd.simtech-mindmapper",
"application/vnd.commonspace",
"application/vnd.yamaha.smaf-audio",
"application/vnd.smaf",
"application/vnd.yamaha.smaf-phrase",
"application/vnd.smart.teacher",
"application/vnd.svd",
"application/sparql-query",
"application/sparql-results+xml",
"application/srgs",
"application/srgs+xml",
"application/ssml+xml",
"application/vnd.koan",
"text/sgml",
"application/vnd.stardivision.calc",
"application/vnd.stardivision.draw",
"application/vnd.stardivision.impress",
"application/vnd.stardivision.math",
"application/vnd.stardivision.writer",
"application/vnd.stardivision.writer-global",
"application/vnd.stepmania.stepchart",
"application/x-stuffit",
"application/x-stuffitx",
"application/vnd.solent.sdkm+xml",
"application/vnd.olpc-sugar",
"audio/basic",
"application/vnd.wqd",
"application/vnd.symbian.install",
"application/smil+xml",
"application/vnd.syncml+xml",
"application/vnd.syncml.dm+wbxml",
"application/vnd.syncml.dm+xml",
"application/x-sv4cpio",
"application/x-sv4crc",
"application/sbml+xml",
"text/tab-separated-values",
"image/tiff",
"application/vnd.tao.intent-module-archive",
"application/x-tar",
"application/x-tcl",
"application/x-tex",
"application/x-tex-tfm",
"application/tei+xml",
"text/plain",
"application/vnd.spotfire.dxp",
"application/vnd.spotfire.sfs",
"application/timestamped-data",
"application/vnd.trid.tpt",
"application/vnd.triscape.mxs",
"text/troff",
"application/vnd.trueapp",
"application/x-font-ttf",
"text/turtle",
"application/vnd.umajin",
"application/vnd.uoml+xml",
"application/vnd.unity",
"application/vnd.ufdl",
"text/uri-list",
"application/vnd.uiq.theme",
"application/x-ustar",
"text/x-uuencode",
"text/x-vcalendar",
"text/x-vcard",
"application/x-cdlink",
"application/vnd.vsf",
"model/vrml",
"application/vnd.vcx",
"model/vnd.mts",
"model/vnd.vtu",
"application/vnd.visionary",
"video/vnd.vivo",
"application/ccxml+xml",
"application/voicexml+xml",
"application/x-wais-source",
"application/vnd.wap.wbxml",
"image/vnd.wap.wbmp",
"audio/x-wav",
"application/davmount+xml",
"application/x-font-woff",
"application/wspolicy+xml",
"image/webp",
"application/vnd.webturbo",
"application/widget",
"application/winhlp",
"text/vnd.wap.wml",
"text/vnd.wap.wmlscript",
"application/vnd.wap.wmlscriptc",
"application/vnd.wordperfect",
"application/vnd.wt.stf",
"application/wsdl+xml",
"image/x-xbitmap",
"image/x-xpixmap",
"image/x-xwindowdump",
"application/x-x509-ca-cert",
"application/x-xfig",
"application/xhtml+xml",
"application/xml",
"application/xcap-diff+xml",
"application/xenc+xml",
"application/patch-ops-error+xml",
"application/resource-lists+xml",
"application/rls-services+xml",
"application/resource-lists-diff+xml",
"application/xslt+xml",
"application/xop+xml",
"application/x-xpinstall",
"application/xspf+xml",
"application/vnd.mozilla.xul+xml",
"chemical/x-xyz",
"text/yaml",
"application/yang",
"application/yin+xml",
"application/vnd.zul",
"application/zip",
"application/vnd.handheld-entertainment+xml",
"application/vnd.zzazz.deck+xml"
        };
        String invalid[] = {
            "text/script,json",
            "text/script;json",
            "com.aspc/script",
            "text",
            ".java",
            "text/script'",
            "text/script\"",
            "text/script javascript",
            "text'a/script",
            "text\"a/script"
        };
        
        for(String m : valid)
        {
            assertTrue(m + " should be valid mime type", StringUtilities.FILE_MIME_TYPE_PATTERN.matcher(m).matches());
        }
        for(String m : invalid)
        {
            assertFalse(m + " should be invalid mime type", StringUtilities.FILE_MIME_TYPE_PATTERN.matcher(m).matches());
        }
    }

    public static void checkMatch(final String regex, final String value)
    {
        boolean matches = Pattern.matches(regex, value);
                
        String failures = "";
        if( matches==false)
        {
            failures+=" Pattern.matches";
        }
        boolean matches2 = value.matches(regex);

        if( matches2==false)
        {
            failures+=" value.matches";
        }     
        
        if( failures.isEmpty()==false)
        {
            fail( "regex:"+ regex + ", value:" + value + " failures: " + failures);
        }
    }
    
    public void testLikeGood()
    {
        String good[][]={
            {"* A * SAVED IS A * EARNED *", " A PENNY SAVED IS A PENNY EARNED ", " A DOLLAR SAVED IS A DOLLAR EARNED "},        
            {"abc*", "ABCD", "ABC"},        
         //   {"jaguar top car", "Top speed Jaguar car is 200kpm", "Jaguar is a top car"},        
        };
        
        for( String check[]: good)
        {
            String pattern=check[0];
            
            for( int i=1; i< check.length; i++)
            {
                String value=check[i];
                boolean matches = StringUtilities.isLike(pattern, value);
                
                assertTrue( value, matches);                
            }   
        }  
    }
    
    public void testLikeBad()
    {
        String good[][]={
            {"* A * SAVED IS A ", " A PENNY SAVED IS A PENNY EARNED ", " A DOLLAR SAVED IS A DOLLAR EARNED "},        
            {"abc*", "AB", "AB*"},        
         //   {"jaguar top car", "Top speed Jaguar car is 200kpm", "Jaguar is a top car"},        
        };
        
        for( String check[]: good)
        {
            String pattern=check[0];
            
            for( int i=1; i< check.length; i++)
            {
                String value=check[i];
                boolean matches = StringUtilities.isLike(pattern, value);
                
                assertFalse( value, matches);                
            }   
        }  
    }
    
    public void testMatchesGood()
    {
        String good[][]={
            {"\"a * saved is a * earned\"", "a penny saved is a penny earned", "a dollar saved is a dollar earned"},        
            {"jaguar top car", "Top speed Jaguar car is 200kpm", "Jaguar is a top car"},        
        };
        
        for( String check[]: good)
        {
            String pattern=check[0];
            
            for( int i=1; i< check.length; i++)
            {
                String value=check[i];
                boolean matches = StringUtilities.matches(pattern, value);
                
                assertTrue( value, matches);                
            }   
        }        
    }
    
    public void testMatchesBad()
    {
        String bad[][]={
            {"\"jaguar top car\"", "Jaguar is a top car"},        
        };
        
        for( String check[]: bad)
        {
            String pattern=check[0];
            
            for( int i=1; i< check.length; i++)
            {
                String value=check[i];
                boolean matches = StringUtilities.matches(pattern, value);
                
                assertFalse( value, matches);                
            }   
        }        
    }
    
    public void testMatch() throws TransformerConfigurationException
    {
        Transformer newTransformer = DocumentUtil.newTransformer();
        LOGGER.info( newTransformer);
        
        String good[][]={
            {"[a-zA-Z]+", "ABC"},        
            {"[a-zA-Z]([a-zA-Z0-9_:\\\\.]|-)*", "ABC","SH31-Full"},        
        };
        
        for( String check[]: good)
        {
            String regex=check[0];
            
            for( int i=1; i< check.length; i++)
            {
                String value=check[i];
                checkMatch( regex, value);
            }                        
        }
    }

    /**
     * De-Duplicate words in a string.
     * "abc,XYZ,Abc,hello,world,xyz" -> "abc,XYZ,hello,world"
     */
    public void testDeDuplication()
    {
        String list[][]={
            {"abc,XYZ,Abc,hello,world,xyz","abc,XYZ,hello,world"},
            {"",""},
            {",,,,,,,,,,,,,",""},
            {",Abc,ABC,Abc,abC,,,,,,,,,","Abc"},
        };
        
        for( String []check:list)
        {
            String start=check[0];
            String expect=check[1];
            String result=StringUtilities.deduplicate(start, ',');
            assertEquals( start, result, expect);
        }
    }
    
    /*
     * User Name: nleck
     * Password: kr4A,+U2
     */
    public void testEncodeDecode()
    {
        String cmd="USERNAME=nleck&PASSWORD=kr4A,+U2";

        String ecmd = StringUtilities.encode(StringUtilities.encodeUTF8base64( cmd));
        
        String dcmd = StringUtilities.decodeUTF8base64( StringUtilities.decode(ecmd));
        
        assertEquals( "decoded should match", cmd, dcmd);
    }    
    
    /*
     * User Name: nleck
     * Password: Xu@M1q+
     */
    public void testEncodeDecode2()
    {
        String cmd="PAGE=LOGIN&USERNAME=nleck&PASSWORD=Xu@M1q+";

        String ecmd = StringUtilities.encode(StringUtilities.encodeUTF8base64( cmd));
        
        String dcmd = StringUtilities.decodeUTF8base64( StringUtilities.decode(ecmd));
        
        assertEquals( "decoded should match", cmd, dcmd);
    }
    
    public void testXML()
    {
        for( int c = 0; c < Character.MAX_VALUE;c++)
        {
            boolean expected= XMLChar.isValid(c);
            boolean got=StringUtilities.checkXML(c);
            
            assertEquals("character '" + (char)c + "' dec: " + c, expected, got);
        }
    }
            
    public void testXML2()
    {
        assertTrue( "valid XML", StringUtilities.checkXML("\\'; DROP TABLE users; --"));
        
        for( String inject: DBTestUnit.getSQLInjectionStrings())
        {            
            boolean expected= true;
            for( int i=0;i < inject.length(); i++)
            {
                char c = inject.charAt(i);
                if( XMLChar.isValid(c) ==false)
                {
                    expected=false;
                    break;
                }
            }
            boolean got=StringUtilities.checkXML(inject);
            
            assertEquals(inject, expected, got);
        }
    }
    
    /**
     * check the random password
     */
    public void testRandomPasswordNonsequentialCheck()
    {
        String pass = "1234";
        assertTrue("it is a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, true, 3));
        assertFalse("it is not a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, true, 4));
        
        pass = "1111";
        assertTrue("it is a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, true, 3));
        assertFalse("it is not a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, true, 4));
        assertFalse("it is not a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, false, true, 3));
        
        pass = "4321";
        assertTrue("it is a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, true, 3));
        assertFalse("it is not a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, true, 4));
        assertFalse("it is not a sequetial password", StringUtilities.passwordHaveSequentialCharacters(pass, true, false, 3));        
        
    }    

    /**
     * remove the passwords from fault tolerant URLs.
     */
    public void testStripPasswordFromURL()
    {
        String urls[]={
            "http://user1:password1@hostname1,http://user2:password2@hostname2",
            "http://user1:password1@hostname1|http://user2:password2@hostname2",
            "sftp://user1:password1@hostname1|ftp://user2:password2@hostname2",
            
        };
        
        for( String url:urls)
        {
            String cleanURL=StringUtilities.stripPasswordFromURL(url);
            if( url.contains("|") && cleanURL.contains("|")==false)
            {
                fail( "Should have '|' in cleanURL: " + cleanURL);                
            }

            if( url.contains(",") && cleanURL.contains(",")==false)
            {
                fail( "Should have ',' in cleanURL: " + url + "->" + cleanURL);                
            }
            
            if( cleanURL.contains("\\"))
            {
                fail( "Should NOT have '\\' in cleanURL: " + url + "->" + cleanURL);                
            }
            if( cleanURL.contains("password"))
            {
                fail( "Should have striped password: " + cleanURL);
            }

            if( cleanURL.contains("user1")==false)
            {
                fail( "Should show user1: " + cleanURL);
            }

            if( cleanURL.contains("user2")==false)
            {
                fail( "Should show user2: " + cleanURL);
            }
            LOGGER.info( url + "->" + cleanURL);
        }
    }

    public void testCompressBase91() throws Exception
    {
       // long base91Time = 0;
      //  long hexTime = 0;

      //  for(int x = 0;x < 100;x++)
      //  {
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i < 1024 * 1024;i++)
            {
                builder.append((char)(256 * Math.random()));
            }

            String inStr = builder.toString();

            long b91s = System.currentTimeMillis();
            String compressed = StringUtilities.compressB91(inStr);
            String outStr = StringUtilities.decompressB91(compressed);
            long b91e = System.currentTimeMillis();

            byte[] out = outStr.getBytes(StandardCharsets.UTF_8);
            byte[] in = inStr.getBytes(StandardCharsets.UTF_8);

            assertEquals("must be same", inStr, outStr);
            assertEquals( "should be same size", in.length, out.length);

           // for( int i = 0; i < in.length;i++)
         //   {
         //       if( out[i] != in[i])
         //       {
         //           fail( i + ") " + in[i] + " != " + out[i]);
         //       }
         //   }
            LOGGER.info( "compress time " + TimeUtil.getDiff(b91s, b91e));

       // }
     //   LOGGER.info("Total time: Base91: " + base91Time + "\thex: " + hexTime);
        //assertTrue("we need base91 faster than hex encoding", base91Time < hexTime);
    }

    public void testBasE91() throws Exception
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0;i < 1024 * 1024;i++)
        {
            builder.append((char)(256 * Math.random()));
        }

        String inStr = builder.toString();
        long b91s = System.currentTimeMillis();
        byte[] in = inStr.getBytes("ISO-8859-1");
        String encodeValue = StringUtilities.encodeBase91(in);

        byte[] out = StringUtilities.decodeBase91(encodeValue);
        long b91e = System.currentTimeMillis();

        LOGGER.info( "encode/decode time " + TimeUtil.getDiff(b91s, b91e));
        //assertEquals("must be same", inStr, outStr);
        assertEquals( "should be same size", in.length, out.length);

        for( int i = 0; i < in.length;i++)
        {
            if( out[i] != in[i])
            {
                fail( i + ") " + in[i] + " != " + out[i]);
            }
        }

        if( encodeValue.length() > inStr.length() * 1.25)
        {
            fail( "too large " + ( encodeValue.length() * 100 /inStr.length()) + "%");
        }

            /************************ Base64 *****************************/
           // StringUtilities util = new StringUtilities();
           // long b64s = System.currentTimeMillis();
           // String base64Encoded = util.encodeBase64(inStr);
           // util.decodeBase64(base64Encoded);
           // long b64e = System.currentTimeMillis();

            /************************ hex encode *************************************/
            //long hs = System.currentTimeMillis();
          //  String hexEncoded = StringUtilities.encode(inStr);
         //   StringUtilities.decode(hexEncoded);
         //   long he = System.currentTimeMillis();

        //    long b91t = b91e - b91s;
        //    base91Time += b91t;
        //    long b64t = b64e - b64s;
        //    base64Time += b64t;
        //    long ht = he - hs;
        //    hexTime += ht;

        //    int length = inStr.length();
         //   long b91o = (long)(encodeValue.length()) * 100 / length - 100;
         //   long b64o = (long)(base64Encoded.length()) * 100 / length - 100;
        //    long hexo = (long)(hexEncoded.length()) * 100 / length - 100;

        //    LOGGER.debug("Base91 time: " + b91t + "\tBase64 time: " + b64t + "\thex time: " + ht + ";\tBase91 overhead: " + b91o + "%\tBase64 overhead: " + b64o + "%\thex overhead: " + hexo + "%");
       // }
    //    LOGGER.info("Total time: Base91: " + base91Time + "\tBase64: " + base64Time + "\thex: " + hexTime);
     //   assertTrue("we need base91 faster than base64", base91Time < base64Time);
     //   assertTrue("we need base91 faster than hex encoding", base91Time < hexTime);
    }

    public void testCodeGeneration()
    {
        String left="’";
        String right="" + (char)0x2019;

        assertEquals( "should not be changed", left, right);
    }

    public void testListHashTags()
    {
        String list[][]={
            {"#Lord #Monckton hoists the eco-tards by their own petard: http://tinyurl.com/37hsd5v  #ClimateDebate  #Climategate", "Lord","Monckton", "ClimateDebate", "Climategate"},
            {"##Lord #Monckton hoists the eco-tards by their own petard: http://tinyurl.com/37hsd5v  #ClimateDebate  #Climategate", "Lord","Monckton", "ClimateDebate", "Climategate"},
            {"###Lord #Monckton hoists the eco-tards by their own petard: http://tinyurl.com/37hsd5v  #ClimateDebate  #Climategate", "Lord","Monckton", "ClimateDebate", "Climategate"},
            {"###Lord #Monckton hoists the eco-tards by their own petard: http://tinyurl.com/37hsd5v  #ClimateDebate  #Climategate #", "Lord","Monckton", "ClimateDebate", "Climategate"},
            {
                "Church Of Climatology In Terminal Decline In Britain: http://wp.me/pE1rC-1VY #climategate #ipcc #ukpolitics #globalwarming  #globalwarming",
                "climategate",
                "ipcc",
                "ukpolitics",
                "globalwarming"
            },
            {
                "#AGW #Scam: The 2500 strong consensus that wasn't! #IPCC insider explains embarrassing disclosure that went viral http://natpo.st/apWhuH",
                "AGW",
                "Scam",
                "IPCC"
            },
            {
                "@AI_AGW #spam #yyyy #Tim_Lambert #AI_AGW #climategate This bot is a spamming xxxx",
                "spam",
                "yyyy",
                "Tim_Lambert",
                "AI_AGW",
                "climategate"
            }
        };

        for( int i = 0; i < list.length;i++)
        {
            String text = list[i][0];

            String actual[] = StringUtilities.listHashTags(text);

            assertEquals( i + ") length of answer for " + text, list[i].length -1, actual.length);
            for( int j = 0; j < actual.length;j++)
            {
                String expectValue = list[i][j+1];
                String actualValue = actual[j];
                if( expectValue.equals(actualValue) == false)
                {
                    StringUtilities.listHashTags(text);
                    fail( "Correct tags " + expectValue + " != " + actualValue);
                }
            }
        }
    }

    public void testShrinkTo140WithHintPre()
    {
        String text = "I'm no longer sceptical. I no longer have any doubt to all. I think climate change is the major challenge facing the world #David #Attenborough";
        String expected = "I'm no longer sceptical. I no longer have any doubt to all. I think climate change is the major challenge facing the world #David";

        String preHint[][]={{"#Attenborough",""},{"#David",""},};

        String actual = StringUtilities.shrinkTo140(text, preHint, null);

        assertEquals( "check", expected, actual);
    }

    public void testShrinkTo140WithHintPre2()
    {
        String text = "@diamondrn UN: Evidence is now 'unequivocal' that humans are causing global warming http://to.ly/4Kyp /via @ClimateDebate"+
                      " #globalwarming #climategate #algore #senate #GOPLeader";
        String expected = "@diamondrn UN: Evidence is now 'unequivocal' that humans are causing global warming http://to.ly/4Kyp /via @ClimateDebate #senate #GOPLeader";

        String preHint[][]={{"#globalwarming",""},{"#climategate",""},{"#algore",""},{"#senate",""},{"#GOPLeader",""}};

        String actual = StringUtilities.shrinkTo140(text, preHint, null);

        assertEquals( "check", expected, actual);
    }

    public void testShrinkTo140WithHintPost()
    {
        String text = "I'm no longer sceptical. I no longer have any doubt to all. I think climate change is the major challenge facing the world-- #David #Attenborough";
        String expected = "I'm no longer sceptical. I no longer have any doubt 2 all. I think climate change is the major challenge facing the world-- #David";

        String postHint[][]={{"#Attenborough",""},{"#David",""},};

        String actual = StringUtilities.shrinkTo140(text, null, postHint);

        assertEquals( "check", expected, actual);
    }

    public void testCompressLetters()
    {
        String sample="What letters can be compressed? Here's the list: cc, ms, ns, ps, ls, fi, fl, ffl, ffi, iv, ix, vi, oy, ii, xi, nj, \". \" (period space), and \", \" (comma space).";
        String expected="What letters can b compressed?Here's the list: ㏄，㎳，㎱" +
                        "，㎰，ʪ，ﬁ，ﬂ，ﬄ，ﬃ，ⅳ，ⅸ，ⅵ，ѹ，ⅱ，ⅺ，ǌ，\"．\" (period space)，&\"，\" (comma space).";

        String actual = StringUtilities.shrinkTo(sample, null, null, 50, 140);

        assertEquals( "check", expected, actual);
    }

    public void testCleanNumberString()
    {
        String sample="  $100,992.0";
        String expected="100992.0";

        String actual = StringUtilities.cleanNumberString(sample);

        assertEquals( "check", expected, actual);
    }

    public void testPlane()
    {
        String sample="Soo happy; buying a plane ticket for the sixteenth ... supposed to be sunny with no clouds! therefore holidays";
        String expected="Soo ☺; buying a ✈ tckt 4 the 16th … su㎰d 2B ☀y w/ no ☁☁!∴ holidays";

        String actual = StringUtilities.shrinkTo(sample, null, null, 50, 140);

        assertEquals( "check", expected, actual);
    }
    public void testTwitterLength()
    {
        String text="012345679A projection ⌨ is a form of ⌨ is projected onto a surface a key，the deⅵce records the corresponding keystroke http://en.wikipedia.org/wiki/Projection_keyboard";
        int len = StringUtilities.twitterLength(text);
        
        assertEquals( "excepted length", 141, len);
    }
    public void testLongUrlShrinkTo140()
    {
        String list[][]={
            {
                "There are multiple lines of direct observations that humans are causing global warming http://www.skepticalscience.com/empirical-evidence-for-global-warming.htm",
                "There are multiple lines of direct observations that humans are causing global warming http://www.skepticalscience.com/empirical-evidence-for-global-warming.htm",
            },
        };
        for (String[] list1 : list) 
        {
            String text = list1[0];
            String expect = list1[1];
            String actual = StringUtilities.shrinkTo140(text, null, null);
            assertEquals( text, expect, actual);
        }

    }

    public void testShrinkTo140()
    {
        String list[][]={
            {
                "Danger of science denial \"be skeptical, ask for evidence BUT when you get it, accept it\"~Michael Specter http://on.ted.com/8G0i ${hashtags} zzzzzzzzzzzzzzz",
                "Danger of science denial \"b skeptical，ask 4 eⅵdence BUT when u get it，a㏄ept it\"~Michael Specter http://on.ted.com/8G0i ${hashtags} zzzzzzzzzz"
            },
            {
                "012345679A projection ⌨ is a form of ⌨ is projected onto a surface a key，the deⅵce records the corresponding keystroke http://en.wikipedia.org/wiki/Projection_keyboard",
                "012345679A projection ⌨ is a form of ⌨ is projected onto a surface a key，the deⅵce records the corresponding keystrok http://en.wikipedia.org/wiki/Projection_keyboard"
            },
            {
                "@elkhorninn Melted the sea ice in the summer months, have paradoxically increased the chances of colder winters northern Europe www.is.gd/jsFMc zzzzzzzzzz",
                "@elkhorninn Melted the sea ice in the summer mths，have paradoⅺcally increased the chances of colder winters northern Europe www.is.gd/jsFMc "
            },
   //         {
   //             "@idiot The Earth will be fine, it's the ape like creatures that inhabit it's surface I worry about http://is.gd/kxL0in #monckton #climate #tcot #zzzzzzzz",
   //             "@idiot Earth will b ﬁne，it's the ape like creatures that ㏌habit it's surface I worry abt www.is.gd/kxL0in #monckton #climate #tcot #zzzzzzzz"
   //         },

            {
                "I'm no longer sceptical. I no longer have any doubt to all. I think climate change is the major challenge facing the world-- David Attenborough",
                "I'm no longer sceptical．I no longer have any doubt 2 all．I think climate change is the major challenge facing the world-- Daⅵd Attenborough",
            },
            {
                "\n \n                           Hello                                               \t\n                                                            ",
                "Hello",
            },
            {
                "Are you zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
                "RU zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
            },
            {
                "One and another zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
                "1&another zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
            },
            {
                "@aVeryLongUser Comprehensive rebuttal of Christopher Monckton presentation by John Abraham http://bit.ly/9bMXls /via @ClimateDebate #AGW #climategate",
                "@aVeryLongUser Comprehe㎱ⅳe rebuttal of Chris Monckton presentation by John Abraham http://bit.ly/9bMXls ⅵa @ClimateDebate #AGW #climategate"
            },
            {
                "Saying there is no global warming because it snows is like saying "+
                "there is no sun because it gets dark at night zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
                "Saying there's no GW b/c it snows is like saying there's no ☀ b/c it gets dark @ night zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
            },
        };
        for (String[] list1 : list) 
        {
            String text = list1[0];
            String expect = list1[1];
            String actual = StringUtilities.shrinkTo(text, null, null, 50, 140);
            assertEquals( text, expect, actual);
        }
    }

    /**
     * check that we parse real values.
     * @throws Exception a serious problem.
     */
    public void testDoubleReal() throws Exception
    {
        @SuppressWarnings("UnnecessaryBoxing")
        Object list[][]={
            {"" + Double.NaN, new Double(Double.NaN)},
            {"" + Double.MAX_VALUE, Double.MAX_VALUE},
            {"" + Double.MIN_VALUE, Double.MIN_VALUE},
            {"0", new Double(0)},
            {"1.2", new Double(1.2)},
            {".2", new Double(.2)},
            {"" + Double.POSITIVE_INFINITY, new Double(Double.POSITIVE_INFINITY)},
            {"" + Double.NEGATIVE_INFINITY, new Double(Double.NEGATIVE_INFINITY)},
            {" 0 ", new Double(0)},
            {"-1", new Double(-1)},
            {" -1 ", new Double(-1)}
        };
        for (Object[] list1 : list) 
        {
            double v=0;
            try {
                v = StringUtilities.parseDouble((String) list1[0]);
            } catch (NumberFormatException nfe) {
                fail("could not parse " + list1[0]);
            }
            Double d = (Double) list1[1];
            assertEquals( "match", d, v);
        }
    }

    /**
     * Check that we are faster
     * @throws Exception a serious problem.
     */
    public void testDoubleRealOnBadData() throws Exception
    {
        checkBuildStack( 100);
    }

    @SuppressWarnings({"empty-statement", "ResultOfMethodCallIgnored"})
    private void checkBuildStack( int count)
    {
        if( count >0)
        {
            checkBuildStack( count-1);
        }
        else
        {
            String list[]={
                "",
                "  11111111111111111111111111111111111111ss",
                "0-0",
                "0..0",
                "-1s",
                "the quick brown fox",
                "A lazy dog",
                "Tina",
                "Angus",
                "William",
                "Linda",
                "Simon",
                "Lea",
                "Edward",
                "Jane",
                "Clare",
                "Rossie",
                "Jonathan",
            };

            // warm up
            for( String t: list)
            {
                try
                {
                    Double.parseDouble(t);

                    fail( "should not be able to parse " + t);
                }
                catch( NumberFormatException nf)
                {
                    ;
                }
            }

            long startNew = System.currentTimeMillis();
            for( int i = 0; i < 10000; i++)
            {
                for( String t: list)
                {
                    try
                    {
                        StringUtilities.parseDouble(t);

                        fail( "should not be able to parse " + t);
                    }
                    catch( NumberFormatException nf)
                    {
                        ;
                    }
                }
            }
            long endNew = System.currentTimeMillis();

            long startOld = System.currentTimeMillis();
            for( int i = 0; i < 10000; i++)
            {
                for( String t: list)
                {
                    try
                    {
                        Double.parseDouble(t);

                        fail( "should not be able to parse " + t);
                    }
                    catch( NumberFormatException nf)
                    {
                        ;
                    }
                }
            }
            long endOld = System.currentTimeMillis();

            long diffOld = endOld-startOld;
            long diffNew = endNew-startNew;

            String msg = "old " + TimeUtil.getDiff(startOld, endOld) + " new "+ TimeUtil.getDiff(startNew, endNew);
            LOGGER.info(msg);
            if( diffOld <= diffNew)
            {
                fail( msg);
            }
        }
    }

    /**
     * Tests base64 string encode/decode with utf-8
     * @throws Exception a test failure
     */
    public void testUnicode() throws Exception
    {
        StringBuilder buffer = new StringBuilder();

        for( int c = 0; c <= 0x20ac; c++)
        {
            buffer.append( c);
        }
        //char c[]= { 0x20ac};

        //String euroSymbol = new String( c);

        String original = buffer.toString();//"euro=" + euroSymbol;
 //       String original = euroSymbol;

        String utf8 = StringUtilities.encodeUTF8( original);

       // StringUtilities su = new StringUtilities();

        String encoded = StringUtilities.encodeBase64( utf8);
        String decoded = StringUtilities.decodeBase64( encoded);

        assertEquals("decoded base 64", utf8, decoded);

        String result = StringUtilities.decodeUTF8( decoded);

        assertEquals("decoded UTF8", original, result);
    }

    public void testRangeUTF8() throws Exception
    {
        //boolean fail=false;
        for( Character c=0; c < Character.MAX_VALUE;c++)
        {
            char a[]={c};
            String s=new String( a);
            if( StringUtilities.checkUnicode(s))
            {
                String utf8 = StringUtilities.encodeUTF8(s);
                String decode = StringUtilities.decodeUTF8(utf8);

                if( s.equals(decode) == false)
                {
                    fail( (int)c + " " + Integer.toHexString(c) + " FAIL" );
                }

                String base64utf8 = StringUtilities.encodeUTF8base64(s);
                decode = StringUtilities.decodeUTF8base64(base64utf8);

                if( s.equals(decode) == false)
                {
                    fail( (int)c + " " + Integer.toHexString(c) + " FAIL" );
                }
            }
        }
    }


    public void testRangeBase64() throws Exception
    {
        //boolean fail=false;
        for( Character c=0; c < Character.MAX_VALUE;c++)
        {
            char a[]={c};
            String s=new String( a);
            if( StringUtilities.check8Bit(s))
            {
                String base64 = StringUtilities.encodeBase64(s);
                String decode = StringUtilities.decodeBase64(base64);

                if( s.equals(decode) == false)
                {
                    fail( (int)c + " " + Integer.toHexString(c) + " GOT " + decode );
                }
            }
        }
    }

    public void testRangeJavaUTF8()
    {
        //boolean fail=false;
        for( Character c=0; c < Character.MAX_VALUE;c++)
        {
            char a[]={c};
            String s=new String( a);
            if( StringUtilities.checkUnicode(s))
            {
                ByteBuffer encode = StandardCharsets.UTF_8.encode(s);
                String decode = StandardCharsets.UTF_8.decode(encode).toString();

                if( s.equals(decode) == false)
                {
                    fail( (int)c + " " + Integer.toHexString(c) + " FAIL" );
                }
            }
        }
    }

    /**
     * check all injection strings are valid.
     *
     * @throws Exception a test failure
     */
    public void testInjectionStringValid() throws Exception
    {
        HashSet<String> set=new HashSet();
        
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();

        for( String injectionString: sqlInjectionStrings)
        {
//            if( String)
            if(set.contains(injectionString))
            {
                fail( "duplicate: " + injectionString);
            }
            set.add(injectionString);
            
            assertTrue(injectionString, StringUtilities.checkUnicode(injectionString));
        }
    }


    /**
     * Check that we have at least one String that needs to be UTF8 encoded.
     *
     * @throws Exception a test failure
     */
    public void testInjectionEncodeUTF8() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();
        boolean foundEncodedUTF8=false;
        for( String injectionString: sqlInjectionStrings)
        {
            String utf8=StringUtilities.encodeUTF8(injectionString);

            if( utf8.equals(injectionString) == false)
            {
               // LOGGER.info( "ORGINAL: " + injectionString);
               // LOGGER.info( "EMCODED: " + utf8);
                foundEncodedUTF8=true;
            }

            String decoded=StringUtilities.decodeUTF8(utf8);

            assertEquals( "could not decode", injectionString, decoded);
        }

        assertTrue( "must have found encoded UT8 strings", foundEncodedUTF8);
    }

    /**
     * Check that we have at least one invalid XML string.
     *
     * @throws Exception a test failure
     */
    public void testInjectionHasInvalidXML() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();
        boolean foundInvalidXML=false;
        for( String injectionString: sqlInjectionStrings)
        {
           foundInvalidXML |= (StringUtilities.checkXML(injectionString)==false);
        }

        assertTrue( "must have found invalid XML strings", foundInvalidXML);
    }

    /**
     * Tests UTF-8  encode/decode
     * @throws Exception a test failure
     */
    public void testInjectionStringUTF8() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();

        for( String injectionString: sqlInjectionStrings)
        {
            byte[] bytes = injectionString.getBytes(StandardCharsets.UTF_8);

            String temp2=new String(bytes, StandardCharsets.UTF_8);

            assertEquals("decoded UTF 8", injectionString, temp2);

            String utf8 = StringUtilities.encodeUTF8( injectionString);
            String decoded = StringUtilities.decodeUTF8( utf8);
            assertEquals("decoded UTF8", injectionString, decoded);
        }
    }

    /**
     * Tests base64 string encode/decode with utf-8
     * @throws Exception a test failure
     */
    public void testInjectionString() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();
       // StringUtilities su = new StringUtilities();

        for( String injectionString: sqlInjectionStrings)
        {
            String utf8 = StringUtilities.encodeUTF8( injectionString);
            String encoded = StringUtilities.encodeBase64( utf8);
            String decoded = StringUtilities.decodeBase64( encoded);

            assertEquals("decoded base 64", utf8, decoded);

            String result = StringUtilities.decodeUTF8( decoded);

            assertEquals("decoded UTF8", injectionString, result);
        }
    }

    /**
     * Tests UTF8 & base64 string encode/decode with utf-8
     * @throws Exception a test failure
     */
    public void testInjectionString2() throws Exception
    {
        String[] sqlInjectionStrings = DBTestUnit.getSQLInjectionStrings();

        for( String injectionString: sqlInjectionStrings)
        {
            String encoded = StringUtilities.encodeUTF8base64( injectionString);
            String decoded = StringUtilities.decodeUTF8base64( encoded);


            assertEquals("decoded UTF8", injectionString, decoded);
        }
    }

//    /**
//     * Tests that fixedWidth method pads correctly
//     * @throws Exception a test failure
//     */
//    public void testFixedWidth() throws Exception
//    {
//        String expectedResult = "ABC     ";
//        String actualResult = StringUtilities.fixedWidth("ABC", 8);
//        assertEquals("Fixed Width", expectedResult, actualResult);
//
//        expectedResult = "12345";
//        actualResult = StringUtilities.fixedWidth("1234567890", 5);
//        assertEquals("Fixed Width (Trim)", expectedResult, actualResult);
//    }

    /**
     * Tests that the leftPad method pads correctly
     * @throws Exception a test failure
     */
    public void testLeftPad() throws Exception
    {
        String expectedResult = "000ABC";
        String actualResult = StringUtilities.leftPad("ABC", 6, "0");
        assertEquals("Left Pad", expectedResult, actualResult);

        expectedResult = "12345";
        actualResult = StringUtilities.leftPad("1234567890", 5, "X");
        assertEquals("Left Pad (Trim)", expectedResult, actualResult);
    }

    /**
     * check that the wrap works OK
     * @throws Exception a test failure
     */
    public void testWrap() throws Exception
    {
        String data = "A Job Manager will only run a  limited  number of  jobs concurrently.  " +
            "The Job Manager can be run from within the servlet engine ( JRun or Tomcat) or in a stand alone application  " +
            "on  other machines. These independent Job Manager applications will have a per application configuration of the number of " +
            "concurrent jobs to run and from which queues. A Job Manager will start each job in a background thread that runs the job " +
            "will be run at a lower priority than the normal requests. The Job Manager will monitor each job that it starts and " +
            "( SCHEDULED jobs only) will update the Job record in the database every five minutes will a heart  beat.  If a  job has " +
            "a \"Running\" status and the time stamp of the last heart beat is more than ten minutes old the job will be declared " +
            "DEAD as the monitoring Job Manager must of died. A Job Manager started on a separate machine could be configured to run " +
            "jobs from a specific queue such as \"REPORT\", how all jobs SCHEDULED for the queue \"REPORT\" will be run on this " +
            "separate machine leaving the real time user requests unaffected. When the Job Manager moves a job from a WAIT state to " +
            "RUN state a race condition is prevented between Job Managers by the dirty cache check in the normal DBObject save routines.";

        String checks [][] = {
            {"queues","5"}
        };
        for (String[] check : checks) 
        {
            String value = check[0];
            int lineNumber = Integer.parseInt(check[1]);
            String line = StringUtilities.wrap( data, 72, lineNumber);
            if( !line.contains(value))
            {
                fail( "didn't find '" + value + "' in '" + line + "'");
            }
        }
    }

    /**
     * Tests that the rightPad method pads correctly
     * @throws Exception a test failure
     */
    public void testRightPad() throws Exception
    {
        String expectedResult = "ABC000";
        String actualResult = StringUtilities.rightPad("ABC", 6, "0");
        assertEquals("Right Pad", expectedResult, actualResult);

        expectedResult = "12345";
        actualResult = StringUtilities.rightPad("1234567890", 5, "X");
        assertEquals("Right Pad (Trim)", expectedResult, actualResult);
    }

    /**
     * Tests the base64 String encode and decode operations
     * @throws Exception a test failure
     */
    public void testBase64() throws Exception
    {
        String expected = "0x_340+v0=dd-=050dfas/dv43";

        String mangle = StringUtilities.encodeBase64( expected );
        mangle = StringUtilities.decodeBase64( mangle );

        if( mangle.equals( expected ) == false )
        {
            fail( "base64 encode then decode failed" );
        }
    }

    public void testBase64Spaces( )
    {
        String sample="VE9PTEJBUl9NT0RFPUlOVEVSTkFMJkNMQVNTX0tFWT0xMzIxQDJ MUAxJkxBWUVSSUQ9MiZTQ1JFRU5fS0VZPTQwOTcwM0AyfjE3MEAxJkhFWF9BRERJVElPTkFMX0NSSVRFUklBPTYxNzM3MzY5Njc2RT"+
                      "U0NkYyMDY5NzMyMDI3MzEzMDM2MzAzMDQwMzE3RTMxMzI0MDMxMjcyMDYxNkU2NDIwNzM2OTZENzA2QzY1NTM3NDYxNzQ3NTczMjAzRDIwMjc0RjUwNDU0RTI3MjA2MTZFNjQyMDI4NjE2Mzc0Njk2RjZF"+
                      "NDI3OTQ0NjE3NDY1MjAzRTNEMjA1NDQ5NEQ0NTJFNDM1MjQ1NDE1NDQ1Mjg1NDQ5NEQ0NTJFNDc0NTU0Mjg1NDQ5NEQ0NTJFNEU0RjU3MjgyOTJDMjc1OTQ1NDE1MjI3MjkyQzU0NDk0RDQ1MkU0NzQ1NT"+
                      "QyODU0NDk0RDQ1MkU0RTRGNTcyODI5MkMyNzRENEY0RTU0NDgyNzI5MkM1NDQ5NEQ0NTJFNDc0NTU0Mjg1NDQ5NEQ0NTJFNEU0RjU3MjgyOTJDMjc0NDQxNTkyNzI5MkMzMjMzMkMzNTM5MkMzNTM5Mjky"+
                      "MDZGNzIyMDYxNjM3NDY5NkY2RTQyNzk0NDYxNzQ2NTIwNjk3MzZFNzU2QzZDMjkmU0VBUkNIX09SREVSQllfREVGPWFjdGlvbkJ5RGF0ZSBkZXNjLCBwcmlvcml0eS5yYXRpbmcgZGVzYyZTSE9XX1ZJRV"+
                      "dfUkVQT1JUX0JUTj1UUlVFJlBBR0VfU0laRT0xMCZBVVRPX1JFRlJFU0g9WUVT";

        String v1=oldDecodeBase64(sample);
        String v2=StringUtilities.decodeBase64(sample);

        assertEquals( "old matches new ", v1, v2);
    }

    /**
     * The Web Servers strip off the trailing ='s so just append them if
     * needed
     * @param encodedStr encoded string
     * @return the decoded string
     */
    private String oldDecodeBase64(final String encodedStr)
    {
        if (encodedStr == null)
        {
            return null;
        }
        String BASE_64_CIPHER =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
        String szSrc = encodedStr.replace(' ', '+');

        int srcLen = szSrc.length();

        StringBuilder buffer = new StringBuilder(srcLen);

        int pos,
                block = 0,
                excess = 0,
                skip = 0;

        boolean finished = false;
        for (pos = 1; true; pos++)
        {
            int val;

            char c;
            if (pos + skip < srcLen)
            {
                c = szSrc.charAt(pos + skip - 1);
            }
            else
            {
                finished = true;
                if (pos + skip == srcLen)
                {
                    c = szSrc.charAt(srcLen - 1);
                }
                else
                {
                    c = '=';
                }
            }

            val = BASE_64_CIPHER.indexOf(c);

            // If the character was not found then
            // it probably is a whitespace type used to break
            // up the string, so we'll just skip it.
            if (val == -1)
            {
                skip++;
                pos--;
                continue;
            }

            // '=' is used to pad out the end of the string.
            if (val == 64)
            {
                excess++;
            }
            block = (block << 6) | val;

            // If encoded properly the string will always be a
            // integral number of four chars.
            if (pos % 4 == 0)
            {
                char x;

                x = (char) ((block >> 16) & 0xFF);
                buffer.append(x);

                x = (char) ((block >> 8) & 0xFF);
                buffer.append(x);

                x = (char) (block & 0xFF);
                buffer.append(x);

                block = 0;

                if (finished == true)
                {
                    break;
                }
            }
        }

        if (buffer.length() > excess)
        {
            buffer.setLength(buffer.length() - excess);

            return buffer.toString();
        }
        else
        {
            return "";
        }
    }

    /**
     * Tests the base64 String encode and decode operations for all characters
     * @throws Exception a test failure
     */
    public void testBase64AllCharacters() throws Exception
    {
        StringBuilder buffer = new StringBuilder();

        for( char c = 0; c < 255; c++)
        {
            buffer.append( c);
        }

        String match = buffer.toString();

        //StringUtilities su = new StringUtilities();

        String eTemp = StringUtilities.encodeBase64( match);
        String dTemp = StringUtilities.decodeBase64( eTemp);

        assertEquals( "didn't save the field", match, dTemp);
    }

    /**
     * Tests the base64 String encode and decode operations for all characters
     * @throws Exception a test failure
     */
    public void testBase64null() throws Exception
    {
        for( int i = 0; i < 101; i++)
        {
            StringBuilder buffer = new StringBuilder();

            for( char c = 0; c < i; c++)
            {
                buffer.append( '\0');
            }

            String match = buffer.toString();

          //  StringUtilities su = new StringUtilities();

            String eTemp = StringUtilities.encodeBase64( match);
            String dTemp = StringUtilities.decodeBase64( eTemp);

            assertEquals( "didn't save the field", match, dTemp);
        }
    }

    /**
     * Tests the base64 byte[] operations on cipher keys
     * @throws Exception a test failure
     */
    public void testBase64Bytes() throws Exception
    {
        KeyGenerator kgen = KeyGenerator.getInstance( CryptoUtil.DEFAULT_ENCRYPTION_ALGORITHM );
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();

        byte[] mangle = StringUtilities.encodeBase64( key );

        String display = new String( mangle ); //#NOPMD
        info("");
        info( display );

        byte[] yek = StringUtilities.decodeBase64( mangle );

        if( key.length != yek.length )
        {
            fail( "the byte arrays are not the same length" );
        }

        if( Arrays.equals( key, yek ) == false )
        {
            fail( "the byte arrays are not equal" );
        }
    }

    /**
     * Tests the encoding and decoding of byte arrays of different sizes
     * @throws Exception a test failure
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testBase64BytesBoundries() throws Exception
    {
        byte[] one = {'a'}, two = {'a','b'}, three = {'a','b','c'}, four = {'a','b','c','d'};

        byte[] enc1 = StringUtilities.encodeBase64( one );
        byte[] enc2 = StringUtilities.encodeBase64( two );
        byte[] enc3 = StringUtilities.encodeBase64( three );
        byte[] enc4 = StringUtilities.encodeBase64( four );

        if( Arrays.equals( one, StringUtilities.decodeBase64( enc1 ) ) == false )
        {
            fail( "one element encode/decode failed" );
        }

        if( Arrays.equals( two, StringUtilities.decodeBase64( enc2 ) ) == false )
        {
            fail( "two element encode/decode failed" );
        }

        if( Arrays.equals( three, StringUtilities.decodeBase64( enc3 ) ) == false )
        {
            fail( "three element encode/decode failed" );
        }

        if( Arrays.equals( four, StringUtilities.decodeBase64( enc4 ) ) == false )
        {
            fail( "four element encode/decode failed" );
        }

        /* test decode boundaries, exceptions */
        try
        {
            StringUtilities.decodeBase64( one );
            fail( "one element decode should fail" );
        }
        catch( Exception ignore )
        {
            info( "good: "+ignore.getMessage() );
        }

        try
        {
            StringUtilities.decodeBase64( two );
        }
        catch( Exception bad )
        {
            info( "bad: "+bad );
            fail( "two element decode should not fail" );
        }

        try
        {
            StringUtilities.decodeBase64( three );
        }
        catch( Exception bad)
        {
            info( "bad: "+bad.getMessage() );
            fail( "three element decode should not fail" );
        }

        try
        {
            StringUtilities.decodeBase64( four );
        }
        catch( Exception bad )
        {
            info( "bad: "+bad.getMessage() );
            fail( "four element decode should not fail" );
        }

        /*byte[] allPad = { '=','=','=','=' };
        try
        {
            StringUtilities.decodeBase64( allPad );
            fail( "all padding symbols should fail" );
        }
        catch( Exception good )
        {
            info( "good: "+good.getMessage() );
        }*/

        byte[] illegal = { 'a', 'b', 'c', '@' };
        try
        {
            StringUtilities.decodeBase64( illegal );
            fail( "non base64 characters should fail" );
        }
        catch( Exception good )
        {
            info( "good: "+good.getMessage() );
        }
    }

    /**
     * Tests base 64 encoding using SecureRandom generated byte arrays
     * @throws Exception a test failure
     */
    public void testBase64RandomBytes() throws Exception
    {
        SecureRandom sr = new SecureRandom();
        byte[] random = new byte[16];
        sr.nextBytes( random );

        byte[] mangle = StringUtilities.encodeBase64( random );
        byte[] plain = StringUtilities.decodeBase64( mangle );

        info( new String( mangle ) );

        if( Arrays.equals( random, plain ) == false )
        {
            fail( "the encoded/decoded bytes do not equal original bytes" );
        }
    }

    /**
     * Tests base64 decode byte[] when the padding has been stripped
     * @throws Exception a test failure
     */
    public void testStripPadding() throws Exception
    {
        SecureRandom sr = new SecureRandom();
        byte[] original = new byte[16];
        sr.nextBytes( original );

        String mangle = new String( StringUtilities.encodeBase64( original ) );
        info( mangle );

        while( mangle.endsWith("=") )
        {
            mangle = mangle.substring( 0, mangle.length() - 1 );
        }

        info( mangle );

        byte[] decoded = StringUtilities.decodeBase64( mangle.getBytes("ascii") );

        if( Arrays.equals( original, decoded ) == false )
        {
            fail( "decoded is not equal to the original" );
        }
    }

    /**
     * Tests blank input to the base64 byte[] operations
     * @throws Exception a test failure
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testBase64BlankInput() throws Exception
    {
        byte[] nul = null;
        byte[] zero = new byte[0];

        try
        {
            StringUtilities.encodeBase64( nul );
            fail( "null input was not detected" );
        }
        catch( NullPointerException npe )//NOPMD
        {
            info( "bad: "+ npe.getMessage() );
            fail( "NullPointerException was thrown" );
        }
        catch( Exception e )
        {
            info( "good: "+e.getMessage() );
        }

        try
        {
            StringUtilities.encodeBase64( zero );
            fail( "zero length input was not detected" );
        }
        catch( Exception e )
        {
            info( "good: "+ e.getMessage() );
        }

        try
        {
            StringUtilities.decodeBase64( nul );
            fail( "null input was not detected" );
        }
        catch( NullPointerException npe )//NOPMD
        {
            info( "bad: "+ npe.getMessage() );
            fail( "NullPointerException was thrown" );
        }
        catch( Exception e )
        {
            info( "good: "+e.getMessage() );
        }

        try
        {
            StringUtilities.decodeBase64( zero );
            fail( "zero length input was not detected" );
        }
        catch( Exception e )
        {
            info( "good: "+ e.getMessage() );
        }
    }

    /**
     * Tests the StringUtilities.contains method
     * @throws Exception a test failure
     */
    public void testContains() throws Exception
    {
        String src = "hello, world", qry = ",";

        if( ! StringUtilities.contains( src, qry ) )
        {
            fail( "contains did not find the input string" );
        }

        qry = ", ";
        if( ! StringUtilities.contains( src, qry ) )
        {
            fail( "contains did not find the multi character input string" );
        }

        if( StringUtilities.contains( null, null ) )
        {
            fail( "contains found a match in null pointers" );
        }

        qry = "xxx";
        if( StringUtilities.contains( src, qry ) )
        {
            fail( "contains found input that does not exist" );
        }
    }

    /**
     * check that we can convert special values
     *
     * @throws Exception a test failure
     */
    public void testConvertHTMLToText() throws Exception
    {
        String html = "&bull;&dagger;&Pi;&alpha;";
        String text = StringUtilities.convertHtmlToText(html);

        String expected = "\u2022\u2020\u03A0\u03B1";
        assertEquals( "Expected: "+expected+" but found: "+text, expected, text);
    }

//    public void testDecode() throws Exception
//    {
//        String url ="/docs//Office/Tina's Laptop business files/Business/ST Software/ASPC2002TaxReturnandFiancials/BPayment Confirmation - ATO_files";
//
//        StringUtilities.decode(url);
//    }
    public void testSplitInvalid() throws Exception
    {
        String invalidValues[]={",';","'"};
        final char stringDelim = '\'';
        final char fieldDelim = ',';
        
        for( String text: invalidValues)
        {
            try{
                String[] valueList = StringUtilities.split( text, fieldDelim, stringDelim);
                
                fail( text + " should have been illegal but returned " + Arrays.asList(valueList));
            }
            catch( IllegalArgumentException iae)
            {
                //Expected
            }
        }
    }
    
    /**
     * check the splitByDelim method splits the values as expected
     *
     * @throws Exception a test failure
     */
    public void testSplit() throws Exception
    {
        String text;
        String[] valueList;
        final char stringDelim = '\'';
        final char fieldDelim = ',';

        text = "abc,def";
        valueList = StringUtilities.split( text, fieldDelim, stringDelim);
        checkSplitValue( valueList, 0, "abc");
        checkSplitValue( valueList, 1, "def");

        text = "'abc','def'";
        valueList = StringUtilities.split( text, fieldDelim, stringDelim);
        checkSplitValue( valueList, 0, "abc");
        checkSplitValue( valueList, 1, "def");

        text = "'a\\'bc','d,ef'";
        valueList = StringUtilities.split( text, fieldDelim, stringDelim);
        checkSplitValue( valueList, 0, "a'bc");
        checkSplitValue( valueList, 1, "d,ef");

        text = "'a\\'bc','d,ef\\''";
        valueList = StringUtilities.split( text, fieldDelim, stringDelim);
        checkSplitValue( valueList, 0, "a'bc");
        checkSplitValue( valueList, 1, "d,ef'");

        text = "'abc',  'def'";
        valueList = StringUtilities.split( text, fieldDelim, stringDelim);
        checkSplitValue( valueList, 0, "abc");
        checkSplitValue( valueList, 1, "def");

        text = "abc\tdef";
        valueList = StringUtilities.split( text, '\t', stringDelim);
        checkSplitValue( valueList, 0, "abc");
        checkSplitValue( valueList, 1, "def");

        valueList = StringUtilities.split( text, '\u0009', stringDelim);
        checkSplitValue( valueList, 0, "abc");
        checkSplitValue( valueList, 1, "def");

        text = "abc|def";
        valueList = StringUtilities.split( text, '|', stringDelim);
        checkSplitValue( valueList, 0, "abc");
        checkSplitValue( valueList, 1, "def");
    }

    private void checkSplitValue( String[] valueList, int pos, String value)
    {
        assertFalse( "Invalid number of items returned when looking for " + value + " at " + pos, valueList.length < pos + 1);
        assertEquals( "Unexpected value: ", value, valueList[pos]);
    }

    /**
     * Tests the <code>indexOfIgnoreCase</code> method of the <code>StringUtilities</code> class.
     */
    public void testIndexOfIgnoreCase()
    {
        ArrayList<Object[]> testCases = new ArrayList<>();

        testCases.add( new Object[] { 0, "AbcDEFxxx", "aBc" } );
        testCases.add( new Object[] { 0, "AbcDEFxxxabc", "abc" } );
        testCases.add( new Object[] { -1, "AbDEFxxxab", "abc" } );
        testCases.add( new Object[] { 2, "deAbCfg", "aBC" } );

        testCases.stream().forEach((testCase) -> {
            assertEquals( "Index of " + testCase[2] + " in " + testCase[1] + " should be " + testCase[0], testCase[0],
                    StringUtilities.indexOfIgnoreCase( (String) testCase[1], (String) testCase[2] ) );
        });
    }

    public void testCapitalizeWordsInString()
    {
        ArrayList<Object[]> testCases = new ArrayList<>();

        testCases.add( new Object[] { "tHis is oNLY a TEST", "This Is Only A Test" } );
        testCases.add( new Object[] { "test-spECIAL ch'ARS", "Test-Special Ch'Ars" } );
        testCases.add( new Object[] { "first one. sECOND oNE", "First One. Second One" } );

        testCases.stream().forEach((testCase) -> {
            assertEquals( "Wrong string after capitalizing the string " ,testCase[1], StringUtilities.capitalizeWordsInString(testCase[0].toString()));
        });
    }


    static void info( final Object o )
    {
        LOGGER.info( o );
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestStringUtilities");//#LOGGER-NOPMD
}
