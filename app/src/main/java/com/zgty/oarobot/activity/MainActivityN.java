package com.zgty.oarobot.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.baidu.ocr.ui.camera.CameraNativeHelper;
import com.baidu.ocr.ui.camera.CameraView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.zgty.oarobot.R;
import com.zgty.oarobot.bean.Account;
import com.zgty.oarobot.bean.IdCard;
import com.zgty.oarobot.bean.Speaking;
import com.zgty.oarobot.bean.Staff;
import com.zgty.oarobot.bean.Time;
import com.zgty.oarobot.bean.Visitor;
import com.zgty.oarobot.camera.CameraSourcePreview;
import com.zgty.oarobot.camera.GraphicOverlay;
import com.zgty.oarobot.common.CommonActivity;
import com.zgty.oarobot.common.Constant;
import com.zgty.oarobot.dao.AccountDaoUtils;
import com.zgty.oarobot.dao.IdCardDaoUtils;
import com.zgty.oarobot.dao.SpeekDaoUtils;
import com.zgty.oarobot.dao.StaffDaoUtils;
import com.zgty.oarobot.dao.TimeDaoUtils;
import com.zgty.oarobot.dao.VisitorDaoUtils;
import com.zgty.oarobot.dao.WorkOnOffDaoUtils;
import com.zgty.oarobot.receiver.DateTimeReceiver;
import com.zgty.oarobot.service.RefreshService;
import com.zgty.oarobot.util.FileUtil;
import com.zgty.oarobot.util.FileUtils;
import com.zgty.oarobot.util.FucUtil;
import com.zgty.oarobot.util.IdentifyFace2;
import com.zgty.oarobot.util.JsonParser;
import com.zgty.oarobot.util.LogToastUtils;
import com.zgty.oarobot.util.MemoryManager;
import com.zgty.oarobot.util.TimeUtils;
import com.zgty.oarobot.util.WXCPUtils;
import com.zgty.oarobot.widget.MyToast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.vision.face.FaceDetector.FAST_MODE;
import static com.google.android.gms.vision.face.FaceDetector.NO_LANDMARKS;
import static com.zgty.oarobot.common.Constant.MAIN_CHECK_CAMERA_TYPE;
import static com.zgty.oarobot.common.OARobotApplication.canUserGoogleTTS;
import static com.zgty.oarobot.common.OARobotApplication.getInstance;
import static com.zgty.oarobot.common.OARobotApplication.isNeedId;
import static com.zgty.oarobot.common.OARobotApplication.isNeedVoice;
import static com.zgty.oarobot.common.OARobotApplication.mSpeech;
import static com.zgty.oarobot.common.OARobotApplication.mTts;

public class MainActivityN extends CommonActivity implements View.OnClickListener {

    private TextView change_mode;
    private TextView name_staff;
    private TextView id_staff;
    private TextView sign_up_time;
    private TextView station_state;
    private TextView waiting_text;
    private TextView robot_state_text;
    private TextView name_part;
    private TextView canuse;
    private TextView allram;
    private TextView robot_speak_text;
    private TextView setting_main;
    private LinearLayout layout_robot;
    private WebView robot_webview;


    private IdentifyFace2 identifyFace;  //识别工具，待分离
    private String userid;//用户ID
    private String userid1;//用户ID,用来联系
    private String username;//用户name

    //时间
    private double timeon;
    private double timeonlate;
    private double timeoffearly;
    private double timeoff;
    private double timeadd;
    private double timenow;
    // 语音听写对象
    private SpeechRecognizer mIat;//待归类
    private int noanswer = 0;
    private int time_second;

    private static final String TAG = MainActivityN.class.getSimpleName();

    private SpeekDaoUtils speekDaoUtils;
    private File file;
    private Intent intentRefreshService;
    private RefreshListBroadCast listBroadCast;
    private WXCPUtils wxcpUtils;
    private boolean canConnect = true;
    private Handler handler1;
    private Runnable runnable;
    private int hearingType;
    private boolean isFirstConnect;
    private String visitorId;

    private CameraSource mCameraSource = null;
    private GraphicFaceTrackerFactory graphicFaceTrackerFactory;
    private GraphicFaceTracker graphicFaceTracker;

    // 本地语法文件
    private static String mLocalGrammar = null;
    public static final String GRAMMAR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test";


    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private RatingBar rb_normal;
    private SoundPool soundPool = new SoundPool(50, 0, 5);

    private static final int RC_HANDLE_GMS = 9001;
    private static final int REQUEST_CODE_CAMERA = 102;
    private long num;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    //全部机器人说的话都写到异步中
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0://识别成功
                    initData();
                    break;
                case 1:
                    robotSpeek(speekDaoUtils.querySpeekingText("cannotRecognise"), 1, 8, 0);
//                    soundPool.play(8,1, 1, 0, 0, 1);
                    hearingType = 0;
                    isFirstConnect = true;
                    break;
                case 2:
                    mIat = SpeechRecognizer.createRecognizer(getApplicationContext(), mInitListener);
                    makelocalGrammar();
                    setRecongizeParam();
                    initLoadSound();
                    robotSpeek("欢迎使用中广通业考勤接待系统", 0, 1, 0);
                    break;
                case 3:
                    List<Visitor> visitors = new VisitorDaoUtils(getApplicationContext()).queryVisitorList(userid);
                    if (visitors != null && visitors.size() > 0) {
                        Visitor visitor = visitors.get(0);
//                        if (TimeUtils.compareAfter(visitor.getTime(), 30) < 0) {
//                            return;
//                        }
                        String id = visitor.getVisit_id();
                        userid1 = id;
                        isFirstConnect = false;
                        Staff staff = new StaffDaoUtils(getApplicationContext()).queryStaffList(id).get(0);
                        username = staff.getName_user();
                        robotSpeek(String.format("您是否联系%s?", staff.getName_user()), 1, 18, Integer.valueOf(staff.getId_clerk()));
                        hearingType = 1;
                    }
                    break;
                case 4:
                    layout_robot.bringToFront();

                    break;
                case 5:
                    robot_webview.bringToFront();
//                    robot_webview.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");

                    break;
                case 6:
                    WebSettings settings = robot_webview.getSettings();
                    settings.setJavaScriptEnabled(true);//设置js支持
                    settings.setAllowFileAccess(true);
                    settings.setPluginState(WebSettings.PluginState.ON);
                    robot_webview.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
                            robot_webview.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");
                        }
                    });
//        settings.setPluginState(true);
//        settings.setSupportZoom(true);//设置缩放支持
//        settings.setDatabaseEnabled(true);//设置界面存储到数据库
//        settings.setDomStorageEnabled(true);//设置界面支持缓存
                    robot_webview.setWebChromeClient(new WebChromeClient());

//        robot_webview.loadUrl("http://www.baidu.com");//设置网址
                    robot_webview.loadUrl("http://192.168.18.220:8080/advertising-0.0.1-SNAPSHOT/gg/index");//设置网址
//                    robot_webview.loadUrl("http://192.168.18.77:8080/advertising-0.0.1-SNAPSHOT/gg/index");//设置网址
//                    MemoryManager memoryManager=new MemoryManager();
//                    allram.setText(memoryManager.getTotalMemory(getApplicationContext()));
//                    canuse.setText(memoryManager.getAvailMemory(getApplicationContext()));
                    break;
                case 7:
//                    robot_webview.bringToFront();
                    robot_webview.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");

                    break;
                case 8:
                    //跳转到声纹识别
//                    LogToastUtils.toastShort(getApplicationContext(), "声纹识别");
//                    num = (long) (Math.random() * 10000);
//                    new MyToast(getApplicationContext(), "您也可以通过读下面的数字进行打卡哦！", 1);
//                    robot_state_text.setText(String.valueOf(num));
//                    int ret = mIat.startListening(new RecognizerListener() {
//                        @Override
//                        public void onVolumeChanged(int i, byte[] bytes) {
//
//                        }
//
//                        @Override
//                        public void onBeginOfSpeech() {
//                            robot_speak_text.setText(null);
//                        }
//
//                        @Override
//                        public void onEndOfSpeech() {
//
//                        }
//
//                        @Override
//                        public void onResult(RecognizerResult recognizerResult, boolean b) {
//
//                            String text = JsonParser.parseIatResult(recognizerResult.getResultString());
//                            robot_speak_text.append(text);
//                        }
//
//                        @Override
//                        public void onError(SpeechError speechError) {
//                            if (robot_speak_text.getText().toString().contains(String.valueOf(num))){
//                                mCameraSource.takePicture(new CameraSource.ShutterCallback() {
//                                    @Override
//                                    public void onShutter() {
//
//                                    }
//                                }, new CameraSource.PictureCallback() {
//                                    @Override
//                                    public void onPictureTaken(byte[] bytes) {
//                                        if (bytes != null) {
//                                            identifyFace.setData(bytes);
////                            robot_state_text.setText("识别成功");
//                                            FileUtils.getFileFromBytes(bytes);
////                            mTts.startSpeaking("打卡成功", null);
////                            mSpeech.speak("success!", TextToSpeech.QUEUE_FLUSH, null);
////                                            isFirst = false;
////                                            mFaceGraphic.setIsFirst(isFirst);
//                                            graphicFaceTracker.success();
//                                        }
//                                    }
//                                });
//                            }
//                        }
//
//                        @Override
//                        public void onEvent(int i, int i1, int i2, Bundle bundle) {
//
//                        }
//                    });
//                    if (ret != ErrorCode.SUCCESS) {
//                        showTip("听写失败,错误码：" + ret);
//                    } else {
//                        showTip("请开始说话");
//                    }
                    break;
            }

        }
    };


    private void makelocalGrammar() {
        mLocalGrammar = FucUtil.readFile(this, "zgty.bnf", "utf-8");
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置文本编码格式
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 设置引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, "local");
        // 设置语法构建路径
        mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, GRAMMAR_PATH);
        //使用8k音频的时候请解开注释
//					mAsr.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
        // 设置资源路径
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        int ret = mIat.buildGrammar("bnf", mLocalGrammar, grammarListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.e("makeGrammar", "语法构建失败,错误码：" + ret);
        }
    }


    private void setRecongizeParam() {
        mIat.setParameter("params", (String) null);
//        mRecognizer.setParameter("engine_type", "cloud");
        mIat.setParameter("engine_type", "mixed");
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        // 设置语法构建路径
        mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, GRAMMAR_PATH);
        mIat.setParameter("result_type", "json");
        // 设置本地识别使用语法id
        mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, "zgty");
        // 设置识别的门限值
        mIat.setParameter(SpeechConstant.MIXED_THRESHOLD, "60");
//        // 使用8k音频的时候请解开注释
//        // mAsr.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.NLP_VERSION, "2.0");
        mIat.setParameter("asr_sch", "1");
        // mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");

        mIat.setParameter("language", "zh_cn");

        mIat.setParameter("accent", "mandarin");


        mIat.setParameter("domain", "fariat");
        mIat.setParameter("aue", "speex-wb;10");

        mIat.setParameter("asr_ptt", "0");
        mIat.setParameter("audio_format", "wav");
        mIat.setParameter("asr_audio_path", Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 构建语法监听器。
     */
    private static GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {

                Log.d("makeGrammar", "语法构建成功：" + grammarId);
            } else {
                Log.d("makeGrammar", "语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };

    // 获取识别资源路径
    private static String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        // 识别通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(getInstance(), ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        // 识别8k资源-使用8k的时候请解开注释
        // tempBuffer.append(";");
        // tempBuffer.append(ResourceUtil.generateResourcePath(this,
        // RESOURCE_TYPE.assets, "asr/common_8k.jet"));
        return tempBuffer.toString();
    }


    private void initLoadSound() {
        soundPool.load(this, R.raw.oarobot1, 1);
        soundPool.load(this, R.raw.oarobot2, 1);
        soundPool.load(this, R.raw.oarobot3, 1);
        soundPool.load(this, R.raw.oarobot4, 1);
        soundPool.load(this, R.raw.oarobot5, 1);
        soundPool.load(this, R.raw.oarobot6, 1);
        soundPool.load(this, R.raw.oarobot7, 1);
        soundPool.load(this, R.raw.oarobot8, 1);
        soundPool.load(this, R.raw.oarobot9, 1);
        soundPool.load(this, R.raw.oarobot10, 1);
        soundPool.load(this, R.raw.oarobot11, 1);
        soundPool.load(this, R.raw.oarobot12, 1);
        soundPool.load(this, R.raw.oarobot13, 1);
        soundPool.load(this, R.raw.oarobot14, 1);
        soundPool.load(this, R.raw.oarobot15, 1);
        soundPool.load(this, R.raw.oarobot16, 1);
        soundPool.load(this, R.raw.oarobot17, 1);
        soundPool.load(this, R.raw.oarobot18, 1);
        soundPool.load(this, R.raw.oarobot19, 1);
        soundPool.load(this, R.raw.oarobot20, 1);
        soundPool.load(this, R.raw.oarobot21, 1);
        soundPool.load(this, R.raw.oarobot22, 1);
        soundPool.load(this, R.raw.oarobot23, 1);
        soundPool.load(this, R.raw.oarobot24, 1);
        soundPool.load(this, R.raw.oarobot25, 1);
        soundPool.load(this, R.raw.oarobot26, 1);
        soundPool.load(this, R.raw.oarobot27, 1);
        soundPool.load(this, R.raw.oarobot28, 1);
        soundPool.load(this, R.raw.oarobot29, 1);
        soundPool.load(this, R.raw.oarobot30, 1);
        soundPool.load(this, R.raw.oarobot31, 1);
        soundPool.load(this, R.raw.oarobot32, 1);
        soundPool.load(this, R.raw.oarobot33, 1);
        soundPool.load(this, R.raw.oarobot34, 1);
        soundPool.load(this, R.raw.oarobot35, 1);
        soundPool.load(this, R.raw.oarobot36, 1);
        soundPool.load(this, R.raw.oarobot37, 1);
        soundPool.load(this, R.raw.oarobot38, 1);
        soundPool.load(this, R.raw.oarobot39, 1);
        soundPool.load(this, R.raw.oarobot40, 1);
        soundPool.load(this, R.raw.oarobot41, 1);
        soundPool.load(this, R.raw.oarobot42, 1);
        soundPool.load(this, R.raw.oarobot43, 1);
        soundPool.load(this, R.raw.oarobot44, 1);
        soundPool.load(this, R.raw.oarobot45, 1);
        soundPool.load(this, R.raw.oarobot46, 1);
        soundPool.load(this, R.raw.oarobot47, 1);
        soundPool.load(this, R.raw.oarobot48, 1);
        soundPool.load(this, R.raw.oarobot49, 1);
        soundPool.load(this, R.raw.oarobot50, 1);
        soundPool.load(this, R.raw.oarobot51, 1);
        soundPool.load(this, R.raw.oarobot52, 1);
        soundPool.load(this, R.raw.oarobot53, 1);
        soundPool.load(this, R.raw.oarobot54, 1);
        soundPool.load(this, R.raw.oarobot55, 1);
        soundPool.load(this, R.raw.oarobot56, 1);
        soundPool.load(this, R.raw.oarobot57, 1);
        soundPool.load(this, R.raw.oarobot58, 1);
        soundPool.load(this, R.raw.oarobot59, 1);
        soundPool.load(this, R.raw.oarobot60, 1);
        soundPool.load(this, R.raw.oarobot61, 1);
        soundPool.load(this, R.raw.oarobot62, 1);
        soundPool.load(this, R.raw.oarobot63, 1);
        soundPool.load(this, R.raw.oarobot64, 1);
        soundPool.load(this, R.raw.oarobot65, 1);
        soundPool.load(this, R.raw.oarobot66, 1);

    }


    /**
     * 合成回调监听。播放完成后进行聆听，适用于请问您找谁？
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {

        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
                startHearing();
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    private void startHearing() {
        robot_speak_text.setTextColor(getResources().getColor(R.color.greenText));
        robot_state_text.setText("请开始说话");
        robot_state_text.setTextColor(getResources().getColor(R.color.greenText));
        // 不显示听写对话框
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        } else {
            showTip("请开始说话");
        }
    }

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogToastUtils.log(TAG, str);

            }
        });
    }


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
            robot_state_text.setText("正在聆听,请靠近屏幕说话>>>");
//            robot_state_text.setTextColor(getResources().getColor(R.color.greenText));
            robot_speak_text.setText(null);

        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
//            if (mTranslateEnable && error.getErrorCode() == 14002) {
//                showTip(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能");
//            } else {
            if (error.getErrorCode() == 10118) {
                checkYourSpeech(1);
            }
            showTip(error.getPlainDescription(true));
//            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入

            showTip("结束说话");
            robot_state_text.setText("聆听结束");
            robot_state_text.setTextColor(getResources().getColor(R.color.grey4Text));
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
//            if (mTranslateEnable) {
//                printTransResult(results);
//            } else {

            String text = JsonParser.parseIatResult(results.getResultString());
            robot_speak_text.append(text);
//            }

            if (isLast) {
                //TODO 最后的结果
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkYourSpeech(0);
                    }
                }, 2000);

            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void checkYourSpeech(int i) {
//        robot_speak_text.setTextColor(getResources().getColor(R.color.colorAccent));
        switch (i) {
            case 0:
                noanswer = 0;
                String text = String.valueOf(robot_speak_text.getText());
                List<Staff> staffList = new StaffDaoUtils(this).queryStaffList();
                boolean hasPerson = false;
                if (text.contains("打卡")) {
                    graphicFaceTracker.reset();
                    robotSpeek("现在已经可以打卡了!", 0, 17, 0);

                    return;
                }
                if (hearingType == 1) {

                    hasPerson = true;
                    if ((!text.contains("不")) && text.contains("是")) {
                        if (canConnect) {
                            robotSpeek(String.format(speekDaoUtils.querySpeekingText("connectForYou"), username), 2, 10, 0);
//                            userid1 = userid;
                        } else {
                            robotSpeek("前面已有联系任务，您还需等待", 0, 15, 0);
//                            robotSpeek("前面已有联系任务，您还需等待" + time_second + "秒", 0, 15);
                        }
                    } else {
                        robotSpeek("请说出您要找的人的名字", 1, 16, 0);
                        hearingType = 0;

                    }

                } else {
                    for (int i1 = 0; i1 < staffList.size(); i1++) {
                        if (text.contains(staffList.get(i1).getName_user())) {

                            if (canConnect) {
                                username = staffList.get(i1).getName_user();
                                userid1 = staffList.get(i1).getId();
                                VisitorDaoUtils visitorDaoUtils = new VisitorDaoUtils(getApplicationContext());
                                if (isFirstConnect) {
                                    visitorId = "visitor" + visitorDaoUtils.findVisitorNum();

                                    identifyFace.addStaff(visitorId);
                                    Visitor visitor = new Visitor();
                                    visitor.setId(visitorId);
                                    visitor.setTime(getNowDate());
                                    visitor.setVisit_id(userid1);
                                    visitor.setInfos("");
                                    visitorDaoUtils.insertVisitor(visitor);
                                } else {
                                    Visitor visitor = new Visitor();
                                    visitor.setId(userid);
                                    visitor.setTime(getNowDate());
                                    visitor.setVisit_id(userid1);
                                    visitor.setInfos("");
                                    visitorDaoUtils.updateVisitor(visitor);
                                }
                                if (isNeedId && isFirstConnect) {
                                    robotSpeek("请您出示身份证！", 0, 34, 0);
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    takeIdCard();
                                } else {
                                    robotSpeek(String.format(speekDaoUtils.querySpeekingText("connectForYou"), ""), 2, 10, 0);
//                                    robotSpeek(String.format(speekDaoUtils.querySpeekingText("connectForYou"), staffList.get(i1).getName_user()), 2, 10, 0);
                                }


                            } else {
                                robotSpeek("前面已有联系任务，您还需等待", 0, 15, 0);
//                                robotSpeek("前面已有联系任务，您还需等待" + time_second + "秒", 0, 15);
                            }
                            hasPerson = true;
                            break;
                        }
                    }
                }

                if (!hasPerson) {
                    robotSpeek(speekDaoUtils.querySpeekingText("connectByYourself"), 0, 11, 0);
                }
                break;
            case 1:
                noanswer++;
                if (noanswer < 3) {
                    robotSpeek(speekDaoUtils.querySpeekingText("cannotHearingClear"), 1, 9, 0);
                } else {
//                    identifyFace.startCameraView();
                    handler.sendEmptyMessage(7);
                }

                break;
        }
    }

    private void robotSpeek(String s, final int type, final int voice, final int user_voice_id) {


        robot_speak_text.setTextColor(getResources().getColor(R.color.colorAccent));
        robot_speak_text.setText(s);
        switch (type) {
            case 0:
                if (canUserGoogleTTS) {
                    mSpeech.speak(s, TextToSpeech.QUEUE_ADD, null);
                } else {
                    mTts.startSpeaking(s, null);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        robot_speak_text.setText("");
                        handler.sendEmptyMessage(7);
                    }
                }, 5000);

                break;
            case 1:
                //mTtsListener
                mTts.startSpeaking(s, mTtsListener);

                break;
            case 2:
                //微信联系
                mTts.startSpeaking(s, new SynthesizerListener() {
                    @Override
                    public void onSpeakBegin() {

                    }

                    @Override
                    public void onBufferProgress(int i, int i1, int i2, String s) {

                    }

                    @Override
                    public void onSpeakPaused() {

                    }

                    @Override
                    public void onSpeakResumed() {

                    }

                    @Override
                    public void onSpeakProgress(int i, int i1, int i2) {

                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        if (speechError == null) {
                            showTip("播放完成");
//                            WeiXinUtils weiXinUtils = new WeiXinUtils(getApplicationContext());
//                            weiXinUtils.SendText("前台有人找您，他的照片发给您");

                            wxcpUtils.sendText(file, userid1, "前台有人找您，您是否同意让他进来？", "image");
                            wxcpUtils.setOnWXCPUtilsListener(new WXCPUtils.OnWXCPUtilsListener() {
                                @Override
                                public void onSuccess() {
                                    successConnect();
                                }

                                @Override
                                public void onError() {
                                    robotSpeek("不好意思，没有为您通知成功!", 0, 14, 0);
                                }
                            });

                        } else {
                            showTip(speechError.getPlainDescription(true));
                        }
                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {

                    }
                });

                break;
            case 3:
                //微信联系人事
                mTts.startSpeaking(s, new SynthesizerListener() {
                    @Override
                    public void onSpeakBegin() {

                    }

                    @Override
                    public void onBufferProgress(int i, int i1, int i2, String s) {

                    }

                    @Override
                    public void onSpeakPaused() {

                    }

                    @Override
                    public void onSpeakResumed() {

                    }

                    @Override
                    public void onSpeakProgress(int i, int i1, int i2) {

                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        if (speechError == null) {
                            showTip("播放完成");
//                            WeiXinUtils weiXinUtils = new WeiXinUtils(getApplicationContext());
//                            weiXinUtils.SendText("前台有人找您，他的照片发给您");
                            userid1 = "wuzhiying16";
                            wxcpUtils.sendText(null, userid1, "离职人员" + username + "，正在前台等候，是否同意让他进来？", "text_out");
                            wxcpUtils.setOnWXCPUtilsListener(new WXCPUtils.OnWXCPUtilsListener() {
                                @Override
                                public void onSuccess() {
                                    successConnect();
                                }

                                @Override
                                public void onError() {
                                    robotSpeek("不好意思，没有为您通知成功!", 0, 19, 0);
                                }
                            });

                        } else {
                            showTip(speechError.getPlainDescription(true));
                        }
                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {

                    }
                });
                break;

        }
        if (isNeedVoice) {
            switch (voice) {
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    if (user_voice_id > 0 && user_voice_id < 33) {
                        soundPool.play(user_voice_id + 34, 1, 1, 0, 0, 1);

                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            soundPool.play(voice, 1, 1, 0, 0, 1);
                        }
                    }, 1000);
                    break;
                case 18:
                    soundPool.play(voice, 1, 1, 0, 0, 1);
                    if (user_voice_id > 0 && user_voice_id < 33) {
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                soundPool.play(user_voice_id + 34, 1, 1, 0, 0, 1);
                            }
                        }, 1200);
                    }
                    break;
                default:
                    soundPool.play(voice, 1, 1, 0, 0, 1);
                    break;

            }

        }


    }

    private void successConnect() {
        robotSpeek("已经为您通知，请稍等!", 0, 13, 0);
        intentRefreshService = new Intent();
        intentRefreshService.setClass(getApplicationContext(), RefreshService.class);
        intentRefreshService.putExtra("userid", userid1);
        startService(intentRefreshService);
        listBroadCast = new RefreshListBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCASTACTION);
        registerReceiver(listBroadCast, filter);
        handler1 = new Handler();
        time_second = 60;
        runnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情

                waiting_text.setVisibility(View.VISIBLE);
                canConnect = false;
                waiting_text.setText("正在为您联系中，剩余" + time_second-- + "秒");
                handler1.postDelayed(this, 1000);
            }
        };
        handler1.postDelayed(runnable, 1000);//每两秒执行一次runnable.
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                handler1.removeCallbacks(runnable);
//                waiting_text.setVisibility(View.GONE);
//                canConnect = true;
//            }
//        }, 60000);

    }

    /**
     * 初始化上下班排班表
     */
    private void initTime() {
        TimeDaoUtils timeDaoUtils = new TimeDaoUtils(this);
        List<Time> times = timeDaoUtils.queryTimeList();
        if (times == null || times.size() == 0) {
            times = new ArrayList<>();
            Time time1 = new Time("time_on", "上班时间", "9:00");
            Time time2 = new Time("time_off", "下班时间", "17:00");
            Time time3 = new Time("late_min", "迟到区间", "30");
            Time time4 = new Time("early_min", "早退区间", "30");
            Time time5 = new Time("time_add", "加班时间", "19:00");
            times.add(time1);
            times.add(time2);
            times.add(time3);
            times.add(time4);
            times.add(time5);
            timeDaoUtils.insertTimeList(times);
        }
        timeon = changeTimeToDouble(timeDaoUtils.queryTimeList("time_on").get(0).getTime());
        timeonlate = timeon + changeTimeToDouble(timeDaoUtils.queryTimeList("late_min").get(0).getTime());
        timeoff = changeTimeToDouble(timeDaoUtils.queryTimeList("time_off").get(0).getTime());
        timeoffearly = timeoff - changeTimeToDouble(timeDaoUtils.queryTimeList("early_min").get(0).getTime());
        timeadd = changeTimeToDouble(timeDaoUtils.queryTimeList("time_add").get(0).getTime());
    }

    private double changeTimeToDouble(String time_on) {
        double d = 0.0;
        if (time_on.contains(":")) {
            String[] split = time_on.split(":");
            d = Double.valueOf(split[0]) + Double.valueOf(split[1]) / 60;
        } else {
            d = Double.valueOf(time_on) / 60;
        }
        return d;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        initView();
        createCameraSource();
        requestPermissions();

        initAlarm();
        handler.sendEmptyMessage(2);
        wxcpUtils = new WXCPUtils(this);
    }


    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(true)
                .setMode(FAST_MODE)
                .setLandmarkType(NO_LANDMARKS)
                .build();

        graphicFaceTrackerFactory = new GraphicFaceTrackerFactory();
        detector.setProcessor(
                new MultiProcessor.Builder<>(graphicFaceTrackerFactory)
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .build();

    }

    /**
     * 闹钟事件，每月1号生成一个cvs
     */
    private void initAlarm() {
        final int INTERVAL = 1000 * 60 * 60 * 24;//每天检查一次
        Intent intent = new Intent(this, DateTimeReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL, sender);

    }


    @Override
    protected void onResume() {
        super.onResume();
        initInfo();
        initTime();
        initAcoount();
        startCameraSource();

        if (identifyFace == null) {
            identifyFace = new IdentifyFace2(this, MAIN_CHECK_CAMERA_TYPE);
            identifyFace.setOnIdentifyListener(new IdentifyFace2.OnIdentifyListener() {
                @Override
                public void onSuccess(String user_id, byte[] b) {
                    if (b != null) {
                        file = FileUtils.getFileFromBytes(b);
                        handler.sendEmptyMessage(3);
                        userid = user_id;
                    } else {
                        userid = user_id;
                        handler.sendEmptyMessage(0);
                    }

                }

                @Override
                public void onSwitch(byte[] b) {
                    file = FileUtils.getFileFromBytes(b);
                    handler.sendEmptyMessage(1);
                }

                @Override
                public void onError() {
                    graphicFaceTracker.reset();
                }

                @Override
                public void onCapture() {

                }

                @Override
                public void onRegisterSuccess() {

                }
            });
        }

//        if (identifyFace == null) {
//            identifyFace = new IdentifyFace(camera_preview, this, MAIN_CHECK_CAMERA_TYPE, this);
//            identifyFace.openSurfaceView();
//        }
//        identifyFace.setOnIdentifyListener(new IdentifyFace.OnIdentifyListener() {
//            @Override
//            public void onSuccess(String user_id) {
//                LogToastUtils.log(TAG, "success");
//                handler.sendEmptyMessage(0);
//                userid = user_id;
////                onResume();
//            }
//
//            @Override
//            public void onSwitch(byte[] data) {
//                file = FileUtils.getFileFromBytes(data);
//                LogToastUtils.log(TAG, "switch");
//                handler.sendEmptyMessage(1);
//            }
//
//            @Override
//            public void onError() {
//                LogToastUtils.log(TAG, "error");
//            }
//
//            @Override
//            public void onCapture() {
//
//            }
//
//            @Override
//            public void onRegisterSuccess() {
//
//            }
//        });

    }

    private void startCameraSource() {
        WindowManager var4 = (WindowManager) this.getSystemService(WINDOW_SERVICE);

        int var6 = var4.getDefaultDisplay().getRotation();
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void clearText() {
        name_staff.setText("");
        id_staff.setText("");
        name_part.setText("");
        sign_up_time.setText("");
        station_state.setText("");
    }

    private void initInfo() {
        clearText();
        speekDaoUtils = new SpeekDaoUtils(this);
        List<Speaking> speakings = speekDaoUtils.querySpeekList();
        if (speakings == null || speakings.size() == 0) {
            speakings = new ArrayList<>();
            Speaking speaking1 = new Speaking("welcomeText", "打开软件欢迎语", "欢迎使用中广通业考勤接待系统");
            Speaking speaking2 = new Speaking("timeOnNormal", "正常上班", "%s，早上好，新的一天开始了，祝您工作好心情！");
            Speaking speaking3 = new Speaking("timeOnLate", "上班迟到", "%s，您今天迟到了，相信您以后不会再迟到了！");
            Speaking speaking4 = new Speaking("goOutNormal", "中途外出", "%s，请您通过！");
            Speaking speaking5 = new Speaking("timeOffEarly", "下班早退", "%s，还没有下班呢，不能早退哦！");
            Speaking speaking6 = new Speaking("timeOffNormal", "正常下班", "%s，工作辛苦了，下班后让自己放松下！");
            Speaking speaking7 = new Speaking("timeAddNormal", "正常加班", "%s，您辛苦了，都加班这么晚了！");
            Speaking speaking8 = new Speaking("cannotRecognise", "陌生人", "我还不认识您，请问您找谁?");
            Speaking speaking9 = new Speaking("cannotHearingClear", "没有听清", "我没有听清您说的话，请问您找谁？");
            Speaking speaking10 = new Speaking("connectForYou", "开始联系", "正在为您联系%s,请稍后！");
            Speaking speaking11 = new Speaking("connectedSuccess", "联系成功", "请到%s！");
            Speaking speaking12 = new Speaking("connectByYourself", "查无此人", "没有找到您所找的人，请您自行联系");
            Speaking speaking13 = new Speaking("connectFailed", "没有应答", "对方没有应答，请您自行联系");
            Speaking speaking14 = new Speaking("getOffStaff", "离职人员", "%s，您已离职，正在为您联系人事！");
            speakings.add(speaking1);
            speakings.add(speaking2);
            speakings.add(speaking3);
            speakings.add(speaking4);
            speakings.add(speaking5);
            speakings.add(speaking6);
            speakings.add(speaking7);
            speakings.add(speaking8);
            speakings.add(speaking9);
            speakings.add(speaking10);
            speakings.add(speaking11);
            speakings.add(speaking12);
            speakings.add(speaking13);
            speakings.add(speaking14);

            speekDaoUtils.insertSpeekList(speakings);
        }
    }

    private void initAcoount() {
        AccountDaoUtils accountDaoUtils = new AccountDaoUtils(this);
        if (accountDaoUtils.queryAccountSize() == 0) {
            Account account = new Account("admin", "zgtyadmin");
            accountDaoUtils.insertAccountList(account);
        }
    }


    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_CONTACTS, Manifest.permission.GET_ACCOUNTS,
                            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handler.sendEmptyMessage(2);
    }

    private void initView() {
        change_mode = findViewById(R.id.change_mode);
        name_staff = findViewById(R.id.name_staff);
        id_staff = findViewById(R.id.id_staff);
        sign_up_time = findViewById(R.id.sign_up_time);
        station_state = findViewById(R.id.station_state);
        layout_robot = findViewById(R.id.abcdefg);
//        layout_robot.setVisibility(View.VISIBLE);

        change_mode.setOnClickListener(this);
        name_part = findViewById(R.id.name_part);
        allram = findViewById(R.id.allram);
        canuse = findViewById(R.id.canuse);
        robot_speak_text = findViewById(R.id.robot_speak_text);
        setting_main = findViewById(R.id.setting_main);
        setting_main.setOnClickListener(this);

        waiting_text = findViewById(R.id.waiting_text);

        mGraphicOverlay = findViewById(R.id.faceOverlay);
        mPreview = findViewById(R.id.preview);
        rb_normal = findViewById(R.id.rb_normal);

        robot_state_text = findViewById(R.id.robot_state_text);

        robot_webview = findViewById(R.id.robot_webview);
        layout_robot.bringToFront();
//        robot_webview.setInitialScale(100);//设置缩放


        handler.sendEmptyMessage(6);
        handler.sendEmptyMessage(5);

//        Timer timer=new Timer();
//        TimerTask timerTask=new TimerTask() {
//            @Override
//            public void run() {
//                handler.sendEmptyMessage(6);
//
//            }
//        };
//        timer.schedule(timerTask,10000,10000);
    }

    //识别完毕后，数据加载，写在异步线程
    private void initData() {
//        userid = "10003";
        new MyToast(this, "打卡成功", 0);
        List<Staff> staffList = new StaffDaoUtils(this).queryStaffList(userid);
        if (staffList != null && staffList.size() > 0) {
            Staff staff = staffList.get(0);
            username = staff.getName_user();
            if (staff.getUser_type().equals("1")) {
                if (canConnect) {
                    robotSpeek(String.format(speekDaoUtils.querySpeekingText("getOffStaff"), username), 3, 12, 0);
                } else {
                    robotSpeek(username + "，您已离职，但前面已有联系任务，您还需等待", 0, 20, 0);
//                    robotSpeek(username + "，您已离职，但前面已有联系任务，您还需等待" + time_second + "秒", 0, 20);
                }

                return;
            }
            name_staff.setText(username);
            id_staff.setText(staff.getId_clerk());
            name_part.setText(staff.getName_part());
            String nowtime = getNowTime();
            timenow = changeTimeToDouble(nowtime);
            sign_up_time.setText(nowtime);
            String type = getType(staff);
            station_state.setText(type);
            WorkOnOffDaoUtils workOnOffDaoUtils = new WorkOnOffDaoUtils(this);
            if (timenow <= 12) {
                workOnOffDaoUtils.updateWorkOn(staff.getId(), nowtime);
            } else {
                workOnOffDaoUtils.updateWorkOff(staff.getId(), nowtime);
            }
            robot_state_text.setText("打卡成功");
//            if (!type.equalsIgnoreCase("中途外出")) {
//                wxcpUtils.sendText(null, userid, "姓名：" + staff.getName_user() + "\n工号：" + staff.getId_clerk() + "\n部门：" + staff.getName_part() + "\n打卡时间：" + nowtime + "\n打卡类型：" + type);
//            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearText();
                    robot_speak_text.setText("");

                }
            }, 5000);
            handler.sendEmptyMessage(7);

        } else {
            LogToastUtils.toastShort(this, "没有录入该信息");
        }

    }

    //获取打卡类型
    private String getType(Staff staff) {
        String type = "";
        int user_voice_id = Integer.valueOf(staff.getId_clerk());

        if (timenow >= timeadd) {
            type = "加班";
            robotSpeek(String.format(speekDaoUtils.querySpeekingText("timeAddNormal"), staff.getName_user()), 0, 7, user_voice_id);
//            soundPool.play(7,1, 1, 0, 0, 1);
        } else if (timenow >= timeoff) {
            type = "正常下班";
            robotSpeek(String.format(speekDaoUtils.querySpeekingText("timeOffNormal"), staff.getName_user()), 0, 6, user_voice_id);
//            soundPool.play(6,1, 1, 0, 0, 1);
        } else if (timenow > timeoffearly) {
            type = "早退";
            robotSpeek(String.format(speekDaoUtils.querySpeekingText("timeOffEarly"), staff.getName_user()), 0, 5, user_voice_id);
//            soundPool.play(5,1, 1, 0, 0, 1);
        } else if (timenow > timeonlate) {
            type = "中途外出";
            robotSpeek(String.format(speekDaoUtils.querySpeekingText("goOutNormal"), staff.getName_user()), 0, 4, user_voice_id);
//            soundPool.play(4,1, 1, 0, 0, 1);
        } else if (timenow > timeon) {
            type = "迟到";
            robotSpeek(String.format(speekDaoUtils.querySpeekingText("timeOnLate"), staff.getName_user()), 0, 3, user_voice_id);
//            soundPool.play(3,1, 1, 0, 0, 1);
        } else {
            type = "正常上班";
            robotSpeek(String.format(speekDaoUtils.querySpeekingText("timeOnNormal"), staff.getName_user()), 0, 2, user_voice_id);
//            soundPool.play(2,1, 1, 0, 0, 1);
        }


        return type;
    }

    //获取当前时间
    private String getNowTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    //获取当前日期时间
    private String getNowDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.change_mode:
                intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_main:
                intent = new Intent(this, LoginActivity.class);
                intent.putExtra("type", 1);
                startActivity(intent);
                break;
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            } else {
                robot_state_text.setText("准备就绪");
            }
        }
    };


    /**
     * 应用挂起时，停止语音合成与识别
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
//        if (mTts != null) {
//            mTts.stopSpeaking();
//        }
        if (mIat != null) {
            mIat.stopListening();

        }
//        if (mCameraSource != null) {
//            mCameraSource.release();
//        }
    }

    /**
     * 销毁应用，同时销毁语音合成，识别，服务，广播
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        identifyFace.finisheIdentify();
        identifyFace = null;
        if (mTts != null) {
            mTts.destroy();
        }
        if (mIat != null) {
            mIat.destroy();
        }
        if (intentRefreshService != null) {
            stopService(intentRefreshService);
            intentRefreshService = null;
        }
        if (listBroadCast != null) {
            unregisterReceiver(listBroadCast);
            listBroadCast = null;
        }
    }


    /**
     * 接收广播，等待联系人回复。
     */
    private class RefreshListBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intentRefreshService != null) {
                stopService(intentRefreshService);
                intentRefreshService = null;
            }
            if (listBroadCast != null) {
                unregisterReceiver(listBroadCast);
                listBroadCast = null;
            }
            handler1.removeCallbacks(runnable);
            waiting_text.setVisibility(View.GONE);
            canConnect = true;
            String result = intent.getStringExtra("result");
            switch (result) {
                case "worktable":
                    robotSpeek("对方已经接受了您的请求，请您直接去他的工位或办公室!", 0, 21, 0);
                    wxcpUtils.sendText(null, userid1, "已经让对方通过！", "text");
                    break;
                case "combig":
                    robotSpeek("对方已经接受了您的请求，请您到大会议室等候!", 0, 22, 0);
                    wxcpUtils.sendText(null, userid1, "已经让对方通过！", "text");
                    break;
                case "comsmall":
                    robotSpeek("对方已经接受了您的请求，请您到小会议室等候!", 0, 23, 0);
                    wxcpUtils.sendText(null, userid1, "已经让对方通过！", "text");
                    break;
                case "talk1":
                    robotSpeek("对方已经接受了您的请求，请您到洽谈室1等候!", 0, 24, 0);
                    wxcpUtils.sendText(null, userid1, "已经让对方通过！", "text");
                    break;
                case "talk2":
                    robotSpeek("对方已经接受了您的请求，请您到洽谈室2等候!", 0, 25, 0);
                    wxcpUtils.sendText(null, userid1, "已经让对方通过！", "text");
                    break;
                case "wangxiaodi04":
                    robotSpeek("对方不方便与您联系，已将您转接至我公司行政!", 2, 26, 0);
                    wxcpUtils.sendText(null, userid1, "已经为对方转接至行政王晓迪！", "text");
                    userid1 = "wangxiaodi04";
                    break;
                case "yuantong05":
                    robotSpeek("对方不方便与您联系，已将您转接至我公司行政!", 2, 26, 0);
                    wxcpUtils.sendText(null, userid1, "已经为对方转接至行政袁彤！", "text");
                    userid1 = "yuantong05";
                    break;
                case "chenjingyi29":
                    robotSpeek("对方不方便与您联系，已将您转接至我公司人事!", 2, 27, 0);
                    wxcpUtils.sendText(null, userid1, "已经为对方转接至人事陈静怡！", "text");
                    userid1 = "chenjingyi29";
                    break;
                case "weixin12":
                    robotSpeek("对方不方便与您联系，已将您转接至我公司开发!", 2, 28, 0);
                    wxcpUtils.sendText(null, userid1, "已经为对方转接至开发魏鑫", "text");
                    userid1 = "weixin12";
                    break;
                case "wuzhiying16":
                    robotSpeek("对方不方便与您联系，已将您转接至我公司开发!", 2, 28, 0);
                    wxcpUtils.sendText(null, userid1, "已经为对方转接至开发巫志英！", "text");
                    userid1 = "wuzhiying16";
                    break;
                case "reject":
                case "other":
                    robotSpeek("对方已经拒绝了您的请求，请您自行联系!", 0, 29, 0);
                    wxcpUtils.sendText(null, userid1, "已经拒绝对方！", "text");
                    break;
                case "waiting":
                    robotSpeek("对方暂时不方便与您联系，请您稍等几分钟后再试!", 0, 30, 0);
                    wxcpUtils.sendText(null, userid1, "已经拒绝对方，并提示对方等待！", "text");
                    break;
                case "changing":
                    robotSpeek("对方暂时无法与您联系，请您修改预约时间!", 0, 31, 0);
                    wxcpUtils.sendText(null, userid1, "已经拒绝对方,并提示改预约时间！", "text");
                    break;
                case "telephone":
                    robotSpeek("对方希望您通过电话联系!", 0, 32, 0);
//                    robotSpeek("对方希望您通过电话联系!电话号码是：" + new StaffDaoUtils(getApplicationContext()).queryStaffList(userid1).get(0).getCall_num(), 0, 32);
                    robot_state_text.setText("电话号码是：" + new StaffDaoUtils(getApplicationContext()).queryStaffList(userid1).get(0).getCall_num());
                    wxcpUtils.sendText(null, userid1, "已经拒绝对方，并提示电话联系！", "text");
                    break;
                case "timeout":
                    robotSpeek("超过一分钟没有答复，请您自行联系!", 0, 33, 0);
                    wxcpUtils.sendText(null, userid1, "您超过一分钟没有回复，该信息已经失效！", "text");
                    break;
            }


        }
    }

    ;
    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            graphicFaceTracker = new GraphicFaceTracker(mGraphicOverlay);
            return graphicFaceTracker;
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private boolean isFirst;
        private float lastSmile;
        private float firstSmile;
        private int needGoSecond = 5000;
        private Timer timer;
        private TimerTask timerTask;


        private int dip2px(Context context, float dipValue) {
            Resources r = context.getResources();
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
        }

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, MainActivityN.this);


        }

        public void reset() {
            isFirst = true;
            mFaceGraphic.setIsFirst(isFirst);
        }

        public void success() {
            isFirst = false;
            mFaceGraphic.setIsFirst(isFirst);
        }


        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            isFirst = true;
            mFaceGraphic.setIsFirst(isFirst);
//            timer.cancel();
//            timerTask.cancel();
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (isFirst) {
                        handler.sendEmptyMessage(8);
                    }
                }
            };
            timer.schedule(timerTask, needGoSecond);
            handler.sendEmptyMessage(4);
//            layout_robot.setVisibility(View.VISIBLE);
//            if (item.getIsSmilingProbability() > 0) {
//                firstSmile = item.getIsSmilingProbability();
//            } else {
//                firstSmile = 0;
//            }
            firstSmile = item.getIsSmilingProbability();
//            robot_state_text.setText("请面对摄像头保持微笑");
//            mSpeech.speak("请您微笑", TextToSpeech.QUEUE_FLUSH, null);
//            mSpeech.speak("please smile", TextToSpeech.QUEUE_FLUSH, null);

//            @SuppressLint("HandlerLeak")
//            Handler handler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    switch (msg.what){
//                        case 0:
//
//                            break;
//                    }
//                }
//            };
//            handler.sendEmptyMessage(0);


        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            handler.sendEmptyMessage(4);
            mFaceGraphic.updateFace(face);
            Log.e("test", "test---------------" + detectionResults.detectorIsOperational());
            Log.e("test", "test---------------" + face.getIsSmilingProbability());
            lastSmile = face.getIsSmilingProbability();
            if (firstSmile <= 0) {
                firstSmile = lastSmile;
            }
            if (lastSmile <= 0) {
                return;
            }
            if (lastSmile > 0 && lastSmile < firstSmile) {
                firstSmile = lastSmile;
            }
            rb_normal.setRating(lastSmile * 5);
            if (isFirst && lastSmile - firstSmile >= 0.3) {
                Log.e("smile", "lastSmile is " + lastSmile + ", firstSmile is " + firstSmile);
                mCameraSource.takePicture(new CameraSource.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        if (bytes != null) {
                            identifyFace.setData(bytes);
//                            robot_state_text.setText("识别成功");
                            FileUtils.getFileFromBytes(bytes);
//                            mTts.startSpeaking("打卡成功", null);
//                            mSpeech.speak("success!", TextToSpeech.QUEUE_FLUSH, null);
                            isFirst = false;
                            mFaceGraphic.setIsFirst(isFirst);

                        }
                    }
                });

            }
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
//            robot_state_text.setText("准备就绪");
            handler.sendEmptyMessage(5);
            rb_normal.setRating(0);
            timer.cancel();
//            timerTask.cancel();
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            rb_normal.setRating(0);
            handler.sendEmptyMessage(5);
            timer.cancel();
//            timerTask.cancel();
        }
    }

    //请出示身份证的跳转方法
    private void takeIdCard() {
        //  初始化本地质量控制模型,释放代码在onDestory中
        //  调用身份证扫描必须加上 intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL, true); 关闭自动初始化和释放本地模型
        CameraNativeHelper.init(this, OCR.getInstance().getLicense(),
                new CameraNativeHelper.CameraNativeInitCallback() {
                    @Override
                    public void onError(int errorCode, Throwable e) {
                        String msg;
                        switch (errorCode) {
                            case CameraView.NATIVE_SOLOAD_FAIL:
                                msg = "加载so失败，请确保apk中存在ui部分的so";
                                break;
                            case CameraView.NATIVE_AUTH_FAIL:
                                msg = "授权本地质量控制token获取失败";
                                break;
                            case CameraView.NATIVE_INIT_FAIL:
                                msg = "本地质量控制";
                                break;
                            default:
                                msg = String.valueOf(errorCode);
                        }
                        LogToastUtils.toastShort(getApplicationContext(), "本地质量控制初始化错误，错误原因： " + msg);
                    }
                });
        Intent intent = new Intent(MainActivityN.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE,
                true);
        // KEY_NATIVE_MANUAL设置了之后CameraActivity中不再自动初始化和释放模型
        // 请手动使用CameraNativeHelper初始化和释放模型
        // 推荐这样做，可以避免一些activity切换导致的不必要的异常
        intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL,
                true);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    //扫描身份证回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                if (!TextUtils.isEmpty(contentType)) {
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                    }
                }
            }
        }
    }

    private void recIDCard(String idCardSide, String filePath) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                if (result != null) {
                    LogToastUtils.toastShort(getApplicationContext(), result.toString());
                    IdCard idCard = new IdCard();
                    idCard.setId(visitorId);
                    idCard.setName(result.getName().getWords());
                    idCard.setId_card_num(result.getIdNumber().getWords());
                    idCard.setSex(result.getGender().getWords());
                    new IdCardDaoUtils(getApplicationContext()).insertIdCard(idCard);
                    robotSpeek(String.format(speekDaoUtils.querySpeekingText("connectForYou"), username), 2, 10, 0);
                }
            }

            @Override
            public void onError(OCRError error) {
                LogToastUtils.toastShort(getApplicationContext(), error.getMessage());
            }
        });
    }
}
