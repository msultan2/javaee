/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
 */

#ifndef _VIEW_CONFIGURATION_PARAMETERS_H_
#define _VIEW_CONFIGURATION_PARAMETERS_H_

#if defined(_MSC_VER) && _MSC_VER < 1300
#pragma warning(disable:4786)
#endif

#include "types.h"

#include <string>
#include <sstream>
#include <map>
#include <vector>

class wxConfigBase;

namespace View
{

class ViewConfigurationParameters
{
public:

    static ViewConfigurationParameters* getInstancePtr();

    static bool construct();
    static void destruct();

    //! Destructor
    virtual ~ViewConfigurationParameters();


    long getGUIUpdateGranularityMS() const { return m_GUIUpdateGranularityMS; }

    long getFileLogLevel() const { return m_fileLogLevel; }
    bool setFileLogLevel(const long logLevel);
    long getConsoleLogLevel() const { return m_consoleLogLevel; }
    bool setConsoleLogLevel(const long logLevel);
    long getLogMaxNumberOfEntriesPerFile() const { return m_logMaxNumberOfEntriesPerFile; }
    long getMaximumLogFileAgeInSeconds() const { return m_maximumLogFileAgeInSeconds; }
    long getMaxNumberOfRowsInEventLogGrid() const { return m_maximumNumberOfRowsInEventLogGrid; }
    bool setMaxNumberOfRowsInEventLogGrid(const long value);

    long getReadApplicationScreenSizeOnStart() const { return m_readApplicationScreenSizeOnStart;  }
    bool setReadApplicationScreenSizeOnStart(const bool value);
    long getStoreApplicationScreenSizeOnExit() const { return m_storeApplicationScreenSizeOnExit;  }
    bool setStoreApplicationScreenSizeOnExit(const bool value);
    void getApplicationScreenSize(int &x, int& y, int& width, int& height) const;
    bool setApplicationScreenSize(const int x, const int y, const int width, const int height);

    void getEventLogGridColumnSize(int &width_0, int& width_1, int& width_2) const;
    bool setEventLogGridColumnSize(const int width_0, const int width_1, const int width_2);

    virtual void restoreDefaultValues();

    void readStartupParametersFromFile();
    virtual void readAllParametersFromFile();

    bool isConfigurationErrorReported() const { return m_configurationErrorReported; }
    const std::vector<std::tstring> getConfigurationErrors() const { return m_configurationErrorStrings; }

protected:
    //! Default constructor. This is singleton design pattern so this constructor is made private
    ViewConfigurationParameters();

    //! copy constructor. Not implemented
    ViewConfigurationParameters(const ViewConfigurationParameters& rhs);
    //! copy assignment operator. Not implemented
    ViewConfigurationParameters& operator=(const ViewConfigurationParameters& rhs);


    bool writeIntToConfigFile(
        const tchar* label,
        const int& variable);

    bool writeStringToConfigFile(
        const tchar* label,
        const std::string& variable);

    bool flush();


    bool readIntFromConfigFile(
        const tchar* label,
        int& variable,
        const int defaultValue,
        const int minValue,
        const int maxValue);

    void readLongFromConfigFile(
        const tchar* label,
        long& variable,
        const long defaultValue,
        const long minLongValue,
        const long maxLongValue);

    void readFloatFromConfigFile(
        const tchar* label,
        float& variable,
        const float defaultValue,
        const float minFloatValue,
        const float maxFloatValue);

    bool readStringFromConfigFile(
        const tchar* label,
        std::string& variable,
        const char* pDefaultValue);

    bool readTStringFromConfigFile(
        const tchar* label,
        std::tstring& variable,
        const tchar* pDefaultValue);

protected:

    wxConfigBase* m_configPtr;


private:
    //Private members:

    static ViewConfigurationParameters* m_instancePtr;
    static bool m_valid;

    long m_GUIUpdateGranularityMS;

    long m_fileLogLevel;
    long m_consoleLogLevel;
    long m_logMaxNumberOfEntriesPerFile;
    long m_maximumLogFileAgeInSeconds;
    long m_maximumNumberOfRowsInEventLogGrid;

    long m_readApplicationScreenSizeOnStart;
    long m_storeApplicationScreenSizeOnExit;
    long m_applicationScreenSizeX;
    long m_applicationScreenSizeY;
    long m_applicationScreenSizeWidth;
    long m_applicationScreenSizeHeight;

    long m_eventLogGridColumnWidth_0;
    long m_eventLogGridColumnWidth_1;
    long m_eventLogGridColumnWidth_2;

    //Miscellaneous
    long m_sendInitialisationSequenceOnConnect;
    std::tstring m_outstationGeographicAddress;

    std::vector<std::tstring> m_configurationErrorStrings;
    bool m_configurationErrorReported;

};

} //namespace

#endif //_VIEW_CONFIGURATION_PARAMETERS_H_
