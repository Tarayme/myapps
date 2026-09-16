package com.privateroom.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    final int BG = Color.rgb(9,9,11), CARD = Color.rgb(24,24,29), MUTED = Color.rgb(150,150,160);
    final int GREEN = Color.rgb(50,210,120), RED = Color.rgb(255,67,85);
    LinearLayout root;
    Handler handler = new Handler(Looper.getMainLooper());
    int callSeconds = 0;
    TextView timerView, spendView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        showExplore();
    }

    TextView t(String s, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(color);
        v.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return v;
    }

    GradientDrawable box(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        return g;
    }

    int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    void base(String title) {
        ScrollView sc = new ScrollView(this);
        sc.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(16),dp(16),dp(28));
        sc.addView(root);
        root.addView(t(title, 28, Color.WHITE, true));
        setContentView(sc);
    }

    void gap(int h){ Space s=new Space(this); root.addView(s,new LinearLayout.LayoutParams(1,dp(h))); }

    Button btn(String label, int color) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(color==GREEN?Color.BLACK:Color.WHITE); b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setAllCaps(false); b.setBackground(box(color,18));
        b.setPadding(dp(12),dp(14),dp(12),dp(14));
        return b;
    }

    void showExplore() {
        base("Live now"); gap(14);
        String[] names={"Mia","Lina","Sofia","Nora"};
        int[] rates={12,18,15,22};
        for(int i=0;i<names.length;i++){
            final int idx=i;
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(14),dp(14),dp(14),dp(14)); card.setBackground(box(CARD,20));
            TextView live=t("● LIVE",14,Color.rgb(255,70,115),true); card.addView(live);
            Space pv=new Space(this); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(155)); pp.topMargin=dp(10); pv.setBackground(box(Color.rgb(40,40,47),16)); card.addView(pv,pp);
            TextView n=t(names[i],21,Color.WHITE,true); LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2); np.topMargin=dp(10); card.addView(n,np);
            card.addView(t(rates[i]+" credits/min",15,MUTED,false));
            TextView online=t("● Online",14,GREEN,true); card.addView(online);
            card.setOnClickListener(v->showCreator(names[idx], rates[idx]));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.bottomMargin=dp(12); root.addView(card,cp);
        }
        Button dash=btn("Creator dashboard", Color.rgb(42,42,50));
        dash.setOnClickListener(v->showDashboard()); root.addView(dash);
    }

    void showCreator(String name,int rate) {
        base("‹  "+name); gap(12);
        FrameLayout preview=new FrameLayout(this); preview.setBackground(box(Color.rgb(31,31,38),22));
        TextView label=t("PUBLIC ROOM",15,MUTED,true); label.setGravity(Gravity.CENTER); preview.addView(label,new FrameLayout.LayoutParams(-1,-1));
        root.addView(preview,new LinearLayout.LayoutParams(-1,dp(420)));
        gap(14); root.addView(t(name,27,Color.WHITE,true)); root.addView(t(rate+" credits/min",16,MUTED,false)); gap(14);
        Button privateBtn=btn("☎  Invite to private room", GREEN);
        privateBtn.setOnClickListener(v->showWaiting(name, rate)); root.addView(privateBtn);
        gap(10); TextView note=t("Private call is billed by elapsed time. Gifts can be sent during the call.",13,Color.rgb(115,115,125),false); root.addView(note);
        gap(18); Button back=btn("Back",Color.rgb(42,42,50)); back.setOnClickListener(v->showExplore()); root.addView(back);
    }

    void showWaiting(String name,int rate) {
        base("Private room request"); gap(60);
        TextView ring=t("◉",90,GREEN,true); ring.setGravity(Gravity.CENTER); root.addView(ring);
        TextView msg=t("Waiting for "+name+"…",24,Color.WHITE,true); msg.setGravity(Gravity.CENTER); root.addView(msg);
        TextView sub=t(rate+" credits/min",16,MUTED,false); sub.setGravity(Gravity.CENTER); root.addView(sub);
        gap(26);
        Button accept=btn("Demo: creator accepts",GREEN); accept.setOnClickListener(v->showCall(name,rate)); root.addView(accept);
        gap(10); Button cancel=btn("Cancel",Color.rgb(42,42,50)); cancel.setOnClickListener(v->showCreator(name,rate)); root.addView(cancel);
    }

    void showCall(String name,int rate) {
        callSeconds=0;
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setBackgroundColor(Color.BLACK);
        FrameLayout video=new FrameLayout(this); video.setBackgroundColor(Color.rgb(22,22,27));
        TextView who=t(name+"\nPRIVATE ROOM",24,Color.WHITE,true); who.setGravity(Gravity.CENTER); video.addView(who,new FrameLayout.LayoutParams(-1,-1));
        page.addView(video,new LinearLayout.LayoutParams(-1,0,1));

        timerView=t("0:00",22,Color.WHITE,true); timerView.setGravity(Gravity.CENTER); spendView=t("0.0 credits",14,MUTED,false); spendView.setGravity(Gravity.CENTER);
        page.addView(timerView); page.addView(spendView);

        LinearLayout controls=new LinearLayout(this); controls.setPadding(dp(14),dp(14),dp(14),dp(22)); controls.setGravity(Gravity.CENTER); controls.setOrientation(LinearLayout.HORIZONTAL);
        Button gift=btn("🎁 Gift",Color.rgb(45,45,54)); Button end=btn("End",RED);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1); bp.setMargins(dp(5),0,dp(5),0); controls.addView(gift,bp); controls.addView(end,bp); page.addView(controls);
        setContentView(page);

        gift.setOnClickListener(v->showGiftDialog()); end.setOnClickListener(v->{handler.removeCallbacksAndMessages(null); showSummary(name,rate);});
        handler.post(new Runnable(){ public void run(){
            callSeconds++; int m=callSeconds/60,s=callSeconds%60;
            timerView.setText(m+":"+(s<10?"0":"")+s);
            spendView.setText(String.format(Locale.US,"%.2f credits",rate*(callSeconds/60.0)));
            handler.postDelayed(this,1000);
        }});
    }

    void showGiftDialog(){
        final String[] gifts={"🌹 Rose — 5 credits","❤️ Heart — 15 credits","👑 Crown — 50 credits"};
        new AlertDialog.Builder(this).setTitle("Send gift").setItems(gifts,(d,w)->Toast.makeText(this,"Gift sent",Toast.LENGTH_SHORT).show()).setNegativeButton("Close",null).show();
    }

    void showSummary(String name,int rate){
        base("Call summary"); gap(20);
        int m=callSeconds/60,s=callSeconds%60; double amount=rate*(callSeconds/60.0);
        root.addView(t(name,24,Color.WHITE,true)); gap(16);
        root.addView(t("Duration   "+m+":"+(s<10?"0":"")+s,18,Color.WHITE,false));
        root.addView(t(String.format(Locale.US,"Call charge   %.2f credits",amount),18,Color.WHITE,false));
        root.addView(t("Gifts   demo",18,MUTED,false)); gap(22);
        Button done=btn("Done",GREEN); done.setOnClickListener(v->showExplore()); root.addView(done);
    }

    void showDashboard(){
        base("Creator earnings"); gap(10);
        root.addView(t("1,248.5 credits",38,Color.WHITE,true)); root.addView(t("Private calls + gifts",15,MUTED,false)); gap(22);
        String[] rows={"Private call  +84.0","Gift · Crown  +40.0","Private call  +62.5","Gift · Heart  +12.0"};
        for(String r:rows){ LinearLayout row=new LinearLayout(this); row.setPadding(dp(14),dp(16),dp(14),dp(16)); row.setBackground(box(CARD,16)); TextView x=t(r,17,Color.WHITE,true); row.addView(x); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.bottomMargin=dp(10); root.addView(row,rp); }
        Button back=btn("Back to live",Color.rgb(42,42,50)); back.setOnClickListener(v->showExplore()); root.addView(back);
    }

    @Override public void onBackPressed(){ showExplore(); }
}
