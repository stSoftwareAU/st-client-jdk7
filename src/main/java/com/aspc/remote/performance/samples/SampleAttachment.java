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
package com.aspc.remote.performance.samples;

import com.aspc.remote.performance.Attachment;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLText;
import com.aspc.remote.soap.Client;

/**
 *  App
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       7 April 2001
 */
public class SampleAttachment extends Attachment
{
    /**
     * 
     * @param name 
     */
    public SampleAttachment(String name)
    {
        super( name);
    }

    /**
     * 
     * @param client the client to use
     * @throws Exception a serious problem
     */
    @Override
    protected void process(Client client) throws Exception
    {
    }

    /**
     * 
     * @throws Exception a serious problem
     * @return the value
     */
    @Override
    public String getContent() throws Exception
    {
        HTMLPage page = new HTMLPage();
        HTMLText text = new HTMLText( data);
        text.setBold( true);
        page.addComponent( text);

        return page.generate();
    }

}
