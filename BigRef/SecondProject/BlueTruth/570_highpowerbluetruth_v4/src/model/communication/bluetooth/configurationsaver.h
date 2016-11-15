/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    04/02/2010  RG      001         V1.00 First Issue
  
*/

#ifndef CONFIGURATION_SAVER_H_
#define CONFIGURATION_SAVER_H_

#include "iobserver.h"
#include "types.h"

#include <boost/shared_ptr.hpp>


namespace Model
{
    class ICoreConfiguration;
}

namespace BlueTooth
{

class ConfigurationSaver : public ::IObserver
{

public:
    explicit ConfigurationSaver(Model::ICoreConfiguration& coreConfiguration);

    virtual ~ConfigurationSaver();

    virtual void notifyOfStateChange(IObservable* observablePtr, const int index);

private:

    //! default constructor. Not implemented
    ConfigurationSaver();
    //! copy constructor. Not implemented
    ConfigurationSaver(const ConfigurationSaver& );
    //! assignment operator. Not implemented
    ConfigurationSaver& operator=(const ConfigurationSaver& );

    //Private members
    Model::ICoreConfiguration& m_coreConfiguration;
    uint64_t m_lastSavedAddress;
};

}

#endif //CONFIGURATION_SAVER_H_
