package com.mojang.mojam.screen;

import java.util.*;

public class Bitmap {
    public int w, h;
    public int[] pixels;

    public Bitmap(int w, int h) {
        this.w = w;
        this.h = h;
        pixels = new int[w * h];
    }

    public void clear(int color) {
        Arrays.fill(pixels, color);
    }

    public void blit(Bitmap bitmap, int x, int y) {
        int x0 = x;
        int x1 = x + bitmap.w;
        int y0 = y;
        int y1 = y + bitmap.h;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            int sp = (yy - y) * bitmap.w + (x0 - x);
            tp -= sp;
            for (int xx = sp; xx < sp + ww; xx++) {
                int col = bitmap.pixels[xx];
                if (col < 0) pixels[tp + xx] = col;
            }
        }
    }

    public void blit(Bitmap bitmap, int x, int y, int www, int hhh) {
        int x0 = x;
        int x1 = x + www;
        int y0 = y;
        int y1 = y + hhh;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            int sp = (yy - y) * bitmap.w + (x0 - x);
            tp -= sp;
            for (int xx = sp; xx < sp + ww; xx++) {
                int col = bitmap.pixels[xx];
                if (col < 0) pixels[tp + xx] = col;
            }
        }
    }

    public void colorBlit(Bitmap bitmap, int x, int y, int color) {
        int x0 = x;
        int x1 = x + bitmap.w;
        int y0 = y;
        int y1 = y + bitmap.h;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        int a2 = (color >> 24) & 0xff;
        int a1 = 256 - a2;

        int rr = color & 0xff0000;
        int gg = color & 0xff00;
        int bb = color & 0xff;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            int sp = (yy - y) * bitmap.w + (x0 - x);
            for (int xx = 0; xx < ww; xx++) {
                int col = bitmap.pixels[sp + xx];
                if (col < 0) {
                    int r = (col & 0xff0000);
                    int g = (col & 0xff00);
                    int b = (col & 0xff);

                    r = ((r * a1 + rr * a2) >> 8) & 0xff0000;
                    g = ((g * a1 + gg * a2) >> 8) & 0xff00;
                    b = ((b * a1 + bb * a2) >> 8) & 0xff;
                    pixels[tp + xx] = 0xff000000 | r | g | b;
                }
            }
        }
    }

    public void fill(int x, int y, int bw, int bh, int color) {
        int x0 = x;
        int x1 = x + bw;
        int y0 = y;
        int y1 = y + bh;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            for (int xx = 0; xx < ww; xx++) {
                pixels[tp + xx] = color;
            }
        }
    }
    
public void line(int x1, int y1, int x2, int y2, int color) {
		
		/*if (x1 < 0)
			x1 = 0;
		if (y1 < 0)
			y1 = 0;
		if (x1 > w)
			x1 = w;
		if (y1 > h)
			y1 = h;
		
		if (x2 < 0)
			x2 = 0;
		if (y2 < 0)
			y2 = 0;
		if (x2 > w)
			x2 = w;
		if (y2 > h)
			y2 = h;*/
		
		if (
				x1 >= w || x1 <=0 ||
				x2 >= w || x2 <=0 ||
				y1 >= h || y1 <=0 ||
				y2 >= h || y2 <=0
				) {
			return;
		}
		
		int dx=Math.abs(x2-x1);
		int dy=Math.abs(y2-y1);
		int sx=1;
		int sy=1;
		
		if (x1 > x2) sx=-1;
		if (y1 > y2) sy=-1;
		
		int err=dx-dy;
		int e2=2*err;				
		
		do {
			pixels[y1*w + x1] = color;
			if (x1==x2 && y1==y2) break;
			
			e2=2*err;
			if ( e2 > -dy ) {
				err-=dy;
				x1+=sx;
			}
			if (e2 < dx ) {
				err+=dx;
				y1+=sy;
			}
		} while ( true );
		
	}
}