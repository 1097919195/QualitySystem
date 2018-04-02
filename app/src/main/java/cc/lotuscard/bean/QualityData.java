package cc.lotuscard.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityData implements Parcelable {

    /**
     * id :
     * category :
     * type :
     * parts : [{"name":"林秀兰","value":28},{"name":"段霞","value":29},{"name":"梁秀英","value":30}]
     */

    private String id;
    private String category;
    private String type;
    private List<Parts> parts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Parts> getParts() {
        return parts;
    }

    public void setParts(List<Parts> parts) {
        this.parts = parts;
    }

    public static class Parts {
        /**
         * name : 林秀兰
         * value : 28
         */

        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.category);
        dest.writeString(this.type);
        dest.writeList(this.parts);
    }

    public QualityData() {
    }

    protected QualityData(Parcel in) {
        this.id = in.readString();
        this.category = in.readString();
        this.type = in.readString();
        this.parts = new ArrayList<Parts>();
        in.readList(this.parts, Parts.class.getClassLoader());
    }

    public static final Parcelable.Creator<QualityData> CREATOR = new Parcelable.Creator<QualityData>() {
        @Override
        public QualityData createFromParcel(Parcel source) {
            return new QualityData(source);
        }

        @Override
        public QualityData[] newArray(int size) {
            return new QualityData[size];
        }
    };
}
