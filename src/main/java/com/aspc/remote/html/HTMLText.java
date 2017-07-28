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

import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.html.style.InternalStyleSheet;
import com.aspc.remote.html.theme.HTMLTheme;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  HTMLText
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       23 April 1998
 */
public class HTMLText extends HTMLComponent
{
    /**
     *
     * @param text
     */
    public HTMLText(String text)
    {
        this( text, true);
    }

    /**
     * Sets the text and whether or not to replace brackets
     * @param text
     * @param replaceBrackets
     */
    public HTMLText(String text, boolean replaceBrackets)
    {
        setText( text, replaceBrackets);
    }

    /**
     *
     * @param text
     * @return the value
     */
    public HTMLText setText( String text)
    {
        setText( text, true);

        return this;
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return "HTMLText( '" + oText + "')";
    }

    /**
     *
     * @param flag
     */
    public void setIndent( boolean flag)
    {
        indent = flag;
    }

    /**
     *
     * @param theText
     * @param replaceBrackets
     * @return the value
     */
    public final HTMLText setText( String theText, boolean replaceBrackets)
    {
        bReplaceBrackets = replaceBrackets;
        oText = theText == null ? "" : theText;
        text = oText;

        text = text.replace( "\r\n","\n");
        text = text.replace( "\r","\n");

        if( replaceBrackets == true)
        {
            int len = text.length();

            StringBuilder buffer = new StringBuilder(len);

            for (int i = 0; i < len; i++)
            {
                char c = text.charAt(i);

                if( c== '&')
                {
                    buffer.append("&amp;");
                }
                else if( c== '<')
                {
                    buffer.append("&lt;");
                }
                else if( c== '>')
                {
                    buffer.append("&gt;");
                }
                else if( c== '\n')
                {
                    buffer.append("<BR>\n");
                }
                else if (  c >= ' ' && c <= '~' )
                {
                    buffer.append(c);
                }
                else
                {
                    buffer.append("&#");
                    buffer.append((int) c);
                    buffer.append(";");
                }
            }

            text= buffer.toString();
        }
        else
        {
            text = text.replace( "<br>", "<BR>");
            /**
             * This is converting one a bit of HTML with no newlines to
             * have one. NL- 13 Oct 2004
             */
            //text = text.replace( "<BR>\n", "\n");
            //text = text.replace( "<BR>", "\n");
        }
        text = text.replace( "\t", "&#09;" );

        // Inspect the text for the number of newline etc.
        String temp;
        temp = text.replace( "<BR>", "" );
        temp = temp.replace( "&nbsp;", "" );

        containsVisibleText = !StringUtilities.isBlank(temp);

        returnCount = 0;

        while( true)
        {
            int pos;

            pos = text.lastIndexOf("<BR>");

            if( pos != -1)
            {
                int pos2 = pos + 4;

                if(
                    pos2 == text.length() ||
                    StringUtilities.isBlank(text.substring(pos2))
                )
                {
                    returnCount++;
                    text = text.substring(0, pos);
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

        return this;
    }

    /**
     *
     * @return the value
     */
    public String getText()
    {
        return oText;
    }

    /**
     *
     * @return the value
     */
    public String getParsedText()
    {
        return text;
    }

    /**
     *
     * @param color
     * @return the value
     */
    public HTMLText setColor(java.awt.Color color)
    {
        this.color = color;

        return this;
    }

    /**
     *
     * @param red
     * @param green
     * @param blue
     * @return the value
     */
    public HTMLText setColor( int red, int green, int blue )
    {
        this.color = new java.awt.Color( red, green, blue );

        return this;
    }

    /**
     *
     * @param relativeSize
     * @return the value
     */
    public HTMLText setFontRelativeSize( int relativeSize)
    {
        getMutableTheme().setDefaultFontRelativeSize(relativeSize);

        return this;
    }

    /**
     *
     * @param size
     * @return the value
     */
    public HTMLText setFontSize( int size)
    {
       fontSize=size;

        return this;
    }

    /**
     *
     * @param face
     * @return the value
     */
    public HTMLText setFont( String face)
    {
        this.fontFamily = face;

        return this;
    }

    /**
     *
     * @param color
     * @return the value
     */
    public HTMLText setFontForegroundColor( Color color)
    {
        getMutableTheme().setDefault(
            HTMLTheme.DKEY_FONT_COLOR,
            color
        );

        return this;
    }

    /**
     *
     * @param on
     * @return the value
     */
    public HTMLText setStrikethrough( boolean on)
    {
        if( on)
        {
            isStrikethrough = Boolean.TRUE;
        }
        else
        {
            isStrikethrough = Boolean.FALSE;
        }
        return this;
    }

    /**
     *
     * @param on
     * @return the value
     */
    public HTMLText setNoWrap( boolean on)
    {
        this.noWrapFg = on;

        return this;
    }

    /**
     *
     * @param on
     * @return the value
     */
    public HTMLText setBold( boolean on)
    {
        if( on)
        {
            isBold = Boolean.TRUE;
        }
        else
        {
            isBold = Boolean.FALSE;
        }

        return this;
    }

    /**
     *
     * @param on
     * @return the value
     */
    public HTMLText setItalic( boolean on)
    {
        if( on)
        {
            isItalic = Boolean.TRUE;
        }
        else
        {
            isItalic = Boolean.FALSE;
        }

        return this;
    }

    /**
     *   Levels 1 - 6
     * @param headerLevel_1to6
     * @return the value
     */
    public HTMLText setHeaderLevel( int headerLevel_1to6)
    {
        if( headerLevel_1to6 < 1 || headerLevel_1to6 > 6)
        {
            throw new RuntimeException(
                "Header Level 1-6 (" + headerLevel_1to6 + ")"
            );
        }
        this.headerLevel = headerLevel_1to6;

        return this;
    }

    /**
     *
     * @param on
     * @return the value
     */
    public HTMLText setUnderline( boolean on)
    {
        if( on)
        {
            isUnderline = Boolean.TRUE;
        }
        else
        {
            isUnderline = Boolean.FALSE;
        }

        return this;
    }

    /**
     *
     * @param type the type
     * @param value the value
     * @return the value
     */
    public HTMLText addStyleElement( String type, String value)
    {
        getStyleSheet().addElement(type, value);

        return this;
    }


    /** do we need a tag
     * @return the tag
     */
    protected boolean needTag()
    {
        return false;
    }

    /**
     * start a tag
     *
     * @param buffer the buffer
     */
    protected void startTag(final StringBuilder buffer)
    {
        buffer.append( "<span");
    }

    /**
     * end a tag
     *
     * @param buffer the buffer
     */
    protected void endTag(final StringBuilder buffer)
    {
        buffer.append( "</span>");
    }

    /**
     * Juggle the BR symbols so that they always come at the
     * end. This allows me to look for them in other spots.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        // If it's blank then just return blank.
        if(
            text.equals("") &&
            returnCount == 0
        )
        {
            return;
        }

        String realText = text;//StringUtilities.encodeUTF8( text);


        if( noWrapFg == true)
        {
            if(bReplaceBrackets == true)
            {
                realText = realText.replace( " ", "&nbsp;");
            }
        }
        else if( indent)
        {
            if( realText.contains("<BR>"))
            {
                int lastPos = 0;

                StringBuilder buffer2 = new StringBuilder();

                while( true)
                {
                    int pos = realText.indexOf( "<BR>\n", lastPos);

                    if( pos == -1) break;

                    int start = lastPos;

                    while( Character.isWhitespace(realText.charAt( start)) && start < pos)
                    {
                        start++;
                    }

                    String temp = realText.substring( lastPos, start);

                    temp = temp.replace( " ", "&nbsp;");

                    buffer2.append( temp);
                    lastPos = pos + 5;
                    buffer2.append( realText.substring( start, lastPos));
                }

                int start = lastPos;

                while( Character.isWhitespace(realText.charAt( start)) && start < realText.length())
                {
                    start++;
                }

                String temp = realText.substring( lastPos, start);

                temp = temp.replace( " ", "&nbsp;");

                buffer2.append( temp);
                buffer2.append( realText.substring( start));

                realText = buffer2.toString();
            }
        }

        HTMLTheme theme = getTheme();

        if( headerLevel != 0)
        {
            buffer.append( "<H");
            buffer.append( headerLevel);
            buffer.append( ">");
        }

        if( color == null)
        {
            color = theme.getDefaultColor( HTMLTheme.DKEY_FONT_COLOR);
            if(Color.BLACK.equals( color))
            {
                color = null;
            }
        }

        int relativeSize;
        relativeSize = theme.getDefaultInt(
            HTMLTheme.DKEY_FONT_RELATIVE_SIZE,
            0
        );


        if(relativeSize != 0 )
        {
            fontSize = findCorrespondingSize( relativeSize);
        }

        HTMLPage page;
        page = getParentPage();

        boolean bold = false;
        boolean italic = false;

//        String face;
//        face =null;// (String)theme.getDefault(
        //    HTMLTheme.DKEY_FONT_FACE
        //);

        HTMLStyleSheet styleSheet = getStyleSheet();
        if( fontSize != 0 && headerLevel == 0)
        {
            styleSheet.addElement( InternalStyleSheet.STYLE_FONT_SIZE, fontSize +"px");
        }

        if( containsVisibleText)
        {
            if( fontFamily != null)
            {
                styleSheet.addElement( InternalStyleSheet.STYLE_FONT_FAMILY, fontFamily );
            }

            if( color != null)
            {
                styleSheet.setColour( "color", color);
            }

            if(
                isUnderline != null &&
                isUnderline == true
            )
            {
                 styleSheet.addElement( InternalStyleSheet.STYLE_TEXT_DECORATION, "underline" );
            }
        }

        String className= getClassName();

        boolean tagStarted=false;

        if( needTag() || styleSheet.getNumElements() > 0||StringUtilities.isBlank(id) == false||StringUtilities.isBlank(className) == false)
        {
            tagStarted=true;
            startTag(buffer);
        }

        if( tagStarted)
        {
            if(styleSheet.getNumElements() > 0 && page != null)
            {
                page.registerStyleSheet(styleSheet);
            }

            if(StringUtilities.isBlank(id) == false)
            {
                buffer.append(" id=\"");
                buffer.append(id);
                buffer.append( "\"");
            }

            if( styleSheet.getNumElements() > 0 )
            {
                if(
                    StringUtilities.isBlank( getClassName()) &&
                    page != null &&
                    page.isPageCompiled()
                )
                {
                    buffer.append(" class=\"").append(styleSheet.getTarget()).append("\"");
                }
                else
                {
                    buffer.append(" ");
                    buffer.append(styleSheet.toInlineStyleSheet());

                    if( StringUtilities.isBlank( className)==false)
                    {
                        buffer.append(" class=\"").append(className).append("\"");
                    }
                }
            }
            else
            {
                if( StringUtilities.isBlank( className)==false)
                {
                    buffer.append(" class=\"").append(className).append("\"");
                }
            }

            buffer.append( ">");
        }

        if (isStrikethrough == null)
        {
            isStrikethrough = Boolean.FALSE;
        }

        if( containsVisibleText)
        {
            if( isBold == null)
            {
                bold = theme.getDefaultBoolean( HTMLTheme.DKEY_FONT_BOLD);
            }
            else
            {
                bold = isBold;
            }

            if( bold == true )
            {
                buffer.append( "<b>");
            }

            if( isStrikethrough == true )
            {
                buffer.append( "<del>");
            }

            if( isItalic == null)
            {
                italic = theme.getDefaultBoolean( HTMLTheme.DKEY_FONT_ITALIC);
            }
            else
            {
                italic = isItalic;
            }

            if( italic == true )
            {
                buffer.append( "<i>");
            }


        }

        buffer.append(realText);

        if( italic)
        {
            buffer.append( "</i>");
        }

        if( isStrikethrough)
        {
            buffer.append( "</del>");
        }

        if( bold)
        {
            buffer.append( "</b>");
        }

        if( tagStarted)
        {
            endTag(buffer);
        }

        if( headerLevel != 0)
        {
            buffer.append( "</h");
            buffer.append( headerLevel);
            buffer.append( ">");
        }

        for(int i = 0; i < returnCount;i++)
        {
            buffer.append( "<br>\n");
        }
    }

    private static final int FONT_SIZE_LIST[][] = {
        {10,-7},
        {10,-6},
        {10,-5},
        {10,-4},
        {10,-3},
        {10,-2},
        {14,-1},
        {16,0},
        {19,1},
        {24,2},
        {32,3},
        {48,4},
        {48,5},
        {48,6},
        {48,7},
    };

    private int findCorrespondingSize( int relativeSize)
    {
        if( relativeSize < -7 ) return 9;
        if( relativeSize > 7) return 72;

        for (int[] FONT_SIZE_LIST1 : FONT_SIZE_LIST) {
            if (FONT_SIZE_LIST1[1] == relativeSize) {
                return FONT_SIZE_LIST1[0];
            }
        }

        return 12;
    }
/*
    private int findCorrespondingRelativeSize( int size)
    {
        if( size < 9 ) return -2;
        if( size > 48) return 7;

        for( int i = 0; i < FONT_SIZE_LIST.length; i++)
        {
            if(
                (
                    FONT_SIZE_LIST[i][0] == size &&
                    (
                        i + 1 >= FONT_SIZE_LIST.length ||
                        FONT_SIZE_LIST[i][0] != size
                    )
                ) ||
                (
                    i + 1 < FONT_SIZE_LIST.length &&
                    FONT_SIZE_LIST[i + 1][0] > size
                )
            )
            {
                return FONT_SIZE_LIST[i][1];
            }
        }

        return 0;
    }
*/
   /**
     * Set Id
     * @param theId
     */
    public void setId(String theId)
    {
        id = theId;
    }

    private boolean     noWrapFg,
                        indent,
                        bReplaceBrackets,
                        containsVisibleText;

    private Boolean     isBold,
                        isItalic,
                        isUnderline,
                        isStrikethrough;

    private int         headerLevel,
                        fontSize,
                        returnCount;

    private java.awt.Color color;

    private String      oText,
                        text,
                        fontFamily;
}
