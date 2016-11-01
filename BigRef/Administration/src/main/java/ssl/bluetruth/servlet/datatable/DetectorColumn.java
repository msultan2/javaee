////////////////////////////////////////////////////////////////////////////////
//
//  THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
//  LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
//  EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
//  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
//  INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
//  OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
//  POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
//  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
//
//  Copyright 2016 (C) Costain Integrated Technology Solutions Limited.
//  All Rights Reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ssl.bluetruth.servlet.datatable;

import com.ssl.utils.datatable.ColumnType;
import com.ssl.utils.datatable.DataTableColumnDef;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mohamed
 */
public class DetectorColumn implements QueryColumns {

    @Override
    public List<DataTableColumnDef> get() {
        List<DataTableColumnDef> columns = new ArrayList<>();
        columns.add(new DataTableColumnDef("Detector", "detector.detector_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("ID", "detector.detector_id", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Location", "detector.location", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Longitude", "detector.latitude", ColumnType.DOUBLE));
        columns.add(new DataTableColumnDef("Latitude", "detector.longitude", ColumnType.DOUBLE));
        columns.add(new DataTableColumnDef("MODE", "CASE detector.mode "
                + "WHEN 0 THEN 'MODE 0 - Idle' "
                + "WHEN 1 THEN 'MODE 1 - Journey Time' "
                + "WHEN 2 THEN 'MODE 2 - Occupancy' "
                + "WHEN 3 THEN 'MODE 3 - Journey Time & Occupancy' "
                + "ELSE 'MODE 0 - Idle' END", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Carriageway", "detector.carriageway", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Logical Groups", "logical_groups", ColumnType.STRING));
        // SCJS 016 START
//        columns.add(new DataTableColumnDef("Status", "CASE "
//                + "WHEN GREATEST(timestamp,COALESCE(last_recorded_timestamp,'1970-01-01 00:00:00.0')) < now()-\"silentThresholdDelayInSeconds\" * interval '1 min' THEN 'Silent' "
//                + "WHEN last_detection_timestamp > NOW() - \"detectorReportingStatusInMinutes\" * interval '1 min' THEN 'Reporting' "
//                + "ELSE 'Degraded' END", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Status", "CASE "
                + "WHEN timestamp<now()-\"silentThresholdDelayInSeconds\" * interval '1 min' THEN 'Silent' "
                + "WHEN last_detection_timestamp > NOW() - \"detectorReportingStatusInMinutes\" * interval '1 min' THEN 'Reporting' "
                + "ELSE 'Degraded' END", ColumnType.STRING));
        return columns;
    }
}
