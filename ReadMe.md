WinRM Client Plugin
==============
This plugin's main goal is to provide WinRM Operations as Build Step.

At present following Operations implemented:
---
  1) Send-File Operation
  
  2) Invoke-Command Operation
  
# Job DSL example
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
# Jenkins Pipeline example
    winRMClient credentialsId: '549bdb9b-0d36-4c21-948a-ca0a9ef6575f', hostName: '192.168.1.7', winRMOperations: [invokeCommand('dir'), sendFile(configurationName: 'DataNoLimits', destination: 'C:\\test', source: 'C:\\test.txt')]