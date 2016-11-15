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
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdateSshKeySteps {
    
    private static final String NEW_PUBLIC_KEY_PATH = System.getProperty("basedir") + "/src/main/resources/newPublicKey.pub";
    
    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;
       
    @When("^I upload a new valid SSH key$")
    public void initiateUpdateSshKey() {
        htmlUtils.clickOnButton(HtmlUtils.UPDATE_SSH_KEY_BUTTON);
        htmlFormUtils.setFile("New SSH public key file", NEW_PUBLIC_KEY_PATH);
        htmlModalUtils.clickFooterButton("Update");
    }
}