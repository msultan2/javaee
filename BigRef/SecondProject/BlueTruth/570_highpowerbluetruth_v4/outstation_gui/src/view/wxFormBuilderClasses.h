///////////////////////////////////////////////////////////////////////////
// C++ code generated with wxFormBuilder (version Sep  8 2010)
// http://www.wxformbuilder.org/
//
// PLEASE DO "NOT" EDIT THIS FILE!
///////////////////////////////////////////////////////////////////////////

#ifndef __wxFormBuilderClasses__
#define __wxFormBuilderClasses__

#include <wx/string.h>
#include <wx/bitmap.h>
#include <wx/image.h>
#include <wx/icon.h>
#include <wx/menu.h>
#include <wx/gdicmn.h>
#include <wx/font.h>
#include <wx/colour.h>
#include <wx/settings.h>
#include <wx/stattext.h>
#include <wx/sizer.h>
#include <wx/button.h>
#include <wx/statbox.h>
#include <wx/panel.h>
#include <wx/grid.h>
#include <wx/scrolwin.h>
#include <wx/notebook.h>
#include <wx/statusbr.h>
#include <wx/frame.h>
#include <wx/statbmp.h>
#include <wx/hyperlink.h>
#include <wx/statline.h>
#include <wx/dialog.h>
#include <wx/textctrl.h>
#include <wx/combobox.h>
#include <wx/spinctrl.h>
#include <wx/checkbox.h>

///////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
/// Class MainFrame
///////////////////////////////////////////////////////////////////////////////
class MainFrame : public wxFrame 
{
	DECLARE_EVENT_TABLE()
	private:
		
		// Private event handlers
		void _wxFB_onClose( wxCloseEvent& event ){ onClose( event ); }
		void _wxFB_onIdle( wxIdleEvent& event ){ onIdle( event ); }
		void _wxFB_onBrowse( wxCommandEvent& event ){ onBrowse( event ); }
		void _wxFB_onFileExit( wxCommandEvent& event ){ onFileExit( event ); }
		void _wxFB_onToolsSettings( wxCommandEvent& event ){ onToolsSettings( event ); }
		void _wxFB_onHelpAbout( wxCommandEvent& event ){ onHelpAbout( event ); }
		void _wxFB_onNotebookPageChanged( wxNotebookEvent& event ){ onNotebookPageChanged( event ); }
		void _wxFB_onButtonStartStopBlueToothDeviceDiscoveryClick( wxCommandEvent& event ){ onButtonStartStopBlueToothDeviceDiscoveryClick( event ); }
		void _wxFB_onButtonStartStopInstationClientClick( wxCommandEvent& event ){ onButtonStartStopInstationClientClick( event ); }
		void _wxFB_onEventLogLeftClick( wxGridEvent& event ){ onEventLogLeftClick( event ); }
		void _wxFB_onEventLogRightClick( wxGridEvent& event ){ onEventLogRightClick( event ); }
		
	
	protected:
		enum
		{
			ID_M_MAINFRAMEPTR = 1000,
			ID_Browse,
			ID_FileExit,
			ID_ToolsSettings,
			ID_HelpAbout,
			ID_M_MAINPANELPTR,
			ID_M_NOTEBOOK,
			ID_M_PANELSBCPTR,
			ID_M_STATICTEXTBLUETOOTHTASKSTATUSPTR,
			ID_M_STATICTEXTDEVICEVENDORLABELPTR,
			ID_M_STATICTEXTDEVICEVENDORPTR,
			ID_M_STATICTEXTPRODUCTIDLABELPTR,
			ID_M_STATICTEXTPRODUCTIDPTR,
			ID_M_STATICTEXTLOCALBLUETOOTHADAPTERIDLABELPTR,
			ID_M_STATICTEXTLOCALBLUETOOTHADAPTERIDPTR,
			ID_M_STATICTEXTLOCALBLUETOOTHADAPTERNAMELABELPTR,
			ID_M_STATICTEXTLOCALBLUETOOTHADAPTERNAMEPTR,
			ID_M_STATICTEXTLOCALBLUETOOTHADAPTERCODLABELPTR,
			ID_M_STATICTEXTLOCALBLUETOOTHADAPTERCODPTR,
			ID_M_STATICTEXTSCANTIMELABELPTR,
			ID_M_STATICTEXTSCANTIMEPTR,
			ID_M_BUTTONSTARTSTOPBLUETOOTHDEVICEDISCOVERYPTR,
			ID_M_STATICTEXTINSTATIONCLIENTGAUGEPTR,
			ID_M_STATICTEXTINSTATIONTOADDRESSPTR,
			ID_M_STATICTEXTINSTATIONTOPORTPTR,
			ID_M_STATICTEXTINSTATIONFROMADDRESSPTR,
			ID_M_BUTTON_START_INSTATION_PTR,
			ID_M_STATICTEXTQUEUEHASBEENDETECTEDPTR,
			ID_M_STATICTEXTFREEFLOWCOUNTPTR,
			ID_M_STATICTEXTMODERATEFLOWCOUNTPTR,
			ID_M_STATICTEXTSLOWFLOWCOUNTPTR,
			ID_M_STATICTEXTVERYSLOWFLOWCOUNTPTR,
			ID_M_STATICTEXTSTATICFLOWCOUNTPTR,
			ID_M_SCROLLEDWINDOWEVIDENTIALCAMERA1,
			ID_M_GRIDUSBDEVICESPTR,
			ID_M_SCROLLEDWINDOWQUEUEDETECTIONPTR,
			ID_M_GRIDQUEUEDETECTIONPTR,
			ID_M_PANELEVENTLOGPTR,
			ID_M_GRIDEVENTLOGPTR,
			ID_M_STATUSBARPTR,
		};
		
		wxMenuBar* m_menubarPtr;
		wxMenu* m_menuFilePtr;
		wxMenu* m_menuToolsPtr;
		wxMenu* m_menuHelpPtr;
		wxPanel* m_mainPanelPtr;
		wxNotebook* m_notebookPtr;
		wxPanel* m_panelGeneralPtr;
		wxStaticText* m_staticTextBlueToothDiscoveryGaugePtr;
		wxStaticText* m_staticTextDeviceVendorLabelPtr;
		wxStaticText* m_staticTextDeviceVendorPtr;
		wxStaticText* m_staticTextProductIDLabelPtr;
		wxStaticText* m_staticTextProductIDPtr;
		wxStaticText* m_staticTextLocalBlueToothAdapterIDLabelPtr;
		wxStaticText* m_staticTextLocalBlueToothAdapterIDPtr;
		wxStaticText* m_staticTextLocalBlueToothAdapterNameLabelPtr;
		wxStaticText* m_staticTextLocalBlueToothAdapterNamePtr;
		wxStaticText* m_staticTextLocalBlueToothAdapterCoDLabelPtr;
		wxStaticText* m_staticTextLocalBlueToothAdapterCoDPtr;
		wxStaticText* m_staticTextInquiryDurationInSecondsLabelPtr;
		wxStaticText* m_staticTextInquiryDurationInSecondsPtr;
		wxButton* m_buttonStartStopBlueToothDeviceDiscoveryPtr;
		wxStaticText* m_staticTextInStationClientGaugePtr;
		wxStaticText* m_staticText30;
		
		wxStaticText* m_staticText31;
		wxStaticText* m_staticTextInStationToAddressPtr;
		wxStaticText* m_staticText33;
		wxStaticText* m_staticTextInStationToPortPtr;
		wxStaticText* m_staticText35;
		
		wxStaticText* m_staticText36;
		wxStaticText* m_staticTextInStationFromAddressPtr;
		wxButton* m_buttonStartStopInstationClientPtr;
		wxStaticText* m_staticTextQueueHasBeenDetectedPtr;
		wxStaticText* m_staticText55;
		
		wxStaticText* m_staticText50;
		wxStaticText* m_staticTextFreeFlowCountPtr;
		wxStaticText* m_staticText51;
		wxStaticText* m_staticTextModerateFlowCountPtr;
		wxStaticText* m_staticText52;
		wxStaticText* m_staticTextSlowFlowCountPtr;
		wxStaticText* m_staticText53;
		wxStaticText* m_staticTextVerySlowFlowCountPtr;
		wxStaticText* m_staticText54;
		wxStaticText* m_staticTextStaticFlowCountPtr;
		wxScrolledWindow* m_scrolledWindowStatisticsPtr;
		wxGrid* m_gridRemoteBlueToothDevicesPtr;
		wxScrolledWindow* m_scrolledWindowQueueDetectionPtr;
		wxGrid* m_gridQueueDetectionPtr;
		wxPanel* m_panelEventLogPtr;
		wxGrid* m_gridEventLogPtr;
		wxStatusBar* m_statusBarPtr;
		
		// Virtual event handlers, overide them in your derived class
		virtual void onClose( wxCloseEvent& event ) { event.Skip(); }
		virtual void onIdle( wxIdleEvent& event ) { event.Skip(); }
		virtual void onBrowse( wxCommandEvent& event ) { event.Skip(); }
		virtual void onFileExit( wxCommandEvent& event ) { event.Skip(); }
		virtual void onToolsSettings( wxCommandEvent& event ) { event.Skip(); }
		virtual void onHelpAbout( wxCommandEvent& event ) { event.Skip(); }
		virtual void onNotebookPageChanged( wxNotebookEvent& event ) { event.Skip(); }
		virtual void onButtonStartStopBlueToothDeviceDiscoveryClick( wxCommandEvent& event ) { event.Skip(); }
		virtual void onButtonStartStopInstationClientClick( wxCommandEvent& event ) { event.Skip(); }
		virtual void onEventLogLeftClick( wxGridEvent& event ) { event.Skip(); }
		virtual void onEventLogRightClick( wxGridEvent& event ) { event.Skip(); }
		
	
	public:
		
		MainFrame( wxWindow* parent, wxWindowID id = ID_M_MAINFRAMEPTR, const wxString& title = wxT("BlueTruth OutStation"), const wxPoint& pos = wxDefaultPosition, const wxSize& size = wxSize( 1024,768 ), long style = wxDEFAULT_FRAME_STYLE|wxTAB_TRAVERSAL );
		~MainFrame();
	
};

///////////////////////////////////////////////////////////////////////////////
/// Class AboutDialog
///////////////////////////////////////////////////////////////////////////////
class AboutDialog : public wxDialog 
{
	DECLARE_EVENT_TABLE()
	private:
		
		// Private event handlers
		void _wxFB_onClose( wxCloseEvent& event ){ onClose( event ); }
		void _wxFB_onInit( wxInitDialogEvent& event ){ onInit( event ); }
		void _wxFB_onOkClick( wxCommandEvent& event ){ onOkClick( event ); }
		
	
	protected:
		enum
		{
			ID_ABOUTDIALOG = 1000,
			ID_M_MAINPANELPTR,
			ID_M_STATICTEXTVERSIONNUMBERPTR,
			ID_M_HYPERLINKPTR,
			ID_M_STATICLINEPTR,
			ID_M_BUTTONOKPTR,
		};
		
		wxPanel* m_mainPanelPtr;
		wxStaticText* m_staticText226;
		wxStaticText* m_staticTextVersionStringPtr;
		wxStaticBitmap* m_bitmap1;
		
		wxStaticText* m_staticText220;
		wxStaticText* m_staticText2201;
		wxStaticText* m_staticText221;
		wxStaticText* m_staticText222;
		wxHyperlinkCtrl* m_hyperlinkPtr;
		wxStaticLine* m_staticlinePtr;
		wxButton* m_buttonOkPtr;
		
		// Virtual event handlers, overide them in your derived class
		virtual void onClose( wxCloseEvent& event ) { event.Skip(); }
		virtual void onInit( wxInitDialogEvent& event ) { event.Skip(); }
		virtual void onOkClick( wxCommandEvent& event ) { event.Skip(); }
		
	
	public:
		
		AboutDialog( wxWindow* parent, wxWindowID id = ID_ABOUTDIALOG, const wxString& title = wxT("About..."), const wxPoint& pos = wxDefaultPosition, const wxSize& size = wxSize( 428,339 ), long style = wxDEFAULT_DIALOG_STYLE );
		~AboutDialog();
	
};

///////////////////////////////////////////////////////////////////////////////
/// Class ErrorDialog
///////////////////////////////////////////////////////////////////////////////
class ErrorDialog : public wxDialog 
{
	DECLARE_EVENT_TABLE()
	private:
		
		// Private event handlers
		void _wxFB_onClose( wxCloseEvent& event ){ onClose( event ); }
		void _wxFB_onIdle( wxIdleEvent& event ){ onIdle( event ); }
		void _wxFB_onInit( wxInitDialogEvent& event ){ onInit( event ); }
		void _wxFB_onClickOk( wxCommandEvent& event ){ onClickOk( event ); }
		void _wxFB_onClickMore( wxCommandEvent& event ){ onClickMore( event ); }
		
	
	protected:
		enum
		{
			ID_ERRORDIALOG = 1000,
			ID_GRID_LOG_MESSAGES,
			ID_ErrorDialogOk,
			ID_ErrorDialogMore,
		};
		
		wxPanel* m_panelSingleMessage;
		wxStaticBitmap* m_bitmapPtr;
		
		wxStaticText* m_staticTextLogMessage;
		wxPanel* m_panelMultipleMessages;
		wxGrid* m_gridLogMessages;
		wxPanel* m_panelButtons;
		wxButton* m_buttonMore;
		
		// Virtual event handlers, overide them in your derived class
		virtual void onClose( wxCloseEvent& event ) { event.Skip(); }
		virtual void onIdle( wxIdleEvent& event ) { event.Skip(); }
		virtual void onInit( wxInitDialogEvent& event ) { event.Skip(); }
		virtual void onClickOk( wxCommandEvent& event ) { event.Skip(); }
		virtual void onClickMore( wxCommandEvent& event ) { event.Skip(); }
		
	
	public:
		
		ErrorDialog( wxWindow* parent, wxWindowID id = ID_ERRORDIALOG, const wxString& title = wxT("BlueTruth OutStation"), const wxPoint& pos = wxDefaultPosition, const wxSize& size = wxSize( 360,120 ), long style = wxCAPTION|wxDIALOG_NO_PARENT|wxRESIZE_BORDER|wxSTAY_ON_TOP );
		~ErrorDialog();
	
};

///////////////////////////////////////////////////////////////////////////////
/// Class SettingsDialog
///////////////////////////////////////////////////////////////////////////////
class SettingsDialog : public wxDialog 
{
	DECLARE_EVENT_TABLE()
	private:
		
		// Private event handlers
		void _wxFB_onClose( wxCloseEvent& event ){ onClose( event ); }
		void _wxFB_onInit( wxInitDialogEvent& event ){ onInit( event ); }
		void _wxFB_onResetToDefaultsClick( wxCommandEvent& event ){ onResetToDefaultsClick( event ); }
		void _wxFB_onClickOk( wxCommandEvent& event ){ onClickOk( event ); }
		void _wxFB_onClickCancel( wxCommandEvent& event ){ onClickCancel( event ); }
		
	
	protected:
		enum
		{
			ID_SETTINGSDIALOG = 1000,
			ID_M_TEXTCTRLSITEIDENTIFIERPTR,
			ID_M_TEXTCTRLSSLSERIALNUMBERPTR,
			ID_M_TEXTCTRLCONFIGURATIONURLPTR,
			ID_M_COMBOBOXBLOOTOOTHDEVICETOBEUSEDPTR,
			ID_M_COMBOBOXBLUETOOTHDEVICEDRIVERPTR,
			ID_M_COMBOBOXVISUALLOGLEVELPTR,
			ID_M_COMBOBOXFILELOGLEVELPTR,
			ID_M_SPINCTRLMAXNUMBEROFENTRIESINEVENTLOGGRID,
			ID_M_CHECKBOXREADONSTARTPTR,
			ID_M_CHECKBOXSAVEONPROGRAMEXITPTR,
			ID_M_BUTTONRESETTODEFAULTSPTR,
			ID_M_BUTTONOK,
			ID_M_BUTTONCANCEL,
		};
		
		wxPanel* m_panel10;
		wxStaticText* m_staticText49;
		wxTextCtrl* m_textCtrlSiteIdentifierPtr;
		wxStaticText* m_staticText40;
		wxTextCtrl* m_textCtrlSSLSerialNumberPtr;
		wxStaticText* m_staticText41;
		wxTextCtrl* m_textCtrlConfigurationURLPtr;
		wxFlexGridSizer* fgSizer7;
		wxStaticText* m_staticText57;
		wxComboBox* m_comboBoxBluetoothDeviceToBeUsedPtr;
		wxStaticText* m_staticText48;
		wxComboBox* m_comboBoxBluetoothDeviceDriverPtr;
		wxStaticText* m_staticText20;
		
		wxComboBox* m_comboBoxVisualLogLevelPtr;
		wxStaticText* m_staticText21;
		
		wxComboBox* m_comboBoxFileLogLevelPtr;
		wxStaticText* m_staticText192;
		
		wxSpinCtrl* m_spinCtrlMaxNumberOfEntriesInEventLogGrid;
		wxCheckBox* m_checkBoxReadOnProgramStartPtr;
		wxCheckBox* m_checkBoxSaveOnProgramExitPtr;
		
		wxButton* m_buttonResetToDefaultsPtr;
		wxButton* m_buttonOkPtr;
		wxButton* m_buttonCancelPtr;
		
		// Virtual event handlers, overide them in your derived class
		virtual void onClose( wxCloseEvent& event ) { event.Skip(); }
		virtual void onInit( wxInitDialogEvent& event ) { event.Skip(); }
		virtual void onResetToDefaultsClick( wxCommandEvent& event ) { event.Skip(); }
		virtual void onClickOk( wxCommandEvent& event ) { event.Skip(); }
		virtual void onClickCancel( wxCommandEvent& event ) { event.Skip(); }
		
	
	public:
		
		SettingsDialog( wxWindow* parent, wxWindowID id = ID_SETTINGSDIALOG, const wxString& title = wxT("Settings"), const wxPoint& pos = wxDefaultPosition, const wxSize& size = wxSize( 348,547 ), long style = wxDEFAULT_DIALOG_STYLE|wxRESIZE_BORDER );
		~SettingsDialog();
	
};

#endif //__wxFormBuilderClasses__
