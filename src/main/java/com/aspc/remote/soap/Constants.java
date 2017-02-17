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
package com.aspc.remote.soap;

 /**
 * User defined Constants class .                                              <BR>
 *                                                                             <BR>
 * <B><I>Result Group XML</I></B>                                              <BR>
 * <pre>
 *  &lt;RG version='2.0'&gt;
 *    &lt;RS offset='100' cursor='a1'&gt;
 *        &lt;DDL&gt;
 *          &lt;CD type='String'&gt;
 *            code
 *          &lt/CD&gt;
 *          &lt;CD type='String'&gt;
 *            name
 *          &lt/CD&gt;
 *        &lt;/DDL&gt;
 *        &lt;DML&gt;
 *          &lt;TR rec_key='AU@1~123@1' rec_id=1928274'&gt;
 *            &lt;COL name='code'&gt;
 *              AU
 *            &lt;/COL&gt;
 *            &lt;COL name='countryName'&gt;
 *              Australia
 *            &lt;/COL&gt;
 *          &lt;/TR&gt;
 *          &lt;TR rec_key='US@1~123@1' rec_id=1928275'&gt;
 *            &lt;COL name='code'&gt;
 *              US
 *            &lt;/COL&gt;
 *            &lt;COL name='countryName'&gt;
 *              U.S.A
 *            &lt;/COL&gt;
 *          &lt;/TR&gt;
 *        &lt;/DML&gt;
 *    &lt;/RS&gt;
 *    &lt;RS&gt;
 *        &lt;DDL&gt;
 *          &lt;CD type='String'&gt;
 *            name
 *          &lt/CD&gt;
 *          &lt;CD type='String'&gt;
 *            email
 *          &lt/CD&gt;
 *        &lt;/DDL&gt;
 *        &lt;DML&gt;
 *          &lt;TR rec_key='1@1~12@1' rec_id=12345'&gt;
 *            &lt;COL name='name'&gt;
 *              Nigel Leck
 *            &lt;/COL&gt;
 *            &lt;COL name='email'&gt;
 *              nigel@stsoftware.com
 *            &lt;/COL&gt;
 *          &lt;/TR&gt;
 *        &lt;/DML&gt;
 *    &lt;/RS&gt;
 *  &lt;/RG&gt;
 * </pre>
 *  <br>
 *  <i>THREAD MODE: NON-MUTABLE</i>
 * @author Nigel Leck
 * @since 29 September 2006
 */
public final class Constants
{
    /**
     * DISABLE the MOVE TO FEATURE
     */
    public static final String DISABLE_MOVE_TO              = "MOVE_TO";
    
    /**
     * XML type integer
     */
    public static final String XML_TYPE_INT                 = "int";

    /**
     * XML type float
     */
    public static final String XML_TYPE_FLOAT               = "float";
    /**
     * XML type boolean
     */
    public static final String XML_TYPE_BOOLEAN             = "boolean";
    /**
     * XML type Date
     */
    public static final String XML_TYPE_DATE                = "date";
    /**
     * XML type datetime
     */
    public static final String XML_TYPE_DATETIME            = "dateTime";
    /**
     * XML type string
     */
    public static final String XML_TYPE_STRING              = "string";

    /**
     * Field of type String
     */
    public static final String FIELD_TYPE_STRING            = "string";

    /**
     * Field of type Integer
     */
    public static final String FIELD_TYPE_INTEGER           = "int";

    /**
     * Field of type Long
     */
    public static final String FIELD_TYPE_LONG              = "long";

    /**
     * Field of type Double
     */
    public static final String FIELD_TYPE_DOUBLE            = "double";

    /**
     * Field of type Date
     */
    public static final String FIELD_TYPE_DATE              = "date";

    /**
     * Field of type Timestamp
     */
    public static final String FIELD_TYPE_TIMESTAMP         = "timestamp";

    /**
     * Field of type Boolean
     */
    public static final String FIELD_TYPE_BOOLEAN           = "boolean";

    /**
     * The command group tag.
     */
    public static final String ELM_COMMANDER                = "COMMANDER";

    /**
     * The command group tag.
     */
    public static final String ATT_COMMANDER_VERSION        = "version";

    /**
     * The result group tag.
     */
    public static final String ELM_RESULTGROUP              = "RG";

    /**
     * The version of the result group
     */
    public static final String ATT_RESULTGROUP_VERSION      = "version";

    /**
     * The version of the result group
     */
    public static final String ATT_RESULTGROUP_VERSION3      = "v";

    /**
     * The result set tag. There maybe 1..N result sets per group
     */
    public static final String ELM_RESULTSET                = "RS";

    /**
     * The title of this record set.
     */
    public static final String ATT_RESULTSET_TITLE          = "title";

    /**
     * The row offset for this record set
     */
    public static final String ATT_RESULTSET_OFFSET         = "offset";

    /**
     * The OPTIONAL row count for the record set.
     */
    public static final String ATT_RESULTSET_ROW_COUNT      = "row_count";

    /**
     * The OPTIONAL estimate row count for the record set.
     */
    public static final String ATT_RESULTSET_EST_COUNT      = "est_count";

    /**
     * The row seq. for this record set
     */
    public static final String ATT_RESULTSET_SEQ            = "seq";

    /**
     * is this not a query
     */
    public static final String ATT_QUERY                    = "query";
    
    /**
     * OPTIONAL = The name of the cursor which hold any more rows.
     */
    public static final String ATT_RESULTSET_CURSOR         = "cursor";

    /**
     * Data definition section.
     */
    public static final String ELM_RESULTSET_DDL            = "DDL";

    /**
     * The field definition. There will be 1..N columns
     */
    public static final String ELM_RESULTSET_CD             = "CD";
    /**
     * The type of the column eg. String, Integer, double.
     */
    public static final String ATT_RESULTSET_CD_TYPE        = "t";

    /**
     * The path of the column eg. father.firstName
     */
    public static final String ATT_RESULTSET_CD_PATH        = "p";

    /**
     * Data Model section.
     */
    public static final String ELM_RESULTSET_DML            = "DML";

    /**
     * A new row in the result set
     */
    public static final String ELM_RESULTSET_TR             = "R";

    /**
     * The record id for this row
     */
    public static final String ATT_RESULTSET_TR_REC_ID      = "id";

    /**
     * OPTIONAL - Record key if different to the record id
     */
    public static final String ATT_RESULTSET_TR_REC_KEY     = "key";

    /**
     * A column within the row
     */
    public static final String ELM_RESULTSET_TR_COL         = "C";

    /**
     * The path of the column.
     */
    public static final String ATT_RESULTSET_TR_COL_PATH    = "p";
    
    /**
     * The encoding type of the column.
     */
    public static final String ATT_RESULTSET_TR_COL_ENCODING = "e";

    /**
     * The encoding type of base 64
     */
    public static final String ENCODING_BASE64 = "64";

    /**
     * Element SESSION once the user logs in
     */
    public static final String ELM_SESSION                  = "SessionID";

    /**
     * Element LAYER once the user logs in
     */
    public static final String ELM_LAYER                    = "layer";
    
    /**
     * Element EXECUTE the commander method
     */
    public static final String ELM_EXECUTE                  = "execute";
    
    /**
     * Element FARM the commander method
     */
    public static final String ELM_FARM                  = "farm";

    /**
     * Element SQL the commander method
     */
    public static final String ELM_SQL                      = "sql";

    /**
     * Element ENCODED SQL the commander method
     */
    public static final String ELM_ESQL                     = "esql";

    /**
     * Element Login
     */
    public static final String ELM_LOGIN                    = "loginResult";
    /**
     * Element version status
     */
    public static final String ELM_VERSION_STATUS           = "VersionStatus";

    /**
     * Element wrap version
     */
    public static final String ELM_WRAP_VERSION           = "WrapVersion";
    
    /**
     * Session info
     */
    public static final String SOAPENV_HEADER_SESSION_INFO  = "SessionInfo";
    /**
     * name space to use
     */
    public static final String SOAPENV_NAMESPACE            = "http://www.aspconverters.com/soap";
    public static final String SOAP_HEADER            = "soapenv:Header";
    public static final String SOAP_BODY            = "soapenv:Body";

    /**
     * session prefix
     */
    public static final String SOAPENV_HEADER_SESSION_PREFIX = "sessionInfo";

    /**
     * username tag
     */
    public static final String SOAPENV_HEADER_SESSION_TAG_USERNAME = "SESSION_USER";

    /**
     * session id
     */
    public static final String SOAPENV_HEADER_SESSION_TAG_ID = "SESSION_ID";

    /**
     * session layer
     */
    public static final String SOAPENV_HEADER_SESSION_TAG_LAYER = "SESSION_SIGNATURE";

    /**
     * session timezone
     */
    public static final String SOAPENV_HEADER_SESSION_TAG_TZ = "SESSION_TZ";

    /**
     * LOGIN function                                                           <BR/>
     *
     *  &lt;q1:login xmlns:q1="http://www.ssmb.com/LoginService/binding"&gt;
     *      &lt;signature xsi:type="xsd:string"&gt;self_test&lt;/signature&gt;
     *      &lt;username xsi:type="xsd:string"&gt;user&lt;/username&gt;
     *      &lt;password xsi:type="xsd:string"&gt;abc123&lt;/password&gt;
     *      &lt;timeZone xsi:type="xsd:string"&gt;GMT -4:00&lt;/timeZone&gt;
     *      &lt;version xsi:type="xsd:string" /&gt;
     *      &lt;soapClient xsi:type="xsd:string" /&gt;
     *      &lt;timeZoneName xsi:type="xsd:string"&gt;Eastern Standard Time&lt;/timeZoneName&gt;
     *  &lt;/q1:login&gt;
     *
     */
    public static final String FUNCTION_LOGIN = "login";

    /**
     * the raw offset of this timezone
     */
    public static final String LOGIN_PARAMETER_TIME_ZONE="timeZone";

    /**
     * the name of this timezone
     */
    public static final String LOGIN_PARAMETER_TIME_ZONE_NAME="timeZoneName";

    /**
     * the language to use for this session
     */
    public static final String LOGIN_PARAMETER_LANGUAGE="language";

    /**
     * the password to attempt to log in with
     */
    public static final String LOGIN_PARAMETER_PASSWORD="password";

    /**
     * the layer to log into
     */
    public static final String LOGIN_PARAMETER_SIGNATURE="signature";

    /**
     * the user name to attempt to log in with
     */
    public static final String LOGIN_PARAMETER_USERNAME="username";

    /**
     * The java version of the client
     */
    public static final String LOGIN_PARAMETER_JAVA_VERSION="javaVersion";

    /**
     * The application version of the client
     */
    public static final String LOGIN_PARAMETER_APP_NAME="appName";

    /**
     * The soap version of the client
     */
    public static final String LOGIN_PARAMETER_SOAP_CLIENT="soapClient";

    /**
     * The client version of the client
     */
    public static final String LOGIN_PARAMETER_VERSION = "version";

    /**
     * The POST/SOAP parameter for joining requests
     */
    public static final String POST_PARAMETER_MESSAGE_ID = "messageID";

    /** 
     * The soap header "moveTo"
     */
    public static final String SOAP_HEADER_MOVE_TO="moveTo";

    /** 
     * The soap header "moveTo" URL
     */
    public static final String SOAP_HEADER_MOVE_TO_URL="URL";

    /**
     * You must change the password now.
     */
    public static final String FAULT_CODE_IMMEDIATE_PASSWORD_CHANGE = "System.Authentication.ImmediatePasswordChange";
    
    /**
     * You must change the password now.
     */
    public static final String FAULT_DESC_IMMEDIATE_PASSWORD_CHANGE  = "Immediate Password Change ";
    
    /**
     * The time zone. 
     */
    public static final String TIMEZONE = "timeZone";

    /** the secret */
    public static final String AUTHENTICATOR_SECRET="SECRET";
    
    /** the secret code */
    public static final String AUTHENTICATOR_SECRET_CODE="SECRET_CODE";
    
    /** the authorization */
    public static final String LOGIN_AUTHORIZATION="authorization";
    
            /**
     * prevent object from being created
     */
    private Constants()
    {
    }
}
