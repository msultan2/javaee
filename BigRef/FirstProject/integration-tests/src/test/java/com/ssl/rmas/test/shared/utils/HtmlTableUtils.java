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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.test.shared.utils;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.SeleniumWebDriverHelper.WebElementHelper;
import com.ssl.cukes.TestTimeoutException;

@Component
public class HtmlTableUtils {

    private static final String KEY_HEADER_NAME = "IP address";
    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;

    public void assertHeadersPresent(List<String> headerNames) {
        for (String headerName : headerNames) {
            assertTrue(webDriverHelper.findElement(getTableXPath()+"//th[text()='" + headerName + "']").isDisplayed());
        }
    }

    public void assertNumberOfColumns(int numberOfColumns) {
        assertEquals("Incorrect number of columns", numberOfColumns, getNumberOfColumns());
    }

    private int getNumberOfColumns() {
        return webDriverHelper.findElements(getTableXPath()+"//th").size();
    }

    public void assertNumberOfRows(int numberOfRows) {
        assertEquals("Incorrect number of rows", numberOfRows, getNumberOfRows());
    }

    public int getNumberOfRows() {
        return webDriverHelper.findElements(getTableXPath()+"/tbody/tr").size();
    }

    public void assertRowsPresent(List<String> textsPresentInRows) throws TestTimeoutException {
        for (String textPresentInRows : textsPresentInRows) {
            assertTrue(webDriverHelper.findElement(getTableXPath()+"//tr//*[text()='" + textPresentInRows + "']").isDisplayed());
        }
    }

    public void assertRowsNotPresent(List<String> textsNotPresentInRows) {
        for (String textsPresentInRows : textsNotPresentInRows) {
            try {
                webDriverHelper.findElement(getTableXPath()+"//tr//*[text()='" + textsPresentInRows + "']");
            } catch (NoSuchElementException | AssertionError ignored) {
                return;
            }
            Assert.fail("Row with "+textsPresentInRows+" was not expected");
        }
    }

    public void waitforNumberOfRows(int rowCount, int timeout) throws TestTimeoutException {
        webDriverHelper.waitForElementCount(getTableXPath()+"//tbody//tr", rowCount, timeout);
    }

    public void assertRowPresent(List<String> row) {
        assertTrue(getRows().stream().anyMatch(row::equals));
    }

    private List<List<String>> getRows() {
        return IntStream.rangeClosed(1, getNumberOfRows())
                .mapToObj(this::getRow)
                .collect(toList());
    }

    private List<String> getRow(final int rowNumber) {
        return IntStream.rangeClosed(1, getNumberOfColumns())
                .mapToObj(columnNumber -> getCellText(rowNumber, columnNumber))
                .collect(toList());
    }

    private String getCellText(final int rowNumber, final int columnNumber) {
        return webDriverHelper.findElement(getTableXPath()+"//tbody//tr[" + rowNumber + "]//td[" + columnNumber + "]").getText();
    }

    public void assertNoRowsPresent() {
        String xPath = getTableXPath()+"/tbody[not(tr)]";
        webDriverHelper.waitForElement(xPath, 0, 5);
    }

    public void assertFullColumnExcept(List<String> rowNames, String columnName, String exception, List<Integer> values) {
        IntStream.range(0, rowNames.size()).forEach(i -> {
            if(!rowNames.get(i).equalsIgnoreCase(exception)) {
                assertThat(getIntFromInput(rowNames.get(i),columnName),is(values.get(i)));
            }
        });
    }

    public void assertFullColumn(List<String> rowNames, String columnName, List<Integer> values) {
        assertFullColumnExcept(rowNames, columnName, null, values);
    }

    public void assertElementIsEmpty(String rowName, String columnName) {
        assertThat(getElement(rowName, columnName, "").getAttribute("textContent"),is(""));
    }

    public void assertInputsEditableIs(String headerName1, String headerName2, boolean editable) {
        List<WebElementHelper> inputElements = getInputElements(headerName1, headerName2);
        for(WebElementHelper inputElement: inputElements) {
            assertThat(inputElement.isEnabled(), is(equalTo(editable)));
        }
    }

    public Optional<Integer> getColumnNumber(String headerName) {
        List<WebElementHelper> header_elements = webDriverHelper.findElements(getTableXPath()+"//th[contains(.,'')]");
        int columnNumber = 1;
        for (WebElementHelper header_element : header_elements) {
            if (header_element.getText().equals(headerName)) {
                return Optional.of(columnNumber);
            }
            columnNumber++;
        }
        return Optional.empty();
    }

    public Optional<Integer> getRowNumber(String rowName) {
    	Optional<Integer> columnNumber = getColumnNumber(KEY_HEADER_NAME);
        if(columnNumber.isPresent()) {
            List<WebElementHelper> body_elements = webDriverHelper.findElements(getTableXPath()+"//tbody//td[" + columnNumber.get() + "]");
            int rowNumber = 1;
            for (WebElementHelper body_element : body_elements) {
                String body_text = body_element.getText();
                if (body_text.equals(rowName)) {
                    return Optional.of(rowNumber);
                }
                rowNumber++;
            }
        } else {
        	Assert.fail("Header \""+KEY_HEADER_NAME+"\" not found");
        }
        return Optional.empty();
    }

    public int getIntFromInput(String rowName, String headerName) {
        return Integer.valueOf(getStringFromInput(rowName, headerName));
    }

    public int getIntFromSpan(String rowName, String headerName) {
        return Integer.valueOf(getStringFromSpan(rowName, headerName));
    }

    public String getStringFromInput(String rowName, String headerName) {
        return getElement(rowName, headerName, "//input").getAttribute("value");
    }

    public String getStringFromSpan(String rowName, String headerName) {
        return getElement(rowName, headerName, "//span").getAttribute("textContent");
    }

    private WebElementHelper getElement(int row_number, int column_number, String element) {
        return webDriverHelper.findElement(getTableXPath()+"//tbody//tr["+row_number+"]//td["+column_number+"]"+element);
    }

    private List<WebElementHelper> getInputElements(int columnNumber1, int columnNumber2) {
        return webDriverHelper.findElements(getTableXPath()+"//tbody//tr//td[position()="+columnNumber1+" or position()="+columnNumber2+"]/input");
    }

    public WebElementHelper getElement(String rowName, String headerName, String element) {
        Optional<Integer> row_number = getRowNumber(rowName);
        if(!row_number.isPresent()) {
        	Assert.fail("Row " +rowName+" not found");
        }
        Optional<Integer> column_number = getColumnNumber(headerName);
        if(!column_number.isPresent()) {
        	Assert.fail("Header " +headerName+" not found");
        }
        return getElement(row_number.get(), column_number.get(), element);
    }

    public List<WebElementHelper> getInputElements(String headerName1, String headerName2) {
    	Optional<Integer> columnNumber1 = getColumnNumber(headerName1);
    	if(!columnNumber1.isPresent()) {
    		Assert.fail("Header " +headerName1+" not found");
    	}
        Optional<Integer> columnNumber2 = getColumnNumber(headerName2);
        if(!columnNumber2.isPresent()) {
        	Assert.fail("Header " +headerName2+" not found");
        }
        return getInputElements(columnNumber1.get(), columnNumber2.get());
    }

    public void set(String rowName, String headerName, String value) {
        WebElementHelper element = getElement(rowName, headerName, "//input");
        element.click().clear().sendKeys(value);
    }

    public void set(String rowName, String headerName, int value) {
        set(rowName, headerName, String.valueOf(value));
    }

    public void setFullColumnExcept(List<String> rowNames, String columnName, String exception, List<Integer> values) {
        IntStream.range(0, rowNames.size()).forEach(i -> {
            if(!rowNames.get(i).equalsIgnoreCase(exception)) {
                set(rowNames.get(i), columnName, values.get(i));
            }
        });
    }

    public void setFullColumn(List<String> rowNames, String columnName, List<Integer> values) {
        setFullColumnExcept(rowNames, columnName, null, values);
    }

    public void waitForLastRow(List<String> rowNames) {
        String xPath = getTableXPath()+"//tbody//td[contains(text(),'"+rowNames.get(rowNames.size()-1)+"')]";
        webDriverHelper.waitForElement(xPath, 0, 5);
    }

    protected String getTableXPath() {
        return "//table[@class='table table-striped']";
    }

    public void clickOnRow(String rowText) {
        String xPath = getTableXPath()+"//tr[contains(..,'"+rowText+"')]";
        webDriverHelper.findElement(xPath).click();
    }
}
