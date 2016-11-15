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
 * Copyright 2002 (C) Simulation Systems Ltd. All Rights Reserved.
 * 
 * EnumFieldFilter.java
 * @author katemallichan
 * 
 * Product:
 *
 * Change History: Created on August 13, 2007, 11:31 AM Version 001
 * Class implementing generation of Hibernate filter criteria for Enum's
 * 2015-02-27 SCJS 676/01 - Bug Fix to handle SQL conditions.
 *
 */
package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EnumFieldFilter extends AbstractFieldFilter implements DataTableFieldFilter {

    
    private ArrayList<Enum> allowedConstants = new ArrayList<Enum>();

    /**
     * Constructs an enum filter for a given field with specified allowed
     * values.
     * @param fieldName - the name of the Entity field this filter applies to.
     * @param constantNames - a list of enum constant names;
     */
    @SuppressWarnings("unchecked")
    public EnumFieldFilter(Class<? extends Enum> enumType, String fieldName, String[] constsNames) {
        super(fieldName);
        if (constsNames == null) {
            throw new NullPointerException("At least one enum constant name must be specified.");
        }
        if (enumType == null) {
            throw new NullPointerException("Enum type must not be null.");
        }
        //allowedConstants  = new ArrayList<T>();
        Iterator<String> it = Arrays.asList(constsNames).iterator();
        while (it.hasNext()) {
            
            allowedConstants.add(Enum.valueOf(enumType, it.next()));
        }
    }

    @Override
    public List<String> getSQLWhereStrings() throws IllegalArgumentException {

        String alias = getFieldName();
        
        if(alias==null) {
            throw new IllegalArgumentException("Error: alias for enum field filter HQL String creation must not be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("( ");
        for(Enum val: allowedConstants){
            sb.append(val.name()).append(",");
        }
        sb.append(" )");
        
        String sql = alias + " in " + sb.toString();

        List<String> sqlList = new ArrayList<String>();
        sqlList.add(sql);
        return sqlList;
    }

    public List<Enum> getAllowedConstants() {
        return allowedConstants;
    }
    
}
