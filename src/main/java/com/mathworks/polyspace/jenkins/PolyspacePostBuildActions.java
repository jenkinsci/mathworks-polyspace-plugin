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

import org.kohsuke.stapler.*;

import com.mathworks.polyspace.jenkins.config.Messages;
import com.mathworks.polyspace.jenkins.config.PolyspaceConfigUtils;
import com.mathworks.polyspace.jenkins.utils.PolyspaceHelpersUtils;
import com.mathworks.polyspace.jenkins.utils.PolyspaceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

import hudson.*;
import hudson.tasks.*;          // The mailer
import hudson.util.FormValidation;
import hudson.model.*;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
// import org.apache.commons.io.IOUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jakarta.annotation.Nonnull;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;

/**
 * {@link Publisher} that sends Polyspace Notification in e-mail.
 */
public class PolyspacePostBuildActions extends Notifier implements SimpleBuildStep {
    private final PolyspaceHelpersUtils polyspaceHelpersUtils = new PolyspaceHelpersUtils();
    private Boolean sendToRecipients;    /** True if we want to send an email to a list of recipients */
    private String recipients;           /** Whitespace-separated list of e-mail addresses that represent recipients */
    private String fileToAttach;         /** File to attach */
    private String mailSubject;          /** File containing the mail subject. "" if generic subject is to be applied */
    private String mailBody;             /** File containing the mail body. "" if generic body is to be applied */
    private Boolean sendToOwners;        /** True if we want to send an email to all owners */
    private String queryBaseName;        /** query base name */
    private String mailSubjectBaseName;  /** base name of the file containing the mail subject. "" if generic subject is to be applied. file for a user is mailSubjectBaseName _ user . ext */
    private String mailBodyBaseName;     /** base name of the file containing the mail body. "" if generic body is to be applied. file for a user is mailBodyBaseName _ user . ext */
    private String uniqueRecipients;     /** Unique recipient that receives all emails */

    // The standard line ending for email messages, as defined by the Internet Message Format
    // standard (RFC 5322), is the sequence of carriage return (CR) followed by line feed (LF),
    // represented as \r\n. 
    // (Whatever the OS).
    private String CRLF = "\r\n";

    @DataBoundConstructor
    public PolyspacePostBuildActions() { }

    @DataBoundSetter
    public void setSendToRecipients(Boolean sendToRecipients ) {
      this.sendToRecipients = sendToRecipients;
    }

    @DataBoundSetter
    public void setRecipients(String recipients) {
      this.recipients = recipients;
    }

    @DataBoundSetter
    public void setFileToAttach(String fileToAttach) {
      this.fileToAttach = fileToAttach;
    }

    @DataBoundSetter
    public void setMailSubject(String mailSubject) {
      this.mailSubject = mailSubject;
    }

    @DataBoundSetter
    public void setMailBody(String mailBody) {
      this.mailBody = mailBody;
    }

    @DataBoundSetter
    public void setSendToOwners(Boolean sendToOwners) {
      this.sendToOwners = sendToOwners;
    }

    @DataBoundSetter
    public void setQueryBaseName(String queryBaseName) {
      this.queryBaseName = queryBaseName;
    }

    @DataBoundSetter
    public void setMailSubjectBaseName(String mailSubjectBaseName) {
      this.mailSubjectBaseName = mailSubjectBaseName;
    }

    @DataBoundSetter
    public void setMailBodyBaseName(String mailBodyBaseName) {
      this.mailBodyBaseName = mailBodyBaseName;
    }

    @DataBoundSetter
    public void setUniqueRecipients(String uniqueRecipients) {
      this.uniqueRecipients = uniqueRecipients;
    }

    private JenkinsLocationConfiguration getJenkinsLocationConfiguration() {
        final JenkinsLocationConfiguration jlc = JenkinsLocationConfiguration.get();
        if (jlc == null) {
            throw new IllegalStateException("JenkinsLocationConfiguration not available");
        }
        return jlc;
    }

    public void sendMail( @QueryParameter String sendMailTo,
                          @QueryParameter String subject,
                          @QueryParameter String text,
                          @QueryParameter String attachSource,
                          @QueryParameter String attachName
                        ) throws IOException
    {
      try {
        String charset = Mailer.descriptor().getCharset();
        MimeMessage msg = new MimeMessage(Mailer.descriptor().createSession());
        msg.setSubject(subject, charset);
        msg.setFrom(stringToAddress(getJenkinsLocationConfiguration().getAdminAddress(), charset));

        String replyToAddress = Mailer.descriptor().getReplyToAddress();
        if (StringUtils.isNotBlank(replyToAddress)) {
            msg.setReplyTo(new Address[]{stringToAddress(replyToAddress, charset)});
        }
        msg.setSentDate(new Date());

        // Add all the recipients
        StringTokenizer tokens = new StringTokenizer(sendMailTo, " ,;");
        while (tokens.hasMoreTokens()) {
          msg.addRecipient(Message.RecipientType.TO, stringToAddress(tokens.nextToken(), charset));
        }

        // create the message body, with the text followed by the file to attach if any
        Multipart multipart = new MimeMultipart();

        if (!attachName.isEmpty()) {
          File file = new File(attachSource);
          if (file.length()  > 10*1024*1024) {
            // size of the attachement is too large
            // do not attach, and add a message in the mail body
            text += CRLF + CRLF + "Size of the attached file is too large  -  Not attached" + CRLF;
          } else {
          MimeBodyPart attachmentBodyPart= new MimeBodyPart();
          DataSource source = new FileDataSource(attachSource);
          attachmentBodyPart.setDataHandler(new DataHandler(source));
          attachmentBodyPart.setFileName(attachName);
          multipart.addBodyPart(attachmentBodyPart);
        }
        }

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(text, charset);
        multipart.addBodyPart(textBodyPart);

        msg.setContent(multipart);
        Transport.send(msg);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new IOException(e.getMessage());
      }
    }

    // Move to utils
    private String getFilenameOwner(final String name, final String owner, FilePath workspace) throws java.io.IOException
    {
      String fileName = workspace + File.separator;
      if (owner.isEmpty()) {
        fileName += name;
      } else {
        fileName += name + polyspaceHelpersUtils.getReportOwner(Paths.get(name), owner);
      }
      // InputStream inputStream = new FileInputStream(fileName);
      // return IOUtils.toString(inputStream);
      return Files.readString(Paths.get(fileName));
    }

    private String generateMailBody(final String body, final String owner, final String attachName, final String attachSource, FilePath workspace, Run<?,?> build) {
      try {
        if ((body != null) && !body.isEmpty()) {
          return getFilenameOwner(body, owner, workspace);
        }
      } catch (java.io.IOException e) {
        // Generate the generic mail body
      }

      String text = "";
      if (owner.isEmpty()) {
        text += "General email sent by Polyspace Jenkins Plugin" + CRLF + CRLF;
      } else {
        text += "Dear " + owner + "," + CRLF;
        text += "Please find attached the findings you own." + CRLF + CRLF;
      }

      if (!attachName.isEmpty()) {
        text += "Please check attached file " + attachName + CRLF;
        try {
          text += "It contains ";
          text += polyspaceHelpersUtils.getCountFindings(Paths.get(attachSource));
          text += " finding(s)" + CRLF;
        } catch (Exception e) {
          text += "Cannot count nb of findings" + CRLF;
        }
      } else if (!attachSource.isEmpty()) {
        text += "Warning: Could not attach " + attachSource + CRLF;
      }

      text += CRLF;
      text += "Check Jenkins console output at ";
      text += getJenkinsLocationConfiguration().getUrl() + build.getUrl() + CRLF;
      text += "Polyspace configuration is using" + CRLF;
      if (!PolyspaceBuildWrapper.descriptor().getPolyspaceAccessURL().equals("POLYSPACE_ACCESS_URL_IS_UNSET")) {
        text += "- Polyspace Access " + PolyspaceBuildWrapper.descriptor().getPolyspaceAccessURL() + CRLF;
      }
      return text;
    }

    private String generateMailSubject(final String subject, final String owner, FilePath workspace, Run<?,?> build) {
      try {
        if ((subject != null) && !subject.isEmpty()) {
          return getFilenameOwner(subject, owner, workspace);
        }
      } catch (java.io.IOException e) {
        // Generate the generic mail subject
      }

      String text = "";
      text += "Polyspace Jenkins Plugin";
      text += " - " + build.getFullDisplayName();
      text += " - " + build.getResult();
      if (owner.isEmpty()) {
        text += " - General Email";
      } else {
        text += " - Email to Finding Owners";
      }
      return text;
    }

    private String getFileFromAgent(FilePath workspace, String fileToAttach) throws IOException, InterruptedException
    {
      FilePath fileOnAgent = workspace.child(fileToAttach);
      if (fileOnAgent.exists())
      {
        Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "polyspace-");
        File fileOnController = new File(tempDir.toFile(), fileOnAgent.getName());
        fileOnAgent.copyTo(new FilePath(fileOnController));
        return fileOnController.toString();
      } else {
        return "";
      }
    }

    private void sendToRecipients(final Run<?,?> build, final FilePath workspace) throws IOException, InterruptedException
    {
      FormValidation fileToAttachValidation = PolyspaceConfigUtils.doCheckFilename(fileToAttach);

      if (fileToAttachValidation == FormValidation.ok())
      {
        String attachSource = "";
        String attachName = "";

        if ((fileToAttach != null) && !fileToAttach.isEmpty())
        {
          attachSource = getFileFromAgent(workspace, fileToAttach);
          attachName = new File(attachSource).getName();
        }

        final String subject = generateMailSubject(mailSubject, "", workspace, build);
        final String body = generateMailBody(mailBody, "", attachName, attachSource, workspace, build);

        sendMail(recipients, subject, body, attachSource, attachName);
      }
      else
      {
        String msg = Messages.errorSendingMail() + " " + fileToAttachValidation.getMessage();
        msg += " in Attachment Filename ('" + fileToAttach + "')";
        throw new RuntimeException(msg);
      }
    }

    private void sendToOwners(final Run<?,?> build, final FilePath workspace) throws IOException, InterruptedException
    {
      String ownerList = getFileFromAgent(workspace, polyspaceHelpersUtils.getReportOwnerList(Paths.get(queryBaseName)).toString());

      if (!ownerList.isEmpty())
      {
        final String ownerListContent = PolyspaceUtils.getFileContent(Paths.get(ownerList));

        try (final Scanner ownerListScanner = new Scanner(ownerListContent))
        {
          while (ownerListScanner.hasNextLine())
          {
            final String owner = ownerListScanner.nextLine();
            final String recipient = uniqueRecipients.isEmpty() ? owner : uniqueRecipients;

            final String attachSource = getFileFromAgent(workspace, polyspaceHelpersUtils.getReportOwner(Paths.get(queryBaseName), owner).toString());
            final String attachName = new File(attachSource).getName();

            final String subject = generateMailSubject(mailSubjectBaseName, owner, workspace, build);
            final String body = generateMailBody(mailBodyBaseName, owner, attachName, attachSource, workspace, build);

            sendMail(recipient, subject, body, attachSource, attachName);
          }
        }
      }
    }

    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException
    {
      if (sendToRecipients && (recipients != null) && !recipients.isEmpty())
      {
        sendToRecipients(build, workspace);
      }

      if (sendToOwners && (queryBaseName != null) && !queryBaseName.isEmpty())
      {
        sendToOwners(build, workspace);
      }
    }

    /**
     * This class does explicit check pointing.
     */
    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
    }

    private static Pattern ADDRESS_PATTERN = Pattern.compile("\\s*([^<]*)<([^>]+)>\\s*");

    /**
     * Converts a String to {@link InternetAddress}.
     * @param strAddress Address String
     * @param charset Charset (encoding) to be used
     * @return {@link InternetAddress} for the specified String
     * @throws AddressException Malformed address
     * @throws UnsupportedEncodingException Unsupported encoding
     */
    public static @Nonnull InternetAddress stringToAddress(@Nonnull String strAddress,
            @Nonnull String charset) throws AddressException, UnsupportedEncodingException {
        Matcher m = ADDRESS_PATTERN.matcher(strAddress);
        if(!m.matches()) {
            return new InternetAddress(strAddress);
        }

        String personal = m.group(1);
        String address = m.group(2);
        return new InternetAddress(address, personal, charset);
    }

    @Extension
      public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
        }

        public String getDisplayName() {
            return com.mathworks.polyspace.jenkins.config.Messages.polyspaceNotification();
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public FormValidation doCheckFileToAttach(@QueryParameter String fileToAttach) {
            return PolyspaceConfigUtils.doCheckFilename(fileToAttach);
        }
    }
}
