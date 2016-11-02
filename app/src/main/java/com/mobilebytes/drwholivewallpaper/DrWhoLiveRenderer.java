/*******************************************************************************
 * Copyright 2010 fredgrott
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.mobilebytes.drwholivewallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_COLOR_ARRAY;
import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_TEST;
import static android.opengl.GLES10.GL_LINEAR;
import static android.opengl.GLES10.GL_MODELVIEW;
import static android.opengl.GLES10.GL_PROJECTION;
import static android.opengl.GLES10.GL_RGBA;
import static android.opengl.GLES10.GL_TEXTURE_2D;
import static android.opengl.GLES10.GL_TEXTURE_COORD_ARRAY;
import static android.opengl.GLES10.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES10.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES10.GL_UNSIGNED_BYTE;
import static android.opengl.GLES10.GL_VERTEX_ARRAY;
import static android.opengl.GLES10.glBindTexture;
import static android.opengl.GLES10.glClear;
import static android.opengl.GLES10.glEnable;
import static android.opengl.GLES10.glEnableClientState;
import static android.opengl.GLES10.glFinish;
import static android.opengl.GLES10.glFlush;
import static android.opengl.GLES10.glGenTextures;
import static android.opengl.GLES10.glLoadIdentity;
import static android.opengl.GLES10.glMatrixMode;
import static android.opengl.GLES10.glTexImage2D;
import static android.opengl.GLES10.glTexParameterf;
import static android.opengl.GLES10.glViewport;


/**
 * The Class DrWhoLiveRenderer.
 */
public class DrWhoLiveRenderer implements GLSurfaceView.Renderer,
        GLWallpaperService.Renderer {

    private static final String TAG = DrWhoLiveRenderer.class.getSimpleName();
    /**
     * The m context.
     */
    private final Context mContext;

    /**
     * The tunnel.
     */
    private final Tunnel3D tunnel;

    /**
     * The created.
     */
    private boolean created;

    /**
     * The w.
     */
    private int w = 1920;

    /**
     * The h.
     */
    private int h = 1080;

    /**
     * The bmp.
     */
    private Bitmap bmp;

    /**
     * The tex.
     */
    private int tex;

    /**
     * Instantiates a new dr who live renderer.
     *
     * @param context the context
     */
    public DrWhoLiveRenderer(Context context) {
        mContext = context;

        // Internal members..
        tunnel = new Tunnel3D(10, 20);
        created = false;

    }

    /**
     * Load texture.
     *
     * @param bmp the bmp
     * @return the int
     */
    private int loadTexture(Bitmap bmp) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bmp.getHeight()
                * bmp.getWidth() * 4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();

        for (int y = 0; y < bmp.getHeight(); y++) {
            for (int x = 0; x < bmp.getWidth(); x++) {
                ib.put(bmp.getPixel(x, y));
            }
        }
        ib.position(0);
        bb.position(0);

        int[] tmp_tex = new int[1];

        glGenTextures(1, tmp_tex, 0);
        int tex = tmp_tex[0];
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bmp.getWidth(), bmp.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        return tex;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        created = true;

        // Enabling the state…
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        // Loading texture…
        bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.plants03);
        tex = loadTexture(bmp);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        w = width;
        h = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        // Check the created flag…
        boolean c;
        synchronized (this) {
            c = created;
        }
        if (!c) {
            return;
        }

        // Setting up the projection…
        float ratio = (float) w / h;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glViewport(0, 0, w, h);

        GLU.gluPerspective(gl10, 45.0f, ratio, 1f, 100f);

        // Setting up the modelview…
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Clear the z-buffer…
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Render the tunnel…
        tunnel.render(-1.6f);
        tunnel.nextFrame();

        // OpenGL finish
        glFlush();
        glFinish();
    }
}
