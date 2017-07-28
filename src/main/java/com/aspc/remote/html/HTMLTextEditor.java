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
import com.aspc.remote.html.input.HTMLInput;
import com.aspc.remote.javaspell.ASpellWrapper;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.json.JSONArray;

/**
 *  HTMLTextAreaEditor
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Jason McGrath
 *  @since       September 13, 2004
 */
public class HTMLTextEditor extends HTMLTextArea
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLTextEditor");//#LOGGER-NOPMD

    private String contentCSS="";
    private String documentBaseURL;
    private boolean removeScriptHost;
    private boolean relativeURLs;
    private String host="";
    private String charCountURL = "";
    private String menubar=null;
     /**
     * Creates a new HTML editor
     * @param name the name of the component
     * @param rows number of rows
     * @param cols number of columns
     */
    public HTMLTextEditor( final String name, final int rows, final int cols)
    {
        // Add extra rows to allow for toolbars
        super(name, rows+4, cols);
    }
    
    public void setCharCountURL(final String url)
    {
        charCountURL = url;
    }

    public void setHost( final String host)
    {
        this.host=host;
    }
    
    /**
     * remove the host
     * @param flag
     */
    public void setRemoveScriptHost( final boolean flag)
    {
        removeScriptHost=flag;
    }

    /**
     * relative URLs
     * @param flag
     */
    public void setRelativeURLs( final boolean flag)
    {
        relativeURLs=flag;
    }

    /**
     * The content CSS
     * @param URLs the URLs comma separated.
     */
    public void setContentCSS( final String URLs)
    {
        contentCSS=URLs;
    }

    /**
     *
     * @param documentBaseURL the base URL
     */
    public void setDocumentBaseURL( final String documentBaseURL)
    {
        this.documentBaseURL=documentBaseURL;
    }

    /**
     * Clear all buttons.
     */
    public void clearButtons()
    {
        rowMap=null;
    }
    
    /** add button and row  values
     * @param text The Button value
     * @param row The Row value
     * @param majorVersion
     */
    public void addButton( final String text, final int row, final int majorVersion)
    {
        if( rowMap == null)
        {
            rowMap = HashMapFactory.create();
            rowMap.put(majorVersion, HashMapFactory.create());
        }
        HashMap<Integer, ArrayList<String>> m = rowMap.get(majorVersion);
        if(m == null)
        {
            m = new HashMap<>();
            rowMap.put(majorVersion, m);
        }
        ArrayList<String> btns = m.get(row);
        if( btns == null)
        {
            btns = new ArrayList<>();
            m.put( row, btns);
        }
        btns.add( text);
    }
    
    /**
     * add plugin
     * @param text plugin
     */
    public void addPlugin(final String text)
    {
        plugins.add(text);
    }
    
    private String getPlugins()
    {
        String p = "";
        for(String plugin : plugins)
        {
            p += plugin + ",";
        }
        while(p.endsWith(","))
        {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /**
     * Sets the height of the textEditor.  Defaults to the height of the text area
     * @param height - new height of text editor
     */
    public void setHeight( final String height)
    {
        this.height = height;
    }

    /**
     * Sets whether a count of characters entered will be displayed to
     *  the user
     * @param fg true to show
     */
    public void setShowExtCharCount( final boolean fg)
    {
        showExtCharCount = fg;
    }

    /**
     * Sets whether the context path is displayed at the bottom of
     *  the component
     * @param flag true to show
     */
    public void setShowContextPath( final boolean flag)
    {
        showContextPath = flag;
    }

    /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser the browser to compile for
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        compile4(browser);
    }

    protected void compile4( final ClientBrowser browser)
    {
        HTMLPage page;
        appendClassName("mce");
        
        onChangeScripts = HashMapFactory.create();
        onChangeScripts.put( "teOnChanged", "");

        page = getParentPage();

        String funcExt = HTMLUtilities.makeValidName(page.doGenerateId());
        String completeCompName = "ONLOAD_COMPLETE_TINYMCE_" + funcExt;
        String funcName = "textEditorInit_" + funcExt;
        String onLoadScript = "\nfunction " + funcName + "(inst)\n" +
                              "{\n" +
                              // "alert(inst.editorId + \" is now initialized.\");\n"+
                               "updateFieldValue('"+ completeCompName +"', 'true');\n"+
                               "checkCurtain();\n"+
                               "}\n";
        page.addJavaScript(onLoadScript);

        if(!Boolean.parseBoolean(page.getFlag(HAS_SET_HAS_HTML_EDITOR)))
        {
            page.addJavaScript("window.hasHTMLTextEditor=true;");

            page.addJavaScript("var htmlTextEditorIdArray = [];");

            page.putFlag(HAS_SET_HAS_HTML_EDITOR, Boolean.TRUE.toString());
        }

        String key = "HTML_AREA";
        String value;
        value = page.getFlag( key);

        if( value.isEmpty())
        {
            page.putFlag(key, "DONE");
        }

        HTMLInput completInputComp = new HTMLInput( completeCompName, "false");
        completInputComp.setInvisible( true);

        page.addJavaScript("htmlTextEditorIdArray.push(\"" + completInputComp.getId() + "\");");

        page.addComponent( completInputComp);

        String initTinymce = getInitTinymceVERSIONV4(browser,page,funcName);
        page.addJavaScript(initTinymce);
        page.addJavaScript("teOnChange", getTeOnChangedScript());

        super.compile( browser);
    }
    
    private String getTeOnChangedScript()
    {
        String script = "function teOnChanged(ed){\n" +
                "if(!ed) return;\n" +
                "var id = ed.id;\n" +
                "if(!id){\n" +
                "setHasChanged(true);\n" + 
                "return;" +
                "}\n" +
                "id = id.substring(id.indexOf('_'));\n" +
                "if(!findElement('OFIELD' + id)){\n" +
                "setHasChanged(true);\n" + 
                "return;" +
                "}\n" +
                "var eValue = findElement('EFIELD' + id).value;\n" +
                "var oValue = findElement('OFIELD' + id).value;\n" +
                "var value = ed.getContent();\n" +
                "if(value !== oValue && value !== eValue){\n" +
                "findElement('EFIELD' + id).value = value;\n" +
                "setHasChanged(true);\n" +
                "}\n" +
                "}\n";
        return script;
    }

    private void addFinderBrowserV4( final HTMLPage page)
    {
        if( page.getFlag("ADDED_FINDER_SCRIPT4").startsWith("Y")==false)
        {
            page.putFlag("ADDED_FINDER_SCRIPT4", "YES");
            String script=
                "function elFinderBrowserV4 (field_name, url, type, win) {\n" +
                "   var elfinder_url = '/doc_explorer?CALLED_BY=tinyMCE&tinyMCEVersion=" + getTinymceMajorVersion() + "&height=580';\n" +    // use an absolute path!
                "   var cmsURL = elfinder_url;\n" +    // script URL - use an absolute path!
                "   if (cmsURL.indexOf(\"?\") < 0) {\n" +
                //add the type as the only query parameter
                "       cmsURL = cmsURL + \"?type=\" + type;\n" +
                "   }else {\n" +
                //add the type as an additional query parameter
                // (PHP session ID is now included if there is one at all)
                "       cmsURL = cmsURL + \"&type=\" + type;\n" +
                "   }\n" +
                "tinymce.activeEditor.windowManager.open({\n" +
                "   file : cmsURL,\n" +
                "   title : 'Cloud Files',\n" +
                "   width : 650,\n" +
                "   height : 600,\n" +
                "   resizable : \"yes\",\n" +
                "   inline : \"yes\",\n" +  // This parameter only has an effect if you use the inlinepopups plugin!
                "   popup_css : false,\n" + // Disable TinyMCE's default popup CSS
                "   close_previous : \"no\"\n" +
                "}, {\n" +
                "     setUrl: function(url) {\n" +
                "       win.document.getElementById(field_name).value = '/' + encodeURI(url);\n" +
                "       if (\"createEvent\" in document) {\n" +
                "         var evt = document.createEvent(\"HTMLEvents\");\n" +
                "         evt.initEvent(\"change\", false, true);\n" +
                "         win.document.getElementById(field_name).dispatchEvent(evt);\n" +
                "       }\n" +
                "       else{\n" +
                "         win.document.getElementById(field_name).fireEvent(\"onchange\");" +
                "       }\n" +
                "     }\n" +
                "   });\n" +
                "   return false;\n" +
                "}";
            page.addJavaScript(script);
        }
    }

    private JSONArray customFormats=null;
    public void setCustomFormats( final @Nullable JSONArray customFormats)
    {
        this.customFormats=customFormats;
    }
    public void setMenuBar( final String menubar)
    {
        this.menubar=menubar;
    }
    private void addCustomFormats( final StringBuilder init)
    {
        if( customFormats!=null)
        {
            init.append("style_formats:");
            init.append( customFormats.toString(2));
            init.append(",");
        }
    }
    /**
     * This method create a tinyMCE init function for tinyMCE version 3_1_0_1
     * @param browser
     * @param page
     * @param funcName
     * @return the TinyMCE init function
     */
    private String getInitTinymceVERSIONV4(final ClientBrowser browser,final HTMLPage page,final String funcName)
    {
        StringBuilder init = new StringBuilder(1000);

        StringBuilder tmpPlugins = new StringBuilder(300);
        init.append( "tinymce.init({");

        addCustomFormats( init);
        init.append( "\nfile_browser_callback : elFinderBrowserV4,\n");

        addFinderBrowserV4( page);

       // We need to hide the curtain after the last HTML editor is completed.

        init.append("init_instance_callback : \"").append(funcName).append( "\",");
        
        init.append("selector:\"#").append(getId()).append("\",");
        init.append("inline: ").append(isInline).append(",");

        if( ASpellWrapper.isSupported())
        {
            String checkerURL=host +"/ReST/v1/spell";
            init.append("spellchecker_rpc_url: \"").append(checkerURL).append("\",");         

            Locale[] listLocales = ASpellWrapper.listLocales();
            init.append("spellchecker_languages: \"");
            boolean started=false;
            for( Locale l: listLocales)
            {
                if( started) 
                {
                    init.append(",");
                }
                else
                {
                    started=true;
                }
                init.append(l.getDisplayName()).append("=").append(l.getLanguage());
            }

            init.append("\",");
        }
        else
        {
            init.append("browser_spellcheck: true,");                
        }

        init.append("theme_advanced_toolbar_align :"+ "\"left\",");

        String containers = "buttons1,buttons2,buttons3,mceEditor";

        if( browser.isBrowserIPad())
        {
            containers = "buttons1,buttons2,mceEditor";
        }
        if( StringUtilities.isBlank( height) == false)
        {
            init.append("height  :" + "\"").append(height).append("\",");
        }
        init.append("force_br_newlines :\"true\",");
        init.append("force_p_newlines :\"false\",");

        if( StringUtilities.notBlank(documentBaseURL))
        {
            init.append("document_base_url:\"").append(documentBaseURL).append("\",");
            init.append("remove_script_host:").append(removeScriptHost).append(",");
            init.append("relative_urls:").append(relativeURLs).append(",");
        }
        else
        {
            init.append("convert_urls : false,");
        }

        AtomicBoolean codeMirrorPluginNeeded=new AtomicBoolean( false);
        if( readOnlyFg == true)
        {
            init.append("menubar : false,");
            //copy button doesn't work in tinyMCE 4.x readonly mode
//            init.append("toolbar1 :" + "\"copy\",");
            init.append("toolbar: false,");

            tmpPlugins.append("plugins : \"");//.append(oldPlugins);

            if( showExtCharCount)
            {
                init.append("theme_advanced_container_customButtons :\"charcountext\",");
                if(StringUtilities.notBlank(charCountURL))
                {
                    init.append("st_charCountURL: \"").append(charCountURL).append("\",");
                }
                containers += ",customButtons";
                tmpPlugins.append(",charcountext");
            }
            init.append("theme_advanced_containers : \"").append(containers).append("\",");
            tmpPlugins.append(",").append(getPlugins());
            tmpPlugins.append("\",");
            init.append( tmpPlugins.toString());
            init.append( "readonly :"+ "\"true\"");

        }
        else
        {
            if( StringUtilities.notBlank(contentCSS))
            {
                init.append( "content_css : \"").append(contentCSS.replace("\"", "\\\"")).append( "\",\n");
            }
          //  oldPlugins.append( "image,link,hr");
            String spellcheckerPlugin="spellchecker";
            String contextmenuPlugin="contextmenu";
            if( ASpellWrapper.isSupported()==false)
            {
                spellcheckerPlugin="";
                contextmenuPlugin="";
            }
            tmpPlugins.append("plugins : \"")
                    //.append(oldPlugins)
                    .append("image,link,hr,insertdatetime,table,fullscreen,")
                    .append(contextmenuPlugin)
                    .append(",paste,preview,media,searchreplace,")
                    .append( "print,wordcount,visualblocks,visualchars,nonbreaking,pagebreak,directionality,autolink,lists,")
                    .append(spellcheckerPlugin).append(",noneditable");
            
            tmpPlugins.append(",textcolor,searchreplace,charmap,table,emoticons,code");
            codeMirrorPluginNeeded.set(true);
            
            if( pluginAvailble("imgmap"))
            {
                tmpPlugins.append(",imgmap");                
            }
            if(pluginAvailble("acheck"))
            {
                tmpPlugins.append(",acheck");
            }
            
            /**
             * http://www.tinymce.com/wiki.php/Plugin:template
             */
            if( rowMap != null)
            {
                String buttons1 = getBtnsForRow(1, browser,codeMirrorPluginNeeded);
                String buttons2 = getBtnsForRow(2, browser, codeMirrorPluginNeeded);
                String buttons3 = getBtnsForRow(3, browser, codeMirrorPluginNeeded);
                init.append("toolbar1 :" + "\"").append(buttons1).append("\",");
                init.append("toolbar2 :" + "\"").append(buttons2).append("\",");
                init.append("toolbar3 :" + "\"").append(buttons3).append("\",");
                if( menubar==null)
                {
                    menubar="false";
                }
                
                if( hasButton( "CharCount"))
                {
                    tmpPlugins.append(",charcount");
                }
            }
            else
            {
                //default buttons, and menu
//                String buttonLine = "undo redo | formatselect | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image";// | spellchecker";

                if( browser.isBrowserMOBILE() == false)
                {
                    init.append("toolbar :\"undo redo | formatselect | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image\",");
                }
            }
            if( menubar!=null)
            {
                init.append("menubar : ").append(menubar).append(",");
            }
            
            StringBuilder externalPlugins=new StringBuilder();
            
            if( codeMirrorPluginNeeded.get())
            {
                tmpPlugins.append(",codemirror");
                if(externalPlugins.length()>0)externalPlugins.append(",");
                externalPlugins.append("codemirror: '/ds/tinymce_codemirror/default/plugin.min.js'");
                init.append(
                    "codemirror: {\n" +
                    "    indentOnInit: true,\n" +  // Whether or not to indent code on init.
                    "    path: '/ds/code_mirror/default/',\n" + // Path to CodeMirror distribution
                    "    config: {\n" +           // CodeMirror config object
//                    "       mode: 'mustache',\n" +
                    "       lineNumbers: true\n" +
                    "    }\n" +
//                    "    jsFiles: [\n" +          // Additional JS files to load
//                    "       'mode/clike/clike.js',\n" +
//                    "       'mode/php/php.js'\n" +
//                    "    ]\n" +
                    "  },"
                );
            }
            
            if( showExtCharCount)
            {
                if(externalPlugins.length()>0)externalPlugins.append(",");
                externalPlugins.append("charcountext: '/ds/charcountext/default/charcountext.js'");
                if(StringUtilities.notBlank(charCountURL))
                {
                    init.append("st_charCountURL: \"").append(charCountURL).append("\",");
                }
            }

            if(externalPlugins.length()>0)
            {
                 init.append("external_plugins: {").append(externalPlugins).append("},");
            }
            tmpPlugins.append(",").append(getPlugins());
            tmpPlugins.append("\",");
            init.append("\n").append( tmpPlugins);

            if(showContextPath || showExtCharCount)
            {
                init.append("statusbar: true,");
            }
            else
            {
                init.append("statusbar: false,");
            }

            init.append("theme_advanced_containers : \"").append(containers).append("\",");

            init.append(
                "paste_auto_cleanup_on_paste : true,"+
                "paste_convert_headers_to_strong : true,"
            );
            init.append("theme_advanced_toolbar_location : \"top\",\n");
            init.append(
                "plugin_insertdate_dateFormat :"+ "\"%Y-%m-%d\","+
                "plugin_insertdate_timeFormat :"+ "\"%H:%M:%S\","+
                "extended_valid_elements :"+
                //"img[id|style|class|src|border|alt|title|hspace|vspace|width|height|align|onmouseover|onmouseout|name],"
                "\"a[id|class|name|href|target|title|onclick|style],"+
                "hr[id|class|width|size|noshade],font[face|size|color|style],span[id|class|align|style]\""
            );
        }

        StringBuilder htmlValidationPatch = new StringBuilder();
        htmlValidationPatch.append("var editorId = ed.id.substring(ed.id.indexOf('_'));\n");
        htmlValidationPatch.append("var ofield = findElement('OFIELD' + editorId);\n");
        htmlValidationPatch.append("if(ofield){\n");
        htmlValidationPatch.append("ofield.value = ed.getContent();\n");
        htmlValidationPatch.append("}\n");
        
        init.append(",\nsetup : function(ed){\n");
        
        init.append("ed.on('init', function(){").append(htmlValidationPatch.toString()).append("});\n");
        init.append("ed.on('change', function(){\n");
        init.append("teOnChanged(ed);\n");
        init.append("});\n");

        init.append("},\n");
        init.append("branding: false");

        init.append( "});");
        return init.toString();
    }
    
    private boolean pluginAvailble( final String plugin)
    {
        String code = plugin + "#" + tinymceVersion;
        Boolean flag = PLUGIN_AVAILABLE.get(code);
                
        if( flag == null)
        {
            String base;
            int majorVersion = getTinymceMajorVersion();
            if(majorVersion == 3)
            {        
                base = "/scripts/tinymce_"+ tinymceVersion +"/jscripts/tiny_mce/";
            }
            else
            {
                base= "/scripts/tinymce_"+ tinymceVersion +"/js/tinymce/";
            }
            String root = CProperties.getDocRoot();
            
            File pluginFile = new File( root + "/" + base + "/plugins/" + plugin + "/editor_plugin.js");
            
            flag = pluginFile.isFile();
            PLUGIN_AVAILABLE.put(code, flag);
        }
        
        return flag;
    }
    
    /**
     * Set Tinymce Version
     * @param version
     */
    public void setTinymceVersion(final String version)
    {
        tinymceVersion = version;
    }
    
    /**
     * get the major of the tinyMCE
     * @return 3 or 4
     */
    public int getTinymceMajorVersion()
    {
        if(StringUtilities.notBlank(tinymceVersion) && tinymceVersion.matches("4.+"))
        {
            HTMLPage p = getParentPage();
            if(p != null)
            {
                String tinyMCEV = p.getFlag("TINYMCE_VERSION");
                if(StringUtilities.notBlank(tinyMCEV))
                {
                    int v = Integer.parseInt(tinyMCEV);
                    if(v < 4)
                    {
                        return 3;
                    }
                }
            }
            return 4;
        }
        else
        {
            return 3;
        }
    }

    /**
     * check table controls not added, if browser is Mac as Mac doesn't supports Table controls
     */
    private boolean isSupported( final String btn, final ClientBrowser browser)
    {
        if (btn.equalsIgnoreCase("tablecontrols"))
        {
            if (browser.osMAC() == true)
            {
                return false;
            }
        }
        return true;
    }

    private boolean hasButton( final String btn)
    {
        int majorVersion = getTinymceMajorVersion();
        for( int row=1; row<=3; row++)
        {
            HashMap<Integer, ArrayList<String>> map = rowMap.get(majorVersion);
            if(map != null)
            {
                ArrayList<String> btns = map.get( row);

                if( btns != null)
                {
                    for( String text: btns)
                    {
                        if( text.equalsIgnoreCase( btn))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** get all the buttons for each corresponding row values
     *
     * @param row The Row value
     *
     * @param browser The Browser name
     *
     * returns button's string value
     */
    private String getBtnsForRow( final int row, final ClientBrowser browser, final AtomicBoolean codeMirrorPluginNeeded)
    {
        int majorVersion = getTinymceMajorVersion();
        StringBuilder sb = new StringBuilder();
        HashMap<Integer, ArrayList<String>> map = rowMap.get(majorVersion);
        if(map != null)
        {
            ArrayList<String> btns = map.get( row);

            if( btns != null)
            {
                for( String btn: btns)
                {
                    if( isSupported(btn, browser))
                    {
                        if( sb.length()>0)
                        {
                            sb.append( ",");
                        }
                        sb.append( btn);
                        if( codeMirrorPluginNeeded != null && btn.equalsIgnoreCase("code"))
                        {
                            codeMirrorPluginNeeded.set(true);
                        }
                    }
                }
            }
        }
        return sb.toString().toLowerCase();
    }
    
    /**
     * textarea can not use inline, tinyMCE will break replace the textarea with
     * div once type anything in the textarea. Only div is supported
     * @param isInline 
     */
    public void setIsInline(boolean isInline)
    {
        this.isInline = isInline;
    }

    // Stores details of buttons to be displayed on toolbar
    private HashMap<Integer, HashMap<Integer, ArrayList<String>>> rowMap = null;
    
    private final ArrayList<String> plugins = new ArrayList<>();

    private HashMap<String, String> onChangeScripts;
    private boolean showExtCharCount,
                    showContextPath,
                    isInline = false;
    private String height;

    private String tinymceVersion =  "3_5_8";
//    private int screenVersion;

    private static final ConcurrentHashMap<String,Boolean> PLUGIN_AVAILABLE=new ConcurrentHashMap();
    
    /**
     * This constant is used for setting in HTMLPage whether we have set the javascript
     * "window.hasHTMLTextEditor=true;" already. If true, then do not set again.
     */
    private static final String HAS_SET_HAS_HTML_EDITOR = "hasSetHasHtmlEditor";
}
