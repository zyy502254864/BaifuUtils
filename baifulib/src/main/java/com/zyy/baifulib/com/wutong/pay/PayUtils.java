package com.zyy.baifulib.com.wutong.pay;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;


import com.google.gson.annotations.SerializedName;
import com.pax.pay.service.aidl.PayHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * Data:
 * 百富支付和打印的工具类，单例
 *
 * @author ZhangYangYang
 * @date 2018/1/9
 */

public class PayUtils {
    private static PayUtils baifuUtils = new PayUtils();
    private PayHelper payHelper;
    Handler handler = new Handler();
    BaifuCallBack callBack;
    private String appID = "com.wutong.android.pos";
    private Context context;

    public static PayUtils getInstance() {
        return baifuUtils;
    }

    private PayUtils() {
    }

    /**
     * 设置回调
     *
     * @param callBack 回调对象
     */
    public void setCallBack(BaifuCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 解除Service绑定，请在context销毁前绑定
     */
    public void unbindService() {
        context.unbindService(conn);
        payHelper = null;
    }

    /**
     * 绑定服务
     *
     * @param context 建议传Application
     */
    public void initService(Context context) {

        if (payHelper != null) {
            Toast.makeText(context, "服务已绑定，请不要重复操作", Toast.LENGTH_SHORT).show();
            return;
        }
        this.context = context;
        Intent intent = new Intent();
        intent.setAction("com.pax.pay.SERVICE");
        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    /**
     * 绑定服务
     *
     * @param context 建议传Application
     * @param appID   设置APPid
     */
    public void initService(Context context, String appID) {
        this.appID = appID;
        initService(context);
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            payHelper = PayHelper.Stub.asInterface(arg1);
            Toast.makeText(context, "service connected OK", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(context, "绑定失败", Toast.LENGTH_SHORT).show();
        }

    };

    /**
     * 银行卡收款
     *
     * @param amt 收款金额
     */
    public boolean cardPay(String amt) {
        if (callBack == null) {
            return false;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("transType", "SALE");
            object.put("transAmount", amt);
            object.put("appId", appID);
            sendMsg(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 支付宝收款
     *
     * @param amt 收款金额
     */
    public boolean scanPayAli(String amt) {
        if (callBack == null) {
            return false;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("transType", "SALE_POS_SCAN_PHONE");
            json.put("transAmount", amt);
            json.put("scanSaleChannel", "alipay");
            json.put("appId", appID);
            sendMsg(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 微信收款
     *
     * @param amt 收款金额
     */
    public boolean scanPayWeChat(String amt) {
        if (callBack == null) {
            return false;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("transType", "SALE_POS_SCAN_PHONE");
            json.put("transAmount", amt);
            json.put("scanSaleChannel", "wechat");
            json.put("appId", appID);
            sendMsg(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 打印
     *
     * @param bitmap 待打印的图片
     */
    public void print(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("ZYY", "图片为空");
            return;
        }
        JSONObject json = new JSONObject();
        String string = convertIconToString(bitmap);
        Log.e("ZYY", string);
        try {
            json.put("transType", "PRN_BITMAP");
            json.put("appId", appID);
            json.put("bitmap", string);
            sendMsg(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void doTrans(JSONObject jsonObject) {
        sendMsg(jsonObject.toString());
    }

    private void sendMsg(final String data) {
        if (payHelper == null) {
            if (callBack != null) {
                callBack.fail("请重新绑定服务", -1);
            }
            return;
        }
        Log.e("ZYY", data);
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        scheduledThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String s = payHelper.doTrans(data);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callBack != null) {
                                callBack.result(s);
                            }
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public interface BaifuCallBack {
        /**
         * 调用结果
         *
         * @param result json格式
         */
        void result(String result);

        /**
         * 错误时调用。
         *
         * @param result 文字描述
         * @param code   错误代码  目前只有一个-1（payHelper==null）
         */
        void fail(String result, int code);
    }

    /**
     * 调用结果解析类
     */
    public class Result {

        /**
         * appId : com.wutong.pay
         * rspCode : 0
         * merchName :    北京物通时空网络科技开发有限公司
         * merchId : 940491048160013
         * termId : 92652009
         * voucherNo : 000058
         * batchNo : 000001
         * isserCode : 00000000000
         * acqCode : 00000000000
         * refNo : 801089206246
         * transTime : 153249
         * transDate : 0110
         * transAmount : 000000000001
         */
        /**
         * 请求结果 0 成功，其它参考文档
         */
        @SerializedName("rspCode") private int rspCode;
        /**
         * 商户名称
         */
        @SerializedName("merchName") private String merchName;
        /**
         * 商户ID
         */
        @SerializedName("merchId") private String merchId;
        /**
         * 终端编号
         */
        @SerializedName("termId") private String termId;
        /**
         * 凭证号
         */
        @SerializedName("voucherNo") private String voucherNo;
        /**
         * 批次号
         */
        @SerializedName("batchNo") private String batchNo;
        /**
         * 发卡行号
         */
        @SerializedName("isserCode") private String isserCode;
        /**
         * 收单行号
         */
        @SerializedName("acqCode") private String acqCode;
        /**
         * 参考号
         */
        @SerializedName("refNo") private String refNo;
        /**
         * 交易时间
         */
        @SerializedName("transTime") private String transTime;
        /**
         * 交易日期
         */
        @SerializedName("transDate") private String transDate;
        /**
         * 交易金额 单位：分
         */
        @SerializedName("transAmount") private String transAmount;


        public int getRspCode() { return rspCode;}

        public void setRspCode(int rspCode) { this.rspCode = rspCode;}

        public String getMerchName() { return merchName;}

        public void setMerchName(String merchName) { this.merchName = merchName;}

        public String getMerchId() { return merchId;}

        public void setMerchId(String merchId) { this.merchId = merchId;}

        public String getTermId() { return termId;}

        public void setTermId(String termId) { this.termId = termId;}

        public String getVoucherNo() { return voucherNo;}

        public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo;}

        public String getBatchNo() { return batchNo;}

        public void setBatchNo(String batchNo) { this.batchNo = batchNo;}

        public String getIsserCode() { return isserCode;}

        public void setIsserCode(String isserCode) { this.isserCode = isserCode;}

        public String getAcqCode() { return acqCode;}

        public void setAcqCode(String acqCode) { this.acqCode = acqCode;}

        public String getRefNo() { return refNo;}

        public void setRefNo(String refNo) { this.refNo = refNo;}

        public String getTransTime() { return transTime;}

        public void setTransTime(String transTime) { this.transTime = transTime;}

        public String getTransDate() { return transDate;}

        public void setTransDate(String transDate) { this.transDate = transDate;}

        public String getTransAmount() { return transAmount;}

        public void setTransAmount(String transAmount) { this.transAmount = transAmount;}
    }


    private String convertIconToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }
}
