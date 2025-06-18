// Copyright (c) 2019-2023 The MathWorks, Inc.
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

package com.mathworks.polyspace.jenkins.config;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.*;

import com.mathworks.polyspace.jenkins.utils.PolyspaceConfigUtils;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.io.*;
import java.util.*;
import jenkins.model.Jenkins;

public class PolyspaceBinConfig extends AbstractDescribableImpl<PolyspaceBinConfig> {

    private String name;
    private String polyspacePath;

    @DataBoundConstructor
    public PolyspaceBinConfig() { }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setPolyspacePath(String polyspacePath) {
        this.polyspacePath = polyspacePath;
    }

    public String getName() { return name; }
    public String getPolyspacePath() { return polyspacePath; }

    @Extension
    public static class DescriptorImpl extends Descriptor<PolyspaceBinConfig> {
      public String getDisplayName() { return Messages.polyspaceBinConfigDisplayName(); }
      public FormValidation doCheckPolyspacePath(@QueryParameter String polyspacePath) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        String command;
        try {
          PolyspaceConfigUtils.checkPolyspaceBinFolderExists(polyspacePath);
          command = polyspacePath + File.separator + "polyspace-bug-finder" + PolyspaceConfigUtils.exeSuffix();
          try {
            PolyspaceConfigUtils.checkPolyspaceBinCommandExists(command);
          } catch (FormValidation val1) {
            try {
              command = polyspacePath + File.separator + "polyspace-bug-finder-server" + PolyspaceConfigUtils.exeSuffix();
              PolyspaceConfigUtils.checkPolyspaceBinCommandExists(command);
            } catch (FormValidation val2) {
              // Manage pre-19a versions of Polyspace
              command = polyspacePath + File.separator + "polyspace-bug-finder-nodesktop" + PolyspaceConfigUtils.exeSuffix();
              PolyspaceConfigUtils.checkPolyspaceBinCommandExists(command);
            }
          }
        } catch (FormValidation val) {
          return val;
        }
        List<String> polyspace = new ArrayList<>();
        polyspace.add(command);
        polyspace.add("-h");
        String commandString = StringUtils.join(polyspace, ' ');
        if (PolyspaceConfigUtils.checkPolyspaceCommand(polyspace)) {
          return FormValidation.ok(Messages.polyspaceCorrectConfig());
        } else {
          return FormValidation.error(Messages.polyspaceBinWrongConfig() + " '" + commandString + "'");
        }
      }
    }

}
