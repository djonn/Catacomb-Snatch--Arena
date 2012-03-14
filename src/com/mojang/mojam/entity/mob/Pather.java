package com.mojang.mojam.entity.mob;

import java.awt.List;
import java.util.ArrayList;
import java.util.Set;

import com.mojang.mojam.entity.Bullet;
import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.level.AStar;
import com.mojang.mojam.level.AvoidableObject;
import com.mojang.mojam.level.Path;
import com.mojang.mojam.level.tile.*;
import com.mojang.mojam.math.Mth;
import com.mojang.mojam.math.Vec2;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.*;

/**
 * @author Morgan
 * 
 */
public class Pather extends Mob {
	public int facing;
	public int walkTime;
	public double stepTime;

	private AStar aStar;
	private Path path;

	private Player player;

	private int shootDelay = 0;
	private int shootRadius = Tile.HEIGHT * 3;
	private double shootRadiusSqr = shootRadius * shootRadius;
	private double speed = 1;
	private int pathTime;

	private double objectAvoidanceRadius = Tile.HEIGHT * 3;
	private double maxTurnRate = Math.PI/10;
	private double lastDirection = 0;
	
	private ArrayList<Vec2> ePosArray;
	private ArrayList<AvoidableObject> aObjectArray;
	
	private Vec2 dPosNew;
	private Vec2 dPos;
	//private int team=0;
	
	/**
	 * @param x
	 * @param y
	 */

	
	public Pather(double x, double y, int team) {
		super(x, y, team);
		setPos(x, y);
		setStartHealth(4);
		dir = TurnSynchronizer.synchedRandom.nextDouble() * Math.PI * 2;
		minimapColor = 0xffff0000;
		yOffs = 10;
		facing = TurnSynchronizer.synchedRandom.nextInt(4);

		deathPoints = 4;
		physicsSlide = true;

		dPosNew = new Vec2();
		dPos = new Vec2();
		ePosArray = new ArrayList<Vec2>();
		aObjectArray = new ArrayList<AvoidableObject>();
		this.team=team;
	}
	/**
	 * @return
	 */
	private Player getPlayer() {
		// get player
		Set<Entity> entities = level.getEntities(pos.x - shootRadius, pos.y
				- shootRadius, pos.x + shootRadius, pos.y + shootRadius);

		Entity closest = null;

		for (Entity e : entities) {
			if (!(e instanceof Player)) {
				continue;
			}
			if (((Player) e).isNotFriendOf(this))
				continue;
			closest = e;
		}
		return (Player) closest;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mojang.mojam.entity.mob.Mob#tick()
	 */
	public void tick() {
		
		ePosArray.clear();
		aObjectArray.clear();
		
		super.tick();

		if (freezeTime > 0)
			return;

		//tryToShoot();
		Vec2 moveTo = tryToPath();
		if (moveTo != null) {
			tryToMove(moveTo);
		}
		
		
		
	}

	/**
	 * 
	 */
	private void resetPath() {
		path = null;
		pathTime = TurnSynchronizer.synchedRandom.nextInt(10) + 1;
	}

	/**
	 * 
	 
	private void tryToShoot() {
		if (--shootDelay <= 0) {

			Set<Entity> entities = level.getEntities(pos.x - shootRadius, pos.y
					- shootRadius, pos.x + shootRadius, pos.y + shootRadius);

			Entity closest = null;
			double closestDist = 99999999.0f;
			for (Entity e : entities) {
				if (!(e instanceof Mob))
					continue;
				if (e == this)
					continue;
				//if ((e instanceof Pather))
				//	continue;
				if (!((Mob) e).isNotFriendOf(this))
					continue;
				double dist = e.pos.distSqr(pos);
				if (dist < shootRadiusSqr && dist < closestDist
						&& level.checkLineOfSight(this, e)) {
					closestDist = dist;
					closest = e;
				}
			}

			if (closest != null) {
				double invDist = 1.0 / Math.sqrt(closestDist);
				double yd = closest.pos.y - pos.y;
				double xd = closest.pos.x - pos.x;
				Bullet bullet = new Bullet(this, xd * invDist, yd * invDist);
				bullet.pos.y -= 10;
				level.addEntity(bullet);
				shootDelay = 10;
			}
		}
	}*/

	/**
	 * @return
	 */
	private Vec2 tryToPath() {
		if (--pathTime > 0)
			return null;

		if (player == null) {
			player = getPlayer();
			pathTime = 100;
			return null;
		}

		if (aStar == null)
			this.aStar = new AStar(level, this);

		if (aStar != null && path == null) {
			// if (pos.sub(player.pos).length() < 64) {
			// resetPath();
			// return null;
			// }
			// mob pos
			/*
			 * Tile tileFrom = level.getTile((int)(level.width / 2
			 * -10),(int)(level.height -13));//level.getTile(pos); Tile tileTo =
			 * level.getTile((int)(level.width / 2 +10),(int)(level.height
			 * -13)); //level.getTile(player.pos);
			 */
			Tile tileFrom = level.getTile(pos);
			//Tile tileTo = level.getTile(player.pos);
			
			
			
			Tile tileTo;
			do {
				tileTo = level.getTile(player.pos);
				if (!tileTo.canPass(this))
					tileTo = null;
			} while (tileTo == null);
			
			
			/*
			Tile tileTo;
			int tileX = 0;
			int tileY = 0;

			do {
				tileX = TurnSynchronizer.synchedRandom.nextInt(level.width);
				tileY = TurnSynchronizer.synchedRandom.nextInt(level.height);

				tileTo = level.getTile(tileX, tileY);
				if (!tileTo.canPass(this))
					tileTo = null;
			} while (tileTo == null);*/

			this.path = this.aStar.getPath(new Vec2(tileFrom.x, tileFrom.y),
					new Vec2(tileTo.x, tileTo.y));
		}

		if (path == null) {
			pathTime = 100;
			return null;
		}

		if (path.isDone()) {
			resetPath();
			return null;
		}

		
		if(	!level.checkLineOfSight(this,path.getWorldPos(path.getIndex())) ) {
			path.previous();
			return path.getWorldPos(path.getIndex());
		}
		
		if (  path.getWorldPos(path.getIndex()).dist(pos) < Tile.WIDTH) {
			path.next();
			return path.getWorldPos(path.getIndex());
		}
		
		if (  path.getWorldPos(path.getIndex()).dist(pos)  < (5 * Tile.WIDTH)) {
			if(	level.checkLineOfSight(this,path.getWorldPos(path.getIndex())) ) {
				Vec2 nextNodePos = path.getWorldPos(path.getIndex()+1);
				
				if (nextNodePos!= null && level.checkLineOfSight(this,nextNodePos)) {
					path.next();
					return path.getWorldPos(path.getIndex());
				}
			}
		} 
		
		
		return path.getWorldPos(path.getIndex());
	}

	
	
	
	private ArrayList<AvoidableObject> getAvoidableEntities() {
		ArrayList<AvoidableObject> r = new ArrayList<AvoidableObject>();

		Set<Entity> entities = level.getEntities(pos.x - objectAvoidanceRadius,
				pos.y - objectAvoidanceRadius, pos.x + objectAvoidanceRadius,
				pos.y + objectAvoidanceRadius);

		double eDanger;

		for (Entity e : entities) {
			eDanger = 1;

			if (!(e instanceof Mob))
				continue;
			if (e == this)
				continue;

			if (e.pos.sub(pos).length() > objectAvoidanceRadius
					&& level.checkLineOfSight(this, e)) {
				continue;
			}

			r.add(new AvoidableObject(e.pos, eDanger, e, ((Mob)e).radius));
		}
		return r;
	}

	private ArrayList<AvoidableObject> getAvoidableTiles() {
		ArrayList<AvoidableObject> r = new ArrayList<AvoidableObject>();

		Vec2 cTilePos = level.getTileFromPosition(pos);
		int x, y;
		Tile tile;
		double tDanger;

		for (x = -1; x <= 1; x++) {
			for (y = -1; y <= 1; y++) {
				tile = level.getTile((int) (x + cTilePos.x),
						(int) (y + cTilePos.y));
				if (tile.canPass(this))
					continue;
				tDanger = 0.5;

				r.add(
						new AvoidableObject(
								level.getPositionFromTile((int) (x + cTilePos.x), (int) (y + cTilePos.y)),
								tDanger,tile,new Vec2(Tile.WIDTH,Tile.HEIGHT)
								));
			}
		}

		return r;
	}

	/**
	 * @param goal
	 */
	private void tryToMove(Vec2 goal) {

		Vec2 dPos = goal.sub(pos);
		// System.out.println("dPos: "+dPos);
		Vec2 dPosNormal = dPos.normal();
		// System.out.println("dPosNormal: "+dPosNormal);
		double dPosRads = Math.atan2(dPosNormal.x, dPosNormal.y);
		// System.out.println("dPosRads: "+dPosRads);
		double dDistance = dPos.length();
		// System.out.println("dDistance: "+dDistance);
		double dPosRadsNew = dPosRads;
		Vec2 dPosNew;
		Vec2 moveBy;
		double tSpeed = speed;
						
		ArrayList<AvoidableObject> avoidableObjects;

		avoidableObjects = getAvoidableEntities();
		avoidableObjects.addAll(getAvoidableTiles());

		Vec2 ePos;
		// Vec2 ePosNormal;
		double ePosRads;
		double eDistance;
		double eDanger;


		for (AvoidableObject o : avoidableObjects) {

			ePos = o.getPos().sub(pos);
			eDanger = o.getDanger();

			
			eDistance = ePos.length();

			if (eDistance > objectAvoidanceRadius) {
				continue;
			}

			ePosRads = Math.atan2(ePos.x, ePos.y);
			ePosRads -= dPosRads;
			ePosRads += Math.PI;
			ePosRads = Mth.normalizeAngle(ePosRads, 0.0);

			eDanger *= (objectAvoidanceRadius - eDistance )
					/ objectAvoidanceRadius;
			this.ePosArray.add(ePos.normal().scale((32*(eDanger+1))));
			this.aObjectArray.add(o);
			dPosRadsNew += (eDanger * ePosRads);
		}

		double turnRate = Mth.normalizeAngle(dPosRadsNew, lastDirection)-lastDirection;
		
		if ( turnRate > maxTurnRate) {
			dPosRadsNew=lastDirection+maxTurnRate;
		} else if ( turnRate < -maxTurnRate) {
			dPosRadsNew=lastDirection-maxTurnRate;
		}
		lastDirection=dPosRadsNew;
		
		dPosNew = new Vec2(Math.sin(dPosRadsNew), Math.cos(dPosRadsNew));

		this.dPosNew = dPosNew.normal().scale(32);
		this.dPos = dPos.normal().scale(dDistance);


		moveBy = dPosNew.scale(tSpeed);

		if (!move(moveBy.x, moveBy.y)) {
			resetPath();
		} else {
			facing = (int) ((Math.atan2(-moveBy.x, moveBy.y) * 8
					/ (Math.PI * 2) - 8.5)) & 7;

			stepTime += speed / 4;
			if (stepTime > 6)
				stepTime = 0;
		}

	}

	/*	*//**
	 * @param goal
	 */
	/*
	 * private void tryToMove(Vec2 goal) {
	 * 
	 * Vec2 dPos = goal.sub(pos); // System.out.println("dPos: "+dPos); Vec2
	 * dPosNormal = dPos.normal(); //
	 * System.out.println("dPosNormal: "+dPosNormal); double dPosRads =
	 * Math.atan2(dPosNormal.x, dPosNormal.y); //
	 * System.out.println("dPosRads: "+dPosRads); double dDistance =
	 * dPos.length(); // System.out.println("dDistance: "+dDistance); double
	 * dPosRadsNew = dPosRads; Vec2 dPosNew; Vec2 moveBy; double tSpeed = speed;
	 * 
	 * 
	 * ////// // Get all of the Entities close by //////
	 * 
	 * Set<Entity> entities = level.getEntities(pos.x - objectAvoidanceRadius,
	 * pos.y - objectAvoidanceRadius, pos.x + objectAvoidanceRadius, pos.y +
	 * objectAvoidanceRadius);
	 * 
	 * objectAvoidanceRadius = Tile.HEIGHT * 3;
	 * 
	 * //freezeTime=10; //xBump=0; //yBump=0;
	 * 
	 * Vec2 ePos; //Vec2 ePosNormal; double ePosRads; double eDistance; double
	 * eDanger;
	 * 
	 * for (Entity e : entities) { eDanger = 0.1;
	 * 
	 * if (!(e instanceof Mob)) continue; if (e == this) continue; // if (e
	 * instanceof Player) // eDanger=0.5; // continue; //if (e instanceof
	 * Pather) // eDanger = 0.01; if (e instanceof Mob) { if (!((Mob)
	 * e).isNotFriendOf(this)) eDanger = 0.9; }
	 * 
	 * // System.out.println("e: "+e);
	 * 
	 * ePos = e.pos.sub(pos); System.out.println("ePos: "+ePos);
	 * this.ePos=ePos.normal().scale(32);
	 * 
	 * eDistance = ePos.length(); //
	 * System.out.println("eDistance: "+eDistance);
	 * 
	 * if (eDistance > objectAvoidanceRadius) { continue; }
	 * 
	 * //ePosNormal = ePos.normal(); ePosRads = Math.atan2(ePos.x, ePos.y);
	 * ePosRads -= dPosRads; ePosRads += Math.PI;
	 * ePosRads=Mth.normalizeAngle(ePosRads,0.0);
	 * 
	 * eDanger *= (objectAvoidanceRadius - eDistance) / objectAvoidanceRadius;
	 * 
	 * dPosRadsNew += (eDanger * ePosRads);
	 * 
	 * }
	 * 
	 * dPosNew = new Vec2(Math.sin(dPosRadsNew), Math.cos(dPosRadsNew));
	 * 
	 * this.dPosNew=dPosNew.normal().scale(32);
	 * this.dPos=dPos.normal().scale(dDistance);
	 * 
	 * if (dPos.length() < Tile.HEIGHT) { path.next(); return; }
	 * 
	 * moveBy = dPosNew.scale(tSpeed);
	 * 
	 * if (!move(moveBy.x, moveBy.y)) { resetPath(); } else { facing = (int)
	 * ((Math.atan2(-moveBy.x, moveBy.y) * 8 / (Math.PI * 2) - 8.5)) & 7;
	 * 
	 * stepTime += speed / 4; if (stepTime > 6) stepTime = 0; }
	 * 
	 * }
	 */
	/**
	 * @param goal
	 */
	/*
	 * private void tryToMove2(Vec2 goal) {
	 * 
	 * Vec2 dPos = goal.sub(pos); // System.out.println("dPos: "+dPos); Vec2
	 * dPosNormal = dPos.normal(); //
	 * System.out.println("dPosNormal: "+dPosNormal); double dPosRads =
	 * Math.atan2(dPosNormal.x, dPosNormal.y); //
	 * System.out.println("dPosRads: "+dPosRads); double dDistance =
	 * dPos.length(); // System.out.println("dDistance: "+dDistance); double
	 * dPosRadsNew = dPosRads; Vec2 dPosNew; Vec2 moveBy;
	 * 
	 * Set<Entity> entities = level.getEntities(pos.x - objectAvoidanceRadius,
	 * pos.y - objectAvoidanceRadius, pos.x + objectAvoidanceRadius, pos.y +
	 * objectAvoidanceRadius);
	 * 
	 * objectAvoidanceRadius = Tile.HEIGHT * 5;
	 * 
	 * Vec2 ePos; Vec2 ePosNormal; double ePosRads; double eDistance; double
	 * eDanger;
	 * 
	 * for (Entity e : entities) { eDanger = 0.1;
	 * 
	 * if (!(e instanceof Mob)) continue; if (e == this) continue; // if (e
	 * instanceof Player) // eDanger=0.5; // continue; // if (e instanceof
	 * Pather) // eDanger=0.2; if (e instanceof Mob) { if (!((Mob)
	 * e).isNotFriendOf(this)) eDanger = 0.5; }
	 * 
	 * // System.out.println("e: "+e);
	 * 
	 * ePos = e.pos.sub(pos); // System.out.println("ePos: "+ePos);
	 * 
	 * eDistance = ePos.length(); //
	 * System.out.println("eDistance: "+eDistance);
	 * 
	 * if (eDistance > objectAvoidanceRadius) { continue; }
	 * 
	 * ePosNormal = ePos.normal(); //
	 * System.out.println("ePosNormals: "+ePosNormal);
	 * 
	 * ePosRads = Math.atan2(ePosNormal.x, ePosNormal.y); //
	 * System.out.println("ePosRads: "+ePosRads);
	 * 
	 * // Rotate ePosRads so dPosRads is 0 ePosRads -= dPosRadsNew; //
	 * System.out.println("ePosRads: "+ePosRads);
	 * 
	 * if (ePosRads > Math.PI / 2) { continue; } else if (ePosRads < -(Math.PI /
	 * 2)) { continue; } // System.out.println("ePosRads: "+ePosRads); //
	 * System.out.println("eDistance: "+eDistance); eDanger *=
	 * (objectAvoidanceRadius - eDistance) / objectAvoidanceRadius;
	 * 
	 * dPosRadsNew -= (eDanger * Math.signum(ePosRads));
	 * 
	 * System.out .println("eDistance: " + eDistance + " DMOD: " +
	 * ((objectAvoidanceRadius - eDistance) / objectAvoidanceRadius) +
	 * " eDanger: " + eDanger + " ePosRads: " + ePosRads + " DeltaRads: " +
	 * (eDanger * ePosRads));
	 * 
	 * // System.out.println("dPosRadsNew: "+dPosRadsNew); if (dPosRadsNew >
	 * Math.PI) { dPosRadsNew -= Math.PI * 2; } else if (dPosRadsNew < -Math.PI)
	 * { dPosRadsNew += Math.PI * 2; } }
	 * 
	 * dPosNew = new Vec2(Math.sin(dPosRadsNew), Math.cos(dPosRadsNew)); //
	 * System.out.println("dPosRadsNew: "+dPosRadsNew); //
	 * System.out.println("SIN: "+Math.sin(dPosRadsNew)+ //
	 * " COS:"+Math.cos(dPosRadsNew)); //
	 * System.out.println("dPosRads: "+dPosRads+ //
	 * " dPosRadsNew: "+dPosRadsNew); System.out.println("DeltaRads: " +
	 * (dPosRads + dPosRadsNew)); if (dPos.length() < Tile.HEIGHT) {
	 * path.next(); return; }
	 * 
	 * moveBy = dPosNew.scale(speed);
	 * 
	 * if (!move(moveBy.x, moveBy.y)) { resetPath(); } else { facing = (int)
	 * ((Math.atan2(-moveBy.x, moveBy.y) * 8 / (Math.PI * 2) - 8.5)) & 7;
	 * 
	 * if (stepTime++ > 6) stepTime = 0; }
	 * 
	 * }
	 *//**
	 * @param goal
	 */
	/*
	 * private void tryToMove1(Vec2 goal) {
	 * 
	 * Vec2 dPos = goal.sub(pos); // System.out.println("dPos: "+dPos); Vec2
	 * dPosNormal = dPos.normal(); //
	 * System.out.println("dPosNormal: "+dPosNormal); double dPosRads =
	 * Math.atan2(dPosNormal.x, dPosNormal.y); //
	 * System.out.println("dPosRads: "+dPosRads); double dDistance =
	 * dPos.length(); // System.out.println("dDistance: "+dDistance); double
	 * dPosRadsNew = dPosRads; Vec2 dPosNew; Vec2 moveBy;
	 * 
	 * Set<Entity> entities = level.getEntities(pos.x - objectAvoidanceRadius,
	 * pos.y - objectAvoidanceRadius, pos.x + objectAvoidanceRadius, pos.y +
	 * objectAvoidanceRadius);
	 * 
	 * Vec2 ePos; Vec2 ePosNormal; double ePosRads; double eDistance; double
	 * eDanger;
	 * 
	 * for (Entity e : entities) { eDanger = 0.1;
	 * 
	 * if (!(e instanceof Mob)) continue; if (e == this) continue; if (e
	 * instanceof Player) eDanger = 0.5; // continue; if (e instanceof Pather)
	 * eDanger = 0.2; if (e instanceof Mob) { if (!((Mob)
	 * e).isNotFriendOf(this)) eDanger = 0.5; }
	 * 
	 * // System.out.println("e: "+e);
	 * 
	 * ePos = e.pos.sub(pos); // System.out.println("ePos: "+ePos);
	 * 
	 * eDistance = ePos.length(); //
	 * System.out.println("eDistance: "+eDistance);
	 * 
	 * if (eDistance > objectAvoidanceRadius) { continue; }
	 * 
	 * ePosNormal = ePos.normal(); //
	 * System.out.println("ePosNormals: "+ePosNormal);
	 * 
	 * ePosRads = Math.atan2(ePosNormal.x, ePosNormal.y); //
	 * System.out.println("ePosRads: "+ePosRads);
	 * 
	 * // Rotate ePosRads so dPosRads is 0 ePosRads -= dPosRads; //
	 * System.out.println("ePosRads: "+ePosRads);
	 * 
	 * if (ePosRads > Math.PI) { ePosRads -= Math.PI * 2; } else if (ePosRads <
	 * -Math.PI) { ePosRads += Math.PI * 2; } //
	 * System.out.println("ePosRads: "+ePosRads); //
	 * System.out.println("eDistance: "+eDistance); eDanger *=
	 * (objectAvoidanceRadius / eDistance);
	 * 
	 * dPosRadsNew -= (eDanger * ePosRads);
	 * 
	 * System.out.println("eDistance: " + eDistance + " eDanger: " + eDanger +
	 * " ePosRads: " + ePosRads + " DeltaRads: " + (eDanger * ePosRads));
	 * 
	 * // System.out.println("dPosRadsNew: "+dPosRadsNew); if (dPosRadsNew >
	 * Math.PI) { dPosRadsNew -= Math.PI * 2; } else if (dPosRadsNew < -Math.PI)
	 * { dPosRadsNew += Math.PI * 2; } }
	 * 
	 * dPosNew = new Vec2(Math.sin(dPosRadsNew), Math.cos(dPosRadsNew)); //
	 * System.out.println("dPosRadsNew: "+dPosRadsNew); //
	 * System.out.println("SIN: "+Math.sin(dPosRadsNew)+ //
	 * " COS:"+Math.cos(dPosRadsNew)); //
	 * System.out.println("dPosRads: "+dPosRads+ //
	 * " dPosRadsNew: "+dPosRadsNew); System.out.println("DeltaRads: " +
	 * (dPosRads + dPosRadsNew)); if (dPos.length() < Tile.HEIGHT) {
	 * path.next(); return; }
	 * 
	 * moveBy = dPosNew.scale(speed);
	 * 
	 * if (!move(moveBy.x, moveBy.y)) { resetPath(); } else { facing = (int)
	 * ((Math.atan2(-moveBy.x, moveBy.y) * 8 / (Math.PI * 2) - 8.5)) & 7;
	 * 
	 * if (stepTime++ > 6) stepTime = 0; }
	 * 
	 * }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mojang.mojam.entity.mob.Mob#getDeatchSound()
	 */
	@Override
	public String getDeatchSound() {
		return "/sound/Enemy Death 2.wav";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mojang.mojam.entity.mob.Mob#die()
	 */
	public void die() {
		super.die();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mojang.mojam.entity.mob.Mob#getSprite()
	 */
	public Bitmap getSprite() {
		
		if (team == Team.Team1 ) {
			return Art.lordLard[(int) stepTime % 6][facing];
		} else { // if (team == Team.Team2 ) {
			return Art.herrSpeck[(int) stepTime % 6][facing];	
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mojang.mojam.entity.mob.Mob#collide(com.mojang.mojam.entity.Entity,
	 * double, double)
	 */
	@Override
	public void collide(Entity entity, double xa, double ya) {
		super.collide(entity, xa, ya);

		if (entity instanceof Mob) {
			Mob mob = (Mob) entity;
			if (isNotFriendOf(mob)) {
				mob.hurt(this, 2);
			}
		}
		freezeTime = TurnSynchronizer.synchedRandom.nextInt(5) + 5;
		xBump = xa*0.5;
		yBump = ya*0.5;

		if (TurnSynchronizer.synchedRandom.nextInt(10) > 5)
			resetPath();

	}

	public void render(Screen screen) {
		super.render(screen);
		
		for (AvoidableObject aO : aObjectArray) {
			//screen.line((int) pos.x, (int) pos.y, (int) (ePos.x + pos.x),
			//		(int) (ePos.y + pos.y), 0x80FF0000);

			//screen.line((int) pos.x, (int) pos.y, (int) (aO.getPos().x),
			//		(int) (aO.getPos().y), 0x80FF0000);
		}
		//screen.line((int) pos.x, (int) pos.y, (int) (dPosNew.x + pos.x),
		//		(int) (dPosNew.y + pos.y), 0x8000FF00);
		//screen.line((int) pos.x, (int) pos.y, (int) (dPos.x + pos.x),
		//		(int) (dPos.y + pos.y), 0x800000FF);
	}
}
