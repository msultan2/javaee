/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
  * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
  * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
  * 
 */
package com.ssl.rmas.utils;

public enum ActivityEnum {
    
    STATIC_DATA ("staticData"),
    RESET_DEVICE ("resetDevice"),
    VERIFY("verify"),
    UPDATE_KEY ("updateKey"),
    DOWNLOAD_LOGS ("downloadLogs"),
    UPLOAD_FIRMWARE("uploadFirmware"),
    UPGRADE_FIRMWARE ("upgradeFirmware"),
    REMOVE_OLD_SSH_PUBLIC_KEYS("removeOldSshPublicKeys"),
    DOWNGRADE_FIRMWARE ("downgradeFirmware");   
    
    private final String activity;
    
    private ActivityEnum(String activity){
        this.activity = activity;
    }
    
    @Override
    public String toString(){
        return this.activity;
    }  
}
