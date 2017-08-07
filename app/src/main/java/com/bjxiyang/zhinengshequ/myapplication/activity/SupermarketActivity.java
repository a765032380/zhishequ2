package com.bjxiyang.zhinengshequ.myapplication.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baisi.myapplication.okhttp.listener.DisposeDataListener;
import com.bjxiyang.zhinengshequ.R;
import com.bjxiyang.zhinengshequ.myapplication.adapter.supermarket.CatograyAdapter;
import com.bjxiyang.zhinengshequ.myapplication.adapter.supermarket.GoodsAdapter;
import com.bjxiyang.zhinengshequ.myapplication.adapter.supermarket.ProductAdapter;
import com.bjxiyang.zhinengshequ.myapplication.app.GuardApplication;
import com.bjxiyang.zhinengshequ.myapplication.base.MySwipeBackActivity;
import com.bjxiyang.zhinengshequ.myapplication.bean.SelectPlot;
import com.bjxiyang.zhinengshequ.myapplication.bean.bianlidian.DianMing;
import com.bjxiyang.zhinengshequ.myapplication.bean.bianlidian.GouWuChe;
import com.bjxiyang.zhinengshequ.myapplication.bean.bianlidian.ShangPinList;
import com.bjxiyang.zhinengshequ.myapplication.bianlidianstatus.BianLiDianStatus;
import com.bjxiyang.zhinengshequ.myapplication.connectionsURL.BianLiDianResponse;
import com.bjxiyang.zhinengshequ.myapplication.connectionsURL.XY_Response;
import com.bjxiyang.zhinengshequ.myapplication.dialog.CommonActionSheetDialog;
import com.bjxiyang.zhinengshequ.myapplication.dialog.GouWuCheDialog;
import com.bjxiyang.zhinengshequ.myapplication.greendao.DaoUtils;
import com.bjxiyang.zhinengshequ.myapplication.manager.SPManager;
import com.bjxiyang.zhinengshequ.myapplication.manager.UserManager;
import com.bjxiyang.zhinengshequ.myapplication.until.DialogUntil;
import com.bjxiyang.zhinengshequ.myapplication.until.LogOutUntil;
import com.bjxiyang.zhinengshequ.myapplication.until.MyUntil;
import com.bjxiyang.zhinengshequ.myapplication.until.UnitGetGouWuChe;
import com.bjxiyang.zhinengshequ.myapplication.update.network.RequestCenter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2017/8/7 0007.
 */

public class SupermarketActivity extends MySwipeBackActivity implements
        View.OnClickListener,SwipeRefreshLayout.OnRefreshListener {
    /**
     * UI
     */
    @BindView(R.id.ll_shopname)
    LinearLayout ll_shopname;
    @BindView(R.id.tv_shopname)
    TextView tv_shopname;
    @BindView(R.id.et_allapp)
    EditText et_allapp;
    @BindView(R.id.ll_jiage)
    LinearLayout ll_jiage;
    @BindView(R.id.iv_up)
    ImageView iv_up;
    @BindView(R.id.tv_car)
    TextView tv_car;
    @BindView(R.id.bv_unm)
    TextView bv_unm;
    @BindView(R.id.tv_totle_money)
    TextView tv_totle_money;
    @BindView(R.id.xuanhaole)
    TextView xuanhaole;
    @BindView(R.id.lv_good)
    ListView lv_good;
    @BindView(R.id.lv_catogary)
    ListView lv_catogary;
    @BindView(R.id.swipeRefreshLayout1)
    SwipeRefreshLayout swipeRefreshLayout1;
    @BindView(R.id.swipeRefreshLayout2)
    SwipeRefreshLayout swipeRefreshLayout2;
    @BindView(R.id.swipeRefreshLayout3)
    SwipeRefreshLayout swipeRefreshLayout3;
    @BindView(R.id.ib_fanghui)
    RelativeLayout ib_fanghui;
    @BindView(R.id.ll_first)
    LinearLayout ll_first;

    private TextView tv_count,tv_totle_money2;
    /**
     * DATE
     */
    private int position1=0;
    private CommonActionSheetDialog commonActionSheetDialog;
    private boolean jiageIsOne=true;
    private int sellerId=-1;
    private DianMing.Result result;
    private List<DianMing.Result> resultList;
    public int communityId=-1;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private CatograyAdapter catograyAdapter;//分类的adapter
    private GoodsAdapter goodsAdapter;//分类下商品adapter
    private ProductAdapter productAdapter;//底部购物车的adapter
    private List<SelectPlot.Obj> mList_plot;
    private List<ShangPinList.Result> list_fenlei;
    private List<ShangPinList.Result.Products> list_shangpin;
    private ViewGroup anim_mask_layout;//动画层
    private  GouWuCheDialog dialog;
    private GuardApplication myApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supermarket);
        ButterKnife.bind(this);
        Log.i("YYYY",UserManager.getInstance().getUser().getObj().getMobilePhone());
        initUi();
    }

    private void initUi() {

        ib_fanghui.setOnClickListener(this);
        ll_jiage.setOnClickListener(this);
        ll_shopname.setOnClickListener(this);
        tv_car.setOnClickListener(this);
        xuanhaole.setOnClickListener(this);


        myApp = GuardApplication.getContent();
        addListener();

        if (SPManager.getInstance().getInt("sellerId",-1)!=-1){
            tv_shopname.setText(SPManager.getInstance().getString("shopName",""));
            getShangPingList(SPManager.getInstance().getInt("sellerId",-1));
        }
        tv_totle_money.setText("￥"+ String.valueOf(df.format(UnitGetGouWuChe.getZongJia())));
        update(false);

    }
    private void addListener() {
        lv_catogary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                position1=position;
                catograyAdapter.setSelection(position);
                catograyAdapter.notifyDataSetChanged();
                goodsAdapter=new GoodsAdapter(SupermarketActivity.this,SupermarketActivity.this,
                        bubble_sort(list_fenlei.get(position).getProducts()),catograyAdapter);
                lv_good.setAdapter(goodsAdapter);
            }
        });
    }
    private void getShangPingList(int sellerId){
        Log.i("YYY","测试显示无数据sellerId="+sellerId);
        DialogUntil.showLoadingDialog(this,"正在加载",true);
        String url_list= BianLiDianResponse.URL_PRODUCT_LIST+"sellerId="+sellerId;
        RequestCenter.order_product_list(url_list, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                DialogUntil.closeLoadingDialog();
                ShangPinList shangPinList= (ShangPinList) responseObj;

                if (shangPinList.getCode()== BianLiDianStatus.STATUS_CODE_SUCCESS){
                    //分类
                    list_fenlei=shangPinList.getResult();
                    //商品
                    if (list_fenlei.size()>0) {
                        catograyAdapter = new CatograyAdapter(myApp, list_fenlei);
                        lv_catogary.setAdapter(catograyAdapter);
                        catograyAdapter.notifyDataSetChanged();
                        list_shangpin = list_fenlei.get(0).getProducts();

                        goodsAdapter = new GoodsAdapter(SupermarketActivity.this, SupermarketActivity.this, bubble_sort(list_shangpin), catograyAdapter);
                        lv_good.setAdapter(goodsAdapter);
                        goodsAdapter.notifyDataSetChanged();
                        showList();
                    }else {
                        showWuShuJu();
                    }
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                DialogUntil.closeLoadingDialog();

            }
        });

    }
    public void handlerCarNum(int type, GouWuChe gouWuChe1, boolean refreshGoodList){
        if (type==1) {
            gouWuChe1.setCount(gouWuChe1.getCount() + 1);
            DaoUtils.getStudentInstance().updateObject(gouWuChe1);
        }else if (type==0){
            if (gouWuChe1!=null){
                if (gouWuChe1.getCount()>1){
                    gouWuChe1.setCount(gouWuChe1.getCount()-1);
                    DaoUtils.getStudentInstance().updateObject(gouWuChe1);
                }else {

                    if (dialog!=null&&dialog.isShowing()&&UnitGetGouWuChe.getConuntAll()==0){
                        dialog.cancel();
                    }
                    gouWuChe1.setCount(0);
                    DaoUtils.getStudentInstance().updateObject(gouWuChe1);
                }
            }
        }
        update(refreshGoodList);

    }
    public void setAnim(final View v, int[] startLocation) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        anim_mask_layout.addView(v);//把动画小球添加到动画层
        final View view = addViewToAnimLayout(anim_mask_layout, v, startLocation);
        int[] endLocation = new int[2];// 存储动画结束位置的X、Y坐标
        tv_car.getLocationInWindow(endLocation);
        // 计算位移
        int endX = 0 - startLocation[0] + 40;// 动画位移的X坐标
        int endY = endLocation[1] - startLocation[1];// 动画位移的y坐标

        TranslateAnimation translateAnimationX = new TranslateAnimation(0,endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);

        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0, 0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationY.setFillAfter(true);

        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(800);// 动画的执行时间
        view.startAnimation(set);
        // 动画监听事件
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
        });

    }
    private View addViewToAnimLayout(final ViewGroup parent, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }
    /**
     * @Description: 创建动画层
     * @param
     * @return void
     * @throws
     */
    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setId(Integer.MAX_VALUE-1);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }
    //刷新布局 总价、购买数量等
    private void update(boolean refreshGoodList){

        int count = UnitGetGouWuChe.getConuntAll();



//        for(int i=0;i<size;i++){
//            GoodsBean item = selectedList.valueAt(i);
//            count += item.getNum();
//            totleMoney += item.getNum()* Double.parseDouble(item.getPrice());
//
        tv_totle_money.setText("￥"+ String.valueOf(df.format(UnitGetGouWuChe.getZongJia())));
        if (tv_totle_money2!=null){
            tv_totle_money2.setText("￥"+ String.valueOf(df.format(UnitGetGouWuChe.getZongJia())));
        }
//        totleMoney = 0.00;
        if(count<1){
            bv_unm.setVisibility(View.GONE);
        }else{
            bv_unm.setVisibility(View.VISIBLE);
        }

        bv_unm.setText(String.valueOf(UnitGetGouWuChe.getConuntAll()));


        if(productAdapter!=null){
            productAdapter.notifyDataSetChanged();
        }

        if(goodsAdapter!=null){
            goodsAdapter.notifyDataSetChanged();
        }

        if(catograyAdapter!=null){
            catograyAdapter.notifyDataSetChanged();
        }

//        if(bottomSheetLayout.isSheetShowing() && count<1){
//            bottomSheetLayout.dismissSheet();
//        }
    }
    private void showList(){
        ll_first.setVisibility(View.VISIBLE);
        swipeRefreshLayout2.setVisibility(View.GONE);
        swipeRefreshLayout3.setVisibility(View.GONE);
    }
    private void showWuShuJu(){
        SPManager.getInstance().putInt("sellerId",-1);
        SPManager.getInstance().putInt("communityId",-2);
        tv_shopname.setText("店名");
        bv_unm.setText("0");
        tv_totle_money.setText("￥0.00");
        ll_first.setVisibility(View.GONE);
        swipeRefreshLayout2.setVisibility(View.VISIBLE);
        swipeRefreshLayout3.setVisibility(View.GONE);
    }
    private void showSelectSJ(){
        SPManager.getInstance().putInt("sellerId",-1);
        SPManager.getInstance().putInt("communityId",-1);
        tv_shopname.setText("店名");
        bv_unm.setText("0");
        tv_totle_money.setText("￥0.00");
        ll_first.setVisibility(View.GONE);
        swipeRefreshLayout2.setVisibility(View.GONE);
        swipeRefreshLayout3.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        communityId = SPManager.getInstance().getInt("communityId",-1);

        if (communityId==-1){
            showSelectSJ();
            MyUntil.show(this,"请选择商家");
        }else if (communityId==-2){
            showWuShuJu();
        } else{
            getDianMing(communityId);

        }
        swipeRefreshLayout1.setRefreshing(false);
        swipeRefreshLayout2.setRefreshing(false);
        swipeRefreshLayout3.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //返回的按键
            case R.id.ib_fanghui:
                finish();
                break;
            //选择商家名字
            case R.id.ll_shopname:
                getData();
                break;
            //弹出购物车
            case R.id.tv_car:
                dialog=new GouWuCheDialog(SupermarketActivity.this,SupermarketActivity.this,goodsAdapter);
                dialog.show();
                break;
            //选择排序的方式
            case R.id.ll_jiage:
                if (!jiageIsOne){
                    iv_up.setBackgroundResource(R.drawable.a_icon_price_pre);
                    jiageIsOne=true;
                }else {
                    iv_up.setBackgroundResource(R.drawable.a_icon_price);
                    jiageIsOne=false;
                }
                goodsAdapter=new GoodsAdapter(SupermarketActivity.this,SupermarketActivity.this,
                        bubble_sort(list_fenlei.get(position1).getProducts()),catograyAdapter);
                lv_good.setAdapter(goodsAdapter);
//                update(false);
                break;
            //提交订单的按键
            case R.id.xuanhaole:
                break;
        }

    }
    private void getDianMing(int communityId){
        resultList=new ArrayList<>();
        String url= BianLiDianResponse.URL_SELLER_LIST+"communityId="+communityId;

        RequestCenter.order_seller_list(url, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                DianMing dianMing= (DianMing) responseObj;
                if (dianMing.getCode() == BianLiDianStatus.STATUS_CODE_SUCCESS) {

                    resultList = dianMing.getResult();
                    result = resultList.get(0);
                    //18706451620
                    //120688jj
                    SPManager.getInstance().putInt("sellerId", result.getId());
                    SPManager.getInstance().putString("shopName", result.getShopName());
                    sellerId = result.getId();

                    tv_shopname.setText(result.getShopName());
                    update(false);
                    getShangPingList(result.getId());
                }else if (dianMing.getCode()==BianLiDianStatus.STATUS_CODE_ERROR_USER_NOTLOGIN){
                    LogOutUntil.logout();
                }else {
                    showWuShuJu();
                }

            }

            @Override
            public void onFailure(Object reasonObj) {
                showWuShuJu();
            }
        });

    }
    private void getData(){
        mList_plot=new ArrayList<>();
        DialogUntil.showLoadingDialog(SupermarketActivity.this,"正在加载",true);
        String url= XY_Response.URL_FINDCOMMUNITYBYPER+"mobilePhone="+
                UserManager.getInstance().getUser().getObj().getMobilePhone();

        RequestCenter.findCommunityByPer(url, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SelectPlot selectPlot= (SelectPlot) responseObj;
                DialogUntil.closeLoadingDialog();
                if (selectPlot.getCode().equals("1000")){
                    mList_plot = selectPlot.getObj();
                    if (mList_plot.size()>0){
                        showActionSheet(mList_plot);
                    }else {
                        MyUntil.show(SupermarketActivity.this,"请选择小区");
                        SPManager.getInstance().remove("sellerId");
                    }
                }else {
                    SPManager.getInstance().remove("sellerId");
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                DialogUntil.closeLoadingDialog();
            }
        });
    }
    public void showActionSheet(final List<SelectPlot.Obj> mList)
    {
        if (mList.size()>0){
            commonActionSheetDialog = new CommonActionSheetDialog(SupermarketActivity.this);

            for (int i=0;i<mList.size();i++) {
                if (i>0){
                    if (!(mList.get(i).getCommunityName()+mList.get(i).getNperName())
                            .equals((mList.get(i-1).getCommunityName()+mList.get(i-1).getNperName())))
                    {
                        commonActionSheetDialog.addMenuItem(mList.get(i).getCommunityName()
                                + mList.get(i).getNperName());
                    }
                }else {
                    commonActionSheetDialog.addMenuItem(mList.get(i).getCommunityName()
                            + mList.get(i).getNperName());
                }
            }
            commonActionSheetDialog.setMenuListener(new CommonActionSheetDialog.MenuListener() {
                @Override
                public void onItemSelected(int position, String item) {
                    communityId=mList.get(position).getCommunityId();
                    SPManager.getInstance().putInt("communityId",communityId);
                    getDianMing(communityId);
                }
                @Override
                public void onCancel() {
                }
            });
            commonActionSheetDialog.show();
        }else {
            Toast.makeText(SupermarketActivity.this,"当前数据为空，请添加小区",Toast.LENGTH_LONG).show();
        }
    }
    private List<ShangPinList.Result.Products> bubble_sort(List<ShangPinList.Result.Products> list_shangpin)
    {

        for (int i = 0; i < list_shangpin.size(); i++)
        {
            for (int j = i; j < list_shangpin.size(); j++)
            {
                double jiege1;
                if (list_shangpin.get(i).getIfDiscount()==0){
                    jiege1=list_shangpin.get(i).getPrice();
                }else {
                    jiege1=list_shangpin.get(i).getDiscountPrice();
                }
                double jiege2;
                if (list_shangpin.get(j).getIfDiscount()==0){
                    jiege2=list_shangpin.get(j).getPrice();
                }else {
                    jiege2=list_shangpin.get(j).getDiscountPrice();
                }
                if (!jiageIsOne) {
                    if (jiege1 > jiege2) {
                        ShangPinList.Result.Products temp = list_shangpin.get(i);
                        list_shangpin.set(i, list_shangpin.get(j));
                        list_shangpin.set(j, temp);
                    }
                }else {
                    if (jiege1 < jiege2) {
                        ShangPinList.Result.Products temp = list_shangpin.get(i);
                        list_shangpin.set(i, list_shangpin.get(j));
                        list_shangpin.set(j, temp);
                    }
                }
            }
        }
        return list_shangpin;
    }
}