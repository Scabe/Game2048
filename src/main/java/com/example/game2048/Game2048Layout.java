package com.example.game2048;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 晨阳大帅比 on 2018/2/23.
 */

public class Game2048Layout extends RelativeLayout{

    public int mColumn = 4;
    private Game2048Item[] mGame2048Items = null;
    private int mMargin = 10;
    private int mPadding;
    private GestureDetector mGestureDetector;
    private OnGame2048Listener mGame2048Listener;
    private boolean isMergrHappen = true;
    private boolean isMoveHappen = true;
    private int mScore = 0;

    public Game2048Layout(Context context) {
        this(context,null,0);
    }

    public Game2048Layout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Game2048Layout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPadding = Math.min(getPaddingBottom(),getPaddingLeft());
        mGestureDetector = new GestureDetector(context,new MyGestureDetector());
    }

    private boolean once = true;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int length = Math.min(getMeasuredHeight(),getMeasuredWidth());
        int childWidth = (length - mPadding*2 - mMargin*(mColumn-1))/mColumn;
        if(once){
            if(mGame2048Items == null)
                mGame2048Items = new Game2048Item[mColumn*mColumn];
            for(int i=0;i<mColumn*mColumn;i++){
                Game2048Item item = new Game2048Item(getContext());
                mGame2048Items[i] = item;
                item.setId(i+1);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(childWidth,childWidth);
                if((i+1)%mColumn!=0)
                    lp.rightMargin = mMargin;
                if(i%mColumn!=0)
                    lp.addRule(RelativeLayout.RIGHT_OF,mGame2048Items[i-1].getId());
                if((i+1)>mColumn){
                    lp.topMargin = mMargin;
                    lp.addRule(RelativeLayout.BELOW,mGame2048Items[i-mColumn].getId());
                }
                addView(item,lp);
            }
            generateNum();
        }
        once = false;
        setMeasuredDimension(length,length);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private enum ACTION{
        LEFT,RIGHT,UP,DOWN
    }

    private class MyGestureDetector implements GestureDetector.OnGestureListener {

        final int FLING_MIN_DISTANCE = 50;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = e2.getX()-e1.getX();
            float y = e2.getY()-e1.getY();
            if(x>FLING_MIN_DISTANCE && Math.abs(velocityX)>Math.abs(velocityY))
                action(ACTION.RIGHT);
            if(x<-FLING_MIN_DISTANCE && Math.abs(velocityX)>Math.abs(velocityY))
                action(ACTION.LEFT);
            if(y>FLING_MIN_DISTANCE && Math.abs(velocityX)<Math.abs(velocityY))
                action(ACTION.DOWN);
            if(y<-FLING_MIN_DISTANCE && Math.abs(velocityX)<Math.abs(velocityY))
                action(ACTION.UP);
            return true;
        }

        private void action(ACTION action) {
            for(int i=0;i<mColumn;i++){
                List<Game2048Item> row = new ArrayList<>();
                for(int j=0;j<mColumn;j++){
                    int index = getIndexByAction(action,i,j);
                    Game2048Item item = mGame2048Items[index];
                    if(item.getNumber()!=0)
                        row.add(item);
                }
                for(int j=0;j<mColumn;j++){
                    int index = getIndexByAction(action,i,j);
                    Game2048Item item = mGame2048Items[index];
                    if(row.size()>j) {
                        if (item.getNumber() != row.get(j).getNumber()) {
                            isMoveHappen = true;
                            break;
                        }
                    }
                }
                //合并相同的
                mergeItem(row);

                //设置合并后的值
                for(int j=0;j<mColumn;j++){
                    int index = getIndexByAction(action,i,j);
                    if(row.size()>j)
                        mGame2048Items[index].setNumber(row.get(j).getNumber());
                    else
                        mGame2048Items[index].setNumber(0);
                }
            }
            generateNum();
        }

        private void mergeItem(List<Game2048Item> row) {
            if(row.size()<2)
                return;
            for(int j = 0;j<row.size()-1;j++){
                Game2048Item item1 = row.get(j);
                Game2048Item item2 = row.get(j+1);
                if(item1.getNumber()==item2.getNumber()){
                    isMergrHappen = true;
                    int val = item1.getNumber()*2;
                    item1.setNumber(val);
                    mScore += val;   //加分
                    if(mGame2048Listener!=null)
                        mGame2048Listener.onScoreChange(mScore);
                    row.remove(j+1);
                }
            }
        }

        private int getIndexByAction(ACTION action, int i, int j) {
            int index = -1;
            switch (action){
                case UP:
                    index = i + j * mColumn;
                    break;
                case DOWN:
                    index = mColumn*(mColumn-1) + i - mColumn * j;
                    break;
                case LEFT:
                    index = j + i * mColumn;
                    break;
                case RIGHT:
                    index = mColumn * i + mColumn - 1 - j;
                    break;
            }
            return index;
        }
    }

    /**
     * 产生一个块
     */
    private void generateNum() {
        if(checkOver()) {
            if (mGame2048Listener != null)
                mGame2048Listener.onGameOver();
            return;
        }
        if(isMergrHappen||isMoveHappen){
            Random random = new Random();
            int next = random.nextInt(mColumn*mColumn);
            Game2048Item item = mGame2048Items[next];
            while (item.getNumber()!=0){
                next = random.nextInt(mColumn*mColumn);
                item = mGame2048Items[next];
            }
            item.setNumber(Math.random() > 0.5 ? 4 : 2 );
            isMoveHappen = isMergrHappen = false;
        }
    }

    private boolean checkOver(){
        if(!isFull()){
            return false;
        }
        for(int i=0;i<mColumn;i++){
            for(int j=0;j<mColumn;j++){
                int index = i * mColumn + j;
                Game2048Item item = mGame2048Items[index];
                if((index+1)%mColumn!=0){ //不是最后一列，则判断和他右边相邻一个数字是否相同
                    Game2048Item itemright = mGame2048Items[index+1];
                    if(item.getNumber()==itemright.getNumber())
                        return false;
                }
                if(index%mColumn!=0){ //不是第一列，则判断和他左边相邻一个数字是否相同
                    Game2048Item itemleft = mGame2048Items[index-1];
                    if(item.getNumber()==itemleft.getNumber())
                        return false;
                }
                if(index<mColumn*(mColumn-1)){ //不是最后一行，则判断和他下边相邻一个数字是否相同
                    Game2048Item itembottom = mGame2048Items[index+mColumn];
                    if(item.getNumber()==itembottom.getNumber())
                        return false;
                }
                if(index+1>mColumn){ //不是第一行，则判断和他上边相邻一个数字是否相同
                    Game2048Item itemtop = mGame2048Items[index-mColumn];
                    if(item.getNumber()==itemtop.getNumber())
                        return false;
                }
            }
        }
        return true;
    }

    private boolean isFull() {
        //检测所有位置是否都有除0以外的数字
        for(int i=0;i<mGame2048Items.length;i++){
            if(mGame2048Items[i].getNumber()==0)
                return false;
        }
        return true;
    }

    public void reStart(){
        for(int i=0;i<mColumn;i++){
            for(int j=0;j<mColumn;j++){
                int index = i * mColumn + j;
                Game2048Item item = mGame2048Items[index];
                item.setNumber(0);
            }
        }
        mScore = 0;
        mGame2048Listener.onScoreChange(0);
        once = true;
        generateNum();
    }

    public void setOnGame2048Listener(OnGame2048Listener onGame2048Listener){
        this.mGame2048Listener = onGame2048Listener;
    }

}
