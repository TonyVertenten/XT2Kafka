package com.xware.xt2kafka.source.kafka;

import com.xware.xt2kafka.common.XtConnectorConfig;
import com.xware.xt2kafka.common.XtGrpcConnection;
import com.xware.xt2kafka.source.grpc.ReceiveFromXT;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xware.xt.grpc.application.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class XtSourceTask extends SourceTask {
    protected static final String VERSION = "0.0.1";
    private static final Logger log = LoggerFactory.getLogger(XtSourceTask.class);

    private XtGrpcConnection xtGrpcConnection;
    private String xTConfigFile;
    private MsgGrpc.MsgStub xt_async;
    private MsgGrpc.MsgBlockingStub xt;
    private String topic;


    @Override
    public void start(Map<String, String> properties) {
        log.info("Starting XtSinkTask {}", properties);
        AbstractConfig config = new AbstractConfig(XtConnectorConfig.CONFIG_DEF, properties);
        log.debug("Config {}", config);

        // Connect to Xt
        xTConfigFile = config.getString(XtConnectorConfig.XT_CONFIG);
        // xTContract = config.getString(XtConnectorConfig.XT_CONTRACT);
        topic = config.getString(XtConnectorConfig.XT_TOPIC);
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
    public List<SourceRecord> poll() throws InterruptedException {
        ArrayList<SourceRecord> records = new ArrayList<>();
        try {
            ReceiveFromXT receive = new ReceiveFromXT(xt_async, xt);
            WaitMsgReply reply = xt.waitMsg(WaitMsgMessage.newBuilder().build());
            for(int i=0; i < reply.getIdsCount(); i++) {
                var msgId = reply.getIds(i).getId();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int bytesReceived = receive.ReceiveData(baos, msgId);
                if(bytesReceived != 0) {
                    Map sourcePartition = Collections.singletonMap("MsgId", msgId.toString());
                    Map sourceOffset = Collections.singletonMap("position", Integer.valueOf(i));
                    records.add(new SourceRecord(sourcePartition, sourceOffset,
                            topic, Schema.STRING_SCHEMA,baos.toString()));
                    baos.flush();
                    baos.close();
                }
                xt.msgSetStatus(MsgSetStatusMessage.newBuilder().setId(reply.getIds(i).getId()).setCmd(MsgStatusCommand.STATUS_OK).build());
            }
            //return records;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    @Override
    public void stop() {
        log.info("Stopping XtSourceTask");
        xtGrpcConnection.xTLogoff();
    }

    @Override
    public String version() {
        return VERSION;
    }
}
