package com.aspc.remote.rest;

import com.aspc.remote.rest.errors.*;
import com.aspc.remote.util.misc.CLogger;
import java.io.FileNotFoundException;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  ReST status.
 * 
 *  @author      Nigel Leck
 *  @since       16 April 2015
 */
public enum Status
{
    /**
     * 100 Continue.
     * The 100 (Continue) status code indicates that the initial part of a request has been received and has not yet been rejected by the server.
     */
    C100_CONTINUE(
        100, 
        "Continue", 
        "The 100 (Continue) status code indicates that the initial part of a request has been received and has not yet been rejected by the server.",
        "https://tools.ietf.org/html/rfc7231#section-6.2.1"
    ),
    
    /**
     * 101 Switching Protocols.
     * The 101 (Switching Protocols) status code indicates that the server understands and is willing to comply with the client's request.
     */
    C101_SWITCHING_PROTOCOLS(
        101, 
        "Switching Protocols", 
        "The 101 (Switching Protocols) status code indicates that the server understands and is willing to comply with the client's request",
        "https://tools.ietf.org/html/rfc7231#section-6.2.2"
    ),

    
    /**
     * 102 Processing.
     * The Status-URI response header may be used with the 102 (Processing).
     */
    C102_PROCESSING(
        102, 
        "Processing", 
        "The Status-URI response header may be used with the 102 (Processing)",
        "https://tools.ietf.org/html/rfc2518#section-9.7"
    ),
    
    /*
     *  <h2>2xx Success</h2>
     *  This class of status codes indicates the action requested by the client was received, understood, accepted and processed successfully.
     */
    
    /**
     * 200 OK.
     * Standard response for successful HTTP requests. The actual response will depend on the request method used. In a GET request, the response will contain an entity corresponding to the requested resource. In a POST request, the response will contain an entity describing or containing the result of the action.
     */
    C200_SUCCESS_OK(
        200, 
        "OK", 
        "Standard response for successful HTTP requests. The actual response will depend on the request method used. In a GET request, the response will contain an entity corresponding to the requested resource. In a POST request, the response will contain an entity describing or containing the result of the action.",
        "https://tools.ietf.org/html/rfc7231#section-6.3.1"
    ),
    
    /**
     * 201 Created.
     * The request has been fulfilled and resulted in a new resource being created.
     */
    C201_SUCCESS_CREATED(
        201, 
        "Created",
        "The request has been fulfilled and resulted in a new resource being created.",
        "https://tools.ietf.org/html/rfc7231#section-6.3.2"
    ),
    
    /**
     * 202 Accepted.
     * The request has been accepted for processing, but the processing has not been completed. The request might or might not eventually be acted upon, as it might be disallowed when processing actually takes place.
     */
    C202_SUCCESS_ACCEPTED(
        202, 
        "Accepted", 
        "The request has been accepted for processing, but the processing has not been completed. The request might or might not eventually be acted upon, as it might be disallowed when processing actually takes place.",
        "https://tools.ietf.org/html/rfc7231#section-6.3.3"
    ),
    
    /**
     * 203 Non-Authoritative Information.
     * The server successfully processed the request, but is returning information that may be from another source.
     */
    C203_SUCCESS_NON_AUTHORITIVE(
        203, 
        "Non-Authoritative Information", 
        "The server successfully processed the request, but is returning information that may be from another source.",
        "https://tools.ietf.org/html/rfc7231#section-6.3.4"
    ),
    
    /**
     * 204 No Content.
     * The server successfully processed the request, but is not returning any content. Usually used as a response to a successful delete request.
     */
    C204_SUCCESS_NO_CONTENT(
        204, 
        "No Content", 
        "The server successfully processed the request, but is not returning any content. Usually used as a response to a successful delete request.",
        "https://tools.ietf.org/html/rfc7231#section-6.3.5"
    ),
    
    /**
     * 205 Reset Content.
     * The server successfully processed the request, but is not returning any content. Unlike a 204 response, this response requires that the requester reset the document view.
     */
    C205_SUCCESS_RESET_CONTENT(
        205, 
        "Reset Content", 
        "The server successfully processed the request, but is not returning any content. Unlike a 204 response, this response requires that the requester reset the document view.",
        "https://tools.ietf.org/html/rfc7231#section-6.3.6"
    ),
    
    /**
     * 206 Partial Content.
     * The server is delivering only part of the resource (byte serving) due to a range header sent by the client. The range header is used by tools like wget to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams.
     */
    C206_SUCCESS_PARTIAL_CONTENT(
        206, 
        "Partial Content",
        "The server is delivering only part of the resource (byte serving) due to a range header sent by the client. The range header is used by tools like wget to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams.",
        "https://tools.ietf.org/html/rfc7233#section-4.1"
    ),
    
    /**
     * 207 Multi-Status (WebDAV).
     * The message body that follows is an XML message and can contain a number of separate response codes, depending on how many sub-requests were made.
     */
    C207_SUCCESS_MULTI_STATUS(
        207, 
        "Multi-Status (WebDAV)", 
        "The message body that follows is an XML message and can contain a number of separate response codes, depending on how many sub-requests were made.",
        "https://tools.ietf.org/html/rfc4918#section-11.1"    
    ),
    
    /**
     * 208 Already Reported (WebDAV).
     * The members of a DAV binding have already been enumerated in a previous reply to this request, and are not being included again.
     */
    C208_SUCCESS_ALREADY_REPORTED(
        208, 
        "Already Reported (WebDAV)","The members of a DAV binding have already been enumerated in a previous reply to this request, and are not being included again.",
        "https://tools.ietf.org/html/rfc5842#section-7.1"
    ),
    
    /**
     * 226 IM Used.
     * The server has fulfilled a request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.
     */
    C226_SUCCESS_IM_USED(
        226, 
        "IM Used", 
        "The server has fulfilled a request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.",
        "https://tools.ietf.org/html/rfc3229#section-10.4.1"
    ),

    /*
     *  <h2>3xx Redirection</h2>
     * 
     *  This class of status code indicates the client must take additional action to complete the request. Many of these status codes are used in URL redirection.
     *  A user agent may carry out the additional action with no user interaction only if the method used in the second request is GET or HEAD. A user agent should 
     *  not automatically redirect a request more than five times, since such redirections usually indicate an infinite loop.
     */
    
    /**
     * 300 Multiple Choices.
     * Indicates multiple options for the resource that the client may follow. It, for instance, could be used to present different format options for video, list files with different extensions, or word sense disambiguation.
     */
    C300_REDIRECT_MULTIPLE_CHOICES(
        300, 
        "Multiple Choices", 
        "Indicates multiple options for the resource that the client may follow. It, for instance, could be used to present different format options for video, list files with different extensions, or word sense disambiguation.",
        "https://tools.ietf.org/html/rfc7231#section-6.4.1"
    ),
    
    /**
     * 301 Moved Permanently.
     * This and all future requests should be directed to the given URI.
     */
    C301_REDIRECT_MOVED_PERMANENTLY(
        301, 
        "Moved Permanently","This and all future requests should be directed to the given URI.",
        "https://tools.ietf.org/html/rfc7231#section-6.4.2"
    ),
    
    /**
     * 302 Found. 
     * 
     * This is an example of industry practice contradicting the standard. 
     * The HTTP/1.0 specification (RFC 1945) required the client to perform a temporary redirect (the original describing phrase was 
     * "Moved Temporarily"), 
     * but popular browsers implemented 302 with the functionality of a 303 See Other. 
     * Therefore, HTTP/1.1 added status codes 303 and 307 to distinguish between the two behaviours. 
     * However, some Web applications and frameworks use the 302 status code as if it were the 303.
     */
    C302_REDIRECT_FOUND(
        302, 
        "Found", 
        "This is an example of industry practice contradicting the standard. The HTTP/1.0 specification (RFC 1945) required the client to perform a temporary redirect (the original describing phrase was \"Moved Temporarily\"), but popular browsers implemented 302 with the functionality of a 303 See Other. Therefore, HTTP/1.1 added status codes 303 and 307 to distinguish between the two behaviours. However, some Web applications and frameworks use the 302 status code as if it were the 303.",
        "https://tools.ietf.org/html/rfc7231#section-6.4.2"
    ),
    
    /**
     * 303 See Other.
     * 
     * The response to the request can be found under another URI using a GET method. When received in response to a POST (or PUT/DELETE), it should be assumed that the server has received the data and the redirect should be issued with a separate GET message.
     */
    C303_REDIRECT_SEE_OTHER(
        303, 
        "See Other", 
        "The response to the request can be found under another URI using a GET method. When received in response to a POST (or PUT/DELETE), it should be assumed that the server has received the data and the redirect should be issued with a separate GET message.",
        "https://tools.ietf.org/html/rfc7231#section-6.4.4"
    ),
    
    /**
     * 304 Not Modified.
     * 
     * Indicates that the resource has not been modified since the version specified by the request headers If-Modified-Since or If-None-Match. This means that there is no need to retransmit the resource, since the client still has a previously-downloaded copy.
     */
    C304_NOT_MODIFIED(
        304, 
        "Not Modified", 
        "Indicates that the resource has not been modified since the version specified by the request headers If-Modified-Since or If-None-Match. This means that there is no need to retransmit the resource, since the client still has a previously-downloaded copy.",
        "https://tools.ietf.org/html/rfc7232#section-4.1"
    ),
    
    /**
     * 305 Use Proxy. 
     * 
     * The requested resource is only available through a proxy, whose address is provided in the response. Many HTTP clients (such as Mozilla and Internet Explorer) do not correctly handle responses with this status code, primarily for security reasons.
     */
    C305_REDIRECT_USE_PROXY(
        305, 
        "Use Proxy", 
        "The requested resource is only available through a proxy, whose address is provided in the response. Many HTTP clients (such as Mozilla and Internet Explorer) do not correctly handle responses with this status code, primarily for security reasons.",
        "https://tools.ietf.org/html/rfc7231#section-6.4.5"
    ),
    
    /**
     * 306 Switch Proxy.
     * @deprecated No longer used. Originally meant \"Subsequent requests should use the specified proxy.\"
     */
    @Deprecated
    C306_REDIRECT_UNUSED(
        306, 
        "Switch Proxy", 
        "No longer used. Originally meant \"Subsequent requests should use the specified proxy.\"",
        "https://tools.ietf.org/html/rfc7231#section-6.4.6"
    ),
    
    /**
     * 307 Temporary Redirect.
     * 
     * In this case, the request should be repeated with another URI; however, future requests should still use the original URI. 
     * In contrast to how 302 was historically implemented, the request method is not allowed to be changed when reissuing the original request. 
     * For instance, a POST request should be repeated using another POST request.
     */
    C307_REDIRECT_TEMPORARY(
        307, 
        "Temporary Redirect",
        "In this case, the request should be repeated with another URI; however, future requests should still use the original URI. In contrast to how 302 was historically implemented, the request method is not allowed to be changed when reissuing the original request. For instance, a POST request should be repeated using another POST request.",
        "https://tools.ietf.org/html/rfc7231#section-6.4.7"
    ),
    
    /**
     * 308 Permanent Redirect (experimental).
     * 
     * The request, and all future requests should be repeated using another URI. 307 and 308 (as proposed) parallel the behaviours of 302 and 301, 
     * but do not allow the HTTP method to change. So, for example, submitting a form to a permanently redirected resource may continue smoothly.
     */
    C308_REDIRECT_PERMANENT(
        308, 
        "Permanent Redirect (experiemental)",
        "The request, and all future requests should be repeated using another URI. 307 and 308 (as proposed) parallel the behaviours of 302 and 301, but do not allow the HTTP method to change. So, for example, submitting a form to a permanently redirected resource may continue smoothly.",
        "https://tools.ietf.org/html/rfc7538#section-3"
    ),

    /**
     * <h2>4xx Client Error</h2>
     * 
     * The 4xx class of status code is intended for cases in which the client seems to have erred. 
     * Except when responding to a HEAD request, the server should include an entity containing an explanation of the error situation, 
     * and whether it is a temporary or permanent condition. 
     * These status codes are applicable to any request method. User agents should display any included entity to the user.
     */
    
    /**
     * 400 Bad Request. 
     * 
     * The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).
     */
    C400_ERROR_BAD_REQUEST(
        400, 
        "Bad Request", 
        "The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).",
        "https://tools.ietf.org/html/rfc7231#section-6.5.1"
    ),
    
    /**
     * 401 Not Authorized. 
     * 
     * Similar to 403 Forbidden, but specifically for use when authentication is required and has failed or has not yet been provided. The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource. See Basic access authentication and Digest access authentication.
     */
    C401_ERROR_NOT_AUTHORIZED(
        401, 
        "Not Authorized",
        "Similar to 403 Forbidden, but specifically for use when authentication is required and has failed or has not yet been provided. The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource. See Basic access authentication and Digest access authentication.",
        "https://tools.ietf.org/html/rfc7235#section-3.1"
    ),
    
    /** 
     * 402 Payment Required. 
     * 
     * @deprecated Reserved for future use. */
    @Deprecated    
    C402_ERROR_PAYMENT_REQUIRED(
        402, 
        "Payment Required",
        "Reserved for future use. The original intention was that this code might be used as part of some form of digital cash or micropayment scheme, but that has not happened, and this code is not usually used. YouTube uses this status if a particular IP address has made excessive requests, and requires the person to enter a CAPTCHA.[citation needed] Some work has been done to implement payments via the digital currency Bitcoin automatically on a 402 request.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.2"
    ),
    
    /**
     * 403 Forbidden. 
     * 
     * The request was a valid request, but the server is refusing to respond to it. Unlike a 401 Unauthorized response, authenticating will make no difference.
     */
    C403_ERROR_FORBIDDEN(
        403, 
        "Forbidden",
        "The request was a valid request, but the server is refusing to respond to it. Unlike a 401 Unauthorized response, authenticating will make no difference.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.3"
    ),
    
    /**
     * 404 Not Found. 
     * 
     * The requested resource could not be found but may be available again in the future. Subsequent requests by the client are permissible.
     */
    C404_ERROR_NOT_FOUND(
        404, 
        "Not found",
        "The requested resource could not be found but may be available again in the future. Subsequent requests by the client are permissible.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.4"
    ),
    
    /**
     * 405 Method Not Allowed.
     * 
     * A request was made of a resource using a request method not supported by that resource; for example, using GET on a form which requires data to be 
     * presented via POST, or using PUT on a read-only resource.
     */
    C405_ERROR_METHOD_NOT_ALLOWED(
        405, 
        "Method Not Allowed",
        "A request was made of a resource using a request method not supported by that resource; for example, using GET on a form which requires data to be presented via POST, or using PUT on a read-only resource.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.5"
    ),
    
    /**
     * 406 Not Acceptable.
     * 
     * The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request.
     */
    C406_ERROR_NOT_ACCEPTABLE(
        406, 
        "Not Acceptable",
        "The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.6"
    ),
        
    /**
     * 407 Proxy Authentication Required.
     * 
     * The client must first authenticate itself with the proxy.
     */
    C407_PROXY_AUTHENTICATION_REQUIRED(
        407, 
        "Proxy Authentication Required",
        "The client must first authenticate itself with the proxy.",
        "https://tools.ietf.org/html/rfc7235#section-3.2"
    ),
    
    /**
     * 408 Request Timeout.
     * 
     * The server timed out waiting for the request. According to HTTP specifications: "The client did not produce a request within the time that the server was prepared to wait. The client MAY repeat the request without modifications at any later time."
     */
    C408_TIMED_OUT_REQUEST(
        408, 
        "Request Timeout",
        "The server timed out waiting for the request. According to HTTP specifications: \"The client did not produce a request within the time that the server was prepared to wait. The client MAY repeat the request without modifications at any later time.\"",
        "https://tools.ietf.org/html/rfc7231#section-6.5.7"
    ),
    
    /**
     * 409 Conflict.
     * 
     * Indicates that the request could not be processed because of conflict in the request, such as an edit conflict in the case of multiple updates.
     */
    C409_ERROR_CONFLICT(
        409, 
        "Conflict",
        "Indicates that the request could not be processed because of conflict in the request, such as an edit conflict in the case of multiple updates.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.8"
    ),
    
    /**
     * 410 Gone.
     * 
     * Indicates that the resource requested is no longer available and will not be available again. This should be used when a resource has been intentionally removed and the resource should be purged. Upon receiving a 410 status code, the client should not request the resource again in the future. Clients such as search engines should remove the resource from their indices.[15] Most use cases do not require clients and search engines to purge the resource, and a "404 Not Found" may be used instead.
     */
    C410_GONE(
        410, 
        "Gone",
        "Indicates that the resource requested is no longer available and will not be available again. This should be used when a resource has been intentionally removed and the resource should be purged. Upon receiving a 410 status code, the client should not request the resource again in the future. Clients such as search engines should remove the resource from their indices.[15] Most use cases do not require clients and search engines to purge the resource, and a \"404 Not Found\" may be used instead.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.9"
    ),
    
    /**
     * 411 Length Required.
     * 
     * The request did not specify the length of its content, which is required by the requested resource.
     */
    C411_LENGTH_REQUIRED(
        411, 
        "Length Required",
        "The request did not specify the length of its content, which is required by the requested resource.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.10"
    ),
    
    /**
     * 412 Precondition Failed (RFC 7232).
     * The server does not meet one of the preconditions that the requester put on the request.
     */
    C412_PRECONDITION_FAILED(
        412, 
        "Precondition Failed (RFC 7232)",
        "The server does not meet one of the preconditions that the requester put on the request.",
        "https://tools.ietf.org/html/rfc7232#section-4.2"
    ),
    
    /**
     * 413 Request Entity Too Large.
     * The request is larger than the server is willing or able to process.
     */
    C413_REQUEST_ENTITY_TOO_LARGE(
        413, 
        "Request Entity Too Large",
        "The request is larger than the server is willing or able to process.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.11"
    ),
    
    /**
     * 414 Request-URI Too Long.
     * The URI provided was too long for the server to process. Often the result of too much data being encoded as a query-string of a GET request, in which case it should be converted to a POST request.
     */
    C414_REQUEST_URI_TOO_LARGE(
        414, 
        "Request URI Too Large",
        "The URI provided was too long for the server to process. Often the result of too much data being encoded as a query-string of a GET request, in which case it should be converted to a POST request.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.12"
    ),
    
    /**
     * 415 Unsupported Media Type.
     * The request entity has a media type which the server or resource does not support. For example, the client uploads an image as image/svg+xml, but the server requires that images use a different format.
     */
    C415_UNSUPPORTED_MEDIA_TYPE(
        415, 
        "Unsupported Media Type",
        "The request entity has a media type which the server or resource does not support. For example, the client uploads an image as image/svg+xml, but the server requires that images use a different format.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.13"
    ),
                
    /**
     * 416 Requested Range Not Satisfiable.
     * The client has asked for a portion of the file (byte serving), but the server cannot supply that portion. For example, if the client asked for a part of the file that lies beyond the end of the file.
     */
    C416_REQUESTED_RANGE_NOT_SATISFIABLE(
        416, 
        "Requested Range Not Satisfiable",
        "The client has asked for a portion of the file (byte serving), but the server cannot supply that portion. For example, if the client asked for a part of the file that lies beyond the end of the file.",
        "https://tools.ietf.org/html/rfc7233#section-4.4"
    ),
    
    /**
     * 417 Expectation Failed.
     * The server cannot meet the requirements of the Expect request-header field.
     */
    C417_EXPECTATION_FAILED(
        417, 
        "Expectation Failed",
        "The server cannot meet the requirements of the Expect request-header field.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.14"
    ),
                
    /**
     * 418 I'm a teapot (RFC 2324).
     * This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee Pot Control Protocol, and is not expected to be implemented by actual HTTP servers. The RFC specifies this code should be returned by tea pots requested to brew coffee.
     */
    C418_I_AM_A_TEAPOT(
        418, 
        "I'm a teapot",
        "This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee Pot Control Protocol, and is not expected to be implemented by actual HTTP servers. The RFC specifies this code should be returned by tea pots requested to brew coffee.",
        null
    ),
    
    /**
     * 419 Authentication Timeout.
     * Not a part of the HTTP standard, 419 Authentication Timeout denotes that previously valid authentication has expired. It is used as an alternative to 401 Unauthorized in order to differentiate from otherwise authenticated clients being denied access to specific server resources.
     */
    C419_AUTHENTICATION_TIMEOUT(
        419, 
        "Authentication Timeout",
        "Not a part of the HTTP standard, 419 Authentication Timeout denotes that previously valid authentication has expired. It is used as an alternative to 401 Unauthorized in order to differentiate from otherwise authenticated clients being denied access to specific server resources.",
        null
    ),
    
    /**
     * 420 Enhance Your Calm.
     * Returned by rest services when your request is too large or frequent.
     */
    C420_ENHANCE_YOUR_CALM(
        420, 
        "Enhance Your Calm",
        "Returned by rest services when your request is too large or frequent",
        "https://dev.twitter.com/overview/api/response-codes"
    ),
    
    /**
     * 421 Misdirected Request (RFC 7540)
     * The request was directed at a server that is not able to produce a response (for example because a connection reuse).
     */
    C421_MISDIRECTED_REQUEST(
        421, 
        "Misdirected Request (RFC 7540)",
        "The request was directed at a server that is not able to produce a response (for example because a connection reuse).",
        "https://tools.ietf.org/html/rfc7540#section-9.1.2"
    ),
    
    /**
     * 422 Un-processable Entity (WebDAV; RFC 4918)
     * The request was well-formed but was unable to be followed due to semantic errors.
     */
    C422_UN_PROCESSABLE_ENTITY(
        422, 
        "Un-processable Entity (WebDAV; RFC 4918)",
        "The request was well-formed but was unable to be followed due to semantic errors.",
        "https://tools.ietf.org/html/rfc4918#section-11.2"
    ),
    
    /**
     * 423 Locked (WebDAV; RFC 4918)
     * The resource that is being accessed is locked.
     */
    C423_LOCKED(
        423, 
        "Locked (WebDAV; RFC 4918)",
        "The resource that is being accessed is locked.",
        "https://tools.ietf.org/html/rfc4918#section-11.3"
    ),
        
    /**
     * 424 Failed Dependency (WebDAV; RFC 4918)
     * The request failed due to failure of a previous request (e.g., a PROPPATCH).
     */
    C424_FAILED_DEPENDENCY(
        424, 
        "Failed Dependency (WebDAV; RFC 4918)",
        "The request failed due to failure of a previous request (e.g., a PROPPATCH).",
        "https://tools.ietf.org/html/rfc4918#section-11.4"
    ),
    
    /**
     * 426 Upgrade Required
     * The client should switch to a different protocol such as TLS/1.0, given in the Upgrade header field.
     */
    C426_UPGRADE_REQUIRED(
        426, 
        "Upgrade Required",
        "The client should switch to a different protocol such as TLS/1.0, given in the Upgrade header field.",
        "https://tools.ietf.org/html/rfc7231#section-6.5.15"
    ),
        
    /**
     * 428 Precondition Required (RFC 6585)
     * The origin server requires the request to be conditional. Intended to prevent \"the 'lost update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict.
     */
    C428_PRECONDITION_REQUIRED(
        428, 
        "Precondition Required (RFC 6585)",
        "The origin server requires the request to be conditional. Intended to prevent \"the 'lost update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict.",
        "https://tools.ietf.org/html/rfc6585#section-3"
    ),
    
    /**
     * 429 Too Many Requests (RFC 6585)
     * The user has sent too many requests in a given amount of time. Intended for use with rate limiting schemes.
     */
    C429_TOO_MANY_REQUESTS(
        429, 
        "Too Many Requests (RFC 6585)",
        "The user has sent too many requests in a given amount of time. Intended for use with rate limiting schemes.",
        "https://tools.ietf.org/html/rfc6585#section-4"
    ),
    
    /**
     * 430 Request Header Fields Too Large
     * This status code indicates that the server is unwilling to process the request because its header fields are too large. 
     * The request MAY be resubmitted after reducing the size of the request header fields.
     * 
     * It can be used both when the set of request header fields in total are too large, and when a single header field is at fault. 
     * In the latter case, the response representation SHOULD specify which header field was too large.
     * 
     * https://tools.ietf.org/id/draft-nottingham-http-new-status-00.html
     */
    C430_HEADER_FIELDS_TOO_LARGE(
        430, 
        "Server is unwilling to process the request because its header fields are too large.",

        "This status code indicates that the server is unwilling to process the request because its header fields are too large. " +
        "The request MAY be resubmitted after reducing the size of the request header fields.\n" +
        "\n" +
        "It can be used both when the set of request header fields in total are too large, and when a single header field is at fault. " +
        "In the latter case, the response representation SHOULD specify which header field was too large.",
        null
    ),
    
    /**
     * 431 Request Header Fields Too Large (RFC 6585).
     * The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large.
     */
    C431_REQUEST_HEADER_FIELDS_TOO_LARGE(
        431, 
        "Request Header Fields Too Large (RFC 6585)",
        "The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large.",
        "https://tools.ietf.org/html/rfc6585#section-5"
    ),
    
    /**
     * 451 Unavailable For Legal Reasons (Internet draft).
     * Defined in the Internet draft "A New HTTP Status Code for Legally-restricted Resources".[60] Intended to be used when resource access is denied for legal reasons, e.g. censorship or government-mandated blocked access. 
     * A reference to the 1953 dystopian novel Fahrenheit 451, where books are outlawed,[61] and the autoignition temperature of paper, 451°F.
     */
    C451_UNAVALILABLE_FOR_LEGAL_REASONS(
        451, 
        "Unavailable For Legal Reasons (Internet draft).",
        "Defined in the internet draft \"A New HTTP Status Code for Legally-restricted Resources\".[60] Intended to be used when resource access is denied for legal reasons, e.g. censorship or government-mandated blocked access. A reference to the 1953 dystopian novel Fahrenheit 451, where books are outlawed,[61] and the autoignition temperature of paper, 451°F.",
        "https://tools.ietf.org/html/rfc7725#section-3"
    ),
    
    /**
     * 498 Token expired/invalid (Esri).
     * Returned by ArcGIS for Server. A code of 498 indicates an expired or otherwise invalid token.
     */
    C498_TOKEN_EXPIRED_OR_INVALID(
        498, 
        "Token expired/invalid (Esri)",
        "Returned by ArcGIS for Server. A code of 498 indicates an expired or otherwise invalid token.",
        null
    ),
    
    /**
     * 500 Internal server error.
     * 
     * A generic error message, given when an unexpected condition was encountered and no more specific message is suitable.
     */
    C500_SERVER_INTERNAL_ERROR(
        500, 
        "Internal server error",
        "A generic error message, given when an unexpected condition was encountered and no more specific message is suitable.",
        "https://tools.ietf.org/html/rfc7231#section-6.6.1"
    ),
    
    /**
     * 501 Not Implemented.
     * 
     * The server either does not recognize the request method, or it lacks the ability to fulfil the request. Usually this implies future availability (e.g., a new feature of a web-service API).
     */
    C501_SERVER_NOT_IMPLEMENTED(
        501, 
        "Not Implemented",
        "The server either does not recognize the request method, or it lacks the ability to fulfil the request. Usually this implies future availability (e.g., a new feature of a web-service API).",
        "https://tools.ietf.org/html/rfc7231#section-6.6.2"
    ),
    
    /**
     * 502 Bad Gateway.
     * 
     * The server was acting as a gateway or proxy and received an invalid response from the upstream server.
     */
    C502_BAD_GATEWAY(
        502, 
        "Bad Gateway",
        "The server was acting as a gateway or proxy and received an invalid response from the upstream server.",
        "https://tools.ietf.org/html/rfc7231#section-6.6.3"
    ),
    
    /**
     * 503 Service Unavailable.
     * 
     * The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.
     */
    C503_SERVICE_UNAVAILABLE(
        503, 
        "Service Unavailable",
        "The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.",
        "https://tools.ietf.org/html/rfc7231#section-6.6.4"
    ),
    
    /**
     * 504 Gateway Timeout.
     * 
     * The server was acting as a gateway or proxy and did not receive a timely response from the upstream server.
     */
    C504_TIMED_OUT_GATEWAY(
        504, 
        "Gateway Timeout",
        "The server was acting as a gateway or proxy and did not receive a timely response from the upstream server.",
        "https://tools.ietf.org/html/rfc7231#section-6.6.5"
    ),
            
    /**
     * 505 HTTP Version Not Supported.
     * 
     * The server does not support the HTTP protocol version used in the request.
     */
    C505_HTTP_VERSION_NOT_SUPPORTED(
        505, 
        "HTTP Version Not Supported",
        "The server does not support the HTTP protocol version used in the request.",
        "https://tools.ietf.org/html/rfc7231#section-6.6.6"
    ),
        
    /**
     * 506 Variant Also Negotiates (RFC 2295).
     * 
     * Transparent content negotiation for the request results in a circular reference.
     */
    C506_VARIANT_ALSO_NEGOTIATES(
        506, 
        "Variant Also Negotiates (RFC 2295)",
        "Transparent content negotiation for the request results in a circular reference.",
        "https://tools.ietf.org/html/rfc2295#section-8.1"
    ),
           
    /**
     * 507 Insufficient Storage (WebDAV; RFC 4918).
     * 
     * The server is unable to store the representation needed to complete the request.
     */
    C507_INSUFFICIENT_STORAGE(
        507, 
        "Insufficient Storage (WebDAV; RFC 4918)",
        "The server is unable to store the representation needed to complete the request.",
        "https://tools.ietf.org/html/rfc4918#section-11.5"
    ),
     
    /**
     * 508 Loop Detected (WebDAV; RFC 5842).
     * 
     * The server detected an infinite loop while processing the request (sent in lieu of 208 Already Reported).
     */
    C508_LOOP_DETECTED(
        508, 
        "Loop Detected (WebDAV; RFC 5842)",
        "The server detected an infinite loop while processing the request (sent in lieu of 208 Already Reported).",
        "https://tools.ietf.org/html/rfc5842#section-7.2"
    ),
     
    /**
     * 509 Bandwidth Limit Exceeded (Apache Web Server/cPanel).
     * 
     * The server has exceeded the bandwidth specified by the server administrator; this is often used by shared hosting providers to limit the bandwidth of customers.
     */
    C509_BANDWIDTH_LIMIT_EXCEEDED(
        509, 
        "Bandwidth Limit Exceeded (Apache Web Server/cPanel)",
        "The server has exceeded the bandwidth specified by the server administrator; this is often used by shared hosting providers to limit the bandwidth of customers.",
        null
    ),
    
    /**
     * 510 Not Extended (RFC 2774).
     * 
     * Further extensions to the request are required for the server to fulfil it.
     */
    C510_NOT_EXTENDED(
        510, 
        "Not Extended (RFC 2774)",
        "Further extensions to the request are required for the server to fulfil it.",
        "https://tools.ietf.org/html/rfc2774#section-7"
    ),
        
    /**
     * 511 Network Authentication Required (RFC 6585).
     * 
     * The client needs to authenticate to gain network access. Intended for use by intercepting proxies used to control access to the network (e.g., "captive portals" used to require agreement to Terms of Service before granting full Internet access via a Wi-Fi hotspot).
     */
    C511_NETWORK_AUTHENTICATION_REQUIRED(
        511, 
        "Network Authentication Required (RFC 6585)",
        "The client needs to authenticate to gain network access. Intended for use by intercepting proxies used to control access to the network (e.g., \"captive portals\" used to require agreement to Terms of Service before granting full Internet access via a Wi-Fi hotspot).",
        null
    ),
    
    /**
     * 520 Unknown Error.
     * 
     * This status code is not specified in any RFC and is returned by many services. "The 520 error is essentially a “catch-all” response for when the origin server returns something unexpected or something that is not tolerated/interpreted (protocol violation or empty response).
     */
    C520_UNKNOWN_ERROR(
        520, 
        "Unknown Error",
        "This status code is not specified in any RFC and is returned by many services. \"The 520 error is essentially a “catch-all” response for when the origin server returns something unexpected or something that is not tolerated/interpreted (protocol violation or empty response).",
        "https://support.cloudflare.com/hc/en-us/articles/200171936-Error-520-Web-server-is-returning-an-unknown-error"
    ),
    
    /**
     * 521 Web Server Is Down.
     * 
     * The origin server has refused the connection.
     */
    C521_WEB_SERVER_IS_DOWN(
        521, 
        "Web Server Is Down",
        "The origin server has refused the connection.",
        "https://support.cloudflare.com/hc/en-us/articles/200171916-Error-521-Web-server-is-down"
    ),
    
    /**
     * 522 Connection Timed Out.
     * 
     * Could not negotiate a TCP handshake with the origin server.
     */
    C522_TIMED_OUT_CONNECTION(
        522, 
        "Connection Timed Out",
        "Could not negotiate a TCP handshake with the origin server.",
        "https://support.cloudflare.com/hc/en-us/articles/200171906-Error-522-Connection-timed-out"
    ),

    /**
     * 523 Origin Is Unreachable.
     * 
     * Could not reach the origin server; for example, if the DNS records for the origin server are incorrect.
     */
    C523_ORIGIN_IS_UNREACHABLE(
        523, 
        "Origin Is Unreachable",
        "Could not reach the origin server; for example, if the DNS records for the origin server are incorrect.",
        "https://support.cloudflare.com/hc/en-us/articles/200171946-Error-523-Origin-is-unreachable"
    ),

    /**
     * 524 A Timeout Occurred.
     * 
     * Was able to complete a TCP connection to the origin server, but did not receive a timely HTTP response.
     */
    C524_TIMED_OUT_RESPONSE(
        524, 
        "A Timeout Occurred",
        "Was able to complete a TCP connection to the origin server, but did not receive a timely HTTP response.",
        "https://support.cloudflare.com/hc/en-us/articles/200171926-Error-524-A-timeout-occurred"
    ),
    
    /**
     * 525 SSL Handshake Failed.
     * 
     * Could not negotiate a SSL/TLS handshake with the origin server.
     */
    C525_SSL_HANDSHAKE_FAILED(
        525, 
        "SSL Handshake Failed",
        "Could not negotiate a SSL/TLS handshake with the origin server.",
        "https://support.cloudflare.com/hc/en-us/articles/200278659-Error-525-SSL-handshake-failed"
    ),
    
    /**
     * 526 Invalid SSL Certificate
     * 
     * Could not validate the SSL/TLS certificate that the origin server presented.
     */
    C526_SSL_INVALID_CERTIFICATE(
        526, 
        "Invalid SSL Certificate",
        "Could not validate the SSL/TLS certificate that the origin server presented.",
        "https://support.cloudflare.com/hc/en-us/articles/200721975-Error-526-Invalid-SSL-certificate"
    ),
        
    /**
     * 530 Site is frozen.
     * 
     * Used by the Pantheon web platform to indicate a site that has been frozen due to inactivity.
     */
    C530_SITE_IS_FROZEN(
        530, 
        "Site is frozen",
        "Used by the Pantheon web platform to indicate a site that has been frozen due to inactivity.",
        null
    ),
    
    /**
     * 598 Network read timeout error.
     * 
     * This status code is not specified in any RFCs, but is used by Microsoft HTTP proxies to signal a network read timeout behind the proxy to a client in front of the proxy.
     */
    C598_TIMED_OUT_SERVER_NETWORK_READ(
        598, 
        "Network read timeout error",
        "This status code is not specified in any RFCs, but is used by Microsoft HTTP proxies to signal a network read timeout behind the proxy to a client in front of the proxy.",
        null
    ),
    
    /**
     * 599 Network connect timeout error.
     * 
     * This status code is not specified in any RFCs, but is used by Microsoft HTTP proxies to signal a network connect timeout behind the proxy to a client in front of the proxy.
     */
    C599_TIMED_OUT_SERVER_NETWORK_CONNECT(
        599, 
        "Network connect timeout error",
        "This status code is not specified in any RFCs, but is used by Microsoft HTTP proxies to signal a network connect timeout behind the proxy to a client in front of the proxy.",
        null
    );

    public final int code;
    
    public final @Nonnull String label;
    public final @Nonnull String description;
    
    /*
     * https://tools.ietf.org/html/rfc7231
     */
    public final @Nullable String reference;

    /**
     * Find by status
     * @param status the integer status.
     * @return the ReSTStatus
     */
    @CheckReturnValue @Nonnull
    public static Status find( final int status)
    {
        for( Status rs: values())
        {
            if( rs.code == status)
            {
                return rs;
            }
        }

        LOGGER.warn( "No HTTP status " + status + " using an alternative");
        if( status >=200)
        {
            if( status < 300)
            {
                return C200_SUCCESS_OK;
            }
            else if( status < 400)
            {
                return C303_REDIRECT_SEE_OTHER;
            }
            else if( status < 500)
            { 
                return C400_ERROR_BAD_REQUEST;
            }
            else
            {
                return C500_SERVER_INTERNAL_ERROR;
            }
        }
        else
        {
            return C501_SERVER_NOT_IMPLEMENTED;
        }        
    }

    /**
     * Is this status an error ?
     * @return TRUE if an error.
     */
    @CheckReturnValue
    public boolean isError()
    {
        if(code >= 200 && code < 400)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * http://www.restapitutorial.com/httpstatuscodes.html
     *
     *
     * @return the status label.
     *
     * @throws FileNotFoundException 404
     * @throws com.aspc.remote.rest.errors.ReSTException unknown status
     */
    @Nonnull
    public String check( ) throws FileNotFoundException, ReSTException
    {
        if( isError() == false)
        {
            return label;
        }

        switch( this)
        {
            case C400_ERROR_BAD_REQUEST:
                throw new BadRequestException( code + " " + label, null);
            case C401_ERROR_NOT_AUTHORIZED:
                throw new NotAuthorizedException( code + " " + label, null);
            case C402_ERROR_PAYMENT_REQUIRED:
                throw new PaymentRequiredException( code + " " + label, null);
            case C403_ERROR_FORBIDDEN:
                throw new ForbiddenException( code + " " + label, null);
            case C404_ERROR_NOT_FOUND:
                throw new FileNotFoundException( code + " " + label);
            case C405_ERROR_METHOD_NOT_ALLOWED:
                throw new MethodNotAllowedException( code + " " + label, null);
            case C406_ERROR_NOT_ACCEPTABLE:
                throw new NotAcceptableException( code + " " + label, null);
            case C409_ERROR_CONFLICT:
                throw new ConflictException( code + " " + label, null);
            case C408_TIMED_OUT_REQUEST:
            case C504_TIMED_OUT_GATEWAY:
            case C522_TIMED_OUT_CONNECTION:
            case C524_TIMED_OUT_RESPONSE:
            case C598_TIMED_OUT_SERVER_NETWORK_READ:
            case C599_TIMED_OUT_SERVER_NETWORK_CONNECT:
                throw new RequestTimeoutException(this, code + " " + label, null);
            default:
                throw new ReSTException( this, code + " " + label, null);
        }
    }

    /**
     * http://www.restapitutorial.com/httpstatuscodes.html
     *
     *
     * @param status the status to check.
     * @return the status name.
     *
     * @throws FileNotFoundException 404
     * @throws com.aspc.remote.rest.errors.ReSTException unknown status
     */
    @Nonnull
    public static String check( final int status) throws FileNotFoundException, ReSTException
    {
        return find( status).check();
    }

    @Override @CheckReturnValue @Nonnull
    public String toString() {
        return code + " " + label;
    }
    
    private Status( final int code, final String label, final String description,final String reference)
    {
        this.code=code;
        this.label=label;
        this.description=description;
        assert reference==null || reference.matches("http(|s)://.*"):"Invalid URL: " + reference;
        this.reference=reference;
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.Status");//#LOGGER-NOPMD
}
