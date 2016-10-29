import java.awt.event.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class AI extends Controller
{
    int movement;
    static final int still = 0;
    static final int left = 1;
    static final int up = 2;
    static final int right = 3;
    static final int down = 4;
    Random r;
    int moveCD;
    int initCD;
    //int xCount;
    //boolean goingRight;
    //int yCount;
    //boolean goingUp;
    int CD;
    
    
    /*TODO
     * 1. major movement
     * 2. shooting and detection
     * 3. minor movement
     */
    public AI(Unit u)
    {
        super(u);
        r = new Random();
        moveCD = 50;
        initCD = (r.nextInt(10)+1)*100;
        CD = 9;
    }
    
    public void majorMovement()
    {
        /*TODO
         * 1. change 1 d movement to 2 d
         * 2. with one can make the random direction thing a random angle chooser
         * 3. bit tricky will have to soften the movement so theres no jerking.
         * 4. can have jerking just make it rare
         * 5. its own cd? soft movemetn is on 1/4 cd of hard movement about? 
         * 6. want the ai to get places - pathfinder? select a random point or predetermined spot and pathfind to it?
         * 7. want the ai to aim at things - get access to position of enemies - to some algorithm stuff to choose a target then blast away
         * 8. want the ai to cooperate - get access to ally information and coordinate - have an emotion var that other ai can read- lots of an emotion leads to a certain 
         *      battle arrangement
         * 9. want the ai to have self-preservation (only if healing bullets) if low on health pathfind to allies
         * 10.if injured ally nearby heal them up
         * 11.suppressing fire - works with enhanced aiming intelligence
         * 
         */
         int direction = r.nextInt(4) + 1;
         if(moveCD >= 50)
         {
             u.stop();
             switch(direction)
             {
             case 1:
                 u.move("left");
                 break;
             case 2:
                 u.move("up");
                 break;
             case 3:
                 u.move("right");
                 break;
             case 4:
                 u.move("down");
                 break;
             }
             moveCD = 0;
         }
         else
         {
             moveCD++;
         }
         if(initCD == 0)
         {
             u.firing = true;
         }
         else
         {
             initCD--;
         }
         
    }

    public void detectEnemies()
    {
        if(CD == 9)
        {
            r = new Random();
            u.x2 = u.x - (r.nextInt(30) - 15);
            u.y2 = u.y - (r.nextInt(30) - 15);
            CD = 0;
        }
        else
        {
            CD++;
        }
    }
    
}
