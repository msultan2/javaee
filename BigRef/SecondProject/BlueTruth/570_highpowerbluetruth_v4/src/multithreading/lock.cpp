#include "stdafx.h"
#include "lock.h"

Lock::~Lock()
{
    m_mutex.release();
}
