package cc.lotuscard.model;

import com.alibaba.fastjson.JSONObject;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.UUID;

import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.CheckContract;
import rx.Observable;

/**
 * Created by Administrator on 2018/4/4 0004.
 */

public class CheckModel implements CheckContract.Model {
    private RxBleClient rxBleClient = AppApplication.getRxBleClient(AppApplication.getAppContext());
    String macAddress = SPUtils.getSharedStringData(AppApplication.getAppContext(), AppConstant.MAC_ADDRESS);
    @Override
    public Observable<byte[]> startMeasure(UUID characteristicUUID) {
        return rxBleClient.getBleDevice(macAddress)
                .establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .compose(RxSchedulers.<byte[]>io_main());
    }

    @Override
    public Observable<JSONObject> upLoadAfterChecked(QualityData qualityData) {
        return null;
    }
}
