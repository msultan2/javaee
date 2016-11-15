///////////////////////////////////////////////////////////////////////////
// C++ code generated with wxFormBuilder (version Sep  8 2010)
// http://www.wxformbuilder.org/
//
// PLEASE DO "NOT" EDIT THIS FILE!
///////////////////////////////////////////////////////////////////////////

#include "wxFormBuilderClasses.h"

#include "../../../src/view/xpm/about_browser_16.xpm"
#include "../../../src/view/xpm/settings.xpm"
#include "../../../src/view/xpm/ssl.xpm"
#include "../../../src/view/xpm/system_log_out_16.xpm"

///////////////////////////////////////////////////////////////////////////

BEGIN_EVENT_TABLE( MainFrame, wxFrame )
	EVT_CLOSE( MainFrame::_wxFB_onClose )
	EVT_IDLE( MainFrame::_wxFB_onIdle )
	EVT_MENU( ID_Browse, MainFrame::_wxFB_onBrowse )
	EVT_MENU( ID_FileExit, MainFrame::_wxFB_onFileExit )
	EVT_MENU( ID_ToolsSettings, MainFrame::_wxFB_onToolsSettings )
	EVT_MENU( ID_HelpAbout, MainFrame::_wxFB_onHelpAbout )
	EVT_NOTEBOOK_PAGE_CHANGED( ID_M_NOTEBOOK, MainFrame::_wxFB_onNotebookPageChanged )
	EVT_BUTTON( ID_M_BUTTONSTARTSTOPBLUETOOTHDEVICEDISCOVERYPTR, MainFrame::_wxFB_onButtonStartStopBlueToothDeviceDiscoveryClick )
	EVT_BUTTON( ID_M_BUTTON_START_INSTATION_PTR, MainFrame::_wxFB_onButtonStartStopInstationClientClick )
	EVT_GRID_CELL_LEFT_CLICK( MainFrame::_wxFB_onEventLogLeftClick )
	EVT_GRID_CELL_RIGHT_CLICK( MainFrame::_wxFB_onEventLogRightClick )
END_EVENT_TABLE()

MainFrame::MainFrame( wxWindow* parent, wxWindowID id, const wxString& title, const wxPoint& pos, const wxSize& size, long style ) : wxFrame( parent, id, title, pos, size, style )
{
	this->SetSizeHints( wxDefaultSize, wxDefaultSize );
	
	m_menubarPtr = new wxMenuBar( 0 );
	m_menuFilePtr = new wxMenu();
	wxMenuItem* m_menuItemBrowsePtr;
	m_menuItemBrowsePtr = new wxMenuItem( m_menuFilePtr, ID_Browse, wxString( wxT("&Browse...") ) , wxT("Browse to application user data directory"), wxITEM_NORMAL );
	m_menuFilePtr->Append( m_menuItemBrowsePtr );
	
	wxMenuItem* m_separator1;
	m_separator1 = m_menuFilePtr->AppendSeparator();
	
	wxMenuItem* m_menuItemfileExitPtr;
	m_menuItemfileExitPtr = new wxMenuItem( m_menuFilePtr, ID_FileExit, wxString( wxT("E&xit") ) + wxT('\t') + wxT("Ctrl+X"), wxT("Exit the program"), wxITEM_NORMAL );
	#ifdef __WXMSW__
	m_menuItemfileExitPtr->SetBitmaps( wxBitmap( system_log_out_16_xpm ) );
	#elif defined( __WXGTK__ )
	m_menuItemfileExitPtr->SetBitmap( wxBitmap( system_log_out_16_xpm ) );
	#endif
	m_menuFilePtr->Append( m_menuItemfileExitPtr );
	
	m_menubarPtr->Append( m_menuFilePtr, wxT("&File") ); 
	
	m_menuToolsPtr = new wxMenu();
	wxMenuItem* m_menuItemToolsSettings;
	m_menuItemToolsSettings = new wxMenuItem( m_menuToolsPtr, ID_ToolsSettings, wxString( wxT("Settings") ) + wxT('\t') + wxT("Alt+F7"), wxT("Show settings dialog"), wxITEM_NORMAL );
	#ifdef __WXMSW__
	m_menuItemToolsSettings->SetBitmaps( wxBitmap( settings_xpm ) );
	#elif defined( __WXGTK__ )
	m_menuItemToolsSettings->SetBitmap( wxBitmap( settings_xpm ) );
	#endif
	m_menuToolsPtr->Append( m_menuItemToolsSettings );
	
	m_menubarPtr->Append( m_menuToolsPtr, wxT("&Tools") ); 
	
	m_menuHelpPtr = new wxMenu();
	wxMenuItem* m_menuItemHelpAboutPtr;
	m_menuItemHelpAboutPtr = new wxMenuItem( m_menuHelpPtr, ID_HelpAbout, wxString( wxT("&About...") ) + wxT('\t') + wxT("F1"), wxT("Show about dialog"), wxITEM_NORMAL );
	#ifdef __WXMSW__
	m_menuItemHelpAboutPtr->SetBitmaps( wxBitmap( about_browser_16_xpm ) );
	#elif defined( __WXGTK__ )
	m_menuItemHelpAboutPtr->SetBitmap( wxBitmap( about_browser_16_xpm ) );
	#endif
	m_menuHelpPtr->Append( m_menuItemHelpAboutPtr );
	
	m_menubarPtr->Append( m_menuHelpPtr, wxT("&Help") ); 
	
	this->SetMenuBar( m_menubarPtr );
	
	wxFlexGridSizer* fgSizer26;
	fgSizer26 = new wxFlexGridSizer( 2, 1, 0, 0 );
	fgSizer26->AddGrowableCol( 0 );
	fgSizer26->AddGrowableRow( 0 );
	fgSizer26->SetFlexibleDirection( wxVERTICAL );
	fgSizer26->SetNonFlexibleGrowMode( wxFLEX_GROWMODE_SPECIFIED );
	
	m_mainPanelPtr = new wxPanel( this, ID_M_MAINPANELPTR, wxDefaultPosition, wxSize( 800,600 ), wxTAB_TRAVERSAL );
	m_mainPanelPtr->SetMinSize( wxSize( 800,600 ) );
	
	wxBoxSizer* bSizerPanelPtr;
	bSizerPanelPtr = new wxBoxSizer( wxVERTICAL );
	
	m_notebookPtr = new wxNotebook( m_mainPanelPtr, ID_M_NOTEBOOK, wxDefaultPosition, wxDefaultSize, wxNB_TOP );
	m_panelGeneralPtr = new wxPanel( m_notebookPtr, ID_M_PANELSBCPTR, wxDefaultPosition, wxDefaultSize, wxTAB_TRAVERSAL );
	wxBoxSizer* bSizer75;
	bSizer75 = new wxBoxSizer( wxVERTICAL );
	
	wxBoxSizer* bSizer80;
	bSizer80 = new wxBoxSizer( wxHORIZONTAL );
	
	wxStaticBoxSizer* sbSizer57;
	sbSizer57 = new wxStaticBoxSizer( new wxStaticBox( m_panelGeneralPtr, wxID_ANY, wxT("BlueTooth Device Discovery:") ), wxVERTICAL );
	
	m_staticTextBlueToothDiscoveryGaugePtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTBLUETOOTHTASKSTATUSPTR, wxT("Connected / Disconnected"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextBlueToothDiscoveryGaugePtr->Wrap( -1 );
	m_staticTextBlueToothDiscoveryGaugePtr->SetFont( wxFont( wxNORMAL_FONT->GetPointSize(), 70, 90, 92, false, wxEmptyString ) );
	m_staticTextBlueToothDiscoveryGaugePtr->SetForegroundColour( wxColour( 255, 0, 0 ) );
	
	sbSizer57->Add( m_staticTextBlueToothDiscoveryGaugePtr, 0, wxALL, 5 );
	
	wxFlexGridSizer* fgSizer3;
	fgSizer3 = new wxFlexGridSizer( 6, 2, 0, 0 );
	fgSizer3->AddGrowableCol( 1 );
	fgSizer3->SetFlexibleDirection( wxBOTH );
	fgSizer3->SetNonFlexibleGrowMode( wxFLEX_GROWMODE_SPECIFIED );
	
	m_staticTextDeviceVendorLabelPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTDEVICEVENDORLABELPTR, wxT("Vendor ID:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextDeviceVendorLabelPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextDeviceVendorLabelPtr, 0, wxALL, 5 );
	
	m_staticTextDeviceVendorPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTDEVICEVENDORPTR, wxT("-"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextDeviceVendorPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextDeviceVendorPtr, 0, wxALL, 5 );
	
	m_staticTextProductIDLabelPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTPRODUCTIDLABELPTR, wxT("Product ID:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextProductIDLabelPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextProductIDLabelPtr, 0, wxALL, 5 );
	
	m_staticTextProductIDPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTPRODUCTIDPTR, wxT("-"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextProductIDPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextProductIDPtr, 0, wxALL, 5 );
	
	m_staticTextLocalBlueToothAdapterIDLabelPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTLOCALBLUETOOTHADAPTERIDLABELPTR, wxT("Local Address:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextLocalBlueToothAdapterIDLabelPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextLocalBlueToothAdapterIDLabelPtr, 0, wxALL, 5 );
	
	m_staticTextLocalBlueToothAdapterIDPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTLOCALBLUETOOTHADAPTERIDPTR, wxT("XX:XX:XX:XX:XX:XX"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextLocalBlueToothAdapterIDPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextLocalBlueToothAdapterIDPtr, 0, wxALL, 5 );
	
	m_staticTextLocalBlueToothAdapterNameLabelPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTLOCALBLUETOOTHADAPTERNAMELABELPTR, wxT("Local Name:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextLocalBlueToothAdapterNameLabelPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextLocalBlueToothAdapterNameLabelPtr, 0, wxALL, 5 );
	
	m_staticTextLocalBlueToothAdapterNamePtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTLOCALBLUETOOTHADAPTERNAMEPTR, wxT("-"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextLocalBlueToothAdapterNamePtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextLocalBlueToothAdapterNamePtr, 0, wxALL, 5 );
	
	m_staticTextLocalBlueToothAdapterCoDLabelPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTLOCALBLUETOOTHADAPTERCODLABELPTR, wxT("CoD:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextLocalBlueToothAdapterCoDLabelPtr->Wrap( -1 );
	m_staticTextLocalBlueToothAdapterCoDLabelPtr->SetToolTip( wxT("Class of Device") );
	
	fgSizer3->Add( m_staticTextLocalBlueToothAdapterCoDLabelPtr, 0, wxALL, 5 );
	
	m_staticTextLocalBlueToothAdapterCoDPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTLOCALBLUETOOTHADAPTERCODPTR, wxT("-"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextLocalBlueToothAdapterCoDPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextLocalBlueToothAdapterCoDPtr, 0, wxALL, 5 );
	
	m_staticTextInquiryDurationInSecondsLabelPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTSCANTIMELABELPTR, wxT("Inquiry Duration:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextInquiryDurationInSecondsLabelPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextInquiryDurationInSecondsLabelPtr, 0, wxALL, 5 );
	
	m_staticTextInquiryDurationInSecondsPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTSCANTIMEPTR, wxT("-"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextInquiryDurationInSecondsPtr->Wrap( -1 );
	fgSizer3->Add( m_staticTextInquiryDurationInSecondsPtr, 0, wxALL, 5 );
	
	sbSizer57->Add( fgSizer3, 1, wxEXPAND, 5 );
	
	wxBoxSizer* bSizer26;
	bSizer26 = new wxBoxSizer( wxVERTICAL );
	
	m_buttonStartStopBlueToothDeviceDiscoveryPtr = new wxButton( m_panelGeneralPtr, ID_M_BUTTONSTARTSTOPBLUETOOTHDEVICEDISCOVERYPTR, wxT("Start"), wxDefaultPosition, wxDefaultSize, 0 );
	m_buttonStartStopBlueToothDeviceDiscoveryPtr->SetToolTip( wxT("Start / Stop BlueTooth Device Discovery") );
	
	bSizer26->Add( m_buttonStartStopBlueToothDeviceDiscoveryPtr, 0, wxALL|wxEXPAND, 5 );
	
	sbSizer57->Add( bSizer26, 0, wxEXPAND, 5 );
	
	bSizer80->Add( sbSizer57, 0, wxALL|wxEXPAND, 5 );
	
	wxStaticBoxSizer* sbSizer61;
	sbSizer61 = new wxStaticBoxSizer( new wxStaticBox( m_panelGeneralPtr, wxID_ANY, wxT("InStation Connection Status") ), wxVERTICAL );
	
	m_staticTextInStationClientGaugePtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTINSTATIONCLIENTGAUGEPTR, wxT("Client Connected/Disconnected"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextInStationClientGaugePtr->Wrap( -1 );
	m_staticTextInStationClientGaugePtr->SetFont( wxFont( wxNORMAL_FONT->GetPointSize(), 70, 90, 92, false, wxEmptyString ) );
	m_staticTextInStationClientGaugePtr->SetForegroundColour( wxColour( 255, 0, 0 ) );
	
	sbSizer61->Add( m_staticTextInStationClientGaugePtr, 0, wxALL, 5 );
	
	wxFlexGridSizer* fgSizer4;
	fgSizer4 = new wxFlexGridSizer( 5, 2, 0, 0 );
	fgSizer4->AddGrowableCol( 1 );
	fgSizer4->SetFlexibleDirection( wxBOTH );
	fgSizer4->SetNonFlexibleGrowMode( wxFLEX_GROWMODE_SPECIFIED );
	
	m_staticText30 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Connected to:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText30->Wrap( -1 );
	m_staticText30->SetFont( wxFont( wxNORMAL_FONT->GetPointSize(), 70, 90, 92, false, wxEmptyString ) );
	
	fgSizer4->Add( m_staticText30, 0, wxALL, 5 );
	
	
	fgSizer4->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_staticText31 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Address:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText31->Wrap( -1 );
	fgSizer4->Add( m_staticText31, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextInStationToAddressPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTINSTATIONTOADDRESSPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextInStationToAddressPtr->Wrap( -1 );
	fgSizer4->Add( m_staticTextInStationToAddressPtr, 0, wxALL, 5 );
	
	m_staticText33 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Port:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText33->Wrap( -1 );
	fgSizer4->Add( m_staticText33, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextInStationToPortPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTINSTATIONTOPORTPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextInStationToPortPtr->Wrap( -1 );
	fgSizer4->Add( m_staticTextInStationToPortPtr, 0, wxALL, 5 );
	
	m_staticText35 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("From:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText35->Wrap( -1 );
	m_staticText35->SetFont( wxFont( wxNORMAL_FONT->GetPointSize(), 70, 90, 92, false, wxEmptyString ) );
	
	fgSizer4->Add( m_staticText35, 0, wxALL, 5 );
	
	
	fgSizer4->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_staticText36 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Address:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText36->Wrap( -1 );
	fgSizer4->Add( m_staticText36, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextInStationFromAddressPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTINSTATIONFROMADDRESSPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextInStationFromAddressPtr->Wrap( -1 );
	fgSizer4->Add( m_staticTextInStationFromAddressPtr, 0, wxALL, 5 );
	
	sbSizer61->Add( fgSizer4, 1, wxEXPAND, 5 );
	
	wxBoxSizer* bSizer751;
	bSizer751 = new wxBoxSizer( wxVERTICAL );
	
	m_buttonStartStopInstationClientPtr = new wxButton( m_panelGeneralPtr, ID_M_BUTTON_START_INSTATION_PTR, wxT("Start"), wxDefaultPosition, wxDefaultSize, 0 );
	m_buttonStartStopInstationClientPtr->SetToolTip( wxT("Start / Stop Primary InStation") );
	m_buttonStartStopInstationClientPtr->SetMinSize( wxSize( 200,-1 ) );
	
	bSizer751->Add( m_buttonStartStopInstationClientPtr, 0, wxALL|wxEXPAND, 5 );
	
	sbSizer61->Add( bSizer751, 0, wxEXPAND, 5 );
	
	bSizer80->Add( sbSizer61, 0, wxALL, 5 );
	
	bSizer75->Add( bSizer80, 0, 0, 5 );
	
	wxBoxSizer* bSizer271;
	bSizer271 = new wxBoxSizer( wxHORIZONTAL );
	
	wxStaticBoxSizer* sbSizer12;
	sbSizer12 = new wxStaticBoxSizer( new wxStaticBox( m_panelGeneralPtr, wxID_ANY, wxT("Queue Detection") ), wxVERTICAL );
	
	m_staticTextQueueHasBeenDetectedPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTQUEUEHASBEENDETECTEDPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextQueueHasBeenDetectedPtr->Wrap( -1 );
	m_staticTextQueueHasBeenDetectedPtr->SetFont( wxFont( wxNORMAL_FONT->GetPointSize(), 70, 90, 92, false, wxEmptyString ) );
	
	sbSizer12->Add( m_staticTextQueueHasBeenDetectedPtr, 0, wxALIGN_CENTER_HORIZONTAL|wxALL, 5 );
	
	wxFlexGridSizer* fgSizer8;
	fgSizer8 = new wxFlexGridSizer( 6, 2, 0, 0 );
	fgSizer8->SetFlexibleDirection( wxBOTH );
	fgSizer8->SetNonFlexibleGrowMode( wxFLEX_GROWMODE_SPECIFIED );
	
	m_staticText55 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Device Count"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText55->Wrap( -1 );
	fgSizer8->Add( m_staticText55, 0, wxALL, 5 );
	
	
	fgSizer8->Add( 100, 0, 1, wxEXPAND, 5 );
	
	m_staticText50 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Free Flow Bin:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText50->Wrap( -1 );
	fgSizer8->Add( m_staticText50, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextFreeFlowCountPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTFREEFLOWCOUNTPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextFreeFlowCountPtr->Wrap( -1 );
	fgSizer8->Add( m_staticTextFreeFlowCountPtr, 0, wxALL, 5 );
	
	m_staticText51 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Moderate Flow Bin:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText51->Wrap( -1 );
	fgSizer8->Add( m_staticText51, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextModerateFlowCountPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTMODERATEFLOWCOUNTPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextModerateFlowCountPtr->Wrap( -1 );
	fgSizer8->Add( m_staticTextModerateFlowCountPtr, 0, wxALL, 5 );
	
	m_staticText52 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Slow Flow Count:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText52->Wrap( -1 );
	fgSizer8->Add( m_staticText52, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextSlowFlowCountPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTSLOWFLOWCOUNTPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextSlowFlowCountPtr->Wrap( -1 );
	fgSizer8->Add( m_staticTextSlowFlowCountPtr, 0, wxALL, 5 );
	
	m_staticText53 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Very Slow Flow Bin:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText53->Wrap( -1 );
	fgSizer8->Add( m_staticText53, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextVerySlowFlowCountPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTVERYSLOWFLOWCOUNTPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextVerySlowFlowCountPtr->Wrap( -1 );
	fgSizer8->Add( m_staticTextVerySlowFlowCountPtr, 0, wxALL, 5 );
	
	m_staticText54 = new wxStaticText( m_panelGeneralPtr, wxID_ANY, wxT("Static Flow Bin:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText54->Wrap( -1 );
	fgSizer8->Add( m_staticText54, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	m_staticTextStaticFlowCountPtr = new wxStaticText( m_panelGeneralPtr, ID_M_STATICTEXTSTATICFLOWCOUNTPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextStaticFlowCountPtr->Wrap( -1 );
	fgSizer8->Add( m_staticTextStaticFlowCountPtr, 0, wxALL, 5 );
	
	sbSizer12->Add( fgSizer8, 1, wxEXPAND, 5 );
	
	bSizer271->Add( sbSizer12, 0, wxALL, 5 );
	
	bSizer75->Add( bSizer271, 1, wxEXPAND, 5 );
	
	m_panelGeneralPtr->SetSizer( bSizer75 );
	m_panelGeneralPtr->Layout();
	bSizer75->Fit( m_panelGeneralPtr );
	m_notebookPtr->AddPage( m_panelGeneralPtr, wxT("General"), true );
	m_scrolledWindowStatisticsPtr = new wxScrolledWindow( m_notebookPtr, ID_M_SCROLLEDWINDOWEVIDENTIALCAMERA1, wxDefaultPosition, wxDefaultSize, wxHSCROLL|wxTAB_TRAVERSAL|wxVSCROLL );
	m_scrolledWindowStatisticsPtr->SetScrollRate( 5, 5 );
	wxBoxSizer* bSizer25111;
	bSizer25111 = new wxBoxSizer( wxVERTICAL );
	
	m_gridRemoteBlueToothDevicesPtr = new wxGrid( m_scrolledWindowStatisticsPtr, ID_M_GRIDUSBDEVICESPTR, wxDefaultPosition, wxDefaultSize, 0 );
	
	// Grid
	m_gridRemoteBlueToothDevicesPtr->CreateGrid( 1, 4 );
	m_gridRemoteBlueToothDevicesPtr->EnableEditing( false );
	m_gridRemoteBlueToothDevicesPtr->EnableGridLines( true );
	m_gridRemoteBlueToothDevicesPtr->EnableDragGridSize( false );
	m_gridRemoteBlueToothDevicesPtr->SetMargins( 0, 0 );
	
	// Columns
	m_gridRemoteBlueToothDevicesPtr->SetColSize( 0, 80 );
	m_gridRemoteBlueToothDevicesPtr->SetColSize( 1, 140 );
	m_gridRemoteBlueToothDevicesPtr->SetColSize( 2, 80 );
	m_gridRemoteBlueToothDevicesPtr->SetColSize( 3, 500 );
	m_gridRemoteBlueToothDevicesPtr->EnableDragColMove( false );
	m_gridRemoteBlueToothDevicesPtr->EnableDragColSize( true );
	m_gridRemoteBlueToothDevicesPtr->SetColLabelSize( 30 );
	m_gridRemoteBlueToothDevicesPtr->SetColLabelValue( 0, wxT("BlueTooth ID") );
	m_gridRemoteBlueToothDevicesPtr->SetColLabelValue( 1, wxT("Name") );
	m_gridRemoteBlueToothDevicesPtr->SetColLabelValue( 2, wxT("CoD") );
	m_gridRemoteBlueToothDevicesPtr->SetColLabelValue( 3, wxT("Major Service Class, Major Device Class (Minor Device Class)") );
	m_gridRemoteBlueToothDevicesPtr->SetColLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Rows
	m_gridRemoteBlueToothDevicesPtr->AutoSizeRows();
	m_gridRemoteBlueToothDevicesPtr->EnableDragRowSize( true );
	m_gridRemoteBlueToothDevicesPtr->SetRowLabelSize( 50 );
	m_gridRemoteBlueToothDevicesPtr->SetRowLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Label Appearance
	
	// Cell Defaults
	m_gridRemoteBlueToothDevicesPtr->SetDefaultCellAlignment( wxALIGN_LEFT, wxALIGN_TOP );
	bSizer25111->Add( m_gridRemoteBlueToothDevicesPtr, 1, wxALL|wxEXPAND, 5 );
	
	m_scrolledWindowStatisticsPtr->SetSizer( bSizer25111 );
	m_scrolledWindowStatisticsPtr->Layout();
	bSizer25111->Fit( m_scrolledWindowStatisticsPtr );
	m_notebookPtr->AddPage( m_scrolledWindowStatisticsPtr, wxT("Statistics"), false );
	m_scrolledWindowQueueDetectionPtr = new wxScrolledWindow( m_notebookPtr, ID_M_SCROLLEDWINDOWQUEUEDETECTIONPTR, wxDefaultPosition, wxDefaultSize, wxHSCROLL|wxVSCROLL );
	m_scrolledWindowQueueDetectionPtr->SetScrollRate( 5, 5 );
	wxBoxSizer* bSizer27;
	bSizer27 = new wxBoxSizer( wxVERTICAL );
	
	m_gridQueueDetectionPtr = new wxGrid( m_scrolledWindowQueueDetectionPtr, ID_M_GRIDQUEUEDETECTIONPTR, wxDefaultPosition, wxDefaultSize, 0 );
	
	// Grid
	m_gridQueueDetectionPtr->CreateGrid( 1, 10 );
	m_gridQueueDetectionPtr->EnableEditing( false );
	m_gridQueueDetectionPtr->EnableGridLines( true );
	m_gridQueueDetectionPtr->EnableDragGridSize( false );
	m_gridQueueDetectionPtr->SetMargins( 0, 0 );
	
	// Columns
	m_gridQueueDetectionPtr->SetColSize( 0, 80 );
	m_gridQueueDetectionPtr->SetColSize( 1, 140 );
	m_gridQueueDetectionPtr->SetColSize( 2, 80 );
	m_gridQueueDetectionPtr->SetColSize( 3, 80 );
	m_gridQueueDetectionPtr->SetColSize( 4, 70 );
	m_gridQueueDetectionPtr->SetColSize( 5, 70 );
	m_gridQueueDetectionPtr->SetColSize( 6, 70 );
	m_gridQueueDetectionPtr->SetColSize( 7, 70 );
	m_gridQueueDetectionPtr->SetColSize( 8, 80 );
	m_gridQueueDetectionPtr->SetColSize( 9, 400 );
	m_gridQueueDetectionPtr->EnableDragColMove( false );
	m_gridQueueDetectionPtr->EnableDragColSize( true );
	m_gridQueueDetectionPtr->SetColLabelSize( 30 );
	m_gridQueueDetectionPtr->SetColLabelValue( 0, wxT("BlueTooth ID") );
	m_gridQueueDetectionPtr->SetColLabelValue( 1, wxT("Name") );
	m_gridQueueDetectionPtr->SetColLabelValue( 2, wxT("First Obs.") );
	m_gridQueueDetectionPtr->SetColLabelValue( 3, wxT("Last Obs.") );
	m_gridQueueDetectionPtr->SetColLabelValue( 4, wxT("Presence") );
	m_gridQueueDetectionPtr->SetColLabelValue( 5, wxT("Absence") );
	m_gridQueueDetectionPtr->SetColLabelValue( 6, wxT("Total Scans") );
	m_gridQueueDetectionPtr->SetColLabelValue( 7, wxT("Bin") );
	m_gridQueueDetectionPtr->SetColLabelValue( 8, wxT("CoD") );
	m_gridQueueDetectionPtr->SetColLabelValue( 9, wxT("Major Service Class, Major Device Class (Minor Device Class)") );
	m_gridQueueDetectionPtr->SetColLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Rows
	m_gridQueueDetectionPtr->EnableDragRowSize( true );
	m_gridQueueDetectionPtr->SetRowLabelSize( 80 );
	m_gridQueueDetectionPtr->SetRowLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Label Appearance
	
	// Cell Defaults
	m_gridQueueDetectionPtr->SetDefaultCellAlignment( wxALIGN_LEFT, wxALIGN_TOP );
	bSizer27->Add( m_gridQueueDetectionPtr, 1, wxALL|wxEXPAND, 5 );
	
	m_scrolledWindowQueueDetectionPtr->SetSizer( bSizer27 );
	m_scrolledWindowQueueDetectionPtr->Layout();
	bSizer27->Fit( m_scrolledWindowQueueDetectionPtr );
	m_notebookPtr->AddPage( m_scrolledWindowQueueDetectionPtr, wxT("Queue Detection"), false );
	m_panelEventLogPtr = new wxPanel( m_notebookPtr, ID_M_PANELEVENTLOGPTR, wxDefaultPosition, wxDefaultSize, wxTAB_TRAVERSAL );
	m_panelEventLogPtr->SetToolTip( wxT("Event Log") );
	
	wxBoxSizer* bSizer20;
	bSizer20 = new wxBoxSizer( wxVERTICAL );
	
	wxStaticBoxSizer* sbSizer122;
	sbSizer122 = new wxStaticBoxSizer( new wxStaticBox( m_panelEventLogPtr, wxID_ANY, wxT("Event log") ), wxVERTICAL );
	
	m_gridEventLogPtr = new wxGrid( m_panelEventLogPtr, ID_M_GRIDEVENTLOGPTR, wxDefaultPosition, wxDefaultSize, 0 );
	
	// Grid
	m_gridEventLogPtr->CreateGrid( 1, 3 );
	m_gridEventLogPtr->EnableEditing( false );
	m_gridEventLogPtr->EnableGridLines( true );
	m_gridEventLogPtr->EnableDragGridSize( false );
	m_gridEventLogPtr->SetMargins( 0, 0 );
	
	// Columns
	m_gridEventLogPtr->EnableDragColMove( false );
	m_gridEventLogPtr->EnableDragColSize( true );
	m_gridEventLogPtr->SetColLabelSize( 30 );
	m_gridEventLogPtr->SetColLabelValue( 0, wxT("Time") );
	m_gridEventLogPtr->SetColLabelValue( 1, wxT("Severity") );
	m_gridEventLogPtr->SetColLabelValue( 2, wxT("Log") );
	m_gridEventLogPtr->SetColLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Rows
	m_gridEventLogPtr->AutoSizeRows();
	m_gridEventLogPtr->EnableDragRowSize( true );
	m_gridEventLogPtr->SetRowLabelSize( 50 );
	m_gridEventLogPtr->SetRowLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Label Appearance
	
	// Cell Defaults
	m_gridEventLogPtr->SetDefaultCellAlignment( wxALIGN_LEFT, wxALIGN_TOP );
	sbSizer122->Add( m_gridEventLogPtr, 1, wxALL|wxEXPAND, 5 );
	
	bSizer20->Add( sbSizer122, 1, wxALL|wxEXPAND, 5 );
	
	m_panelEventLogPtr->SetSizer( bSizer20 );
	m_panelEventLogPtr->Layout();
	bSizer20->Fit( m_panelEventLogPtr );
	m_notebookPtr->AddPage( m_panelEventLogPtr, wxT("Event Log"), false );
	
	bSizerPanelPtr->Add( m_notebookPtr, 1, wxEXPAND | wxALL, 5 );
	
	m_mainPanelPtr->SetSizer( bSizerPanelPtr );
	m_mainPanelPtr->Layout();
	fgSizer26->Add( m_mainPanelPtr, 1, wxEXPAND, 0 );
	
	this->SetSizer( fgSizer26 );
	this->Layout();
	m_statusBarPtr = this->CreateStatusBar( 5, wxST_SIZEGRIP, ID_M_STATUSBARPTR );
}

MainFrame::~MainFrame()
{
}

BEGIN_EVENT_TABLE( AboutDialog, wxDialog )
	EVT_CLOSE( AboutDialog::_wxFB_onClose )
	EVT_INIT_DIALOG( AboutDialog::_wxFB_onInit )
	EVT_BUTTON( ID_M_BUTTONOKPTR, AboutDialog::_wxFB_onOkClick )
END_EVENT_TABLE()

AboutDialog::AboutDialog( wxWindow* parent, wxWindowID id, const wxString& title, const wxPoint& pos, const wxSize& size, long style ) : wxDialog( parent, id, title, pos, size, style )
{
	this->SetSizeHints( wxDefaultSize, wxDefaultSize );
	
	wxBoxSizer* bSizerMainPtr;
	bSizerMainPtr = new wxBoxSizer( wxVERTICAL );
	
	m_mainPanelPtr = new wxPanel( this, ID_M_MAINPANELPTR, wxDefaultPosition, wxDefaultSize, wxTAB_TRAVERSAL );
	wxBoxSizer* bSizerPanelPtr;
	bSizerPanelPtr = new wxBoxSizer( wxVERTICAL );
	
	wxBoxSizer* bSizer1;
	bSizer1 = new wxBoxSizer( wxHORIZONTAL );
	
	wxBoxSizer* bSizer11;
	bSizer11 = new wxBoxSizer( wxVERTICAL );
	
	m_staticText226 = new wxStaticText( m_mainPanelPtr, wxID_ANY, wxT("BlueTruth Outstation"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText226->Wrap( -1 );
	m_staticText226->SetFont( wxFont( 20, 70, 90, 90, false, wxEmptyString ) );
	
	bSizer11->Add( m_staticText226, 0, wxALL, 5 );
	
	m_staticTextVersionStringPtr = new wxStaticText( m_mainPanelPtr, ID_M_STATICTEXTVERSIONNUMBERPTR, wxT("Version 0.01"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticTextVersionStringPtr->Wrap( -1 );
	bSizer11->Add( m_staticTextVersionStringPtr, 0, wxALL, 5 );
	
	bSizer1->Add( bSizer11, 1, wxEXPAND, 5 );
	
	m_bitmap1 = new wxStaticBitmap( m_mainPanelPtr, wxID_ANY, wxBitmap( ssl_xpm ), wxDefaultPosition, wxDefaultSize, 0 );
	bSizer1->Add( m_bitmap1, 0, wxALL, 5 );
	
	bSizerPanelPtr->Add( bSizer1, 1, wxEXPAND, 5 );
	
	
	bSizerPanelPtr->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_staticText220 = new wxStaticText( m_mainPanelPtr, wxID_ANY, wxT("BlueTruth OutStation"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText220->Wrap( -1 );
	m_staticText220->SetFont( wxFont( 10, 70, 90, 90, false, wxEmptyString ) );
	
	bSizerPanelPtr->Add( m_staticText220, 0, wxALL|wxALIGN_CENTER_HORIZONTAL, 5 );
	
	m_staticText2201 = new wxStaticText( m_mainPanelPtr, wxID_ANY, wxT("Developed by:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText2201->Wrap( -1 );
	m_staticText2201->SetFont( wxFont( 10, 70, 90, 92, false, wxEmptyString ) );
	
	bSizerPanelPtr->Add( m_staticText2201, 0, wxALL|wxALIGN_CENTER_HORIZONTAL, 5 );
	
	m_staticText221 = new wxStaticText( m_mainPanelPtr, wxID_ANY, wxT("Simulation Systems Limited\nUnit 12, Market Industrial Estate\nYatton, Bristol, BS49 4RF\n\ntel. (01934) 838803\nfax. (01934) 876202"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText221->Wrap( -1 );
	m_staticText221->SetFont( wxFont( 10, 70, 90, 90, false, wxEmptyString ) );
	
	bSizerPanelPtr->Add( m_staticText221, 0, wxALL|wxALIGN_CENTER_HORIZONTAL, 5 );
	
	m_staticText222 = new wxStaticText( m_mainPanelPtr, wxID_ANY, wxT("Homepage:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText222->Wrap( -1 );
	m_staticText222->SetFont( wxFont( 10, 70, 90, 92, false, wxEmptyString ) );
	
	bSizerPanelPtr->Add( m_staticText222, 0, wxALL|wxALIGN_CENTER_HORIZONTAL, 5 );
	
	m_hyperlinkPtr = new wxHyperlinkCtrl( m_mainPanelPtr, ID_M_HYPERLINKPTR, wxT("www.simulation-systems.co.uk"), wxT("www.simulation-systems.co.uk"), wxDefaultPosition, wxDefaultSize, wxHL_DEFAULT_STYLE );
	m_hyperlinkPtr->SetFont( wxFont( 10, 70, 90, 90, false, wxEmptyString ) );
	
	bSizerPanelPtr->Add( m_hyperlinkPtr, 0, wxALL|wxALIGN_CENTER_HORIZONTAL, 5 );
	
	m_staticlinePtr = new wxStaticLine( m_mainPanelPtr, ID_M_STATICLINEPTR, wxDefaultPosition, wxDefaultSize, wxLI_HORIZONTAL );
	bSizerPanelPtr->Add( m_staticlinePtr, 0, wxEXPAND | wxALL, 5 );
	
	m_buttonOkPtr = new wxButton( m_mainPanelPtr, ID_M_BUTTONOKPTR, wxT("OK"), wxDefaultPosition, wxDefaultSize, 0 );
	bSizerPanelPtr->Add( m_buttonOkPtr, 0, wxALL|wxALIGN_CENTER_HORIZONTAL, 5 );
	
	m_mainPanelPtr->SetSizer( bSizerPanelPtr );
	m_mainPanelPtr->Layout();
	bSizerPanelPtr->Fit( m_mainPanelPtr );
	bSizerMainPtr->Add( m_mainPanelPtr, 1, wxEXPAND, 0 );
	
	this->SetSizer( bSizerMainPtr );
	this->Layout();
	
	this->Centre( wxBOTH );
}

AboutDialog::~AboutDialog()
{
}

BEGIN_EVENT_TABLE( ErrorDialog, wxDialog )
	EVT_CLOSE( ErrorDialog::_wxFB_onClose )
	EVT_IDLE( ErrorDialog::_wxFB_onIdle )
	EVT_INIT_DIALOG( ErrorDialog::_wxFB_onInit )
	EVT_BUTTON( ID_ErrorDialogOk, ErrorDialog::_wxFB_onClickOk )
	EVT_BUTTON( ID_ErrorDialogMore, ErrorDialog::_wxFB_onClickMore )
END_EVENT_TABLE()

ErrorDialog::ErrorDialog( wxWindow* parent, wxWindowID id, const wxString& title, const wxPoint& pos, const wxSize& size, long style ) : wxDialog( parent, id, title, pos, size, style )
{
	this->SetSizeHints( wxSize( 360,120 ), wxSize( -1,-1 ) );
	
	wxBoxSizer* bSizerTop;
	bSizerTop = new wxBoxSizer( wxVERTICAL );
	
	m_panelSingleMessage = new wxPanel( this, wxID_ANY, wxDefaultPosition, wxSize( 360,-1 ), wxTAB_TRAVERSAL );
	m_panelSingleMessage->Enable( false );
	m_panelSingleMessage->SetMinSize( wxSize( 360,-1 ) );
	
	wxBoxSizer* bSizer1;
	bSizer1 = new wxBoxSizer( wxHORIZONTAL );
	
	m_bitmapPtr = new wxStaticBitmap( m_panelSingleMessage, wxID_ANY, wxNullBitmap, wxDefaultPosition, wxSize( -1,-1 ), 0 );
	bSizer1->Add( m_bitmapPtr, 0, wxEXPAND|wxALL, 5 );
	
	
	bSizer1->Add( 5, 0, 0, wxEXPAND, 5 );
	
	m_staticTextLogMessage = new wxStaticText( m_panelSingleMessage, wxID_ANY, wxEmptyString, wxPoint( -1,-1 ), wxSize( 295,-1 ), wxALIGN_LEFT );
	m_staticTextLogMessage->Wrap( -1 );
	bSizer1->Add( m_staticTextLogMessage, 1, wxTOP|wxRIGHT|wxLEFT, 10 );
	
	m_panelSingleMessage->SetSizer( bSizer1 );
	m_panelSingleMessage->Layout();
	bSizerTop->Add( m_panelSingleMessage, 0, wxEXPAND, 5 );
	
	m_panelMultipleMessages = new wxPanel( this, wxID_ANY, wxDefaultPosition, wxSize( 360,-1 ), wxTAB_TRAVERSAL );
	m_panelMultipleMessages->Hide();
	m_panelMultipleMessages->SetMinSize( wxSize( 360,-1 ) );
	
	wxBoxSizer* bSizer53;
	bSizer53 = new wxBoxSizer( wxVERTICAL );
	
	m_gridLogMessages = new wxGrid( m_panelMultipleMessages, ID_GRID_LOG_MESSAGES, wxDefaultPosition, wxDefaultSize, 0 );
	
	// Grid
	m_gridLogMessages->CreateGrid( 0, 2 );
	m_gridLogMessages->EnableEditing( false );
	m_gridLogMessages->EnableGridLines( true );
	m_gridLogMessages->EnableDragGridSize( false );
	m_gridLogMessages->SetMargins( 0, 0 );
	
	// Columns
	m_gridLogMessages->SetColSize( 0, 24 );
	m_gridLogMessages->SetColSize( 1, 304 );
	m_gridLogMessages->EnableDragColMove( false );
	m_gridLogMessages->EnableDragColSize( false );
	m_gridLogMessages->SetColLabelSize( 24 );
	m_gridLogMessages->SetColLabelValue( 0, wxT(" ") );
	m_gridLogMessages->SetColLabelValue( 1, wxT("Message") );
	m_gridLogMessages->SetColLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Rows
	m_gridLogMessages->EnableDragRowSize( false );
	m_gridLogMessages->SetRowLabelSize( 0 );
	m_gridLogMessages->SetRowLabelAlignment( wxALIGN_CENTRE, wxALIGN_CENTRE );
	
	// Label Appearance
	
	// Cell Defaults
	m_gridLogMessages->SetDefaultCellAlignment( wxALIGN_LEFT, wxALIGN_TOP );
	bSizer53->Add( m_gridLogMessages, 1, wxEXPAND, 5 );
	
	m_panelMultipleMessages->SetSizer( bSizer53 );
	m_panelMultipleMessages->Layout();
	bSizerTop->Add( m_panelMultipleMessages, 1, wxEXPAND, 5 );
	
	m_panelButtons = new wxPanel( this, wxID_ANY, wxDefaultPosition, wxDefaultSize, wxTAB_TRAVERSAL );
	wxBoxSizer* bSizer2;
	bSizer2 = new wxBoxSizer( wxHORIZONTAL );
	
	
	bSizer2->Add( 0, 0, 1, wxEXPAND, 5 );
	
	wxBoxSizer* bSizer85;
	bSizer85 = new wxBoxSizer( wxVERTICAL );
	
	wxButton* m_buttonOk;
	m_buttonOk = new wxButton( m_panelButtons, ID_ErrorDialogOk, wxT("&OK"), wxDefaultPosition, wxDefaultSize, 0 );
	m_buttonOk->SetDefault(); 
	bSizer85->Add( m_buttonOk, 0, wxALL|wxALIGN_CENTER_VERTICAL, 5 );
	
	bSizer2->Add( bSizer85, 0, 0, 5 );
	
	wxBoxSizer* bSizer86;
	bSizer86 = new wxBoxSizer( wxVERTICAL );
	
	m_buttonMore = new wxButton( m_panelButtons, ID_ErrorDialogMore, wxT("&More..."), wxDefaultPosition, wxDefaultSize, 0 );
	bSizer86->Add( m_buttonMore, 0, wxALL, 5 );
	
	bSizer2->Add( bSizer86, 0, 0, 5 );
	
	
	bSizer2->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_panelButtons->SetSizer( bSizer2 );
	m_panelButtons->Layout();
	bSizer2->Fit( m_panelButtons );
	bSizerTop->Add( m_panelButtons, 0, wxEXPAND|wxTOP|wxBOTTOM, 5 );
	
	this->SetSizer( bSizerTop );
	this->Layout();
}

ErrorDialog::~ErrorDialog()
{
}

BEGIN_EVENT_TABLE( SettingsDialog, wxDialog )
	EVT_CLOSE( SettingsDialog::_wxFB_onClose )
	EVT_INIT_DIALOG( SettingsDialog::_wxFB_onInit )
	EVT_BUTTON( ID_M_BUTTONRESETTODEFAULTSPTR, SettingsDialog::_wxFB_onResetToDefaultsClick )
	EVT_BUTTON( ID_M_BUTTONOK, SettingsDialog::_wxFB_onClickOk )
	EVT_BUTTON( ID_M_BUTTONCANCEL, SettingsDialog::_wxFB_onClickCancel )
END_EVENT_TABLE()

SettingsDialog::SettingsDialog( wxWindow* parent, wxWindowID id, const wxString& title, const wxPoint& pos, const wxSize& size, long style ) : wxDialog( parent, id, title, pos, size, style )
{
	this->SetSizeHints( wxSize( 300,-1 ), wxDefaultSize );
	
	wxBoxSizer* bSizerTop;
	bSizerTop = new wxBoxSizer( wxVERTICAL );
	
	m_panel10 = new wxPanel( this, wxID_ANY, wxDefaultPosition, wxDefaultSize, wxTAB_TRAVERSAL );
	wxBoxSizer* bSizer79;
	bSizer79 = new wxBoxSizer( wxVERTICAL );
	
	wxStaticBoxSizer* sbSizer11;
	sbSizer11 = new wxStaticBoxSizer( new wxStaticBox( m_panel10, wxID_ANY, wxT("Core Parameters") ), wxVERTICAL );
	
	wxFlexGridSizer* fgSizer6;
	fgSizer6 = new wxFlexGridSizer( 3, 2, 0, 0 );
	fgSizer6->AddGrowableCol( 1 );
	fgSizer6->SetFlexibleDirection( wxHORIZONTAL );
	fgSizer6->SetNonFlexibleGrowMode( wxFLEX_GROWMODE_SPECIFIED );
	
	m_staticText49 = new wxStaticText( m_panel10, wxID_ANY, wxT("Site Identifier"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText49->Wrap( -1 );
	fgSizer6->Add( m_staticText49, 0, wxALL|wxALIGN_RIGHT|wxALIGN_CENTER_VERTICAL, 5 );
	
	m_textCtrlSiteIdentifierPtr = new wxTextCtrl( m_panel10, ID_M_TEXTCTRLSITEIDENTIFIERPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0, wxDefaultValidator, wxT("Site Identifier unique to each deployment") );
	fgSizer6->Add( m_textCtrlSiteIdentifierPtr, 0, wxALL|wxALIGN_CENTER_VERTICAL|wxEXPAND, 5 );
	
	m_staticText40 = new wxStaticText( m_panel10, wxID_ANY, wxT("SSL Serial Number"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText40->Wrap( -1 );
	fgSizer6->Add( m_staticText40, 0, wxALL|wxALIGN_RIGHT|wxALIGN_CENTER_VERTICAL, 5 );
	
	m_textCtrlSSLSerialNumberPtr = new wxTextCtrl( m_panel10, ID_M_TEXTCTRLSSLSERIALNUMBERPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0, wxDefaultValidator, wxT("SSL Serial Number which is used as unique identifier of the OutStation") );
	fgSizer6->Add( m_textCtrlSSLSerialNumberPtr, 0, wxALL|wxEXPAND|wxALIGN_CENTER_VERTICAL, 5 );
	
	m_staticText41 = new wxStaticText( m_panel10, wxID_ANY, wxT("Configuration File URL"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText41->Wrap( -1 );
	fgSizer6->Add( m_staticText41, 0, wxALL|wxALIGN_RIGHT|wxALIGN_CENTER_VERTICAL, 5 );
	
	m_textCtrlConfigurationURLPtr = new wxTextCtrl( m_panel10, ID_M_TEXTCTRLCONFIGURATIONURLPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0 );
	m_textCtrlConfigurationURLPtr->SetToolTip( wxT("Full URL (Uniform Resource Locator) to configuration file") );
	
	fgSizer6->Add( m_textCtrlConfigurationURLPtr, 0, wxALL|wxEXPAND|wxALIGN_CENTER_VERTICAL, 5 );
	
	sbSizer11->Add( fgSizer6, 1, wxEXPAND, 5 );
	
	bSizer79->Add( sbSizer11, 0, wxALL|wxEXPAND, 5 );
	
	wxStaticBoxSizer* sbSizer10;
	sbSizer10 = new wxStaticBoxSizer( new wxStaticBox( m_panel10, wxID_ANY, wxT("Blootooth Device:") ), wxVERTICAL );
	
	fgSizer7 = new wxFlexGridSizer( 2, 2, 0, 0 );
	fgSizer7->AddGrowableCol( 1 );
	fgSizer7->SetFlexibleDirection( wxHORIZONTAL );
	fgSizer7->SetNonFlexibleGrowMode( wxFLEX_GROWMODE_SPECIFIED );
	
	m_staticText57 = new wxStaticText( m_panel10, wxID_ANY, wxT("Device to be used:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText57->Wrap( -1 );
	fgSizer7->Add( m_staticText57, 0, wxALL|wxALIGN_CENTER_VERTICAL|wxALIGN_RIGHT, 5 );
	
	m_comboBoxBluetoothDeviceToBeUsedPtr = new wxComboBox( m_panel10, ID_M_COMBOBOXBLOOTOOTHDEVICETOBEUSEDPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0, NULL, 0 ); 
	fgSizer7->Add( m_comboBoxBluetoothDeviceToBeUsedPtr, 0, wxALL|wxEXPAND|wxALIGN_CENTER_VERTICAL, 5 );
	
	m_staticText48 = new wxStaticText( m_panel10, wxID_ANY, wxT("Device driver:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText48->Wrap( -1 );
	fgSizer7->Add( m_staticText48, 0, wxALL|wxALIGN_CENTER_VERTICAL|wxALIGN_RIGHT, 5 );
	
	m_comboBoxBluetoothDeviceDriverPtr = new wxComboBox( m_panel10, ID_M_COMBOBOXBLUETOOTHDEVICEDRIVERPTR, wxEmptyString, wxDefaultPosition, wxDefaultSize, 0, NULL, 0 ); 
	fgSizer7->Add( m_comboBoxBluetoothDeviceDriverPtr, 0, wxALIGN_CENTER_VERTICAL|wxALL|wxEXPAND, 5 );
	
	sbSizer10->Add( fgSizer7, 1, wxEXPAND, 5 );
	
	bSizer79->Add( sbSizer10, 0, wxALL|wxEXPAND, 5 );
	
	wxStaticBoxSizer* sbSizer18;
	sbSizer18 = new wxStaticBoxSizer( new wxStaticBox( m_panel10, wxID_ANY, wxT("Logging") ), wxVERTICAL );
	
	wxBoxSizer* bSizer24;
	bSizer24 = new wxBoxSizer( wxHORIZONTAL );
	
	m_staticText20 = new wxStaticText( m_panel10, wxID_ANY, wxT("Visual Log Level:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText20->Wrap( -1 );
	bSizer24->Add( m_staticText20, 0, wxALL|wxALIGN_CENTER_VERTICAL, 5 );
	
	
	bSizer24->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_comboBoxVisualLogLevelPtr = new wxComboBox( m_panel10, ID_M_COMBOBOXVISUALLOGLEVELPTR, wxT("Log levels"), wxDefaultPosition, wxDefaultSize, 0, NULL, wxCB_READONLY ); 
	m_comboBoxVisualLogLevelPtr->SetToolTip( wxT("Logging window or console level") );
	
	bSizer24->Add( m_comboBoxVisualLogLevelPtr, 0, wxALL|wxALIGN_CENTER_VERTICAL, 5 );
	
	sbSizer18->Add( bSizer24, 1, wxEXPAND, 5 );
	
	wxBoxSizer* bSizer25;
	bSizer25 = new wxBoxSizer( wxHORIZONTAL );
	
	m_staticText21 = new wxStaticText( m_panel10, wxID_ANY, wxT("File Log Level:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText21->Wrap( -1 );
	bSizer25->Add( m_staticText21, 0, wxALL|wxALIGN_CENTER_VERTICAL, 5 );
	
	
	bSizer25->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_comboBoxFileLogLevelPtr = new wxComboBox( m_panel10, ID_M_COMBOBOXFILELOGLEVELPTR, wxT("Log levels"), wxDefaultPosition, wxDefaultSize, 0, NULL, wxCB_READONLY ); 
	m_comboBoxFileLogLevelPtr->SetToolTip( wxT("File logging level") );
	
	bSizer25->Add( m_comboBoxFileLogLevelPtr, 0, wxALL|wxALIGN_CENTER_VERTICAL, 5 );
	
	sbSizer18->Add( bSizer25, 1, wxEXPAND, 5 );
	
	wxBoxSizer* bSizer75;
	bSizer75 = new wxBoxSizer( wxHORIZONTAL );
	
	m_staticText192 = new wxStaticText( m_panel10, wxID_ANY, wxT("Max number of entries in ET Event Log:"), wxDefaultPosition, wxDefaultSize, 0 );
	m_staticText192->Wrap( -1 );
	m_staticText192->SetToolTip( wxT("Maximum number of entries in the ET Event Log.\nThere more entries the slower the program is.\nThe recommended value is 1000") );
	
	bSizer75->Add( m_staticText192, 0, wxALL|wxALIGN_CENTER_VERTICAL, 5 );
	
	
	bSizer75->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_spinCtrlMaxNumberOfEntriesInEventLogGrid = new wxSpinCtrl( m_panel10, ID_M_SPINCTRLMAXNUMBEROFENTRIESINEVENTLOGGRID, wxEmptyString, wxDefaultPosition, wxDefaultSize, wxSP_ARROW_KEYS, 1, 100000, 1 );
	bSizer75->Add( m_spinCtrlMaxNumberOfEntriesInEventLogGrid, 0, wxALL, 5 );
	
	sbSizer18->Add( bSizer75, 1, wxEXPAND, 5 );
	
	bSizer79->Add( sbSizer18, 0, wxALL|wxEXPAND, 5 );
	
	wxStaticBoxSizer* sbSizer19;
	sbSizer19 = new wxStaticBoxSizer( new wxStaticBox( m_panel10, wxID_ANY, wxT("Main Window Size and Location") ), wxVERTICAL );
	
	m_checkBoxReadOnProgramStartPtr = new wxCheckBox( m_panel10, ID_M_CHECKBOXREADONSTARTPTR, wxT("&Read on program start"), wxDefaultPosition, wxDefaultSize, 0 );
	m_checkBoxReadOnProgramStartPtr->SetToolTip( wxT("The last stored size and position of the main program window \nwill or will not be read on program start") );
	
	sbSizer19->Add( m_checkBoxReadOnProgramStartPtr, 0, wxALL, 5 );
	
	m_checkBoxSaveOnProgramExitPtr = new wxCheckBox( m_panel10, ID_M_CHECKBOXSAVEONPROGRAMEXITPTR, wxT("&Save on program exit"), wxDefaultPosition, wxDefaultSize, 0 );
	m_checkBoxSaveOnProgramExitPtr->SetToolTip( wxT("The size and position of the main program window \nwill or will not be stored on program exit") );
	
	sbSizer19->Add( m_checkBoxSaveOnProgramExitPtr, 0, wxALL, 5 );
	
	bSizer79->Add( sbSizer19, 0, wxALL|wxEXPAND, 5 );
	
	wxBoxSizer* bSizer86;
	bSizer86 = new wxBoxSizer( wxVERTICAL );
	
	
	bSizer86->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_buttonResetToDefaultsPtr = new wxButton( m_panel10, ID_M_BUTTONRESETTODEFAULTSPTR, wxT("Reset to Defaults"), wxDefaultPosition, wxDefaultSize, 0 );
	bSizer86->Add( m_buttonResetToDefaultsPtr, 0, wxALL|wxALIGN_RIGHT, 5 );
	
	bSizer79->Add( bSizer86, 1, wxALL|wxEXPAND, 5 );
	
	wxBoxSizer* bSizer3;
	bSizer3 = new wxBoxSizer( wxHORIZONTAL );
	
	
	bSizer3->Add( 0, 0, 1, wxEXPAND, 5 );
	
	m_buttonOkPtr = new wxButton( m_panel10, ID_M_BUTTONOK, wxT("&OK"), wxDefaultPosition, wxDefaultSize, 0 );
	bSizer3->Add( m_buttonOkPtr, 0, wxALL, 5 );
	
	m_buttonCancelPtr = new wxButton( m_panel10, ID_M_BUTTONCANCEL, wxT("&Cancel"), wxDefaultPosition, wxDefaultSize, 0 );
	bSizer3->Add( m_buttonCancelPtr, 0, wxALL, 5 );
	
	
	bSizer3->Add( 0, 0, 1, wxEXPAND, 5 );
	
	bSizer79->Add( bSizer3, 0, wxALL|wxEXPAND, 5 );
	
	m_panel10->SetSizer( bSizer79 );
	m_panel10->Layout();
	bSizer79->Fit( m_panel10 );
	bSizerTop->Add( m_panel10, 1, wxEXPAND, 5 );
	
	this->SetSizer( bSizerTop );
	this->Layout();
	
	this->Centre( wxBOTH );
}

SettingsDialog::~SettingsDialog()
{
}
