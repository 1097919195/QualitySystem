package cc.lotuscard.contract;

import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;


import java.util.ArrayList;

import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.PartsData;
import cc.lotuscard.bean.QualityData;
import io.reactivex.Maybe;
import io.reactivex.Observable;


/**
 * Created by Administrator on 2018/3/28 0028.
 */

public interface QualityContract {
    interface Model extends BaseModel {
        Observable<PartsData> getQualityDataWithCard(String num);//质检项目

        Observable<PartsData> getQualityData(String num);//质检项目

        Observable<PartsData> getQualityDataWithQRCode(String QRCode);//质检项目

        Observable<HttpResponse<ArrayList<QualityData.Parts>>> getQualitySampleData(String id);//质检样衣

        Observable<ScanResult> getBleDeviceData();

        Maybe<RxBleDeviceServices> chooseDeviceConnect(String macAddress);
    }

    interface View extends BaseView {
        void returnGetQualityDataWithCard(PartsData qualityData);

        void returnGetQualityData(PartsData qualityData);

        void returnGetQualityDataWithQRCode(PartsData qualityData);

        void returnGetQualitySampleData(HttpResponse<ArrayList<QualityData.Parts>> qualityData);

        void returnGetBleDeviceData(ScanResult scanResult);

        void returnChooseDeviceConnectWithSetUuidAndMacAddress(RxBleDeviceServices deviceServices,String macAddress);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void getQualityDataWithCardRequest(String num);

        public abstract void getQualityDataRequest(String num);

        public abstract void getQualityDataRequestWithQRCode(String QRCode);

        public abstract void getQualitySampleDataRequest(String id);

        public abstract void getBleDeviceDataRequest();

        public abstract void chooseDeviceConnectRequest(String macAddress);
    }

}
