package android.app.smdt.httputil;

public class Download {

/*
    public class MainActivity extends Activity {

        //下载文件时开启线程的个数
        static int ThreadCount = 3;

        //下载结束的线程的个数
        static int finishedThread = 0;

        //用于记录下载进度
        int currentProgress;

        //下载文件的文件名
        String fileName = "python-2.7.5.amd64.msi";

        //确定下载地址
        String path = "http://192.168.0.101:8080/app/" + fileName;

        //pb对象用于在进度条中设置下载进度
        private ProgressBar pb;

        //tv对象用于在TextView中显示下载进度
        TextView tv;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            pb = (ProgressBar) findViewById(R.id.pb);
            tv = (TextView) findViewById(R.id.tv);
        }

        //创建一个消息处理器对象
        Handler handler = new Handler() {

            //在主线程中处理从子线程中发送过来的消息
            public void handleMessage(android.os.Message msg) {

                //刷新TextView中显示的下载进度
                tv.setText((long)pb.getProgress() * 100 / pb.getMax() + "%");
            }
        };

        //下载文件按钮响应函数
        public void download(View v){

            //创建一个子线程,用于下载文件
            Thread t = new Thread() {

                //执行子线程(下载文件)
                @Override
                public void run() {

                    try {

                        //将下载地址封装成URL对象
                        URL url = new URL(path);

                        //创建连接对象,此时未建立连接
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        //设置请求方式为get请求
                        conn.setRequestMethod("GET");

                        //设置连接超时
                        conn.setConnectTimeout(5000);

                        //设置读取超时
                        conn.setReadTimeout(5000);

                        //如果请求成功
                        if(conn.getResponseCode() == 200) {

                            //获得需要下载的文件的长度
                            int length = conn.getContentLength();

                            //设置进度条的最大值就是原文件的总长度
                            pb.setMax(length);

                            //创建File对象
                            File file = new File(Environment.getExternalStorageDirectory(), fileName);

                            //创建临时文件
                            RandomAccessFile raf = new RandomAccessFile(file, "rwd");

                            //设置临时文件的大小
                            raf.setLength(length);

                            //关闭临时文件
                            raf.close();

                            //计算出每个线程应该下载多少字节
                            int size = length / ThreadCount;

                            //遍历下载线程
                            for (int i = 0; i < ThreadCount; i++) {

                                //计算线程下载的开始位置
                                int startIndex = i * size;

                                //计算线程下载的结束位置
                                int endIndex = (i + 1) * size - 1;

                                //如果是最后一个线程，那么结束位置写死
                                if(i == ThreadCount - 1) {
                                    endIndex = length - 1;
                                }

                                //创建下载线程
                                DownLoadThread thread = new DownLoadThread(startIndex, endIndex, i);

                                //启动下载线程
                                thread.start();
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }

        //创建一个继承自线程类的下载线程类(其实是一个内部类)
        class DownLoadThread extends Thread {

            //下载开始的位置
            int startIndex;

            //下载结束的位置
            int endIndex;

            //下载线程的id
            int threadId;

            //下载线程类的构造方法
            public DownLoadThread(int startIndex, int endIndex, int threadId) {
                super();
                this.startIndex = startIndex;
                this.endIndex = endIndex;
                this.threadId = threadId;
            }

            //执行下载线程
            @Override
            public void run() {

                try {

                    //创建进度临时文件
                    File progressFile = new File(Environment.getExternalStorageDirectory(), threadId + ".txt");

                    //如果SD卡中存在进度临时文件
                    if(progressFile.exists()) {

                        //创建文件输出流
                        FileInputStream fis = new FileInputStream(progressFile);

                        //InputStreamReader:创建输输入流缓冲区
                        //BufferedReader:创建读取缓冲区
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        //从进度临时文件中读取出上一次下载的总进度，然后与原本的开始位置相加，得到新的开始位置
                        int lastProgress = Integer.parseInt(br.readLine());

                        //设置初始位置
                        startIndex += lastProgress;

                        //把上次下载的进度显示至进度条
                        currentProgress += lastProgress;
                        pb.setProgress(currentProgress);

                        //发送消息，让主线程刷新文本进度
                        handler.sendEmptyMessage(1);

                        //关闭文件输入流
                        fis.close();
                    }

                    System.out.println("线程" + threadId + "的下载区间是：" + startIndex + "---" + endIndex);

                    //将下载地址封装成URL对象
                    URL url = new URL(path);

                    //创建连接对象,此时未建立连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    //设置请求方式为get请求
                    conn.setRequestMethod("GET");

                    //设置连接超时
                    conn.setConnectTimeout(5000);

                    //设置读取超时
                    conn.setReadTimeout(5000);

                    //设置本次http请求所请求的数据的区间
                    conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);

                    //请求部分数据，相应码是206
                    if(conn.getResponseCode() == 206) {

                        //流里此时只有1/3原文件的数据
                        InputStream is = conn.getInputStream();

                        byte[] b = new byte[1024];
                        int len = 0;
                        int total = 0;

                        //拿到临时文件的输出流
                        File file = new File(Environment.getExternalStorageDirectory(), fileName);

                        //使用临时文件输出流创建临时文件
                        RandomAccessFile raf = new RandomAccessFile(file, "rwd");

                        //把文件的写入位置移动至startIndex
                        raf.seek(startIndex);

                        while((len = is.read(b)) != -1) {

                            //每次读取流里数据之后，同步把数据写入临时文件
                            raf.write(b, 0, len);
                            total += len;

                            System.out.println("线程" + threadId + "下载了" + total);

                            //每次读取流里数据之后，把本次读取的数据的长度显示至进度条
                            currentProgress += len;
                            pb.setProgress(currentProgress);

                            //发送消息，让主线程刷新文本进度
                            handler.sendEmptyMessage(1);

                            //生成一个专门用来记录下载进度的临时文件
                            RandomAccessFile progressRaf = new RandomAccessFile(progressFile, "rwd");

                            //每次读取流里数据之后，同步把当前线程下载的总进度写入进度临时文件中
                            progressRaf.write((total + "").getBytes());

                            //关闭临时文件
                            progressRaf.close();
                        }

                        System.out.println("线程" + threadId + "下载完毕-------------------小志参上！");

                        //关闭临时文件
                        raf.close();

                        //下载结束的进程个数加1
                        finishedThread++;

                        synchronized (path) {

                            //如果所有的下载进程都下载结束
                            if(finishedThread == ThreadCount) {

                                //遍历下载进度
                                for (int i = 0; i < ThreadCount; i++) {

                                    //获取下载进度所在的文件
                                    File f = new File(Environment.getExternalStorageDirectory(), i + ".txt");

                                    //删除保存下载进度的临时文件
                                    f.delete();
                                }

                                //下载结束的下载线程的个数设置为0
                                finishedThread = 0;
                            }
                        }
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
*/
    }
