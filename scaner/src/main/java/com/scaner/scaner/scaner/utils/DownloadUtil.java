package com.scaner.scaner.scaner.utils;

import android.os.Handler;
import android.text.TextUtils;

import com.zjrb.core.R;
import com.zjrb.core.api.okhttp.OkHttpUtils;
import com.zjrb.core.utils.PathUtil;
import com.zjrb.core.utils.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author wanglinjie
 * @date 2018/4/17
 * 文件下载工具
 */
final public class DownloadUtil {
    private static DownloadUtil mInstance;
    private final OkHttpClient okHttpClient;
    private final Handler mainThreadHandler;
    private OnDownloadListener mListener;
    private static String fileName;
    private static String dir;

    public static DownloadUtil get() {
        if (mInstance == null) {
            synchronized (DownloadUtil.class) {
                if (mInstance == null)
                    mInstance = new DownloadUtil();
            }
        }
        fileName = "";
        dir = "";
        return mInstance;
    }

    private DownloadUtil() {
        okHttpClient = OkHttpUtils.getClient();
        mainThreadHandler = new Handler();
    }

    public DownloadUtil setDir(String dir) {
        this.dir = dir;
        return this;
    }

    public DownloadUtil setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DownloadUtil setListener(OnDownloadListener listener) {
        this.mListener = listener;
        return this;
    }

    /**
     * 判断字符串是否为URL
     *
     * @param urls 用户头像key
     * @return true:是URL、false:不是URL
     */
    public boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());//比对
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();//判断是否匹配
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }


    public void download(String url) {
        if (TextUtils.isEmpty(url)) {
            onFail(UIUtils.getString(R.string.error_invalid_apk_url));
            return;
        }
        if (TextUtils.isEmpty(fileName))
            fileName = getNameFromUrl(url);
        if (TextUtils.isEmpty(fileName)) {
            onFail(UIUtils.getString(R.string.error_invalid_apk_url));
            return;
        }
        if (TextUtils.isEmpty(dir))
            dir = PathUtil.getDownloadPath();
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onFail(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                FileOutputStream fos = null;
                byte[] buff = new byte[2048];
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(dir, fileName);
                    if (!file.exists())
                        file.createNewFile();
                    fos = new FileOutputStream(file);
                    int len;
                    int sum = 0;
                    while ((len = is.read(buff)) != -1) {
                        fos.write(buff, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        onLoading(progress);
                    }
                    fos.flush();
                    onSuccess(dir + File.separator + fileName);
                } catch (Exception e) {
                    onFail(e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void onSuccess(final String path) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onSuccess(path);
                }
            }
        });
    }

    private void onLoading(final int progress) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onLoading(progress);
                }
            }
        });
    }

    private void onFail(final String err) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onFail(err);
                }
            }
        });
    }

    /**
     * 从下载连接中解析出文件名
     *
     * @param url
     * @return 文件名
     */
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载进度
         *
         * @param progress 进度
         */
        void onLoading(int progress);

        /**
         * 下载成功
         *
         * @param path 文件路径
         */
        void onSuccess(String path);

        /**
         * 下载失败
         *
         * @param err 错误信息
         */
        void onFail(String err);
    }
}
