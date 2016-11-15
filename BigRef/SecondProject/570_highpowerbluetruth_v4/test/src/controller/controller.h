/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+

    Description:
    The controller is a class to control the main actions of the program.
    It is run as a separate thread and signals are passed (events) to inform
    about actions expected to happen or about events that have just happend.
    A set of public functions is available to provide these signals.
    The class processes these events in its own thread and executes actions in 
    the Model or View classes.

    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/


#ifndef _CONTROLLER_H_
#define _CONTROLLER_H_


namespace Controller
{

class Controller
{
public:

    //! destructor
    virtual ~Controller();

    static bool construct();
    static void destruct();

    static bool isValid();

private:

    static Controller* getInstancePtr();

    //! default constructor
    Controller();

    //! copy constructor. Not implemented
    Controller(const Controller& rhs);
    //! assignment operator. Not implemented
    Controller& operator=(const Controller& rhs);


    //Private members:

    static Controller* m_instancePtr;
    static bool m_valid;

};

}

#endif //_CONTROLLER_H_
