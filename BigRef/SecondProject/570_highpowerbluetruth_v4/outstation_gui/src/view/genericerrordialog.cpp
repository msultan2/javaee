#include "stdafx.h"
#include "genericerrordialog.h"

#include "logger.h"
#include "os_utilities.h"
#include "xpm/error_16.xpm"
#include "xpm/warning_16.xpm"
#include "xpm/error_32.xpm"
#include "xpm/warning_32.xpm"
#include <wx/app.h>
#include <wx/dc.h>

#include <sstream>
#include <iomanip>
#include <algorithm>

namespace
{
    const int MAX_ERROR_LOGS_IN_TABLE = 50;
    const int DEFAULT_SINGLE_MESSAGE_PANEL_WIDTH_MULTIPLIER = 6;
    const int WRAPPING_SIZE = 800;
    const int DIALOG_MAX_SIZE = 800;

    const char MODULE_NAME[] = "ErrorDialogTableIconRenderer";
};

namespace View
{

const wxString ErrorDialogTableIconRenderer::WARNING_ICON_FLAG(_T("xXx_WARNING_xXx"));
const wxString ErrorDialogTableIconRenderer::ERROR_ICON_FLAG(_T("xXx_ERROR_xXx"));

ErrorDialogTableIconRenderer::ErrorDialogTableIconRenderer()
    :
wxGridCellStringRenderer(),
m_errorBitmap(error_16_xpm),
m_warningBitmap(warning_16_xpm)
{
    //do nothing
}

ErrorDialogTableIconRenderer::~ErrorDialogTableIconRenderer()
{
    // do nothing
}

void ErrorDialogTableIconRenderer::Draw(wxGrid& grid,
                  wxGridCellAttr& attr,
                  wxDC& dc,
                  const wxRect& rect,
                  int row, int col,
                  bool isSelected)
{
    if (grid.GetCellValue(row, col) == WARNING_ICON_FLAG)
    {
        wxGridCellRenderer::Draw(grid, attr, dc, rect, row, col, isSelected);
        wxRect imageRect(0, 0, m_warningBitmap.GetWidth(), m_warningBitmap.GetHeight());
        imageRect = imageRect.CentreIn(rect);
        dc.DrawBitmap(m_warningBitmap, imageRect.GetTopLeft(), true);
    }
    else if (grid.GetCellValue(row, col) == ERROR_ICON_FLAG)
    {
        wxGridCellRenderer::Draw(grid, attr, dc, rect, row, col, isSelected);
        wxRect imageRect(0, 0, m_errorBitmap.GetWidth(), m_errorBitmap.GetHeight());
        imageRect = imageRect.CentreIn(rect);
        dc.DrawBitmap(m_errorBitmap, imageRect.GetTopLeft(), true);
    }
    else
    {
        wxGridCellStringRenderer::Draw(grid, attr, dc, rect, row, col, isSelected);
    }
}

wxSize ErrorDialogTableIconRenderer::GetBestSize(wxGrid& grid,
                           wxGridCellAttr& attr,
                           wxDC& dc,
                           int row, int col)
{
    if (grid.GetCellValue(row, col) == WARNING_ICON_FLAG)
    {
        return wxSize(m_warningBitmap.GetWidth(), m_warningBitmap.GetHeight());
    }
    else if (grid.GetCellValue(row, col) == ERROR_ICON_FLAG)
    {
        return wxSize(m_errorBitmap.GetWidth(), m_errorBitmap.GetHeight());
    }
    else
    {
        return wxGridCellStringRenderer::GetBestSize(grid, attr, dc, row, col);
    }
}

wxGridCellRenderer *ErrorDialogTableIconRenderer::Clone() const
{
    return new ErrorDialogTableIconRenderer();
}


GenericErrorDialog::GenericErrorDialog(wxWindow* parent )
:
IErrorDialog(),
ErrorDialog(parent),
KeyPressHandler(),
m_showingMore(false),
m_errorBitmap(error_32_xpm),
m_warningBitmap(warning_32_xpm),
m_logMutex(),
m_warningLogsToAdd(),
m_errorLogsToAdd(),
m_biggestStringLength(0)
{
    Logger::setErrorDialog(this);

    m_gridLogMessages->SetDefaultRenderer(new ErrorDialogTableIconRenderer());

    new KeyPressEventHandler(this, this);

    m_staticTextLogMessage->Wrap(WRAPPING_SIZE);

    m_panelMultipleMessages->Enable();
    m_panelSingleMessage->Enable();

    Fit();
    Layout();
}

GenericErrorDialog::~GenericErrorDialog()
{
    Logger::setErrorDialog(0);
}

void GenericErrorDialog::onInit( wxInitDialogEvent& WXUNUSED(ev) )
{
    wxInitDialogEvent initEv;
    OnInitDialog(initEv);

    SetReturnCode(wxID_NONE);
}

void GenericErrorDialog::onClose( wxCloseEvent& WXUNUSED(ev) )
{
    if (GetReturnCode() == wxID_NONE)
    {
        SetReturnCode(wxID_CANCEL);
    }
    //else do nothing

    Hide();
}

void GenericErrorDialog::onClickOk( wxCommandEvent& WXUNUSED(event) )
{
    deleteAllRows();

    SetReturnCode(wxID_OK);
    Close();
}

void GenericErrorDialog::onClickMore( wxCommandEvent& WXUNUSED(event) )
{
    if (m_showingMore)
    {
        showSingleMessage();
    }
    else
    {
        showMultipleMessage();
    }

    m_showingMore = !m_showingMore;
}

void GenericErrorDialog::showSingleMessage()
{
    m_panelMultipleMessages->Hide();
    m_panelSingleMessage->Show();
    m_buttonMore->SetLabel(_T("More..."));

    m_staticTextLogMessage->Wrap(WRAPPING_SIZE);
    wxSize size = m_staticTextLogMessage->GetBestSize();
    m_staticTextLogMessage->SetSize(size);
    m_staticTextLogMessage->SetMinSize(size);
    m_staticTextLogMessage->SetMaxSize(size);
    size = m_panelSingleMessage->GetSize();
    m_panelSingleMessage->Fit(); //adjust the size of the single message panel to fit the message
    size = m_panelSingleMessage->GetSize();
    m_panelSingleMessage->SetMinSize(size);
    m_panelSingleMessage->SetMaxSize(size);

    SetMinSize(wxSize(360, 120));
    SetMaxSize(wxSize(1024, -1));

    Fit();
    Layout();

    Centre();

    const wxSize NEW_MIN_SIZE(GetSize());
    const wxSize NEW_MAX_SIZE(-1, GetSize().GetY());
    SetMinSize(GetSize());
    SetMaxSize(NEW_MAX_SIZE);
}

void GenericErrorDialog::showMultipleMessage()
{
    Freeze();
    Hide();

    addPendingLogsToTable();
    m_panelSingleMessage->Show(false);
    m_panelMultipleMessages->Show(true);
    m_panelMultipleMessages->Fit();
    m_buttonMore->SetLabel(_T("Less..."));

    SetMinSize(wxSize(360, 120));
    SetMaxSize(wxSize(1024, -1));

    SetSize(WRAPPING_SIZE, 400);

    Layout();

    Centre();

    const wxSize NEW_MIN_SIZE(GetSize());
    const wxSize NEW_MAX_SIZE(-1, GetSize().GetY());
    SetMinSize(GetSize());
    SetMaxSize(NEW_MAX_SIZE);

    Show();
    Thaw();
}

void GenericErrorDialog::deleteAllRows()
{
    m_gridLogMessages->DeleteRows(0, m_gridLogMessages->GetRows());
}

void GenericErrorDialog::addWarningLog( const wxString &logMessage )
{
    addLog(false, logMessage);
}

void GenericErrorDialog::addErrorLog( const wxString &logMessage )
{
    addLog(true, logMessage);
}

void GenericErrorDialog::displayError(const char* error)
{
    addErrorLog(OS_Utilities::StringToTString(error).c_str());
}

void GenericErrorDialog::displayFatalError(const char* error)
{
    addErrorLog(OS_Utilities::StringToTString(error).c_str());
}

void GenericErrorDialog::displayWarning(const char* warning)
{
    addWarningLog(OS_Utilities::StringToTString(warning).c_str());
}

void GenericErrorDialog::addLog( bool isError, const wxString &logMessage )
{
    {
        wxMutexLocker lock(m_logMutex);

        if (isError)
        {
            m_errorLogsToAdd.push_back(logMessage);
        }
        else
        {
            m_warningLogsToAdd.push_back(logMessage);
        }
    }

    wxWakeUpIdle();
}

void GenericErrorDialog::onIdle( wxIdleEvent& WXUNUSED(event) )
{
    addPendingLogsToTable();
}

void GenericErrorDialog::addPendingLogsToTable()
{
    wxMutexLocker lock(m_logMutex);

    if (m_warningLogsToAdd.empty() && m_errorLogsToAdd.empty())
    {
        // do nothing
    }
    else
    {
        m_gridLogMessages->BeginBatch();

        // Add log message logic here
        bool isFirstMessage = !IsShown();

        unsigned int numberOfLogsAdded = 0;

        std::vector<wxString>::iterator iter = m_warningLogsToAdd.begin();
        while (iter != m_warningLogsToAdd.end())
        {
            addPendingLog(isFirstMessage, false, *iter);
            isFirstMessage = false;
            iter++;
            numberOfLogsAdded++;
        }

        iter = m_errorLogsToAdd.begin();
        while (iter != m_errorLogsToAdd.end())
        {
            addPendingLog(isFirstMessage, true, *iter);
            isFirstMessage = false;
            iter++;
            numberOfLogsAdded++;
        }

        m_gridLogMessages->EndBatch();

        m_biggestStringLength = 0;
        if (!IsShown())
        {
            showSingleMessage();

            m_buttonMore->Hide();
            m_panelButtons->Fit();
            m_panelButtons->Layout();

            m_showingMore = false;

            if (numberOfLogsAdded > 1)
            {
                m_buttonMore->Show();
            }
            else
            {
                // do nothing
            }

            Show();
        }
        else
        {
            if (!m_buttonMore->IsShown())
            {
                m_buttonMore->Show();
                m_panelButtons->Fit();
                m_panelButtons->Layout();
            }
            else
            {
                // do nothing
            }

            Layout();
        }

        m_errorLogsToAdd.clear();
        m_warningLogsToAdd.clear();

        SetFocus();
    }
}

void GenericErrorDialog::addPendingLog( bool isFirst, bool isError, const wxString &logMessage )
{
    int rowNumber = m_gridLogMessages->GetRows();
    while (rowNumber >= MAX_ERROR_LOGS_IN_TABLE)
    {
        m_gridLogMessages->DeleteRows();
        rowNumber--;
    }

    m_gridLogMessages->AppendRows();

    wxString logMessageWithoutLineBreaks = logMessage;
    logMessageWithoutLineBreaks.Replace(_T("\n"), _T(" "));

    m_gridLogMessages->SetCellValue(rowNumber, 1, logMessageWithoutLineBreaks);

    m_gridLogMessages->AutoSizeRow(rowNumber);
    m_gridLogMessages->AutoSizeColumn(1);

    if (isError)
    {
        m_gridLogMessages->SetCellValue(
            rowNumber,
            0,
            ErrorDialogTableIconRenderer::ERROR_ICON_FLAG);
    }
    else
    {
        m_gridLogMessages->SetCellValue(
            rowNumber,
            0,
            ErrorDialogTableIconRenderer::WARNING_ICON_FLAG);
    }

    if (isFirst)
    {
        m_staticTextLogMessage->SetLabel(logMessage);

        if (isError)
        {
            m_bitmapPtr->SetBitmap(m_errorBitmap);
        }
        else
        {
            m_bitmapPtr->SetBitmap(m_warningBitmap);
        }
    }
}

bool GenericErrorDialog::onKeyPressed(const wxKeyEvent& ev)
{
    bool retVal = true;
    const int KEY_CODE = ev.GetKeyCode();

    switch (KEY_CODE)
    {
        case WXK_ESCAPE:
        {
            deleteAllRows();
            Close();

            break;
        }
        default:
        {
            retVal = false;
            break;
        }
    }

    return retVal;
}

} //namespace
