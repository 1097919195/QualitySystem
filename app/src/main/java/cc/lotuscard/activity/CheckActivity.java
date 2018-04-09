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

import com.alibaba.fastjson.JSONObject;
import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.aspsine.irecyclerview.universaladapter.recyclerview.OnItemClickListener;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;

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
    TextView deBattery;

    CommonRecycleViewAdapter<QualityData.Parts> adapter;
    List<QualityData.Parts> partses = new ArrayList<>();
    int unMeasuredCounts;
    int itemPostion = 0;

    boolean remuasure = false;
    int currentItem = 0;

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

        partses = getIntent().getParcelableArrayListExtra(AppConstant.QUALITY_DATA);
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

// FIXME: 2018/4/9 0009
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position) {
                //此postion从2开始
                ToastUtil.showShort(String.valueOf(position));
                itemPostion = position;
                remuasure = true;
                MyLineLayout layout = (MyLineLayout) view;
                layout.setState(MeasureStateEnum.MEASURED.ordinal());
                layout.invalidate();
                adapter.notifyDataSetChanged();
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position) {
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        unMeasuredCounts = partses.size();
        String mac = AppApplication.getMacAddress(this);
        UUID characteristicUUID = AppApplication.getUUID(this);
        if (mac != null && characteristicUUID != null) {
            mPresenter.startMeasureRequest(characteristicUUID);
        } else {
            ToastUtil.showShort("请先连接蓝牙");
        }

    }

    //蓝牙测量结果处理
    @Override
    public void returnStartMeasure(byte[] bytes) {
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
            a2 = a2 / 10;
//            fragment.handleMeasureData(a1, (float) a2 / 10, a3);
            ToastUtil.showShort(String.valueOf(a1) + "==" + String.valueOf(a2) + "==" + String.valueOf(a3));
            deBattery.setText(a3 + "%");

            //是否需要重新测量
            if (remuasure) {
                partses.get(itemPostion).setActValue(Float.valueOf(a1) / 10); //cm
                adapter.notifyDataSetChanged();
                remuasure = false;
            }else {
                if (unMeasuredCounts != 0) {
                    assignValue(Float.valueOf(a1), Float.valueOf(a2)); //mm
                } else {
                    //无未测项目，提示测量完成
                    ToastUtil.showShort(getString(R.string.measure_completed));
                }
            }
        }
    }

    private void assignValue(Float length, Float angle) {
        try {
            partses.get(partses.size()-unMeasuredCounts).setActValue(length / 10); //cm
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

    }
}
