package com.spcow.plugins.winrmclient;

import javaposse.jobdsl.dsl.Context;

import java.util.ArrayList;
import java.util.List;

public class WinRMClientJobDslContext implements Context {

    List<WinRMOperation> winRMOperations = new ArrayList<>();
    String hostName;
    String credentialsId;

    void hostName(String hostName) {
        this.hostName = hostName;
    }

    void credentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    void sendFile(String source, String destination, String configurationName) {
        SendFileWinRMOperation sendFileWinRMOperation = new SendFileWinRMOperation(source, destination, configurationName);
        winRMOperations.add(sendFileWinRMOperation);
    }

    void invokeCommand(String command) {
        InvokeCommandWinRMOperation invokeCommandWinRMOperation = new InvokeCommandWinRMOperation(command);
        winRMOperations.add(invokeCommandWinRMOperation);
    }
}
