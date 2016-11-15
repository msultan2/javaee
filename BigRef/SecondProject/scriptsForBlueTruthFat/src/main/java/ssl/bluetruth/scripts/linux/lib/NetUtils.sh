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

# Sends a http request 
# $1 = Servlet with parameters
# $2 = Post data (optional)
# Example:
#       sendHttpRequest "localhost:8080/BlueTruthReceiver2/Statistics" "Detector2,10,E,DevBadRep1:0:10:FFFFFFFFFFFFFFFB:FFFFFFFFFFFFFFF5,DevBadRep2:0:A:0:0,0"

sendHttpRequest() {
        servletWithParameters=$1
        postData=$2
        if [[ $postData ]]; then
		wget --post-data $postData -q $servletWithParameters --delete-after;
                #echo $servletWithParameters "{" $postData "}"
                echo "wget --post-data "$postData" -q "$servletWithParameters" --delete-after";
	else
		wget -q "$servletWithParameters" --delete-after;
                #echo "$servletWithParameters";
                echo "wget -q "$servletWithParameters" --delete-after";
	fi
}

inStation() {
#    williamsInStation="10.163.49.151:80/"
    williamsInStation="localhost:8080/"
    inStation=$williamsInStation;
    echo $inStation;
}
