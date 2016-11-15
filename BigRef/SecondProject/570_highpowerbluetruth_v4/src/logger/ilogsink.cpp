#include "stdafx.h"
#include "ilogsink.h"


ILogSink::~ILogSink()
{
    //do nothing - abstract class
}

ILogSink::ILogSink()
{
    //do nothing - abstract class
}

ILogSink::ILogSink(const ILogSink&)
{
    //do nothing - abstract class
}

ILogSink& ILogSink::operator=(const ILogSink& rhs)
{
    if (this != &rhs)
    {
     //do nothing - abstract class
    }
    else
    {
     //do nothing
    }

    return *this;
}
