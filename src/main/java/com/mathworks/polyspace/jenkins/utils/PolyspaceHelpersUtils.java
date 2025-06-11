// Copyright (c) 2024 The MathWorks, Inc.
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PolyspaceHelpersUtils {

  /**
   * @param ownerList - The owner list file
   * @param owner - An owner
   * @return {@code true} if {@code owner} is in {@code ownerList} - {@code false} otherwise
   * @throws IOException Error while accessing {@code ownerList}
   */
  public static Boolean isOwnerInFile(final Path ownerList, final String owner) throws IOException
  {
    if (!ownerList.toFile().exists()) {
      // The owner list file does not exist. So the owner is not in the list!
      return false;
    }

    final String content = PolyspaceUtils.getFileContent(ownerList);
    return content.lines().anyMatch(line -> line.equals(owner));
  }

  /**
   * Append {@code line} with a new line in {@code file}
   * @param file - Path to a file
   * @param line - Line string to add
   * @throws IOException Error while accessing {@code file}
   */
  public static void appendLineInFile(final Path file, final String line) throws IOException
  {
    final String lineWithNewLine = line + System.lineSeparator();
    Files.write(file, lineWithNewLine.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, // Create the file if it does not exist
      StandardOpenOption.APPEND); // Append to the file if it exists
  }

  /**
   * Build the report-owner filename:
   * <ul>
   *    <li>Owner is empty: "{@code report}" unmodified</li>
   *    <li>Report without extension: "{@code report}_{@code owner}"</li>
   *    <li>Report with extension: "{@code report}_{@code owner}.ext"</li>
   * </ul>
   * @param report - Path to the report
   * @param owner - An owner
   * @return The report-owner filename
   */
  public static Path getReportOwner(final Path report, final String owner)
  {
    if (owner.isEmpty()) return report;

    final String reportAsString = report.toString();
    final int extensionPos = reportAsString.lastIndexOf('.');
    final int separatorPos = reportAsString.lastIndexOf(File.separatorChar);

    if (separatorPos >= extensionPos) return Paths.get(reportAsString + "_" + owner);

    return Paths.get(reportAsString.substring(0, extensionPos) + "_" + owner + reportAsString.substring(extensionPos));
  }

  /**
   * Build the report-owner-list filename: {@code report}.owners.list
   * @param report - Path to the report
   * @return The report-owner-list filename
   */
  public static Path getReportOwnerList(final Path report)
  {
    return Paths.get(report + ".owners.list");
  }

  /**
   * Get the column ID of {@code token} in {@code line}
   * Note that tokens in {@code line} are separated by tabs
   * @param line - Line string containing the report column names
   * @param token - Searched token
   * @return The column ID
   */
  public static int reportGetColId(final String line, final String token)
  {
    final List<String> tokens = Arrays.asList(line.split("\t"));
    final int index = tokens.indexOf(token);
    if (index != -1) {
        return index;
    }
    throw new RuntimeException("Title '" + token + "' does not exist");
  }

  /**
   * Filter {@code originalReport} against {@code filters} into {@code filteredReport} associated with {@code owner}
   * @param originalReport - Path to original report
   * @param filteredReport - Path to the new filtered report - it may already exist to allow to append multiple filtered reports
   * @param owner - The owner for the filtered report - must be a username matching an email as it will also be used to send the email notification
   * @param filters - An array of strings containing one or more "key" "value" pairs
   * @throws IOException Error while accessing {@code originalReport} or {@code filteredReport}
   */
  public static void reportFilter(final Path originalReport, final Path filteredReport, final String owner, final String[] filters) throws IOException {
    int n = 0;

    // Check original report
    if (!Files.exists(originalReport))
    {
      throw new RuntimeException("Original report '" + originalReport + "' does not exist");
    }

    // Check filters
    if (filters.length == 0)
    {
      throw new RuntimeException("Missing filters");
    }

    // Check filtered report
    Path filteredReport_owner = PolyspaceHelpersUtils.getReportOwner(filteredReport, owner);
    if (!owner.isEmpty() && filteredReport_owner.toFile().isDirectory()) {
      throw new RuntimeException("Cannot create filtered report, a directory with the same name already exists: '" + filteredReport_owner + "'");
    }

    // Check owners list
    final Path ownerList = PolyspaceHelpersUtils.getReportOwnerList(filteredReport);   // name of the file that contains all owners that have been filtered
    if (ownerList.toFile().isDirectory()) {
      throw new RuntimeException("Cannot create owner list, a directory with the same name already exists: '" + ownerList + "'");
    }

    // Return if original report is empty
    final String originalReportContent = PolyspaceUtils.getFileContent(originalReport);
    if (originalReportContent.isEmpty())
    {
      return;
    }

    try (final Scanner originalReportScanner = new Scanner(originalReportContent))
    {
      // Compute filters
      final String titleLine = originalReportScanner.nextLine();
      int nCriteria = (filters.length - n) / 2;
      int[] colId = new int[nCriteria];
      String[] criteria = new String[nCriteria];
      for (int id = 0; id < nCriteria; id++) {
        colId[id] = PolyspaceHelpersUtils.reportGetColId(titleLine, filters[n++]);
        criteria[id] = filters[n++];
      }

      // If the output file already exists, new lines will be concatenated to the existing file.
      // This allows to concat several filtering outputs into the same target file.
      ArrayList<String> filteredReportContent = new ArrayList<>();
      boolean addTitle = (!filteredReport_owner.toFile().exists());
      if (addTitle)
      {
        filteredReportContent.add(titleLine);
      }

      boolean filteredReportEmpty = true;
      while (originalReportScanner.hasNextLine())
      {
        String line = originalReportScanner.nextLine();
        String[] tokens = line.split("\t");
        boolean matching = true;
        for (int id = 0; matching && (id < nCriteria); id++) {
          matching = tokens[colId[id]].equals(criteria[id]);
        }
        if (matching) {
          filteredReportEmpty = false;
          filteredReportContent.add(line);
        }
      }

      PolyspaceUtils.writeContent(filteredReport_owner, filteredReportContent);

      if (!filteredReportEmpty && !owner.isEmpty()) {
        // this owner must be added to the list of owners, if this is not already the case
        if (!PolyspaceHelpersUtils.isOwnerInFile(ownerList, owner)) {
          PolyspaceHelpersUtils.appendLineInFile(ownerList, owner);
        }
      }
    }
  }

  /**
   * @param report - Path to the report
   * @return - Number of findings in {@code report}: number of lines - 1 (title line)
   * @throws IOException Error while accessing {@code report}
   */
  public static long getCountFindings(final Path report) throws IOException {
    return PolyspaceUtils.getFileLineCount(report) - 1;
  }

  /**
   *
   * @param report - Path to the report
   * @param max - Thereshold for status
   * @return - Job status string: SUCCESS if num findings smaller than {@code max}, UNSTABLE otherwise
   * @throws IOException Error while accessing {@code report}
   */
  public static String getReportStatus(final Path report, final long max) throws IOException
  {
    final long nb = PolyspaceHelpersUtils.getCountFindings(report);
    return nb > max ? "UNSTABLE" : "SUCCESS";
  }

  /**
   * Small utility class to hold a results runId and projectId
   */
  static public class AccessUploadResult {
    public String runId;
    public String projectId;

    public AccessUploadResult() {
      runId = "";
      projectId = "";
    }

    public AccessUploadResult(final String r, final String p) {
      runId = r;
      projectId = p;
    }
  }

    /**
   * @param output - Captured content of upload command stdout
   * @return - RunId and ProjectId for result found in {@code output}
   * @throws IOException Error while accessing {@code output}
   */
  private static AccessUploadResult getAccessUploadResult(Path output) throws IOException
  {
    AccessUploadResult result = new AccessUploadResult();
    final String content = PolyspaceUtils.getFileContent(output);

    content.lines()
      .filter(line -> line.startsWith("Upload successful for RUN_ID"))
      .findFirst()
      .ifPresent(line -> {
          String[] tokens = line.split(" ");
          result.runId = tokens[4];
          result.projectId = tokens[7];
      });

    return result;
  }

    /**
   * @param output - Path to the report
   * @return Result runId
   * @throws IOException Error while accessing {@code output}
   */
  public static String getAccessResultRunId(Path output) throws IOException
  {
    AccessUploadResult result = getAccessUploadResult(output);
    if (result.runId.isEmpty())
    {
      throw new RuntimeException("Cannot find runId in '" + output + "'");
    }
    return result.runId;
  }

    /**
   * @param output - Path to the report
   * @return Result projectId
   * @throws IOException Error while accessing {@code output}
   */
  public static String getAccessResultProjectId(Path output) throws IOException
  {
    AccessUploadResult result = getAccessUploadResult(output);
    if (result.projectId.isEmpty())
    {
      throw new RuntimeException("Cannot find projectId in '" + output + "'");
    }
    return result.projectId;
  }

    /**
   * @param output - Path to the report
   * @param accessURL - Root Access server URL
   * @return Results URL
   * @throws IOException Error while accessing {@code output}
   */
  public static String getAccessResultUrl(Path output, String accessURL) throws IOException
  {
    AccessUploadResult result = getAccessUploadResult(output);
    if (result.projectId.isEmpty())
    {
      throw new RuntimeException("Cannot find project url from '" + output + "'");
    }
    return accessURL + "/metrics/index.html?a=review&p=" + result.projectId + "&r=" + result.runId;
  }
}
