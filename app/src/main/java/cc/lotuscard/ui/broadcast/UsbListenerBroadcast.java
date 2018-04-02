package cc.lotuscard.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import cc.lotuscard.app.AppApplication;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class UsbListenerBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.hardware.usb.action.USB_STATE")) {
            if (intent.getExtras().getBoolean("connected")) {
                // usb 插入
                Toast.makeText(AppApplication.getAppContext(), "usb 插入", Toast.LENGTH_SHORT).show();
            } else {
                //   usb 拔出
                Toast.makeText(AppApplication.getAppContext(), "usb 拔出", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
