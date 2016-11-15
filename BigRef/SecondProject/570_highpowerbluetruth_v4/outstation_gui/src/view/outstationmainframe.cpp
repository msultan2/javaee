#include "stdafx.h"
#include "outstationmainframe.h"

#include "applicationconfiguration.h"
#include "bluetooth/bluetooth_utils.h"
#include "controller.h"
#include "datacontainer.h"
#include "instation/instationhttpclient.h"
#include "outstationconfigurationparameters.h"
#include "outstationsettingsdialog.h"
#include "ierrordialog.h"
#include "lock.h"
#include "logger.h"
#include "model.h"
#include "os_utilities.h"
#include "queuedetector.h"
#include "types.h"
#include "ssh/reversesshconnector.h"
#include "utils.h"
#include "version.h"
#include "view.h"
#include "xpm/outstation_gui.xpm"

#include <cassert>
#include <wx/clipbrd.h>
#include <wx/dataobj.h>
#include <wx/msgdlg.h>


namespace
{
    const int ERROR_THRESHOLD_VALUE_OF_SHELL_EXECUTE = 32;

    const int DEFAULT_MAXIMUM_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID = 1000;


    const char MODULE_NAME[] = "OutStationMainFrame";

enum
{
	ID_START = wxID_HIGHEST,
	ID_TIMER,
	ID_DEVICE_INQUIRY_TIMER,
};

}


namespace View
{

BEGIN_EVENT_TABLE(OutStationMainFrame, MainFrame)
    EVT_TIMER(ID_TIMER, OutStationMainFrame::OnProgressTimer)
END_EVENT_TABLE()


OutStationMainFrame::OutStationMainFrame(wxWindow* parent, OutStationConfigurationParameters& configurationParameters)
:
MainFrame(parent),
KeyPressHandler(),
::IObserver(),
m_configurationParameters(configurationParameters),
m_currentLogNumber(1),
m_maximumNumberOfRowsInEventLogGrid(DEFAULT_MAXIMUM_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID),
m_selectedRowOnEventLogGrid(-1),
m_pEventLogPopupOnGrid(0),
m_pRetrieveConfigurationClient(),
m_pInStationClient(),
m_pInStationReverseSSHConnector(),
m_pInStationDataContainer(),
m_pQueueDetector(),
m_dateTimeOnStatusbar(),
m_timer(this, ID_TIMER),
m_lastDeviceInquiryEndTime(),
m_deviceInquiryTimeOnStatusbar(),
m_deviceInquiryTimer(this, ID_DEVICE_INQUIRY_TIMER)
{
    //Clear local bluetooth device settings
    const Model::TLocalDeviceRecord localDeviceRecord;
    updateLocalBlueToothDevice(localDeviceRecord);

    new KeyPressEventHandler(this, this);

    //Activate the first tab in the notebook
    m_notebookPtr->ChangeSelection(0);

    //Set statusbar widths
    m_statusBarPtr->SetFieldsCount(eSTATUS_BAR_SIZE);
#ifdef _WIN32
    const int WIDTHS[eSTATUS_BAR_SIZE] = { -1, 200, 230, 200, 80 };
#else //XFCE4 uses bigger font so some columns have been made wider
    const int WIDTHS[eSTATUS_BAR_SIZE] = { -1, 280, 280, 200, 80 };
#endif
    m_statusBarPtr->SetStatusWidths(eSTATUS_BAR_SIZE, &WIDTHS[0]);

    //Prepare bluetooth devices grid
    m_gridRemoteBlueToothDevicesPtr->DeleteRows(0, m_gridRemoteBlueToothDevicesPtr->GetRows());
#ifdef _WIN32
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(0, 120);
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(1, 110);
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(2, 70);
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(3, 400);
#else //XFCE4 uses bigger font so some columns have been made wider
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(0, 130);
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(1, 60);
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(2, 80);
    m_gridRemoteBlueToothDevicesPtr->SetColumnWidth(3, 480);
#endif

    //Prepare queue detection grid
    m_gridQueueDetectionPtr->DeleteRows(0, m_gridQueueDetectionPtr->GetRows());
#ifdef _WIN32
    m_gridQueueDetectionPtr->SetColumnWidth(0, 120);
    m_gridQueueDetectionPtr->SetColumnWidth(1, 110);
    m_gridQueueDetectionPtr->SetColumnWidth(2, 120);
    m_gridQueueDetectionPtr->SetColumnWidth(3, 120);
    m_gridQueueDetectionPtr->SetColumnWidth(4, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(5, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(6, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(7, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(8, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(9, 400);
#else //XFCE4 uses bigger font so some columns have been made wider
    m_gridQueueDetectionPtr->SetColumnWidth(0, 130);
    m_gridQueueDetectionPtr->SetColumnWidth(1, 60); //mostly not used on linux (RawHCI)
    m_gridQueueDetectionPtr->SetColumnWidth(2, 150);
    m_gridQueueDetectionPtr->SetColumnWidth(3, 150);
    m_gridQueueDetectionPtr->SetColumnWidth(4, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(5, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(6, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(7, 70);
    m_gridQueueDetectionPtr->SetColumnWidth(8, 80);
    m_gridQueueDetectionPtr->SetColumnWidth(9, 480);
#endif

    //Prepare log event grid
#ifdef _WIN32
    m_gridEventLogPtr->DeleteRows(0, m_gridEventLogPtr->GetRows());
    m_gridEventLogPtr->SetColumnWidth(0, 130);
    m_gridEventLogPtr->SetColumnWidth(1, 50);
    m_gridEventLogPtr->SetColumnWidth(2, 600);
#else //XFCE4 uses bigger font so some columns have been made wider
    m_gridEventLogPtr->DeleteRows(0, m_gridEventLogPtr->GetRows());
    m_gridEventLogPtr->SetColumnWidth(0, 190);
    m_gridEventLogPtr->SetColumnWidth(1, 50);
    m_gridEventLogPtr->SetColumnWidth(2, 600);
#endif

    //Prepare log event grid
    m_maximumNumberOfRowsInEventLogGrid = m_configurationParameters.getMaxNumberOfRowsInEventLogGrid();
    if (m_gridEventLogPtr->GetRows() > 0)
    {
        m_gridEventLogPtr->DeleteRows(0, m_gridEventLogPtr->GetRows());
    }
    //else do nothing

    //Set jpeg and other image handlers. Without this action jpegs cannot be handled
    wxInitAllImageHandlers();

    //Set program logo. Because different size may be required (depending on window manager)
    //a set of resized icons (iconBundle) will be created
    //Load program logo
    wxImage programLogoImage(outstation_gui_xpm);
    wxIconBundle iconBundle;
    //Scale and create bitmap which can be converted to icon after

    std::vector<int> iconResolutions;
    iconResolutions.push_back(16);
    iconResolutions.push_back(24);
    iconResolutions.push_back(32);
    iconResolutions.push_back(64);
    iconResolutions.push_back(128);
    iconResolutions.push_back(256);

    for (size_t i=0; i<iconResolutions.size(); ++i)
    {
        wxImage programLogoImageRescaled(programLogoImage);
        const int RESOLUTION = iconResolutions[i];
        programLogoImageRescaled.Rescale(RESOLUTION, RESOLUTION);

        wxBitmap programLogoBitmap(programLogoImageRescaled);

        wxIcon programLogoIcon;
        programLogoIcon.CopyFromBitmap(programLogoBitmap);

        iconBundle.AddIcon(programLogoIcon);
    }

    //Finally set icon bundle
    SetIcons(iconBundle);
    updateApplicationTitle();

    m_timer.Start(1000);
}

OutStationMainFrame::~OutStationMainFrame()
{
    if (m_pEventLogPopupOnGrid != 0)
    {
        m_pEventLogPopupOnGrid->Disconnect(wxEVT_COMMAND_MENU_SELECTED,
            wxCommandEventHandler(OutStationMainFrame::onEventLogPopupGridClick),
            NULL,
            this);

        delete m_pEventLogPopupOnGrid;
    }
    //else do nothing

    View::mainFrameHasBeenDestroyed();
}


void OutStationMainFrame::setup(
    boost::shared_ptr<InStation::InStationHTTPClient> pRetrieveConfigurationClient)
{
    m_pRetrieveConfigurationClient = pRetrieveConfigurationClient;
}

void OutStationMainFrame::setup(
    boost::shared_ptr<InStation::InStationHTTPClient> pInStationClient,
    boost::shared_ptr<Model::DataContainer> pInStationDataContainer)
{
    m_pInStationClient = pInStationClient;
    m_pInStationDataContainer = pInStationDataContainer;
}

void OutStationMainFrame::setup(
    boost::shared_ptr<InStation::ReverseSSHConnector> pInStationReverseSSHConnector)
{
    m_pInStationReverseSSHConnector = pInStationReverseSSHConnector;
}

void OutStationMainFrame::setup(boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector)
{
    m_pQueueDetector = pQueueDetector;
}

void OutStationMainFrame::notifyOfStateChange(IObservable* observablePtr, const int )
{
    assert(observablePtr != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste
}

void OutStationMainFrame::updateBlueToothDeviceCollection(
    const Model::DataContainer::TRemoteDeviceRecordCollection& remoteDeviceRecordCollection)
{
    m_gridRemoteBlueToothDevicesPtr->BeginBatch();

    if (m_gridRemoteBlueToothDevicesPtr->GetRows() > 0)
    {
        m_gridRemoteBlueToothDevicesPtr->DeleteRows(0, m_gridRemoteBlueToothDevicesPtr->GetRows());
    }
    //else do nothing

    m_gridRemoteBlueToothDevicesPtr->AppendRows(remoteDeviceRecordCollection.size());
    int rowIndex = 0;

    for (
        Model::DataContainer::TRemoteDeviceRecordCollection::const_iterator
            iter(remoteDeviceRecordCollection.begin()),
            iterEnd(remoteDeviceRecordCollection.end());
        iter != iterEnd;
        ++iter)
    {
        m_gridRemoteBlueToothDevicesPtr->SetCellValue(
            rowIndex, 0, wxString::FromAscii(Utils::convertMACAddressToString(iter->second.address).c_str()));
        m_gridRemoteBlueToothDevicesPtr->SetCellValue(
            rowIndex, 1, wxString::FromAscii(iter->second.name.c_str()));
        m_gridRemoteBlueToothDevicesPtr->SetCellValue(
            rowIndex, 2, wxString::Format(wxT("%08X"), iter->second.deviceClass));
        m_gridRemoteBlueToothDevicesPtr->SetCellValue(
            rowIndex, 3, wxString::FromAscii(
            BlueTooth::decodeDeviceClass(iter->second.deviceClass).c_str()));

       ++rowIndex;
    }

    m_gridRemoteBlueToothDevicesPtr->EndBatch();
}

void OutStationMainFrame::updateLocalBlueToothDevice(const Model::TLocalDeviceRecord& localDeviceRecord)
{
    if (localDeviceRecord.manufacturerID != 0)
    {
        m_staticTextDeviceVendorPtr->SetLabel(
            wxString::Format(wxT("0x%x"), localDeviceRecord.manufacturerID));
    }
    else
    {
        m_staticTextDeviceVendorPtr->SetLabel(wxT("-"));
    }

    if (localDeviceRecord.impSubversion != 0)
    {
        m_staticTextProductIDPtr->SetLabel(
            wxString::Format(wxT("0x%x"), localDeviceRecord.impSubversion));
    }
    else
    {
        m_staticTextProductIDPtr->SetLabel(wxT("-"));
    }

    if (localDeviceRecord.address != 0)
    {
        m_staticTextLocalBlueToothAdapterIDPtr->SetLabel(
            wxString::FromAscii(Utils::convertMACAddressToString(localDeviceRecord.address).c_str()));
    }
    else
    {
        m_staticTextLocalBlueToothAdapterIDPtr->SetLabel(wxT("XX:XX:XX:XX:XX:XX"));
    }

    m_staticTextLocalBlueToothAdapterNamePtr->SetLabel(
        wxString::FromAscii(localDeviceRecord.name.c_str()));

    if (localDeviceRecord.deviceClass != 0)
    {
        m_staticTextLocalBlueToothAdapterCoDPtr->SetLabel(
            wxString::Format(wxT("%08x"), localDeviceRecord.deviceClass));
    }
    else
    {
        m_staticTextLocalBlueToothAdapterCoDPtr->SetLabel(wxT("-"));
    }

}

void OutStationMainFrame::updateQueueDetectionView()
{
    if (m_pQueueDetector == 0)
    {
        return;
    }
    //else do nothing

    //First rewrite the map in order of lastObserved
    std::multimap<uint64_t, Model::TRemoteDeviceRecord> deviceCollectionOrderdByLastObserved;

    {
        ::Lock lock(m_pQueueDetector->getDeviceRecordCollectionMutex());
        const QueueDetection::QueueDetector::TRemoteDeviceRecordCollection& deviceRecordCollection =
            m_pQueueDetector->getDeviceRecordCollection();

        for (QueueDetection::QueueDetector::TRemoteDeviceRecordCollection::const_reverse_iterator
                iter(deviceRecordCollection.end()),
                iterBegin(deviceRecordCollection.begin());
            iter != iterBegin;
            ++iter)
        {
            deviceCollectionOrderdByLastObserved.insert(
                std::pair<uint64_t, Model::TRemoteDeviceRecord>(
                    iter->second.lastObservationTime, iter->second) );
        }
    }

    static const wxColourDatabase COLOUR_DB;
    static const wxColour UNDEFINED_COLOUR(COLOUR_DB.Find(wxT("WHITE")));
    static const wxColour FREE_FLOW_COLOUR(COLOUR_DB.Find(wxT("GREEN")));
    static const wxColour MODERATE_FLOW_COLOUR(COLOUR_DB.Find(wxT("GREEN YELLOW")));
    static const wxColour SLOW_FLOW_COLOUR(COLOUR_DB.Find(wxT("YELLOW")));
    static const wxColour VERY_SLOW_FLOW_COLOUR(COLOUR_DB.Find(wxT("ORANGE RED")));
    static const wxColour STATIC_FLOW_COLOUR(COLOUR_DB.Find(wxT("SKY BLUE")));
    static const wxColour BACKGROUND_FLOW_COLOUR(COLOUR_DB.Find(wxT("GREY")));


    m_gridQueueDetectionPtr->BeginBatch();

    if (m_gridQueueDetectionPtr->GetRows() > 0)
    {
        m_gridQueueDetectionPtr->DeleteRows(0, m_gridQueueDetectionPtr->GetRows());
    }
    //else do nothing

    m_gridQueueDetectionPtr->AppendRows(deviceCollectionOrderdByLastObserved.size());
    int rowIndex = 0;

    static const TTime_t ZERO_TIME(pt::time_from_string("1970-01-01 00:00:00.000"));

    for (
        std::multimap<uint64_t, Model::TRemoteDeviceRecord>::const_reverse_iterator
            iter(deviceCollectionOrderdByLastObserved.end()),
            iterBegin(deviceCollectionOrderdByLastObserved.begin());
        iter != iterBegin;
        ++iter)
    {
        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 0, wxString::FromAscii(Utils::convertMACAddressToString(iter->second.address).c_str()));
        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 1, wxString::FromAscii(iter->second.name.c_str()));

        const TTimeDiff_t FIRST_OBSERVED_TIME_SINCE_ZERO(
            0, 0, iter->second.firstObservationTime, 0);
        const std::string FIRST_OBSERVED_TIME_SINCE_ZERO_STR(
            pt::to_simple_string(ZERO_TIME + FIRST_OBSERVED_TIME_SINCE_ZERO));
        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 2, wxString::FromAscii(FIRST_OBSERVED_TIME_SINCE_ZERO_STR.c_str()));

        const TTimeDiff_t LAST_OBSERVED_TIME_SINCE_ZERO(0, 0, iter->second.lastObservationTime, 0);
        const std::string LAST_OBSERVED_TIME_SINCE_ZERO_STR(pt::to_simple_string(ZERO_TIME + LAST_OBSERVED_TIME_SINCE_ZERO));
        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 3, wxString::FromAscii(LAST_OBSERVED_TIME_SINCE_ZERO_STR.c_str()));

        if (iter->second.numberOfScansPresent > 0)
        {
            m_gridQueueDetectionPtr->SetCellValue(
                rowIndex, 4, wxString::Format(wxT("%d"), iter->second.numberOfScansPresent));
            m_gridQueueDetectionPtr->SetCellValue(
                rowIndex, 5, wxString::Format(wxT("%d"), iter->second.numberOfScansAbsent));
            m_gridQueueDetectionPtr->SetCellValue(
                rowIndex, 6, wxString::Format(wxT("%d"), iter->second.numberOfScans));
        }
        //else do not print. Version 4+

        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 7, wxString::FromAscii(Model::getBinTypeName(iter->second.binType)));

        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 8, wxString::Format(wxT("%08X"), iter->second.deviceClass));
        m_gridQueueDetectionPtr->SetCellValue(
            rowIndex, 9, wxString::FromAscii(
            BlueTooth::decodeDeviceClass(iter->second.deviceClass).c_str()));

        //Colour entries depending on bin type
        Model::EBinType binType = iter->second.binType;

        const wxColour* pColour = &UNDEFINED_COLOUR;
        switch (binType)
        {
            case Model::eBIN_TYPE_FREE_FLOW:
            {
                pColour = &FREE_FLOW_COLOUR;
                break;
            }
            case Model::eBIN_TYPE_MODERATE_FLOW:
            {
                pColour = &MODERATE_FLOW_COLOUR;
                break;
            }
            case Model::eBIN_TYPE_SLOW_FLOW:
            {
                pColour = &SLOW_FLOW_COLOUR;
                break;
            }
            case Model::eBIN_TYPE_VERY_SLOW_FLOW:
            {
                pColour = &VERY_SLOW_FLOW_COLOUR;
                break;
            }
            case Model::eBIN_TYPE_STATIC_FLOW:
            {
                pColour = &STATIC_FLOW_COLOUR;
                break;
            }
            case Model::eBIN_TYPE_BACKGROUND:
            {
                pColour = &BACKGROUND_FLOW_COLOUR;
                break;
            }
            default:
            {
                //do nothing
                break;
            }
        }

        const int numberOfColumns = m_gridQueueDetectionPtr->GetCols();
        for (int i=0; i<numberOfColumns; ++i)
        {
            m_gridQueueDetectionPtr->SetCellBackgroundColour(rowIndex, i, *pColour);
        }

        ++rowIndex;
    }

    m_gridQueueDetectionPtr->EndBatch();

    m_staticTextFreeFlowCountPtr->SetLabel(wxString::Format(wxT("%d"),
        m_pQueueDetector->getDeviceCount(Model::eBIN_TYPE_FREE_FLOW)));
    m_staticTextModerateFlowCountPtr->SetLabel(wxString::Format(wxT("%d"),
        m_pQueueDetector->getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW)));
    m_staticTextSlowFlowCountPtr->SetLabel(wxString::Format(wxT("%d"),
        m_pQueueDetector->getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW)));
    m_staticTextVerySlowFlowCountPtr->SetLabel(wxString::Format(wxT("%d"),
        m_pQueueDetector->getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW)));
    m_staticTextStaticFlowCountPtr->SetLabel(wxString::Format(wxT("%d"),
        m_pQueueDetector->getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW)));

    static const wxColour QUEUE_PRESENT_COLOUR(COLOUR_DB.Find(wxT("RED")));
    static const wxColour QUEUE_NOT_PRESENT_COLOUR(COLOUR_DB.Find(wxT("GREEN")));
    static const wxColour QUEUE_NOT_READY_COLOUR(COLOUR_DB.Find(wxT("BLUE")));
    static const wxColour QUEUE_FAULT_COLOUR(COLOUR_DB.Find(wxT("BLACK")));

    switch (m_pQueueDetector->getQueuePresenceState())
    {
        case QueueDetection::eQUEUE_PRESENCE_STATE_QUEUE_PRESENT:
        {
            m_staticTextQueueHasBeenDetectedPtr->SetLabel(wxT("QUEUE!!!"));
            m_staticTextQueueHasBeenDetectedPtr->SetForegroundColour(QUEUE_PRESENT_COLOUR);
            break;
        }

        case QueueDetection::eQUEUE_PRESENCE_STATE_NO_QUEUE:
        {
            m_staticTextQueueHasBeenDetectedPtr->SetLabel(wxT("NO QUEUE"));
            m_staticTextQueueHasBeenDetectedPtr->SetForegroundColour(QUEUE_NOT_PRESENT_COLOUR);
            break;
        }

        case QueueDetection::eQUEUE_PRESENCE_STATE_NOT_READY:
        {
            m_staticTextQueueHasBeenDetectedPtr->SetLabel(wxT("NOT READY!!!"));
            m_staticTextQueueHasBeenDetectedPtr->SetForegroundColour(QUEUE_NOT_READY_COLOUR);
            break;
        }

        case QueueDetection::eQUEUE_PRESENCE_STATE_FAULT:
        {
            m_staticTextQueueHasBeenDetectedPtr->SetLabel(wxT("FAULT!!!"));
            m_staticTextQueueHasBeenDetectedPtr->SetForegroundColour(COLOUR_DB.Find(wxT("BLACK")));
            break;
        }

        default:
        {

        }
    }
}

void OutStationMainFrame::setBlueToothDiscoveryNotRunning()
{
    m_staticTextBlueToothDiscoveryGaugePtr->SetForegroundColour(wxColour(wxT("Red")));
    m_staticTextBlueToothDiscoveryGaugePtr->SetLabel(wxT("Not Running"));
    m_buttonStartStopBlueToothDeviceDiscoveryPtr->SetLabel(wxT("Start"));

    m_staticTextInquiryDurationInSecondsPtr->SetLabel(wxT("-"));

    m_lastDeviceInquiryEndTime.SetToCurrent();

    setStatusBarConnectionLabel(wxT("BlueTooth Device Discovery: Not Running"), OutStationMainFrame::eSTATUS_BAR_BLUETOOTH_DEVICE_INDEX);
}

void OutStationMainFrame::setBlueToothDiscoveryRunning(const unsigned int blueToothInquiryDurationInSeconds)
{
    m_staticTextBlueToothDiscoveryGaugePtr->SetForegroundColour(wxColour(wxT("Green")));
    m_staticTextBlueToothDiscoveryGaugePtr->SetLabel(wxT("Running"));
    m_buttonStartStopBlueToothDeviceDiscoveryPtr->SetLabel(wxT("Stop"));

    m_staticTextInquiryDurationInSecondsPtr->SetLabel(wxString::Format(wxT("%d s"), blueToothInquiryDurationInSeconds));

    setStatusBarConnectionLabel(wxT("BlueTooth Device Discovery: Running"), OutStationMainFrame::eSTATUS_BAR_BLUETOOTH_DEVICE_INDEX);
}

void OutStationMainFrame::restartBlueToothInquiryTimer(const unsigned int blueToothInquiryDurationInSeconds)
{
    m_lastDeviceInquiryEndTime = wxDateTime::Now() + wxTimeSpan(0, 0, blueToothInquiryDurationInSeconds);
}

void OutStationMainFrame::stopBlueToothInquiryTimer()
{
    m_lastDeviceInquiryEndTime.SetToCurrent();
}

void OutStationMainFrame::setInStationConnectionGaugeColour(const wxColour& colour)
{
    m_staticTextInStationClientGaugePtr->SetForegroundColour(colour);
}

void OutStationMainFrame::setInStationConnectionGaugeLabel(const wxString& label)
{
    m_staticTextInStationClientGaugePtr->SetLabel(label);
}

void OutStationMainFrame::setInStationNotRunning()
{
    m_staticTextInStationClientGaugePtr->SetForegroundColour(wxColour(wxT("Red")));
    m_staticTextInStationClientGaugePtr->SetLabel(wxT("Not Running"));
    m_buttonStartStopInstationClientPtr->SetLabel(wxT("Start"));
    setStatusBarConnectionLabel(wxT("InStation: Not Running"), OutStationMainFrame::eSTATUS_BAR_INSTATION_INDEX);
}

void OutStationMainFrame::setInStationDisconnected()
{
    m_staticTextInStationClientGaugePtr->SetForegroundColour(wxColour(wxT("Red")));
    m_staticTextInStationClientGaugePtr->SetLabel(wxT("Disconnected"));
    m_buttonStartStopInstationClientPtr->SetLabel(wxT("Stop"));
    setStatusBarConnectionLabel(wxT("InStation: Disconnected"), OutStationMainFrame::eSTATUS_BAR_INSTATION_INDEX);
}

void OutStationMainFrame::setInStationConnecting()
{
    m_staticTextInStationClientGaugePtr->SetForegroundColour(wxColour(wxT("Grey")));
    m_staticTextInStationClientGaugePtr->SetLabel(wxT("Connecting..."));
    m_buttonStartStopInstationClientPtr->SetLabel(wxT("Stop"));
    setStatusBarConnectionLabel(wxT("InStation: Connecting..."), OutStationMainFrame::eSTATUS_BAR_INSTATION_INDEX);
}

void OutStationMainFrame::setInStationConnected()
{
    m_staticTextInStationClientGaugePtr->SetForegroundColour(wxColour(wxT("Green")));
    m_staticTextInStationClientGaugePtr->SetLabel(wxT("Connected"));
    m_buttonStartStopInstationClientPtr->SetLabel(wxT("Stop"));
    setStatusBarConnectionLabel(wxT("InStation: Connected"), OutStationMainFrame::eSTATUS_BAR_INSTATION_INDEX);
}

void OutStationMainFrame::setInStationFromAddress(const wxString& text)
{
    m_staticTextInStationFromAddressPtr->SetLabel(text);
}

void OutStationMainFrame::setInStationToAddress(const wxString& text)
{
    m_staticTextInStationToAddressPtr->SetLabel(text);
}

void OutStationMainFrame::setInStationToPort(const wxString& text)
{
    m_staticTextInStationToPortPtr->SetLabel(text);
}


//void OutStationMainFrame::setSbcSshConnectionGaugeColour(const wxColour& colour)
//{
//    m_staticTextSbcSshConnectionStatusPtr->SetForegroundColour(colour);
//}
//
//void OutStationMainFrame::setSbcSshConnectionGaugeLabel(const wxString& label)
//{
//    m_staticTextSbcSshConnectionStatusPtr->SetLabel(label);
//}
//
//void OutStationMainFrame::setSbcEavConnectionGaugeColour(const wxColour& colour)
//{
//    m_staticTextSbcEavStatusPtr->SetForegroundColour(colour);
//}
//
//void OutStationMainFrame::setSbcEavConnectionGaugeLabel(const wxString& label)
//{
//    m_staticTextSbcEavStatusPtr->SetLabel(label);
//}

void OutStationMainFrame::setStatusBarConnectionLabel(const wxString& label, const size_t index)
{
    SetStatusText(label, index);
}


void OutStationMainFrame::addLogs(TLogRecordCollection& logRecordCollection)
{
    m_gridEventLogPtr->BeginBatch();

    //Remove a few rows so that the size after the following append operation does not exceed the maximum
    assert(logRecordCollection.size() < INT_MAX); //protect against overflow during subsequent cast
    int numberOfRowsToDelete = m_gridEventLogPtr->GetRows() + static_cast<int>(logRecordCollection.size()) - m_maximumNumberOfRowsInEventLogGrid;

    //Check if something is selected and delete the selected and preceding records only when after deletion precisely max -1 records will remain
    if (numberOfRowsToDelete <= m_selectedRowOnEventLogGrid)
    {
        numberOfRowsToDelete = 0;
    }
    //else do nothing

    if (numberOfRowsToDelete > m_gridEventLogPtr->GetRows())
    {
        numberOfRowsToDelete = m_gridEventLogPtr->GetRows();
    }
    //else do nothing

    if (numberOfRowsToDelete > 0)
    {
        m_gridEventLogPtr->DeleteRows(0, numberOfRowsToDelete);

        //Adjust current selection index but do not update selection yet
        m_selectedRowOnEventLogGrid -= numberOfRowsToDelete;
        if (m_selectedRowOnEventLogGrid < 0)
        {
            m_selectedRowOnEventLogGrid = -1;
        }
        //else do nothing

    }
    //else do nothing

    //Relabel rows
    int tmpLogNumber = m_currentLogNumber + logRecordCollection.size() - m_gridEventLogPtr->GetRows();
    if (numberOfRowsToDelete > 0)
    {
        const int NUMBER_OF_ROWS_BEFORE_APPENDING = m_gridEventLogPtr->GetRows();
        for(int i=0; i<NUMBER_OF_ROWS_BEFORE_APPENDING; ++i)
        {
            m_gridEventLogPtr->SetRowLabelValue(i, wxString::Format(wxT("%d"), tmpLogNumber++));
        }
    }
    //else do nothing

    int rowIndex = m_gridEventLogPtr->GetRows();
    m_gridEventLogPtr->AppendRows(logRecordCollection.size());

    for (
        TLogRecordCollection::iterator iter(logRecordCollection.begin());
        iter != logRecordCollection.end();
        ++iter)
    {
        m_gridEventLogPtr->SetCellValue(rowIndex, 0, wxString((*iter)->time));

        wxString logLevelText;

        switch (static_cast<int>((*iter)->logLevel))
        {
            case LOG_LEVEL_DEBUG3:
            {
                logLevelText = _T("DEBUG3");
                break;
            }
            case LOG_LEVEL_DEBUG2:
            {
                logLevelText = _T("DEBUG2");
                break;
            }
            case LOG_LEVEL_DEBUG1:
            {
                logLevelText = _T("DEBUG1");
                break;
            }
            case LOG_LEVEL_INFO:
            {
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 0, *wxGREEN);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 1, *wxGREEN);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 2, *wxGREEN);
                logLevelText = _T("INFO");
                break;
            }
            case LOG_LEVEL_NOTICE:
            {
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 0, *wxGREEN);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 1, *wxGREEN);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 2, *wxGREEN);
                logLevelText = _T("NOTICE");
                break;
            }
            case LOG_LEVEL_WARNING:
            {
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 0, *wxLIGHT_GREY);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 1, *wxLIGHT_GREY);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 2, *wxLIGHT_GREY);
                logLevelText = _T("WARNING");
                break;
            }
            case LOG_LEVEL_ERROR:
            {
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 0, *wxRED);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 1, *wxRED);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 2, *wxRED);
                logLevelText = _T("ERROR");
                break;
            }
            case LOG_LEVEL_EXCEPTION:
            {
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 0, *wxRED);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 1, *wxRED);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 2, *wxRED);
                logLevelText = _T("EXCEPTION");
                break;
            }
            case LOG_LEVEL_FATAL:
            default:
            {
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 0, *wxRED);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 1, *wxRED);
                m_gridEventLogPtr->SetCellBackgroundColour(rowIndex, 2, *wxRED);
                logLevelText = _T("FATAL");
                break;
            }
        }
        m_gridEventLogPtr->SetCellValue(rowIndex, 1, logLevelText);

        m_gridEventLogPtr->SetCellValue(rowIndex, 2, wxString((*iter)->text));

        m_gridEventLogPtr->SetRowLabelValue(rowIndex, wxString::Format(wxT("%d"), m_currentLogNumber));

        ++m_currentLogNumber;
        ++rowIndex;
    }

    //Select the just-appended row
    if (m_gridEventLogPtr->GetSelectedRows().IsEmpty())
    {
        m_gridEventLogPtr->MakeCellVisible(m_gridEventLogPtr->GetRows() - 1, 0);
    }
    //else do nothing

    m_gridEventLogPtr->EndBatch();
}

void OutStationMainFrame::updateCurrentTimeOnStatusBar()
{
    wxDateTime now(wxDateTime::Now());
    if (!m_dateTimeOnStatusbar.IsValid() || (now.GetSecond() != m_dateTimeOnStatusbar.GetSecond()))
    {
        SetStatusText(wxString::Format(wxT("%02u:%02u:%02u"),
            now.GetHour(), now.GetMinute(), now.GetSecond()), eSTATUS_BAR_TIME_INDEX);
        m_dateTimeOnStatusbar = now;
    }
    //else do nothing
}

void OutStationMainFrame::updateDeviceInquiryTimeOnStatusBar()
{
    if (m_lastDeviceInquiryEndTime.IsValid())
    {
        wxDateTime now(wxDateTime::Now());
        if (m_lastDeviceInquiryEndTime >= now)
        {
            wxTimeSpan timeLeft(m_lastDeviceInquiryEndTime - now);
            if (timeLeft.GetSeconds() != m_deviceInquiryTimeOnStatusbar.GetSeconds())
            {
                SetStatusText(
					wxString::Format(wxT("Inquiring... Seconds left: %lli"), timeLeft.GetSeconds()), 
					eSTATUS_BAR_BLUETOOTH_INQUIRY_TIME);
                m_deviceInquiryTimeOnStatusbar = timeLeft;
            }
            //else do nothing
        }
        else
        {
            SetStatusText(wxT("Inquiry finished"), eSTATUS_BAR_BLUETOOTH_INQUIRY_TIME);
        }
    }
    else
    {
        SetStatusText(wxT(""), eSTATUS_BAR_BLUETOOTH_INQUIRY_TIME);
    }
}

void OutStationMainFrame::setSizeAndPosition()
{
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;
    m_configurationParameters.getApplicationScreenSize(x, y, width, height);
    SetSize(x, y, width, height);

    int width_0 = 0;
    int width_1 = 0;
    int width_2 = 0;
    m_configurationParameters.getEventLogGridColumnSize(width_0, width_1, width_2);
    m_gridEventLogPtr->SetColumnWidth(0, width_0);
    m_gridEventLogPtr->SetColumnWidth(1, width_1);
    m_gridEventLogPtr->SetColumnWidth(2, width_2);
}

bool OutStationMainFrame::onKeyPressed(const wxKeyEvent& ev)
{
    bool retVal = false;

    const int KEY_CODE = ev.GetKeyCode();

    switch (KEY_CODE)
    {
        case WXK_TAB:
        case WXK_NUMPAD_TAB:
        case WXK_RETURN:
        case WXK_NUMPAD_ENTER:
        {
            break;
        }

        case WXK_ESCAPE:
        {
            wxWindow* windowHavingFocusPtr = FindFocus();

            if (windowHavingFocusPtr->GetParent() == m_gridEventLogPtr)
            {
                m_gridEventLogPtr->ClearSelection();
                m_selectedRowOnEventLogGrid = -1;
            }
            else
            {
                //do nothing
            }

            break;
        }

        case WXK_UP:
        case WXK_NUMPAD_UP:
        {
            wxWindow* windowHavingFocusPtr = FindFocus();

            if (windowHavingFocusPtr->GetParent() == m_gridEventLogPtr)
            {
                if (m_gridEventLogPtr->GetRows() > 0) //grid is not empty
                {
                    if (m_selectedRowOnEventLogGrid > 0) //something is selected
                    {
                        m_selectedRowOnEventLogGrid -= 1;
                    }
                    else if (m_selectedRowOnEventLogGrid == -1) //nothing is selected
                    {
                        m_selectedRowOnEventLogGrid = m_gridEventLogPtr->GetRows();
                    }
                    //else do nothing

                    m_gridEventLogPtr->SelectRow(m_selectedRowOnEventLogGrid);
                    m_gridEventLogPtr->MakeCellVisible(m_selectedRowOnEventLogGrid, 0);
                    retVal = true;
                }
                //else do nothing
            }
            else
            {
                //do nothing
            }

            break;
        }

        case WXK_DOWN:
        case WXK_NUMPAD_DOWN:
        {
            wxWindow* windowHavingFocusPtr = FindFocus();

            if (windowHavingFocusPtr->GetParent() == m_gridEventLogPtr)
            {
                if (m_gridEventLogPtr->GetRows() > 0) //grid is not empty
                {
                    if (m_selectedRowOnEventLogGrid >= 0)//something is selected
                    {
                        m_selectedRowOnEventLogGrid += 1;
                        if (m_selectedRowOnEventLogGrid >= m_gridEventLogPtr->GetRows())
                        {
                            m_selectedRowOnEventLogGrid = m_gridEventLogPtr->GetRows();
                        }
                        //else do nothing
                    }
                    else if (m_selectedRowOnEventLogGrid == -1)
                    {
                        m_selectedRowOnEventLogGrid = m_gridEventLogPtr->GetRows() - 1;
                    }
                    //else do nothing

                    m_gridEventLogPtr->SelectRow(m_selectedRowOnEventLogGrid);
                    m_gridEventLogPtr->MakeCellVisible(m_selectedRowOnEventLogGrid, 0);
                    retVal = true;
                }
                //else do nothing
            }
            else
            {
                //do nothing
            }

            break;
        }

        case 3: //Ctrl+C
        {
            wxWindow* windowHavingFocusPtr = FindFocus();

            if (windowHavingFocusPtr->GetParent() == m_gridEventLogPtr)
            {
                //If something is selected copy the contents of log to clipboard
                if ((m_selectedRowOnEventLogGrid != -1))
                {
                    if (wxTheClipboard->Open())
                    {
                        // This data objects are held by the clipboard,
                        // so do not delete them in the app.
                        wxTheClipboard->SetData(
                            new wxTextDataObject(
                                m_gridEventLogPtr->GetCellValue(m_selectedRowOnEventLogGrid, 2))
                            );
                        wxTheClipboard->Close();
                        retVal = true;
                    }
                    //else do nothing
                }
                //else do nothing
            }
            else
            {
                //do nothing
            }

            break;
        }

        case 'g':
        case 'G':
        {
            m_notebookPtr->ChangeSelection(0);
            retVal = true;
            break;
        }

        case 's':
        case 'S':
        {
            m_notebookPtr->ChangeSelection(1);
            retVal = true;
            break;
        }

        case 'q':
        case 'Q':
        {
            m_notebookPtr->ChangeSelection(2);
            retVal = true;
            break;
        }

        case 'e':
        case 'E':
        case 'l':
        case 'L':
        {
            m_notebookPtr->ChangeSelection(3);
            retVal = true;
            break;
        }

        default:
        {
            // do nothing
            break;
        }
    }

    return retVal;
}

void OutStationMainFrame::onClose( wxCloseEvent& ev )
{
    if (ev.CanVeto())
    {
        int result = wxMessageBox(
            _T("Are you sure you wish to exit?\n")
            _T("The current operation will be halted."),
            _T("Exit Application"),
            wxCENTRE | wxYES_NO | wxNO_DEFAULT | wxICON_QUESTION,
            this);

        if (result == wxYES)
        {
            if (m_configurationParameters.getStoreApplicationScreenSizeOnExit())
            {
                int x = 0;
                int y = 0;
                int width = 0;
                int height = 0;
                GetScreenPosition(&x, &y);

                GetSize(&width, &height);
                m_configurationParameters.setApplicationScreenSize(x, y, width, height);

                //Save width of columns of event logger
                const int width_0 = m_gridEventLogPtr->GetColumnWidth(0);
                const int width_1 = m_gridEventLogPtr->GetColumnWidth(1);
                const int width_2 = m_gridEventLogPtr->GetColumnWidth(2);
                m_configurationParameters.setEventLogGridColumnSize(width_0, width_1, width_2);
            }
            //else do nothing

            ev.Skip();
        }
        else
        {
            // Stop disallowed by user - do not shut down
            ev.Veto();
        }
    }
    else
    {
        // can't veto this request (for whatever reason)
        // Process the request but block until it's safe to allow windows to exit

        ev.Skip();
    }
}

void OutStationMainFrame::onIdle( wxIdleEvent& WXUNUSED(ev) )
{
    View::processEvents();
}

void OutStationMainFrame::onBrowse( wxCommandEvent& WXUNUSED(event) )
{
#ifdef _WIN32
    HINSTANCE result = ::ShellExecute(
        NULL,
        _T("explore"),
        BlueTruth::ApplicationConfiguration::getUserDataDirectory().c_str(),
        NULL,
        NULL,
        SW_RESTORE);

    if (reinterpret_cast<int>(result) > ERROR_THRESHOLD_VALUE_OF_SHELL_EXECUTE)
    {
        // command succeeded
    }
    else
    {
        std::tostringstream ss;
        ss << _T("Unable to browse to \"") << BlueTruth::ApplicationConfiguration::getUserDataDirectory().c_str() << _T("\" folder.");
        Logger::logError(ss.str(), true);
    }
#else
    const wxString USER_DATA_DIRECTORY(BlueTruth::ApplicationConfiguration::getUserDataDirectory().c_str());

    std::ostringstream command;
    command << "xdg-open \"" << USER_DATA_DIRECTORY.ToAscii() << "\"";
    system(command.str().c_str());
#endif
}

void OutStationMainFrame::onFileExit( wxCommandEvent& WXUNUSED(ev) )
{
    Close();
}

void OutStationMainFrame::onToolsSettings( wxCommandEvent& WXUNUSED(ev) )
{
    View::showSettingsDialog();
}

void OutStationMainFrame::onHelpAbout(wxCommandEvent& WXUNUSED(ev))
{
    View::showAboutDialog();
}

void OutStationMainFrame::onEventLogLeftClick( wxGridEvent& ev )
{
    m_selectedRowOnEventLogGrid = ev.GetRow();
    m_gridEventLogPtr->SelectRow(m_selectedRowOnEventLogGrid);
}

void OutStationMainFrame::onEventLogRightClick( wxGridEvent& WXUNUSED(ev) )
{
    if (m_pEventLogPopupOnGrid == 0)
    {
        m_pEventLogPopupOnGrid = new wxMenu();

        m_pEventLogPopupOnGrid->Append(ID_Clear, _T("&Clear"));
        m_pEventLogPopupOnGrid->Append(ID_ChangeLogLevel, _T("Change &log level"));
        m_pEventLogPopupOnGrid->AppendSeparator();
        m_pEventLogPopupOnGrid->Append(wxID_COPY, wxT("&Copy\tCtrl+C"));

        m_pEventLogPopupOnGrid->Connect(wxEVT_COMMAND_MENU_SELECTED,
            wxCommandEventHandler(OutStationMainFrame::onEventLogPopupGridClick),
            0,
            this);
    }
    //else //already created - do nothing.

    PopupMenu(m_pEventLogPopupOnGrid);
}

void OutStationMainFrame::onEventLogPopupGridClick(wxCommandEvent& ev)
{
    switch (ev.GetId())
    {
        case ID_Clear:
        {
            //Clear all log entries
            m_gridEventLogPtr->DeleteRows(0, m_gridEventLogPtr->GetRows());
            m_selectedRowOnEventLogGrid = -1;

            break;
        }

        case ID_ChangeLogLevel:
        {
            View::showSettingsDialog();

            break;
        }

        case wxID_COPY:
        {
            //If something is selected copy the contents of log to clipboard
            if (m_selectedRowOnEventLogGrid != -1)
            {
                if (wxTheClipboard->Open())
                {
                    // This data objects are held by the clipboard,
                    // so do not delete them in the app.
                    wxTheClipboard->SetData(
                        new wxTextDataObject(
                            m_gridEventLogPtr->GetCellValue(m_selectedRowOnEventLogGrid, 2))
                        );
                    wxTheClipboard->Close();
                }
                //else do nothing
            }
            //else do nothing

            break;
        }

        default:
        {
            //do nothing
            break;
        }
    }
}

void OutStationMainFrame::onButtonStartStopBlueToothDeviceDiscoveryClick( wxCommandEvent& WXUNUSED(ev))
{
    Controller::Controller::startStopBlueToothDeviceDiscovery();
    switch (Controller::Controller::getBlueToothDeviceDiscoveryStatus())
    {
        case Controller::Controller::eINTERFACE_STOPPED:
        {
            m_buttonStartStopBlueToothDeviceDiscoveryPtr->SetLabel(wxT("Start"));
            break;
        }
        case Controller::Controller::eINTERFACE_STARTED:
        default:
        {
            m_buttonStartStopBlueToothDeviceDiscoveryPtr->SetLabel(wxT("Stop"));
            break;
        }
    }
}

void OutStationMainFrame::onButtonStartStopInstationClientClick(wxCommandEvent& WXUNUSED(ev))
{
    Controller::Controller::startStopInstationClient();
}

#define TESTING
void OutStationMainFrame::onSendRawJourneyTimeToInStationClick(wxCommandEvent& WXUNUSED(ev))
{
    if ((m_pInStationClient != 0) && (m_pInStationDataContainer != 0))
    {
#ifdef TESTING
        m_pInStationDataContainer->updateRemoteDeviceRecord(Model::TRemoteDeviceRecord(0x123456789012u, 0));
        m_pInStationDataContainer->updateRemoteDeviceRecord(Model::TRemoteDeviceRecord(0x345678345678u, 0));
#endif
        using InStation::InStationHTTPClient;
        using Model::DataContainer;

        InStationHTTPClient::TRawDeviceDetectionCollection collection;
        const DataContainer::TRemoteDeviceRecordCollection& deviceRecords(
            m_pInStationDataContainer->getRemoteDeviceCollection());
        for(DataContainer::TRemoteDeviceRecordCollection::const_iterator iter(deviceRecords.begin()), iterEnd(deviceRecords.end());
            iter != iterEnd;
            ++iter)
        {
            collection.push_back(InStationHTTPClient::TRawDeviceDetection(iter->second.address));
        }

        const TTime_t INQUIRY_START_TIME(pt::second_clock::universal_time());
        m_pInStationClient->sendRawDeviceDetection(
            INQUIRY_START_TIME,
            collection,
            true,
            false);
    }
    else
    {
        Logger::log(LOG_LEVEL_DEBUG1, "InStation client is not running. Ignoring raw journey request...");
    }
}

void OutStationMainFrame::onSendCongestionReportToInStationClick(wxCommandEvent& WXUNUSED(ev))
{
//    if (m_pInStationClient != 0)
//    {
//        QueueDetection::TCongestionReport report(m_pQueueDetector->getCongestionReport());
//        m_pInStationClient->sendCongestionReport(report, true, false);
//    }
//    else
//   {
//       Logger::log(LOG_LEVEL_DEBUG1, "InStation client is not running. Ignoring congestion report request...");
//    }
}

void OutStationMainFrame::onRetrieveConfigurationClick(wxCommandEvent& WXUNUSED(ev))
{
    if (m_pRetrieveConfigurationClient != 0)
    {
        m_pRetrieveConfigurationClient->sendConfigurationRequest(true, false);
    }
    else
    {
        Logger::log(LOG_LEVEL_DEBUG1, "Retrieve Configuration client is not running. Ignoring request...");
    }
}

void OutStationMainFrame::onSendMessageToInStationClick(wxCommandEvent& WXUNUSED(ev))
{
//    if (m_pInStationClient != 0)
//    {
//        using InStation::InStationHTTPClient;
//
//        InStationHTTPClient::TAlertAndStatusReportCollection collection;
//        collection.push_back(InStationHTTPClient::TAlertAndStatusReport(001, 4));
//        collection.push_back(InStationHTTPClient::TAlertAndStatusReport(002, 5));
//
//        m_pInStationClient->sendAlertAndStatusReport(collection, true, false);
//    }
//    else
//    {
//        Logger::log(LOG_LEVEL_DEBUG1, "InStation client is not running. Ignoring request to send message...");
//    }
}

void OutStationMainFrame::onStartInStationSSHConnectionClick(wxCommandEvent& WXUNUSED(ev))
{
    if (m_pInStationReverseSSHConnector != 0)
    {
        m_pInStationReverseSSHConnector->open(10001);
    }
    //else do nothing
}

void OutStationMainFrame::onStopInStationSSHConnectionClick(wxCommandEvent& WXUNUSED(ev))
{
    if (m_pInStationReverseSSHConnector != 0)
    {
        m_pInStationReverseSSHConnector->close();
    }
    //else do nothing
}

void OutStationMainFrame::updateApplicationTitle()
{
    wxString title;
	title
		<< wxString::FromAscii(Version::getApplicationName().c_str())
		<< wxT(" - ") << wxString::FromAscii(Version::getVersionAsString().c_str());

    SetTitle(title);
}

void OutStationMainFrame::OnProgressTimer(wxTimerEvent& WXUNUSED(ev))
{
    updateCurrentTimeOnStatusBar();
    updateDeviceInquiryTimeOnStatusBar();
    m_timer.Start(100);
}

} //namespace
