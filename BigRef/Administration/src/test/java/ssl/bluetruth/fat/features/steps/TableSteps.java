/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import com.google.common.base.Predicate;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

/**
 *
 * @author pwood
 */
public class TableSteps {

    private WebDriver browser;

    public TableSteps(SharedState sharedState) throws MalformedURLException {
        browser = sharedState.getBrowser();
    }

    @Then("^a table has columns:$")
    public void a_table_has_columns(DataTable expectedColumns) throws Throwable {
        List<List<String>> actualHeaders = new ArrayList<List<String>>();

        final List<WebElement> tableHeaders = browser.findElements(By.xpath("//thead//tr//th/div"));
        for (WebElement tableHeader : tableHeaders) {
            actualHeaders.add(Arrays.asList(tableHeader.getText()));
        }
        expectedColumns.diff(actualHeaders);
    }

    @Then("^the table column (.*) is visible$")
    public void the_table_column_is_visible(String column) throws Throwable {
        final String xpath = "//thead//tr//th/div[contains(text(),'" + column + "')]";
        assertNotNull(browser.findElement(By.xpath(xpath)));
    }

    @Then("^the table column (.*) is not visible$")
    public void the_table_column_is_not_visible(String column) throws Throwable {
        final String xpath = "//thead//tr//th/div[contains(text(),'" + column + "')]";
        try {
            browser.findElement(By.xpath(xpath));
            fail("Column '" + column + "' shouldn't exist");
        } catch (WebDriverException wde) {
        }
    }

    @Then("^the table has (\\d+) rows?$")
    public void the_table_has_rows(int expectedRowCount) throws Throwable {
        waitUntil(tableHasRows(expectedRowCount));
    }

    @Then("^the table has (\\d+) rows?:$")
    public void the_table_has_rows(int expectedRowCount, DataTable datatable) throws Throwable {
        waitUntil(tableHasRows(expectedRowCount));
       
        List<List<String>> table = new ArrayList<List<String>>();
        List<WebElement> rows = browser.findElements(By.xpath("//table/tbody/tr"));
        for (WebElement row : rows) {
            List<WebElement> tds = row.findElements(By.xpath("td"));
            List<String> tdTexts = new ArrayList<String>();
            for (WebElement td : tds) {
                tdTexts.add(td.getText());
            }
            table.add(tdTexts);
        }
        datatable.diff(table);
    }

    @Then("^column \"(.*)\" is:$")
    public void column_is(String column, DataTable expectedValues) throws Throwable {
        String xpath =
                "//table/tbody/tr/td[count(//table/thead/tr/th[.='" + column + "']/preceding-sibling::th)+1]";

        List<List<String>> actualValues = new ArrayList<List<String>>();

        final List<WebElement> tds = browser.findElements(By.xpath(xpath));
        for (WebElement td : tds) {
            actualValues.add(Arrays.asList(td.getText()));
        }
        expectedValues.diff(actualValues);
    }

    private Predicate<WebDriver> tableHasRows(final int expectedRows) {
        return new Predicate<WebDriver>() {
            public boolean apply(WebDriver browser) {
                final List<WebElement> rows = browser.findElements(By.xpath("//tbody/tr"));
                int totalRows = 0;
                for (WebElement row : rows) {
                    if (row.isDisplayed()) {
                        totalRows++;
                    }
                }
                return totalRows == expectedRows;
            }
        };
    }

    private void waitUntil(Predicate<WebDriver> isTrue) {
        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(browser)
                .withTimeout(10, TimeUnit.SECONDS)
                .pollingEvery(1, TimeUnit.SECONDS)
                .ignoring(WebDriverException.class);

        wait.until(isTrue);
    }
}
