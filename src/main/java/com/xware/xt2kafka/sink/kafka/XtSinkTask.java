package com.xware.xt2kafka.sink.kafka;

import com.xware.xt2kafka.common.XtGrpcConnection;
import com.xware.xt2kafka.common.XtConnectorConfig;
import com.xware.xt2kafka.sink.grpc.SendToXT;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xware.xt.grpc.application.MsgGrpc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class XtSinkTask extends SinkTask {

    private static Logger log = LoggerFactory.getLogger(XtSinkTask.class);
    private static final String VERSION = "1.0.0";

    private XtGrpcConnection xtGrpcConnection;
    private SendToXT sendToXT;
    private String xTConfigFile;
    private MsgGrpc.MsgStub xt_async;
    private MsgGrpc.MsgBlockingStub xt;
    private String xTContract;

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public void start(Map<String, String> properties) {
        log.info("Starting XtSinkTask {}", properties);
        AbstractConfig config = new AbstractConfig(XtConnectorConfig.CONFIG_DEF, properties);
        log.debug("Config {}", config);

        // Connect to Xt
        xTConfigFile = config.getString(XtConnectorConfig.XT_CONFIG);
        xTContract = config.getString(XtConnectorConfig.XT_CONTRACT);
        try {
            xtGrpcConnection = new XtGrpcConnection(xTConfigFile);
            var logonReply = xtGrpcConnection.xTLogon();
            xt_async = xtGrpcConnection.getXt_async();
            xt = xtGrpcConnection.getXt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void put(Collection<SinkRecord> collection) {
        if (collection.size() > 0) {
            log.debug("Received {} records for XT", collection.size());
        }

        sendToXT = new SendToXT(xt_async, xt);
        try {
            for (SinkRecord record : collection) {
                InputStream inputStream = new ByteArrayInputStream(record.value().toString().getBytes());
                var MsgId = sendToXT.SendMsgToXT(inputStream, xTContract);
            }

        } catch (Exception e) {
            log.error("Error sending to XT", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stop() {
        log.info("Stopping XtSinkTask");
        xtGrpcConnection.xTLogoff();
    }
}
