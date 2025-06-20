// Copyright (c) 2019-2025 The MathWorks, Inc.
// All Rights Reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.mathworks.polyspace.jenkins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.mathworks.polyspace.jenkins.config.Messages;
import com.mathworks.polyspace.jenkins.config.PolyspaceAccessConfig;
import com.mathworks.polyspace.jenkins.config.PolyspaceBinConfig;
import com.mathworks.polyspace.jenkins.constants.PolyspaceConstants;
import com.mathworks.polyspace.jenkins.utils.PolyspaceConfigUtils;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;

public class PolyspaceBuildWrapper extends SimpleBuildWrapper {

    private String serverConfig = null;
    private String polyspaceAccessCredentialId = null;
    private String binConfig = null;

    @DataBoundConstructor
    public PolyspaceBuildWrapper() {
    }

    @DataBoundSetter
    public void setServerConfig(String serverConfig) {
      this.serverConfig = serverConfig;
    }

    @DataBoundSetter
    public void setPolyspaceAccessCredentialId(String polyspaceAccessCredentialId) {
      this.polyspaceAccessCredentialId = polyspaceAccessCredentialId;
    }

    @DataBoundSetter
    public void setBinConfig(String binConfig) {
      this.binConfig = binConfig;
    }

    public String getValue(String value, String default_value) {
      if (!StringUtils.isEmpty(value)) {
        return value;
      }
      return default_value;
    }

    private void update_access(Context context, final PolyspaceAccessConfig server, final String user, final String password) {
      if ((server == null) || (server.getPolyspaceAccessName().equals(getUnsetValue()))) {
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_PROTOCOL, PolyspaceConstants.POLYSPACE_ACCESS_PROTOCOL + " IS UNSET");
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_HOST, PolyspaceConstants.POLYSPACE_ACCESS_HOST + " IS UNSET");
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_PORT, PolyspaceConstants.POLYSPACE_ACCESS_PORT + " IS UNSET");
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_URL, PolyspaceConstants.POLYSPACE_ACCESS_URL + " IS UNSET");
        descriptor().setPolyspaceAccessURL("");
        context.env(PolyspaceConstants.POLYSPACE_ACCESS, PolyspaceConstants.POLYSPACE_ACCESS + " IS UNSET");
      } else {
        String protocol = getValue(server.getPolyspaceAccessProtocol(), "https");
        String host = getValue(server.getPolyspaceAccessHost(), "localhost");
        String port = getValue(server.getPolyspaceAccessPort(), "9443");
        String url = protocol + "://" + host + ":" + port;

        context.env(PolyspaceConstants.POLYSPACE_ACCESS_PROTOCOL, protocol);
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_HOST, host);
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_PORT, port);
        context.env(PolyspaceConstants.POLYSPACE_ACCESS_URL, url);
        descriptor().setPolyspaceAccessURL(url);

        if (!(StringUtils.isEmpty(user) || StringUtils.isEmpty(password))) {
          String polypaceAccess = "polyspace-access -tmp-dir tmp-dir";
          polypaceAccess += " -protocol " + protocol + " -host " + host + " -port " + port;
          polypaceAccess += " -login " + user + " -encrypted-password " + password;
          context.env(PolyspaceConstants.POLYSPACE_ACCESS, polypaceAccess);
        } else {
          context.env(PolyspaceConstants.POLYSPACE_ACCESS, "ps_helper_access IS UNSET");
        }
      }
    }

    // Update global Context
    // - PATH
    // - ps_helper
    private void update_global(Context context, final PolyspaceBinConfig bin, final EnvVars initialEnvironment) {
      String path = initialEnvironment.get("PATH");
      String jenkins_home = initialEnvironment.get("JENKINS_HOME");
      if (Functions.isWindows()) {
        path = jenkins_home + "\\jre\\bin" + File.pathSeparator + path;   // Adding Path to java on windows
      }
      if (bin != null) {
        path = bin.getPolyspacePath() + File.pathSeparator + path;        // Adding the path to polyspace
        context.env(PolyspaceConstants.POLYSPACE_BIN, bin.getPolyspacePath());
      } else {
        context.env(PolyspaceConstants.POLYSPACE_BIN, "POLYSPACE_BIN_IS_UNSET");
      }
      context.env("PATH", path);

      String polyspaceJar = jenkins_home + File.separator + "plugins" + File.separator + "mathworks-polyspace" + File.separator + "WEB-INF" + File.separator + "lib" + File.separator + "mathworks-polyspace.jar";
      // the path to the jar may have some space
      if (Functions.isWindows()) {
        polyspaceJar = "\"" + polyspaceJar + "\"";
      } else {
        polyspaceJar = polyspaceJar.replace(" ", "\\ ");
      }
      context.env("ps_helper", "java -cp " +  polyspaceJar + " com.mathworks.polyspace.jenkins.PolyspaceHelpers");
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
      // update context and variables associated with Polyspace Access
      update_access(context, getDescriptor().getServerConfig(serverConfig), getPolyspaceAccessUser(getPolyspaceAccessCredentialId()), getPolyspaceAccessPassword(getPolyspaceAccessCredentialId()));

      // update the path and general helpers
      update_global(context, getDescriptor().getBinConfig(binConfig), initialEnvironment);
    }

    public String getServerConfig() { return serverConfig; }
    public String getPolyspaceAccessCredentialId() { return polyspaceAccessCredentialId; }
    public String getBinConfig() { return binConfig; }

    public final static String getUnsetValue() { return "<unset>"; }

    public static String getPolyspaceAccessUser(String credentialId){
        return retrieveCredentialInfo(true, credentialId);
    }

    public static String getPolyspaceAccessPassword(String credentialId){
        return retrieveCredentialInfo(false, credentialId);
    }

    private static String retrieveCredentialInfo(boolean getUsername, String credentialId){
        if (!StringUtils.isEmpty(credentialId)){
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentialsInItemGroup(
                            StandardCredentials.class, Jenkins.get(), ACL.SYSTEM2, Collections.emptyList()
                    ), CredentialsMatchers.withId(credentialId)
            );

            if (credentials instanceof UsernamePasswordCredentials) {
                UsernamePasswordCredentials c = (UsernamePasswordCredentials)credentials;
                if (getUsername){
                    return c.getUsername();
                } else{
                    return c.getPassword().getPlainText();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public static DescriptorImpl descriptor() {
        final Jenkins jenkins = Jenkins.get();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins instance is not ready");
        }
        return jenkins.getDescriptorByType(PolyspaceBuildWrapper.DescriptorImpl.class);
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        private CopyOnWriteList<PolyspaceAccessConfig> polyspaceAccessConfigs = new CopyOnWriteList<>();
        private CopyOnWriteList<PolyspaceBinConfig> polyspaceBinConfigs = new CopyOnWriteList<>();
        private final PolyspaceConfigUtils polyspaceConfigUtils = new PolyspaceConfigUtils();

        String polyspaceAccessURL;

        String getPolyspaceAccessURL() { return polyspaceAccessURL; }
        void setPolyspaceAccessURL(String polyspaceAccessURL) { this.polyspaceAccessURL = polyspaceAccessURL; }

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        public String getDisplayName() { return Messages.polyspaceBuildWrapperDisplayName(); }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
          polyspaceAccessConfigs.replaceBy(req.bindJSONToList(PolyspaceAccessConfig.class, formData.get("polyspaceAccessConfigs")));
          polyspaceBinConfigs.replaceBy(req.bindJSONToList(PolyspaceBinConfig.class, formData.get("polyspaceBinConfigs")));

          save();
          return super.configure(req,formData);
        }

        // configuration of Polyspace Binaries
        public void addPolyspaceBinConfig(PolyspaceBinConfig value) {
          polyspaceBinConfigs.add(value);
        }
        public PolyspaceBinConfig[] getpolyspaceBinConfigs() {
            return polyspaceBinConfigs.toArray(new PolyspaceBinConfig[0]);
        }
        public PolyspaceBinConfig getBinConfig(String name) {
          if (name == null) {
            return null;
          }
          for (PolyspaceBinConfig config : polyspaceBinConfigs) {
            if ((config.getName() != null) && (config.getName().equals(name)))
              return config;
          }
          return null;
        }

        // configuration of Polyspace Access
        public void addPolyspaceAccessConfig(PolyspaceAccessConfig value) {
          polyspaceAccessConfigs.add(value);
        }
        public PolyspaceAccessConfig[] getpolyspaceAccessConfigs() {
            return polyspaceAccessConfigs.toArray(new PolyspaceAccessConfig[0]);
        }
        public PolyspaceAccessConfig getServerConfig(String name) {
          if (name == null) {
            return null;
          }
          for (PolyspaceAccessConfig config : polyspaceAccessConfigs) {
            if ((config.getPolyspaceAccessName() != null) && (config.getPolyspaceAccessName().equals(name)))
                return config;
          }
          return null;
        }

        public ListBoxModel doFillServerConfigItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(getUnsetValue());
            for (PolyspaceAccessConfig config : polyspaceAccessConfigs) {
                items.add(config.getPolyspaceAccessName());
            }
            return items;
        }

        public ListBoxModel doFillBinConfigItems() {
            ListBoxModel items = new ListBoxModel();
            for (PolyspaceBinConfig config : polyspaceBinConfigs) {
                items.add(config.getName());
            }
            return items;
        }

        public ListBoxModel doFillPolyspaceAccessCredentialIdItems(@AncestorInPath Jenkins context) {
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM2,
                            context,
                            StandardUsernamePasswordCredentials.class,
                            new ArrayList<>(),
                            CredentialsMatchers.anyOf(
                                    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)
                            )
                    );
        }

        public FormValidation doCheckPolyspaceAccess(@AncestorInPath Item item, @QueryParameter String serverConfig, @QueryParameter String polyspaceAccessCredentialId, @QueryParameter String binConfig) throws IOException, InterruptedException
        {
          if (item == null) {
            return FormValidation.error(Messages.internalError());
          }
          item.checkPermission(Item.CONFIGURE);

          final PolyspaceBinConfig bin = getBinConfig(binConfig);

          final PolyspaceAccessConfig server = getServerConfig(serverConfig);
          if (server == null) {
            return FormValidation.warning("Polyspace Access Configuration is not provided");
          }

          String user = getPolyspaceAccessUser(polyspaceAccessCredentialId);
          String password = getPolyspaceAccessPassword(polyspaceAccessCredentialId);
          if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            return FormValidation.error("Missing login / password");
          }

          String protocol = server.getPolyspaceAccessProtocol();
          String host = server.getPolyspaceAccessHost();
          String port = server.getPolyspaceAccessPort();

          return polyspaceConfigUtils.checkPolyspaceAccess(bin.getPolyspacePath(), user, password, protocol, host, port);
        }
    }
}
