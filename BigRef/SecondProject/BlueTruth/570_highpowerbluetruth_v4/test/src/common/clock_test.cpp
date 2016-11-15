#include "stdafx.h"
#include <gtest/gtest.h>

#include "clock.h"

TEST(Clock, getMillisecondsFromProgramStart)
{
    Clock clock;
    uint64_t value = clock.getMillisecondsFromConstruction();
    EXPECT_TRUE(value < 1000);

    clock.getLocalTime();
}

TEST(Clock, universalTime)
{
    Clock clock;
    const TTime_t epochTime(pt::time_from_string("1970-01-01 00:00:00.000"));
    TTime_t timeValue;
    TTimeDiff_t deltaTime;

    clock.getUniversalTime();
    clock.setUniversalTime(epochTime);

    //advanceUniversalTimeBySeconds
    clock.advanceUniversalTimeBySeconds(1000);
    timeValue = clock.getUniversalTime();
    deltaTime = timeValue - epochTime;
    EXPECT_TRUE(deltaTime.total_seconds() >= 1000);
    EXPECT_TRUE(deltaTime.total_seconds() < 1000 + 1);

    //advanceUniversalTimeByMilliSeconds
    clock.advanceUniversalTimeByMilliSeconds(1000);
    timeValue = clock.getUniversalTime();
    deltaTime = timeValue - epochTime;
    EXPECT_TRUE(deltaTime.total_seconds() >= 1001);
    EXPECT_TRUE(deltaTime.total_seconds() < 1001 + 1);

    //+= operator
    clock += 99;
    timeValue = clock.getUniversalTime();
    deltaTime = timeValue - epochTime;
    EXPECT_TRUE(deltaTime.total_seconds() >= 1100);
    EXPECT_TRUE(deltaTime.total_seconds() < 1100 + 1);

    //getUniversalTimeSinceEpochInSeconds
    uint64_t value = clock.getUniversalTimeSinceEpochInSeconds();
    EXPECT_TRUE(value >= 1100);
    EXPECT_TRUE(value < 1100 + 1);

    //getUniversalTimeAsStringWithMilliseconds(string& )
    std::string timeString;
    clock.getUniversalTimeAsStringWithMilliseconds(timeString);
    EXPECT_STREQ("1970-Jan-01 00:18:20", timeString.c_str()); //18*60+20 = 1100

    //getUniversalTimeAsStringWithMilliseconds(wstring& )
    std::wstring wtimeString;
    clock.getUniversalTimeAsStringWithMilliseconds(wtimeString);
    EXPECT_STREQ(L"1970-Jan-01 00:18:20", wtimeString.c_str()); //18*60+20 = 1100
}
