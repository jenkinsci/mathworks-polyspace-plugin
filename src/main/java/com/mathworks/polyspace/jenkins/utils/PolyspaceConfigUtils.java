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

package com.mathworks.polyspace.jenkins.utils;

import com.mathworks.polyspace.jenkins.config.Messages;

import hudson.tasks.*;      // The mailer
import hudson.util.FormValidation;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public final class PolyspaceConfigUtils {
  public static final String exeSuffix() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return ".exe";
    } else {
      return "";
    }
  }

  public static void checkPolyspaceBinFolderExists(String polyspacePath) throws FormValidation {
    File path = new File(polyspacePath);
    if (!path.isDirectory()) {
      throw FormValidation.warning(Messages.polyspaceBinNotFound());
    }
  }

  public static void checkPolyspaceBinCommandExists(String polyspaceCommand) throws FormValidation {
    File command = new File(polyspaceCommand);
    if (!command.exists()) {
      throw FormValidation.warning(Messages.polyspaceBinNotValid());
    }
  }

  public static FormValidation checkPolyspaceAccess(String polyspacePath, String user, String password, String protocol, String host, String port) {
    String polyspaceCmd = polyspacePath + File.separator + "polyspace-access" + exeSuffix();
    try {
      checkPolyspaceBinFolderExists(polyspacePath);
      checkPolyspaceBinCommandExists(polyspaceCmd);
    } catch (FormValidation validation) {
      return validation;
    }

    List<String> Access = new ArrayList<>();
    Access.add(polyspaceCmd);
    Access.add("-login");
    Access.add(user);
    Access.add("-encrypted-password");
    Access.add(password);

    if (StringUtils.isNotEmpty(protocol)) {
        Access.add("-protocol");
        Access.add(protocol);
    }

    if (StringUtils.isNotEmpty(host)) {
        Access.add("-host");
        Access.add(host);
    }

    if (StringUtils.isNotEmpty(port)) {
        Access.add("-port");
        Access.add(port);
    }

    // Querying -list-project can be very long but it is necessary to launch a real command to check if it returns any error
    Access.add("-list-project");
    String commandString = StringUtils.join(Access, ' ');
    if (checkPolyspaceCommand(Access)) {
      return FormValidation.ok(Messages.polyspaceCorrectConfig());
    } else {
      return FormValidation.error(Messages.polyspaceAccessWrongConfig() + " '" + commandString + "'");
    }
  }

  public static Boolean checkPolyspaceCommand(List<String> Command) {
    boolean testOK = false;

    try
    {
      ProcessBuilder pb = new ProcessBuilder(Command);
      Process p = pb.start();

      // Read output stream and error stream
      final BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream(), Mailer.descriptor().getCharset()));
      final BufferedReader errorBuffer = new BufferedReader(new InputStreamReader(p.getErrorStream(), Mailer.descriptor().getCharset()));
      boolean processRunning = true;
      // String tempo;

      while (processRunning)
      {
        // While process writes on the standard output, read data
        while (inputBuffer.ready())
        {
          /*tempo = */inputBuffer.readLine();
          // For debug only
          // if (tempo != null) System.out.println(tempo);
        }
        // While process writes on the standard error, read data
        while (errorBuffer.ready())
        {
          /*tempo = */errorBuffer.readLine();
          // For debug only
          // if (tempo != null) System.out.println("Error stream: "+tempo);
        }

        try
        {
          // Test if the process is terminated
          int exitValue = p.exitValue();
          // For debug only
          // System.out.println("Exit value: "+exitValue);
          testOK = exitValue == 0;
          processRunning = false;
        }
        catch (IllegalThreadStateException itse)
        {
          // Process not yet terminated, wait for 100 ms and come back in the loop
          Thread.sleep(100);
        }
      }

      // Close opened streams
      inputBuffer.close();
      errorBuffer.close();
    }
    catch (IOException | InterruptedException e)
    {
      e.printStackTrace();
    }

    return testOK;
  }

  public static FormValidation doCheckProtocol(String value) {
    if (!(value.equals("http") || value.equals("https"))) {
      return FormValidation.error(Messages.wrongProtocol());
    }
    return FormValidation.ok();
  }

  public static FormValidation doCheckPort(String value) {
    if (StringUtils.isNumeric(value)) {
      return FormValidation.ok();
    }
    return FormValidation.error(Messages.portMustBeANumber());
  }

  public static FormValidation doCheckFilename(String value) {
    if (!value.isEmpty()) {
      if (value.charAt(0) == '/' || value.charAt(0) == '\\') {
        // Don't allow / or \: only allow a relative directory
        return FormValidation.error(Messages.absoluteDirectoryForbidden());
      } else if (value.contains("..")) {
        // Don't allow ..: don't arbitrary target directories outside $WORKSPACE
        return FormValidation.error(Messages.previousDirectoryForbidden());
      } else {
        return FormValidation.ok();
      }
    } else {
      return FormValidation.ok();
    }
  }
}
