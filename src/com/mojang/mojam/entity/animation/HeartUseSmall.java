package com.mojang.mojam.entity.animation;

import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Screen;

public class HeartUseSmall extends Animation {
    public HeartUseSmall(double x, double y) {
        super(x, y, TurnSynchronizer.synchedRandom.nextInt(10) + 40); //@random
    }

    public void render(Screen screen) {
        int anim = Art.fxHeartUseSmall.length - life * Art.fxHeartUseSmall.length / duration - 1;
        screen.blit(Art.fxHeartUseSmall[anim][0], pos.x - 16, pos.y - 16 - 4);
    }
}
