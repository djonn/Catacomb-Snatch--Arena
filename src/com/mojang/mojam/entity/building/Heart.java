package com.mojang.mojam.entity.building;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.animation.HeartUseSmall;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;
import com.mojang.mojam.screen.Screen;

public class Heart extends Building{

    public Heart(double x, double y, int team) {
		super(x, y, team);
	}

	static int healthValue = 3;
	private Player reciever = null;

    public void render(Screen screen) {
        Bitmap image = Art.heart;

        screen.blit(image, pos.x - image.w / 2, pos.y - image.h + 8);
        renderMarker(screen);
    }


    public void use(Entity user) {
        super.use(user);
        
        if (user instanceof Player) {
        	reciever = (Player) user;
        	this.remove();
        	level.addEntity(new HeartUseSmall(pos.x, pos.y));
        	if(reciever.health < reciever.maxHealth){
        		reciever.health += healthValue;
        		if(reciever.health > reciever.maxHealth){
        			reciever.health = reciever.maxHealth;
        		}
        		
        	}
        	reciever.carrying = null;
        }
        
        
    }
}
