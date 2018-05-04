package cc.lotuscard.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.baseapp.AppManager;
import com.jaydenxiao.common.baserx.RxBus2;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.baserx.RxSubscriber;
import com.jaydenxiao.common.commonutils.ImageLoaderUtils;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.SPUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cc.lotuscard.ILotusCallBack;
import cc.lotuscard.LotusCardDriver;
import cc.lotuscard.LotusCardParam;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.app.AppConstant;
import cc.lotuscard.bean.BleDevice;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import cc.lotuscard.identificationcardtest.R;
import cc.lotuscard.model.QualityModel;
import cc.lotuscard.presenter.QualityPresenter;
import cc.lotuscard.broadcast.StartingUpBroadcast;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import static cc.lotuscard.LotusCardDriver.m_InEndpoint;
import static cc.lotuscard.LotusCardDriver.m_OutEndpoint;
import static cc.lotuscard.LotusCardDriver.m_UsbDeviceConnection;


public class LotusCardDemoActivity extends BaseActivity<QualityPresenter,QualityModel> implements QualityContract.View, ILotusCallBack {

    private LotusCardDriver mLotusCardDriver;
    private UsbManager usbManager = null;
    private UsbDevice usbDevice = null;
    private UsbInterface usbInterface = null;
    private UsbDeviceConnection usbDeviceConnection = null;
    private final int m_nVID = 1306;//供应商ID
    private final int m_nPID = 20763;//产品识别码
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private Boolean haveUsbHostApi = false;
    private String deviceNode;//USB设备名称
    private HashMap<String, UsbDevice> deviceList;

    private long deviceHandle = -1;
    private Handler mHandler = null;
    private CardOperateThread cardOperateThread;
    private LotusCardParam tLotusCardParam1 = new LotusCardParam();
    private UsbDeviceConnection conn = null;//这个类用于发送和接收数据和控制消息到USB设备

    /*********************************** BLE *********************************/
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private CommonRecycleViewAdapter<BleDevice> bleDeviceAdapter;
    private MaterialDialog scanResultDialog,cirProgressBarWithScan,cirProgressBarWithChoose;
    private List<String> rxBleDeviceAddressList = new ArrayList<>();

    /*********************************** UI *********************************/
    public static final int REQUEST_CODE_WECHATUSER = 1201;
    private static final int REQUEST_CODE_CONTRACT = 1202;
    public static final String REDIRECT_URI = "redirect_uri";
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private TextView m_tvDeviceNode;
    public TextView displayCard;
    private EditText displayCode;
    private Boolean flag = false;
    private StartingUpBroadcast startingUpBroadcast;
    private ImageView bleState;
    private ImageView company_logo;
    private Button btnClearLog, scanTwoCode;

    @Override
    protected void onResume() {
        super.onResume();
        displayCard.setText(AppConstant.QUALITY_CARD);
        flag = true;

        initBleState();
        configureBleList();
        initBleStateListener();
    }

    private void initBleStateListener() {
        bleState.setOnClickListener(v ->  {
            String macAddress = SPUtils.getSharedStringData(AppApplication.getAppContext(),AppConstant.MAC_ADDRESS);
            if (TextUtils.isEmpty(macAddress)) {
                scanAndConnectBle();
            }else {
                new MaterialDialog.Builder(this)
                        .title("已绑定智能尺: " + macAddress + "，需要连接新智能尺？")
                        .titleColor(getResources().getColor(R.color.ff5001))
                        .positiveText(R.string.sure)
                        .negativeText(R.string.cancel)
                        .backgroundColor(getResources().getColor(R.color.white))
                        .onPositive((dialog, which) -> {
                            scanAndConnectBle();
                        })
                        .show();
            }
        });
    }

    private void configureBleList() {
        bleDeviceAdapter = new CommonRecycleViewAdapter<BleDevice>(this,R.layout.item_bledevice, bleDeviceList) {
            @Override
            public void convert(ViewHolderHelper helper, BleDevice bleDevice) {
                TextView text_name = helper.getView(R.id.text_name);
                TextView text_mac = helper.getView(R.id.text_mac);
                TextView text_rssi = helper.getView(R.id.text_rssi);
                text_name.setText(bleDevice.getName());
                text_mac.setText(bleDevice.getAddress());
                text_rssi.setText(String.valueOf(bleDevice.getRssi()));

                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //连接蓝牙
                        mPresenter.chooseDeviceConnectRequest(text_mac.getText().toString());
                        if (scanResultDialog != null) {
                            scanResultDialog.dismiss();
                        }
                    }
                });

            }
        };

        scanResultDialog = new MaterialDialog.Builder(this)
                .title(R.string.choose_device_prompt)
                .content("已检测到的蓝牙设备...")
                .backgroundColor(getResources().getColor(R.color.white))
                .titleColor(getResources().getColor(R.color.scan_result_list_title))
                .adapter(bleDeviceAdapter, null)
                .dividerColor(getResources().getColor(R.color.white))
                .build();

        cirProgressBarWithScan = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content("扫描附近蓝牙...")
                .backgroundColor(getResources().getColor(R.color.white))
                .build();

        cirProgressBarWithChoose = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content("配对中...")
                .backgroundColor(getResources().getColor(R.color.white))
                .build();
    }

    private void scanAndConnectBle() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        //先判断蓝牙是否打开
        if (!defaultAdapter.isEnabled()) {
            new MaterialDialog.Builder(this)
                    .content(getString(R.string.can_open_ble))
                    .positiveText(getString(R.string.open))
                    .negativeText(getString(R.string.cancel))
                    .backgroundColor(getResources().getColor(R.color.white))
                    .contentColor(getResources().getColor(R.color.primary))
                    .onPositive((dialog, which) -> defaultAdapter.enable())
                    .show();
        } else {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.requestEach(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(permission -> { // will emit 2 Permission objects
                        if (permission.granted) {
                            // FIXME: 2018/4/10 0010 需检测当前位置有没有开启
                            cirProgressBarWithScan.show();
                            Timer timer = new Timer();
                            timer = new Timer(true);
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (cirProgressBarWithScan.isShowing()){
                                        cirProgressBarWithScan.dismiss();
                                        RxBus2.getInstance().post(AppConstant.NO_BLE_FIND,true);
                                    }
                                }
                            }, 6000);
                            rxBleDeviceAddressList.clear();
                            bleDeviceList.clear();
                            mPresenter.getBleDeviceDataRequest();

                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // Denied permission without ask never again
                        } else {
                            // Denied permission with ask never again
                            // Need to go to the settings
                            ToastUtil.showShort(getString(R.string.unauthorized_location));
                        }
                    });
        }
    }

    //蓝牙初始化
    private void initBleState() {
        String macAddress = SPUtils.getSharedStringData(AppApplication.getAppContext(),AppConstant.MAC_ADDRESS);
        if (!TextUtils.isEmpty(macAddress)) {
            bleState.setBackgroundResource(R.drawable.ble_connected);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.act_main;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this,mModel);
    }

    @Override
    public void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        StartingUpBroadcastRecive();
        m_tvDeviceNode = (TextView) findViewById(R.id.tvDeviceNode);
        displayCard = (TextView) findViewById(R.id.displayCard);
        displayCode = (EditText) findViewById(R.id.displayCode);
        bleState = (ImageView) findViewById(R.id.bleState);
        company_logo = (ImageView) findViewById(R.id.company_logo);
        btnClearLog = (Button) findViewById(R.id.btnClearLog);
        scanTwoCode = (Button) findViewById(R.id.scan_twoCode);

        mLotusCardDriver = new LotusCardDriver();
        mLotusCardDriver.m_lotusCallBack = this;
        // 设置USB读写回调 串口可以不用此操作
        haveUsbHostApi = SetUsbCallBack();
        //测卡器设备检测
        cardDeviceChecked();

        initHandleCardDetails();
        initTimer();
        initRxBus2FindBle();
        initListener();
//        initPhotoLogo();
    }

    private void initListener() {
        btnClearLog.setOnClickListener(v -> {
            mPresenter.getQualityDataRequest("5ae1ab8cf93bfb038f326f33");
            if (null != displayCard) {
                displayCard.setText("");
            }
            if (null != displayCode) {
                displayCode.setText("");
            }
        });

        scanTwoCode.setOnClickListener(v -> {
            Intent intent = new Intent(this, cc.lotuscard.camera.CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE_WECHATUSER);
        });

        // FIXME: 2018/4/28 0028
        findViewById(R.id.btnCodeTrun).setOnClickListener(v -> {
            if (displayCode.getEditableText().length() > 5) {
                mPresenter.getQualityDataRequest(displayCode.getEditableText().toString());
            }

        });
    }

    //二维码扫描返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle bundle = data.getExtras();
            String result = bundle.getString("result");
            switch (resultCode) {
                case SCAN_HINT:
                    if (result != null) {
                        LogUtils.loge("二维码解析====" + result);
                        if (requestCode == REQUEST_CODE_CONTRACT) {
                            mPresenter.getQualityDataRequest(result);
                        } else if (requestCode == REQUEST_CODE_WECHATUSER) {
                            if (result.contains("https")) {
                                //解析出tid(ThirdMember中id)
                                int redirectUriIndex = result.indexOf(REDIRECT_URI) + REDIRECT_URI.length() + 1;
                                String s = result.substring(redirectUriIndex);
                                try {
//                                    String tid = LogUtils.getParams(s, "tid");
//                                    String cid = PreferencesUtils.getInstance(getActivity()).getMeasureCid();
//                                    mPresenter.getThirdMemberInfo(tid, cid);
                                    mPresenter.getQualityDataRequest(result);
                                } catch (Exception e) {
                                    ToastUtil.showShort("二维码解析失败，请重试");
                                    return;
                                }
                            } else {
                                mPresenter.getQualityDataRequest(result);
                            }
                        }
                    } else {
                        ToastUtil.showShort(getString(R.string.scan_qrcode_failed));
                    }
                    break;
                case CODE_HINT:
                    if (result != null) {
                        if (requestCode == REQUEST_CODE_CONTRACT) {
                            mPresenter.getQualityDataRequest(result);
                        } else if (requestCode == REQUEST_CODE_WECHATUSER) {
                            mPresenter.getQualityDataRequest(result);
                        }
                    } else {
                        ToastUtil.showShort(getString(R.string.enter_qrcode_error));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void initPhotoLogo() {
        mRxManager.add(Observable.timer(100, TimeUnit.MILLISECONDS) // 直接使用glide加载的话，activity切换动画时背景短暂为默认背景色
                .compose(RxSchedulers.io_main())
                .subscribeWith(new RxSubscriber<Long>(mContext, false) {
                    @Override
                    protected void _onNext(Long aLong) {
                        ImageLoaderUtils.displayBigPhoto(mContext, company_logo, "http://cms-bucket.nosdn.127.net/23a33655f8e04e4ab809bcacd581dbde20180424164958.jpeg");
                    }

                    @Override
                    protected void _onError(String message) {

                    }
                }));
    }

    private void initTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //先后顺序不可变
                KeepUsbDeviceState();
                if (!haveUsbHostApi) {
                    KeepUsbDeviceState();
                }

                CheckUsbDeviceState();
            }
        };
        timer.schedule(timerTask, 6000, 6000);
    }

    private void initHandleCardDetails() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (flag) {
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                    String strDate = formatter.format(curDate);
                    displayCard.setText(msg.obj.toString() + "  (" + strDate + ")");
                    AppConstant.QUALITY_CARD = msg.obj.toString();
                    mPresenter.getQualityDataRequest(AppConstant.QUALITY_CARD);
                    flag = false;
                }
            }
        };
    }

    private void initRxBus2FindBle() {
        //监听是否发现附近蓝牙
        mRxManager.on(AppConstant.NO_BLE_FIND, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isChecked) throws Exception {
                if (isChecked) {
                    ToastUtil.showShort("附近没有可见设备！请重试");
                }
            }
        });
    }

    //设置USB读写回调
    private Boolean SetUsbCallBack() {
        Boolean bResult = false;
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        // Get UsbManager from Android.
        usbManager = (UsbManager) AppApplication.getAppContext().getSystemService(USB_SERVICE);
        if (null == usbManager){
            return bResult;
        }

        //获取设备及设备名字
        deviceList = usbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            for (UsbDevice device : deviceList.values()) {
                if ((m_nVID == device.getVendorId())
                        && (m_nPID == device.getProductId())) {
                    usbDevice = device;
                    deviceNode = usbDevice.getDeviceName();
                    break;
                }
            }
        }
        if (null == usbDevice){
            return bResult;
        }
        usbInterface = usbDevice.getInterface(0);
        if (null == usbInterface){
            return bResult;
        }
        if (false == usbManager.hasPermission(usbDevice)) {//权限判断
            usbManager.requestPermission(usbDevice, pendingIntent);
        }

        if (usbManager.hasPermission(usbDevice)) {
            conn = usbManager.openDevice(usbDevice);//获取实例
        }

        if (null == conn){
            return bResult;
        }

        if (conn.claimInterface(usbInterface, true)) {
            usbDeviceConnection = conn;
        } else {
            conn.close();
        }
        if (null == usbDeviceConnection){
            return bResult;
        }
        // 把上面获取的对性设置到接口中用于回调操作
        LotusCardDriver.m_UsbDeviceConnection = usbDeviceConnection;
        if (usbInterface.getEndpoint(1) != null) {
            LotusCardDriver.m_OutEndpoint = usbInterface.getEndpoint(1);
        }
        if (usbInterface.getEndpoint(0) != null) {
            LotusCardDriver.m_InEndpoint = usbInterface.getEndpoint(0);
        }
        bResult = true;
        return bResult;
    }

    //刷卡器USB状态检测
    private void cardDeviceChecked() {
        if (haveUsbHostApi) {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("已连接");
                }
            });
            initAuto();
        }else {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("未连接");
                }
            });
        }
    }

    //自动检测USB设备初始化
    public void initAuto() {
        if (-1 == deviceHandle) {
            deviceHandle = mLotusCardDriver.OpenDevice("", 0, 0, 0, 0,// 使用内部默认超时设置
                    true);
        }
        if (deviceHandle != -1) {
            cardOperateThread = new CardOperateThread();
            new Thread(cardOperateThread).start();
        }
    }

    //实时观察设备状态
    public void CheckUsbDeviceState() {
        if (haveUsbHostApi) {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("已连接");
                }
            });
        }else {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("未连接");
                }
            });
        }
    }

    //保持设备连接（防止自动断开）
    public void KeepUsbDeviceState() {
        if (haveUsbHostApi) {
            //有设备
            haveUsbHostApi = false;
            mLotusCardDriver.CloseDevice(deviceHandle);
            usbDevice = null;
            conn = null;
        }else{
            //无设备
            usbManager = (UsbManager) AppApplication.getAppContext().getSystemService(USB_SERVICE);
            if (null == usbManager){
                return;
            }
            //获取设备及设备名字
            deviceList = usbManager.getDeviceList();
            if (!deviceList.isEmpty()) {
                for (UsbDevice device : deviceList.values()) {
                    if ((m_nVID == device.getVendorId()) && (m_nPID == device.getProductId())) {
                        usbDevice = device;
                        deviceNode = usbDevice.getDeviceName();
                        break;
                    }
                }
            }
            if (null == usbDevice) {
                //时间久了会检测不到
//                AppManager.getAppManager().finishAllActivity();
//                Intent intent = new Intent(AppApplication.getAppContext(), LotusCardDemoActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                AppApplication.getAppContext().startActivity(intent);
//                android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(0);
                return;
            }

            usbInterface = usbDevice.getInterface(0);

            if (usbManager.hasPermission(usbDevice)) {
                // FIXME: 2018/4/18 0018
                conn = usbManager.openDevice(usbDevice);//获取实例
            }

            if (conn == null) {
                //时间久了会检测不到
//                AppManager.getAppManager().finishAllActivity();
//                Intent intent = new Intent(AppApplication.getAppContext(), LotusCardDemoActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                AppApplication.getAppContext().startActivity(intent);
//                android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(0);
                return;
            }

            if (conn.claimInterface(usbInterface, true)) {
                usbDeviceConnection = conn;
            } else {
                conn.close();
            }
            // 把上面获取的对性设置到接口中用于回调操作
            LotusCardDriver.m_UsbDeviceConnection = usbDeviceConnection;
            if (usbInterface.getEndpoint(1) != null) {
                LotusCardDriver.m_OutEndpoint = usbInterface.getEndpoint(1);
            }
            if (usbInterface.getEndpoint(0) != null) {
                LotusCardDriver.m_InEndpoint = usbInterface.getEndpoint(0);
            }

            if (deviceHandle == -1) {
                initAuto();
            }else {
                deviceHandle = mLotusCardDriver.OpenDevice("", 0, 0, 0, 0,// 使用内部默认超时设置
                        true);
            }
            haveUsbHostApi = true;
        }

    }

    public void StartingUpBroadcastRecive() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        startingUpBroadcast = new StartingUpBroadcast();
        registerReceiver(startingUpBroadcast, intentFilter);
    }

    //需要质检的数据
    @Override
    public void returnGetQualityData(QualityData qualityData) {
        if (qualityData != null) {
            ArrayList<QualityData.Parts> mlist = qualityData.getParts();

            if (mlist.size() > 0) {
                AppManager.getAppManager().finishActivity(CheckActivity.class);
                AppConstant.QUALITY_NUMBER = qualityData.getId();
                AppConstant.QUALITY_CATEGORY = qualityData.getCategory();
                CheckActivity.startActivity(mContext, mlist);
            } else {
                ToastUtil.showShort("无对应的数据!");
            }
        }
    }

    //获取附近的蓝牙设备
    @Override
    public void returnGetBleDeviceData(ScanResult scanResult) {
        if (scanResult != null) {
            RxBleDevice device = scanResult.getBleDevice();
            if (!rxBleDeviceAddressList.contains(device.getMacAddress())) {//避免重复添加设备
                rxBleDeviceAddressList.add(device.getMacAddress());
                bleDeviceList.add(new BleDevice(device.getName(), device.getMacAddress(), scanResult.getRssi()));
                bleDeviceAdapter.notifyDataSetChanged();
            }

            if (rxBleDeviceAddressList.size() != 0 && cirProgressBarWithScan.isShowing()) {
                cirProgressBarWithScan.dismiss();
                scanResultDialog.show();
            }
        }
    }

    @Override
    public void returnChooseDeviceConnectWithSetUuidAndMacAddress(RxBleDeviceServices deviceServices,String macAddress) {
        SPUtils.setSharedStringData(AppApplication.getAppContext(), AppConstant.MAC_ADDRESS,macAddress);
        bleState.setImageResource(R.drawable.ble_connected);
        for (BluetoothGattService service : deviceServices.getBluetoothGattServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (isCharacteristicNotifiable(characteristic)) {
                    SPUtils.setSharedStringData(AppApplication.getAppContext(), AppConstant.UUID,characteristic.getUuid().toString());
                    cirProgressBarWithChoose.dismiss();
                    ToastUtil.showShort("蓝牙配对成功");
                    break;
                }
            }
        }
    }
    private boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    @Override
    public boolean callBackExtendIdDeviceProcess(Object objUser, byte[] arrBuffer) {
        return false;
    }

    @Override
    public boolean callBackReadWriteProcess(long nDeviceHandle, boolean bRead, byte[] arrBuffer) {
        int nResult = 0;
        boolean bResult = false;
        int nBufferLength = arrBuffer.length;
        int nWaitCount = 0;
        if (null == m_UsbDeviceConnection) {
            AddLog("null == m_UsbDeviceConnection");
            return false;
        }
        if (null == m_OutEndpoint) {
            AddLog("null == m_OutEndpoint");
            return false;
        }
        if (null == m_InEndpoint) {
            AddLog("null == m_InEndpoint");
            return false;
        }
        if (nBufferLength < 65) {
            AddLog("nBufferLength < 65");
            return false;
        }
        if (true == bRead) {
            arrBuffer[0] = 0;
            while (true) {
                nResult = m_UsbDeviceConnection.bulkTransfer(m_InEndpoint,
                        arrBuffer, 64, 3000);
                if (nResult <= 0) {
                    AddLog("nResult <= 0 is " + nResult);
                    break;
                }
                if (arrBuffer[0] != 0) {
                    //此处调整一下
                    System.arraycopy(arrBuffer, 0, arrBuffer, 1, nResult);
                    arrBuffer[0] = (byte)nResult;
                    break;
                }
                nWaitCount++;
                if (nWaitCount > 1000) {
                    AddLog("nWaitCount > 1000");
                    break;
                }
            }
            if (nResult == 64) {
                bResult = true;
            } else {
                AddLog("nResult != 64 is" +nResult);
                bResult = false;
            }
        } else {
            nResult = m_UsbDeviceConnection.bulkTransfer(m_OutEndpoint,
                    arrBuffer, 64, 3000);
            if (nResult == 64) {
                bResult = true;
            } else {
                AddLog("m_OutEndpoint bulkTransfer Write error");
                bResult = false;
            }
        }
        return bResult;
    }
    public void AddLog(String strLog) {
    }

    //子线程检测卡号
    public class CardOperateThread implements Runnable {
        @Override
        public void run() {
            boolean bResult;
            int nRequestType;
            long lCardNo;
            int n=0;

            while (true) {//使得线程循环
                if (haveUsbHostApi && flag) {//是否暂停
                    Log.e("test===",String.valueOf(n++));
                    try {
                        nRequestType = LotusCardDriver.RT_NOT_HALT;//未进入休眠的卡
                        bResult = mLotusCardDriver.GetCardNo(deviceHandle, nRequestType, tLotusCardParam1);//获取卡号，true表示成功

                        //如果失败了则sleep跳出,再循环
                        if (!bResult) {
                            Thread.sleep(500);
                            continue;
                        }

                        Message msg = new Message();
                        lCardNo = bytes2long(tLotusCardParam1.arrCardNo);
                        msg.obj = lCardNo;
                        mHandler.sendMessage(msg);

                        mLotusCardDriver.Beep(deviceHandle, 10);//响铃
                        mLotusCardDriver.Halt(deviceHandle);//响铃关闭
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    //获取到的卡号是4个字节的需转换
    public long bytes2long(byte[] byteNum) {
        long num = 0;
        for (int ix = 3; ix >= 0; --ix) {
            num <<= 8;
            if (byteNum[ix] < 0) {
                num |= (256 + (byteNum[ix]) & 0xff);
            } else {
                num |= (byteNum[ix] & 0xff);
            }
        }
        return num;
    }

    @Override
    public void showLoading(String title) {
        if (title=="chooseConnect") {
            cirProgressBarWithChoose.show();
        }

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorTip(String msg) {
        flag = true;
        ToastUtil.showShort(msg);
        //蓝牙连接失败
        if(msg=="connectFail"){
            cirProgressBarWithChoose.dismiss();
            bleState.setImageResource(R.drawable.ble_disconnected);
            SPUtils.setSharedStringData(AppApplication.getAppContext(), AppConstant.UUID, "");
            SPUtils.setSharedStringData(AppApplication.getAppContext(), AppConstant.MAC_ADDRESS, "");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        flag = false;//暂停识别卡
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (startingUpBroadcast != null) {
            unregisterReceiver(startingUpBroadcast);
        }
    }
}
