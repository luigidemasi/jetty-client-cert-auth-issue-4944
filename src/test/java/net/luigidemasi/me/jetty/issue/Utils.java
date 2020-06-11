package net.luigidemasi.me.jetty.issue;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Random;
import static net.luigidemasi.me.jetty.issue.Constant.*;

public class Utils {

    private static final Random random = new Random(System.currentTimeMillis());
    private static Logger LOG =  LoggerFactory.getLogger(Utils.class);


    private Utils() {}


    public static HttpResponse clientCerificateAuthHTTPRequest(String certificateAlias,String keystoreFileName, String truststoreFileName ) throws Exception {

        File keystore = Paths.get(JettyClientAuthenticationExpiredCertificateTest.class.getClassLoader().getResource(keystoreFileName).toURI()).toFile();
        File truststore = Paths.get(JettyClientAuthenticationExpiredCertificateTest.class.getClassLoader().getResource(truststoreFileName).toURI()).toFile();

        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(readStore(KEYSTOREPASS, keystoreFileName), KEYPASS.toCharArray(), (aliases, socket) -> certificateAlias)
                .loadTrustMaterial(truststore, KEYPASS.toCharArray())
                .build();

        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory =
                new SSLConnectionSocketFactory(sslContext.getSocketFactory(),
                        new NoopHostnameVerifier());
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslConnectionFactory)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);


        HttpClient httpClient = builder.build(); //HttpClients.custom().setSSLContext(sslContext).build();
        return  httpClient.execute(new HttpGet("https://localhost:"+ HTTPS_PORT));

    }


    public static KeyStore readStore(String storePassword, String keystoreFileName) throws Exception {
        try (InputStream keyStoreStream = Utils.class.getClassLoader().getResourceAsStream(keystoreFileName)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreStream, storePassword.toCharArray());
            return keyStore;
        }
    }
    public static int findNextAvailablePort() {
        int minPort = 49152;
        int maxPort = 65535;

        int portRange = maxPort - minPort;
        int candidatePort;
        int searchCounter = 0;
        do {
            if (searchCounter > portRange) {
                throw new IllegalStateException(String.format(
                        "Could not find an available TCP port in the range [%d, %d] after %d attempts",
                        minPort, maxPort, searchCounter));
            }
            candidatePort = findRandomPort(minPort, maxPort);
            searchCounter++;
        }
        while (!isPortAvailable(candidatePort));

        return candidatePort;
    }

    public static int findRandomPort(int minPort, int maxPort) {
        int portRange = maxPort - minPort;
        return minPort + random.nextInt(portRange + 1);
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
                    port, 1, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void waitUntilServerIsStarted(Server server) throws InterruptedException {
        if (server == null) {
            return;
        }
        while (!server.isStarted()) {
            Thread.sleep(2000);
        }
    }


}
