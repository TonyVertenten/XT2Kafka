package com.xware.xt2kafka.sink;

import com.xware.xt2kafka.common.XtGrpcConnection;
import com.xware.xt2kafka.sink.grpc.SendToXT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xware.xt.grpc.application.MsgGrpc;

import javax.net.ssl.SSLException;
import java.io.*;
import java.security.cert.CertificateException;

public class SendToXTTest {

    static XtGrpcConnection connection;

    @BeforeAll
    public static void setup() throws CertificateException, SSLException {
        connection = new XtGrpcConnection("C:/XtSamples/GrpcSamplesettings.json");
        var logonReply = connection.xTLogon();
    }

    @AfterAll
    public static void teardown() {
        var logoffReply = connection.xTLogoff();
        connection = null;
    }

    @Test
    public void sendMessageTest() {
        try {
            FileInputStream payload = new FileInputStream("C:/xTSamples/Greeting.xml");
            MsgGrpc.MsgBlockingStub xt = connection.getXt();
            MsgGrpc.MsgStub xt_async = connection.getXt_async();
            SendToXT send = new SendToXT(xt_async, xt);
            var MsgId = send.SendMsgToXT(payload, "GrpcJava Contract");
            Assertions.assertNotEquals(0L, MsgId);
            payload.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sendMessageAsBytesTest() {
        try {
            FileInputStream payload = new FileInputStream("C:/xTSamples/Greeting.xml");
            // read file into string
            String sPayload = converStreamToString(payload);
            MsgGrpc.MsgBlockingStub xt = connection.getXt();
            MsgGrpc.MsgStub xt_async = connection.getXt_async();
            SendToXT send = new SendToXT(xt_async, xt);
            InputStream inputStream = new ByteArrayInputStream(sPayload.getBytes());
            var MsgId = send.SendMsgToXT(inputStream, "GrpcJava Contract");
            Assertions.assertNotEquals(0L, MsgId);
            payload.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String converStreamToString(InputStream is) {
        if (is == null) {
            return "";
        }

        java.util.Scanner s = new java.util.Scanner(is);
        s.useDelimiter("\\A");

        String streamString = s.hasNext() ? s.next() : "";

        s.close();

        return streamString;
    }
}
