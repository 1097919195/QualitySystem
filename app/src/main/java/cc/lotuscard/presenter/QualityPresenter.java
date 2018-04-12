package cc.lotuscard.presenter;


import com.jaydenxiao.common.baserx.RxSubscriber;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;

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
                .filter(r -> r.getBleDevice().getName() != null)//过滤名字为空的值
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
    public void chooseDeviceConnectRequest(String mac) {
        mRxManage.add(mModel.chooseDeviceConnect(mac)
                .doOnSubscribe(disposable->
                    mView.showLoading("chooseConnect"))
                .subscribe(services -> {
                    mView.returnChooseDeviceConnectWithSetUuid(services);
                    mView.returnChooseDeviceConnectWithSetAddress(mac);
                },e -> mView.showErrorTip("connectFail")));

    }

}
