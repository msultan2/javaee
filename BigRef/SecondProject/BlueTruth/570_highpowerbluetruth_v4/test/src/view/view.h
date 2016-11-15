/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
 */

#ifndef _VIEW_H_
#define _VIEW_H_


#include "iobserver.h"

#include "controller.h"
#include "datacontainer.h"
#include "eventhandler.h"
#include "logger.h"
#include "mutex.h"
#include "types.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>


namespace Model
{
    class ICoreConfiguration;
}


namespace View
{

class OutStationConfigurationParameters;

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

    static bool construct();
    static void destruct();

    static bool isValid();

    static void log(boost::shared_ptr<LogRecord> plogRecord);

    static View* getInstancePtr();

private:

    //! default constructor
    View(Model::ICoreConfiguration& coreConfiguration);

    //! default constructor. Not implemented
    View();
    //! copy constructor. Not implemented
    View(const View& rhs);
    //! assignment operator. Not implemented
    View& operator=(const View& rhs);

    ////Private members:

    static View* m_instancePtr;
    static bool m_valid;

    Model::ICoreConfiguration& m_configurationParameters;
};

}

#endif //_VIEW_H_
