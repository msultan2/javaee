/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.csv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ianp
 */
public class ScrollingCSVInputStream extends InputStream {

    private byte[] currentLineBuffer = null;
    private int currLinePos = 0;
    private final Iterator<? extends Object[]> tableResults;
    private final String[] formatStrings;
    private final Class<? extends Enum>[] enumTypes;

    public ScrollingCSVInputStream(List<String> columnNames, Iterator<? extends Object[]> tableResults) {
        this.tableResults = tableResults;
        final StringBuilder line = new StringBuilder();
        String prefix = "";
        
        formatStrings = new String[columnNames.size()];
        enumTypes = new Class[columnNames.size()];
        int arrayIndex = 0;
        for (String s : columnNames) {
            line.append(prefix);
            prefix = ",";
            line.append(s);
            if (s.contains("(octal)")) {
                formatStrings[arrayIndex++] = "%03o";
            } else if (s.contains("(hex)")) {
                formatStrings[arrayIndex++] = "%02X";
            } else {
                formatStrings[arrayIndex++] = "%s";
            }
        }
        line.append("\r\n");
        currentLineBuffer = line.toString().getBytes();
    }
    
    public void setColumnEnumType(int column, Class<? extends Enum> type) {
        if (column >= 0 && column < enumTypes.length) {
            enumTypes[column] = type;
        }
    }

    @Override
    public int read() throws IOException {
        int result = -1;

        if (currLinePos >= currentLineBuffer.length) {
            currLinePos = 0;
            String prefix = "";
            final StringBuilder line = new StringBuilder();
            if(tableResults.hasNext()) {
                Object[] in = tableResults.next();
                for(int index = 0; index < in.length; index++) {
                    Object o = in[index];
                    line.append(prefix);
                    prefix = ",";
                    if(o != null) {
                        Enum selectedEnumValue = null;
                        if (enumTypes[index] != null) {
                            Class<? extends Enum> enumType = enumTypes[index];
                            String objectAsString = String.valueOf(o);
                            
                            try {
                                int objectAsNumber = Integer.parseInt(objectAsString);
                                Enum[] enumConstants = enumType.getEnumConstants();
                                if (objectAsNumber >= 0 && objectAsNumber < enumConstants.length) {
                                    selectedEnumValue = enumConstants[objectAsNumber];
                                }
                            } catch (NumberFormatException numberFormatException) {
                                //Not a number. Try as an enum name
                                try {
                                    selectedEnumValue = Enum.valueOf(enumType, objectAsString);
                                } catch (Exception e) {
                                    //Not a name either. fall back to default processing.
                                }
                            }
                        }
                        
                        if (selectedEnumValue != null) {
                            line.append(selectedEnumValue.toString());
                        } else {
                            line.append(String.format(formatStrings[index], o));
                        }
                    }
                }
                line.append("\r\n");
                currentLineBuffer = line.toString().getBytes();
            } else {
                currentLineBuffer = new byte[0];
                return -1;
            }
        }

        result = currentLineBuffer[currLinePos];
        currLinePos++;
        
        return result;
    }
}
