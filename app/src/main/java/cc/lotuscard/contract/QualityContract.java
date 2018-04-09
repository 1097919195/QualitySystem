package cc.lotuscard.contract;

import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.List;

import cc.lotuscard.bean.BleDevice;
import cc.lotuscard.bean.QualityData;
import rx.Observable;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public interface QualityContract {
    interface Model extends BaseModel {
        Observable<QualityData> getQualityData(String id);

        Observable<ScanResult> getBleDeviceData();

        Observable<RxBleDeviceServices> chooseDeviceConnect(String mac);
    }

    interface View extends BaseView {
        void returnGetQualityData(QualityData qualityData);

        void returnGetBleDeviceData(ScanResult scanResult);

        void returnChooseDeviceConnectWithSetUuid(RxBleDeviceServices rxBleConnection);
        void returnChooseDeviceConnectWithSetAddress(String mac);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void getQualityDataRequest(String id);

        public abstract void getBleDeviceDataRequest();

        public abstract void chooseDeviceConnectRequest(String mac);
    }

}
