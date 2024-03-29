package com.mojang.mojam.entity.building;

import com.mojang.mojam.entity.*;
import com.mojang.mojam.entity.animation.HeartUseSmall;
import com.mojang.mojam.entity.mob.Team;
import com.mojang.mojam.gui.Font;
import com.mojang.mojam.screen.*;


public class ShopItem extends Building {

    private int facing = 0;

    public static final int SHOP_TURRET = 0;
    public static final int SHOP_HARVESTER = 1;
    public static final int SHOP_BOMB = 2;
    public static final int SHOP_HEART = 3;

    public static final int[] COST = {
            500, 300, 500, 150
    };

    private final int type;

    public ShopItem(double x, double y, int type, int team) {
        super(x, y, team);
        this.type = type;
        isImmortal = true;
        if (team == Team.Team1) {
            facing = 0;
        }
    }

    @Override
    public void render(Screen screen) {
        super.render(screen);
        Bitmap image = getSprite();
        Font.draw(screen, "" + COST[type], (int) (pos.x - image.w / 2) + 3, (int) (pos.y + 7));
    }

    public void init() {
    }

    public void tick() {
        super.tick();
    }

    public Bitmap getSprite() {
        switch (type) {
        case SHOP_TURRET:
            return Art.turret[facing][0];
        case SHOP_HARVESTER:
            return Art.harvester[facing][0];
        case SHOP_BOMB:
            return Art.bomb;
        case SHOP_HEART:
        	return Art.heartShop;
        }	
        return Art.turret[facing][0];
    }

    @Override
    public void use(Entity user) {
        Player player = (Player) user;
        if (player.carrying == null && player.getScore() >= COST[type]) {
            player.payCost(COST[type]);
            Building item = null;
            switch (type) {
            case SHOP_TURRET:
                item = new Turret(pos.x, pos.y, team);
                level.addEntity(item);
                player.pickup(item);
                break;
            case SHOP_HARVESTER:
                item = new Harvester(pos.x, pos.y, team);
                level.addEntity(item);
                player.pickup(item);
                break;
            case SHOP_BOMB:
                item = new Bomb(pos.x, pos.y);
                level.addEntity(item);
                player.pickup(item);
                break;
            case SHOP_HEART:
                //item = new Heart(pos.x, pos.y, team);
                  
          	if (user instanceof Player) {
                Player reciever = (Player) user;
               	level.addEntity(new HeartUseSmall(pos.x, pos.y));
                if(reciever.health < reciever.maxHealth){
                    reciever.health += Heart.healthValue;
                    if(reciever.health > reciever.maxHealth){
                    	reciever.health = reciever.maxHealth;
                    }
                }
                reciever.carrying = null;
               }
            break;
            }    
        }
    }
}
