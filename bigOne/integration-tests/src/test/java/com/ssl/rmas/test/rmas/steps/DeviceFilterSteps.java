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

package com.ssl.rmas.test.rmas.steps;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.HtmlDeviceFilterUtils;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlTableUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DeviceFilterSteps {

    private static final List<String> EXPECTED_DEVICES = Arrays.asList("10.163.49.68", "1.1.1.68");
    private static final List<String> NON_EXPECTED_DEVICES = Arrays.asList("1.1.1.1", "1.1.1.2", "1.1.1.3", "1.1.1.4");
    private static final String MANTENANCE_CONTRACT_REGION_FILTER_LABEL = "Maintenance region";

    @Autowired
    private HtmlButtonUtils htmlButtonUtils;
    @Autowired
    private HtmlDeviceFilterUtils htmlDeviceFilterUtils;
    @Autowired
    private HtmlTableUtils htmlTableUtils;

    @When("^I filter the device list$")
    public void filterDevices() {
        htmlDeviceFilterUtils.set(0, MANTENANCE_CONTRACT_REGION_FILTER_LABEL, "South West");
        htmlButtonUtils.clickOfClass(HtmlButtonUtils.PLUS_BUTTON_CLASS);
        htmlDeviceFilterUtils.set(1, MANTENANCE_CONTRACT_REGION_FILTER_LABEL, "North West");
        htmlButtonUtils.click(HtmlButtonUtils.SEARCH_BUTTON);
    }

    @Then("^I should only see the filtered devices$")
    public void checkDevices() throws TestTimeoutException {
        htmlTableUtils.waitforNumberOfRows(EXPECTED_DEVICES.size(), 5);
        htmlTableUtils.assertRowsPresent(EXPECTED_DEVICES);
        htmlTableUtils.assertRowsNotPresent(NON_EXPECTED_DEVICES);
    }
}
