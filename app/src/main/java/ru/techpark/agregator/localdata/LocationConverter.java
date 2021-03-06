package ru.techpark.agregator.localdata;

import androidx.room.TypeConverter;
import ru.techpark.agregator.event.Location;

class LocationConverter {

    @TypeConverter
    public String fromLocation(Location location){
        if (location == null) return "";
        return location.getSlug()+ "," + location.getName() + "," + location.getTimezone();
    }

    @TypeConverter
    public Location toLocation(String line){
        if(line.length() == 0) return new Location("", "", "");
        String[] arr = line.split(",");
        return new Location(arr[0], arr[1], arr[2]);
    }

}
