package com.mathworks.polyspace.jenkins;

import com.mathworks.polyspace.jenkins.utils.PolyspaceHelpersUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PolyspaceHelpers.class, PolyspaceHelpersUtils.class})
public class PolyspaceHelpersTest {

    @Mock
    private PolyspaceHelpersUtils mockUtils;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String USAGE_REPORT_FILTER = "Usage: ps_helper -report-filter <original_report> <filtered_report> [<owner>] [<title> <value>]+" + System.lineSeparator();
    private static final String USAGE_REPORT_STATUS = "Usage: ps_helper -report-status <report_file> <max_findings_threshold>" + System.lineSeparator();
    private static final String USAGE_REPORT_COUNT_FINDINGS = "Usage: ps_helper -report-count-findings <report_file>" + System.lineSeparator();
    private static final String USAGE_PRINT_RUNID = "Usage: ps_helper -print-runid <polyspace_upload_output_file>" + System.lineSeparator();
    private static final String USAGE_PRINT_PROJECTID = "Usage: ps_helper -print-projectid <polyspace_upload_output_file>" + System.lineSeparator();
    private static final String USAGE_PRINT_PROJECTURL = "Usage: ps_helper -print-projecturl <polyspace_upload_output_file> <polyspace_server_url>" + System.lineSeparator();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        PowerMockito.whenNew(PolyspaceHelpersUtils.class).withAnyArguments().thenReturn(mockUtils);
    }

    @Test
    public void testMainReportFilter() throws IOException {
        String[] args = {"-report-filter", "original.txt", "filtered.txt", "owner1", "Module", "moduleA"};
        PolyspaceHelpers.main(args);
        verify(mockUtils).reportFilter(eq(Paths.get("original.txt")), eq(Paths.get("filtered.txt")), eq("owner1"), eq(new String[]{"Module", "moduleA"}));
    }

    @Test
    public void testMainReportFilterNoOwner() throws IOException {
        String[] args = {"-report-filter", "original.txt", "filtered.txt", "Module", "moduleA"};
        PolyspaceHelpers.main(args);
        verify(mockUtils).reportFilter(eq(Paths.get("original.txt")), eq(Paths.get("filtered.txt")), eq(""), eq(new String[]{"Module", "moduleA"}));
    }

    @Test
    public void testMainReportFilterUnderscore() throws IOException {
        String[] args = {"report_filter", "original.txt", "filtered.txt", "owner1", "Module", "moduleA"};
        PolyspaceHelpers.main(args);
        verify(mockUtils).reportFilter(eq(Paths.get("original.txt")), eq(Paths.get("filtered.txt")), eq("owner1"), eq(new String[]{"Module", "moduleA"}));
    }

    @Test
    public void testMainReportStatus() throws IOException {
        String[] args = {"-report-status", "report.txt", "10"};
        when(mockUtils.getReportStatus(any(Path.class), anyLong())).thenReturn("SUCCESS");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getReportStatus(eq(Paths.get("report.txt")), eq(10L));
        assertEquals("SUCCESS" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainReportStatusUnderscore() throws IOException {
        String[] args = {"report_status", "report.txt", "10"};
        when(mockUtils.getReportStatus(any(Path.class), anyLong())).thenReturn("FAILURE"); // Changed for variety
        PolyspaceHelpers.main(args);
        verify(mockUtils).getReportStatus(eq(Paths.get("report.txt")), eq(10L));
        assertEquals("FAILURE" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainReportCountFindings() throws IOException {
        String[] args = {"-report-count-findings", "report.txt"};
        when(mockUtils.getCountFindings(any(Path.class))).thenReturn(5L);
        PolyspaceHelpers.main(args);
        verify(mockUtils).getCountFindings(eq(Paths.get("report.txt")));
        assertEquals("5" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainReportCountFindingsUnderscore() throws IOException {
        String[] args = {"report_count_findings", "report.txt"};
        when(mockUtils.getCountFindings(any(Path.class))).thenReturn(15L);
        PolyspaceHelpers.main(args);
        verify(mockUtils).getCountFindings(eq(Paths.get("report.txt")));
        assertEquals("15" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainPrintRunId() throws IOException {
        String[] args = {"-print-runid", "upload_output.txt"};
        when(mockUtils.getAccessResultRunId(any(Path.class))).thenReturn("run123");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getAccessResultRunId(eq(Paths.get("upload_output.txt")));
        assertEquals("run123" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainPrintRunIdUnderscore() throws IOException {
        String[] args = {"print_runid", "upload_output.txt"};
        when(mockUtils.getAccessResultRunId(any(Path.class))).thenReturn("run456");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getAccessResultRunId(eq(Paths.get("upload_output.txt")));
        assertEquals("run456" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainPrintProjectId() throws IOException {
        String[] args = {"-print-projectid", "upload_output.txt"};
        when(mockUtils.getAccessResultProjectId(any(Path.class))).thenReturn("project789");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getAccessResultProjectId(eq(Paths.get("upload_output.txt")));
        assertEquals("project789" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainPrintProjectIdUnderscore() throws IOException {
        String[] args = {"print_projectid", "upload_output.txt"};
        when(mockUtils.getAccessResultProjectId(any(Path.class))).thenReturn("projectABC");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getAccessResultProjectId(eq(Paths.get("upload_output.txt")));
        assertEquals("projectABC" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainPrintProjectUrl() throws IOException {
        String[] args = {"-print-projecturl", "upload_output.txt", "http://example.com"};
        when(mockUtils.getAccessResultUrl(any(Path.class), anyString())).thenReturn("http://example.com/project/123");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getAccessResultUrl(eq(Paths.get("upload_output.txt")), eq("http://example.com"));
        assertEquals("http://example.com/project/123" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainPrintProjectUrlUnderscore() throws IOException {
        String[] args = {"print_projecturl", "upload_output.txt", "http://newexample.com"};
        when(mockUtils.getAccessResultUrl(any(Path.class), anyString())).thenReturn("http://newexample.com/project/456");
        PolyspaceHelpers.main(args);
        verify(mockUtils).getAccessResultUrl(eq(Paths.get("upload_output.txt")), eq("http://newexample.com"));
        assertEquals("http://newexample.com/project/456" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainNoArgs() throws IOException {
        String[] args = {};
        PolyspaceHelpers.main(args);
        String expectedOutput = USAGE_REPORT_FILTER +
                                USAGE_REPORT_STATUS +
                                USAGE_REPORT_COUNT_FINDINGS +
                                USAGE_PRINT_RUNID +
                                USAGE_PRINT_PROJECTID +
                                USAGE_PRINT_PROJECTURL;
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testMainInvalidArg() throws IOException {
        String[] args = {"-invalid-arg"};
        PolyspaceHelpers.main(args);
        String expectedOutput = USAGE_REPORT_FILTER +
                                USAGE_REPORT_STATUS +
                                USAGE_REPORT_COUNT_FINDINGS +
                                USAGE_PRINT_RUNID +
                                USAGE_PRINT_PROJECTID +
                                USAGE_PRINT_PROJECTURL;
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testMainReportFilterMissingArgs() throws IOException {
        String[] args = {"-report-filter", "original.txt"}; // Not enough arguments
        PolyspaceHelpers.main(args);
        assertEquals(USAGE_REPORT_FILTER, outContent.toString());
    }

    // Add similar short arg tests for other commands
    @Test
    public void testMainReportStatusMissingArgs() throws IOException {
        String[] args = {"-report-status", "report.txt"}; // Not enough arguments
        PolyspaceHelpers.main(args);
        assertEquals(USAGE_REPORT_STATUS, outContent.toString());
    }

    @Test
    public void testMainReportCountFindingsMissingArgs() throws IOException {
        String[] args = {"-report-count-findings"}; // Not enough arguments
        PolyspaceHelpers.main(args);
        assertEquals(USAGE_REPORT_COUNT_FINDINGS, outContent.toString());
    }

    @Test
    public void testMainPrintRunIdMissingArgs() throws IOException {
        String[] args = {"-print-runid"}; // Not enough arguments
        PolyspaceHelpers.main(args);
        assertEquals(USAGE_PRINT_RUNID, outContent.toString());
    }

    @Test
    public void testMainPrintProjectIdMissingArgs() throws IOException {
        String[] args = {"-print-projectid"}; // Not enough arguments
        PolyspaceHelpers.main(args);
        assertEquals(USAGE_PRINT_PROJECTID, outContent.toString());
    }

    @Test
    public void testMainPrintProjectUrlMissingArgs() throws IOException {
        String[] args = {"-print-projecturl", "output.txt"}; // Not enough arguments
        PolyspaceHelpers.main(args);
        assertEquals(USAGE_PRINT_PROJECTURL, outContent.toString());
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}
