/*
 *  Copyright (c) 2000-2004 ASP Converters Pty Ltd.
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
*/
package com.aspc.remote.html;

import com.aspc.remote.util.misc.StringUtilities;


/**
 *  HTML FORM element
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Liam
 *  @since       24 Oct 2012
 */
public class HTMLSelectTree extends HTMLContainer
{

    
    public HTMLSelectTree(String atitle,String listSql,String selected)
    {
        this.title = atitle;
        this.listSql = listSql;
        this.selected = selected;
    }

    /**
     * get the ID of this component
     *
     * @return the ID
     */
    @Override
    public String getId()
    {
        return id;
    }
    
    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     * @return This current form
     */
    public HTMLSelectTree setId( final String id)
    {
        iSetId(id);
        
        return this;
    }
    
    /**
     * Set the name of this form
     *
     * @param name The form name
     * @return This current form
     */
    public HTMLSelectTree setName( final String name)
    {
        iSetName( name);
        
        return this;
    }
    
    
    /**
     * Set the on submit action
     *
     * @param browser The browser.
     */
    @Override
    protected void compile( ClientBrowser browser)
    {
        HTMLPage page = getParentPage();
        page.addGWT("com.aspc.gwt.selectCellTree.SelectCellTree");
        String counter = page.getFlag("SelectCellTree:counter");
        int tmpID = 1;
        if (StringUtilities.isBlank(counter) == false) 
        {
            tmpID = Integer.parseInt(counter);
            tmpID++;
        }
        page.putFlag("SelectCellTree:counter", "" + tmpID);
        selectTreeId = "CT" + tmpID;
        
        page.addToList("SelectCellTree", selectTreeId);
        
        page.addToDictionary("SelectCellTree_" +selectTreeId, "LIST_SQL", listSql);
        page.addToDictionary("SelectCellTree_" +selectTreeId, "SELECTED", selected); 
        page.addToDictionary("SelectCellTree_" +selectTreeId, "TITLE", title);
        
        super.compile( browser);
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
        buffer.append("<div id=\"").append(selectTreeId).append("\"></div>");
         
        super.iGenerate(browser, buffer);
    }
    
    private String selectTreeId;
    private final String listSql;
    private final String selected;
    private final String title;

}
