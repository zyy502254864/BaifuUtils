package com.zyy.baifulib.com.wutong.pay;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.pax.api.PrintException;
import com.pax.api.PrintManager;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.impl.GL;


/**
 * Created by lhg on 2017/5/17.
 */

public class Printer {

    public static final int FONT_BIG = 30;
    public static final int FONT_NORMAL = 24;
    public static final int FONT_SMALL = 20;

    static String showDate(String date) {
        if (TextUtils.isEmpty(date) || date.length() != 8) {
            return "";
        }
        StringBuilder sb = new StringBuilder(date);
        sb.insert(6, "/");
        sb.insert(4, "/");
        return sb.toString();
    }

    static String showTime(String time) {
        if (TextUtils.isEmpty(time) || time.length() != 6) {
            return "";
        }
        StringBuilder sb = new StringBuilder(time);
        sb.insert(4, ":");
        sb.insert(2, ":");
        return sb.toString();
    }
    private static GL instance;
    public static GL getGL(Context context) {
         instance = GL.getInstance(context);
        return instance;
    }

    //384宽度
    public static String print(int no, final Bitmap bitmap, boolean rePrint) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                IImgProcessing.IPage page = instance.getImgProcessing().createPage();
                page.addLine().addUnit(bitmap);
                String error = printPage(page);
                bitmap.recycle();

            }
        }.start();
        return "";
    }


    public static String printPage(IImgProcessing.IPage page) {
        int ret = 0;
        try {
            IImgProcessing imgProcessing = instance.getImgProcessing();
            Bitmap bitmap = imgProcessing.pageToBitmap(page, 384);

            PrintManager manager = PrintManager.getInstance();
            manager.prnInit();
            manager.prnSetGray(20);
            manager.prnBitmap(bitmap);
            manager.prnStart();
            manager.prnClose();

            bitmap.recycle();
        } catch (PrintException e) {
            e.printStackTrace();
            ret = e.exceptionCode;
        }
        return getError(ret);
    }

    public static String getError(int ret) {
        switch (ret) {
            case 1:
                return "打印机忙， 请等待...";
            case 2:
                return "打印机缺纸, 请装纸...";
            case 8:
                return "打印机过热, 请稍等...";
            case 9:
                return "打印机电压过低";
            default:
                return null;
        }
    }

}
