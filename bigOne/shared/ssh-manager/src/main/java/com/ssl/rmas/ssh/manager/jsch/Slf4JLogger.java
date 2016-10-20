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
package com.ssl.rmas.ssh.manager.jsch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Slf4JLogger implements com.jcraft.jsch.Logger {

    private static final Logger slf4jLogger = LoggerFactory.getLogger("com.jcraft.jsch");

    private static final int DEBUG_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.DEBUG;
    private static final int INFO_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.INFO;
    private static final int WARN_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.WARN;

    @Override
    public boolean isEnabled(int level) {
        if (level <= DEBUG_LEVEL_THRESHOLD) {
            return slf4jLogger.isDebugEnabled();
        }
        if (level <= INFO_LEVEL_THRESHOLD) {
            return slf4jLogger.isInfoEnabled();
        }
        if (level <= WARN_LEVEL_THRESHOLD) {
            return slf4jLogger.isWarnEnabled();
        }

        return slf4jLogger.isErrorEnabled();
    }

    @Override
    public void log(int level, String message) {
        if (level <= DEBUG_LEVEL_THRESHOLD) {
            slf4jLogger.debug(message);
        } else if (level <= INFO_LEVEL_THRESHOLD) {
            slf4jLogger.info(message);
        } else if (level <= WARN_LEVEL_THRESHOLD) {
            slf4jLogger.warn(message);
        } else {
            slf4jLogger.error(message);
        }
    }
}
