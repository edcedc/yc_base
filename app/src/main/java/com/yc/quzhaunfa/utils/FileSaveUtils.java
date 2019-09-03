package com.yc.quzhaunfa.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import android.text.format.Formatter;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.yc.quzhaunfa.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 作者：yc on 2018/10/22.
 * 邮箱：501807647@qq.com
 * 版本：v1.0
 */

public class FileSaveUtils {

    private static NotificationManager notificationManager;
    private static Notification notification; //下载通知进度提示
    private static NotificationCompat.Builder builder;
    private boolean flag = false; //进度框消失标示 之后发送通知

    /**
     *  保存本地指定路径加刷新图库
     * @param act
     * @param imgBitmap
     * @param name
     */
    public static boolean save(final Context act, final Bitmap imgBitmap, String name){
        boolean orExistsDir = FileUtils.createOrExistsDir(Constants.imgUrl);
        if (orExistsDir){
            String fileName = Constants.imgUrl + name + ".png";
            if (imgBitmap != null && !imgBitmap.isRecycled()){
                boolean save = com.blankj.utilcode.util.ImageUtils.save(imgBitmap, fileName, Bitmap.CompressFormat.PNG, true);
                if (save){
                    MediaScannerConnection.scanFile(act,
                            new String[]{fileName},
                            new String[]{"image/png"},
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    LogUtils.e(path);
                                    Glide.get(act).getBitmapPool().put(imgBitmap);
                                    ToastUtils.showShort("下载成功");
                                }
                            });
                }
            }else {
                ToastUtils.showShort("下载成功");
            }
        }
        return orExistsDir;
    }


    public static void saveVideo(final Context act, final String fileUrl, final String name){
        boolean orExistsDir = FileUtils.createOrExistsDir(Constants.mainPath);
        if (orExistsDir) {
            final String videoPath = Constants.videoUrl;
            boolean orExistsDir1 = FileUtils.createOrExistsDir(videoPath);
            if (orExistsDir1){
                ToastUtils.showShort("正在下载，请稍后...");
                Observable.create(new ObservableOnSubscribe<Progress>() {
                            @Override
                            public void subscribe(@NonNull final ObservableEmitter<Progress> e) throws Exception {
                                OkGo.<File>get(fileUrl)//
                                        .execute(new FileCallback(videoPath, name + ".mp4") {
                                            @Override
                                            public void onSuccess(Response<File> response) {
                                                e.onComplete();
                                            }

                                            @Override
                                            public void onError(Response<File> response) {
                                                e.onError(response.getException());
                                            }

                                            @Override
                                            public void downloadProgress(Progress progress) {
                                                e.onNext(progress);
                                            }
                                        });
                            }
                        })
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(@NonNull Disposable disposable) throws Exception {
                                        LogUtils.e("正在下载中...");
                                        initNotification(act);
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())//
                                .subscribe(new Observer<Progress>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
//                                        addDisposable(d);
                                    }

                                    @Override
                                    public void onNext(@NonNull Progress progress) {
                                        String downloadLength = Formatter.formatFileSize(act, progress.currentSize);
                                        String totalLength = Formatter.formatFileSize(act, progress.totalSize);
                                        LogUtils.e(downloadLength + "/" + totalLength);
                                        String speed = Formatter.formatFileSize(act, progress.speed);
//                                        tvNetSpeed.setText(String.format("%s/s", speed));
//                                        tvProgress.setText(numberFormat.format(progress.fraction));
//                                        pbProgress.setMax(10000);
//                                        pbProgress.setProgress((int) (progress.fraction * 10000));

                                        builder.setProgress(100, (int) (progress.fraction * 100), false);
                                        builder.setContentText("下载进度:" + (int) (progress.fraction * 100) + "%");
                                        notification = builder.build();
                                        notificationManager.notify(1, notification);
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        e.printStackTrace();
                                        LogUtils.e("下载出错", e.getMessage());
                                        ToastUtils.showShort("下载出错", e.getMessage());
                                    }

                                    @Override
                                    public void onComplete() {
                                        LogUtils.e("下载完成");

                                        builder.setContentTitle("下载完成")
                                                .setContentText("")
                                                .setAutoCancel(true);//设置通知被点击一次是否自动取消
                                        notification = builder.build();
                                        notificationManager.notify(1, notification);
                                    }
                                });
            }
        }
    }

    //初始化通知
    private static void initNotification(Context act) {
        notificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(act);
        builder.setContentTitle("正在下载...") //设置通知标题
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(act.getResources(), R.mipmap.ic_launcher)) //设置通知的大图标
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(true)//设置通知被点击一次是否自动取消
                .setContentText("下载进度:" + "0%")
                .setProgress(100, 0, false);
        notification = builder.build();//构建通知对象
    }

    public static boolean save(File file, String name){
        boolean orExistsDir = FileUtils.createOrExistsDir(Constants.imgUrl);
        if (orExistsDir){
            String fileName = Constants.imgUrl + name + ".png";
            copy(file, new File(fileName));
            LogUtils.e(fileName);
            ToastUtils.showShort("下载成功");
        }
        return orExistsDir;
    }

    /**
     * 复制文件
     * @param source 输入文件
     * @param target 输出文件
     */
    private static void copy(File source, File target) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
