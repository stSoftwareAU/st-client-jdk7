/*
 *  Copyright (c) 2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance.tasks;

import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.BenchMarkWebClient;
import com.aspc.remote.performance.Task;
import com.aspc.remote.performance.internal.WebClient;
import com.aspc.remote.util.misc.StringUtilities;
import org.w3c.dom.Element;

/**
 *  Loads a screen using a HTTPUnit client
 *  Usage <TASK name="TEST SCREEN" java_class="com.aspc.remote.performance.tasks.ScreenLoadTask"
 *                                 class_name="SDBComponent"
 *                                 base_object_key="123@2~124@1"
 *                                 screen_key="3422@1~124@1"
 *                                 screen_type="DATAENTRY"
 *                                 url_params="ADDITIONAL_CRITERIA=name eq test"
 *                                 match_content='REPORT_1'/>
 *
 *  The attributes are as follows:
 *  
 *  Screen Details (Identifies which screen to load)
 *  screen_key  - the global key of the actual screen (optional)
 *  screen_type - if the screen_key is not available then a type of screen can be specified
 *                The type can be one of the following: "DATA_ENTRY" (default), "MULTI_UPDATE", "REPORT", 
 *                                                      "SEARCH", "SUMMARY", "DATA ENTRY PRINTOUT" 
 *  class_name  - If the screen_type is used then the class_name needs to be specified to identify
 *                which class to find the default screen for 
 *  
 *  Additional Details
 *  base_object_key - A global key of a DBObject that should be loaded into the specified screen
 *  url_params      - Additional parameter passed to the screen which are seperated by the AT (@) symbol
 *  match_content   - Ensures that HTML output contains supplied content
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       26 September, 2006 06:03:17
 */
public class ScreenLoadTask extends Task
{
    private String class_name,
                   url_params,
                   screen_type,
                   screen_key,
                   lastPage,
                   base_object_key,
                   match_content;
   
    
    /**
     * Default constructor.
     * @param name The name
     * @param bmClient The benchmark client
     */
    public ScreenLoadTask(String name, BenchMarkClient bmClient)
    {
        super( name, bmClient);
    }
   
    
    /**
     * Processes the task.
     * @throws Exception If something went wrong
     */
    @Override
    protected void process() throws Exception
    {
        if( bmClient instanceof BenchMarkWebClient == false)
        {
            throw new Exception( "A Web Client is required for this Task");
        }
        
        BenchMarkWebClient webClient = (BenchMarkWebClient)bmClient;              
                           
        WebClient httpClient = webClient.getHttpClient();
       
        String urlRequest = getHREF(httpClient );
        String text = httpClient.getResponse( urlRequest); 
       
        lastPage = text;
        
        if( StringUtilities.isBlank( match_content) == false)
        {
            if( !text.contains(match_content))
            {
                throw new Exception( "Could not find occurence of '" + match_content + "' in results");
            }
        }
    }
    
    /**
     *
     * @return the value
     */
    public String getLastPage()
    {
        return lastPage;
    }
    
    private String getHREF( WebClient httpClient) throws Exception
    {

        String servletName = "/screen/dataentry";
        if( StringUtilities.isBlank( screen_type) == false)
        {
            if( screen_type.equals("REPORT"))
            {
                servletName = "/report/generic";
            }
            else if( screen_type.equals("SUMMARY"))
            {
                servletName = "/summary/generic";
            }
            else if( screen_type.equals("SEARCH"))
            {
                servletName = "/search/generic";
            }
        }
        
        StringBuilder params = new StringBuilder();        

        if( StringUtilities.isBlank(screen_key))
        {
            if( StringUtilities.isBlank(class_name))
            {
                throw new Exception( "Class name is required if no screen key is provided");
            }
            params.append( "CLASS_NAME=" + class_name);
        }
        else
        {
            params.append( "SCREEN_KEY=" + screen_key);
        }

        if( StringUtilities.isBlank(base_object_key) == false)
        {
            params.append( "&GLOBAL_KEY=" + base_object_key);
        }

        if( StringUtilities.isBlank(url_params) == false)
        {
            params.append( "&" + url_params);
        }
        
        return  httpClient.getHost() + servletName + "?" + params.toString();
        

    }    
    
   /**
     * Set item attributes.
     * @param element The xml element to fill out from
     * @throws Exception If something went wrong
     */
    @Override
    protected void setItemAttributes(Element element) throws Exception
    {
        super.setItemAttributes(element);
        
        if( element.hasAttribute( "screen_key"))
        {
            screen_key =  element.getAttribute( "screen_key");
        }
        if( element.hasAttribute( "base_object_key"))
        {
            base_object_key = element.getAttribute( "base_object_key");
        } 
        if( element.hasAttribute( "screen_type"))
        {
            screen_type = element.getAttribute( "screen_type");
        }
        if( element.hasAttribute( "class_name"))
        {
            class_name = element.getAttribute( "class_name");
        }
        url_params = "";
        if( element.hasAttribute( "url_params"))
        {
            url_params = element.getAttribute( "url_params");
        }
        if( element.hasAttribute( "match_content"))
        {
            match_content =  element.getAttribute( "match_content");
        }

        for( int i = 0; i < 100; i++)
        {
            String name = "url_param" + i;
            if( element.hasAttribute( name))
            {
                String temp;
                temp = element.getAttribute( name);
                
                if( StringUtilities.isBlank( url_params) == false)
                {
                    url_params += "&";
                }
                
                if( StringUtilities.isBlank( temp) == false)
                {
                    url_params += temp;
                }
            }
        }
    }
    
}
