# Copyright (c) 2019 The MathWorks, Inc.
# All Rights Reserved.
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

#
# Example of polyspace access usage
# Please refer to https://github.com/mathworks/mathworks-polyspace-plugin in order to configure the
# jenkins plugin


#
# Example of Polyspace Jenkins Script
# Same behavior than polyspace-access-01, with
#     - specific notifications are generated to developers:
#       - userA receives findings which are Programming that have impact High or Medium
#       - userB receives findings which belongs to function bug_memstdlib()
#       - userC receives findings which belongs to function DoesNotExist().
#       As there is no such findings, userC will not receive any notification
#     - and Jenkins status is "UNSTABLE" when there are more than 10 Red Defects
#
# Different steps are used
# 1- Sniff the project
# 2- Run Bug Finder or Code Prover
# 3- Upload results on Polyspace Access
# 4- Export report and filter only the defects "High"
# 5- Filter Reports
# 6- Filter Reports for userA, userB and UserC
# 7- Update Jenkins status
#

set -e
export RESULT=ResultBF
export PROG=Bug_Finder_Example
export PARENT_PROJECT_ON_ACCESS=/public/access/access-02
rm -rf Notification && mkdir -p Notification



#
# 1- Sniff the project
#    Pre-requite: the sources must be checkout in the workspace
#                 this can be performed using the jenkins git plugin: https://github.com/jenkinsci/git-plugin
#
build_cmd="gcc -c sources/*.c"
polyspace-configure \
      -allow-overwrite \
      -allow-build-error \
      -prog $PROG \
      -author jenkins \
      -output-options-file $PROG.psopts \
      $build_cmd

#
# 2- Run Bug Finder
#
polyspace-bug-finder-server -options-file $PROG.psopts -results-dir $RESULT

#
# 3- Upload results on Polyspace Access
#
$ps_helper_access -create-project $PARENT_PROJECT_ON_ACCESS
$ps_helper_access \
      -upload $RESULT \
      -parent-project $PARENT_PROJECT_ON_ACCESS \
      -project $PROG

#
# 4- Export report and filter only the defects "High"
#
$ps_helper_access \
      -export $PARENT_PROJECT_ON_ACCESS/$PROG \
      -o Notification/Report_Everything.tsv \
      -defects High
  
#
# 5- Filter Reports
# In this example, we are interested in all Red Defects
#
$ps_helper -report-filter                         \
    Notification/Report_Everything.tsv            \
    Notification/Results_All.tsv                  \
    Family "Defect"                               \
    Color "Red"

#
# 6- Filter Reports for userA, userB and UserC
#       - userA receives findings which are Programming that have impact High or Medium
#       - userB receives findings which belongs to function bug_memstdlib()
#       - userC receives findings which belongs to function DoesNotExist().
#
$ps_helper -report-filter                         \
    Notification/Report_Everything.tsv            \
    Notification/Results_Users.tsv                \
    userA                                         \
    Group "Programming"                           \
    Information "Impact: High"
$ps_helper -report-filter                         \
    Notification/Report_Everything.tsv            \
    Notification/Results_Users.tsv                \
    userA                                         \
    Group "Programming"                           \
    Information "Impact: Medium"
$ps_helper -report-filter                         \
    Notification/Report_Everything.tsv            \
    Notification/Results_Users.tsv                \
    userB                                         \
    Function "bug_memstdlib()"
$ps_helper -report-filter                         \
    Notification/Report_Everything.tsv            \
    Notification/Results_Users.tsv                \
    userC                                         \
    Function "DoesNotExist()"

#
# 7- Update Jenkins status
#    Jenkins status is "UNSTABLE" when there are more than 10 Red Defects
#
BUILD_STATUS=$($ps_helper -report-status Notification/Results_All.tsv 10)

#
# Exit with correct build status
#
[ "$BUILD_STATUS" != "SUCCESS" ] && exit 129
exit 0

#
# Execute shell <advanced><Exit code to set build unstable> = 129
#

#
# Performed by the post-build action, when Polyspace Notification post-build action
# is parametrized as:
#
#   Send to recipients [Checked]
#     E-mail usernames:      Users to receive the notification, separated by commas
#     Attachment filename:   Notification/Results_All.tsv
#     Mail subject filename: <empty>
#     Mail body filename:    <empty>
#
#   Send different e-mails to individual recipients [Checked]
#     Attachment file base name:   Notification/Results_Users.tsv
#     Mail subject file base name: <empty>
#     Mail body file base name:    <empty>
#

