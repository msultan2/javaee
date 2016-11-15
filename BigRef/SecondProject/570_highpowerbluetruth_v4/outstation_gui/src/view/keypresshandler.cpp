#include "stdafx.h"
#include "keypresshandler.h"
#include <algorithm>

namespace View
{

BEGIN_EVENT_TABLE( KeyPressEventHandler, wxEvtHandler )
    EVT_CHAR_HOOK( KeyPressEventHandler::onKeyPress )
    EVT_CHAR( KeyPressEventHandler::onKeyPress )
    EVT_WINDOW_DESTROY( KeyPressEventHandler::onDestroy )
END_EVENT_TABLE()

KeyPressEventHandler::KeyPressEventHandler(wxWindow *window, KeyPressHandler *keypressHandler) :
    wxEvtHandler(),
    m_window(window),
    m_keypressHandler(keypressHandler)
{
    // Register self on the event stack for the provided window
    if (m_window)
    {
        wxWindowList &children = m_window->GetChildren();

        for (unsigned int i = 0; i < children.size(); i++)
        {
			new KeyPressEventHandler(children[i], m_keypressHandler);
        }

        m_window->PushEventHandler(this);
    }
    else
    {
        // abort
        delete this;
    }
}


KeyPressEventHandler::~KeyPressEventHandler()
{
    if (m_window)
    {
        m_window->RemoveEventHandler(this);
        m_window = 0;
    }
    else
    {
        // do nothing
    }
    m_keypressHandler = 0;
}

void KeyPressEventHandler::onKeyPress( wxKeyEvent& ev )
{
    if (m_window && m_keypressHandler)
    {
        // Check if this is the escape key
        //if (!m_keypressHandler->onKeyPressed(event.GetKeyCode(), event.ControlDown(), event.ShiftDown(), event.AltDown()))
        if (!m_keypressHandler->onKeyPressed(ev))
        {
            // Event was not processed - allow the event to continue propagating
            ev.Skip();
        }
        else
        {
            // do nothing
        }
    }
    else
    {
        // Skip
        ev.Skip();
    }
}

void KeyPressEventHandler::onDestroy( wxWindowDestroyEvent& event )
{
    if (m_window)
    {
        if (event.GetEventObject() != m_window)
        {
            // Event was not meant for us, so don't eat it.
            event.Skip();
        }
        else
        {
            event.Skip();
            GetNextHandler()->ProcessEvent(event);
            event.Skip(FALSE);

            // Use RemoveEventHandler, safer then PopEventHandler
            m_window->RemoveEventHandler(this);
            m_window = 0;
            // Self-cleaning
            delete this;
        }
    }
    else
    {
        event.Skip();
    }
}

} //namespace
