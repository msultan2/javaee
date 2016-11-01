/*
 *
 * RunCukesIT.java
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
 *  Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 *
 */
package com.ssl.rmas.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/test-results/rait/cucumber-html-report", "junit:target/test-results/rait/cucumber-junit-report.xml", "json:target/test-results/rait/cucumber.json"},
        glue = {"classpath:com/ssl/rmas/test/shared", "classpath:com/ssl/cukes", "classpath:com/ssl/rmas/test/rait"},
        features = "classpath:features",
        strict = true,
        tags = {"~@wip", "@integration", "@RAIT"})
public class RunRAITCukesIT {

    @BeforeClass
    public static void loadProperties() throws IOException {

    }
}
