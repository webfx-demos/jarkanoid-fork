package eu.hansolo.fx.jarkanoid;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import dev.webfx.platform.audio.Audio;
import dev.webfx.platform.audio.AudioService;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.shutdown.Shutdown;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.windowlocation.WindowLocation;
import eu.hansolo.fx.jarkanoid.Constants.BlockType;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class Main extends Application {

    private final static boolean IS_BROWSER = UserAgent.isBrowser();
    protected enum PaddleState {
        STANDARD(80, 22),
        WIDE(120, 22),
        LASER(80, 22);

        protected final double width;
        protected final double height;

        PaddleState(final double width, final double height) {
            this.width  = width;
            this.height = height;
        }
    }
    private enum BonusType {
        BONUS_C,  // Catch Ball      (lime)
        BONUS_D,  // 3-Balls         (cyan)
        BONUS_F,  // Wide            (dark blue)
        BONUS_L,  // Laser           (red)
        BONUS_S,  // Slow            (dark yellow)
        BONUS_B,  // Next Level      (magenta)
        BONUS_P;  // Additional life (gray)
    }
    private enum EnemyType {
        MOLECULE;
    }

    private static final Random      RND                  = new Random();
    private static final double      WIDTH                = 560;
    private static final double      HEIGHT               = 740;
    private static final double      INSET                = 22;
    private static final double      UPPER_INSET          = 85;
    private static final double      PADDLE_OFFSET_Y      = 68;
    private static final double      PADDLE_SPEED         = 8;
    private static final double      TORPEDO_SPEED        = 12;
    private static final double      BALL_SPEED           = clamp(0.1, 10, PropertyManager.INSTANCE.getDouble(Constants.BALL_SPEED_KEY, 3));
    private static final double      BONUS_BLOCK_SPEED    = clamp(0.1, 5, PropertyManager.INSTANCE.getDouble(Constants.BONUS_BLOCK_SPEED_KEY, 3));
    private static final double      ENEMY_SPEED          = clamp(0.1, 5, PropertyManager.INSTANCE.getDouble(Constants.ENEMY_SPEED_KEY, 3));
    private static final double      BLOCK_WIDTH          = 38;
    private static final double      BLOCK_HEIGHT         = 20;
    private static final double      BLOCK_STEP_X         = 40;
    private static final double      BLOCK_STEP_Y         = 22;
    private static final double      BONUS_BLOCK_WIDTH    = 38;
    private static final double      BONUS_BLOCK_HEIGHT   = 18;
    private static final double      ENEMY_WIDTH          = 32;
    private static final double      ENEMY_HEIGHT         = 32;
    private static final double      EXPLOSION_WIDTH      = 32;
    private static final double      EXPLOSION_HEIGHT     = 32;
    private static final double      BALL_VX_INFLUENCE    = 0.75;
    private static final Font        SCORE_FONT           = Fonts.emulogic(20);
    private static final Color       HIGH_SCORE_RED       = Color.rgb(229, 2, 1);
    private static final Color       SCORE_WHITE          = Color.WHITE;
    private static final Color       TEXT_GRAY            = Color.rgb(216, 216, 216);
    private static final int         BONUS_BLOCK_INTERVAL = 20;

    //private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Instant                  gameStartTime;
    private long                     levelStartTime;
    private boolean                  running;
    private AnimationTimer           timer;
    private long                     lastTimerCall;
    private long                     lastAnimCall;
    private long                     lastBonusAnimCall;
    private long                     lastEnemyUpdateCall;
    private long                     lastOneSecondCheck;
    private Canvas                   bkgCanvas;
    private GraphicsContext          bkgCtx;
    private Canvas                   canvas;
    private GraphicsContext          ctx;
    private Canvas                   brdrCanvas;
    private GraphicsContext          brdrCtx;
    private Image                    logoImg;
    private Image                    copyrightImg;
    private Image                    bkgPatternImg1;
    private Image                    bkgPatternImg2;
    private Image                    bkgPatternImg3;
    private Image                    bkgPatternImg4;
    private ImagePattern             bkgPatternFill1;
    private ImagePattern             bkgPatternFill2;
    private ImagePattern             bkgPatternFill3;
    private ImagePattern             bkgPatternFill4;
    private ImagePattern             borderPatternFill;
    private Image                    borderVerticalImg;
    private Image                    borderPartVerticalImg;
    private Image                    topDoorImg;
    private Image                    ulCornerImg;
    private Image                    urCornerImg;
    private Image                    pipeImg;
    private ImagePattern             pipePatternFill;
    private Image                    paddleMiniImg;
    private Image                    paddleStdShadowImg;
    private Image                    paddleWideShadowImg;
    private Image                    paddleGunShadowImg;
    private Image                    paddleMapStdImg;
    private Image                    paddleMapWideImg;
    private Image                    paddleMapGunImg;
    private Image                    blinkMapImg;
    private Image                    ballImg;
    private Image                    ballShadowImg;
    private Image                    torpedoImg;
    private Image                    goldBlockImg;
    private Image                    grayBlockImg;
    private Image                    whiteBlockImg;
    private Image                    orangeBlockImg;
    private Image                    cyanBlockImg;
    private Image                    limeBlockImg;
    private Image                    redBlockImg;
    private Image                    blueBlockImg;
    private Image                    magentaBlockImg;
    private Image                    yellowBlockImg;
    private Image                    bonusBlockCMapImg;
    private Image                    bonusBlockFMapImg;
    private Image                    bonusBlockDMapImg;
    private Image                    bonusBlockSMapImg;
    private Image                    bonusBlockLMapImg;
    private Image                    bonusBlockBMapImg;
    private Image                    bonusBlockPMapImg;
    private Image                    openDoorMapImg;
    private Image                    moleculeMapImg;
    private Image                    blockShadowImg;
    private Image                    bonusBlockShadowImg;
    private Image                    explosionMapImg;
    private Audio                    gameStartSnd;
    private Audio                    startLevelSnd;
    private Audio                    ballPaddleSnd;
    private Audio                    ballBlockSnd;
    private Audio                    ballHardBlockSnd;
    private Audio                    laserSnd;
    private Audio                    explosionSnd;
    private Audio                    gameOverSnd;
    private Paddle                   paddle;
    private List<Ball>               balls;
    private List<Block>              blocks;
    private List<BonusBlock>         bonusBlocks;
    private List<Torpedo>            torpedoes;
    private int                      noOfLifes;
    private long                     score;
    private PaddleState              paddleState;
    private long                     highscore;
    private int                      level;
    private int                      animateInc;
    private List<Blink>              blinks;
    private double                   ballSpeed;
    private boolean                  readyLevelVisible;
    private int                      paddleResetCounter;
    private int                      speedResetCounter;
    private int                      nextLevelDoorCounter;
    private boolean                  nextLevelDoorOpen;
    private double                   nextLevelDoorAlpha;
    private boolean                  movingPaddleOut;
    private OpenDoor                 openDoor;
    private boolean                  showStartHint;
    private int                      silverBlockMaxHits;
    private int                      blockCounter;
    private boolean                  stickyPaddle;
    private List<Enemy>              enemies;
    private List<Explosion>          explosions;
    private Pos                      enemySpawnPosition;
    private double                   topLeftDoorAlpha;
    private double                   topRightDoorAlpha;
    private FIFO<Block>              blockFifo;
    private EventHandler<MouseEvent> mouseHandler;


    // ******************** Methods *******************************************
    @Override public void init() {
        running                  = false;
        paddleState              = PaddleState.STANDARD;
        highscore                = PropertyManager.INSTANCE.getLong(Constants.HIGHSCORE_KEY, 0);
        level                    = 1;
        blinks                   = new ArrayList<>();
        ballSpeed                = BALL_SPEED;
        readyLevelVisible        = false;
        paddleResetCounter       = 0;
        speedResetCounter        = 0;
        nextLevelDoorCounter     = 0;
        nextLevelDoorOpen        = false;
        nextLevelDoorAlpha       = 1.0;
        movingPaddleOut          = false;
        openDoor                 = new OpenDoor(WIDTH - 20, UPPER_INSET + 565);
        showStartHint            = IS_BROWSER;
        silverBlockMaxHits       = 2;
        blockCounter             = 0;
        stickyPaddle             = false;
        enemySpawnPosition       = Pos.CENTER;
        topLeftDoorAlpha         = 1.0;
        topRightDoorAlpha        = 1.0;
        blockFifo                = new FIFO<>(9);

        lastOneSecondCheck       = System.nanoTime();
        lastTimerCall            = System.nanoTime();
        lastAnimCall             = System.nanoTime();
        lastBonusAnimCall        = System.nanoTime();
        lastEnemyUpdateCall      = System.nanoTime();
        timer                    = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (running) {
                    // 1 second check
                    if (now > lastOneSecondCheck + 1_000_000_000) {
                        // After 15 seconds in the level enemies will be spawned every 10 seconds if less 5 enemies in the game
                        long levelPlayTime = Instant.now().getEpochSecond() - levelStartTime;
                        if (levelPlayTime > 15 && enemies.size() < 5 && levelPlayTime % 10 == 0) {
                            enemySpawnPosition = RND.nextBoolean() ? Pos.TOP_LEFT : Pos.TOP_RIGHT;
                            switch(enemySpawnPosition) {
                                case TOP_LEFT  : topLeftDoorAlpha  = 0.99; break;
                                case TOP_RIGHT : topRightDoorAlpha = 0.99; break;
                            }
                        }

                        if (paddleResetCounter > 0) {
                            paddleResetCounter--;
                            if (paddleResetCounter == 0) {
                                paddleState = PaddleState.STANDARD;
                            }
                        }
                        if (speedResetCounter > 0) {
                            speedResetCounter--;
                            if (speedResetCounter == 0) {
                                ballSpeed = BALL_SPEED;
                            }
                        }
                        if (nextLevelDoorCounter > 0) {
                            nextLevelDoorCounter--;
                            if (nextLevelDoorCounter == 0 && !movingPaddleOut) {
                                nextLevelDoorAlpha = 1.0;
                                nextLevelDoorOpen  = false;
                                drawBorder();
                            }
                        }

                        lastOneSecondCheck = now;
                    }

                    // Animate bonus blocks and top doors
                    if (now > lastBonusAnimCall + 50_000_000) {
                        // Update bonus blocks
                        bonusBlocks.forEach(bonusBlock -> bonusBlock.update());

                        // Fade out top doors
                        if (topLeftDoorAlpha < 1) {
                            topLeftDoorAlpha -= 0.1;
                            if (topLeftDoorAlpha <= 0) {
                                spawnEnemy(Pos.TOP_LEFT);
                                topLeftDoorAlpha = 1;
                            }
                            drawBorder();
                        } else if (topRightDoorAlpha < 1) {
                            topRightDoorAlpha -= 0.1;
                            if (topRightDoorAlpha <= 0) {
                                spawnEnemy(Pos.TOP_RIGHT);
                                topRightDoorAlpha = 1;
                            }
                            drawBorder();
                        }
                        lastBonusAnimCall = now;
                    }

                    // Animate enemies
                    if (now > lastEnemyUpdateCall + 100_000_000) {
                        enemies.forEach(enemy -> enemy.update());
                        explosions.forEach(explosion -> explosion.update());
                        lastEnemyUpdateCall = now;
                    }

                    // Animation of paddle glow
                    if (now > lastAnimCall + 5_000_000) {
                        animateInc++;
                        lastAnimCall = now;
                    }

                    // Main loop
                    if (now > lastTimerCall) {
                        hitTests();
                        updateAndDraw();
                        if (nextLevelDoorOpen) { drawBorder(); }
                        lastTimerCall = now;
                    }

                    if (movingPaddleOut) {
                        paddle.x += 1;
                        paddle.bounds.set(paddle.x, paddle.y, paddleState.width, paddle.height);
                        updateAndDraw();
                        if (paddle.x > WIDTH) {
                            level++;
                            if (level > Constants.LEVEL_MAP.size()) { level = 1; }
                            score += 10_000;
                            startLevel(level);
                        }
                    }
                } else {
                    if (!showStartHint && Instant.now().getEpochSecond() - gameStartTime.getEpochSecond() > 8) {
                        showStartHint = true;
                        startScreen();
                    }
                }
            }
        };

        // Setup canvas nodes
        bkgCanvas  = new Canvas(WIDTH, HEIGHT);
        bkgCtx     = bkgCanvas.getGraphicsContext2D();

        canvas     = new Canvas(WIDTH, HEIGHT);
        ctx        = canvas.getGraphicsContext2D();

        brdrCanvas = new Canvas(WIDTH, HEIGHT);
        brdrCtx    = brdrCanvas.getGraphicsContext2D();
        brdrCanvas.setMouseTransparent(true);

        // Attach mouse dragging to paddle
        mouseHandler = e -> {
            EventType<MouseEvent> type = (EventType<MouseEvent>) e.getEventType();
            if (MouseEvent.MOUSE_DRAGGED.equals(type)) {
                double x = e.getSceneX() - paddleState.width * 0.5;
                if (x + paddleState.width > WIDTH - INSET) {
                    if (nextLevelDoorOpen && !movingPaddleOut) { movingPaddleOut = true; }
                    x = WIDTH - INSET - paddleState.width;
                }
                if (x < INSET) { x = INSET; }
                paddle.x = x;
                paddle.bounds.set(x, paddle.y, paddleState.width, paddle.height);
            }
        };
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseHandler);

        // Load all images
        loadImages();

        // Load all sounds
        //loadSounds(); // => moved to start() as it doesn't work at this stage to load the Gluon audio servie (must be in UI thread)

        bkgPatternFill1   = new ImagePattern(bkgPatternImg1, 0, 0, 68, 117, false);
        bkgPatternFill2   = new ImagePattern(bkgPatternImg2, 0, 0, 64, 64, false);
        bkgPatternFill3   = new ImagePattern(bkgPatternImg3, 0, 0, 64, 64, false);
        bkgPatternFill4   = new ImagePattern(bkgPatternImg4, 0, 0, 64, 64, false);
        borderPatternFill = new ImagePattern(borderVerticalImg, 0, 0, 20, 113, false);
        pipePatternFill   = new ImagePattern(pipeImg, 0, 0, 5, 17, false);


        // Initialize paddles
        paddle = new Paddle();

        // Initialize level
        balls       = new ArrayList<>(); //new CopyOnWriteArrayList<>();
        blocks      = new ArrayList<>(); //new CopyOnWriteArrayList<>();
        bonusBlocks = new ArrayList<>(); //new CopyOnWriteArrayList<>();
        enemies     = new ArrayList<>(); //new CopyOnWriteArrayList<>();
        explosions  = new ArrayList<>(); //new CopyOnWriteArrayList<>();
        torpedoes   = new ArrayList<>(); //new CopyOnWriteArrayList<>();
        noOfLifes   = 3;
        score       = 0;
    }

    @Override public void start(final Stage stage) {
        gameStartTime = Instant.now();

        final StackPane pane  = new StackPane(bkgCanvas, canvas, brdrCanvas);
        pane.setMaxSize(WIDTH, HEIGHT); // Necessary to scale up with ScalePane
        final Scene     scene = new Scene(new ScalePane(pane), WIDTH, HEIGHT, Color.BLACK);

        scene.setOnKeyPressed(e -> {
            if (running) {
                if (movingPaddleOut) { return; }
                switch (e.getCode()) {
                    case RIGHT:
                    case D:
                        movePaddleRight();
                        break;
                    case LEFT:
                    case A:
                        movePaddleLeft();
                        break;
                    case SPACE: {
                        final long activeBalls = balls.stream().filter(ball -> ball.active).count();
                        if (activeBalls > 0) {
                            if (PaddleState.LASER == paddleState) { fire(paddle.bounds.centerX); }
                        } else {
                            stickyPaddle       = false;
                            balls.forEach(ball -> {
                                ball.active        = true;
                                ball.bornTimestamp = Instant.now().getEpochSecond();
                            });
                        }
                    }
                }
            } else {
                // Block for the first 8 seconds to give it some time to play the game start song
                if (IS_BROWSER || Instant.now().getEpochSecond() - gameStartTime.getEpochSecond() > 8) {
                    level = 1;
                    String queryString = WindowLocation.getQueryString();
                    if (queryString != null && queryString.startsWith("level="))
                        level = Integer.parseInt(queryString.substring(6));
                    startLevel(level);
                }
            }
        });
        scene.setOnKeyReleased(e -> {
            switch(e.getCode()) {
                case RIGHT:
                case LEFT: stopPaddle();
            }
        });

        stage.setTitle("JArkanoid");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);

        // Waiting that the font and required images are loaded before displaying the start screen
        DeviceSceneUtil.onFontsAndImagesLoaded(this::startScreen, logoImg, copyrightImg);

        timer.start();

        loadSounds();

        if (!IS_BROWSER)
            playSound(gameStartSnd);
    }

    @Override public void stop() {
        Shutdown.softwareShutdown(true, 0);
        //Platform.exit();
        //System.exit(0);
    }


    // Helper methods
    private void loadImages() {
        logoImg               = new Image(Resource.toUrl("jarkanoid_logo.png", getClass()), 460, 118, true, false);
        copyrightImg          = new Image(Resource.toUrl("copyright.png", getClass()), 458, 115, true, false);
        bkgPatternImg1        = new Image(Resource.toUrl("backgroundPattern_1.png", getClass()), 68, 117, true, false);
        bkgPatternImg2        = new Image(Resource.toUrl("backgroundPattern_2.png", getClass()), 64, 64, true, false);
        bkgPatternImg3        = new Image(Resource.toUrl("backgroundPattern_3.png", getClass()), 64, 64, true, false);
        bkgPatternImg4        = new Image(Resource.toUrl("backgroundPattern_4.png", getClass()), 64, 64, true, false);
        borderVerticalImg     = new Image(Resource.toUrl("borderVertical.png", getClass()), 20, 113, true, false);
        borderPartVerticalImg = new Image(Resource.toUrl("borderPartVertical.png", getClass()), 20, 71, true, false);
        topDoorImg            = new Image(Resource.toUrl("topDoor.png", getClass()), 64, 23, true, false);
        ulCornerImg           = new Image(Resource.toUrl("upperLeftCorner.png", getClass()), 15, 20, true, false);
        urCornerImg           = new Image(Resource.toUrl("upperRightCorner.png", getClass()), 15, 20, true, false);
        pipeImg               = new Image(Resource.toUrl("pipe.png", getClass()), 5, 17, true, false);
        paddleMapStdImg       = new Image(Resource.toUrl("paddlemap_std.png", getClass()), 640, 176, false, false);
        paddleMapWideImg      = new Image(Resource.toUrl("paddlemap_wide.png", getClass()), 960, 176, false, false);
        paddleMapGunImg       = new Image(Resource.toUrl("paddlemap_gun.png", getClass()), 640, 176, false, false);
        blinkMapImg           = new Image(Resource.toUrl("blink_map.png", getClass()), 304, 60, false, false);
        paddleMiniImg         = new Image(Resource.toUrl("paddle_std.png", getClass()), 40, 11, true, false);
        paddleStdShadowImg    = new Image(Resource.toUrl("paddle_std_shadow.png", getClass()), 80, 22, true, false);
        paddleWideShadowImg   = new Image(Resource.toUrl("paddle_wide_shadow.png", getClass()), 121, 22, true, false);
        paddleGunShadowImg    = new Image(Resource.toUrl("paddle_gun_shadow.png", getClass()), 80, 22, true, false);
        ballImg               = new Image(Resource.toUrl("ball.png", getClass()), 12, 12, true, false);
        ballShadowImg         = new Image(Resource.toUrl("ball_shadow.png", getClass()), 12, 12, true, false);
        torpedoImg            = new Image(Resource.toUrl("torpedo.png", getClass()), 41, 23, true, false);
        goldBlockImg          = new Image(Resource.toUrl("goldBlock.png", getClass()), 38, 20, true, false);
        grayBlockImg          = new Image(Resource.toUrl("grayBlock.png", getClass()), 38, 20, true, false);
        whiteBlockImg         = new Image(Resource.toUrl("whiteBlock.png", getClass()), 38, 20, true, false);
        orangeBlockImg        = new Image(Resource.toUrl("orangeBlock.png", getClass()), 38, 20, true, false);
        cyanBlockImg          = new Image(Resource.toUrl("cyanBlock.png", getClass()), 38, 20, true, false);
        limeBlockImg          = new Image(Resource.toUrl("limeBlock.png", getClass()), 38, 20, true, false);
        redBlockImg           = new Image(Resource.toUrl("redBlock.png", getClass()), 38, 20, true, false);
        blueBlockImg          = new Image(Resource.toUrl("blueBlock.png", getClass()), 38, 20, true, false);
        magentaBlockImg       = new Image(Resource.toUrl("magentaBlock.png", getClass()), 38, 20, true, false);
        yellowBlockImg        = new Image(Resource.toUrl("yellowBlock.png", getClass()), 38, 20, true, false);
        blockShadowImg        = new Image(Resource.toUrl("block_shadow.png", getClass()), 38, 20, true, false);
        bonusBlockCMapImg     = new Image(Resource.toUrl("block_map_bonus_c.png", getClass()), 190, 72, true, false);
        bonusBlockFMapImg     = new Image(Resource.toUrl("block_map_bonus_f.png", getClass()), 190, 72, true, false);
        bonusBlockDMapImg     = new Image(Resource.toUrl("block_map_bonus_d.png", getClass()), 190, 72, true, false);
        bonusBlockSMapImg     = new Image(Resource.toUrl("block_map_bonus_s.png", getClass()), 190, 72, true, false);
        bonusBlockLMapImg     = new Image(Resource.toUrl("block_map_bonus_l.png", getClass()), 190, 72, true, false);
        bonusBlockBMapImg     = new Image(Resource.toUrl("block_map_bonus_b.png", getClass()), 190, 72, true, false);
        bonusBlockPMapImg     = new Image(Resource.toUrl("block_map_bonus_p.png", getClass()), 190, 72, true, false);
        openDoorMapImg        = new Image(Resource.toUrl("open_door_map.png", getClass()), 120, 71, true, false);
        moleculeMapImg        = new Image(Resource.toUrl("molecule_map.png", getClass()), 256, 96, true, false);
        explosionMapImg       = new Image(Resource.toUrl("explosion_map.png", getClass()), 128, 128, true, false);
        bonusBlockShadowImg   = new Image(Resource.toUrl("bonus_block_shadow.png", getClass()), 38, 18, true, false);
    }

    private void loadSounds() {
        gameStartSnd     = loadSound(Resource.toUrl("game_start.mp3", getClass()));
        startLevelSnd    = loadSound(Resource.toUrl("level_ready.mp3", getClass()));
        ballPaddleSnd    = loadSound(Resource.toUrl("ball_paddle.mp3", getClass()));
        ballBlockSnd     = loadSound(Resource.toUrl("ball_block.mp3", getClass()));
        ballHardBlockSnd = loadSound(Resource.toUrl("ball_hard_block.mp3", getClass()));
        laserSnd         = loadSound(Resource.toUrl("gun.mp3", getClass()));
        explosionSnd     = loadSound(Resource.toUrl("explosion.mp3", getClass()));
        gameOverSnd      = loadSound(Resource.toUrl("game_over.mp3", getClass()));
    }

    private Audio loadSound(String url) {
        return AudioService.loadSound(url);
    }

    private static double clamp(final double min, final double max, final double value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }

    private static int clamp(final int min, final int max, final int value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }


    // ******************** Game control **************************************
    private void movePaddleRight() { paddle.vX = PADDLE_SPEED; }

    private void movePaddleLeft() { paddle.vX = -PADDLE_SPEED; }

    private void stopPaddle() { paddle.vX = 0; }

    private void fire(final double x) {
        if (torpedoes.size() > 0) { return; }
        torpedoes.add(new Torpedo(torpedoImg, x, HEIGHT - 50));
        playSound(laserSnd);
    }


    // Play audio clips
    private void playSound(final Audio audioClip) {
        if (audioClip != null)
            audioClip.play();
    }


    // Spawn enemy
    private void spawnEnemy(final Pos position) {
        switch (position) {
            case TOP_LEFT: enemies.add(new Enemy(100 + topDoorImg.getWidth() * 0.5 - ENEMY_WIDTH * 0.5, UPPER_INSET, EnemyType.MOLECULE)); break;
            case TOP_RIGHT: enemies.add(new Enemy(WIDTH - 100 - topDoorImg.getWidth() * 0.5 - ENEMY_WIDTH * 0.5, UPPER_INSET, EnemyType.MOLECULE)); break;
        }
    }


    // Re-Spawn Ball
    private void spawnBall() {
        if (balls.size() > 0) { return; }
        balls.add(new Ball(ballImg, paddle.bounds.centerX, paddle.bounds.minY - ballImg.getHeight() * 0.5 - 1, (RND.nextDouble() * (2 * ballSpeed) - ballSpeed)));
    }


    // Start Screen
    private void startScreen() {
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        drawBackground(1);
        drawBorder();
    }


    // Start Level
    private void startLevel(final int level) {
        levelStartTime     = Instant.now().getEpochSecond();
        blockCounter       = 0;
        nextLevelDoorAlpha = 1.0;
        nextLevelDoorOpen  = false;
        movingPaddleOut    = false;
        paddle.countX      = 0;
        paddle.countY      = 0;
        animateInc         = 0;
        paddle.x           = WIDTH * 0.5 - paddleState.width * 0.5;
        paddle.bounds.minX = paddle.x - paddle.width * 0.5;
        readyLevelVisible  = true;
        playSound(startLevelSnd);
        setupBlocks(level);
        bonusBlocks.clear();
        balls.clear();
        enemies.clear();
        explosions.clear();
        spawnBall();
        if (!running) { running = true; }
        drawBackground(level);
        drawBorder();
        updateAndDraw();
        schedule(() -> { readyLevelVisible = false; }, 2, TimeUnit.SECONDS);
    }

    private void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        //executor.schedule(runnable, delay, timeUnit);
        Scheduler.scheduleDelay(timeUnit.toMillis(delay), runnable);
    }


    // Game Over
    private void gameOver() {
        schedule(() -> startScreen(), 5, TimeUnit.SECONDS);

        playSound(gameOverSnd);
        running = false;
        balls.clear();
        torpedoes.clear();

        updateAndDraw();

        if (score > highscore) {
            PropertyManager.INSTANCE.setLong(Constants.HIGHSCORE_KEY, score);
            highscore = score;
        }
        PropertyManager.INSTANCE.storeProperties();
        score       = 0;
        noOfLifes   = 3;
        paddleState = PaddleState.STANDARD;
    }


    // Setup blocks for given level
    private void setupBlocks(final int level) {
        blocks.clear();
        BlockType[][] level2 = Constants.LEVEL_MAP.get(level);
        silverBlockMaxHits = level % 8 == 0 ? silverBlockMaxHits + 1 : silverBlockMaxHits;
        for (int iy = 0 ; iy < level2.length ; iy++) {
            for (int ix = 0 ; ix < level2[iy].length ; ix++) {
                Block block;
                final BlockType blockType = level2[iy][ix];
                switch (blockType) {
                    case GOLD: block = new Block(goldBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 0, blockType.maxHits, blockType); break;
                    case GRAY: block = new Block(grayBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 0, silverBlockMaxHits, blockType); break;
                    case WHIT: block = new Block(whiteBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 10, blockType.maxHits, blockType); break;
                    case ORNG: block = new Block(orangeBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 60, blockType.maxHits, blockType); break;
                    case CYAN: block = new Block(cyanBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 70, blockType.maxHits, blockType); break;
                    case LIME: block = new Block(limeBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 80, blockType.maxHits, blockType); break;
                    case RUBY: block = new Block(redBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 90, blockType.maxHits, blockType); break;
                    case BLUE: block = new Block(blueBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 100, blockType.maxHits, blockType); break;
                    case MGNT: block = new Block(magentaBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 110, blockType.maxHits, blockType); break;
                    case YLLW: block = new Block(yellowBlockImg, INSET + ix * BLOCK_STEP_X, INSET + 110 + iy * BLOCK_STEP_Y, 120, blockType.maxHits, blockType); break;
                    default: block = null;
                }
                if (null == block) { continue; }
                blocks.add(block);
            }
        }
    }


    // ******************** HitTests ******************************************
    private void hitTests() {
        // torpedo hits block or enemy
        for (Torpedo torpedo : torpedoes) {
            if (PaddleState.LASER == paddleState) {
                for (Block block : blocks) {
                    if (block.bounds.intersects(torpedo.bounds)) {
                        block.hits++;
                        if (block.hits == block.maxHits) {
                            block.toBeRemoved = true;
                            score += block.value;
                        }
                        torpedo.toBeRemoved = true;
                        break;
                    }
                }
                for (Enemy enemy : enemies) {
                    if (enemy.bounds.intersects(torpedo.bounds)) {
                        enemy.toBeRemoved   = true;
                        torpedo.toBeRemoved = true;
                        explosions.add(new Explosion(enemy.x, enemy.y, enemy.vX, enemy.vY, 1.0));
                        playSound(explosionSnd);
                        break;
                    }
                }
            }
        }

        // paddle hits bonus blocks
        for (BonusBlock bonusBlock : bonusBlocks) {
            if (bonusBlock.bounds.intersects(paddle.bounds)) {
                bonusBlock.toBeRemoved = true;
                switch (bonusBlock.bonusType) {
                    case BONUS_C: { stickyPaddle = true; }
                    case BONUS_D: {
                        if (balls.size() == 1) {
                            Ball ball = balls.get(0);
                            double vX1 = (Math.sin(Math.toRadians(10)) * ball.vX);
                            double vY1 = (Math.cos(Math.toRadians(10)) * ball.vY);
                            double vX2 = (Math.sin(Math.toRadians(-10)) * ball.vX);
                            double vY2 = (Math.cos(Math.toRadians(-10)) * ball.vY);
                            balls.add(new Ball(ballImg, ball.x, ball.y, vX1, vY1));
                            balls.add(new Ball(ballImg, ball.x, ball.y, vX2, vY2));
                        }
                        break;
                    }
                    case BONUS_F: {
                        paddleResetCounter = 30;
                        paddleState        = PaddleState.WIDE;
                        break;
                    }
                    case BONUS_L: {
                        paddleResetCounter = 30;
                        paddleState        = PaddleState.LASER;
                        break;
                    }
                    case BONUS_S: {
                        speedResetCounter = 30;
                        ballSpeed         = BALL_SPEED * 0.5;
                        break;
                    }
                    case BONUS_B: {
                        nextLevelDoorCounter = 5;
                        nextLevelDoorOpen    = true;
                        break;
                    }
                    case BONUS_P: { noOfLifes = clamp(2, 5, noOfLifes + 1); break;}
                }
            }
        }
    }


    // ******************** Redraw ********************************************
    private void drawBackground(final int level) {
        bkgCtx.clearRect(0, 0, WIDTH, HEIGHT);
        bkgCtx.setFill(Color.BLACK);
        bkgCtx.fillRect(0, 0, WIDTH, HEIGHT);

        if (running) {
            // Use background pattern related to level
            if (level % 4 == 0) {
                bkgCtx.setFill(bkgPatternFill4);
            } else if (level % 3 == 0) {
                bkgCtx.setFill(bkgPatternFill3);
            } else if (level % 2 == 0) {
                bkgCtx.setFill(bkgPatternFill2);
            } else {
                bkgCtx.setFill(bkgPatternFill1);
            }
            bkgCtx.fillRect(0, UPPER_INSET, WIDTH, HEIGHT);

            // Draw shadow
            bkgCtx.setFill(Color.rgb(0, 0, 0, 0.3));
            bkgCtx.fillRect(0, UPPER_INSET, 40, HEIGHT);
            bkgCtx.fillRect(0, UPPER_INSET, WIDTH, 20);
        } else {
            ctx.setFont(SCORE_FONT);
            ctx.setTextBaseline(VPos.TOP);
            ctx.setFill(HIGH_SCORE_RED);
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.fillText("HIGH SCORE", WIDTH * 0.5, IS_BROWSER ? 5 : 0); // Adding 5px in browser otherwise looks cut
            ctx.setFill(SCORE_WHITE);
            ctx.fillText(Long.toString(highscore), WIDTH * 0.5, IS_BROWSER ? 35 : 30);

            if (showStartHint) {
                ctx.fillText("Hit space to start", WIDTH * 0.5, HEIGHT * 0.6);
            }

            bkgCtx.drawImage(logoImg, (WIDTH - logoImg.getWidth()) * 0.5, HEIGHT * 0.25);

            bkgCtx.drawImage(copyrightImg, (WIDTH - copyrightImg.getWidth()) * 0.5, HEIGHT * 0.75);
        }
    }

    private void updateAndDraw() {
        ctx.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw Torpedos
        for (Torpedo torpedo : torpedoes) {
            torpedo.update();
            ctx.drawImage(torpedo.image, torpedo.bounds.x, torpedo.bounds.y);
        }

        // Draw shadows
        ctx.save();
        ctx.translate(10, 10);

        // Draw block shadows
        blocks.forEach(block -> ctx.drawImage(blockShadowImg, block.x, block.y));

        // Draw bonus block shadows
        bonusBlocks.forEach(bonusBlock -> ctx.drawImage(bonusBlockShadowImg, bonusBlock.x, bonusBlock.y));

        // Draw paddle shadow
        if (noOfLifes > 0) {
            switch (paddleState) {
                case STANDARD: ctx.drawImage(paddleStdShadowImg, paddle.bounds.minX, paddle.bounds.minY); break;
                case WIDE: ctx.drawImage(paddleWideShadowImg, paddle.bounds.minX, paddle.bounds.minY); break;
                case LASER: ctx.drawImage(paddleGunShadowImg, paddle.bounds.minX, paddle.bounds.minY); break;
            }
        }

        // Draw ball shadow
        balls.forEach(ball -> ctx.drawImage(ballShadowImg, ball.bounds.minX, ball.bounds.minY));
        ctx.restore();

        // Draw blocks
        blocks.forEach(block -> ctx.drawImage(block.image, block.x, block.y));

        // Draw bonus blocks
        bonusBlocks.forEach(bonusBlock -> {
            switch(bonusBlock.bonusType) {
                case BONUS_C: ctx.drawImage(bonusBlockCMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
                case BONUS_F: ctx.drawImage(bonusBlockFMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
                case BONUS_D: ctx.drawImage(bonusBlockDMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
                case BONUS_L: ctx.drawImage(bonusBlockLMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
                case BONUS_S: ctx.drawImage(bonusBlockSMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
                case BONUS_B: ctx.drawImage(bonusBlockBMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
                case BONUS_P: ctx.drawImage(bonusBlockPMapImg, bonusBlock.countX * BONUS_BLOCK_WIDTH, bonusBlock.countY * BONUS_BLOCK_HEIGHT, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT, bonusBlock.x, bonusBlock.y, BONUS_BLOCK_WIDTH, BONUS_BLOCK_HEIGHT); break;
            }
        });

        // Draw blinks
        blinks.forEach(blink -> ctx.drawImage(blinkMapImg, blink.countX * BLOCK_WIDTH, blink.countY * BLOCK_HEIGHT, BLOCK_WIDTH, BLOCK_HEIGHT, blink.x, blink.y, BLOCK_WIDTH, BLOCK_HEIGHT));

        // Draw enemies
        enemies.forEach(enemy -> {
            switch(enemy.enemyType) {
                case MOLECULE: ctx.drawImage(moleculeMapImg, enemy.countX * ENEMY_WIDTH, enemy.countY * ENEMY_HEIGHT, ENEMY_WIDTH, ENEMY_HEIGHT, enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT); break;
            }
        });

        // Draw explosions
        explosions.forEach(explosion -> ctx.drawImage(explosionMapImg, explosion.countX * EXPLOSION_WIDTH, explosion.countY * EXPLOSION_HEIGHT, EXPLOSION_WIDTH, EXPLOSION_HEIGHT, explosion.x, explosion.y, EXPLOSION_WIDTH, EXPLOSION_HEIGHT));

        // Draw ball(s)
        balls.forEach(ball -> {
            ball.update();
            ctx.drawImage(ballImg, ball.bounds.x, ball.bounds.y);
        });

        // Draw paddle
        if (noOfLifes > 0) {
            if (!movingPaddleOut) {
                paddle.update();
            }
            switch (paddleState) {
                case STANDARD: ctx.drawImage(paddleMapStdImg, paddle.countX * paddleState.width, paddle.countY * paddleState.height, paddleState.width, paddleState.height, paddle.x, paddle.y, paddleState.width, paddleState.height); break;
                case WIDE: ctx.drawImage(paddleMapWideImg, paddle.countX * paddleState.width, paddle.countY * paddleState.height, paddleState.width, paddleState.height, paddle.x, paddle.y, paddleState.width, paddleState.height); break;
                case LASER: ctx.drawImage(paddleMapGunImg, paddle.countX * paddleState.width, paddle.countY * paddleState.height, paddleState.width, paddleState.height, paddle.x, paddle.y, paddleState.width, paddleState.height); break;
            }
        } else {
            ctx.setFill(TEXT_GRAY);
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.fillText("GAME OVER", WIDTH * 0.5, HEIGHT * 0.75);
        }

        // Draw score
        ctx.setFill(Color.WHITE);
        ctx.setFont(SCORE_FONT);
        ctx.setTextAlign(TextAlignment.RIGHT);
        ctx.setTextBaseline(VPos.TOP);
        ctx.fillText(Long.toString(score), 140, IS_BROWSER ? 35 : 30);

        ctx.setFill(HIGH_SCORE_RED);
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.fillText("HIGH SCORE", WIDTH * 0.5, IS_BROWSER ? 5 : 0); // Adding 5px in browser otherwise looks cut
        ctx.setFill(SCORE_WHITE);
        ctx.fillText(Long.toString(score > highscore ? score : highscore), WIDTH * 0.5, IS_BROWSER ? 35: 30);

        // Draw no of lifes
        for (int i = 0 ; i < noOfLifes ; i++) {
            ctx.drawImage(paddleMiniImg, INSET + 2 + 42 * i, HEIGHT - 30);
        }

        // Draw ready level label
        if (readyLevelVisible) {
            ctx.setFill(TEXT_GRAY);
            ctx.setFont(SCORE_FONT);
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.fillText("ROUND " + level, WIDTH * 0.5, HEIGHT * 0.65);
            ctx.fillText("READY", WIDTH * 0.5, HEIGHT * 0.65 + 2 * SCORE_FONT.getSize());
        }

        // Remove sprites
        balls.removeIf(ball -> ball.toBeRemoved);
        blinks.removeIf(blink -> blink.toBeRemoved);
        blocks.removeIf(block -> block.toBeRemoved);
        bonusBlocks.removeIf(bonusBlock -> bonusBlock.toBeRemoved);
        enemies.removeIf(enemy -> enemy.toBeRemoved);
        explosions.removeIf(explosion -> explosion.toBeRemoved);
        torpedoes.removeIf(torpedo -> torpedo.toBeRemoved);

        // Respawn ball and check for game over
        if (!movingPaddleOut && balls.isEmpty() && noOfLifes > 0) {
            noOfLifes -= 1;
            if (noOfLifes == 0) { gameOver(); }
            spawnBall();
        }

        // Update blinks
        blinks.forEach(blink -> blink.update());

        // Check for level completeness
        if (blocks.isEmpty() || blocks.stream().filter(block -> block.maxHits > -1).count() == 0) {
            level++;
            if (level > Constants.LEVEL_MAP.size()) { level = 1; }
            startLevel(level);
        }
    }

    private void drawBorder() {
        brdrCtx.clearRect(0, 0, WIDTH, HEIGHT);
        if (running) {
            // Draw top border
            brdrCtx.setFill(pipePatternFill);
            brdrCtx.fillRect(17, 68, 83, 17);
            brdrCtx.fillRect(100 + topDoorImg.getWidth(), 68, WIDTH - 200 - 2 * topDoorImg.getWidth(), 17);
            brdrCtx.fillRect(WIDTH - 100, 68, 83, 17);

            // Draw vertical border
            brdrCtx.setFill(borderPatternFill);
            brdrCtx.fillRect(0, UPPER_INSET, 20, HEIGHT - UPPER_INSET);
            if (nextLevelDoorOpen) {
                brdrCtx.fillRect(WIDTH - 20, UPPER_INSET, 20, 563 );
                brdrCtx.fillRect(WIDTH - 20, UPPER_INSET + 565 + borderPartVerticalImg.getHeight(), 20, 100);
            } else {
                brdrCtx.fillRect(WIDTH - 20, UPPER_INSET, 20, HEIGHT);
            }

            // Draw border corners
            brdrCtx.drawImage(ulCornerImg, 2.5, 67.5);
            brdrCtx.drawImage(urCornerImg, WIDTH - urCornerImg.getWidth() - 2.5, 67.5);

            // Draw next level door
            if (nextLevelDoorOpen) {
                for (int i = 0 ; i < 6 ; i++) {
                    brdrCtx.drawImage(borderPartVerticalImg, 0, UPPER_INSET + i * 113);
                    if (i < 5) {
                        brdrCtx.drawImage(borderPartVerticalImg, WIDTH - 20, UPPER_INSET + i * 113);
                    }
                }
                if (nextLevelDoorAlpha > 0.01) {
                    nextLevelDoorAlpha -= 0.01;
                }
                brdrCtx.save();
                brdrCtx.setGlobalAlpha(nextLevelDoorAlpha);
                brdrCtx.drawImage(borderPartVerticalImg, WIDTH - 20, UPPER_INSET + 565);
                brdrCtx.restore();

                openDoor.update();
                ctx.drawImage(openDoorMapImg, openDoor.countX * 20, 0, 20, 71, WIDTH - 20, UPPER_INSET + 565, 20, 71);
            } else {
                for (int i = 0; i < 6; i++) {
                    brdrCtx.drawImage(borderPartVerticalImg, 0, UPPER_INSET + i * 113);
                    brdrCtx.drawImage(borderPartVerticalImg, WIDTH - 20, UPPER_INSET + i * 113);
                }
            }

            // Draw upper doors
            brdrCtx.save();
            brdrCtx.setGlobalAlpha(topLeftDoorAlpha);
            brdrCtx.drawImage(topDoorImg, 100, 65);
            brdrCtx.setGlobalAlpha(topRightDoorAlpha);
            brdrCtx.drawImage(topDoorImg, WIDTH - 100 - topDoorImg.getWidth(), 65);
            brdrCtx.restore();
        }
    }


    // ******************** Inner Classes *************************************
    private abstract class Sprite {
        public Image     image;
        public Bounds    bounds;
        public double    x; // Center of Sprite in x-direction
        public double    y; // Center of Sprite in y-direction
        public double    r;
        public double    vX;
        public double    vY;
        public double    vR;
        public double    width;
        public double    height;
        public double    size;
        public double    radius;
        public boolean   toBeRemoved;


        // ******************** Constructors **************************************
        public Sprite() {
            this(null, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final Image image) {
            this(image, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final Image image, final double x, final double y) {
            this(image, x, y, 0, 0, 0, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double vX, final double vY) {
            this(image, x, y, 0, vX, vY, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double r, final double vX, final double vY) {
            this(image, x, y, r, vX, vY, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double r, final double vX, final double vY, final double vR) {
            this.image       = image;
            this.x           = x;
            this.y           = y;
            this.r           = r;
            this.vX          = vX;
            this.vY          = vY;
            this.vR          = vR;
            this.width       = null == image ? 0 : image.getWidth();
            this.height      = null == image ? 0 : image.getHeight();
            this.size        = this.width > this.height ? width : height;
            this.radius      = this.size * 0.5;
            this.toBeRemoved = false;
            this.bounds      = null == image ? new Bounds(0, 0, 0, 0) : new Bounds(x - image.getWidth() * 0.5, y - image.getHeight() * 0.5, image.getWidth(), image.getHeight());
        }


        // ******************** Methods *******************************************
        protected void init() {}

        public void respawn() {}

        public abstract void update();
    }

    public abstract class AnimatedSprite extends Sprite {
        protected final int    maxFrameX;
        protected final int    maxFrameY;
        protected       double scale;
        protected       int    countX;
        protected       int    countY;


        // ******************** Constructors **************************************
        public AnimatedSprite(final int maxFrameX, final int maxFrameY, final double scale) {
            this(0, 0, 0, 0, 0, 0, maxFrameX, maxFrameY, scale);
        }
        public AnimatedSprite(final double x, final double y, final double vX, final double vY, final int maxFrameX, final int maxFrameY, final double scale) {
            this(x, y, 0, vX, vY, 0, maxFrameX, maxFrameY, scale);
        }
        public AnimatedSprite(final double x, final double y, final double r, final double vX, final double vY, final double vR, final int maxFrameX, final int maxFrameY, final double scale) {
            super(null, x, y, r, vX, vY, vR);
            this.maxFrameX = maxFrameX;
            this.maxFrameY = maxFrameY;
            this.scale     = scale;
            this.countX    = 0;
            this.countY    = 0;
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countX == maxFrameX && countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Paddle extends AnimatedSprite {

        // ******************** Constructors **************************************
        public Paddle() {
            super(WIDTH * 0.5 - paddleState.width * 0.5, HEIGHT - PADDLE_OFFSET_Y, 0, 0, 8, 8, 1.0);
            init();
        }


        // ******************** Methods *******************************************
        @Override protected void init() {
            this.width  = paddleState.width;
            this.height = paddleState.height;
            this.size   = height;
            this.radius = size * 0.5;
            this.bounds.set(this.x, this.y, paddleState.width, this.height);
        }

        @Override public void respawn() {
            this.x  = WIDTH * 0.5;
            this.bounds.set(this.x, this.y, paddleState.width, this.height);
            this.vX = 0;
            this.vY = 0;
        }

        @Override public void update() {
            x += vX;

            if (x + paddleState.width > WIDTH - INSET) {
                if (nextLevelDoorOpen && !movingPaddleOut) { movingPaddleOut = true; }
                x = WIDTH - INSET - paddleState.width;
            }
            if (x < INSET) {
                x = INSET;
            }
            this.bounds.set(this.x, this.y, paddleState.width, this.height);

            countX = animateInc;
            if (countX == maxFrameX) {
                countX     = 0;
                animateInc = 0;
                countY++;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Blink extends AnimatedSprite {

        // ******************** Constructors **************************************
        public Blink(final double x, final double y) {
            super(x, y, 0, 0, 8, 3, 1.0);
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countX == maxFrameX && countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Block extends Sprite {
        public       int       value;
        public       int       hits;
        public final int       maxHits;
        public final BlockType blockType;


        // ******************** Constructors **************************************
        public Block(final Image image, final double x, final double y, final int value, final int maxHits, final BlockType blockType) {
            super(image);
            this.x           = x;
            this.y           = y;
            this.value       = value;
            this.maxHits     = maxHits;
            this.blockType   = blockType;
            this.hits        = 0;
            this.width       = BLOCK_WIDTH;
            this.height      = BLOCK_HEIGHT;
            this.bounds.set(x, y, width, height);
            init();
        }


        // ******************** Methods *******************************************
        @Override protected void init() {
            size   = width > height ? width : height;
            radius = size * 0.5;

            // Velocity
            vX = 0;
            vY = 0;
        }

        @Override public void update() { }

        @Override public String toString() { return new StringBuilder().append(blockType).append("(").append(x).append(",").append(y).append(")").toString(); }

        public boolean equals(final Block other) {
            return this.blockType == other.blockType &&
                   this.x         == other.x &&
                   this.y         == other.y &&
                   this.value     == other.value;
        }
    }

    private class BonusBlock extends AnimatedSprite {
        public BonusType bonusType;


        // ******************** Constructors **************************************
        public BonusBlock(final double x, final double y, final BonusType bonusType) {
            super(x, y, 0, BONUS_BLOCK_SPEED, 5, 4, 1.0);
            this.bonusType   = bonusType;
            this.width       = BLOCK_WIDTH;
            this.height      = BLOCK_HEIGHT;
            this.bounds.set(x, y, width, height);
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            y += vY;
            if (y > HEIGHT) {
                toBeRemoved = true;
            }
            countX++;
            if (countX == maxFrameX) {
                countY++;
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
            this.bounds.set(this.x, this.y, this.width, this.height);
        }
    }

    private final static double BIG_VALUE = 100_000; // Big enough to ensure the ball won't cross the borders
    private final static Bounds[] BORDER_BOUNDS = {
            new Bounds(-BIG_VALUE + INSET * 0.8, -BIG_VALUE, BIG_VALUE, 2 * BIG_VALUE), // left border bounds
            new Bounds(WIDTH - INSET * 0.8, -BIG_VALUE, BIG_VALUE, 2 * BIG_VALUE), // right border bounds
            new Bounds(-BIG_VALUE, -BIG_VALUE + UPPER_INSET, 2 * BIG_VALUE, BIG_VALUE) // top border bounds
    };

    private class Ball extends Sprite {
        public boolean active;
        public long    bornTimestamp;


        // ******************** Constructors **************************************
        public Ball(final Image image, final double x, final double y, final double vX) {
            this(image, x, y, vX, false);
        }
        public Ball(final Image image, final double x, final double y, final double vX, final boolean active) {
            super(image, paddle.bounds.centerX, paddle.bounds.minY - image.getHeight() * 0.5 - BALL_SPEED - 1, 0, -ballSpeed);
            this.vX            = vX;
            this.active        = active;
            this.bornTimestamp = Instant.now().getEpochSecond();
        }
        private Ball(final Image image, final double x, final double y, final double vX, final double vY) {
            super(image, x, y, vX, vY);
            this.active        = true;
            this.bornTimestamp = Instant.now().getEpochSecond();
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            if (!active) {
                this.x = paddle.bounds.centerX;
                this.y = paddle.bounds.minY - image.getHeight() * 0.5 - BALL_SPEED - 1;
            } else { // We need to check if the ball hits a block
                double x0 = x, y0 = y, x1 = x0 + vX, y1 = y0 + vY; // (x0, y0) = initial coordinates, (x1, y1) = final coordinates in case there is no hit
                // (x1, y1) may need a correction if the ball hits a block
                while (true) { // Several iterations are possible, because even the corrected (x1, y1) might hit another block
                    double fx0 = x0, fy0 = y0, fx1 = x1, fy1 = y1; // Capturing final values for the lambda expression below
                    BallHit ballHit = Stream.concat(blocks.stream().map(b -> b.bounds), // Iterating over all block bounds
                                    Stream.concat(Arrays.stream(BORDER_BOUNDS), Stream.of(paddle.bounds))) // together with the borders and paddle bounds (processed identically)
                            .map(bounds -> bounds.computeBallHit(fx0, fy0, fx1, fy1, radius)) // computing a possible ball hit with the bounds (returns null if no hits)
                            .filter(Objects::nonNull) // removing non-hits
                            // If that trajectory (x0, y0) -> (x1, y1) hits several blocks, we keep the first hit block
                            .min(Comparator.comparingDouble(ballHit1 -> ballHit1.beforeHitDistance)) // first hit = min hit distance (x0, y0) -> (xHit, yHit)
                            .orElse(null); // returning null if no hit
                    if (ballHit == null) { // If there is no hit, (x1, y1) doesn't need correction
                        this.x = x1;
                        this.y = y1;
                        break; // breaking the loop
                    }
                    // We have a ball hit when reaching this code.
                    // => inverting vX or Vy if needed
                    if (ballHit.inverseVx)
                        vX = -vX;
                    if (ballHit.inverseVy)
                        vY = -vY;
                    // Preparing the next loop iteration by updating (x0, y0) -> (x1, y1) to (xHit, yHit) -> (correctedX, correctedY)
                    x0 = ballHit.xHit;
                    y0 = ballHit.yHit;
                    x1 = ballHit.correctedX;
                    y1 = ballHit.correctedY;
                    // But before looping, we manage some special case when hitting the paddle or the blocks
                    if (ballHit.hitBounds == paddle.bounds) {
                        Bounds pb = paddle.bounds;
                        if (stickyPaddle) {
                            this.x       = pb.centerX;
                            this.y       = pb.minY - image.getHeight() * 0.5 - BALL_SPEED - 1;
                            this.active  = false;
                            break;
                        } else {
                            // Influence vX of ball if vX of paddle != 0
                            if (paddle.vX != 0) {
                                double speedXY = Math.sqrt(vX * vX + vY * vY);
                                double posX    = (x1 - pb.centerX) / (pb.width * 0.5);
                                double speedX  = speedXY * posX * BALL_VX_INFLUENCE;
                                vX = speedX;
                                vY = - Math.sqrt(speedXY * speedXY - speedX * speedX);
                                // Recalculating (x1, y1) from this new speed
                                x1 = x0 + vX;
                                y1 = y0 + vY;
                            } else { // angle correction due to paddle round corners
                                double pcr = pb.height; // paddle corner radius
                                boolean hitLeftCorner = x0 < pb.minX + pcr; // Note: x0 is the hit X at this point
                                boolean hitRightCorner = x0 > pb.maxX - pcr;
                                if (hitLeftCorner || hitRightCorner) {
                                    double distanceToCorner = hitLeftCorner ? pb.minX + pcr - x0 : x0 - pb.maxX + pcr;
                                    double cornerAngleRad = Math.acos(distanceToCorner * 0.85 / pcr); // Note: 0.85 factor is because the corner is not as sharp as a circle at the edge
                                    double minRad = Math.PI / 180 * 35; // To avoid the speed to be too horizontal
                                    double maxRad = Math.PI / 180 * 75; // Better to keep the hit speed
                                    // Low angle correction => minRad
                                    if (Double.isNaN(cornerAngleRad) || cornerAngleRad < minRad)
                                        cornerAngleRad = minRad;
                                    // Keeping the hit speed if to closed to 90°
                                    if (cornerAngleRad < maxRad) {
                                        // Applying the new speed angle
                                        vY = BALL_SPEED * Math.sin(cornerAngleRad) * (y0 > pb.centerY ? 1 : -1); // Not inverting vY if the hit is too low (-> user will lose the ball, sorry)
                                        vX = BALL_SPEED * Math.cos(cornerAngleRad) * (hitLeftCorner ? -1 : 1);
                                        // Recalculating (x1, y1) from this new speed
                                        x1 = x0 + vX;
                                        y1 = y0 + vY;
                                    }
                                }
                            }
                        }
                        playSound(ballPaddleSnd);
                    }
                    // We retrieve the block hit by the ball for the hit sound, block blink & score management:
                    Block block = blocks.stream().filter(b -> b.bounds == ballHit.hitBounds).findFirst().orElse(null);
                    if (block != null) { // Can be null if the ball hit something else (paddle or border)
                        switch (block.blockType) {
                            case GOLD: {
                                playSound(ballHardBlockSnd);
                                blinks.add(new Blink(block.bounds.minX, block.bounds.minY));
                                break;
                            }
                            case GRAY: {
                                block.hits++;
                                if (block.hits == block.maxHits) {
                                    score += level * 50;
                                    blockCounter += 1;
                                    block.toBeRemoved = true;
                                    playSound(ballBlockSnd);
                                } else {
                                    playSound(ballHardBlockSnd);
                                    blinks.add(new Blink(block.bounds.minX, block.bounds.minY));
                                }
                                break;
                            }
                            default: {
                                block.hits++;
                                if (block.hits >= block.maxHits) {
                                    score += block.value;
                                    blockCounter += 1;
                                    block.toBeRemoved = true;
                                    playSound(ballBlockSnd);
                                    if (blockCounter % BONUS_BLOCK_INTERVAL == 0) {
                                        bonusBlocks.add(new BonusBlock(block.x, block.y, BonusType.values()[RND.nextInt(BonusType.values().length)]));
                                    }
                                }
                            }
                        }
                        blockFifo.add(block);
                        // Checking for bounds
                        final List<Block> items = blockFifo.getItems();
                        if (items.size() == 9) {
                            if (items.get(0).equals(items.get(6)) && items.get(1).equals(items.get(5)) && items.get(1).equals(items.get(7)) && items.get(2).equals(items.get(4)) && items.get(2).equals(items.get(8))) {
                                this.vX += 0.1;
                            } else if (items.get(0).equals(items.get(8)) && items.get(1).equals(items.get(7)) && items.get(2).equals(items.get(6)) && items.get(3).equals(items.get(5))) {
                                this.vX += 0.1;
                            }
                        }
                    }
                }
            }

            this.bounds.set(this.x - this.width * 0.5, this.y - this.height * 0.5, this.width, this.height);

            // Hit test ball with enemies
            for (Enemy enemy : enemies) {
                boolean ballHitsEnemy = this.bounds.intersects(enemy.bounds);
                if (ballHitsEnemy) {
                    enemy.toBeRemoved = true;
                    explosions.add(new Explosion(enemy.x, enemy.y, enemy.vX, enemy.vY, 1.0));
                    playSound(explosionSnd);
                    if (bounds.centerX > enemy.bounds.minX && bounds.centerX < enemy.bounds.maxX) {
                        // Top or Bottom hit
                        vY = -vY;
                    } else if (bounds.centerY > enemy.bounds.minY && bounds.centerY < enemy.bounds.maxY) {
                        // Left or Right hit
                        vX = -vX;
                    } else {
                        double dx = Math.abs(bounds.centerX - enemy.bounds.centerX) - enemy.bounds.width * 0.5;
                        double dy = Math.abs(bounds.centerY - enemy.bounds.centerY) - enemy.bounds.height * 0.5;
                        if (dx > dy) {
                            // Left or Right hit
                            vX = -vX;
                        } else {
                            // Top or Bottom hit
                            vY = -vY;
                        }
                    }
                    break;
                }
            }

            if (Double.compare(vX, 0) == 0) { vX = 0.5; }

            if (this.bounds.maxY > HEIGHT) {
                this.toBeRemoved = true;
            }
        }
    }

    private class Torpedo extends Sprite {

        // ******************** Constructors **************************************
        public Torpedo(final Image image, final double x, final double y) {
            super(image, x, y - image.getHeight(), 0, TORPEDO_SPEED);
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            y -= vY;
            this.bounds.set(this.x - this.width * 0.5, this.y - this.height * 0.5, this.width, this.height);
            if (bounds.minY < UPPER_INSET) {
                toBeRemoved = true;
            }
        }
    }

    private class OpenDoor extends AnimatedSprite {

        // ******************** Constructors **************************************
        public OpenDoor(final double x, final double y) {
            super(x, y, 0, 0, 3, 0, 1.0);
            this.bounds.set(x, y, width, height);
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            countX++;
            if (countX == maxFrameX) {
                countY++;
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Enemy extends AnimatedSprite {
        public EnemyType enemyType;
        private double initialVx;


        // ******************** Constructors **************************************
        public Enemy(final double x, final double y, final EnemyType enemyType) {
            super(x, y, 0, ENEMY_SPEED, 8, 3, 1.0);
            this.initialVx   = RND.nextDouble() * ENEMY_SPEED;
            this.vX          = RND.nextBoolean() ? initialVx : -initialVx;
            this.enemyType   = enemyType;
            this.width       = ENEMY_WIDTH;
            this.height      = ENEMY_HEIGHT;
            this.radius      = Math.sqrt(ENEMY_HEIGHT * ENEMY_HEIGHT * 0.25 + ENEMY_WIDTH * ENEMY_WIDTH * 0.25);
            this.bounds.set(this.x - this.width * 0.5, this.y - this.height * 0.5, this.width, this.height);
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            x += vX;
            y += vY;

            if (bounds.maxX > WIDTH - INSET) {
                this.x  = WIDTH - INSET - this.width;
                this.vX = -initialVx;
            }
            if (bounds.minX < INSET) {
                this.x  = INSET;
                this.vX = Math.abs(initialVx);
            }
            if (bounds.minY < UPPER_INSET) {
                this.y  = UPPER_INSET;
                this.vY = 3.0;
            }

            this.bounds.set(this.x, this.y, this.width, this.height);

            // Hit test enemy with blocks
            for (Block block : blocks) {
                boolean enemyHitsBlock = this.bounds.intersects(block.bounds);
                if (enemyHitsBlock) {
                    if (bounds.centerX > block.bounds.minX && bounds.centerX < block.bounds.maxX) {
                        // Top or Bottom hit
                        vY = -vY;
                    } else if (bounds.centerY > block.bounds.minY && bounds.centerY < block.bounds.maxY) {
                        // Left or Right hit
                        vX = -vX;
                    } else {
                        double dx = Math.abs(bounds.centerX - block.bounds.centerX) - block.bounds.width * 0.5;
                        double dy = Math.abs(bounds.centerY - block.bounds.centerY) - block.bounds.height * 0.5;
                        if (dx > dy) {
                            // Left or Right hit
                            vX = -vX;
                        } else {
                            // Top or Bottom hit
                            vY = -vY;
                        }
                    }
                    break;
                }
            }

            // Hit test enemy with paddle
            if (bounds.intersects(paddle.bounds)) {
                this.toBeRemoved = true;
                explosions.add(new Explosion(this.x, this.y, this.vX, this.vY, 1.0));
                playSound(explosionSnd);
            }

            if (y > HEIGHT) { toBeRemoved = true; }

            countX++;
            if (countX == maxFrameX) {
                countY++;
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Explosion extends AnimatedSprite {

        // ******************** Constructors **************************************
        public Explosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 4, 4, scale);
        }


        // ******************** Methods *******************************************
        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countX == maxFrameX && countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    public static class Bounds {
        public double x;
        public double y;
        public double width;
        public double height;
        public double minX;
        public double minY;
        public double maxX;
        public double maxY;
        public double centerX;
        public double centerY;


        // ******************** Constructors **************************************
        public Bounds() {
            this(0, 0, 0, 0);
        }
        public Bounds(final double width, final double height) {
            this(0, 0, width, height);
        }
        public Bounds(final double x, final double y, final double width, final double height) {
            set(x, y, width, height);
        }


        // ******************** Methods *******************************************
        public void set(final Bounds bounds) {
            set(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        public void set(final double x, final double y, final double width, final double height) {
            this.x       = x;
            this.y       = y;
            this.width   = width;
            this.height  = height;
            this.minX    = x;
            this.minY    = y;
            this.maxX    = x + width;
            this.maxY    = y + height;
            this.centerX = x + width * 0.5;
            this.centerY = y + height * 0.5;
        }

        public boolean contains(final double x, final double y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }

        public boolean intersects(final Bounds other) {
            return other.minX <= maxX && minX <= other.maxX && other.minY <= maxY && minY <= other.maxY;
        }

        private BallHit computeBallHit(double x0, double y0, double x1, double y1, double r) {
            // Increased +r bounds to simplify computation and focus only on ball center
            double minXr = minX - r, minYr = minY - r, maxXr = maxX + r, maxYr = maxY + r;
            double yHit = 0, xHit = 0;
            boolean hit = false, inverseVy = false, inverseVx = false;
            // Did the ball hit the bottom border?
            if (y0 >= maxYr && y1 <= maxYr) { // Means that the ball crossed the bottom line (while moving up)
                xHit = computeLineIntersectionX(-1, maxYr, 1, maxYr, x0, y0, x1, y1); // Where on X?
                hit = xHit >= minXr && xHit <= maxXr; // X condition for a hit
                if (hit) {
                    yHit = maxYr;
                    inverseVy = true;
                }
            }
            // If not, did it hit the top border?
            if (!hit && y0 <= minYr && y1 >= minYr) { // Means that the ball crossed the top line (while moving down)
                xHit = computeLineIntersectionX(-1, minYr, 1, minYr, x0, y0, x1, y1); // Where on X?
                hit = xHit >= minXr && xHit <= maxXr; // X condition for a hit
                if (hit) {
                    yHit = minYr;
                    inverseVy = true;
                }
            }
            // If not, did it hit the left border?
            if (!hit && x0 <= minXr && x1 >= minXr) { // Means that the ball crossed the left line (while moving to right)
                yHit = computeLineIntersectionY(minXr, 1, minXr, -1, x0, y0, x1, y1); // Where on Y?
                hit = yHit >= minYr && yHit <= maxYr; // Y condition for a hit
                if (hit) {
                    xHit = minXr;
                    inverseVx = true;
                }
            }
            // If not, did it hit the right border?
            if (!hit && x0 >= maxXr && x1 <= maxXr) { // Means that the ball crossed the right line (while moving to left)
                yHit = computeLineIntersectionY(maxXr, 1, maxXr, -1, x0, y0, x1, y1); // Where on Y?
                hit = yHit >= minYr && yHit <= maxYr; // Y condition for a hit
                if (hit) {
                    xHit = maxXr;
                    inverseVx = true;
                }
            }
            // If not, is the ball inside the bounds? (this may happen with the paddle moving quickly)
            if (!hit && contains(x1, y1)) {
                hit = true;
                xHit = computeLineIntersectionX(-1, minYr, 1, minYr, x0, y0, x1, y1); // Where on X?
                yHit = minYr;
                //x0 = x1 = xHit;
                //y0 = y1 = yHit;
                inverseVy = true;
            }
            if (!hit)
                return null;
            BallHit ballHit = new BallHit(this);
            ballHit.xHit = xHit;
            ballHit.yHit = yHit;
            ballHit.inverseVx = inverseVx;
            ballHit.inverseVy = inverseVy;
            ballHit.beforeHitDistance = distance(x0, y0, xHit, yHit);
            double afterHitDistance = distance(xHit, yHit, x1, y1);
            ballHit.correctedX = inverseVx ? xHit - (x1 - xHit) * afterHitDistance : x1;
            ballHit.correctedY = inverseVy ? yHit - (y1 - yHit) * afterHitDistance : y1;
            return ballHit;
        }
    }

    private static double computeLineIntersectionX(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return computeLineIntersection(x1, y1, x2, y2, x3, y3, x4, y4, true);
    }

    private static double computeLineIntersectionY(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return computeLineIntersection(x1, y1, x2, y2, x3, y3, x4, y4, false);
    }

    private static double computeLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, boolean x) {
        double a1 = y2 - y1;
        double b1 = x1 - x2;
        double c1 = a1 * x1 + b1 * y1;

        double a2 = y4 - y3;
        double b2 = x3 - x4;
        double c2 = a2 * x3 + b2 * y3;

        double delta = a1 * b2 - a2 * b1;
        return x ? (b2 * c1 - b1 * c2) / delta : (a1 * c2 - a2 * c1) / delta;
    }

    private static double distance(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0, dy = y1 - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static class BallHit {
        private final Bounds hitBounds;
        private double beforeHitDistance;
        private double xHit;
        private double yHit;
        private double correctedX;
        private double correctedY;
        private boolean inverseVx;
        private boolean inverseVy;

        public BallHit(Bounds hitBounds) {
            this.hitBounds = hitBounds;
        }
    }

    // ******************** Start *********************************************
    public static void main(String[] args) {
        launch(args);
    }
}