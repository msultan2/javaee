/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    17/06/2013  RG      001         V1.00 First Issue
*/

#ifndef QUEUE_DETECTOR_H_
#define QUEUE_DETECTOR_H_

#include "datacontainer.h"
#include "iobservable.h"
#include "iobserver.h"
#include "clock.h"
#include "mutex.h"
#include "remotedevicerecord.h"
#include "types.h"

#include <boost/shared_ptr.hpp>
#include <map>
#include <string>


namespace Model
{

const char* getBinTypeName(const EBinType type);

}

class Clock;


namespace QueueDetection
{


enum EQueuePresenceState
{
    eQUEUE_PRESENCE_STATE_NO_QUEUE = 0,
    eQUEUE_PRESENCE_STATE_QUEUE_PRESENT = 9,
    eQUEUE_PRESENCE_STATE_NOT_READY = 0xFE,
    eQUEUE_PRESENCE_STATE_FAULT = 0xFF
};

/**
 The structure used to pass data that is send to the InStation in
 congestion reports.
 */
struct CongestionReport
{
    unsigned int numberOfDevicesInFreeFlowBin;
    unsigned int numberOfDevicesInModerateFlowBin;
    unsigned int numberOfDevicesInSlowFlowBin;
    unsigned int numberOfDevicesInVerySlowFlowBin;
    unsigned int numberOfDevicesInStaticFlowBin;
    EQueuePresenceState queuePresenceState;
    ::TTime_t queueStartTime;
    ::TTime_t queueEndTime;

    CongestionReport()
    :
    numberOfDevicesInFreeFlowBin(0),
    numberOfDevicesInModerateFlowBin(0),
    numberOfDevicesInSlowFlowBin(0),
    numberOfDevicesInVerySlowFlowBin(0),
    numberOfDevicesInStaticFlowBin(0),
    queuePresenceState(eQUEUE_PRESENCE_STATE_NOT_READY),
    queueStartTime(pt::not_a_date_time),
    queueEndTime(pt::not_a_date_time)
    {}

    void reset()
    {
        //Set to values defined in the constructor
        *this = CongestionReport();
    }
};
typedef struct CongestionReport TCongestionReport;


/**
 The class used to process the scanned device vector and to detect if
 congestion is present or not.
 */
class QueueDetector :
    public ::IObservable,
    public ::IObserver
{
public:

    typedef Model::DataContainer::TRemoteDeviceRecordCollection TRemoteDeviceRecordCollection;

    QueueDetector(
        TRemoteDeviceRecordCollection& deviceCollection,
        ::Mutex& deviceCollectionMutex,
        ::Clock* pClock
    );

    virtual ~QueueDetector();


    void setup(
        const unsigned int inquiryScanDurationInSeconds,
        const unsigned int dropOutScanCycleThresholdInSeconds,
        const unsigned int freeFlowThresholdInSeconds,
        const unsigned int moderateFlowThresholdInSeconds,
        const unsigned int slowFlowThresholdInSeconds,
        const unsigned int verySlowFlowThresholdInSeconds,
        const Model::EBinType queueAlertThresholdBin,
        const unsigned int queueDetectionThresholdNumber,
        const unsigned int queueClearanceThresholdNumber,
        const unsigned int startupIntervalInSeconds);
    bool isConfigured() const { return m_isConfigured; }

    virtual void notifyOfStateChange(IObservable* pObservable, const int index);

    void updateDevices();

    void updateDevicesFromRawTime(const TSteadyTimePoint& currentTime);

    void reset();

    const TRemoteDeviceRecordCollection& getDeviceRecordCollection() const { return m_deviceCollection; }
    ::Mutex& getDeviceRecordCollectionMutex() const { return m_deviceCollectionMutex; }

    unsigned int getDeviceCount(const Model::EBinType binType) const;

    EQueuePresenceState getQueuePresenceState() { return m_queuePresenceState; }

    TCongestionReport getCongestionReport() const;

private:

    //! default constructor. Not implemented
    QueueDetector();
    //! copy constructor. Not implemented
    QueueDetector(const QueueDetector& );
    //! assignment operator. Not implemented
    QueueDetector& operator=(const QueueDetector& );


    //Private members:
    TRemoteDeviceRecordCollection& m_deviceCollection;
    ::Mutex& m_deviceCollectionMutex;
    ::Clock* m_pClock;

    bool m_isConfigured;

    //! Inquiry scan duration in seconds
    unsigned int m_inquiryScanDurationInSeconds;

    //! Duration in seconds of device absence (=device is not being observed
    //! over that long period) after which a device is removed from the statistics
    unsigned int m_dropOutScanCycleThresholdInSeconds;
    //! Duration in inquiry cycle units of device absence (=device is not being observed
    //! over that long period) after which a device is removed from the statistics
    unsigned int m_dropOutScanCycleThreshold;


    //All thresholds define upper value in seconds e.g.
    //the "Free Flow" bin will be in the range (0, m_freeFlowThreshold>,
    //the "Moderate Flow" (m_freeFlowThreshold, m_moderateFlowThreshold> etc.
    //For all drivers except Raw HCI a car will belong to the "Moderate Flow" bin
    // if seen at least m_freeFlowThreshold+1 times and at most m_moderateFlowThreshold times.

    ///! Upper "Free Flow" threshold in seconds
    unsigned int m_freeFlowThresholdInSeconds;
    ///! Upper "Free Flow" threshold in inquiry cycle units (=number of scans)
    unsigned int m_freeFlowThreshold;
    ///! Upper "Moderate Flow" threshold in seconds
    unsigned int m_moderateFlowThresholdInSeconds;
    ///! Upper "Moderate Flow" threshold in inquiry cycle units (=number of scans)
    unsigned int m_moderateFlowThreshold;
    ///! Upper "Slow Flow" threshold in seconds
    unsigned int m_slowFlowThresholdInSeconds;
    ///! Upper "Slow Flow" threshold in inquiry cycle units (=number of scans)
    unsigned int m_slowFlowThreshold;
    ///! Upper "Very Slow Flow" threshold in seconds
    unsigned int m_verySlowFlowThresholdInSeconds;
    ///! Upper "Very Slow Flow" threshold in inquiry cycle units (=number of scans)
    unsigned int m_verySlowFlowThreshold;

    EQueuePresenceState m_queuePresenceState;
    Model::EBinType m_queueAlertThresholdBin;
    unsigned int m_queueDetectionThresholdNumber;
    unsigned int m_queueClearanceThresholdNumber;

    mutable TCongestionReport m_congestionReport;
    mutable ::Mutex m_congestionReportMutex;

    TSteadyTimePoint m_startupTime;
    uint64_t m_startupTimeSinceZeroInSeconds;
    unsigned int m_startupIntervalInSeconds;
};

}

#endif //QUEUE_DETECTOR_H_
