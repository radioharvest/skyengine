package aq.oceanbase.skyscroll.data;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.logic.enums.ESound;

import java.util.HashMap;
import java.util.Random;

public class SoundManager {
    /*private final int sClick1 = R.raw.sky_loop_click1;
    private final int sClick2 = R.raw.sky_loop_click2;
    private final int sClick3 = R.raw.sky_loop_click3;
    private final int sClick4 = R.raw.sky_loop_click4;
    private final int sClick5 = R.raw.sky_loop_click5;*/

    private final int sClick1 = 0;
    private final int sClick2 = 1;
    private final int sClick3 = 2;
    private final int sClick4 = 3;
    private final int sClick5 = 4;

    private Context mContext;

    private Random mGenerator;

    private SoundPool mSoundPool;
    private HashMap mSoundMap;

    private float mVolume = 1.0f;

    public SoundManager(Context context) {
        mContext = context;
        mGenerator = new Random();

        initSounds();
    }

    public void initSounds() {
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);

        mSoundMap = new HashMap(5);
        mSoundMap.put(sClick1, mSoundPool.load(mContext, R.raw.sky_loop_click1, 1));
        mSoundMap.put(sClick2, mSoundPool.load(mContext, R.raw.sky_loop_click2, 2));
        mSoundMap.put(sClick3, mSoundPool.load(mContext, R.raw.sky_loop_click3, 3));
        mSoundMap.put(sClick4, mSoundPool.load(mContext, R.raw.sky_loop_click4, 4));
        mSoundMap.put(sClick5, mSoundPool.load(mContext, R.raw.sky_loop_click5, 5));
    }

    public void playRandomClick() {
        int id = mGenerator.nextInt(5);
        playSound(id);
    }

    public void playSound(int id) {
        if (mSoundPool == null || mSoundMap == null) {
            initSounds();
        }

        mSoundPool.play((int)mSoundMap.get(id), mVolume, mVolume, 1, 0, 1f);
    }
}
