package aq.oceanbase.skyscroll.logic;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.data.QuestionDBHelper;
import aq.oceanbase.skyscroll.data.SoundManager;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.text.ScoreBar;
import aq.oceanbase.skyscroll.graphics.elements.window.QuestionWindow;
import aq.oceanbase.skyscroll.graphics.elements.window.Window;
import aq.oceanbase.skyscroll.graphics.render.RenderContainer;
import aq.oceanbase.skyscroll.graphics.elements.background.Background;
import aq.oceanbase.skyscroll.logic.events.WindowEvent;
import aq.oceanbase.skyscroll.logic.events.WindowEventListener;
import aq.oceanbase.skyscroll.logic.menu.MenuController;
import aq.oceanbase.skyscroll.logic.questions.Question;
import aq.oceanbase.skyscroll.logic.questions.QuestionManager;
import aq.oceanbase.skyscroll.logic.tree.nodes.Node;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.touch.TouchHandler2D;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.touch.Touchable2D;
import aq.oceanbase.skyscroll.utils.math.MathUtilities;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.sql.SQLException;
import java.util.*;

public class Game implements Touchable2D {

    //TODO: check blocks initialization, issue can be over there
    public static final int QUESTIONS_AMOUNT = 4;

    public static enum MODE {
        TREE, QUESTION, MENU
    }

    public static enum ANSWER {
        CORRECT, WRONG, NONE
    }

    private MODE mDrawmode = MODE.MENU;
    private boolean mModeSwitched = false;

    private int[] mScreenMetrics;

    private Context mContext;

    public Deque<GameContext> mGameContextStack;

    //Managers
    private MenuController mMenuController;

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 5.0f;
    private float mMaxDist = 30.0f;

    private int mBlinkTime = 800;
    private long mTimer = -1;

    private float mDefaultVolume = 0.4f;
    private float mFadedVolume = 0.25f;

    //Tree
    public GameSession mGameSession;

    private int mCurrentNode;

    //Gameplay variables
    private int mScore = 0;

    //Contexts
    private RenderContainer mBackgroundRenderables = new RenderContainer();
    private RenderContainer mUIRenderables = new RenderContainer();

    private GameContext mTreeContext;

    //Backgrounds
    private int mGridTiles;
    private float mGridTileSize = 0.0f;
    private float mGridShiftFactor = 0.0f;
    private float mBackgroundShiftFactor = 0.3f / ((mMaxHeight - mMinHeight) + 1.0f);


    public Background mTreeBackground;
    private Background mGridBackground;

    // HUD
    private ScoreBar mScoreBar;

    private MediaPlayer mMediaPlayer;
    private SoundManager mSoundManager;

    private QuestionManager mQuestionManager;

    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = mMinHeight;

    private Camera mCamera = new Camera(new Vector3f(0.0f, 0.0f, mDistance),
            new Vector3f(0.0f, 0.0f, -1.0f),
            new Vector3f(0.0f, 1.0f, 0.0f));

    //Touch variables
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);

    private final TouchHandler mTreeTouchHandler = new TouchHandler() {
        @Override
        public void onSwipeHorizontal(float amount) {
            mMomentum.x = amount;
        }

        @Override
        public void onSwipeVertical(float amount) {
            mMomentum.y = amount;
        }

        @Override
        public void onScale(float span) {
            if (Math.abs(span) > 0.1) mDistance = mDistance - span;
            if (mDistance <= mMinDist) mDistance = mMinDist;
            else if (mDistance > mMaxDist) mDistance = mMaxDist;
        }

        @Override
        public void onTap(TouchRay touchRay) {
            mSoundManager.playRandomClick();
            //mCurrentNode = mGameSession.tree.performRaySelection(new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics));
            mCurrentNode = mGameSession.tree.performRaySelection(touchRay);
            if (mCurrentNode != -1) {
                openNodeQuestion(mCurrentNode);
            }
        }
    };



    //Events
    public class WindowListener implements WindowEventListener {
        @Override
        public void onClose(WindowEvent e) {

            mGameContextStack.pop().onPop();
            mMediaPlayer.setVolume( mDefaultVolume, mDefaultVolume );
            //mGameContextStack.push(mTreeContext);
        }

        @Override
        public void onAnswer(WindowEvent e) {
            updateNodeStatus(mCurrentNode, e.isAnsweredCorrectly());
            if (e.isAnsweredCorrectly()) {
                mScore += 100;
                mScoreBar.setScore(mScore);
                Log.e("Debug", "" + mScore);
            }
        }
    }

    ///////////////////////////////////////
    //////////// CONSTRUCTOR //////////////
    ///////////////////////////////////////
    public Game(Context context) {
        mGameSession = new GameSession();
        mContext = context;

        mGridTiles = 1;

        mTreeBackground = new Background(R.drawable.bckgnd2, 0.5f);
        mGridBackground = new Background(R.drawable.grid, 1.0f);

        mSoundManager = new SoundManager(context);
        mQuestionManager = new QuestionManager(context);

        mBackgroundRenderables.clear();
        mBackgroundRenderables.addRenderable(mTreeBackground).addRenderable(mGridBackground);
        mBackgroundRenderables.lock();

        mScoreBar = new ScoreBar(ScoreBar.SCOREALIGN.RIGHT);
        mScoreBar.setScore(mScore);

        mUIRenderables.clear();
        mUIRenderables.addRenderable(mScoreBar);
        mUIRenderables.lock();

        mSoundManager = new SoundManager(context);
        mQuestionManager = new QuestionManager(context);

        createTreeContext();

        mGameContextStack = new ArrayDeque<>();
    }



    public void setScreenMetrics(int[] screenMetrics) {
        this.mScreenMetrics = screenMetrics;
        Log.e("Debug", "Metrics set: " + mScreenMetrics[2] + " " + mScreenMetrics[3]);
        this.onScreenMetricsUpdate();
    }

    public void onRenderStarted() {
        int[] metrics = this.getScreenMetrics();
        Log.e("Debug", "MenuCrationInputGame: " + metrics[2] + " " + metrics[3]);
        mMenuController = new MenuController(this);

        GameContext menuContext = new GameContext();
        menuContext.getRenderContainer().addRenderable(mMenuController.getCurrentMenuRenderContainer());

        mGameContextStack.push( createMenuContext() );
        //switchMode(MODE.MENU);

        mCamera.getPos().print("Debug", "Cam start pos");

        mMediaPlayer = MediaPlayer.create(mContext, R.raw.sky_loop);
        mMediaPlayer.setVolume( mDefaultVolume, mDefaultVolume );
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.start();
            }

        });

        mMediaPlayer.start();
    }

    public void onReinit() {
        mBackgroundRenderables.lock();
        mUIRenderables.lock();
    }

    public void onScreenMetricsUpdate() {
        mGridTileSize = (float)mScreenMetrics[3]/(float)mScreenMetrics[2];
        //mGridTileSize = 1.3f;
        mGridShiftFactor = mGridTiles / (((mMaxHeight - mMinHeight) + 1.0f) * mGridTileSize);
        updateBackground();

        mScoreBar.onScreenMetricsSet(mScreenMetrics[2] - 10, mScreenMetrics[3] + 5, 70, mScreenMetrics);

        Log.e("Debug", "Width: " + mScreenMetrics[2] + "Height: " + mScreenMetrics[3]);
        Log.e("Debug", "Grid Factor " + mGridShiftFactor);
        Log.e("Debug", "Tiles At Screen " + mGridTileSize);
    }

    public void onActivityPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void onActivityResume() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.sky_loop);
            mMediaPlayer.setVolume( mDefaultVolume, mDefaultVolume );
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.start();
                }

            });
        }
        else {
            mMediaPlayer.start();
        }
    }


    public void changeCurrentContext(GameContext gameContext) {
        mGameContextStack.pop().onPop();
        mGameContextStack.push(gameContext);
    }

    public void addContext(GameContext gameContext) {
        mGameContextStack.push(gameContext);
    }


    //<editor-fold desc="Getters and Setters">
    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera cam) {
        this.mCamera = cam;
    }

    public float getHeight() {
        return this.mHeight;
    }

    public void setHeight(float height) {
        if (height < mMinHeight) this.mHeight = mMinHeight;
        else if (height > mMaxHeight) this.mHeight = mMaxHeight;
        else this.mHeight = height;
    }

    public void setContext (Context context) {
        this.mContext = context;
    }

    public int[] getScreenMetrics() {
        return this.mScreenMetrics;
    }
    //</editor-fold>


    //<editor-fold desc="Updaters">
    private void updateAngle() {
        mGameSession.tree.updateAngle(mMomentum.x);
    }

    private void updateHeight() {
        mHeight = mHeight + mMomentum.y;
        if (mHeight > mMaxHeight) mHeight = mMaxHeight;
        if (mHeight < mMinHeight) mHeight = mMinHeight;
    }

    private void updateMomentum() {
        if (mMomentum.x != 0.0f) {
            mMomentum.x = MathUtilities.decrementConvergingValue(mMomentum.x, 1.7f);
        }

        if (mMomentum.y != 0.0f) {
            mMomentum.y = MathUtilities.decrementConvergingValue(mMomentum.y, 0.1f);
        }
    }

    private void updateBackground() {
        mTreeBackground.setShift(mHeight - mMinHeight, mBackgroundShiftFactor, 0.7f);
        mGridBackground.setShift(mHeight - mMinHeight, mGridShiftFactor, mGridTileSize);
    }

    private void updateCameraPosition() {
        Vector3f updPos = mCamera.getPos();
        updPos.y = mHeight;
        updPos.z = mDistance;

        Vector3f updDir = mCamera.getDir();
        updDir.y = mHeight;

        mCamera.setPos(updPos);
        mCamera.setDir(updDir);
        mCamera.updateCamera();
    }


    //</editor-fold>


    //<editor-fold desc="Node processing">
    private void updateNodeStatus(int nodeId, boolean answered) {
        if (nodeId >= 0 && nodeId < mGameSession.tree.getNodesAmount()) {
            if (answered) mGameSession.tree.setNodeCorrect(nodeId);
            else mGameSession.tree.setNodeWrong(mCurrentNode);
        }
    }

    private void openNodeQuestion(int nodeId) {
        if(mGameSession.tree.getNode(nodeId).getState() == Node.NODESTATE.OPEN) {
            //createQuestionWindow(getQuestion(nodeId));
            //createQuestionWindow( mQuestionManager.getUniqueQuestion() );
            mGameContextStack.push( createQuestionContext(mQuestionManager.getUniqueQuestion()) );
            mMediaPlayer.setVolume( mFadedVolume, mFadedVolume );
        }
    }
    //</editor-fold>


    private void createTreeContext() {
        final TouchHandler touchHandler = new TouchHandler() {
            @Override
            public void onSwipeHorizontal(float amount) {
                mMomentum.x = amount;
            }

            @Override
            public void onSwipeVertical(float amount) {
                mMomentum.y = amount;
            }

            @Override
            public void onScale(float span) {
                if (Math.abs(span) > 0.1) mDistance = mDistance - span;
                if (mDistance <= mMinDist) mDistance = mMinDist;
                else if (mDistance > mMaxDist) mDistance = mMaxDist;
            }

            @Override
            public void onTap(TouchRay touchRay) {
                mSoundManager.playRandomClick();
                //mCurrentNode = mGameSession.tree.performRaySelection(new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics));
                mCurrentNode = mGameSession.tree.performRaySelection(touchRay);
                if (mCurrentNode != -1) {
                    openNodeQuestion(mCurrentNode);
                }
            }
        };

        mTreeContext = new GameContext(touchHandler);
        mTreeContext.getRenderContainer().addRenderable(mBackgroundRenderables).addRenderable(mGameSession.tree).addRenderable(mUIRenderables);
    }

    private GameContext createMenuContext() {
        GameContext gameContext = new GameContext();
        gameContext.getRenderContainer().addRenderable(mBackgroundRenderables).addRenderable(mMenuController.getCurrentPage());
        gameContext.addTouchable(mMenuController.getCurrentPage());

        return gameContext;
    }

    private GameContext createQuestionContext(Question question) {
        QuestionWindow questionWindow = new QuestionWindow(10, 10, 2.0f, mCamera, mScreenMetrics);
        questionWindow.addQuestion(question);
        questionWindow.addWindowEventListener(new WindowListener());        //TODO: WTF?
        questionWindow.setOpacity(0.23f);

        GameContext gameContext = new GameContext();
        gameContext.getRenderContainer().addRenderable(mBackgroundRenderables).addRenderable(questionWindow);
        gameContext.addTouchable(questionWindow);

        return gameContext;
    }

    //<editor-fold desc="Questions">
    private void populateQuestionDB() {
        QuestionDBHelper qDBHelper = new QuestionDBHelper(mContext);
        try {
            qDBHelper.openW();
            qDBHelper.addQuestion(getQuestionForDB(0));
            for (int i = 1; i < 32; i++) {
                qDBHelper.addQuestion(getQuestionForDB(i));
            }
            qDBHelper.close();
        } catch (SQLException excep) {
            Log.e("Error", "Database population error");
        }
    }

    private Question getQuestionForDB(int id) {
        String text;
        String[] buttons;
        switch (id) {
            case 1:
                text = "Какой цвет снизу на российском флаге?";
                buttons = new String[] {"Красный", "Белый", "Синий", "Голубой"};
                break;
            case 2:
                text = "Самое быстрое животное в мире";
                buttons = new String[] {"Гепард", "Леопард", "Вилорог", "Заяц русак"};
                break;
            case 3:
                text = "Самая распространенная фамилия России";
                buttons = new String[] {"Смирнов", "Иванов", "Петров", "Сидоров"};
                break;
            case 4:
                text = "Самая ценная порода дерева";
                buttons = new String[] {"Эбеновое", "Бакаут", "Бальзамо", "Зебрано"};
                break;
            case 5:
                text = "Самое древнее животное";
                buttons = new String[] {"Таракан", "Крокодил", "Ехидна", "Муравей"};
                break;
            case 6:
                text = "Сколько материков на земле?";
                buttons = new String[] {"6", "5", "7", "9"};
                break;
            case 7:
                text = "Какая птица каждый день навещала прикованного к скале Прометея?";
                buttons = new String[] {"Орел", "Сокол", "Ворон", "Сова"};
                break;
            case 8:
                text = "Самое большое государство в мире";
                buttons = new String[] {"Россия", "Китай", "Канада", "США"};
                break;
            case 9:
                text = "Сколько суток составляют високосный год?";
                buttons = new String[] {"366", "365", "364", "367"};
                break;
            case 10:
                text = "Сколько раз старик из сказки А. С. Пушкина вызывал Золотую рыбку?";
                buttons = new String[] {"5", "4", "3", "7"};
                break;
            case 11:
                text = "Продукт питания, который не портится";
                buttons = new String[] {"Мёд", "Балык", "Сыр", "Чернослив"};
                break;
            case 12:
                text = "От укусов каких существ погибает больше всего людей?";
                buttons = new String[] {"пчёлы", "змеи", "акулы", "пауки"};
                break;
            case 13:
                text = "На что чаще всего бывает аллергия у людей?";
                buttons = new String[] {"молоко", "шерсть", "цветы", "цитрусы"};
                break;
            case 14:
                text = "Существо с 3 веками";
                buttons = new String[] {"верблюд", "ящерица", "крокодил", "паук"};
                break;
            case 15:
                text = "Сколько литров воды в одном кубическом метре?";
                buttons = new String[] {"1000", "984", "1024", "900"};
                break;
            case 16:
                text = "Кто из этих апостолов был братом Петра?";
                buttons = new String[] {"Андрей", "Павел", "Иоанн", "Матфей"};
                break;
            case 17:
                text = "Какой титул носил белогвардейский генерал Петр Николаевич Врангель?";
                buttons = new String[] {"барон", "граф", "князь", "герцог"};
                break;
            case 18:
                text = "Русская народная мудрость гласит: \"Мила та сторона, где ... резан\"";
                buttons = new String[] {"Пуп", "Свет", "Хлеб", "Дом"};
                break;
            case 19:
                text = "Кого первым встречает героиня сказки \"Гуси-лебеди\", отправившаяся на поиски брата?";
                buttons = new String[] {"Печь", "Река", "Яблоня", "Русалка"};
                break;
            case 20:
                text = "Что выращивали для продажи в столице братья из сказки Ершова \"Конек-горбунок\"?";
                buttons = new String[] {"Пшеницу", "Рожь", "Овёс", "Горох"};
                break;
            case 21:
                text = "Когда был основан Санкт-Петербург?";
                buttons = new String[] {"1703", "1698", "1721", "1802"};
                break;
            case 22:
                text = "Кто из мушкетёров был графом де Ля Фер?";
                buttons = new String[] {"Атос", "Портос", "Арамис", "Д\"артаньян"};
                break;
            case 23:
                text = "С какого знака Зодиака начинается новый астрологический год?";
                buttons = new String[] {"Овен", "Стрелец", "Козерог", "Водолей"};
                break;
            case 24:
                text = "Сколько оборотов делает Земля вокруг своей оси за 24 часа?";
                buttons = new String[] {"1", "4", "7", "30"};
                break;
            case 25:
                text = "Как звали царя в \"Сказке о золотом петушке\"?";
                buttons = new String[] {"Дадон", "Гвидон", "Драдон", "Батый"};
                break;
            case 26:
                text = "Что было на голове человека с улицы Бассейной в известном произведении С. Маршака?";
                buttons = new String[] {"Сковорода", "Ведро", "Горшок", "Кастрюля"};
                break;
            case 27:
                text = "Что означает в переводе с французского название пирожного \"безе\"?";
                buttons = new String[] {"Поцелуй", "Нежный", "Пушистый", "Облако"};
                break;
            case 28:
                text = "Чем Балда воду в море мутил?";
                buttons = new String[] {"Веревкой", "Палкой", "Рукой", "Цепью"};
                break;
            case 29:
                text = "Сколько букв в русском языке?";
                buttons = new String[] {"33", "32", "30", "34"};
                break;
            case 30:
                text = "Где появились самые первые бумажные деньги?";
                buttons = new String[] {"Китай", "Греция", "Рим", "Англия"};
                break;
            case 31:
                text = "Сколько струн у балалайки?";
                buttons = new String[] {"3", "4", "5", "2"};
                break;
            default:
                text = "Древние римляне называли друзей ворами, причем ворующими самое дорогое. Что именно?";
                buttons = new String[] {"Время", "Деньги", "Любовь", "Идеи"};
                break;
        }

        return new Question(text, buttons, 0);
    }

    private Question getQuestion(long id) {
        QuestionDBHelper qDBHelper = new QuestionDBHelper(mContext);
        Question question;
        try {
            qDBHelper.openR();
            question = qDBHelper.getQuestion(id + 1);
            qDBHelper.close();
        } catch (SQLException excep) {
            question = new Question("Errors have occured\nWe won't tell you where or why\nLazy programmers",
                    new String[] {"CLOSE", "BLANK", "BLANK", "BLANK"}, 0);
        }
        question.shuffleAnswers();
        return question;
    }
    //</editor-fold>



    public void closeMenu() {
        Log.e("Debug", "menu closed");
        mGameContextStack.push(mTreeContext);
    }


    public void onSwipeHorizontal(float amount) {
        mGameContextStack.peek().onSwipeHorizontal(amount);
    }

    public void onSwipeVertical(float amount) {
        mGameContextStack.peek().onSwipeVertical(amount);
    }

    public void onScale(float span) {
        mGameContextStack.peek().onScale(span);
    }

    public void onTap(float x, float y) {
        TouchRay touchRay = new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics);
        mGameContextStack.peek().onTap(touchRay);
    }


    public void update() {
        if (mMomentum.nonZero()) {
            updateHeight();
            updateBackground();
            updateAngle();
            updateMomentum();
        }

        updateCameraPosition();
    }

}
