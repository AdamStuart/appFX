/*
 * Copyright 2014 Lynden, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package container.gmapsfx.javascript.object;

import container.gmapsfx.javascript.JavascriptObject;

/**
 *
 * @author robt
 */
public class MapOptions extends JavascriptObject {
    
    protected LatLong center;
    protected MapTypeIdEnum mapType;
    protected boolean mapMarker;
    protected boolean overviewMapControl;
    protected boolean panControl;
    protected boolean rotateControl;
    protected boolean scaleControl;
    protected boolean streetViewControl;
    protected int zoom;
    protected boolean zoomControl;
    protected boolean mapTypeControl;
    

    public MapOptions() {
        super(GMapObjectType.OBJECT);
    }
    
    public MapOptions center( LatLong c ) {
        setProperty("center", c);
        center = c;
        return this;
    }
    
    public MapOptions mapMarker( boolean m ) {
        setProperty("mapMarker", m );
        mapMarker = m;
        return this;
    }
    
    public MapOptions mapType( MapTypeIdEnum c ) {
        setProperty( "mapTypeId", c );
        mapType = c;
        return this;
    }
    
    public MapOptions overviewMapControl( boolean c ) {
        setProperty("overviewMapControl", c );
        overviewMapControl = c;
        return this;
    }
    
    public MapOptions panControl( boolean panControl ) {
        setProperty( "panControl", panControl);
        panControl = panControl;
        return this;
    }
    
    public MapOptions rotateControl( boolean c ) {
        rotateControl = c;
       setProperty( "rotateControl", rotateControl );
        return this;
    }
    
    public MapOptions scaleControl( boolean c ) {
        scaleControl = c;
        setProperty( "scaleControl", c );
        return this;
    }
    
    public MapOptions streetViewControl( boolean c ) {
        streetViewControl = c;
        setProperty( "streetViewControl", c );
        return this;
    }
    
    public MapOptions zoom( int z ) {
        setProperty( "zoom", z );
        this.zoom = z;
        return this;
    }
    
    public MapOptions zoomControl( boolean c ) {
        setProperty( "zoomControl", c );
        zoomControl = c;
        return this;
    }
    
    public MapOptions mapTypeControl( boolean c ) {
        setProperty( "mapTypeControl", c);
        mapTypeControl = c;
        return this;
    }
    
}
