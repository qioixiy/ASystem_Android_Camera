package com.xyz.DrivingRecorder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListViewSlideAdapter extends BaseAdapter {

    private List<VideoDataModel.VideoMetaData> dataList;
    private Context context;
    private OnClickListenerEditOrDelete onClickListenerEditOrDelete;

    private InnerItemOnClickListener mInnerItemOnClickListener;

    public ListViewSlideAdapter(Context context, List<VideoDataModel.VideoMetaData> bulbList) {
        this.dataList = bulbList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final String strName = dataList.get(position).getName();
        final String strContext = dataList.get(position).getPath();
        final String strDesc = dataList.get(position).getDesc();
        View view;
        ViewHolder viewHolder;
        if (null == convertView) {
            view = View.inflate(context, R.layout.item_slide_video, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView) view.findViewById(R.id.tvName);
            viewHolder.tvContext = (TextView) view.findViewById(R.id.tvContent);
            viewHolder.img = (ImageView) view.findViewById(R.id.imgLamp);
            viewHolder.tvDelete = (TextView) view.findViewById(R.id.delete);
            viewHolder.tvEdit = (TextView) view.findViewById(R.id.tvEdit);
            viewHolder.btn_play = (Button)view.findViewById(R.id.button_item_slide_video_play);

            view.setTag(viewHolder);//store up viewHolder
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.img.setImageResource(R.drawable.video_icon);
        viewHolder.tvName.setText(strName);
        viewHolder.tvContext.setText("录制原因：" + strDesc);
        viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListenerEditOrDelete != null) {
                    onClickListenerEditOrDelete.OnClickListenerDelete(position);
                }
            }
        });
        viewHolder.btn_play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mInnerItemOnClickListener != null) {
                    mInnerItemOnClickListener.onClick(position);
                }
            }
        });

       /* view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInnerItemOnClickListener != null) {
                    mInnerItemOnClickListener.onClick(position);
                }
            }
        });*/

        return view;
    }

    interface InnerItemOnClickListener {
        void onClick(int position);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnClickListener listener){
        this.mInnerItemOnClickListener = listener;
    }

    private class ViewHolder {
        TextView tvName, tvContext, tvEdit, tvDelete;
        ImageView img;
        Button btn_play;
    }

    public interface OnClickListenerEditOrDelete {
        void OnClickListenerEdit(int position);

        void OnClickListenerDelete(int position);
    }

    public void setOnClickListenerEditOrDelete(OnClickListenerEditOrDelete onClickListenerEditOrDelete1) {
        this.onClickListenerEditOrDelete = onClickListenerEditOrDelete1;
    }

}