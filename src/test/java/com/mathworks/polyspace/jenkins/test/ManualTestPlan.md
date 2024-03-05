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

# Tests setup

## Install plugin
  - Login if necessary.
  - Go to "Dashboard > Manage Jenkins > Manage Plugins".
  - Go to the "Advanced" tab.
  - In the "Upload Plugin" section, click on "Choose File".
  - Select "<sandbox>/mathworks-polyspace-plugin/target/mathworks-polyspace.hpi.
  - Click on "Upload".
  - The screen goes to "Installing Plugins/Upgrades": select the "Restart Jenkins when..." checkbox.
  - Click the reload button of your browser and wait until Jenkins has restarted.

## Create Jenkins credentials for an Access user
  - Login if necessary.
  - Go to "Dashboard > Manage Jenkins > Manage Credentials".
  - Click on the "Jenkins" store.
  - Click on the "Global credentials (unrestricted)" domain.
  - On the left menu, click "Add Credentials" to add a new Access user/encrypted-password in Jenkins.
  - Note that you must give an encrypted password, generated with "polyspace-access -encrypt-password".
  - Click "OK" when done.

# Test Polypace global configuration
  - Run tests on Windows.
  - Run tests on Linux.
  - Open a new Jenkins tab in your browser. Keep both tabs side by side on your screen.

## Nominal use-case
  - On the LEFT tab, set up the Polyspace configuration:
    - Go to "Dashboard > Manage Jenkins > Configure System".
    - Scroll down to the Polyspace section.
    - Click on "Add Polyspace Installation Folder":
      - Give it some name.
      - In the "Path ending with polyspace/bin" box, enter an existing folder containing a working polyspace-bug-finder[-server].exe binary. Check the message:
        - Message type = OK
        - Message content = Correct Configuration.
      - In the "Path ending with polyspace/bin" box, enter an existing folder containing a working polyspace-bug-finder-nodesktop.exe binary for pre-19a versions. Check the message:
        - Message type = OK
        - Message content = Correct Configuration.
      - Click on "Apply" to save settings.
    - Click on "Add Polyspace Access server":
      - Fill name (a label to be displayed in Jenkins), protocol (http or https), host name and port number with working configuration.
      - Click on "Apply" to save settings.
  - In the RIGHT tab, create a new job:
    - Go to "Dashboard > New Item": enter some name, select "Freestyle project" and click OK.
    - Scroll down to the "Build Environment" section and select the "Select Polyspace installation settings" checkbox.
    - In the "Polyspace installation folder" section, select the "Polyspace Installation Folder" configuration you have created previously.
    - In the "Polyspace Access server" section, select the "Polyspace Access server" configuration you have created previously.
    - In the "Polyspace Access username and encrypted password" section, select the Access user you have created previously.
    - Click on "Check server settings" in the "Polyspace Access server" section. Assuming the provided configuration is working, check the message:
      - Waiting message: "Check server settings..." (this takes a while when the configuration is correct).
      - Message type = OK
      - Message content = Correct Configuration.

## Unknown Polyspace installation folder
  - In the LEFT tab, modify the "Polyspace installation folder": 
    - In the "Path ending with polyspace/bin" box, enter an unknown folder.
    - Click on TAB for the change to be taken into account.
    - Check the message:
      - Message type = WARNING
      - Message content = Specified polyspace/bin folder path cannot be found on Jenkins server. If you run a distributed build, check that the bin folder exists on the worker machine.
      - Click on "Apply" to save settings.
  - In the RIGHT tab:
    - Click on "Check server settings" in the "Polyspace Access server" section.
    - Check the message: same as above.

## Existing folder not containing Polyspace
  - Repeat the same workflow and check the returned message on both LEFT and RIGHT tabs:
    - Check the message for an existing folder not containing any polyspace-bug-finder[-server].exe binary
      - Message type = WARNING
      - Message content = Specified polyspace/bin folder path does not point to a valid Polyspace product. If you run a distributed build, check that the bin folder on the worker machine points to a valid Polyspace product.
      - Click on "Apply" to save settings.

## Fake or broken Polyspace binary
  - Repeat the same workflow and check the returned message on both LEFT and RIGHT tabs:
    - Check the message for an existing folder containing a fake polyspace-bug-finder[-server].exe binary.
      - Message type = ERROR
      - Message content = Command used to test Polyspace binary failed: 'C:\your\test\path\polyspace\bin\polyspace-bug-finder.exe -h'
      - Click on "Apply" to save settings.

## Wrong protocol
  - Repeat the same workflow and check the returned message on both LEFT and RIGHT tabs:
    - Check the message for any wrong "Polyspace Access server" configuration:
      - Unexpected protocol:
        - Message type = ERROR
        - Message content = "Protocol must be http or https."
      - Wrong protocol (http instead of https or vice-versa), unknown hostname, wrong port, wrong Access version (Access server version does not match "Polyspace Installation Folder" version):
        - Message type = ERROR
        - Message content = "Could not connect to Polyspace Access server. For more information run this command: '\\your\path\to\matlab\polyspace\bin\polyspace-access.exe -login \<user\> -encrypted-password \<encrypted-password\> -protocol \<protocol\> -host \<hostname\> -port \<port\> -list-project"

# Test Polyspace notification

  - Run test job:
    - Windows controller with Windows agent.
    - Windows controller with Linux agent.
    - Linux controller with Linux agent.
    - Linux controller with Windows agent.

## TODO nominal use-case
  - Setup Access server in global configuration.
  - Setup Polyspace installation folder in global configuration.
  - Create test job:
    - In "General", check "Restrict where this project can be run":
      - Fill in "Label Expression" = "linux_agent" or "windows_agent".
    - In "Build Environment", check "Select Polyspace installation settings".
    - In "Polyspace installation folder", select created folder.
    - In "Polyspace Access server", select created server.
    - In "Polyspace Access username and encrypted password, select created user.
    - In "Build steps", select the "Add build step" dropdown and select "Execute Windows batch command" or "Execute shell"
    - In the created batch/shell step, enter: "path/to/command.bat" or "path/to/script.sh"
      - TODO create Windows command.bat.
      - TODO create Linux script.sh.
    - In "Post-build actions", select the "Add post-build action" dropdown and select "Polyspace Notification".
    - In the created "Polyspace Notification":
      - Check "Send common e-mail" and fill in the fields:
        - E-mail usernames = "your email"
        - Attachment filename = "Notification/Report_New.tsv"
        - Mail subject filename = "Notification/mail_subject.txt"
        - Mail body filename = "Notification/mail_body.txt"
      - Check "Send different e-mails to individual recipients" and fill in the fields:
        - Attachment file base name = "Notification/Results_Users.tsv"
        - Mail subject base filename = "Notification/mail_subject.txt"
        - Mail body base filename = "Notification/mail_body.txt"
        - Unique recipients - Debug only = "your email"
    - Save
  - Run job:
    - Check job completes successfully
    - Check you receive a mail with:
      - Title = "COMMON EMAIL: N new findings for test-job".
      - Attachment = "Report_New.tsv" containing (TODO).
      - Body = "TODO".
    - Check you receive 3 mails with:
      - Title = "INDIVIDUAL EMAIL: N new findings for test-job".
      - Attachment = "Report_Users_userA.tsv" or "Report_Users_userB.tsv" or "Report_Users_userC.tsv" containing (TODO).
      - Body = "TODO".

## TODO Error cases on "Attachment filename"    

  - Attachment filename = "/etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception
  - Attachment filename = "../../../etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception

## TODO Error cases on "Mail subject filename"

  - Mail subject filename = "/etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception
  - Mail subject filename = "../../../etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception

## TODO Error cases on "Mail body filename"

  - Mail body filename = "/etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception
  - Mail body filename = "../../../etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception

## TODO Error cases on "Attachment file base name"

  - Attachment file base name = "/etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception
  - Attachment file base name = "../../../etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception

## TODO Error cases on "Mail subject base filename"

  - Mail subject base filename = "/etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception
  - Mail subject base filename = "../../../etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception

## TODO Error cases on "Mail body base filename"

  - Mail body base filename = "/etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception
  - Mail body base filename = "../../../etc/passwd"
    - Message type = ERROR
    - Message content = TODO
    - Run job => failure with exception

