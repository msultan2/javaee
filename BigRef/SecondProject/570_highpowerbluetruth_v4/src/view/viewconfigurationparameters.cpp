#include "stdafx.h"
#include "viewconfigurationparameters.h"
#include "loggerdefinitions.h"

#include <cfloat>

#ifdef _WIN32
#define USE_REGISTRY
#endif

#if !defined(USE_REGISTRY) && !defined(_DEBUG) && defined(_WIN32)
#define USE_REGISTRY
#endif

#ifndef _WIN32
#undef _T
#endif

#if defined(USE_REGISTRY)
#include <wx/config.h>
#else
//undefine _T macro on linux
#include <wx/fileconf.h>
#endif

//The following const variables must be defined elsewhere (app.cpp) file.
extern const tchar APPLICATION_NAME[];
extern const tchar VENDOR_NAME[];
extern const tchar APPLICATION_CONFIG_FILE_NAME[];

namespace
{
    const long MIN_LONG_VALUE = 0L;
    const long MAX_LONG_VALUE = LONG_MAX;

    const float MIN_FLOAT_VALUE = 0.0F;
    const float MAX_FLOAT_VALUE = FLT_MAX;

    const long DEFAULT_GUI_GRANULARITY_MS = 50L;
    const long MAX_GUI_GRANULARITY_MS = 1000L;

    const long DEFAULT_READ_APPLICATION_SCREEN_SIZE_ON_START = 1; //true
    const long DEFAULT_STORE_APPLICATION_SCREEN_SIZE_ON_START = 1; //true
    const long DEFAULT_APPLICATION_WIDTH = 1024L;
    const long DEFAULT_APPLICATION_HEIGHT = 768L;

    const long DEFAULT_EVENT_LOG_GRID_COLUMN_WIDTH_0 = 130L;
    const long DEFAULT_EVENT_LOG_GRID_COLUMN_WIDTH_1 = 50L;
    const long DEFAULT_EVENT_LOG_GRID_COLUMN_WIDTH_2 = 600L;

    const long DEFAULT_SEND_INITIALISATION_SEQUENCE_ON_CONNECT = true;

    const tchar DEFAULT_OUTSTATION_GEOGRAPHIC_ADDRESS[] = _T("M00/0000A");


    const tchar LABEL_MAXIMUM_NUMBER_OF_SAMPLES_PER_FILE[] = _T("General/MaximumNumberOfSamplesPerFile");
    const tchar LABEL_GUI_GRANULARITY_MS[] = _T("General/GUIGranularityMS");

    const tchar LABEL_FILE_LOG_LEVEL[] = _T("Logging/FileLogLevel");
    const tchar LABEL_CONSOLE_LOG_LEVEL[] = _T("Logging/ConsoleLogLevel");
    const tchar LABEL_MAX_NUMBER_OF_ENTRIES_PER_FILE[] = _T("Logging/MaxNumberOfEntriesPerFile");
    const tchar LABEL_MAX_LOG_FILE_AGE_IN_SECONDS[] = _T("Logging/MaximumLogFileAgeInSeconds");
    const tchar LABEL_MAXIMUM_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID[] = _T("Logging/MaximumNumberOfRowsInEventLogGrid");

    const tchar LABEL_READ_APPLICATION_SCREEN_SIZE_ON_START[]  = _T("Size/ReadOnStart");
    const tchar LABEL_STORE_APPLICATION_SCREEN_SIZE_ON_START[] = _T("Size/StoreOnFinish");
    const tchar LABEL_APPLICATION_SCREEN_SIZE_X[] = _T("Size/X");
    const tchar LABEL_APPLICATION_SCREEN_SIZE_Y[] = _T("Size/Y");
    const tchar LABEL_APPLICATION_SCREEN_SIZE_WIDTH[] = _T("Size/Width");
    const tchar LABEL_APPLICATION_SCREEN_SIZE_HEIGHT[] = _T("Size/Height");

    const tchar LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_0[] = _T("Size/EventLogGridColumnWidth_0");
    const tchar LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_1[] = _T("Size/EventLogGridColumnWidth_1");
    const tchar LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_2[] = _T("Size/EventLogGridColumnWidth_2");

    const tchar LABEL_SEND_INITIALISATION_SEQUENCE_ON_CONNECT[] = _T("Miscellaneous/SendInitialisationSequenceOnConnect");

    const tchar LABEL_OUTSTATION_GEOGRAPHIC_ADDRESS[] = _T("Miscellaneous/OutstationGeographicAddress");

    const tchar EMPTY_CHAR[] = _T("");

    const long DEFAULT_LOG_LEVEL = LOG_LEVEL_INFO;
    const long DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE = 100000;
    const long DEFAULT_MAX_LOG_FILE_AGE_IN_SECONDS = 60*60*24*7; //7 days
    const long DEFAULT_MAX_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID = 1000;

    const tchar LOCAL_STR_NAME[] = _T("");
    const tchar GLOBAL_STR_NAME[] = _T("");
    const tchar GLOBAL_FILE_NAME[] = _T("");

    const char MODULE_NAME[] = "ViewConfigurationParameters";

}

namespace View
{
ViewConfigurationParameters* ViewConfigurationParameters::m_instancePtr = 0;
bool ViewConfigurationParameters::m_valid = true;

bool ViewConfigurationParameters::construct()
{
    if (m_instancePtr)
    {
        // do nothing
    }
    else
    {
        m_instancePtr = new ViewConfigurationParameters();
    }

    return m_valid;
}

void ViewConfigurationParameters::destruct()
{
    delete m_instancePtr;
    m_instancePtr = 0;
}

ViewConfigurationParameters::ViewConfigurationParameters()
:
m_configPtr(0),
m_GUIUpdateGranularityMS(DEFAULT_GUI_GRANULARITY_MS),
m_fileLogLevel(DEFAULT_LOG_LEVEL),
m_consoleLogLevel(DEFAULT_LOG_LEVEL),
m_logMaxNumberOfEntriesPerFile(DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE),
m_maximumLogFileAgeInSeconds(DEFAULT_MAX_LOG_FILE_AGE_IN_SECONDS),
m_maximumNumberOfRowsInEventLogGrid(DEFAULT_MAX_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID),
m_readApplicationScreenSizeOnStart(DEFAULT_READ_APPLICATION_SCREEN_SIZE_ON_START),
m_storeApplicationScreenSizeOnExit(DEFAULT_STORE_APPLICATION_SCREEN_SIZE_ON_START),
m_applicationScreenSizeX(0),
m_applicationScreenSizeY(0),
m_applicationScreenSizeWidth(DEFAULT_APPLICATION_WIDTH),
m_applicationScreenSizeHeight(DEFAULT_APPLICATION_HEIGHT),
m_eventLogGridColumnWidth_0(),
m_eventLogGridColumnWidth_1(),
m_eventLogGridColumnWidth_2(),
m_sendInitialisationSequenceOnConnect(DEFAULT_SEND_INITIALISATION_SEQUENCE_ON_CONNECT),
m_outstationGeographicAddress(DEFAULT_OUTSTATION_GEOGRAPHIC_ADDRESS),
m_configurationErrorStrings(),
m_configurationErrorReported(false)
{

#ifdef USE_REGISTRY
    m_configPtr = new wxConfig(APPLICATION_NAME, VENDOR_NAME, LOCAL_STR_NAME, GLOBAL_STR_NAME, wxCONFIG_USE_LOCAL_FILE);
#else
    m_configPtr = new wxFileConfig(
        APPLICATION_NAME,
        VENDOR_NAME,
        APPLICATION_CONFIG_FILE_NAME,
        GLOBAL_FILE_NAME,
        wxCONFIG_USE_LOCAL_FILE|wxCONFIG_USE_RELATIVE_PATH);
#endif

    readStartupParametersFromFile();
}

ViewConfigurationParameters::~ViewConfigurationParameters()
{
    delete m_configPtr;
}

ViewConfigurationParameters* ViewConfigurationParameters::getInstancePtr()
{
    return m_instancePtr;
}

void ViewConfigurationParameters::restoreDefaultValues()
{
    m_GUIUpdateGranularityMS = DEFAULT_GUI_GRANULARITY_MS;

    m_fileLogLevel = DEFAULT_LOG_LEVEL;
    m_consoleLogLevel = DEFAULT_LOG_LEVEL;
    m_logMaxNumberOfEntriesPerFile = DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE;
    m_maximumLogFileAgeInSeconds = DEFAULT_MAX_LOG_FILE_AGE_IN_SECONDS;
    m_maximumNumberOfRowsInEventLogGrid = DEFAULT_MAX_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID;

    m_readApplicationScreenSizeOnStart = DEFAULT_READ_APPLICATION_SCREEN_SIZE_ON_START;
    m_storeApplicationScreenSizeOnExit = DEFAULT_STORE_APPLICATION_SCREEN_SIZE_ON_START;
    m_applicationScreenSizeX = 0;
    m_applicationScreenSizeY = 0;
    m_applicationScreenSizeWidth = DEFAULT_APPLICATION_WIDTH;
    m_applicationScreenSizeHeight = DEFAULT_APPLICATION_HEIGHT;

    m_sendInitialisationSequenceOnConnect = DEFAULT_SEND_INITIALISATION_SEQUENCE_ON_CONNECT;
    m_outstationGeographicAddress = DEFAULT_OUTSTATION_GEOGRAPHIC_ADDRESS;
}

void ViewConfigurationParameters::readStartupParametersFromFile()
{
}

void ViewConfigurationParameters::readAllParametersFromFile()
{
    m_configurationErrorStrings.clear();
    m_configurationErrorReported = false;

    readLongFromConfigFile(LABEL_GUI_GRANULARITY_MS, m_GUIUpdateGranularityMS, DEFAULT_GUI_GRANULARITY_MS, 0, MAX_GUI_GRANULARITY_MS);

    readLongFromConfigFile(LABEL_FILE_LOG_LEVEL, m_fileLogLevel, DEFAULT_LOG_LEVEL, MIN_LONG_VALUE, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_CONSOLE_LOG_LEVEL, m_consoleLogLevel, DEFAULT_LOG_LEVEL, MIN_LONG_VALUE, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_MAX_NUMBER_OF_ENTRIES_PER_FILE, m_logMaxNumberOfEntriesPerFile, DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE, 1, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_MAX_LOG_FILE_AGE_IN_SECONDS, m_maximumLogFileAgeInSeconds, DEFAULT_MAX_LOG_FILE_AGE_IN_SECONDS, 1, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_MAXIMUM_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID, m_maximumNumberOfRowsInEventLogGrid, DEFAULT_MAX_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID, 1, 100000);

    readLongFromConfigFile(LABEL_READ_APPLICATION_SCREEN_SIZE_ON_START, m_readApplicationScreenSizeOnStart, DEFAULT_READ_APPLICATION_SCREEN_SIZE_ON_START, 0, 1);
    readLongFromConfigFile(LABEL_STORE_APPLICATION_SCREEN_SIZE_ON_START, m_storeApplicationScreenSizeOnExit, DEFAULT_STORE_APPLICATION_SCREEN_SIZE_ON_START, 0, 1);
    readLongFromConfigFile(LABEL_APPLICATION_SCREEN_SIZE_X, m_applicationScreenSizeX, 0, LONG_MIN, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_APPLICATION_SCREEN_SIZE_Y, m_applicationScreenSizeY, 0, LONG_MIN, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_APPLICATION_SCREEN_SIZE_WIDTH, m_applicationScreenSizeWidth, DEFAULT_APPLICATION_WIDTH, 0, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_APPLICATION_SCREEN_SIZE_HEIGHT, m_applicationScreenSizeHeight, DEFAULT_APPLICATION_HEIGHT, 0, MAX_LONG_VALUE);

    readLongFromConfigFile(LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_0, m_eventLogGridColumnWidth_0, DEFAULT_EVENT_LOG_GRID_COLUMN_WIDTH_0, LONG_MIN, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_1, m_eventLogGridColumnWidth_1, DEFAULT_EVENT_LOG_GRID_COLUMN_WIDTH_1, LONG_MIN, MAX_LONG_VALUE);
    readLongFromConfigFile(LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_2, m_eventLogGridColumnWidth_2, DEFAULT_EVENT_LOG_GRID_COLUMN_WIDTH_2, LONG_MIN, MAX_LONG_VALUE);

    m_configPtr->Flush();
}


bool ViewConfigurationParameters::writeIntToConfigFile(const tchar* label, const int& variable)
{
    return m_configPtr->Write(label, variable);
}

bool ViewConfigurationParameters::writeStringToConfigFile(const tchar* label, const std::string& variable)
{
    return m_configPtr->Write(label, wxString::FromAscii(variable.c_str()));
}

bool ViewConfigurationParameters::flush()
{
    return m_configPtr->Flush();
}

bool ViewConfigurationParameters::readIntFromConfigFile(
    const tchar* label,
    int& variable,
    const int defaultValue,
    const int minValue,
    const int maxValue)
{
    bool result = true;

    long longVariable = 0;
    //Read variable from the configuration file
    if ( m_configPtr->Read(label, &longVariable) )
    {
        //Success, value has been read. Now check the range
        if ((longVariable>=static_cast<long>(minValue)) && (longVariable<=static_cast<long>(maxValue)))
        {
            //Value is in range
            variable = longVariable;
        }
        else
        {
            m_configurationErrorReported = true;

            //Log error
            std::tostringstream errorStream;
            errorStream << "Configuration parameter \"" << label
                << "\" has invalid value (expected range "
                << minValue << "-" << maxValue
                << ", default value=" << defaultValue << "). Please check your registry settings.";
            m_configurationErrorStrings.push_back(errorStream.str());

            variable = defaultValue;
            result = false;
        }
    }
    else
    {
        variable = defaultValue;
        m_configPtr->Write(label, static_cast<long>(variable));
        m_configPtr->Flush();
    }

    return result;
}

void ViewConfigurationParameters::readLongFromConfigFile(
    const tchar* label,
    long& variable,
    const long defaultValue,
    const long minLongValue,
    const long maxLongValue)
{
    //Read variable from the configuration file
    if ( m_configPtr->Read(label, &variable) )
    {
        //Success, value has been read. Now check the range
        if ((variable>=minLongValue) && (variable<=maxLongValue))
        {
            //Value is in range. Do nothing
        }
        else
        {
            m_configurationErrorReported = true;

            //Log error
            std::tostringstream errorStream;
            errorStream << "Configuration parameter \"" << label
                << "\" has invalid value (expected range "
                << minLongValue << "-" << maxLongValue
                << ", default value=" << defaultValue << "). Please check your registry settings.";
            m_configurationErrorStrings.push_back(errorStream.str());

            variable = defaultValue;
        }
    }
    else
    {
        variable = defaultValue;
        m_configPtr->Write(label, variable);
        m_configPtr->Flush();
    }
}

void ViewConfigurationParameters::readFloatFromConfigFile(
    const tchar *label,
    float& variable,
    const float defaultValue,
    const float minFloatValue,
    const float maxFloatValue)
{
    double valueFromConfigFile = 0.0;
    //Read variable
    if ( m_configPtr->Read(label, &valueFromConfigFile))
    {
        variable = static_cast<float>(valueFromConfigFile);

        //Success, value has been read. Now check the range
        if ((variable>=minFloatValue) && (variable<=maxFloatValue))
        {
            //Value is in range. Do nothing
        }
        else
        {
            m_configurationErrorReported = true;

            //Log error
            std::tostringstream errorStream;
            errorStream << "Parameter: " << label
                << " incorrect value. Expected range from "
                << minFloatValue << " to " << maxFloatValue
                << " . Using default value of " << defaultValue << " .";
            m_configurationErrorStrings.push_back(errorStream.str());

            variable = defaultValue;
        }
    }
    else
    {
        variable = defaultValue;
        m_configPtr->Write(label, static_cast<double>(variable));
        m_configPtr->Flush();
    }
}

bool ViewConfigurationParameters::readStringFromConfigFile(const tchar *label, std::string& variable, const char* pDefaultValue)
{
    wxString tmpString;

    //Read variable
    if ( m_configPtr->Read(label, &tmpString) )
    {
        variable = tmpString.ToAscii();
    }
    else
    {
        if (pDefaultValue != 0)
        {
            variable = pDefaultValue;
        }
        else
        {
            variable.clear();
        }

        m_configPtr->Write(label, wxString::FromAscii(variable.c_str()));
        m_configPtr->Flush();
    }

    return true;
}

bool ViewConfigurationParameters::readTStringFromConfigFile(const tchar *label, std::tstring& variable, const tchar* pDefaultValue)
{
    wxString tmpString;

    //Read variable
    if ( m_configPtr->Read(label, &tmpString) )
    {
        variable = tmpString;
    }
    else
    {
        if (pDefaultValue != 0)
        {
            variable = pDefaultValue;
            m_configPtr->Write(label, variable.c_str());
            m_configPtr->Flush();
        }
        //else do nothing
    }

    return true;
}

bool ViewConfigurationParameters::setFileLogLevel(const long logLevel)
{
    m_fileLogLevel = logLevel;

    bool result = m_configPtr->Write(LABEL_FILE_LOG_LEVEL, m_fileLogLevel);

    m_configPtr->Flush();

    return result;
}

bool ViewConfigurationParameters::setConsoleLogLevel(const long logLevel)
{
    m_consoleLogLevel = logLevel;

    bool result = m_configPtr->Write(LABEL_CONSOLE_LOG_LEVEL, m_consoleLogLevel);

    m_configPtr->Flush();

    return result;
}

bool ViewConfigurationParameters::setMaxNumberOfRowsInEventLogGrid(const long value)
{
    m_maximumNumberOfRowsInEventLogGrid = value;

    bool result = m_configPtr->Write(LABEL_MAXIMUM_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID, m_maximumNumberOfRowsInEventLogGrid);

    m_configPtr->Flush();

    return result;
}

bool ViewConfigurationParameters::setReadApplicationScreenSizeOnStart(const bool value)
{
    m_readApplicationScreenSizeOnStart = value;

    bool result = m_configPtr->Write(LABEL_READ_APPLICATION_SCREEN_SIZE_ON_START, m_readApplicationScreenSizeOnStart);

    m_configPtr->Flush();

    return result;
}

bool ViewConfigurationParameters::setStoreApplicationScreenSizeOnExit(const bool value)
{
    m_storeApplicationScreenSizeOnExit = value;

    bool result = m_configPtr->Write(LABEL_STORE_APPLICATION_SCREEN_SIZE_ON_START, m_storeApplicationScreenSizeOnExit);

    m_configPtr->Flush();

    return result;
}

void ViewConfigurationParameters::getApplicationScreenSize(int &x, int& y, int& width, int& height) const
{
    x = m_applicationScreenSizeX;
    y = m_applicationScreenSizeY;
    width = m_applicationScreenSizeWidth;
    height = m_applicationScreenSizeHeight;
}

bool ViewConfigurationParameters::setApplicationScreenSize(const int x, const int y, const int width, const int height)
{
    m_applicationScreenSizeX = x;
    m_applicationScreenSizeY = y;
    m_applicationScreenSizeWidth = width;
    m_applicationScreenSizeHeight = height;

    bool result = true;

    result = m_configPtr->Write(LABEL_APPLICATION_SCREEN_SIZE_X, m_applicationScreenSizeX) && result;
    result = m_configPtr->Write(LABEL_APPLICATION_SCREEN_SIZE_Y, m_applicationScreenSizeY) && result;
    result = m_configPtr->Write(LABEL_APPLICATION_SCREEN_SIZE_WIDTH, m_applicationScreenSizeWidth) && result;
    result = m_configPtr->Write(LABEL_APPLICATION_SCREEN_SIZE_HEIGHT, m_applicationScreenSizeHeight) && result;

    m_configPtr->Flush();

    return result;
}

void ViewConfigurationParameters::getEventLogGridColumnSize(int &width_0, int& width_1, int& width_2) const
{
    width_0 = m_eventLogGridColumnWidth_0;
    width_1 = m_eventLogGridColumnWidth_1;
    width_2 = m_eventLogGridColumnWidth_2;
}

bool ViewConfigurationParameters::setEventLogGridColumnSize(const int width_0, const int width_1, const int width_2)
{
    m_eventLogGridColumnWidth_0 = width_0;
    m_eventLogGridColumnWidth_1 = width_1;
    m_eventLogGridColumnWidth_2 = width_2;

    bool result = true;

    result = m_configPtr->Write(LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_0, m_eventLogGridColumnWidth_0) && result;
    result = m_configPtr->Write(LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_1, m_eventLogGridColumnWidth_1) && result;
    result = m_configPtr->Write(LABEL_EVENT_LOG_GRID_COLUMN_WIDTH_2, m_eventLogGridColumnWidth_2) && result;

    m_configPtr->Flush();

    return result;
}

} //namespace
