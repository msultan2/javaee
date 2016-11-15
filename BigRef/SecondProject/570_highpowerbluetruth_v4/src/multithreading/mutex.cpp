#include "stdafx.h"
#include "mutex.h"
#include "criticalsection.h"

#include <cassert>
#include <new>

Mutex::Mutex()
: 
m_criticalSectionPtr(new (std::nothrow) CriticalSection())
{
    //do nothing
}

Mutex::~Mutex()
{
    delete m_criticalSectionPtr;
    m_criticalSectionPtr = 0;
}

void Mutex::acquire()
{
    assert(m_criticalSectionPtr != 0);
    m_criticalSectionPtr->acquire();
}

void Mutex::release()
{
    assert(m_criticalSectionPtr != 0);
    m_criticalSectionPtr->release();
}

bool Mutex::tryToAcquire(const unsigned int timeout)
{
    return m_criticalSectionPtr->tryToAcquire(timeout);
}
