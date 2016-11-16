package com.geri.app.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.geri.app.ui.R;

/**
 * Created by Geri on 2016/10/22.
 */
public class MapShowActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener, View.OnClickListener {

    // 浏览路线节点相关
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    private LocationClient locationClient;
    private BDLocationListener locationListener;
    private int locType;
    private float direction;// 手机方向信息
    private double longitude;// 精度
    private float radius;// 定位精度半径，单位是米
    private double latitude;// 维度
    private String addrStr;// 反地理编码
    private String province;// 省份信息
    private String city;// 城市信息
    private String district;// 区县信息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        // 初始化地图
        TextView back = (TextView) findViewById(R.id.back);
        Button jump = (Button) findViewById(R.id.jump);
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        //获取定位到我的位置
        init();
        jump.setOnClickListener(this);
        back.setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
                break;

            case R.id.jump:
                Intent intent = new Intent();
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("addrStr", addrStr);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    /**
     * 获取定位到我的位置
     */
    private void init() {

        mBaidumap = mMapView.getMap();

        mBaidumap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaidumap.setMyLocationEnabled(true);
        // 1. 初始化LocationClient类
        locationClient = new LocationClient(getApplicationContext());
        // 2. 声明LocationListener类
        locationListener = new MyLocationListener();
        // 3. 注册监听函数
        locationClient.registerLocationListener(locationListener);
        // 4. 设置参数
        LocationClientOption locOption = new LocationClientOption();
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
//        locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

        locationClient.setLocOption(locOption);
        // 6. 开启/关闭 定位SDK
        locationClient.start();
    }



    class MyLocationListener implements BDLocationListener {
        // 异步返回的定位结果
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            locType = location.getLocType();
//            Toast.makeText(MapShowActivity.this, "当前定位的返回值是："+locType, Toast.LENGTH_SHORT).show();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            if (location.hasRadius()) {// 判断是否有定位精度半径
                radius = location.getRadius();
            }
            if (locType == BDLocation.TypeGpsLocation) {//
//                Toast.makeText(
//                        MapShowActivity.this,
//                        "当前速度是：" + location.getSpeed() + "~~定位使用卫星数量："
//                                + location.getSatelliteNumber(),
//                        Toast.LENGTH_SHORT).show();
            } else if (locType == BDLocation.TypeNetWorkLocation) {
                addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
//                Toast.makeText(MapShowActivity.this, addrStr,
//                        Toast.LENGTH_SHORT).show();
            }
            direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
            province = location.getProvince();// 省份
            city = location.getCity();// 城市
            district = location.getDistrict();// 区县
//            Toast.makeText(MapShowActivity.this,
//                    province + "~" + city + "~" + district, Toast.LENGTH_SHORT)
//                    .show();
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)//
                    .direction(direction)// 方向
                    .latitude(latitude)//
                    .longitude(longitude)//
                    .build();
            // 设置定位数据
            mBaidumap.setMyLocationData(locData);
            LatLng ll = new LatLng(latitude, longitude);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
            mBaidumap.animateMapStatus(msu);

        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }
}
