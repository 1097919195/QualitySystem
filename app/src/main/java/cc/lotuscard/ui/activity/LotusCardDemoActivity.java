package cc.lotuscard.ui.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.commonutils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cc.lotuscard.LotusCardDriver;
import cc.lotuscard.LotusCardParam;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.contract.QualityContract;
import cc.lotuscard.identificationcardtest.R;
import cc.lotuscard.model.QualityModel;
import cc.lotuscard.presenter.QualityPresenter;
import cc.lotuscard.ui.broadcast.UsbListenerBroadcast;


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

    /*********************************** UI *********************************/
    private TextView m_tvDeviceNode;

    private TextView displayCard;
    private EditText displayCode;
    private Boolean flag = false;
    private UsbListenerBroadcast usbListenerBroadcast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

    }

    @Override
    protected void onResume() {
        super.onResume();
        flag = true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this,mModel);
    }

    @Override
    public void initView() {
        UsbListenerState();
        mLotusCardDriver = new LotusCardDriver();
        m_tvDeviceNode = (TextView) findViewById(R.id.tvDeviceNode);
        displayCard = (TextView) findViewById(R.id.displayCard);
        displayCode = (EditText) findViewById(R.id.displayCode);
        // 设置USB读写回调 串口可以不用此操作
        haveUsbHostApi = SetUsbCallBack();
        //测卡器设备检测
        cardDeviceChecked();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (flag) {
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                    String strDate = formatter.format(curDate);
                    displayCard.setText(strDate + "====" + msg.obj.toString());
                    mPresenter.getQualityDataRequest(msg.obj.toString());
                }
            }
        };



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
                    m_tvDeviceNode.setText("Device Node:" + "未检测到设备!请重启程序");
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

    //清除按钮
    public void OnClearLogListener(View arg0) {
        if (null == displayCard)
            return;
        displayCard.setText("");
        if (null == displayCode)
            return;
        displayCode.setText("");

//        haveUsbHostApi = false;
        mLotusCardDriver.CloseDevice(deviceHandle);
        deviceHandle = -1;
        cardDeviceChecked();

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
                QualityActivity.startActivity(mContext, mlist);
            } else {
                ToastUtil.showShort("无对应的数据!");
            }
        }
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
////        QualityActivity.startActivity(mContext,grivatyValue,value);
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
                if (flag) {//是否暂停
                    // FIXME: 2018/4/1 0001 线程运行速度变慢了好多
//                    Log.e("test===",String.valueOf(n++));
                    try {
                        nRequestType = LotusCardDriver.RT_NOT_HALT;//未进入休眠的卡
                        bResult = mLotusCardDriver.GetCardNo(deviceHandle, nRequestType, tLotusCardParam1);//获取卡号，true表示成功

                        //如果失败了则sleep跳出,再循环
                        if (!bResult) {
                            Thread.sleep(1000);
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

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorTip(String msg) {
        ToastUtil.showShort(msg);
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
