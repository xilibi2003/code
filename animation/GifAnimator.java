
package com.hello;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import java.io.InputStream;

public class GifAnimator extends View {
    InputStream is = null;
    InputStream is1 = null;
    Movie movie;
    long moviestart;

    public GifAnimator(Context context) {
        super(context);
        init(context);
    }

    public GifAnimator(Context paramContext, AttributeSet attr) {
        super(paramContext, attr);
        init(paramContext);
    }

    public GifAnimator(Context paramContext, AttributeSet attr, int def) {
        super(paramContext, attr, def);
        init(paramContext);
    }

    private void init(Context paramContext) {
        // load a gif pic.
        is = paramContext.getResources().openRawResource(R.raw.alert_bell_animation_bl);
        movie = Movie.decodeStream(is);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = SystemClock.uptimeMillis();
        if (moviestart == 0L)
            moviestart = now;

        int relTime = (int) ((now - moviestart) % movie.duration());
        movie.setTime(relTime);
        movie.draw(canvas, 0.0F, 0.0F);
        invalidate();
    }

}
