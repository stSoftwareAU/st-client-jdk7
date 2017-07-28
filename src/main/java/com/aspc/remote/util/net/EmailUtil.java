/**
 * STS Remote library
 *
 * Copyright (C) 2006 stSoftware Pty Ltd
 *
 * stSoftware.com.au
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Bug fixes, suggestions and comments should be sent to:
 *
 * info AT stsoftware.com.au
 *
 * or by snail mail to:
 *
 * stSoftware building C, level 1, 14 Rodborough Rd Frenchs Forest 2086
 * Australia.
 */
package com.aspc.remote.util.net;

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.ThreadPool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.naming.CommunicationException;
import javax.naming.Context;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * EmailUtil
 *
 * <br>
 * <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author Nigel Leck
 * @since July 29, 2013
 */
public final class EmailUtil
{
    public static enum ConnectionSecurity{
        SSL_TLS,
        STARTTLS,
        NONE;
    };
    
    /**
     * Which DNS provider should we use ( Google by default)
     */
    private static final String DNS_PROVIDER_URL = "DNS_PROVIDER_URL";

    /** 
     * How long to remember the domain name.
     */
    private static final long TTL_GOOD=4L * 60L * 60L * 1000L;
    private static final long TTL_BAD=5L * 60L * 1000L;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.EmailUtil");//#LOGGER-NOPMD
    private static final HashSet<String> KNOWN_GOOD_HOSTS;
    public static final ThreadLocal<Boolean> DISABLE_MX_LOOPUP=new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };
    
    /**
     * http://www.regular-expressions.info/email.html
     */
    private static final String EMAIL_REGEX = "^[A-Z0-9._%+\\-#'&]+@[A-Z0-9.-]+\\.[A-Z]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile( EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    @Nonnull @CheckReturnValue
    public static Session makeSessionSMTP(
        final String host, 
        final int port, 
        final String username, 
        final String password, 
        final ConnectionSecurity security
    ) throws InvalidDataException
    {
        Properties props = new Properties();

        if (StringUtilities.isBlank(host))
        {
            throw new InvalidDataException("No host defined for emailing");
        }
        
        props.put("mail.smtp.host", host);
          
        switch (security) {
            case SSL_TLS:
                props.put("mail.smtp.port", 465);
                props.put("mail.smtp.ssl.enable", "true");
                break;
            case STARTTLS:
                props.put("mail.smtp.port", 587);
                props.setProperty("mail.smtp.starttls.enable", "true");
                break;
            case NONE:
                props.put("mail.smtp.port", 25);
                break;
            default:
                throw new InvalidDataException("Invalid Security " + security);
        }
        
        if (port > 0)
        {
            props.put("mail.smtp.port", port);
        }
        
        Session session;
        if (StringUtilities.notBlank(username))
        {
            props.put("mail.smtp.auth", "true");

            session = Session.getInstance(props, new PasswordAuthenticator(username, password));
        }
        else
        {
            props.put("mail.smtp.auth", "false");
            session = Session.getInstance(props, null);
        }
        return session;
    }
    
    /**
     * validate if the url in html email body is on localhost
     * @param htmlBody email html body
     * @return returns null if html valid, or the invalid url
     */
    @CheckReturnValue @Nullable
    public static String checkLinks(final @Nonnull String htmlBody)
    {
        Document doc = Jsoup.parse(htmlBody);
        Elements elements = doc.getAllElements();
        for(Element e : elements)
        {
            String tag = e.tagName();
            String url = "";
            if("a".equalsIgnoreCase(tag))
            {
                url = e.attr("href");
            }
            else if("img".equalsIgnoreCase(tag))
            {
                url = e.attr("src");
            }
            if(StringUtilities.isBlank(url))
            {
                continue;
            }
            //remove variables
            while(true)
            {
                int pos1 = url.indexOf("${");
                if(pos1 == -1)
                {
                    break;
                }
                int pos2 = url.indexOf('}', pos1);
                if(pos2 == -1)
                {
                    break;
                }
                url = url.substring(0, pos1) + "a" + url.substring(pos2 + 1);
            }
            if(url.contains("://"))
            {
                try
                {
                    URL u = new URL(url);
                    if("localhost".equalsIgnoreCase(u.getHost()))
                    {
                        return url;
                    }
                }
                catch(MalformedURLException ex)
                {
                    return url;
                }
            }
        }
        return null;
    }
    
    public static void validateSMTP(
        final String host, 
        final int port, 
        final String username, 
        final String password, 
        final ConnectionSecurity security
    ) throws AuthenticationFailedException, MessagingException
    {
        try {
            
            Session session = makeSessionSMTP(host, port, username, password, security);
            Properties p=session.getProperties();
            p.setProperty("mail.smtp.connectiontimeout", "10000");
            p.setProperty("mail.smtp.timeout", "10000");
            Transport transport = session.getTransport("smtp");
            transport.connect();
//            if( port>0)
//            {
//                transport.connect(host, port, username, password);
//            }
//            else
//            {
//                transport.connect(host, username, password);
//            }
            
            transport.close();

        } catch(AuthenticationFailedException e) {
            LOGGER.warn("SMTP: Authentication Failed " + host, e);
            throw e;

        } catch(MessagingException e) {
            LOGGER.warn("SMTP: Messaging Exception Occurred " + host, e);
            throw e;
        } catch (InvalidDataException e) {
            String msg=e.getMessage();
            LOGGER.error(msg, e);
            throw new MessagingException( msg, e);
        }
    }

    /**
     * Validate an email address.
     *
     * @param email the email to validate.
     * @param hostCache OPTIONAL cache of host names
     * @throws InvalidDataException details of the issue found.
     *
     * http://en.wikipedia.org/wiki/E-mail_address#Local_part
     */
    public static void validate(final @Nonnull String email, final @Nullable Map<String, Object> hostCache) throws InvalidDataException
    {
        String checkEmail=email.toLowerCase().trim();
        String invalidStrings[] =
        {
            "..", ".@"
        };
        for (String illegal : invalidStrings)
        {
            if (email.contains(illegal))
            {
                throw new InvalidDataException("email " + email + " may not contain '" + illegal + "'");
            }
        }

        String[] split = checkEmail.split("@");

        if (split.length != 2)
        {
            throw new InvalidDataException("email " + email + " should contain one @ symbol");
        }

        Matcher m = EMAIL_PATTERN.matcher(checkEmail);
        if (m.matches() == false)
        {
            throw new InvalidDataException("email " + email + " is an invalid email");
        }

        String domain = split[1];

        String message = checkMX(domain, hostCache);
        if (message != null)
        {
            throw new InvalidDataException(message);
        }
    }

    /**
     * returns a String array of mail exchange servers (mail hosts) sorted from most preferred to least preferred
     * see: RFC 974 - Mail routing and the domain system
     * see: RFC 1034 - Domain names - concepts and facilities
     * see: http://java.sun.com/j2se/1.5.0/docs/guide/jndi/jndi-dns.html
     *    - DNS Service Provider for the Java Naming Directory Interface (JNDI)
     * @param domainName the domain to check     
     * @return list of hosts
     * @throws javax.naming.NamingException
     */
    @Nonnull @CheckReturnValue
    public static String[] lookupMailHosts(final @Nonnull String domainName) throws NamingException
    {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        final java.util.Hashtable<String,String> env=new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        
        env.put("com.sun.jndi.dns.timeout.initial", "5000");    /* quite short... too short? */
        env.put("com.sun.jndi.dns.timeout.retries", "3");
        
        env.put(Context.PROVIDER_URL, CProperties.getProperty(DNS_PROVIDER_URL, "dns://8.8.8.8,dns://8.8.4.4"));
        
        InitialDirContext ictx = new InitialDirContext( env);

        // get the MX records from the default DNS directory service provider
        //    NamingException thrown if no DNS record found for domainName
        Attributes attributes = ictx.getAttributes("dns:/" + domainName, new String[] {"MX"});
        // attributeMX is an attribute ('list') of the Mail Exchange(MX) Resource Records(RR)
        Attribute attributeMX = attributes.get("MX");

        // if there are no MX RRs then default to domainName (see: RFC 974)
        if (attributeMX == null)
        {
            return (new String[] {domainName});
        }

        // split MX RRs into Preference Values(pvhn[0]) and Host Names(pvhn[1])
        String[][] pvhn = new String[attributeMX.size()][2];
        for (int i = 0; i < attributeMX.size(); i++)
        {
            pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");
        }

        // sort the MX RRs by RR value (lower is preferred)
        Arrays.sort(pvhn, (String[] o1, String[] o2) -> (Integer.parseInt(o1[0]) - Integer.parseInt(o2[0])));

        // put sorted host names in an array, get rid of any trailing '.'
        String[] sortedHostNames = new String[pvhn.length];
        for (int i = 0; i < pvhn.length; i++)
        {
            sortedHostNames[i] = pvhn[i][1].endsWith(".") ?
                pvhn[i][1].substring(0, pvhn[i][1].length() - 1) : pvhn[i][1];
        }
        return sortedHostNames;
    }

    private static class MX{
        final String message;
        final long expiry;
        MX( final String message, final long expiry)
        {
            this.message=message;
            this.expiry=expiry;
        }
    }
    
    /**
     * check the MX record
     *
     * @param hostName host to check
     * @param hostCache OPTIONAL cache of host names
     * @return message if NOT valid otherwise NULL
     */
    @CheckReturnValue @Nullable
    public static String checkMX(final @Nonnull String hostName, final @Nullable Map<String, Object> hostCache)
    {
        assert hostName.contains("@")==false: "Invalid host name " + hostName;
        
        String message = null;
        if( DISABLE_MX_LOOPUP.get()==false)
        {
            String tmpHostName=hostName.toLowerCase();
            if (hostCache != null)
            {
                Object value=hostCache.get(tmpHostName);
                if( value instanceof MX)
                {
                    MX mx=(MX)value;
                    if( mx.expiry<System.currentTimeMillis())
                    {
                        message=null;
                        hostCache.remove(tmpHostName);
                    }
                    else
                    {
                        message=mx.message;
                    }
                }
                else if( value instanceof String)
                {
                    message=(String)value;
                }
            }

            if (message == null && KNOWN_GOOD_HOSTS.contains(tmpHostName)==false)
            {
                try
                {
                    String[] lookupMailHosts = lookupMailHosts(hostName);

                    if( lookupMailHosts.length > 0)
                    {
                        message="";
                    }
                    else
                    {
                        message="no mail server(MX record) for " + hostName;
                    }
                }
                catch( CommunicationException ce)
                {
                    LOGGER.warn(hostName, ce);
                }
                catch( NamingException ne)
                {
                    LOGGER.warn(hostName, ne);
                    message=ne.getMessage();
                    if( message.contains("DNS name not found"))
                    {
                        message="No DNS record for '" + hostName + "'";
                    }
                    else if( message.equalsIgnoreCase("DNS error"))
                    {
                        message="couldn't find any name servers for '" + hostName + "'";
                    }
                    else if( message.startsWith("DNS server failure"))
                    {
                        message = "unknown host '" + hostName + "', " + message;
                    }
                }

                if (hostCache != null)
                {
                    if( message==null)
                    {
                        hostCache.remove(tmpHostName);  
                    }
                    else
                    {
                        MX mx;
                        if( StringUtilities.isBlank(message))
                        {
                            mx=new MX( "", System.currentTimeMillis() + TTL_GOOD);
                        }
                        else
                        {
                            mx=new MX( message, System.currentTimeMillis() + TTL_BAD);
                        }                        

                        hostCache.put(tmpHostName, mx);
                    }
                }
            }
        }

        if (message == null || message.isEmpty())
        {
            return null;
        }

        return message;
    }

    static
    {
        KNOWN_GOOD_HOSTS=new HashSet();
        KNOWN_GOOD_HOSTS.add( "optusnet.com.au");// only seems to be valid in Australia
        KNOWN_GOOD_HOSTS.add( "gmail.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "hotmail.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "google.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "defence.gov.au");// no need to check

        KNOWN_GOOD_HOSTS.add( "hostpapa.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "hostgator.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "ipage.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "bluehost.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "siteground.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "yahoo.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "godaddy.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "hostmonster.com");// no need to check
        KNOWN_GOOD_HOSTS.add( "ixwebhosting.com");// no need to check

        ThreadPool.addPurifier(DISABLE_MX_LOOPUP::remove);
    }
    
    private static class PasswordAuthenticator extends Authenticator
    {

        PasswordAuthenticator(final String user, final String password)
        {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            if (StringUtilities.isBlank(user))
            {
                return null;
            }
            return new PasswordAuthentication(user, password);
        }
        private final String user,  password;
    }
}
