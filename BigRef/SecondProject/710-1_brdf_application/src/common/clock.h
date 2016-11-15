/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _CLOCK_H_
#define _CLOCK_H_


#include <stdint.h>

#include <boost/date_time/local_time/local_time.hpp>
namespace pt = boost::posix_time;
typedef pt::ptime TTime_t;
typedef pt::time_duration TTimeDiff_t;
typedef unsigned long long TIMEOUT;


/**
 * @brief A class that wraps-up clock/timing functionality
 */
class Clock
{
public:
    //! default constructor
    Clock();

    virtual ~Clock();

    /**Get number of milliseconds since clock construction
     * @return the elapsed time in milliseconds since clock object creation/construction
     */
    virtual uint64_t getMillisecondsFromConstruction();

    /**
     * Get local time as TTime_t class
     */
    virtual ::TTime_t getLocalTime();

    /**
     * @brief Get current UTC time
     */
    ::TTime_t getUniversalTime();

    void getUniversalTimeAsStringWithMilliseconds(std::string& result);
    void getUniversalTimeAsStringWithMilliseconds(std::wstring& result);

    /**
     * @brief Get current UTC time as number of seconds since 1970/1/1 0:00
     *
     * @return UTC time as number of seconds since 1970/1/1 0:00
     */
    uint64_t getUniversalTimeSinceEpochInSeconds();

#ifdef TESTING

    /**
     * @brief Set clock to a particular value in time
     *
     * This function should be used for testing to initialise clock to a particular value
     * @param value time value to be assigned to the clock
     */
    void setUniversalTime(const ::TTime_t& value);

    /**
     * @brief Add seconds to the current time
     *
     * This function should be used for testing to simulate progress of time
     * @param numberOfSeconds number of seconds to add to the currently simulated time
     */
    void advanceUniversalTimeBySeconds(const int unsigned numberOfSeconds);

    /**
     * @brief Add milliseconds to the current time
     *
     * This function should be used for testing to simulate progress of time
     * @param numberOfMilliseconds number of milliseconds to add to the currently simulated time
     */
    void advanceUniversalTimeByMilliSeconds(const int unsigned numberOfMilliseconds);

    /**
     * @brief Add seconds to the current time
     *
     * This function should be used for testing to simulate progress of time
     * @param numberOfSeconds number of seconds to add to the currently simulated time
     */
    ::TTime_t operator+=(const int unsigned numberOfSeconds);

#endif

    ///! @return the elapsed time in microseconds since EPOCH 1/1/1970
    static uint64_t getMicrosecondsSinceEpoch();

    ///! @return the elapsed time in milliseconds since EPOCH 1/1/1970
    static uint64_t getMillisecondsSinceEpoch();

    //!
    //!
    void setMillisecondsSinceEpoch(const uint64_t numberOfMilliseconds);

private:

    //! copy constructor. Not implemented
    Clock(const Clock& rhs);
    //! copy assignment operator. Not implemented
    Clock& operator=(const Clock& rhs);


    //Private memebers:
    uint64_t m_numberOfMillisecondsDelta;

	::TTime_t m_universalTime;
};

#endif //_CLOCK_H_
