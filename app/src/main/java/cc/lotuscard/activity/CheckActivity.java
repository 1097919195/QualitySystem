package cc.lotuscard.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.animation.ScaleInAnimation;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.aspsine.irecyclerview.universaladapter.recyclerview.OnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.baseapp.AppManager;
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


import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import cc.lotuscard.identificationcardtest.BuildConfig;
import cc.lotuscard.identificationcardtest.R;
import cc.lotuscard.model.CheckModel;
import cc.lotuscard.presenter.CheckPresenter;
import cc.lotuscard.utils.BitmapUtils;
import cc.lotuscard.utils.HexString;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
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
    @BindView(R.id.photosRCV)
    RecyclerView photosRCV;
    @BindView(R.id.camera_add)
    FrameLayout camera_add;

    CommonRecycleViewAdapter<PartsData.ApparelInfoBean> adapter;
    CommonRecycleViewAdapter<Bitmap> photosAdapter;
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

    // 保存图片的文件
    private Uri imageUri;
    public static final File PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);//获得外部存储器的第一层的文件对象
    public static final String JPG_SUFFIX = ".jpg";
    private String picName;
    public static final int TAKE_PHOTO = 1222;
    List<Bitmap> photosList = new ArrayList<>();
    List<String> imgTag = new ArrayList<>();

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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);//底部导航栏覆盖activity
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
        initTakePhotosAdapter();

        findViewById(R.id.write).setOnClickListener(v -> {
//            mPresenter.AcceptWeightDataRequest(characteristicUUID);
//            mPresenter.settingWeightConfigureRequest(characteristicUUID,"C".getBytes());
        });
    }

    private void initTakePhotosAdapter() {
        photosAdapter = new CommonRecycleViewAdapter<Bitmap>(this, R.layout.item_take_photos, photosList) {
            @Override
            public void convert(ViewHolderHelper helper, Bitmap bitmap) {
                ImageView img = helper.getView(R.id.img);
                ImageView del = helper.getView(R.id.del);

                img.setImageBitmap(bitmap);
                del.setOnClickListener(v -> {
                    photosList.remove(helper.getLayoutPosition());
                    imgTag.remove(helper.getLayoutPosition());
                    photosAdapter.notifyDataSetChanged();
                });
            }
        };
        View view = View.inflate(this, R.layout.list_take_photos, null);
        view.findViewById(R.id.camera_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showShort("拍照！");
            }
        });
//        photosIRCV.addHeaderView(view);
        photosRCV.setAdapter(photosAdapter);
        photosRCV.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
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
//                            List<String> gravity = new ArrayList<>();
//                            List<Float> actValue = new ArrayList<>();
//                            for (PartsData.ApparelInfoBean p : partses) {
//                                gravity.add(p.getName());
//                            }
//                            for (PartsData.ApparelInfoBean p : partses) {
//                                actValue.add(p.getActValue());
//                            }
//                            String[] stringsGrivaty = (String[]) gravity.toArray(new String[gravity.size()]);
//                            Float[] stringsActValue = (Float[]) actValue.toArray(new Float[actValue.size()]);
//                            Object[][] result = {stringsGrivaty, stringsActValue};
//                            LogUtils.loge("resultSave==" + result[0][0] + result[1][0]);
//                            mPresenter.upLoadAfterCheckedRequest(result);
                            List<PartsData.ApparelInfoBean> data = (new MultipartBeanWithUserData(partses)).getParts();
                            MultipartBody.Part[] images = new MultipartBody.Part[imgTag.size()];
                            for (int i=0;i<imgTag.size();i++) {
                                images[i] = getSpecialBodyTypePic(imgTag.get(i));
                            }
                            mPresenter.upLoadQualityDataRequest(AppConstant.QUALITY_ID,data, remark,images);
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

        camera_add.setOnClickListener(v -> {
            capturePic();
        });
    }

    /**
     * 读取特体大图
     *
     * @param filename
     * @return
     */
    private MultipartBody.Part getSpecialBodyTypePic(String filename) {
        File f = new File(PATH + File.separator + AppConstant.FILE_PROVIDER_NAME + File.separator + filename + JPG_SUFFIX);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);//创建RequestBody，其中`multipart/form-data`为编码类型
        return MultipartBody.Part.createFormData("images[]", filename, requestFile);
    }


    private void capturePic() {
        Date date = new Date(System.nanoTime());
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(date.toString().getBytes());
            picName = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            picName = date.toString();
        }
        File storageFile = new File(PATH.getAbsoluteFile() + File.separator + AppConstant.FILE_PROVIDER_NAME);
        if (!storageFile.isDirectory()) {//创建目录
            storageFile.mkdirs();
        }
        File outputImage = new File(storageFile, picName + JPG_SUFFIX);
        try {
            outputImage.createNewFile();//createNewFile()是创建一个不存在的文件。
        } catch (IOException e) {
            LogUtils.loge(e.toString());
        }
        //将File对象转换为Uri并启动照相程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // 系统版本大于N的统一用FileProvider处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 将文件转换成content://Uri的形式
            imageUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".fileprovider", outputImage);
            // 申请临时访问权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } else {
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            imageUri = Uri.fromFile(outputImage);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        startActivityForResult(intent, TAKE_PHOTO);
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
                primary.setText(parts.getValue());
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

        //默认分割线
//        irc.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //添加自定义分割线
        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);//这里导入的包和自己封装的库不同
        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.custom_divider));
        irc.addItemDecoration(divider);

        irc.setAdapter(adapter);
//        irc.setLayoutManager(new LinearLayoutManager(this));
        irc.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
        adapter.closeLoadAnimation();
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
        measurelength = length;
        if (battery > 20) {
            deBattery.setTextColor(getResources().getColor(R.color.primary));
            deBattery.setText(battery + "%");
        }else {
            deBattery.setTextColor(getResources().getColor(R.color.red));
            deBattery.setText(battery + "%" + "请记得及时充电");
        }


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
//扫描指定文件(通知系统刷新相册)
                Intent intentBc1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intentBc1.setData(imageUri);
                this.sendBroadcast(intentBc1);

                Bitmap bitmap = BitmapUtils.decodeUri(this, imageUri, 800, 800);
                if (bitmap != null) {
                    photosList.add(bitmap);
                    if (!TextUtils.isEmpty(picName)) {
                        imgTag.add(picName);
                        LogUtils.loge("imaTag=="+picName);
                    }
                    photosAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showShort("拍照失败！");
                }
                break;
            default:
                break;
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
        AppConstant.QUALITY_CARD = "";
        AppConstant.QUALITY_NUMBER = "";
        AppConstant.QUALITY_CATEGORY = "";
        finish();
    }

    @Override
    public void showLoading(String title) {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorTip(String msg) {
        //用户信息的token过期时
        if (msg == "token过期") {
            SPUtils.setSharedStringData(AppApplication.getAppContext(),AppConstant.TOKEN,"");
            AppManager.getAppManager().finishAllActivity();
            Intent intent = new Intent(CheckActivity.this, AccountActivity.class);
            startActivity(intent);
            ToastUtil.showShort("用户信息已经过期,请重新登录");
            return;
        }
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
            speechSynthesizer.stop();
        }

    }
}
