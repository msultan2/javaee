#include "stdafx.h"
#include "boostasio.h"

#include "logger.h"


namespace
{
    const char MODULE_NAME[] = "BoostAsio";
}


namespace Model
{

void BoostAsio::logError(
    const char* identifier,
    const char* additionalText,
    const boost::system::error_code& errorCode)
{
    if (!Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
        return;

    std::ostringstream ss;
    ss << MODULE_NAME << ": ERROR! ";
    if (identifier != 0)
    {
        ss << identifier << ", ";
    }
    //else do nothing

    if (additionalText != 0)
    {
        ss << additionalText << ": ";
    }
    //else do nothing

    ss << errorCode.message();

    //Additional description/explanation
    if (errorCode == boost::asio::error::eof)
    {
        ss << " (Connection closed cleanly by peer)";
    }
    //else do nothing

    Logger::log(
        LOG_LEVEL_DEBUG3,
        ss.str().c_str());
}

} // namespace
