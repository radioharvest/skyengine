package aq.oceanbase.skyscroll.game.questions;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.data.QuestionDBHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionManager {
    public static int K_INVALID_QUESTION_ID = -1;

    private Context mContext;
    private QuestionDBHelper mDBHelper;

    private List<Long> mAvailableIdList = new ArrayList<>();
    private Random mGenerator;

    public QuestionManager(Context context) {
        this.mContext = context;
        mDBHelper = new QuestionDBHelper(mContext);

        mGenerator = new Random();

        populateQuestionDB();
        populateAvailableIdList();
    }

    private void populateAvailableIdList() {
        long questionAmount = 46;

        /*try {
            mDBHelper.openR();
            questionAmount = mDBHelper.getEntryCount();
            mDBHelper.close();
        } catch (SQLException excep) {
            questionAmount = 0;
        }*/

        Log.e("Debug", "questionsAmount: " + questionAmount);

        for (long i = 0; i < questionAmount; i++) {
            mAvailableIdList.add(i);
        }
    }

    private void populateQuestionDB() {
        QuestionDBHelper qDBHelper = new QuestionDBHelper(mContext);
        try {
            qDBHelper.openW();
            qDBHelper.addQuestion(getQuestionForDB(0));
            for (int i = 1; i < 46; i++) {
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
            case 32:
                text = "Самайнофобия - это";
                buttons = new String[] {"боязнь Хэллоуина", "боязнь Рождества", "боязнь самолетов", "боязнь света"};
                break;
            case 33:
                text = "Самая большая организация по защите окружающей среды";
                buttons = new String[] {"Greenpeace", "Greenday", "Whitepeace", "Peace"};
                break;
            case 34:
                text = "Построенный в 1886 году Ванкувер носит имя офицера Британского морского флота";
                buttons = new String[] {"Джорджа", "Джеймса", "Джимми", "Джона"};
                break;
            case 35:
                text = "Первый прототип танка в Первой Мировой войне назывался";
                buttons = new String[] {"Малыш Вилли", "Джорджини", "Билл", "Крейсер"};
                break;
            case 36:
                text = "Самая популярная фамилия в Пекине на 2006 год";
                buttons = new String[] {"Ван", "Ся", "Лю", "Цинь"};
                break;
            case 37:
                text = "Сколько углов у снежинки?";
                buttons = new String[] {"6", "4", "8", "нет точного ответа"};
                break;
            case 38:
                text = "Какого цвета кровь у тараканов?";
                buttons = new String[] {"Белого", "Красного", "Черного", "Жёлтого"};
                break;
            case 40:
                text = "Страна, полностью занимающая весь континент";
                buttons = new String[] {"Австралия", "Австрия", "Япония", "Иран"};
                break;
            case 41:
                text = "Какого цвета шуба у русского Деда Мороза?";
                buttons = new String[] {"Синего", "Красного", "Белого", "Зеленого"};
                break;
            case 42:
                text = "Самый большой потребитель молока?";
                buttons = new String[] {"Финляндия", "Франция", "Норвегия", "Германия"};
                break;
            case 43:
                text = "Приблизительный возраст Земли?";
                buttons = new String[] {"4,5 млд лет", "2,5 млд лет", "100 млн лет", "10 млд лет"};
                break;
            case 44:
                text = "Любимое блюдо Карла Великого?";
                buttons = new String[] {"Сыр", "Творог", "Пирог", "Колбаса"};
                break;
            case 45:
                text = "Первая книга, напечатанная в Англии, была посвящена..";
                buttons = new String[] {"шахматам", "картам", "путешествиям", "природе"};
                break;
            default:
                text = "Древние римляне называли друзей ворами, причем ворующими самое дорогое. Что именно?";
                buttons = new String[] {"Время", "Деньги", "Любовь", "Идеи"};
                break;
        }

        return new Question(text, buttons, 0);
    }

    public long getUniqueQuestionId() {
        if ( mAvailableIdList.isEmpty() ) {
            return K_INVALID_QUESTION_ID;
        }

        int index = mGenerator.nextInt(mAvailableIdList.size());
        long id = mAvailableIdList.get(index);
        mAvailableIdList.remove(index);

        Log.e("Debug", "Chosen index: " + index + " chosen id: " + id);

        if (mAvailableIdList.isEmpty()) {
            populateAvailableIdList();
        }

        return id;
    }

    public Question getUniqueQuestion() {
        return getQuestion(getUniqueQuestionId());
    }

    public Question getQuestion(long id) {
        Question question;
        try {
            mDBHelper.openR();
            question = mDBHelper.getQuestion(id + 1);
            mDBHelper.close();
        } catch (SQLException excep) {
            question = new Question("Errors have occured\nWe won't tell you where or why\nLazy programmers",
                    new String[] {"CLOSE", "BLANK", "BLANK", "BLANK"}, 0);
        }
        question.shuffleAnswers();
        return question;
    }
}
