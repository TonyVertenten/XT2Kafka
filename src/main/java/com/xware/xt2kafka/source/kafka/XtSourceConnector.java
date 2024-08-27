package com.xware.xt2kafka.source.kafka;

import com.xware.xt2kafka.common.XtConnectorConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XtSourceConnector extends SourceConnector {
    protected static final String VERSION = "0.0.1";

    private static final Logger log = LoggerFactory.getLogger(XtSourceConnector.class);
    private Map<String, String> configProps = null;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting XtSourceConnector");
        configProps = props;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return XtSourceTask.class;
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
        log.info("Stopping XtSourceConnector");
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
