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
package com.aspc.remote.tail;

import com.aspc.remote.util.misc.*;
import java.awt.Color;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.logging.Log;

/**
 *  Tail output Stream
 *
 * <br>
 * <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel
 *  @since       2 July 2009
 */
public class TailTextPane
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.tail.TailTextPane");//#LOGGER-NOPMD

   // private final JTextPane panel;
    private final StyledDocument sd;
    private static final int MAX_LENGTH=100 * 1024;

    /**
     *
     * @param panel the panel
     */
    public TailTextPane( final JTextPane panel)
    {
        this.sd = panel.getStyledDocument();
    }

    private static final Pattern LEVEL_PATTERN = Pattern.compile("[0-9\\- :,]+\\|(INFO|WARN|FATAL|ERROR)\\|.+\\|.*");
    private Color lastColor;
    protected String editText(final TailLine line, final SimpleAttributeSet attributes )
    {

        //[0-9\- :,]+\|(INFO|WARN|FATAL|ERROR)\|.+\|.*
        Color color=line.color;
        String txt=line.text;
        String editTXT=txt;
        Matcher m = LEVEL_PATTERN.matcher(txt.trim());

        if( m.matches())
        {
            int start=txt.indexOf("|");
            int end=txt.indexOf("|", start+1);

            String level = txt.substring(start+1, end);

            if( level.equals("FATAL"))
            {
                Toolkit.getDefaultToolkit().beep();
                color=Color.RED;
                lastColor=color;
                StyleConstants.setBackground(attributes, Color.YELLOW);
            }
            else if( level.equals("ERROR"))
            {
                color=Color.RED;
                lastColor=color;
            }
            else if( level.equals("WARN"))
            {
                color=Color.ORANGE;
                lastColor=color;
            }
            else
            {
                lastColor=null;
            }

            editTXT=txt.substring(end + 1);
        }
        else if( color==null)
        {
            color=lastColor;
        }

        if( color != null)
        {
            StyleConstants.setForeground(attributes, color);
        }

        return editTXT;
    }

    public void addLine(final TailLine line) throws InterruptedException, InvocationTargetException
    {
        SimpleAttributeSet attributes =new SimpleAttributeSet();

        String editText=editText( line, attributes);

        SwingUtilities.invokeAndWait(
            new RunnableImpl(sd, editText, attributes)
        );
    }

    private static class RunnableImpl implements Runnable
    {

        private final String text;
        private final SimpleAttributeSet attributes;
        private final StyledDocument sd;

        RunnableImpl(final StyledDocument sd, final String text, final SimpleAttributeSet attributes)
        {
            this.sd=sd;
            this.text = text;
            this.attributes=attributes;
        }

        @Override
        public void run()
        {
            try
            {
                int len = text.length();

                int textLen = sd.getLength();

                if( len + textLen > MAX_LENGTH)
                {
                    int removeLen = (len + textLen) - MAX_LENGTH;
                    if( removeLen < 2048) removeLen=2048;
                    if( removeLen > textLen) removeLen=textLen;

                    sd.remove(0, removeLen);
                    textLen-=removeLen;
                }

                sd.insertString(textLen, text, attributes);

            }
            catch( Exception e)
            {
                LOGGER.warn( "couldn't append " + text, e);
            }
        }
    }
}
