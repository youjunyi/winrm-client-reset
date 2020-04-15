package com.spcow.plugins.winrmclient;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/*
 ```
 job {
    steps {
        winRMClient {
            hostName(String hostName)
            credentialsId(String credentialsId)
            sendFile(String source, String destination, String configurationName)
            invokeCommand(String command)
        }
    }
 }
 ```
 For example:
 ```
    freeStyleJob('WinRMClientJob') {
        steps {
          winRMClient {
            hostName('192.168.1.2')
            credentialsId('44620c50-1589-4617-a677-7563985e46e1')
            sendFile('C:\\test.txt','C:\\test', 'DataNoLimits')
            invokeCommand('dir')
          }
        }
    }
 ```
*/

@Extension(optional = true)
public class WinRMClientJobDslExtension extends ContextExtensionPoint {

    @RequiresPlugin(id = "winrm-client", minimumVersion = "1.0")
    @DslExtensionMethod(context = StepContext.class)
    public Object winRMClient(Runnable closure) {
        WinRMClientJobDslContext context = new WinRMClientJobDslContext();
        executeInContext(closure, context);
        return new WinRMClientBuilder(context.hostName, context.credentialsId, context.winRMOperations);
    }
}
