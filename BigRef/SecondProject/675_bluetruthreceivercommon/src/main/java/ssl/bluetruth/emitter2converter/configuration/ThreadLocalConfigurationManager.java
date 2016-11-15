/**
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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created on 27th April 2015
 */

package ssl.bluetruth.emitter2converter.configuration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to avoid the problems which could appear from using statics.
 * ThreadLocal stores the ConfigurationManager that could be access at any moment in the thread.
 * At the beginning of each thread, a ConfigurationManager must be set (In the servlets, the schedules and the tests).
 * At the end of each thread, the ConfigurationManager must be removed (In the servlets, the schedules and the tests).
 * @author jtrujillo-brenes
 */
public class ThreadLocalConfigurationManager {
    
    private static final ThreadLocal<ConfigurationManager> threadLocalConfigurationManager = new ThreadLocal<>();
    
    public static void set(ConfigurationManager configurationManager) {
        threadLocalConfigurationManager.set(configurationManager);
    }
    
    public static void setFrom(HttpServletRequest request) {
        setFrom(request.getSession().getServletContext());
    }
    
    public static void setFrom(ServletConfig config) {
        setFrom(config.getServletContext());
    }
    
    private static void setFrom(ServletContext servletContext) {
        ConfigurationManager configurationManager = (ConfigurationManager) servletContext.getAttribute(ConfigurationManager.CONFIGURATION_MANAGER_IN_SERVLETCONTEXT);
        threadLocalConfigurationManager.set(configurationManager);
    }
    
    public static ConfigurationManager get() {
        if(threadLocalConfigurationManager.get() == null) {
            throw new NullPointerException("ConfigurationManager is null probably because ThreadLocalConfigurationManager hasn't been set in this thread");
        }
        return threadLocalConfigurationManager.get();
    }
    
    public static void remove() {
       threadLocalConfigurationManager.remove();
    }
    
}
