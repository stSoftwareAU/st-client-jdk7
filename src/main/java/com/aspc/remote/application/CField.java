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
package com.aspc.remote.application;

import org.apache.commons.logging.Log;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import com.aspc.remote.util.misc.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  CField general Error utilities
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       7 December 1996
 */
public class CField extends JPanel implements DocumentListener, FocusListener
{
    //Constructors
    /**
     *
     */
    public CField()
    {
        this( "UNTITLED", 8, true);
    }

    /**
     * 
     * @param labelNm 
     */
    public CField( String labelNm)
    {
        this(labelNm, 8, true);
    }

    /**
     * 
     * @param labelNm 
     * @param count 
     */
    public CField( String labelNm, int count)
    {
        this(labelNm, count, true);
    }

    /**
     * 
     * @param count 
     */
    public CField( int count)
    {
        this("", count, true);
    }

    // With editable flag
    /**
     * 
     * @param labelNm 
     * @param editFg 
     */
    public CField( String labelNm, boolean editFg)
    {
        this(labelNm, 8, editFg);
    }

    /**
     * 
     * @param count 
     * @param editFg 
     */
    public CField( int count, boolean editFg)
    {
        this("", count, editFg);
    }

    /**
     * 
     * @param labelNm 
     * @param count 
     * @param editFg 
     */
    public CField( String labelNm, int count, boolean editFg)//NOPMD
    {
        super(true);
        notifyValidate = new Vector();//NOPMD
        setOpaque(true);

        //Create the field
        field = makeField( count);//NOPMD
        add( field);
        field.setBorder( BorderFactory.createLoweredBevelBorder());

        field.getDocument().addDocumentListener(this);

        field.addFocusListener( this);

        setEditable( editableFg);//NOPMD

        setTitle(labelNm);//NOPMD
        setAlignmentX(0);
        setPicture( "S");//NOPMD
    }

    //input
    /**
     * 
     * @param i 
     */
    public void setValue( int i)
    {
        setText( "" + i);
    }

    // Output
    /**
     * 
     * @return the value
     */
    public Object getObject()
    {
        doFieldChange( rawText, true);

        return rawObject;
    }

    /**
     * 
     * @return the value
     */
    public synchronized String getText()
    {
        Object o;

        o = getObject( );

        if( o == null ) return "";

        return o.toString();
    }


    /**
     * 
     * @return the value
     */
    public int getInt()
    {
        try
        {
            if( type.equals( "I" ) || type.equals( "F"))
            {
                Object o;
                o = getObject();
                if( o == null) return 0;

                return ((Number)o).intValue();
            }
            else
            {
                throw new Exception( "This field type '" + type + "' is not a number field");
            }
        }
        catch( Exception e)
        {
            programmerError = e;
            setupFieldUI();
            return Integer.MIN_VALUE;
        }
    }

    /**
     * 
     * @param pic 
     */
    public void setPicture( String pic)
    {
        String temp,
               validTypes = "IDFS";
        picture = pic;
        try
        {
            temp = pic.substring(0, 1).toUpperCase();
            formats = null;

            if( validTypes.contains(temp))
            {
                type = temp;
            }
            else
            {
                throw new Exception( "Not a valid field type '" + temp + "'");
            }

            int l;

            l = pic.indexOf("{");

            if( l != -1)
            {
                temp = pic.substring( l + 1, pic.lastIndexOf( "}"));
                LOGGER.debug( this + ".setPicture(\"" + pic + "\") is " + temp);

                if( type.equals( "S"))
                {
                    if( temp.contains("U"))
                    {
                        toUpperFg = true;
                    }
                    if( temp.contains("L"))
                    {
                        toLowerFg = true;
                    }
                    if( temp.contains("T"))
                    {
                        trimFg = true;
                    }
                }
                else
                {
                    formats = new Vector();//NOPMD
                    StringTokenizer st = new StringTokenizer(temp, "|");
                    while( st.hasMoreTokens())
                    {
                        String f;
                        f = st.nextToken();

                        if( type.equals( "I") || type.equals( "F"))
                        {
                            formats.addElement(
                                new DecimalFormat( f)
                            );
                        }
                        else if( type.equals( "D"))
                        {
                            SimpleDateFormat df;
                            df = new SimpleDateFormat( f);//NOPMD

                            formats.addElement(df);
                        }
                    }
                }
            }
            else
            {
                if( type.equals( "I"))
                {
                    setPicture( "I{#,##0}");
                }
                else if( type.equals( "F"))
                {
                    setPicture( "F{#,##0.00}");
                }
                else if( type.equals( "D"))
                {
                    setPicture( "D{dd MMM yyyy|dd MMM yy|dd/MM/yyyy|dd/MM/yy|dd-MM-yyyy|dd-MM-yy}");
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.info( "Error set field picture ", e);
            programmerError = e;
            setupFieldUI( );
        }
    }

    /**
     * 
     * @param e 
     */
    public void setProgrammerError( Exception e)
    {
        programmerError = e;
        setupFieldUI();
    }

    /**
     * 
     * @param count 
     * @return the value
     */
    protected JTextField makeField( int count)
    {
        return new JTextField( count);
    }

    /**
     * 
     * @return the value
     */
    public JTextField getValue()
    {
        return field;
    }


    // Methods
    @Override
    public void requestFocus()
    {
        field.requestFocus();
    }

    /**
     * 
     * @param inFlag 
     */
    @Override
    public void setEnabled( boolean inFlag)
    {
        if( label != null) label.setEnabled( inFlag);
        field.setEnabled( inFlag);
    }

    /**
     * 
     * @param inFlag 
     */
    public void setEditable( boolean inFlag)
    {
        editableFg = inFlag;

        field.setEditable( editableFg);

        setupFieldUI();

    }

    /**
     * 
     * @param text 
     */
    @Override
    public void setToolTipText( String text)
    {
        field.setToolTipText( text);
        originalToolTipText = text;
    }

    @Override
    public void updateUI()
    {
        super.updateUI();
        setupFieldUI();
    }

    /**
     * Sets the fields text applying any formating as required {<B>Thread Safe</B>}.
     * @param inString 
     */
    public synchronized void setText( String inString)
    {
        doFieldChange( inString, true);
        doUpdate();
    }

    /**
     * 
     * @return the value
     */
    public String getTitle( )
    {
        String title = "";
        if( label != null)
        {
            title = label.getText();
        }

        return title;
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    /**
     * 
     * @param inTitle 
     */
    public void setTitle( String inTitle)
    {
        if( label != null)
        {
            remove( label);
        }

        if( inTitle.equals( "") == false)
        {
            label = new FLabel( inTitle);
            remove(field);

            add( label);
            add( field);
        }
    }

    /**
     * 
     * @param listener 
     */
    public void addValidationListener( ValidationListener listener)
    {
        notifyValidate.addElement( listener);
    }

    void validateField()
    {
        int i;
        boolean successFg = true;

        ValidationEvent event;
        ValidationListener listener;

        event = new ValidationEvent( this);

        for( i = 0; i < notifyValidate.size() && successFg == true; i++)
        {
            listener = (ValidationListener)notifyValidate.elementAt( i);
            successFg = listener.performValidation( event);
        }

        if( successFg != true)
        {
            requestFocus();
        }
    }

    // Document Listener Event.
    /**
     * 
     * @param de 
     */
    @Override
    public void changedUpdate(DocumentEvent de)
    {
        //if( debug) LOGGER.info( this + ".changedUpdate()");
        if( field.hasFocus( ))
        {
            doFieldChange( field.getText(), false);
        }
    }

    /**
     * 
     * @param de 
     */
    @Override
    public void insertUpdate(DocumentEvent de)
    {
        //if( debug) LOGGER.info( this + ".insertUpdate()");
        if( field.hasFocus( ))
        {
            doFieldChange( field.getText(), false);
        }
    }

    /**
     * 
     * @param de 
     */
    @Override
    public void removeUpdate(DocumentEvent de)
    {
        //if( debug) LOGGER.info( this + ".removeUpdate()");
        if( field.hasFocus( ))
        {
            doFieldChange( field.getText(), false);
        }
    }

    /**
     * 
     * @param e 
     */
    @Override
    public void focusLost( FocusEvent e)
    {
        //if( debug) LOGGER.info( this + ".focusLost()");
        doFieldChange( rawText, true);
        doUpdate();

    }

    /**
     * 
     * @param fe 
     */
    @Override
    public void focusGained( FocusEvent fe)
    {
        /*if( debug)
        {
            LOGGER.info( this + ".focusGained()");
            LOGGER.info( "display = " + displayText);
            LOGGER.info( "raw = " + rawText);
        }
        */
        doUpdate();
    }

    /**
     * 
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        if( label != null)
        {
            return "CField(\"" + label.getText() + "\")";
        }

        return "CField()";
    }

    // Privates
    private void doFieldChange( String text, boolean full_fg)//NOPMD
    {
        userError = null;
        rawObject = null;
        if( text == null) text = "";
        rawText = text;
        displayText = text;

        text = text.trim();

        if( full_fg == false) return;

        try
        {
            if( formats != null)
            {
                if( text.equals( "") == false)
                {
                    if( type.equals( "D"))
                    {
                        for( int i = 0; i < formats.size(); i++)
                        {
                            try
                            {
                                Format f;
                                f = (Format)formats.elementAt(i);

                                rawObject = f.parseObject( text);

                                if(rawObject instanceof Date)
                                {
                                    GregorianCalendar gc =DateUtil.makeGC( TimeZone.getDefault(), (Date)rawObject);

                                    if( gc.get( Calendar.YEAR) < 1800)
                                    {
                                        rawObject = null;
                                        continue;
                                    }
                                }
                                LOGGER.debug( "Formated '" + text + "' with " + (i+1) + " of " + formats.size());
                                break;
                            }
                            catch( Exception e)
                            {
                                LOGGER.info( "error", e);
                            }
                        }
                    }
                    else
                    {
                        //final StringUtilities su = new StringUtilities();

                        text = StringUtilities.replace( text, ",","");
                        text = text.replace( ")", "");
                        text = text.replace( "(", "-");

                        try
                        {
                            rawObject = new Double( text);
                        }
                        catch( Exception e)
                        {
                            LOGGER.debug( "parse", e);
                        }
                    }

                    if( rawObject == null)
                    {
                         throw new Exception( "Must be in the format '" + picture + "'");
                    }

                    displayText =((Format)formats.elementAt(0)).format(rawObject);
                }
            }
            else
            {
                if( toUpperFg == true)
                {
                    displayText = displayText.toUpperCase();
                }
                if( toLowerFg == true)
                {
                    displayText = displayText.toLowerCase();
                }
                if( trimFg == true)
                {
                    displayText = displayText.trim();
                }
                rawObject = rawText;
            }
        }
        catch( Exception e)
        {
            LOGGER.info( this + ".doFieldChange( \"" + text +"\") ", e);
            userError = e;
        }
    }

    private void doUpdate()
    {
        // Because I get a Remove then an Insert in One loop just check
        // that I'm not just going around in circles.
        Runnable r = new Runnable ()
        {
            @Override
            public void run()
            {
                String newText,
                       oldText;

                try
                {
                    oldText = field.getText();
                    if( field.hasFocus( ))
                    {
                        newText = rawText;
                    }
                    else
                    {
                        newText = displayText;
                    }

                    if( newText != null && newText.equals( oldText) == false)
                    {
                        field.setText( newText);
                    }
                }
                catch( Exception e)
                {
                    programmerError = e;
                }

                setupFieldUI();
            }
        };

        SwingUtilities.invokeLater(r);
    }

    private void setupFieldUI()
    {
        Color bgColour;

        if( field == null || type == null) return;

        if( editableFg == true)
        {
            bgColour = UIManager.getColor( "TextField.background");
        }
        else
        {
            bgColour = UIManager.getColor( "Label.background");
        }

        if( bgColour == null)
        {
            programmerError = new Exception( "Couldn't find colour");
        }

        Exception displayException = null;
        if( programmerError != null)
        {
            bgColour = Color.magenta;
            displayException = programmerError;
            field.setToolTipText( programmerError.getMessage());
            wasError = true;
        }
        else if( userError != null)
        {
            displayException = userError;
            bgColour = Color.red;
            wasError = true;
        }
        else if( wasError == true)
        {
            wasError = false;
            field.setToolTipText( originalToolTipText);
        }

        if( displayException != null)
        {
            String msg;
            msg = displayException.getMessage();
            if( StringUtilities.isBlank(msg))
            {
                msg = displayException.toString();
            }

            LOGGER.debug( "Set tool tip '" + msg + "'");

            field.setToolTipText( msg);
        }

        field.setBackground( bgColour);

        if( type.equals( "I") || type.equals( "F"))
        {
            field.setForeground( Color.black);
            if( field.hasFocus())
            {
                ;//field.setAlignment();
            }
            else
            {
                if( rawObject != null)
                {
                    if( ((Number)rawObject).doubleValue() < 0)
                    {
                        field.setForeground( Color.red);
                    }
                }
            }
        }
    }


    private JTextField          field;
    private FLabel              label                   = null;
    private boolean             editableFg              = true,
                                toUpperFg,
                                toLowerFg,
                                trimFg,
                                wasError;

    private Exception           programmerError,
                                userError;

    private String              type,
                                picture,
                                rawText,
                                displayText,
                                originalToolTipText;

    private Object              rawObject;

    private Vector              formats;

    private Vector              notifyValidate;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CField");//#LOGGER-NOPMD
}
