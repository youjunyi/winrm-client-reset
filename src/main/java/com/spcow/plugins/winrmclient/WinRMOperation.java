package com.spcow.plugins.winrmclient;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Run;
import hudson.model.TaskListener;

public abstract class WinRMOperation extends AbstractDescribableImpl<WinRMOperation> {
    public abstract boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener,
                                         String hostName, String userName, String password);
}
