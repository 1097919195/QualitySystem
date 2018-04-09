package cc.lotuscard.presenter;

import com.jaydenxiao.common.baserx.RxSubscriber;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.CheckContract;

/**
 * Created by Administrator on 2018/4/4 0004.
 */

public class CheckPresenter extends CheckContract.Presenter{
    @Override
    public void startMeasureRequest(UUID characteristicUUID) {
        mRxManage.add(mModel.startMeasure(characteristicUUID).subscribe(new RxSubscriber<byte[]>(mContext,false) {
            @Override
            protected void _onNext(byte[] bytes) {
                mView.returnStartMeasure(bytes);
            }

            @Override
            protected void _onError(String message) {

            }
        }));
    }

    @Override
    public void upLoadAfterCheckedRequest(QualityData qualityData) {

    }
}
