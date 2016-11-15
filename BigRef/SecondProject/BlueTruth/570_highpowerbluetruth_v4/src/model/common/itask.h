/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
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
    virtual void shutdown() = 0;

protected:

    //! default constructor
    ITask();
    //! copy constructor. Not implemented
    ITask(const ITask& );
    //! assignment operator. Not implemented
    ITask& operator=(const ITask& );
};


#endif //_I_TASK_H_
