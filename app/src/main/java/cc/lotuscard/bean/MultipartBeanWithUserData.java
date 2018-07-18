package cc.lotuscard.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/7/18 0018.
 */

public class MultipartBeanWithUserData {
    private List<PartsData.ApparelInfoBean> parts;

    public List<PartsData.ApparelInfoBean> getParts() {
        return parts;
    }

    public void setParts(List<PartsData.ApparelInfoBean> parts) {
        this.parts = parts;
    }

    public MultipartBeanWithUserData(List<PartsData.ApparelInfoBean> parts) {
        this.parts = parts;
    }
}
