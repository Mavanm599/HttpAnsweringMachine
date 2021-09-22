package org.kendar.servers.proxy.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.RemoteServerStatus;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.servers.proxy.SimpleProxyHandlerImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class ProxyHandlerApis  implements FilteringClass {
    private SimpleProxyHandler simpleProxyHandler;
    ObjectMapper mapper = new ObjectMapper();

    public ProxyHandlerApis(SimpleProxyHandler simpleProxyHandler){
        this.simpleProxyHandler = simpleProxyHandler;
    }
    @Override
    public String getId() {
        return "org.kendar.servers.proxy.api.ProxyHandlerApis";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes",
            method = "GET")
    public boolean getProxies(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        res.addHeader("Content-type", "application/json");
        res.setResponse(mapper.writeValueAsString(proxyes));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "GET")
    public boolean getProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = Integer.parseInt(req.getPathParameter("id"));
        res.addHeader("Content-type", "application/json");
        for(var item:proxyes){
            if(item.getId()==id){

                res.setResponse(mapper.writeValueAsString(item));
                return false;
            }
        }
        res.setStatusCode(404);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "PUT")
    public boolean removeProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = Integer.parseInt(req.getPathParameter("id"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<RemoteServerStatus>();
        for(var item:proxyes){
            if(item.getId()==id){continue;}
            newList.add(item);
        }
        simpleProxyHandler.setProxies(proxyes);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "PUT")
    public boolean updateProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = Integer.parseInt(req.getPathParameter("id"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<RemoteServerStatus>();
        var newData = mapper.readValue((String)req.getRequest(),RemoteServerStatus.class);
        res.addHeader("Content-type", "application/json");
        for(var item:proxyes){
            var clone = item.clone();
            if(clone.getId()!=id){newList.add(clone);}
            clone.setTest(newData.getTest());
            clone.setWhen(newData.getWhen());
            clone.setWhere(newData.getWhere());
            newList.add(clone);
        }
        simpleProxyHandler.setProxies(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes",
            method = "POST")
    public boolean addProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = Integer.parseInt(req.getPathParameter("id"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<RemoteServerStatus>();
        var newData = mapper.readValue((String)req.getRequest(),RemoteServerStatus.class);
        res.addHeader("Content-type", "application/json");
        var maxValue = -1;
        for(var item:proxyes){
            newList.add( item.clone());
            maxValue= Math.max(item.getId(),maxValue);
        }
        maxValue++;
        newData.setId(maxValue);
        newList.add(newData);
        simpleProxyHandler.setProxies(newList);
        res.setStatusCode(200);
        return false;
    }
}
