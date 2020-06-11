package net.luigidemasi.me.jetty.issue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static net.luigidemasi.me.jetty.issue.Utils.findNextAvailablePort;

public class Constant {

    public static String SERVER_TRUSTSTORE = "keys/server_truststore.jks";
    public static String SERVER_KEYSTORE   = "keys/server_identity.jks";



    public static String SERVER_TRUSTSTORE_WITH_EXPIRED_CERTIFICATE_PATH;
    public static String SERVER_TRUSTSTORE_WITH_VALID_CERTIFICATE_PATH;

    public static String SERVER_KEYSTOREPATH;
    public static int HTTPS_PORT;
    public static int HTTP_PORT;
    public static final String KEYSTOREPASS = "password";
    public static final String KEYPASS = "password";


    static {
        try {
            SERVER_TRUSTSTORE_WITH_EXPIRED_CERTIFICATE_PATH = Paths.get(Constant.class.getClassLoader().getResource(SERVER_TRUSTSTORE).toURI()).toFile().getCanonicalPath();
            SERVER_KEYSTOREPATH   = Paths.get(Constant.class.getClassLoader().getResource(SERVER_KEYSTORE).toURI()).toFile().getCanonicalPath();
            HTTP_PORT = findNextAvailablePort();
            HTTPS_PORT = findNextAvailablePort();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
