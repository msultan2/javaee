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

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class DeviceOperationSteps {

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private DeviceListSteps deviceListSteps;

    @Given("^I have navigated to the device landing page$")
    public void goToDeviceLandingPage() throws IOException, InterruptedException, TestTimeoutException {
        deviceListSteps.goToDeviceListPage();
        deviceListSteps.goToDevicePage();
        checkFixedDetails();
    }

    @Then("^I will be able to see the updated device details$")
    public void checkDetails() throws IOException, InterruptedException {
        checkFixedDetails();
        checkUpdatedDetails();
    }

    private void checkFixedDetails() {
        htmlUtils.assertTitleText(DeviceListSteps.DEVICE_TITLE, DeviceListSteps.TITLE_SIZE_1);
        htmlUtils.assertPanelHeading(DeviceListSteps.EXPECTED_LABEL);
        htmlUtils.assertDivText("IP address: 10.163.49.68");
        htmlUtils.assertDivText("RCC: South West");
        htmlUtils.assertDivText("Manufacturer: SSL");
        htmlUtils.assertDivText("Maintenance region: South West");
        htmlUtils.assertDivText("Bandwidth limit: 256");
    }

    private void checkUpdatedDetails() {
        htmlUtils.assertDivText("Device list:");
        htmlUtils.assertSpanText("alm");
        htmlUtils.assertSpanText("ami");
        htmlUtils.assertSpanText("signal94xxstandard");
        htmlUtils.assertDivText("HE geographic address: M99-1234A");
        htmlUtils.assertDivText("Firmware version: 7.0");
        htmlUtils.assertDivText("Type number: 0");
        htmlUtils.assertDivText("Serial number: 207838");
        htmlUtils.assertDivText("Hardware version: A");
        htmlUtils.assertDivText("Hostname: M99-1234A.alm.ha.org");
        htmlUtils.assertDivText("Latitude: 51.392588");
        htmlUtils.assertDivText("Longitude: -2.825793");
        htmlUtils.assertDivText("Manufacturer data: cpu=Vortex6357DX,issue=Sxxxx");
        htmlUtils.assertDivText("Enrolment date: " + LocalDate.now());
    }
}
