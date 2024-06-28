package com.meteor.wechatbc.launch.login;

import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.http.HttpUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.meteor.wechatbc.launch.login.PrintQRCodeCallBack;
import com.meteor.wechatbc.util.VersionCheck;

import java.awt.*;
import java.nio.charset.StandardCharsets;

public class DefaultPrintQRCodeCallBack implements PrintQRCodeCallBack {
    @Override
    public String print(String uuid) {
        String url = "https://login.weixin.qq.com/qrcode/"+uuid;
        final String decode = QrCodeUtil.decode(IoUtil.toStream(HttpUtil.downloadBytes(url)));
        QrConfig qrConfig = QrConfig.create()
                .setForeColor(Color.WHITE)
                .setBackColor(Color.BLACK)
                .setCharset(StandardCharsets.UTF_8)
                .setShapeHint(SymbolShapeHint.FORCE_SQUARE)
                .setWidth(5)
                .setHeight(5)
                .setMargin(1);
        final BitMatrix bitMatrix = QrCodeUtil.encode(decode, qrConfig);
        for (int j = 0; j < bitMatrix.getHeight(); j++) {
            for (int i = 0; i < bitMatrix.getWidth(); i++) {
                if (bitMatrix.get(i, j)) {
                    System.out.print("■");
                } else {
                    System.out.print("  ");
                }

            }
            System.out.println();
        }
        System.out.println();
        System.out.println();

        String asciiArt = QrCodeUtil.generateAsAsciiArt(decode,qrConfig);
        System.out.println(asciiArt);

        System.out.println("请扫码登录! " + url);

        return null;
    }
}
