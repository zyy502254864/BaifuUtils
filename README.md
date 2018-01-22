# 百富A920工具类

用于百富A920的支付和打印  

## 支付
原理：通过绑定service，实例化aidl调用其中的方法进行传递数据。数据传递时子线程中完成

* 通过PayUtils.getInstance()方法获取 PayUtils对象。
* 调用该对象的initService(Context context)方法进行初始化  
*** 当context被销毁时应调用unbindService()方法解除Service的绑定 ***
* 通过setCallBack(BaifuCallBack callBack)设置支付回调
* 根据需求调用下方收款方法进行收款
  1. 银行卡收款 cardPay(String amt)
  2. 支付宝收款 scanPayAli(String amt)
  3. 微信收款  scanPayWeChat(String amt)
