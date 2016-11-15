/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    07/02/2013  RG      001         V1.00 First Issue

 */

#ifndef _VIEW_H_
#define _VIEW_H_


#include "iobserver.h"

#include "controller.h"
#include "datacontainer.h"
#include "eventhandler.h"
#include "loggerdefinitions.h"
#include "mutex.h"
#include "types.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>


namespace View
{

class IErrorDialog;
class OutStationConfigurationParameters;
class OutStationMainFrame;
class OutStationSettingsDialog;
class OutStationAboutDialog;

struct LogRecord
{
    std::tstring time;
    LoggingLevel logLevel;
    std::tstring text;
};

class View: public ::IObserver
{
public:

     //! destructor
    virtual ~View();

    static void setup();

    virtual void notifyOfStateChange(::IObservable* observablePtr, const int index);

    static bool construct();
    static void destruct();

    static bool isValid();

    static void showSettingsDialog();
    static void showAboutDialog();

    static void processEvents();
    static void waitUntilAllEventsAreComplete();

    static void mainFrameHasBeenDestroyed();
    static OutStationMainFrame* getMainFrame();
    static OutStationSettingsDialog* getSettingsDialog();
    static IErrorDialog* getErrorDialog();

    static void log(boost::shared_ptr<LogRecord> plogRecord);

    static View* getInstancePtr();

private:

    //! default constructor
    View(OutStationConfigurationParameters& configurationParameters);

    //! default constructor. Not implemented
    View();
    //! copy constructor. Not implemented
    View(const View& rhs);
    //! assignment operator. Not implemented
    View& operator=(const View& rhs);

    void _setup();

    void _waitUntilAllEventsAreComplete();

    enum DialogEvent
    {
        SHOW_SETTINGS_DIALOG,
        SHOW_ABOUT_DIALOG,

        //These two events must be the last enumeration literals in this enumeration
        LAST_DIALOG_EVENT,
        NUMBER_OF_DIALOG_EVENTS
    };

    enum GUIEvent
    {
        EXIT,

        BLUETOOTH_LOCAL_DEVICE_HAS_BEEN_CHANGED,
        BLUETOOTH_REMOTE_DEVICE_COLLECTION_HAS_BEEN_CHANGED,
        BLUETOOTH_DISCOVERER_STATE_CHANGE,
        BLUETOOTH_DISCOVERER_INQUIRY_START,
        BLUETOOTH_DISCOVERER_INQUIRY_END,
        INSTATION_CLIENT_STATE_CHANGE,
        QUEUE_DETECTOR_STATE_CHANGE,

        NEW_LOGS_ADDED,
        //These two events must be the last enumeration literals in this enumeration
        LAST_GUI_EVENT,
        NUMBER_OF_GUI_EVENTS
    };

    enum DialogResult
    {
        NO_RESULT = 0,
        RESULT_OK,
        RESULT_CANCEL
    };

    static void addDialogEvent(const DialogEvent &ev);
    static void addGUIEvent(const GUIEvent &ev);

    void _showSettingsDialog();
    void _showAboutDialog();

    void _updateBlueToothDeviceClientStateGauge();
    void _restartBlueToothInquiryTimer();
    void _stopBlueToothInquiryTimer();
    void _updateLocalBlueToothDevice();
    void _updateBlueToothDeviceCollection();
    void _updateInstationClientStateGauge();
    void _updateQueueDetectionView();

    void _updateLogs();

    void _updateTimeOnStatusBar();

    void _processDialogEvents();
    void _processGUIEvents();

    //Private members:

    static View* m_pInstance;
    static bool m_valid;

    OutStationConfigurationParameters& m_configurationParameters;

    OutStationMainFrame* m_pFrame;

    IErrorDialog* m_pErrorDialog;
    OutStationSettingsDialog* m_pSettingsDialog;
    OutStationAboutDialog* m_pAboutDialog;

    enum EBlueToothDeviceState
    {
        eBLUETOOTH_DEVICE_STATE_RUNNING = 1,
        eBLUETOOTH_DEVICE_STATE_NOT_RUNNING
    };
    EBlueToothDeviceState m_blueToothDeviceMonitorState;
    Model::DataContainer::TRemoteDeviceRecordCollection m_remoteBlueToothDeviceCollection;
    Model::TLocalDeviceRecord m_localBlueToothDevice;
    ::Mutex m_localBlueToothDeviceMutex;
    unsigned int m_blueToothInquiryDurationInSeconds;

    enum EInstationClientState
    {
        eINSTATION_CLIENT_STATE_NOT_RUNNING = 1,
        eINSTATION_CLIENT_STATE_IDLE,
        eINSTATION_CLIENT_STATE_CONNECTING,
        eINSTATION_CLIENT_STATE_CONNECTED
    };
    EInstationClientState m_instationClientState;
    ::Mutex m_instationClientStateMutex;
    std::string m_instationFromAddress;
    std::string m_instationToAddress;
    uint16_t m_instationToPort;

    EInstationClientState m_retrieveConfigurationClientState;

    typedef std::list<boost::shared_ptr<LogRecord> > TLogRecordCollection;
    TLogRecordCollection m_logsCollection;
    mutable ::Mutex m_logsCollectionMutex;

    EventHandler m_dialogEventHandler;
    EventHandler m_guiEventHandler;

    bool m_allDialogEventsProcessed;
    bool m_allGUIEventsProcessed;
};

}

#endif //_VIEW_H_
