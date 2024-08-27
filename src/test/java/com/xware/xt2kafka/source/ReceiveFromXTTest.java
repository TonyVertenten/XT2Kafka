package com.xware.xt2kafka.source;

import com.xware.xt2kafka.common.XtGrpcConnection;
import com.xware.xt2kafka.source.grpc.ReceiveFromXT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xware.xt.grpc.application.*;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;

public class ReceiveFromXTTest {
    static XtGrpcConnection connection;

    @BeforeAll
    public static void setup() throws CertificateException, SSLException {
        connection = new XtGrpcConnection();
        var logonReply = connection.xTLogon();
    }

    @AfterAll
    public static void teardown() {
        var logoffReply = connection.xTLogoff();
        connection = null;
    }

    @Test
    public void ReceiveMsgFromXT() {
        try {
            FileOutputStream fos = new FileOutputStream("C:/xTSamples/testoutput.xml");
            MsgGrpc.MsgBlockingStub xt = connection.getXt();
            MsgGrpc.MsgStub xt_async = connection.getXt_async();
            ReceiveFromXT receive = new ReceiveFromXT(xt_async, xt);
            WaitMsgReply reply = receive.ReceiveMsgFromXT(fos);
            Assertions.assertNotEquals(0, reply.getIdsCount());
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void ReceiveMsgFromXTinByteArrayOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MsgGrpc.MsgBlockingStub xt = connection.getXt();
        MsgGrpc.MsgStub xt_async = connection.getXt_async();
        ReceiveFromXT receive = new ReceiveFromXT(xt_async, xt);
        try {
            WaitMsgReply reply = receive.ReceiveMsgFromXT(baos);
            Assertions.assertNotEquals(0, reply.getIdsCount());
            System.out.println("baos size: " + baos.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void ReceiveMsgOneByOne() {
        MsgGrpc.MsgBlockingStub xt = connection.getXt();
        MsgGrpc.MsgStub xt_async = connection.getXt_async();
        ReceiveFromXT receive = new ReceiveFromXT(xt_async, xt);
        WaitMsgReply reply = xt.waitMsg(WaitMsgMessage.newBuilder().build());
        for(int i=0; i < reply.getIdsCount(); i++) {
            var msgId = reply.getIds(i).getId();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                int bytesReceived = receive.ReceiveData(baos, msgId);
                if(bytesReceived != 0) {
                    var message = baos.toString();
                }
                Assertions.assertNotEquals(0, bytesReceived);
                xt.msgSetStatus(MsgSetStatusMessage.newBuilder().setId(reply.getIds(i).getId()).setCmd(MsgStatusCommand.STATUS_OK).build());
                baos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }

}
