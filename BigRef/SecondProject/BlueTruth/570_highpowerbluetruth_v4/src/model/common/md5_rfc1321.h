//////////////////////////////////////////////////////////////////////////////
//
// System:      TSS Tunnel Access Controller
// Module:      rfc1321
// Author:      Dean Warren
// Compiler:    Microsoft Visual C++
// Description: The rfc1321 module provides an md5 algorithm as developed by a
//              third party (see the comments below).
//              
// Compilation Flags and Parameters:
//              Default
//
// Modification History:
//
// 2006-
//
//////////////////////////////////////////////////////////////////////////////


// ----------------------------------------
// Copyright (C) 1991-2, RSA Data Security, Inc. Created 1991. All
// rights reserved.
// 
// License to copy and use this software is granted provided that it
// is identified as the "RSA Data Security, Inc. MD5 Message-Digest
// Algorithm" in all material mentioning or referencing this software
// or this function.
// 
// License is also granted to make and use derivative works provided
// that such works are identified as "derived from the RSA Data
// Security, Inc. MD5 Message-Digest Algorithm" in all material
// mentioning or referencing the derived work.
// 
// RSA Data Security, Inc. makes no representations concerning either
// the merchantability of this software or the suitability of this
// software for any particular purpose. It is provided "as is"
// without express or implied warranty of any kind.
// 
// These notices must be retained in any copies of any part of this
// documentation and/or software.
// ----------------------------------------


#ifndef _RFC1321_H
#define _RFC1321_H

#include "types.h"


namespace Model
{

const unsigned int SIZEOF_DIGEST = 16;
typedef uint8_t DiggestType[SIZEOF_DIGEST];

void calculateMD5FromString(
    const unsigned char* data,
    const size_t len,
    DiggestType* pDiggestResult);

} //namespace

#endif // _RFC1321_H
