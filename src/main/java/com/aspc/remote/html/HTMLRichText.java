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

import com.aspc.remote.application.*;
import com.aspc.remote.util.misc.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import org.apache.commons.logging.Log;

/**
 *  HTMLRichText
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       10 March 1998
 */
public class HTMLRichText extends HTMLComponent
{
    /**
     * 
     * @param richText 
     */
    public HTMLRichText( String richText )
    {
        this.richText = richText;
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
        final Object pars[] = new Object[3];

        pars[0] = richText;
        pars[1] = null;
        pars[2] = browser;

        Runnable r = new Runnable() {
public void run(){
            try
            {
                doConvert( pars, (ClientBrowser)pars[2]);
            }
            catch( Exception e )
            {
                LOGGER.error("RTFText convert", e);
                pars[1] = e.toString();
            }
        }
};

        CApp.swingInvokeAndWait(r);

        buffer.append( (String)pars[1]);
    }

    private void doConvert( Object pars[], ClientBrowser browser) throws Exception//NOPMD
    {
        JTextPane pane = new JTextPane();

        RTFEditorKit rtf = new RTFEditorKit();
        pane.setEditorKit( rtf );

        doc = new DefaultStyledDocument();
        pane.setDocument( doc );

        ByteArrayInputStream in = new ByteArrayInputStream(
            ((String)pars[0]).getBytes(StandardCharsets.UTF_8)
        );

        rtf.read( in, doc, 0 );

        parseHTML(browser);
        doc = null;

        pars[1] = panel.generate();
    }

    /**
     * 
     * @return the value
     */
    public String text()
    {
        final String pars[] = new String[2];

        pars[0] = richText;
        pars[1] = null;

        Runnable r = new Runnable(){
public void run(){
            try
            {
                doConvert2( pars);
            }
            catch( Exception e )
            {
                LOGGER.error( "Error", e);
                pars[1] = e.toString();
            }
}
        };

        CApp.swingInvokeAndWait(r);

        String text = pars[1];
        byte b[] = text.getBytes(StandardCharsets.UTF_8);
        for( int i = 0; i < b.length; i++ )
        {
            if( b[i] <= 0)
                b[i] = 42;
        }
        text = new String( b );

        return text;
    }

    private void doConvert2( String pars[]) throws Exception//NOPMD
    {
        JTextPane pane = new JTextPane();
        RTFEditorKit rtf = new RTFEditorKit();
        pane.setEditorKit( rtf );

        doc = new DefaultStyledDocument();
        pane.setDocument( doc );

        ByteArrayInputStream in = new ByteArrayInputStream( pars[0].getBytes(StandardCharsets.UTF_8) );

        rtf.read( in, doc, 0 );

        int len = doc.getLength();
        if( len > 0 )
            len--;
        String text = doc.getText( 0, len );
        byte b[] = text.getBytes(StandardCharsets.UTF_8);
        for( int i = 0; i < b.length; i++ )
        {
            if( b[i] < 0 )
                b[i] = 42;
        }

        pars[1] = new String( b );

        doc = null;
    }

    private void parseHTML(ClientBrowser browser) throws Exception
    {
        panel = new HTMLPanel();
        if( browser.canHandleTables())
        {
            table = new HTMLTable();
            panel.addComponent(table);
        }

        ElementIterator it =  new ElementIterator(
            doc.getDefaultRootElement()
        );

        /*
          This will be a section element for a styled document.
          We represent this element in html as the body tags.
          Therefore we ignore it.
         */
        it.current();

        Element next;

        while((next = it.next()) != null)//NOPMD
        {
            if (isText(next) == false)
            {
                continue;
            }
            AttributeSet attr = next.getAttributes();
            writeHTMLTags(attr);
            text( next);
        }

        // now remove empty text lines from the start...
        while( container.getComponentCount() > 0 )
        {
            HTMLComponent component = container.getComponent( 0 );
            if( component instanceof HTMLText )
            {
                if( StringUtilities.isBlank(((HTMLText) component).getText()) )
                {
                    container.removeComponent( 0 );
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }

        // ... and remove empty text lines from the end
        while( container.getComponentCount() > 0 )
        {
            HTMLComponent component = container.getComponent( container.getComponentCount() - 1 );
            if( component instanceof HTMLText )
            {
                if( StringUtilities.isBlank(((HTMLText) component).getText()) )
                {
                    container.removeComponent( container.getComponentCount() - 1 );
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
    }

    /**
     * 
     * @param elem 
     * @throws javax.swing.text.BadLocationException 
     * @return the value
     */
    protected String getText(
        Element elem
    ) throws BadLocationException
    {
        return doc.getText(
            elem.getStartOffset(),
            elem.getEndOffset() - elem.getStartOffset()
        );
    }

    /**
     * 
     * @param elem 
     * @throws javax.swing.text.BadLocationException 
     * @throws java.io.IOException if an IO exception occurs.
     */
    protected void text(Element elem) throws BadLocationException, IOException
    {
        String t = getText(elem);

        if (t.length() > 0)
        {
            // Convert "text    text" to "text&nbsp;&nbsp;&nbsp; text"
            // ie., all spaces to non-breaking spaces, except the last space
            String delim = " \t\f";
            StringTokenizer st = new StringTokenizer( t, delim, true );
            t = "";
            int spaceCount = 0;
            while( st.hasMoreTokens() )
            {
                String token = st.nextToken();
                if( delim.contains(token) )
                {
                    // Accumulate the spaces until we hit some text
                    spaceCount++;
                }
                else
                {
                    // Got some text, output any accumulated spaces first,
                    for( int i = 0; i < spaceCount; i++ )
                    {
                        if( i + 1 == spaceCount )
                        {
                            t += " ";
                        }
                        else
                        {
                            t += "&nbsp;";
                        }
                    }
                    spaceCount = 0;
                    // then output the text
                    t += token;
                }
            }


            HTMLText text = new HTMLText( t );
            container.addComponent(text);

            if( StyleConstants.isBold(lastAttributes))
            {
                text.setBold(true);
            }
            if( StyleConstants.isItalic(lastAttributes))
            {
                text.setItalic(true);
            }
            if( StyleConstants.isUnderline(lastAttributes))
            {
                text.setUnderline(true);
            }

            Color color = (Color)lastAttributes.getAttribute(
                StyleConstants.Foreground
            );
            if (color != null)
            {
                text.setColor(color);
            }

            Integer size = (Integer)lastAttributes.getAttribute(
                StyleConstants.FontSize
            );

            int fontSize = 0;
            if (size != null)
            {
                int fontMapping[][] =
                {
                //      / HTML relative font size
                //     |   / Windows font size
                    { -3, 8 },
                    { -2, 9 },  // Exact match
                    { -1, 10 }, // Exact match
                    { 0,  12 }, // Exact match
                    { 1,  14 }, // Exact match
                    { 1,  16 },
                    { 2,  18 }, // Exact match
                    { 2,  20 },
                    { 3,  22 },
                    { 3,  24 }, // Exact match
                    { 4,  36 }  // Exact match
                };

                int s = size;
                for (int[] fontMapping1 : fontMapping) {
                    if (fontMapping1[1] <= s) {
                        fontSize = fontMapping1[0];
                    }
                }
            }
            text.setFontRelativeSize( fontSize );

            String family = (String)lastAttributes.getAttribute(StyleConstants.FontFamily);
            if (family != null)
            {
                text.setFont(family);
            }
        }
    }

    /**
     * 
     * @param elem 
     * @return the value
     */
    protected boolean isText(Element elem)
    {
        return (
            elem.getName().equals( AbstractDocument.ContentElementName)
        );
    }

    /**
     * 
     * @param attr 
     * @throws java.io.IOException if an IO exception occurs.
     */
    protected void writeHTMLTags(AttributeSet attr) throws IOException
    {
        int thisAllignment;

        thisAllignment = StyleConstants.getAlignment(attr);

        if(
            lastAttributes == null ||
            thisAllignment !=
            StyleConstants.getAlignment(lastAttributes)
        )
        {
            container = new HTMLContainer();

            if( table != null)
            {
                int row;
                row = table.getComponentCount();
                table.setCell(container,row,0);

                switch (thisAllignment) {
                    case StyleConstants.ALIGN_CENTER:
                        table.setCellAlignment("CENTER",row, 0);
                        break;
                    case StyleConstants.ALIGN_LEFT:
                        table.setCellAlignment("LEFT",row, 0);
                        break;
                    case StyleConstants.ALIGN_RIGHT:
                        table.setCellAlignment("RIGHT",row, 0);
                        break;
                    default:
                        break;
                }
            }
            else
            {
                panel.addComponent(container);
            }
        }

        lastAttributes = attr;
    }

    private DefaultStyledDocument doc;

    private AttributeSet lastAttributes;
    private HTMLPanel panel;
    private HTMLContainer container;//NOPMD
    private HTMLTable table;
    private final String richText;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLRichText");//#LOGGER-NOPMD
}
