/*
 *
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

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.SeleniumWebDriverHelper.WebElementHelper;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HtmlSvgUtils {

    private final SeleniumWebDriverHelper webDriverHelper;

    @Autowired
    public HtmlSvgUtils(SeleniumWebDriverHelper webDriverHelper) {
        this.webDriverHelper = webDriverHelper;
    }

    public void checkTextIsPresentOnSchematic(String text) {
        WebElementHelper schematicSvgElement = webDriverHelper.findElement("//*[@id='schematic']//*[local-name()='svg']//*[contains(text(), '" + text + "')]");
        assertThat(schematicSvgElement.isDisplayed(), is(true));
    }

    public void checkTextIsNearOtherTextOnSchematic(String text1, String text2, double relativeProximity) {
        WebElementHelper schematicElement = webDriverHelper.findElement("//*[@id='schematic']//*[local-name()='svg']");

        WebElementHelper element1 = schematicElement.findElement(By.xpath("//*[contains(text(), '" + text1 + "')]"));
        WebElementHelper element2 = schematicElement.findElement(By.xpath("//*[contains(text(), '" + text2 + "')]"));

        Dimension schematicSize = schematicElement.getSize();

        Point element1Location = element1.getLocation();
        Point element2Location = element2.getLocation();

        double relativeOffsetX = 1.0 * (element1Location.x - element2Location.x) / schematicSize.width;
        double relativeOffsetY = 1.0 * (element1Location.y - element2Location.y) / schematicSize.height;

        double relativeOffsetHypoteneuse = Math.hypot(relativeOffsetX, relativeOffsetY);

        assertThat(relativeOffsetHypoteneuse, is(lessThanOrEqualTo(relativeProximity)));
    }

    public void assertValue(String container, String attribute, String value) {
        String id = (container+" "+attribute).replace(" ", "_");
        webDriverHelper.findElement("//*[@id='"+id+"' and contains(text(),'"+value+"')]");
    }

    public void assertNotValue(String container, String attribute, String value) {
        String id = (container+" "+attribute).replace(" ", "_");
        webDriverHelper.findElement("//*[@id='"+id+"' and not(contains(text(),'"+value+"'))]");
    }

}
