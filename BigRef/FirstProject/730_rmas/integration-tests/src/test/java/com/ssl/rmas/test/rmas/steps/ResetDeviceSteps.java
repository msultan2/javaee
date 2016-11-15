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
  * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
  *
 */
package com.ssl.rmas.test.rmas.steps;

import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.When;

public class ResetDeviceSteps {

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;

    @When("^I select the option to reset device$")
    public void initiateResetDevice() {
        htmlUtils.clickOnButton(HtmlUtils.RESET_DEVICE_BUTTON);
    }
    @When("^I initiate the reset command$")
    public void initiateResetDeviceWithinModalDialog() {
        htmlModalUtils.clickOnButton(HtmlUtils.RESET_DEVICE_BUTTON);
    }
}