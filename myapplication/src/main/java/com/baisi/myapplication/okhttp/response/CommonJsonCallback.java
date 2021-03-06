package com.baisi.myapplication.okhttp.response;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.baisi.myapplication.adutil.ResponseEntityToModule;
import com.baisi.myapplication.okhttp.exception.OkHttpException;
import com.baisi.myapplication.okhttp.listener.DisposeDataHandle;
import com.baisi.myapplication.okhttp.listener.DisposeDataListener;
import com.baisi.myapplication.okhttp.listener.DisposeHandleCookieListener;
import com.baisi.myapplication.util.MyLogUntil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author vision
 * @function 专门处理JSON的回调
 */
public class CommonJsonCallback implements Callback {

    /**
     * the logic layer exception, may alter in different app
     */
    protected final String RESULT_CODE = "ecode"; // 有返回则对于http请求来说是成功的，但还有可能是业务逻辑上的错误
    protected final int RESULT_CODE_VALUE = 0;
    protected final String ERROR_MSG = "emsg";
    protected final String EMPTY_MSG = "";
    protected final String COOKIE_STORE = "Set-Cookie"; // decide the server it
    // can has the value of
    // set-cookie2

    /**
     * the java layer exception, do not same to the logic error
     */
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int JSON_ERROR = -2; // the JSON relative error
    protected final int OTHER_ERROR = -3; // the unknow error

    /**
     * 将其它线程的数据转发到UI线程
     */
    private Handler mDeliveryHandler;
    private DisposeDataListener mListener;
    private Class<?> mClass;

    public CommonJsonCallback(DisposeDataHandle handle) {

        this.mListener = handle.mListener;
        this.mClass = handle.mClass;
        this.mDeliveryHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onFailure(final Call call, final IOException ioexception) {
        /**
         * 此时还在非UI线程，因此要转发
         */
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {


                mListener.onFailure(new OkHttpException(NETWORK_ERROR, ioexception));
            }
        });
    }

    @Override
    public void onResponse(final Call call, final Response response) throws IOException {
        final String result = response.body().string();
        final ArrayList<String> cookieLists = handleCookie(response.headers());
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {


                handleResponse(result);


                /**
                 * handle the cookie
                 */
                if (mListener instanceof DisposeHandleCookieListener) {
                    ((DisposeHandleCookieListener) mListener).onCookie(cookieLists);
                }
            }
        });
    }

    private ArrayList<String> handleCookie(Headers headers) {
        ArrayList<String> tempList = new ArrayList<String>();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.name(i).equalsIgnoreCase(COOKIE_STORE)) {
                tempList.add(headers.value(i));
            }
        }
        return tempList;
    }

    private void handleResponse(Object responseObj) {

        MyLogUntil.iJsonFormat("JSON_RES",String.valueOf(responseObj),false);
        Log.i("JSON_LLLL",String.valueOf(responseObj));
//        Log.i("AAA",String.valueOf(responseObj));
        if (responseObj == null || responseObj.toString().trim().equals("")) {
            mListener.onFailure(new OkHttpException(NETWORK_ERROR, EMPTY_MSG));
            return;
        }

        JSONArray jsonArray;
        JSONObject result = null;

        Object json = null;
        try {
            json = new JSONTokener(String.valueOf(responseObj)).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(json instanceof JSONObject){
            result = (JSONObject)json;
        }else if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
            try {
                for(int i=0 ; i < jsonArray.length() ;i++)
                {
                    //获取每一个JsonObject对象
                    result = jsonArray.getJSONObject(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            /**
             * 协议确定后看这里如何修改
             */

//            JSONObject result = new JSONObject(String.valueOf(responseObj));

            if (mClass == null) {

                mListener.onSuccess(result);
            } else {
//                if (result!=null){
//                    Log.i("BBB",String.valueOf(responseObj));
//                    mListener.onSuccess(result);
//
//                }else {
//                    mListener.onFailure(new OkHttpException(JSON_ERROR, EMPTY_MSG));
//                }

                Object obj = ResponseEntityToModule.parseJsonObjectToModule(result, mClass);
                Log.i("CCC",String.valueOf(obj));
                if (obj != null) {
                    mListener.onSuccess(obj);
                } else {
                    mListener.onFailure(new OkHttpException(JSON_ERROR, EMPTY_MSG));
                }
            }
        } catch (Exception e) {
            Log.i("DDD",String.valueOf(NETWORK_ERROR));
            mListener.onFailure(new OkHttpException(OTHER_ERROR, e.getMessage()));
            e.printStackTrace();
        }
    }
}