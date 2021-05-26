package com.awesome.towerdefense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    private Context context;
    private Context applicationContext = null;
    private ConstraintLayout mainLayout;
    private GameView gameView;
    private int screenWidth, screenHeight;
    private AudioManager audioManager;
    private Audio audio;
    private int currentApiVersion;
    private BroadcastReceiver receiver;
    private boolean isOnline;
    private Canvas c;
    private Bitmap appTextures, gameTextures;
    private String gameState;
    private Rect rectOrigin, rectDestiny;
    private String action = "";
    private int touchX, touchY;
    private boolean fieldMoved = false;
    private int touchInitY;
    private int menuButtonHalfSize;
    private final char PLAY_BUTTON = 32, LEVEL_BUTTON = 33, CLOSE_BUTTON = 34, LEVEL_BLOCKED_BUTTON = 112, LEVEL_COMING_SOON = 120;
    private final char DIALOG_TL = 100, DIALOG_TR = 101, DIALOG_BL = 102, DIALOG_BR = 103;
    private final char DIALOG_BG = 105, DIALOG_L = 106, DIALOG_R = 116, DIALOG_T = 107, DIALOG_B = 117, DIALOG_BUTTON = 108, MENU_BG = 118, LEVEL_BG = 119;
    private int dialogBlock, dialogHalfBlock, dialogButtonHalfWidth, dialogButtonHalfHeight;
    private int messageTextSize, buttonTextSize;
    private boolean exitAppButtonPressed = false;
    private long frameCounter = 0, lastFpsTime;
    private int fps;
    private int vertLevels, levelButtonArea, levelButtonHalfArea, numberOfAvailableLevels;
    private boolean levelsScreenMoved = false;
    private int lsy; // levels screen displacement y
    private int levelsMarginY, extraLevelsScreen, levelButtonPressed;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private String deviceFilesFolder;
    private String installedMod;
    private String remoteServer;
    private int updateStatus;
    private boolean heightCorrected = false;
    private String TAG = "TDTag: ";
    private Typeface mainFont;
    private boolean audioInitialized = false;
    private int audioClick, audioTowerBuilt, audioTowerUpgrade, audioSelect, audioFieldSelect, audioWon, audioLost;
    private int audioEnemy0Attack, audioEnemy1Attack, audioEnemy2Attack, audioEnemy3Attack, audioEnemy4Attack, audioEnemy5Attack, audioEnemy6Attack, audioEnemy7Attack, audioEnemy8Attack, audioEnemy9Attack;
    private int audioEnemy0Death, audioEnemy1Death, audioEnemy2Death, audioEnemy3Death, audioEnemy4Death, audioEnemy5Death, audioEnemy6Death, audioEnemy7Death, audioEnemy8Death, audioEnemy9Death;
    private int audioTower1Attack, audioTower2Attack, audioTower3Attack, audioTower4Attack;
    private int audioTower1Death, audioTower2Death, audioTower3Death, audioTower4Death;

    // game
    private int level = 0, waves, currWave, waveStartPoint;
    private int spriteNumber;
    private final char GAME_CLOSE_BUTTON = 35;
    private String info;
    private long now;
    private boolean alive;
    private int dialogWidth, dialogHeight;
    private int dialogYesButtonX, dialogNoButtonX, dialogButtonY;
    private boolean gameClosePressed, won;
    private long waveStartTime, waveStartDelay, winTime, winDelay;
    private int highestLevel;
    private boolean[] levelAvailable;
    private int[] achievedRanks;
    private String[] levelTitle, levelDescription;
    private int currentRank;
    private boolean levelStartFieldScroll = false;
    private long levelStartTime, levelStartFieldScrollDelay = 1000;

    // field
    private int extraScreen; // how much beyond screenHeight will be used for the field
    private int fdy; // field displacement y
    private final int OUT_OF_BOUNDS = 100000;
    private int marginY;
    private int minVertTower, maxVertTower;
    private final char EMPTY1 = 46, EMPTY2 = 44, EMPTY3 = 58, EMPTY4 = 59, BASE = 43, BLOCKED1 = 47, BLOCKED2 = 45, BLOCKED3 = 60, BLOCKED4 = 62;
    private ArrayList<Integer> blockedColumns;
    private String fieldFromFile;

    // coins
    private final char COIN = 38;
    private int levelCoins, coinSize, coinHalfSize;

    // towers
    private final char TOWER1 = 39, TOWER2 = 40, TOWER3 = 41, TOWER4 = 42;
    private final char TOWER1_D = 124, TOWER2_D = 125, TOWER3_D = 126, TOWER4_D = 127;
    private final char TL1 = 80, TL2 = 81, TL3 = 82, TL4 = 83, TL5 = 84, TL6A1 = 85, TL6A2 = 86, TL6B1 = 87, TL6B2 = 88;
    private final char TLA = 153, TLB = 154;
    private final char FIELD_SELECTION = 89, TOWER_MENU = 90, UPGRADE_MENU = 91, CONFIRMATION = 92, TOWER_SHOTS = 109;
    private final char TOWER_UNAVAILABLE = 113, TOWER_UPGRADE_UNAVAILABLE = 114, NO_UPGRADE_LIMIT = 115;
    private final char BUILDING = 152;
    private char[][] fieldTower;
    private boolean[][] buildingTower;
    private long[][] buildingTowerTime;
    private long buildingTowerDuration;
    private char[][] fieldLevel;
    private boolean[][] fieldTowerShooting;
    private long[][] fieldTowerShootingTime;
    private int[][] fieldTowerShootingSprite;
    private long towerShootingSpriteInterval;
    private int towerShootingSprite, numberOfTowerShootingSprites;
    private int fieldSelectionX, oldFieldSelectionX;
    private int fieldSelectionY, oldFieldSelectionY;
    private boolean towerMenu, upgradeMenu, confirmation;
    private int towerMenuWidth, towerMenuHalfHeight;
    private int upgradeMenuWidth, upgradeMenuHalfHeight, upMenuX, upMenuY, upMenuCostY, towerUpLevel;
    private int confirmationX, confirmationY;
    private int horizTower, vertTowers;
    private int towerSize, towerHalfSize;
    private HashMap<Character, Integer> costTower1, costTower2, costTower3, costTower4;
    private HashMap<Character, Integer> sellTower1, sellTower2, sellTower3, sellTower4;
    private HashMap<Character, Integer> fullLifeTower1, fullLifeTower2, fullLifeTower3, fullLifeTower4;
    private HashMap<Character, Integer> reachTower1, reachTower2, reachTower3, reachTower4;
    private HashMap<Character, Integer> attackTower1, attackTower2, attackTower3, attackTower4;
    private long[][] lastAttackTower;
    private HashMap<Character, Long> attackIntervalTower1, attackIntervalTower2, attackIntervalTower3, attackIntervalTower4;
    private int towerShotStep;
    private ArrayList<Character> towerShotTower, towerShotLevel;
    private ArrayList<Integer> towerShotX, towerShotY, towerShotEnemyTargeted, towerShotSingleDamage, towerShotInteractions;
    private ArrayList<Double> towerShotAngle;
    private ArrayList<Long> towerShotLastSprite;
    private ArrayList<Integer> towerShotSprite;
    private int numberOfTowerShotSprites, whichShotSprite;
    private long towerShotSpriteInterval;
    private int towerShotsSize, towerShotsHalfSize, whichShotX, whichShotY;
    private HashMap<Character, Boolean> towerAvailable;
    private HashMap<Character, Character> towerMaxUpdate;
    private char currentTower, currentTowerLevel;
    private long tempTime;
    private long[][] towerTimerOfDeath;
    private long deadTowerTimeLimit = 2000;
    private Paint fieldTowerAlpha;

    // enemies
    private final char ENEMY0 = 48, ENEMY1 = 49, ENEMY2 = 50, ENEMY3 = 51, ENEMY4 = 52, ENEMY5 = 53, ENEMY6 = 54, ENEMY7 = 55, ENEMY8 = 56, ENEMY9 = 57;
    private final char ENEMY0_D = 121, ENEMY1_D = 122, ENEMY2_D = 123, ENEMY3_D = 135, ENEMY4_D = 136, ENEMY5_D = 137, ENEMY6_D = 138, ENEMY7_D = 139, ENEMY8_D = 140, ENEMY9_D = 141;
    private final char ENEMY0_SHOT = 142, ENEMY1_SHOT = 143, ENEMY2_SHOT = 144, ENEMY3_SHOT = 145, ENEMY4_SHOT = 146, ENEMY5_SHOT = 147, ENEMY6_SHOT = 148, ENEMY7_SHOT = 149, ENEMY8_SHOT = 150, ENEMY9_SHOT = 151;
    private int enemyFieldBlock;
    private String[] waveEnemies;
    private int[] waveEnemyX, waveEnemyShotY;
    private int[] waveEnemySprites, waveEnemyCurrSprite;
    private float[] waveEnemyY, waveEnemySpeed, waveEnemySize;
    private int[] waveEnemyReach, waveEnemyAttack, waveEnemyHeartWeight, waveEnemyPayment;
    private long[] waveEnemyLastAttack, waveEnemyAttackInterval, waveEnemySpriteInterval, waveEnemyLastSprite;
    private boolean[] waveEnemyMoving;
    private int enemyDefaultSize, enemyDefaultHalfSize, atX, enemyCurrentSize, enemyCurrentHalfSize, enemyCurrentLifeBarDistance;
    private char[] waveEnemyTowerWeakness;
    private char[] waveEnemyLevelWeakness;
    private float weaknessFactor = 1.5f;
    private ArrayList<Integer> deadEnemyX, deadEnemyY;
    private ArrayList<Float> deadEnemySize;
    private ArrayList<Character> deadEnemyChar;
    private ArrayList<Long> deadEnemyTime;
    private long deadEnemyTimeLimit = 2000;
    private String[] enemySettings0, enemySettings1, enemySettings2, enemySettings3, enemySettings4, enemySettings5, enemySettings6, enemySettings7, enemySettings8, enemySettings9;
    private Paint deadEnemyAlpha;

    // life
    private final char LIFE_BG = 93, LIFE_TOWER = 94, LIFE_ENEMY = 95, HEART = 96;
    private int towerLifeScale, enemyLifeScale;
    private int halfLifeBarInPX, lifeInPX; // dynamic values for enemies and towers
    private int currLife, fullLife; // dynamic values for towers
    private int lifeBarHalfWidth, lifeBarHeight;
    private int[] waveEnemyFullLife, waveEnemyCurrLife;
    private int[][] towerCurrLife;
    private int levelHearts, currHearts, heartSize, heartHalfSize;

    // network
    private int numberOfFilesToUpdate;
    private ArrayList<String> filesToDownload;
    private long localCheckingTime, serverTimeOutLimit = 10000, serverTimeOutStart;
    private HttpURLConnection listRemoteLevelsConnection = null, remoteLastModifiedConnection = null, downloadFileConnection = null;
    private final int UPD_NOT_STARTED = -1, UPD_DONE = 0, UPD_CHECKING_GAME_UPDS = 1, UPD_ERROR_OFFLINE = 2, UPD_ERROR_OTHER = 3;
    private final int UPD_READY_TO_DOWNLOAD = 4, UPD_DOWNLOADING = 5, UPD_READY_TO_CHECK_LOCAL = 6, UPD_CHECKING_LOCAL = 7;
    private final int UPD_LOCAL_MISSING = 8, UPD_READY_TO_CHECK_LEVELS = 9, UPD_CHECKING_LEVELS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        now = System.currentTimeMillis();
        context = this;
        applicationContext = getApplicationContext();
        decorView = this.getWindow().getDecorView();
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        deviceFilesFolder = context.getExternalFilesDir(null).getAbsolutePath();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        installedMod = sharedPref.getString("installedMod", "default");
        checkForLocalFolders();
        loadAppTextures();
        mainFont = FontCache.get("mainFont.ttf", context);

        // full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }

        audioInit();

        prepareConnectionPermissions();
        updateStatus = UPD_NOT_STARTED;
        isOnline = isNetworkAvailable();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context cxt, Intent intent) {
                isOnline = isNetworkAvailable();
            }
        };
        registerReceiver(receiver, filter);
        remoteServer = "https://onenice.monster/awesomerobot/towerdefense/";
        checkServerForUpdates();

        getScreenDimensions();
        initVariables();

        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.mainLayout);
        gameView = new GameView(context);
        mainLayout.addView(gameView);
    }

    private void initVariables() {
        // global
        highestLevel = sharedPref.getInt("highestLevel", 1);
        readAchievedRanks(installedMod);
        lastFpsTime = System.currentTimeMillis();
        gameState = "menu";
        menuButtonHalfSize = screenWidth / 15;
        info = "";
        dialogBlock = screenWidth / 30;
        dialogHalfBlock = dialogBlock / 2;
        dialogButtonHalfWidth = screenWidth / 8;
        dialogButtonHalfHeight = dialogButtonHalfWidth / 2;
        messageTextSize = dialogBlock + (dialogHalfBlock / 4);
        buttonTextSize = dialogBlock;
        dialogWidth = 24;

        dialogYesButtonX = ((screenWidth - (dialogBlock * dialogWidth)) / 2) + ((dialogWidth - 2) * dialogBlock) - dialogButtonHalfWidth;
        dialogNoButtonX = ((screenWidth - (dialogBlock * dialogWidth)) / 2) + ((2 * dialogBlock) + dialogButtonHalfWidth);

        // game field
        waveStartPoint = -100;
        towerMenuWidth = screenWidth / 9;
        towerMenuHalfHeight = towerMenuWidth * 2;

        buildingTowerDuration = 400;

        upgradeMenuWidth = towerMenuWidth;
        upgradeMenuHalfHeight = upgradeMenuWidth * 2;

        fdy = 0;
        rectOrigin = new Rect();
        rectDestiny = new Rect();

        lifeBarHalfWidth = screenWidth / 30;
        lifeBarHeight = lifeBarHalfWidth / 3;
        heartSize = screenWidth / 20;
        heartHalfSize = heartSize / 2;
        coinSize = heartSize;
        coinHalfSize = coinSize / 2;

        minVertTower = 1;

        towerShootingSpriteInterval = 200;
        numberOfTowerShootingSprites = 2;
        towerShotSpriteInterval = 150;
        numberOfTowerShotSprites = 2;

        waveStartDelay = 5000;
        winDelay = 2000;

        towerAvailable = new HashMap<>();
        towerMaxUpdate = new HashMap<>();

        deadEnemyAlpha = new Paint();
        fieldTowerAlpha = new Paint();
    }

    private void getScreenDimensions() {
        WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        Display display = null;
        try {
            display = wm.getDefaultDisplay();
        } catch (NullPointerException e) {
            System.out.println(TAG + e.getMessage());
        }

        if (display != null) {
            Point screenSize = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(screenSize);
                screenWidth = screenSize.x;
                screenHeight = screenSize.y;
            } else {
                display.getSize(screenSize);
                screenWidth = screenSize.x;
                screenHeight = screenSize.y;
            }
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }

        if (screenWidth > screenHeight) {
            int temp = screenWidth;
            screenWidth = screenHeight;
            screenHeight = temp;
        }
    }

    // network

    private void prepareConnectionPermissions() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    private void checkServerForUpdates() {
        serverTimeOutStart = now;
        updateStatus = UPD_CHECKING_GAME_UPDS;
        filesToDownload = new ArrayList<>();
        CheckGameUpdatesTask checkGameUpdatesTask = new CheckGameUpdatesTask();
        checkGameUpdatesTask.execute();
    }

    private class CheckGameUpdatesTask extends AsyncTask<URL, Void, ArrayList<String>> {
        protected ArrayList<String> doInBackground(URL... urls) {
            return listGameUpdates();
        }

        protected void onPostExecute(ArrayList<String> list) {
            for (String each : list) {
                filesToDownload.add(each);
            }
            updateStatus = UPD_READY_TO_CHECK_LEVELS;
        }
    }

    private ArrayList<String> listGameUpdates() {
        String[] files = {"towers_settings.txt", "app_textures.png"}; // TODO this list should come from a list_app_updates.php
        ArrayList<String> gameUpdates = new ArrayList<>();
        for (String each : files) {
            if (getRemoteLastModified("", each) > getLocalLastModified("default", each)) {
                gameUpdates.add(each);
            }
        }
        return gameUpdates;
    }

    private class ListRemoteLevelsTask extends AsyncTask<URL, Void, ArrayList<String>> {
        protected ArrayList<String> doInBackground(URL... urls) {
            return listRemoteLevels();
        }

        protected void onPostExecute(ArrayList<String> list) {
            String textureFileName;
            if (list.size() > 0) {
                for (int ftu = 0; ftu < list.size(); ftu ++) {
                    textureFileName = list.get(ftu).substring(0, list.get(ftu).length() - 4) + "_textures.png";
                    if (getRemoteLastModified("levels", list.get(ftu)) > getLocalLastModified("default/levels", list.get(ftu))) {
                        filesToDownload.add("levels/" + list.get(ftu));
                    }
                    if (getRemoteLastModified("levels/textures", textureFileName) > getLocalLastModified("default/levels/textures", textureFileName)) {
                        filesToDownload.add("levels/textures/" + textureFileName);
                    }
                }
                updateStatus = UPD_READY_TO_DOWNLOAD;

            } else if (updateStatus == UPD_CHECKING_GAME_UPDS) {
                updateStatus = UPD_ERROR_OTHER;
            }
        }
    }

    private ArrayList<String> listRemoteLevels() {
        ArrayList<String> remoteLevels = new ArrayList<>();
        if (isOnline) {
            int serverResponseCode;

            DataOutputStream wr = null;
            try {
                URL url = new URL(remoteServer + "listlevels.php");

                listRemoteLevelsConnection = (HttpURLConnection) url.openConnection();
                listRemoteLevelsConnection.setDoInput(true);
                listRemoteLevelsConnection.setDoOutput(true);
                listRemoteLevelsConnection.setRequestMethod("POST");

                wr = new DataOutputStream(listRemoteLevelsConnection.getOutputStream());

                /*
                serverResponseCode = listRemoteLevelsConnection.getResponseCode();
                String serverResponseMessage = listRemoteLevelsConnection.getResponseMessage();
                System.out.println(TAG + "server response: " + serverResponseMessage + " - code: " + serverResponseCode);
                if (serverResponseCode == 200){
                    System.out.println(TAG + "levels list fetched");
                }
                */

                // get php response
                InputStream is = listRemoteLevelsConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                while((line = rd.readLine()) != null) {
                    if (line.startsWith("level")) remoteLevels.add(line);
                }
                rd.close();
            } catch (Exception e) {
                updateStatus = UPD_ERROR_OTHER;
                e.printStackTrace();
            } finally {
                try {
                    wr.flush();
                    wr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            updateStatus = UPD_ERROR_OFFLINE;
        }
        return remoteLevels;
    }

    private long getRemoteLastModified(String folderName, String fileName) {
        long time = 0;
        try {
            URL url;
            if (folderName.isEmpty()) {
                url = new URL(remoteServer + fileName);
            } else {
                url = new URL(remoteServer + folderName + "/" + fileName);
            }
            remoteLastModifiedConnection = (HttpURLConnection) url.openConnection();
            String date = remoteLastModifiedConnection.getHeaderField("Last-Modified");
            SimpleDateFormat f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            try {
                Date d = f.parse(date);
                assert d != null;
                time = d.getTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (remoteLastModifiedConnection != null) {
                    remoteLastModifiedConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return time;
    }

    private long getLocalLastModified(String folderName, String fileName) {
        File file = null;
        try {
            if (folderName.isEmpty()) {
                file = new File(deviceFilesFolder + "/" + fileName);
            } else {
                file = new File(deviceFilesFolder + "/" + folderName + "/" + fileName);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (file != null) {
            if (file.exists()) {
                return file.lastModified();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, InputStream> {
        String dF = "";
        protected InputStream doInBackground(String... dFile) {
            dF = dFile[0];
            return downloadFile(dFile[0]);
        }

        protected void onPostExecute(InputStream downloadedFile) {
            if (downloadedFile != null) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File(deviceFilesFolder + "/default/" + dF));
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = downloadedFile.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    System.out.println(TAG + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        System.out.println(TAG + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            float dt = ((now - serverTimeOutStart) / 100) / 10f;
            System.out.println(TAG + dF + " downloaded in " + dt + "s");
            serverTimeOutStart = now;
            numberOfFilesToUpdate --;
        }
    }

    private InputStream downloadFile(String dFile) {
        try {
            URL url = new URL(remoteServer + dFile);
            downloadFileConnection = (HttpURLConnection) url.openConnection();
            downloadFileConnection.setRequestMethod("GET");
            downloadFileConnection.setDoOutput(true);
            downloadFileConnection.connect();
            return downloadFileConnection.getInputStream();
        } catch (Exception e) {
            System.out.println(TAG + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void checkForTimedOutConnection() {
        if (now - serverTimeOutStart > serverTimeOutLimit) {
            updateStatus = UPD_ERROR_OTHER;
            if (listRemoteLevelsConnection != null) {
                try {
                    listRemoteLevelsConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (remoteLastModifiedConnection != null) {
                try {
                    remoteLastModifiedConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (downloadFileConnection != null) {
                try {
                    downloadFileConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // IO

    public boolean onTouchEvent(MotionEvent me) {
        int index = me.getActionIndex();
        int xPos = (int) me.getX(index);
        int yPos = (int) me.getY(index);
        if (me.getActionMasked() == MotionEvent.ACTION_DOWN || me.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            action = "down";
            touchX = xPos;
            touchY = yPos;
        } else if (me.getActionMasked() == MotionEvent.ACTION_MOVE) {
            action = "move";
            touchX = xPos;
            touchY = yPos;
        } else if (me.getActionMasked() == MotionEvent.ACTION_UP || me.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            action = "up";
            touchX = xPos;
            touchY = yPos;
        }
        return true;
    }

    private void checkForLocalFolders() {
        if (!exists("default")) {
            makeFolder("default");
        }
        if (!exists("default/levels")) {
            makeFolder("default/levels");
        }
        if (!exists("default/levels/textures")) {
            makeFolder("default/levels/textures");
        }
    }

    private boolean exists(String path) {
        File fPath = new File(deviceFilesFolder + "/" + path);
        return fPath.exists();
    }

    private boolean makeFolder(String path) {
        File fPath = new File(deviceFilesFolder + "/" + path);
        return fPath.mkdir();
    }

    private void deleteLevelsScreenSettings(String mod) {
        String file = deviceFilesFolder + "/" + mod + "/level_screen_settings.txt";
        File dlFile = new File(file);
        dlFile.delete();
    }

    private void checkForTowersSettingsFile() {
        try {
            File towersSettingsFile = new File(deviceFilesFolder + "/" + installedMod, "towers_settings.txt");
            if (!towersSettingsFile.exists()) {
                InputStream inputStream = context.getAssets().open("towers_settings.txt");
                OutputStream outputStream = new FileOutputStream(deviceFilesFolder + "/" + installedMod + "/towers_settings.txt");
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                inputStream.close();
                inputStream = null;
                outputStream.flush();
                outputStream.close();
                outputStream = null;
                towersSettingsFile.setLastModified(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean setLevelsScreen(String mod) {
        boolean result = true;
        File lssModFolder, lssLevelsFolder, lssFile;
        String folderPath = deviceFilesFolder + "/" + mod;
        String fileName = "level_screen_settings.txt";
        String filePath = folderPath + "/" + fileName;
        lssModFolder = new File(folderPath);
        lssFile = new File(filePath);
        lssLevelsFolder = new File(folderPath + "/levels");

        if (!lssModFolder.exists()) {
            result = false; //if it's a mod, folder 'levels' should exist since installation
        }

        if (result) {
            if (mod.equals("default")) {
                File[] fList;
                String tempValue;
                ArrayList<String> list = new ArrayList<>();
                ArrayList<String> levelNames = new ArrayList<>();
                ArrayList<String> levelDescriptions = new ArrayList<>();
                try {
                    fList = lssLevelsFolder.listFiles();

                    for (int fl = 0; fl < fList.length; fl ++) {
                        if (!fList[fl].getName().equals("textures")) {
                            list.add(fList[fl].getName());
                        }
                    }

                    for (String each : list) {
                        tempValue = getValueFromLocalLevel(mod, each, "levelname");
                        if (!tempValue.equals("error") && !tempValue.isEmpty()) {
                            levelNames.add(tempValue);
                        } else {
                            result = false;
                        }
                        tempValue = getValueFromLocalLevel(mod, each, "leveldescription");
                        if (!tempValue.equals("error") && !tempValue.isEmpty()) {
                            levelDescriptions.add(tempValue);
                        } else {
                            result = false;
                        }
                    }

                    // puts everything in order
                    int ns = list.size();
                    String temp;
                    for (int o = 0; o < ns; o ++) {
                        for (int n = o + 1; n < ns; n ++) {
                            int fn = Integer.parseInt(list.get(n).substring(6, 9));
                            int fo = Integer.parseInt(list.get(o).substring(6, 9));
                            if (fn < fo) {
                                temp = list.get(n);
                                list.set(n, list.get(o));
                                list.set(o, temp);

                                temp = levelNames.get(n);
                                levelNames.set(n, levelNames.get(o));
                                levelNames.set(o, temp);

                                temp = levelDescriptions.get(n);
                                levelDescriptions.set(n, levelDescriptions.get(o));
                                levelDescriptions.set(o, temp);
                            }
                        }
                    }

                    /*for (int g = 0; g < list.size(); g ++) { // just prints the generated lists
                        System.out.println(TAG + list.get(g) + " - " + levelNames.get(g) + " - " + levelDescriptions.get(g));
                    }*/

                } catch (Exception e) {
                    result = false;
                    e.printStackTrace();
                }
                if (result && levelNames.size() > 0 && levelDescriptions.size() > 0 && levelNames.size() == levelDescriptions.size()) {
                    result = saveLevelListFile(mod, levelNames, levelDescriptions);
                } else {
                    result = false;
                }
            } else { // TODO create mod level screen settings

            }

            vertLevels = 0;

            if (result) { // reads file
                String[] lineParts, resultParts;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(lssFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.replaceAll(" = ", "=");
                        lineParts = line.split("=");
                        if (lineParts.length > 1) {
                            resultParts = lineParts[1].split(";;; ");

                            if (vertLevels == 0) {
                                // initializing levelTitle and levelDescription arrays
                                numberOfAvailableLevels = resultParts.length;
                                vertLevels = numberOfAvailableLevels + 3;

                                levelAvailable = new boolean[vertLevels];
                                Arrays.fill(levelAvailable, false);
                                for (int lA = 0; lA < numberOfAvailableLevels; lA++) {
                                    levelAvailable[lA] = true;
                                }
                                levelTitle = new String[vertLevels];
                                levelDescription = new String[vertLevels];
                                Arrays.fill(levelTitle, "Coming soon");
                                Arrays.fill(levelDescription, "...");
                            }

                            switch (lineParts[0]) {
                                case "level names" :
                                    levelTitle = resultParts;
                                    break;
                                case "level descriptions" :
                                    levelDescription = resultParts;
                                    break;
                            }

                            levelButtonArea = screenWidth / 4;
                            levelButtonHalfArea = levelButtonArea / 2;
                            levelsMarginY = screenHeight - (levelButtonArea * (screenHeight / levelButtonArea));
                            extraLevelsScreen = ((vertLevels - (screenHeight / levelButtonArea)) * levelButtonArea) - levelsMarginY + (2 * levelButtonArea);
                            if (vertLevels > (screenHeight / levelButtonArea) + 1) {
                                lsy = extraLevelsScreen;
                            } else {
                                lsy = 0;
                            }
                        } else {
                            result = false;
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    result = false;
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private String getValueFromLocalLevel(String mod, String fileName, String value) {
        String levelName = "";
        BufferedReader reader = null;
        File gvFile = new File(deviceFilesFolder + "/" + mod + "/levels/" + fileName);
        try {
            reader = new BufferedReader(new FileReader(gvFile));
            String fileTextLine;
            String[] lineParts;
            while ((fileTextLine = reader.readLine()) != null) {
                fileTextLine = fileTextLine.replaceAll(" = ", "=");
                if (!fileTextLine.startsWith("#")) {
                    lineParts = fileTextLine.split("=");
                    if (lineParts.length > 1) {
                        lineParts[0] = lineParts[0].replace(" ", "");
                        if (lineParts[0].equals(value)) {
                            levelName = lineParts[1];
                        }
                    }
                }
            }
        } catch (Exception e) {
            levelName = "error";
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    levelName = "error";
                    e.printStackTrace();
                }
            }
        }
        return levelName;
    }

    private boolean saveLevelListFile(String mod, ArrayList<String> lNames, ArrayList<String> lDescriptions) {
        boolean result = true;
        String path = deviceFilesFolder + "/" + mod;
        String fileName = "level_screen_settings.txt";
        File file = new File(path, fileName);
        String data = "";
        if (lNames.size() == lDescriptions.size()) {
            data = "level names = ";
            for (int sd = 0; sd < lNames.size(); sd ++) {
                data += lNames.get(sd);
                if (sd < lNames.size() - 1) {
                    data += ";;; ";
                }
            }
            data += "\nlevel descriptions = ";
            for (int sd = 0; sd < lDescriptions.size(); sd ++) {
                data += lDescriptions.get(sd);
                if (sd < lDescriptions.size() - 1) {
                    data += ";;; ";
                }
            }
        } else {
            result = false;
        }

        if (result) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(data.getBytes());
            } catch(Exception e) {
                result = false;
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    //TODO load mods

    private boolean loadLevelSettings(String mod, int whichLevel) {
        boolean result = true;

        String whichLevelTXT = "level_001.txt";
        if (whichLevel >= 100) {
            whichLevelTXT = "level_" + whichLevel + ".txt";
        } else if (whichLevel >= 10) {
            whichLevelTXT = "level_0" + whichLevel + ".txt";
        } else if (whichLevel >= 1) {
            whichLevelTXT = "level_00" + whichLevel + ".txt";
        } else {
            result = false;
        }

        fieldFromFile = "";

        File gvFile = new File(deviceFilesFolder + "/" + mod + "/levels/" + whichLevelTXT);

        towerSize = 0;
        vertTowers = 0;
        levelCoins = 0;
        levelHearts = 0;
        waves = 0;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(gvFile));
            String fileTextLine;
            String[] lineParts;
            while ((fileTextLine = reader.readLine()) != null) {
                fileTextLine = fileTextLine.replaceAll(" ", "");
                fileTextLine = fileTextLine.toLowerCase();
                if (!fileTextLine.startsWith("#")) {
                    lineParts = fileTextLine.split("=");
                    if (lineParts.length > 1) {
                        switch (lineParts[0]) {
                            case "towers" :
                                try {
                                    towerSize = screenWidth / Integer.parseInt(lineParts[1]);
                                } catch (NumberFormatException e) {
                                    result = false;
                                    System.out.println(TAG + "towerSize: " + e.getMessage());
                                }
                                break;
                            case "fieldlength" :
                                try {
                                    vertTowers = Integer.parseInt(lineParts[1]);
                                } catch (NumberFormatException e) {
                                    result = false;
                                    System.out.println(TAG + "vertTowers: " + e.getMessage());
                                }
                                break;
                            case "levelcoins" :
                                try {
                                    levelCoins = Integer.parseInt(lineParts[1]);
                                } catch (NumberFormatException e) {
                                    result = false;
                                    System.out.println(TAG + "levelCoins: " + e.getMessage());
                                }
                                break;
                            case "levellives" :
                                try {
                                    levelHearts = Integer.parseInt(lineParts[1]);
                                } catch (NumberFormatException e) {
                                    result = false;
                                    System.out.println(TAG + "levelHearts: " + e.getMessage());
                                }
                                break;
                            case "tower1" :
                                if (lineParts[1].startsWith("true")) {
                                    towerAvailable.put(TOWER1, true);
                                } else if (lineParts[1].startsWith("false")) {
                                    towerAvailable.put(TOWER1, false);
                                }
                                break;
                            case "tower2" :
                                if (lineParts[1].startsWith("true")) {
                                    towerAvailable.put(TOWER2, true);
                                } else if (lineParts[1].startsWith("false")) {
                                    towerAvailable.put(TOWER2, false);
                                }
                                break;
                            case "tower3" :
                                if (lineParts[1].startsWith("true")) {
                                    towerAvailable.put(TOWER3, true);
                                } else if (lineParts[1].startsWith("false")) {
                                    towerAvailable.put(TOWER3, false);
                                }
                                break;
                            case "tower4" :
                                if (lineParts[1].startsWith("true")) {
                                    towerAvailable.put(TOWER4, true);
                                } else if (lineParts[1].startsWith("false")) {
                                    towerAvailable.put(TOWER4, false);
                                }
                                break;
                            case "maxleveltower1" :
                                switch (lineParts[1]) {
                                    case "1" :
                                        towerMaxUpdate.put(TOWER1, TL1);
                                        break;
                                    case "2" :
                                        towerMaxUpdate.put(TOWER1, TL2);
                                        break;
                                    case "3" :
                                        towerMaxUpdate.put(TOWER1, TL3);
                                        break;
                                    case "4" :
                                        towerMaxUpdate.put(TOWER1, TL4);
                                        break;
                                    case "5" :
                                        towerMaxUpdate.put(TOWER1, TL5);
                                        break;
                                    case "6a" :
                                        towerMaxUpdate.put(TOWER1, TL6A1);
                                        break;
                                    case "6b" :
                                        towerMaxUpdate.put(TOWER1, TL6B1);
                                        break;
                                    case "max" :
                                        towerMaxUpdate.put(TOWER1, NO_UPGRADE_LIMIT);
                                        break;
                                    case "empty" :
                                    default :
                                        towerMaxUpdate.put(TOWER1, EMPTY1);
                                        break;
                                }
                                break;
                            case "maxleveltower2" :
                                switch (lineParts[1]) {
                                    case "1" :
                                        towerMaxUpdate.put(TOWER2, TL1);
                                        break;
                                    case "2" :
                                        towerMaxUpdate.put(TOWER2, TL2);
                                        break;
                                    case "3" :
                                        towerMaxUpdate.put(TOWER2, TL3);
                                        break;
                                    case "4" :
                                        towerMaxUpdate.put(TOWER2, TL4);
                                        break;
                                    case "5" :
                                        towerMaxUpdate.put(TOWER2, TL5);
                                        break;
                                    case "6a" :
                                        towerMaxUpdate.put(TOWER2, TL6A1);
                                        break;
                                    case "6b" :
                                        towerMaxUpdate.put(TOWER2, TL6B1);
                                        break;
                                    case "max" :
                                        towerMaxUpdate.put(TOWER2, NO_UPGRADE_LIMIT);
                                        break;
                                    case "empty" :
                                    default :
                                        towerMaxUpdate.put(TOWER2, EMPTY1);
                                        break;
                                }
                                break;
                            case "maxleveltower3" :
                                switch (lineParts[1]) {
                                    case "1" :
                                        towerMaxUpdate.put(TOWER3, TL1);
                                        break;
                                    case "2" :
                                        towerMaxUpdate.put(TOWER3, TL2);
                                        break;
                                    case "3" :
                                        towerMaxUpdate.put(TOWER3, TL3);
                                        break;
                                    case "4" :
                                        towerMaxUpdate.put(TOWER3, TL4);
                                        break;
                                    case "5" :
                                        towerMaxUpdate.put(TOWER3, TL5);
                                        break;
                                    case "6a" :
                                        towerMaxUpdate.put(TOWER3, TL6A1);
                                        break;
                                    case "6b" :
                                        towerMaxUpdate.put(TOWER3, TL6B1);
                                        break;
                                    case "max" :
                                        towerMaxUpdate.put(TOWER3, NO_UPGRADE_LIMIT);
                                        break;
                                    case "empty" :
                                    default :
                                        towerMaxUpdate.put(TOWER3, EMPTY1);
                                        break;
                                }
                                break;
                            case "maxleveltower4" :
                                switch (lineParts[1]) {
                                    case "1" :
                                        towerMaxUpdate.put(TOWER4, TL1);
                                        break;
                                    case "2" :
                                        towerMaxUpdate.put(TOWER4, TL2);
                                        break;
                                    case "3" :
                                        towerMaxUpdate.put(TOWER4, TL3);
                                        break;
                                    case "4" :
                                        towerMaxUpdate.put(TOWER4, TL4);
                                        break;
                                    case "5" :
                                        towerMaxUpdate.put(TOWER4, TL5);
                                        break;
                                    case "6a" :
                                        towerMaxUpdate.put(TOWER4, TL6A1);
                                        break;
                                    case "6b" :
                                        towerMaxUpdate.put(TOWER4, TL6B1);
                                        break;
                                    case "max" :
                                        towerMaxUpdate.put(TOWER4, NO_UPGRADE_LIMIT);
                                        break;
                                    case "empty" :
                                    default :
                                        towerMaxUpdate.put(TOWER4, EMPTY1);
                                        break;
                                }
                                break;
                            case "waves" :
                                waveEnemies = lineParts[1].split(",");
                                waves = waveEnemies.length;
                                break;
                            case "enemy0" :
                                enemySettings0 = lineParts[1].split(",");
                                break;
                            case "enemy1" :
                                enemySettings1 = lineParts[1].split(",");
                                break;
                            case "enemy2" :
                                enemySettings2 = lineParts[1].split(",");
                                break;
                            case "enemy3" :
                                enemySettings3 = lineParts[1].split(",");
                                break;
                            case "enemy4" :
                                enemySettings4 = lineParts[1].split(",");
                                break;
                            case "enemy5" :
                                enemySettings5 = lineParts[1].split(",");
                                break;
                            case "enemy6" :
                                enemySettings6 = lineParts[1].split(",");
                                break;
                            case "enemy7" :
                                enemySettings7 = lineParts[1].split(",");
                                break;
                            case "enemy8" :
                                enemySettings8 = lineParts[1].split(",");
                                break;
                            case "enemy9" :
                                enemySettings9 = lineParts[1].split(",");
                                break;
                            case "field" :
                                fieldFromFile += lineParts[1];
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    result = false;
                    e.printStackTrace();
                }
            }
        }
        if (towerSize == 0 ||
                vertTowers == 0 ||
                levelCoins == 0 ||
                levelHearts == 0 ||
                !towerAvailable.containsKey(TOWER1) ||
                !towerAvailable.containsKey(TOWER2) ||
                !towerAvailable.containsKey(TOWER3) ||
                !towerAvailable.containsKey(TOWER4) ||
                !towerMaxUpdate.containsKey(TOWER1) ||
                !towerMaxUpdate.containsKey(TOWER2) ||
                !towerMaxUpdate.containsKey(TOWER3) ||
                !towerMaxUpdate.containsKey(TOWER4) ||
                waves == 0) {
            result = false;
        }
        return result;
    }

    private void loadAppTextures() {
        try {
            if (appTextures != null) {
                appTextures.recycle();
                appTextures = null;
            }
            File appTexturesFile = new File(deviceFilesFolder + "/default", "app_textures.png");
            if (!appTexturesFile.exists()) {
                appTextures = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.app_textures, null);
                FileOutputStream fileOutputStream = new FileOutputStream(appTexturesFile);
                appTextures.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();

                appTexturesFile.setLastModified(1);

                System.out.println(TAG + "App skin loaded from drawable");
            } else {
                appTextures = BitmapFactory.decodeFile(deviceFilesFolder + "/default/app_textures.png", null);
                System.out.println(TAG + "App skin loaded from device storage");
            }
        } catch (Exception e) {
            System.out.println(TAG + e.getMessage());
        }
    }

    private boolean loadLevelTextures(String mod, int whichLevel) {
        boolean result = true;
        String levelFileName = "";

        if (whichLevel >= 100) {
            levelFileName = "level_" + whichLevel + "_textures.png";
        } else if (whichLevel >= 10) {
            levelFileName = "level_0" + whichLevel + "_textures.png";
        } else if (whichLevel >= 1) {
            levelFileName = "level_00" + whichLevel + "_textures.png";
        } else {
            result = false;
        }

        try {
            if (gameTextures != null) {
                gameTextures.recycle();
                gameTextures = null;
            }
            gameTextures = BitmapFactory.decodeFile(deviceFilesFolder + "/" + mod + "/levels/textures/" + levelFileName, null);
        } catch (Exception e) {
            System.out.println(TAG + e.getMessage());
            result = false;
        }
        if (gameTextures == null) result = false;
        return result;
    }

    private void loadTowersSettings() {
        File tsFile = new File(deviceFilesFolder + "/" + installedMod + "/towers_settings.txt");
        BufferedReader reader = null;
        costTower1 = new HashMap<>();
        costTower2 = new HashMap<>();
        costTower3 = new HashMap<>();
        costTower4 = new HashMap<>();
        sellTower1 = new HashMap<>();
        sellTower2 = new HashMap<>();
        sellTower3 = new HashMap<>();
        sellTower4 = new HashMap<>();
        fullLifeTower1 = new HashMap<>();
        fullLifeTower2 = new HashMap<>();
        fullLifeTower3 = new HashMap<>();
        fullLifeTower4 = new HashMap<>();
        reachTower1 = new HashMap<>();
        reachTower2 = new HashMap<>();
        reachTower3 = new HashMap<>();
        reachTower4 = new HashMap<>();
        attackTower1 = new HashMap<>();
        attackTower2 = new HashMap<>();
        attackTower3 = new HashMap<>();
        attackTower4 = new HashMap<>();
        attackIntervalTower1 = new HashMap<>();
        attackIntervalTower2 = new HashMap<>();
        attackIntervalTower3 = new HashMap<>();
        attackIntervalTower4 = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(tsFile));
            String fileTextLine;
            String[] lineParts;
            String[] values;
            char lastChar;
            while ((fileTextLine = reader.readLine()) != null) {
                fileTextLine = fileTextLine.replaceAll(" ", "");
                if (!fileTextLine.startsWith("#")) {
                    lineParts = fileTextLine.split("=");
                    if (lineParts.length > 1) {
                        lastChar = lineParts[0].charAt(lineParts[0].length() - 1);
                        values = lineParts[1].split(",");
                        switch (lastChar) {
                            case '1' :
                                if (lineParts[0].startsWith("costTower")) {
                                    costTower1.put(EMPTY1, Integer.parseInt(values[0]));
                                    costTower1.put(TL1, Integer.parseInt(values[1]));
                                    costTower1.put(TL2, Integer.parseInt(values[2]));
                                    costTower1.put(TL3, Integer.parseInt(values[3]));
                                    costTower1.put(TL4, Integer.parseInt(values[4]));
                                    costTower1.put(TL5, Integer.parseInt(values[5]));
                                    costTower1.put(TL6A1, Integer.parseInt(values[6]));
                                    costTower1.put(TL6B1, Integer.parseInt(values[7]));
                                } else if (lineParts[0].startsWith("sellTower")) {
                                    sellTower1.put(TL1, Integer.parseInt(values[0]));
                                    sellTower1.put(TL2, Integer.parseInt(values[1]));
                                    sellTower1.put(TL3, Integer.parseInt(values[2]));
                                    sellTower1.put(TL4, Integer.parseInt(values[3]));
                                    sellTower1.put(TL5, Integer.parseInt(values[4]));
                                    sellTower1.put(TL6A1, Integer.parseInt(values[5]));
                                    sellTower1.put(TL6A2, Integer.parseInt(values[6]));
                                    sellTower1.put(TL6B1, Integer.parseInt(values[7]));
                                    sellTower1.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("fullLifeTower")) {
                                    fullLifeTower1.put(TL1, Integer.parseInt(values[0]));
                                    fullLifeTower1.put(TL2, Integer.parseInt(values[1]));
                                    fullLifeTower1.put(TL3, Integer.parseInt(values[2]));
                                    fullLifeTower1.put(TL4, Integer.parseInt(values[3]));
                                    fullLifeTower1.put(TL5, Integer.parseInt(values[4]));
                                    fullLifeTower1.put(TL6A1, Integer.parseInt(values[5]));
                                    fullLifeTower1.put(TL6A2, Integer.parseInt(values[6]));
                                    fullLifeTower1.put(TL6B1, Integer.parseInt(values[7]));
                                    fullLifeTower1.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("reachTower")) {
                                    reachTower1.put(TL1, Integer.parseInt(values[0]));
                                    reachTower1.put(TL2, Integer.parseInt(values[1]));
                                    reachTower1.put(TL3, Integer.parseInt(values[2]));
                                    reachTower1.put(TL4, Integer.parseInt(values[3]));
                                    reachTower1.put(TL5, Integer.parseInt(values[4]));
                                    reachTower1.put(TL6A1, Integer.parseInt(values[5]));
                                    reachTower1.put(TL6A2, Integer.parseInt(values[6]));
                                    reachTower1.put(TL6B1, Integer.parseInt(values[7]));
                                    reachTower1.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackTower")) {
                                    attackTower1.put(TL1, Integer.parseInt(values[0]));
                                    attackTower1.put(TL2, Integer.parseInt(values[1]));
                                    attackTower1.put(TL3, Integer.parseInt(values[2]));
                                    attackTower1.put(TL4, Integer.parseInt(values[3]));
                                    attackTower1.put(TL5, Integer.parseInt(values[4]));
                                    attackTower1.put(TL6A1, Integer.parseInt(values[5]));
                                    attackTower1.put(TL6A2, Integer.parseInt(values[6]));
                                    attackTower1.put(TL6B1, Integer.parseInt(values[7]));
                                    attackTower1.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackIntervalTower")) {
                                    attackIntervalTower1.put(TL1, Long.parseLong(values[0]));
                                    attackIntervalTower1.put(TL2, Long.parseLong(values[1]));
                                    attackIntervalTower1.put(TL3, Long.parseLong(values[2]));
                                    attackIntervalTower1.put(TL4, Long.parseLong(values[3]));
                                    attackIntervalTower1.put(TL5, Long.parseLong(values[4]));
                                    attackIntervalTower1.put(TL6A1, Long.parseLong(values[5]));
                                    attackIntervalTower1.put(TL6A2, Long.parseLong(values[6]));
                                    attackIntervalTower1.put(TL6B1, Long.parseLong(values[7]));
                                    attackIntervalTower1.put(TL6B2, Long.parseLong(values[8]));
                                }
                                break;
                            case '2' :
                                if (lineParts[0].startsWith("costTower")) {
                                    costTower2.put(EMPTY1, Integer.parseInt(values[0]));
                                    costTower2.put(TL1, Integer.parseInt(values[1]));
                                    costTower2.put(TL2, Integer.parseInt(values[2]));
                                    costTower2.put(TL3, Integer.parseInt(values[3]));
                                    costTower2.put(TL4, Integer.parseInt(values[4]));
                                    costTower2.put(TL5, Integer.parseInt(values[5]));
                                    costTower2.put(TL6A1, Integer.parseInt(values[6]));
                                    costTower2.put(TL6B1, Integer.parseInt(values[7]));
                                } else if (lineParts[0].startsWith("sellTower")) {
                                    sellTower2.put(TL1, Integer.parseInt(values[0]));
                                    sellTower2.put(TL2, Integer.parseInt(values[1]));
                                    sellTower2.put(TL3, Integer.parseInt(values[2]));
                                    sellTower2.put(TL4, Integer.parseInt(values[3]));
                                    sellTower2.put(TL5, Integer.parseInt(values[4]));
                                    sellTower2.put(TL6A1, Integer.parseInt(values[5]));
                                    sellTower2.put(TL6A2, Integer.parseInt(values[6]));
                                    sellTower2.put(TL6B1, Integer.parseInt(values[7]));
                                    sellTower2.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("fullLifeTower")) {
                                    fullLifeTower2.put(TL1, Integer.parseInt(values[0]));
                                    fullLifeTower2.put(TL2, Integer.parseInt(values[1]));
                                    fullLifeTower2.put(TL3, Integer.parseInt(values[2]));
                                    fullLifeTower2.put(TL4, Integer.parseInt(values[3]));
                                    fullLifeTower2.put(TL5, Integer.parseInt(values[4]));
                                    fullLifeTower2.put(TL6A1, Integer.parseInt(values[5]));
                                    fullLifeTower2.put(TL6A2, Integer.parseInt(values[6]));
                                    fullLifeTower2.put(TL6B1, Integer.parseInt(values[7]));
                                    fullLifeTower2.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("reachTower")) {
                                    reachTower2.put(TL1, Integer.parseInt(values[0]));
                                    reachTower2.put(TL2, Integer.parseInt(values[1]));
                                    reachTower2.put(TL3, Integer.parseInt(values[2]));
                                    reachTower2.put(TL4, Integer.parseInt(values[3]));
                                    reachTower2.put(TL5, Integer.parseInt(values[4]));
                                    reachTower2.put(TL6A1, Integer.parseInt(values[5]));
                                    reachTower2.put(TL6A2, Integer.parseInt(values[6]));
                                    reachTower2.put(TL6B1, Integer.parseInt(values[7]));
                                    reachTower2.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackTower")) {
                                    attackTower2.put(TL1, Integer.parseInt(values[0]));
                                    attackTower2.put(TL2, Integer.parseInt(values[1]));
                                    attackTower2.put(TL3, Integer.parseInt(values[2]));
                                    attackTower2.put(TL4, Integer.parseInt(values[3]));
                                    attackTower2.put(TL5, Integer.parseInt(values[4]));
                                    attackTower2.put(TL6A1, Integer.parseInt(values[5]));
                                    attackTower2.put(TL6A2, Integer.parseInt(values[6]));
                                    attackTower2.put(TL6B1, Integer.parseInt(values[7]));
                                    attackTower2.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackIntervalTower")) {
                                    attackIntervalTower2.put(TL1, Long.parseLong(values[0]));
                                    attackIntervalTower2.put(TL2, Long.parseLong(values[1]));
                                    attackIntervalTower2.put(TL3, Long.parseLong(values[2]));
                                    attackIntervalTower2.put(TL4, Long.parseLong(values[3]));
                                    attackIntervalTower2.put(TL5, Long.parseLong(values[4]));
                                    attackIntervalTower2.put(TL6A1, Long.parseLong(values[5]));
                                    attackIntervalTower2.put(TL6A2, Long.parseLong(values[6]));
                                    attackIntervalTower2.put(TL6B1, Long.parseLong(values[7]));
                                    attackIntervalTower2.put(TL6B2, Long.parseLong(values[8]));
                                }
                                break;
                            case '3' :
                                if (lineParts[0].startsWith("costTower")) {
                                    costTower3.put(EMPTY1, Integer.parseInt(values[0]));
                                    costTower3.put(TL1, Integer.parseInt(values[1]));
                                    costTower3.put(TL2, Integer.parseInt(values[2]));
                                    costTower3.put(TL3, Integer.parseInt(values[3]));
                                    costTower3.put(TL4, Integer.parseInt(values[4]));
                                    costTower3.put(TL5, Integer.parseInt(values[5]));
                                    costTower3.put(TL6A1, Integer.parseInt(values[6]));
                                    costTower3.put(TL6B1, Integer.parseInt(values[7]));
                                } else if (lineParts[0].startsWith("sellTower")) {
                                    sellTower3.put(TL1, Integer.parseInt(values[0]));
                                    sellTower3.put(TL2, Integer.parseInt(values[1]));
                                    sellTower3.put(TL3, Integer.parseInt(values[2]));
                                    sellTower3.put(TL4, Integer.parseInt(values[3]));
                                    sellTower3.put(TL5, Integer.parseInt(values[4]));
                                    sellTower3.put(TL6A1, Integer.parseInt(values[5]));
                                    sellTower3.put(TL6A2, Integer.parseInt(values[6]));
                                    sellTower3.put(TL6B1, Integer.parseInt(values[7]));
                                    sellTower3.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("fullLifeTower")) {
                                    fullLifeTower3.put(TL1, Integer.parseInt(values[0]));
                                    fullLifeTower3.put(TL2, Integer.parseInt(values[1]));
                                    fullLifeTower3.put(TL3, Integer.parseInt(values[2]));
                                    fullLifeTower3.put(TL4, Integer.parseInt(values[3]));
                                    fullLifeTower3.put(TL5, Integer.parseInt(values[4]));
                                    fullLifeTower3.put(TL6A1, Integer.parseInt(values[5]));
                                    fullLifeTower3.put(TL6A2, Integer.parseInt(values[6]));
                                    fullLifeTower3.put(TL6B1, Integer.parseInt(values[7]));
                                    fullLifeTower3.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("reachTower")) {
                                    reachTower3.put(TL1, Integer.parseInt(values[0]));
                                    reachTower3.put(TL2, Integer.parseInt(values[1]));
                                    reachTower3.put(TL3, Integer.parseInt(values[2]));
                                    reachTower3.put(TL4, Integer.parseInt(values[3]));
                                    reachTower3.put(TL5, Integer.parseInt(values[4]));
                                    reachTower3.put(TL6A1, Integer.parseInt(values[5]));
                                    reachTower3.put(TL6A2, Integer.parseInt(values[6]));
                                    reachTower3.put(TL6B1, Integer.parseInt(values[7]));
                                    reachTower3.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackTower")) {
                                    attackTower3.put(TL1, Integer.parseInt(values[0]));
                                    attackTower3.put(TL2, Integer.parseInt(values[1]));
                                    attackTower3.put(TL3, Integer.parseInt(values[2]));
                                    attackTower3.put(TL4, Integer.parseInt(values[3]));
                                    attackTower3.put(TL5, Integer.parseInt(values[4]));
                                    attackTower3.put(TL6A1, Integer.parseInt(values[5]));
                                    attackTower3.put(TL6A2, Integer.parseInt(values[6]));
                                    attackTower3.put(TL6B1, Integer.parseInt(values[7]));
                                    attackTower3.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackIntervalTower")) {
                                    attackIntervalTower3.put(TL1, Long.parseLong(values[0]));
                                    attackIntervalTower3.put(TL2, Long.parseLong(values[1]));
                                    attackIntervalTower3.put(TL3, Long.parseLong(values[2]));
                                    attackIntervalTower3.put(TL4, Long.parseLong(values[3]));
                                    attackIntervalTower3.put(TL5, Long.parseLong(values[4]));
                                    attackIntervalTower3.put(TL6A1, Long.parseLong(values[5]));
                                    attackIntervalTower3.put(TL6A2, Long.parseLong(values[6]));
                                    attackIntervalTower3.put(TL6B1, Long.parseLong(values[7]));
                                    attackIntervalTower3.put(TL6B2, Long.parseLong(values[8]));
                                }
                                break;
                            case '4' :
                                if (lineParts[0].startsWith("costTower")) {
                                    costTower4.put(EMPTY1, Integer.parseInt(values[0]));
                                    costTower4.put(TL1, Integer.parseInt(values[1]));
                                    costTower4.put(TL2, Integer.parseInt(values[2]));
                                    costTower4.put(TL3, Integer.parseInt(values[3]));
                                    costTower4.put(TL4, Integer.parseInt(values[4]));
                                    costTower4.put(TL5, Integer.parseInt(values[5]));
                                    costTower4.put(TL6A1, Integer.parseInt(values[6]));
                                    costTower4.put(TL6B1, Integer.parseInt(values[7]));
                                } else if (lineParts[0].startsWith("sellTower")) {
                                    sellTower4.put(TL1, Integer.parseInt(values[0]));
                                    sellTower4.put(TL2, Integer.parseInt(values[1]));
                                    sellTower4.put(TL3, Integer.parseInt(values[2]));
                                    sellTower4.put(TL4, Integer.parseInt(values[3]));
                                    sellTower4.put(TL5, Integer.parseInt(values[4]));
                                    sellTower4.put(TL6A1, Integer.parseInt(values[5]));
                                    sellTower4.put(TL6A2, Integer.parseInt(values[6]));
                                    sellTower4.put(TL6B1, Integer.parseInt(values[7]));
                                    sellTower4.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("fullLifeTower")) {
                                    fullLifeTower4.put(TL1, Integer.parseInt(values[0]));
                                    fullLifeTower4.put(TL2, Integer.parseInt(values[1]));
                                    fullLifeTower4.put(TL3, Integer.parseInt(values[2]));
                                    fullLifeTower4.put(TL4, Integer.parseInt(values[3]));
                                    fullLifeTower4.put(TL5, Integer.parseInt(values[4]));
                                    fullLifeTower4.put(TL6A1, Integer.parseInt(values[5]));
                                    fullLifeTower4.put(TL6A2, Integer.parseInt(values[6]));
                                    fullLifeTower4.put(TL6B1, Integer.parseInt(values[7]));
                                    fullLifeTower4.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("reachTower")) {
                                    reachTower4.put(TL1, Integer.parseInt(values[0]));
                                    reachTower4.put(TL2, Integer.parseInt(values[1]));
                                    reachTower4.put(TL3, Integer.parseInt(values[2]));
                                    reachTower4.put(TL4, Integer.parseInt(values[3]));
                                    reachTower4.put(TL5, Integer.parseInt(values[4]));
                                    reachTower4.put(TL6A1, Integer.parseInt(values[5]));
                                    reachTower4.put(TL6A2, Integer.parseInt(values[6]));
                                    reachTower4.put(TL6B1, Integer.parseInt(values[7]));
                                    reachTower4.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackTower")) {
                                    attackTower4.put(TL1, Integer.parseInt(values[0]));
                                    attackTower4.put(TL2, Integer.parseInt(values[1]));
                                    attackTower4.put(TL3, Integer.parseInt(values[2]));
                                    attackTower4.put(TL4, Integer.parseInt(values[3]));
                                    attackTower4.put(TL5, Integer.parseInt(values[4]));
                                    attackTower4.put(TL6A1, Integer.parseInt(values[5]));
                                    attackTower4.put(TL6A2, Integer.parseInt(values[6]));
                                    attackTower4.put(TL6B1, Integer.parseInt(values[7]));
                                    attackTower4.put(TL6B2, Integer.parseInt(values[8]));
                                } else if (lineParts[0].startsWith("attackIntervalTower")) {
                                    attackIntervalTower4.put(TL1, Long.parseLong(values[0]));
                                    attackIntervalTower4.put(TL2, Long.parseLong(values[1]));
                                    attackIntervalTower4.put(TL3, Long.parseLong(values[2]));
                                    attackIntervalTower4.put(TL4, Long.parseLong(values[3]));
                                    attackIntervalTower4.put(TL5, Long.parseLong(values[4]));
                                    attackIntervalTower4.put(TL6A1, Long.parseLong(values[5]));
                                    attackIntervalTower4.put(TL6A2, Long.parseLong(values[6]));
                                    attackIntervalTower4.put(TL6B1, Long.parseLong(values[7]));
                                    attackIntervalTower4.put(TL6B2, Long.parseLong(values[8]));
                                }
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (costTower1.size() != 8 &&
                    costTower2.size() != 8 &&
                    costTower3.size() != 8 &&
                    costTower4.size() != 8 &&
                    sellTower1.size() != 9 &&
                    sellTower2.size() != 9 &&
                    sellTower3.size() != 9 &&
                    sellTower4.size() != 9 &&
                    fullLifeTower1.size() != 9 &&
                    fullLifeTower2.size() != 9 &&
                    fullLifeTower3.size() != 9 &&
                    fullLifeTower4.size() != 9 &&
                    reachTower1.size() != 9 &&
                    reachTower2.size() != 9 &&
                    reachTower3.size() != 9 &&
                    reachTower4.size() != 9 &&
                    attackTower1.size() != 9 &&
                    attackTower2.size() != 9 &&
                    attackTower3.size() != 9 &&
                    attackTower4.size() != 9 &&
                    attackIntervalTower1.size() != 9 &&
                    attackIntervalTower2.size() != 9 &&
                    attackIntervalTower3.size() != 9 &&
                    attackIntervalTower4.size() != 9) {
                System.out.println(TAG + "Towers settings loaded from default values");
                costTower1 = new HashMap<>();
                costTower2 = new HashMap<>();
                costTower3 = new HashMap<>();
                costTower4 = new HashMap<>();
                sellTower1 = new HashMap<>();
                sellTower2 = new HashMap<>();
                sellTower3 = new HashMap<>();
                sellTower4 = new HashMap<>();
                fullLifeTower1 = new HashMap<>();
                fullLifeTower2 = new HashMap<>();
                fullLifeTower3 = new HashMap<>();
                fullLifeTower4 = new HashMap<>();
                reachTower1 = new HashMap<>();
                reachTower2 = new HashMap<>();
                reachTower3 = new HashMap<>();
                reachTower4 = new HashMap<>();
                attackTower1 = new HashMap<>();
                attackTower2 = new HashMap<>();
                attackTower3 = new HashMap<>();
                attackTower4 = new HashMap<>();
                attackIntervalTower1 = new HashMap<>();
                attackIntervalTower2 = new HashMap<>();
                attackIntervalTower3 = new HashMap<>();
                attackIntervalTower4 = new HashMap<>();
                costTower1.put(EMPTY1, 100);
                costTower1.put(TL1, 120);
                costTower1.put(TL2, 140);
                costTower1.put(TL3, 160);
                costTower1.put(TL4, 180);
                costTower1.put(TL5, 200);
                costTower1.put(TL6A1, 220);
                costTower1.put(TL6B1, 220);
                costTower2.put(EMPTY1, 100);
                costTower2.put(TL1, 120);
                costTower2.put(TL2, 140);
                costTower2.put(TL3, 160);
                costTower2.put(TL4, 180);
                costTower2.put(TL5, 200);
                costTower2.put(TL6A1, 220);
                costTower2.put(TL6B1, 220);
                costTower3.put(EMPTY1, 100);
                costTower3.put(TL1, 120);
                costTower3.put(TL2, 140);
                costTower3.put(TL3, 160);
                costTower3.put(TL4, 180);
                costTower3.put(TL5, 200);
                costTower3.put(TL6A1, 220);
                costTower3.put(TL6B1, 220);
                costTower4.put(EMPTY1, 100);
                costTower4.put(TL1, 120);
                costTower4.put(TL2, 140);
                costTower4.put(TL3, 160);
                costTower4.put(TL4, 180);
                costTower4.put(TL5, 200);
                costTower4.put(TL6A1, 220);
                costTower4.put(TL6B1, 220);

                sellTower1.put(TL1, 80);
                sellTower1.put(TL2, 180);
                sellTower1.put(TL3, 290);
                sellTower1.put(TL4, 420);
                sellTower1.put(TL5, 560);
                sellTower1.put(TL6A1, 730);
                sellTower1.put(TL6A2, 900);
                sellTower1.put(TL6B1, 730);
                sellTower1.put(TL6B2, 900);
                sellTower2.put(TL1, 80);
                sellTower2.put(TL2, 180);
                sellTower2.put(TL3, 290);
                sellTower2.put(TL4, 420);
                sellTower2.put(TL5, 560);
                sellTower2.put(TL6A1, 730);
                sellTower2.put(TL6A2, 900);
                sellTower2.put(TL6B1, 730);
                sellTower2.put(TL6B2, 900);
                sellTower3.put(TL1, 80);
                sellTower3.put(TL2, 180);
                sellTower3.put(TL3, 290);
                sellTower3.put(TL4, 420);
                sellTower3.put(TL5, 560);
                sellTower3.put(TL6A1, 730);
                sellTower3.put(TL6A2, 900);
                sellTower3.put(TL6B1, 730);
                sellTower3.put(TL6B2, 900);
                sellTower4.put(TL1, 80);
                sellTower4.put(TL2, 180);
                sellTower4.put(TL3, 290);
                sellTower4.put(TL4, 420);
                sellTower4.put(TL5, 560);
                sellTower4.put(TL6A1, 730);
                sellTower4.put(TL6A2, 900);
                sellTower4.put(TL6B1, 730);
                sellTower4.put(TL6B2, 900);

                fullLifeTower1.put(TL1, 200);
                fullLifeTower1.put(TL2, 220);
                fullLifeTower1.put(TL3, 240);
                fullLifeTower1.put(TL4, 260);
                fullLifeTower1.put(TL5, 280);
                fullLifeTower1.put(TL6A1, 300);
                fullLifeTower1.put(TL6A2, 320);
                fullLifeTower1.put(TL6B1, 340);
                fullLifeTower1.put(TL6B2, 360);
                fullLifeTower2.put(TL1, 200);
                fullLifeTower2.put(TL2, 220);
                fullLifeTower2.put(TL3, 240);
                fullLifeTower2.put(TL4, 260);
                fullLifeTower2.put(TL5, 280);
                fullLifeTower2.put(TL6A1, 300);
                fullLifeTower2.put(TL6A2, 320);
                fullLifeTower2.put(TL6B1, 340);
                fullLifeTower2.put(TL6B2, 360);
                fullLifeTower3.put(TL1, 200);
                fullLifeTower3.put(TL2, 220);
                fullLifeTower3.put(TL3, 240);
                fullLifeTower3.put(TL4, 260);
                fullLifeTower3.put(TL5, 280);
                fullLifeTower3.put(TL6A1, 300);
                fullLifeTower3.put(TL6A2, 320);
                fullLifeTower3.put(TL6B1, 340);
                fullLifeTower3.put(TL6B2, 360);
                fullLifeTower4.put(TL1, 200);
                fullLifeTower4.put(TL2, 220);
                fullLifeTower4.put(TL3, 240);
                fullLifeTower4.put(TL4, 260);
                fullLifeTower4.put(TL5, 280);
                fullLifeTower4.put(TL6A1, 300);
                fullLifeTower4.put(TL6A2, 320);
                fullLifeTower4.put(TL6B1, 340);
                fullLifeTower4.put(TL6B2, 360);

                reachTower1.put(TL1, 2);
                reachTower1.put(TL2, 2);
                reachTower1.put(TL3, 2);
                reachTower1.put(TL4, 2);
                reachTower1.put(TL5, 2);
                reachTower1.put(TL6A1, 3);
                reachTower1.put(TL6A2, 3);
                reachTower1.put(TL6B1, 3);
                reachTower1.put(TL6B2, 3);
                reachTower2.put(TL1, 2);
                reachTower2.put(TL2, 2);
                reachTower2.put(TL3, 2);
                reachTower2.put(TL4, 2);
                reachTower2.put(TL5, 2);
                reachTower2.put(TL6A1, 3);
                reachTower2.put(TL6A2, 3);
                reachTower2.put(TL6B1, 3);
                reachTower2.put(TL6B2, 3);
                reachTower3.put(TL1, 2);
                reachTower3.put(TL2, 2);
                reachTower3.put(TL3, 2);
                reachTower3.put(TL4, 2);
                reachTower3.put(TL5, 2);
                reachTower3.put(TL6A1, 3);
                reachTower3.put(TL6A2, 3);
                reachTower3.put(TL6B1, 3);
                reachTower3.put(TL6B2, 3);
                reachTower4.put(TL1, 2);
                reachTower4.put(TL2, 2);
                reachTower4.put(TL3, 2);
                reachTower4.put(TL4, 2);
                reachTower4.put(TL5, 2);
                reachTower4.put(TL6A1, 3);
                reachTower4.put(TL6A2, 3);
                reachTower4.put(TL6B1, 3);
                reachTower4.put(TL6B2, 3);

                attackTower1.put(TL1, 3);
                attackTower1.put(TL2, 4);
                attackTower1.put(TL3, 5);
                attackTower1.put(TL4, 6);
                attackTower1.put(TL5, 7);
                attackTower1.put(TL6A1, 8);
                attackTower1.put(TL6A2, 10);
                attackTower1.put(TL6B1, 8);
                attackTower1.put(TL6B2, 10);
                attackTower2.put(TL1, 3);
                attackTower2.put(TL2, 4);
                attackTower2.put(TL3, 5);
                attackTower2.put(TL4, 6);
                attackTower2.put(TL5, 7);
                attackTower2.put(TL6A1, 8);
                attackTower2.put(TL6A2, 10);
                attackTower2.put(TL6B1, 8);
                attackTower2.put(TL6B2, 10);
                attackTower3.put(TL1, 3);
                attackTower3.put(TL2, 4);
                attackTower3.put(TL3, 5);
                attackTower3.put(TL4, 6);
                attackTower3.put(TL5, 7);
                attackTower3.put(TL6A1, 8);
                attackTower3.put(TL6A2, 10);
                attackTower3.put(TL6B1, 8);
                attackTower3.put(TL6B2, 10);
                attackTower4.put(TL1, 3);
                attackTower4.put(TL2, 4);
                attackTower4.put(TL3, 5);
                attackTower4.put(TL4, 6);
                attackTower4.put(TL5, 7);
                attackTower4.put(TL6A1, 8);
                attackTower4.put(TL6A2, 10);
                attackTower4.put(TL6B1, 8);
                attackTower4.put(TL6B2, 10);

                attackIntervalTower1.put(TL1, (long)700);
                attackIntervalTower1.put(TL2, (long)680);
                attackIntervalTower1.put(TL3, (long)660);
                attackIntervalTower1.put(TL4, (long)640);
                attackIntervalTower1.put(TL5, (long)620);
                attackIntervalTower1.put(TL6A1, (long)600);
                attackIntervalTower1.put(TL6A2, (long)580);
                attackIntervalTower1.put(TL6B1, (long)600);
                attackIntervalTower1.put(TL6B2, (long)580);
                attackIntervalTower2.put(TL1, (long)700);
                attackIntervalTower2.put(TL2, (long)680);
                attackIntervalTower2.put(TL3, (long)660);
                attackIntervalTower2.put(TL4, (long)640);
                attackIntervalTower2.put(TL5, (long)620);
                attackIntervalTower2.put(TL6A1, (long)600);
                attackIntervalTower2.put(TL6A2, (long)580);
                attackIntervalTower2.put(TL6B1, (long)600);
                attackIntervalTower2.put(TL6B2, (long)580);
                attackIntervalTower3.put(TL1, (long)700);
                attackIntervalTower3.put(TL2, (long)680);
                attackIntervalTower3.put(TL3, (long)660);
                attackIntervalTower3.put(TL4, (long)640);
                attackIntervalTower3.put(TL5, (long)620);
                attackIntervalTower3.put(TL6A1, (long)600);
                attackIntervalTower3.put(TL6A2, (long)580);
                attackIntervalTower3.put(TL6B1, (long)600);
                attackIntervalTower3.put(TL6B2, (long)580);
                attackIntervalTower4.put(TL1, (long)700);
                attackIntervalTower4.put(TL2, (long)680);
                attackIntervalTower4.put(TL3, (long)660);
                attackIntervalTower4.put(TL4, (long)640);
                attackIntervalTower4.put(TL5, (long)620);
                attackIntervalTower4.put(TL6A1, (long)600);
                attackIntervalTower4.put(TL6A2, (long)580);
                attackIntervalTower4.put(TL6B1, (long)600);
                attackIntervalTower4.put(TL6B2, (long)580);
            } else {
                System.out.println(TAG + "Towers settings values loaded from file");
            }
        }
    }

    private long getFreeMemory() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMem = runtime.totalMemory() - runtime.freeMemory();
        final long maxHeapSize = runtime.maxMemory();
        return maxHeapSize - usedMem;
    }

    private int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public float pxToDp(int px, Context ctx) {
        return px / ctx.getResources().getDisplayMetrics().density;
    }

    private void handleMenuTouch() {
        if (!action.isEmpty()) {
            switch (action) {
                case "down" :
                case "move" :
                    break;
                case "up" :
                    switch (updateStatus) {
                        case UPD_NOT_STARTED :
                            if (!exitAppButtonPressed &&
                                    touchX >= screenWidth - (menuButtonHalfSize * 3) &&
                                    touchY <= menuButtonHalfSize * 3) {
                                audio.play(audioClick, false, now);
                                exitAppButtonPressed = true;
                            } else if (yesButtonPressed()) {
                                audio.play(audioClick, false, now);
                                onBackPressed();
                            } else if (noButtonPressed()) {
                                audio.play(audioClick, false, now);
                                exitAppButtonPressed = false;
                            }
                            break;
                        case UPD_DONE :
                            if (!exitAppButtonPressed) {
                                if (touchX >= (screenWidth / 2) - menuButtonHalfSize &&
                                        touchX <= (screenWidth / 2) + menuButtonHalfSize &&
                                        touchY >= (screenHeight / 2) - menuButtonHalfSize &&
                                        touchY <= (screenHeight / 2) + menuButtonHalfSize) { // play
                                    audio.play(audioClick, false, now);
                                    levelButtonPressed = 0;
                                    // TODO should align with the last reached level
                                    if (vertLevels > (screenHeight / levelButtonArea) + 1) {
                                        lsy = extraLevelsScreen;
                                    } else {
                                        lsy = 0;
                                    }
                                    gameState = "levels";
                                } else if (touchX >= screenWidth - (menuButtonHalfSize * 3) &&
                                        touchY <= menuButtonHalfSize * 3) {
                                    audio.play(audioClick, false, now);
                                    exitAppButtonPressed = true;
                                }
                            } else if (yesButtonPressed()) {
                                audio.play(audioClick, false, now);
                                onBackPressed();
                            } else if (noButtonPressed()) {
                                audio.play(audioClick, false, now);
                                exitAppButtonPressed = false;
                            }
                            break;
                        case UPD_ERROR_OFFLINE :
                            if (!exitAppButtonPressed) {
                                if (yesButtonPressed()) { // offline, retry
                                    audio.play(audioClick, false, now);
                                    checkServerForUpdates();
                                } else if (noButtonPressed()) { // offline, cancel
                                    audio.play(audioClick, false, now);
                                    updateStatus = UPD_READY_TO_CHECK_LOCAL;
                                }
                            } else if (yesButtonPressed()) {
                                audio.play(audioClick, false, now);
                                onBackPressed();
                            } else if (noButtonPressed()) {
                                audio.play(audioClick, false, now);
                                exitAppButtonPressed = false;
                            }
                            break;
                        case UPD_ERROR_OTHER :
                            if (!exitAppButtonPressed) {
                                if (touchX >= screenWidth - (menuButtonHalfSize * 3) &&
                                        touchY <= menuButtonHalfSize * 3) { // close
                                    audio.play(audioClick, false, now);
                                    exitAppButtonPressed = true;
                                } else if (yesButtonPressed()) { // update error, continue
                                    audio.play(audioClick, false, now);
                                    updateStatus = UPD_READY_TO_CHECK_LOCAL;
                                }
                            } else if (yesButtonPressed()) {
                                audio.play(audioClick, false, now);
                                onBackPressed();
                            } else if (noButtonPressed()) {
                                audio.play(audioClick, false, now);
                                exitAppButtonPressed = false;
                            }
                            break;
                        case UPD_LOCAL_MISSING :
                            if (yesButtonPressed()) {
                                audio.play(audioClick, false, now);
                                onBackPressed();
                            }
                            break;
                    }
                    break;
            }
            action = "";
        }
    }

    private void handleLevelsTouch() {
        if (!action.isEmpty()) {
            switch (action) {
                case "down" :
                    touchInitY = touchY;
                    break;
                case "move" :
                    if (vertLevels + 2 > screenHeight / levelButtonArea && levelButtonPressed == 0) {
                        levelsScreenMoved = true; // move levels screen up and down
                        int tempFdy = lsy + (touchInitY - touchY);
                        if (tempFdy >= 0 && tempFdy <= extraLevelsScreen) {
                            lsy = tempFdy;
                        } else if (tempFdy < 0) {
                            lsy = 0;
                        } else {
                            lsy = extraLevelsScreen;
                        }
                        touchInitY = touchY;
                    }
                    break;
                case "up" :
                    if (levelButtonPressed > 0 && levelButtonPressed <= highestLevel && levelAvailable[levelButtonPressed - 1]) {
                        if (yesButtonPressed()) { // dialog yes
                            audio.play(audioClick, false, now);
                            action = "";
                            gameState = "game";
                            initLevel(levelButtonPressed, "easy");
                        } else if (noButtonPressed()) { // dialog no
                            levelButtonPressed = 0;
                        }
                    } else if (touchX >= screenWidth - (menuButtonHalfSize * 3) &&
                            touchY <= menuButtonHalfSize * 3) { // close
                        audio.play(audioClick, false, now);
                        action = "";
                        onBackPressed();
                    } else if (touchX >= (screenWidth / 2) - menuButtonHalfSize &&
                            touchX <= (screenWidth / 2) + menuButtonHalfSize) { // level button pressed
                        float bY = ((float)((vertLevels + 2) * levelButtonArea) - (touchY + lsy)) / levelButtonArea;
                        if (bY % 1 >= 0.2f &&
                                bY % 1 <= 0.8f &&
                                (int) bY > 0 &&
                                (int) bY <= vertLevels &&
                                (int) bY <= highestLevel &&
                                levelAvailable[(int) bY - 1]) {
                            audio.play(audioClick, false, now);
                            levelButtonPressed = (int) bY;
                        }
                        action = "";
                    }
                    break;
            }
        }
    }

    private void handleGameTouch() {
        if (!action.isEmpty()) {
            switch (action) {
                case "down" :
                    touchInitY = touchY;
                    if (levelStartFieldScroll) levelStartFieldScroll = false;
                    break;
                case "move" :
                    if (vertTowers > screenHeight / towerSize && alive && !won && !gameClosePressed) {
                        fieldMoved = true; // move field up and down
                        if (towerMenu) towerMenu = false;
                        if (upgradeMenu) upgradeMenu = false;
                        if (confirmation) confirmation = false;
                        resetTowerMark();
                        int tempFdy = fdy + (touchInitY - touchY);
                        if (tempFdy >= 0 && tempFdy <= extraScreen) {
                            fdy = tempFdy;
                        } else if (tempFdy < 0) {
                            fdy = 0;
                        } else {
                            fdy = extraScreen;
                        }
                        touchInitY = touchY;
                    }
                    break;
                case "up" :
                    if (!fieldMoved && alive && !gameClosePressed && !won) {
                        if (touchX >= screenWidth - (menuButtonHalfSize * 3) &&
                                touchY <= menuButtonHalfSize * 3) { // close
                            audio.play(audioClick, false, now);
                            gameClosePressed = true;
                        } else if (towerMenu) { // put a tower in the field
                            if (touchX <= towerMenuWidth &&
                                    touchY >= (screenHeight / 2) - towerMenuHalfHeight &&
                                    touchY < (screenHeight / 2) - (towerMenuHalfHeight / 2)) {
                                if (confirmation &&
                                        confirmationX == 0 &&
                                        confirmationY == (screenHeight / 2) - towerMenuHalfHeight) { // put tower1 on the field
                                    audio.play(audioTowerBuilt, true, now);
                                    levelCoins -= costTower1.get(currentTowerLevel);
                                    fieldTower[fieldSelectionX][fieldSelectionY] = TOWER1;
                                    currentTower = fieldTower[fieldSelectionX][fieldSelectionY];
                                    fieldLevel[fieldSelectionX][fieldSelectionY] = TL1;
                                    currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                    towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower1.get(currentTowerLevel);
                                    setBuildingStatus(fieldSelectionX, fieldSelectionY);
                                    resetTowerMark();
                                    towerMenu = false;
                                    upgradeMenu = false;
                                } else if (levelCoins >= costTower1.get(currentTowerLevel) &&
                                        towerAvailable.get(TOWER1)) {
                                    audio.play(audioSelect, false, now);
                                    confirmation = true;
                                    confirmationX = 0;
                                    confirmationY = (screenHeight / 2) - towerMenuHalfHeight;
                                } else {
                                    System.out.println(TAG + "not enough coins");
                                }
                            } else if (touchX <= towerMenuWidth &&
                                    touchY >= (screenHeight / 2) - (towerMenuHalfHeight / 2) &&
                                    touchY < screenHeight / 2) {
                                if (confirmation &&
                                        confirmationX == 0 &&
                                        confirmationY == (screenHeight / 2) - (towerMenuHalfHeight / 2)) { // put tower2 on the field
                                    audio.play(audioTowerBuilt, true, now);
                                    levelCoins -= costTower2.get(currentTowerLevel);
                                    fieldTower[fieldSelectionX][fieldSelectionY] = TOWER2;
                                    currentTower = fieldTower[fieldSelectionX][fieldSelectionY];
                                    fieldLevel[fieldSelectionX][fieldSelectionY] = TL1;
                                    currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                    towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower2.get(currentTowerLevel);
                                    setBuildingStatus(fieldSelectionX, fieldSelectionY);
                                    resetTowerMark();
                                    towerMenu = false;
                                    upgradeMenu = false;
                                } else if (levelCoins >= costTower2.get(currentTowerLevel) &&
                                        towerAvailable.get(TOWER2)) {
                                    audio.play(audioSelect, false, now);
                                    confirmation = true;
                                    confirmationX = 0;
                                    confirmationY = (screenHeight / 2) - (towerMenuHalfHeight / 2);
                                } else {
                                    System.out.println(TAG + "not enough coins");
                                }
                            } else if (touchX <= towerMenuWidth &&
                                    touchY >= screenHeight / 2 &&
                                    touchY < (screenHeight / 2) + (towerMenuHalfHeight / 2)) {
                                if (confirmation &&
                                        confirmationX == 0 &&
                                        confirmationY == screenHeight / 2) { // put tower3 on the field
                                    audio.play(audioTowerBuilt, true, now);
                                    levelCoins -= costTower3.get(currentTowerLevel);
                                    fieldTower[fieldSelectionX][fieldSelectionY] = TOWER3;
                                    currentTower = fieldTower[fieldSelectionX][fieldSelectionY];
                                    fieldLevel[fieldSelectionX][fieldSelectionY] = TL1;
                                    currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                    towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower3.get(currentTowerLevel);
                                    setBuildingStatus(fieldSelectionX, fieldSelectionY);
                                    resetTowerMark();
                                    towerMenu = false;
                                    upgradeMenu = false;
                                } else if (levelCoins >= costTower3.get(currentTowerLevel) &&
                                        towerAvailable.get(TOWER3)) {
                                    audio.play(audioSelect, false, now);
                                    confirmation = true;
                                    confirmationX = 0;
                                    confirmationY = screenHeight / 2;
                                } else {
                                    System.out.println(TAG + "not enough coins");
                                }
                            } else if (touchX <= towerMenuWidth &&
                                    touchY >= (screenHeight / 2) + (towerMenuHalfHeight / 2) &&
                                    touchY <= (screenHeight / 2) + towerMenuHalfHeight) {
                                if (confirmation &&
                                        confirmationX == 0 &&
                                        confirmationY == (screenHeight / 2) + (towerMenuHalfHeight / 2)) { // put tower4 on the field
                                    audio.play(audioTowerBuilt, true, now);
                                    levelCoins -= costTower4.get(currentTowerLevel);
                                    fieldTower[fieldSelectionX][fieldSelectionY] = TOWER4;
                                    currentTower = fieldTower[fieldSelectionX][fieldSelectionY];
                                    fieldLevel[fieldSelectionX][fieldSelectionY] = TL1;
                                    currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                    towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower4.get(currentTowerLevel);
                                    setBuildingStatus(fieldSelectionX, fieldSelectionY);
                                    resetTowerMark();
                                    towerMenu = false;
                                    upgradeMenu = false;
                                } else if (levelCoins >= costTower4.get(currentTowerLevel) &&
                                        towerAvailable.get(TOWER4)) {
                                    audio.play(audioSelect, false, now);
                                    confirmation = true;
                                    confirmationX = 0;
                                    confirmationY = (screenHeight / 2) + (towerMenuHalfHeight / 2);
                                } else {
                                    System.out.println(TAG + "not enough coins");
                                }
                            } else {
                                checkFieldTouch();
                            }
                        } else if (upgradeMenu) {
                            if (touchX >= screenWidth - upgradeMenuWidth &&
                                    touchY >= (screenHeight / 2) - upgradeMenuHalfHeight &&
                                    touchY < (screenHeight / 2) - (upgradeMenuHalfHeight / 2)) {
                                switch (currentTowerLevel) {
                                    case TL5 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == (screenHeight / 2) - upgradeMenuHalfHeight) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL6A1;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL5) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = (screenHeight / 2) - upgradeMenuHalfHeight;
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                    case TL6A1 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == (screenHeight / 2) - upgradeMenuHalfHeight) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL6A2;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL6A1) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = (screenHeight / 2) - upgradeMenuHalfHeight;
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                }
                            } else if (touchX >= screenWidth - upgradeMenuWidth &&
                                    touchY >= (screenHeight / 2) - (upgradeMenuHalfHeight / 2) &&
                                    touchY < screenHeight / 2) {
                                switch (currentTowerLevel) {
                                    case TL5 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == (screenHeight / 2) - (upgradeMenuHalfHeight / 2)) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL6B1;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL5) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = (screenHeight / 2) - (upgradeMenuHalfHeight / 2);
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                    case TL6B1 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == (screenHeight / 2) - (upgradeMenuHalfHeight / 2)) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL6B2;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL6B1) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = (screenHeight / 2) - (upgradeMenuHalfHeight / 2);
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                }
                            } else if (touchX >= screenWidth - upgradeMenuWidth &&
                                    touchY >= screenHeight / 2 &&
                                    touchY < (screenHeight / 2) + (upgradeMenuHalfHeight / 2)) {
                                switch (currentTowerLevel) {
                                    case TL1 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == screenHeight / 2) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL2;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL1) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = screenHeight / 2;
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                    case TL2 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == screenHeight / 2) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL3;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL2) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = screenHeight / 2;
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                    case TL3 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == screenHeight / 2) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL4;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL3) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = screenHeight / 2;
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                    case TL4 :
                                        if (confirmation &&
                                                confirmationX == screenWidth - upgradeMenuWidth &&
                                                confirmationY == screenHeight / 2) {
                                            audio.play(audioTowerUpgrade, true, now);
                                            payUpgrade();
                                            fieldLevel[fieldSelectionX][fieldSelectionY] = TL5;
                                            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];
                                            upgradeTowerLife();
                                            resetConfirmation();
                                            checkTowerLevel();
                                        } else if (towerMaxUpdate.get(currentTower) != TL4) {
                                            if (hasMoneyForUpgrade()) {
                                                audio.play(audioSelect, false, now);
                                                confirmation = true;
                                                confirmationX = screenWidth - upgradeMenuWidth;
                                                confirmationY = screenHeight / 2;
                                            } else {
                                                System.out.println(TAG + "not enough coins");
                                            }
                                        }
                                        break;
                                }
                            } else if (touchX >= screenWidth - upgradeMenuWidth &&
                                    touchY >= (screenHeight / 2) + (upgradeMenuHalfHeight / 2) &&
                                    touchY <= (screenHeight / 2) + upgradeMenuHalfHeight) { // sell tower
                                if (confirmation &&
                                        confirmationX == screenWidth - upgradeMenuWidth &&
                                        confirmationY == (screenHeight / 2) + (upgradeMenuHalfHeight / 2)) {
                                    switch (currentTower) {
                                        case TOWER1 :
                                            levelCoins += (int)((float)(sellTower1.get(currentTowerLevel)) / fullLifeTower1.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]);
                                            break;
                                        case TOWER2 :
                                            levelCoins += (int)((float)(sellTower2.get(currentTowerLevel)) / fullLifeTower2.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]);
                                            break;
                                        case TOWER3 :
                                            levelCoins += (int)((float)(sellTower3.get(currentTowerLevel)) / fullLifeTower3.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]);
                                            break;
                                        case TOWER4 :
                                            levelCoins += (int)((float)(sellTower4.get(currentTowerLevel)) / fullLifeTower4.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]);
                                            break;
                                    }
                                    destroyTower(fieldSelectionX, fieldSelectionY);
                                    resetTowerMark();
                                    upgradeMenu = false;
                                } else {
                                    audio.play(audioSelect, false, now);
                                    confirmation = true;
                                    confirmationX = screenWidth - upgradeMenuWidth;
                                    confirmationY = (screenHeight / 2) + (upgradeMenuHalfHeight / 2);
                                }
                            } else {
                                checkFieldTouch();
                            }
                        } else {
                            checkFieldTouch();
                        }
                    } else if (yesButtonPressed()) { // dialog yes
                        audio.play(audioClick, false, now);
                        audio.stop();
                        if (!alive) {
                            alive = true;
                            initLevel(level, "easy");
                        } else if (won && now - winTime > winDelay) {
                            action = "";
                            levelButtonPressed = 0;
                            onBackPressed();
                        } else if (gameClosePressed) {
                            action = "";
                            levelButtonPressed = 0;
                            onBackPressed();
                        }
                    } else if (noButtonPressed()) { // dialog no
                        audio.play(audioClick, false, now);
                        if (!alive) {
                            audio.stop();
                            levelButtonPressed = 0;
                            onBackPressed();
                        } else if (gameClosePressed) {
                            gameClosePressed = false;
                        }
                    }
                    fieldMoved = false;
                    break;
            }
            action = "";
        }
    }

    private void setBuildingStatus(int ii, int jj) {
        buildingTowerTime[ii][jj] = now;
        buildingTower[ii][jj] = true;
    }

    private boolean yesButtonPressed() {
        boolean pressed = false;
        if (touchX >= dialogYesButtonX - dialogButtonHalfWidth &&
                touchX <= dialogYesButtonX + dialogButtonHalfWidth &&
                touchY >= dialogButtonY - dialogButtonHalfHeight &&
                touchY <= dialogButtonY + dialogButtonHalfHeight) {
            pressed = true;
        }
        return pressed;
    }

    private boolean noButtonPressed() {
        boolean pressed = false;
        if (touchX >= dialogNoButtonX - dialogButtonHalfWidth &&
                touchX <= dialogNoButtonX + dialogButtonHalfWidth &&
                touchY >= dialogButtonY - dialogButtonHalfHeight &&
                touchY <= dialogButtonY + dialogButtonHalfHeight) {
            pressed = true;
        }
        return pressed;
    }

    private void readAchievedRanks(String mod) {
        achievedRanks = new int[highestLevel];
        Arrays.fill(achievedRanks, 0);
        String[] ranks = new String[0];
        File arFile = new File(deviceFilesFolder + "/" + mod + "/achieved_ranks.txt");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(arFile));
            String fileTextLine;
            String[] lineParts;
            while ((fileTextLine = reader.readLine()) != null) {
                fileTextLine = fileTextLine.replaceAll(" ", "");
                fileTextLine = fileTextLine.toLowerCase();
                if (!fileTextLine.startsWith("#")) {
                    lineParts = fileTextLine.split("=");
                    if (lineParts.length > 1) {
                        if (lineParts[0].equals("achievedranks")) {
                            if (lineParts[1].contains(",")) {
                                ranks = lineParts[1].split(",");
                            } else {
                                ranks = new String[1];
                                ranks[0] = lineParts[1];
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(TAG + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(TAG + e.getMessage());
                }
            }
        }
        int cr;
        if (ranks.length > 0) {
            for (int ra = 0; ra < highestLevel; ra ++) {
                if (ra < ranks.length) {
                    try {
                        cr = Integer.parseInt(ranks[ra]);
                        if (cr < 0 || cr > 3) {
                            cr = 0;
                        }
                        achievedRanks[ra] = cr;
                    } catch (Exception e) {
                        achievedRanks[ra] = 0;
                        e.printStackTrace();
                        System.out.println(TAG + e.getMessage());
                    }
                }
            }
        }
    }

    private void saveAchievedRanks(String mod) {
        String ar = "";
        String path = deviceFilesFolder + "/" + mod;
        String fileName = "achieved_ranks.txt";
        File file = new File(path, fileName);
        String data = "";
        for (int sar = 0; sar < achievedRanks.length; sar ++) {
            if (sar == achievedRanks.length - 1) {
                ar += " " + achievedRanks[sar];
            } else {
                ar += " " + achievedRanks[sar] + ",";
            }
        }
        data = "achieved ranks =" + ar;

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(TAG + e.getMessage());
        } finally {
            try {
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(TAG + e.getMessage());
            }
        }
    }

    private void audioInit() {
        audioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        audio = new Audio();
        audio.initAudio(context);
        audioClick = audio.load(R.raw.audio_click);
        audioEnemy0Attack = audio.load(R.raw.audio_enemy0_attack);
        audioEnemy1Attack = audio.load(R.raw.audio_enemy1_attack);
        audioEnemy2Attack = audio.load(R.raw.audio_enemy2_attack);
        audioEnemy3Attack = audio.load(R.raw.audio_enemy3_attack);
        audioEnemy4Attack = audio.load(R.raw.audio_enemy4_attack);
        audioEnemy5Attack = audio.load(R.raw.audio_enemy5_attack);
        audioEnemy6Attack = audio.load(R.raw.audio_enemy6_attack);
        audioEnemy7Attack = audio.load(R.raw.audio_enemy7_attack);
        audioEnemy8Attack = audio.load(R.raw.audio_enemy8_attack);
        audioEnemy9Attack = audio.load(R.raw.audio_enemy9_attack);
        audioEnemy0Death = audio.load(R.raw.audio_enemy0_death);
        audioEnemy1Death = audio.load(R.raw.audio_enemy1_death);
        audioEnemy2Death = audio.load(R.raw.audio_enemy2_death);
        audioEnemy3Death = audio.load(R.raw.audio_enemy3_death);
        audioEnemy4Death = audio.load(R.raw.audio_enemy4_death);
        audioEnemy5Death = audio.load(R.raw.audio_enemy5_death);
        audioEnemy6Death = audio.load(R.raw.audio_enemy6_death);
        audioEnemy7Death = audio.load(R.raw.audio_enemy7_death);
        audioEnemy8Death = audio.load(R.raw.audio_enemy8_death);
        audioEnemy9Death = audio.load(R.raw.audio_enemy9_death);
        audioTowerBuilt = audio.load(R.raw.audio_tower_built);
        audioTowerUpgrade = audio.load(R.raw.audio_tower_upgrade);
        audioTower1Attack = audio.load(R.raw.audio_tower1_attack);
        audioTower2Attack = audio.load(R.raw.audio_tower2_attack);
        audioTower3Attack = audio.load(R.raw.audio_tower3_attack);
        audioTower4Attack = audio.load(R.raw.audio_tower4_attack);
        audioTower1Death = audio.load(R.raw.audio_tower1_death);
        audioTower2Death = audio.load(R.raw.audio_tower2_death);
        audioTower3Death = audio.load(R.raw.audio_tower3_death);
        audioTower4Death = audio.load(R.raw.audio_tower4_death);
        audioSelect = audio.load(R.raw.audio_select);
        audioFieldSelect = audio.load(R.raw.audio_field_select);
        audioWon = audio.load(R.raw.audio_won);
        audioLost = audio.load(R.raw.audio_lost);
    }

    // game stuff

    private void initLevel(int whichLevel, String whichDifficulty) {
        boolean levelSettings = true;
        boolean levelTextures = true;

        try {
            towerAvailable.clear();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }

        try {
            towerMaxUpdate.clear();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }

        levelSettings = loadLevelSettings(installedMod, whichLevel);
        if (level != whichLevel) { // loads level textures only if not already loaded
            levelTextures = loadLevelTextures(installedMod, whichLevel);
        }

        if (!levelSettings) {
            System.out.println(TAG + "error loading level settings");
        }
        if (!levelTextures) {
            System.out.println(TAG + "error loading level textures");
        }

        if (levelSettings && levelTextures) {
            audio.playTrack();
            level = whichLevel;
            currWave = 0;
            towerMenu = false;
            upgradeMenu = false;
            confirmation = false;
            confirmationX = OUT_OF_BOUNDS;
            confirmationY = OUT_OF_BOUNDS;
            alive = true;
            gameClosePressed = false;
            won = false;

            enemyFieldBlock = towerSize / 3;
            enemyDefaultSize = towerSize / 3;
            towerHalfSize = towerSize / 2;
            marginY = screenHeight - (towerSize * (screenHeight / towerSize));
            enemyDefaultHalfSize = enemyDefaultSize / 2;
            horizTower = screenWidth / towerSize;
            towerShotStep = enemyDefaultSize / 6;
            towerShotsSize = enemyDefaultSize;
            towerShotsHalfSize = towerShotsSize / 2;

            maxVertTower = vertTowers - 1;
            extraScreen = ((vertTowers - (screenHeight / towerSize)) * towerSize) - marginY;
            fdy = extraScreen;
            fieldTower = new char[horizTower][vertTowers];
            buildingTower = new boolean[horizTower][vertTowers];
            buildingTowerTime = new long[horizTower][vertTowers];
            fieldLevel = new char[horizTower][vertTowers];
            towerCurrLife = new int[horizTower][vertTowers];
            lastAttackTower = new long[horizTower][vertTowers];
            fieldTowerShooting = new boolean[horizTower][vertTowers];
            fieldTowerShootingTime = new long[horizTower][vertTowers];
            fieldTowerShootingSprite = new int[horizTower][vertTowers];
            towerTimerOfDeath = new long[horizTower][vertTowers];

            setField();
            currHearts = levelHearts;

            // TODO these two should be calculated depending on the enemies
            towerLifeScale = 5;
            enemyLifeScale = 1;

            towerShotTower = new ArrayList<>();
            towerShotLevel = new ArrayList<>();
            towerShotX = new ArrayList<>();
            towerShotY = new ArrayList<>();
            towerShotEnemyTargeted = new ArrayList<>();
            towerShotSingleDamage = new ArrayList<>();
            towerShotInteractions = new ArrayList<>();
            towerShotAngle = new ArrayList<>();
            towerShotLastSprite = new ArrayList<>();
            towerShotSprite = new ArrayList<>();

            deadEnemyX = new ArrayList<>();
            deadEnemyY = new ArrayList<>();
            deadEnemyChar = new ArrayList<>();
            deadEnemyTime = new ArrayList<>();
            deadEnemySize = new ArrayList<>();

            if (fieldTower[0].length > screenHeight / towerSize) {
                levelStartFieldScroll = true;
            } else {
                fdy = 0;
                levelStartFieldScroll = false;
            }

            levelStartTime = now;

            setNewWave(currWave);
        } else {
            System.out.println(TAG + "error loading level");
            action = "";
            gameState = "menu";
        }
    }

    private void setNewWave(int cw) {
        int tempI;
        waveEnemyX = new int[waveEnemies[cw].length()];
        waveEnemyY = new float[waveEnemies[cw].length()];
        waveEnemyShotY = new int[waveEnemies[cw].length()];
        waveEnemyFullLife = new int[waveEnemies[cw].length()];
        waveEnemyCurrLife = new int[waveEnemies[cw].length()];
        waveEnemySpeed = new float[waveEnemies[cw].length()];
        waveEnemyReach = new int[waveEnemies[cw].length()];
        waveEnemyAttack = new int[waveEnemies[cw].length()];
        waveEnemyLastAttack = new long[waveEnemies[cw].length()];
        waveEnemyAttackInterval = new long[waveEnemies[cw].length()];
        waveEnemySprites = new int[waveEnemies[cw].length()];
        waveEnemyCurrSprite = new int[waveEnemies[cw].length()];
        waveEnemySpriteInterval = new long[waveEnemies[cw].length()];
        waveEnemyLastSprite = new long[waveEnemies[cw].length()];
        waveEnemyHeartWeight = new int[waveEnemies[cw].length()];
        waveEnemyTowerWeakness = new char[waveEnemies[cw].length()];
        waveEnemyLevelWeakness = new char[waveEnemies[cw].length()];
        waveEnemyPayment = new int[waveEnemies[cw].length()];
        waveEnemySize = new float[waveEnemies[cw].length()];
        waveEnemyMoving = new boolean[waveEnemies[cw].length()];

        /*
        // calculates spaceAvailableForEnemies
        int fc = 0;
        for (int xc = 0; xc < horizTower; xc ++) {
            if (fieldTower[xc][0] != BLOCKED1 &&
                    fieldTower[xc][0] != BLOCKED2 &&
                    fieldTower[xc][0] != BLOCKED3 &&
                    fieldTower[xc][0] != BLOCKED4) {
                fc ++;
            }
        }
        int spaceAvailableForEnemies = fc * enemyDefaultSize;*/

        Arrays.fill(waveEnemyX, OUT_OF_BOUNDS);
        float sumOfEnemySizes = 0;
        for (int i = 0; i < waveEnemies[cw].length(); i ++) {
            do {
                waveEnemyX[i] = (int) (Math.random() * screenWidth);
                tempI = waveEnemyX[i] / towerSize;
                if (tempI > horizTower - 1) {
                    tempI = horizTower - 1;
                }
            } while (fieldTower[tempI][0] == BLOCKED1 ||
                    fieldTower[tempI][0] == BLOCKED2 ||
                    fieldTower[tempI][0] == BLOCKED3 ||
                    fieldTower[tempI][0] == BLOCKED4 ||
                    waveEnemyX[i] == OUT_OF_BOUNDS);
            waveEnemyShotY[i] = enemyDefaultHalfSize;
            switch (waveEnemies[cw].charAt(i)) {
                case ENEMY0 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings0[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings0[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings0[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings0[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings0[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings0[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings0[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings0[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings0[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings0[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings0[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings0[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY1 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings1[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings1[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings1[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings1[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings1[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings1[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings1[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings1[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings1[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings1[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings1[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings1[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY2 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings2[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings2[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings2[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings2[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings2[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings2[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings2[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings2[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings2[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings2[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings2[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings2[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY3 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings3[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings3[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings3[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings3[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings3[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings3[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings3[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings3[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings3[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings3[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings3[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings3[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY4 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings4[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings4[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings4[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings4[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings4[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings4[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings4[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings4[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings4[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings4[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings4[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings4[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY5 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings5[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings5[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings5[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings5[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings5[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings5[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings5[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings5[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings5[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings5[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings5[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings5[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY6 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings6[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings6[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings6[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings6[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings6[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings6[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings6[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings6[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings6[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings6[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings6[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings6[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY7 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings7[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings7[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings7[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings7[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings7[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings7[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings7[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings7[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings7[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings7[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings7[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings7[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY8 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings8[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings8[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings8[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings8[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings8[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings8[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings8[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings8[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings8[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings8[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings8[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings8[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
                case ENEMY9 :
                    try {
                        waveEnemyFullLife[i] = Integer.parseInt(enemySettings9[0]);
                    } catch (Exception e) {
                        waveEnemyFullLife[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpeed[i] = Float.parseFloat(enemySettings9[1]);
                    } catch (Exception e) {
                        waveEnemySpeed[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttack[i] = Integer.parseInt(enemySettings9[2]);
                    } catch (Exception e) {
                        waveEnemyAttack[i] = 3;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyAttackInterval[i] = Integer.parseInt(enemySettings9[3]);
                    } catch (Exception e) {
                        waveEnemyAttackInterval[i] = 1000;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySprites[i] = Integer.parseInt(enemySettings9[4]);
                    } catch (Exception e) {
                        waveEnemySprites[i] = 8;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemySpriteInterval[i] = Integer.parseInt(enemySettings9[5]);
                    } catch (Exception e) {
                        waveEnemySpriteInterval[i] = 150;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyHeartWeight[i] = Integer.parseInt(enemySettings9[6]);
                    } catch (Exception e) {
                        waveEnemyHeartWeight[i] = 1;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyPayment[i] = Integer.parseInt(enemySettings9[7]);
                    } catch (Exception e) {
                        waveEnemyPayment[i] = 20;
                        e.printStackTrace();
                    }
                    try {
                        waveEnemyReach[i] = Integer.parseInt(enemySettings9[8]);
                    } catch (Exception e) {
                        waveEnemyReach[i] = 1;
                        e.printStackTrace();
                    }
                    switch (enemySettings9[9]) {
                        case "TOWER1" :
                            waveEnemyTowerWeakness[i] = TOWER1;
                            break;
                        case "TOWER2" :
                            waveEnemyTowerWeakness[i] = TOWER2;
                            break;
                        case "TOWER3" :
                            waveEnemyTowerWeakness[i] = TOWER3;
                            break;
                        case "TOWER4" :
                            waveEnemyTowerWeakness[i] = TOWER4;
                            break;
                    }
                    switch (enemySettings9[10]) {
                        case "A" :
                            waveEnemyLevelWeakness[i] = TLA;
                            break;
                        case "B" :
                            waveEnemyLevelWeakness[i] = TLB;
                            break;
                    }
                    try {
                        waveEnemySize[i] = Float.parseFloat(enemySettings9[11]);
                    } catch (Exception e) {
                        waveEnemySize[i] = 1;
                        e.printStackTrace();
                    }
                    break;
            }
            waveEnemyCurrLife[i] = waveEnemyFullLife[i];
            waveEnemyCurrSprite[i] = (int) Math.floor(Math.random() * waveEnemySprites[i]);
            waveEnemyLastAttack[i] = System.currentTimeMillis();
            waveEnemyLastSprite[i] = System.currentTimeMillis();
            waveEnemyMoving[i] = true;
            sumOfEnemySizes += waveEnemySize[i];
        }
        float averageEnemySize = sumOfEnemySizes / waveEnemies[cw].length() * enemyDefaultSize;
        for (int i = 0; i < waveEnemies[cw].length(); i ++) {
            waveEnemyY[i] = waveStartPoint - (float) (Math.random() + (i * averageEnemySize * 1.2));
        }
        waveStartTime = now;
    }

    private void setField() {
        int fffPos = 0;
        for (int j = 0; j < vertTowers; j ++) {
            for (int i = 0; i < horizTower; i ++) {
                if (fffPos < fieldFromFile.length()) {
                    fieldTower[i][j] = fieldFromFile.charAt(fffPos);
                } else {
                    fieldTower[i][j] = BASE;
                }
                fffPos ++;
                if (j == vertTowers - 1) {
                    fieldTower[i][j] = BASE;
                }
                buildingTower[i][j] = false;
                buildingTowerTime[i][j] = now;
                fieldLevel[i][j] = EMPTY1;
                towerCurrLife[i][j] = OUT_OF_BOUNDS;
                lastAttackTower[i][j] = System.currentTimeMillis();
                fieldTowerShooting[i][j] = false;
                fieldTowerShootingTime[i][j] = System.currentTimeMillis();
                fieldTowerShootingSprite[i][j] = 0;
                towerTimerOfDeath[i][j] = now;
            }
        }
    }

    private void getTowerUpgradeLevel(int ii, int jj) {
        switch (fieldLevel[ii][jj]) {
            case TL1:
                towerUpLevel = 0;
                break;
            case TL2:
                towerUpLevel = 1;
                break;
            case TL3:
                towerUpLevel = 2;
                break;
            case TL4:
                towerUpLevel = 3;
                break;
            case TL5:
                towerUpLevel = 4;
                break;
            case TL6A1:
                towerUpLevel = 5;
                break;
            case TL6A2:
                towerUpLevel = 6;
                break;
            case TL6B1:
                towerUpLevel = 7;
                break;
            case TL6B2:
                towerUpLevel = 8;
                break;
        }
    }

    private void getTowerShootingSprite(int ii, int jj) {
        if (fieldTowerShooting[ii][jj]) {
            switch (currentTower) {
                case TOWER1 :
                    towerShootingSpriteInterval = attackIntervalTower1.get(currentTowerLevel);
                break;
                case TOWER2 :
                    towerShootingSpriteInterval = attackIntervalTower2.get(currentTowerLevel);
                break;
                case TOWER3 :
                    towerShootingSpriteInterval = attackIntervalTower3.get(currentTowerLevel);
                break;
                case TOWER4 :
                    towerShootingSpriteInterval = attackIntervalTower4.get(currentTowerLevel);
                break;
            }
            tempTime = now - fieldTowerShootingTime[ii][jj];
            if (tempTime > towerShootingSpriteInterval) {
                fieldTowerShootingTime[ii][jj] = now;
                fieldTowerShootingSprite[ii][jj] = 0;
                fieldTowerShooting[ii][jj] = false;
            } else if (tempTime > towerShootingSpriteInterval / 4 * 3) {
                fieldTowerShootingSprite[ii][jj] = 2;
            } else if (tempTime > towerShootingSpriteInterval / 2) {
                fieldTowerShootingSprite[ii][jj] = 1;
            } else {
                fieldTowerShootingSprite[ii][jj] = 0;
            }

            towerShootingSprite = fieldTowerShootingSprite[ii][jj];
        } else {
            towerShootingSprite = 0;
        }
    }

    private void getTowerShotSprite(int gtss) {
        int tempShotSprite;
        if (now - towerShotLastSprite.get(gtss) > towerShotSpriteInterval) {
            towerShotLastSprite.set(gtss, now);
            tempShotSprite = towerShotSprite.get(gtss) + 1;
            if (tempShotSprite > numberOfTowerShotSprites) {
                tempShotSprite = 0;
            }
            towerShotSprite.set(gtss, tempShotSprite);
        }
        whichShotSprite = towerShotSprite.get(gtss);
    }

    private void checkTowerShotLevel(char whichTower, char whichLevel) {
        switch (whichTower) {
            case TOWER1 :
                whichShotY = 0;
                break;
            case TOWER2 :
                whichShotY = 1;
                break;
            case TOWER3 :
                whichShotY = 2;
                break;
            case TOWER4 :
                whichShotY = 3;
                break;
        }
        switch (whichLevel) {
            case TL1 :
                whichShotX = 0;
                break;
            case TL2 :
                whichShotX = 1;
                break;
            case TL3 :
                whichShotX = 2;
                break;
            case TL4 :
                whichShotX = 3;
                break;
            case TL5 :
                whichShotX = 4;
                break;
            case TL6A1 :
                whichShotX = 5;
                break;
            case TL6A2 :
                whichShotX = 6;
                break;
            case TL6B1 :
                whichShotX = 7;
                break;
            case TL6B2 :
                whichShotX = 8;
                break;
        }
    }

    private void checkForEnemies(int ii, int jj) {
        int currReach = 0, damageToCause = 0;
        long tempInterval = 0;
        if (fieldTower[ii][jj] != EMPTY1 &&
                fieldTower[ii][jj] != EMPTY2 &&
                fieldTower[ii][jj] != EMPTY3 &&
                fieldTower[ii][jj] != EMPTY4 &&
                fieldTower[ii][jj] != BASE &&
                fieldTower[ii][jj] != BLOCKED1 &&
                fieldTower[ii][jj] != BLOCKED2 &&
                fieldTower[ii][jj] != BLOCKED3 &&
                fieldTower[ii][jj] != BLOCKED4 &&
                fieldTower[ii][jj] != TOWER1_D &&
                fieldTower[ii][jj] != TOWER2_D &&
                fieldTower[ii][jj] != TOWER3_D &&
                fieldTower[ii][jj] != TOWER4_D) {
            switch (fieldTower[ii][jj]) {
                case TOWER1 :
                    tempInterval = attackIntervalTower1.get(fieldLevel[ii][jj]);
                    currReach = reachTower1.get(fieldLevel[ii][jj]);
                    break;
                case TOWER2 :
                    tempInterval = attackIntervalTower2.get(fieldLevel[ii][jj]);
                    currReach = reachTower2.get(fieldLevel[ii][jj]);
                    break;
                case TOWER3 :
                    tempInterval = attackIntervalTower3.get(fieldLevel[ii][jj]);
                    currReach = reachTower3.get(fieldLevel[ii][jj]);
                    break;
                case TOWER4 :
                    tempInterval = attackIntervalTower4.get(fieldLevel[ii][jj]);
                    currReach = reachTower4.get(fieldLevel[ii][jj]);
                    break;
            }
            if (now - lastAttackTower[ii][jj] >= tempInterval) { // if it's time
                lastAttackTower[ii][jj] = now;
                for (int k = 0; k < waveEnemies[currWave].length(); k ++) { // goes through every enemy in the wave
                    if (waveEnemyX[k] >= (ii * towerSize) - (currReach * towerHalfSize) &&
                            waveEnemyX[k] <= (ii * towerSize) + towerSize + (currReach * towerHalfSize) &&
                            waveEnemyY[k] >= (jj * towerSize) - (currReach * towerHalfSize) &&
                            waveEnemyY[k] <= (jj * towerSize) + towerSize + (currReach * towerHalfSize)) { // if at reach

                        switch (fieldTower[ii][jj]) { // causes the right damage to enemy
                            case TOWER1:
                                audio.play(audioTower1Attack, false, now);
                                if (waveEnemyTowerWeakness[k] == TOWER1 && (
                                        (waveEnemyLevelWeakness[k] == TLA && (fieldLevel[ii][jj] == TL6A1 && fieldLevel[ii][jj] == TL6A2)) ||
                                        (waveEnemyLevelWeakness[k] == TLB && (fieldLevel[ii][jj] == TL6B1 && fieldLevel[ii][jj] == TL6B2))
                                )) {
                                    damageToCause = (int) (attackTower1.get(fieldLevel[ii][jj]) * weaknessFactor);
                                } else {
                                    damageToCause = attackTower1.get(fieldLevel[ii][jj]);
                                }
                                break;
                            case TOWER2:
                                audio.play(audioTower2Attack, false, now);
                                if (waveEnemyTowerWeakness[k] == TOWER2 && (
                                        (waveEnemyLevelWeakness[k] == TLA && (fieldLevel[ii][jj] == TL6A1 && fieldLevel[ii][jj] == TL6A2)) ||
                                                (waveEnemyLevelWeakness[k] == TLB && (fieldLevel[ii][jj] == TL6B1 && fieldLevel[ii][jj] == TL6B2))
                                )) {
                                    damageToCause = (int) (attackTower2.get(fieldLevel[ii][jj]) * weaknessFactor);
                                } else {
                                    damageToCause = attackTower2.get(fieldLevel[ii][jj]);
                                }
                                break;
                            case TOWER3:
                                audio.play(audioTower3Attack, false, now);
                                if (waveEnemyTowerWeakness[k] == TOWER3 && (
                                        (waveEnemyLevelWeakness[k] == TLA && (fieldLevel[ii][jj] == TL6A1 && fieldLevel[ii][jj] == TL6A2)) ||
                                                (waveEnemyLevelWeakness[k] == TLB && (fieldLevel[ii][jj] == TL6B1 && fieldLevel[ii][jj] == TL6B2))
                                )) {
                                    damageToCause = (int) (attackTower3.get(fieldLevel[ii][jj]) * weaknessFactor);
                                } else {
                                    damageToCause = attackTower3.get(fieldLevel[ii][jj]);
                                }
                                break;
                            case TOWER4:
                                audio.play(audioTower4Attack, false, now);
                                if (waveEnemyTowerWeakness[k] == TOWER4 && (
                                        (waveEnemyLevelWeakness[k] == TLA && (fieldLevel[ii][jj] == TL6A1 && fieldLevel[ii][jj] == TL6A2)) ||
                                                (waveEnemyLevelWeakness[k] == TLB && (fieldLevel[ii][jj] == TL6B1 && fieldLevel[ii][jj] == TL6B2))
                                )) {
                                    damageToCause = (int) (attackTower4.get(fieldLevel[ii][jj]) * weaknessFactor);
                                } else {
                                    damageToCause = attackTower4.get(fieldLevel[ii][jj]);
                                }
                                break;
                        }

                        createTowerShot(fieldTower[ii][jj], fieldLevel[ii][jj], (ii * towerSize) + towerHalfSize, jj * towerSize, damageToCause, k);
                        k = waveEnemies[currWave].length(); // end the loop
                    }
                }
            }
        }
    }

    private void createTowerShot(char whichTower, char whichLevel, int x1, int y1, int damage, int whichEnemy) {
        int x2 = waveEnemyX[whichEnemy] + enemyDefaultHalfSize;
        int y2;
        if (waveEnemyMoving[whichEnemy]) {
            y2 = (int)(waveEnemyY[whichEnemy] + enemyDefaultHalfSize + waveEnemySpeed[whichEnemy]);
        } else {
            y2 = (int)(waveEnemyY[whichEnemy] + enemyDefaultHalfSize);
        }
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int towerToTargetDistance = (int) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        towerShotTower.add(whichTower);
        towerShotLevel.add(whichLevel);
        towerShotX.add(x1);
        towerShotY.add(y1);
        towerShotAngle.add(angle);
        towerShotEnemyTargeted.add(whichEnemy);
        towerShotSingleDamage.add(damage);
        towerShotInteractions.add(towerToTargetDistance / towerShotStep);
        towerShotLastSprite.add(now);
        towerShotSprite.add(0);

        fieldTowerShooting[x1 / towerSize][y1 / towerSize] = true;
        fieldTowerShootingTime[x1 / towerSize][y1 / towerSize] = now;
        fieldTowerShootingSprite[x1 / towerSize][y1 / towerSize] = 1;
    }

    private void removeEnemy(int ke, boolean dead) {
        if (dead) {
            deadEnemyX.add(waveEnemyX[ke]);
            deadEnemyY.add((int)(waveEnemyY[ke]));
            deadEnemySize.add(waveEnemySize[ke]);
            switch (waveEnemies[currWave].charAt(ke)) {
                case ENEMY0:
                    audio.play(audioEnemy0Death, false, now);
                    deadEnemyChar.add(ENEMY0_D);
                    break;
                case ENEMY1:
                    audio.play(audioEnemy1Death, false, now);
                    deadEnemyChar.add(ENEMY1_D);
                    break;
                case ENEMY2:
                    audio.play(audioEnemy2Death, false, now);
                    deadEnemyChar.add(ENEMY2_D);
                    break;
                case ENEMY3:
                    audio.play(audioEnemy3Death, false, now);
                    deadEnemyChar.add(ENEMY3_D);
                    break;
                case ENEMY4:
                    audio.play(audioEnemy4Death, false, now);
                    deadEnemyChar.add(ENEMY4_D);
                    break;
                case ENEMY5:
                    audio.play(audioEnemy5Death, false, now);
                    deadEnemyChar.add(ENEMY5_D);
                    break;
                case ENEMY6:
                    audio.play(audioEnemy6Death, false, now);
                    deadEnemyChar.add(ENEMY6_D);
                    break;
                case ENEMY7:
                    audio.play(audioEnemy7Death, false, now);
                    deadEnemyChar.add(ENEMY7_D);
                    break;
                case ENEMY8:
                    audio.play(audioEnemy8Death, false, now);
                    deadEnemyChar.add(ENEMY8_D);
                    break;
                case ENEMY9:
                    audio.play(audioEnemy9Death, false, now);
                    deadEnemyChar.add(ENEMY9_D);
                    break;
            }
            deadEnemyTime.add(now);
        }
        waveEnemyX[ke] = OUT_OF_BOUNDS;
    }

    private void payUpgrade() {
        switch (currentTower) {
            case TOWER1 :
                levelCoins -= costTower1.get(currentTowerLevel);
                break;
            case TOWER2 :
                levelCoins -= costTower2.get(currentTowerLevel);
                break;
            case TOWER3 :
                levelCoins -= costTower3.get(currentTowerLevel);
                break;
            case TOWER4 :
                levelCoins -= costTower4.get(currentTowerLevel);
                break;
        }
    }

    private void upgradeTowerLife() {
        switch (currentTower) {
            case TOWER1 :
                towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower1.get(currentTowerLevel);
                break;
            case TOWER2 :
                towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower2.get(currentTowerLevel);
                break;
            case TOWER3 :
                towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower3.get(currentTowerLevel);
                break;
            case TOWER4 :
                towerCurrLife[fieldSelectionX][fieldSelectionY] = fullLifeTower4.get(currentTowerLevel);
                break;
        }
    }

    private boolean hasMoneyForUpgrade() {
        boolean hasMoney = false;
        switch (currentTower) {
            case TOWER1 :
                if (levelCoins >= costTower1.get(currentTowerLevel)) {
                    hasMoney = true;
                }
                break;
            case TOWER2 :
                if (levelCoins >= costTower2.get(currentTowerLevel)) {
                    hasMoney = true;
                }
                break;
            case TOWER3 :
                if (levelCoins >= costTower3.get(currentTowerLevel)) {
                    hasMoney = true;
                }
                break;
            case TOWER4 :
                if (levelCoins >= costTower4.get(currentTowerLevel)) {
                    hasMoney = true;
                }
                break;
        }
        return hasMoney;
    }

    private void checkFieldTouch() {
        oldFieldSelectionX = fieldSelectionX;
        oldFieldSelectionY = fieldSelectionY;
        fieldSelectionX = touchX / towerSize;
        fieldSelectionY = (touchY + fdy) / towerSize;
        if (fieldSelectionY >= vertTowers) {
            resetTowerMark();
        } else {
            currentTower = fieldTower[fieldSelectionX][fieldSelectionY];
            currentTowerLevel = fieldLevel[fieldSelectionX][fieldSelectionY];

            if ((fieldSelectionY <= minVertTower ||
                    fieldSelectionY >= maxVertTower) ||
                    (fieldSelectionX == oldFieldSelectionX &&
                            fieldSelectionY == oldFieldSelectionY) || currentTower == BLOCKED1 || currentTower == BLOCKED2 || currentTower == BLOCKED3 || currentTower == BLOCKED4) {
                audio.play(audioFieldSelect, false, now);
                resetTowerMark();
                towerMenu = false;
                upgradeMenu = false;
            } else if (currentTower == EMPTY1 ||
                    currentTower == EMPTY2 ||
                    currentTower == EMPTY3 ||
                    currentTower == EMPTY4 ||
                    currentTower == TOWER1_D ||
                    currentTower == TOWER2_D ||
                    currentTower == TOWER3_D ||
                    currentTower == TOWER4_D) {
                audio.play(audioFieldSelect, false, now);
                towerMenu = true; // mark in the field where touched
                confirmation = false;
                confirmationX = OUT_OF_BOUNDS;
                confirmationY = OUT_OF_BOUNDS;
            } else if (currentTower != BASE && currentTower != BLOCKED1 && currentTower != BLOCKED2 && currentTower != BLOCKED3 && currentTower != BLOCKED4) {
                audio.play(audioFieldSelect, false, now);
                towerMenu = false;
                upgradeMenu = true; // the place has already a tower
                confirmation = false;
                confirmationX = OUT_OF_BOUNDS;
                confirmationY = OUT_OF_BOUNDS;
                checkTowerType();
                checkTowerLevel();
            }
        }
    }

    private void destroyTower(int tmx, int tmy) {
        switch(fieldTower[tmx][tmy]) {
            case TOWER1 :
                audio.play(audioTower1Death, true, now);
                fieldTower[tmx][tmy] = TOWER1_D;
                break;
            case TOWER2 :
                audio.play(audioTower2Death, true, now);
                fieldTower[tmx][tmy] = TOWER2_D;
                break;
            case TOWER3 :
                audio.play(audioTower3Death, true, now);
                fieldTower[tmx][tmy] = TOWER3_D;
                break;
            case TOWER4 :
                audio.play(audioTower4Death, true, now);
                fieldTower[tmx][tmy] = TOWER4_D;
                break;
        }
        towerTimerOfDeath[tmx][tmy] = now;
        fieldLevel[tmx][tmy] = EMPTY1;
        towerCurrLife[tmx][tmy] = OUT_OF_BOUNDS;
        if (fieldSelectionX == tmx && fieldSelectionY == tmy) {
            if (towerMenu) towerMenu = false;
            if (upgradeMenu) upgradeMenu = false;
            if (confirmation) confirmation = false;
        }

    }

    private void resetTowerMark() {
        fieldSelectionX = OUT_OF_BOUNDS;
        fieldSelectionY = OUT_OF_BOUNDS;
        resetConfirmation();
    }

    private void resetConfirmation() {
        confirmation = false;
        confirmationX = OUT_OF_BOUNDS;
        confirmationY = OUT_OF_BOUNDS;
    }

    private void checkTowerType() {
        switch (currentTower) {
            case TOWER1 :
                upMenuX = 0;
                break;
            case TOWER2 :
                upMenuX = 1;
                break;
            case TOWER3 :
                upMenuX = 2;
                break;
            case TOWER4 :
                upMenuX = 3;
                break;
        }
    }

    private void checkTowerLevel() {
        switch (currentTowerLevel) {
            case TL1 :
                upMenuY = 0;
                upMenuCostY = (screenHeight / 2) + (towerMenuHalfHeight / 24 * 7);
                break;
            case TL2 :
                upMenuY = 1;
                upMenuCostY = (screenHeight / 2) + (towerMenuHalfHeight / 24 * 7);
                break;
            case TL3 :
                upMenuY = 2;
                upMenuCostY = (screenHeight / 2) + (towerMenuHalfHeight / 24 * 7);
                break;
            case TL4 :
                upMenuY = 3;
                upMenuCostY = (screenHeight / 2) + (towerMenuHalfHeight / 24 * 7);
                break;
            case TL5 :
                upMenuY = 4;
                upMenuCostY = (screenHeight / 2) - (towerMenuHalfHeight / 30 * 22);
                break;
            case TL6A1 :
                upMenuY = 5;
                upMenuCostY = (screenHeight / 2) - (towerMenuHalfHeight / 30 * 22);
                break;
            case TL6A2 :
                upMenuY = 6;
                upMenuCostY = (screenHeight / 2) - (towerMenuHalfHeight / 30 * 22);
                break;
            case TL6B1 :
                upMenuY = 7;
                upMenuCostY = (screenHeight / 2) - (towerMenuHalfHeight / 5);
                break;
            case TL6B2 :
                upMenuY = 8;
                upMenuCostY = (screenHeight / 2) - (towerMenuHalfHeight / 5);
                break;
        }
    }

    // drawings

    private void drawMenu() {
        drawAppTextures(MENU_BG, 0, 0, null);
        switch (updateStatus) {
            case UPD_NOT_STARTED :
                if (!exitAppButtonPressed) {
                    drawAppTextures(CLOSE_BUTTON, screenWidth - (menuButtonHalfSize * 2), menuButtonHalfSize * 2, null);
                } else {
                    drawDialog("Close the app now?", "Yes", "No");
                }
                break;
            case UPD_DONE :
                if (!exitAppButtonPressed) {
                    drawAppTextures(PLAY_BUTTON, screenWidth / 2, screenHeight / 2, null);
                    drawAppTextures(CLOSE_BUTTON, screenWidth - (menuButtonHalfSize * 2), menuButtonHalfSize * 2, null);
                } else {
                    drawDialog("Close the app now?", "Yes", "No");
                }
                break;
            case UPD_CHECKING_GAME_UPDS :
                drawDialog("Checking game updates...", "", "");
                checkForTimedOutConnection();
                break;
            case UPD_READY_TO_CHECK_LEVELS :
                ListRemoteLevelsTask listRemoteLevelsTask = new ListRemoteLevelsTask();
                listRemoteLevelsTask.execute();
                serverTimeOutStart = now;
                updateStatus = UPD_CHECKING_LEVELS;
                break;
            case UPD_CHECKING_LEVELS :
                drawDialog("Checking levels updates...", "", "");
                checkForTimedOutConnection();
                break;
            case UPD_READY_TO_DOWNLOAD :
                drawDialog("Ready do download", "", "");
                if (filesToDownload != null) {
                    numberOfFilesToUpdate = filesToDownload.size();
                    if (numberOfFilesToUpdate > 0) deleteLevelsScreenSettings("default");
                    for (String each : filesToDownload) {
                        System.out.println(TAG + "Downloading " + each + "...");
                        DownloadFileTask downloadFileTask = new DownloadFileTask();
                        downloadFileTask.execute(each);
                    }
                    updateStatus = UPD_DOWNLOADING;
                    float dt = ((now - serverTimeOutStart) / 100) / 10f;
                    System.out.println(TAG + "Server query took " + dt + "s");
                    serverTimeOutStart = now;
                } else {
                    updateStatus = UPD_READY_TO_CHECK_LOCAL;
                }
                break;
            case UPD_DOWNLOADING :
                if (numberOfFilesToUpdate == 0) {
                    updateStatus = UPD_READY_TO_CHECK_LOCAL;
                } else if (numberOfFilesToUpdate > 1) {
                    drawDialog("Downloading " + numberOfFilesToUpdate + " updates...", "", "");
                } else {
                    drawDialog("Downloading " + numberOfFilesToUpdate + " update...", "", "");
                }
                checkForTimedOutConnection();
                break;
            case UPD_READY_TO_CHECK_LOCAL :
                localCheckingTime = now;
                drawDialog("Checking local files...", "", "");
                updateStatus = UPD_CHECKING_LOCAL;
                break;
            case UPD_CHECKING_LOCAL :
                drawDialog("Checking local files...", "", "");
                if (filesToDownload != null) {
                    if (filesToDownload.size() == 0) {
                        if (now - localCheckingTime > 1000) {
                            checkForTowersSettingsFile();
                            loadTowersSettings();
                            if (setLevelsScreen(installedMod)) {
                                updateStatus = UPD_DONE;
                            } else {
                                updateStatus = UPD_LOCAL_MISSING;
                            }
                        }
                    } else {
                        if (now - localCheckingTime > 3000) {
                            checkForTowersSettingsFile();
                            loadTowersSettings();
                            if (filesToDownload.contains("app_textures.png")) loadAppTextures();
                            if (setLevelsScreen(installedMod)) {
                                updateStatus = UPD_DONE;
                            } else {
                                updateStatus = UPD_LOCAL_MISSING;
                            }
                        }
                    }
                } else if (now - localCheckingTime > 1000) {
                    if (setLevelsScreen(installedMod)) {
                        updateStatus = UPD_DONE;
                    } else {
                        updateStatus = UPD_LOCAL_MISSING;
                    }
                }
                break;
            case UPD_LOCAL_MISSING :
                drawDialog("There are no levels installed", "Exit", "");
                break;
            case UPD_ERROR_OFFLINE :
                drawDialog("The app needs to look for updates.\nPlease, check your connection.", "Retry", "Cancel");
                break;
            case UPD_ERROR_OTHER :
                drawDialog("Failed.", "Continue", "");
                break;
        }
    }

    private void drawLevels() {
        drawAppTextures(LEVEL_BG, 0, 0, null);

        if (levelButtonPressed == 0 && levelButtonPressed <= highestLevel) {
            int lN = 1;
            for (int dly = vertLevels; dly > 0; dly --) {
                if (levelAvailable[lN - 1]) {
                    if (lN <= highestLevel) {
                        currentRank = achievedRanks[lN - 1];
                        drawAppTextures(LEVEL_BUTTON, screenWidth / 2, (dly * levelButtonArea) + levelButtonHalfArea - lsy, null);
                        drawText(lN + "", 50, screenWidth / 2, (dly * levelButtonArea) + levelButtonHalfArea - lsy + (menuButtonHalfSize / 2), 0xFFFFFFFF, 0xFF2F4F4E, 0, Paint.Align.CENTER);
                    } else {
                        drawAppTextures(LEVEL_BLOCKED_BUTTON, screenWidth / 2, (dly * levelButtonArea) + levelButtonHalfArea - lsy, null);
                    }
                } else {
                    drawAppTextures(LEVEL_COMING_SOON, screenWidth / 2, (dly * levelButtonArea) + levelButtonHalfArea - lsy, null);
                    drawText("Coming", 24, screenWidth / 2, (dly * levelButtonArea) + levelButtonHalfArea - (menuButtonHalfSize / 3) - lsy, 0xDDFFFFFF, 0xFFFFFFFF, 0, Paint.Align.CENTER);
                    drawText("soon", 24, screenWidth / 2, (dly * levelButtonArea) + levelButtonHalfArea - lsy, 0xDDFFFFFF, 0xFFFFFFFF, 0, Paint.Align.CENTER);
                }
                lN ++;
            }
            drawAppTextures(CLOSE_BUTTON, screenWidth - (menuButtonHalfSize * 2), menuButtonHalfSize * 2, null);
        } else {
            drawDialog(levelTitle[levelButtonPressed - 1] + "\n\n" + levelDescription[levelButtonPressed - 1], "Play", "Cancel");
        }
    }

    private void drawGame() {
        drawFieldAndTowers();
        if (!gameClosePressed && !won) {
            drawWave();
        } else if (!gameClosePressed) {
            drawDeadEnemies();
        }
        drawGameUI();
    }

    private void drawFieldAndTowers() {
        long tt;
        // level start field scroll
        if (levelStartFieldScroll && now - levelStartTime > levelStartFieldScrollDelay) {
            fdy -= enemyDefaultHalfSize / 3;
            if (fdy < 0) {
                fdy = 0;
                levelStartFieldScroll = false;
            }
        }

        for (int j = 0; j < fieldTower[0].length; j ++) {
            for (int i = 0; i < fieldTower.length; i ++) {
                getTowerUpgradeLevel(i, j);
                getTowerShootingSprite(i, j);
                if (buildingTower[i][j]) {
                    drawSprite(BUILDING, i * towerSize, (j * towerSize) - fdy, null);
                    drawBuildingBar(i, j);
                    if (now - buildingTowerTime[i][j] > buildingTowerDuration) {
                        buildingTower[i][j] = false;
                    }
                } else {
                    if (fieldTower[i][j] == TOWER1_D ||
                            fieldTower[i][j] == TOWER2_D ||
                            fieldTower[i][j] == TOWER3_D ||
                            fieldTower[i][j] == TOWER4_D) {
                        tt = now - towerTimerOfDeath[i][j];
                        if (tt >= deadTowerTimeLimit) { // tower death ended, cleans field
                            fieldTower[i][j] = EMPTY1;
                            drawSprite(fieldTower[i][j], i * towerSize, (j * towerSize) - fdy, null);
                        } else { // fade dead tower
                            int dta = 255 - (int)((float)tt / deadTowerTimeLimit * 255);
                            fieldTowerAlpha.setAlpha(dta);
                            drawSprite(EMPTY1, i * towerSize, (j * towerSize) - fdy, null);
                            drawSprite(fieldTower[i][j], i * towerSize, (j * towerSize) - fdy, fieldTowerAlpha);
                        }
                    } else { // draws field
                        drawSprite(fieldTower[i][j], i * towerSize, (j * towerSize) - fdy, null);
                    }
                    drawTowerLifeBar(i, j);
                    if (!gameClosePressed) checkForEnemies(i, j);
                }
            }
        }

        if (fieldTower[0].length < screenHeight / towerSize) {
            for (int j = fieldTower[0].length; j <= screenHeight / towerSize; j ++) {
                for (int i = 0; i < fieldTower.length; i ++) {
                    drawSprite(BASE, i * towerSize, (j * towerSize) - fdy, null);
                }
            }
        }

        if (!gameClosePressed) {
            drawTowerShots();
            if (towerMenu || upgradeMenu)
                drawSprite(FIELD_SELECTION, fieldSelectionX * towerSize, fieldSelectionY * towerSize - fdy, null);
        }
    }

    private void drawBuildingBar(int ii, int jj) {
        fullLife = (int)buildingTowerDuration;
        currLife = (int)(now - buildingTowerTime[ii][jj]);
        halfLifeBarInPX = towerHalfSize / 3 * 2;
        lifeInPX = (int) (halfLifeBarInPX * 2 * ((float) currLife / (float) fullLife));
        drawSprite(LIFE_BG, (ii * towerSize) + towerHalfSize, (jj * towerSize) - fdy + towerSize - lifeBarHeight, null);
        drawSprite(LIFE_TOWER, (ii * towerSize) + towerHalfSize, (jj * towerSize) - fdy + towerSize - lifeBarHeight, null);
    }

    private void drawTowerLifeBar(int ii, int jj) {
        switch (fieldTower[ii][jj]) {
            case TOWER1 :
                fullLife = fullLifeTower1.get(fieldLevel[ii][jj]);
                break;
            case TOWER2 :
                fullLife = fullLifeTower2.get(fieldLevel[ii][jj]);
                break;
            case TOWER3 :
                fullLife = fullLifeTower3.get(fieldLevel[ii][jj]);
                break;
            case TOWER4 :
                fullLife = fullLifeTower4.get(fieldLevel[ii][jj]);
                break;
            case EMPTY1 :
            case EMPTY2 :
            case EMPTY3 :
            case EMPTY4 :
            case BLOCKED1 :
            case BLOCKED2 :
            case BLOCKED3 :
            case BLOCKED4 :
            case TOWER1_D :
            case TOWER2_D :
            case TOWER3_D :
            case TOWER4_D :
                fullLife = OUT_OF_BOUNDS;
                break;
        }
        if (fullLife != OUT_OF_BOUNDS) {
            currLife = towerCurrLife[ii][jj];
            halfLifeBarInPX = fullLife / towerLifeScale;
            lifeInPX = (int) (halfLifeBarInPX * 2 * ((float) currLife / (float) fullLife));
            drawSprite(LIFE_BG, (ii * towerSize) + towerHalfSize, (jj * towerSize) - fdy + towerSize - lifeBarHeight, null);
            drawSprite(LIFE_TOWER, (ii * towerSize) + towerHalfSize, (jj * towerSize) - fdy + towerSize - lifeBarHeight, null);
        }
    }

    private void drawTowerShots() {
        for (int dts = 0; dts < towerShotInteractions.size(); dts ++) { // draws tower shots
            towerShotX.set(dts, (int) (towerShotX.get(dts) + Math.cos(towerShotAngle.get(dts)) * towerShotStep));
            towerShotY.set(dts, (int) (towerShotY.get(dts) + Math.sin(towerShotAngle.get(dts)) * towerShotStep));
            towerShotInteractions.set(dts, towerShotInteractions.get(dts) - 1);
            checkTowerShotLevel(towerShotTower.get(dts), towerShotLevel.get(dts));
            getTowerShotSprite(dts);
            drawSprite(TOWER_SHOTS, towerShotX.get(dts), towerShotY.get(dts) - fdy, null);
        }

        // check if reached destination
        int dts = 0;
        while (dts < towerShotInteractions.size() && dts < waveEnemyCurrLife.length) {
            if (towerShotInteractions.get(dts) < 1) {
                try {
                    waveEnemyCurrLife[towerShotEnemyTargeted.get(dts)] -= towerShotSingleDamage.get(dts); // causes damage to enemy
                    if (waveEnemyCurrLife[towerShotEnemyTargeted.get(dts)] <= 0 &&
                            waveEnemyX[towerShotEnemyTargeted.get(dts)] != OUT_OF_BOUNDS) { // kills enemy
                        levelCoins += waveEnemyPayment[towerShotEnemyTargeted.get(dts)];
                        removeEnemy(towerShotEnemyTargeted.get(dts), true);
                    }
                    towerShotTower.remove(dts);
                    towerShotLevel.remove(dts);
                    towerShotX.remove(dts);
                    towerShotY.remove(dts);
                    towerShotAngle.remove(dts);
                    towerShotEnemyTargeted.remove(dts);
                    towerShotSingleDamage.remove(dts);
                    towerShotInteractions.remove(dts);
                    towerShotLastSprite.remove(dts);
                    towerShotSprite.remove(dts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dts ++;
        }
    }

    private void drawWave() {
        drawDeadEnemies();
        if (now - waveStartTime > waveStartDelay) {
            int waveCharsLeft = 0;
            for (int i = 0; i < waveEnemies[currWave].length(); i ++) { // goes through every char of the wave
                if (waveEnemyX[i] != OUT_OF_BOUNDS) { // checks if needs to be drawn
                    waveCharsLeft ++;
                    spriteNumber = waveEnemyCurrSprite[i]; // gets which sprite should be drawn
                    if (waveEnemyMoving[i]) {
                        atX = 0;
                    } else {
                        atX = 400;
                    }
                    enemyCurrentSize = (int)(waveEnemySize[i] * enemyDefaultSize);
                    enemyCurrentHalfSize = enemyCurrentSize / 2;
                    enemyCurrentLifeBarDistance = enemyCurrentSize + (enemyCurrentSize / 4);
                    drawSprite(waveEnemies[currWave].charAt(i), waveEnemyX[i], (int)(waveEnemyY[i] - fdy), null);

                    // changes enemy sprite
                    if (now - waveEnemyLastSprite[i] >= waveEnemySpriteInterval[i]) {
                        waveEnemyLastSprite[i] = now;
                        spriteNumber ++;
                        if (spriteNumber == waveEnemySprites[i]) {
                            spriteNumber = 0;
                        }
                        waveEnemyCurrSprite[i] = spriteNumber;
                    }

                    // enemies life bar
                    halfLifeBarInPX = waveEnemyFullLife[i] / enemyLifeScale;
                    lifeInPX = (int) (halfLifeBarInPX * 2 * ((float) waveEnemyCurrLife[i] / (float) waveEnemyFullLife[i]));
                    drawSprite(LIFE_BG, waveEnemyX[i], (int)(waveEnemyY[i] - enemyCurrentLifeBarDistance - fdy), null);
                    drawSprite(LIFE_ENEMY, waveEnemyX[i], (int)(waveEnemyY[i] - enemyCurrentLifeBarDistance - fdy), null);

                    // check if can move forward
                    int tX;
                    float tY;
                    tX = waveEnemyX[i] / towerSize;
                    tY = (waveEnemyY[i] + waveEnemyReach[i]) / towerSize;
                    if (tY >= minVertTower && tY < maxVertTower) {
                        if (fieldTower[tX][(int)tY] == EMPTY1 ||
                                fieldTower[tX][(int)tY] == EMPTY2 ||
                                fieldTower[tX][(int)tY] == EMPTY3 ||
                                fieldTower[tX][(int)tY] == EMPTY4 ||
                                fieldTower[tX][(int)tY] == BASE ||
                                fieldTower[tX][(int)tY] == TOWER1_D ||
                                fieldTower[tX][(int)tY] == TOWER2_D ||
                                fieldTower[tX][(int)tY] == TOWER3_D ||
                                fieldTower[tX][(int)tY] == TOWER4_D) {
                            waveEnemyY[i] = waveEnemyY[i] + waveEnemySpeed[i]; // move forward
                            waveEnemyMoving[i] = true;
                        } else {
                            // move enemy shot
                            if (waveEnemyShotY[i] > 0 && waveEnemyShotY[i] < towerHalfSize) {
                                switch (waveEnemies[currWave].charAt(i)) {
                                    case ENEMY0 :
                                        drawSprite(ENEMY0_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY1 :
                                        drawSprite(ENEMY1_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY2 :
                                        drawSprite(ENEMY2_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY3 :
                                        drawSprite(ENEMY3_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY4 :
                                        drawSprite(ENEMY4_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY5 :
                                        drawSprite(ENEMY5_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY6 :
                                        drawSprite(ENEMY6_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY7 :
                                        drawSprite(ENEMY7_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY8 :
                                        drawSprite(ENEMY8_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                    case ENEMY9 :
                                        drawSprite(ENEMY9_SHOT, waveEnemyX[i], (int)(waveEnemyY[i] + waveEnemyShotY[i] - fdy), null);
                                        break;
                                }
                                waveEnemyShotY[i] = waveEnemyShotY[i] + (int)(waveEnemySpeed[i] * 2);
                            } else {
                                waveEnemyShotY[i] = 0;
                            }
                            // if time, start attack to tower
                            if (now - waveEnemyLastAttack[i] >= waveEnemyAttackInterval[i]) {
                                waveEnemyShotY[i] = (int)(waveEnemyShotY[i] + waveEnemySpeed[i]);
                                switch (waveEnemies[currWave].charAt(i)) {
                                    case ENEMY0 :
                                        audio.play(audioEnemy0Attack, false, now);
                                        break;
                                    case ENEMY1 :
                                        audio.play(audioEnemy1Attack, false, now);
                                        break;
                                    case ENEMY2 :
                                        audio.play(audioEnemy2Attack, false, now);
                                        break;
                                    case ENEMY3 :
                                        audio.play(audioEnemy3Attack, false, now);
                                        break;
                                    case ENEMY4 :
                                        audio.play(audioEnemy4Attack, false, now);
                                        break;
                                    case ENEMY5 :
                                        audio.play(audioEnemy5Attack, false, now);
                                        break;
                                    case ENEMY6 :
                                        audio.play(audioEnemy6Attack, false, now);
                                        break;
                                    case ENEMY7 :
                                        audio.play(audioEnemy7Attack, false, now);
                                        break;
                                    case ENEMY8 :
                                        audio.play(audioEnemy8Attack, false, now);
                                        break;
                                    case ENEMY9 :
                                        audio.play(audioEnemy9Attack, false, now);
                                        break;
                                }
                                waveEnemyLastAttack[i] = now;
                                towerCurrLife[tX][(int)tY] -= waveEnemyAttack[i];
                                // check if tower still alive
                                if (towerCurrLife[tX][(int)tY] <= 0) {
                                    destroyTower(tX, (int)tY);
                                }
                            }
                            waveEnemyMoving[i] = false;
                        }
                    } else {
                        waveEnemyY[i] = waveEnemyY[i] + waveEnemySpeed[i]; // move forward
                    }

                    if (waveEnemyY[i] >= (vertTowers * towerSize) + enemyFieldBlock) { // got to the goal
                        removeEnemy(i, false);
                        currHearts -= waveEnemyHeartWeight[i];
                        if (currHearts <= 0 && alive) {
                            alive = false;
                            audio.play(audioLost, true, now);
                        }
                    }
                }
            }

            if (waveCharsLeft == 0) { // next wave
                if (currWave + 1 < waves) {
                    currWave ++;
                    setNewWave(currWave);
                } else {
                    winTime = now;
                    won = true;
                    audio.play(audioWon, true, now);

                    int tempRank;
                    if (currHearts == levelHearts) {
                        tempRank = 3;
                    } else if (currHearts >= levelHearts - 2) {
                        tempRank = 2;
                    } else if (currHearts >= levelHearts - 4) {
                        tempRank = 1;
                    } else {
                        tempRank = 0;
                    }
                    if (tempRank > achievedRanks[level - 1]) achievedRanks[level - 1] = tempRank;

                    saveAchievedRanks(installedMod);

                    highestLevel ++;
                    if (highestLevel > vertLevels) {
                        highestLevel = vertLevels;
                    } else {
                        editor = sharedPref.edit();
                        editor.putInt("highestLevel", highestLevel);
                        editor.apply();
                    }

                    readAchievedRanks(installedMod);
                }
            }
        }
    }

    private void drawDeadEnemies() {
        int des = 0;
        long et;
        while (des < deadEnemyChar.size()) {
            et = now - deadEnemyTime.get(des);
            if (et > deadEnemyTimeLimit) {
                deadEnemyChar.remove(des);
                deadEnemyTime.remove(des);
                deadEnemyX.remove(des);
                deadEnemyY.remove(des);
                deadEnemySize.remove(des);
            } else {
                int dea = 255 - (int)((float)et / deadEnemyTimeLimit * 255);
                deadEnemyAlpha.setAlpha(dea);
                enemyCurrentSize = (int)(deadEnemySize.get(des) * enemyDefaultSize);
                enemyCurrentHalfSize = enemyCurrentSize / 2;
                drawSprite(deadEnemyChar.get(des), deadEnemyX.get(des), deadEnemyY.get(des) - fdy, deadEnemyAlpha);
                des ++;
            }
        }
    }

    private void drawGameUI() {
        if (alive && !gameClosePressed && !won) {
            if (towerMenu) {
                drawSprite(TOWER_MENU, 0, screenHeight / 2, null);

                // unavailable tower or price
                if (!towerAvailable.get(TOWER1)) {
                    drawSprite(TOWER_UNAVAILABLE, 0, (screenHeight / 2) - towerMenuHalfHeight, null);
                } else {
                    drawText(costTower1.get(EMPTY1) + "", 32, towerMenuWidth / 2, (screenHeight / 2) - (towerMenuHalfHeight / 30 * 22), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                }
                if (!towerAvailable.get(TOWER2)) {
                    drawSprite(TOWER_UNAVAILABLE, 0, (screenHeight / 2) - (towerMenuHalfHeight / 2), null);
                } else {
                    drawText(costTower2.get(EMPTY1) + "", 32, towerMenuWidth / 2, (screenHeight / 2) - (towerMenuHalfHeight / 5), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                }
                if (!towerAvailable.get(TOWER3)) {
                    drawSprite(TOWER_UNAVAILABLE, 0, (screenHeight / 2), null);
                } else {
                    drawText(costTower3.get(EMPTY1) + "", 32, towerMenuWidth / 2, (screenHeight / 2) + (towerMenuHalfHeight / 24 * 7), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                }
                if (!towerAvailable.get(TOWER4)) {
                    drawSprite(TOWER_UNAVAILABLE, 0, (screenHeight / 2) + (towerMenuHalfHeight / 2), null);
                } else {
                    drawText(costTower4.get(EMPTY1) + "", 32, towerMenuWidth / 2, (screenHeight / 2) + (towerMenuHalfHeight / 30 * 25), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                }
                if (confirmation) drawSprite(CONFIRMATION, confirmationX, confirmationY, null);
            } else if (upgradeMenu) {
                drawSprite(UPGRADE_MENU, screenWidth - upgradeMenuWidth, screenHeight / 2, null);
                char tempLevel = currentTowerLevel;
                switch (fieldTower[fieldSelectionX][fieldSelectionY]) {
                    case TOWER1 :
                        if (costTower1.containsKey(tempLevel)) {
                            drawText(costTower1.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY, 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                            if (tempLevel == TL5)
                                drawText(costTower1.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY + (towerMenuHalfHeight / 2), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        }
                        drawText((int)((float)(sellTower1.get(currentTowerLevel)) / fullLifeTower1.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]) + "", 32, screenWidth - (towerMenuWidth / 2), (screenHeight / 2) + (towerMenuHalfHeight / 30 * 25), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        break;
                    case TOWER2 :
                        if (costTower1.containsKey(tempLevel)) {
                            drawText(costTower2.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY, 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                            if (tempLevel == TL5)
                                drawText(costTower2.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY + (towerMenuHalfHeight / 2), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        }
                        drawText((int)((float)(sellTower2.get(currentTowerLevel)) / fullLifeTower2.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]) + "", 32, screenWidth - (towerMenuWidth / 2), (screenHeight / 2) + (towerMenuHalfHeight / 30 * 25), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        break;
                    case TOWER3 :
                        if (costTower1.containsKey(tempLevel)) {
                            drawText(costTower3.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY, 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                            if (tempLevel == TL5)
                                drawText(costTower3.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY + (towerMenuHalfHeight / 2), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        }
                        drawText((int)((float)(sellTower3.get(currentTowerLevel)) / fullLifeTower3.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]) + "", 32, screenWidth - (towerMenuWidth / 2), (screenHeight / 2) + (towerMenuHalfHeight / 30 * 25), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                    break;
                    case TOWER4 :
                        if (costTower1.containsKey(tempLevel)) {
                            drawText(costTower4.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY, 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                            if (tempLevel == TL5)
                                drawText(costTower4.get(tempLevel) + "", 32, screenWidth - (towerMenuWidth / 2), upMenuCostY + (towerMenuHalfHeight / 2), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        }
                        drawText((int)((float)(sellTower4.get(currentTowerLevel)) / fullLifeTower4.get(currentTowerLevel) * towerCurrLife[fieldSelectionX][fieldSelectionY]) + "", 32, screenWidth - (towerMenuWidth / 2), (screenHeight / 2) + (towerMenuHalfHeight / 30 * 25), 0xFFFFFFFF, 0xFF000000, 6, Paint.Align.CENTER);
                        break;
                }

                // unavailable upgrades in menu
                if (towerMaxUpdate.containsKey(currentTower)) {
                    if (currentTowerLevel == towerMaxUpdate.get(currentTower))
                        drawSprite(TOWER_UPGRADE_UNAVAILABLE, screenWidth - upgradeMenuWidth, screenHeight / 2, null);
                }
                if (confirmation) drawSprite(CONFIRMATION, confirmationX, confirmationY, null);
            }

            // draw lives
            drawSprite(HEART, 100, 100 - heartHalfSize, null);
            drawText(currHearts + "", 35, 100 + heartSize, 113, 0xFFFF0000, 0xFF000000, 4, Paint.Align.LEFT);

            // draw level coins
            drawSprite(COIN, 200, 100 - coinHalfSize, null);
            drawText(levelCoins + "", 35, 200 + coinSize, 113, 0xFFFFFF00, 0xFF000000, 4, Paint.Align.LEFT);

            // draw waves counter
            if (now - waveStartTime > waveStartDelay) {
                if (currWave + 1 < waves) {
                    drawText("wave " + (currWave + 1) + " of " + waves, 35, 100, 180, 0xFF00FF00, 0xFF000000, 4, Paint.Align.LEFT);
                } else {
                    drawText("last wave", 35, 100, 180, 0xFF00FF00, 0xFF000000, 4, Paint.Align.LEFT);
                }
            } else {
                if (currWave + 1 < waves) {
                    drawText("wave " + (currWave + 1) + " of " + waves + " in " + ((waveStartDelay / 1000) - (now - waveStartTime) / 1000) + " seconds", 35, 100, 180, 0xFF00FF00, 0xFF000000, 4, Paint.Align.LEFT);
                } else {
                    drawText("last wave in " + ((waveStartDelay / 1000) - (now - waveStartTime) / 1000) + " seconds", 35, 100, 180, 0xFF00FF00, 0xFF000000, 4, Paint.Align.LEFT);
                }
            }

            // close
            drawSprite(GAME_CLOSE_BUTTON, screenWidth - (menuButtonHalfSize * 2), menuButtonHalfSize * 2, null);
        } else if (!alive) {
            drawDialog("You have lost.  8_<(", "Retry", "Quit");
        } else if (won && now - winTime > winDelay) {
            drawDialog("You won.  8<D", "\\o/", "");
        } else if (gameClosePressed) {
            drawDialog("Want to leave the game?", "Leave", "Continue");
        }

        frameCounter ++;
        long delay = now - lastFpsTime;
        if (delay > 1000) {
            fps = (int)Math.round((((double) frameCounter) / delay) * 1000);
            frameCounter = 0;
            lastFpsTime = now;
        }
        info = "fps: " + fps;

        // info
        if (!info.isEmpty()) {
            drawText(info, 35, screenWidth / 2, screenHeight - 100, 0xFF00FFFF, 0xFF000000, 4, Paint.Align.CENTER);
            info = "";
        }
    }

    private void drawDialog(String text, String yes, String no) {
        ArrayList<String> textLines = new ArrayList<>();
        int charCounter = 0;
        int columnCounter = 0;
        int lineCounter = 0;
        int newLineStart = 0;
        while (charCounter < text.length()) {
            if (columnCounter == (dialogWidth * 2) - 9 || text.charAt(charCounter) == '\n' || charCounter >= text.length() - 1) {
                if (textLines.size() == 0) {
                    textLines.add(" " + text.substring(newLineStart, charCounter));
                } else {
                    textLines.add(text.substring(newLineStart, charCounter));
                }
                newLineStart = charCounter;
                columnCounter = 0;
                lineCounter ++;
            }
            charCounter ++;
            columnCounter ++;
        }
        textLines.set(textLines.size() -1, textLines.get(textLines.size() -1) + text.charAt(text.length() - 1));
        if (yes.isEmpty() && no.isEmpty()) {
            dialogHeight = lineCounter + (lineCounter / 4 * 3) + 4;
        } else {
            dialogHeight = lineCounter + (lineCounter / 4 * 3) + 11;
        }

        int xx = (screenWidth - (dialogBlock * dialogWidth)) / 2;
        int yy = (screenHeight - (dialogBlock * dialogHeight)) / 2;
        char whichSprite;
        for (int jj = 0; jj < dialogHeight; jj ++) {
            for (int ii = 0; ii < dialogWidth; ii ++) {
                if (ii == 0) {
                    if (jj == 0) {
                        whichSprite = DIALOG_TL;
                    } else if (jj == dialogHeight - 1) {
                        whichSprite = DIALOG_BL;
                    } else {
                        whichSprite = DIALOG_L;
                    }
                } else if (ii == dialogWidth - 1) {
                    if (jj == 0) {
                        whichSprite = DIALOG_TR;
                    } else if (jj == dialogHeight - 1) {
                        whichSprite = DIALOG_BR;
                    } else {
                        whichSprite = DIALOG_R;
                    }
                } else if (jj == 0) {
                    whichSprite = DIALOG_T;
                } else if (jj == dialogHeight -1) {
                    whichSprite = DIALOG_B;
                } else {
                    whichSprite = DIALOG_BG;
                }
                drawAppTextures(whichSprite, xx + (ii * dialogBlock), yy + (jj * dialogBlock), null);
            }
        }

        for (int tl = 0; tl < textLines.size(); tl ++) {
            drawText(textLines.get(tl), messageTextSize, xx + (dialogBlock * 2), yy + (dialogBlock * 2) + (dialogBlock / 6 * 5) + (tl * messageTextSize / 2 * 3), 0xFFFFFFFF, 0xFF0000FF, 0, null);
        }

        dialogButtonY = ((screenHeight - (dialogBlock * dialogHeight)) / 2) + ((dialogHeight - 2) * dialogBlock) - dialogButtonHalfHeight;
        if (!yes.isEmpty()) drawDialogButton(dialogYesButtonX, dialogButtonY, yes);
        if (!no.isEmpty()) drawDialogButton(dialogNoButtonX, dialogButtonY, no);
    }

    private void drawDialogButton(int ddbx, int ddby, String bt) {
        drawAppTextures(DIALOG_BUTTON, ddbx, ddby, null);
        drawText(bt, buttonTextSize, ddbx, ddby + (buttonTextSize / 4), 0xFF242609, 0xFF000000, 2, Paint.Align.CENTER);
    }

    private void drawText(String text, int size, int x, int y, int fColor, int strokeColor, int stroke, Paint.Align alignment) {
        float letterSpacing = 0;
        if (alignment == null) alignment = Paint.Align.LEFT;
        Paint paint = new Paint();
        if (stroke > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextAlign(alignment);
            paint.setTextSize(size);
            paint.setStrokeWidth(stroke);
            paint.setColor(strokeColor);
            paint.setTypeface(mainFont);
            paint.setLetterSpacing(letterSpacing);
            c.drawText(text, x, y, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(alignment);
        paint.setTextSize(size);
        paint.setColor(fColor);
        paint.setTypeface(mainFont);
        paint.setLetterSpacing(letterSpacing);
        c.drawText(text, x, y, paint);
    }

    private void drawAppTextures(char sprite, int sx, int sy, Paint paint) {
        switch (sprite) {
            case PLAY_BUTTON :
                rectOrigin.set(0, 0, 99, 99);
                rectDestiny.set(sx - menuButtonHalfSize, sy - menuButtonHalfSize, sx + menuButtonHalfSize, sy + menuButtonHalfSize);
                break;
            case CLOSE_BUTTON :
                rectOrigin.set(200, 0, 299, 99);
                rectDestiny.set(sx - menuButtonHalfSize, sy - menuButtonHalfSize, sx + menuButtonHalfSize, sy + menuButtonHalfSize);
                break;
            case LEVEL_BUTTON :
                rectOrigin.set(0, 100 + (currentRank * 100), 99, 199 + (currentRank * 100));
                rectDestiny.set(sx - menuButtonHalfSize, sy - menuButtonHalfSize, sx + menuButtonHalfSize, sy + menuButtonHalfSize);
                break;
            case LEVEL_BLOCKED_BUTTON :
                rectOrigin.set(100, 0, 199, 99);
                rectDestiny.set(sx - menuButtonHalfSize, sy - menuButtonHalfSize, sx + menuButtonHalfSize, sy + menuButtonHalfSize);
                break;
            case LEVEL_COMING_SOON :
                rectOrigin.set(100, 100, 199, 199);
                rectDestiny.set(sx - menuButtonHalfSize, sy - menuButtonHalfSize, sx + menuButtonHalfSize, sy + menuButtonHalfSize);
                break;
            case DIALOG_TL :
                rectOrigin.set(100, 200, 149, 249);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_TR :
                rectOrigin.set(200, 200, 249, 249);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_BL :
                rectOrigin.set(100, 300, 149, 349);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_BR :
                rectOrigin.set(200, 300, 249, 349);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_BG :
                rectOrigin.set(150, 250, 199, 299);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_L :
                rectOrigin.set(100, 250, 149, 299);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_T :
                rectOrigin.set(150, 200, 199, 249);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_R :
                rectOrigin.set(200, 250, 249, 299);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_B :
                rectOrigin.set(150, 300, 199, 349);
                rectDestiny.set(sx, sy, sx + dialogBlock, sy + dialogBlock);
                break;
            case DIALOG_BUTTON :
                rectOrigin.set(100, 350, 199, 399);
                rectDestiny.set(sx - dialogButtonHalfWidth, sy - dialogButtonHalfHeight, sx + dialogButtonHalfWidth, sy + dialogButtonHalfHeight);
                break;
            case MENU_BG :
                rectOrigin.set(300, 0, 487, 249);
                rectDestiny.set(sx, sy, sx + screenWidth, sy + screenHeight);
                break;
            case LEVEL_BG :
                rectOrigin.set(300, 250, 487, 499);
                rectDestiny.set(sx, sy, sx + screenWidth, sy + screenHeight);
                break;
        }
        c.drawBitmap(appTextures, rectOrigin, rectDestiny, paint);
    }

    private void drawSprite(char sprite, int sx, int sy, Paint paint) {
        switch (sprite) {
            case GAME_CLOSE_BUTTON :
                rectOrigin.set(900, 250, 999, 349);
                rectDestiny.set(sx - menuButtonHalfSize, sy - menuButtonHalfSize, sx + menuButtonHalfSize, sy + menuButtonHalfSize);
                break;
            case HEART :
                rectOrigin.set(100, 0, 149, 49);
                rectDestiny.set(sx, sy, sx + heartSize, sy + heartSize);
                break;
            case COIN :
                rectOrigin.set(150, 0, 199, 49);
                rectDestiny.set(sx, sy, sx + coinSize, sy + coinSize);
                break;
            case FIELD_SELECTION :
                rectOrigin.set(900, 50, 949, 99);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER_MENU :
                rectOrigin.set(950, 0, 999, 199);
                rectDestiny.set(sx, sy - towerMenuHalfHeight, sx + towerMenuWidth, sy + towerMenuHalfHeight);
                break;
            case UPGRADE_MENU :
                rectOrigin.set(1450 + (upMenuY * 50), upMenuX * 200, 1499 + (upMenuY * 50), 199 + (upMenuX * 200));
                rectDestiny.set(sx, sy - upgradeMenuHalfHeight, sx + upgradeMenuWidth, sy + upgradeMenuHalfHeight);
                break;
            case CONFIRMATION :
                rectOrigin.set(900, 100, 949, 149);
                rectDestiny.set(sx, sy, sx + towerMenuWidth, sy + towerMenuWidth);
                break;
            case BUILDING :
                rectOrigin.set(850, 50, 899, 99);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER1 :
                rectOrigin.set(1000 + (towerUpLevel * 50), towerShootingSprite * 200, 1049 + (towerUpLevel * 50), 49 + (towerShootingSprite * 200));
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER2 :
                rectOrigin.set(1000 + (towerUpLevel * 50), 50 + (towerShootingSprite * 200), 1049 + (towerUpLevel * 50), 99 + (towerShootingSprite * 200));
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER3 :
                rectOrigin.set(1000 + (towerUpLevel * 50), 100 + (towerShootingSprite * 200), 1049 + (towerUpLevel * 50), 149 + (towerShootingSprite * 200));
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER4 :
                rectOrigin.set(1000 + (towerUpLevel * 50), 150 + (towerShootingSprite * 200), 1049 + (towerUpLevel * 50), 199 + (towerShootingSprite * 200));
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER1_D :
                rectOrigin.set(1950, 0, 1999, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER2_D :
                rectOrigin.set(1950, 50, 1999, 99);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER3_D :
                rectOrigin.set(1950, 100, 1999, 149);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case TOWER4_D :
                rectOrigin.set(1950, 150, 1999, 199);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case EMPTY1 :
                rectOrigin.set(750, 0, 799, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case EMPTY2 :
                rectOrigin.set(800, 0, 849, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case EMPTY3 :
                rectOrigin.set(850, 0, 899, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case EMPTY4 :
                rectOrigin.set(900, 0, 949, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case BLOCKED1 :
                rectOrigin.set(250, 0, 299, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case BLOCKED2 :
                rectOrigin.set(300, 0, 349, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case BLOCKED3 :
                rectOrigin.set(350, 0, 399, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case BLOCKED4 :
                rectOrigin.set(400, 0, 449, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case BASE :
                rectOrigin.set(0, 0, 49, 49);
                rectDestiny.set(sx, sy, sx + towerSize, sy + towerSize);
                break;
            case ENEMY0 :
                rectOrigin.set(atX + spriteNumber * 50, 50, atX + 49 + (spriteNumber * 50), 99);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY1 :
                rectOrigin.set(atX + spriteNumber * 50, 100, atX + 49 + (spriteNumber * 50), 149);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY2 :
                rectOrigin.set(atX + spriteNumber * 50, 150, atX + 49 + (spriteNumber * 50), 199);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY3 :
                rectOrigin.set(atX + spriteNumber * 50, 200, atX + 49 + (spriteNumber * 50), 249);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY4 :
                rectOrigin.set(atX + spriteNumber * 50, 250, atX + 49 + (spriteNumber * 50), 299);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY5 :
                rectOrigin.set(atX + spriteNumber * 50, 300, atX + 49 + (spriteNumber * 50), 349);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY6 :
                rectOrigin.set(atX + spriteNumber * 50, 350, atX + 49 + (spriteNumber * 50), 399);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY7 :
                rectOrigin.set(atX + spriteNumber * 50, 400, atX + 49 + (spriteNumber * 50), 449);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY8 :
                rectOrigin.set(atX + spriteNumber * 50, 450, atX + 49 + (spriteNumber * 50), 499);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY9 :
                rectOrigin.set(atX + spriteNumber * 50, 500, atX + 49 + (spriteNumber * 50), 549);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY0_D :
                rectOrigin.set(800, 50, 849, 99);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY1_D :
                rectOrigin.set(800, 100, 849, 149);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY2_D :
                rectOrigin.set(800, 150, 849, 199);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY3_D :
                rectOrigin.set(800, 200, 849, 249);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY4_D :
                rectOrigin.set(800, 250, 849, 299);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY5_D :
                rectOrigin.set(800, 300, 849, 349);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY6_D :
                rectOrigin.set(800, 350, 849, 399);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY7_D :
                rectOrigin.set(800, 400, 849, 449);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY8_D :
                rectOrigin.set(800, 450, 849, 499);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY9_D :
                rectOrigin.set(800, 500, 849, 549);
                rectDestiny.set(sx - enemyCurrentHalfSize, sy - enemyCurrentSize, sx + enemyCurrentHalfSize, sy);
                break;
            case ENEMY0_SHOT :
                rectOrigin.set(spriteNumber * 50, 1000, 49 + (spriteNumber * 50), 1049);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY1_SHOT :
                rectOrigin.set(spriteNumber * 50, 1050, 49 + (spriteNumber * 50), 1099);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY2_SHOT :
                rectOrigin.set(spriteNumber * 50, 1100, 49 + (spriteNumber * 50), 1149);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY3_SHOT :
                rectOrigin.set(spriteNumber * 50, 1150, 49 + (spriteNumber * 50), 1199);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY4_SHOT :
                rectOrigin.set(spriteNumber * 50, 1200, 49 + (spriteNumber * 50), 1249);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY5_SHOT :
                rectOrigin.set(spriteNumber * 50, 1250, 49 + (spriteNumber * 50), 1299);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY6_SHOT :
                rectOrigin.set(spriteNumber * 50, 1300, 49 + (spriteNumber * 50), 1349);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY7_SHOT :
                rectOrigin.set(spriteNumber * 50, 1350, 49 + (spriteNumber * 50), 1399);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY8_SHOT :
                rectOrigin.set(spriteNumber * 50, 1400, 49 + (spriteNumber * 50), 1449);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case ENEMY9_SHOT :
                rectOrigin.set(spriteNumber * 50, 1450, 49 + (spriteNumber * 50), 1499);
                rectDestiny.set(sx - enemyDefaultHalfSize, sy - enemyDefaultSize, sx + enemyDefaultHalfSize, sy);
                break;
            case LIFE_BG :
                rectOrigin.set(50, 0, 99, 15);
                rectDestiny.set(sx - halfLifeBarInPX, sy, sx + halfLifeBarInPX, sy + lifeBarHeight);
                break;
            case LIFE_TOWER :
                rectOrigin.set(50, 16, 99, 31);
                rectDestiny.set(sx - halfLifeBarInPX + 1, sy + 1, sx - halfLifeBarInPX + lifeInPX - 2, sy + lifeBarHeight - 1);
                break;
            case LIFE_ENEMY :
                rectOrigin.set(50, 32, 99, 47);
                rectDestiny.set(sx - halfLifeBarInPX + 1, sy + 1, sx - halfLifeBarInPX + lifeInPX - 2, sy + lifeBarHeight - 1);
                break;
            case TOWER_SHOTS :
                rectOrigin.set(1000 + (whichShotX * 50), 1000 + (whichShotY * 50) + (whichShotSprite * 200), 1049 + (whichShotX * 50), 1049 + (whichShotY * 50) + (whichShotSprite * 200));
                rectDestiny.set(sx - towerShotsHalfSize, sy - towerShotsHalfSize, sx + towerShotsHalfSize, sy + towerShotsHalfSize);
                break;
            case TOWER_UNAVAILABLE :
                rectOrigin.set(950, 200, 999, 249);
                rectDestiny.set(sx, sy, sx + towerMenuWidth, sy + towerMenuWidth);
                break;
            case TOWER_UPGRADE_UNAVAILABLE :
                rectOrigin.set(1900,  upMenuX * 200, 1949, 199 + (upMenuX * 200));
                rectDestiny.set(sx, sy - upgradeMenuHalfHeight, sx + upgradeMenuWidth, sy + upgradeMenuHalfHeight);
                break;
        }
        c.drawBitmap(gameTextures, rectOrigin, rectDestiny, paint);
    }

    @Override
    public void onBackPressed() {
        switch (gameState) {
            case "menu":
                if (receiver != null) {
                    unregisterReceiver(receiver);
                }
                audio.unload();
                super.onBackPressed();
                break;
            case "levels":
                gameState = "menu";
                break;
            case "game":
                gameState = "levels";
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        int pressedKey = event.getUnicodeChar();

        if (action == KeyEvent.ACTION_DOWN) {
            /*if (keyCode == KeyEvent.KEYCODE_BACK) {

            } else */
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
        }
        return true;
    }

    public class GameView extends View {

        public GameView(Context ctx) {
            super(ctx);
            context = ctx;
        }

        public GameView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            context = ctx;
        }

        public GameView(Context ctx, AttributeSet attrs, int defStyle) {
            super(ctx, attrs, defStyle);
            context = ctx;
        }

        protected void onDraw(Canvas canvas) {
            c = canvas;
            if (!heightCorrected) {
                if (screenHeight != getHeight()) {
                    screenWidth = getWidth();
                    screenHeight = getHeight();
                    heightCorrected = true;
                }
            }
            now = System.currentTimeMillis();
            if (c != null) {
                switch (gameState) {
                    case "menu":
                        drawMenu();
                        handleMenuTouch();
                        break;
                    case "levels":
                        drawLevels();
                        handleLevelsTouch();
                        break;
                    case "game":
                        drawGame();
                        handleGameTouch();
                        break;
                }
            }
            this.invalidate();
        }
    }
}