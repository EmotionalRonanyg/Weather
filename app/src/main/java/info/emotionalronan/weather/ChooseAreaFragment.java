package info.emotionalronan.weather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.emotionalronan.weather.db.City;
import info.emotionalronan.weather.db.County;
import info.emotionalronan.weather.db.Province;
import info.emotionalronan.weather.util.HttpUtil;
import info.emotionalronan.weather.util.LogUtil;
import info.emotionalronan.weather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by YG on 2017/4/27.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY= 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //区列表
    private List<County> countyList;

    private Province provinceSelected;//选中的省
    private City citySelected; //选中的市
    private int currentLevel;//当前选中的级别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        LogUtil.d("----ChooseAreaFragment","onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    provinceSelected = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    citySelected = cityList.get(position);
                    queryCounties();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     *  查询全国所有的省 优先从数据库中查 如果没有查到在到服务器
     */
    private void queryProvinces() {


        titleText.setText("中国");
        backButton.setVisibility(View.GONE);

        provinceList = DataSupport.findAll(Province.class);

        if (provinceList.size()>0){
            dataList.clear();
            for (Province provice:provinceList){
                dataList.add(provice.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /**
     *  查询全省所有的市 优先从数据库中查 如果没有查到在到服务器
     */
    private void queryCities() {
        titleText.setText(provinceSelected.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?",String.valueOf(provinceSelected.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city :cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;

        }else {
            int provinceCode = provinceSelected.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    /**
     *  查询全市所有的区 优先从数据库中查 如果没有查到在到服务器
     */
    private void queryCounties() {
        titleText.setText(citySelected.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?",String.valueOf(citySelected.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county :countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;

        }else {
            int provinceCode = provinceSelected.getProvinceCode();
            int cityCode = citySelected.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     *  根据传入的地址和类型 在到服务器上 查询省市县数据
     */
    private void queryFromServer(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,provinceSelected.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,citySelected.getId());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                               queryProvinces();
                            }else if("city".equals(type)){
                               queryCities();
                            }else if("county".equals(type)){
                               queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     *  显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中 ...");
            progressDialog.setCanceledOnTouchOutside(false);//
        }
        progressDialog.show();
    }
    /**
     *  关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
