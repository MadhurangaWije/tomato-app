package io.kavith.tomato_app;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TomatoAPIResponse {
    @SerializedName("disease_name")
    @Expose
    public String disease_name;

    public TomatoAPIResponse(String disease_name) {
        this.disease_name = disease_name;
    }

    public String getDisease_name() {
        return disease_name;
    }

    public void setDisease_name(String disease_name) {
        this.disease_name = disease_name;
    }
}
