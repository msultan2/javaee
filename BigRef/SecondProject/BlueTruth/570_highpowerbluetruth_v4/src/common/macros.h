/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/


#ifndef _MACROS_H_
#define _MACROS_H_

#include "logger.h"

//Try and catch macro used when dynamically constructing an object
#define START_NEW_OPERATOR_TRY_CATCH_SECTION() \
    try \
    {

#define END_NEW_OPERATOR_TRY_CATCH_SECTION(ClassName, objectPtr, resultVariable) \
    } \
    catch (std::bad_alloc& badAlloc) \
    { \
        Logger::logMemoryAllocationError(MODULE_NAME, MODULE_NAME, badAlloc.what(), #ClassName); \
    \
        (resultVariable) = false; \
        (objectPtr) = 0; \
    } \
    catch(...) \
    { \
        Logger::logUnknownException(MODULE_NAME, MODULE_NAME, #ClassName); \
    \
        (resultVariable) = false; \
        (objectPtr) = 0; \
    }

#endif //_MACROS_H_
