#include "stdafx.h"
#include <gtest/gtest.h>

#include "gsmmodem/gsmmodemsignallevelprocessor.h"

using GSMModem::SignalLevelProcessor;


TEST(GSMModemSignalLevelProcessor, all)
{
    SignalLevelProcessor signalLevelProcessor;
    signalLevelProcessor.setup(3);

    signalLevelProcessor.updateSignalLevel(1);
    signalLevelProcessor.updateSignalLevel(2);
    signalLevelProcessor.updateSignalLevel(3);

    EXPECT_EQ(1, signalLevelProcessor.getMinSignalLevel());
    EXPECT_EQ(2, signalLevelProcessor.getAverageSignalLevel());
    EXPECT_EQ(3, signalLevelProcessor.getMaxSignalLevel());
}
