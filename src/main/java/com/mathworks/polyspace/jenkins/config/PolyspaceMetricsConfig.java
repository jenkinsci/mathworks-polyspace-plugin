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

package com.mathworks.polyspace.config;

import org.kohsuke.stapler.*;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.io.*;
import java.util.*;
import javax.servlet.ServletException;

public class PolyspaceMetricsConfig extends AbstractDescribableImpl<PolyspaceMetricsConfig> {

    private String polyspaceMetricsName = null;
    private String polyspaceMetricsHost = null;
    private String polyspaceMetricsPort = null;

    @DataBoundConstructor
    public PolyspaceMetricsConfig() { }

    @DataBoundSetter
    public void setPolyspaceMetricsName(String polyspaceMetricsName) {
      this.polyspaceMetricsName = polyspaceMetricsName;
    }

    @DataBoundSetter
    public void setPolyspaceMetricsHost(String polyspaceMetricsHost) {
      this.polyspaceMetricsHost = polyspaceMetricsHost;
    }

    @DataBoundSetter
    public void setPolyspaceMetricsPort(String polyspaceMetricsPort) {
      this.polyspaceMetricsPort = polyspaceMetricsPort;
    }

    public String getPolyspaceMetricsName() {
        return polyspaceMetricsName;
    }
    public String getPolyspaceMetricsHost() {
        return polyspaceMetricsHost;
    }
    public String getPolyspaceMetricsPort() {
        return polyspaceMetricsPort;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<PolyspaceMetricsConfig> {
      public String getDisplayName() { return Messages.polyspaceMetricsConfigDisplayName(); }
      public FormValidation doCheckPolyspaceMetricsPort(@QueryParameter String value)
          throws IOException, ServletException {
            return PolyspaceConfigUtils.doCheckPort(value);
      }
    }
}
