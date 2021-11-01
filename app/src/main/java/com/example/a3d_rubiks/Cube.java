package com.example.a3d_rubiks;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.HashMap;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Cube {
    public volatile float angleAnimation = 0f;
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    //"attribute vec4 aColor;" +
                    //"uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    //"  vColor = aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;


    private final float[] mMVPMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private float[] mTempMatrix = new float[16];

    static final int COORDS_PER_VERTEX = 3;
    static final int COLORS_PER_VERTEX = 4;

    private short drawOrder[][] = {
            {0, 1, 2, 0, 2, 3},//front
            {0, 4, 5, 0, 5, 3}, //Top
            {0, 1, 6, 0, 6, 4}, //left
            {3, 2, 7, 3, 7 ,5}, //right
            {1, 2, 7, 1, 7, 6}, //bottom
            {4, 6, 7, 4, 7, 5} //back
    }; //(order to draw vertices)

    final float cubeColor3[][] =
            { // order is front, top, left, right, bottom, back
                    { 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f },
                    { 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f },
                    { 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f },
                    { 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f },
                    { 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f },
                    { 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f, 0.14117647058f, 0.14117647058f, 0.14117647058f, 1.0f }
            };
    float cubeCoords[] = {
            -0.5f, 0.5f, 0.5f,   // front top left 0
            -0.5f, -0.5f, 0.5f,   // front bottom left 1
            0.5f, -0.5f, 0.5f,   // front bottom right 2
            0.5f, 0.5f, 0.5f,  // front top right 3
            -0.5f, 0.5f, -0.5f,   // back top left 4
            0.5f, 0.5f, -0.5f,   // back top right 5
            -0.5f, -0.5f, -0.5f,   // back bottom left 6
            0.5f, -0.5f, -0.5f,  // back bottom right 7
    };
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride = COLORS_PER_VERTEX*4;
    private final int[] offsets;

    public Cube(int[] offsets) {
        this.offsets = offsets;
        for (int i = 0; i < cubeCoords.length; i++) { cubeCoords[i] += (float)offsets[i % 3]-1f; }
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }
    public void draw(float[] mProjectionMatrix, int axis, float angleX, float angleY) {
        //GLES20.glFrontFace(GLES20.GL_CCW); GLES20.glEnable(GLES20.GL_CULL_FACE); GLES20.glCullFace(GLES20.GL_BACK);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0, -15f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        float[] animationMatrix = new float[16]; Matrix.setIdentityM(animationMatrix, 0);
        Matrix.rotateM(animationMatrix, 0, this.getAngleAnimation(), (0==axis)?1:0, (1==axis)?1:0, (2==axis)?1:0);
        //Matrix.rotateM(animationMatrix, 0, 50, 0, 0, 1);
        Matrix.rotateM(mRotationMatrix, 0, angleY, 1.0f, 0, 0);
        Matrix.rotateM(mRotationMatrix, 0, angleX, 0, 1.0f, 0);
        Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrix, 0, animationMatrix, 0);
        mTempMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mModelMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);
        mTempMatrix = mMVPMatrix.clone();
        Matrix.multiplyMM(mMVPMatrix, 0, mTempMatrix, 0, mModelMatrix, 0);

        for (int face = 0; face < 6; face++) {
            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
            ByteBuffer dlb = ByteBuffer.allocateDirect(6 * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder[face]);
            drawListBuffer.position(0);
            final int min = 0; final int max = 5; // inclusive
            final int random = new Random().nextInt((max-min) + 1) + min;
            GLES20.glUniform4fv(mColorHandle, 1, cubeColor3[face], 0);
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder[face].length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        }
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    public int[] getOffsets() {
        return offsets;
    }
    public float getAngleAnimation() {
        return angleAnimation;
    }
    public void setAngleAnimation(float angle) {
        angleAnimation = angle;
    }
    public void setColor(int faceIndex, float[] color) {
        this.cubeColor3[faceIndex] = color;
    }
}