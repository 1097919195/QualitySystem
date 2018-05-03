package cc.lotuscard.model;

import com.alibaba.fastjson.JSONObject;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.List;
import java.util.UUID;

import cc.lotuscard.api.Api;
import cc.lotuscard.api.HostType;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.bean.RetQuality;
import cc.lotuscard.contract.CheckContract;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;


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
    public Observable<RetQuality> upLoadAfterChecked(Object[][] qualityDataList) {
        return Api.getDefault(HostType.QUALITY_DATA_TEST)
                .getUpLoadAfterChecked(qualityDataList)
                .compose(RxSchedulers.io_main());
    }

    @Override
    public Observable<RxBleConnection.RxBleConnectionState> checkBleConnectState() {
        return rxBleClient.getBleDevice(macAddress)
                .observeConnectionStateChanges()
                .compose(RxSchedulers.io_main());
    }

    @Override
    public Observable<byte[]> acceptWeightData(UUID characteristicUUID) {
        return rxBleClient.getBleDevice(macAddress)
                .establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(characteristicUUID))
                .compose(RxSchedulers.io_main());
    }

    @Override
    public Maybe<byte[]> settingWeightConfigure(UUID characteristicUUID, byte[] data) {
        return rxBleClient.getBleDevice(macAddress)
                .establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(characteristicUUID, data))
                .firstElement()
                .compose(RxSchedulers.io_main_maybe());
    }
}
