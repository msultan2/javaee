/*
 * CreateChartAction.java
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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.7
 *
 * Created on 17-Jun-2015 12:15 PM
 * 
 */
package ssl.bluetruth.chart.actions;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.naming.NamingException;
import ssl.bluetruth.chart.Chart;
import ssl.bluetruth.common.BlueTruthException;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 * Chart Action class to generate chart 
 * @author svenkataramanappa
 */
public class CreateChartAction {

    Chart chart;
    private static final Logger LOGGER = LogManager.getLogger(CreateChartAction.class);

    public CreateChartAction(Chart chart) {
        this.chart = chart;
    }

    public void actionPerformed() {
        ScheduledExecutorService pool = ActionsThreadPool.getInstance().getPool();
        final Future future = pool.submit(new CreateChart(chart));
        pool.schedule(new Runnable() {
            @Override
            public void run() {
                if (future.isCancelled()) {
                    LOGGER.error("Thread assigned to created chart was cancelled");
                    delete();
                } else if (!future.isDone()) {
                    LOGGER.debug("Cancelling thread that was assigned to create chart, "
                            + "Timeout (>3 minutes to process task)");
                    future.cancel(true);
                    delete();
                }
            }
        }, 3, TimeUnit.MINUTES);
    }

    class CreateChart implements Runnable {

        Chart chart;
        
        public CreateChart(Chart chart) {
            this.chart = chart;
        }

        @Override
        public void run() {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                chart.write(os);
                save(os.toByteArray());
            } catch (BlueTruthException ex) {
                LOGGER.error("Bluetruth Exception occured during chart creation" + ex);
                delete();
            }
        }

        private void save(byte[] b) {

            Connection conn = null;
            PreparedStatement ps = null;

            try {
                DatabaseManager dm = DatabaseManager.getInstance();
                conn = dm.getDatasource().getConnection();
                ps = conn.prepareStatement("UPDATE chart SET chart_image_data = ?, complete = TRUE WHERE id=?;");
                ps.setBytes(1, b);
                ps.setInt(2, chart.getId());
                ps.executeUpdate();
            } catch (SQLException | DatabaseManagerException | NamingException ex) {
                LOGGER.error("Exception occured while saving the chart in the database" + ex);
                delete();
            } finally {
                try {
                    if (ps != null && !ps.isClosed()) {
                        ps.close();
                    }
                } catch (SQLException ex) {
                    LOGGER.error("SQL Exception occured while closing prepared statement");
                }

                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    LOGGER.error("SQL Exception, failed to close database connection");
                }
            }
        }
    }

    private void delete() {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            conn = dm.getDatasource().getConnection();
            ps = conn.prepareStatement("DELETE FROM chart WHERE id=?;");
            ps.setInt(1, chart.getId());
            ps.executeUpdate();
        } catch (SQLException | DatabaseManagerException | NamingException ex) {
            LOGGER.error("Exception occured while deleting chart from the database" + ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception occured while closing prepared statement");
            }
            
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close database connection");
            }
        }
    }
}
