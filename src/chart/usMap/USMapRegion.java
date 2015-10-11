/*
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package chart.usMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlTransient;

public class USMapRegion implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer regionId;
    private String name;
    private short international;
    private int startZone;
    private int endZone;

    public USMapRegion() {    }

    public USMapRegion(Integer regionId) {        this.regionId = regionId;    }

    public USMapRegion(Integer regionId, short international, int startZone, int endZone) {
        this.regionId = regionId;
        this.international = international;
        this.startZone = startZone;
        this.endZone = endZone;
    }

    public static final ObservableList<String> americanRegions = FXCollections.observableArrayList();
    static public void initRegions()
    {
    	americanRegions.addAll("All", "Northeast", "Mid-Atlantic", "South", "Mid-West", "Ark-La-Tex", "Southwest", "West", "Territories");
    }

    public Integer getRegionId() 				{       return regionId;    }
    public void setRegionId(Integer regionId) 	{        this.regionId = regionId;    }

    public String getName() 					{        return name;    }
    public void setName(String name) 			{        this.name = name;    }

    public short getInternational() 			{        return international;  }
    public void setInternational(short international) {  this.international = international;   }

    public int getStartZone() 					{        return startZone;    }
    public void setStartZone(int startZone) 	{        this.startZone = startZone;    }

    public int getEndZone() 					{        return endZone;    }
    public void setEndZone(int endZone) 		{        this.endZone = endZone;    }


    @Override    public int hashCode() {    	return (regionId != null ? regionId.hashCode() : 0);    }

    @Override    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof USMapRegion))             return false;
        USMapRegion other = (USMapRegion) object;
        if ((this.regionId == null && other.regionId != null) || (this.regionId != null && !this.regionId.equals(other.regionId))) 
            return false;
        return true;
    }

    @Override    public String toString() {        return name;    }
    private static final String[] Northeast = new String[]{ "DE", "MA", "RI", "NH", "ME", "VT", "CT", "NY", "NJ", "PA" };
    private static final String[] MidAtlantic = new String[]{ "DC", "VA", "MD", "WV", "NC", "SC" };
    private static final String[] South = new String[]{ "GA", "TN", "FL", "AL", "MS" };
    private static final String[] MidWest = new String[]{ "KY", "OH", "IN", "MI", "IA", "IL", "WI", "MN", "SD", "ND", "MT", "MO", "KS", "NE" };
    private static final String[] ArkLaTex = new String[]{ "LA", "AR", "OK", "TX" };
    private static final String[] Southwest = new String[]{ "CO", "WY", "ID", "UT", "AZ", "NM", "NV" };
    private static final String[] West = new String[]{ "CA", "HI", "AS", "OR", "WA" };
    private static final String[] Territories = new String[]{ "PR", "VI" };
    public static final String[] ALL_STATES = new String[]{ "DE", "MA", "RI", "NH", "ME", "VT", "CT", "NY", "NJ", "PA" , "PR", "VI", "DC", "VA", "MD", "WV", "NC", "SC", "GA", "TN", "FL", "AL", "MS", "KY", "OH", "IN", "MI", "IA", "IL", "WI", "MN", "SD", "ND", "MT", "MO", "KS", "NE", "LA", "AR", "OK", "TX","CO", "WY", "ID", "UT", "AZ", "NM", "NV","CA", "HI", "AS", "OR", "WA" };
    private static final Map<String,String> stateToRegionMap = new HashMap<String,String>();
    static {
        // sort them so we can use binary search later
        Arrays.sort(Northeast);
        Arrays.sort(MidAtlantic);
        Arrays.sort(South);
        Arrays.sort(MidWest);
        Arrays.sort(ArkLaTex);
        Arrays.sort(Southwest);
        Arrays.sort(West);
        Arrays.sort(Territories);
        Arrays.sort(ALL_STATES);
        // populate map
        for (String state: Northeast) stateToRegionMap.put(state, "Northeast");
        for (String state: MidAtlantic) stateToRegionMap.put(state, "Mid-Atlantic");
        for (String state: South) stateToRegionMap.put(state, "South");
        for (String state: MidWest) stateToRegionMap.put(state, "Mid-West");
        for (String state: ArkLaTex) stateToRegionMap.put(state, "Ark-La-Tex");
        for (String state: Southwest) stateToRegionMap.put(state, "Southwest");
        for (String state: West) stateToRegionMap.put(state, "West");
        for (String state: Territories) stateToRegionMap.put(state, "Territories");
    }
    
    public static String getRegionName(String state) {
        return stateToRegionMap.get(state);
    }
    
    /**
     * Get the list of states in this region if its a US region or null if international
     * 
     * @return array of state two char names
     */
    public String[] computeStates() {
       String name = getName();
       if ("Northeast".equals(name)) 		return Northeast;
        if ("Mid-Atlantic".equals(name))  	return  MidAtlantic;
        if ("South".equals(name))  			return South;
        if ("Mid-West".equals(name))  		return MidWest;
        if ("Ark-La-Tex".equals(name))  	return ArkLaTex;
        if ("Southwest".equals(name))  		return Southwest;
        if ("West".equals(name))   			return West;
        if ("Territories".equals(name))   	return Territories;
        return null;
    }
}
