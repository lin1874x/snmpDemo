package com.lin1874.snmp.client;

import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;

import java.io.IOException;

public interface SnmpClient {

    void get(String oid) throws IOException;

    void getBulk(String oid) throws IOException;

    void walk(String oid);

    default Address getAddress(String ip, String port) {
        return GenericAddress.parse("udp:" + ip + "/" + port);
    }

}
