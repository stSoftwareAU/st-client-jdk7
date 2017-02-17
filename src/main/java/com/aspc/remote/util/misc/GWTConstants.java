package com.aspc.remote.util.misc;

/**
 *
 *  @author      Lei Gao
 *  @version     $Revision: 1.1 $
 *  @since       22 April 2016 
 */
public interface GWTConstants
{
    // repeat rule
    /**
     * the title
     */
    public static final String ELEMENT_TITLE="TITLE";
    /**
     * The division
     */
    public static final String ELEMENT_DIV="DIV";
    /**
     * The menu
     */
    public static final String REPEAT_RULE_HTML_ID="GWT_REPEAT_RULE";
    /**
     * the dictionary
     */
    public static final String DICT_REPEAT_RULE="GWT_DICT_REPEAT_RULE";

    /**
     * byDay hidden field id
     */
    public static final String DICT_REPEAT_RULE_BYDAY_ID="GWT_DICT_REPEAT_RULE_BYDAY_ID";
    
     /**
     * byMonthDay hidden field id
     */
    public static final String DICT_REPEAT_RULE_BYMONTHDAY_ID="GWT_DICT_REPEAT_RULE_BYMONTHDAY_ID";
    
     /**
     * byMonth hidden field id
     */
    public static final String DICT_REPEAT_RULE_BYMONTH_ID="GWT_DICT_REPEAT_RULE_BYMONTH_ID";
    
     /**
     * frequency hidden field id
     */
    public static final String DICT_REPEAT_RULE_FREQUENCY_ID = "GWT_DICT_REPEAT_RULE_FREQUENCY";
    
    /**
     * interval hidden field id
     */
    public static final String DICT_REPEAT_RULE_INTERNAL_ID = "GWT_DICT_REPEAT_RULE_INTERNAL";
    
    /**
     * start date hidden field id
     */
    public static final String DICT_REPEAT_RULE_STARTDT_ID = "GWT_DICT_REPEAT_RULE_STARDDT_ID";
    
     /**
     * end date hidden field id
     */
    public static final String DICT_REPEAT_RULE_ENDDT_ID = "GWT_DICT_REPEAT_RULE_ENDDT_ID";
    
     /**
     * num of times hidden field id
     */
    public static final String DICT_REPEAT_RULE_NUMOFTIMES_ID = "GWT_DICT_REPEAT_RULE_NUMOFTIMES_ID";
    
    /**
     * the message
     */
    public static final String DICTIONARY_MESSAGE_LIST="MESSAGE_LIST";

    public static final String DICTIONARY_TABLE_LIST="TABLE_LIST";

    /**
     * The components
     */
    public static final String DICTIONARY_COMPONENTS="components";

    /**
     * the list of resizable columns.
     */
    public static final String DICTIONARY_RESIZEABLE="resizeable";

     /**
     * The label
     */
    public static final String TOOLBAR_RIGHT_BUTTON_LABEL= "TOOLBAR_RIGHT_BUTTON_LABEL";
    
    /**
     * The type                                                                                                                                                                                                                                                                         
     */
    public static final String TOOLBAR_RIGHT_BUTTON_TYPE = "TOOLBAR_RIGHT_BUTTON_TYPE";

    /**
     * Module name
     */
    public static final String AJAX_FIELD_MODULE_NAME = "com.aspc.gwt.ajaxfield.Ajaxfield";
    
    /**
     * Parameter Dictionary name
     */
    public static final String AJAX_FIELD_DICTIONARY = "AJAX_FIELD_DICTIONARY";
    /**
     * field count parameter
     */
    public static final String NUM_FIELDS_ON_PAGE = "NUM_FIELDS_ON_PAGE";
    /**
     * DOM id field prefix
     */
    public static final String DOM_ID_FIELD = "DOM_KEY_FIELD";
    /**
     * Path field prefix
     */
    public static final String PATH_FIELD  = "PATH_FIELD";
    /**
     * db class prefix
     */
    public static final String DB_CLASS_FIELD = "DB_CLASS_FIELD";
    /**
     * global key prefix
     */
    public static final String GLOBAL_KEY_FIELD = "GLOBAL_KEY_FIELD";
    /**
     * format prefix
     */
    public static final String FORMAT_FIELD = "FORMAT_FIELD";
    
    /**
     * Font bold
     */
    public static final String FONT_BOLD = "FONT_BOLD";
    
    /**
     * Font size
     */
    public static final String FONT_SIZE = "FONT_SIZE";
        
    /**
     * includes tags.
     */
    public static final String INCLUDES_TAGS = "INCLUDES_TAGS";

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
    public static final String ATT_RESULTSET_SEQ           = "seq";

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
     * The soap header
     */
    public static final String SOAP_HEADER="soapenv:Header";

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
    /**
     *
     */
    public static final String DICTIONARY_RELOAD="RELOAD";
    /**
     *
     */
    public static final String ELEMENT_COUNTER="COUNTER";
    /**
     *
     */
    public static final String ELEMENT_TARGET="TARGET";
    /**
     *
     */
    public static final String ELEMENT_SECONDS="SECONDS";
}
