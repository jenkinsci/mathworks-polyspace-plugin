// Copyright (c) 2024-2025 The MathWorks, Inc.
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

package com.mathworks.polyspace.jenkins.test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mathworks.polyspace.jenkins.utils.PolyspaceHelpersUtils;

import org.junit.jupiter.api.Test;
import com.mathworks.polyspace.jenkins.utils.PolyspaceUtils;

class PolyspaceHelpersUtilsTest {

  private static String cwd = System.getProperty("user.dir");
  private static Path ownersList = Paths.get(cwd, "src", "test", "data", "owners.list.txt");
  private static Path results = Paths.get(cwd, "src", "test", "data", "results.tsv");
  private static Path emptyResults = Paths.get(cwd, "src", "test", "data", "emptyResults.tsv");
  private static Path accessUploadSuccessOutput = Paths.get(cwd, "src", "test", "data", "accessUploadSuccessOutput.txt");
  private static Path accessUploadFailureOutput = Paths.get(cwd, "src", "test", "data", "accessUploadFailureOutput.txt");
  private static Path simpleFile = Paths.get(cwd, "src", "test", "data", "simpleFileWithEndingNewLine.txt");
  private static Path allMisraC2012 = Paths.get(cwd, "src", "test", "data", "allMisraC2012.tsv");
  private static Path allStaticMemory = Paths.get(cwd, "src", "test", "data", "allStaticMemory.tsv");
  private static Path allFoo = Paths.get(cwd, "src", "test", "data", "allFoo.tsv");
  private static Path allFooOwnersList = Paths.get(cwd, "src", "test", "data", "allFoo.tsv.owners.list");

  @Test
  void testOwnerInFile() throws Exception
  {
    assertTrue(PolyspaceHelpersUtils.isOwnerInFile(ownersList, "sbobin"));
    assertTrue(PolyspaceHelpersUtils.isOwnerInFile(ownersList, "cpreve"));
    assertFalse(PolyspaceHelpersUtils.isOwnerInFile(ownersList, "ddelarue"));
  }

  @Test
  void testAppendLineInFile() throws Exception
  {
    Path tempFile = PolyspaceUtils.copyToTempFile(simpleFile);
    assertEquals(
      "line1\nline2\nline3 with ending new line\n",
      PolyspaceUtils.getFileContent(tempFile)
    );

    PolyspaceHelpersUtils.appendLineInFile(tempFile, "line4");
    assertEquals(
      "line1\nline2\nline3 with ending new line\nline4\n",
      PolyspaceUtils.getFileContent(tempFile)
    );
    Files.delete(tempFile);
  }

  @Test
  void testGetReportOwner()
  {
    assertEquals(
      "report",
      PolyspaceHelpersUtils.getReportOwner(Paths.get("report"), "").toString()
    );
    assertEquals(
      "reportWithNoExtension_owner",
      PolyspaceHelpersUtils.getReportOwner(Paths.get("reportWithNoExtension"), "owner").toString()
    );
    assertEquals(
      "report_owner.extension",
      PolyspaceHelpersUtils.getReportOwner(Paths.get("report.extension"), "owner").toString()
    );
  }

  @Test
  void testGetReportOwnerList()
  {
    assertEquals(
      "report.owners.list",
      PolyspaceHelpersUtils.getReportOwnerList(Paths.get("report")).toString()
    );
  }

  @Test
  void testReportGetColId()
  {
    final String titleLine = "FindingID\tFamily\tGroup\tColor\tNew\tCheck\tInformation\tFunction\tFile\tStatus\tSeverity\tURL";

    assertEquals(0, PolyspaceHelpersUtils.reportGetColId(titleLine, "FindingID"));
    assertEquals(5, PolyspaceHelpersUtils.reportGetColId(titleLine, "Check"));
    assertEquals(11, PolyspaceHelpersUtils.reportGetColId(titleLine, "URL"));

    Exception exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.reportGetColId(titleLine, "BLURP"));
    assertEquals("Title 'BLURP' does not exist", exception.getMessage());
  }

  @Test
  void testReportFilterEmpty() throws Exception
  {
    // Test filtering an empty report does nothing
    final Path filteredReport = Paths.get(cwd, "src", "test", "data", "computedFilteredEmptyResults.tsv");

    PolyspaceHelpersUtils.reportFilter(emptyResults, filteredReport, "", new String[] { "header", "value" });

    assertFalse(filteredReport.toFile().exists());
  }

  @Test
  void testReportFilterAllMisraC2012() throws Exception
  {
    // Test extract all Family="MISRA C:2012"
    final Path filteredReport = Paths.get(cwd, "src", "test", "data", "computedAllMisraC2012.tsv");
    Files.deleteIfExists(filteredReport);

    PolyspaceHelpersUtils.reportFilter(results, filteredReport, "", new String[] { "Family", "MISRA C:2012" });

    final String expectedContent = PolyspaceUtils.getFileContent(allMisraC2012);
    final String computedContent = PolyspaceUtils.getFileContent(filteredReport);

    assertEquals(expectedContent, computedContent);

    Files.deleteIfExists(filteredReport);
  }

  @Test
  void testReportFilterAllStaticMemory() throws Exception
  {
    // Test extract all Group="Static memory"
    final Path filteredReport = Paths.get(cwd, "src", "test", "data", "computedAllStaticMemory.tsv");
    Files.deleteIfExists(filteredReport);

    PolyspaceHelpersUtils.reportFilter(results, filteredReport, "", new String[] { "Group", "Static memory" });

    final String expectedContent = PolyspaceUtils.getFileContent(allStaticMemory);
    final String computedContent = PolyspaceUtils.getFileContent(filteredReport);

    assertEquals(expectedContent, computedContent);

    Files.deleteIfExists(filteredReport);
  }

  @Test
  void testReportFilterAllFooWithOwner() throws Exception
  {
    // Test extract all Function="foo()" with owner sbobin
    final String owner = "sbobin";

    Path filteredReport = Paths.get(cwd, "src", "test", "data", "computedAllFoo.tsv");
    final Path computedFile = PolyspaceHelpersUtils.getReportOwner(filteredReport, owner);
    Files.deleteIfExists(computedFile);
    final Path computedOwnersList = PolyspaceHelpersUtils.getReportOwnerList(filteredReport);
    Files.deleteIfExists(computedOwnersList);

    PolyspaceHelpersUtils.reportFilter(results, filteredReport, owner, new String[] { "Function", "foo()" });

    assertEquals(PolyspaceUtils.getFileContent(allFoo), PolyspaceUtils.getFileContent(computedFile));
    assertEquals(PolyspaceUtils.getFileContent(allFooOwnersList), PolyspaceUtils.getFileContent(computedOwnersList));

    Files.deleteIfExists(computedOwnersList);
    Files.deleteIfExists(computedFile);
  }

  @Test
  void testReportFilterOriginalReportDoesNotExist()
  {
    // Original report does not exist
    Exception exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.reportFilter(Paths.get("original report"), Paths.get("filtered report"), "", new String[] {  }));
    assertEquals("Original report 'original report' does not exist", exception.getMessage());
  }

  @Test
  void testReportFilterNoFilters()
  {
    // Test giving no filters
    Exception exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.reportFilter(results, Paths.get("filtered report"), "owner", new String[] { }));
    assertEquals("Missing filters", exception.getMessage());
  }

  @Test
  void testReportFilterDirectoryAsFilteredReport() throws Exception
  {
    // Test exception on giving a directory as filtered report
    Path filteredReport = Paths.get(cwd, "src", "test", "data", "filteredReport");
    String owner = "owner";
    Path filteredReport_owner = PolyspaceHelpersUtils.getReportOwner(filteredReport, owner);

    Files.deleteIfExists(filteredReport_owner);
    Files.createDirectory(filteredReport_owner);

    Exception exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.reportFilter(results, filteredReport, owner, new String[] { "header", "value"}));
    assertEquals("Cannot create filtered report as the directory '" + cwd + "/src/test/data/filteredReport_owner'", exception.getMessage());

    Files.deleteIfExists(filteredReport_owner);
  }

  @Test
  void testReportFilterDirectoryAsOwnersList() throws Exception
  {
    // Test exception when owner list file is a directory
    Path filteredReport = Paths.get(cwd, "src", "test", "data", "filteredReport");
    String owner = "owner";
    final Path ownerList = PolyspaceHelpersUtils.getReportOwnerList(filteredReport);   // name of the file that contains all owners that have been filtered

    Files.deleteIfExists(ownerList);
    Files.createDirectory(ownerList);

    Exception exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.reportFilter(results,filteredReport, owner, new String[] { "header", "value"}));
    assertEquals("Cannot create owner list as the directory '" + cwd + "/src/test/data/filteredReport.owners.list'", exception.getMessage());

    Files.deleteIfExists(ownerList);
  }

  @Test
  void testGetCountFindings() throws Exception
  {
    assertEquals(25, PolyspaceHelpersUtils.getCountFindings(results));
  }

  @Test
  void testGetReportStatus() throws Exception
  {
    assertEquals("UNSTABLE", PolyspaceHelpersUtils.getReportStatus(results, 10));
    assertEquals("SUCCESS", PolyspaceHelpersUtils.getReportStatus(results, 50));
  }

  @Test
  void testGetAccessUpload() throws Exception
  {
    String runId = PolyspaceHelpersUtils.getAccessResultRunId(accessUploadSuccessOutput);
    assertEquals("17263", runId);

    String projectId = PolyspaceHelpersUtils.getAccessResultProjectId(accessUploadSuccessOutput);
    assertEquals("4928", projectId);

    String url = PolyspaceHelpersUtils.getAccessResultUrl(accessUploadSuccessOutput, "ACCESS_URL");
    assertEquals("ACCESS_URL/metrics/index.html?a=review&p=4928&r=17263", url);

    Exception exception;

    exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.getAccessResultRunId(accessUploadFailureOutput));
    assertEquals("Cannot find runId in '" + cwd + "/src/test/data/accessUploadFailureOutput.txt'", exception.getMessage());

    exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.getAccessResultProjectId(accessUploadFailureOutput));
    assertEquals("Cannot find projectId in '" + cwd + "/src/test/data/accessUploadFailureOutput.txt'", exception.getMessage());

    exception = assertThrows(RuntimeException.class, () ->
      PolyspaceHelpersUtils.getAccessResultUrl(accessUploadFailureOutput, "ACCESS_URL"));
    assertEquals("Cannot find project url from '" + cwd + "/src/test/data/accessUploadFailureOutput.txt'", exception.getMessage());
  }

}
