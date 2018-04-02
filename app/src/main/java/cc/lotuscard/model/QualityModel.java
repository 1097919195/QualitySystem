package cc.lotuscard.model;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaydenxiao.common.baserx.RxSchedulers;

import java.util.ArrayList;
import java.util.List;

import cc.lotuscard.api.Api;
import cc.lotuscard.api.HostType;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2018/3/28 0028.
 */

public class QualityModel implements QualityContract.Model {
    @Override
    public Observable<QualityData> getQualityData(String id) {
        return Api.getDefault(HostType.QUALITY_DATA)
                .getQuality(id)
                //如果不规范的话
//                .map(new Func1<JSONArray, QualityData>() {
//                    @Override
//                    public QualityData call(JSONArray jsonArray) {
//                        QualityData qualityData1 = new QualityData();
//                        List<QualityData.Parts> partses = new ArrayList<QualityData.Parts>();
//                        Gson gson = new Gson();
//                        partses =gson.fromJson(jsonArray.toString(),new TypeToken<List<QualityData.Parts>>() {
//                        }.getType());
//                        qualityData1.setParts(partses);
//                        return qualityData1;
//                    }
//                })
                .compose(RxSchedulers.<QualityData>io_main());
    }
}
