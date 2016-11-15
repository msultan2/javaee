/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    06/08/2013  RG      001         V1.00 First Issue
*/

#ifndef _FAULT_H_
#define _FAULT_H_


#include "types.h"


namespace Model
{

class Fault
{
public:

    //! default constructor
    Fault();
    //! destructor
    virtual ~Fault();

    bool get() const { return m_value; }
    void set();
    void clear();

    bool wasReported() const { return m_wasReported; }
    void setWasReported();

    bool isPending() const { return m_pending; }
    void setPending() { m_pending = true; }
    void clearPending() { m_pending = false; }

	const ::TTime_t& getSetTime() const;
	const ::TTime_t& getClearTime() const;

protected:

    //! copy constructor. Not implemented
    Fault(const Fault& );
    //! assignment operator. Not implemented
    Fault& operator=(const Fault& );

    //Private members:
    bool m_value;
    bool m_wasReported;
    //TODO Should this variable be associated with a mutex?
    bool m_pending;

	::TTime_t m_setTime;
	::TTime_t m_clearTime;
};

}

#endif //_FAULT_H_
