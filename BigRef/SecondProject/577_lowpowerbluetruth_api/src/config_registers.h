#include <xc.h>

_CONFIG1(WDTPS_PS32768 & FWPSA_PR128 & WINDIS_OFF & FWDTEN_ON & ICS_PGx1 & GWRP_OFF & GCP_OFF & JTAGEN_OFF )
_CONFIG2(POSCMOD_HS /*& I2C2SEL_PRI*/ & IOL1WAY_ON & OSCIOFNC_OFF & FCKSM_CSDCMD & FNOSC_PRIPLL & IESO_OFF  & PLL96MHZ_ON & PLLDIV_DIV2 )
//Input Clock = 7.3728MHz using Div2 produces InClock/2 * 24= 88.4736 MHz, which divides to 29.4912MHz for system clock
//note CLKDIV defaults to 0x0100 so CPDIV1 selects divide by 1 (followed by /3)
_CONFIG3(0xFFFF)
