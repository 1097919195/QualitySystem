package cc.lotuscard.model;


import com.jaydenxiao.common.baserx.RxSchedulers;

import cc.lotuscard.api.Api;
import cc.lotuscard.api.HostType;
import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.LoginTokenData;
import cc.lotuscard.contract.AccountContract;
import io.reactivex.Observable;

/**
 * Created by Administrator on 2018/5/21 0021.
 */

public class AccountModel implements AccountContract.Model{
    @Override
    public Observable<LoginTokenData> getTokenSignIn(String userName, String passWord) {
        return Api.getDefault(HostType.QUALITY_DATA_NEW)
                .getTokenWithSignIn(userName, passWord)
                .map(new Api.HttpResponseFunc<>())
                .compose(RxSchedulers.io_main());
    }
}
