package net.luigidemasi.me.jetty.issue;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

import static net.luigidemasi.me.jetty.issue.Constant.HTTPS_PORT;
import static net.luigidemasi.me.jetty.issue.Constant.HTTP_PORT;
import static net.luigidemasi.me.jetty.issue.Constant.SERVER_KEYSTOREPATH;
import static net.luigidemasi.me.jetty.issue.Constant.SERVER_TRUSTSTORE_WITH_EXPIRED_CERTIFICATE_PATH;
import static net.luigidemasi.me.jetty.issue.Utils.clientCerificateAuthHTTPRequest;
import static net.luigidemasi.me.jetty.issue.Utils.waitUntilServerIsStarted;


public class JettyClientAuthenticationExpiredCertificateTest {

    private static Logger LOG =  LoggerFactory.getLogger(JettyClientAuthenticationExpiredCertificateTest.class);

    private static Server server;

    @ClassRule
    public static TemporaryFolder jetty_base_dir = new TemporaryFolder();

    @ClassRule
    public static TemporaryFolder jetty_home_dir = new TemporaryFolder();

    @ClassRule
    public static TemporaryFolder webroot_dir = new TemporaryFolder();


    @Test
    public void testExpiredCertificate() throws Exception {
        waitUntilServerIsStarted(server);
        HttpResponse response = null;

        try {
            response = clientCerificateAuthHTTPRequest("client","keys/client_identity.jks", "keys/client_truststore.jks" );

        }catch (Throwable e){
            LOG.error("fail, reason: "+e.getMessage());
            e.printStackTrace();
            TestCase.fail();
        }
        TestCase.assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();

        LOG.info("----------------------------------------");
        LOG.info("Response status line: "+response.getStatusLine());
        EntityUtils.consume(entity);
    }

    @BeforeClass
    public static void setup() throws Exception {
        LOG.trace(Constant.KEYPASS);

        String jetty_base = jetty_base_dir.getRoot().getCanonicalPath();
        String jetty_home = jetty_home_dir.getRoot().getCanonicalPath();
        String webroot = webroot_dir.getRoot().getCanonicalPath();
        File serverTruststore = new File(SERVER_TRUSTSTORE_WITH_EXPIRED_CERTIFICATE_PATH);
        File serverKeystore = new File(SERVER_KEYSTOREPATH);
        File webapps = jetty_base_dir.newFolder("webapps");
        File etc = jetty_home_dir.newFolder("etc");
        File log = jetty_home_dir.newFolder("logs");

        File newServerTruststore = new File(etc.getCanonicalPath() + File.separator + "server_truststore.jks");
        File newServerKeystore = new File(etc.getCanonicalPath() + File.separator + "server_identity.jks");

        File index = Paths.get(JettyClientAuthenticationExpiredCertificateTest.class.getClassLoader().getResource("index.html").toURI()).toFile();
        File newIndex  = new File(etc.getCanonicalPath() + File.separator + "index.html");

        //copy trust store
        FileUtils.copyFile(serverTruststore, newServerTruststore);

        //copy trust store
        FileUtils.copyFile(serverKeystore, newServerKeystore);

        //copy webdefault
        FileUtils.copyFile(index, newIndex);

        LOG.info("JETTY_HOME="+jetty_home);
        LOG.info("JETTY_BASE="+jetty_base);
        LOG.info("webroot="+webroot);
        LOG.info("etc="+etc.getAbsolutePath());
        LOG.info("HTTP_PORT="+ HTTP_PORT);
        LOG.info("HTTPS_PORT="+ HTTPS_PORT);

        server = JettyServerFactory.create(jetty_base, jetty_home, webroot, HTTP_PORT, HTTPS_PORT);

        server.start();
        //server.join();
    }

    @AfterClass
    public static void setDown() throws Exception {
        if (server != null) {
            server.setStopAtShutdown(true);
            server.setStopTimeout(2000L);
            server.stop();
        }
    }
}
