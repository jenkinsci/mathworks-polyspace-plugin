package com.mathworks.polyspace.jenkins;

import com.mathworks.polyspace.jenkins.utils.PolyspaceHelpersUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolyspaceHelpersTest {

    @Mock
    private PolyspaceHelpersUtils mockUtils;

    private PolyspaceHelpers polyspaceHelpersInstance;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String USAGE_REPORT_FILTER = "Usage: ps_helper -report-filter <original_report> <filtered_report> [<owner>] [<title> <value>]+" + System.lineSeparator();
    private static final String USAGE_REPORT_STATUS = "Usage: ps_helper -report-status <report> <nb_to_fail>" + System.lineSeparator();
    private static final String USAGE_REPORT_COUNT_FINDINGS = "Usage: ps_helper -report-count-findings <report>" + System.lineSeparator();
    private static final String USAGE_PRINT_RUNID = "Usage: ps_helper -print-runid <access upload output>" + System.lineSeparator();
    private static final String USAGE_PRINT_PROJECTID = "Usage: ps_helper -print-projectid <access upload output>" + System.lineSeparator();
    private static final String USAGE_PRINT_PROJECTURL = "Usage: ps_helper -print-projecturl <access upload output> <access_url>" + System.lineSeparator();

    @BeforeEach
    public void setUp() {
        // MockitoExtension handles mockUtils initialization via @Mock
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        polyspaceHelpersInstance = new PolyspaceHelpers(mockUtils);
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testReportFilter() throws IOException {
        String[] args = {"-report-filter", "original.txt", "filtered.txt", "owner1", "Module", "moduleA"};
        polyspaceHelpersInstance.reportFilter(args);
        verify(mockUtils).reportFilter(eq(Paths.get("original.txt")), eq(Paths.get("filtered.txt")), eq("owner1"), eq(new String[]{"Module", "moduleA"}));
    }

    @Test
    void testReportFilterNoOwner() throws IOException {
        String[] args = {"-report-filter", "original.txt", "filtered.txt", "Module", "moduleA"};
        polyspaceHelpersInstance.reportFilter(args);
        verify(mockUtils).reportFilter(eq(Paths.get("original.txt")), eq(Paths.get("filtered.txt")), eq(""), eq(new String[]{"Module", "moduleA"}));
    }

    @Test
    void testReportFilterUnderscore() throws IOException {
        String[] args = {"report_filter", "original.txt", "filtered.txt", "owner1", "Module", "moduleA"};
        polyspaceHelpersInstance.reportFilter(args);
        verify(mockUtils).reportFilter(eq(Paths.get("original.txt")), eq(Paths.get("filtered.txt")), eq("owner1"), eq(new String[]{"Module", "moduleA"}));
    }

    @Test
    void testReportStatus() throws IOException {
        String[] args = {"-report-status", "report.txt", "10"};
        when(mockUtils.getReportStatus(any(Path.class), anyLong())).thenReturn("SUCCESS");
        polyspaceHelpersInstance.reportStatus(args);
        verify(mockUtils).getReportStatus(eq(Paths.get("report.txt")), eq(10L));
        assertEquals("SUCCESS" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testReportStatusUnderscore() throws IOException {
        String[] args = {"report_status", "report.txt", "10"};
        when(mockUtils.getReportStatus(any(Path.class), anyLong())).thenReturn("FAILURE");
        polyspaceHelpersInstance.reportStatus(args);
        verify(mockUtils).getReportStatus(eq(Paths.get("report.txt")), eq(10L));
        assertEquals("FAILURE" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testReportCountFindings() throws IOException {
        String[] args = {"-report-count-findings", "report.txt"};
        when(mockUtils.getCountFindings(any(Path.class))).thenReturn(5L);
        polyspaceHelpersInstance.reportCountFindings(args);
        verify(mockUtils).getCountFindings(eq(Paths.get("report.txt")));
        assertEquals("5" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testReportCountFindingsUnderscore() throws IOException {
        String[] args = {"report_count_findings", "report.txt"};
        when(mockUtils.getCountFindings(any(Path.class))).thenReturn(15L);
        polyspaceHelpersInstance.reportCountFindings(args);
        verify(mockUtils).getCountFindings(eq(Paths.get("report.txt")));
        assertEquals("15" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPrintRunId() throws IOException {
        String[] args = {"-print-runid", "upload_output.txt"};
        when(mockUtils.getAccessResultRunId(any(Path.class))).thenReturn("run123");
        polyspaceHelpersInstance.printRunId(args);
        verify(mockUtils).getAccessResultRunId(eq(Paths.get("upload_output.txt")));
        assertEquals("run123" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPrintRunIdUnderscore() throws IOException {
        String[] args = {"print_runid", "upload_output.txt"};
        when(mockUtils.getAccessResultRunId(any(Path.class))).thenReturn("run456");
        polyspaceHelpersInstance.printRunId(args);
        verify(mockUtils).getAccessResultRunId(eq(Paths.get("upload_output.txt")));
        assertEquals("run456" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPrintProjectId() throws IOException {
        String[] args = {"-print-projectid", "upload_output.txt"};
        when(mockUtils.getAccessResultProjectId(any(Path.class))).thenReturn("project789");
        polyspaceHelpersInstance.printProjectId(args);
        verify(mockUtils).getAccessResultProjectId(eq(Paths.get("upload_output.txt")));
        assertEquals("project789" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPrintProjectIdUnderscore() throws IOException {
        String[] args = {"print_projectid", "upload_output.txt"};
        when(mockUtils.getAccessResultProjectId(any(Path.class))).thenReturn("projectABC");
        polyspaceHelpersInstance.printProjectId(args);
        verify(mockUtils).getAccessResultProjectId(eq(Paths.get("upload_output.txt")));
        assertEquals("projectABC" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPrintProjectUrl() throws IOException {
        String[] args = {"-print-projecturl", "upload_output.txt", "http://example.com"};
        when(mockUtils.getAccessResultUrl(any(Path.class), anyString())).thenReturn("http://example.com/project/123");
        polyspaceHelpersInstance.printProjectUrl(args);
        verify(mockUtils).getAccessResultUrl(eq(Paths.get("upload_output.txt")), eq("http://example.com"));
        assertEquals("http://example.com/project/123" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPrintProjectUrlUnderscore() throws IOException {
        String[] args = {"print_projecturl", "upload_output.txt", "http://newexample.com"};
        when(mockUtils.getAccessResultUrl(any(Path.class), anyString())).thenReturn("http://newexample.com/project/456");
        polyspaceHelpersInstance.printProjectUrl(args);
        verify(mockUtils).getAccessResultUrl(eq(Paths.get("upload_output.txt")), eq("http://newexample.com"));
        assertEquals("http://newexample.com/project/456" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testReportFilterMissingArgs() throws IOException {
        String[] args = {"-report-filter", "original.txt"};
        polyspaceHelpersInstance.reportFilter(args);
        assertEquals(USAGE_REPORT_FILTER, outContent.toString());
    }

    @Test
    void testReportStatusMissingArgs() throws IOException {
        String[] args = {"-report-status", "report.txt"};
        polyspaceHelpersInstance.reportStatus(args);
        assertEquals(USAGE_REPORT_STATUS, outContent.toString());
    }

    @Test
    void testReportCountFindingsMissingArgs() throws IOException {
        String[] args = {"-report-count-findings"};
        polyspaceHelpersInstance.reportCountFindings(args);
        assertEquals(USAGE_REPORT_COUNT_FINDINGS, outContent.toString());
    }

    @Test
    void testPrintRunIdMissingArgs() throws IOException {
        String[] args = {"-print-runid"};
        polyspaceHelpersInstance.printRunId(args);
        assertEquals(USAGE_PRINT_RUNID, outContent.toString());
    }

    @Test
    void testPrintProjectIdMissingArgs() throws IOException {
        String[] args = {"-print-projectid"};
        polyspaceHelpersInstance.printProjectId(args);
        assertEquals(USAGE_PRINT_PROJECTID, outContent.toString());
    }

    @Test
    void testPrintProjectUrlMissingArgs() throws IOException {
        String[] args = {"-print-projecturl", "output.txt"};
        polyspaceHelpersInstance.printProjectUrl(args);
        assertEquals(USAGE_PRINT_PROJECTURL, outContent.toString());
    }

    @Test
    void testMainCalledWithNoArguments() throws IOException {
        String[] args = {};
        PolyspaceHelpers.main(args); // Directly call the static main method

        String expectedOutput = USAGE_REPORT_FILTER +
                                USAGE_REPORT_STATUS +
                                USAGE_REPORT_COUNT_FINDINGS +
                                USAGE_PRINT_RUNID +
                                USAGE_PRINT_PROJECTID +
                                USAGE_PRINT_PROJECTURL;
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void testMainCalledWithInvalidArguments() throws IOException {
        String[] args = {"-some-invalid-command"};
        PolyspaceHelpers.main(args); // Directly call the static main method

        String expectedOutput = USAGE_REPORT_FILTER +
                                USAGE_REPORT_STATUS +
                                USAGE_REPORT_COUNT_FINDINGS +
                                USAGE_PRINT_RUNID +
                                USAGE_PRINT_PROJECTID +
                                USAGE_PRINT_PROJECTURL;
        assertEquals(expectedOutput, outContent.toString());
    }
}
