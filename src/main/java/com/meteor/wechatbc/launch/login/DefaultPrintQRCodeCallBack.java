package com.meteor.wechatbc.launch.login;

import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.http.HttpUtil;
import com.meteor.wechatbc.launch.login.PrintQRCodeCallBack;
import com.meteor.wechatbc.util.VersionCheck;

import java.awt.*;

public class DefaultPrintQRCodeCallBack implements PrintQRCodeCallBack {
    @Override
    public String print(String uuid) {
        String url = "https://login.weixin.qq.com/qrcode/"+uuid;
        final String decode = QrCodeUtil.decode(IoUtil.toStream(HttpUtil.downloadBytes(url)));
        QrConfig qrConfig = QrConfig.create()
                .setForeColor(Color.WHITE)
                .setBackColor(Color.BLACK)
                .setWidth(0)
                .setHeight(0).setMargin(1);
        String asciiArt = QrCodeUtil.generateAsAsciiArt(decode,qrConfig);
        System.out.println(asciiArt);
        System.out.println("请扫码登录!");

        return null;
    }
}
