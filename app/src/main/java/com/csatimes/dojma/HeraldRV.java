package com.csatimes.dojma;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.Tracker;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Vikramaditya Kukreja on 19-06-2016.
 */

public class HeraldRV extends RecyclerView.Adapter<HeraldRV.ViewHolder> implements
        ItemTouchHelperAdapter, View.OnClickListener {
    private Context context;
    private RealmList<HeraldNewsItemFormat> resultsList;
    private int pixels = DHC.dpToPx(25);
    private Realm database;
    private int dismissPosition;
    private Tracker mTracker;
    private boolean isGoogleChromeInstalled = false;
    private CustomTabsIntent customTabsIntent;
    private Activity activity;

    public HeraldRV(Context context, RealmList<HeraldNewsItemFormat> resultsList, Realm
            database, Activity activity) {
        this.context = context;
        this.resultsList = resultsList;
        this.database = database;
        this.activity = activity;
        Fresco.initialize(context);
        //copy link
//        Intent copy_intent = new Intent(this, CopyLinkBroadcastReceiver.class);
//        String copy_label = "Copy Link";
//        PendingIntent copy_pendingIntent = PendingIntent.getBroadcast(this, 0, copy_intent, PendingIntent.FLAG_UPDATE_CURRENT);


        customTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(context, R.color.blue500))
                .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable
                        .ic_arrow_back))
                .addDefaultShareMenuItem()
                // .addMenuItem(copy_label, copy_pendingIntent)
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slid_out_right)
                .setExitAnimations(context, R.anim.slide_in_right, R.anim.slid_out_right)
                .setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.amber500))
                .enableUrlBarHiding().build();


//        AnalyticsApplication application = (AnalyticsApplication) getActivity()
//                .getApplication();
//        mTracker = application.getDefaultTracker();
//        mTracker.setScreenName("Herald");
//        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void setGoogleChromeInstalled(boolean isGoogleChromeInstalled) {
        this.isGoogleChromeInstalled = isGoogleChromeInstalled;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View herald_card_view_format = inflater.inflate(R.layout.herald_card_view_format, parent, false);
        // Return a new holder instance
        return new ViewHolder(herald_card_view_format);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HeraldNewsItemFormat foobar = resultsList.get(position);
        final int pos = position;

        holder.date.setText(foobar.getOriginalDate());
        holder.author.setText(foobar.getAuthor());
        holder.title.setText(foobar.getTitle());

        if (foobar.isFav())
            holder.fav.setLiked(true);
        else holder.fav.setLiked(false);

        if (foobar.isRead()) {
            // holder.card.setCardBackgroundC(ContextCompat.getColor(context,R.color
            //       .cardview_shadow_end_color));

        }

        if (foobar.getDesc() == null) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(resultsList.get(position).getLink(), new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    database.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            HeraldNewsItemFormat foo = bgRealm.where(HeraldNewsItemFormat.class)
                                    .equalTo("postID", resultsList.get(pos).getPostID())
                                    .findFirst();
                            Document bar = Jsoup.parse(response);
                            Elements _foobar = bar.getElementsByTag("p");
                            if (_foobar.first().hasText()) {
                                StringBuilder sb = new StringBuilder(_foobar.first().text());
                                if (sb.length() < 100) {
                                    sb.append(" ... ");
                                    if (_foobar.last().hasText())
                                        sb.append(_foobar.last().text());
                                }
                                foo.setDesc(sb.toString());
                            } else {
                                foo.setDesc(foo.getTitle());
                            }
                        }
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(request);
        } else {
            holder.desc.setText(foobar.getDesc());
        }
        holder.imageView.setImageURI(Uri.parse(foobar.getImageURL())
        );

    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT <= 22) {
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
            return haveConnectedWifi || haveConnectedMobile;
        } else {

        }
        return false;
    }

    @Override
    public void onItemDismiss(final int position, RecyclerView rv) {
        database.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                HeraldNewsItemFormat _lolwa = database.where(HeraldNewsItemFormat.class).equalTo
                        ("postID", resultsList.get(position).getPostID()).findFirst();
                _lolwa.setDismissed(true);
            }
        });
        dismissPosition = position;
        this.resultsList.remove(position);
        this.notifyItemRemoved(position);
        Snackbar.make(rv, "Article dismissed", Snackbar.LENGTH_LONG).setAction("UNDO", this).show();

    }


    @Override
    public void onClick(View view) {

        database.beginTransaction();

        HeraldNewsItemFormat _lolwa = database.where(HeraldNewsItemFormat.class).equalTo
                ("postID", resultsList.get(dismissPosition).getPostID()).findFirst();
        _lolwa.setDismissed(false);

        database.commitTransaction();

        resultsList.add(dismissPosition, _lolwa);
        notifyItemInserted(dismissPosition);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public SimpleDraweeView imageView;
        public TextView title;
        public TextView author;
        public TextView date;
        public TextView desc;
        public LikeButton fav;
        public CardView card;

        public ViewHolder(final View itemView) {
            super(itemView);
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.herald_rv_item_image);
            author = (TextView) itemView.findViewById(R.id.herald_rv_item_author);
            date = (TextView) itemView.findViewById(R.id.herald_rv_item_date);
            title = (TextView) itemView.findViewById(R.id.herald_rv_item_title);
            desc = (TextView) itemView.findViewById(R.id.herald_rv_desc);
            fav = (LikeButton) itemView.findViewById(R.id.herald_like_button);
            card = (CardView) itemView;
            itemView.setOnClickListener(this);
            fav.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    database.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            HeraldNewsItemFormat foo = realm.where(HeraldNewsItemFormat.class)
                                    .equalTo("postID", resultsList.get(getAdapterPosition()).getPostID
                                            ()).findFirst();
                            foo.setFav(true);
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    database.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            HeraldNewsItemFormat foo = realm.where(HeraldNewsItemFormat.class)
                                    .equalTo("postID", resultsList.get(getAdapterPosition()).getPostID
                                            ()).findFirst();
                            foo.setFav(false);
                        }
                    });
                }
            });
        }


        @Override
        public void onClick(View view) {
            {
                if (view.getId() == itemView.getId()) {
                    /*if (isOnline())*/
                    {

                        Intent openWebpage = null;
                        if (isGoogleChromeInstalled) {
                            //openWebpage = new Intent(context, ChromeCustomTab.class);
                            CustomTabActivityHelper.openCustomTab(activity, customTabsIntent,
                                    Uri
                                            .parse(resultsList.get(getAdapterPosition()).getLink()),
                                    new CustomTabActivityHelper.CustomTabFallback() {
                                        @Override
                                        public void openUri(Activity activity, Uri uri) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            context.startActivity(intent);
                                        }
                                    });
                        } else {
                            openWebpage = new Intent(context, OpenWebpage.class);
                            openWebpage.putExtra("URL", resultsList.get(getAdapterPosition())
                                    .getLink());
                            openWebpage.putExtra("TITLE", resultsList.get(getAdapterPosition()).getTitle
                                    ());
                            openWebpage.putExtra("POSTID", resultsList.get(getAdapterPosition()).getPostID
                                    ());

                            context.startActivity(openWebpage);
                        }
                    }
                }

            }

        }
    }
}
