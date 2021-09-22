package org.kendar.servers.dns.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class DnsServersApis implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getId() {
        return "org.kendar.servers.dns.api.DnsApis";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers",
            method = "GET")
    public boolean getDnsServers(Request req, Response res) throws JsonProcessingException {
        //var proxyes = simpleProxyHandler.getProxies();
        res.addHeader("Content-type", "application/json");
        //res.setResponse(mapper.writeValueAsString(proxyes));
        return false;
    }
}
