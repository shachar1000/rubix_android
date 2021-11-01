
package com.example.a3d_rubiks;




import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.ActionBar;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import android.app.ActionBar.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class OpenGLES20Activity extends Activity {
    public volatile static Boolean startScramble = false;
    public volatile static Boolean startSolve = false;
    public static Boolean getStartScramble() { return startScramble; }
    public static void setStartScramble(Boolean startScrambleArg) { startScramble = startScrambleArg; }
    public static Boolean getStartSolve() { return startSolve; }
    public static void setStartSolve(Boolean startSolveArg) { startSolve = startSolveArg; }
    private GLSurfaceView gLView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView gLView = new MyGLSurfaceView(this);
        setContentView(gLView);
        LinearLayout ll = new LinearLayout(this); ll.setWeightSum(2);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        Button buttonScramble = new Button(this); //buttonScramble.setId(View.generateViewId());
        Button buttonSolve = new Button(this); //buttonSolve.setId(View.generateViewId());
        buttonScramble.setText("ערבב"); buttonSolve.setText("פתור");
        //RelativeLayout.LayoutParams Btnparams = (RelativeLayout.LayoutParams) buttonScramble.getLayoutParams();
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        param.setMargins(20, 20, 10, 20);
        buttonSolve.setLayoutParams(param); buttonScramble.setLayoutParams(param);
        ll.addView(buttonScramble); // new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)
        ll.addView(buttonSolve);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        this.addContentView(ll, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        buttonScramble.setOnClickListener(new View.OnClickListener() {public void onClick(View v) { setStartScramble(true); Log.d("TAG", String.valueOf(getStartScramble()));}});
        buttonSolve.setOnClickListener(new View.OnClickListener() {public void onClick(View v) { MyGLRenderer.delayStringCubeMapping = 0; setStartScramble(false); setStartSolve(true); }});

        if(!Python.isStarted()) { Python.start(new AndroidPlatform(this)); }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("android_rubix");
        //PyObject obj = pyobj.callAttr("generateMoves"); // pass arguments
        //Log.d("TAG", "Hello");
        //Log.d("TAG", obj.toString());
    }
}
class MyGLSurfaceView extends GLSurfaceView {
    private float previousX; private float previousY;
    private final float FACTOR = 180.0f / 2000;
    public final MyGLRenderer renderer;
    public MyGLSurfaceView(Context context){
        super(context);
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer();
        setRenderer(renderer);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX(); float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x-previousX;
                float dy = y-previousY;
                renderer.setAngleX(renderer.getAngleX()+ dx * FACTOR);
                renderer.setAngleY(renderer.getAngleY()+ dy * FACTOR);
        }
        previousX = x;
        previousY = y;
        return true;
    }

}