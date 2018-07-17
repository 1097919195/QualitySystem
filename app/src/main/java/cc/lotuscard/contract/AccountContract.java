package cc.lotuscard.contract;

import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;

import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.LoginTokenData;
import io.reactivex.Observable;

/**
 * Created by Administrator on 2018/5/21 0021.
 */

public interface AccountContract {
    interface Model extends BaseModel {
        Observable<LoginTokenData> getTokenSignIn(String userName, String passWord);
    }

    interface View extends BaseView {
        void returnGetTokenSignIn(LoginTokenData loginTokenData);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void getTokenSignInRequest(String userName, String passWord);
    }

}
