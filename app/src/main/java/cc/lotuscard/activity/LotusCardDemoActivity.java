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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aspsine.irecyclerview.universaladapter.ViewHolderHelper;
import com.aspsine.irecyclerview.universaladapter.recyclerview.CommonRecycleViewAdapter;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.scan.ScanResult;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cc.lotuscard.LotusCardDriver;
import cc.lotuscard.LotusCardParam;
import cc.lotuscard.app.AppApplication;
import cc.lotuscard.bean.BleDevice;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import cc.lotuscard.identificationcardtest.R;
import cc.lotuscard.model.QualityModel;
import cc.lotuscard.presenter.QualityPresenter;
import cc.lotuscard.broadcast.UsbListenerBroadcast;


public class LotusCardDemoActivity extends BaseActivity<QualityPresenter,QualityModel> implements QualityContract.View {

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

    private int deviceHandle = -1;
    private Handler mHandler = null;
    private CardOperateThread cardOperateThread;
    private LotusCardParam tLotusCardParam1 = new LotusCardParam();

    /*********************************** BLE *********************************/
    private RxBleClient rxBleClient;
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private CommonRecycleViewAdapter<BleDevice> bleDeviceAdapter;
    private MaterialDialog scanResultDialog;
    private List<String> rxBleDeviceAddressList = new ArrayList<>();

    /*********************************** UI *********************************/
    private TextView m_tvDeviceNode;

    private TextView displayCard;
    private EditText displayCode;
    private Boolean flag = false;
    private UsbListenerBroadcast usbListenerBroadcast;
    private ImageView bleState;

    @Override
    protected void onResume() {
        super.onResume();
        flag = true;

        initBleState();
        configureBleList();
        bleState.setOnClickListener(v ->  {
            // FIXME: 2018/4/4 0004 
            String macAddress = AppApplication.getMacAddress(LotusCardDemoActivity.this);
//            if (TextUtils.isEmpty(macAddress)) {
//
//            }
            scanAndConnectBle();
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
                .backgroundColor(getResources().getColor(R.color.white))
                .titleColor(getResources().getColor(R.color.scan_result_list_title))
                .dividerColor(getResources().getColor(R.color.divider))
                .adapter(bleDeviceAdapter, null).build();
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
                            // permission.name is granted !
                            scanResultDialog.show();
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
        String macAddress = AppApplication.getMacAddress(this);
        if (!TextUtils.isEmpty(macAddress)) {
            RxBleDevice rxBleDevice = rxBleClient.getBleDevice(macAddress);
            if (rxBleDevice != null && rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
                bleState.setBackgroundResource(R.drawable.ble_connected);
            }
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
        UsbListenerState();
//        mLotusCardDriver = new LotusCardDriver();
        m_tvDeviceNode = (TextView) findViewById(R.id.tvDeviceNode);
        displayCard = (TextView) findViewById(R.id.displayCard);
        displayCode = (EditText) findViewById(R.id.displayCode);
        bleState = (ImageView) findViewById(R.id.bleState);
        // 设置USB读写回调 串口可以不用此操作
//        haveUsbHostApi = SetUsbCallBack();
//        //测卡器设备检测
//        cardDeviceChecked();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==1) {
                    if (flag) {
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                        String strDate = formatter.format(curDate);
                        displayCard.setText(strDate + "====" + msg.obj.toString());
                        mPresenter.getQualityDataRequest(msg.obj.toString());
                    }
                }
                if (msg.what == 2) {

                }

            }
        };

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
//                Message msg = new Message();
//                msg.obj = i;
//                msg.what = 2;
//                mHandler.sendMessage(msg);

                //先后顺序不可变
                KeepUsbDeviceState();
                CheckUsbDeviceState();

            }
        };
//        timer.schedule(timerTask,6000,10000);


        rxBleClient = AppApplication.getRxBleClient(this);

    }

    // FIXME: 2018/4/2 0002 添加USB插拔状态监听
    //设置USB读写回调
    private Boolean SetUsbCallBack() {
        Boolean bResult = false;
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        // Get UsbManager from Android.
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        if (null == usbManager)
            return bResult;

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
        if (null == usbDevice)
            return bResult;
        usbInterface = usbDevice.getInterface(0);
        if (null == usbInterface)
            return bResult;
        if (false == usbManager.hasPermission(usbDevice)) {//权限判断
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
        UsbDeviceConnection conn = null;//这个类用于发送和接收数据和控制消息到USB设备
        if (usbManager.hasPermission(usbDevice)) {
            conn = usbManager.openDevice(usbDevice);//获取实例
        }

        if (null == conn)
            return bResult;

        if (conn.claimInterface(usbInterface, true)) {
            usbDeviceConnection = conn;
        } else {
            conn.close();
        }
        if (null == usbDeviceConnection)
            return bResult;
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

    //刷卡器USB检测
    private void cardDeviceChecked() {
        if (haveUsbHostApi) {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("Device Node:" + deviceNode);
                }
            });
            initAuto();
        }else {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("Device Node:" + "未检测到设备!");
                }
            });
        }
    }

    //自动检测USB设备初始化
    public void initAuto() {
        if (-1 == deviceHandle) {
            deviceHandle = mLotusCardDriver.OpenDevice("", 0, 0, true);
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
                    m_tvDeviceNode.setText("Device Node:" + deviceNode);
                }
            });
        }else {
            m_tvDeviceNode.post(new Runnable() {
                @Override
                public void run() {
                    m_tvDeviceNode.setText("Device Node:" + "未检测到设备!");
                }
            });
        }
    }

    //保持设备连接
    public void KeepUsbDeviceState() {
        if (haveUsbHostApi) {
            //有设备
            haveUsbHostApi = false;
            mLotusCardDriver.CloseDevice(deviceHandle);
            usbDevice = null;
//            ToastUtil.showShort("无设备连接");
        }else{
            //无设备
            usbManager = (UsbManager) getSystemService(USB_SERVICE);

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
            if (null == usbDevice)
                return;
            usbInterface = usbDevice.getInterface(0);
            UsbDeviceConnection conn = null;//这个类用于发送和接收数据和控制消息到USB设备
            if (usbManager.hasPermission(usbDevice)) {
                conn = usbManager.openDevice(usbDevice);//获取实例
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
                deviceHandle = mLotusCardDriver.OpenDevice("", 0, 0, true);
            }
            haveUsbHostApi = true;
        }

    }

    //清除按钮
    public void OnClearLogListener(View arg0) {
        mPresenter.getQualityDataRequest("159");
        if (null == displayCard)
            return;
        displayCard.setText("");
        if (null == displayCode)
            return;
        displayCode.setText("");

        if (haveUsbHostApi) {
            //有设备
            haveUsbHostApi = false;
            mLotusCardDriver.CloseDevice(deviceHandle);
            usbDevice = null;
            ToastUtil.showShort("无设备连接");
        }else{
            //无设备
            usbManager = (UsbManager) getSystemService(USB_SERVICE);

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
            if (null == usbDevice)
                return;
            usbInterface = usbDevice.getInterface(0);
            UsbDeviceConnection conn = null;//这个类用于发送和接收数据和控制消息到USB设备
            if (usbManager.hasPermission(usbDevice)) {
                conn = usbManager.openDevice(usbDevice);//获取实例
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
                    deviceHandle = mLotusCardDriver.OpenDevice("", 0, 0, true);
            }
            haveUsbHostApi = true;
        }

    }

    public void UsbListenerState() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        usbListenerBroadcast = new UsbListenerBroadcast();
        registerReceiver(usbListenerBroadcast, intentFilter);
    }

    //需要质检的数据
    @Override
    public void returnGetQualityData(QualityData qualityData) {
        Log.e("succeed", "succeed");
        if (qualityData != null) {
            ArrayList<QualityData.Parts> mlist = qualityData.getParts();

            if (mlist.size() > 0) {
//                startQualityControl(mlist);
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
//            LogUtils.loge("device==="+scanResult.toString());
            if (!rxBleDeviceAddressList.contains(device.getMacAddress())) {//避免重复添加设备
                rxBleDeviceAddressList.add(device.getMacAddress());
                bleDeviceList.add(new BleDevice(device.getName(), device.getMacAddress(), scanResult.getRssi()));
                bleDeviceAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void returnChooseDeviceConnectWithSetUuid(RxBleDeviceServices deviceServices) {
        bleState.setImageResource(R.drawable.ble_connected);
        for (BluetoothGattService service : deviceServices.getBluetoothGattServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (isCharacteristicNotifiable(characteristic)) {
                    AppApplication.setUUID(this, characteristic.getUuid());
//                    ToastUtil.showShort(String.valueOf(characteristic.getUuid()));
                    ToastUtil.showShort("蓝牙连接成功");
                    break;
                }
            }
        }
    }

    private boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    @Override
    public void returnChooseDeviceConnectWithSetAddress(String mac) {
        AppApplication.setMacAddress(this, mac);
    }

    // FIXME: 2018/4/4 0004
    //对应蓝牙是否还能被检测
    private void equipmentDie(ScanResult scanResult) {
    }

//    private void startQualityControl(List<QualityData.Parts> mlist) {
//        String name;
//        int value = 0;
//        List<String> gravity = new ArrayList<>();
//        List<Integer> unit = new ArrayList<>();
//        for (QualityData.Parts p : mlist) {
//            gravity.add(p.getName());
//        }
//        Log.e("TAG2", gravity.toString());
//        for (int i=0;i<mlist.size();i++) {
//            name = gravity.get(i);
//            Log.e("TAG3", name);
//        }
//
//        for (QualityData.Parts p : mlist) {
//            unit.add(p.getValue());
//        }
//        Log.e("TAG2", unit.toString());
//        for (int i=0;i<mlist.size();i++) {
//            value = unit.get(i);
//            Log.e("TAG3", String.valueOf(value));
//        }
//
//
//        int grivatyCount = gravity.size();//获取数组的个数 等于集合的个数
//        String[] grivatyValue = gravity.toArray(new String[grivatyCount]);//把集合转化成数组
//
////        CheckActivity.startActivity(mContext,grivatyValue,value);
//    }

    //子线程
    public class CardOperateThread implements Runnable {
        @Override
        public void run() {
            boolean bResult;
            int nRequestType;
            long lCardNo;
            int n=0;

            while (true) {//使得线程循环
                if (haveUsbHostApi && flag) {//是否暂停
                    // FIXME: 2018/4/1 0001 线程运行速度变慢了好多
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
                        msg.what = 1;
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

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorTip(String msg) {
        ToastUtil.showShort(msg);
        //蓝牙连接失败
        if(msg=="connectFail"){
            bleState.setImageResource(R.drawable.ble_disconnected);
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
        if (usbListenerBroadcast != null) {
            unregisterReceiver(usbListenerBroadcast);
        }
    }
}
