package ru.techpark.agregator.localdata;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import ru.techpark.agregator.event.Date;
import ru.techpark.agregator.event.Event;
import ru.techpark.agregator.event.Image;
import ru.techpark.agregator.event.Location;
import ru.techpark.agregator.event.Place;


@Entity(tableName = "event_table", indices = {@Index(value = {"title", "description"}, unique = true)})      // таблица всех ивентов
public class EventTable {

    @PrimaryKey(autoGenerate = true)
    public int id;     // id каждой записи (увеличивается автоматически)

    public String title;   // название
    public String description;     // краткое описание
    public String body_text;       // подробное описание
    public String price;       // цена
    @TypeConverters({ImageConverter.class})
    public List<Image> images;     // изображения (адрес string)
    @TypeConverters({LocationConverter.class})
    public Location location;
    @TypeConverters({DateConverter.class})
    public List<Date> dates;       // дата события
    @TypeConverters({PlaceConverter.class})
    public Place place;

    public EventTable() {
    }

    public EventTable(Event event) {
        this.dates = event.getDates();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.body_text = event.getBody_text();
        this.price = event.getPrice();
        this.images = event.getImages();
        this.location = event.getLocation();
        this.place = event.getPlace();
    }

    public List<Image> getImages() {
        return images;
    }

    public List<Date> getDates() {
        return dates;
    }
}
