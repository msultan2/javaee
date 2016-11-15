#include "stdafx.h"
#include "gsmmodem/test_gsmmodemsignallevelprocessor.h"


namespace Testing
{

TestGSMModemSignalLevelProcessor::TestGSMModemSignalLevelProcessor()
:
GSMModem::SignalLevelProcessor()
{
    //do nothing
}

TestGSMModemSignalLevelProcessor::~TestGSMModemSignalLevelProcessor()
{
    //do nothing
}

void TestGSMModemSignalLevelProcessor::setSignalLevel(const int value)
{
    for (size_t i=0; i<m_circularBuffer.getSize(); ++i)
    {
        m_circularBuffer.process(value);
    }
}

} //namespace
