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

/**
 *
 * @author mohamed
 */
class DetectorQueryBody implements QueryBody {

    public String get() {
//        return "detector "
//                + "JOIN detector_statistic ON detector.detector_id = detector_statistic.detector_id "
//                + "JOIN detector_configuration ON detector.detector_id = detector_configuration.detector_id "
//                + "JOIN detector_status ON  detector.detector_id = detector_status.detector_id "
//                + "JOIN ( "
//                + "   SELECT detector_logical_group.detector_id, "
//                + "   ('['::text || array_to_string(array_agg(DISTINCT detector_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
//                + "   FROM detector_logical_group "
//                + "	  JOIN instation_user_logical_group ON "
//                + "	  detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
//                + "	  JOIN instation_user ON "
//                + "	  instation_user.username = instation_user_logical_group.username "
//                + "	  AND instation_user.username = '%s'"
//                + "	  GROUP BY detector_logical_group.detector_id"
//                + ") available ON available.detector_id = detector.detector_id"
//                + " AND detector.active = true";
//
        return "detector "
                + "JOIN detector_statistic ON detector.detector_id = detector_statistic.detector_id "
                + "JOIN detector_configuration ON detector.detector_id = detector_configuration.detector_id "
                + "JOIN detector_status ON  detector.detector_id = detector_status.detector_id "
                + "JOIN ( "
                + "   SELECT detector_logical_group.detector_id, "
                + "   ('['::text || array_to_string(array_agg(DISTINCT detector_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
                + "   FROM detector_logical_group "
                + "	  JOIN instation_user_logical_group ON "
                + "	  detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
                + "	  JOIN instation_user ON "
                + "	  instation_user.username = instation_user_logical_group.username "
                + "	  AND instation_user.username = '%s'"
                + "	  GROUP BY detector_logical_group.detector_id"
                + ") available ON available.detector_id = detector.detector_id";
    }

}
