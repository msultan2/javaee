/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/


#ifndef _ERROR_DIALOG_H_
#define _ERROR_DIALOG_H_


namespace View
{

class IErrorDialog
{
public:

    //! destructor
    virtual ~IErrorDialog();

    //! display error
    virtual void displayError(const char* error) = 0;

    //! display error
    virtual void displayFatalError(const char* error) = 0;

    //! display error
    virtual void displayWarning(const char* warning) = 0;

protected:

    //! default constructor
    IErrorDialog();

    //! copy constructor
    IErrorDialog(const IErrorDialog& rhs);

    //! copy assignment operator
    IErrorDialog& operator=(const IErrorDialog&);
};

}

#endif //_ERROR_DIALOG_H_
