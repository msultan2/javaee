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
package com.ssl.rmas.test.rait.steps;


import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class DeviceConnectionSteps {

    public static final String DEVICE_IP = "10.163.49.68";
    private static final String PRIVATE_KEY_PATH = System.getProperty("basedir") + "/src/main/resources/privateKey";
    private static final String INVALID_PRIVATE_KEY_PATH = System.getProperty("basedir") + "/src/main/resources/privateKey.invalid";
    private static final String BANDWIDTH_LIMIT = "256";

    private HtmlUtils htmlUtils;
    private HtmlFormUtils htmlFormUtils;
    private HtmlButtonUtils htmlButtonUtils;

    @Autowired
    public void setHtmlUtils(HtmlUtils htmlUtils) {
        this.htmlUtils = htmlUtils;
    }

    @Autowired
    public void setHtmlFormUtils(HtmlFormUtils htmlFormUtils) {
        this.htmlFormUtils = htmlFormUtils;
    }

    @Autowired
    public void setHtmlButtonUtils(HtmlButtonUtils htmlButtonUtils) {
        this.htmlButtonUtils = htmlButtonUtils;
    }

    @Given("^I have started the RAIT application$")
    public void navigateToDeviceConnection() {
        htmlUtils.goToRaitHomePage();
    }

    @Given("^I have entered connections parameters$")
    public void connectedToDevice() throws TestTimeoutException {
        setConnectionParams(DEVICE_IP, PRIVATE_KEY_PATH, BANDWIDTH_LIMIT);
    }

    @When("^I select an invalid SSH private key file$")
    public void selectInvalidKey() throws TestTimeoutException {
        setConnectionParams(DEVICE_IP, INVALID_PRIVATE_KEY_PATH, BANDWIDTH_LIMIT);
    }

    private void setConnectionParams(String deviceIp, String privateKeyPath, String bandwidthLimit) throws TestTimeoutException {
        htmlFormUtils.set(HtmlFormUtils.DEVICE_IP, deviceIp);
        htmlFormUtils.setFile(HtmlFormUtils.PRIVATE_KEY, privateKeyPath);
        htmlFormUtils.set(HtmlFormUtils.BANDWIDTH_LIMIT, bandwidthLimit);
        htmlButtonUtils.waitAndClick(HtmlUtils.NEXT_BUTTON, 1);
    }
}
