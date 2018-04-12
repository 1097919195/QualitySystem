package cc.lotuscard.model;

import android.bluetooth.BluetoothGattCharacteristic;

import com.jaydenxiao.common.baserx.RxSchedulers;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import cc.lotuscard.api.Api;
import cc.lotuscard.api.HostType;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;

import io.reactivex.Maybe;
import io.reactivex.Observable;


/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityModel implements QualityContract.Model {
    private RxBleClient rxBleClient = AppApplication.getRxBleClient(AppApplication.getAppContext());

    @Override
    public Observable<QualityData> getQualityData(String id) {
        return Api.getDefault(HostType.QUALITY_DATA)
                .getQuality(id)
                //如果不规范的话
//                .map(new Func1<JSONArray, QualityData>() {
//                    @Override
//                    public QualityData call(JSONArray jsonArray) {
//                        QualityData qualityData1 = new QualityData();
//                        List<QualityData.Parts> partses = new ArrayList<QualityData.Parts>();
//                        Gson gson = new Gson();
//                        partses =gson.fromJson(jsonArray.toString(),new TypeToken<List<QualityData.Parts>>() {
//                        }.getType());
//                        qualityData1.setParts(partses);
//                        return qualityData1;
//                    }
//                })
                .compose(RxSchedulers.<QualityData>io_main());
    }

    @Override
    public Observable<ScanResult> getBleDeviceData() {
        return rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder().build()
        ).compose(RxSchedulers.<ScanResult>io_main());
    }

    @Override
    public Maybe<RxBleDeviceServices> chooseDeviceConnect(String mac) {
         return rxBleClient.getBleDevice(mac)
                .establishConnection(false) //autoConnect flag布尔值：是否直接连接到远程设备（false）或在远程设备变为可用时立即自动连接
                .flatMapSingle(RxBleConnection::discoverServices)
                .firstElement() // Disconnect automatically after discovery
                .compose(RxSchedulers.<RxBleDeviceServices>io_main_maybe());
    }

}
