%%This function reads all records from 'raw_rssi_scan_results.csv' file
%% and creates a map of device records starting from minTime over
%% duration of durationInSeconds. Finally the map is plotted on 
%% the screen

%Clear all existing variables and set formatting
clear all;
more off;
format long;

fid = fopen('rssi_measurements/raw_rssi_scan_results.csv.obwodnica_40_60kmh', 'r');
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
MACMap_averageRSSI = zeros(uniqueMACCollectionSize, 1);
MACMap_squaredAverageRSSI = zeros(uniqueMACCollectionSize, 1);
printf("Number of devices observed: %d\n", uniqueMACCollectionSize);


%Create a map containing information how many times a device have been 
%observed
iMax = max(size(timeCollection));
for i=1:iMax
	found = false;
	for j=1:uniqueMACCollectionSize
		if (MACCollection(i,1) == uniqueMACCollection(j))
			MACMap_numberOfOccurences(j)++; %increase occurence count
			MACMap_averageRSSI(j) += rssiCollection(i, 1);
			MACMap_squaredAverageRSSI(j) += rssiCollection(i) * rssiCollection(i);
			found = true;
		else
			%do nothing
		endif
	endfor
	assert(found);
endfor

for j=1:uniqueMACCollectionSize
	MACMap_averageRSSI(j) /= MACMap_numberOfOccurences(j);
	MACMap_squaredAverageRSSI(j) /= MACMap_numberOfOccurences(j);
	
	printf("MAC=%x, occurences=%d, average RSSI=%g, RSSI deviation=%g\n", 
		uniqueMACCollection(j), 
		MACMap_numberOfOccurences(j), 
		MACMap_averageRSSI(j), 
		sqrt(MACMap_squaredAverageRSSI(j) - MACMap_averageRSSI(j)*MACMap_averageRSSI(j)));
endfor


cla;
plot(timeObservedCollection(1:uniqueTimeCollectionSize), rssiCollection(1:uniqueTimeCollectionSize), 'x');
grid on;
title("Number of device observations vs time");
xlabel("time [s]");
ylabel("RSSI");
legend("RSSI vs time");
