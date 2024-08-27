package com.xware.xt2kafka.common;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.handler.ssl.SslContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xware.xt.grpc.application.*;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

public class XtGrpcConnection {
    private String configFile = "C:/XtSamples/GrpcSamplesettings.json";
    private String uri = "";
    private String password = "";
    private String connect = "";
    private String connIP = "";
    private Integer connPort = 0;
    private JSONParser parser = new JSONParser();
    private InputStream caStream = null;
    private String contract = "";

    public XtGrpcConnection() {
    }

    public XtGrpcConnection(String configFile) {
        this.configFile = configFile;
    }



    public MsgGrpc.MsgBlockingStub getXt() {
        return xt;
    }

    public MsgGrpc.MsgStub getXt_async() {
        return xt_async;
    }

    private MsgGrpc.MsgBlockingStub xt = null;
    private MsgGrpc.MsgStub xt_async = null;
    private Channel channel = null;

    public LogOffReply xTLogoff() {
        var logOffMessage = LogOffMessage.newBuilder().build();
        return xt.logOff(logOffMessage);
    }

    public LogOnReply xTLogon() throws CertificateException, SSLException {
        Metadata auth_meta = new Metadata();
        loadConfigFile();
        createChannel(auth_meta);
        xt = MsgGrpc.newBlockingStub(channel);
        xt_async = MsgGrpc.newStub(channel);
        LogOnReply logonReply = xt.logOn(LogOnMessage.newBuilder()
                .setApplication(uri)
                .setPassword(password)
                .build());

        // Set the authentication data returned from LogOn in the metadata interceptor
        auth_meta.put(Metadata.Key.of(logonReply.getMetaKey(), Metadata.ASCII_STRING_MARSHALLER), logonReply.getTicket());
        return logonReply;
    }

    private void createChannel(Metadata auth_meta) throws CertificateException, SSLException {
        if(caStream != null) {
            SslContext sslContext = createSslContext();
            NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(connIP, connPort);

            channelBuilder.sslContext(sslContext);
            // Add a metadata interceptor for authentication when the channel is created
            channelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(auth_meta));
            channel = channelBuilder.build();
        }
        else {
            ManagedChannelBuilder channelBuilder = ManagedChannelBuilder.forAddress(connIP, connPort)
                    .usePlaintext();
            channelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(auth_meta));
            channel = channelBuilder.build();
        }
    }

    private SslContext createSslContext() throws CertificateException, SSLException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate cert = certificateFactory.generateCertificate(caStream);

        X509Certificate x509Cert = (X509Certificate)cert;

        return GrpcSslContexts.forClient().trustManager(x509Cert).build();
    }

    public void loadConfigFile() {
        try {
            Object obj = parser.parse(new FileReader(configFile));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject app = (JSONObject) jsonObject.get("application");
            password = (String) app.get("password");
            String ca = (String) jsonObject.get("cabundle");
            uri = (String) app.get("uri");
            connect = (String) jsonObject.get("connect");
            String[] arrOfConn = connect.split(":", 2);
            connIP = arrOfConn[0];
            connPort = (Integer) Integer.parseInt(arrOfConn[1]);
            if(ca != null && !ca.isEmpty()) {
                caStream = new ByteArrayInputStream(ca.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfig(Map<String, String> poperties){

    }


}
