/*
* Copyright (C) 2010- Peer internet solutions
*
* This file is part of mixare.
*
* This program is free software: you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see <http://www.gnu.org/licenses/>
*/

package org.mixare.data;

import java.util.ArrayList;

import net.peterd.zombierun.util.GRSConverter;

import org.mixare.DownloadManager;
import org.mixare.MixContext;
import org.mixare.MixView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.res.Resources;
import android.content.res.TypedArray;


/**
 * The XML Parser
 * @author hannes
 */
public class XMLHandler extends DataHandler {

	public static final int MAX_POI = 200;

	public XMLHandler(MixContext ctx, DownloadManager downloadManager) {
		this.ctx = ctx;
		
		this.downloadManager = downloadManager;
		
		poiImagesList = ctx.getPoiImagesList();
		categoryIndexMap = ctx.getCategoryIndexMap();
		
		categoryHash = ctx.getCategoryHash();
		Resources resources = ctx.getMixView().getResources();
		String category[] = resources.getStringArray(edu.fsu.cs.argame.R.array.category);
		TypedArray drawables = resources.obtainTypedArray(edu.fsu.cs.argame.R.array.category_drawable);
		TypedArray drawablesFocus = resources.obtainTypedArray(edu.fsu.cs.argame.R.array.category_drawable_focus);
		TypedArray drawablesBig = resources.obtainTypedArray(edu.fsu.cs.argame.R.array.category_drawable_big);
		
		/** Puts all the id of the category images to the categoryHash**/
		for(int i=0; i<category.length; i++)
			categoryHash.put(category[i], drawables.getResourceId(i, 0)+":"+drawablesFocus.getResourceId(i, 0)+":"+drawablesBig.getResourceId(i, 0));
	}
		
	public void load(Document doc) {
        Element root = doc.getDocumentElement();
        processXML(root);
	}
	
	/**
	 * The actual XML Parser
	 * @param root the element to be parsed
	 */
	private void processXML(Element root) {
		
		/** Gets all elements inside every RESULT tag and adds it as a marker */
        for(int i = 0; !downloadManager.isStop() && MixView.ctx.getDataView().markers.size() < MAX_POI &&
        		i < root.getElementsByTagName("RESULT").getLength(); i++ ) {
        	
        	String title = "";
        	String grsX = "";
        	String grsY = "";
        	String category = "";
        	String id = "";
            String address = "";
            String pnu = "";
            
            /** gets all marker attributes **/
            try {
	            title = root.getElementsByTagName("FCLTS_NM").item(i).getFirstChild().getNodeValue();
	            grsX = root.getElementsByTagName("FCLTY_X").item(i).getFirstChild().getNodeValue();
	            grsY = root.getElementsByTagName("FCLTY_Y").item(i).getFirstChild().getNodeValue();
	            category = root.getElementsByTagName("ICO_URL").item(i).getFirstChild().getNodeValue();
	            id = root.getElementsByTagName("SEARCH_ID").item(i).getFirstChild().getNodeValue();
            } catch(Exception e) {}
            
            /** address and phone number may be empty **/
            try {
                    address = root.getElementsByTagName("FIRST_AD").item(i).getFirstChild().getNodeValue();
            } catch(Exception e) {}
            
            try {
                    pnu = root.getElementsByTagName("FCLTY_TN").item(i).getFirstChild().getNodeValue();
            } catch(Exception e) {}
            
            String[] parsedCat = category.split(".png");
            String[] parsedCat2 = parsedCat[0].split("TL_");
            category = parsedCat2[1];
            
            ArrayList<Double> longLat = new ArrayList<Double>();
            /** converts the obtained grsx and y values to longitude and latitude **/
            longLat = GRSConverter.KTMtoLL(Double.parseDouble(grsX)/100.0, Double.parseDouble(grsY)/100.0); 

            /** creates a marker with the given parsed data **/
            createMarker( title, longLat.get(0), longLat.get(1), 0, "", category, "", address, pnu, id);
            
            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}
	
}
