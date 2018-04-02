package cc.lotuscard.contract;

import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;

import java.util.List;

import cc.lotuscard.bean.QualityData;
import rx.Observable;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public interface QualityContract {
    interface Model extends BaseModel {
        Observable<QualityData> getQualityData(String id);
    }

    interface View extends BaseView {
        void returnGetQualityData(QualityData qualityData);
    }

    abstract static class Presenter extends BasePresenter<View, Model> {
        public abstract void getQualityDataRequest(String id);
    }

}
