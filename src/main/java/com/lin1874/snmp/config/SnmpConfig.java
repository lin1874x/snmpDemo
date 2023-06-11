package com.lin1874.snmp.config;


import lombok.Data;

@Data
public class SnmpConfig {

    private String ip;
    private String port;

    private Integer version;

    private String community;

    private Long timeout;

    private Integer maxRepetitions;

    private String username;

    private String authPassword;

    private String privPassword;

    private String authAlgorithm;

    private String privAlgorithm;

    private String contextEngineId;


}
