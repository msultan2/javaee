 --
 -- THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 -- ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 -- TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 -- PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 -- SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 -- SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 -- LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 -- USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 -- AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 -- LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 -- ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 -- POSSIBILITY OF SUCH DAMAGE.
 --
 -- Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 --
 -- Created on 4th August 2015
 --
 -- Author: josetrujillo-brenes


DO $$
    DECLARE  
        test varchar := 'ModeChange';
        detectorId varchar := 'DetectorTesting'||test;
    BEGIN
        RAISE INFO '%', test; 

        DELETE FROM occupancy WHERE detector_id = detectorId;
        DELETE FROM device_detection WHERE detector_id = detectorId;
        DELETE FROM detector_configuration WHERE detector_id = detectorId;
        DELETE FROM detector WHERE detector_id = detectorId;
        
        INSERT INTO detector VALUES (detectorId, detectorId, 3, 'North', 'default', 51.609488965698596, -2.6403236389160156, detectorId, 1, true);

        UPDATE detector_configuration
            SET "congestionReportPeriodInSeconds" = '10',
            "statisticsReportPeriodInSeconds" = '20',
            "absenceThresholdInSeconds" = '5',
            "queueDetectionStartupIntervalInSeconds" = '1',
            "settingsCollectionInterval1" = '1',
            "settingsCollectionInterval2" = '1',
            "signReports" = false
        WHERE detector_id = detectorId;

        INSERT INTO detector_logical_group VALUES (detectorId, (SELECT current_setting('testSettings.testGroup')));
    END
$$;