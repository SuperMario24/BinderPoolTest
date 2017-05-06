# BinderPoolTest
多个进程间AIDL通信

1个Service实现多个不同业务模块进行AIDL通信

AIDL流程：
1）.创建一个service和一个AIDL接口
2）.创建一个类继承AIDL接口中的Stub类，并实现其抽象方法。
3).在Service的onBind方法中，返回这个类的对象。
4).客户端进行绑定Service，建立连接，调用方法。


1.核心思想：根据业务模块的特征，返回对应的Binder。
2.需要进行线程同步。
