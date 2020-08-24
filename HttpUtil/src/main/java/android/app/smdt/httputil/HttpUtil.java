package android.app.smdt.httputil;

import android.util.Log;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *                                              网络命令表
 * 网络传输命令
 * 序号    命令名称                               英文名                    命令值                     方向
 * 1.       登录                                  Login                      0                       C --> S
 * 2.      心跳请求                               HeartBea                    1                       C --> S
 * 3.      获取终端配置                            Config                     2                       S --> C
 * 4.      设置重启                                Reboot                    3                        S --> C
 * 5.      设置系统时间                            SetTime                    4                        S-->C
 * 6.      设置定时开关机                         SetOnOff                    5                        S --> C
 * 7.      获取远程播放布局以及远程数据              Player                     6
 * 8.      截屏                                  ScreenShot                  7

 * 创建一个okHttp的应用
 * 1.添加OKHTTP3的依赖
 *   compile 'com.squareup.okhttp3:okhttp:3.7.0'
 *   compile 'com.squareup.okio:okio:1.12.0' 这个不能加，否则编译不能通过。
 */
 public class HttpUtil {
     private static final String TAG =  HttpUrl.class.getSimpleName();
     private static final boolean DebugEnabled = true;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POSt";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";

    private static final String FILE_TYPE_FILE = "file/*";
    private static final String FILE_TYPE_IMAGE = "image/*";
    private static final String FILE_TYPE_AUDIO = "audio/*";
    private static final String FILE_TYPE_VIDEO = "video/*";
    //创建连接超时
    private static final int CONNECT_TIMEOUT = 60;
    //创建读取超时
    private static final int READ_TIMEOUT = 100;
    //创建写超时
    private static final int WRITE_TIMEOUT = 60;
    private  OkHttpClient okHttpClient;

    private final HashMap<String,List<Cookie>> cookieStore = new HashMap<String,List<Cookie>>();

    public HttpUtil(int connectTimeOut,int readTimeOut,int writeTimeOut) {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(),cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
    }

    public HttpUtil() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    /**
     * GET 异步调用
     *
     * @param url
     * @param callback
     */
    public void okHttpGetAsyn(String url, Callback callback) {
        //1.创建一个OkHttpClient对象
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
        //2.创建一个请求request
        Request req = new Request.Builder()
                .url(url)
                .method(METHOD_GET, null)
                .build();
        //3.创建一个Call
        Call call = okHttpClient.newCall(req);
        //4.将请求call 加入调用
        call.enqueue(callback);
    }

    /**
     * GET的同步调用
     *
     * @param url
     */
    public void okHttpGetSync(String url) {
        //1.创建一个okHttpClient对象
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
        //2.创建一个请求
        Request req = new Request.Builder()
                .url(url)
                .method(METHOD_GET, null)
                .build();
        //3.创建一个Call
        final Call call = okHttpClient.newCall(req);
        //4.进行同步调用,不过需要运行在独立线程中
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response resp = call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 异步使用get方式下载文件到mFile中
     *
     * @param url
     * @param mFile
     */
    public void okHttpDownLoadByGetAsync(String url, final String mFile) {
        //1.创建OkHttpClient对象
        okHttpClient = new OkHttpClient();
        //2.创建一个Request对象
        Request reqest = new Request.Builder()
                .url(url)
                .get()
                .build();
        //3.创建一个Call对象，参数就是request
        Call call = okHttpClient.newCall(reqest);
        //4.加入到队列
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("okHttp", call.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取字节流
                InputStream is = response.body().byteStream();
                //获取输出文件字节流
                FileOutputStream outs = new FileOutputStream(mFile);
                //开始获取
                int len = 0;
                byte[] buf = new byte[128];
                while ((len = is.read(buf)) != -1) {
                    outs.write(buf, 0, len);
                    outs.flush();
                }
                outs.close();
                is.close();
            }
        });
    }

    /**
     * post发送表单 键值对发送
     *
     * @param url
     * @param keyPair
     * @param callback
     */
    public void okHttpPostByKeyPair(String url, Map<String, String> keyPair, Callback callback) {
        if (null == keyPair || keyPair.isEmpty()) {
            return;
        }
        //1.创建一个OkHttpClient的对象
        okHttpClient = new OkHttpClient();
        //2.通过new FormBody(）调用builder()方法，创建一个请求体RequestBody.可以通过add添加键值对
        FormBody.Builder builder = new FormBody.Builder();
        Iterator<String> ite = keyPair.keySet().iterator();
        while (ite.hasNext()) {
            String key = ite.next();
            String val = keyPair.get(key);
            builder.add(key, val);
        }
        RequestBody body = builder.build();
        //3.创建一个Request请求对象。参数body作为post命令中
        Request reqest = new Request.Builder()
                .url(url)
                .method(METHOD_POST, body)
                .build();
        //4.创建一个Call对象，参数就是request
        Call call = okHttpClient.newCall(reqest);
        //5.加入到调度
        call.enqueue(callback);
    }

    /**
     * @param url
     * @param json     这个是一个json的对象。需要转换一下
     * @param callback
     */
    public void okHttpPostByJson(String url, JSONObject json, okhttp3.Callback callback) {
        //创建一个转换类型
        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
        //将json转换成字符串
        String value = json.toString();
        //1.创建一个OkHttpClient
        okHttpClient = new OkHttpClient();
        //2.通过RequestBody.create 创建ReqestBody 对象
        RequestBody requestBody = RequestBody.create(mediaType, value);
        //3.创建一个Request对象，
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //4.创建一个Call 对象
        Call call = okHttpClient.newCall(request);
        //5.加入到队列
        call.enqueue(callback);
    }

    /**
     * 上传一个文件
     *
     * @param url
     * @param mFile
     * @param callback
     */
    public void okHttpPostByFile(String url, File mFile, Callback callback) {
        //创建一个mediaType
        MediaType mediaType = MediaType.parse("application/octet-stream");
        //1.创建一个OkHttpClient
        okHttpClient = new OkHttpClient();
        //2.创建一个RequestBody。使用create来创建
        RequestBody body = RequestBody.create(mediaType, mFile);
        //3.创建一个Request对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        //4.创建一个Call对象
        Call call = okHttpClient.newCall(request);
        //5.调入到队列
        call.enqueue(callback);
    }

    public void upLoadByMultipartAsync(String url, Map<String, String> keyPair, File imgFile, Callback callback) {
        //1.创建OkHttpClient对象
        okHttpClient = new OkHttpClient();
        //2创建一个请求体，这里使用Multipart来创建
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //3.设置类型是表单
        builder.setType(MultipartBody.FORM);
        //4.加入表单中的参数
        Iterator<String> ite = keyPair.keySet().iterator();
        while (ite.hasNext()) {
            String key = ite.next();
            String val = keyPair.get(key);
            builder.addFormDataPart(key, val);
        }
        builder.addFormDataPart("image", "1.png", RequestBody.create(MediaType.parse("image/png"), imgFile));
        RequestBody body = builder.build();
        //5.创建一个reqest
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        //6.创建一个Call
        Call call = okHttpClient.newCall(request);
        //7.设置
        call.enqueue(callback);
    }
}

