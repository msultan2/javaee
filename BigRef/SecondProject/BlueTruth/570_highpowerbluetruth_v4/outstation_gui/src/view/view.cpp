#include "stdafx.h"
#include "view.h"

#include "activeboostasiotcpclient.h"
#include "bluetooth/acquiredevicestask.h"
#include "bluetooth/devicediscoverer.h"
#include "datacontainer.h"
#include "genericerrordialog.h"
#include "lock.h"
#include "logger.h"
#include "model.h"
#include "os_utilities.h"
#include "outstationconfigurationparameters.h"
#include "outstationmainframe.h"
#include "outstationsettingsdialog.h"
#include "outstationaboutdialog.h"
#include "queuedetector.h"

#include <wx/app.h>

namespace
{
    const char MODULE_NAME[] = "View";
}

namespace View
{

View* View::m_pInstance = 0;
bool View::m_valid = true;


View* View::getInstancePtr()
{
    return m_pInstance;
}

View::View(OutStationConfigurationParameters& configurationParameters)
:
::IObserver(),
m_configurationParameters(configurationParameters),
m_pFrame(0),
m_pErrorDialog(0),
m_pSettingsDialog(0),
m_pAboutDialog(0),
m_blueToothDeviceMonitorState(eBLUETOOTH_DEVICE_STATE_NOT_RUNNING),
m_remoteBlueToothDeviceCollection(),
m_localBlueToothDevice(),
m_localBlueToothDeviceMutex(),
m_blueToothInquiryDurationInSeconds(0),
m_instationClientState(eINSTATION_CLIENT_STATE_NOT_RUNNING),
m_instationClientStateMutex(),
m_instationFromAddress(),
m_instationToAddress(),
m_instationToPort(0),
m_retrieveConfigurationClientState(eINSTATION_CLIENT_STATE_NOT_RUNNING),
m_logsCollection(),
m_logsCollectionMutex(),
m_dialogEventHandler("Dialog"),
m_guiEventHandler("GUI"),
m_allDialogEventsProcessed(false),
m_allGUIEventsProcessed(false)
{
    START_NEW_OPERATOR_TRY_CATCH_SECTION();
        m_pFrame = new OutStationMainFrame(reinterpret_cast<wxWindow*>(NULL), m_configurationParameters);
    END_NEW_OPERATOR_TRY_CATCH_SECTION(OutStationMainFrame, m_pFrame, m_valid);

    START_NEW_OPERATOR_TRY_CATCH_SECTION();
        m_pErrorDialog = new GenericErrorDialog(m_pFrame);
    END_NEW_OPERATOR_TRY_CATCH_SECTION(GenericErrorDialog, m_pErrorDialog, m_valid);

    START_NEW_OPERATOR_TRY_CATCH_SECTION();
        assert(Logger::getLogHandler() != 0);
        m_pSettingsDialog = new OutStationSettingsDialog(
            m_pFrame,
            m_configurationParameters,
            *Logger::getLogHandler());
        m_pSettingsDialog->addObserver(m_pFrame);
    END_NEW_OPERATOR_TRY_CATCH_SECTION(OutStationSettingsDialog, m_pSettingsDialog, m_valid);

    START_NEW_OPERATOR_TRY_CATCH_SECTION();
        m_pAboutDialog = new OutStationAboutDialog(m_pFrame);
    END_NEW_OPERATOR_TRY_CATCH_SECTION(OutStationAboutDialog, m_pAboutDialog, m_valid);

    Logger::setErrorDialog(m_pErrorDialog);

    _updateBlueToothDeviceClientStateGauge();
    _updateInstationClientStateGauge();

    m_pFrame->Show(true);

    if (m_configurationParameters.getReadApplicationScreenSizeOnStart())
    {
        m_pFrame->setSizeAndPosition();
    }
    else
    {
        m_pFrame->CenterOnScreen();
    }
}

View::~View()
{
    //do nothing
}

void View::setup()
{
    if (m_pInstance != 0)
    {
        m_pInstance->_setup();
    }
    //else do nothing
}

void View::_setup()
{
    if (m_pSettingsDialog != 0)
    {
        m_pSettingsDialog->setup(Model::Model::getDataContainer());
    }
    //else do nothing
}

void View::notifyOfStateChange(::IObservable* observablePtr, const int index)
{
    assert(observablePtr != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        Model::ActiveBoostAsioTCPClient* pTcpClient =
            dynamic_cast<Model::ActiveBoostAsioTCPClient* >(observablePtr);

        if (pTcpClient != 0)
        {
            if (pTcpClient->isOfIdentifier(CONGESTION_REPORTING_CLIENT_IDENTIFIER))
            {
                if (
                    !pTcpClient->isRunning() ||
                    (index == Model::Model::eINSTATION_CLIENT_IS_STOPPING)
                    )
                {
                    ::Lock lock(m_instationClientStateMutex);
                    m_instationClientState = eINSTATION_CLIENT_STATE_NOT_RUNNING;
                }
                else
                {
                    switch (pTcpClient->getStatus())
                    {
                        case Model::ActiveBoostAsioTCPClient::eSTATUS_STOPPED:
                        {
                            ::Lock lock(m_instationClientStateMutex);
                            m_instationClientState = eINSTATION_CLIENT_STATE_IDLE;
                            break;
                        }
                        case Model::ActiveBoostAsioTCPClient::eSTATUS_CONNECTING:
                        {
                            ::Lock lock(m_instationClientStateMutex);
                            m_instationClientState = eINSTATION_CLIENT_STATE_CONNECTING;
                            break;
                        }
                        case Model::ActiveBoostAsioTCPClient::eSTATUS_CONNECTED:
                        {
                            ::Lock lock(m_instationClientStateMutex);
                            m_instationClientState = eINSTATION_CLIENT_STATE_CONNECTED;
                            break;
                        }
                        default:
                        {
                            //do nothing
                            break;
                        }
                    }
                }

                m_instationFromAddress = pTcpClient->getLocalAddress();
                m_instationToAddress = pTcpClient->getRemoteAddress();
                m_instationToPort = pTcpClient->getRemotePortNumber();

                addGUIEvent(INSTATION_CLIENT_STATE_CHANGE);
            }
            //else do nothing

            return;
        }
        //else do nothing
    }

    {
        Model::DataContainer* pDataContainer =
            dynamic_cast<Model::DataContainer* >(observablePtr);

        if (pDataContainer != 0)
        {
            if (index == Model::DataContainer::eLOCAL_DEVICE_HAS_BEEN_CHANGED)
            {
                ::Lock lock(m_localBlueToothDeviceMutex);
                m_localBlueToothDevice = pDataContainer->getLocalDeviceRecord();
                if (m_localBlueToothDevice.hciRoute == -1) //i.e. is not initialised
                {
                    m_localBlueToothDevice.reset();
                }
                //else do nothing

                addGUIEvent(BLUETOOTH_LOCAL_DEVICE_HAS_BEEN_CHANGED);
            }
            else if (index == Model::DataContainer::eREMOTE_DEVICE_COLLECTION_HAS_BEEN_CHANGED)
            {
                Model::DataContainer::TRemoteDeviceRecordCollection& remoteDeviceCollection =
                    pDataContainer->getRemoteDeviceCollection();
                ::Mutex& remoteDeviceCollectionMutex =
                    pDataContainer->getRemoteDeviceCollectionMutex();
                ::Lock lock(remoteDeviceCollectionMutex);

                m_remoteBlueToothDeviceCollection.clear();
                for(Model::DataContainer::TRemoteDeviceRecordCollection::iterator
                        iter(remoteDeviceCollection.begin()),
                        iterEnd(remoteDeviceCollection.end());
                    iter != iterEnd;
                    ++iter)
                {
                    if (iter->second.presentInTheLastInquiry)
                    {
                        m_remoteBlueToothDeviceCollection[iter->first] = iter->second;
                    }
                    //else do nothing
                }

                addGUIEvent(BLUETOOTH_REMOTE_DEVICE_COLLECTION_HAS_BEEN_CHANGED);
            }
            else
            {
                //do nothing
            }


            return;
        }
        //else do nothing
    }

    {
        BlueTooth::AcquireDevicesTask* pTask =
            dynamic_cast<BlueTooth::AcquireDevicesTask* >(observablePtr);

        if (pTask != 0)
        {
            if (
                (index == BlueTooth::AcquireDevicesTask::eSTARTING) ||
                (index == BlueTooth::AcquireDevicesTask::eSTOPPING)
                )
            {
                m_blueToothDeviceMonitorState =
                    pTask->isRunning() ? eBLUETOOTH_DEVICE_STATE_RUNNING : eBLUETOOTH_DEVICE_STATE_NOT_RUNNING;
                m_blueToothInquiryDurationInSeconds = pTask->getInquiryDurationInSeconds();

                addGUIEvent(BLUETOOTH_DISCOVERER_STATE_CHANGE);
            }
            else
            {
                //do nothing
            }

            return;
        }
        //else do nothing
    }

    {
        BlueTooth::DeviceDiscoverer* pDeviceDiscoverer =
            dynamic_cast<BlueTooth::DeviceDiscoverer* >(observablePtr);

        if (pDeviceDiscoverer != 0)
        {
            if (index == BlueTooth::DeviceDiscoverer::eDEVICE_INQUIRY_START)
            {
                addGUIEvent(BLUETOOTH_DISCOVERER_INQUIRY_START);
            }
            else if (index == BlueTooth::DeviceDiscoverer::eDEVICE_INQUIRY_END)
            {
                addGUIEvent(BLUETOOTH_DISCOVERER_INQUIRY_END);
            }
            else
            {
                //do nothing
            }

            return;
        }
        //else do nothing
    }

    {
        QueueDetection::QueueDetector* pQueueDetector =
            dynamic_cast<QueueDetection::QueueDetector* >(observablePtr);

        if (pQueueDetector != 0)
        {
            addGUIEvent(QUEUE_DETECTOR_STATE_CHANGE);

            return;
        }
        //else do nothing
    }
}

bool View::construct()
{
    if (m_pInstance == 0)
    {
        m_pInstance = new View(*OutStationConfigurationParameters::getInstancePtr());
    }
    else
    {
        // already constructed, do nothing!
    }

    return m_valid;
}

void View::destruct()
{
    if (m_pInstance != 0)
    {
        delete m_pInstance;
        m_pInstance = 0;
    }
    else
    {
        // already destroyed, do nothing!
    }
}

bool View::isValid()
{
    return m_valid;
}

void View::showSettingsDialog()
{
    addDialogEvent(SHOW_SETTINGS_DIALOG);
}

void View::_showSettingsDialog()
{
    if (m_pSettingsDialog != 0)
    {
        m_pSettingsDialog->Centre();
        m_pSettingsDialog->ShowModal();
    }
    else
    {
        Logger::logSoftwareException(
            MODULE_NAME,
            "_showSettingsDialog",
            "function called but m_pSettingsDialog=0");
    }
}

void View::showAboutDialog()
{
    addDialogEvent(SHOW_ABOUT_DIALOG);
}

void View::_showAboutDialog()
{
    if (m_pAboutDialog != 0)
    {
        m_pAboutDialog->Centre();
        m_pAboutDialog->ShowModal();
    }
    else
    {
        Logger::logSoftwareException(
            MODULE_NAME,
            "_showAboutDialog",
            "function called but m_aboutDialog=0");
    }
}

void View::_updateBlueToothDeviceClientStateGauge()
{
    switch (static_cast<int>(m_blueToothDeviceMonitorState))
    {
        case eBLUETOOTH_DEVICE_STATE_NOT_RUNNING:
        {
            m_pFrame->setBlueToothDiscoveryNotRunning();

            break;
        }
        case eBLUETOOTH_DEVICE_STATE_RUNNING:
        {
            m_pFrame->setBlueToothDiscoveryRunning(m_blueToothInquiryDurationInSeconds);

            break;
        }
        default:
        {
            break;
        }
    }
}

void View::_restartBlueToothInquiryTimer()
{
    m_pFrame->restartBlueToothInquiryTimer(m_blueToothInquiryDurationInSeconds);
}

void View::_stopBlueToothInquiryTimer()
{
    m_pFrame->stopBlueToothInquiryTimer();
}

void View::_updateLocalBlueToothDevice()
{
    Model::TLocalDeviceRecord localBlueToothDevice;

    {
        ::Lock lock(m_localBlueToothDeviceMutex);
        localBlueToothDevice = m_localBlueToothDevice;
    }

    m_pFrame->updateLocalBlueToothDevice(localBlueToothDevice);
}

void View::_updateBlueToothDeviceCollection()
{
    m_pFrame->updateBlueToothDeviceCollection(m_remoteBlueToothDeviceCollection);
}

void View::_updateInstationClientStateGauge()
{
    EInstationClientState instationClientState;
    {
        ::Lock lock(m_instationClientStateMutex);
        instationClientState = m_instationClientState;
    }

    switch (static_cast<int>(instationClientState))
    {
        case eINSTATION_CLIENT_STATE_NOT_RUNNING:
        {
            m_pFrame->setInStationNotRunning();

            m_pFrame->setInStationFromAddress(_T(""));
            m_pFrame->setInStationToAddress(_T(""));
            m_pFrame->setInStationToPort(_T(""));

            break;
        }
        case eINSTATION_CLIENT_STATE_IDLE:
        {
            m_pFrame->setInStationDisconnected();

            m_pFrame->setInStationFromAddress(_T(""));
            m_pFrame->setInStationToAddress(_T(""));
            m_pFrame->setInStationToPort(_T(""));

            break;
        }
        case eINSTATION_CLIENT_STATE_CONNECTING:
        {
            m_pFrame->setInStationConnecting();

            m_pFrame->setInStationFromAddress(wxString::FromAscii(m_instationFromAddress.c_str()));
            m_pFrame->setInStationToAddress(_T(""));
            m_pFrame->setInStationToPort(_T(""));

            break;
        }
        case eINSTATION_CLIENT_STATE_CONNECTED:
        {
            m_pFrame->setInStationConnected();

            m_pFrame->setInStationFromAddress(wxString::FromAscii(m_instationFromAddress.c_str()));
            m_pFrame->setInStationToAddress(wxString::FromAscii(m_instationToAddress.c_str()));
            m_pFrame->setInStationToPort(wxString::Format(_T("%d"), m_instationToPort));

            break;
        }
        default:
        {
            break;
        }
    }
}

void View::_updateQueueDetectionView()
{
    m_pFrame->updateQueueDetectionView();
}

void View::_updateLogs()
{
    Lock lock(m_logsCollectionMutex);
    m_pFrame->addLogs(m_logsCollection);
    m_logsCollection.clear();
}

void View::_updateTimeOnStatusBar()
{
    static wxDateTime lastUpdateTime(wxDateTime::Now());
    const static int TIME_UPDATE_TIME_IN_MS = 100;

    const wxDateTime NOW(wxDateTime::Now());
    if (NOW.Subtract(lastUpdateTime).GetMilliseconds() > TIME_UPDATE_TIME_IN_MS)
    {
        m_pFrame->updateCurrentTimeOnStatusBar();
        lastUpdateTime = NOW;
    }
    //else do nothing


    //Another way to do the same thing:
    //static int lastUpdateTimeSeconds(wxDateTime::Now().GetSecond());

    //const wxDateTime NOW(wxDateTime::Now());
    //if (NOW.GetSecond() != lastUpdateTimeSeconds)
    //{
    //    m_pFrame->updateCurrentTimeOnStatusBar();
    //    lastUpdateTimeSeconds = NOW.GetSecond();
    //}
    ////else do nothing
}

void View::mainFrameHasBeenDestroyed()
{
    getInstancePtr()->m_pFrame = 0;
}

OutStationMainFrame* View::getMainFrame()
{
    return getInstancePtr()->m_pFrame;
}

OutStationSettingsDialog* View::getSettingsDialog()
{
    return getInstancePtr()->m_pSettingsDialog;
}

IErrorDialog* View::getErrorDialog()
{
    return getInstancePtr()->m_pErrorDialog;
}

void View::log(boost::shared_ptr<LogRecord> plogRecord)
{
    Lock lock(getInstancePtr()->m_logsCollectionMutex);
    getInstancePtr()->m_logsCollection.push_back(plogRecord);
    addGUIEvent(NEW_LOGS_ADDED);
}

void View::addDialogEvent(const DialogEvent &ev)
{
    const int EVENT_ID = static_cast<int>(ev);

    getInstancePtr()->m_dialogEventHandler.setEvent(EVENT_ID);
    wxWakeUpIdle();
}

void View::addGUIEvent(const GUIEvent &ev)
{
    const int EVENT_ID = static_cast<int>(ev);

    getInstancePtr()->m_guiEventHandler.setEvent(EVENT_ID);
    wxWakeUpIdle();
}

void View::processEvents()
{
    getInstancePtr()->_processDialogEvents();
    getInstancePtr()->_processGUIEvents();
}

void View::_processDialogEvents()
{
    size_t eventID = 0;
    while (m_dialogEventHandler.waitOnAnyEvent(0, eventID))
    {
        switch (eventID)
        {
            case SHOW_SETTINGS_DIALOG:
            {
                _showSettingsDialog();
                break;
            }
            case SHOW_ABOUT_DIALOG:
            {
                _showAboutDialog();
                break;
            }
            case LAST_DIALOG_EVENT:
            {
                m_allDialogEventsProcessed = true;
                break;
            }
            default:
            {
                Logger::logInvalidEnumerationLiteral(MODULE_NAME,
                    "_processDialogEvents",
                    "eventID",
                    static_cast<int>(eventID));
                break;
            }
        }
    }
}

void View::_processGUIEvents()
{
    size_t eventID = 0;
    while (m_guiEventHandler.waitOnAnyEvent(0, eventID))
    {
        assert(m_pFrame != 0);

        switch (eventID)
        {
            case EXIT:
            {
                if (!m_pFrame->Close(true))
                {
                    OS_Utilities::sleep(100);
                    addGUIEvent(EXIT);
                }
                else
                {
                    //do nothing
                }
                break;
            }

            case BLUETOOTH_DISCOVERER_STATE_CHANGE:
            {
                _updateBlueToothDeviceClientStateGauge();
                break;
            }

            case BLUETOOTH_DISCOVERER_INQUIRY_START:
            {
                _restartBlueToothInquiryTimer();
                break;
            }

            case BLUETOOTH_DISCOVERER_INQUIRY_END:
            {
                _stopBlueToothInquiryTimer();
                break;
            }

            case BLUETOOTH_LOCAL_DEVICE_HAS_BEEN_CHANGED:
            {
                _updateLocalBlueToothDevice();
                break;
            }

            case BLUETOOTH_REMOTE_DEVICE_COLLECTION_HAS_BEEN_CHANGED:
            {
                _updateBlueToothDeviceCollection();
                break;
            }

            case INSTATION_CLIENT_STATE_CHANGE:
            {
                _updateInstationClientStateGauge();
                break;
            }

            case QUEUE_DETECTOR_STATE_CHANGE:
            {
                _updateQueueDetectionView();
                break;
            }

            case NEW_LOGS_ADDED:
            {
                _updateLogs();
                break;
            }

            case LAST_GUI_EVENT:
            {
                m_allGUIEventsProcessed = true;
                break;
            }
            default:
            {
                Logger::logInvalidEnumerationLiteral(MODULE_NAME,
                    "_processGUIEvents",
                    "eventID",
                    static_cast<int>(eventID));
                break;
            }
        }
    }

    _updateTimeOnStatusBar();
}

void View::waitUntilAllEventsAreComplete()
{
    getInstancePtr()->_waitUntilAllEventsAreComplete();
}

void View::_waitUntilAllEventsAreComplete()
{
    m_allDialogEventsProcessed = false;
    m_allGUIEventsProcessed = false;

    addDialogEvent(LAST_DIALOG_EVENT);
    addGUIEvent(LAST_GUI_EVENT);

    while (!(m_allDialogEventsProcessed && m_allGUIEventsProcessed))
    {
        //Wait until both flags are true
        OS_Utilities::sleep(100);
    }
}

} //namespace
