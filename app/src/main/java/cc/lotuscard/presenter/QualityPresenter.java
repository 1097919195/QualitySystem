package cc.lotuscard.presenter;


import com.jaydenxiao.common.baserx.RxSubscriber;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.scan.ScanResult;

import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityPresenter extends QualityContract.Presenter{
    @Override
    public void getQualityDataRequest(String id) {

        mRxManage.add(mModel.getQualityData(id).subscribe(new RxSubscriber<QualityData>(mContext,true) {
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
                .subscribe(new RxSubscriber<ScanResult>(mContext,false) {
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
        mRxManage.add(mModel.chooseDeviceConnect(mac).subscribe(new RxSubscriber<RxBleDeviceServices>(mContext) {
            @Override
            protected void _onNext(RxBleDeviceServices deviceServices) {
                mView.returnChooseDeviceConnectWithSetUuid(deviceServices);
                mView.returnChooseDeviceConnectWithSetAddress(mac);
            }

            @Override
            protected void _onError(String message) {
                mView.showErrorTip("connectFail");
            }
        }));

    }
}
