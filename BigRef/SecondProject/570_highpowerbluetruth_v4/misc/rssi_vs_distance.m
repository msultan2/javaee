distance0 = [ 0, 1, 2, 3, 4, \
	5, 8, 10, 15, 20, \
	30, 40, 50 ];
rssi0 = [ -34.81, -51.03, -55.67, -57.33, -59.58, \
	-60.60, -61.34, -61.02, -63.23, -69.31, \
	-74.75, -80.26, -82.96 ];
rssi_deviation0 = [ 2.13, 1.83, 2.04, 2.50, 3.44, \
	3.30, 2.01, 1.87, 1.82, 2.46, \
	3.91, 2.13, 1.58]

distance1A = [ 1, 2, 3, 4, 5, \
	8, 10, 15, 20, 30\
	40, 50, 70];

%afba694d Nokia 6120C
rssi1A = [ -50.08, -50.64, -55.79, -54.13, -59.13, \
	-59.93, -61.15, -65.99, -70.85, -76.39, \
	-77.50, -84.17 ];

%fd80a1b8 Nokia 5310 Express
rssi1B = [ -46.47, -45.66, -48.21, -51.84, -59.74, \
	-56.42, -57.16, -58.97, -71.01, -78.34, \
	-78.33, -80.20, -87.71 ];

%7cc81be7 Nokia 5230
rssi1C = [ -56.22, -46.19, -45.43, -48.23, -56.72, \
	-50.92, -51.78, -55.97, -63.89, -72.37, \
	-74.52, -81.32, -84.11 ];

cla;
hold on;
plot(distance0(:), rssi0(:), '-xcr');
plot(distance1A(1:12), rssi1A(:), '-xcg');
plot(distance1A(:), rssi1B(:), '-xcb');
plot(distance1A(:), rssi1C(:), '-xcv');
legend("Nokia 6120C (1)", "Nokia 6120C (2)", "Nokia 5310", "Nokia 5230");
hold off;

grid on;
title("Received Signal Strength Indication (RSSI) vs distance");
xlabel("distance [m]");
ylabel("RSSI");

print -dpng -color -r150 -F:48 rssi_vs_distance.png
