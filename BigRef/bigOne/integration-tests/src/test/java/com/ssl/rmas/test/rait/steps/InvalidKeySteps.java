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

import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class InvalidKeySteps {

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;

    @When("^I initiate a command on the device$")
    public void initiateCommand() {
        htmlUtils.clickOnButton(HtmlUtils.DOWNLOAD_STATIC_DATA_BUTTON);
    }

    @Then("^I should be notified of failure$")
    public void checkFailureMessage() throws InterruptedException {
        htmlModalUtils.waitForStatus(HtmlModalUtils.STATUS_FAILURE);
        htmlModalUtils.assertTextIsDisplayed("Incorrect private key");
    }
}
