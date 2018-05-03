package cc.lotuscard.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/4/28 0028.
 */

public class MeasureResultSave {
    public MeasureResultSave(List<QualityData.Parts> parts) {
        this.parts = parts;
    }

    private List<QualityData.Parts> parts;

    public List<QualityData.Parts> getParts() {
        return parts;
    }

    public void setParts(List<QualityData.Parts> parts) {
        this.parts = parts;
    }

}
