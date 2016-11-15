/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/


#ifndef ILOG_SINK_H_
#define ILOG_SINK_H_

#include "logger.h"
#include <string>


class ILogSink
{
public:

    //! destructor
    virtual ~ILogSink();

    //! display error
    virtual void handleLog(const ESeverityLevel logLevel, const std::string& timeString, const std::string& text) = 0;
    virtual void handleLog(const ESeverityLevel logLevel, const std::wstring& timeString, const std::wstring& text) = 0;

    virtual void setLogLevel(const ESeverityLevel logLevel) = 0;

    virtual void setFileLogMaxNumberOfEntries(const unsigned int maxNumberOfEntries) = 0;

    virtual void setFileLogMaxNumberOfCharacters(const unsigned int maxNumberOfCharacters) = 0;

    virtual void logLogLevel() = 0;

protected:

    //! default constructor
    ILogSink();

    //! copy constructor
    ILogSink(const ILogSink& rhs);

    //! copy assignment operator
    ILogSink& operator=(const ILogSink&);
};

#endif //ILOG_SINK_H_
