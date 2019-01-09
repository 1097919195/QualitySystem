package cc.lotuscard.presenter;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.jaydenxiao.common.baserx.RxSubscriber;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.ArrayList;

import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.PartsData;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityPresenter extends QualityContract.Presenter {
    @Override
    public void getQualityDataWithCardRequest(String num) {
        mRxManage.add(mModel.getQualityDataWithCard(num).subscribeWith(new RxSubscriber<PartsData>(mContext, true) {
            @Override
            protected void _onNext(PartsData partsData) {
                mView.returnGetQualityDataWithCard(partsData);
            }

            @Override
            protected void _onError(String message) {
                mView.showErrorTip(message);
            }
        }));
    }

    @Override
    public void getQualityDataRequest(String num) {
        mRxManage.add(mModel.getQualityData(num)
                .subscribeWith(new RxSubscriber<PartsData>(mContext, true) {
                    @Override
                    protected void _onNext(PartsData qualityData) {
                        mView.returnGetQualityData(qualityData);
                    }

                    @Override
                    protected void _onError(String message) {
                        mView.showErrorTip("clear_editText");
                        mView.showErrorTip(message);
                    }
                }));
    }

    @Override
    public void getQualityDataRequestWithQRCode(String QRCode) {
        mRxManage.add(mModel.getQualityDataWithQRCode(QRCode)
                .subscribeWith(new RxSubscriber<PartsData>(mContext, true) {
                    @Override
                    protected void _onNext(PartsData qualityData) {
                        mView.returnGetQualityDataWithQRCode(qualityData);
                    }

                    @Override
                    protected void _onError(String message) {
                        mView.showErrorTip(message);
                    }
                }));
    }

    @Override
    public void getQualitySampleDataRequest(String id) {
        mRxManage.add(mModel.getQualitySampleData(id)
                .subscribeWith(new RxSubscriber<HttpResponse<ArrayList<QualityData.Parts>>>(mContext, true) {
                    @Override
                    protected void _onNext(HttpResponse<ArrayList<QualityData.Parts>> qualityData) {
                        mView.returnGetQualitySampleData(qualityData);
                    }

                    @Override
                    protected void _onError(String message) {
                        mView.showErrorTip(message);
                    }
                }));
    }

    @Override
    public void getBleDeviceDataRequest() {
        mRxManage.add(mModel.getBleDeviceData()
                .subscribeWith(new RxSubscriber<ScanResult>(mContext, false) {
                    @Override
                    protected void _onNext(ScanResult scanResult) {
                        mView.returnGetBleDeviceData(scanResult);
                    }

                    @Override
                    protected void _onError(String message) {
//                mView.showErrorTip(message);
                    }
                }));

    }

    @Override
    public void chooseDeviceConnectRequest(String macAddress) {
        mRxManage.add(mModel.chooseDeviceConnect(macAddress)
                .doOnSubscribe(disposable->
                        mView.showLoading("chooseConnect"))
                .subscribe(deviceServices -> {
                    mView.returnChooseDeviceConnectWithSetUuidAndMacAddress(deviceServices,macAddress);
                },e -> {mView.showErrorTip("connectFail");LogUtils.loge(e.getCause().toString());}));

    }

}
