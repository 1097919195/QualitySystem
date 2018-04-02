package cc.lotuscard.presenter;

import com.jaydenxiao.common.baserx.RxSubscriber;

import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityPresenter extends QualityContract.Presenter{
    @Override
    public void getQualityDataRequest(String id) {

        mRxManage.add(mModel.getQualityData(id).subscribe(new RxSubscriber<QualityData>(mContext,false) {
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
}
