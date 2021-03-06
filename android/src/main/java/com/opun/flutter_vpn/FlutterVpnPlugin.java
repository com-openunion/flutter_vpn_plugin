package com.opun.flutter_vpn;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.sangfor.sdk.SFMobileSecuritySDK;
import com.sangfor.sdk.base.SFSDKFlags;
import com.sangfor.sdk.base.SFSDKMode;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static com.sangfor.sdk.base.SFSDKMode.MODE_SANDBOX;
import static com.sangfor.sdk.base.SFSDKMode.MODE_VPN;
import static com.sangfor.sdk.base.SFSDKMode.MODE_VPN_SANDBOX;

/** FlutterVpnPlugin */
public class FlutterVpnPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
  private static final String TAG = "FlutterVpnPlugin";
  private static final String NAMESPACE = "plugins.openunion/vpn_plugin";
  private final PluginRegistry.Registrar registrar;
  private final Activity activity;
  private final MethodChannel channel;
  private MethodChannel.Result mResult;
  private SFSDKMode mSDKMode;

  /** Plugin registration. */
  public static void registerWith(PluginRegistry.Registrar registrar) {
    final FlutterVpnPlugin instance = new FlutterVpnPlugin(registrar);
    registrar.addRequestPermissionsResultListener(instance);
  }

  FlutterVpnPlugin(PluginRegistry.Registrar r) {
    this.registrar = r;
    this.activity = r.activity();
    this.channel = new MethodChannel(registrar.messenger(), NAMESPACE + "/methods");
    channel.setMethodCallHandler(this);
  }


  @Override
  public void onMethodCall(MethodCall call, Result result) {
    mResult = result;
    switch (call.method) {
      case "initSDK": {
        boolean isInitSucc = false;

        //只使用VPN功能场景:表明启用VPN安全接入功能,详情参考集成指导文档
        SFSDKMode sdkMode = MODE_VPN;
//        //只使用安全沙箱功能场景:表明启用安全沙箱功能,详情参考集成指导文档
//        SFSDKMode sdkMode = SFSDKMode.MODE_SANDBOX;
//        //同时使用VPN功能+安全沙箱功能场景:表明同时启用VPN功能+安全沙箱功能,详情参考集成指导文档
//                SFSDKMode sdkMode = MODE_VPN_SANDBOX;
        if (sdkMode == MODE_VPN) {
          //表明是单应用或者是主应用
          int sdkFlags = SFSDKFlags.FLAGS_HOST_APPLICATION;
          //表明使用VPN功能中的TCP模式
          sdkFlags |= SFSDKFlags.FLAGS_VPN_MODE_TCP;
          //初始化SDK
          SFMobileSecuritySDK.getInstance().initSDK(activity.getApplicationContext(), sdkMode, sdkFlags, null);
          isInitSucc = true;
        } else if (sdkMode == MODE_SANDBOX) {
          //表明是单应用或者是主应用
          int sdkFlags = SFSDKFlags.FLAGS_HOST_APPLICATION;
          //表明启用安全沙箱功能中的文件隔离功能
          sdkFlags |= SFSDKFlags.FLAGS_ENABLE_FILE_ISOLATION;
          //初始化SDK
          SFMobileSecuritySDK.getInstance().initSDK(activity.getApplicationContext(), sdkMode, sdkFlags, null);
          isInitSucc = true;
        } else if (sdkMode == MODE_VPN_SANDBOX) {
          //表明是单应用或者是主应用
          int sdkFlags = SFSDKFlags.FLAGS_HOST_APPLICATION;
          //表明使用VPN功能中的TCP模式
          sdkFlags |= SFSDKFlags.FLAGS_VPN_MODE_TCP;
          //表明启用安全沙箱功能中的文件隔离功能
          sdkFlags |= SFSDKFlags.FLAGS_ENABLE_FILE_ISOLATION;
          //初始化SDK
          SFMobileSecuritySDK.getInstance().initSDK(activity.getApplicationContext(), sdkMode, sdkFlags, null);
          isInitSucc = true;
        }else {
          isInitSucc = false;
          Toast.makeText(activity.getApplicationContext(), "SDK模式错误", Toast.LENGTH_LONG).show();
        }
        mSDKMode = sdkMode;

        result.success(isInitSucc);
        break;
      }
      case "startPrimaryAuth": {
        String vpnPath = call.argument(Constants.PARAM_KEY_VPN);
        String servicePath = call.argument(Constants.PARAM_KEY_SERVICE);
        String token = call.argument(Constants.PARAM_KEY_TOKEN);
        Log.e("vpnPath ==>", vpnPath);
        Log.e("servicePath ==>", servicePath);
        Log.e("token ==>", token);
        SFMobileSecuritySDK.getInstance().startPrimaryAuth(vpnPath, servicePath, token);
        result.success(null);
        break;
      }

      case "vpnLogout": {
        SFMobileSecuritySDK.getInstance().logout();
        result.success(null);
        break;
      }
      case "getPlatformVersion": {
        result.success("Android " + android.os.Build.VERSION.RELEASE);

      }

      default: {
        result.notImplemented();
        break;
      }
    }
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    return false;
  }
}
