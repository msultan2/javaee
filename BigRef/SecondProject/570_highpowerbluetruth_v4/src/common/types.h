#ifndef _AAV_TYPES_H
#define _AAV_TYPES_H

#include "unicode_types.h"

#include <limits>
#include <climits>
#include <new>

#ifdef _WIN32
#include <windows.h>
#endif

#define __STDC_LIMIT_MACROS


#if defined (__linux__) || defined (__FreeBSD__)
#   include <stdint.h>

    typedef uint64_t uint64_t;
    typedef uint8_t BYTE;
    typedef uint16_t WORD;
    typedef uint32_t DWORD;
    typedef uint32_t LONG;

    typedef unsigned int UINT;
    typedef DWORD* DWORD_PTR;
#else

#ifndef uint8_t
    typedef unsigned char uint8_t;
#endif

#ifndef uint16_t
    typedef unsigned short uint16_t;
#endif

#ifndef uint32_t
    typedef unsigned int uint32_t;
#endif

#ifndef uint64_t
#   if defined(_MSC_VER) || defined(__BORLANDC__)
        typedef unsigned __int64 uint64_t;
#   else
        typedef long long unsigned int uint64_t;
#   endif
#endif

#ifndef int8_t
    typedef signed char int8_t;
#endif

#ifndef int16_t
    typedef signed short int16_t;
#endif

#ifndef int32_t
    typedef signed int int32_t;
#endif

#ifndef int64_t
#   if defined(_MSC_VER) || defined(__BORLANDC__)
        typedef __int64 int64_t;
#   else
        typedef long long int64_t;
#   endif
#endif


#endif //defined (__linux__)


// one-byte constants
extern const uint8_t UINT8_MAX_VALUE;
extern const uint8_t UINT8_MIN_VALUE;
extern const int8_t  INT8_MAX_VALUE;
extern const int8_t  INT8_MIN_VALUE;

extern const uint8_t UINT8_MASK;
extern const uint8_t UINT8_MASK_SIGN_BIT;

// two-byte constants
extern const uint16_t UINT16_MAX_VALUE;
extern const uint16_t UINT16_MIN_VALUE;
extern const int16_t  INT16_MAX_VALUE;
extern const int16_t  INT16_MIN_VALUE;

extern const uint16_t UINT16_MASK;
extern const uint16_t UINT16_MASK_BYTE_0;
extern const uint16_t UINT16_MASK_BYTE_1;
extern const uint16_t UINT16_MASK_SIGN_BIT;

// four-byte constants
extern const uint32_t UINT32_MAX_VALUE;
extern const uint32_t UINT32_MIN_VALUE;
extern const int32_t  INT32_MAX_VALUE;
extern const int32_t  INT32_MIN_VALUE;

extern const uint32_t UINT32_MASK;
extern const uint32_t UINT32_MASK_BYTE_0;
extern const uint32_t UINT32_MASK_BYTE_1;
extern const uint32_t UINT32_MASK_BYTE_2;
extern const uint32_t UINT32_MASK_BYTE_3;
extern const uint32_t UINT32_MASK_SIGN_BIT;

// eight-byte constants
extern const uint64_t UINT64_MAX_VALUE;
extern const uint64_t UINT64_MIN_VALUE;

extern const uint64_t UINT64_MASK;
extern const uint64_t UINT64_MASK_BYTE_0;
extern const uint64_t UINT64_MASK_BYTE_1;
extern const uint64_t UINT64_MASK_BYTE_2;
extern const uint64_t UINT64_MASK_BYTE_3;
extern const uint64_t UINT64_MASK_BYTE_4;
extern const uint64_t UINT64_MASK_BYTE_5;
extern const uint64_t UINT64_MASK_BYTE_6;
extern const uint64_t UINT64_MASK_BYTE_7;
extern const uint64_t UINT64_MASK_SIGN_BIT;


namespace
{
    const uint64_t MILLISECONDS_PER_SECOND = 1000;
    const uint64_t MICROSECONDS_PER_MILLISECOND = 1000;

    const uint64_t MICROSECONDS_PER_SECOND = MICROSECONDS_PER_MILLISECOND * MILLISECONDS_PER_SECOND;


    const unsigned int NUMBER_OF_BITS_PER_BYTE = 8U;
    const unsigned int SAMPLE_SIZE_IN_BYTES = 2;

} //namespace

extern const tchar NEW_LINE[];
extern const tchar EMPTY_STRING[];
extern const tchar DIRECTORY_SEPARATOR[];

extern const char SCANF_UINT64_MODIFIER[];
extern const char SCANF_INT64_MODIFIER[];
extern const tchar T_SCANF_UINT64_MODIFIER[];
extern const tchar T_SCANF_INT64_MODIFIER[];


#define LENGTH_OF_NONE_HASH_IN_BYTES (6)
#define LENGTH_OF_RAND1_HASH_IN_BYTES (8)
#define LENGTH_OF_SHA256_HASH_IN_BYTES (32)

/**
 * Define LENGTH_OF_HASH_IN_BYTES as maximum of all HASHes
 */
//#define MAX(a,b) (((a)>(b))?(a):(b))
//#define LENGTH_OF_HASH_IN_BYTES max(LENGTH_OF_RAND1_HASH_IN_BYTES, LENGTH_OF_SHA256_HASH_IN_BYTES)
//#undef max
#define LENGTH_OF_HASH_IN_BYTES LENGTH_OF_SHA256_HASH_IN_BYTES

enum
{
    eHASHING_FUNCTION_NONE = 0,
    eHASHING_FUNCTION_RAND1 = 1,
    eHASHING_FUNCTION_SHA256 = 2
};



#include <sys/timeb.h>
#ifndef _WIN32
    typedef timeb _timeb;
#endif


//----------------------------------------------------------------------------
//Time definitions
#include <boost/date_time/local_time/local_time.hpp>
namespace pt = boost::posix_time;
typedef pt::ptime TTime_t;
typedef pt::time_duration TTimeDiff_t;

extern const TTime_t ZERO_TIME_UTC;

#include <boost/chrono.hpp>
namespace bc = boost::chrono;
typedef bc::steady_clock TSteadyClock;
typedef bc::steady_clock::time_point TSteadyTimePoint;
typedef bc::steady_clock::duration TSteadyTimeDuration;

extern const TSteadyTimePoint ZERO_TIME_STEADY;

//----------------------------------------------------------------------------


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


#ifdef UNUSED
#undef UNUSED
#endif

#ifdef __linux__
#define UNUSED(x) x __attribute__ ((unused))
#else
#define UNUSED(x) (void)(x)
#endif

#endif //_AAV_TYPES_H
