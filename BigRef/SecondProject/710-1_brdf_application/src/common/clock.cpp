#include "stdafx.h"
#include "clock.h"

//TODO Consider removing of these preprocesor inclusions when compiling on Windows platform
#ifdef _WIN32

#include <windows.h>

#elif defined (__linux__)

#include <ctime>
#include <sys/times.h>
const TIMEOUT TIMES_CLOCKS_PER_SECOND = ::sysconf(_SC_CLK_TCK); //number of clock ticks per second

#else
#error Platform not supported
#endif


namespace
{
    const uint64_t NUMBER_OF_MILLISECONDS_PER_SECOND = 1000ull;
    const TTime_t epochTime(pt::time_from_string("1970-01-01 00:00:00.000"));
}


Clock::Clock()
:
m_numberOfMillisecondsDelta(getMillisecondsSinceEpoch()),
m_universalTime()
{
    //do nothing
}

Clock::~Clock()
{
    //do nothing
}

uint64_t Clock::getMicrosecondsSinceEpoch()
{
    TTime_t localTime(pt::microsec_clock::local_time());
    TTimeDiff_t timeSinceEpoch(localTime - epochTime);
    return timeSinceEpoch.total_microseconds();
}

uint64_t Clock::getMillisecondsSinceEpoch()
{
    TTime_t localTime(pt::microsec_clock::local_time());
    TTimeDiff_t timeSinceEpoch(localTime - epochTime);
    return timeSinceEpoch.total_milliseconds();
}

uint64_t Clock::getMillisecondsFromConstruction()
{
    //Calculate reference time
    const TTime_t localTime(pt::microsec_clock::local_time());
    const TTimeDiff_t timeSinceEpoch(localTime - epochTime);
    uint64_t result = timeSinceEpoch.total_milliseconds() - m_numberOfMillisecondsDelta;

    return result;
}

::TTime_t Clock::getLocalTime()
{
    return pt::microsec_clock::local_time();
}

uint64_t Clock::getUniversalTimeSinceEpochInSeconds()
{
    const TTime_t currentTime(getUniversalTime());
    const TTimeDiff_t currentTimeSinceEpoch(currentTime - epochTime);
    return currentTimeSinceEpoch.total_seconds();
}

#if !defined TESTING

/**
 * @brief return UTC time
 * @return UTC time as a TTime class
 */
::TTime_t Clock::getUniversalTime()
{
    return pt::microsec_clock::universal_time();
}

#else

::TTime_t Clock::getUniversalTime()
{
    //Assign true value if universalTime faked value has not been defined yet
    if (m_universalTime.is_not_a_date_time())
    {
        return pt::microsec_clock::universal_time();
    }
    else
    {
        return m_universalTime;
    }
}

void Clock::setUniversalTime(const ::TTime_t& value)
{
    m_universalTime = value;
}

void Clock::advanceUniversalTimeBySeconds(const int unsigned numberOfSeconds)
{
    assert(!m_universalTime.is_not_a_date_time());
    m_universalTime += pt::seconds(numberOfSeconds);
}

void Clock::advanceUniversalTimeByMilliSeconds(const int unsigned numberOfMilliseconds)
{
    assert(!m_universalTime.is_not_a_date_time());
    m_universalTime += pt::millisec(numberOfMilliseconds);
}

::TTime_t Clock::operator+=(const int unsigned numberOfSeconds)
{
    assert(!m_universalTime.is_not_a_date_time());
    m_universalTime += pt::seconds(numberOfSeconds);

    return m_universalTime;
}

#endif

void Clock::getUniversalTimeAsStringWithMilliseconds(std::string& result)
{
    std::basic_stringstream<char> ss;
    ss << getUniversalTime();
    result = ss.str();
}

void Clock::getUniversalTimeAsStringWithMilliseconds(std::wstring& result)
{
    std::basic_stringstream<wchar_t> ss;
    ss << getUniversalTime();
    result = ss.str();
}
