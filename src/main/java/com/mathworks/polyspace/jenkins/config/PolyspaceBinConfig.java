// Copyright (c) 2019 The MathWorks, Inc.
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

import com.mathworks.polyspace.jenkins.PolyspaceHelpers;

import org.kohsuke.stapler.*;
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
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        List<String> Command = new ArrayList<String>();
        Command.add(polyspacePath + File.separator + "polyspace" + PolyspaceHelpers.exeSuffix());
        Command.add("-h");
        if (PolyspaceHelpers.checkPolyspaceCommand(Command)) {
          return FormValidation.ok(Messages.polyspaceCorrectConfig());
        } else {
          return FormValidation.error(Messages.polyspaceBinWrongConfig());
        }
      }
    }

}
