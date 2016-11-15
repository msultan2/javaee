/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is a generic class used by implementation of
                 TCP server and client based on boost:asio library
*/

#ifndef _BOOST_ASIO_H_
#define _BOOST_ASIO_H_

#include "activeobject.h"
#include "atomicvariable.h"

#include <boost/asio.hpp>
#include <boost/thread.hpp>


namespace Model
{

/**
 * @brief A helper class to provide logging functionality when errors are received from boost::asio modules.
 */
class BoostAsio
{
public:

    /**
     * Log error that originated from boost::asio module
     */
    static void logError(
        const char* identifier,
        const char* additionalText,
        const boost::system::error_code& errorCode);

private:
    //! default destructor. Not implemented
    virtual ~BoostAsio() = delete;
    //! default constructor. Not implemented
    BoostAsio() = delete;
    //! copy constructor. Not implemented
    BoostAsio(const BoostAsio& rhs) = delete;
    //! copy assignment operator. Not implemented
    BoostAsio& operator=(const BoostAsio& rhs) = delete;
};

}

#endif //_BOOST_ASIO_H_
