package cc.lotuscard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
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
import java.util.Arrays;
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

    CommonRecycleViewAdapter<String> adapter;
    List<String> nameList = new ArrayList<>();
    List<QualityData.Parts> partses = new ArrayList<>();

//    public static void startActivity(Context mContext, String[] name,int value) {
//        Intent intent = new Intent(mContext, QualityActivity.class);
//        intent.putExtra(AppConstant.QUALITY_DATA, name);
//        intent.putExtra(AppConstant.QUALITY_DATA2, value);
//        mContext.startActivity(intent);
//    }

    public static void startActivity(Context mContext, QualityData value) {
        Intent intent = new Intent(mContext, QualityActivity.class);
//        intent.putExtra(AppConstant.QUALITY_DATA, name);
        intent.putExtra(AppConstant.QUALITY_DATA2, value);
        mContext.startActivity(intent);
    }

//    public static void startActivity(Context mContext, String qualityData) {
//        Intent intent = new Intent(mContext, QualityActivity.class);
//        intent.putExtra(AppConstant.QUALITY_DATA, qualityData);
//        mContext.startActivity(intent);
//    }

    @Override
    public int getLayoutId() {
        return R.layout.frag_quality;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        partses = getIntent().getParcelableExtra(AppConstant.QUALITY_DATA);
        final String[] names = getIntent().getStringArrayExtra(AppConstant.QUALITY_DATA);
        nameList = Arrays.asList(names);
        adapter = new CommonRecycleViewAdapter<String>(this,R.layout.item_quality,nameList) {
            @Override
            public void convert(ViewHolderHelper helper, String s) {
                TextView gravity1 = helper.getView(R.id.gravity);
                TextView unit1 = helper.getView(R.id.unit);

                gravity1.setText(s);

            }

//            @Override
//            public void convert(ViewHolderHelper helper, QualityData.Parts parts) {
//
//                TextView gravity1 = helper.getView(R.id.gravity);
//                TextView unit1 = helper.getView(R.id.unit);
//
//                String name = null;
//                List<String> gravity = new ArrayList<>();
//                List<String> unit = new ArrayList<>();
//                for (QualityData.Parts p : partses) {
//                    gravity.add(p.getName());
//                }
//                Log.e("TAG2", gravity.toString());
//                for (int i=0;i<partses.size();i++) {
//                    name = gravity.get(i);
//                    Log.e("TAG3", name);
//                }
//                gravity1.setText(name);
//                unit1.setText(parts.getValue());
//            }
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
