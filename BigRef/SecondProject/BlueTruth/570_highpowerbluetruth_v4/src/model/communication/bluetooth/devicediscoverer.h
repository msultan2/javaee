/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef DEVICE_DISCOVERER_H_
#define DEVICE_DISCOVERER_H_

#include "iobservable.h"

#include "datacontainer.h"
#include "mutex.h"
#include "types.h"

#include <boost/shared_ptr.hpp>


namespace QueueDetection
{
    class QueueDetector;
}

namespace InStation
{
    class IInStationReporter;
}


namespace BlueTooth
{

/**
 * @brief This module is providing a local radio (local bluetooth device) to work with.
 *
 */
class DeviceDiscoverer : public ::IObservable
{
public:

    /** @brief default constructor
     *  @param pDataContainer DataContainer class object to be used for processing
     *  @param isInLegacyMode true if version 3.x, false if version 4.0+
    */
    DeviceDiscoverer(
        boost::shared_ptr<Model::DataContainer>& pDataContainer,
        const bool isInLegacyMode,
        const char* rawScanResultsFileName = 0);

    //! destructor
    virtual ~DeviceDiscoverer();

    void setup(
        boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector,
        boost::shared_ptr<InStation::IInStationReporter> pInStationReporter);

    /**
     * @brief Inquire devices in the neibourghood
     *
     * @return true - if local adapter is available and functional
     * */
    bool inquireDevices();

    /**
     * @brief Interrupt device inquiry
     *
     * This function interrupts a device inquiry run in another thread. The operating
     * system itself may be busy with the previous scan inquiry request and this
     * function should signal that we wan to interrupt it, e.g. to gracefully exit
     * the program.
     * */
    void interruptInquireDevices();

    /**
     * @brief Get a list of all local bluetooth devices including all available information about them
     *
     * @param pResult The list containing available local devices
     * */
	void getLocalDeviceCollection(
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::DataContainer::TLocalDeviceRecordCollection* pResult) const;

    /**
     *  @brief Update local radio information
     *
     * @return true - if local adapter is available and functional
     * @param address MAC address of the interface being looked for.
     *   If set to zero this function returns the first available local radio device.
     * @param pLocalDeviceRecord The record to be updated. To get info about a particular
     *   device set pLocalDeviceRecord->address to the relevant address.
     */
    bool getLocalRadioInfo(
        const uint64_t address,
        Model::TLocalDeviceRecord_shared_ptr& pLocalDeviceRecord) const;

    /**
     *  @brief Setup local radio device
     *
     * @return true - if local adapter is available and functional
     * @param localDeviceRecord The record to be updated.
     * */
    bool setupLocalRadio(
        Model::TLocalDeviceRecord_shared_ptr& pLocalDeviceRecord) const;


    /**
     * @brief Helper function. Verify if any local radio devices are present.
     *
     * If not report a fault if not reported yet, otherwise clear fault if not cleared yet
     * @return true if a radio found and is functional
     */
     bool checkForLocalRadio();

    /**
     * Provide mutex. This mutex can be used to get a list of local radios and to
     * avoid interrupting of the currently executing scan
     * */
    ::Mutex& getMutex();

    enum
    {
        eDEVICE_INQUIRY_START = 1,
        eDEVICE_INQUIRY_END,
    };

    /**
     * @brief Check if the record file is open.
     *
     * In production deployment this function should always return false because
     * the data should not log to this file (be aware that disk space may be fully
     * consumed by this file.
     */
    bool isRecordFileOpen() const;


private:

    //! default constructor. Not implemented
    DeviceDiscoverer();
    //! copy constructor. Not implemented
    DeviceDiscoverer(const DeviceDiscoverer& );
    //! assignment operator. Not implemented
    DeviceDiscoverer& operator=(const DeviceDiscoverer& );


    /**
     * @brief Open the file containing the results of scanning
     * The discovered devices will be written to /var/cache/bt/raw_scan_results.csv file.
     * the file format is:
     * -# All lines starting with # are comments. On start-up information about inquiry
     * duration and driver type should be written
     * -# A record of a valid device will be of format: firstObservationTime, lastObservationTime, MAC_address, CoD, name
     * -# An empty scan result (i.e. no devices were discovered) is of format: firstObservationTime, lastObservationTime, <empty>, <empty>, <empty>
     * -# A fault during scanning is of format: firstObservationTime, lastObservationTime, <empty>, 000000, "Error"
     */
    bool openRecordFile();

    /**
     * @brief Write the results of the last scan to the record file
     * @param lastScanWasOk a flag to indicate the result of the last scan. Used to report error in the file
     */
    void writeDiscoveredDevicesToRecordFile(const bool lastScanWasOk);

    ///! Close the file containing the results of SCANNING
    void closeRecordFile();


    void updateBluetoothDeviceFault(const bool faultPresent);

    ///! Scan for remote devices
    ///! @result true if scan request is ok, false otherwise
    bool scanForRemoteDevices();

    ///! Verify if inquiry duration changed and report it to the file if applicable
    void verifyIfInquiryDurationChanged();

    ///! Verify if device driver changed and report it to the file if applicable
    void verifyIfDriverChanged();


    //Private members:
    boost::shared_ptr<Model::DataContainer> m_pDataContainer;
    const bool M_IS_IN_LEGACY_MODE; //i.e. version 3.x
    boost::shared_ptr<QueueDetection::QueueDetector> m_pQueueDetector;
    boost::shared_ptr<InStation::IInStationReporter> m_pInStationReporter;

    ::Mutex m_mutex;

    std::string m_rawScanResultsFileName;
    std::ofstream m_rawScanResultsOutputFile;
    unsigned int m_lastInquiryDurationInSeconds;
    Model::EDeviceDriver m_lastDeviceDriver;
};

}

#endif //DEVICE_DISCOVERER_H_
