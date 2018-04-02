package cc.lotuscard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.commonutils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.identificationcardtest.R;

/**
 * Created by Administrator on 2018/3/29 0029.
 */

public class QualityActivity extends BaseActivity {

    @Bind(R.id.irc_quality_data)
    IRecyclerView irc;

    CommonRecycleViewAdapter<QualityData.Parts> adapter;
    List<QualityData.Parts> partses = new ArrayList<>();

    public static void startActivity(Context mContext, ArrayList<QualityData.Parts> partses) {
        Intent intent = new Intent(mContext, QualityActivity.class);
        intent.putExtra(AppConstant.QUALITY_DATA, partses);
        mContext.startActivity(intent);
    }


    @Override
    public int getLayoutId() {
        return R.layout.frag_quality;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        partses = getIntent().getParcelableArrayListExtra(AppConstant.QUALITY_DATA);
//        LogUtils.loge("hahaha=="+partses.get(0).getName());
        adapter = new CommonRecycleViewAdapter<QualityData.Parts>(this,R.layout.item_quality,partses) {
            @Override
            public void convert(ViewHolderHelper helper, QualityData.Parts parts) {
                TextView gravity1 = helper.getView(R.id.gravity);
                TextView unit1 = helper.getView(R.id.unit);

                gravity1.setText(parts.getName());
                unit1.setText(String.valueOf(parts.getValue()));
            }
        };
        irc.setAdapter(adapter);
        irc.setLayoutManager(new LinearLayoutManager(this));
//        irc.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));二选一

        //listview底部跟随一个按钮，适应屏幕
        View view = View.inflate(this, R.layout.list_bottom_button, null);
        view.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showShort("质检完成啦！！");
            }
        });
        irc.addFooterView(view);
    }
}
