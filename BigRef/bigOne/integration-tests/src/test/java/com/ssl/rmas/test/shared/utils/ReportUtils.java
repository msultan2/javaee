/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.test.shared.utils;

import cucumber.api.Scenario;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Utility class to create report sheet
 */
public class ReportUtils implements SmartLifecycle {

    private int rowNumber = 0;
    private Map<Integer, String[]> testresultdata_rait;
    private Map<Integer, String[]> testresultdata_rmas;
    private boolean implemented = false;
    private Scenario scenario;
    private final String pathForRaitReportSheet;
    private final String pathForRmasReportSheet;
    private final String raitProjectName;
    private final String rmasProjectName;
    private final String columnHeader_1 = "Scenario Description";
    private final String columnHeader_2 = "Actual Result";
    private final String columnHeader_3 = "Jira Reference";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ReportUtils.class);

    public ReportUtils() {
        this.pathForRaitReportSheet = System.getProperty("test.report.rait.path", "target/test-results/rait/Test_report.xls");
        this.raitProjectName = System.getProperty("project.rait", "RAIT");
        this.pathForRmasReportSheet = System.getProperty("test.report.rmas.path", "target/test-results/rmas/Test_report.xls");
        this.rmasProjectName = System.getProperty("project.name", "RMAS");
    }

    @Before("@RAIT , @RMAS")
    public void intializeScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    @After("@RAIT , @RMAS")
    public void printReport() {
        scenario.write("Status: " + scenario.getStatus() + ", Please refer to Jira issue \"" + getJiraNumber() + "\" for acceptance criteria");
        if (getTag(raitProjectName).contains(raitProjectName)) {
            testresultdata_rait.put(rowNumber++, new String[]{scenario.getName(), scenario.getStatus(), getJiraNumber()});
        } else if (getTag(rmasProjectName).contains(rmasProjectName)) {
            testresultdata_rmas.put(rowNumber++, new String[]{scenario.getName(), scenario.getStatus(), getJiraNumber()});
        }
    }

    @Override
    public void start() {
        //do nothing, Implemented for Life cycle interface
    }

    @Override
    public boolean isRunning() {
        return implemented;
    }

    public List<String> getListsOfTags() {
        Collection<String> tag = scenario.getSourceTagNames();
        List<String> tagsList = new ArrayList<>(tag);
        return tagsList;
    }

    public String getTag(String tagName) {
        for (String tagList : getListsOfTags()) {
            if (tagList.equals("@" + tagName)) {
                return tagList;
            }
        }
        return "No tag found";
    }

    public void createSheet(Map<Integer, String[]> data, String path) throws FileNotFoundException, IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Result");
        int rowNum = 0;
        for (Map.Entry<Integer, String[]> map : data.entrySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            String[] strArr = map.getValue();
            short cellNum = 0;
            for (String text : strArr) {
                HSSFCell cell = row.createCell(cellNum++);
                cell.setCellValue(text);
            }
        }
        FileOutputStream file = new FileOutputStream(new File(path));
        workbook.write(file);
        file.close();

    }

    @Override
    public void stop() {
        if (getTag(raitProjectName).contains(raitProjectName)) {
            try {
                createSheet(testresultdata_rait, pathForRaitReportSheet);
            } catch (IOException ex) {
                logger.info("Attempt to create outputstream file failed", ex);
            }
        } else if (getTag(rmasProjectName).contains(rmasProjectName)) {
            try {
                createSheet(testresultdata_rmas, pathForRmasReportSheet);
            } catch (IOException ex) {
                logger.info("Attempt to create outputstream file failed", ex);
            }
        }
        implemented = true;

    }

    public void storeDataInColumns(Map<Integer, String[]> results) {
        results.put(rowNumber++, new String[]{columnHeader_1, columnHeader_2, columnHeader_3});
    }

    @Override
    public boolean isAutoStartup() {
        testresultdata_rait = new LinkedHashMap<>();
        storeDataInColumns(testresultdata_rait);
        testresultdata_rmas = new LinkedHashMap<>();
        storeDataInColumns(testresultdata_rmas);
        implemented = true;
        return true;
    }

    @Override
    public void stop(Runnable r) {
        stop();
    }

    @Override
    public int getPhase() {
        //do nothing, Implemented for Life cycle interface
        return 0;
    }

    public String getJiraNumber() {
        for (String tag : getListsOfTags()) {
            if (tag.contains(rmasProjectName + "-")) {
                return tag.replace("@", "");
            }
        }
        return "Jira reference number not found";
    }

}
