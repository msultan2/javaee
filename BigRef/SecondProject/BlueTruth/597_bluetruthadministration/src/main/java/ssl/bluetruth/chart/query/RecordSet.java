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
package ssl.bluetruth.chart.query;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RecordSet {

    private Map<String, Object> recordSetdata;
    private String xField;
    private final Set<String> yField = new LinkedHashSet<>();
    private String predicatedFieldName;
    private String additionalFieldName;

    public RecordSet() {
        this.recordSetdata = new HashMap<>();
    }

    public String getAdditionalFieldName() {
        return additionalFieldName;
    }

    public void setAdditionalFieldName(String additionalFieldName) {
        this.additionalFieldName = additionalFieldName;
    }

    public void clear() {
        this.predicatedFieldName = "";
        this.additionalFieldName = "";
        this.xField = "";
        if (!yField.isEmpty()) {
            this.yField.clear();
        }
        if (!recordSetdata.isEmpty()) {
            this.recordSetdata.clear();
        }
    }

    public Map<String, Object> getRecordSetdata() {
        return recordSetdata;
    }

    public void setRecordSetdata(Map<String, Object> recordSetdata) {
        this.recordSetdata = recordSetdata;
    }

    public String getxField() {
        return xField;
    }

    public void setxField(String xField) {
        this.xField = xField;
    }

    public Set<String> getyField() {
        return yField;
    }

    public void setyField(String yField) {
        this.yField.add(yField);
    }

    public String getPredicatedFieldName() {
        return predicatedFieldName;
    }

    public void setPredicatedFieldName(String predicatedFieldName) {
        this.predicatedFieldName = predicatedFieldName;
    }

}
