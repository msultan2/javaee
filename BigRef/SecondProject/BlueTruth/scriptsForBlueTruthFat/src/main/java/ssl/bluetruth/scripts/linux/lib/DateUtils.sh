 #
 # THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 # ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 # TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 # PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 # SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 # SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 # LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 # USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 # AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 # LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 # ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 # POSSIBILITY OF SUCH DAMAGE.
 #
 # Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 #
 # Created on 14th July 2015
 #
 # Author: josetrujillo-brenes


# Return current date in UTC
nowInUtc() {
	echo $(date --utc);
}

# Returns seconds since epoch in hex of a given date
# $1 = Date
# Example:
#	secondsInHexOf "$(nowInUtc)"
secondsInHexOf() {
        echo $(printf '%X' $(date +"%s" --date "$1" --utc));
}

# Returns the given date minus the given seconds
# $1 = Seconds
# $2 = Date
# Example:
#	nowMinus1Minute=$(dateBefore 60 "$(nowInUtc)");
dateBefore() {
	echo $(date --date "$2-$1 seconds" --utc);
}

# Returns the given date plus the given seconds in UTC
# $1 = Seconds
# Example:
#	nowPlus1Minute=$(dateAfterNow "$(secondsOf 0 0 60)");
dateAfterNow() {
        echo $(date -u --date="$1 seconds");
}

# Gives a date the specify format or the default "%Y-%m-%d %H:%M:%S" if no format is specify
# $1 = Date
# $2 = Format (Optional)
# Examples:
#	$(dateInFormat "$(nowInUtc)" "%Y-%m-%d") = 2015-07-14
#	$(dateInFormat "$(nowInUtc)") = 2015-07-14 12:56:29
dateInFormat() {
	if [[ $2 ]]; then
		echo $(date +"$2" --date "$1");
	else		
		echo $(date +"%Y-%m-%d %H:%M:%S" --date "$1");
	fi		
}

# Translates hours minutes and seconds into seconds
# When called with 1 parameter:
# $1 = Seconds
# When called with 2 parameter:
# $1 = Minutes
# $2 = Seconds
# When called with 3 parameter:
# $1 = Hours
# $2 = Minutes
# $3 = Seconds
# Examples:
#	$(secondsOf 1) = 1
#	$(secondsOf 1 0) = 60
#	$(secondsOf 1 1 1) = 3661
#	$(secondsOf 0 1 1000) = 1060
secondsOf() { 
	if [[ $3 ]]; then
		echo $(($1 * 3600 + $(secondsOf $2 $3)));
	elif [[ $2 ]]; then
		echo $(($1 * 60 + $2));
	elif [[ $1 ]]; then
		echo $1;
	else
		echo 0;
	fi
}
