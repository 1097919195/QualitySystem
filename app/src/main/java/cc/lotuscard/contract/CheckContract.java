package cc.lotuscard.contract;

import com.alibaba.fastjson.JSONObject;
import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.UUID;

import cc.lotuscard.bean.QualityData;
import io.reactivex.Observable;


/**
 * Created by Administrator on 2018/4/4 0004.
 */

public interface CheckContract {
    interface Model extends BaseModel {
        Observable<byte[]> startMeasure(UUID characteristicUUID);

        Observable<JSONObject> upLoadAfterChecked(QualityData qualityData);

        Observable<RxBleConnection.RxBleConnectionState> checkBleConnectState();
    }

    interface View extends BaseView {

        void returnupLoadAfterChecked(JSONObject jsonObject);

        void returnStartMeasure(Float length, Float angle, int battery);

        void returnCheckBleConnectState(RxBleConnection.RxBleConnectionState connectionState);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void startMeasureRequest(UUID characteristicUUID);

        public abstract void upLoadAfterCheckedRequest(QualityData qualityData);

        public abstract void checkBleConnectStateRequest();
    }

}
