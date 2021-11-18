## Default logging

Inside the global configuration you can find the logging configuration.
These works exactly like the standard Log4j log properties, define the
package or class and the logging level.

The logPath variables sets the log file location, null means inside the 
main jar directory

The logRoundtripsPath sets the directory where to store the full static 
and dynamic requests. If null does not store anything

The 

    [{  "id": "global",
        ...
        "logging": {
            "logPath": null,
            "logRoundtripsPath": null,
            "loggers": [
                {"org.kendar.servers.http.Request":"DEBUG"},

## Special loggers

* org.kendar.servers.http.Request: To log the requests, just when they come to the server
    * NONE: logs nothing (default)
    * INFO: Show the request path
    * DEBUG: Show the request data with the first 100 chars of the request content
    * TRACE: Show the full request content
* org.kendar.servers.http.Response: To log the response, before sending it back
    * NONE: logs nothing (default)
    * DEBUG: Show the request data with the first 100 chars of the request content
    * TRACE: Show the full request content
* org.kendar.servers.http.StaticRequest
    * NONE: logs nothing (default)
    * DEBUG: Logs on file the static requests
* org.kendar.servers.http.DynamicRequest
    * NONE: logs nothing (default)
    * DEBUG: Logs on file the dynamic requests