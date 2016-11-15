/*
    System: BlueTruth Outstation Project
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    04/10/2013  RG      001         V1.00 First Issue
 */

#ifndef _MODEL_H_
#define _MODEL_H_

#include "types.h"

#include "test_coreconfiguration.h"

#include <boost/shared_ptr.hpp>
#include <string>
#include <vector>


namespace Model
{
    class IniConfiguration;

class Model
{
public:

    //! default constructor
    Model();
    //! destructor
    virtual ~Model();

    static bool construct();
    static void destruct();

    static Model* getInstancePtr();
    static bool isValid();

    static void applyNewIniConfiguration(const boost::shared_ptr<IniConfiguration> pIniConfiguration);


private:

    //! copy constructor. Not implemented
    Model(const Model& );
    //! assignment operator. Not implemented
    Model& operator=(const Model& );


    void _applyNewIniConfiguration(const boost::shared_ptr<IniConfiguration> pIniConfiguration);


    //Private members:

    static Model* m_instancePtr;
    static bool m_valid;


    boost::shared_ptr<IniConfiguration> m_pIniConfiguration;
    Testing::TestCoreConfiguration m_coreConfiguration;
};

} //namespace Model

#endif //_MODEL_H_
