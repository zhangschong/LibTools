package com.lib.utils;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by zhanghong on 17-5-17.
 */

public class ViewCacher {

    private SparseArray<View> mViews = new SparseArray<>(20);

    private final  View mParents;

    public ViewCacher(View view){
        mParents = view;
    }

    public ViewCacher(Context context, int viewId){
        mParents = View.inflate(context,viewId,null);
    }

    public <T extends View> T findView(int id){
        View view = mViews.get(id);
        if(null == view){
            view = mParents.findViewById(id);
            mViews.put(id,view);
        }
        return (T) view;
    }

    public View getParentView(){
        return mParents;
    }

}
