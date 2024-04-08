package de.dennisguse.opentracks.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class WeatherInformation implements Parcelable {

    private Track.Id id;
    private double temperature;

//    public WeatherInfo(double temperature) {
//        this.temperature = temperature;
//    }

//    protected WeatherInfo(Parcel in) {
//        temperature = in.readDouble();
//    }

//    public static final Creator<WeatherInfo> CREATOR = new Creator<WeatherInfo>() {
//        @Override
//        public WeatherInfo createFromParcel(Parcel in) {
//            return new WeatherInfo(in);
//        }
//
//        @Override
//        public WeatherInfo[] newArray(int size) {
//            return new WeatherInfo[size];
//        }
//    };

    protected WeatherInformation(Parcel in) {
        id = in.readParcelable(Track.Id.class.getClassLoader());
        temperature = in.readDouble();
    }

    public static final Creator<WeatherInformation> CREATOR = new Creator<WeatherInformation>() {
        @Override
        public WeatherInformation createFromParcel(Parcel in) {
            return new WeatherInformation(in);
        }

        @Override
        public WeatherInformation[] newArray(int size) {
            return new WeatherInformation[size];
        }
    };

    @Nullable
    public Track.Id getId() {
        return id;
    }

    public void setId(Track.Id id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @NonNull
    @Override
    public String toString() {
        return "WeatherInfo{" +
                "temperature=" + temperature +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(temperature);
    }
}