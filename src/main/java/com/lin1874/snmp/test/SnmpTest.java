package com.lin1874.snmp.test;

import com.lin1874.snmp.client.SnmpClient;
import com.lin1874.snmp.client.SnmpClientV2;
import com.lin1874.snmp.client.SnmpClientV3;
import com.lin1874.snmp.config.SnmpConfig;
import org.snmp4j.mp.SnmpConstants;

import java.io.IOException;

public class SnmpTest {

    public static void main(String[] args) throws IOException {
        /**
         * v2
         */
        SnmpConfig snmpConfig = new SnmpConfig();
        snmpConfig.setIp("192.168.1.8");
        snmpConfig.setPort("161");
        snmpConfig.setCommunity("public");
        snmpConfig.setTimeout(5000L);
        snmpConfig.setVersion(SnmpConstants.version2c);
        snmpConfig.setMaxRepetitions(50);

        SnmpClient snmpClientV2 = new SnmpClientV2(snmpConfig);
        snmpClientV2.get(".1.3.6.1.2.1.1.9.1.2.1");
        snmpClientV2.getBulk(".1.3.6.1.2.1.1.9.1.2.1");
        snmpClientV2.walk(".1.3");

        /**
         * v3
         */
        SnmpConfig snmpConfigV3 = new SnmpConfig();
        snmpConfigV3.setIp("192.168.1.8");
        snmpConfigV3.setPort("161");
        snmpConfigV3.setTimeout(5000L);
        snmpConfigV3.setVersion(SnmpConstants.version3);
        snmpConfigV3.setMaxRepetitions(50);
        snmpConfigV3.setUsername("lin1874x");
        snmpConfigV3.setAuthAlgorithm("MD5");
        snmpConfigV3.setAuthPassword("Admin123");
        snmpConfigV3.setPrivAlgorithm("DES");
        snmpConfigV3.setPrivPassword("Admin123");


        SnmpClient snmpClientV3 = new SnmpClientV3(snmpConfigV3);
        snmpClientV3.get(".1.3.6.1.2.1.1.9.1.2.4");
        snmpClientV3.getBulk(".1.3.6.1.2.1.1.9.1.2.4");
        snmpClientV3.walk(".1.3.6.1.2.1.1.9.1.2.4");
    }
}
