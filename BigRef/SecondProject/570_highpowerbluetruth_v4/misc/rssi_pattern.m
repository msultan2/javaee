%%This function reads all records from 'raw_rssi_scan_results.csv' file
%% and creates a map of device records starting from minTime over
%% duration of durationInSeconds. Finally the map is plotted on 
%% the screen

reloadData = 1;
if (reloadData == 1)

%Clear all existing variables and set formatting
clear all;
more off;
format long;

%fid = fopen('raw_rssi_scan_results.csv.TX_power_ref', 'r');
fid = fopen('/var/cache/bt/raw_rssi_scan_results.csv', 'r');
header = fgetl(fid);

counter = 0;
numberOfEntries = 0;

barUnit = 100;
printf("Reading file contents (one bar=%d): ", barUnit);

while (!feof(fid))
    eachLine = fgetl(fid);

	eol=false;
	skip=false;
	i = 1;
	j = 1;
	index = 1;
	word = '';
	sizeOfLine = max(size(eachLine));
	while (eol == false)
		%Ignore the line if this line is a comment
		if (eachLine(1) == '#')
			skip = true;
			break;
		endif
		
		if ((j>sizeOfLine) || (eachLine(j) == ',')) 
			if (j>i)
				word = eachLine(i:(j-1));
			else
				word = '';
			endif
			
			if (index == 1)
				_timeObserved = str2num(word);
			elseif (index == 2)
				_MAC = int64(hex2dec(word));
			elseif (index == 3)
				_cod = hex2dec(word);
			elseif (index == 4)
				_rssi = str2num(word);
				eol = true;
			else
			endif
			i = j+1;
			index++;
		endif

		j++;
	endwhile
	
	if (!skip)
		if (numberOfEntries == 0)
			timeObservedCollection = _timeObserved;
			MACCollection = _MAC;
			codCollection = _cod;
			rssiCollection = _rssi;
		else    
			timeObservedCollection = [timeObservedCollection; _timeObserved];
			MACCollection = [MACCollection; _MAC];
			codCollection = [codCollection; _cod];
			rssiCollection  = [rssiCollection;  _rssi];
		endif
		numberOfEntries++;
	endif
	
    counter++;
	if (mod(counter, barUnit) == 0)
		printf("|");
	endif
endwhile
fclose(fid);
printf("\n");

printf("%d lines processed.\n", counter);

if (numberOfEntries == 0)
	printf("Nothing to do. Exiting...\n");
	return;
endif

timeCollection = timeObservedCollection;
%printf("Time span <%d, %d>\n", min(timeCollection), max(timeCollection));
uniqueTimeCollection = unique(timeCollection);
uniqueTimeCollection = unique(timeCollection);
uniqueTimeCollectionSize = max(size(uniqueTimeCollection));
%timeMap = [ uniqueTimeCollection, zeros(uniqueTimeCollectionSize, 1)];
uniqueMACCollection = unique(MACCollection);
uniqueMACCollectionSize = max(size(uniqueMACCollection));
MACMap_numberOfOccurences = zeros(uniqueMACCollectionSize, 1);
MACMap_firstObservation = zeros(uniqueMACCollectionSize, 1);
MACMap_howLongObserved = zeros(uniqueMACCollectionSize, 1);
MACMap_averageRSSI = zeros(uniqueMACCollectionSize, 1);
MACMap_squaredAverageRSSI = zeros(uniqueMACCollectionSize, 1);
MACMap_maxRSSI = -100*ones(uniqueMACCollectionSize, 1);
printf("Number of devices observed: %d\n", uniqueMACCollectionSize);


%Create a map containing information how many times a device have been 
%observed
iMax = max(size(timeCollection));
for i=1:iMax
	found = false;
	for j=1:uniqueMACCollectionSize
		if (MACCollection(i) == uniqueMACCollection(j))
			if (MACMap_firstObservation(j) == 0)
				MACMap_firstObservation(j) = timeCollection(i);
			endif
			MACMap_howLongObserved(j) = (timeCollection(i) - MACMap_firstObservation(j))/1000;
			MACMap_numberOfOccurences(j)++; %increase occurence count
			MACMap_averageRSSI(j) += rssiCollection(i);
			MACMap_squaredAverageRSSI(j) += rssiCollection(i) * rssiCollection(i);
			MACMap_maxRSSI(j) = max(MACMap_maxRSSI(j), rssiCollection(i));
			found = true;
			break;
		else
			%do nothing
		endif
	endfor
	assert(found);
endfor

for j=1:uniqueMACCollectionSize %iterate over each entry in uniqueMACCollection
	MACMap_averageRSSI(j) /= MACMap_numberOfOccurences(j);
	MACMap_squaredAverageRSSI(j) /= MACMap_numberOfOccurences(j);
	
	printf("MAC=%x, occurences=%d, first observed=%d, observed for=%d[s], average RSSI=%g, RSSI deviation=%g, max RSSI=%g\n", 
		uniqueMACCollection(j), 
		MACMap_numberOfOccurences(j), 
		MACMap_firstObservation(j), 
		MACMap_howLongObserved(j),
		MACMap_averageRSSI(j), 
		sqrt(MACMap_squaredAverageRSSI(j) - MACMap_averageRSSI(j)*MACMap_averageRSSI(j)),
		MACMap_maxRSSI(j));
endfor

endif

cla;
hold on;
clear t0; t0Set = 0;
%Find the first record of a particular MAC address in time and use it as t0
analysisStartPoint = 0;
%analysisEndPoint = analysisStartPoint + 400*1000;

for i=1:uniqueMACCollectionSize 
	if (
		(uniqueMACCollection(i) ==    118912346445) || %6210C
		(uniqueMACCollection(i) == 220862196751335) || %Nokia 5320
		(uniqueMACCollection(i) == 159158966033915) || %Xperia
		(uniqueMACCollection(i) == 224905483367772) || %MB525
		(1 == 0)
		)
		for j=1:size(timeObservedCollection,1)
			if (
				(MACCollection(j) == uniqueMACCollection(i)) &&
				(timeObservedCollection(j) >= analysisStartPoint) &&
				%(rssiCollection(j) > -85) &&
				(1 == 1)
			   )
				if (t0Set == 0)
					t0Set = 1;
					t0 = timeObservedCollection(j)
				else
					if (timeObservedCollection(j) < t0)
						t0 = timeObservedCollection(j)
					endif
				endif 
				break;
			endif
		endfor
	endif
endfor

analysisStartPoint = t0 - 100*1000
analysisEndPoint = analysisStartPoint + 2500*1000;
					

%iterate over each entry in uniqueMACCollection and copy all entries
%into a new vector starting from the first occurence
n = 0;
n_max = 10;

for i=1:uniqueMACCollectionSize 
	clear X; clear Y; 
	XSet = 0;

	%Process only those entries where there is something to analyse
	if (
%		(MACMap_numberOfOccurences(i) > 15) && (MACMap_numberOfOccurences(i) < 50) &&
%		(MACMap_numberOfOccurences(i) > 5) && (MACMap_numberOfOccurences(i) < 12) &&
%		(MACMap_howLongObserved(i) > 5) && (MACMap_howLongObserved(i) < 100) &&
%		(MACMap_howLongObserved(i) > 15) && (MACMap_howLongObserved(i) < 30) &&
		(
		(uniqueMACCollection(i) ==    118912346445) || %6210C
		(uniqueMACCollection(i) == 220862196751335) || %Nokia 5320
		(uniqueMACCollection(i) == 159158966033915) || %Xperia
		(uniqueMACCollection(i) == 224905483367772) || %MB525
		(1 == 0)
		) &&
%		(MACMap_maxRSSI(i) > -75) &&
		(1 == 1)
		)
		
		for j=1:size(timeObservedCollection,1)
			if (
				(MACCollection(j) == uniqueMACCollection(i)) &&
				(timeObservedCollection(j) >= analysisStartPoint) &&
				(timeObservedCollection(j) <= analysisEndPoint) &&
				%(rssiCollection(j) > -85) &&
				(1 == 1)
				)
				
				if (XSet == 0)
					XSet = 1;
					X = (timeObservedCollection(j) - t0)/1000;
					Y = rssiCollection(j);
				else
					X = [ X; (timeObservedCollection(j) - t0)/1000 ];
					Y = [ Y; rssiCollection(j) ];
				endif
			endif
		endfor
		
		
		uniqueMACCollection(i)
		printf("MAC=%x, occurences=%d, first observed=%d, observed for=%d[s], average RSSI=%g, RSSI deviation=%g, max RSSI=%g\n", 
			uniqueMACCollection(i), 
			MACMap_numberOfOccurences(i), 
			MACMap_firstObservation(i), 
			MACMap_howLongObserved(i),
			MACMap_averageRSSI(i), 
			sqrt(MACMap_squaredAverageRSSI(i) - MACMap_averageRSSI(i)*MACMap_averageRSSI(i)),
			MACMap_maxRSSI(i));
		
		style="-";
		switch (mod(n,5))
			case {0}
				style = strcat(style, "+");
			case {1}
				style = strcat(style, "o");
			case {2}
				style = strcat(style, "*");
			case {3}
				style = strcat(style, "x");
			otherwise
				style = strcat(style, "^");
		endswitch
		switch (mod(n,6))
			case {0}
				style = strcat(style, "0");
			case {1}
				style = strcat(style, "1");
			case {2}
				style = strcat(style, "2");
			case {3}
				style = strcat(style, "3");
			case {4}
				style = strcat(style, "4");
			otherwise
				style = strcat(style, "5");
		endswitch

		plot(X, Y, style);		
		n++;
	endif
	
	if (n>=n_max)
		break;
	endif
endfor
hold off;

grid on;
xlabel("time [s]");
ylabel("RSSI");
legend("RSSI vs time");

printf("%d devices plotted\n", n);
%print -dpng -color -r150  rssi_pattern_busy_roundabout_short.png
