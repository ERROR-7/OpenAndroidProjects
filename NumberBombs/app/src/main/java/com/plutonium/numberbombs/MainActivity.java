package com.plutonium.numberbombs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{

    //游戏变量
    public static int min = 1;
    public static int max;
    public static int bomb;
    public static int round;
    boolean buttontype;
    int input;

    //存储相关
    SharedPreferences sharedPreferences;

    //动画变量
    public int min_temp;
    public int max_temp;
    public boolean minflag,maxflag;

    //预定义控件
    public EditText editText;
    public Button button;
    public TextView bombtextView;
    public TextView minnumtextview;
    public TextView maxnumtextview;

    //功能
    //自动填充
    public boolean auto_fill;
    //记录
    public boolean record;

    //音效相关
    public static float vol;
    static SoundPool soundPool;
    private static final Map <Integer, Integer> soundmap = new HashMap<>();


    //Activity创建时事件
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //实例化存储
        sharedPreferences = this.getSharedPreferences("DATA",MODE_PRIVATE);
        SharedPreferences.Editor speditor = sharedPreferences.edit();
        //读取功能
        max = sharedPreferences.getInt("MAX",100);
        vol = sharedPreferences.getFloat("VOL",1);
        auto_fill = sharedPreferences.getBoolean("AUTO_FILL",false);
        record = sharedPreferences.getBoolean("RECORD",false);
        //覆写设置
        speditor.putString("A_WARNING_ZH","擅自修改数值将导致游戏崩溃.");
        speditor.putString("A_WARNING_EN","Unauthorized modification may cause app to crash.");
        speditor.putInt("MAX",max);
        speditor.putFloat("VOL",vol);
        speditor.putBoolean("AUTO_FILL",auto_fill);
        speditor.putBoolean("RECORD",record);
        speditor.apply();

        //生成随机数
        Random rand = new Random();
        //NOTE:此方法返回随机数 n ∈ [0,bound]
        bomb = rand.nextInt(max-min -2) + min + 1;

        //实例化
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.confirm_button);
        bombtextView = findViewById(R.id.bomb_textview);
        minnumtextview = findViewById(R.id.minum_textview);
        maxnumtextview = findViewById(R.id.maxnum_textview);

        //标题栏
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setSubtitle(R.string.actionbar_welcome);  //actionbar副标题

        //放个音效我容易吗我
        //新建一个音频池
        SoundPool.Builder builder = new SoundPool.Builder();
        //传入音频数量
        builder.setMaxStreams(20);
        //AudioAttributes是一个封装音频各种属性的方法
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        //设置音频流的合适的属性
        attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        //加载一个AudioAttributes
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();
        soundmap.put(1, soundPool.load(this, R.raw.cesium, 1));
        soundmap.put(2, soundPool.load(this, R.raw.sound_boom, 1));
        soundmap.put(3, soundPool.load(this, R.raw.counter, 1));

        //载入数值
        min_temp = min;
        max_temp = max;
        minnumtextview.setText(String.valueOf(min_temp));
        maxnumtextview.setText(String.valueOf(max_temp));

    }

    //返回键监听
    @Override
    public void onBackPressed()
    {
        //NOTE:创建对话框
        new AlertDialog
                .Builder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_content)
                .setIcon(R.drawable.exit_dark)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // 点击“确认”后的操作
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                })
                .show();
    }

    //重写溢出菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //NOTE:下面这种智障写法又长又臭又没用
        /*
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_main,menu);
        */
        return super.onCreateOptionsMenu(menu);
    }

    //重写菜单<准备>事件
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        //<取得>音效开关
        if(vol != 0)
        {
            menu.findItem(R.id.sound_selector).setChecked(true);

        }
        else
        {
            menu.findItem(R.id.sound_selector).setChecked(false);
        }

        //<取得>记录开关
        if(auto_fill)
        {
            menu.findItem(R.id.auto_fill).setChecked(true);
        }
        else
        {
            menu.findItem(R.id.auto_fill).setChecked(false);
        }

        //<取得>自动填充开关
        if(record)
        {
            menu.findItem(R.id.record).setChecked(true);
        }
        else
        {
            menu.findItem(R.id.record).setChecked(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    //重写菜单<选择>事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        switch (item.getItemId())
        {
            //重置
            case R.id.menu_restart:
                RestartConfirm();
                break;
            //<修改>音效开关
            case R.id.sound_selector:
                if (item.isChecked())
                {
                    item.setChecked(false);
                    vol = 0;
                }
                else
                {
                    item.setChecked(true);
                    vol = 1;
                }
                sharedPreferences.edit().putFloat("VOL",vol).apply();
                break;

            //<修改>最大数
            case R.id.set_max:
                setting_max();
                break;

            //<修改>自动填充开关
            case R.id.auto_fill:
                if(item.isChecked())
                {
                    item.setChecked(false);
                    auto_fill = false;
                    sharedPreferences.edit().putBoolean("AUTO_FILL",false).apply();
                }
                else
                {
                    item.setChecked(true);
                    auto_fill = true;
                    sharedPreferences.edit().putBoolean("AUTO_FILL",true).apply();
                }
                break;

            //<修改>记录开关
            case R.id.record:
                if(item.isChecked())
                {
                    item.setChecked(false);
                    record = false;
                    sharedPreferences.edit().putBoolean("RECORD",false).apply();
                }
                else
                {
                    item.setChecked(true);
                    record = true;
                    sharedPreferences.edit().putBoolean("RECORD",true).apply();
                }
                break;

            //转至“关于”页
            case R.id.menu_about:
                Intent intent_to_about = new Intent(MainActivity.this,AboutActivity.class);
                startActivity(intent_to_about);
                break;

            //退出
            case R.id.menu_exit:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //非常强大的确认按钮
    public void Confirm_Click(View view)
    {
        if (!buttontype) //按钮类别为false时为确认按钮
        {
            //取得文本框内容
            String inputString = editText.getText().toString();
            if (inputString.equals(""))//防空框
            {
                Toast.makeText(MainActivity.this, R.string.error_content_empty, Toast.LENGTH_LONG).show();
            }
            else
            {
                //NOTE:string转int,放在这里是因为string为空时转int得0.
                input = Integer.parseInt(inputString);
                if (input == bomb) //游戏结束
                {
                    //憋闪辣
                    flashing_handler.removeCallbacksAndMessages(null);
                    //更换按钮类型
                    buttontype = true;
                    //修改按钮文字
                    button.setText(R.string.button_1);
                    //展示炸弹
                    String boomnumstring = Integer.toString(bomb);
                    bombtextView.setText(boomnumstring);
                    bombtextView.setTextColor(0xFFD50000);//要用8位颜色码
                    //锁定文本框
                    editText.setHint(R.string.hints_1);
                    editText.setEnabled(false);
                    //发出提示
                    Toast.makeText(MainActivity.this, R.string.toast_gameover, Toast.LENGTH_LONG).show();
                    //BOOM!!!
                    soundPool.play(soundmap.get(2), vol, vol, 1, 0, 1);
                }
                else
                {
                    if (input <= min)//判断是否小于范围
                    {
                        Toast.makeText(MainActivity.this, R.string.error_content_leq, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        if (input >= max)//判断是否大于范围
                        {
                            Toast.makeText(MainActivity.this, R.string.error_content_geq, Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            if (input < bomb)//小于炸弹,放左边
                            {
                                min = input;    //修改当前下限
                                minflag = false;
                                //minnumtextview.setText(inputString);
                            }
                            else//大于炸弹,放右边
                            {
                                max = input;    //修改当前上限
                                maxflag = false;
                                //maxnumtextview.setText(inputString);
                            }
                            main_controller.post(main_ctrl);

                            //如果剩余可能小于15炸弹就变成黄色并且闪烁
                            if (max - min <= 16)
                            {
                                //刷新线程
                                flashing_handler.removeCallbacksAndMessages(null);
                                flashing_handler.post(flashing);

                                bombtextView.setTextColor(0xFFFFAB00);  //黄色
                                soundPool.play(soundmap.get(1),vol,vol, 1, 0, 1);
                            }
                            //强行给副标题搞个用途
                            round += 1;
                            String roundstr
                                    = getResources().getString(R.string.rounds_1)
                                    + round
                                    + getResources().getString(R.string.rounds_2);
                            Objects.requireNonNull(getSupportActionBar()).setSubtitle(roundstr);



                        }
                    }
                }
            }
            //清空文本框以便再次输入
            editText.setText(null);

            //自动填充功能
            //须在清空后再重新填入避免白给
            if(auto_fill)
            {
                //避免和"重置"冲突
                if(!buttontype)
                {
                    if(max-min==2)
                    {
                        editText.setText(String.valueOf(bomb));
                        button.setText(R.string.button_233);
                    }
                }
            }

        }
        else //按钮类别为true时为重置按钮
        {
            Restart();
        }
    }

    //重置功能块
    public void Restart()
    {

        //更新变量
        min = 1;
        max = sharedPreferences.getInt("MAX",100);
        round = 0;
        Random rand = new Random();
        bomb = rand.nextInt(max-min+1) + min;

        //传递到线程实现动画
        minflag = maxflag = false;
        main_controller.post(main_ctrl);

        //更新界面
        String roundstr
                = getResources().getString(R.string.rounds_1)
                + round
                + getResources().getString(R.string.rounds_2);
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(roundstr);
        buttontype=false;
        editText.setEnabled(true);
        editText.setHint(R.string.hints_0);
        button.setText(R.string.button_0);
        bombtextView.setText(R.string.default_bomb);
        bombtextView.setTextColor(0xFF00C853);

    }

    //对话框
    //重置确认对话框功能块
    public void RestartConfirm()
    {
        AlertDialog alertDialog = new AlertDialog
                .Builder(this)
                .setTitle(R.string.dialog_restart_title)
                .setMessage(R.string.dialog_restart_content)
                .setIcon(R.drawable.restart_dark)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //确认
                        Restart();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    //选择项目变量

    //修改最大数对话框功能块
    int item ;
    public void setting_max()
    {
        item = sharedPreferences.getInt("ITEM",1);
        //定义菜单文本串常量组
        final String[] items =
                {
                        getResources().getString(R.string.m_50),
                        getResources().getString(R.string.m_100),
                        getResources().getString(R.string.m_200),
                        getResources().getString(R.string.m_500),
                        getResources().getString(R.string.m_1000),
                };
        Toast.makeText(MainActivity.this,R.string.toast_setting_content,Toast.LENGTH_LONG).show();

        AlertDialog alertDialog = new AlertDialog
                .Builder(this)
                .setTitle(R.string.set_max)
                //NOTE:setMessage和setSigleChoiceItems冲突,只能用一个.
                //.setMessage(R.string.dialog_setting_content)
                .setIcon(R.drawable.settings_dark)
                //NOTE:checkedItem从0开始数.
                .setSingleChoiceItems(items,item, new DialogInterface.OnClickListener()
                {
                    //点击条目时事件
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case 0:
                                item = 0;
                                break;
                            case 1:
                                item = 1;
                                break;
                            case 2:
                                item = 2;
                                break;
                            case 3:
                                item = 3;
                                break;
                            case 4:
                                item = 4;
                                break;
                            default:
                                Toast.makeText(MainActivity.this,"错误！不要用修改器欺负我QwQ.",Toast.LENGTH_LONG).show();
                                System.exit(-1);

                        }
                    }
                })
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (item)
                        {
                            case 0:
                                max = 50;
                                break;
                            case 1:
                                max = 100;
                                break;
                            case 2:
                                max = 200;
                                break;
                            case 3:
                                max = 500;
                                break;
                            case 4:
                                max = 1000;
                                break;
                        }
                        sharedPreferences.edit().putInt("MAX",max).apply();
                        sharedPreferences.edit().putInt("ITEM",item).apply();
                        Restart();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        item = sharedPreferences.getInt("ITEM",1);
                    }
                })
                .show();




    }

    //动画
    //.创建Handler
    //..闪烁Handler
    private Handler flashing_handler = new Handler();
    //..主控Handler
    private Handler main_controller = new Handler();

    //.闪烁Runnable
    private Runnable flashing = new Runnable()
    {
        boolean flash = false;
        @Override
        public void run()
        {
            if (!flash)
            {
                flash = true;
                bombtextView.setTextColor(0x00FFAB00);  //透明的黄色(?迷惑
            }
            else
            {
                flash = false;
                bombtextView.setTextColor(0xFFFFAB00);
            }
            flashing_handler.postDelayed(this,30*(max-min)+45);
        }
    };

    //.动画主控
    private Runnable main_ctrl = new Runnable()
    {
        @Override
        public void run()
        {
            if (!minflag | !maxflag)
            {
                //锁定按钮
                button.setEnabled(false);
                //音效
                soundPool.play(soundmap.get(3),vol,vol,1,0,1);
                //传到相应功能块实现动画
                min_effect();
                max_effect();
                main_controller.postDelayed(this,25);
            }
            else
            {
                //解锁按钮
                button.setEnabled(true);
                //关闭动画线程
                main_controller.removeCallbacksAndMessages(null);
            }

        }
    };

    //.最小数动画功能块
    public void min_effect()
    {
        if(min_temp==min)
        {
            minflag=true;
        }
        else
        {
            if(min_temp<min)
            {
                min_temp+=1;
            }
            else
            {
                min_temp-=1;
            }
            minnumtextview.setText(String.valueOf(min_temp));
        }

    }
    //.最大数动画功能块
    public void max_effect()
    {
        if(max_temp==max)
        {
            maxflag=true;
        }
        else
        {
            if(max_temp<max)
            {
                max_temp+=1;
            }
            else
            {
                max_temp-=1;
            }
            maxnumtextview.setText(String.valueOf(max_temp));
        }
    }

}