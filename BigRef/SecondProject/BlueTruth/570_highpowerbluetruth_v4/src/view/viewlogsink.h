/*
    System: BlueTruth Outstation
    Module: BoostLogHandler
    Author: Radoslaw Golebiewski
    Compiler: Visual Studio 2008 Express Edition
    Description: This class that implements loggin. Any logs generated
        with the Logger::log() functions will be routed to this class.
        This is the legacy log handler used in NECT and IPT projects for
        logging to the console.
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/


#ifndef _WX_GRID_LOG_SINK_H
#define _WX_GRID_LOG_SINK_H

#include "ilogsink.h"

class ViewLogSink : public ILogSink
{
public:
    //! constructor
    ViewLogSink();

    //! destructor
    virtual ~ViewLogSink();

    virtual void handleLog(const ESeverityLevel logLevel, const std::string& timeString, const std::string& text);
    virtual void handleLog(const ESeverityLevel logLevel, const std::wstring& timeString, const std::wstring& text);

    virtual void setLogLevel(const ESeverityLevel logLevel);

    virtual void setFileLogMaxNumberOfEntries(const unsigned int maxNumberOfEntries);

    virtual void setFileLogMaxNumberOfCharacters(const unsigned int maxNumberOfCharacters);

private:
    //! copy constructor, not implemented
    ViewLogSink(const ViewLogSink& rhs);
    //! copy assignment operator, not implemented
    ViewLogSink& operator=(const ViewLogSink& rhs);

    //! log logging level to the log file
    void logLogLevel();

    //private members:
    ESeverityLevel m_logLevel;
};

#endif // _WX_GRID_LOG_SINK_H
