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
 * DatastoreOrderField.java
 * @author katemallichan
 * 
 * Product:
 *
 * Change History: Created on August 13, 2007, 11:31 AM Version 001
 * Class implementing generation of Hibernate filter criteria for Boolean fields
 * 2015-02-27 SCJS 676/01 - Bug Fix to handle SQL conditions.
 *
 */

package com.ssl.utils.datatable;

/**
 *
 * @author katemallichan
 */
public class DatastoreOrderField extends SimpleOrderField {

    public DatastoreOrderField(String fieldName, SortDirection orderBy) {
        super(fieldName, orderBy);
    }
    
    public String getSQLOrderField(String fieldAlias) {
        SortDirection type = getOrderByType();
        String orderDirection = type.toString();

        String hql = fieldAlias + " " + orderDirection;

        return hql;
    }
}
