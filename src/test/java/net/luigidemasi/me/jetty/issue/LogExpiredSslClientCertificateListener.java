package net.luigidemasi.me.jetty.issue;

import org.eclipse.jetty.io.ssl.SslHandshakeListener;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.Optional;

public class LogExpiredSslClientCertificateListener implements SslHandshakeListener {
    private static final Logger LOG = Log.getLogger(LogExpiredSslClientCertificateListener.class);

    @Override
    public void handshakeFailed(Event event, Throwable failure) {
        String peerHost = null;
        int peerPort = 0;

        if (event.getSSLEngine() != null){
            peerHost = event.getSSLEngine().getPeerHost();
            peerPort = event.getSSLEngine().getPeerPort();
        }

        Throwable current = failure;
        while(current != null && current.getCause() != null) {
            current = current.getCause();
        }
        LOG.warn("Peer {}:{} - {}: {}",peerHost, peerPort, current.getClass().getCanonicalName(), current.getMessage() );

        /* Expected output:
            10:54:24.932 [qtp710708543-20] WARN  n.l.m.j.i.LogExpiredSslClientCertificateListener - Peer 127.0.0.1:37474 - java.security.cert.CertificateExpiredException: NotAfter: Sun Jun 16 17:11:33 CEST 2019
        */
    }
}
