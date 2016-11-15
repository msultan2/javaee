#include "stdafx.h"
#include <gtest/gtest.h>

#include "activeobject.h"

#include "test_activeobject.h"
#include <boost/thread/thread.hpp>

using Testing::TestActiveObject;


TEST(ActiveObject, main)
{
    //Test the plain ActiveObject
    ActiveObject object("NAME");

    EXPECT_STREQ("NAME", object.getName().c_str());
    EXPECT_FALSE(object.isRunning());

    object.resumeThread();
    object.waitUntilRunning();
    EXPECT_TRUE(object.isRunning());

    object.shutdownThread("me");
}

TEST(ActiveObject, main_extended)
{
    TestActiveObject object("NAME");

    EXPECT_STREQ("NAME", object.getName().c_str());
    EXPECT_FALSE(object.isRunning());
    EXPECT_FALSE(object.isInitThreadCalled());
    EXPECT_FALSE(object.isRunCalled());
    EXPECT_FALSE(object.isFlushThreadCalled());
    object.clearFlags();

    object.resumeThread();
    object.waitUntilRunning();

    EXPECT_FALSE(object.isIdle());
    EXPECT_TRUE(object.isRunning());
    EXPECT_TRUE(object.isInitThreadCalled());
    EXPECT_TRUE(object.isRunCalled());
    EXPECT_FALSE(object.isFlushThreadCalled());
    object.clearFlags();

    object.resumeThread();
    EXPECT_TRUE(object.isRunning());
    EXPECT_FALSE(object.isInitThreadCalled());
    EXPECT_FALSE(object.isRunCalled());
    EXPECT_FALSE(object.isFlushThreadCalled());
    object.clearFlags();

    object.shutdownThread("me");
    EXPECT_FALSE(object.isRunning());
    EXPECT_FALSE(object.isRunning());
    EXPECT_FALSE(object.isInitThreadCalled());
    EXPECT_TRUE(object.isFlushThreadCalled());
    object.clearFlags();

    object.shutdownThread("me");
    EXPECT_FALSE(object.isRunning());
    EXPECT_FALSE(object.isRunning());
    EXPECT_FALSE(object.isInitThreadCalled());
    EXPECT_FALSE(object.isFlushThreadCalled());
    object.clearFlags();
}
