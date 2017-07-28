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

import com.aspc.remote.application.Shutdown;
import java.util.ArrayList;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ErrorCode;

import java.util.Properties;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.Session;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import org.apache.log4j.spi.TriggeringEventEvaluator;

/**
 * Send an e-mail when a specific logging event occurs, typically on
 * errors or fatal errors.
 * <p>The number of logging events delivered in this e-mail depend on
 * the value of <b>BufferSize</b> option. The
 * <code>SMTPAppender</code> keeps only the last
 * <code>BufferSize</code> logging events in its cyclic buffer. This
 * keeps memory requirements at a reasonable level while still
 * delivering useful application context.
 * By default, an email message will be sent when an ERROR or higher
 * severity message is appended.  The triggering criteria can be
 * modified by setting the evaluatorClass property with the name
 * of a class implementing TriggeringEventEvaluator, setting the evaluator
 * property with an instance of TriggeringEventEvaluator or
 * nesting a triggeringPolicy element where the specified
 * class implements TriggeringEventEvaluator.
 * 
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Ceki G&uuml;lc&uuml;
 *  @since       18 December 2007
 */
public final class DelaySMTPAppender extends AppenderSkeleton
        implements Runnable
{

    private String to;
    /**
     * Comma separated list of cc recipients.
     */
    private String cc;
    /**
     * Comma separated list of BCC recipients.
     */
    private String bcc;
    private String from;
    private String subject;
    private String smtpHost;
    private String smtpUsername;
    private String smtpPassword;
    private boolean smtpDebug = false;
    private boolean locationInfo = true;
    private int smtpPort=-1;
    private boolean sslEnabled=false;
    private static final BufferEntry BLANK = new BufferEntry();
    private static final ExecutorService SENDER= Executors.newSingleThreadExecutor();

    /**
     *
     */
    private final ArrayList<BufferEntry> msgBuffer = new ArrayList();
    /**
     *
     */
    protected Message msg;
    /**
     *
     */
    public long delaySeconds = 30;
    private final AtomicBoolean senderScheduled=new AtomicBoolean();
    private static final int MESSAGE_MAX_SIZE = 4096; // default messageSize
    private static final long SUPPRESS_INTERVAL = 60 * 1000L; // 1 minute
    private static final int MAX_MESSAGES = 60;
    
    private static long counterStartTime = 0;//MT CHECKED
    private static int counter = 0;//MT CHECKED
    private static boolean isSuppressed;//MT CHECKED
    
    /**
     * Store for the current thread whether we are currently appending to the
     * SMTP buffer
     */
    private static final ThreadLocal<Boolean> IS_THREAD_CURRENTLY_APPENDING = new ThreadLocal()
    {

        @Override
        protected Object initialValue()
        {
            return true;
        }
    };

    /**
     * The default constructor will instantiate the appender with a
     * {@link TriggeringEventEvaluator} that will trigger on events with
     * level ERROR or higher.
     */
    public DelaySMTPAppender()
    {
        final DelaySMTPAppender current=this;
        Shutdown.addListener(() -> {
            current.sendBuffer();
        });
    }

    /**
     * Enables or disables logging to SMTP for this thread
     * @param enabled
     */
    public static void setSMTPForThreadEnabled(boolean enabled)
    {
        IS_THREAD_CURRENTLY_APPENDING.set(enabled);
    }

    /**
     * Activate the specified options, such as the SMTP host, the
     * recipient, from, etc. 
     */
    @Override
    public void activateOptions()
    {
        Session session = createSession();
        msg = new MimeMessage(session);

        try
        {
            addressMessage(msg);
            if (subject != null)
            {
                msg.setSubject(subject);
            }
        } 
        catch (MessagingException e)
        {
            LogLog.warn("Could not activate SMTPAppender options.", e);
        }
    }

    /**
     *   Address message.
     *   @param msg message, may not be null.
     *   @throws MessagingException thrown if error addressing message. 
     */
    protected void addressMessage(final Message msg) throws MessagingException
    {
        if (from != null)
        {
            msg.setFrom(getAddress(from));
        }
        else
        {
            msg.setFrom();
        }

        if (to != null && to.length() > 0)
        {
            msg.setRecipients(Message.RecipientType.TO, parseAddress(to));
        }

        //Add CC receipients if defined.
        if (cc != null && cc.length() > 0)
        {
            msg.setRecipients(Message.RecipientType.CC, parseAddress(cc));
        }

        //Add BCC receipients if defined.
        if (bcc != null && bcc.length() > 0)
        {
            msg.setRecipients(Message.RecipientType.BCC, parseAddress(bcc));
        }
    }

    /**
     *  Create mail session.
     *  @return mail session, may not be null.
     */
    protected Session createSession()
    {
        Properties props;
        try
        {
            props = new Properties(System.getProperties());
        }
        catch (SecurityException ex)
        {
            props = new Properties();
        }

        if (StringUtilities.notBlank(smtpHost))
        {
            props.put("mail.smtp.host", smtpHost);
        }
        
        if(smtpPort>0)
        {
           props.put("mail.smtp.port", smtpPort);
        }
    
        props.put("mail.smtp.ssl.enable", sslEnabled);
        props.put("mail.smtp.socketFactory.fallback", "true");
        final String pw=smtpPassword == null? props.getProperty("mail.smtp.password" ) : smtpPassword;
        final String user=smtpUsername==null ? props.getProperty("mail.smtp.user" ): smtpUsername;
        
        Authenticator auth = null;
        if (StringUtilities.notBlank(pw) && StringUtilities.notBlank(user))
        {
            props.put("mail.smtp.auth", "true");
            auth = new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(user, pw);
                }
            };
        }
        Session session = Session.getInstance(props, auth);
        if (smtpDebug)
        {
            session.setDebug(smtpDebug);
        }
        return session;
    }

    private boolean isCurrentlyAppending()
    {
        boolean ret = IS_THREAD_CURRENTLY_APPENDING.get();
        return ret;
    }
    
    /**
     * Perform SMTPAppender specific appending actions, mainly adding
     * the event to a cyclic buffer and checking if the event triggers
     * an e-mail to be sent. 
     * @param event the event to log
     */
    @Override
    public void append(LoggingEvent event)
    {
        if (isCurrentlyAppending())
        {
            if (!checkEntryConditions())
            {
                return;
            }

            if (duplicateExists(event) == false)
            {
                event.getThreadName();
                event.getNDC();
                event.getMDCCopy();
                if (locationInfo)
                {
                    event.getLocationInformation();
                }

                BufferEntry entry = new BufferEntry();
                entry.setMessage(event,layout);

                synchronized (this)
                {
                    if (senderScheduled.get()==false)
                    {
                        SENDER.submit(this);
                        senderScheduled.set(true);
                    }

                    if( msgBuffer.size()>MAX_MESSAGES)
                    {
                        msgBuffer.add(BLANK);
                    }
                    else
                    {
                        msgBuffer.add(entry);
                    }
                }
            }
        }
    }

    /**
     * Check if event is already in the buffer
     * @param event the event
     * @return true if duplicate exists.
     */
    protected boolean duplicateExists(final LoggingEvent event)
    {
        Object msgObject = event.getMessage();

        String eventKey;

        if (msgObject != null)
        {
            eventKey = msgObject.toString();
        }
        else
        {
            eventKey = "<null>";
        }

        //cut off [REPEATED
        synchronized (this)
        {
            for (BufferEntry bufferEntry:msgBuffer)
            {
                String bufferMessage = bufferEntry.getMessage();

                if (eventKey.equals(bufferMessage))
                {
                    bufferEntry.incrementCount();

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This method determines if there is a sense in attempting to append.
     * <p>It checks whether there is a set output target and also if
     * there is a set layout. If these checks fail, then the boolean
     * value <code>false</code> is returned.
     * 
     * @return true if we should send. 
     */
    protected boolean checkEntryConditions()
    {
        if (this.msg == null)
        {
            errorHandler.error("Message object not configured.");
            return false;
        }

        if (this.layout == null)
        {
            errorHandler.error("No layout set for appender named [" + name + "].");
            return false;
        }
        return true;
    }

    /**
     *
     */
    @Override
    public synchronized void close()
    {
        this.closed = true;
    }

    private InternetAddress getAddress(String addressStr)
    {
        try
        {
            return new InternetAddress(addressStr);
        } 
        catch (AddressException e)
        {
            errorHandler.error("Could not parse address [" + addressStr + "].", e,
                    ErrorCode.ADDRESS_PARSE_FAILURE);
            return null;
        }
    }

    private InternetAddress[] parseAddress(String addressStr)
    {
        try
        {
            return InternetAddress.parse(addressStr, true);
        } 
        catch (AddressException e)
        {
            errorHandler.error("Could not parse address [" + addressStr + "].", e,
                    ErrorCode.ADDRESS_PARSE_FAILURE);
            return null;
        }
    }

    /**
     * Returns value of the <b>To</b> option.
     * @return people to send to.
     */
    public String getTo()
    {
        return to;
    }

    /**
     * The <code>SMTPAppender</code> requires a {@link
     * org.apache.log4j.Layout layout}.
     * @return requires layout. 
     */
    @Override
    public boolean requiresLayout()
    {
        return true;
    }

    /**
     * Send the contents of the cyclic buffer as an e-mail message.
     */
    protected void sendBuffer()
    {
        ArrayList<BufferEntry> buffer;
        synchronized (this)
        {
            try{
                buffer = (ArrayList) msgBuffer.clone();
                msgBuffer.clear();
            }
            finally
            {
                senderScheduled.set(false);
            }
        }

        if (buffer.isEmpty())
        {
            return;
        }

        StringBuilder sbuf = new StringBuilder();
        try
        {
            Date now = new Date();
            if(now.getTime() - counterStartTime > SUPPRESS_INTERVAL)
            {
                counter = 0;
                counterStartTime = now.getTime();
                isSuppressed = false;
            }
            counter++;
            if(counter > MAX_MESSAGES)
            {
                isSuppressed = true;
                return;
            }
            else if(counter == MAX_MESSAGES)
            {
                isSuppressed = true;
            }
            MimeBodyPart part = new MimeBodyPart();

            String t = layout.getHeader();
            if (t != null)
            {
                sbuf.append(t);
            }
            for (BufferEntry entry:buffer)
            {          
                sbuf.append(entry.getMessage());
                if (entry.getCount() > 1)
                {
                    sbuf.append("[ ");
                    sbuf.append( entry.getCount());
                    sbuf.append( " DUPLICATES]");
                }             
            }
            t = layout.getFooter();
            if (t != null)
            {
                sbuf.append(t);
            }
            // truncate the message
            int currentLenght = sbuf.length();
            String messageBody;
            if (currentLenght >= MESSAGE_MAX_SIZE && buffer.size() > 1)
            {
                messageBody = sbuf.substring(0, MESSAGE_MAX_SIZE) + Layout.LINE_SEP + "The message truncated from " + (currentLenght/1024) + "k";
            }
            else
            {
                messageBody = sbuf.toString();
            }

            part.setContent(messageBody, layout.getContentType());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            msg.setContent(mp);

            msg.setSentDate(new Date());
            if(isSuppressed)
            {
                msg.setSubject("Additional Emails Suppressed");
            }
            Transport.send(msg);
        } 
        catch (MessagingException e)
        {
            LogLog.warn(sbuf.toString());
            LogLog.warn("Error occurred while sending e-mail notification.", e);
        }
    }

    /**
     * Returns value of the <b>From</b> option.
     * @return from list
     */
    public String getFrom()
    {
        return from;
    }

    /**
     * Returns value of the <b>Subject</b> option.
     * @return the subject
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * The <b>From</b> option takes a string value which should be a
     * e-mail address of the sender.
     * @param from who from 
     */
    public void setFrom(String from)
    {
        this.from = from;
    }

    /**
     * The <b>Subject</b> option takes a string value which should be a
     * the subject of the e-mail message.
     * @param subject the subject of the mail 
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    /**
     * The <b>SMTPHost</b> option takes a integer for the port.
     * @param smtpPort  the port
     */
    public void setSMTPPort(final int smtpPort)
    {
        this.smtpPort = smtpPort;
    }
    
    /**
     * Turn on SSL.
     * @param sslEnabled  Should use SSL
     */
    public void setSSL(final boolean sslEnabled)
    {
        this.sslEnabled = sslEnabled;
    }
    
    /**
     * The <b>SMTPHost</b> option takes a string value which should be a
     * the host name of the SMTP server that will send the e-mail message.
     * @param smtpHost  the host
     */
    public void setSMTPHost(String smtpHost)
    {
        this.smtpHost = smtpHost;
    }

    /**
     * Returns value of the <b>SMTPHost</b> option.
     * @return the host
     */
    public String getSMTPHost()
    {
        return smtpHost;
    }

    /**
     * The <b>To</b> option takes a string value which should be a
     * comma separated list of e-mail address of the recipients.
     * @param to 
     */
    public void setTo(String to)
    {
        this.to = to;
    }

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By
     * default, it is set to false which means there will be no effort
     * to extract the location information related to the event. As a
     * result, the layout that formats the events as they are sent out
     * in an e-mail is likely to place the wrong location information
     * (if present in the format).
     * <p>Location information extraction is comparatively very slow and
     * should be avoided unless performance is not a concern.
     * @param locationInfo 
     */
    public void setLocationInfo(boolean locationInfo)
    {
        this.locationInfo = locationInfo;
    }

    /**
     * Returns value of the <b>LocationInfo</b> option.
     * @return the value
     */
    public boolean getLocationInfo()
    {
        return locationInfo;
    }

    /**
     * Set the frequency of the thread that sends the buffer.
     * @param delaySeconds the number of seconds to delay
     */
    public void setDelaySeconds(long delaySeconds)
    {
        this.delaySeconds = delaySeconds;
    }

    /**
     * Returns value of the <b>delaySeconds</b> option.
     * @return the value
     */
    public long getDelaySeconds()
    {
        return delaySeconds;
    }

    /**
     * Set the cc recipient addresses.
     * @param addresses recipient addresses as comma separated string, may be null.
     */
    public void setCc(final String addresses)
    {
        this.cc = addresses;
    }

    /**
     * Get the cc recipient addresses.
     * @return recipient addresses as comma separated string, may be null.
     */
    public String getCc()
    {
        return cc;
    }

    /**
     * Set the BCC recipient addresses.
     * @param addresses recipient addresses as comma separated string, may be null.
     */
    public void setBcc(final String addresses)
    {
        this.bcc = addresses;
    }

    /**
     * Get the BCC recipient addresses.
     * @return recipient addresses as comma separated string, may be null.
     */
    public String getBcc()
    {
        return bcc;
    }

    /**
     * The <b>SmtpPassword</b> option takes a string value which should be the password required to authenticate against
     * the mail server.
     * @param password password, may be null.
     */
    public void setSMTPPassword(final String password)
    {
        this.smtpPassword = password;
    }

    /**
     * The <b>SmtpUsername</b> option takes a string value which should be the username required to authenticate against
     * the mail server.
     * @param username user name, may be null.
     */
    public void setSMTPUsername(final String username)
    {
        this.smtpUsername = username;
    }

    /**
     * Setting the <b>SmtpDebug</b> option to true will cause the mail session to log its server interaction to stdout.
     * This can be useful when debugging the appender but should not be used during production because username and
     * password information is included in the output.
     * @param debug debug flag.
     */
    public void setSMTPDebug(final boolean debug)
    {
        this.smtpDebug = debug;
    }

    /**
     * Get SMTP password.
     * @return SMTP password, may be null.
     */
    public String getSMTPPassword()
    {
        return smtpPassword;
    }

    /**
     * Get SMTP user name.
     * @return SMTP user name, may be null.
     */
    public String getSMTPUsername()
    {
        return smtpUsername;
    }

    /**
     * Get SMTP debug.
     * @return SMTP debug flag.
     */
    public boolean getSMTPDebug()
    {
        return smtpDebug;
    }

    /**
     * Only send the email buffer periodically
     * 
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void run()
    {
        Thread t = Thread.currentThread();
        String tn=t.getName();

        try
        {
            t.setName("delay email appender [waiting]");

            Thread.sleep(delaySeconds * 1000);
        } 
        catch (InterruptedException e)
        {
        }
        finally
        {
            try{
                sendBuffer(); // The actual sendig of the emails. 
            }
            finally
            {
                t.setName(tn);
            }
        }
    }

    /**
     * Class to store the LoggingEvent and count of duplicate messages
     * 
     */
    private static class BufferEntry
    {   
        int eventCount;
        int originalLength;
        boolean isTruncated = false;
        String message = "<null>";

        BufferEntry()
        {     
            this.eventCount = 1;
            this.originalLength = 0;
        }

        void incrementCount()
        {
            eventCount++;
        }

        void setMessage(final LoggingEvent event, final Layout aLayout)
        {
            if (event != null)
            {
                String originalMessage =  aLayout.format(event);
                if (originalMessage != null)
                {     
                    this.originalLength = originalMessage.length();
                    if (this.originalLength >= MESSAGE_MAX_SIZE)
                    {
                        this.message = originalMessage.substring(0, MESSAGE_MAX_SIZE);
                        this.isTruncated = true;
                    }
                    else
                    {
                        this.message = originalMessage;
                    }                

                    if (aLayout.ignoresThrowable())
                    {
                        String[] s = event.getThrowableStrRep();
                        StringBuilder sb = new StringBuilder();
                        if (s != null)
                        {
                            for (String item : s)
                            {
                                sb.append(item);
                                sb.append(Layout.LINE_SEP);
                            }
                        }
                        this.message = this.message +  sb.toString();
                    }                
                }
                else
                {
                    this.message = "<null>"; 
                }
            }  
        }

        String getMessage()
        { 
            String tmpMessage = "";
            if (this.isTruncated)
            {
                tmpMessage = Layout.LINE_SEP + "The inner message truncated from " + (originalLength/1024) + "k";
            }
            return this.message + tmpMessage;
        }

        int getCount()
        {
            return eventCount;
        }

        int getMessageOriginalLength()
        {
            return this.originalLength;
        }

        boolean getIsTruncated()
        {
            return this.isTruncated;
        }
    }
    
    static
    {
        ThreadPool.addPurifier(new DelaySMTPPurifier());
    }    
}
