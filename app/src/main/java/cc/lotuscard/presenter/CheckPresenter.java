package cc.lotuscard.presenter;

import com.jaydenxiao.common.baserx.RxSubscriber;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.bean.RetQuality;
import cc.lotuscard.contract.CheckContract;
import cc.lotuscard.utils.HexString;

/**
 * Created by Administrator on 2018/4/4 0004.
 */

public class CheckPresenter extends CheckContract.Presenter{
    private static final int MEASURE_DURATION = 400;

    @Override
    public void startMeasureRequest(UUID characteristicUUID) {
        mRxManage.add(mModel.startMeasure(characteristicUUID)
                .throttleFirst(MEASURE_DURATION, TimeUnit.MILLISECONDS)
                .subscribeWith(new RxSubscriber<byte[]>(mContext,false) {
                    @Override
                    protected void _onNext(byte[] bytes) {
                        String s = HexString.bytesToHex(bytes);
                        if (s.length() == AppConstant.STANDARD_LENGTH) {
                            int code = Integer.parseInt("8D6A", 16);
                            int length = Integer.parseInt(s.substring(0, 4), 16);
                            int angle = Integer.parseInt(s.substring(4, 8), 16);
                            int battery = Integer.parseInt(s.substring(8, 12), 16);
                            int a1 = length ^ code;
                            int a2 = angle ^ code;
                            int a3 = battery ^ code;
                            a1 += AppConstant.ADJUST_VALUE;
                            mView.returnStartMeasure(Float.valueOf(a1) / 10, Float.valueOf(a2) / 10, a3);
                        }

                    }

                    @Override
                    protected void _onError(String message) {
//                        mView.showErrorTip("连接通讯失败！");

                    }
                }));
    }

    @Override
    public void upLoadAfterCheckedRequest(Object[][] qualityDataList) {

        mRxManage.add(mModel.upLoadAfterChecked(qualityDataList).subscribeWith(new RxSubscriber<RetQuality>(mContext, true) {
            @Override
            protected void _onNext(RetQuality retQuality) {
                mView.returnupLoadAfterChecked(retQuality);
            }

            @Override
            protected void _onError(String message) {
                mView.showErrorTip(message);

            }
        }));
    }

    @Override
    public void checkBleConnectStateRequest() {
        mRxManage.add(mModel.checkBleConnectState()
                .subscribe(
                        connectedState->mView.returnCheckBleConnectState(connectedState)
                ));
    }

    @Override
    public void AcceptWeightDataRequest(UUID characteristicUUID) {
        mRxManage.add(mModel.acceptWeightData(characteristicUUID)
                .subscribe(
                        bytes -> {
                            mView.returnAcceptWeightData(bytes);
                        }
                ));
    }

    @Override
    public void settingWeightConfigureRequest(UUID characteristicUUID, byte[] data) {
        mRxManage.add(mModel.settingWeightConfigure(characteristicUUID, data)
                .subscribe(
                        bytes -> {
                            mView.returnSettingWeightConfigure(bytes);
                        }
                ));
    }


}
