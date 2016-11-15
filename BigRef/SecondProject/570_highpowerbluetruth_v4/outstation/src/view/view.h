/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    07/02/2013  RG      001         V1.00 First Issue

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


namespace Model
{
    class CoreConfiguration;
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

    static bool construct(Model::CoreConfiguration& coreConfiguration);
    static void destruct();

    static bool isValid();

    static void log(boost::shared_ptr<LogRecord> plogRecord);

    static View* getInstancePtr();

private:

    //! Main constructor
    View(Model::CoreConfiguration& configurationParameters);

    View();
    View(const View& rhs);
    View& operator=(const View& rhs);


    //Private members:
    static View* m_instancePtr;
    static bool m_valid;

    Model::CoreConfiguration& m_coreConfiguration;
};

}

#endif //_VIEW_H_
