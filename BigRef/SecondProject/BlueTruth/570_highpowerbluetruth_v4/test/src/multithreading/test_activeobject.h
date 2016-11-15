#ifndef TEST_ACTIVE_OBJECT
#define TEST_ACTIVE_OBJECT


#include "activeobject.h"

namespace Testing
{

class TestActiveObject : public ActiveObject
{
public:

    TestActiveObject(
        const char* name,
        const size_t stackSize = 0,
        const int priority = -1);

    //! destructor
    virtual ~TestActiveObject();

    virtual void initThread();
    virtual void run();
    virtual void flushThread();

    void resumeThread();


    void clearFlags();

    bool isInitThreadCalled() const;
    bool isRunCalled() const;
    bool isFlushThreadCalled() const;

private:

    mutable boost::mutex m_localMutex;
    bool _initThreadCalled;
    bool _runCalled;
    bool _flushThreadCalled;

};

}

#endif //TEST_ACTIVE_OBJECT
