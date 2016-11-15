/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _I_TASK_H_
#define _I_TASK_H_


class ITask
{

public:

    //! destructor
    virtual ~ITask();

    virtual void initialise() = 0;
    virtual void perform() = 0;
    virtual void stop() = 0;
    virtual void shutdown(const char* requestorName) = 0;

protected:

    //! default constructor
    ITask();
    //! copy constructor. Not implemented
    ITask(const ITask& );
    //! assignment operator. Not implemented
    ITask& operator=(const ITask& );
};


#endif //_I_TASK_H_
