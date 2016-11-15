#include "stdafx.h"
#include "itask.h"


ITask::~ITask()
{
    //do nothing - abstract class
}

ITask::ITask()
{
    //do nothing - abstract class
}

ITask::ITask(const ITask& )
{
    //do nothing - abstract class
}

ITask& ITask::operator=(const ITask& rhs)
{
    if (this != &rhs)
    {
        //do nothing
    }
    else
    {
        //do nothing
    }

    return *this;
}
