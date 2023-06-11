package com.lin1874.snmp.client;

import com.lin1874.snmp.config.SnmpConfig;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.List;

public class SnmpClientV2 implements SnmpClient{

    private final Snmp snmp;

    private final CommunityTarget target;

    private final SnmpConfig snmpConfig;

    public SnmpClientV2(SnmpConfig snmpConfig) throws IOException {
        if (SnmpConstants.version2c != snmpConfig.getVersion()) {
            throw new RuntimeException("snmp client init error, snmp version is not 2c");
        }
        this.snmpConfig = snmpConfig;
        snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.listen();

        target = new CommunityTarget();
        target.setCommunity(new OctetString(snmpConfig.getCommunity()));
        target.setAddress(getAddress(snmpConfig.getIp(), snmpConfig.getPort()));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(snmpConfig.getTimeout());
    }

    @Override
    public void get(String oid) throws IOException {
        if (null == snmp || null == target) {
            throw new RuntimeException("snmp client is not init");
        }
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        ResponseEvent response = snmp.send(pdu, target);
        if (response.getResponse() == null) {
            System.out.println("request timeout");
        }
        else {
            System.out.println("Received response from: "+ response.getPeerAddress());
            System.out.println(response.getResponse().toString());
        }
    }

    @Override
    public void getBulk(String oid) throws IOException {
        if (null == snmp || null == target) {
            throw new RuntimeException("snmp client is not init");
        }
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(snmpConfig.getMaxRepetitions());

        ResponseEvent response = snmp.send(pdu, target);
        if (response.getResponse() == null) {
            System.out.println("request timeout");
        }
        else {
            System.out.println("Received response from: "+ response.getPeerAddress());
            System.out.println(response.getResponse().toString());
        }
    }

    @Override
    public void walk(String oid) {
        int cnt = 0;
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.walk(target, new OID[]{new OID(oid)});
        for (TreeEvent event : events) {
            if (event != null) {
                VariableBinding[] varBindings = event.getVariableBindings();
                if (varBindings != null) {
                    for (VariableBinding varBinding : varBindings) {
                        cnt++;
                        System.out.println(varBinding.getOid() + " = " + varBinding.getVariable());
                    }
                }
            } else {
                System.out.println("request error");
            }
        }
        System.out.println("walk item number is : " + cnt);
    }
}
