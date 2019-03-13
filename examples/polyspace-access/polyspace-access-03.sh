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
# Same behavior than polyspace-access-02, with
#     - specific specific subject and mail body
#       All: shows a step by step of the algorithm
#
# Different steps are used
# 0- Initialize mail body
# 1- Sniff the project
# 2- Run Bug Finder or Code Prover
# 3- Upload results on Polyspace Access
# 4- Export report
# 5- Filter Reports
# 6- Filter Reports for userA, userB and UserC
# 7- Update Jenkins status
# 8- update mail body and mail subject
#

set -e
export RESULT=ResultBF
export PROG=Bug_Finder_Example
export PARENT_PROJECT_ON_ACCESS=/public/access/access-03
rm -rf Notification && mkdir -p Notification

run_command()
{
# $1 is a message
# $2 $3 ... is the command to dump and to run
message=$1
shift
cat >> Notification/mailbody_manager.txt << EOF
$(date): $message

EOF
"$@"
}

#
# 0- Initialize Mail Body
#
cat > Notification/mailbody_manager.txt << EOF
Dear Manager(s)

Here is the report of the Jenkins Job ${JOB_NAME} #${BUILD_NUMBER}
It contains all Red Defects found in Bug Finder Example project

EOF

#
# 1- Sniff the project
#    Pre-requite: the sources must be checked out in the workspace
#                 this can be performed using the jenkins git plugin: https://github.com/jenkinsci/git-plugin
#
build_cmd="gcc -c sources/*.c"
run_command "Sniffing the sources",               \
            polyspace-configure                   \
                -allow-overwrite                  \
                -allow-build-error                \
                -prog $PROG                       \
                -author jenkins                   \
                -output-options-file $PROG.psopts \
                    $build_cmd

#
# 2- Run Bug Finder
#
run_command "Run Bug finder" \
            polyspace-bug-finder-server -options-file $PROG.psopts -results-dir $RESULT

#
# 3- Upload results on Polyspace Access
#
run_command "Create Project $PARENT_PROJECT_ON_ACCESS" \
  $ps_helper_access -create-project $PARENT_PROJECT_ON_ACCESS
run_command "Upload on $PARENT_PROJECT_ON_ACCESS/$PROG" \
  $ps_helper_access \
        -upload $RESULT \
        -parent-project $PARENT_PROJECT_ON_ACCESS \
        -project $PROG \
        -o upload.output
PROJECT_RUNID=$($ps_helper -print-runid upload.output)
PROJECT_URL=$($ps_helper -print-projecturl upload.output $POLYSPACE_ACCESS_URL)

#
# 4- Export report
#
run_command "Download report from $PARENT_PROJECT/$PROG" \
  $ps_helper_access \
        -export $PROJECT_RUNID \
        -o Notification/Report_Everything.tsv \
        -defects High

#
# 5- Filter Reports
# In this example, we are interested in all Red Defects
#
run_command "Filter Reports - Red Defects"                    \
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
run_command "Filter Reports - userA interested in Group=='Programming' and Information=='Impact: High'" \
            $ps_helper -report-filter                    \
                Notification/Report_Everything.tsv       \
                Notification/Results_Users.tsv           \
                userA                                    \
                Group "Programming"                      \
                Information "Impact: High"
run_command "Filter Reports - userA also interested in Group=='Programming' and Information=='Impact: Medium'" \
            $ps_helper -report-filter                    \
                Notification/Report_Everything.tsv       \
                Notification/Results_Users.tsv           \
                userA                                    \
                Group "Programming"                      \
                Information "Impact: Medium"
run_command "Filter Reports - userB interested in findings in Function=='bug_memstdlib()'" \
            $ps_helper -report-filter                    \
                Notification/Report_Everything.tsv       \
                Notification/Results_Users.tsv           \
                userB                                    \
                Function "bug_memstdlib()"
run_command "Filter Reports - userC interested in findings in Function=='DoesNotExist()'" \
            $ps_helper -report-filter                    \
                Notification/Report_Everything.tsv       \
                Notification/Results_Users.tsv           \
                userC                                    \
                Function "DoesNotExist()"

#
# 7- Update Jenkins status
#    Jenkins status is "UNSTABLE" when there are more than 10 Red Defects
#
BUILD_STATUS=$($ps_helper -report-status Notification/Results_All.tsv 10)

#
# 8- update mail body and mail subject
#
NB_FINDINGS_ALL=$($ps_helper -report-count-findings Notification/Results_All.tsv)
NB_FINDINGS_USERA=$($ps_helper -report-count-findings Notification/Results_Users_userA.tsv)
NB_FINDINGS_USERB=$($ps_helper -report_count-findings Notification/Results_Users_userB.tsv)
NB_FINDINGS_USERC=$($ps_helper -report-count-findings Notification/Results_Users_userC.tsv)
cat >> Notification/mailbody_manager.txt << EOF

Number of Red defects: $NB_FINDINGS_ALL
Number of findings owned by userA: $NB_FINDINGS_USERA
Number of findings owned by userB: $NB_FINDINGS_USERB
Number of findings owned by userC: $NB_FINDINGS_USERC

All results are uploaded in: $PROJECT_URL

Build Status: $BUILD_STATUS

EOF

cat > Notification/mailsubject_manager.txt << EOF
Jenkins for Manager - $BUILD_STATUS as $NB_FINDINGS_ALL findings
EOF

for user in userA userB userC
do
  echo "Jenkins for $user - $($ps_helper -report-count-findings Notification/Results_Users_$user.tsv) findings" > Notification/mailsubject_user_$user.txt
done

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
#     Mail subject filename: Notification/mailsubject_manager.txt
#     Mail body filename:    Notification/mailbody_manager.txt
#
#   Send different e-mails to individual recipients [Checked]
#     Attachment file base name:   Notification/Results_Users.tsv
#     Mail subject file base name: Notification/mailsubject_user.txt
#    	Mail body file base name:    <empty>
#
