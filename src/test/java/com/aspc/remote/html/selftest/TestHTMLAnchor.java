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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import com.aspc.remote.html.*;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import org.apache.commons.logging.Log;

/**
 *  check the GWT modules.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Paul Smout
 *  
 *  @since          June 5 2008
 */
public class TestHTMLAnchor extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.selftest.TestHTMLAnchor");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestHTMLAnchor(String name)
    {
        super( name);
    }

    /**
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestHTMLAnchor.class);
        return suite;
    }

    /**
     * Check Valid HREF
     *
     * @throws Exception a serious problem
     */
    public void testValidHREF() throws Exception
    {

        String checks[]={
            "/report_explorer/transfer?ESEARCHOR1=ESEARCHAND%7cTask%3apercUntilClosed%7cNE%7c100%7cESEARCHAND%7cTask%3astartDate%7cGE%7c01+Jul+2015+00%3a00%7cESEARCHAND%7cTask%3aendDate%7cLT%7c31+Jul+2015+23%3a59%7cESEARCHAND%7cTask%3aassignTo%7cIS%7c1%40110%7e12%401%7cESEARCHAND%7cTask%3atype%7cNE%7cOPEN%5fENDED%5fACTIVITY%7cESEARCHAND%7cActivity%3aattendeeConfirmed%7cEQ%7cTRUE&EFIELD_sortDesc=&SCREEN_KEY=146017@2~170@1&EFIELD_sortField=&FORMAT=CSV&EFIELD_name=Open%20Tasks/Events&CLASS_KEY=1321@2~1@1&TS=1436320751634&ADD_VALUES=@@@@@@@@@@@@@",
            "/explorer/transfer/docs/Membership/Audit/ExportAudit_2015-07-18+10%3a35.csv?DOC_KEY=89@170~632@1",
            "http://stSoftware.com.au/site/ST/article?",
            "http://stSoftware.com.au/site/ST/article",
            "http://stSoftware.com.au/site/ST/article?ABC",
            "http://stSoftware.com.au/site/ST/article?UUID=7d00030d0251ef9e-10012ada6-140658c52e4",
            "javascript:wosn('/screen/dataentry?UC=66&amp;CLASS_KEY=7700@1~1@1&amp;LAYERID=1&amp;UE=E74E1049','_blank',700,860)",
            "javascript:wosy( document.location + '&amp;MODE=PRINT', '_blank', 1024, 600)",
            "javascript:export_csv();",
            "/customize/table/tree?UC=77&amp;PAGE=TREE&amp;BRANCH=1095@1~1@1&amp;OPEN=true&amp;UE=BFDD1421",
            "https://www.jobtrack.com.au/ical/7d00030d/LZGL-1ELK-S7DD-AE8L-1BH0.ics",
            "javascript:;",
            "javascript:comboOpenEdit_SiteScriptModule_replacedBy( 'EFIELD_SITESCRIPTMODULE_COL_REPLACEDBY')",
            "/site/template-music/?UC=55&amp;CMS_MODE=DESIGN&amp;UE=48E70DEF",
            "#",
            "article-item.html?CMS_MODE=DESIGN",
            "#?CMS_MODE=DESIGN",
            "http://localhost:8080/ReST/v5/class/SiteConfigurationVariable?q=site%20is%20152@2~7407@1&order=varGroup,sequence,id&fields=global_key%20as%20gk,type.code%20as%20type,varValue.val%20as%20value,varGroup.name%20as%20groupname,varGroup%7bdisplay%3dglobal_key%7d%20as%20groupGK,name,label,varValue%7bdisplay%3dglobal_key%7d%20as%20valueGK"
        };

        for( String url:checks)
        {
            if( HTMLAnchor.validateHREF(url)==false)
            {
                fail( "Should be valid " + url);
            }
        }
    }

    public void testInvalidHREF() throws Exception
    {
        String checks[]={
            "mailto:support@stsoftware.com.au?subject=Bug%20Report&amp;body=%0aException%20Type%3a%20java.lang.AssertionError%0aUser%3a%20owner%40demoscan%0aThe%20request%20URI%3a%20https%3a%2f%2flocalhost%3a8080%2fsite%2ftemplate-putty3%2fpages%2fteam.html%0aThe%20exception%20message%3a%20invalid%20href%20mailto%3asupport%40stsoftware.com.au%3fsubject%3dBug%2520Report%26amp%3bbody%3d",
            "/report/generic?CLASS_NAME=Award&LAYERID=3102&HEX_USER=61646D696E3A61646D696E&SEARCH_ORDERBY=Award:code.[IndustrialInstrumentFiles:industrialInstrumentId].file.name&SEARCH_ORDERBY_LABEL=true",
            "/report/generic?EMBEDDED=YES&ROWID=8592220881&SCREEN_KEY=1208021%402%7e170%401&LAYERID=110&HEX_USER=757365723A61626331323334353637&SEARCH_ORDERBY=DBEmail:recipientsHTML{limit='5'}&SEARCH_ORDERBY_LABEL=true",
            "/screen/dataentry?CLASS_KEY=2256@1~1@1&LAYERID=110&SFIELD_contact=$Contact:contactId$",

            "https://https://www.jobtrack.com.au/ical/7d00030d/LZGL-1ELK-S7DD-AE8L-1BH0.ics",
            "https://http://www.jobtrack.com.au/ical/7d00030d/LZGL-1ELK-S7DD-AE8L-1BH0.ics",
            "/report_explorer/transfer?ESEARCHOR1=ESEARCHAND|Task%3apercUntilClosed|NE|100|ESEARCHAND|Task%3astartDate|GE|01+Jul+2015+00%3a00|ESEARCHAND|Task%3aendDate|LT|31+Jul+2015+23%3a59|ESEARCHAND|Task%3aassignTo|IS|1%40110%7e12%401|ESEARCHAND|Task%3atype|NE|OPEN%5fENDED%5fACTIVITY|ESEARCHAND|Activity%3aattendeeConfirmed|EQ|TRUE&EFIELD_sortDesc=&SCREEN_KEY=146017@2~170@1&EFIELD_sortField=&FORMAT=CSV&EFIELD_name=Open%20Tasks/Events&CLASS_KEY=1321@2~1@1&TS=1436320751634&ADD_VALUES=@@@@@@@@@@@@@",
            "/report_explorer/transfer?ESEARCHOR1=ESEARCHAND|Task%3apercUntilClosed|NE|100|ESEARCHAND|Task%3astartDate|GE|01+Jul+2015+00%3a00|ESEARCHAND|Task%3aendDate|LT|31+Jul+2015+23%3a59|ESEARCHAND|Task%3aassignTo|IS|1%40110%7e12%401|ESEARCHAND|Task%3atype|NE|OPEN%5fENDED%5fACTIVITY|ESEARCHAND|Activity%3aattendeeConfirmed|EQ|TRUE&EFIELD_sortDesc=&SCREEN_KEY=146017@2~170@1&EFIELD_sortField=&FORMAT=CSV&EFIELD_name=Open Tasks/Events&CLASS_KEY=1321@2~1@1&TS=1436320751634&ADD_VALUES=@@@@@@@@@@@@@",
            "https://javascript:wosn('/screen/dataentry?UC=66&amp;CLASS_KEY=7700@1~1@1&amp;LAYERID=1&amp;UE=E74E1049','_blank',700,860)",
            "http://localhost:8080/ReST/v5/class/SiteConfigurationVariable?q=site%20is%20152@2~7407@1&order=varGroup,sequence,id&fields=global_key%20as%20gk,type.code%20as%20type,varValue.val%20as%20value,varGroup.name%20as%20groupname,varGroup{display%3dglobal_key}%20as%20groupGK,name,label,varValue{display%3dglobal_key}%20as%20valueGK"

        };
        for( String url:checks)
        {
            if( HTMLAnchor.validateHREF(url))
            {
                fail( "Should NOT be valid " + url);
            }
        }
    }
    
    public void testCallParameter() throws Exception
    {
        HTMLAnchor anc = new HTMLAnchor( "/group_email");
        try
        {
            anc.addCallParameter("abc", "a|b");
        }
        catch(AssertionError e)
        {
            //good
        }
        anc.addCallParameter("abc", StringUtilities.encode("a|b"));
        String href = anc.getHREF();
        assertFalse("should NOT include '|'", href.contains("|"));
    }
}
