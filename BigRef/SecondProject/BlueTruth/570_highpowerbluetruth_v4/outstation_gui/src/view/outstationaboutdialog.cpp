#include "stdafx.h"
#include "outstationaboutdialog.h"

#include "os_utilities.h"
#include "version.h"

namespace
{
    const char MODULE_NAME[] = "OutStationAboutDialog";
}


namespace View
{

OutStationAboutDialog::OutStationAboutDialog(wxWindow* pParent)
:
AboutDialog(pParent),
KeyPressHandler()
{
    const std::tstring VERSION_STRING(OS_Utilities::StringToTString(Version::getVersionAsString()));

    const wxString FORMATTED_VERSION_STRING(
            wxString::Format(wxT("%s"), VERSION_STRING.c_str())
        );

    m_staticTextVersionStringPtr->SetLabel(FORMATTED_VERSION_STRING);

    new KeyPressEventHandler(this, this);

}

OutStationAboutDialog::~OutStationAboutDialog()
{
    //do nothing
}

void OutStationAboutDialog::onInit( wxInitDialogEvent& WXUNUSED(ev) )
{
    wxInitDialogEvent initEv;
    OnInitDialog(initEv);

    SetReturnCode(wxID_NONE);
}

void OutStationAboutDialog::onClose( wxCloseEvent& WXUNUSED(ev) )
{
    if (GetReturnCode() == wxID_NONE)
    {
        SetReturnCode(wxID_CANCEL);
    }
    //else do nothing

    Hide();
}

void OutStationAboutDialog::onOkClick( wxCommandEvent& WXUNUSED(ev) )
{
    Close();
}

bool OutStationAboutDialog::onKeyPressed(const wxKeyEvent& ev)
{
    bool retVal = false;
    const int KEY_CODE = ev.GetKeyCode();

    if (KEY_CODE == WXK_ESCAPE)
    {
        Close();

        retVal = true;
    }
    else
    {
        // do nothing
    }

    return retVal;
}

} //namespace
