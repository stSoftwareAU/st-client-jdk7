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
package com.aspc.remote.html.input;

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.internal.HTMLFormComponent;
import com.aspc.remote.html.internal.HandlesSingleClick;
import com.aspc.remote.html.internal.InternalEvent;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.util.misc.StringUtilities;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * .HTML Input
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       26 February 1998
 */
public class HTMLInput extends  HTMLFormComponent implements HandlesSingleClick
{    
    /**
     * The default length for email.
     * www.w3schools.com/tags/att_input_size.asp
     */
    public static final int DEFAULT_EMAIL_LENGTH=35;
    
    /**
     * http://www.w3.org/TR/html5/the-input-element.html#attr-input-type
     */
    @SuppressWarnings("PublicInnerClass")
    public enum Type
    {
        /** An arbitrary string */
        HIDDEN ("hidden"),
        /** Text with no line breaks */
        TEXT ("text"),
        /** Search field */
        SEARCH ("search"),
        /** Telephone */
        TEL ("tel"),
        /** An absolute IRI */
        URL ("url"),
        /** An e-mail address or list of e-mail addresses */
        EMAIL ("email"),
        /** Text field that obscures data entry */
        PASSWORD ("password"),
        /**
         * A date and time (year, month, day, hour, minute, second, fraction of a second) with the time zone set to UTC
         * A date and time control
         */
        DATETIME ("datetime"),
        /** A date (year, month, day) with no time zone */
        DATE ("date"),
        /** A date consisting of a year and a month with no time zone */
        MONTH ("month"),
        /** A date consisting of a week-year number and a week number with no time zone */
        WEEK ("week"),
        /** A time (hour, minute, seconds, fractional seconds) with no time zone */
        TIME ("time"),
        /** A date and time (year, month, day, hour, minute, second, fraction of a second) with no time zone */
        DATETIME_LOCAL ("datetime-local"),
        /** A numerical value */
        NUMBER ("number"),
        /** A numerical value, with the extra semantic that the exact value is not important */
        RANGE ("range"),
        /** An sRGB color with 8-bit red, green, and blue components */
        COLOR ("color"),
        /** A check box */
        CHECKBOX ("checkbox"),
        /** A radio button */
        RADIO ("radio"),
        /** Zero or more files each with a MIME type and optionally a file name */
        FILE ("file"),
        /** An enumerated value, with the extra semantic that it must be the last value selected and initiates form submission */
        SUBMIT ("submit"),
        /** A coordinate, relative to a particular image's size, with the extra semantic that it must be the last value selected and initiates form submission */
        IMAGE ("image"),
        /** reset */
        RESET ("reset"),
        /** button */
        BUTTON ("button");

        /**
         * the type name
         */
        public final String name;

        Type( final String name)
        {
            this.name=name;
        }
    }

    private String pattern;
    private Type type= Type.TEXT;

    private Integer fieldSize;
    /**
     * the maximum length
     */
    protected int  maxLength;
    private Number minNumber;
    private Number maxNumber;
    private Number stepNumber;
    
    private String placeHolder;

    /**
     *
     */
    protected boolean   readOnlyFg,
                        disabledFg,
                        invisibleFg,
                        forceTabIndex;

    /**
     * the value
     */
    protected String    value;

    /**
     * Auto completer
     */
    protected boolean   useAutoCompleter=false;
    private boolean     autoCompleterUsed=false;
    private String suggestId;
    private String lookupDisplayName;
    private String lookupClass;
    private String lookupCode;
    private String inputFieldName;
    private String gInputFieldName;
    private String inputFieldType;
    private String autoCompFilter;
    private boolean disableRefresh;
    private boolean required;

    /**
     *
     * @param name
     */
    public HTMLInput(final @Nonnull String name)
    {
        iSetName(name);

        appendClassName(STYLE_STS_FIELD);
    }

    /**
     *
     * @param name
     * @param value the value
     */
    public HTMLInput(final @Nonnull String name,  final @Nullable String value)
    {
        this( name);

        setValue( value);
    }

    /**
     *
     * @param name name
     * @param value value
     * @param lookupDisplayName lookupDisplayName
     * @param lookupClass lookupClass
     * @param lookupCode lookupCode
     * @param inputFieldName inputFieldName
     * @param gInputFieldName gInputFieldName
     * @param fieldType fieldType
     * @param autoCompFilter autoCompFilter
     * @param disableRefresh disable refresh
     */
    public HTMLInput(
        final @Nonnull String name,
        final @Nullable String value,
        final String lookupDisplayName,
        final String lookupClass,
        final String lookupCode,
        final String inputFieldName,
        final String gInputFieldName,
        final String fieldType,
        final String autoCompFilter,
        final boolean disableRefresh
    )
    {
        this( name);
        setValue( value);
        this.lookupDisplayName = lookupDisplayName;
        this.lookupClass = lookupClass;
        this.lookupCode = lookupCode;
        this.inputFieldName = inputFieldName;
        this.gInputFieldName = gInputFieldName;
        this.inputFieldType = fieldType;
        this.autoCompFilter = autoCompFilter;
        this.disableRefresh = disableRefresh;
    }

    /**
     * get the minimum number
     * @return the minimum number
     */
    @CheckForNull @Nullable
    public Number getMinNumber( )
    {
        return minNumber;
    }

    /**
     * get the maximum number
     * @return the maximum number
     */
    @CheckForNull @Nullable
    public Number getMaxNumber( )
    {
        return maxNumber;
    }

    /**
     * set the minimum number
     * @param min the minimum number
     */
    public void setMinNumber( final Number min)
    {
        minNumber=min;
    }

    /**
     * set the step
     * @param step how many to step
     */
    public void setStep( final Number step)
    {
        this.stepNumber=step;
    }
    
    /**
     * set the maximum number
     * @param max the maximum number
     */
    public void setMaxNumber( final Number max)
    {
        maxNumber=max;
    }
    
    public void setRequired( final boolean flag)
    {
        required=flag;
    }
    
    /**
     * set the placeholder string
     * @param plSring the place holder string
     */
    public void setPlaceHolder( final String plSring)
    {
        placeHolder=plSring;
    }

    /**
     * get the placeholder string
     * @return the place holder string
     */
    @CheckForNull @Nullable
    public String getPlaceHolder()
    {
        return placeHolder;
    }
    
    /**
     * The field type
     * @param type the field type
     */
    public void setType( final Type type)
    {
        assert type != null: "type must not be null";
        assert type != Type.NUMBER || StringUtilities.isBlank(pattern) : "Must not set type=number & a pattern";

        this.type=type;
    }

    /**
     * Set the pattern to use
     * @param pattern the pattern
     */
    public void setPattern( final String pattern)
    {
        assert type != Type.NUMBER || StringUtilities.isBlank(pattern) : "Must not set type=number & a pattern";
        this.pattern=pattern;
    }

    /**
     * Prevent the click of the mouse from propagation up the dom and kicking off other things.
     */
    @Override
    public void cancelClickBubble()
    {
        cancelBubble = true;
        touch();
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String getId()
    {
        return id;
    }

    public void setID( final String id)
    {
        iSetId(id);
    }
    
    /**
     *
     * @param event the event
     * @param script
     */
    public void addEvent( final HTMLEvent event, final String script )
    {
        iAddEvent( event, script);
    }

    /** {@inheritDoc } */
    @Override
    public void addOnChangeEvent( final String call)
    {
        iAddEvent( new InternalEvent( "onChange", call), "");
    }

    /** {@inheritDoc } */
    @Override
    public void addOnChangeEvent( final String call, final String script)
    {
        iAddEvent( new InternalEvent( "onChange", call), script);
    }

    /**
     *
     * @param call
     */
    public void addOnBlurEvent( String call)
    {
        iAddEvent( new InternalEvent( "onBlur", call), "");
    }
        
    /**
     * add the on invalid method
     * @param call
     */
    public void addOnInvalidEvent( final String call)
    {
        iAddEvent( new InternalEvent( "onInvalid", call), "");
    }
        
    /**
     * add the on input method
     * @param call
     */
    public void addOnInputEvent( final String call)
    {
        iAddEvent( new InternalEvent( "onInput", call), "");
    }

    /**
     *
     * @param call
     */
    public void addOnFocusEvent( String call)
    {
        iAddEvent( new InternalEvent( "onFocus", call), "");
    }

    /**
     *
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param value the value
     */
    @Override
    public final void setValue( final @Nullable String value)
    {
        assert type != Type.DATE || (value == null || StringUtilities.isBlank(value) || value.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")): name + " date value is in the wrong format " + value;
        assert (invisibleFg || value==null || value.matches("[^\n\r]*")): name + " input values may not contain new lines or carriage returns: " + value;
        assert value==null|| StringUtilities.validCharactersHTML(value): name + " input value contains invalid characters: " + (value == null ? "NULL" : StringUtilities.encode(value));
        this.value = value;
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String getValue( )
    {
        if( value == null) return "";

        return value;
    }

    /**
     *
     * @param size
     */
    public void setSize( final @Nonnegative int size)
    {
        assert size > 0: "field size must be positive integer " + size;
        if( size > 0)
        {
            this.fieldSize = size;
        }
    }


    /**
     *
     * @param size
     */
    public void setMaxLength( final @Nonnegative int size)
    {
        assert size>=0: "size must be non-negative was: " + size;
        this.maxLength = size;
    }

    /**
     *
     * @param fg
     */
    public void setForceTabIndex( final boolean fg)
    {
        this.forceTabIndex = fg;
    }

    /**
     *
     * @param fg
     *
    public void setUseCurrentFont( boolean fg)
    {
        useCurrentFont = fg;
    }*/

    /**
     * generate the raw HTML for this component.
     *
     * http://www.wufoo.com/html5/
     * 
     * http://html5test.com/compare/browser/chrome28/ff24/ie11.html
     * 
     * http://caniuse.com/input-number
     * 
     * <pre>
     *     Input type   IE  Firefox Opera   Chrome  Safari
     *     ==========   === ======= =====   ======  ======
     *     email         10     4.0   9.0     10.0      No
     *     url           10     4.0   9.0     10.0      No
     *     number        10      No   9.0      7.0     5.1
     *     range         10      No   9.0      4.0     4.0
     *     Date pickers  No      No   9.0     10.0     5.1
     *     search        10     4.0  11.0     10.0      No
     *     color         No      No  11.0       12      No
     * </pre>
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    @SuppressWarnings("SuspiciousIndentAfterControlStatement")
    protected void iGenerate(final @Nonnull ClientBrowser browser, final @Nonnull StringBuilder buffer)
    {
        if (autoCompleterUsed)
        {
            buffer.append("<div id=\"").append(suggestId).append("\"></div>");
        }

        Type realType = type;

        if (invisibleFg == true || autoCompleterUsed == true)
        {
            realType=Type.HIDDEN;
        }
        String tmpPattern = pattern;
        
        if( null != realType )
        switch (realType) {
            case NUMBER:
                if( browser.supportsTypeNumber() == false)
                {
                    if( stepNumber instanceof Integer)
                    {
                        if( minNumber !=null && minNumber.doubleValue()>=0)                                            
                        {
                            tmpPattern="[0-9]*";
                        }
                        else
                        {
                            tmpPattern="(-|[0-9])*";
                        }
                        
                        if( fieldSize == null)
                        {
                            int calSize=10;
                            if( maxNumber != null)
                            {
                                calSize = maxNumber.toString().length();
                            }
                            if( minNumber != null)
                            {
                                int tmpSize = minNumber.toString().length();
                                if( tmpSize > calSize)
                                {
                                    calSize=tmpSize;
                                }
                            }
                            
                            fieldSize=calSize;
                        }
                    }
                    else
                    {
                        if( minNumber !=null && minNumber.doubleValue()>=0)
                        {
                            tmpPattern="(\\.|[0-9])*";
                        }
                        else
                        {
                            tmpPattern="(\\.|-|[0-9])*";
                        }
                    }
                    realType=Type.TEXT;
                }   break;
            case EMAIL:
                if( browser.supportsTypeEmail() == false)
                {
                    if( fieldSize == null)
                    {
                        fieldSize= DEFAULT_EMAIL_LENGTH;                                            
                    }
                    realType=Type.TEXT;
                }   break;
            case URL:
                if( browser.supportsTypeURL() == false)
                {
                    if( fieldSize == null)
                    {
                        fieldSize= 40;
                    }
                    realType=Type.TEXT;
                }   break;
            case DATE:
            case DATETIME:
            case DATETIME_LOCAL:
                if(browser.supportsTypeDate() == false)
                {
                    realType=Type.TEXT;
                }   break;
            case SEARCH:
                if(browser.supportsTypeSearch() == false)
                {
                    realType=Type.TEXT;
                }   break;
            case TEL:
                if( StringUtilities.isBlank(tmpPattern))
                {
                    tmpPattern=StringUtilities.PHONE_REGEX.pattern();
                }   break;
            default:
                break;
        }
        
        assert realType != Type.DATE || StringUtilities.isBlank(value) || value.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]"): "date value is in the wrong format " + value;
        assert type == Type.NUMBER || (type != Type.NUMBER && stepNumber ==null): "Can not have a step for non number types " + stepNumber;

        buffer.append( "<input");

        if (disabledFg)
        {
            if (forceTabIndex == false)
            {
                setTabIndex(-1);
            }
        }

        String temp="";
        if (value != null && StringUtilities.isBlank(value) == false)
        {
            temp=value;
        }
        
        temp = StringUtilities.encodeHTML(temp);
       
        if( "".equals(temp) == false)
        {
            buffer.append(" value=\"").append(temp).append("\"");
        }
        
        if( realType != Type.HIDDEN)
        {
            /**
             * readonly & disabled seem only to work with IE4 & above
             */
            if (readOnlyFg == true)
            {
                buffer.append(" READONLY");
            }

            if (disabledFg == true)
            {
                if( browser.canHandleDisabledInput())
                {
                    buffer.append(" DISABLED");
                }
            }

            if( null != realType)
            switch (realType) {
                case NUMBER:
                    Number lenMaxValue=maxNumber;
                    if( lenMaxValue == null)
                    {
                        lenMaxValue=Long.MAX_VALUE;
                    }   Number lenMinValue=minNumber;
                    if( lenMinValue == null)
                    {
                        lenMinValue=Long.MIN_VALUE;
                    }   int longMaxLength = lenMaxValue.toString().length();
                    int longMinLength = lenMinValue.toString().length();
                    if(lenMaxValue.longValue() < 0)
                    {
                        longMaxLength--;
                    }   if(lenMinValue.longValue() < 0)
                    {
                        longMinLength--;
                    }   Number tmpMin = minNumber;
                    Number tmpMax = maxNumber;
                    if( fieldSize != null)
                    {
                        StringBuilder sb = new StringBuilder("");
                        
                        if (fieldSize >= longMinLength)
                        {
                            sb.append(Long.toString(Long.MIN_VALUE));
                        }
                        else
                        {
                            sb.append("-");
                            while( sb.length() < fieldSize)
                            {
                                sb.append( "9");
                            }
                        }
                        
                        Long sMin;
                        if( sb.length() > 1)
                        {
                            sMin =new Long( sb.toString());
                        }
                        else
                        {
                            sMin=new Long(0);
                        }
                        
                        if( tmpMin == null || sMin > tmpMin.longValue())
                        {
                            tmpMin = sMin;
                        }
                        
                        sb = new StringBuilder( "");
                        
                        if (fieldSize >= longMaxLength)
                        {
                            sb.append(Long.toString(Long.MAX_VALUE));
                        }
                        else
                        {
                            while( sb.length() < fieldSize)
                            {
                                sb.append( "9");
                            }
                        }
                        
                        Long sMax = new Long( sb.toString());
                        
                        if( tmpMax == null || sMax < tmpMax.longValue())
                        {
                            tmpMax = sMax;
                        }
                    }   if( tmpMin != null) buffer.append( " min=").append( tmpMin);
                    if( tmpMax != null) buffer.append( " max=").append( tmpMax);
                    if( stepNumber != null)
                    {
                        buffer.append( " step=").append( stepNumber);
                    }
                    else
                    {
                        /*
                        * http://blog.isotoma.com/2012/03/html5-input-typenumber-and-decimalsfloats-in-chrome/
                        */
                        buffer.append( " step=\"any\"");
                    }   
                    break;
                case DATE:
                case DATETIME:
                case DATETIME_LOCAL:
                    break;
                default:
                    Integer tmpSize = fieldSize;
                    if( tmpSize == null) tmpSize=10;
                    buffer.append(" size=").append(tmpSize);
                    if (maxLength > 0)
                    {
                        buffer.append(" MAXLENGTH=").append(maxLength);
                    }   if( StringUtilities.isBlank(tmpPattern) == false)
                    {
                        String ePattern= tmpPattern;//.replace("\\", "\\\\");
                        ePattern= ePattern.replace("\"", "\\\"");
                        ePattern= ePattern.replace("&", "&amp;");
                        buffer.append(" pattern=\"");
                        buffer.append( ePattern);
                        buffer.append( "\"");
                    }   break;
            }
        }

        if( realType != null)
        {
            buffer.append(" type=\"").append(realType.name).append("\"");
        }
        
        if( realType != Type.HIDDEN)
        {
            if( StringUtilities.notBlank(placeHolder ))
            {
                buffer.append(" placeholder=\"").append(StringUtilities.encodeHTML(placeHolder)).append("\"");
            }

            if( required)
            {
                buffer.append(" required");
            }
        }
        
        iGenerateAttributes(browser, buffer);

        buffer.append("/>");
        
        if(realType == Type.PASSWORD)
        {
            buffer.append("<input type=\"password\" style=\"display: none;\" />");
        }

    }

    /**
     * Get HTML String
     * @param browser browser
     * @return string
     */
    @CheckReturnValue @Nonnull
    public String getHtmlString(final @Nonnull ClientBrowser browser)
    {
        StringBuilder buffer = new StringBuilder();
        iGenerate(browser , buffer);
        return buffer.toString();
    }
    
    
    /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser
     */
    @Override
    protected void compile( final @Nonnull ClientBrowser browser)
    {
        if( invisibleFg == false)
        {
            if (useAutoCompleter && disabledFg == false && browser.canHandleGWT())
            {
                HTMLPage page = getParentPage();
                page.addGWT("com.aspc.gwt.autocompleter.AutoCompleter");

                String counter = page.getFlag("AutoCompleter:counter");
                int tmpID = 1;

                if( StringUtilities.isBlank( counter) == false)
                {
                    tmpID = Integer.parseInt(counter);
                    tmpID++;
                }

                page.putFlag("AutoCompleter:counter", "" + tmpID);

                suggestId="S" + tmpID;

                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_FIELD_NAME", getId());
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "VALUE", getValue());
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_DISPLAY_NAME", lookupDisplayName);
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_CLASS", lookupClass);
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_CODE", lookupCode);
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "INPUT_FIELD_NAME", inputFieldName);
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "GINPUT_FIELD_NAME", gInputFieldName);

                int lSize = 10;
                if( fieldSize != null) lSize=fieldSize;

                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_FIELD_LENGHT",String.valueOf(lSize));
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_FIELD_TYPE", inputFieldType);
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_AUTOCOMP_FILTER", autoCompFilter);
                if (disableRefresh)
                {
                    page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_FIELD_DISABLE_REFRESH", "Y");
                }
                else
                {
                    page.addToDictionary("AUTOCOMPLETER_" +suggestId, "LOOKUP_FIELD_DISABLE_REFRESH", "N");
                }
                String focus = "FALSE";
                if( tabIndex != null && tabIndex == 1)
                {
                     focus = "TRUE";
                }
                page.addToDictionary("AUTOCOMPLETER_" +suggestId, "INIT_FOCUS", focus);

                page.addToList("AUTOCOMPLETER", suggestId);

                autoCompleterUsed=true;
            }
        }

        if (disabledFg == true)
        {
            if( browser.canHandleDisabledInput()==false)
            {
                appendClassName("disabled");
            }
        }
        super.compile( browser);
    }

    /**
     *
     * @param flag
     */
    public void setPassword( final boolean flag)
    {
        if( flag==true)
        {
            type= Type.PASSWORD;
        }
        else if( type == Type.PASSWORD)
        {
            type=Type.TEXT;
        }
    }

    /**
     * Hides the field.
     * @param flag
     */
    public void setInvisible( final boolean flag)
    {
        invisibleFg = flag;
    }
    
    /**
     * is invisible
     * @return true if invisible.
     */
    @CheckReturnValue
    public boolean isInvisible()
    {
        return invisibleFg;
    }

    /**
     * Disables this input.
     * @param flag
     */
    public void setDisabled( final boolean flag)
    {
        disabledFg = flag;
    }

    /**
     * Sets the field to READONLY
     * @param flag
     */
    public void setReadOnly( final boolean flag)
    {
        readOnlyFg = flag;
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue
    public boolean isReadOnly( )
    {
        return readOnlyFg;
    }

/**
     * Is this is Disabled
     * @return TRUE if disabled
     */
    @Override @CheckReturnValue
    public boolean isDisabled()
    {
        return disabledFg;
    }


     /**
     * the use of auto completer GWT
     * @param flag true to disable.
     */
    public void setUseAutoCompleter( final boolean flag)
    {
        useAutoCompleter=flag;
    }

}
