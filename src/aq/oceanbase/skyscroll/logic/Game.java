package aq.oceanbase.skyscroll.logic;

import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.render.RenderContainer;
import aq.oceanbase.skyscroll.graphics.primitives.Background;
import aq.oceanbase.skyscroll.graphics.windows.Button;
import aq.oceanbase.skyscroll.graphics.windows.Window;
import aq.oceanbase.skyscroll.logic.tree.nodes.Node;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.utils.math.MathMisc;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import android.text.format.Time;
import java.util.Calendar;

public class Game {

    public static enum MODE {
        TREE, QUESTION
    }

    private MODE mDrawmode = MODE.TREE;
    private boolean mModeSwitched = false;

    private int[] mScreenMetrics;

    public RenderContainer mCurrentRenderables;
    private RenderContainer mTreeRenderables = new RenderContainer();
    private RenderContainer mQuestionRenderables = new RenderContainer();

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 8.0f;
    private float mMaxDist = 30.0f;

    private int mBlinkTime = 800;
    private long mTimer = -1;

    //Tree
    public GameSession mGameSession;

    private int mCurrentNode;

    //Windows
    public Window mWindow;

    //Backgrounds
    private Background mCurrentBackground;
    public Background mTreeBackground = new Background(R.drawable.bckgnd1);
    private Background mQuestionBackground = mTreeBackground;


    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;

    private Camera mCamera = new Camera(new Vector3f(0.0f, 8.0f, mDistance),
            new Vector3f(0.0f, 0.0f, -1.0f),
            new Vector3f(0.0f, 1.0f, 0.0f));

    //Touch variables
    public TouchHandler mTouchHandler;
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
        public void onTap(float x, float y) {
            mCurrentNode = mGameSession.tree.performRaySelection(new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics));
            //Log.e("Draw", new StringBuilder().append("Pos: ").append(x).append(" ").append(y).toString());
            if (mCurrentNode != -1) {
                if(mGameSession.tree.getNode(mCurrentNode).getState() == Node.NODESTATE.OPEN) {
                    createWindow(getQuestion(mCurrentNode));
                    switchMode(MODE.QUESTION);
                }

            }
        }
    };

    private final TouchHandler mWindowTouchHandler = new TouchHandler() {
        @Override
        public void onSwipeVertical(float amount) {
            mWindow.scrollContent((int)(-amount * 40));
        }

        @Override
        public void onTap(float x, float y) {
            Button.STATE answered;
            answered = mWindow.pressButton((int) x, (int) y, mCamera, mScreenMetrics);
            if (mCurrentNode >= 0 && mCurrentNode < mGameSession.tree.getNodesAmount()) {
                /*if (answered) mGameSession.tree.getNode(mCurrentNode).setState(Node.NODESTATE.RIGHT);
                else mGameSession.tree.getNode(mCurrentNode).setState(Node.NODESTATE.WRONG);*/
                if (answered == Button.STATE.RIGHT) mGameSession.tree.setNodeRight(mCurrentNode);
                else if (answered == Button.STATE.WRONG) mGameSession.tree.setNodeWrong(mCurrentNode);
            }

            if (answered != Button.STATE.NEUTRAL) {
                Time now = new Time();
                now.setToNow();
                mTimer = now.toMillis(false);
                Log.e("Debug", new StringBuilder("Set").append(mTimer).toString());
            }

            /*
            killWindow();
            mWindow = null;

            switchMode(MODE.TREE);
            */
        }
    };


    public Game() {
        mGameSession = new GameSession();
        //mCurrentBackground = mTreeBackground;

        mTreeRenderables.addRenderable(mTreeBackground).addRenderable(mGameSession.tree);
        setMode(MODE.TREE);
    }

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

    public void setScreenMetrics(int[] screenMetrics) {
        this.mScreenMetrics = screenMetrics;
    }

    public RenderContainer getRenderables() {
        return mCurrentRenderables;
    }


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
            mMomentum.x = MathMisc.decrementConvergingValue(mMomentum.x, 1.7f);
        }

        if (mMomentum.y != 0.0f) {
            mMomentum.y = MathMisc.decrementConvergingValue(mMomentum.y, 0.1f);
        }
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


    private Question getQuestion(int id) {
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



    private void createWindow(Question question) {
        mQuestionRenderables.clear();
        mWindow = new Window(20, 100, 2.0f, mCamera, mScreenMetrics);
        mWindow.setQuestion(question);
        mQuestionRenderables.addRenderable(mTreeBackground).addRenderable(mWindow);
    }

    private void killWindow() {
        mQuestionRenderables.clear();
    }


    private void setMode(MODE mode) {
        switch (mode) {
            case TREE:
                mCurrentRenderables = mTreeRenderables;
                mTouchHandler = mTreeTouchHandler;
                break;
            case QUESTION:
                mCurrentRenderables = mQuestionRenderables;
                mTouchHandler = mWindowTouchHandler;
                break;
        }

        mDrawmode = mode;
    }

    private void switchMode (MODE mode) {
        /*if (mode == MODE.QUESTION)
            createWindow(this.getQuestion(1));
        else {
            killWindow();
            mWindow = null;
        }*/


        setMode(mode);
    }

    public void update() {
        if (mMomentum.nonZero()) {
            updateHeight();
            updateAngle();
            updateMomentum();
        }

        updateCameraPosition();

        //Log.e("Debug", new StringBuilder().append(Calendar.getInstance().get(Calendar.MILLISECOND) - mTimer).toString());
        //Log.e("Debug", new StringBuilder().append(mTimer).toString());

        if (mTimer != -1) {
            Time now = new Time();
            now.setToNow();
            if (now.toMillis(false) - mTimer > mBlinkTime) {
                killWindow();
                mWindow = null;

                switchMode(MODE.TREE);

                mTimer = -1;
            }
        }
    }

}
