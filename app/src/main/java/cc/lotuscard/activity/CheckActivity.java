package cc.lotuscard.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.aspsine.irecyclerview.universaladapter.recyclerview.OnItemClickListener;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.baserx.RxBus;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.CheckContract;
import cc.lotuscard.contract.QualityContract;
import cc.lotuscard.identificationcardtest.R;
import cc.lotuscard.model.CheckModel;
import cc.lotuscard.presenter.CheckPresenter;
import cc.lotuscard.utils.HexString;
import cc.lotuscard.widget.MeasureStateEnum;
import cc.lotuscard.widget.MyLineLayout;

/**
 * Created by Administrator on 2018/3/29 0029.
 */

public class CheckActivity extends BaseActivity<CheckPresenter, CheckModel> implements CheckContract.View {

    //    @Bind(R.id.irc_quality_data)
    IRecyclerView irc;
    TextView deBattery,reconnect;

    CommonRecycleViewAdapter<QualityData.Parts> adapter;
    List<QualityData.Parts> partses = new ArrayList<>();
    int unMeasuredCounts=0;
    int measuredCounts=0;
    int itemPostion = 0;
    int itemPostionAgo = 0;
    boolean remuasure = false;
    String mac = SPUtils.getSharedStringData(AppApplication.getAppContext(), AppConstant.MAC_ADDRESS);
    UUID characteristicUUID = UUID.fromString(SPUtils.getSharedStringData(AppApplication.getAppContext(), AppConstant.UUID));
    List<Integer> canRemeasureData = new ArrayList<>();

    public static void startActivity(Context mContext, ArrayList<QualityData.Parts> partses) {
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
        irc = (IRecyclerView) findViewById(R.id.irc_quality_data);
        deBattery = (TextView) findViewById(R.id.de_battery);
        reconnect = (TextView) findViewById(R.id.reconnect);

        partses = getIntent().getParcelableArrayListExtra(AppConstant.QUALITY_DATA);

        initRcycleAdapter();
        itemClickRemeasure();
        initMeasure();

        // FIXME: 2018/4/12 0012 状态检测
        RxBleClient rxBleClient = AppApplication.getRxBleClient(this);
        RxBleDevice rxBleDevice = rxBleClient.getBleDevice(mac);
        RxBleConnection.RxBleConnectionState kkk = rxBleDevice.getConnectionState();

        reconnect.setOnClickListener(v -> {
            mPresenter.startMeasureRequest(characteristicUUID);
        });

        // FIXME: 2018/4/10 0010 添加蓝牙状态监听
//        mRxManager.on(AppConstant.CONNECT_SUCCEED, new Action1<Boolean>() {
//            @Override
//            public void call(Boolean isConnect) {
//                if (isConnect) {
//                    ToastUtil.showShort("确认连接");
//                }else {
//                    ToastUtil.showShort("连接失败");
//                }
//            }
//        });
//
//        RxBus.getInstance().post(AppConstant.CONNECT_SUCCEED,true);
    }

    private void initRcycleAdapter() {
        adapter = new CommonRecycleViewAdapter<QualityData.Parts>(this, R.layout.item_quality, partses) {
            @Override
            public void convert(ViewHolderHelper helper, QualityData.Parts parts) {
                TextView gravity = helper.getView(R.id.gravity);
                TextView primary = helper.getView(R.id.primary);
                TextView currect = helper.getView(R.id.currect);

                gravity.setText(parts.getName());
                primary.setText(String.valueOf(parts.getOriValue()));
                currect.setText(String.valueOf(parts.getActValue()));
                currect.setTextColor(getResources().getColor(R.color.battery_color));
                if (parts.isSelected()) {
                    //选中的样式
                    helper.setBackgroundColor(R.id.gravity,getResources().getColor(R.color.battery_color));
                } else {
                    //未选中的样式
                    helper.setBackgroundColor(R.id.gravity,getResources().getColor(R.color.white));
                }

            }
        };
        //listview底部跟随一个按钮，适应屏幕
        View view = View.inflate(this, R.layout.list_bottom_button, null);
        view.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showShort("质检完成啦！！");
            }
        });
        irc.addFooterView(view);
        irc.setAdapter(adapter);
//        irc.setLayoutManager(new LinearLayoutManager(this));
        irc.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
    }

    private void initMeasure() {
        measuredCounts = partses.size();
        unMeasuredCounts = measuredCounts;
        partses.get(0).setSelected(true);
        if (mac != null && characteristicUUID != null) {
            mPresenter.startMeasureRequest(characteristicUUID);
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
                    if(unMeasuredCounts!=0)
                        partses.get(measuredCounts-unMeasuredCounts).setSelected(false);
                    partses.get(itemPostion).setSelected(true);
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

        deBattery.setText(battery + "%");

        //测量指定部位
        if (remuasure) {
            partses.get(itemPostion).setActValue(length); //cm
            partses.get(itemPostion).setSelected(false);

            if (unMeasuredCounts != 0)
                partses.get(measuredCounts - unMeasuredCounts).setSelected(true);
            adapter.notifyDataSetChanged();
            remuasure = false;
        } else {
            if (unMeasuredCounts != 0) {
                assignValue(length, angle); //mm
            } else {
                ToastUtil.showShort(getString(R.string.measure_completed));
            }
        }

    }

    private void assignValue(Float length, Float angle) {
        try {
            if(unMeasuredCounts!=1)
                partses.get(measuredCounts+1-unMeasuredCounts).setSelected(true);
            partses.get(measuredCounts-unMeasuredCounts).setSelected(false);
            partses.get(measuredCounts-unMeasuredCounts).setActValue(length); //cm
            canRemeasureData.add(measuredCounts-unMeasuredCounts);
            if (unMeasuredCounts != 0) {
                unMeasuredCounts = unMeasuredCounts - 1;
            }
            adapter.notifyDataSetChanged();

        } catch (Exception e) {

        }

    }

    //质检上传
    @Override
    public void returnupLoadAfterChecked(JSONObject jsonObject) {

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
        if(msg=="当前连接已断开！"){
//            bleState.setImageResource(R.drawable.ble_disconnected);
        }

    }

    @Override
    public void onBackPressed() {
        showHandleBackPress();
    }

    private void showHandleBackPress() {
        new MaterialDialog.Builder(this)
                .title("确定要离开当前量体界面?")
                .onPositive((d, i) -> {
                    finish();
                })
                .positiveText(getResources().getString(R.string.sure))
                .negativeColor(getResources().getColor(R.color.ff0000))
                .negativeText("点错了")
                .show();
    }
}
