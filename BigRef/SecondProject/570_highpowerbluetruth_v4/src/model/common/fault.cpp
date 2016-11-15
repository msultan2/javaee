#include "stdafx.h"
#include "fault.h"

#include "types.h"


namespace Model
{

Fault::Fault()
:
m_value(false),
m_wasReported(true),
m_pending(false),
m_setTime(pt::not_a_date_time),
m_clearTime(pt::not_a_date_time)
{
    //do nothing
}

Fault::~Fault()
{
    //do nothing
}

void Fault::set()
{
    if (m_wasReported && !m_value)
    {
        m_value = true;
        m_wasReported = false;
		m_setTime = pt::microsec_clock::universal_time();
    }
    //else do not clear
}

void Fault::clear()
{
    if (m_wasReported && m_value)
    {
        m_value = false;
        m_wasReported = false;
		m_clearTime = pt::microsec_clock::universal_time();
    }
    //else do not clear
}

void Fault::setWasReported()
{
    if (m_pending)
    {
        m_wasReported = true;
        m_pending = false;
    }
    //else do nothing
}

const ::TTime_t& Fault::getSetTime() const
{
	return m_setTime;
}

const ::TTime_t& Fault::getClearTime() const
{
	return m_clearTime;
}

} //namespace
