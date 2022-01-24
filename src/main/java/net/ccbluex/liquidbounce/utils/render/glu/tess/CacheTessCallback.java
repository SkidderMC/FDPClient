/*
 * Copyright (c) 2002-2008 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.ccbluex.liquidbounce.utils.render.glu.tess;

import net.ccbluex.liquidbounce.utils.render.glu.VertexData;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class CacheTessCallback extends GLUtessellatorCallbackAdapter {
    public final static CacheTessCallback INSTANCE = new CacheTessCallback();

    private ArrayList<double[]> vertices = null;
    private ArrayList<GlCommand> commands = null;

    private int type = 0;
    private int startIndex = 0;
    private int count = 0;

    public void record() {
        if(vertices != null) {
            vertices.clear();
        } else {
            vertices = new ArrayList<>();
        }
        if(commands != null) {
            commands.clear();
        } else {
            commands = new ArrayList<>();
        }
    }

    public VertexCache getResult() {
        VertexCache vertexCache = new VertexCache(vertices, commands);
        commands = null;
        vertices = null;
        return vertexCache;
    }

    public void begin(int type) {
        startIndex = vertices.size();
        this.type = type;
        count = 0;
    }

    public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
        double[] combined = new double[6];
        combined[0] = coords[0];
        combined[1] = coords[1];
        combined[2] = coords[2];
        combined[3] = 1;
        combined[4] = 1;
        combined[5] = 1;

        for (int i=0;i<outData.length;i++) {
            outData[i] = new VertexData(combined);
        }
    }

    public void end() {
        commands.add(new GlCommand(startIndex, count, type));
    }

    public void vertex(Object vertexData) {
        VertexData vertex = (VertexData) vertexData;

        vertices.add(vertex.data);
        count++;
    }

    public class GlCommand {
        public int from;
        public int count;
        public int type;

        public GlCommand(int from, int count, int type) {
            this.from = from;
            this.count = count;
            this.type = type;
        }
    }

    public class VertexCache {

        private final int vbo;
        private final ArrayList<GlCommand> commands;

        public VertexCache(ArrayList<double[]> vertices, ArrayList<GlCommand> commands) {
            this.vbo = GL15.glGenBuffers();
            FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * 3);
            for (double[] vertex : vertices) { // float占用的内存比double小，所以这里用float
                buffer.put((float) vertex[0]);
                buffer.put((float) vertex[1]);
                buffer.put((float) vertex[2]);
            }
            buffer.flip();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            this.commands = commands;
        }

        public void render() {
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
            for (GlCommand command : commands) {
                GL11.glDrawArrays(command.type, command.from, command.count);
            }
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        }

        public void destroy() {
            GL15.glDeleteBuffers(vbo);
            commands.clear();
        }
    }
}