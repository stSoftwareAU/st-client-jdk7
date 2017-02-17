package com.aspc.remote.html;

/**
 *
 *  @author      Lei Gao
 *  @since       06 March 2015 
 */
public interface HTMLTableInterface
{
    public void iGenerate(final ClientBrowser browser, final StringBuilder buffer);
    
    public void iGenerateAttributes(final ClientBrowser browser, final StringBuilder buffer);
}
