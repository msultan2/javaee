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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.bluetruth.receiver.v2.entities;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.GenericParsable;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Hex;

import fj.F;

import java.util.Date;

import com.ssl.bluetruth.receiver.v2.misc.annotations.Variable;

import java.util.ArrayList;

import static fj.data.Array.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;

/**
 *
 * @author Liban Abdulkadir
 */
@Component
@Scope("prototype")
public class StatisticsReport extends GenericParsable {

    private static final Logger logger = LogManager.getLogger(StatisticsReport.class.getName());
    public @Variable(index = 0)
    String id;
    public @Variable(index = 1)
    Date reportStart;
    public @Variable(index = 1)
    @Hex //report start time (UTC) in s in hex format
    int startTime;
    public @Variable(index = 2)
    @Hex //report time duration (UTC) in s in hex format
    int reportDuration;
    private List<BluetoothDevice> devices;

    private String report_table = "statistics_report";
    private String device_table = "statistics_device";
    @Autowired
    private ConfigurationManager configurationManager;

    public StatisticsReport() {
    }

    public StatisticsReport reportTable(String table) {
        this.report_table = table;
        return this;
    }

    public StatisticsReport deviceTable(String table) {
        this.device_table = table;
        return this;
    }

    private ArrayList<BluetoothDevice> parseDevices(String data) {
        String[] ss = data.split(",");
        Collection<BluetoothDevice> col = array(ss).toList().take(ss.length)
                .map(new F<String, BluetoothDevice>() {
                    @Override
                    public BluetoothDevice f(String a) {
                        return BluetoothDevice.deserialise(a);
                    }
                }).toCollection();
        return new ArrayList<>(col);
    }

    @Override
    public void postParse() {
        if (logger.isTraceEnabled()) {
            logger.trace("Data to validate = " + data);
        }

        String validLastSeenData = getValidLastSeenDevicesString(data);

        if (validLastSeenData != null && !validLastSeenData.isEmpty()) {
            String detectorId = getDetectorId(data);
            devices = getDevicesWithValidFirstSeen(parseDevices(validLastSeenData), detectorId);

        } else {
            devices = Collections.emptyList();
        }
    }

    private String getDetectorId(String data) {
        String[] ss = data.split(",");
        String detectorId = ss[0];

        if (logger.isTraceEnabled()) {
            logger.trace("Detector ID = " + ss[0]);
        }

        return detectorId;
    }

    /*
     Gets the timestampTolerance value from the local cache for a
     detector/outstation configuration.
     */
    public int getTimestampTolerance(final String detectorId) {

        int timestampTolerance = 0;

        try {
            timestampTolerance = configurationManager.getInt(detectorId,
                    ConfigurationManager.TIMESTAMP_TOLERANCE_MS);

        } catch (InvalidConfigurationException | RuntimeException mex) {
            logger.error("Unable to retrieve valid ConfigurationManager instance", mex);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("timestampTolerance = " + timestampTolerance
                    + " for detectorId: " + detectorId);
        }

        return timestampTolerance;
    }

    //Filters out all Bluetooth Devices that have a timestamp set in the future
    private List<BluetoothDevice> getDevicesWithValidFirstSeen(List<BluetoothDevice> bds, String detectorId) {
        if (logger.isTraceEnabled()) {
            logger.trace("Validating detections based on \"firstSeen\"");
        }

        List<BluetoothDevice> filteredBDs = new ArrayList<BluetoothDevice>();

        int timestampToleranceMs = getTimestampTolerance(detectorId);
        Instant timestampWithTolerance;

        for (BluetoothDevice bd : bds) {
            timestampWithTolerance = Instant.ofEpochMilli(Instant.now().toEpochMilli() + timestampToleranceMs);

            if (bd.getFirstSeen().compareTo(timestampWithTolerance) <= 0) {  // firstSeen is not in future
                filteredBDs.add(bd);

            } else {
                logger.warn("Bluetooth detection with id \"" + bd.getId() + "\" was rejected with first seen: " + bd.getFirstSeen().toString());
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Bluetooth devices " + filteredBDs + " are valid");
        }

        return filteredBDs;
    }

    public void insertDevices(final int report_id) {
        if (devices.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder("INSERT INTO %s(report_id,addr,cod,first_seen,"
                + "reference_point,last_seen) VALUES");
        for (int i = 0; i < devices.size(); i++) {
            sb.append("(?,?,?,?,?,?),");
        }
        String statement = String.format(sb.toString().substring(0, sb.length() - 1), device_table);
        new Db().sql(statement).set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                int i = 0;
                for (BluetoothDevice bd : devices) {
                    ps.setInt(++i, report_id);
                    ps.setString(++i, bd.getId());
                    ps.setInt(++i, bd.getCod());
                    ps.setTimestamp(++i, new Timestamp(bd.getFirstSeen().toEpochMilli()));
                    ps.setTimestamp(++i, new Timestamp(bd.getReferencePoint().toEpochMilli()));
                    ps.setTimestamp(++i, bd.getLastSeen() != null ? new Timestamp(bd.getLastSeen().toEpochMilli()) : null);
                }
            }
        }).go().close();
    }

    public int insertReport() {
        return (Integer) (new Db().sql(String.format("INSERT INTO %s (detector_id,report_start,report_end,of_id)"
                + "VALUES (?,?,?,?) RETURNING report_id", report_table))
                .set(new DBSetsParams() {
                    @Override
                    public void set(PreparedStatement ps) throws SQLException {
                        ps.setString(1, id);
                        ps.setTimestamp(2, new Timestamp(reportStart.getTime()));
                        ps.setTimestamp(3, new Timestamp(reportStart.getTime() + reportDuration * 1000L));
                        ps.setInt(4, new Detector().id(id).getObfuscatingFunction());
                    }
                })
                .go(new DBHasResult<Integer>() {
                    @Override
                    public Integer done(ResultSet rs) throws SQLException {
                        rs.next();
                        return rs.getInt("report_id");
                    }
                }).close().response());
    }

    public void insert() {
        if (!devices.isEmpty()
                && !(reportStart.compareTo(new Date()) > 0)) {
            int report_id = insertReport();
            insertDevices(report_id);
        }
    }

    @Override
    public void onValidated() {
        insert();
    }

    private String getValidLastSeenDevicesString(String data) {
        String[] ss = data.split(",");
        Collection<String> validDeviceList = array(ss).toList().drop(3).take(ss.length - 4).filter(BluetoothDevice::isLastSeenValid).toCollection();
        String retVal = String.join(",",validDeviceList);
        
        if (logger.isTraceEnabled()) {
            logger.trace("Validated detections based on \"lastSeen\" = " + retVal);
        }

        return retVal;
    }
    
    public List<BluetoothDevice> getDevices() {
		return devices;
	}
}