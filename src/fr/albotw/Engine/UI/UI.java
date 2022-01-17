package fr.albotw.Engine.UI;

import fr.albotw.Engine.GL.Shader;
import fr.albotw.Engine.Util;
import org.joml.Matrix4f;
import org.lwjgl.nuklear.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static fr.albotw.Engine.CONFIG.WINDOW_HEIGHT;
import static fr.albotw.Engine.CONFIG.WINDOW_WIDTH;
import static fr.albotw.Engine.Util.ioResourceToByteBuffer;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class UI {
    public static UI instance;
    public static NkContext context;

    private ILayout layout;

    private NkUserFont default_font;
    private NkBuffer cmds;
    private NkDrawNullTexture null_texture;
    private NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;

    private ByteBuffer ttf;

    private int vbo, vao, ebo;
    private Shader shader;

    public static void createUI() {
        if (UI.instance == null) {
            UI.instance = new UI();
        }
    }

    private UI() {}

    public void setLayout(ILayout layout) {
        this.layout = layout;
    }

    public void init(long window) throws Exception {
        UI.context = NkContext.create();
        this.default_font = NkUserFont.create();
        this.cmds = NkBuffer.create();
        this.null_texture = NkDrawNullTexture.create();

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();

        NkAllocator ALLOCATOR = NkAllocator.create()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));

        nk_init(UI.context, ALLOCATOR, null);
        nk_buffer_init(this.cmds, ALLOCATOR, 4 * 1024);

        this.shader = new Shader();
        shader.createVertexShader(Util.loadResource("ressources/shaders/NKVertex.glsl"));
        shader.createFragmentShader(Util.loadResource("ressources/shaders/NKFragment.glsl"));
        shader.link();

        int attrib_pos = glGetAttribLocation(shader.getProgramID(), "Position");
        int attrib_uv = glGetAttribLocation(shader.getProgramID(), "TexCoord");
        int attrib_col = glGetAttribLocation(shader.getProgramID(), "Color");

        vbo = glGenBuffers();
        ebo = glGenBuffers();
        vao = glGenVertexArrays();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        glEnableVertexAttribArray(attrib_pos);
        glEnableVertexAttribArray(attrib_uv);
        glEnableVertexAttribArray(attrib_col);

        glVertexAttribPointer(attrib_pos, 2, GL_FLOAT, false, 20, 0);
        glVertexAttribPointer(attrib_uv, 2, GL_FLOAT, false, 20, 8);
        glVertexAttribPointer(attrib_col, 4, GL_UNSIGNED_BYTE, true, 20, 16);

        int nullTexID = glGenTextures();
        null_texture.texture().id(nullTexID);
        null_texture.uv().set(0.5f, 0.5f);

        glBindTexture(GL_TEXTURE_2D, nullTexID);
        try (MemoryStack stack = stackPush()) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        this.initFont();
    }

    public void initFont() {
        try {
            this.ttf = ioResourceToByteBuffer("ressources/FiraSans.ttf", 512 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int BITMAP_W = 1024;
        int BITMAP_H = 1024;
        int FONT_HEIGHT = 18;
        int fontTexID = glGenTextures();

        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(95);

        float scale;
        float descent;

        try (MemoryStack stack = stackPush()) {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;

            ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

            STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
            stbtt_PackEnd(pc);

            // Convert R8 to RGBA8
            ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < bitmap.capacity(); i++) {
                texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
            }
            texture.flip();

            glBindTexture(GL_TEXTURE_2D, fontTexID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            memFree(texture);
            memFree(bitmap);
        }

        default_font
                .width((handle, h, text, len) -> {
                    float text_width = 0;
                    try (MemoryStack stack = stackPush()) {
                        IntBuffer unicode = stack.mallocInt(1);

                        int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
                        int text_len = glyph_len;

                        if (glyph_len == 0) {
                            return 0;
                        }

                        IntBuffer advance = stack.mallocInt(1);
                        while (text_len <= len && glyph_len != 0) {
                            if (unicode.get(0) == NK_UTF_INVALID) {
                                break;
                            }

                            /* query currently drawn glyph information */
                            stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
                            text_width += advance.get(0) * scale;

                            /* offset next glyph */
                            glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
                            text_len += glyph_len;
                        }
                    }
                    return text_width;
                })
                .height(FONT_HEIGHT)
                .query((handle, font_height, glyph, codepoint, next_codepoint) -> {
                    try (MemoryStack stack = stackPush()) {
                        FloatBuffer x = stack.floats(0.0f);
                        FloatBuffer y = stack.floats(0.0f);

                        STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);
                        IntBuffer advance = stack.mallocInt(1);

                        stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
                        stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

                        NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);

                        ufg.width(q.x1() - q.x0());
                        ufg.height(q.y1() - q.y0());
                        ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
                        ufg.xadvance(advance.get(0) * scale);
                        ufg.uv(0).set(q.s0(), q.t0());
                        ufg.uv(1).set(q.s1(), q.t1());
                    }
                })
                .texture(it -> it
                        .id(fontTexID));

        nk_style_set_font(UI.context, default_font);
    }

    public boolean isMouseOnUI() {
        return nk_window_is_any_hovered(UI.context);
    }

    public void render(int AA, int max_vertex_buffer, int max_element_buffer) {
        if (this.layout != null) {
            layout.layout(layout.getX(), layout.getY());
        };

        try {
            // setup global state
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_SCISSOR_TEST);
            glActiveTexture(GL_TEXTURE0);

            // setup program
            this.shader.bind();
            this.shader.setUniform("Texture", 0);
            Matrix4f proj = new Matrix4f(2.0f / WINDOW_WIDTH, 0.0f, 0.0f, 0.0f,
                                         0.0f, -2.0f / WINDOW_HEIGHT, 0.0f, 0.0f,
                                         0.0f, 0.0f, -1.0f, 0.0f,
                                         -1.0f, 1.0f, 0.0f, 1.0f);
            this.shader.setUniform("ProjMtx", proj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        {
            // convert from command queue into draw list and draw to screen

            // allocate vertex and element buffer
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

            glBufferData(GL_ARRAY_BUFFER, max_vertex_buffer, GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, max_element_buffer, GL_STREAM_DRAW);

            // load draw vertices & elements directly into vertex + element buffer
            ByteBuffer vertices = Objects.requireNonNull(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, max_vertex_buffer, null));
            ByteBuffer elements = Objects.requireNonNull(glMapBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_WRITE_ONLY, max_element_buffer, null));
            try (MemoryStack stack = stackPush()) {
                // fill convert configuration
                NkConvertConfig config = NkConvertConfig.calloc(stack)
                        .vertex_layout(VERTEX_LAYOUT)
                        .vertex_size(20)
                        .vertex_alignment(4)
                        .null_texture(null_texture)
                        .circle_segment_count(22)
                        .curve_segment_count(22)
                        .arc_segment_count(22)
                        .global_alpha(1.0f)
                        .shape_AA(AA)
                        .line_AA(AA);

                // setup buffers to load vertices and elements
                NkBuffer vbuf = NkBuffer.malloc(stack);
                NkBuffer ebuf = NkBuffer.malloc(stack);

                nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
                nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
                nk_convert(UI.context, cmds, vbuf, ebuf, config);
            }
            glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
            glUnmapBuffer(GL_ARRAY_BUFFER);

            // iterate over and execute each draw command
            float fb_scale_x = 1.0f;
            float fb_scale_y = 1.0f;

            long offset = NULL;
            for (NkDrawCommand cmd = nk__draw_begin(UI.context, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, UI.context)) {
                if (cmd.elem_count() == 0) {
                    continue;
                }
                glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
                glScissor(
                        (int) (cmd.clip_rect().x() * fb_scale_x),
                        (int) ((WINDOW_HEIGHT - (int) (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
                        (int) (cmd.clip_rect().w() * fb_scale_x),
                        (int) (cmd.clip_rect().h() * fb_scale_y)
                );
                glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
                offset += cmd.elem_count() * 2;
            }
            nk_clear(UI.context);
        }

        // default OpenGL state
        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glDisable(GL_BLEND);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);
    }
}
