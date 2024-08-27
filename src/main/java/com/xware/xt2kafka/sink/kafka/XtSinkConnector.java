package com.xware.xt2kafka.sink.kafka;

import com.xware.xt2kafka.common.XtConnectorConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XtSinkConnector extends SinkConnector {
    protected static final String VERSION = "0.0.1";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, String> configProps = null;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting XT2Kafka Sink Connector {}", props);
        configProps = props;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return XtSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        for (int task = 0; task < maxTasks; task++) {
            taskConfigs.add(configProps);
        }
        return taskConfigs;
    }

    @Override
    public void stop() {
        log.info("Stopping XT2Kafka Sink Connector {}", configProps);
    }

    @Override
    public ConfigDef config() {
        return XtConnectorConfig.CONFIG_DEF;
    }

    @Override
    public String version() {
        return VERSION;
    }
}
