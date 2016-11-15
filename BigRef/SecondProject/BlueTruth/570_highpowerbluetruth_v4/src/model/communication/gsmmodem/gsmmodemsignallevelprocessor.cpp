#include "stdafx.h"
#include "gsmmodem/gsmmodemsignallevelprocessor.h"

#include "lock.h"

#include <cassert>


namespace
{
    const int NOT_PRESENT_VALUE = -255;
}


namespace GSMModem
{

SignalLevelProcessor::SignalLevelProcessor()
:
m_circularBuffer(1),
m_circularBufferMutex()
{
    //do nothing
}

SignalLevelProcessor::~SignalLevelProcessor()
{
    //do nothing
}

void SignalLevelProcessor::setup(const size_t bufferSize)
{
    ::Lock lock(m_circularBufferMutex);
    m_circularBuffer.setSize(bufferSize);
    m_circularBuffer.fill(NOT_PRESENT_VALUE);
}

int SignalLevelProcessor::getMinSignalLevel() const
{
    ::Lock lock(m_circularBufferMutex);
    int result = INT_MAX;
    const size_t BUFFER_SIZE = m_circularBuffer.getSize();

    for (size_t i=0; i<BUFFER_SIZE; ++i)
    {
        if (m_circularBuffer.getDelayed(i) < result)
            result = m_circularBuffer.getDelayed(i);
        //else do nothing
    }

    return result;
}

int SignalLevelProcessor::getAverageSignalLevel() const
{
    ::Lock lock(m_circularBufferMutex);
    int result = 0;
    int numberOfValues = 0;

    const size_t BUFFER_SIZE = m_circularBuffer.getSize();
    assert(BUFFER_SIZE > 0);

    for (size_t i=0; i<BUFFER_SIZE; ++i)
    {
        const int value = m_circularBuffer.getDelayed(i);
        if (value != NOT_PRESENT_VALUE)
        {
            result += value;
            ++numberOfValues;
        }
        //else do nothing
    }

    if (numberOfValues > 0)
        result /= numberOfValues;
    else
        result = NOT_PRESENT_VALUE;

    return result;
}

int SignalLevelProcessor::getMaxSignalLevel() const
{
    ::Lock lock(m_circularBufferMutex);
    int result = INT_MIN;
    const size_t BUFFER_SIZE = m_circularBuffer.getSize();

    for (size_t i=0; i<BUFFER_SIZE; ++i)
    {
        if (m_circularBuffer.getDelayed(i) > result)
            result = m_circularBuffer.getDelayed(i);
        //else do nothing
    }

    return result;
}

void SignalLevelProcessor::updateSignalLevel(const int value)
{
    ::Lock lock(m_circularBufferMutex);
    m_circularBuffer.process(value);
}

void SignalLevelProcessor::reset()
{
    ::Lock lock(m_circularBufferMutex);
    m_circularBuffer.fill(NOT_PRESENT_VALUE);
}

} //namespace
