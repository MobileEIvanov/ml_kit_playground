package examples.com.mlkitplayground;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by emil.ivanov on 7/21/18.
 */
public class UtilsImageLoader {


    public static void loadImage(Context context, ImageView imageView, Uri uri) {
        if (context == null || imageView == null || uri == null) {
            return;
        }
        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed()) {
                return;
            }
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.no_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(context)
                .load(uri)
                .apply(options)
                .into(imageView);
    }
}
