/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:

    Modification History:

    Date        Who     SCJS No     Remarks
    06/10/2013  RG      001         V1.00 First Issue
*/


#ifndef _TEST_OBSERVER_H_
#define _TEST_OBSERVER_H_

#include "iobserver.h"

#include <vector>


namespace Testing
{

class TestObserver : public ::IObserver
{
public:

    //! Default constructor.
    TestObserver();

    //! Destructor.
    virtual ~TestObserver();

    //! Called to notify the observer that the file system has become
    //! (or is no longer) operational.
    virtual void notifyOfStateChange(::IObservable* pObservable, const int index);

    std::vector<int>& getIndexCollection();

private:
    //! Copy constructor, not implemented.
    TestObserver(const TestObserver&);
    //! Assignment operator, not implemented.
    TestObserver& operator=(const TestObserver&);

    std::vector<int> m_indexCollection;
};

}

#endif /*_TEST_OBSERVER_H_*/
