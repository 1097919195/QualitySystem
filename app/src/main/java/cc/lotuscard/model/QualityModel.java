package cc.lotuscard.model;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

import cc.lotuscard.api.Api;
import cc.lotuscard.api.HostType;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.bean.BleDevice;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

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
    public Observable<RxBleDeviceServices> chooseDeviceConnect(String mac) {
        return rxBleClient.getBleDevice(mac)
                .establishConnection(false)
                .flatMap(RxBleConnection::discoverServices)
                .first() // Disconnect automatically after discovery
                .compose(RxSchedulers.<RxBleDeviceServices>io_main());
    }
}
