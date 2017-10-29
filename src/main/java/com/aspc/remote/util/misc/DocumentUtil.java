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
package com.aspc.remote.util.misc;

import com.aspc.developer.ThreadCop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 *  Handles XML documents.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *  DOM objects created by Xerces are NOT multi-thread safe even if these objects are only in read only mode may not be accessed concurrently by separate threads. Currently we are sharing the DOM objects for the stored results across threads.
 *
 *   http://xerces.apache.org/xerces2-j/faq-dom.html#faq-1
 *   http://old.nabble.com/DOM-thread-safety-issues---disapearring-children-td31795248.html
 *
 *  @author      Nigel Leck
 *  @since       3 December 2001, 12:02
 */
@SuppressWarnings({"NestedAssignment", "AssertWithSideEffects", "BroadCatchBlock", "TooBroadCatch"})
public final class DocumentUtil
{
    /**
     * The parser type.
     */
    public static enum PARSER{
        DEFAULT,
        TOLERANT,
        VALIDATE
    };
    
    private static final boolean TRANSFORMER_CACHABLE;
    /**
     * get the node text
     * @param doc the document
     * @param nodeTag the node
     * @return the text
     */
    @CheckReturnValue @Nonnull
    public static String getNodeText(final @Nonnull Document doc, final @Nonnull String nodeTag)
    {
        String value = "";

        NodeList nl = doc.getElementsByTagName(nodeTag);
        if( nl != null)
        {
            Node node = nl.item(0);

            if( node != null)
            {
                Node childNode = node.getFirstChild();
                if( childNode != null)
                {
                    value = childNode.getNodeValue();
                }
            }
        }

        return value;
    }
    
    /**
     * returns the current builder for this thread
     * @return the current document builder
     * @throws java.lang.Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static DocumentBuilder getBuilder() throws Exception
    {
        return getBuilder(PARSER.DEFAULT);
    }
    
    /**
     * returns the current builder for this thread
     * @param parser the parser to use. 
     * @return the current document builder
     * @throws java.lang.Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static DocumentBuilder getBuilder( @Nonnull final PARSER parser) throws Exception
    {
        switch (parser) {
            case DEFAULT:
            {
                DocumentBuilder builder = BUILDER_LOCAL.get();
                
                if( builder == null)
                {
                    synchronized( BUILDER_FACTORY)
                    {
                        builder = BUILDER_FACTORY.newDocumentBuilder();
                    }
                    
                    BUILDER_LOCAL.set( builder);
                }
                
                return builder;
            }
            case TOLERANT:
            {
                DocumentBuilder builder = BUILDER_TOLERANT_LOCAL.get();
                
                if( builder == null)
                {
                    synchronized( BUILDER_TOLERANT_FACTORY)
                    {
                        builder = BUILDER_TOLERANT_FACTORY.newDocumentBuilder();
                    }
                    
                    BUILDER_TOLERANT_LOCAL.set( builder);
                }
                
                return builder;
            }
            case VALIDATE:
            {
                DocumentBuilder builder = BUILDER_VALIDATE_LOCAL.get();
                
                if( builder == null)
                {
                    synchronized( BUILDER_VALIDATE_FACTORY)
                    {
                        builder = BUILDER_VALIDATE_FACTORY.newDocumentBuilder();
                    }
                    
                    BUILDER_VALIDATE_LOCAL.set( builder);
                }
                
                return builder;
            }
            default:
                throw new IllegalArgumentException( "unknown parser " + parser);
        }
    }

    /**
     * make a document.
     * @param xml The XML to parse
     * @return The newly created and populated DOM
     * @throws DocumentException a serious problem
     */
    @CheckReturnValue @Nonnull
    public static Document makeDocument( final @Nonnull String xml) throws DocumentException
    {
        return makeDocument( xml, PARSER.DEFAULT);
    }
    
    /**
     * make a document.
     * @param xml The XML to parse
     * @param parser the parser to use
     * @return The newly created and populated DOM
     * @throws DocumentException a serious problem
     */
    @CheckReturnValue @Nonnull
    public static Document makeDocument( final @Nonnull String xml, final @Nonnull PARSER parser) throws DocumentException
    {
        if( StringUtilities.isBlank( xml))
        {
            throw new DocumentException( "can't make document as the XML is blank");
        }

        InputSource t;
        try
        (StringReader r = new StringReader(xml)) {
            t = new InputSource( r);
            DocumentBuilder builder = getBuilder(parser);
            
            Document doc = builder.parse(t);

            if( ASSERT_ENABLED)
            {
                doc = new WrapperDocument(doc);
            }

            return doc;
        }
        catch( Exception e)
        {
            String shortXML=xml;
            if( shortXML.length()> 2048 + 10)
            {
                shortXML=xml.substring(0,1024) +"\n...\n"+ xml.substring(xml.length() -1024);
            }
            LOGGER.warn( shortXML, e);
            throw new DocumentException (shortXML, e);
        }
    }

    /**
     * Load a file and return it as an XML Document object
     * @param file the file to read the XML file from
     * @return The XML document
     * @throws java.lang.Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static Document loadDocument( final @Nonnull File file) throws Exception
    {
        return loadDocument( file, PARSER.DEFAULT);
    }
    
    /**
     * Load a file and return it as an XML Document object
     * @param file the file to read the XML file from
     * @param parser the parser
     * @return The XML document
     * @throws java.lang.Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static Document loadDocument( final @Nonnull File file, final @Nonnull PARSER parser) throws Exception
    {
        String xml = FileUtil.readFile(file);

        return makeDocument( xml, parser);
    }

    /**
     * Read the encoded file
     * @param file file to be read
     * @param encoding what for the encoding is
     * @return document
     * @throws Exception if file does not exists
     */
    @CheckReturnValue @Nonnull
    public static Document loadEncodedDocument( final @Nonnull File file, final @Nonnull String encoding) throws Exception
    {
        StringBuilder buffer = new StringBuilder();

        try
        (InputStreamReader fr = new InputStreamReader(new FileInputStream(file.getPath()), encoding)) {
            char array[] = new char[10240];

            while( true)
            {
                int len = fr.read( array);

                if( len <= 0) break;

                buffer.append( array,0, len);
            }
        }

        String xml = buffer.toString();

        return makeDocument( xml);
    }


    /**
     * write a document to a file.
     * @param document the document to write
     * @param file the file to write to.
     * @throws java.lang.Exception a serious problem
     */
    public static void writeDocument(
        final @Nonnull Document document,
        final @Nonnull File file
    ) throws Exception
    {
        try
        (FileWriter w = new FileWriter( file)) {
            writeNode(document, w);
        }
    }

    /**
     * write a document to a file.
     * @param node
     * @param w
     * @throws java.lang.Exception a serious problem
     */
    public static void writeNode(
        final @Nonnull Node node,
        final @Nonnull Writer w
    ) throws Exception
    {
        writeNode(node, w, 2);
    }

    /**
     * write a document to a file.
     * @param node
     * @param w
     * @param indent amount to indent
     * @throws java.lang.Exception a serious problem
     */
    public static void writeNode(
        final @Nonnull Node node,
        final @Nonnull Writer w,
        final @Nonnegative int indent
    ) throws Exception
    {
        if( node == null) throw new IllegalArgumentException( "node must not be null");
        if( w == null) throw new IllegalArgumentException( "writer must not be null");
        if( indent < 0) throw new IllegalArgumentException( "indent must not be zero or more");

        Transformer transformer = newTransformer();
        transformer.reset();
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(w);

        if( indent > 0)
        {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        }

        transformer.transform(source, result);
    }

    /**
     * Clear the templates cache
     */
    public static void clearCache()
    {
        TEMPLATES.clear();
    }
    
    /**
     * New XSLT transformer.
     * @param xslt the XSLT to transform.
     * @return the transformer.
     * @throws TransformerConfigurationException
     * @throws IOException if an IO exception occurs.
     */
    @CheckReturnValue @Nonnull
    public static Transformer newTransformer( final @Nonnull String xslt) throws TransformerConfigurationException, IOException
    {
        if( StringUtilities.isBlank(xslt))
        {
            throw new TransformerConfigurationException( "blank XSLT");
        }
        
        Templates templates = TEMPLATES.get(xslt);
        
        if( templates == null)
        {
            
            ByteArrayInputStream in=null;
            ByteArrayOutputStream out=null;

            try
            {
                in= new ByteArrayInputStream(xslt.getBytes( StandardCharsets.UTF_8));
                out= new ByteArrayOutputStream();
                StreamSource ss = new StreamSource(in);
               
                templates = TRANSFORMER_FACTORY.newTemplates( ss);
                if( templates == null)
                {
                    throw new TransformerConfigurationException( "null returned for " + xslt);
                }
                TEMPLATES.put(xslt, templates);
            }
            catch( TransformerConfigurationException tce)
            {
                LOGGER.warn( xslt, tce);
                throw tce;
            }
            finally
            {
                if( out != null) out.close();
                if( in != null) in.close();
            }
        }
        assert templates!=null;
        return templates.newTransformer();
    }
    
    /**
     * New transformer. 
     * @return the new clean transformer.
     * 
     * @throws TransformerConfigurationException 
     */
    @CheckReturnValue @Nonnull
    public static Transformer newTransformer() throws TransformerConfigurationException
    {
        Transformer transformer= TRANSFORMER_LOCAL.get();
        if(transformer == null)
        {
            transformer=TRANSFORMER_FACTORY.newTransformer();

            /*
             * Only cache if we can call reset. Old version of the Xalan can't call the method "reset"
            */
            if( TRANSFORMER_CACHABLE)
            {
                try
                {
                    transformer.reset();
                    TRANSFORMER_LOCAL.set(transformer);
                }
                catch( UnsupportedOperationException uoe)
                {
                    // Just don't cache. 
                }
            }
        }
        else
        {
            transformer.reset();
        }
        
        return transformer;
    }
    
    /**
     * Create a new XML document
     * @return the new blank document
     * @throws java.lang.Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static Document newDocument() throws Exception
    {
        DocumentBuilder builder = getBuilder();

        Document doc= builder.newDocument();

        if( ASSERT_ENABLED)
        {
            doc = new WrapperDocument(doc);
        }

        return doc;
    }

    /**
     * convert a document to a string.
     * @param doc The document to convert
     * @return The XML string
     * @throws java.lang.Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static String docToString(final @Nonnull Document doc) throws Exception
    {
        StringWriter w = new StringWriter();
        writeNode(doc, w, 0);

        String data = w.toString();

        return data;
    }
    
    @CheckReturnValue @Nonnull
    public static JSONObject docToJson(final @Nonnull Document doc) throws Exception
    {
        String xml=DocumentUtil.docToString(doc);
        String cleanXML=removeNamespace( xml);

        JSONObject json = org.json.XML.toJSONObject(cleanXML);
        return json;
    }

    private static String removeNamespace(final String xml)
    {
        String ret = null;
        int strStart = 0;
        boolean finished = false;
        if (xml != null) {
            String tmpXML=xml.replace("&#13;", "\r").replace("\r\n", "\n");
            //BE CAREFUL : allocate enough size for StringBuffer to avoid expansion
            StringBuilder sb = new StringBuilder(tmpXML.length());
            while (!finished) {

                int start = tmpXML.indexOf('<', strStart);
                int end = tmpXML.indexOf('>', strStart);
                if (start != -1 && end != -1) {
                    // Appending anything before '<', including '<'
                    sb.append(tmpXML, strStart, start + 1);

                    String tag = tmpXML.substring(start + 1, end);
                    if (tag.charAt(0) == '/') {
                        // Appending '/' if it is "</"
                        sb.append('/');
                        tag = tag.substring(1);
                    }

                    int colon = tag.indexOf(':');
                    int space = tag.indexOf(' ');
                    if (colon != -1 && (space == -1 || colon < space)) {
                        tag = tag.substring(colon + 1);
                    }
                    // Appending tag with prefix removed, and ">"
                    sb.append(tag).append('>');
                    strStart = end + 1;
                } else {
                    finished = true;
                }
            }

            ret = sb.toString();
        }
        return ret;
    }

    /**
     * The function normaliseDocument removes superfluous and non-intuitive "#TEXT#" nodes created from a space between
     * XML elements
     * sourced from http://www.javaworld.com/javaworld/jw-06-2001/jw-0622-traps-p2.html
     * @param n the node to normalize
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    public static void normaliseDocument(final @Nonnull Node n)
    {
        if (!n.hasChildNodes())
        {
            return;
        }

        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++)
        {
            Node cn = nl.item(i);
            if (cn.getNodeType() == Node.TEXT_NODE)
            {
                String temp =  cn.getNodeValue();

                if( StringUtilities.isBlank(temp))
                {
                    n.removeChild(cn);
                    i--;
                }
            }
            else
            {
                normaliseDocument(cn);
            }
        }
    }

    /**
     *  Convenience function for creating a text-only element.
     * @param doc the document
     * @param element_name
     * @param element_value
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static Element createTextOnlyElement( final @Nonnull Document doc, final @Nonnull String element_name, final @Nonnull String element_value )
    {
        Element newElement = doc.createElement(element_name);
        Text text = doc.createTextNode(element_value);
        newElement.appendChild(text);
        return newElement;
    }

    /**
     * Do not allow the DocumentUtil to be created. 
     */
    private DocumentUtil()
    {
    }

    /**
     * Developer only version of document to confirm correct usage.
     */
    private static class WrapperDocument implements Document
    {
        private final Document doc;
        WrapperDocument( final Document baseDocument)
        {
            doc=baseDocument;
            ThreadCop.monitor(doc, ThreadCop.MODE.ACCESS_ONLY_BY_CREATING_THREAD);
        }

        /** {@inheritDoc} */
        @Override
        public DocumentType getDoctype()
        {
            ThreadCop.access(doc);
            return doc.getDoctype();
        }

        /** {@inheritDoc} */
        @Override
        public DOMImplementation getImplementation()
        {
            ThreadCop.access(doc);
            return doc.getImplementation();
        }

        /** {@inheritDoc} */
        @Override
        public Element getDocumentElement()
        {
            ThreadCop.access(doc);
            return doc.getDocumentElement();
        }

        /** {@inheritDoc} */
        @Override
        public Element createElement(final String tagName) throws DOMException
        {
            if( tagName == null) throw new NullPointerException( "createElement( null)");
            ThreadCop.access(doc);
            return doc.createElement(tagName);
        }

        /** {@inheritDoc} */
        @Override
        public DocumentFragment createDocumentFragment()
        {
            ThreadCop.access(doc);
            return doc.createDocumentFragment();
        }

        /** {@inheritDoc} */
        @Override
        public Text createTextNode(final String data)
        {
            if( data == null) 
            {
                throw new NullPointerException( "createTextNode( null)");
            }
            
            ThreadCop.access(doc);
            return doc.createTextNode(data);
        }

        /** {@inheritDoc} */
        @Override
        public Comment createComment(String data)
        {
            if( data == null) throw new NullPointerException( "createComment( null)");
            ThreadCop.access(doc);
            return doc.createComment(data);
        }

        /** {@inheritDoc} */
        @Override
        public CDATASection createCDATASection(String data) throws DOMException
        {
            if( data == null) throw new NullPointerException( "createCDATASection( null)");
            ThreadCop.access(doc);
            return doc.createCDATASection(data);
        }

        /** {@inheritDoc} */
        @Override
        public ProcessingInstruction createProcessingInstruction(final String target, final String data) throws DOMException
        {
            if( target == null || data == null) throw new NullPointerException( "createProcessingInstruction( " + target + "," + data + ")");
            ThreadCop.access(doc);
            return doc.createProcessingInstruction(target, data);
        }

        /** {@inheritDoc} */
        @Override
        public Attr createAttribute(final String name) throws DOMException
        {
            if( name == null) throw new NullPointerException( "createAttribute( null)");
            ThreadCop.access(doc);
            return doc.createAttribute(name);
        }

        /** {@inheritDoc} */
        @Override
        public EntityReference createEntityReference(final String name) throws DOMException
        {
            if( name == null) throw new NullPointerException( "createEntityReference( null)");
            ThreadCop.access(doc);
            return doc.createEntityReference(name);
        }

        /** {@inheritDoc} */
        @Override
        public NodeList getElementsByTagName(String tagname)
        {
            if( tagname == null) throw new NullPointerException( "getElementsByTagName( null)");            
            ThreadCop.access(doc);
            return doc.getElementsByTagName(tagname);
        }

        /** {@inheritDoc} */
        @Override
        public Node importNode(Node importedNode, boolean deep) throws DOMException
        {
            if( importedNode == null) throw new NullPointerException( "importNode( null," + deep + ")"); 
            ThreadCop.access(doc);
            return doc.importNode(importedNode, deep);
        }

        /** {@inheritDoc} */
        @Override
        public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException
        {
            if( namespaceURI == null || qualifiedName == null) throw new NullPointerException( "createElementNS( " + namespaceURI + "," + qualifiedName + ")");
            ThreadCop.access(doc);
            return doc.createElementNS(namespaceURI, qualifiedName);
        }

        /** {@inheritDoc} */
        @Override
        public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException
        {
            if( namespaceURI == null || qualifiedName == null) throw new NullPointerException( "createAttributeNS( " + namespaceURI + "," + qualifiedName + ")");
            ThreadCop.access(doc);
            return doc.createAttributeNS(namespaceURI,qualifiedName);
        }

        /** {@inheritDoc} */
        @Override
        public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
        {
            if( namespaceURI == null || localName == null) throw new NullPointerException( "getElementsByTagNameNS( " + namespaceURI + "," + localName + ")");
            ThreadCop.access(doc);
            return doc.getElementsByTagNameNS(namespaceURI, localName);
        }

        /** {@inheritDoc} */
        @Override
        public Element getElementById(String elementId)
        {
            if(elementId == null) throw new NullPointerException( "getElementById( " + elementId + ")");
            ThreadCop.access(doc);
            return doc.getElementById(elementId);
        }

        /** {@inheritDoc} */
        @Override
        public String getInputEncoding()
        {
            ThreadCop.access(doc);
            return doc.getInputEncoding();
        }

        /** {@inheritDoc} */
        @Override
        public String getXmlEncoding()
        {
            ThreadCop.access(doc);
            return doc.getXmlEncoding();
        }

        /** {@inheritDoc} */
        @Override
        public boolean getXmlStandalone()
        {
            ThreadCop.access(doc);
            return doc.getXmlStandalone();
        }

        /** {@inheritDoc} */
        @Override
        public void setXmlStandalone(boolean xmlStandalone) throws DOMException
        {
            ThreadCop.access(doc);
            doc.setXmlStandalone(xmlStandalone);
        }

        /** {@inheritDoc} */
        @Override
        public String getXmlVersion()
        {
            ThreadCop.access(doc);
            return doc.getXmlVersion();
        }

        /** {@inheritDoc} */
        @Override
        public void setXmlVersion(String xmlVersion) throws DOMException
        {
            if(xmlVersion == null) throw new NullPointerException( "setXmlVersion( " + xmlVersion + ")");
            ThreadCop.access(doc);
            doc.setXmlVersion(xmlVersion);
        }

        /** {@inheritDoc} */
        @Override
        public boolean getStrictErrorChecking()
        {
            ThreadCop.access(doc);
            return doc.getStrictErrorChecking();
        }

        /** {@inheritDoc} */
        @Override
        public void setStrictErrorChecking(boolean strictErrorChecking)
        {
            ThreadCop.access(doc);
            doc.setStrictErrorChecking(strictErrorChecking);
        }

        /** {@inheritDoc} */
        @Override
        public String getDocumentURI()
        {
            ThreadCop.access(doc);
            return doc.getDocumentURI();
        }

        /** {@inheritDoc} */
        @Override
        public void setDocumentURI(String documentURI)
        {
            if(documentURI == null) throw new NullPointerException( "setDocumentURI( " + documentURI + ")");
            ThreadCop.access(doc);
            doc.setDocumentURI( documentURI);
        }

        /** {@inheritDoc} */
        @Override
        public Node adoptNode(Node source) throws DOMException
        {
            if(source == null) throw new NullPointerException( "adoptNode( " + source + ")");
            ThreadCop.access(doc);
            return doc.adoptNode( source);
        }

        /** {@inheritDoc} */
        @Override
        public DOMConfiguration getDomConfig()
        {
            ThreadCop.access(doc);
            return doc.getDomConfig();
        }

        /** {@inheritDoc} */
        @Override
        public void normalizeDocument()
        {
            ThreadCop.access(doc);
            doc.normalizeDocument();
        }

        /** {@inheritDoc} */
        @Override
        public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException
        {
            if(n == null||namespaceURI == null || qualifiedName==null) throw new NullPointerException( "renameNode( " + n + ","+namespaceURI+ ","+qualifiedName +")");
            ThreadCop.access(doc);
            return doc.renameNode(n, namespaceURI, qualifiedName);
        }

        /** {@inheritDoc} */
        @Override
        public String getNodeName()
        {
            ThreadCop.access(doc);
            return doc.getNodeName();
        }

        /** {@inheritDoc} */
        @Override
        public String getNodeValue() throws DOMException
        {
            ThreadCop.access(doc);
            return doc.getNodeValue();
        }

        /** {@inheritDoc} */
        @Override
        public void setNodeValue(String nodeValue) throws DOMException
        {
            if(nodeValue == null) throw new NullPointerException( "setNodeValue( " + nodeValue + ")");
            ThreadCop.access(doc);
            doc.setNodeValue(nodeValue);
        }

        /** {@inheritDoc} */
        @Override
        public short getNodeType()
        {
            ThreadCop.access(doc);
            return doc.getNodeType();
        }

        /** {@inheritDoc} */
        @Override
        public Node getParentNode()
        {
            ThreadCop.access(doc);
            return doc.getParentNode();
        }

        /** {@inheritDoc} */
        @Override
        public NodeList getChildNodes()
        {
            ThreadCop.access(doc);
            return doc.getChildNodes();
        }

        /** {@inheritDoc} */
        @Override
        public Node getFirstChild()
        {
            ThreadCop.access(doc);
            return doc.getFirstChild();
        }

        /** {@inheritDoc} */
        @Override
        public Node getLastChild()
        {
            ThreadCop.access(doc);
            return doc.getLastChild();
        }

        /** {@inheritDoc} */
        @Override
        public Node getPreviousSibling()
        {
            ThreadCop.access(doc);
            return doc.getPreviousSibling();
        }

        /** {@inheritDoc} */
        @Override
        public Node getNextSibling()
        {
            ThreadCop.access(doc);
            return doc.getNextSibling();
        }

        /** {@inheritDoc} */
        @Override
        public NamedNodeMap getAttributes()
        {
            ThreadCop.access(doc);
            return doc.getAttributes();
        }

        /** {@inheritDoc} */
        @Override
        public Document getOwnerDocument()
        {
            ThreadCop.access(doc);
            return doc.getOwnerDocument();
        }

        /** {@inheritDoc} */
        @Override
        public Node insertBefore(Node newChild, Node refChild) throws DOMException
        {
            if( newChild == null||refChild == null ) throw new NullPointerException( "insertBefore( " + newChild + ","+refChild +")");
            ThreadCop.access(doc);
            return doc.insertBefore( newChild, refChild);
        }

        /** {@inheritDoc} */
        @Override
        public Node replaceChild(Node newChild, Node oldChild) throws DOMException
        {
            if( newChild == null||oldChild == null ) throw new NullPointerException( "replaceChild( " + newChild + ","+oldChild +")");
            ThreadCop.access(doc);
            return doc.replaceChild(newChild, oldChild);
        }

        /** {@inheritDoc} */
        @Override
        public Node removeChild(Node oldChild) throws DOMException
        {
            if( oldChild == null ) throw new NullPointerException( "removeChild( " + oldChild +")");

            ThreadCop.access(doc);
            return doc.removeChild(oldChild);
        }

        /** {@inheritDoc} */
        @Override
        public Node appendChild(Node newChild) throws DOMException
        {
            if( newChild == null ) throw new NullPointerException( "appendChild( " + newChild +")");

            ThreadCop.access(doc);
            return doc.appendChild(newChild);
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasChildNodes()
        {
            ThreadCop.access(doc);
            return doc.hasChildNodes();
        }

        /** {@inheritDoc} */
        @Override
        public Node cloneNode(boolean deep)
        {
            ThreadCop.access(doc);
            return doc.cloneNode(deep);
        }

        /** {@inheritDoc} */
        @Override
        public void normalize()
        {
            ThreadCop.access(doc);
            doc.normalize();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSupported(String feature, String version)
        {
            if( feature == null || version==null) throw new NullPointerException( "isSupported( " + feature +"," + version +")");
            ThreadCop.access(doc);
            return doc.isSupported(feature, version);
        }

        /** {@inheritDoc} */
        @Override
        public String getNamespaceURI()
        {
            ThreadCop.access(doc);
            return doc.getNamespaceURI();
        }

        /** {@inheritDoc} */
        @Override
        public String getPrefix()
        {
            ThreadCop.access(doc);
            return doc.getPrefix();
        }

        /** {@inheritDoc} */
        @Override
        public void setPrefix(String prefix) throws DOMException
        {
            if( prefix == null ) throw new NullPointerException( "setPrefix( " + prefix +")");

            ThreadCop.access(doc);
            doc.setPrefix(prefix);
        }

        /** {@inheritDoc} */
        @Override
        public String getLocalName()
        {
            ThreadCop.access(doc);
            return doc.getLocalName();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasAttributes()
        {
            ThreadCop.access(doc);
            return doc.hasAttributes();
        }

        /** {@inheritDoc} */
        @Override
        public String getBaseURI()
        {
            ThreadCop.access(doc);
            return doc.getBaseURI();
        }

        /** {@inheritDoc} */
        @Override
        public short compareDocumentPosition(Node other) throws DOMException
        {
            if( other == null ) throw new NullPointerException( "compareDocumentPosition( " + other +")");

            ThreadCop.access(doc);
            return doc.compareDocumentPosition(other);
        }

        /** {@inheritDoc} */
        @Override
        public String getTextContent() throws DOMException
        {
            ThreadCop.access(doc);
            return doc.getTextContent();
        }

        /** {@inheritDoc} */
        @Override
        public void setTextContent(String textContent) throws DOMException
        {
            if( textContent == null ) throw new NullPointerException( "setTextContent( " + textContent +")");
            ThreadCop.access(doc);
            doc.setTextContent(textContent);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSameNode(Node other)
        {
            ThreadCop.access(doc);
            return doc.isSameNode(other);
        }

        /** {@inheritDoc} */
        @Override
        public String lookupPrefix(String namespaceURI)
        {
            ThreadCop.access(doc);
            return doc.lookupPrefix(namespaceURI);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isDefaultNamespace(String namespaceURI)
        {
            ThreadCop.access(doc);
            return doc.isDefaultNamespace(namespaceURI);
        }

        /** {@inheritDoc} */
        @Override
        public String lookupNamespaceURI(String prefix)
        {
            ThreadCop.access(doc);
            return doc.lookupNamespaceURI(prefix);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEqualNode(Node arg)
        {
            ThreadCop.access(doc);
            return doc.isEqualNode(arg);
        }

        /** {@inheritDoc} */
        @Override
        public Object getFeature(String feature, String version)
        {
            ThreadCop.access(doc);
            return doc.getFeature(feature, version);
        }

        /** {@inheritDoc} */
        @Override
        public Object setUserData(String key, Object data, UserDataHandler handler)
        {
            ThreadCop.access(doc);
            return doc.setUserData(key, data, handler);
        }

        /** {@inheritDoc} */
        @Override
        public Object getUserData(String key)
        {
            ThreadCop.access(doc);
            return doc.getUserData(key);
        }
    }

    private static final ThreadLocal<DocumentBuilder> BUILDER_LOCAL=new ThreadLocal<>();
    private static final ThreadLocal<DocumentBuilder> BUILDER_TOLERANT_LOCAL=new ThreadLocal<>();
    private static final ThreadLocal<DocumentBuilder> BUILDER_VALIDATE_LOCAL=new ThreadLocal<>();
    
    private static final ThreadLocal<Transformer> TRANSFORMER_LOCAL=new ThreadLocal<>();
    private static final ConcurrentHashMap<String, Templates>TEMPLATES=new ConcurrentHashMap<>();
    
    private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final DocumentBuilderFactory BUILDER_TOLERANT_FACTORY = DocumentBuilderFactory.newInstance();
    private static final DocumentBuilderFactory BUILDER_VALIDATE_FACTORY = DocumentBuilderFactory.newInstance();
    
    private static final TransformerFactory TRANSFORMER_FACTORY;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.DocumentUtil");//#LOGGER-NOPMD
    private static final boolean ASSERT_ENABLED;

    static
    {
        boolean check=false;
        assert check=true;
        ASSERT_ENABLED=check;

        BUILDER_FACTORY.setValidating( false);
        BUILDER_FACTORY.setExpandEntityReferences( false);
        BUILDER_FACTORY.setNamespaceAware( true);
        BUILDER_FACTORY.setIgnoringComments( true);
        
        Class factoryClass = BUILDER_FACTORY.getClass();
        CodeSource cs = factoryClass.getProtectionDomain().getCodeSource();

        if (cs != null)
        {
            LOGGER.debug("XML factory: " + factoryClass + " class source : " + cs.getLocation());
        }
                
        BUILDER_TOLERANT_FACTORY.setValidating( false);
        BUILDER_TOLERANT_FACTORY.setNamespaceAware( false);
        BUILDER_TOLERANT_FACTORY.setIgnoringComments( true);     
        
        BUILDER_VALIDATE_FACTORY.setValidating( true);        
        BUILDER_VALIDATE_FACTORY.setNamespaceAware( true);
        
        TransformerFactory tmpFactory;
        try
        {
            tmpFactory = TransformerFactory.newInstance(
                "org.apache.xalan.processor.TransformerFactoryImpl",
                DocumentUtil.class.getClassLoader()
            );
        }
        catch( TransformerFactoryConfigurationError tfce)
        {
            LOGGER.warn("could not get xalan", tfce);
            tmpFactory = TransformerFactory.newInstance();
        }
        
        boolean cachable=true;
        TRANSFORMER_FACTORY=tmpFactory;
        try
        {
            Transformer transformer=TRANSFORMER_FACTORY.newTransformer();
            
            transformer.reset();
        }
        catch( TransformerConfigurationException e)
        {
            LOGGER.warn( "Can not reset transformer", e);
            cachable=false;
        }
        TRANSFORMER_CACHABLE=cachable;
        
        Class transformerFactoryClass = TRANSFORMER_FACTORY.getClass();
        cs = transformerFactoryClass.getProtectionDomain().getCodeSource();

        if (cs != null)
        {
            if( TRANSFORMER_CACHABLE == false)
            {
                LOGGER.warn("***** NON CACHABLE XLST factory: " + transformerFactoryClass + " class source : " + cs.getLocation());                
            }
            else
            {
                LOGGER.debug("XLST factory: " + transformerFactoryClass + " class source : " + cs.getLocation());
            }
        }             
    }
}
