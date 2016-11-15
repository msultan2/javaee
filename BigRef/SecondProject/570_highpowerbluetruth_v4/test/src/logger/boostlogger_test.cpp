#include "stdafx.h"
#include <gtest/gtest.h>

#include "managedfilelogger.h"
#include "logger.h"


TEST(Logger, all)
{
    ASSERT_FALSE(ManagedFileLogger::checkDirectoryAccess(_T("/abc/")));
    ASSERT_FALSE(ManagedFileLogger::checkDirectoryAccess(_T("/proc/")));
    ASSERT_TRUE(ManagedFileLogger::checkDirectoryAccess(_T("/tmp/")));

    Logger::initialise("/abc/");
    Logger::log(LOG_LEVEL_DEBUG3, "LOG_LEVEL_DEBUG3");
    Logger::destruct();

    Logger::initialise("/proc/");
    Logger::log(LOG_LEVEL_DEBUG3, "LOG_LEVEL_DEBUG3");
    Logger::destruct();

    Logger::initialise("/tmp/");
    Logger::setConsoleLogLevel(LOG_LEVEL_INFO);
    Logger::setFileLogLevel(LOG_LEVEL_DEBUG3);
    Logger::logLogLevel();

    Logger::log(LOG_LEVEL_DEBUG3, "LOG_LEVEL_DEBUG3");
    Logger::log(LOG_LEVEL_DEBUG2, "LOG_LEVEL_DEBUG2");
    Logger::log(LOG_LEVEL_DEBUG1, "LOG_LEVEL_DEBUG1");
    Logger::log(LOG_LEVEL_INFO, "LOG_LEVEL_INFO");
    Logger::log(LOG_LEVEL_NOTICE, "LOG_LEVEL_NOTICE");
    Logger::log(LOG_LEVEL_WARNING, "LOG_LEVEL_WARNING");
    Logger::log(LOG_LEVEL_ERROR, "LOG_LEVEL_ERROR");
    Logger::log(LOG_LEVEL_EXCEPTION, "LOG_LEVEL_EXCEPTION");
    Logger::log(LOG_LEVEL_FATAL, "LOG_LEVEL_FATAL");
    Logger::log(MAX_LOG_LEVEL_PLUS_ONE, "MAX_LOG_LEVEL_PLUS_ONE");

    Logger::log(LOG_LEVEL_DEBUG3, "LOG_LEVEL_DEBUG3 (normal chars)", "description2", "description3", "description4");
    Logger::log(LOG_LEVEL_DEBUG3, L"LOG_LEVEL_DEBUG3 (wide chars)", L"description2", L"description3", L"description4");
}
