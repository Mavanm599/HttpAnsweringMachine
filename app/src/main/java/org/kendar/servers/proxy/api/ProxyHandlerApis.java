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
    private final SimpleProxyHandler simpleProxyHandler;
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
            method = "GET",id="1015a4b4-277d-11ec-9621-0242ac130002")
    public boolean getProxies(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(proxyes));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "GET",id="1016a4b4-277d-11ec-9621-0242ac130002")
    public boolean getProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = req.getPathParameter("id");
        for(var item:proxyes){
            if(item.getId().equalsIgnoreCase(id)){
                res.addHeader("Content-type", "application/json");
                res.setResponseText(mapper.writeValueAsString(item));
                return false;
            }
        }
        res.setStatusCode(404);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "DELETE",id="1017a4b4-277d-11ec-9621-0242ac130002")
    public boolean removeProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = req.getPathParameter("id");
        var newList = new ArrayList<RemoteServerStatus>();
        for(var item:proxyes){
            if(item.getId().equalsIgnoreCase(id)){continue;}
            newList.add(item);
        }
        simpleProxyHandler.setProxies(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "PUT",id="1018a4b4-277d-11ec-9621-0242ac130002")
    public boolean updateProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id = req.getPathParameter("id");
        var newList = new ArrayList<RemoteServerStatus>();
        var newData = mapper.readValue((String)req.getRequestText(),RemoteServerStatus.class);

        for(var item:proxyes){
            var clone = item.copy();
            if(!clone.getId().equalsIgnoreCase(id)){
                newList.add(clone);
                continue;
            }
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
            pathAddress = "/api/proxyes/swap/{id1}/{id2}",
            method = "PUT",id="1019a4b4-277d-11ec-9621-0242ac130002")
    public boolean swapProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var id1 = req.getPathParameter("id1");
        var id2 = req.getPathParameter("id2");
        var newList = new ArrayList<RemoteServerStatus>();
        var id1Index=-1;
        var id2Index = -1;
        for (int i = 0; i < proxyes.size(); i++) {
            var clone = proxyes.get(i).copy();
            if(proxyes.get(i).getId().equalsIgnoreCase(id1))id1Index=i;
            if(proxyes.get(i).getId().equalsIgnoreCase(id2))id2Index=i;
            newList.add(clone);
        }
        var id1Clone = proxyes.get(id1Index).copy();
        proxyes.set(id1Index,proxyes.get(id2Index));
        proxyes.set(id2Index,id1Clone);
        simpleProxyHandler.setProxies(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/proxyes",
            method = "POST",id="1020a4b4-277d-11ec-9621-0242ac130002")
    public boolean addProxy(Request req, Response res) throws JsonProcessingException {
        var proxyes = simpleProxyHandler.getProxies();
        var newList = new ArrayList<>(proxyes);
        var newData = mapper.readValue((String)req.getRequestText(),RemoteServerStatus.class);
        newList.add(newData);
        simpleProxyHandler.setProxies(newList);
        res.setStatusCode(200);
        return false;
    }
}
