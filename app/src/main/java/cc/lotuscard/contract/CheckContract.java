package cc.lotuscard.contract;

import com.alibaba.fastjson.JSONObject;
import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.List;
import java.util.UUID;

import cc.lotuscard.bean.QualityData;
import cc.lotuscard.bean.RetQuality;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * Created by Administrator on 2018/4/4 0004.
 */

public interface CheckContract {
    interface Model extends BaseModel {
        Observable<byte[]> startMeasure(UUID characteristicUUID);

        Observable<RetQuality> upLoadAfterChecked(Object[][] qualityDataList);

        Observable<RxBleConnection.RxBleConnectionState> checkBleConnectState();

        Observable<byte[]> acceptWeightData(UUID characteristicUUID);

        Maybe<byte[]> settingWeightConfigure(UUID characteristicUUID, byte[] data);
    }

    interface View extends BaseView {
        void returnStartMeasure(Float length, Float angle, int battery);

        void returnupLoadAfterChecked(RetQuality retQuality);

        void returnCheckBleConnectState(RxBleConnection.RxBleConnectionState connectionState);

        void returnAcceptWeightData(byte[] bytes);

        void returnSettingWeightConfigure(byte[] bytes);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void startMeasureRequest(UUID characteristicUUID);

        public abstract void upLoadAfterCheckedRequest(Object[][] qualityDataList);

        public abstract void checkBleConnectStateRequest();

        public abstract void AcceptWeightDataRequest(UUID characteristicUUID);

        public abstract void settingWeightConfigureRequest(UUID characteristicUUID, byte[] data);
    }

}
