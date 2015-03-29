# Marker Placement #
## Introduction ##
There are three marker types (categories) in mixare:
  * Normal markers (POIs)
  * Navigation markers (NAVs)
  * Social media markers (BUBs)

## Normal Markers ##
At the moment the navigation marker are positioned by manipulating the vector that displays them on screen. This will be changed by altering the height(altitude) instead, the less we interfere with the locationvectors the better.

## Navigation markers ##
At the moment the navigation marker are positioned by manipulating the vector that displays them on screen. This will be changed by altering the height(altitude) instead, the less we interfere with the locationvectors the better.

## Social media markers ##
The socialmedia markers appears as they are floating in the ski. Their position is calculated from the actual latitude+longitude by setting the altitude to a certain height. This height will be explained in the following paragraphs.

The intended behavior is that the social media marker are never placed below roughly 20 degrees (0.35 radians) from the horizon. The more a social media is far, the bigger will be its altitude. This helps the user to read the closest messages withouth having to point the phone too much high. A status message being post at radius distance (where radius is the maximum range set by the user to display POIs) it will be displayed at roughly 45 degrees (0.85 radians).

Social media markers will span across +20 and +45 degrees above the horizon.

The altitude is therefore made of three components:
```
double altitude = curGPSFix.getAltitude()+Math.sin(0.35)*distance+Math.sin(0.4)*(distance/(MixView.dataView.getRadius()*1000f/distance));
```
  * curGPSFix.getAltitude() -> use current user's altitude, this sets the horizon as baseline
  * Math.sin(0.35)distance -> that's the altitude in meters of 20 degrees at the current marker's distance
  * Math.sin(0.4)(distance/(MixView.dataView.getRadius()1000f/distance)) -> this adds additional height to each marker, the more far one marker is, the higher it will be placed (distance is in meters, Radius in kilometers)