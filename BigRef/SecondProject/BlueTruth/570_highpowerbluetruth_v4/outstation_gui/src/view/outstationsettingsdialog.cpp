#include "stdafx.h"
#include "outstationsettingsdialog.h"

#include "bluetooth/devicediscoverer.h"
#include "datacontainer.h"
#include "lock.h"
#include "logger.h"
#include "loghandler.h"
#include "model.h"
#include "os_utilities.h"
#include "outstationmainframe.h"
#include "outstationconfigurationparameters.h"
#include "utils.h"
#include "view.h"

#include <wx/msgdlg.h>
#include <wx/busyinfo.h>


namespace
{
    const char MODULE_NAME[] = "OutStationSettingsDialog";
}

namespace View
{

OutStationSettingsDialog::OutStationSettingsDialog(
    wxWindow* pParent,
    OutStationConfigurationParameters& configurationParameters,
    LogHandler& logHandler)
:
SettingsDialog(pParent),
KeyPressHandler(),
::IObservable(),
m_configurationParameters(configurationParameters),
m_errorText(),
m_logHandler(logHandler)
{
    new KeyPressEventHandler(this, this);

    m_comboBoxBluetoothDeviceDriverPtr->Clear();
#if defined _WIN32
    m_comboBoxBluetoothDeviceDriverPtr->Append(wxString(wxT("WindowsBluetooth")));
    m_comboBoxBluetoothDeviceDriverPtr->Append(wxString(wxT("WindowsWSA")));
#elif defined __linux__
    m_comboBoxBluetoothDeviceDriverPtr->Append(wxString(wxT("RawHCI")));
    m_comboBoxBluetoothDeviceDriverPtr->Append(wxString(wxT("NativeBluez")));
    m_comboBoxBluetoothDeviceDriverPtr->Append(wxString(wxT("Parani")));
#else
#error Operating System not supported
#endif


    //Prepare Log level comboboxes
    m_comboBoxVisualLogLevelPtr->Clear();
    m_comboBoxFileLogLevelPtr->Clear();
    for(int i=0; i<MAX_LOG_LEVEL_PLUS_ONE; ++i)
    {
        switch (i)
        {
            case LOG_LEVEL_DEBUG3:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("DEBUG3")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("DEBUG3")));
                break;
            }
            case LOG_LEVEL_DEBUG2:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("DEBUG2")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("DEBUG2")));
                break;
            }
            case LOG_LEVEL_DEBUG1:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("DEBUG1")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("DEBUG1")));
                break;
            }
            case LOG_LEVEL_INFO:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("INFO")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("INFO")));
                break;
            }
            case LOG_LEVEL_NOTICE:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("NOTICE")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("NOTICE")));
                break;
            }
            case LOG_LEVEL_WARNING:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("WARNING")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("WARNING")));
                break;
            }
            case LOG_LEVEL_ERROR:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("ERROR")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("ERROR")));
                break;
            }
            case LOG_LEVEL_EXCEPTION:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("EXCEPTION")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("EXCEPTION")));
                break;
            }
            case LOG_LEVEL_FATAL:
            {
                m_comboBoxVisualLogLevelPtr->Append(wxString(wxT("FATAL")));
                m_comboBoxFileLogLevelPtr->Append(wxString(wxT("FATAL")));
                break;
            }
            default:
            {
                break;
            }
        }
    }
}

OutStationSettingsDialog::~OutStationSettingsDialog()
{
    //do nothing
}

void OutStationSettingsDialog::setup(boost::shared_ptr<Model::DataContainer> pDataContainer)
{
    m_pDataContainer = pDataContainer;
}

void OutStationSettingsDialog::onInit( wxInitDialogEvent& WXUNUSED(ev) )
{
    wxInitDialogEvent initEv;
    OnInitDialog(initEv);

    //Set selection for logLevel
    m_comboBoxVisualLogLevelPtr->SetSelection(
        static_cast<int>(m_logHandler.getConsoleLogLevel()));
    m_comboBoxFileLogLevelPtr->SetSelection(
        static_cast<int>(m_logHandler.getFileLogLevel()));
}


void OutStationSettingsDialog::onClose( wxCloseEvent& WXUNUSED(ev) )
{
    if (GetReturnCode() == wxID_NONE)
    {
        SetReturnCode(wxID_CANCEL);
    }
    //else do nothing

    Hide();
}

void OutStationSettingsDialog::onResetToDefaultsClick( wxCommandEvent& WXUNUSED(ev) )
{
    m_configurationParameters.restoreDefaultValues();
    populateWidgets();
}

void OutStationSettingsDialog::onClickOk( wxCommandEvent& WXUNUSED(ev) )
{
    if ( Validate() && TransferDataFromWindow() )
    {
        //Site Identifier
#if wxUSE_UNICODE
        std::string siteIdentifier(m_textCtrlSiteIdentifierPtr->GetValue().ToAscii().data());
#else
        std::string siteIdentifier(m_textCtrlSiteIdentifierPtr->GetValue().ToAscii());
#endif
        m_configurationParameters.setSiteIdentifier(siteIdentifier);

        //SSL Serial Number
#if wxUSE_UNICODE
        std::string sslSerialNumber(m_textCtrlSSLSerialNumberPtr->GetValue().ToAscii().data());
#else
        std::string sslSerialNumber(m_textCtrlSSLSerialNumberPtr->GetValue().ToAscii());
#endif
        m_configurationParameters.setSSLSerialNumber(sslSerialNumber);

        //Configuration URL
#if wxUSE_UNICODE
        std::string configurationURL(m_textCtrlConfigurationURLPtr->GetValue().ToAscii().data());
#else
        std::string configurationURL(m_textCtrlConfigurationURLPtr->GetValue().ToAscii());
#endif
        m_configurationParameters.setConfigurationURL(configurationURL);


        //Device driver to be used
        const std::string CURRENT_DEVICE_DRIVER(m_configurationParameters.getDeviceDriver());
        const std::string NEW_DEVICE_DRIVER(m_comboBoxBluetoothDeviceDriverPtr->GetStringSelection().ToAscii());
        if (CURRENT_DEVICE_DRIVER != NEW_DEVICE_DRIVER)
        {
            m_configurationParameters.setDeviceDriver(NEW_DEVICE_DRIVER);

            const wxString NEW_DEVICE_DRIVER_STR(wxString::FromAscii(NEW_DEVICE_DRIVER.c_str()));
#if defined _WIN32

            if (NEW_DEVICE_DRIVER_STR == wxT("WindowsWSA"))
            {
                m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                    Model::eDEVICE_DRIVER_WINDOWS_WSA;
            }
            else
            {
                m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                    Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH;
            }

#elif defined __linux__

            if (NEW_DEVICE_DRIVER_STR == wxT("NativeBluez"))
            {
                m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                    Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ;
            }
            else if (NEW_DEVICE_DRIVER_STR == wxT("Parani"))
            {
                m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                    Model::eDEVICE_DRIVER_LINUX_PARANI;

                m_pDataContainer->getLocalDeviceConfiguration().paraniPortName =
                    m_configurationParameters.getParaniPortName();
                m_pDataContainer->getLocalDeviceConfiguration().paraniBitRate =
                    m_configurationParameters.getParaniBitRate();
            }
            else
            {
                m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                    Model::eDEVICE_DRIVER_LINUX_RAW_HCI;
            }

            m_pDataContainer->getLocalDeviceRecord().hciRoute = -1;

#else
#error Operating System not supported
#endif

        }
        //else do nothing

        //Bluetooth device to be used
        Model::DataContainer::TLocalDeviceRecordCollection localDeviceCollection;
        Model::TLocalDeviceConfiguration localDeviceConfiguration;
        if (m_pDataContainer != 0)
        {
            localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();
        }
        //else do nothing

        boost::shared_ptr<BlueTooth::DeviceDiscoverer> pDeviceDiscoverer = Model::Model::getDeviceDiscoverer();
        if (pDeviceDiscoverer != 0)
        {
#ifdef _WIN32
            pDeviceDiscoverer->getLocalDeviceCollection(localDeviceConfiguration, &localDeviceCollection);
#elif defined __linux__
            if (localDeviceConfiguration.deviceDriver == Model::eDEVICE_DRIVER_LINUX_PARANI)
            {
                localDeviceCollection[m_pDataContainer->getLocalDeviceRecord().address] =
                    m_pDataContainer->getLocalDeviceRecord();
            }
            else
            {
                pDeviceDiscoverer->getLocalDeviceCollection(localDeviceConfiguration, &localDeviceCollection);
            }
#else
#error Operating System not supported
#endif
        }
        //else do nothing

        //Iterate over the entire collection and find the selected device. Then set this device to be used.
        const wxString blueToothDeviceComboBoxSelection(m_comboBoxBluetoothDeviceToBeUsedPtr->GetValue());
        for (Model::DataContainer::TLocalDeviceRecordCollection::const_iterator iter(localDeviceCollection.begin()), iterEnd(localDeviceCollection.end());
            iter != iterEnd;
            ++iter)
        {
            wxString comboEntryText(wxString::FromAscii(Utils::convertMACAddressToString(iter->second.address).c_str()));
            if (!iter->second.name.empty())
            {
                comboEntryText << wxT(" (") << wxString::FromAscii(iter->second.name.c_str()) << wxT(")");
            }
            //else do nothing

            if (blueToothDeviceComboBoxSelection == comboEntryText)
            {
                if (m_pDataContainer != 0)
                {
                    m_pDataContainer->setLocalDeviceRecord(iter->second);
                }
                //else do nothing

                break;
            }
            //else continue
        }


        const int VISUAL_LOG_LEVEL = m_comboBoxVisualLogLevelPtr->GetSelection();
        switch (VISUAL_LOG_LEVEL)
        {
            case LOG_LEVEL_DEBUG3:
            case LOG_LEVEL_DEBUG2:
            case LOG_LEVEL_DEBUG1:
            case LOG_LEVEL_INFO:
            case LOG_LEVEL_NOTICE:
            case LOG_LEVEL_WARNING:
            case LOG_LEVEL_ERROR:
            case LOG_LEVEL_EXCEPTION:
            case LOG_LEVEL_FATAL:
            {
                if (static_cast<int>(m_logHandler.getConsoleLogLevel()) != VISUAL_LOG_LEVEL)
                {
                    m_logHandler.setConsoleLogLevel(static_cast<LoggingLevel>(VISUAL_LOG_LEVEL));
                    m_configurationParameters.setConsoleLogLevel(static_cast<long>(VISUAL_LOG_LEVEL));

                    Logger::log(
                        LOG_LEVEL_NOTICE,
                        _T("Visual Log Level changed to "),
                        m_comboBoxVisualLogLevelPtr->GetString(VISUAL_LOG_LEVEL)
                        );
                }
                //else do nothing

                break;
            }
            default:
            {
                break;
            }
        }

        const int FILE_LOG_LEVEL = m_comboBoxFileLogLevelPtr->GetSelection();
        switch (FILE_LOG_LEVEL)
        {
            case LOG_LEVEL_DEBUG3:
            case LOG_LEVEL_DEBUG2:
            case LOG_LEVEL_DEBUG1:
            case LOG_LEVEL_INFO:
            case LOG_LEVEL_NOTICE:
            case LOG_LEVEL_WARNING:
            case LOG_LEVEL_ERROR:
            case LOG_LEVEL_EXCEPTION:
            case LOG_LEVEL_FATAL:
            {
                if (static_cast<int>(m_logHandler.getFileLogLevel()) != FILE_LOG_LEVEL)
                {
                    m_logHandler.setFileLogLevel(static_cast<LoggingLevel>(FILE_LOG_LEVEL));
                    m_configurationParameters.setFileLogLevel(static_cast<long>(FILE_LOG_LEVEL));

                    Logger::log(
                        LOG_LEVEL_NOTICE,
                        _T("File Log Level changed to "),
                        m_comboBoxFileLogLevelPtr->GetString(FILE_LOG_LEVEL)
                        );
                }
                //else do nothing

                break;
            }
            default:
            {
                break;
            }
        }

        {
            const int MAX_NUMBER_OF_ENTRIES_IN_EVENT_LOG_GRID = m_spinCtrlMaxNumberOfEntriesInEventLogGrid->GetValue();
            if (m_configurationParameters.getMaxNumberOfRowsInEventLogGrid() != MAX_NUMBER_OF_ENTRIES_IN_EVENT_LOG_GRID)
            {
                View::getMainFrame()->setMaximumNumberOfRowsInEventLogGrid(MAX_NUMBER_OF_ENTRIES_IN_EVENT_LOG_GRID);
                m_configurationParameters.setMaxNumberOfRowsInEventLogGrid(static_cast<long>(MAX_NUMBER_OF_ENTRIES_IN_EVENT_LOG_GRID));

                Logger::log(
                    LOG_LEVEL_NOTICE,
                    _T("Max number of entries in event log grid changed to "),
                    wxString::Format(wxT("%d"), m_spinCtrlMaxNumberOfEntriesInEventLogGrid->GetValue())
                    );
            }
            //else do nothing
        }

        {
            const bool SHOULD_READ_SCREEN_SIZE_ON_PROGRAM_START =
                (m_configurationParameters.getReadApplicationScreenSizeOnStart()!=0);
            if (SHOULD_READ_SCREEN_SIZE_ON_PROGRAM_START != m_checkBoxReadOnProgramStartPtr->GetValue())
            {
                m_configurationParameters.setReadApplicationScreenSizeOnStart(
                    m_checkBoxReadOnProgramStartPtr->GetValue() ? 1:0 );
            }
            //else do nothing
        }

        {
            const bool SHOULD_STORE_SCREEN_SIZE_ON_PROGRAM_EXIT =
                (m_configurationParameters.getStoreApplicationScreenSizeOnExit()!=0);
            if (SHOULD_STORE_SCREEN_SIZE_ON_PROGRAM_EXIT != m_checkBoxSaveOnProgramExitPtr->GetValue())
            {
                m_configurationParameters.setStoreApplicationScreenSizeOnExit(
                    m_checkBoxSaveOnProgramExitPtr->GetValue() ? 1:0 );
            }
            //else do nothing
        }

        Close();
        SetReturnCode(wxID_OK);
    }
    else
    {
        //Display error message
        wxMessageDialog invalidSettingsDialogPtr(
            this,
            m_errorText,
            wxT("Invalid settings"),
            wxOK | wxICON_ERROR);

        invalidSettingsDialogPtr.CenterOnParent();
        invalidSettingsDialogPtr.ShowModal();
        m_errorText.clear();
    }
}


void OutStationSettingsDialog::onClickCancel( wxCommandEvent& WXUNUSED(ev) )
{
    m_configurationParameters.readAllParametersFromFile();

    SetReturnCode(wxID_CANCEL);

    Hide();
}

bool OutStationSettingsDialog::onKeyPressed(const wxKeyEvent& ev)
{
    bool retVal = false;
    const int KEY_CODE = ev.GetKeyCode();

    if (KEY_CODE == WXK_ESCAPE)
    {
        wxCommandEvent evt;
        onClickCancel(evt);

        retVal = true;
    }
    else
    {
        // do nothing
    }

    return retVal;
}

bool OutStationSettingsDialog::TransferDataToWindow()
{
    m_configurationParameters.readAllParametersFromFile();
    populateWidgets();

    SetReturnCode(wxID_NONE);
    return true;
}

void OutStationSettingsDialog::populateWidgets()
{
    m_textCtrlSiteIdentifierPtr->SetValue(
        wxString::FromAscii(m_configurationParameters.getSiteIdentifier().c_str()));
    m_textCtrlSSLSerialNumberPtr->SetValue(
        wxString::FromAscii(m_configurationParameters.getSSLSerialNumber().c_str()));
    m_textCtrlConfigurationURLPtr->SetValue(
        wxString::FromAscii(m_configurationParameters.getConfigurationURL().c_str()));

    m_spinCtrlMaxNumberOfEntriesInEventLogGrid->SetValue(
        static_cast<int>(m_configurationParameters.getMaxNumberOfRowsInEventLogGrid()));
    m_checkBoxReadOnProgramStartPtr->SetValue(
        m_configurationParameters.getReadApplicationScreenSizeOnStart() != 0);
    m_checkBoxSaveOnProgramExitPtr->SetValue(
        m_configurationParameters.getStoreApplicationScreenSizeOnExit() != 0);


    //Check all available local devices and append to the combobox list
    //First check what device is currently used.
    Model::TLocalDeviceRecord localDeviceFromDataContainer;
    if (m_pDataContainer != 0)
    {
        localDeviceFromDataContainer = m_pDataContainer->getLocalDeviceRecord();
    }
    //else do nothing

    Model::DataContainer::TLocalDeviceRecordCollection localDeviceCollection;
    Model::LocalDeviceConfiguration localDeviceConfiguration;
    if (m_pDataContainer != 0)
    {
        localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();
    }
    //else do nothing

    boost::shared_ptr<BlueTooth::DeviceDiscoverer> pDeviceDiscoverer = Model::Model::getDeviceDiscoverer();
    if (pDeviceDiscoverer != 0)
    {
        wxBusyInfo* busyInfoPtr = new wxBusyInfo(_T("Waiting for the current scan to finish..."), NULL);

        //::Lock lock(pDeviceDiscoverer->getMutex());
        pDeviceDiscoverer->getLocalDeviceCollection(localDeviceConfiguration, &localDeviceCollection);

        delete busyInfoPtr;
    }
    //else do nothing


    //Populate combobox and select a device currently in use
    int i=0;
    m_comboBoxBluetoothDeviceToBeUsedPtr->Clear();
    for (Model::DataContainer::TLocalDeviceRecordCollection::const_iterator iter(localDeviceCollection.begin()), iterEnd(localDeviceCollection.end());
        iter != iterEnd;
        ++iter)
    {
        wxString comboEntryText(wxString::FromAscii(Utils::convertMACAddressToString(iter->second.address).c_str()));
        if (!iter->second.name.empty())
        {
            comboEntryText << wxT(" (") << wxString::FromAscii(iter->second.name.c_str()) << wxT(")");
        }
        //else do nothing

        m_comboBoxBluetoothDeviceToBeUsedPtr->Append(comboEntryText);
        if (localDeviceFromDataContainer.address == iter->second.address)
        {
            m_comboBoxBluetoothDeviceToBeUsedPtr->SetSelection(i);
        }
        //else do nothing

        ++i;
    }

    wxString currentDeviceDriver(wxString::FromAscii(m_configurationParameters.getDeviceDriver().c_str()));
    switch (localDeviceConfiguration.deviceDriver)
    {
#if defined _WIN32
        case Model::eDEVICE_DRIVER_WINDOWS_WSA:
        {
            currentDeviceDriver = wxT("WindowsWSA");
            break;
        }

        case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
        default:
        {
            currentDeviceDriver = wxT("WindowsBluetooth");
            break;
        }

#elif defined __linux__

        case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
        {
            currentDeviceDriver = wxT("NativeBluez");
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_PARANI:
        {
            currentDeviceDriver = wxT("Parani");
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
        default:
        {
            currentDeviceDriver = wxT("RawHCI");
            break;
        }

#else
#error Operating System not supported
#endif
    }

    for (unsigned int i=0; i < m_comboBoxBluetoothDeviceDriverPtr->GetCount(); ++i)
    {
        if (m_comboBoxBluetoothDeviceDriverPtr->GetString(i) == currentDeviceDriver)
        {
            m_comboBoxBluetoothDeviceDriverPtr->SetSelection(i);
            break;
        }
        //else do nothing
    }

    Fit();
    Layout();
}

bool OutStationSettingsDialog::TransferDataFromWindow()
{
    return true;
}

bool OutStationSettingsDialog::Validate()
{
    return true;
}

} //namespace
