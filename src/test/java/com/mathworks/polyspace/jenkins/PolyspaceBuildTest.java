// Copyright (c) 2019-2021 The MathWorks, Inc.
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

package com.mathworks.polyspace.jenkins.test;

import com.mathworks.polyspace.jenkins.config.*;
import com.mathworks.polyspace.jenkins.*;
import hudson.Functions;
import hudson.model.*;
import hudson.tasks.*;
import hudson.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.jvnet.hudson.test.*;
import java.io.File;

public class PolyspaceBuildTest {

  @Rule
  public JenkinsRule rule = new JenkinsRule();

  /* the wrapper to be used by all tests */
  public PolyspaceBuildWrapper wrapper;
  
  private File ROOT_TMP_FOLDER;
  private String ROOT_TMP = System.getProperty("java.io.tmpdir") + File.separator + "jenkins-plugin-unittests";
  private String BIN_NOT_FOUND = ROOT_TMP + File.separator +  "binNotFound";
  private String BIN_NOT_VALID = ROOT_TMP + File.separator +  "NotValid/polyspace/bin";
  private String FAKE_BIN_FOLDER = ROOT_TMP + File.separator +  "NoValidBinary/polyspace/bin";
  private String FAKE_POLYSPACE = FAKE_BIN_FOLDER + File.separator +  "polyspace-bug-finder";

  /* Create configuration and wrapper common to all tests */
  @Before
  public void initialize() throws Exception {
    // Set some binaries
    PolyspaceBinConfig bin1 = new PolyspaceBinConfig();
    bin1.setName("bin1");
    bin1.setPolyspacePath("path_to_bin1");
    PolyspaceBinConfig bin2 = new PolyspaceBinConfig();
    bin2.setName("bin2");
    bin2.setPolyspacePath("path_to_bin2");
    
    // Set some Metrics
    PolyspaceMetricsConfig metrics1 = new PolyspaceMetricsConfig();
    metrics1.setPolyspaceMetricsName("metrics1");
    metrics1.setPolyspaceMetricsHost("metrics1.com");
    metrics1.setPolyspaceMetricsPort("22427");
    PolyspaceMetricsConfig metrics2 = new PolyspaceMetricsConfig();
    metrics2.setPolyspaceMetricsName("metrics2");
    metrics2.setPolyspaceMetricsHost("metrics2.com");
    metrics2.setPolyspaceMetricsPort("32427");

    // Set some Accesses
    PolyspaceAccessConfig access1 = new PolyspaceAccessConfig();
    access1.setPolyspaceAccessName("access1");
    access1.setPolyspaceAccessHost("access1.com");
    access1.setPolyspaceAccessProtocol("https");
    access1.setPolyspaceAccessPort("19443");
    PolyspaceAccessConfig access2 = new PolyspaceAccessConfig();
    access2.setPolyspaceAccessName("access2");
    access2.setPolyspaceAccessHost("access2.com");
    access2.setPolyspaceAccessProtocol("http");
    access2.setPolyspaceAccessPort("19444");

    // Add all bins as possible bin in the wrapper
    wrapper = new PolyspaceBuildWrapper();
    wrapper.getDescriptor().addPolyspaceBinConfig(bin1);
    wrapper.getDescriptor().addPolyspaceBinConfig(bin2);
    wrapper.getDescriptor().addPolyspaceMetricsConfig(metrics1);
    wrapper.getDescriptor().addPolyspaceMetricsConfig(metrics2);
    wrapper.getDescriptor().addPolyspaceAccessConfig(access1);
    wrapper.getDescriptor().addPolyspaceAccessConfig(access2);

    // Create some folders and files to be checked
    ROOT_TMP_FOLDER = new File(ROOT_TMP);
    ROOT_TMP_FOLDER.mkdirs();
    new File(BIN_NOT_VALID).mkdirs();
    new File(FAKE_BIN_FOLDER).mkdirs();
    new File(FAKE_POLYSPACE).createNewFile();
  }

  @After
  public void finalize() {
    ROOT_TMP_FOLDER.delete();
  }

  // Check the drop down list, that can be chosen in "Build Environment"
  @Test
  public void testDropDown() throws Exception {
    final ListBoxModel bin = wrapper.getDescriptor().doFillBinConfigItems();
    assertEquals(2, bin.size());
    assertEquals("bin1", bin.get(0).name);
    assertEquals("bin1", bin.get(0).value);
    assertEquals("bin2", bin.get(1).name);
    assertEquals("bin2", bin.get(1).value);
    
    final ListBoxModel metrics = wrapper.getDescriptor().doFillMetricsConfigItems();
    assertEquals(3, metrics.size());
    assertEquals("<unset>", metrics.get(0).name);
    assertEquals("<unset>", metrics.get(0).value);
    assertEquals("metrics1", metrics.get(1).name);
    assertEquals("metrics1", metrics.get(1).value);
    assertEquals("metrics2", metrics.get(2).name);
    assertEquals("metrics2", metrics.get(2).value);
    
    final ListBoxModel access = wrapper.getDescriptor().doFillServerConfigItems();
    assertEquals(3, access.size());
    assertEquals("<unset>", access.get(0).name);
    assertEquals("<unset>", access.get(0).value);
    assertEquals("access1", access.get(1).name);
    assertEquals("access1", access.get(1).value);
    assertEquals("access2", access.get(2).name);
    assertEquals("access2", access.get(2).value);    
  }

  /* all environment variables */
  public static String allEnvVariables[] = {
    "PATH",
    
    "ps_helper_metrics_upload",
    "POLYSPACE_METRICS_HOST",
    "POLYSPACE_METRICS_PORT",
    "POLYSPACE_METRICS_URL",
    
    "ps_helper_access",
    "POLYSPACE_ACCESS_PROTOCOL",
    "POLYSPACE_ACCESS_HOST",
    "POLYSPACE_ACCESS_PORT",
    "POLYSPACE_ACCESS_URL",
  };

  public String getCommandAllEnvVariables() {
    Boolean isWindows = Functions.isWindows();
    String command = "";
    for (String var : allEnvVariables) {
      command += "echo " + var + " = ";
      if (isWindows) {
        command += "%" + var + "%\n";
      } else {
        command += "$" + var + "\n";
      }
    }
    return command;
  }
  
  public void checkBinUnset(FreeStyleBuild build) throws Exception {
    rule.assertLogNotContains("path_to_bin1", build);
    rule.assertLogNotContains("path_to_bin2", build);
  }

  public void checkMetricsUnset(FreeStyleBuild build) throws Exception {
    rule.assertLogContains("ps_helper_metrics_upload = ps_helper_metrics_upload IS UNSET", build);
    rule.assertLogContains("POLYSPACE_METRICS_HOST = POLYSPACE_METRICS_HOST IS UNSET", build);
    rule.assertLogContains("POLYSPACE_METRICS_PORT = POLYSPACE_METRICS_PORT IS UNSET", build);
    rule.assertLogContains("POLYSPACE_METRICS_URL = POLYSPACE_METRICS_URL IS UNSET", build);
  }

  public void checkMetricsSet(FreeStyleBuild build, String ps_helper_metrics_upload, String host, String port, String url) throws Exception {
    rule.assertLogContains("ps_helper_metrics_upload = " + ps_helper_metrics_upload, build);
    rule.assertLogContains("POLYSPACE_METRICS_HOST = " + host, build);
    rule.assertLogContains("POLYSPACE_METRICS_PORT = " + port, build);
    rule.assertLogContains("POLYSPACE_METRICS_URL = " + url, build);
  }

  public void checkAccessUnset(FreeStyleBuild build) throws Exception {
    rule.assertLogContains("ps_helper_access = ps_helper_access IS UNSET", build);
    rule.assertLogContains("POLYSPACE_ACCESS_PROTOCOL = POLYSPACE_ACCESS_PROTOCOL IS UNSET", build);
    rule.assertLogContains("POLYSPACE_ACCESS_HOST = POLYSPACE_ACCESS_HOST IS UNSET", build);
    rule.assertLogContains("POLYSPACE_ACCESS_PORT = POLYSPACE_ACCESS_PORT IS UNSET", build);
    rule.assertLogContains("POLYSPACE_ACCESS_URL = POLYSPACE_ACCESS_URL IS UNSET", build);
  }

  public void checkAccessSet(FreeStyleBuild build, String ps_helper_access, String protocol, String host, String port, String url) throws Exception {
    rule.assertLogContains("ps_helper_access = " + ps_helper_access, build);
    rule.assertLogContains("POLYSPACE_ACCESS_PROTOCOL = " + protocol, build);
    rule.assertLogContains("POLYSPACE_ACCESS_HOST = " + host, build);
    rule.assertLogContains("POLYSPACE_ACCESS_PORT = " + port, build);
    rule.assertLogContains("POLYSPACE_ACCESS_URL = " + url, build);
  }
  
  public FreeStyleBuild runBatchCommand(final String command) throws Exception {
    // Create a new freestyle project with a unique name, with an "Execute shell" build step;
    FreeStyleProject project = rule.createFreeStyleProject();
    project.getBuildWrappersList().add(wrapper);

    // Add the commands to run in the freestyle project.
    // if running on Windows, this will be an "Execute Windows batch command" build step
    Builder step = Functions.isWindows() ? new BatchFile(command) : new Shell(command);
    project.getBuildersList().add(step);

    // Enqueue a build of the project, wait for it to complete, and assert success
    return rule.buildAndAssertSuccess(project);
  }

  // Check everything is ok when there is no configuration checked
  @Test
  public void testNoConfig() throws Exception {
    String command = getCommandAllEnvVariables();
    FreeStyleBuild build = runBatchCommand(command);
    
    // Assert that the console log contains the output we expect
    checkBinUnset(build);
    checkMetricsUnset(build);
  }

  // Check "PATH" contains the selected bin path, that is "path_to_bin2"
  @Test
  public void testBin() throws Exception {
    wrapper.setBinConfig("bin2");
    
    String command = getCommandAllEnvVariables();
    FreeStyleBuild build = runBatchCommand(command);

    // Assert that the console log contains the output we expect
    rule.assertLogContains("path_to_bin2", build);
    rule.assertLogNotContains("path_to_bin1", build);
    checkMetricsUnset(build);
    checkAccessUnset(build);
  }

  // Check "Metrics Variables" are OK
  @Test
  public void testMetrics() throws Exception {
    wrapper.setMetricsConfig("metrics2");
    
    String command = getCommandAllEnvVariables();
    FreeStyleBuild build = runBatchCommand(command);

    // Assert that the console log contains the output we expect
    checkBinUnset(build);
    checkMetricsSet(build, 
      "polyspace-results-repository -f -upload -server metrics2.com:32427",
      "metrics2.com",
      "32427",
      "metrics2.com");    // the url does not contain the port as the port is used to upload only
    checkAccessUnset(build);
  }
  
  // Check "Access Variables" are OK
  @Test
  public void testAccess() throws Exception {
    wrapper.setServerConfig("access2");
    
    String command = getCommandAllEnvVariables();
    FreeStyleBuild build = runBatchCommand(command);

    // Assert that the console log contains the output we expect
    checkBinUnset(build);
    checkMetricsUnset(build);
    checkAccessSet(build, 
      "ps_helper_access IS UNSET",        // because there is no credentials
      "http",
      "access2.com",
      "19444",
      "http://access2.com:19444");
  }

  // Check test functions for Metrics
  @Test
  public void testCheckMetrics() throws Exception {
    PolyspaceMetricsConfig.DescriptorImpl desc_metrics = new PolyspaceMetricsConfig.DescriptorImpl();

    FormValidation port_metrics_ok = desc_metrics.doCheckPolyspaceMetricsPort("1234");
    assertEquals(FormValidation.Kind.OK, port_metrics_ok.kind);

    FormValidation port_metrics_ko = desc_metrics.doCheckPolyspaceMetricsPort("wrong-port");
    assertEquals(FormValidation.Kind.ERROR, port_metrics_ko.kind);
    assertEquals(com.mathworks.polyspace.jenkins.config.Messages.portMustBeANumber(), port_metrics_ko.renderHtml());
  }

  // Check test functions for Access
  @Test
  public void testCheckAccess() throws Exception {
    PolyspaceAccessConfig.DescriptorImpl desc_access = new PolyspaceAccessConfig.DescriptorImpl();

    FormValidation port_access_ok = desc_access.doCheckPolyspaceAccessPort("1234");
    assertEquals(FormValidation.Kind.OK, port_access_ok.kind);

    FormValidation port_access_ko = desc_access.doCheckPolyspaceAccessPort("wrong-port");
    assertEquals(FormValidation.Kind.ERROR, port_access_ko.kind);
    assertEquals(com.mathworks.polyspace.jenkins.config.Messages.portMustBeANumber(), port_access_ko.renderHtml());
    
    FormValidation protocol_access_http  = desc_access.doCheckPolyspaceAccessProtocol("http");
    assertEquals(FormValidation.Kind.OK, protocol_access_http.kind);

    FormValidation protocol_access_https = desc_access.doCheckPolyspaceAccessProtocol("https");
    assertEquals(FormValidation.Kind.OK, protocol_access_https.kind);

    FormValidation protocol_access_ko    = desc_access.doCheckPolyspaceAccessProtocol("wrong_protocol");
    assertEquals(FormValidation.Kind.ERROR, protocol_access_ko.kind);
    assertEquals(com.mathworks.polyspace.jenkins.config.Messages.wrongProtocol(), protocol_access_ko.renderHtml());
  }

  // Check test functions that check parameters
  @Test
  public void testCheckPolyspaceBin() throws Exception {
    PolyspaceBinConfig.DescriptorImpl desc_bin = new PolyspaceBinConfig.DescriptorImpl();

    FormValidation path_bin_not_found = desc_bin.doCheckPolyspacePath(BIN_NOT_FOUND); // Folder was not created
    assertEquals(FormValidation.Kind.WARNING, path_bin_not_found.kind);
    assertEquals(com.mathworks.polyspace.jenkins.config.Messages.polyspaceBinNotFound(), path_bin_not_found.renderHtml());

    FormValidation path_bin_not_valid = desc_bin.doCheckPolyspacePath(BIN_NOT_VALID); // Folder exists it but it does not contain a polyspace binary
    assertEquals(FormValidation.Kind.WARNING, path_bin_not_valid.kind);
    assertEquals(com.mathworks.polyspace.jenkins.config.Messages.polyspaceBinNotValid(), path_bin_not_valid.renderHtml());

    FormValidation path_bin_wrong_config = desc_bin.doCheckPolyspacePath(FAKE_BIN_FOLDER); // Folder exists but contains a wrong polyspace binary
    assertEquals(FormValidation.Kind.ERROR, path_bin_wrong_config.kind);
    assertEquals(com.mathworks.polyspace.jenkins.config.Messages.polyspaceBinWrongConfig() + " &#039;" + FAKE_POLYSPACE + " -h&#039;", path_bin_wrong_config.renderHtml());
  }
}
