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
 *TestGWT1
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */

package com.aspc.remote.html;

import com.aspc.remote.util.misc.StringUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will generate the html for the AjaxField
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component </i>
 *
 *  @author      Paul Smout
 *  @since       June 3, 2008, 7:13 PM
 */
public class HTMLAjaxField extends HTMLComponent
{
    /**
     * Used as a key into onpage datastorage
     */
    public static final String AJAX_FIELD_PARAMS = "AJAX_FIELD_PARAMS";

    /**
     * Used as a key to the flag to indicate whether post compile processing is complete
     */
    public static final String AJAX_FIELD_POST_COMPILED = "AJAX_FIELD_POST_COMPILED";
    private static final String WAITING_GIF = "/images/waiting.gif";
    private final String path;
    private final String dbclass;
    private final String globalkey;
    private String domId = "";
    private final String format;
    private boolean bold;
    private boolean includesTags;
    private int fontSize;
    /**
     * Module name
     */
    public static final String AJAX_FIELD_MODULE_NAME = "com.aspc.gwt.ajaxfield.Ajaxfield";

    /**
     * Parameter Dictionary name
     */
    public static final String AJAX_FIELD_DICTIONARY = "AJAX_FIELD_DICTIONARY";
    /**
     * field count parameter
     */
    public static final String NUM_FIELDS_ON_PAGE = "NUM_FIELDS_ON_PAGE";
    /**
     * DOM id field prefix
     */
    public static final String DOM_ID_FIELD = "DOM_KEY_FIELD";
    /**
     * Path field prefix
     */
    public static final String PATH_FIELD  = "PATH_FIELD";
    /**
     * db class prefix
     */
    public static final String DB_CLASS_FIELD = "DB_CLASS_FIELD";
    /**
     * global key prefix
     */
    public static final String GLOBAL_KEY_FIELD = "GLOBAL_KEY_FIELD";
    /**
     * format prefix
     */
    public static final String FORMAT_FIELD = "FORMAT_FIELD";

    /**
     * Font bold
     */
    public static final String FONT_BOLD = "FONT_BOLD";
    
    /**
     * includes tags.
     */
    public static final String INCLUDES_TAGS = "INCLUDES_TAGS";

    /**
     * Font size
     */
    public static final String FONT_SIZE = "FONT_SIZE";

    /**
     *
     * @param path
     * @param dbclass
     * @param globalkey
     * @param format
     */
    public HTMLAjaxField(final String path, final String dbclass, final String globalkey, final String format)
    {
        assert checkPath( path): "inalid path " + path;
        this.path = path;
        this.dbclass = dbclass;
        this.globalkey = globalkey;
        this.format = format;
    }

    private boolean checkPath( final String path)
    {
        int bracket=path.lastIndexOf("}");
        int last=path.indexOf("]", bracket);
        int start=path.indexOf("[", bracket);
        
        if( last!=-1 && start == -1)
        {
            return false;
        }
        
        return true;
    }
    
     /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser client browser
     */
    @Override
    protected void compile(final ClientBrowser browser)
    {
        HTMLPage page = getParentPage();
        page.addGWT(AJAX_FIELD_MODULE_NAME);
        List<HTMLAjaxFieldHelper> ajaxFields = (List<HTMLAjaxFieldHelper>)page.getWorkingStorage(AJAX_FIELD_PARAMS);
        if(ajaxFields == null)
        {
           ajaxFields = new ArrayList<>();
           page.setWorkingStorage(AJAX_FIELD_PARAMS, ajaxFields);
        }

        domId = page.doGenerateId();
        HTMLAjaxFieldHelper helper = new HTMLAjaxFieldHelper();
        helper.setDbClass(dbclass);
        helper.setDomId(domId);
        helper.setGlobalKey(globalkey);
        helper.setPath(path);
        helper.setFormat(format);
        helper.setBold(bold);
        helper.setIncludesTags(includesTags);
        if (fontSize > 0)
        {
            helper.setFontSize(fontSize);
        }
        ajaxFields.add(helper);

        page.registerPostCompileCallBack(this);
    }

    @Override
    protected void postCompile(ClientBrowser browser)
    {
        super.postCompile(browser);
        HTMLPage page = getParentPage();

        Object flag = page.getFlag(HTMLAjaxField.AJAX_FIELD_POST_COMPILED);
        if ("".equals(flag))
        {
            String dict = buildDataDictionary();
            page.addJavaScript(dict);
            page.putFlag(HTMLAjaxField.AJAX_FIELD_POST_COMPILED,AJAX_FIELD_POST_COMPILED);
        }
    }

    private String buildDataDictionary()
    {
        List<HTMLAjaxFieldHelper> ajaxFields = (List<HTMLAjaxFieldHelper>)
                getParentPage().getWorkingStorage(AJAX_FIELD_PARAMS);
        StringBuilder data = new StringBuilder();
        if(ajaxFields.size() > 0)
        {
            data.append("var " + AJAX_FIELD_DICTIONARY + " = {\n");
            addFieldToDict(data, NUM_FIELDS_ON_PAGE,null,String.valueOf(ajaxFields.size()));
            for(int i = 0; i < ajaxFields.size(); i++)
            {
               int j=i+1;
               addFieldToDict(data, DOM_ID_FIELD,String.valueOf(j),String.valueOf(ajaxFields.get(i).getDomId()));
               addFieldToDict(data, PATH_FIELD,String.valueOf(j),String.valueOf(ajaxFields.get(i).getPath()));
               addFieldToDict(data, DB_CLASS_FIELD,String.valueOf(j),String.valueOf(ajaxFields.get(i).getDbClass()));
               addFieldToDict(data, GLOBAL_KEY_FIELD,String.valueOf(j),String.valueOf(ajaxFields.get(i).getGlobalKey()));
               addFieldToDict(data, FONT_BOLD,String.valueOf(j),String.valueOf(ajaxFields.get(i).isBold()));
               addFieldToDict(data, INCLUDES_TAGS,String.valueOf(j),String.valueOf(ajaxFields.get(i).isIncludesTags()));
               addFieldToDict(data, FONT_SIZE,String.valueOf(j),String.valueOf(ajaxFields.get(i).getFontSize()));

               if (i< (ajaxFields.size() -1 ))
               {
                 addFieldToDict(data, FORMAT_FIELD,String.valueOf(j),String.valueOf(ajaxFields.get(i).getFormat()));
               }
               else
               {
                 addFieldToDict(data, FORMAT_FIELD,String.valueOf(j),String.valueOf(ajaxFields.get(i).getFormat()), true);
               }
            }
            data.append("};");

        }
        return data.toString();
    }
    private void addFieldToDict(StringBuilder sb, String fieldName,String suffix, String value)
    {
        addFieldToDict( sb,  fieldName, suffix,  value,  false);
    }

    private void addFieldToDict(StringBuilder sb, String fieldName,String suffix, String value, boolean last)
    {
         sb.append(fieldName);
         if (suffix!=null)
         {
            sb.append("_");
            sb.append(suffix);
         }
         sb.append(":\"");
         sb.append(value);

         sb.append("\"");
         if (!last)
         {
             sb.append(",");
         }
         sb.append("\n");
    }

    @Override
    protected void iGenerate(ClientBrowser browser, StringBuilder buffer)
    {
        buffer.append("<span id=\"");
        buffer.append(domId);
        String className = getClassName();
        if(StringUtilities.notBlank(className))
        {
            buffer.append("\" class=\"").append(className);
        }
        buffer.append("\" >");
        HTMLImage img  =new HTMLImage(WAITING_GIF);
        img.iGenerate(browser, buffer);
        buffer.append("</span>");
    }

    /**
     * @return the dom id for this field
     */
    public String getDomId()
    {
        return domId;
    }

    /**
     * Is bold
     * @return bold
     */
    public boolean isBold()
    {
        return bold;
    }

    /**
     * Get Font Size
     * @return size
     */
    public int getFontSize()
    {
        return fontSize;
    }

    /**
     * Set Bold
     * @param bold bold
     */
    public void setBold(boolean bold)
    {
        this.bold = bold;
    }
    /**
     * Set includes Tags
     * @param includesTags includes tags
     */
    public void setIncludesTags(boolean includesTags)
    {
        this.includesTags = includesTags;
    }
    /**
     * Font Size
     * @param fontSize fontSize
     */
    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

}

