package com.spcow.plugins.winrmclient;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Strings;
import hudson.*;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WinRMClientBuilder extends Builder implements SimpleBuildStep {

    private final String hostName;
    private final
    @CheckForNull
    String credentialsId;

    private final List<WinRMOperation> winRMOperations;

    @DataBoundConstructor
    public WinRMClientBuilder(String hostName, String credentialsId, List<WinRMOperation> winRMOperations) {
        this.hostName = hostName;
        this.credentialsId = credentialsId;
        this.winRMOperations = winRMOperations;
    }

    public String getHostName() {
        return hostName;
    }

    public
    @Nullable
    String getCredentialsId() {
        return credentialsId;
    }

    public List<WinRMOperation> getWinRMOperations() {
        return Collections.unmodifiableList(winRMOperations);
    }

    public StandardUsernameCredentials getCredentials(Item project) {
        StandardUsernameCredentials credentials = null;
        try {

            credentials = credentialsId == null ? null : this.lookupSystemCredentials(credentialsId, project);
            if (credentials != null) {
                return credentials;
            }
        } catch (Throwable t) {

        }

        return credentials;
    }

    public static StandardUsernameCredentials lookupSystemCredentials(String credentialsId, Item project) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider
                        .lookupCredentials(StandardUsernameCredentials.class, project, ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId)
        );
    }

    public String getUsername(EnvVars environment, Item project) {
        String Username = "";
        if (!Strings.isNullOrEmpty(credentialsId)) {
            Username = this.getCredentials(project).getUsername();
        }
        return Username;
    }

    public String getPassword(EnvVars environment, Item project) {
        String Password = "";
        if (!Strings.isNullOrEmpty(credentialsId)) {
            Password = Secret.toString(StandardUsernamePasswordCredentials.class.cast(this.getCredentials(project)).getPassword());
        }
        return Password;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        EnvVars envVars = build.getEnvironment(listener);
        Item project = build.getParent();

        boolean result = false;
        if (winRMOperations.size() > 0) {
            for (WinRMOperation item : winRMOperations) {
                result = item.runOperation(build, workspace, launcher, listener, this.hostName,
                        this.getUsername(envVars, project), this.getPassword(envVars, project));
                if (!result) break;
            }
        } else {
            listener.getLogger().println("No WinRM Operation added.");
            result = true;
        }
        if (!result) {
            throw new AbortException("WinRM Operations failed.");
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    @Symbol("winRMClient")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a host name");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "WinRM Client";
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item owner) {
            if (owner == null || !owner.hasPermission(Item.CONFIGURE)) {
                return new ListBoxModel();
            }
            return new StandardUsernameListBoxModel().withEmptySelection()
                    .withAll(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, owner,
                            ACL.SYSTEM, Collections.<DomainRequirement>emptyList()));
        }

        @SuppressWarnings("unused")
        public List<WinRMOperationDescriptor> getWinRMOperationDescriptors() {
            List<WinRMOperationDescriptor> result = new ArrayList<WinRMOperationDescriptor>();
            Jenkins j = Jenkins.getInstance();
            if (j == null) {
                return result;
            }
            for (Descriptor<WinRMOperation> d : j.getDescriptorList(WinRMOperation.class)) {
                if (d instanceof WinRMOperationDescriptor) {
                    WinRMOperationDescriptor descriptor = (WinRMOperationDescriptor) d;
                    result.add(descriptor);
                }
            }
            return result;
        }
    }
}

