/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    23/10/2013  RG      001         V1.00 First Issue
*/

#ifndef _SIGNAL_LEVEL_PROCESSOR_H_
#define _SIGNAL_LEVEL_PROCESSOR_H_

#include "types.h"
#include "circularbuffer.h"

#include "mutex.h"


namespace GSMModem
{

class SignalLevelProcessor
{

public:

    //! default constructor
    SignalLevelProcessor();
    //! destructor
    virtual ~SignalLevelProcessor();

    void setup(const size_t bufferSize);

    int getMinSignalLevel() const;
    int getAverageSignalLevel() const;
    int getMaxSignalLevel() const;

    void updateSignalLevel(const int value);
    void reset();


protected:

    Model::CircularBuffer<int> m_circularBuffer;
    mutable ::Mutex m_circularBufferMutex;


private:
    //! copy constructor, not implemented
    SignalLevelProcessor(const SignalLevelProcessor& );
    //! assignment operator, not implemented
    SignalLevelProcessor& operator=(const SignalLevelProcessor& );

};

}

#endif //_SIGNAL_LEVEL_PROCESSOR_H_
