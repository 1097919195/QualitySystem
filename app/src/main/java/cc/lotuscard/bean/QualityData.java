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

    private String _id;
    private String category;
    private String type;
    private ArrayList<Parts> parts;

    protected QualityData(Parcel in) {
        _id = in.readString();
        category = in.readString();
        type = in.readString();
    }

    public static final Creator<QualityData> CREATOR = new Creator<QualityData>() {
        @Override
        public QualityData createFromParcel(Parcel in) {
            return new QualityData(in);
        }

        @Override
        public QualityData[] newArray(int size) {
            return new QualityData[size];
        }
    };

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
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

    public ArrayList<Parts> getParts() {
        return parts;
    }

    public void setParts(ArrayList<Parts> parts) {
        this.parts = parts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(category);
        dest.writeString(type);
    }

    public static class Parts implements Parcelable {
        /**
         * name : 林秀兰
         * value : 28
         */

        private String name;
        private float value;
        private float actValue;

        private boolean isSelected;

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getOriValue() {
            return value;
        }

        public void setOriValue(float oriValue) {
            this.value = oriValue;
        }

        public float getActValue() {
            return actValue;
        }

        public void setActValue(float actValue) {
            this.actValue = actValue;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeFloat(this.value);
            dest.writeFloat(this.actValue);
        }

        public Parts() {
        }

        protected Parts(Parcel in) {
            this.name = in.readString();
            this.value = in.readFloat();
            this.actValue = in.readFloat();
        }

        public static final Creator<Parts> CREATOR = new Creator<Parts>() {
            @Override
            public Parts createFromParcel(Parcel source) {
                return new Parts(source);
            }

            @Override
            public Parts[] newArray(int size) {
                return new Parts[size];
            }
        };
    }

}
