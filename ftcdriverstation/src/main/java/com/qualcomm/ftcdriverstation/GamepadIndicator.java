package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class GamepadIndicator {
   protected ImageView activeView;
   protected ImageView baseView;
   protected final Context context;
   protected final int idActive;
   protected final int idBase;
   protected GamepadIndicator.State state;

   public GamepadIndicator(Activity var1, int var2, int var3) {
      this.state = GamepadIndicator.State.INVISIBLE;
      this.context = var1;
      this.idActive = var2;
      this.idBase = var3;
      this.initialize(var1);
   }

   protected void indicate() {
      Animation var1 = AnimationUtils.loadAnimation(this.context, 2130771993);
      this.activeView.setImageResource(2131165339);
      var1.setAnimationListener(new AnimationListener() {
         public void onAnimationEnd(Animation var1) {
            GamepadIndicator.this.activeView.setImageResource(2131165338);
         }

         public void onAnimationRepeat(Animation var1) {
            GamepadIndicator.this.activeView.setImageResource(2131165338);
         }

         public void onAnimationStart(Animation var1) {
         }
      });
      this.activeView.startAnimation(var1);
   }

   public void initialize(Activity var1) {
      this.activeView = (ImageView)var1.findViewById(this.idActive);
      this.baseView = (ImageView)var1.findViewById(this.idBase);
   }

   public void setState(final GamepadIndicator.State var1) {
      this.state = var1;
      AppUtil.getInstance().runOnUiThread(new Runnable() {
         public void run() {
            int var1x = null.$SwitchMap$com$qualcomm$ftcdriverstation$GamepadIndicator$State[var1.ordinal()];
            if (var1x != 1) {
               if (var1x != 2) {
                  if (var1x == 3) {
                     GamepadIndicator.this.indicate();
                  }
               } else {
                  GamepadIndicator.this.activeView.setVisibility(4);
                  GamepadIndicator.this.baseView.setVisibility(0);
               }
            } else {
               GamepadIndicator.this.activeView.setVisibility(4);
               GamepadIndicator.this.baseView.setVisibility(4);
            }

         }
      });
   }

   public static enum State {
      INDICATE,
      INVISIBLE,
      VISIBLE;

      static {
         GamepadIndicator.State var0 = new GamepadIndicator.State("INDICATE", 2);
         INDICATE = var0;
      }
   }
}
