/*
    System: EAV
    Module: BoostLogSink
    Author: Radoslaw Golebiewski
    Compiler: Visual Studio 2008 Express Edition
    Description: This class that implements loggin. Any logs generated 
        with the Logger::log() functions will be routed to this class.
        The class uses boost log library to generate logs. 
    Modification History:

    Date        Who     SCJS No     Remarks
    2012-09-27  RG      001         Created
*/

#ifndef _BOOST_LOG_SINK_H
#define _BOOST_LOG_SINK_H

#include "ilogsink.h"
#include "types.h"

#include <boost/log/common.hpp>


class BoostLogSink : public ILogSink
{
public:
    //! constructor
    BoostLogSink();

    //! destructor
    virtual ~BoostLogSink();

    virtual void handleLog(const LoggingLevel logLevel, const std::string& text);
    virtual void handleLog(const LoggingLevel logLevel, const std::wstring& text);

    virtual void setLogLevel(const LoggingLevel logLevel);

    void deleteOldLogFiles(const time_t maximumLogFileAgeInSeconds);

private:
    //! copy constructor, not implemented
    BoostLogSink(const BoostLogSink& rhs);
    //! copy assignment operator, not implemented
    BoostLogSink& operator=(const BoostLogSink& rhs);
    
    //! log logging level to the log file
    void logLogLevel();

    static boost::log::sources::logger m_boostLogger;

    LoggingLevel m_logLevel;
};

#endif // _BOOST_LOG_SINK_H
