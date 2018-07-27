package cc.lotuscard.model;


import android.text.TextUtils;

import com.jaydenxiao.common.baserx.RxSchedulers;

import com.jaydenxiao.common.commonutils.LogUtils;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cc.lotuscard.api.Api;
import cc.lotuscard.api.ApiConstants;
import cc.lotuscard.api.ApiService;
import cc.lotuscard.api.HostType;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.PartsData;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;

import cc.lotuscard.utils.exception.ApiException;
import cc.lotuscard.utils.exception.TimeoutException;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;


/**
 * Created by Administrator on 2018/3/28 0028.
 */
//   jsonarray  to  javabean
//        QualityData qualityData1 = new QualityData();
//        List<QualityData.Parts> partses = new ArrayList<QualityData.Parts>();
//        Gson gson = new Gson();
//        partses = gson.fromJson(jsonArray.toString(), new TypeToken<List<QualityData.Parts>>() {
//        }.getType());
//        qualityData1.setParts(partses);
//        return qualityData1;
public class QualityModel implements QualityContract.Model {
    private RxBleClient rxBleClient = AppApplication.getRxBleClient(AppApplication.getAppContext());

    @Override
    public Observable<PartsData> getQualityDataWithCard(String num) {
        return Api.getDefault(HostType.QUALITY_DATA_NEW)
                .getQualityWithCard(num)
                .map(new Api.HttpResponseFunc<>())
                .compose(RxSchedulers.io_main());
    }

    @Override
    public Observable<PartsData> getQualityData(String num) {
        //api2
//        return retrofit.create(ApiService.class)
//                .getQuality("5ae05f9df93bfb047018e653")
//                .map(new HttpHelper.HttpResponseFunc<>())
//                .compose(RxSchedulers.<QualityData>io_main()
//                );

        return Api.getDefault(HostType.QUALITY_DATA_NEW)
                .getQuality(num)
                .map(new Api.HttpResponseFunc<>())
                .compose(RxSchedulers.io_main()
                );
    }

    @Override
    public Observable<PartsData> getQualityDataWithQRCode(String QRCode) {
        return Api.getDefault(HostType.QUALITY_DATA_NEW)
                .getQualityWithQRCode(QRCode)
                .map(new Api.HttpResponseFunc<>())
                .compose(RxSchedulers.io_main()
                );
    }

    @Override
    public Observable<HttpResponse<ArrayList<QualityData.Parts>>> getQualitySampleData(String id) {
        return Api.getDefault(HostType.QUALITY_DATA)
                .getQualitySample(id)
                .compose(RxSchedulers.io_main()
                );
    }

    @Override
    public Observable<ScanResult> getBleDeviceData() {
        return rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)//此段代码会导致部分设备找不打对应的RxBleDeviceServices,模式一定要对
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder().build())
                .filter(s -> !TextUtils.isEmpty(s.getBleDevice().getName()))
                .compose(RxSchedulers.<ScanResult>io_main());
    }

    @Override
    public Maybe<RxBleDeviceServices> chooseDeviceConnect(String macAddress) {
        return rxBleClient.getBleDevice(macAddress)
                .establishConnection(false) //autoConnect flag布尔值：是否直接连接到远程设备（false）或在远程设备变为可用时立即自动连接
                .flatMapSingle(RxBleConnection::discoverServices)
                .firstElement() // Disconnect automatically after discovery
                .compose(RxSchedulers.io_main_maybe());
    }

}
