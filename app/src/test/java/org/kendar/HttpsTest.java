package org.kendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.kendar.mocks.EnvironmentImpl;
import org.kendar.servers.AnsweringHttpServer;
import org.kendar.servers.AnsweringHttpsServer;
import org.kendar.servers.certificates.CertificatesManagerImpl;
import org.kendar.servers.dns.CustomHttpConectionBuilderImpl;
import org.kendar.servers.http.AnsweringHandlerImpl;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.FileResourcesUtilsImpl;
import org.kendar.utils.LoggerBuilderImpl;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HttpsTest {
    /*@Test
    public void testHttps() throws InterruptedException, IOException {

        var environment = new EnvironmentImpl();
        var loggerBuilder = new LoggerBuilderImpl();
        var fileRes = new FileResourcesUtilsImpl();
        var certsManager = new CertificatesManagerImpl(fileRes,loggerBuilder);
        var connectionBuilde= new CustomHttpConectionBuilderImpl(loggerBuilder);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,connectionBuilde);
        answerer.setHttpsForwardPort(443);
        answerer.setHttpsForwardProtocol("https");
        var server = new AnsweringHttpServer(loggerBuilder,answerer,certsManager,environment);
        server.setPort(20443);

        Thread thread = new Thread(server);
        thread.start();
        Thread.sleep(100);

        HttpGet httpRequest = new HttpGet("http://www.microsoft.com:20443");

        //httpRequest.addHeader(AnsweringHandlerImpl.TEST_OVERWRITE_HOST,"https://www.microsoft.org");

        var builder = new CustomHttpConectionBuilderImpl(new LoggerBuilderImpl());
        ConcurrentHashMap<String, List<String>> domains = new ConcurrentHashMap<>();
        List<String> address = new ArrayList<>();
        address.add("127.0.0.1");
        domains.put("www.microsoft.com",address);
        builder.setDomains(domains);
        CloseableHttpClient httpClient = builder.getConnection().build();
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        server.stop();
        //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");

        Assert.assertTrue(body.contains("my.host.com:20080"));
    }*/
}