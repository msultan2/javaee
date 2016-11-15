/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ssl.utils.datatable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nthompson
 */
public class StringUtils {
    public static String join(Collection<? extends Object> collection, String join) {
        StringBuilder sb = new StringBuilder();
        Iterator<? extends Object> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (sb.length() != 0) {
                sb.append(join);
            }
            sb.append(String.valueOf(iterator.next()));
        }
        return sb.toString();
    }

    public static Map<String, List<String>> convertRequestParams(Map requestParams) {
        Map<String, List<String>> retVal = new HashMap<String, List<String>>();

        Iterator i = requestParams.entrySet().iterator();
        while(i.hasNext()){
            Map.Entry pair = (Map.Entry) i.next();
            String[] s = (String[]) pair.getValue();
            retVal.put((String)pair.getKey(), Arrays.asList(s));
        }

        return retVal;
    }
}
