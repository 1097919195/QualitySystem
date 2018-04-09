package cc.lotuscard.app;


import android.content.Context;

import com.jaydenxiao.common.baseapp.BaseApplication;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.polidea.rxandroidble.RxBleClient;

import java.util.UUID;

import cc.lotuscard.identificationcardtest.BuildConfig;

/**
 * APPLICATION
 */
public class AppApplication extends BaseApplication {
    private RxBleClient rxBleClient;
    private UUID characteristicUUID;
    private String macAddress;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化logger,注意拷贝的话BuildConfig.LOG_DEBUG一定要是在当前module下的包名，配置文件中判断测适和发行版本
        LogUtils.logInit(BuildConfig.LOG_DEBUG);
        rxBleClient = RxBleClient.create(this);
    }

    public static RxBleClient getRxBleClient(Context context) {
        AppApplication application = ((AppApplication) context.getApplicationContext());
        return application.rxBleClient;
    }

    public static void setUUID(Context context, UUID characteristicUUID) {
        AppApplication application = ((AppApplication) context.getApplicationContext());
        application.characteristicUUID = characteristicUUID;
    }

    public static UUID getUUID(Context context) {
        AppApplication application = ((AppApplication) context.getApplicationContext());
        return application.characteristicUUID;
    }

    public static void setMacAddress(Context context, String macAddress) {
        AppApplication application = ((AppApplication) context.getApplicationContext());
        application.macAddress = macAddress;
    }

    public static String getMacAddress(Context context) {
        AppApplication application = ((AppApplication) context.getApplicationContext());
        return application.macAddress;
    }
}
