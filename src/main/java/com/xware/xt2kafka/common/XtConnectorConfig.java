package com.xware.xt2kafka.common;

import org.apache.kafka.common.config.ConfigDef;

public class XtConnectorConfig {
    public static final String XT_CONFIG = "xt.config";
    public static final String XT_CONTRACT = "xt.contract";
    public static final String XT_PASSWORD = "xt.password";
    public static final String XT_URI = "xt.uri";
    public static final String XT_CONNIP = "xt.connip";
    public static final String XT_CONNPORT = "xt.connport";
    public static final String XT_CA = "xt.ca";
    public static final String XT_TOPIC = "xt.topic";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(XT_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "XT Grpc Setting JSON full filename")
            .define(XT_CONTRACT, ConfigDef.Type.STRING,"", ConfigDef.Importance.MEDIUM,"XT Contract name. If empty XT will determine contract name")
            .define("topics", ConfigDef.Type.STRING, "XTSINK.DEMO", ConfigDef.Importance.MEDIUM, "Kafka Topic")
            .define(XT_PASSWORD, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "XT Password")
            .define(XT_URI, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "XT URI")
            .define(XT_CONNIP, ConfigDef.Type.STRING, "localhost", ConfigDef.Importance.HIGH, "XT ConnIP")
            .define(XT_CONNPORT, ConfigDef.Type.STRING, "61001", ConfigDef.Importance.HIGH, "XT ConnPort")
            .define(XT_CA, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "XT CA")
            .define(XT_TOPIC, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "XT Topic");

}
