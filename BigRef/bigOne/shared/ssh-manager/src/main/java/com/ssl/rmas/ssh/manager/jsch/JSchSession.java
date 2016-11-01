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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config/application.properties")
public class JSchSession {

    private final JSch jsch;

    @Autowired
    private Slf4JLogger jschLogger;

    @Value("${sshUser:rmas_user}")
    private String user;

    @Value("${sshPort:22}")
    private int port;

    @Value("${sshSessionTimeout:5000}")
    private int timeout;

    public JSchSession() {
        JSch.setLogger(jschLogger);
        jsch = new JSch();
    }

    Session getSession(ConnectionParams connParams) throws JSchException {
        jsch.removeAllIdentity();
        jsch.addIdentity(user, connParams.getPrivateKey().getBytes(StandardCharsets.UTF_8), null, null);
        Session session = jsch.getSession(user, connParams.getIpAddress(), port);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(timeout);
        return session;
    }

    void disconnectSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
