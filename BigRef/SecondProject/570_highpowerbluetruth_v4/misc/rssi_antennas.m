distance = [ 5, 10, 20, 40, 80 ];

%A - omnidirectional antenna
%B - can antenna

%afba694d Nokia 6120C
rssi1A = [ -57.4, -60.4, -70.6, -79.7, -90.0 ];
rssi1B = [ -72.6, -57.2, -70.1, -75.0, -86.3 ];

%15a21dfb Xperia S
rssi2A = [ -60.7, -57.4, -70.9, -75.6, -81.9 ];
rssi2B = [ -54.2, -50.4, -58.2, -68.3, -83.7 ];

%7cc81be7 Nokia 5230
rssi3A = [ -61.1, -63.2, -71.0, -77.2, -84.5 ];
rssi3B = [ -53.4, -47.7, -57.7, -63.6, -81.8 ];

%e371b55c MB525
rssi4A = [ -49.7, -53.3, -59.3, -65.3, -75.6 ];
rssi4B = [ -41.1, -33.8, -44.1, -51.4, -72.5 ];

cla;
hold on;
plot(distance, rssi1B(:), '-o0');
plot(distance, rssi1A(:), '-x0');
plot(distance, rssi2B(:), '-o1');
plot(distance, rssi2A(:), '-x1');
plot(distance, rssi3B(:), '-o2');
plot(distance, rssi3A(:), '-x2');
plot(distance, rssi4B(:), '-o3');
plot(distance, rssi4A(:), '-x3');
legend(
	"Nokia 6120C (can)", 
	"Nokia 6120C (omni)", 
	"Xperia S (can)", 
	"Xperia S (omni)", 
	"Nokia 5230 (can)", 
	"Nokia 5230 (omni)", 
	"MB525 (can)",
	"MB525 (omni)"
	);
hold off;

grid on;
title("Received Signal Strength Indication (RSSI) vs distance");
xlabel("distance [m]");
ylabel("RSSI");

print -dpng -color -r300  rssi_antenna.png
