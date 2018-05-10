package cc.lotuscard.presenter;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.jaydenxiao.common.baserx.RxSubscriber;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;

import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityPresenter extends QualityContract.Presenter {
    @Override
    public void getQualityDataRequest(String id) {

        mRxManage.add(mModel.getQualityData(id)
                .subscribeWith(new RxSubscriber<QualityData>(mContext, true) {
                    @Override
                    protected void _onNext(QualityData qualityData) {
                        mView.returnGetQualityData(qualityData);
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
