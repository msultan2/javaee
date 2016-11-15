/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF TH
 * IS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.7 
 *
 * Created on 27-July-2015 14:00 PM
 *
 */
package ssl.bluetruth.emitter2converter.configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;

/**
 * This class is used in ConfigurationManagerImpl to implement the ConfigurationManager interface. 
 * When a OutStation has a invalid configuration (null value), this implementation will use default values from the database. 
 * @author nchavan
 */
public class DefaultConfigurationFromDataBase implements DefaultConfiguration {
    
    private static final String SQL_SELECT_CONFIGURATION = "SELECT property, value FROM default_configuration";
    private final Logger logger = LogManager.getLogger(getClass());
                
    public DefaultConfigurationFromDataBase() {        
    }
            
    @Override
    public Map<String, String> getMap() throws InvalidConfigurationException {
       return getMapFromDataBase(); 
    }            
    
    private Map<String, String> getMapFromDataBase()  {
        Map<String, String> newMap = new HashMap();
        try (Connection connection = DatabaseManager.getInstance().getDatasource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_CONFIGURATION)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while(resultSet.next()){
                        newMap.put(resultSet.getString("property"), resultSet.getString("value"));
                    }
                }
            }
        } catch (SQLException | NamingException | DatabaseManagerException ex) {
            String message = "Fail to load the configuration from database. Cause: " + ex.getLocalizedMessage();
            logger.error(message, ex);
        }      
        return newMap;
    } 
}
