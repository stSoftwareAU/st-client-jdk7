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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import com.aspc.remote.html.*;
import java.util.ArrayList;
import java.awt.Color;
import java.io.FileWriter;
import com.aspc.remote.util.misc.*;
import java.io.File;
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
public class TestColumnSpan extends TestCase
{
    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestColumnSpan(String name)
    {
        super( name);
    }

    /**
     * @param args  */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestColumnSpan.class);
        return suite;
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    public void testMatch() throws Exception
    {
        HTMLPage page = new HTMLPage();
        
        HTMLTable table = new HTMLTable();
        table.setBorder(1);
        page.addComponent( table);
        
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = r + ":" + c;
                
                table.setCell( key, r, c);
                table.setCellRowSpan( r, r, c);
                table.setCellColSpan( c, r, c);
            }
        }

        String temp = page.generate();
        
        try (FileWriter w = new FileWriter( System.getProperty( "user.home") + "/temp.html" )) {
            w.write( temp);
        }
        HTMLPage page2 = new HTMLPage();
        
        HTMLTable table2 = new HTMLTable();
        table2.setBorder(1);
        page2.addComponent( table2);
        
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                table2.setCellRowSpan( r, r, c);
                table2.setCellColSpan( c, r, c);
            }
        }

        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = r + ":" + c;
                
                table2.setCell( key, r, c);
            }
        }

        String temp2 = page2.generate();

        temp = StringUtilities.replace( temp, " ", "");
        temp2 = StringUtilities.replace( temp2, " ", "");
        assertEquals( "Check pages match", temp, temp2);
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    public void testRowSpan() throws Exception
    {
        HTMLPage page = new HTMLPage();
        
        HTMLTable table = new HTMLTable();
        table.setBorder(1);
        page.addComponent( table);
        
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = r + ":" + c;
                
                table.setCell( key, r, c);
                table.setCellRowSpan( r, r, c);
                //table.setCellColSpan( c, r, c);
            }
        }

        String temp = page.generate();
        File rowFile=File.createTempFile("rows", ".html");
        File row2File=File.createTempFile("rows2", ".html");
        try{
            try (FileWriter w = new FileWriter( rowFile)) {
                w.write( temp);
            }
            HTMLPage page2 = new HTMLPage();

            HTMLTable table2 = new HTMLTable();
            table2.setBorder(1);
            page2.addComponent( table2);

            for( int r = 0; r < 10; r++)
            {
                for( int c = 0; c < 10; c++)
                {
                    table2.setCellRowSpan( r, r, c);
                    //table2.setCellColSpan( c, r, c);
                }
            }

            for( int r = 0; r < 10; r++)
            {
                for( int c = 0; c < 10; c++)
                {
                    String key = r + ":" + c;

                    table2.setCell( key, r, c);
                }
            }

            String temp2 = page2.generate();

            try (FileWriter w2 = new FileWriter(row2File )) {
                w2.write( temp2);
            }
            //temp = StringUtilities.replace( temp, " ", "");
            //temp2 = StringUtilities.replace( temp2, " ", "");
            assertEquals( "Check pages match", temp, temp2);
        }
        finally{
            row2File.delete();
            rowFile.delete();
        }
    }


    /**
     * 
     * @throws Exception a serious problem
     */
    public void testNoDuplicate() throws Exception
    {
        HTMLPage page = new HTMLPage();
        
        HTMLTable table = new HTMLTable();
        table.setBorder(1);
        table.setBackGroundColor( Color.BLUE);
        page.addComponent( table);
        
        ArrayList list = new ArrayList();
       
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = "a:" + r + ":" + c;
                
                list.add( key);
                
                table.setCell( key, r, c);
                table.setCellRowSpan( r, r, c);
                table.setCellColSpan( c, r, c);
            }
        }

        table = new HTMLTable();
        table.setBorder(1);
        table.setBackGroundColor( Color.GREEN);
        page.addComponent( table);

        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = "b:" + r + ":" + c;
                
                list.add( key);
                
                table.setCell( key, r, c);
                table.setCellRowSpan( 10 - r, r, c);
                table.setCellColSpan( 10 - c, r, c);
            }
        }

        table = new HTMLTable();
        table.setBorder(1);
        table.setBackGroundColor( Color.CYAN);
        page.addComponent( table);
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = "c:" + r + ":" + c;
                
                list.add( key);
                
                table.setCell( key, r, c);
                table.setCellRowSpan( r, r, c);
                table.setCellColSpan( 10 - c, r, c);
            }
        }

        table = new HTMLTable();
        table.setBorder(1);
        table.setBackGroundColor( Color.MAGENTA);
        page.addComponent( table);
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                table.setCellRowSpan( 10 - r, r, c);
                table.setCellColSpan( c, r, c);
            }
        }
        
        for( int r = 0; r < 10; r++)
        {
            for( int c = 0; c < 10; c++)
            {
                String key = "d:" + r + ":" + c;
                
                list.add( key);
                
                table.setCell( key, r, c);
            }
        }

        String temp = page.generate();
        
        for (Object list1 : list) {
            String key = (String) list1;
            int pos = temp.indexOf( key);
            if( pos == -1 )
            {
                fail( "We lost the cell " + key);
            }
            int lastPos = temp.lastIndexOf( key);
            if( pos != lastPos)
            {
                fail( "We duplicated a cell " + key);                
            }
        }
//        try (FileWriter w = new FileWriter( System.getProperty( "user.home") + "/test.html" )) {
//            w.write( temp);
//        }
    }
}
