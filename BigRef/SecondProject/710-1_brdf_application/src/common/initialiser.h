/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _INITIALISER_H_
#define _INITIALISER_H_

#include "unicode_types.h"

#include <vector>


class Initialiser
{
public:
    Initialiser(const std::vector<std::tstring>& directories);

    virtual ~Initialiser();

private:
    //! default constructor, not implemented
    //Initialiser();

    //! copy constructor, not implemented
    Initialiser& operator=(const Initialiser&);

    //! copy assignment operator, not implemented
    Initialiser(const Initialiser& rhs);


    bool CreateDirectoryStructure(const std::vector<std::tstring>& directories);


    unsigned int m_timesInitialised;
};

#endif // _INITIALISER_H_
