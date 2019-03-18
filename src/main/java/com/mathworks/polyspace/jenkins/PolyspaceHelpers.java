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

package com.mathworks.polyspace.jenkins;

import hudson.tasks.*;      // The mailer
import org.apache.commons.lang3.SystemUtils;
import java.util.*;
import java.io.*;

public class PolyspaceHelpers {

  public static final String exeSuffix() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return ".exe";
    } else {
      return "";
    }
  }

  public static Boolean checkPolyspaceCommand(List<String> Command) {
    BufferedReader br = null;
    try  {
      ProcessBuilder pb = new ProcessBuilder(Command);
      Process p = pb.start();
      br = new BufferedReader(new InputStreamReader(p.getErrorStream(), Mailer.descriptor().getCharset()));
      p.waitFor();

      if (p.exitValue() != 0) {
        return false;
      }
    } catch (RuntimeException e) {
      return false;
    } catch (Exception e) {
      return false;
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (Exception e) {
        return false;
      }
    }
    return true;
  }

  // return true if owner is part of the owner list file fOwnerList
  private static Boolean ownerInFile(File fOwnerList, final String owner) throws IOException {
    if (!fOwnerList.exists()) {
      return false;    // the owner list file does not exist. so the owner is not in the list
    }

    InputStream inputStream = new FileInputStream(fOwnerList);        // open the input file
    Reader fr = new InputStreamReader(inputStream, "UTF-8");
    BufferedReader br = new BufferedReader(fr);
    String this_line;
    try {
      // read the owner list file line by line
      while ((this_line = br.readLine()) != null) {
        if (this_line.equals(owner)) {
          return true;    // owner is found
        }
      }
    } finally {
      br.close();
    }
    return false;         // owner has not been found
  }

  // append a line in a file
  private static void appendLineInFile(File f, final String line) throws IOException {
    OutputStream outputStream = new FileOutputStream(f, true);          // true to append
    Writer fw = new OutputStreamWriter(outputStream, "UTF-8");
    BufferedWriter bw = new BufferedWriter(fw);

    try {
      bw.write(line);
      bw.newLine();
    } finally {
      bw.close();
    }
  }

  // return the report name for a given owner
  public static String getReportOwner(final String report, final String owner)
  {
    if (!owner.equals("")) {
      final int extensionPos = report.lastIndexOf('.');
      final int separatorPos = report.lastIndexOf(File.separatorChar);
      if (separatorPos < extensionPos) {
        // insert owner before the extension
        return report.substring(0, extensionPos) + "_" + owner + report.substring(extensionPos);
      } else {
        return report + "_" + owner;    // no extension, so add the owner name
      }
    } else {
      return report;    // no owner, so same rerport name
    }
  }

  public static String getReportOwnerList(final String report) {
    return report + ".owners.list";
  }

  // Get the colid of 'title' in the line.
  // Tokens in 'line' are separated by tabs
  private static int reportGetColId(final String[] tokens, final String title) throws RuntimeException
  {
    for (int i=0; i<tokens.length; i++) {
      if (tokens[i].equals(title)) {
        return i;
      }
    }
    throw new RuntimeException("Title '" + title + "' does not exist");
  }

  private static void reportFilter(final String[] arg) throws IOException, RuntimeException {
    if (arg.length < 5) {
      System.out.println("Usage: ps_helper -report-filter <original_report> <filtered_report> [<owner>] [<title> <value>]+");
      return;
    }

    int n = 0;
    n++;  // the command - no need to be kept
    final String originalReport = arg[n++];   // name of the original report
    final String filteredReport = arg[n++];   // name of the filtered report

    String owner;
    if ((arg.length % 2) == 0) {
      // even number of arguments ==> there is owner that is specified
      owner = arg[n++];
    } else {
      owner = "";
    }

    InputStream inputStream = new FileInputStream(originalReport);
    Reader fr = new InputStreamReader(inputStream, "UTF-8");
    BufferedReader br = new BufferedReader(fr);
    BufferedWriter bw = null;
    String this_line = br.readLine();
    Boolean added_one_line = false;       // true if the report for the owner contains a new line
    try {
      if (this_line == null) {
        return;
      }

      int nCriteria = (arg.length - n) / 2;
      String[] tokens = this_line.split("\t");
      int[] colId = new int[nCriteria];
      String[] criteria = new String[nCriteria];
      for (int id = 0; id < nCriteria; id++) {
        colId[id] = reportGetColId(tokens, arg[n++]);
        criteria[id] = arg[n++];
      }

      // Open the filtered report
      String filteredReport_owner = getReportOwner(filteredReport, owner);
      File fout = new File(filteredReport_owner);
      if (fout.isDirectory()) {
        throw new RuntimeException("Cannot create filtered report as the directory '" + filteredReport_owner + "'");
      }
      Boolean addTitle = (!fout.exists());

      // Add the title line if the filtered report does not exist yet
      OutputStream outputStream = new FileOutputStream(fout, true);
      Writer fw = new OutputStreamWriter(outputStream, "UTF-8");
      bw = new BufferedWriter(fw);

      if (addTitle) {
        bw.write(this_line);
        bw.newLine();
      }

      while ((this_line = br.readLine()) != null) {
        tokens = this_line.split("\t");
        boolean ok = true;
        for (int id = 0; ok && (id < nCriteria); id++) {
          ok = tokens[colId[id]].equals(criteria[id]);
        }
        if (ok) {
          added_one_line = true;
          bw.write(this_line);
          bw.newLine();
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
      if (bw != null) {
        bw.close();
      }
    }

    if (added_one_line && !owner.equals("")) {
      // this owner must be added to the list of owner, if this is not already the case
      final String ownerList = getReportOwnerList(filteredReport);   // name of the file that contains all owners that have been filtered
      File fOwnerList = new File(ownerList);
      if (fOwnerList.isDirectory()) {
        throw new RuntimeException("Cannot create owner list as the directory '" + ownerList + "'");
      }

      if (!ownerInFile(fOwnerList, owner)) {
        appendLineInFile(fOwnerList, owner);
      }
    }
  }

  static public class AccessUploadResult {
    String runId;
    String projectId;
    
    public AccessUploadResult() {
      runId = "";
      projectId = "";
    }
  };
  
  private static AccessUploadResult getAccessUploadResult(final String prs_upload_output) throws IOException, RuntimeException {
    AccessUploadResult accessResult = new AccessUploadResult();
    InputStream inputStream = new FileInputStream(prs_upload_output);
    Reader fr = new InputStreamReader(inputStream, "UTF-8");
    BufferedReader br = new BufferedReader(fr);
    try {
      String this_line;
      while ((this_line = br.readLine()) != null) {
        if (this_line.startsWith("Upload successful for RUN_ID")) {
          String[] tokens = this_line.split(" ");
          accessResult.runId = tokens[4];
          accessResult.projectId = tokens[7];
          break;
        }
      }
    } finally {
      br.close();
    }
    return accessResult;
  }
  
  private static void prsPrintRunId(final String[] arg) throws IOException, RuntimeException {
    if (arg.length != 2) {
      System.out.println("Usage: ps_helper -print-runid <prs upload output>");
      return;
    }
    final String prs_upload_output = arg[1];
    AccessUploadResult accessResult = getAccessUploadResult(prs_upload_output);
    if (accessResult.runId.equals("")) {
      throw new RuntimeException("Cannot find runId in '" + prs_upload_output + "'");
    }
    System.out.println(accessResult.runId);
  }
  
  private static void prsPrintProjectId(final String[] arg) throws IOException, RuntimeException {
    if (arg.length != 2) {
      System.out.println("Usage: ps_helper -print-projectid <prs upload output>");
      return;
    }
    final String prs_upload_output = arg[1];
    AccessUploadResult accessResult = getAccessUploadResult(prs_upload_output);
    if (accessResult.projectId.equals("")) {
      throw new RuntimeException("Cannot find projectId in '" + prs_upload_output + "'");
    }
    System.out.println(accessResult.projectId);
  }
  
  private static void prsPrintProjectURL(final String[] arg) throws IOException, RuntimeException {
    if (arg.length != 3) {
      System.out.println("Usage: ps_helper -print-projecturl <prs upload output> <prs_url>");
      return;
    }
    final String prs_upload_output = arg[1];
    AccessUploadResult accessResult = getAccessUploadResult(prs_upload_output);
    if (accessResult.projectId.equals("") || accessResult.runId.equals("")) {
      throw new RuntimeException("Cannot find project url from '" + prs_upload_output + "'");
    }
    System.out.println(arg[2] + "/metrics/index.html?a=review&p=" + accessResult.projectId + "&r=" + accessResult.runId);
  }
  
  private static void reportStatus(final String[] arg) throws IOException, NumberFormatException {
    if (arg.length != 3) {
      System.out.println("Usage: ps_helper -report-status <report> <nb_to_fail>");
      return;
    }

    final String report = arg[1];
    int max = Integer.parseInt(arg[2]);
    InputStream inputStream = new FileInputStream(report);
    Reader fr = new InputStreamReader(inputStream, "UTF-8");
    BufferedReader br = new BufferedReader(fr);
    try {
      String this_line;
      int nb = 0;
      while ((this_line = br.readLine()) != null) {
        nb ++;
      }
      nb --;    // line containing the titles of the columns is removed from the count

      if (nb > max) {
        System.out.println("UNSTABLE");
      } else {
        System.out.println("SUCCESS");
      }
    } finally {
      br.close();
    }
  }

  public static int reportCountFindings(final String report) {
    BufferedReader br = null;
    try {
      InputStream inputStream = new FileInputStream(report);
      Reader fr = new InputStreamReader(inputStream, "UTF-8");
      br = new BufferedReader(fr);
      String this_line;
      int nb = 0;
      while ((this_line = br.readLine()) != null) {
        nb ++;
      }
      nb --;    // line containing the titles of the columns is removed from the count
      return nb;
    } catch (IOException e) {
      return 0;
    } catch (Exception e) {
      return 0;
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          return 0;
        }
      }
    }
  }

  private static void reportCountFindings(final String[] arg) throws IOException, NumberFormatException {
    if (arg.length != 2) {
      System.out.println("Usage: ps_helper -report-count-findings <report>");
      return;
    }

    int nb = reportCountFindings(arg[1]);
    System.out.println(nb);
  }

  public static void main (String[] arg) throws IOException, RuntimeException, NumberFormatException{
    Boolean usage = false;
    if (arg.length == 0) {
      usage = true;
    } else if (arg[0].equals("-report-filter") || arg[0].equals("report_filter")) {
      reportFilter(arg);
    } else if (arg[0].equals("-report-status") || arg[0].equals("report_status")) {
      reportStatus(arg);
    } else if (arg[0].equals("-report-count-findings") || arg[0].equals("report_count_findings")) {
      reportCountFindings(arg);
    } else if (arg[0].equals("-print-runid") || arg[0].equals("prs_print_runid")) {
      prsPrintRunId(arg);
    } else if (arg[0].equals("-print-projectid") || arg[0].equals("prs_print_projectid")) {
      prsPrintProjectId(arg);
    } else if (arg[0].equals("-print-projecturl") || arg[0].equals("prs_print_projecturl")) {
      prsPrintProjectURL(arg);
    } else {
      usage = true;
    }

    if (usage) {
      String[] empty = {} ;
      reportFilter(empty);
      reportStatus(empty);
      reportCountFindings(empty);
      prsPrintRunId(empty);
      prsPrintProjectId(empty);
      prsPrintProjectURL(empty);
    }
  }
}
