<!--
  Copyright (c) 2019-2024 The MathWorks, Inc.
  All Rights Reserved.

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
-->

# Jenkins MathWorks Polyspace Plugin

[![Open in MATLAB Online](https://www.mathworks.com/images/responsive/global/open-in-matlab-online.svg)](https://matlab.mathworks.com/open/github/v1?repo=mathworks/mathworks-polyspace-plugin)

The MathWorks Polyspace Plugin for Jenkins enables automated Polyspace analyses within Jenkins.

## Getting Started 

### Step 1: Configure Plugin
Select **Manage Jenkins** and then **Configure System**. In the **Polyspace** section, enter shorthands for:

* Paths to Polyspace Server installation folder.
* Hostname, port number and protocol for Polyspace Access server.
* Hostname and port number for Polyspace Metrics server.

You later use these shorthands in Jenkins projects.
Also, specify the e-mail server (if not already specified) in the **E-mail Notification** section.

### Step 2: Configure Jenkins Project

Create a new project. In the **Build Environment** section of the project, choose **Select Polyspace installation settings**. Then:
1. Select the global shorthands you defined earlier for the Polyspace installation folder, Polyspace Metrics server and/or Polyspace Access server. 
2. Select an username and encrypted password for Polyspace Access. You can also add new users with the **Add** button. The username and password gets stored in the Credentials plugin. You can edit or delete users (or add new users) directly in the Credentials plugin. 

**Note**: Obtain an encrypted form of a Polyspace Access password using `polyspace-access -encrypt-password`. See [polyspace-access](https://www.mathworks.com/help/bugfinder/ref/polyspaceaccess.html).

### Step 3: Enter Build Scripts for Polyspace Analysis

In the **Build** section of the project, select **Execute shell** or **Execute Windows batch command**. Enter a script that uses the Polyspace executables:

* [polyspace-bug-finder-server](https://www.mathworks.com/help/bugfinder/ref/polyspacebugfinderservercommand.html) or [polyspace-code-prover-server](https://www.mathworks.com/help/codeprover/ref/polyspacecodeproverservercommand.html) to run Polyspace Bug Finder or Polyspace Code Prover.
* [polyspace-access](https://www.mathworks.com/help/bugfinder/ref/polyspaceaccess.html) to upload analysis results to the Polyspace Access server.
* polyspace-results-repository to upload analysis results to the Polyspace Metrics server.

For other Polyspace commands, see the documentation of
[Polyspace Bug Finder Server](https://www.mathworks.com/help/bugfinder/) or
[Polyspace Code Prover Server](https://www.mathworks.com/help/codeprover/).
See example scripts below.

You can also use the helper utilities available with the Jenkins plugin:

* `ps_helper_access` to use the `polyspace-access` command without specifying hostname, port number and user credentials.
* `ps_helper_metrics` to use the `polyspace-results-repository` command without specifying hostname and port number.
* `ps_helper` to filter results based on specified criteria, generate a pass/fail status for the build based on number of findings and print a project id and run number for the current upload.

For the syntax of these helper utilities, click the **?** icon next to the Polyspace fields in the **Build Environment** section of the project.

### Step 4: Configure Post-Analysis E-mail Notification

In the **Post-build Actions** section of the project, select **Polyspace Notification**. You can choose to do one of the following:
* Send a common e-mail notification to one or more recipients. The e-mail can contain a summary of the analysis such as number of findings. You can also send in attachment a text file with the findings in the project (with links to individual findings in Polyspace Access).
* Send different e-mails to individual recipients based on file or component ownership or some other criteria. You can filter a text file with the results using the `ps_helper` utility and assign the filtered results to specific owners. You can then send text files with these filtered results in e-mail attachments to the owners.

For more information, click the **?** icon next to the Polyspace fields in the **Post-build Actions** section of the project.

## Examples

Script examples can be found at
https://github.com/mathworks/mathworks-polyspace-plugin/tree/master/examples.
