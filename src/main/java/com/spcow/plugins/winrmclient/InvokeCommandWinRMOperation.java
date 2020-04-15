package com.spcow.plugins.winrmclient;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.CommandInterpreter;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.Serializable;

public class InvokeCommandWinRMOperation extends WinRMOperation implements Serializable {

    private final String command;
    private final static String REMOTE_INVOKE_COMMAND_PATH = "/com/spcow/plugins/winrmclient/InvokeCommandWinRMOperation/Remote-Invoke-Command.ps1";

    @DataBoundConstructor
    public InvokeCommandWinRMOperation(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener,
                                String hostName, String userName, String password) {
        boolean result = false;
        try {
            StringBuilder sbCommand = new StringBuilder();
            sbCommand.append("try");
            sbCommand.append(System.lineSeparator());
            sbCommand.append("{");
            sbCommand.append(System.lineSeparator());
            sbCommand.append(command);
            sbCommand.append(System.lineSeparator());
            sbCommand.append("}");
            sbCommand.append(System.lineSeparator());
            sbCommand.append("catch");
            sbCommand.append(System.lineSeparator());
            sbCommand.append("{");
            sbCommand.append(System.lineSeparator());
            sbCommand.append("throw");
            sbCommand.append(System.lineSeparator());
            sbCommand.append("}");
            final String strCommand = sbCommand.toString();
            final CommandInterpreter ciUserCommand = new CommandInterpreter(strCommand) {
                @Override
                public String[] buildCommandLine(FilePath filePath) {
                    return new String[0];
                }

                @Override
                protected String getContents() {
                    return strCommand;
                }

                @Override
                protected String getFileExtension() {
                    return Utils.getFileExtension();
                }
            };
            FilePath ciUserCommandScriptFile = ciUserCommand.createScriptFile(buildWorkspace);
            StreamSource ssRemoteInvokeCommand = new StreamSource(WinRMClientBuilder.class.getResourceAsStream(REMOTE_INVOKE_COMMAND_PATH));
            final String strRemoteInvokeCommand = Utils.getStringFromInputStream(ssRemoteInvokeCommand.getInputStream());
            CommandInterpreter ciRemoteInvokeCommandScript = new CommandInterpreter(strRemoteInvokeCommand) {
                @Override
                public String[] buildCommandLine(FilePath filePath) {
                    return new String[0];
                }

                @Override
                protected String getContents() {
                    return strRemoteInvokeCommand;
                }

                @Override
                protected String getFileExtension() {
                    return Utils.getFileExtension();
                }
            };
            FilePath fpRemoteInvokeCommandScriptFile = ciRemoteInvokeCommandScript.createScriptFile(buildWorkspace);
            StringBuilder sb = new StringBuilder();
            sb.append(". " + fpRemoteInvokeCommandScriptFile.getRemote());
            sb.append(System.lineSeparator());
            sb.append("Remote-Invoke-Command");
            sb.append(" ");
            sb.append("\"" + ciUserCommandScriptFile.getRemote() + "\"");
            sb.append(" ");
            sb.append("\"" + hostName + "\"");
            sb.append(" ");
            sb.append("\"" + userName + "\"");
            sb.append(" ");
            sb.append("\"" + password + "\"");
            CommandInterpreter remoteCommandInterpreter = new CommandInterpreter(sb.toString()) {
                @Override
                public String[] buildCommandLine(FilePath script) {
                    return Utils.buildCommandLine(script);
                }

                @Override
                protected String getContents() {
                    return Utils.getContents(command);
                }

                @Override
                protected String getFileExtension() {
                    return Utils.getFileExtension();
                }

            };
            FilePath scriptFile = remoteCommandInterpreter.createScriptFile(buildWorkspace);
            int exitStatus = launcher.launch().cmds(remoteCommandInterpreter.buildCommandLine(scriptFile)).stdout(listener).join();
            scriptFile.delete();
            ciUserCommandScriptFile.delete();
            fpRemoteInvokeCommandScriptFile.delete();
            result = didErrorsOccur(exitStatus);
        } catch (RuntimeException  e) {
            listener.fatalError(e.getMessage());
        } catch (InterruptedException e) {
            listener.fatalError(e.getMessage());
        } catch (IOException e) {
            listener.fatalError(e.getMessage());
        }
        return result;
    }

    private boolean didErrorsOccur(int exitStatus) {
        boolean result = true;
        if (exitStatus != 0) {
            result = false;
        }
        return result;
    }

    @Extension
    @Symbol("invokeCommand")
    public static class DescriptorImpl extends WinRMOperationDescriptor {
        public String getDisplayName() {
            return "Invoke-Command";
        }

    }
}
