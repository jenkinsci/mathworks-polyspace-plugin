package com.mathworks.polyspace.jenkins.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.mathworks.polyspace.jenkins.PolyspaceHelpers;
import com.mathworks.polyspace.jenkins.utils.PolyspaceHelpersUtils;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

class PolyspaceHelpersTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @org.junit.jupiter.api.BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @org.junit.jupiter.api.AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        outContent.reset(); // Reset the stream for the next test
    }

    @Test
    void testMainReportFilter() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-filter", "original.txt", "filtered.txt", "owner1", "filter_title", "filter_value"};
            Path expectedOriginalReport = Paths.get("original.txt");
            Path expectedFilteredReport = Paths.get("filtered.txt");
            String expectedOwner = "owner1";
            String[] expectedFilters = {"filter_title", "filter_value"};

            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.reportFilter(
                eq(expectedOriginalReport),
                eq(expectedFilteredReport),
                eq(expectedOwner),
                eq(expectedFilters)
            ), times(1));
        }
    }

    @Test
    void testMainReportFilterNoOwner() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-filter", "original.txt", "filtered.txt", "filter_title", "filter_value"};
            Path expectedOriginalReport = Paths.get("original.txt");
            Path expectedFilteredReport = Paths.get("filtered.txt");
            String expectedOwner = ""; // Expect empty owner
            String[] expectedFilters = {"filter_title", "filter_value"};

            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.reportFilter(
                eq(expectedOriginalReport),
                eq(expectedFilteredReport),
                eq(expectedOwner),
                eq(expectedFilters)
            ), times(1));
        }
    }

    @Test
    void testMainReportFilterInsufficientArgs() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-filter", "original.txt"};
            PolyspaceHelpers.main(args);

            assertEquals("Usage: ps_helper -report-filter <original_report> <filtered_report> [<owner>] [<title> <value>]+" + System.lineSeparator(), outContent.toString());
            mockedStatic.verify(() -> PolyspaceHelpersUtils.reportFilter(
                any(Path.class),
                any(Path.class),
                any(String.class),
                any(String[].class)
            ), never());
        }
    }

    // Tests for -print-runid
    @Test
    void testMainPrintRunId() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-print-runid", "access_output.txt"};
            Path expectedPath = Paths.get("access_output.txt");
            String expectedRunId = "12345";

            mockedStatic.when(() -> PolyspaceHelpersUtils.getAccessResultRunId(eq(expectedPath)))
                        .thenReturn(expectedRunId);

            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.getAccessResultRunId(eq(expectedPath)), times(1));
            assertEquals(expectedRunId + System.lineSeparator(), outContent.toString());
        }
    }

    @Test
    void testMainPrintRunIdInsufficientArgs() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-print-runid"};
            PolyspaceHelpers.main(args);

            assertEquals("Usage: ps_helper -print-runid <access upload output>" + System.lineSeparator(), outContent.toString());
            mockedStatic.verify(() -> PolyspaceHelpersUtils.getAccessResultRunId(any(Path.class)), never());
        }
    }

    // Tests for -print-projectid
    @Test
    void testMainPrintProjectId() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-print-projectid", "access_output.txt"};
            Path expectedPath = Paths.get("access_output.txt");
            String expectedProjectId = "project_abc";

            mockedStatic.when(() -> PolyspaceHelpersUtils.getAccessResultProjectId(eq(expectedPath)))
                        .thenReturn(expectedProjectId);

            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.getAccessResultProjectId(eq(expectedPath)), times(1));
            assertEquals(expectedProjectId + System.lineSeparator(), outContent.toString());
        }
    }

    @Test
    void testMainPrintProjectIdInsufficientArgs() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-print-projectid"};
            PolyspaceHelpers.main(args);

            assertEquals("Usage: ps_helper -print-projectid <access upload output>" + System.lineSeparator(), outContent.toString());
            mockedStatic.verify(() -> PolyspaceHelpersUtils.getAccessResultProjectId(any(Path.class)), never());
        }
    }

    // Tests for -print-projecturl
    @Test
    void testMainPrintProjectUrl() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-print-projecturl", "access_output.txt", "http://access.example.com"};
            Path expectedPath = Paths.get("access_output.txt");
            String expectedAccessUrl = "http://access.example.com";
            String expectedProjectUrl = "http://access.example.com/project/123";


            mockedStatic.when(() -> PolyspaceHelpersUtils.getAccessResultUrl(eq(expectedPath), eq(expectedAccessUrl)))
                        .thenReturn(expectedProjectUrl);

            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.getAccessResultUrl(eq(expectedPath), eq(expectedAccessUrl)), times(1));
            assertEquals(expectedProjectUrl + System.lineSeparator(), outContent.toString());
        }
    }

    @Test
    void testMainPrintProjectUrlInsufficientArgs() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-print-projecturl", "access_output.txt"}; // Missing access_url
            PolyspaceHelpers.main(args);

            assertEquals("Usage: ps_helper -print-projecturl <access upload output> <access_url>" + System.lineSeparator(), outContent.toString());
            mockedStatic.verify(() -> PolyspaceHelpersUtils.getAccessResultUrl(any(Path.class), any(String.class)), never());
        }
    }

    // Tests for -report-status
    @Test
    void testMainReportStatus() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-status", "report.xml", "5"};
            Path expectedPath = Paths.get("report.xml");
            int expectedNbToFail = 5;
            String expectedStatus = "KO"; // Example, actual logic is in PolyspaceHelpersUtils

            // We don't need to mock the return value of getReportStatus itself for this test,
            // but we do need to mock any methods it might call if we were testing deeper.
            // For PolyspaceHelpers.main, we just care it calls the util method.
            // The actual output will be handled by the PolyspaceHelpers.main logic based on what getReportStatus returns.
            mockedStatic.when(() -> PolyspaceHelpersUtils.getReportStatus(eq(expectedPath), eq((long)expectedNbToFail)))
                        .thenReturn(expectedStatus);


            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.getReportStatus(eq(expectedPath), eq((long)expectedNbToFail)), times(1));
            assertEquals(expectedStatus + System.lineSeparator(), outContent.toString());
        }
    }

    @Test
    void testMainReportStatusInsufficientArgs() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-status"};
            PolyspaceHelpers.main(args);

            assertEquals("Usage: ps_helper -report-status <report> <nb_to_fail>" + System.lineSeparator(), outContent.toString());
            mockedStatic.verify(() -> PolyspaceHelpersUtils.getReportStatus(any(Path.class), any(Integer.class)), never());
        }
    }

    // Tests for -report-count-findings
    @Test
    void testMainReportCountFindings() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-count-findings", "report.xml"}; // No severities
            Path expectedPath = Paths.get("report.xml");
            long expectedTotalCount = 25L; // Example total count

            // This now refers to the single-argument getCountFindings
            mockedStatic.when(() -> PolyspaceHelpersUtils.getCountFindings(eq(expectedPath)))
                        .thenReturn(expectedTotalCount);

            PolyspaceHelpers.main(args);

            mockedStatic.verify(() -> PolyspaceHelpersUtils.getCountFindings(eq(expectedPath)), times(1));
            assertEquals(expectedTotalCount + System.lineSeparator(), outContent.toString());
        }
    }

    @Test
    void testMainReportCountFindingsInsufficientArgs() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-report-count-findings"}; // Report path missing
            PolyspaceHelpers.main(args);

            assertEquals("Usage: ps_helper -report-count-findings <report>" + System.lineSeparator(), outContent.toString());
            // Verify the single-argument getCountFindings was not called
            mockedStatic.verify(() -> PolyspaceHelpersUtils.getCountFindings(any(Path.class)), never());
        }
    }

    // Tests for general usage messages
    @Test
    void testMainNoArgsUsage() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {};
            PolyspaceHelpers.main(args);

            String expectedOutput = "Usage: ps_helper -report-filter <original_report> <filtered_report> [<owner>] [<title> <value>]+" + System.lineSeparator() +
                                  "Usage: ps_helper -report-status <report> <nb_to_fail>" + System.lineSeparator() +
                                  "Usage: ps_helper -report-count-findings <report>" + System.lineSeparator() +
                                  "Usage: ps_helper -print-runid <access upload output>" + System.lineSeparator() +
                                  "Usage: ps_helper -print-projectid <access upload output>" + System.lineSeparator() +
                                  "Usage: ps_helper -print-projecturl <access upload output> <access_url>" + System.lineSeparator();
            assertEquals(expectedOutput, outContent.toString());

            mockedStatic.verifyNoInteractions();
        }
    }

    @Test
    void testMainUnknownArgUsage() throws Exception {
        try (MockedStatic<PolyspaceHelpersUtils> mockedStatic = Mockito.mockStatic(PolyspaceHelpersUtils.class)) {
            String[] args = {"-unknown-command"};
            PolyspaceHelpers.main(args);

            String expectedOutput = "Usage: ps_helper -report-filter <original_report> <filtered_report> [<owner>] [<title> <value>]+" + System.lineSeparator() +
                                  "Usage: ps_helper -report-status <report> <nb_to_fail>" + System.lineSeparator() +
                                  "Usage: ps_helper -report-count-findings <report>" + System.lineSeparator() +
                                  "Usage: ps_helper -print-runid <access upload output>" + System.lineSeparator() +
                                  "Usage: ps_helper -print-projectid <access upload output>" + System.lineSeparator() +
                                  "Usage: ps_helper -print-projecturl <access upload output> <access_url>" + System.lineSeparator();
            assertEquals(expectedOutput, outContent.toString());

            mockedStatic.verifyNoInteractions();
        }
    }
}
