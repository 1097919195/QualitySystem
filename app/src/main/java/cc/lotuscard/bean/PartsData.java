package cc.lotuscard.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/7/17 0017.
 */

public class PartsData implements Parcelable {

    /**
     * _id : 5b4d8a769134ca387b586df2
     * name : 订单
     * num : 123654
     * category : 西服
     * type : 1
     * ban_xing : null
     * inventory : 123
     * apparel_info : [{"name":"胸围","value":12},{"name":"脚踝","value":32},{"name":"臂长","value":21},{"name":"肩宽","value":26}]
     */

    private String _id;
    private String name;
    private String num;
    private String category;
    private int type;
    private int inventory;//库存
    private ArrayList<ApparelInfoBean> apparel_info;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public ArrayList<ApparelInfoBean> getApparel_info() {
        return apparel_info;
    }

    public void setApparel_info(ArrayList<ApparelInfoBean> apparel_info) {
        this.apparel_info = apparel_info;
    }

    public static class ApparelInfoBean implements Parcelable {
        /**
         * name : 胸围
         * value : 12
         */

        private String name;
        private String value;
        private float actValue;
        private boolean isSelected;

        public float getActValue() {
            return actValue;
        }

        public void setActValue(float actValue) {
            this.actValue = actValue;
        }

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

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public ApparelInfoBean() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.value);
            dest.writeFloat(this.actValue);
            dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        }

        protected ApparelInfoBean(Parcel in) {
            this.name = in.readString();
            this.value = in.readString();
            this.actValue = in.readFloat();
            this.isSelected = in.readByte() != 0;
        }

        public static final Creator<ApparelInfoBean> CREATOR = new Creator<ApparelInfoBean>() {
            @Override
            public ApparelInfoBean createFromParcel(Parcel source) {
                return new ApparelInfoBean(source);
            }

            @Override
            public ApparelInfoBean[] newArray(int size) {
                return new ApparelInfoBean[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeString(this.name);
        dest.writeString(this.num);
        dest.writeString(this.category);
        dest.writeInt(this.type);
        dest.writeInt(this.inventory);
        dest.writeList(this.apparel_info);
    }

    public PartsData() {
    }

    protected PartsData(Parcel in) {
        this._id = in.readString();
        this.name = in.readString();
        this.num = in.readString();
        this.category = in.readString();
        this.type = in.readInt();
        this.inventory = in.readInt();
        this.apparel_info = new ArrayList<ApparelInfoBean>();
        in.readList(this.apparel_info, ApparelInfoBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<PartsData> CREATOR = new Parcelable.Creator<PartsData>() {
        @Override
        public PartsData createFromParcel(Parcel source) {
            return new PartsData(source);
        }

        @Override
        public PartsData[] newArray(int size) {
            return new PartsData[size];
        }
    };
}
