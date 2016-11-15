/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
 */


#ifndef _KEY_PRESS_HANDLER_H_
#define _KEY_PRESS_HANDLER_H_

#include <wx/window.h>

namespace View
{

class KeyPressHandler
{
public:
    KeyPressHandler() {}
    virtual ~KeyPressHandler() {}

    virtual bool onKeyPressed(const wxKeyEvent& ev) = 0;

private:
    //! copy constructor. Not implemented
    KeyPressHandler(const KeyPressHandler &rhs);
    //! copy assignment operator. Not implemented
    KeyPressHandler& operator=(const KeyPressHandler &rhs);
};

class KeyPressEventHandler : public wxEvtHandler
{
	DECLARE_EVENT_TABLE()
public:
	KeyPressEventHandler(wxWindow *window, KeyPressHandler *keypressHandler);
    virtual ~KeyPressEventHandler();
private:
    //! copy constructor. Not implemented
    KeyPressEventHandler(const KeyPressEventHandler &rhs);
    //! copy assignment operator. Not implemented
    KeyPressEventHandler& operator=(const KeyPressEventHandler &rhs);

	void onKeyPress( wxKeyEvent& event );
    void onDestroy( wxWindowDestroyEvent& event );

    wxWindow *m_window;
    KeyPressHandler *m_keypressHandler;
};

}//namespace

#endif //_KEY_PRESS_HANDLER_H_
