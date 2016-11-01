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

package com.ssl.rmas.test.rmas.utils;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.SeleniumWebDriverHelper.WebElementHelper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class HtmlDeviceFilterUtils {

    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;

    public void set(int row, String key, String value) {
        String xPath = getXPath()+getInputXPath(key);
        webDriverHelper.findElement(xPath);
        List<WebElementHelper> elements = webDriverHelper.findElements(xPath);
        elements.get(row).click().clear().sendKeys(value);
    }

    public String getXPath() {
        return "//form[contains(.,'Search')]";
    }

    private String getInputXPath(String key) {
        return "//label[text()='" + key + "']/..//input";
    }
}
