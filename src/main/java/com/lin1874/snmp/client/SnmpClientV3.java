package com.lin1874.snmp.client;

import com.lin1874.snmp.config.SnmpConfig;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.List;

public class SnmpClientV3 implements SnmpClient {

    private final Snmp snmp;

    private final UserTarget target;

    private final SnmpConfig snmpConfig;

    public SnmpClientV3(SnmpConfig snmpConfig) throws IOException {
        if (SnmpConstants.version3 != snmpConfig.getVersion()) {
            throw new RuntimeException("snmp client init error, snmp version is not 3");
        }
        this.snmpConfig = snmpConfig;
        snmp = new Snmp(new DefaultUdpTransportMapping());
        UsmUser user = new UsmUser(
                new OctetString(snmpConfig.getUsername()),
                getAlgorithm(snmpConfig.getAuthAlgorithm()),
                new OctetString(snmpConfig.getAuthPassword()),
                getAlgorithm(snmpConfig.getPrivAlgorithm()),
                new OctetString(snmpConfig.getPrivPassword()));
        USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(),
                new OctetString(MPv3.createLocalEngineID()), 0);
        usm.setEngineDiscoveryEnabled(true);
        usm.addUser(new OctetString(snmpConfig.getUsername()),
                new OctetString("80001f8880700f474b048a8464000000"),
                user);
        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.listen();

        //查看SNMP引擎ID
        // snmpwalk -v 3 -u <username> -l authPriv -a SHA -A <auth_password> -x AES -X <priv_password> localhost sysDescr.0
        target = new UserTarget();
        target.setAddress(getAddress(snmpConfig.getIp(), snmpConfig.getPort()));
        target.setTimeout(snmpConfig.getTimeout());
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(new OctetString(snmpConfig.getUsername()));

    }

    private OID getAlgorithm(String algorithmName) {
        if ("MD5".equals(algorithmName)) {
            return AuthMD5.ID;
        } else if ("DES".equals(algorithmName)) {
            return PrivDES.ID;
        } else {
            throw new RuntimeException("algorithmName is not support!");
        }
    }

    @Override
    public void get(String oid) throws IOException {
        ScopedPDU requestPDU = new ScopedPDU();
        requestPDU.add(new VariableBinding(new OID(oid)));
        requestPDU.setType(PDU.GET);
        ResponseEvent response = snmp.send(requestPDU, target);
        if (response.getResponse() == null) {
            // request timed out
            System.out.println("请求超时");
        } else {
            System.out.println("Received response from: " + response.getPeerAddress());
            // dump response PDU
            System.out.println(response.getResponse().toString());
        }
    }

    @Override
    public void getBulk(String oid) throws IOException {
        if (null == snmp || null == target) {
            throw new RuntimeException("snmp client is not init");
        }

        ScopedPDU requestPDU = new ScopedPDU();
        requestPDU.add(new VariableBinding(new OID(oid)));
        requestPDU.setType(PDU.GETBULK);
        // 请求的返回结果的数量
        requestPDU.setMaxRepetitions(snmpConfig.getMaxRepetitions());
        // setNonRepeaters参数的值不能超过请求中指定的OID个数，否则会抛出异常。
        // 如果setNonRepeaters设置为0，则所有OID都会重复获取，直到达到最大重复次数或获取到指定数量的值为止。
        requestPDU.setNonRepeaters(0);
        ResponseEvent response = snmp.send(requestPDU, target);
        if (response.getResponse() == null) {
            System.out.println("request timeout");
        } else {
            System.out.println("Received response from: " + response.getPeerAddress());
            System.out.println(response.getResponse().toString());
        }
    }

    @Override
    public void walk(String oid) {
        int cnt = 0;
        TreeUtils treeUtils = new TreeUtils(snmp, new MyDefaultPDUFactory(PDU.GETBULK,  new OctetString(snmpConfig.getContextEngineId())));
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
    private static class MyDefaultPDUFactory extends DefaultPDUFactory {
        private OctetString contextEngineId = null;

        public MyDefaultPDUFactory(int pduType, OctetString contextEngineId) {
            super(pduType);
            this.contextEngineId = contextEngineId;
        }

        @Override
        public PDU createPDU(Target target) {
            PDU pdu = super.createPDU(target);
            if (target.getVersion() == SnmpConstants.version3) {
                ((ScopedPDU)pdu).setContextEngineID(contextEngineId);
            }
            return pdu;
        }
    }
}
