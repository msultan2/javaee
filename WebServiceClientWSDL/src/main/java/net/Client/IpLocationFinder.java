package net.Client;

import net.webservicex.GeoIP;
import net.webservicex.GeoIPService;
import net.webservicex.GeoIPServiceSoap;

public class IpLocationFinder {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ipAddress = "82.129.130.219";
		GeoIPService ipService=new GeoIPService();
		GeoIPServiceSoap geoIPServiceSoap=ipService.getGeoIPServiceSoap();
		GeoIP geoIP=geoIPServiceSoap.getGeoIP(ipAddress);
		System.out.println(geoIP.getCountryName());;
	}

}
