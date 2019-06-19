package com.kuaiyou.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;

public class AdActivity extends Activity {
    //    private String sdkKey = null;
//    private String adId = null;
//    private int videoType;
//    private String installedPackagename = null;
    private Uri uri = null;
    private String[] instlReport;
    private String gdtExtraUrls;
    private String clickId_gdt;
//    private int route = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setTheme(android.R.style.Theme_NoDisplay);
        try {
            Bundle bundle = getIntent().getExtras();
            uri = bundle.getParcelable("path");
            instlReport = bundle.getStringArray("install_report");
            gdtExtraUrls=bundle.getString("gdt_conversion_link");
            clickId_gdt=bundle.getString("click_id_gdt");
            Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uri);
            installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
//        installIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
//                getApplicationInfo().packageName);
            startActivityForResult(installIntent, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 汇报成功安装
            AdViewUtils.reportEffect(instlReport);
            if (!TextUtils.isEmpty(gdtExtraUrls))
                AdViewUtils.reportEffect(new String[]{AdViewUtils.getGdtActionLink(gdtExtraUrls, clickId_gdt, ConstantValues.ACTION_ID_INSTALLED)});

//
            Toast.makeText(this, "应用安装成功", 0).show();
            AdViewUtils.logInfo("Install Done");
//            }
        } else {
            // 未能安装 do nothing
            AdViewUtils.logInfo("Install Cancel,RESULT_OK=" + resultCode);
        }
        this.finish();
    }

}
