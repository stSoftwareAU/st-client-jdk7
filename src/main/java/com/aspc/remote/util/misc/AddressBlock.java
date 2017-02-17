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
package com.aspc.remote.util.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckReturnValue;
import javax.annotation.*;
import javax.annotation.Nullable;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import org.apache.commons.logging.Log;


/**
 *  <i>THREAD MODE: MULTI-THREADED</i>
 * @author Nigel Leck
 * @since 8 August 2014
 */
public final class AddressBlock
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.AddressBlock");//#LOGGER-NOPMD
    public static Builder builder()
    {
        return new Builder();
    }
    private List<String> dnsbls;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private static final String[] RECORD_TYPES = { "A", "TXT" };
    private final ConcurrentHashMap<String, DNSBL> cache=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Future<DNSBL>>loading=new ConcurrentHashMap();
    private long defaultTimeout;
    private InitialDirContext ictx;
    private boolean disabled;

    private AddressBlock()
    {

    }

    public @CheckReturnValue boolean knowsIP( final String ip)
    {
        if( StringUtilities.LOCAL_IP_PATTERN.matcher(ip).matches())
        {
            return true;
        }

        DNSBL details = fetchDetails(ip);

        return details!=null;
    }

    private @CheckReturnValue DNSBL fetchDetails( final String ip)
    {
        DNSBL details=cache.get(ip);

        if( details == null)
        {
            return null;
        }

        return details;
    }

    /**
     * The reason this IP is blocked.
     *
     * @param ip the address to check.
     * @param timeout in milliseconds or negative for default timeout
     * @return NULL if we have timed out, "" if not blocked and non-blank if blocked.
     */
    public @CheckReturnValue @Nullable String getReason( @Nonnull final String ip, final long timeout)
    {
        if( disabled) return null;

        if( StringUtilities.IP_PATTERN.matcher(ip).matches()==false)
        {
            throw new IllegalArgumentException( ip + " must match pattern " + StringUtilities.IP_PATTERN.pattern());
        }

        if( StringUtilities.LOCAL_IP_PATTERN.matcher(ip).matches())
        {
            return null;
        }

        long tmpTimeout=timeout;
        if( tmpTimeout < 0)
        {
            tmpTimeout=defaultTimeout;
        }

        DNSBL dnsbl=fetchDetails(ip);

        if( dnsbl == null)
        {
            Future<DNSBL> future = loading.get(ip);

            if( future == null)
            {
                Callable<DNSBL> call=new CallBL(ictx, dnsbls, ip);

                future = EXECUTOR.submit(call);

                loading.putIfAbsent(ip, future);
            }

            try
            {
                dnsbl=future.get(tmpTimeout, TimeUnit.MILLISECONDS);
                cache.put(ip, dnsbl);
                loading.remove(ip);
            }
            catch( ExecutionException ee)
            {
                LOGGER.warn( "could not fetch black list for " + ip, ee);
                loading.remove(ip);
            }
            catch (java.util.concurrent.TimeoutException ex)
            {
                //This is ok.
                return null;
            }
            catch( InterruptedException e)
            {
                throw CLogger.rethrowRuntimeExcepton(e);
            }
        }

        if( dnsbl == null)
        {
            assert false: "should not be null here";
            return "";
        }
        return dnsbl.reason;
    }

    private static class DNSBL
    {
        final long cachePeriod;
        final String reason;

        DNSBL( final String reason, final long cachePeriod)
        {
            this.reason=reason;
            this.cachePeriod=cachePeriod;
        }
    }

    public static class Builder
    {
        private Builder()
        {

        }

        private final ArrayList<String> providerDNS=new ArrayList<>();
        private final ArrayList<String> blDNS=new ArrayList<>();
        private long timeout=0;
        private boolean disabled;

        public @Nonnull Builder setDisabled( final boolean disabled)
        {
            this.disabled=disabled;
            return this;
        }

        public @Nonnull Builder addProviderDNS( @Nonnull final String ip)
        {
            if( StringUtilities.IP_PATTERN.matcher(ip).matches()==false)
            {
                throw new IllegalArgumentException( ip + " must match pattern " + StringUtilities.IP_PATTERN.pattern());
            }
            providerDNS.add(ip);
            return this;
        }

        public @Nonnull Builder addBL( @Nonnull final String dns)
        {
            if( StringUtilities.HOST_PATTERN.matcher(dns).matches()==false)
            {
                throw new IllegalArgumentException( dns + " must match pattern " + StringUtilities.IP_PATTERN.pattern());
            }
            blDNS.add(dns);
            return this;
        }

        public @Nonnull Builder setTimeout( final long timeout)
        {
            if( timeout <0)
            {
                throw new IllegalArgumentException( "Timeout must be greater than or equal to zero" );
            }

            this.timeout=timeout;

            return this;
        }

        public @CheckReturnValue @Nonnull AddressBlock create()
        {
            AddressBlock bl=new AddressBlock();

            @SuppressWarnings("UseOfObsoleteCollectionType")
            java.util.Hashtable<String, String> env = new java.util.Hashtable<>();

            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            if( timeout>0)
            {
                env.put("com.sun.jndi.dns.timeout.initial", Long.toString(timeout));
            }
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            if( providerDNS.isEmpty())
            {
                //"dns://8.8.8.8 dns://8.8.4.4"
                /* Open DNS */
                env.put(Context.PROVIDER_URL, "dns://208.67.222.222 dns://208.67.220.220");
            }
            else
            {
                StringBuilder sb=new StringBuilder();
                for( String ip: providerDNS)
                {
                    sb.append("dns://").append(ip).append( " ");
                }

                env.put(Context.PROVIDER_URL, sb.toString().trim());
            }

            try {
                bl.ictx = new InitialDirContext(env);
            } catch (NamingException ex) {
                throw CLogger.rethrowRuntimeExcepton(ex);
            }

            ArrayList tmpList=(ArrayList)blDNS.clone();
            if( tmpList.isEmpty())
            {
                tmpList.add("b.barracudacentral.org");
                tmpList.add("cbl.abuseat.org");
                tmpList.add("bl.blocklist.de");

                // tmpList.add("sbl.spamhaus.org");

               // tmpList.add("zen.spamhaus.org");
                //tmpList.add("blackholes.easynet.nl");
                        // dnsBL.addLookupService("blackholes.easynet.nl");
           // dnsBL.addLookupService("sbl.spamhaus.org");
          //  dnsBL.addLookupService("zen.spamhaus.org");
           // dnsBL.addLookupService("proxies.blackholes.wirehub.net");
           // dnsBL.addLookupService("bl.spamcop.net");
           // dnsBL.addLookupService("sbl.spamhaus.org");
           // dnsBL.addLookupService("dnsbl.njabl.org");
           // dnsBL.addLookupService("list.dsbl.org");
           // dnsBL.addLookupService("multihop.dsbl.org");
           // dnsBL.addLookupService("cbl.abuseat.org");
            }

            bl.dnsbls=Collections.unmodifiableList( tmpList);
            bl.disabled=disabled;
            return bl;
        }
    }

    private static class CallBL implements Callable<DNSBL> {

        private final String ip;
        private final List<String> dnsbls;
        private final InitialDirContext ictx;

        CallBL( final InitialDirContext ictx, final List<String> dnsbls, final String ip)
        {
            this.ip = ip;
            this.dnsbls=dnsbls;
            this.ictx=ictx;
        }

        @Override
        public DNSBL call() throws Exception
        {
            String[] parts = ip.split("\\.");
            String reversedAddress = parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0];
            DNSBL details=null;
            boolean issueFound=false;
            boolean someClean=false;
            for (String service : dnsbls)
            {
                try
                {
                    Attribute attribute;
                    Attributes attributes;

                    attributes = ictx.getAttributes(reversedAddress + "." + service, RECORD_TYPES);
                    attribute = attributes.get("TXT");

                    String reason = "blocked by " + service;
                    if (attribute != null)
                    {
                        String temp=attribute.toString();
                        if( StringUtilities.notBlank(temp)) reason=temp;
                    }
                    details=new DNSBL(reason, 24 * 60 * 60 * 1000);

                    break;
                }
                catch (NameNotFoundException e)
                {
                    //this is good
                    someClean=true;
                }
                catch (NamingException e)
                {
                    //LOGGER.warn( ip, e);

                    issueFound=true;
                }
            }

            if( details == null)
            {
                long cachePeriod=24 * 60 * 60 * 1000;

                if( issueFound)
                {
                    if( someClean)
                    {
                        cachePeriod=1 * 60 * 60 * 1000;
                    }
                    else
                    {
                        cachePeriod=5 * 60 * 1000;
                    }
                }

                details=new DNSBL("", cachePeriod);
            }
            return details;
        }
    }
}
