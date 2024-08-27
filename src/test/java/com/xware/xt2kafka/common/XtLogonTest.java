package com.xware.xt2kafka.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xware.xt.grpc.application.LogOffReply;
import xware.xt.grpc.application.LogOnReply;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class XtLogonTest {


    @Test
    public void xTLogonShouldReturnLogonReply() throws CertificateException, SSLException {
        XtGrpcConnection xtGrpcConnection = new XtGrpcConnection();
        LogOnReply logOnReply = xtGrpcConnection.xTLogon();
        Assertions.assertNotNull(logOnReply, "xtLogon() returned null");
        var logOffReply = xtGrpcConnection.xTLogoff();
    }
}
