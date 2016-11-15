#include "stdafx.h"
#include <gtest/gtest.h>

#ifdef _WIN32
#include <windows.h>
#include "vld.h"
#endif

#include "clock.h"
#include "logger.h"
#include "utils.h"

#include <boost/thread.hpp>
#include <stdio.h>


int taskMainFunction(int argc, char **argv)
{
    Clock gClock;

    Utils::setClock(&gClock);
    Logger::initialise("/tmp/");
    Logger::setConsoleLogLevel(LOG_LEVEL_DEBUG3);
    Logger::setFileLogLevel(LOG_LEVEL_DEBUG3);

    ::testing::InitGoogleTest(&argc, argv);
    int result = RUN_ALL_TESTS();

#ifdef _WIN32
    ::Sleep(100);

    printf("\nPress 'Q' to quit...\n");

    int ch = 0;
    do
    {
        ch = getchar (); // wait for input
        ch = toupper(ch);
    } while (ch != 'Q');
    printf("Finished!\n");

#elif defined __linux__
    //Only Linux
    ::sleep(1);
    printf("Finished!\n");
#else
    //do nothing
#endif

    Logger::destruct();
    Utils::setClock(0);

    return result;
}


int main(int argc, char **argv)
{
    //Due to valgrind/helgrind complaints invoke this function so that
    //boost::detail::set_current_thread_data() is called.
    boost::this_thread::sleep(boost::posix_time::milliseconds(0));

    return taskMainFunction(argc, argv);
}
