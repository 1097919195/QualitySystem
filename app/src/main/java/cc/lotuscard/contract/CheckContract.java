package cc.lotuscard.contract;

import com.alibaba.fastjson.JSONObject;
import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import cc.lotuscard.bean.QualityData;
import rx.Observable;

/**
 * Created by Administrator on 2018/4/4 0004.
 */

public interface CheckContract {
    interface Model extends BaseModel {
        Observable<byte[]> startMeasure(UUID characteristicUUID);

        Observable<JSONObject> upLoadAfterChecked(QualityData qualityData);
    }

    interface View extends BaseView {
        void returnStartMeasure(byte[] bytes);

        void returnupLoadAfterChecked(JSONObject jsonObject);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void startMeasureRequest(UUID characteristicUUID);

        public abstract void upLoadAfterCheckedRequest(QualityData qualityData);
    }

}
