package com.example.taek.googlemapsample;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Taek on 2017-11-15.
 */

public class FoundListViewAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater = null;
    private ArrayList<Address> addressList = null;
    private ViewHolder viewHolder = null;

    public FoundListViewAdapter(Context context, ArrayList<Address> arrays) {
        this.context = context;
        this.addressList = arrays;
        this.inflater = LayoutInflater.from(context);
    }

    public class ViewHolder {
        TextView tv_address = null;
    }

    // Adapter가 관리할 Data의 개수를 설정합니다.
    @Override
    public int getCount() {
        return addressList.size();
    }

    // Adapter가 관리하는 Data의 Item의 position을 <객체> 형태로 얻어 옵니다.
    @Override
    public Address getItem(int position) {
        return addressList.get(position);
    }

    // Adapter가 관리하는 Data의 Item의 position 값의 ID를 얻어 옵니다.
    @Override
    public long getItemId(int position) {
        return position;
    }

    // ListView에 뿌려질 한 줄의 Row를 설정합니다.
    @Override
    public View getView(int position, View convertview, ViewGroup parent) {
        View v = convertview;

        if (v == null) {
            viewHolder = new ViewHolder();
            v = inflater.inflate(R.layout.item_listview, parent);
            viewHolder.tv_address = (TextView) v.findViewById(R.id.tv_address);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        viewHolder.tv_address.setText(getItem(position).getAddressLine(0));

        // image 나 button 등에 Tag를 사용해서 position 을 부여해 준다.
        // Tag란 View를 식별할 수 있게 바코드 처럼 Tag를 달아 주는 View의 기능
        // 이라고 생각 하시면 됩니다.

        return v;
    }

    // Adapter가 관리하는 Data List를 교체 한다.
    // 교체 후 Adapter.notifyDataSetChanged() 메서드로 변경 사실을
    // Adapter에 알려 주어 ListView에 적용 되도록 한다.
    public void setArrayList(ArrayList<Address> arrays) {
        this.addressList = arrays;
    }

    @Override
    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }

    public void free() {
        inflater = null;
        addressList = null;
        viewHolder = null;
        context = null;
    }
}
