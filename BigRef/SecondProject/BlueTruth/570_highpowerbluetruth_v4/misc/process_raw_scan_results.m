%%This function reads all records from 'raw_scan_results.csv' file
%% and creates a map of device records starting from minTime over
%% duration of durationInSeconds. Finally the map is plotted on 
%% the screen

%Clear all existing variables and set formatting
%clear all;
more off;
format long;

if (exist("minTime", "var") != 1)
	printf("minTime variable not defined. Using current time value...\n");
	minTime = time - 8*60*60;
	minTime = 1377850119;
endif
if (exist("durationInSeconds", "var") != 1)
	printf("durationInSeconds variable not defined. Using default value (1 hour)...\n");
	durationInSeconds = 60*60 %60 minutes
endif

maxTime = minTime + durationInSeconds;
maxTime = 1377866974;

printf("Looking for records from \n\t%s\nto\n\t%s\n", \
	strftime ("%R (%Z) %A %e %B %Y", (localtime(minTime))), \
	strftime ("%R (%Z) %A %e %B %Y", (localtime(maxTime))));

fid = fopen('raw_scan_results.csv', 'r');
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
				_timeFirstObserved = str2num(word);
				if ((_timeFirstObserved < minTime) || (_timeFirstObserved > maxTime))
					skip = true;
					break;
				endif
			elseif (index == 2)
				_timeLastObserved = str2num(word);
			elseif (index == 3)
				%ignore
			elseif (index == 4)
				_MAC = word;
			elseif (index == 5)
				_cod = word;
			elseif (index == 6)
				_name = word;
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
			timeFirstObservedCollection = _timeFirstObserved;
			timeLastObservedCollection = _timeLastObserved;
			MACCollection = _MAC;
			codCollection = _cod;
			%nameCollection = _name;
		else    
			timeFirstObservedCollection = [timeFirstObservedCollection; _timeFirstObserved];
			timeLastObservedCollection  = [timeLastObservedCollection;  _timeLastObserved];
			MACCollection = [MACCollection; _MAC];
			codCollection = [codCollection; _cod];
			%nameCollection = [nameCollection; _name];
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

printf("%d lines processed. %d lines match the time span.\n", counter, numberOfEntries);

if (numberOfEntries == 0)
	printf("Nothing to do. Exiting...\n");
	return;
endif

timeCollection = timeFirstObservedCollection;
printf("Time span <%d, %d>\n", min(timeCollection), max(timeCollection));
uniqueTimeCollection = unique(timeCollection);
uniqueTimeCollectionSize = max(size(uniqueTimeCollection));
timeMap = [ uniqueTimeCollection, zeros(uniqueTimeCollectionSize, 1)];
uniqueMACCollection = unique(MACCollection);
uniqueMACCollectionSize = max(size(uniqueMACCollection));
MACMap = [ uniqueMACCollection, zeros(uniqueMACCollectionSize, 1)];
printf("Number of devices observed: %d\n", uniqueMACCollectionSize);

%Create a map containing information how many times a device have been 
%observed
iMax = max(size(timeCollection));
for i=1:iMax
	found = false;
	for j=1:uniqueMACCollectionSize
		if (MACCollection(i) == MACMap(j, 1))
			MACMap(j, 2)++;
			found = true;
		else
			%do nothing
		endif
	endfor
	assert(found);
endfor


%Create a map containing information how many devices have been 
%observed at t time, t - index
iMax = max(size(timeCollection));
index = 1;
for i=1:iMax
	if (timeCollection(i) == timeMap(index, 1))
		timeMap(index, 2)++;
	else
		index++;
		#index+49, timeCollection(i), timeMap(index)
		assert(timeCollection(i) == timeMap(index));
		timeMap(index, 2)++;
	endif
endfor

numberOfDeviceReadings = sum(timeMap(:,2))
printf("Found %d device readings\n", numberOfDeviceReadings);

numberOfTimeSlotsInTheMap = size(timeMap, 1);
for i=1:numberOfTimeSlotsInTheMap
	timeMap(i, 1) -= minTime;
endfor
printf("Done!\n");

minTimeMapXValue = min(timeMap(:,1));
maxTimeMapXValue = max(timeMap(:,1));
minTimeMapYValue = min(timeMap(:,2));
maxTimeMapYValue = max(timeMap(:,2));

cla;
plot(timeMap(1:uniqueTimeCollectionSize,1), timeMap(1:uniqueTimeCollectionSize,2), 'x');
grid on;
title("Number of device observations vs time");
xlabel("time [s]");
ylabel("number of observations per scan");
legend("number of observations per scan");
legend("boxon");
axis([0, maxTimeMapXValue, -0.5, maxTimeMapYValue + 0.5]);

if (maxTimeMapYValue > 0)
	text(30, 0.8 * maxTimeMapYValue - 0, strcat( \
		"Number of observations: ", num2str(numberOfDeviceReadings), "\n", \
%		"Average inquiry period: ", num2str((maxTimeMapXValue - minTimeMapXValue + 1) / numberOfTimeSlotsInTheMap), " s\n", \
		"Number of devices observed: ", num2str(uniqueMACCollectionSize), \
		""));
endif
