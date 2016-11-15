#include "stdafx.h"
#include "types.h"
#include "unicode_types.h"


const uint8_t UINT8_MAX_VALUE   = 255u;
const uint8_t UINT8_MIN_VALUE   = 0u;
const int8_t  INT8_MAX_VALUE    = 127;
const int8_t  INT8_MIN_VALUE    = -128;

const uint8_t UINT8_MASK          = 0xffu;
const uint8_t UINT8_MASK_SIGN_BIT = 0x80u;

const uint16_t UINT16_MAX_VALUE = 65535u;
const uint16_t UINT16_MIN_VALUE = 0u;
const int16_t  INT16_MAX_VALUE  = 32767;
const int16_t  INT16_MIN_VALUE  = -32768;

const uint16_t UINT16_MASK          = 0xffffu;
const uint16_t UINT16_MASK_BYTE_0   = 0x00ffu;
const uint16_t UINT16_MASK_BYTE_1   = 0xff00u;
const uint16_t UINT16_MASK_SIGN_BIT = 0x8000u;

const uint32_t UINT32_MAX_VALUE = 4294967295u;
const uint32_t UINT32_MIN_VALUE = 0u;
const int32_t  INT32_MAX_VALUE  = 2147483647;
const int32_t  INT32_MIN_VALUE  = -2147483647-1; // Necessity

const uint32_t UINT32_MASK          = 0xffffffffu;
const uint32_t UINT32_MASK_BYTE_0   = 0x000000ffu;
const uint32_t UINT32_MASK_BYTE_1   = 0x0000ff00u;
const uint32_t UINT32_MASK_BYTE_2   = 0x00ff0000u;
const uint32_t UINT32_MASK_BYTE_3   = 0xff000000u;
const uint32_t UINT32_MASK_SIGN_BIT = 0x80000000u;

const uint64_t UINT64_MIN_VALUE     = 0x0000000000000000ull;
const uint64_t UINT64_MAX_VALUE     = 0xffffffffffffffffull;

const uint64_t UINT64_MASK          = 0xffffffffffffffffull;
const uint64_t UINT64_MASK_BYTE_0   = 0x00000000000000ffull;
const uint64_t UINT64_MASK_BYTE_1   = 0x000000000000ff00ull;
const uint64_t UINT64_MASK_BYTE_2   = 0x0000000000ff0000ull;
const uint64_t UINT64_MASK_BYTE_3   = 0x00000000ff000000ull;
const uint64_t UINT64_MASK_BYTE_4   = 0x000000ff00000000ull;
const uint64_t UINT64_MASK_BYTE_5   = 0x0000ff0000000000ull;
const uint64_t UINT64_MASK_BYTE_6   = 0x00ff000000000000ull;
const uint64_t UINT64_MASK_BYTE_7   = 0xff00000000000000ull;
const uint64_t UINT64_MASK_SIGN_BIT = 0x8000000000000000ull;


const tchar NEW_LINE[] = _T("\n");
#ifdef _WIN32
    const tchar DIRECTORY_SEPARATOR[] = _T("\\");
#else
    const tchar DIRECTORY_SEPARATOR[] = _T("/");
#endif
    const tchar EMPTY_STRING[] = _T("");
    const tchar SINGLE_SPACE[] =  _T(" ");

#ifdef _WIN32
    const char SCANF_UINT64_MODIFIER[] = "%I64u";
    const char SCANF_INT64_MODIFIER[] = "%I64";
    const tchar T_SCANF_UINT64_MODIFIER[] = _T("%I64u");
    const tchar T_SCANF_INT64_MODIFIER[] = _T("%I64");
#else
    const char SCANF_UINT64_MODIFIER[] = "%llu";
    const char SCANF_INT64_MODIFIER[] = "%lld";
    const tchar T_SCANF_UINT64_MODIFIER[] = _T("%llu");
    const tchar T_SCANF_INT64_MODIFIER[] = _T("%lld");
#endif

const TTime_t ZERO_TIME_UTC(pt::time_from_string("1970-01-01 00:00:00.000"));
const TSteadyTimePoint ZERO_TIME_STEADY;

