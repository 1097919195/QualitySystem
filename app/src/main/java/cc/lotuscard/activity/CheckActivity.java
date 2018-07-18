package cc.lotuscard.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.aspsine.irecyclerview.universaladapter.recyclerview.OnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.baserx.RxBus2;
import com.jaydenxiao.common.commonutils.FormatUtil;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.MultipartBeanWithUserData;
import cc.lotuscard.bean.PartsData;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.bean.RetQuality;
import cc.lotuscard.contract.CheckContract;
import cc.lotuscard.identificationcardtest.R;
import cc.lotuscard.model.CheckModel;
import cc.lotuscard.presenter.CheckPresenter;
import cc.lotuscard.utils.HexString;
import io.reactivex.functions.Consumer;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2018/3/29 0029.
 */

public class CheckActivity extends BaseActivity<CheckPresenter, CheckModel> implements CheckContract.View {


    //    @BindView(R.id.irc_quality_data)
    IRecyclerView irc;
    //    @BindView(R.id.bleState)
    TextView deBattery;
    //    @BindView(R.id.reconnect)
    TextView card_num, qu_num, clo_category;
    TextView big_grivity;
    TextView connect_state;
    TextView bleName;
    Button btnBigSaveData, btnBack;
    @BindView(R.id.btn_remark)
    Button btn_remark;

    CommonRecycleViewAdapter<PartsData.ApparelInfoBean> adapter;
    List<PartsData.ApparelInfoBean> partses = new ArrayList<>();
    int unMeasuredCounts=0;
    int measuredCounts=0;
    int itemPostion = 0;
    int itemPostionAgo = 0;
    boolean remuasure = false;
    String mac = SPUtils.getSharedStringData(AppApplication.getAppContext(), AppConstant.MAC_ADDRESS);
    String uuidString = SPUtils.getSharedStringData(AppApplication.getAppContext(), AppConstant.UUID);
    UUID characteristicUUID = null;
    List<Integer> canRemeasureData = new ArrayList<>();

    public SpeechSynthesizer speechSynthesizer;//提供对已安装的语音合成引擎的功能的访问
    float measurelength = 0;

    String remark = "";

    public static void startActivity(Context mContext, ArrayList<PartsData.ApparelInfoBean> partses) {
        Intent intent = new Intent(mContext, CheckActivity.class);
        intent.putExtra(AppConstant.QUALITY_DATA, partses);
        mContext.startActivity(intent);
    }


    @Override
    public int getLayoutId() {
        return R.layout.act_quality;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    public void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        initSpeech();
        irc = (IRecyclerView) findViewById(R.id.irc_quality_data);
        deBattery = (TextView) findViewById(R.id.de_battery);
        card_num = (TextView) findViewById(R.id.card_num);
        qu_num = (TextView) findViewById(R.id.qu_num);
        clo_category = (TextView) findViewById(R.id.clo_category);
        big_grivity = (TextView) findViewById(R.id.big_grivity);
        connect_state = (TextView) findViewById(R.id.connect_state);
        bleName = (TextView) findViewById(R.id.bleName);
        btnBigSaveData = (Button) findViewById(R.id.big_save_data);
        btnBack = (Button) findViewById(R.id.btn_back);

        card_num.setText(AppConstant.QUALITY_CARD);
        qu_num.setText(AppConstant.QUALITY_NUMBER);
        clo_category.setText(AppConstant.QUALITY_CATEGORY);
        partses = getIntent().getParcelableArrayListExtra(AppConstant.QUALITY_DATA);

        initRxBus2WithSetTV();
        initRcycleAdapter();
        itemClickRemeasure();
        initMeasure();
        initListener();
//        initReconnectBtn();

        findViewById(R.id.write).setOnClickListener(v -> {
//            mPresenter.AcceptWeightDataRequest(characteristicUUID);
//            mPresenter.settingWeightConfigureRequest(characteristicUUID,"C".getBytes());
        });
    }

    private void initListener() {
//        btnBigSaveData.setOnClickListener(v -> {
//            if (unMeasuredCounts == 0) {
//                mPresenter.upLoadAfterCheckedRequest(partses);
//            } else {
//                ToastUtil.showShort("您还没有完成全部质检部位！");
//            }
//        });

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        RxView.clicks(btnBigSaveData)
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (unMeasuredCounts == 0) {
                            List<String> gravity = new ArrayList<>();
                            List<Float> actValue = new ArrayList<>();
                            for (PartsData.ApparelInfoBean p : partses) {
                                gravity.add(p.getName());
                            }
                            for (PartsData.ApparelInfoBean p : partses) {
                                actValue.add(p.getActValue());
                            }
                            String[] stringsGrivaty = (String[]) gravity.toArray(new String[gravity.size()]);
                            Float[] stringsActValue = (Float[]) actValue.toArray(new Float[actValue.size()]);
                            Object[][] result = {stringsGrivaty, stringsActValue};
                            LogUtils.loge("resultSave==" + result[0][0] + result[1][0]);
//                            mPresenter.upLoadAfterCheckedRequest(result);

//                            Map<String, RequestBody> map = new HashMap<>();
//                            map.put("clothes_id", RequestBody.create(null, String.valueOf(price)));
//                            map.put("quality_data", RequestBody.create(null, description));
//                            map.put("remark", RequestBody.create(null, name));
                            List<PartsData.ApparelInfoBean> data = (new MultipartBeanWithUserData(partses)).getParts();
                            MultipartBody.Part[] images = new MultipartBody.Part[3];
                            mPresenter.upLoadQualityDataRequest("5b4dad389134ca3e8e7a2132",data, remark,images);
                        } else {
                            ToastUtil.showShort("您还没有完成全部质检部位！");
                        }
                    }
                });

        btn_remark.setOnClickListener(v -> {
                new MaterialDialog.Builder(CheckActivity.this)
                        .title("备注信息")
//                        .widgetColor(Color.BLUE)//输入框光标的颜色
                        //前2个一个是hint一个是预输入的文字
                        .input("请输入备注内容", remark, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                remark = input.toString();
                            }
                        })
                        .negativeText("取消")
                        .positiveColor(getResources().getColor(R.color.main_blue))
                        .show();
        });
    }

    private void initSpeech() {
        try {
            if (speechSynthesizer == null) {
                speechSynthesizer = new SpeechSynthesizer(this, AppConstant.SPEECH_APP_KEY, AppConstant.SPEECH_APP_SECRET);
            }
            speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
            speechSynthesizer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, 70);
            speechSynthesizer.init(null);
        } catch (Exception e) {
            LogUtils.loge(e.getMessage());
            ToastUtil.showShort("语音播报出现异常");
        }
    }

    private void initReconnectBtn() {
//        reconnect.setOnClickListener(v -> {
//            if (uuidString != "") {//确保蓝牙没有配对成功是不能进操作的
//                if (AppApplication.getRxBleClient(this).getBleDevice(mac).getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
//                    ToastUtil.showShort("蓝牙通讯很好，无需重新连接");
//                } else {
//                    ToastUtil.showShort("重新建立连接中");
//                    mPresenter.startMeasureRequest(characteristicUUID);
//                }
//            } else {
//                ToastUtil.showShort("您还没有配对过蓝牙");
//            }
//
//        });
    }

    private void initRxBus2WithSetTV() {
        mRxManager.on(AppConstant.DISPLAY_GRIVITY, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                big_grivity.setText(partses.get(integer).getName());

                if (measurelength != 0 && unMeasuredCounts != 0) {
                    if (remuasure) {
                        speechSynthesizer.playText(partses.get(itemPostion).getName() + String.valueOf(measurelength) + "厘米,请测" + partses.get(integer).getName());
                        remuasure = false;
                    } else {
                        speechSynthesizer.playText(partses.get(measuredCounts - unMeasuredCounts - 1).getName() + String.valueOf(measurelength) + "厘米,请测" + partses.get(integer).getName());
                    }
                    measurelength = 0;
                } else {
                    speechSynthesizer.playText("请测" + partses.get(integer).getName());
                }


            }
        });
    }

    private void initRcycleAdapter() {
        adapter = new CommonRecycleViewAdapter<PartsData.ApparelInfoBean>(this, R.layout.item_quality, partses) {
            @Override
            public void convert(ViewHolderHelper helper, PartsData.ApparelInfoBean parts) {
                TextView gravity = helper.getView(R.id.gravity);
                TextView primary = helper.getView(R.id.primary);
                TextView currect = helper.getView(R.id.currect);

                gravity.setText(parts.getName());
                primary.setText(String.valueOf(parts.getValue()));
                currect.setText(String.valueOf(parts.getActValue()));

                currect.setTextColor(getResources().getColor(R.color.battery_color));
                if (parts.isSelected()) {
                    //选中的样式
                    helper.setBackgroundColor(R.id.gravity, getResources().getColor(R.color.item_selector));
                    helper.setBackgroundColor(R.id.primary, getResources().getColor(R.color.item_selector));
                    helper.setBackgroundColor(R.id.currect, getResources().getColor(R.color.item_selector));

                } else {
                    //未选中的样式
                    helper.setBackgroundColor(R.id.gravity,getResources().getColor(R.color.white));
                    helper.setBackgroundColor(R.id.primary, getResources().getColor(R.color.white));
                    helper.setBackgroundColor(R.id.currect, getResources().getColor(R.color.white));
                }

                if (parts.getActValue() != 0) {
                    primary.setTextColor(getResources().getColor(R.color.c333));
                }

            }
        };
        //listview底部跟随一个按钮，适应屏幕
//        View view = View.inflate(this, R.layout.list_bottom_button, null);
//        view.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (unMeasuredCounts == 0) {
//                    mPresenter.upLoadAfterCheckedRequest(partses);
//                }else {
//                    ToastUtil.showShort("您还没有完成全部质检部位！");
//                }
//            }
//        });
//        irc.addFooterView(view);
        irc.setAdapter(adapter);
//        irc.setLayoutManager(new LinearLayoutManager(this));
        irc.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
    }

    private void initMeasure() {
        measuredCounts = partses.size();
        unMeasuredCounts = measuredCounts;
        partses.get(0).setSelected(true);
//        RxBus2.getInstance().post(AppConstant.DISPLAY_GRIVITY, 0);
        big_grivity.setText(partses.get(0).getName());
        speechSynthesizer.playText("请测" + partses.get(0).getName());//代替上面的解决快速刷卡语音

        if (mac != "" && uuidString != "") {
            characteristicUUID = UUID.fromString(uuidString);
            mPresenter.startMeasureRequest(characteristicUUID);
            mPresenter.checkBleConnectStateRequest();
        } else {
            ToastUtil.showShort("请先配对蓝牙设备！");
        }
    }

    private void itemClickRemeasure() {
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position) {
//                ToastUtil.showShort(String.valueOf(position));
                if (canRemeasureData.size() > position) {
                    itemPostion = position;
                    remuasure = true;
                    partses.get(itemPostionAgo).setSelected(false);
                    if (unMeasuredCounts != 0) {
                        partses.get(measuredCounts-unMeasuredCounts).setSelected(false);
                    }

                    partses.get(itemPostion).setSelected(true);
                    RxBus2.getInstance().post(AppConstant.DISPLAY_GRIVITY, itemPostion);

                    adapter.notifyDataSetChanged();
                    itemPostionAgo = itemPostion;
                }else {
                    ToastUtil.showShort("请先按顺序完成第一次测量");
                }

            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position) {
                return false;
            }
        });
    }

    //蓝牙测量结果处理
    @Override
    public void returnStartMeasure(Float length, Float angle, int battery) {
        ToastUtil.showShort(String.valueOf(length) + "==" + String.valueOf(angle) + "==" + String.valueOf(battery));

        measurelength = length;
        deBattery.setText(battery + "%");

        //测量指定部位
        if (remuasure) {
            partses.get(itemPostion).setActValue(length);
            partses.get(itemPostion).setSelected(false);

            if (unMeasuredCounts != 0) {
                partses.get(measuredCounts - unMeasuredCounts).setSelected(true);
                RxBus2.getInstance().post(AppConstant.DISPLAY_GRIVITY, measuredCounts - unMeasuredCounts);
            } else {
                speechSynthesizer.playText(partses.get(itemPostion).getName() + String.valueOf(length) + "厘米");
                remuasure = false;
            }
            adapter.notifyDataSetChanged();

        } else {
            if (unMeasuredCounts != 0) {
                assignValue(length, angle);
            } else {
                speechSynthesizer.playText("已经测量完毕");
                ToastUtil.showShort(getString(R.string.measure_completed));
            }
        }

    }

    //蓝牙状态监听
    @Override
    public void returnCheckBleConnectState(RxBleConnection.RxBleConnectionState connectionState) {
        RxBleClient rxBleClient = AppApplication.getRxBleClient(this);
        RxBleDevice rxBleDevice = rxBleClient.getBleDevice(mac);
        RxBleConnection.RxBleConnectionState bleState = rxBleDevice.getConnectionState();
        if (bleState==connectionState.DISCONNECTED) {
            ToastUtil.showShort("连接断开");
            connect_state.setText("未连接");
            bleName.setText("");
            mPresenter.startMeasureRequest(characteristicUUID);//自动连接
        }
        if (bleState==connectionState.CONNECTED) {
            ToastUtil.showShort("蓝牙通信成功，请开始测量");
            connect_state.setText("已连接");
            bleName.setText(mac);
        }
    }

    //体重计接受read
    @Override
    public void returnAcceptWeightData(byte[] bytes) {
        String s = HexString.bytesToHex(bytes);
        ToastUtil.showShort(String.valueOf(bytes));
        LogUtils.loge("qweqwe" + bytes);
    }

    //体重计写入write
    @Override
    public void returnSettingWeightConfigure(byte[] bytes) {
        String s = HexString.bytesToHex(bytes);
        ToastUtil.showShort(String.valueOf(bytes));
        LogUtils.loge("qweqwe" + bytes);

    }

    private void assignValue(Float length, Float angle) {
        try {
            if (unMeasuredCounts != 1) {//这个操作只有蓝牙按下后才会触发，所以unMeasuredCounts不能为1
                partses.get(measuredCounts+1-unMeasuredCounts).setSelected(true);
                RxBus2.getInstance().post(AppConstant.DISPLAY_GRIVITY, measuredCounts + 1 - unMeasuredCounts);
            }
            partses.get(measuredCounts-unMeasuredCounts).setSelected(false);
            partses.get(measuredCounts - unMeasuredCounts).setActValue(length);
            speechSynthesizer.playText(partses.get(measuredCounts - unMeasuredCounts).getName() + String.valueOf(length) + "厘米");
            canRemeasureData.add(measuredCounts-unMeasuredCounts);
            if (unMeasuredCounts != 0) {
                unMeasuredCounts = unMeasuredCounts - 1;
            }
            adapter.notifyDataSetChanged();

        } catch (Exception e) {

        }

    }

    //质检上传(测试)
    @Override
    public void returnupLoadAfterChecked(RetQuality retQuality) {
        if (retQuality != null && retQuality.getStatus() == 200) {
            ToastUtil.showShort(retQuality.getMsg());
            AppConstant.QUALITY_CARD = "";
            AppConstant.QUALITY_NUMBER = "";
            AppConstant.QUALITY_CATEGORY = "";
            finish();
        }

    }

    //质检结果上传
    @Override
    public void returnUploadQualityData(HttpResponse httpResponse) {
        ToastUtil.showShort("上传成功");
    }

    @Override
    public void showLoading(String title) {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorTip(String msg) {
        ToastUtil.showShort(msg);

    }

    @Override
    public void onBackPressed() {
        showHandleBackPress();
    }

    private void showHandleBackPress() {
        new MaterialDialog.Builder(this)
                .title("确定要离开当前量体界面?")
                .onPositive((d, i) -> {
                    AppConstant.QUALITY_CARD = "";
                    AppConstant.QUALITY_NUMBER = "";
                    AppConstant.QUALITY_CATEGORY = "";
                    finish();
                })
                .positiveText(getResources().getString(R.string.sure))
                .negativeColor(getResources().getColor(R.color.ff0000))
                .negativeText("点错了")
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechSynthesizer != null) {
            speechSynthesizer = null;
        }
    }
}
