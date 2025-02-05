package org.kendar.utils;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContextBuilder;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.ResolvedDomain;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionBuilderImpl implements ConnectionBuilder{
    private static final HttpRequestRetryHandler requestRetryHandler =
            (exception, executionCount, context) -> executionCount != 3;
    private final Logger logger;
    private DnsMultiResolver multiResolver;
    protected final ConcurrentHashMap<String, ResolvedDomain> domains = new ConcurrentHashMap<>();
    private SystemDefaultDnsResolver remoteDnsResolver;
    private SystemDefaultDnsResolver fullDnsResolver;
    private Registry<ConnectionSocketFactory> defaultRegistry;
    private Registry<ConnectionSocketFactory> sslUncheckedRegistry;
    private ConcurrentHashMap<String,HttpClientConnectionManager> connectionManagers = new ConcurrentHashMap<>();

    public ConnectionBuilderImpl(DnsMultiResolver multiResolver,
                                 LoggerBuilder loggerBuilder){
        this.multiResolver = multiResolver;
        this.logger = loggerBuilder.build(ConnectionBuilder.class);
    }

    private SystemDefaultDnsResolver buildFullResolver() {
        return  new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                var result = multiResolver.resolve(host);
                if(result.size()>1) {
                    return new InetAddress[]{InetAddress.getByName(result.get(0))};
                }
                return new InetAddress[]{};
            }
        };
    }

    private SystemDefaultDnsResolver buildRemoteResolver() {
        return  new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                ResolvedDomain descriptor;
                var currentTime = Calendar.getInstance().getTimeInMillis();
                List<String> hosts;
                if (domains.containsKey(host)) {
                    descriptor = domains.get(host);
                    if ((descriptor.timestamp + 10 * 60 * 1000) < currentTime) {
                        domains.remove(host);
                        hosts = multiResolver.resolveRemote(host);
                        descriptor = new ResolvedDomain();
                        descriptor.domains.addAll(hosts);
                        domains.put(host, descriptor);
                    }
                } else {
                    hosts = multiResolver.resolveRemote(host);
                    descriptor = new ResolvedDomain();
                    descriptor.domains.addAll(hosts);
                    domains.put(host, descriptor);
                }
                hosts = new ArrayList<>(descriptor.domains);
                var address = new InetAddress[hosts.size()];
                for (int i = 0; i < hosts.size(); i++) {
                    address[i] = InetAddress.getByName(hosts.get(i));
                }
                return address;
            }
        };
    }

    private Registry<ConnectionSocketFactory> buildSslUncheckedRegistry() {
        SSLContextBuilder contextBuilder = new SSLContextBuilder();
        try {
            contextBuilder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(contextBuilder.build(),
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            //SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory()).build()

            RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();
            return builder
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf).build();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return null;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Registry<ConnectionSocketFactory> buildDefaultRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
    }

    @PostConstruct
    public void init(){
        this.remoteDnsResolver= buildRemoteResolver();
        this.fullDnsResolver = buildFullResolver();
        this.defaultRegistry = buildDefaultRegistry();
        this.sslUncheckedRegistry = buildSslUncheckedRegistry();
    }

    public HttpClientConnectionManager getConnectionManger(boolean remoteDns){
        return getConnectionManger(remoteDns,true);
    }

    public HttpClientConnectionManager getConnectionManger(boolean remoteDns, boolean checkSsl){
        var key = remoteDns+":"+checkSsl;
        //return new BasicHttpClientConnectionManager();
//        return connectionManagers.computeIfAbsent(key,(k)->{
//
//            var dnsResolver = remoteDns?this.remoteDnsResolver:this.fullDnsResolver;
//            var registry = checkSsl?this.defaultRegistry:this.sslUncheckedRegistry;
//
//            /*var result = new PoolingHttpClientConnectionManager(
//                            // We're forced to create a SocketFactory Registry.  Passing null
//                            //   doesn't force a default Registry, so we re-invent the wheel.
//                            registry,
//                            dnsResolver // Our DnsResolver
//                    );
//            result.setDefaultMaxPerRoute(10);
//            result.setMaxTotal(100);*/
//            BasicHttpClientConnectionManager result = new BasicHttpClientConnectionManager(
//    /* We're forced to create a SocketFactory Registry.  Passing null
//       doesn't force a default Registry, so we re-invent the wheel. */
//                    registry,
//                    null, /* Default ConnectionFactory */
//                    null, /* Default SchemePortResolver */
//                    dnsResolver  /* Our DnsResolver */
//            );
//            return result;
//        });

        var dnsResolver = remoteDns?this.remoteDnsResolver:this.fullDnsResolver;
        var registry = checkSsl?this.defaultRegistry:this.sslUncheckedRegistry;

            /*var result = new PoolingHttpClientConnectionManager(
                            // We're forced to create a SocketFactory Registry.  Passing null
                            //   doesn't force a default Registry, so we re-invent the wheel.
                            registry,
                            dnsResolver // Our DnsResolver
                    );
            result.setDefaultMaxPerRoute(10);
            result.setMaxTotal(100);*/
        HttpClientConnectionManager result = new BasicHttpClientConnectionManager(
    /* We're forced to create a SocketFactory Registry.  Passing null
       doesn't force a default Registry, so we re-invent the wheel. */
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                null, /* Default ConnectionFactory */
                null, /* Default SchemePortResolver */
                dnsResolver  /* Our DnsResolver */
        );
        result = new PoolingHttpClientConnectionManager();
                // We're forced to create a SocketFactory Registry.  Passing null
                //   doesn't force a default Registry, so we re-invent the wheel.
       /*         RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                dnsResolver // Our DnsResolver
        );*/
        //result.setDefaultMaxPerRoute(10);
        //result.setMaxTotal(100);
        return result;
    }

    @Override
    public CloseableHttpClient buildClient(boolean remoteDns, boolean checkSsl, int port,String protocol) {
        var dnsResolver = remoteDns?this.remoteDnsResolver:this.fullDnsResolver;
        var connectionManager = this.getConnectionManger(remoteDns,checkSsl);
        return HttpClientBuilder.create()
                //.setRetryHandler(requestRetryHandler)
                //.setConnectionManager(connectionManager)
                .setDnsResolver(dnsResolver)
                /*.setSchemePortResolver(httpHost -> {
                    if(port>0) {
                        return port;
                    }
                    if(protocol!=null && protocol.equalsIgnoreCase("https")) return 443;
                    return 80;
                })*/
                //.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                //.setSSLContext(sc)
                .setConnectionManagerShared(true)
                //.disableConnectionState()
                .disableRedirectHandling()
                /*.setSchemePortResolver((host)->{
                    var result = port;
                    if(result>0) {
                        return result;
                    }
                    if(protocol!=null && protocol.equalsIgnoreCase("https")) {
                        result = 443;
                    }else {
                        result = 80;
                    }
                    return result;
                })*/
                .build();
    }
}
