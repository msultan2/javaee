/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
 */

#ifndef _VIEW_H_
#define _VIEW_H_


#include "iobserver.h"

#include "controller.h"
#include "eventhandler.h"
#include "logger.h"
#include "types.h"

#include <boost/shared_ptr.hpp>


namespace Model
{
    class BrdfXmlConfiguration;
}


namespace View
{

struct LogRecord
{
    std::tstring time;
    ESeverityLevel logLevel;
    std::tstring text;
};


class View: public ::IObserver
{
public:

     //! destructor
    virtual ~View();

    virtual void notifyOfStateChange(::IObservable* observablePtr);
    virtual void notifyOfStateChange(::IObservable* observablePtr, const int index);

    static bool construct(Model::BrdfXmlConfiguration& brdfConfiguration);
    static void destruct();

    static bool isValid();

    static void log(boost::shared_ptr<LogRecord> plogRecord);

    static View* getInstancePtr();

private:

    //! Main constructor
    View(Model::BrdfXmlConfiguration& configurationParameters);

    View();
    View(const View& rhs);
    View& operator=(const View& rhs);


    //Private members:
    static View* m_instancePtr;
    static bool m_valid;

    Model::BrdfXmlConfiguration& m_brdfXmlConfiguration;
};

}

#endif //_VIEW_H_
