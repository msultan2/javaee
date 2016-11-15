transmit_power = [ 20, 10, 0, -10, -20, -30, -40, -50, -60, -70 ];

%001bafba694d Nokia 6120C
pA = [ 24/24, 23/23, 21/22, (17+22)/(17+22), 22/24, 23/24, 21/23, 20/23, (22+9)/(24+9), 21/23 ];
%c8df7cc81be7 Nokia 5230
pB = [ 24/24, 23/23, 19/22, (17+21)/(17+22), 19/24, 21/24, 19/23, 21/23, (23+9)/(24+9), 18/23 ];

cla;
hold on;
plot(transmit_power, pA, '-x0');
plot(transmit_power, pB, '-x1');
legend(
	"Nokia 6120C", 
	"Nokia 5230" 
	);
hold off;

grid on;
title("Probability of receiving a response vs transmit power");
xlabel("P [dBm]");
ylabel("p");

print -dpng -color -r300  transmit_power.png
