package com.scientificrat.robocon2017blecontrol.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scientificrat.robocon2017blecontrol.R;

import java.util.ArrayList;

/**
 * Created by huangzhengyue on 2016/10/27.
 */

public class DeviceListAdapter extends BaseAdapter {

    private int selectedPosition = -1;

    private LayoutInflater layoutInflater; //得到一个LayoutInflater对象用来导入布局

    public DeviceListAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    // 设备表
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    public String getDeviceAddress(int position) {
        return deviceList.get(position).getAddress();
    }

    public String getDeviceName(int position) {
        return deviceList.get(position).getName();
    }

    //for debug only
    public String getDeviceUUID(int position) {
        ParcelUuid[] parcelUUIDs = deviceList.get(position).getUuids();
        if (parcelUUIDs != null && parcelUUIDs.length != 0) {
            for (ParcelUuid parcelUUID : parcelUUIDs) {
                Log.e("uuid:", parcelUUID.toString());
            }
            return parcelUUIDs[0].toString();
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.devicelist_item, null);
        }

        TextView addrText = (TextView) convertView.findViewById(R.id.deviceAddressText);
        TextView nameText = (TextView) convertView.findViewById(R.id.deviceName);

        addrText.setText(deviceList.get(position).getAddress());
        nameText.setText(deviceList.get(position).getName());
        // selectedPosition 需要对应的adapterView onItemClickedListener 中指定
        if (this.selectedPosition == position) {
//            convertView.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(230, 238, 156)));
            convertView.setBackgroundColor(Color.rgb(230, 238, 156));

        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    public void addDevice(BluetoothDevice bluetoothDevice) {
        //这里不用担心任何问题，BluetoothDevice.hashCode()是使用 MAC address 计算的
        if (!this.deviceList.contains(bluetoothDevice)) {
            deviceList.add(bluetoothDevice);
        }
        //刷新
        this.notifyDataSetInvalidated();
    }

    public void removeInfo(int position) {
        deviceList.remove(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public BluetoothDevice getSelectedDevice() {
        try {
            return deviceList.get(selectedPosition);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    /**
     * 清空列表
     */
    public void clear() {
        this.deviceList.clear();
        clearSelect();
    }

    public void clearSelect() {
        this.selectedPosition = -1;// 什么都没有选中
        this.notifyDataSetInvalidated();// 更新视图
    }
}

