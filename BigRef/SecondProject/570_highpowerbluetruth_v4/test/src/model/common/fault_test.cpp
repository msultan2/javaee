#include "stdafx.h"
#include <gtest/gtest.h>

#include "datacontainer.h"

using Model::DataContainer;
using Model::Fault;
using Model::RemoteDeviceRecord;


TEST(Fault, fullCycle)
{
    Fault fault;

    EXPECT_FALSE(fault.get());
    EXPECT_TRUE(fault.wasReported());
    EXPECT_FALSE(fault.isPending());
    EXPECT_TRUE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_TRUE(fault.getClearTime() == pt::not_a_date_time);

    fault.set();
    EXPECT_TRUE(fault.get());
    EXPECT_FALSE(fault.wasReported());
    EXPECT_FALSE(fault.isPending());
    EXPECT_FALSE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_TRUE(fault.getClearTime() == pt::not_a_date_time);

    fault.setPending();
    EXPECT_TRUE(fault.get());
    EXPECT_FALSE(fault.wasReported());
    EXPECT_TRUE(fault.isPending());
    EXPECT_FALSE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_TRUE(fault.getClearTime() == pt::not_a_date_time);

    fault.setWasReported();
    EXPECT_TRUE(fault.get());
    EXPECT_TRUE(fault.wasReported());
    EXPECT_FALSE(fault.isPending());
    EXPECT_FALSE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_TRUE(fault.getClearTime() == pt::not_a_date_time);

    fault.clear();
    EXPECT_FALSE(fault.get());
    EXPECT_FALSE(fault.wasReported());
    EXPECT_FALSE(fault.isPending());
    EXPECT_FALSE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_FALSE(fault.getClearTime() == pt::not_a_date_time);

    fault.setPending();
    EXPECT_FALSE(fault.get());
    EXPECT_FALSE(fault.wasReported());
    EXPECT_TRUE(fault.isPending());
    EXPECT_FALSE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_FALSE(fault.getClearTime() == pt::not_a_date_time);

    fault.setWasReported();
    EXPECT_FALSE(fault.get());
    EXPECT_TRUE(fault.wasReported());
    EXPECT_FALSE(fault.isPending());
    EXPECT_FALSE(fault.getSetTime() == pt::not_a_date_time);
    EXPECT_FALSE(fault.getClearTime() == pt::not_a_date_time);
}
